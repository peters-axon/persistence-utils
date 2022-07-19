package com.axonivy.utils.persistence.dao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceException;
import javax.persistence.Version;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.transaction.TransactionRolledbackException;

import org.hibernate.Hibernate;
import org.hibernate.Transaction;
import org.hibernate.jpa.HibernateEntityManager;
import org.hibernate.proxy.HibernateProxy;

import com.axonivy.utils.persistence.IvyEntityManager;
import com.axonivy.utils.persistence.ReflectionUtilitities;
import com.axonivy.utils.persistence.annotations.CascadeCopy;
import com.axonivy.utils.persistence.logging.Logger;

/**
 * Base class for all persistent beans.
 */
public abstract class AbstractDAO implements BaseDAO {

	private static final ThreadLocal<ManagedTransaction> threadLocalTransaction = new ThreadLocal<>();
	// hold an additional map of transactions which is only used for logging and
	// debugging purposes
	private static final Map<Thread, ManagedTransaction> currentTransactions = Collections
			.synchronizedMap(new HashMap<>());

	private static final Logger LOG = Logger.getLogger(AbstractDAO.class);

	private static final String INDENT_INC = "    ";

	/**
	 * limit the expressions count in the queries, as usually there is a fix limit
	 * which DB supports (usually around 1000)
	 */
	public static final int MAX_VARIABLES_IN_HQL_QUERY_COUNT = 990;

	/**
	 * public constructor
	 */
	public AbstractDAO() {

	}

	/**
	 * Convenience function to get {@link HibernateEntityManager}.
	 *
	 * @return singleton EntityManager
	 */
	protected HibernateEntityManager getEM() {
		return IvyEntityManager.getInstance().getIvyEntityManager(getPersistenceUnitName(), new HashMap<String, Object>());
	}

	/**
	 * Function needed mainly for testing or showing system information.
	 * 
	 * @param propertyKey this is connection url
	 *
	 * @return value of the key in the emf properties map
	 */
	public Object getEntityManagerProperty(String propertyKey) {
		return getEM().getEntityManagerFactory().getProperties().get(propertyKey);
	}

	/**
	 * Begin session of ivy entity manager
	 * 
	 * @return autocloseable instance which can be used in e.g. try with resources
	 */
	public AutoCloseTransaction beginSession() {
		return IvyEntityManager.getInstance().beginSession();
	}

	/**
	 * Close session of ivy entity manager
	 */
	public void closeSession() {
		IvyEntityManager.getInstance().closeSession();
	}

	/**
	 * Start a database transaction.
	 *
	 * Note: before a transaction you should open a session!
	 *
	 * @throws TransactionRolledbackException this exception must be thrown when 
	 * a call to Session.commit results in a rollback of the current transaction 
	 */
	public void beginTransaction() throws TransactionRolledbackException {
		ManagedTransaction ta = threadLocalTransaction.get();
		if (ta == null) {
			ta = new ManagedTransaction();
			threadLocalTransaction.set(ta);
			currentTransactions.put(Thread.currentThread(), ta);
		}

		ta.begin();
	}

	/**
	 * Start a database transaction which can be autoclosed.
	 *
	 * If not all of the isTransactionErrors given to the transaction are set to
	 * <code>false</code>, then the autoclose will cause a rollback, otherwise a
	 * commit.
	 *
	 * Note: before a transaction you should open a session!
	 *
	 * @param isTransactionError array which is tested for errors, if any error
	 *                           found a rollback is triggered
	 * @return AutoCloseTransaction instance which can be used in try with resources
	 * @throws TransactionRolledbackException this exception must be thrown when 
	 * a call to Session.commit results in a rollback of the current transaction
	 */
	public AutoCloseTransaction beginTransaction(Boolean[] isTransactionError) throws TransactionRolledbackException {
		if (isTransactionError == null || isTransactionError.length < 1) {
			throw new IllegalArgumentException(
					"isTransactionError input is empty, it needs at least an array of 1 Boolean ");
		}

		beginTransaction();

		if (isTransactionError.length > 0 && isTransactionError[0] != null) {// there is a specified flag instance reset
			// it, if it is null it means we dont
			// need to support autoclose
			isTransactionError[0] = true;// per default set error to true, it has to be cleared outside, so that a
			// commit can be done
		}

		return () -> {
			if (Arrays.stream(isTransactionError).filter(Objects::nonNull).anyMatch(b -> b.equals(Boolean.TRUE))) {
				rollbackTransaction();
				throw new TransactionRolledbackException(
						"isTransactionError flag was set, rollback of transaction was done");
			} else {
				commitTransaction();
			}
		};

	}

	/**
	 * Commit an ongoing transaction, do not do anything if no transaction is
	 * started
	 * 
	 * @throws TransactionRolledbackException this exception must be thrown when 
	 * a call to Session.commit results in a rollback of the current transaction
	 */
	public void commitTransaction() throws TransactionRolledbackException {
		ManagedTransaction ta = threadLocalTransaction.get();
		if (ta != null && ta.commit() <= 0) {
			threadLocalTransaction.remove();
			currentTransactions.remove(Thread.currentThread());
		}
	}

	/**
	 * Rollback an ongoing transaction, do not do anything if no transaction is
	 * started
	 */
	public void rollbackTransaction() {
		ManagedTransaction ta = threadLocalTransaction.get();
		if (ta != null) {
			ta.rollback();
			threadLocalTransaction.remove();
			currentTransactions.remove(Thread.currentThread());
		}
	}

	/**
	 * Inner class defining transaction contexts Counts open transactions, close
	 * after closeCount reached
	 *
	 */
	private class ManagedTransaction {
		private Transaction transaction;
		private int count = 0;
		private boolean isActive = false;

		protected ManagedTransaction() {
		}

		/**
		 * Begin a potentially nested transaction
		 * 
		 * @throws TransactionRolledbackException
		 */
		public void begin() throws TransactionRolledbackException {
			LOG.debug("transaction begin (nesting: {0} active: {1} thread: {2})", count + 1, isActive,
					Thread.currentThread().getId());
			if (count > 0 && !isActive) {
				throw new TransactionRolledbackException("Transaction was rolled back");
			}
			if (count++ == 0) {
				transaction = getEM().getSession().getTransaction();
				transaction.begin();
				isActive = true;
			}
		}

		/**
		 * Commit a transaction if there is no nested transaction
		 * 
		 * @return int number of active transaction levels
		 * @throws TransactionRolledbackException
		 */
		public int commit() throws TransactionRolledbackException {
			LOG.debug("transaction commit (nesting: {0} active: {1} thread: {2})", count, isActive,
					Thread.currentThread().getId());
			if (count > 0 && !isActive) {
				throw new TransactionRolledbackException("Transaction was rolled back");
			}
			if (--count <= 0) {
				LOG.debug("committing to database {0}", Thread.currentThread().getId());
				transaction.commit();
				isActive = false;
			}
			return count;
		}

		/**
		 * Rollback all transactions
		 */
		public void rollback() {
			LOG.debug("transaction rollback (nesting: {0} active: {1} thread: {2})", count, isActive,
					Thread.currentThread().getId());
			if (isActive) {
				transaction.rollback();
			}
			isActive = false;
			count = 0;
		}

		@Override
		public String toString() {
			return String.format(
					"active: %b, ref-count: %d, Transaction: [active: %b, status: %s, timeout: %d]",
					isActive, count, transaction.isActive(), transaction.getStatus().name(),
					transaction.getTimeout());
		}
	}

	/**
	 * getter for all current transactions in a map (key is the thread).
	 *
	 * @return the current transactions
	 */
	public static Map<Thread, ManagedTransaction> getCurrentTransactions() {
		return currentTransactions;
	}

	/**
	 * Unproxy a Hibernate entity proxy.
	 * 
	 * @param entity represents a bean
	 * @return unproxified object
	 */
	@SuppressWarnings("unchecked")
	public <T> T unproxy(T entity) {
		if (entity != null && entity instanceof HibernateProxy) {
			return (T) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
		}
		return entity;
	}

	/**
	 * Unproxy the Hibernate entity and initiate all direct fields.
	 * 
	 * @param entity represents a bean
	 * @return unproxified initialized object
	 */
	@SuppressWarnings("unchecked")
	public static <T> T unproxyAndInitialize(T entity) {
		if (entity != null && entity instanceof HibernateProxy) {
			HibernateProxy proxy = (HibernateProxy) entity;
			Hibernate.initialize(proxy);
			return (T) proxy.getHibernateLazyInitializer().getImplementation();
		}
		return entity;
	}

	/**
	 * Initialize all direct fields of an Hibernate proxy entity
	 * 
	 * @param entity represents a bean
	 */
	public void initialize(Object entity) {
		if (entity != null && entity instanceof HibernateProxy) {
			Hibernate.initialize(entity);
		}
	}


	/**
	 * Remove entity from EntityManager cache.
	 * 
	 * @param entity represents a bean
	 */
	public void evict(Object entity) {
		getEM().detach(entity); // or use hibernate getEM().getSession().evict()
	}

	/**
	 * Construct expression for a series of joins.
	 *
	 * @param expressionMap store all {@link Join}s of a query
	 * @param root a root type in the from clause
	 * @param attrA represents an attribute of a Java type
	 * @param attrB represents an attribute of a Java type
	 * @param attrC represents an attribute of a Java type
	 * @param attrD represents an attribute of a Java type
	 * @param attrE represents an attribute of a Java type
	 * @param <A> the represented type that contains the attribute
	 * @param <B> the type of the represented attribute or
	 * the type the represented collection belongs to
	 * @param <C> the element type of the represented collection or 
	 * the type the represented collection belongs to
	 * @param <D> the type the represented collection belongs to or
	 * the represented type that contains the attribute
	 * @param <E> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <F> the type of the expression or 
	 * the type of the represented attribute
	 * @param <X> The type of the represented collection
	 * @param <Y> The type of the represented collection
	 * @return expression which is a chain of all the attributes beginning from root
	 */
	@SuppressWarnings("unchecked")
	public static <A, B, C, D, E, F, X, Y> Expression<F> getExpression(ExpressionMap expressionMap, Root<A> root,
			Attribute<? super A, B> attrA, PluralAttribute<? super B, X, C> attrB,
			PluralAttribute<? super C, Y, D> attrC, Attribute<? super D, E> attrD, Attribute<? super E, F> attrE) {
		return (Expression<F>) getExpressionInternal(expressionMap, root, attrA, attrB, attrC, attrD, attrE);
	}

	/**
	 * Construct expression for a series of joins.
	 *
	 * @param expressionMap store all {@link Join}s of a query
	 * @param root a root type in the from clause
	 * @param attrA represents an attribute of a Java type
	 * @param attrB represents an attribute of a Java type
	 * @param attrC represents an attribute of a Java type
	 * @param attrD represents an attribute of a Java type
	 * @param attrE represents an attribute of a Java type
	 * @param <A> the represented type that contains the attribute
	 * @param <B> the type of the represented attribute or
	 * the type the represented collection belongs to
	 * @param <C> the element type of the represented collection or 
	 * the represented type that contains the attribute
	 * @param <D> the type of the represented attribute or
	 * the type the represented collection belongs to
	 * @param <E> the element type of the represented collection or 
	 * the represented type that contains the attribute
	 * @param <F> the type of the expression or 
	 * the type of the represented attribute
	 * @param <X> The type of the represented collection
	 * @param <Y> The type of the represented collection
	 * @return expression which is a chain of all the attributes beginning from root
	 */
	@SuppressWarnings("unchecked")
	public static <A, B, C, D, E, F, X, Y> Expression<F> getExpression(ExpressionMap expressionMap, Root<A> root,
			Attribute<? super A, B> attrA, PluralAttribute<? super B, X, C> attrB, Attribute<? super C, D> attrC,
			PluralAttribute<? super D, Y, E> attrD, Attribute<? super E, F> attrE) {
		return (Expression<F>) getExpressionInternal(expressionMap, root, attrA, attrB, attrC, attrD, attrE);
	}

	/**
	 * Construct expression for a series of joins.
	 *
	 * @param expressionMap	store all {@link Join}s of a query
	 * @param from a root type in the from clause
	 * @param attribute1 represents an attribute of a Java type
	 * @param attribute2 represents an attribute of a Java type
	 * @param attribute3 represents an attribute of a Java type
	 * @param attribute4 represents an attribute of a Java type
	 * @param <A> the represented type that contains the attribute
	 * @param <B> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <C> the type of the represented attribute or
	 * the type the represented collection belongs to
	 * @param <D> the element type of the represented collection or 
	 * the represented type that contains the attribute
	 * @param <E> the type of the expression or 
	 * the type of the represented attribute
	 * @param <X> The type of the represented collection
	 * @return chained expression
	 */
	@SuppressWarnings("unchecked")
	public static <X, A, B, C, D, E> Expression<E> getExpression(ExpressionMap expressionMap, Root<A> from,
			Attribute<? super A, B> attribute1, Attribute<? super B, C> attribute2,
			PluralAttribute<? super C, X, D> attribute3, Attribute<? super D, E> attribute4) {
		return (Expression<E>) getExpressionInternal(expressionMap, from, attribute1, attribute2, attribute3,
				attribute4);
	}

	/**
	 * Construct expression for a series of joins.
	 *
	 * @param expressionMap store all {@link Join}s of a query
	 * @param from a root type in the from clause
	 * @param attribute1 represents an attribute of a Java type
	 * @param attribute2 represents an attribute of a Java type
	 * @param attribute3 represents an attribute of a Java type
	 * @param attribute4 represents an attribute of a Java type
	 * @param attribute5 represents an attribute of a Java type
	 * @param attribute6 represents an attribute of a Java type
	 * @param <A> the represented type that contains the attribute
	 * @param <B> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <C> the type of the represented attribute or
	 * the type the represented collection belongs to
	 * @param <D> the element type of the represented collection or 
	 * the represented type that contains the attribute
	 * @param <E> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <F> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <G> the type of the expression or 
	 * the type of the represented attribute
	 * @param <X> The type of the represented collection
	 * @return chained expression
	 */
	@SuppressWarnings("unchecked")
	public static <X, A, B, C, D, E, F, G> Expression<G> getExpression(ExpressionMap expressionMap, Root<A> from,
			Attribute<? super A, B> attribute1, Attribute<? super B, C> attribute2,
			PluralAttribute<? super C, X, D> attribute3, Attribute<? super D, E> attribute4,
			Attribute<? super E, F> attribute5, Attribute<? super F, G> attribute6) {
		return (Expression<G>) getExpressionInternal(expressionMap, from, attribute1, attribute2, attribute3,
				attribute4, attribute5, attribute6);
	}

	/**
	 * Construct expression for a series of joins.
	 *
	 * @param expressionMap store all {@link Join}s of a query
	 * @param from a root type in the from clause
	 * @param attribute1 represents an attribute of a Java type
	 * @param attribute2 represents an attribute of a Java type
	 * @param attribute3 represents an attribute of a Java type
	 * @param attribute4 represents an attribute of a Java type
	 * @param attribute5 represents an attribute of a Java type
	 * @param attribute6 represents an attribute of a Java type
	 * @param attribute7 represents an attribute of a Java type
	 * @param <A> the represented type that contains the attribute
	 * @param <B> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <C> the type of the represented attribute or
	 * the type the represented collection belongs to
	 * @param <D> the element type of the represented collection or 
	 * the represented type that contains the attribute
	 * @param <E> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <F> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <G> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <H> the type of the expression or 
	 * the type of the represented attribute
	 * @param <X> The type of the represented collection
	 * @return chained expression
	 */
	@SuppressWarnings("unchecked")
	public static <X, A, B, C, D, E, F, G, H> Expression<H> getExpression(ExpressionMap expressionMap, Root<A> from,
			Attribute<? super A, B> attribute1, Attribute<? super B, C> attribute2,
			PluralAttribute<? super C, X, D> attribute3, Attribute<? super D, E> attribute4,
			Attribute<? super E, F> attribute5, Attribute<? super F, G> attribute6,
			Attribute<? super G, H> attribute7) {
		return (Expression<H>) getExpressionInternal(expressionMap, from, attribute1, attribute2, attribute3,
				attribute4, attribute5, attribute6, attribute7);
	}

	/* SAPInvoicePosition Expression start */
	/**
	 * @param expressionMap store all {@link Join}s of a query
	 * @param from a root type in the from clause
	 * @param attribute1 represents an attribute of a Java type
	 * @param attribute2 represents an attribute of a Java type
	 * @param attribute3 represents an attribute of a Java type
	 * @param attribute4 represents an attribute of a Java type
	 * @param attribute5 represents an attribute of a Java type
	 * @param attribute6 represents an attribute of a Java type
	 * @param attribute7 represents an attribute of a Java type
	 * @param attribute8 represents an attribute of a Java type
	 * @param <A> the represented type that contains the attribute
	 * @param <B> the type of the represented attribute or
	 * the type the represented collection belongs to
	 * @param <C> the element type of the represented collection or 
	 * the represented type that contains the attribute
	 * @param <D> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <E> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <F> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <G> the type of the represented attribute or
	 * The type the represented collection belongs to
	 * @param <H> the element type of the represented collection or 
	 * the represented type that contains the attribute
	 * @param <I> the type of the expression or 
	 * the type of the represented attribute
	 * @param <X> the type of the represented collection
	 * @param <Y> the type of the represented collection
	 * @return chained expression
	 */
	@SuppressWarnings("unchecked")
	public static <A, B, C, D, E, F, G, H, I, X, Y> Expression<I> getExpression(ExpressionMap expressionMap,
			Root<A> from, Attribute<? super A, B> attribute1, PluralAttribute<? super B, X, C> attribute2,
			Attribute<? super C, D> attribute3, Attribute<? super D, E> attribute4, Attribute<? super E, F> attribute5,
			Attribute<? super F, G> attribute6, PluralAttribute<? super G, Y, H> attribute7,
			Attribute<? super H, I> attribute8) {
		return (Expression<I>) getExpressionInternal(expressionMap, from, attribute1, attribute2, attribute3,
				attribute4, attribute5, attribute6, attribute7, attribute8);
	}

	/* improve performance */
	/**
	 * Construct expression for a series of joins.
	 *
	 * @param expressionMap store all {@link Join}s of a query
	 * @param from a root type in the from clause
	 * @param attribute1 represents an attribute of a Java type
	 * @param attribute2 represents an attribute of a Java type
	 * @param attribute3 represents an attribute of a Java type
	 * @param attribute4 represents an attribute of a Java type
	 * @param attribute5 represents an attribute of a Java type
	 * @param attribute6 represents an attribute of a Java type
	 * @param <A> the represented type that contains the attribute
	 * @param <B> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <C> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <D> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <E> the type of the represented attribute
	 * the type the represented collection belongs to
	 * @param <F> the element type of the represented collection or
	 * the represented type that contains the attribute
	 * @param <G> the type of the expression or
	 * the type of the represented attribute
	 * @param <X> the type of the represented collection
	 * @return chained expression
	 */
	@SuppressWarnings("unchecked")
	public static <A, B, C, D, E, F, G, X> Expression<G> getExpression(ExpressionMap expressionMap, Root<A> from,
			Attribute<? super A, B> attribute1, Attribute<? super B, C> attribute2, Attribute<? super C, D> attribute3,
			Attribute<? super D, E> attribute4, PluralAttribute<? super E, X, F> attribute5,
			Attribute<? super F, G> attribute6) {
		return (Expression<G>) getExpressionInternal(expressionMap, from, attribute1, attribute2, attribute3,
				attribute4, attribute5, attribute6);
	}

	/**
	 * Construct expression for a series of joins.
	 *
	 * @param expressionMap store all {@link Join}s of a query
	 * @param from a root type in the from clause
	 * @param attribute1 represents an attribute of a Java type
	 * @param attribute2 represents an attribute of a Java type
	 * @param attribute3 represents an attribute of a Java type
	 * @param attribute4 represents an attribute of a Java type
	 * @param attribute5 represents an attribute of a Java type
	 * @param attribute6 represents an attribute of a Java type
	 * @param attribute7 represents an attribute of a Java type
	 * @param attribute8 represents an attribute of a Java type
	 * @param <A> the represented type that contains the attribute
	 * @param <B> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <C> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <D> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <E> the type of the represented attribute
	 * the type the represented collection belongs to
	 * @param <F> the element type of the represented collection or
	 * the represented type that contains the attribute
	 * @param <G> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <H> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <I> the type of the expression or
	 * the type of the represented attribute
	 * @param <X> the type of the represented collection
	 * @return chained expression
	 */
	@SuppressWarnings("unchecked")
	public static <A, B, C, D, E, F, G, H, I, X> Expression<I> getExpression(ExpressionMap expressionMap, Root<A> from,
			Attribute<? super A, B> attribute1, Attribute<? super B, C> attribute2, Attribute<? super C, D> attribute3,
			Attribute<? super D, E> attribute4, PluralAttribute<? super E, X, F> attribute5,
			Attribute<? super F, G> attribute6, Attribute<? super G, H> attribute7,
			Attribute<? super H, I> attribute8) {
		return (Expression<I>) getExpressionInternal(expressionMap, from, attribute1, attribute2, attribute3,
				attribute4, attribute5, attribute6, attribute7, attribute8);
	}

	/**
	 * Construct expression for a series of joins.
	 *
	 * @param expressionMap store all {@link Join}s of a query
	 * @param from a root type in the from clause
	 * @param attribute1 represents an attribute of a Java type
	 * @param attribute2 represents an attribute of a Java type
	 * @param attribute3 represents an attribute of a Java type
	 * @param attribute4 represents an attribute of a Java type
	 * @param attribute5 represents an attribute of a Java type
	 * @param attribute6 represents an attribute of a Java type
	 * @param attribute7 represents an attribute of a Java type
	 * @param attribute8 represents an attribute of a Java type
	 * @param <A> the represented type that contains the attribute
	 * @param <B> the type of the represented attribute or
	 * the type the represented collection belongs to
	 * @param <C> the element type of the represented collection or 
	 * the type the represented collection belongs to
	 * @param <D> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <E> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <F> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <G> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <H> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <I> the type of the expression or
	 * the type of the represented attribute
	 * @param <X> the type of the represented collection
	 * @return chained expression
	 */
	@SuppressWarnings("unchecked")
	public static <A, B, C, D, E, F, G, H, I, X> Expression<I> getExpression(ExpressionMap expressionMap, Root<A> from,
			Attribute<? super A, B> attribute1, PluralAttribute<? super B, X, C> attribute2,
			Attribute<? super C, D> attribute3, Attribute<? super D, E> attribute4, Attribute<? super E, F> attribute5,
			Attribute<? super F, G> attribute6, Attribute<? super G, H> attribute7,
			Attribute<? super H, I> attribute8) {
		return (Expression<I>) getExpressionInternal(expressionMap, from, attribute1, attribute2, attribute3,
				attribute4, attribute5, attribute6, attribute7, attribute8);
	}

	/**
	 * Construct expression for a series of joins.
	 *
	 * @param expressionMap store all {@link Join}s of a query
	 * @param from a root type in the from clause
	 * @param attribute1 represents an attribute of a Java type
	 * @param attribute2 represents an attribute of a Java type
	 * @param attribute3 represents an attribute of a Java type
	 * @param attribute4 represents an attribute of a Java type
	 * @param attribute5 represents an attribute of a Java type
	 * @param attribute6 represents an attribute of a Java type
	 * @param attribute7 represents an attribute of a Java type
	 * @param attribute8 represents an attribute of a Java type
	 * @param attribute9 represents an attribute of a Java type
	 * @param <A> the represented type that contains the attribute
	 * @param <B> the type of the represented attribute or
	 * the type the represented collection belongs to
	 * @param <C> the element type of the represented collection or 
	 * the type the represented collection belongs to
	 * @param <D> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <E> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <F> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <G> the type of the represented attribute or
	 * the type the represented collection belongs to
	 * @param <H> the element type of the represented collection or 
	 * the type the represented collection belongs to
	 * @param <I> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <J> the type of the expression or
	 * the type of the represented attribute
	 * @param <X> the type of the represented collection
	 * @param <Y> the type of the represented collection
	 * @return chained expression
	 */
	@SuppressWarnings("unchecked")
	public static <A, B, C, D, E, F, G, H, I, J, X, Y> Expression<J> getExpression(ExpressionMap expressionMap,
			Root<A> from, Attribute<? super A, B> attribute1, PluralAttribute<? super B, X, C> attribute2,
			Attribute<? super C, D> attribute3, Attribute<? super D, E> attribute4, Attribute<? super E, F> attribute5,
			Attribute<? super F, G> attribute6, PluralAttribute<? super G, Y, H> attribute7,
			Attribute<? super H, I> attribute8, Attribute<? super I, J> attribute9) {
		return (Expression<J>) getExpressionInternal(expressionMap, from, attribute1, attribute2, attribute3,
				attribute4, attribute5, attribute6, attribute7, attribute8, attribute9);
	}

	/**
	 * Construct expression for a series of joins.
	 *
	 * @param expressionMap store all {@link Join}s of a query
	 * @param from a root type in the from clause
	 * @param attribute1 represents an attribute of a Java type
	 * @param attribute2 represents an attribute of a Java type
	 * @param attribute3 represents an attribute of a Java type
	 * @param attribute4 represents an attribute of a Java type
	 * @param attribute5 represents an attribute of a Java type
	 * @param attribute6 represents an attribute of a Java type
	 * @param attribute7 represents an attribute of a Java type
	 * @param attribute8 represents an attribute of a Java type
	 * @param attribute9 represents an attribute of a Java type
	 * @param attribute10 represents an attribute of a Java type
	 * @param <A> the represented type that contains the attribute
	 * @param <B> the type of the represented attribute or
	 * the type the represented collection belongs to
	 * @param <C> the element type of the represented collection or 
	 * the type the represented collection belongs to
	 * @param <D> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <E> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <F> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <G> the type of the represented attribute or
	 * the type the represented collection belongs to
	 * @param <H> the element type of the represented collection or 
	 * the type the represented collection belongs to
	 * @param <I> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <J> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <K> the type of the expression or
	 * the type of the represented attribute
	 * @param <X> the type of the represented collection
	 * @param <Y> the type of the represented collection
	 * @return chained expression
	 */
	@SuppressWarnings("unchecked")
	public static <A, B, C, D, E, F, G, H, I, J, K, X, Y> Expression<K> getExpression(ExpressionMap expressionMap,
			Root<A> from, Attribute<? super A, B> attribute1, PluralAttribute<? super B, X, C> attribute2,
			Attribute<? super C, D> attribute3, Attribute<? super D, E> attribute4, Attribute<? super E, F> attribute5,
			Attribute<? super F, G> attribute6, PluralAttribute<? super G, Y, H> attribute7,
			Attribute<? super H, I> attribute8, Attribute<? super I, J> attribute9,
			Attribute<? super J, K> attribute10) {
		return (Expression<K>) getExpressionInternal(expressionMap, from, attribute1, attribute2, attribute3,
				attribute4, attribute5, attribute6, attribute7, attribute8, attribute9, attribute10);
	}

	/**
	 * Construct expression for a series of joins.
	 *
	 * @param expressionMap store all {@link Join}s of a query
	 * @param from a root type in the from clause
	 * @param attribute1 represents an attribute of a Java type
	 * @param attribute2 represents an attribute of a Java type
	 * @param attribute3 represents an attribute of a Java type
	 * @param attribute4 represents an attribute of a Java type
	 * @param attribute5 represents an attribute of a Java type
	 * @param attribute6 represents an attribute of a Java type
	 * @param attribute7 represents an attribute of a Java type
	 * @param attribute8 represents an attribute of a Java type
	 * @param attribute9 represents an attribute of a Java type
	 * @param attribute10 represents an attribute of a Java type
	 * @param attribute11 represents an attribute of a Java type
	 * @param attribute12 represents an attribute of a Java type
	 * @param <A> the represented type that contains the attribute
	 * @param <B> the type of the represented attribute or
	 * the type the represented collection belongs to
	 * @param <C> the element type of the represented collection or 
	 * the type the represented collection belongs to
	 * @param <D> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <E> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <F> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <G> the type of the represented attribute or
	 * the type the represented collection belongs to
	 * @param <H> the element type of the represented collection or 
	 * the type the represented collection belongs to
	 * @param <I> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <J> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <K> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <L> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <M> the type of the expression or
	 * the type of the represented attribute
	 * @param <X> the type of the represented collection
	 * @param <Y> the type of the represented collection
	 * @return chained expression
	 */
	@SuppressWarnings("unchecked")
	public static <A, B, C, D, E, F, G, H, I, J, K, L, M, X, Y> Expression<M> getExpression(ExpressionMap expressionMap,
			Root<A> from, Attribute<? super A, B> attribute1, PluralAttribute<? super B, X, C> attribute2,
			Attribute<? super C, D> attribute3, Attribute<? super D, E> attribute4, Attribute<? super E, F> attribute5,
			Attribute<? super F, G> attribute6, PluralAttribute<? super G, Y, H> attribute7,
			Attribute<? super H, I> attribute8, Attribute<? super I, J> attribute9, Attribute<? super J, K> attribute10,
			Attribute<? super K, L> attribute11, Attribute<? super L, M> attribute12) {
		return (Expression<M>) getExpressionInternal(expressionMap, from, attribute1, attribute2, attribute3,
				attribute4, attribute5, attribute6, attribute7, attribute8, attribute9, attribute10, attribute11,
				attribute12);
	}

	/**
	 * Construct expression for a series of joins.
	 *
	 * @param expressionMap store all {@link Join}s of a query
	 * @param from a root type in the from clause
	 * @param attribute1 represents an attribute of a Java type
	 * @param attribute2 represents an attribute of a Java type
	 * @param attribute3 represents an attribute of a Java type
	 * @param attribute4 represents an attribute of a Java type
	 * @param attribute5 represents an attribute of a Java type
	 * @param attribute6 represents an attribute of a Java type
	 * @param attribute7 represents an attribute of a Java type
	 * @param attribute8 represents an attribute of a Java type
	 * @param attribute9 represents an attribute of a Java type
	 * @param attribute10 represents an attribute of a Java type
	 * @param attribute11 represents an attribute of a Java type
	 * @param <A> the represented type that contains the attribute
	 * @param <B> the type of the represented attribute or
	 * the type the represented collection belongs to
	 * @param <C> the element type of the represented collection or 
	 * the type the represented collection belongs to
	 * @param <D> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <E> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <F> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <G> the type of the represented attribute or
	 * the type the represented collection belongs to
	 * @param <H> the element type of the represented collection or 
	 * the type the represented collection belongs to
	 * @param <I> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <J> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <K> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <L> the type of the expression or
	 * the type of the represented attribute
	 * @param <X> the type of the represented collection
	 * @param <Y> the type of the represented collection
	 * @return chained expression
	 */
	@SuppressWarnings("unchecked")
	public static <A, B, C, D, E, F, G, H, I, J, K, L, X, Y> Expression<L> getExpression(ExpressionMap expressionMap,
			Root<A> from, Attribute<? super A, B> attribute1, PluralAttribute<? super B, X, C> attribute2,
			Attribute<? super C, D> attribute3, Attribute<? super D, E> attribute4, Attribute<? super E, F> attribute5,
			Attribute<? super F, G> attribute6, PluralAttribute<? super G, Y, H> attribute7,
			Attribute<? super H, I> attribute8, Attribute<? super I, J> attribute9, Attribute<? super J, K> attribute10,
			Attribute<? super K, L> attribute11) {
		return (Expression<L>) getExpressionInternal(expressionMap, from, attribute1, attribute2, attribute3,
				attribute4, attribute5, attribute6, attribute7, attribute8, attribute9, attribute10, attribute11);
	}
	/* SAPInvoicePosition Expression end */

	/**
	 * Construct expression for a series of joins.
	 *
	 * @param expressionMap		store all {@link Join}s of a query
	 * @param from				a root type in the from clause
	 * @param attribute1		represents an attribute of a Java type
	 * @param attribute2		represents an attribute of a Java type
	 * @param attribute3		represents an attribute of a Java type
	 * @param attribute4		represents an attribute of a Java type
	 * @param attribute5		represents an attribute of a Java type	
	 * @param attribute6		represents an attribute of a Java type
	 * @param <A> the represented type that contains the attribute
	 * @param <B> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <C> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <D> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <E> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <F> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <G> the type of the expression or
	 * the type of the represented attribute
	 * @return chained expression
	 */
	@SuppressWarnings("unchecked")
	public static <A, B, C, D, E, F, G> Expression<G> getExpression(ExpressionMap expressionMap, Root<A> from,
			Attribute<? super A, B> attribute1, Attribute<? super B, C> attribute2, Attribute<? super C, D> attribute3,
			Attribute<? super D, E> attribute4, Attribute<? super E, F> attribute5,
			Attribute<? super F, G> attribute6) {
		return (Expression<G>) getExpressionInternal(expressionMap, from, attribute1, attribute2, attribute3,
				attribute4, attribute5, attribute6);
	}

	/**
	 * Construct expression for a series of joins.
	 *
	 * @param expressionMap		store all {@link Join}s of a query
	 * @param from				a root type in the from clause
	 * @param attribute1		represents an attribute of a Java type
	 * @param attribute2		the type PluralAttribute represent persistent collection-valued attributes
	 * @param attribute3		represents an attribute of a Java type
	 * @param attribute4		represents an attribute of a Java type
	 * @param attribute5		represents an attribute of a Java type
	 * @param attribute6		represents an attribute of a Java type
	 * @param attribute7		represents an attribute of a Java type\
	 * @param <A> the represented type that contains the attribute
	 * @param <B> the type of the represented attribute or
	 * the type the represented collection belongs to
	 * @param <C> the element type of the represented collection or 
	 * the represented type that contains the attribute
	 * @param <D> the type of the represented attribute or
	 * the type of the represented attribute
	 * @param <E> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <F> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <G> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <H> the type of the expression or
	 * the type of the represented attribute
	 * @param <X> the type of the represented collection
	 * @return chained expression
	 */
	@SuppressWarnings("unchecked")
	public static <A, B, C, D, E, F, G, H, X> Expression<H> getExpression(ExpressionMap expressionMap, Root<A> from,
			Attribute<? super A, B> attribute1, PluralAttribute<? super B, X, C> attribute2,
			Attribute<? super C, D> attribute3, Attribute<? super D, E> attribute4, Attribute<? super E, F> attribute5,
			Attribute<? super F, G> attribute6, Attribute<? super G, H> attribute7) {
		return (Expression<H>) getExpressionInternal(expressionMap, from, attribute1, attribute2, attribute3,
				attribute4, attribute5, attribute6, attribute7);
	}

	/**
	 * Construct expression for a series of joins. 1 singular , 2 plural attributes
	 * and 4 singular attributes
	 *
	 * @param expressionMap		store all {@link Join}s of a query
	 * @param from				a root type in the from clause
	 * @param attribute1		represents an attribute of a Java type
	 * @param attribute2		the type PluralAttribute represent persistent collection-valued attributes
	 * @param attribute3		the type PluralAttribute represent persistent collection-valued attributes
	 * @param attribute4		represents an attribute of a Java type
	 * @param attribute5		represents an attribute of a Java type
	 * @param attribute6		represents an attribute of a Java type
	 * @param attribute7		represents an attribute of a Java type
	 * @param <A> the represented type that contains the attribute
	 * @param <B> the type of the represented attribute or
	 * the type the represented collection belongs to
	 * @param <C> the element type of the represented collection or 
	 * the type the represented collection belongs to
	 * @param <D> the element type of the represented collection or 
	 * the represented type that contains the attribute
	 * @param <E> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <F> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <G> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <H> the type of the expression or
	 * the type of the represented attribute
	 * @param <X> the type of the represented collection
	 * @param <Y> the type of the represented collection
	 * @return result expression
	 */
	@SuppressWarnings("unchecked")
	public static <A, B, C, D, E, F, G, H, X, Y> Expression<H> getExpression(ExpressionMap expressionMap, Root<A> from,
			Attribute<? super A, B> attribute1, PluralAttribute<? super B, X, C> attribute2,
			PluralAttribute<? super C, Y, D> attribute3, Attribute<? super D, E> attribute4,
			Attribute<? super E, F> attribute5, Attribute<? super F, G> attribute6,
			Attribute<? super G, H> attribute7) {
		return (Expression<H>) getExpressionInternal(expressionMap, from, attribute1, attribute2, attribute3,
				attribute4, attribute5, attribute6, attribute7);
	}

	/**
	 * Construct expression for a series of joins.
	 *
	 * @param expressionMap		store all {@link Join}s of a query
	 * @param from				a root type in the from clause
	 * @param attribute1		represents an attribute of a Java type
	 * @param attribute2		the type PluralAttribute represent persistent collection-valued attributes
	 * @param attribute3		represents an attribute of a Java type
	 * @param attribute4		represents an attribute of a Java type
	 * @param attribute5		represents an attribute of a Java type
	 * @param attribute6		represents an attribute of a Java type
	 * @param <A> the represented type that contains the attribute
	 * @param <B> the type of the represented attribute or
	 * the type the represented collection belongs to
	 * @param <C> the element type of the represented collection or 
	 * the represented type that contains the attribute
	 * @param <D> the type of the represented attribute or 
	 * the represented type that contains the attribute
	 * @param <E> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <F> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <G> the type of the expression or
	 * the type of the represented attribute
	 * @param <X> the type of the represented collection
	 * @return chained expression
	 */
	@SuppressWarnings("unchecked")
	public static <A, B, C, D, E, F, G, X> Expression<G> getExpression(ExpressionMap expressionMap, Root<A> from,
			Attribute<? super A, B> attribute1, PluralAttribute<? super B, X, C> attribute2,
			Attribute<? super C, D> attribute3, Attribute<? super D, E> attribute4, Attribute<? super E, F> attribute5,
			Attribute<? super F, G> attribute6) {
		return (Expression<G>) getExpressionInternal(expressionMap, from, attribute1, attribute2, attribute3,
				attribute4, attribute5, attribute6);
	}

	/**
	 * Construct expression for a series of joins
	 * 
	 * @param expressionMap		store all {@link Join}s of a query
	 * @param from				a root type in the from clause
	 * @param attribute1		represents persistent single-valued properties or fields
	 * @param attribute2		represents an attribute of a Java type
	 * @param attribute3		represents an attribute of a Java type
	 * @param attribute4		represents an attribute of a Java type
	 * @param attribute5		represents an attribute of a Java type
	 * @param <A> the type containing the represented attribute
	 * @param <B> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <C> the type of the represented attribute or 
	 * the represented type that contains the attribute
	 * @param <D> the type of the represented attribute or 
	 * the represented type that contains the attribute
	 * @param <E> the type of the represented attribute or
	 * the represented type that contains the attribute
	 * @param <F> the type of the expression or
	 * the type of the represented attribute
	 * @return attribute joined to 5 previous parent attributes
	 */
	@SuppressWarnings("unchecked")
	public static <A, B, C, D, E, F> Expression<F> getExpression(ExpressionMap expressionMap, Root<A> from,
			SingularAttribute<? super A, B> attribute1, Attribute<? super B, C> attribute2,
			Attribute<? super C, D> attribute3, Attribute<? super D, E> attribute4,
			Attribute<? super E, F> attribute5) {
		return (Expression<F>) getExpressionInternal(expressionMap, from, attribute1, attribute2, attribute3,
				attribute4, attribute5);
	}

	/**
	 * Construct expression for a series of joins
	 * 
	 * @param expressionMap		store all {@link Join}s of a query
	 * @param from				a root type in the from clause
	 * @param attribute1		represents persistent single-valued properties or fields
	 * @param attribute2		the type PluralAttribute represent persistent collection-valued attributes
	 * @param attribute3		the type PluralAttribute represent persistent collection-valued attributes
	 * @param attribute4		represents an attribute of a Java type
	 * @param <A> the type containing the represented attribute
	 * @param <B> the type of the represented attribute or
	 * the type the represented collection belongs to
	 * @param <C> the element type of the represented collection or 
	 * the type the represented collection belongs to
	 * @param <D> the element type of the represented collection or 
	 * the represented type that contains the attribute
	 * @param <E> the type of the expression or
	 * the type of the represented attribute
	 * @param <X1> the type of the represented collection
	 * @param <X2> the type of the represented collection
	 * @return attribute joined to 4 previous parent attributes
	 */
	@SuppressWarnings("unchecked")
	public static <X1, X2, A, B, C, D, E> Expression<E> getExpression(ExpressionMap expressionMap, Root<A> from,
			SingularAttribute<? super A, B> attribute1, PluralAttribute<? super B, X1, C> attribute2,
			PluralAttribute<? super C, X2, D> attribute3, Attribute<? super D, E> attribute4) {
		return (Expression<E>) getExpressionInternal(expressionMap, from, attribute1, attribute2, attribute3,
				attribute4);
	}

	/**
	 * Construct expression for a series of joins
	 * 
	 * @param expressionMap		store all {@link Join}s of a query
	 * @param from				a root type in the from clause
	 * @param attribute1		the type PluralAttribute represent persistent collection-valued attributes
	 * @param attribute2		represents an attribute of a Java type
	 * @param attribute3		represents an attribute of a Java type
	 * @param attribute4		represents an attribute of a Java type
	 * @param <A> the type the represented collection belongs to
	 * @param <B> the element type of the represented collection or
	 * the represented type that contains the attribute
	 * @param <C> the type of the represented attribute or 
	 * the represented type that contains the attribute
	 * @param <D> the type of the represented attribute or  
	 * the represented type that contains the attribute
	 * @param <E> the type of the expression or
	 * the type of the represented attribute
	 * @param <X> the type of the represented collection
	 * @return attribute joined to 4 previous parent attributes
	 */
	@SuppressWarnings("unchecked")
	public static <X, A, B, C, D, E> Expression<E> getExpression(ExpressionMap expressionMap, Root<A> from,
			PluralAttribute<? super A, X, B> attribute1, Attribute<? super B, C> attribute2,
			Attribute<? super C, D> attribute3, Attribute<? super D, E> attribute4) {
		return (Expression<E>) getExpressionInternal(expressionMap, from, attribute1, attribute2, attribute3,
				attribute4);
	}

	/**
	 * Construct expression for a series of joins
	 * 
	 * @param expressionMap	store all {@link Join}s of a query
	 * @param from				a root type in the from clause
	 * @param attribute0		represents an attribute of a Java type
	 * @param attribute1		the type PluralAttribute represent persistent collection-valued attributes
	 * @param attribute2		represents an attribute of a Java type
	 * @param attribute3		represents an attribute of a Java type
	 * @param <A0> the represented type that contains the attribute
	 * @param <A> the type of the represented attribute or
	 * the type the represented collection belongs to
	 * @param <B> the element type of the represented collection or
	 * the represented type that contains the attribute
	 * @param <C> the type of the represented attribute or 
	 * the represented type that contains the attribute
	 * @param <D> the type of the expression or
	 * the type of the represented attribute
	 * @param <X> the type of the represented collection
	 * @return attribute joined to 4 previous parent attributes
	 */
	@SuppressWarnings("unchecked")
	public static <X, A0, A, B, C, D> Expression<D> getExpression(ExpressionMap expressionMap, Root<A0> from,
			Attribute<? super A0, A> attribute0, PluralAttribute<? super A, X, B> attribute1,
			Attribute<? super B, C> attribute2, Attribute<? super C, D> attribute3) {
		return (Expression<D>) getExpressionInternal(expressionMap, from, attribute0, attribute1, attribute2,
				attribute3);
	}

	/**
	 * Construct expression for a series of joins
	 * 
	 * @param expressionMap	store all {@link Join}s of a query
	 * @param from				a root type in the from clause
	 * @param attribute0		represents an attribute of a Java type
	 * @param attribute1		the type PluralAttribute represent persistent collection-valued attributes
	 * @param attribute2		represents an attribute of a Java type
	 * @param attribute3		represents an attribute of a Java type
	 * @param attribute4		represents an attribute of a Java type
	 * @param <A0> the represented type that contains the attribute
	 * @param <A> the type of the represented attribute or
	 * the type the represented collection belongs to
	 * @param <B> the element type of the represented collection or
	 * the represented type that contains the attribute
	 * @param <C> the type of the represented attribute or 
	 * the represented type that contains the attribute
	 * @param <D> the type of the represented attribute or 
	 * the represented type that contains the attribute
	 * @param <E> the type of the expression or
	 * the type of the represented attribute
	 * @param <X> the type of the represented collection
	 * @return attribute joined to 5 previous parent attributes
	 */
	@SuppressWarnings("unchecked")
	public static <X, A0, A, B, C, D, E> Expression<E> getExpression(ExpressionMap expressionMap, Root<A0> from,
			Attribute<? super A0, A> attribute0, PluralAttribute<? super A, X, B> attribute1,
			Attribute<? super B, C> attribute2, Attribute<? super C, D> attribute3,
			Attribute<? super D, E> attribute4) {
		return (Expression<E>) getExpressionInternal(expressionMap, from, attribute0, attribute1, attribute2,
				attribute3, attribute4);
	}

	/**
	 * Construct expression for a series of joins
	 * 
	 * @param expressionMap		store all {@link Join}s of a query
	 * @param from				a root type in the from clause
	 * @param attribute1		represents an attribute of a Java type
	 * @param attribute2		represents an attribute of a Java type
	 * @param attribute3		represents an attribute of a Java type
	 * @param attribute4		represents an attribute of a Java type
	 * @param <A> the represented type that contains the attribute
	 * @param <B> the type of the represented attribute or 
	 * the represented type that contains the attribute
	 * @param <C> the type of the represented attribute or 
	 * the represented type that contains the attribute
	 * @param <D> the type of the represented attribute or 
	 * the represented type that contains the attribute
	 * @param <E> the type of the expression or
	 * the type of the represented attribute
	 * @return attribute joined to 4 previous parent attributes
	 */
	@SuppressWarnings("unchecked")
	public static <A, B, C, D, E> Expression<E> getExpression(ExpressionMap expressionMap, Root<A> from,
			Attribute<? super A, B> attribute1, Attribute<? super B, C> attribute2, Attribute<? super C, D> attribute3,
			Attribute<? super D, E> attribute4) {

		return (Expression<E>) getExpressionInternal(expressionMap, from, attribute1, attribute2, attribute3,
				attribute4);
	}

	/**
	 * Construct expression for a series of joins
	 * 
	 * @param expressionMap	store all {@link Join}s of a query
	 * @param from				a root type in the from clause
	 * @param attribute1		the type PluralAttribute represent persistent collection-valued attributes
	 * @param attribute2		represents an attribute of a Java type
	 * @param attribute3		represents an attribute of a Java type
	 * @param <A> the type the represented collection belongs to
	 * @param <B> the element type of the represented collection or
	 * the represented type that contains the attribute
	 * @param <C> the type of the represented attribute or 
	 * the represented type that contains the attribute
	 * @param <D> the type of the expression or
	 * the type of the represented attribute
	 * @param <X> the type of the represented collection
	 * @return attribute joined to 3 previous parent attributes
	 */
	@SuppressWarnings("unchecked")
	public static <X, A, B, C, D> Expression<D> getExpression(ExpressionMap expressionMap, Root<A> from,
			PluralAttribute<? super A, X, B> attribute1, Attribute<? super B, C> attribute2,
			Attribute<? super C, D> attribute3) {

		return (Expression<D>) getExpressionInternal(expressionMap, from, attribute1, attribute2, attribute3);
	}

	/**
	 * Construct expression for a series of joins
	 * 
	 * @param expressionMap	store all {@link Join}s of a query
	 * @param from			a root type in the from clause
	 * @param attribute1	represents an attribute of a Java type
	 * @param attribute2	represents an attribute of a Java type
	 * @param attribute3	represents an attribute of a Java type
	 * @param <A> the represented type that contains the attribute
	 * @param <B> the type of the represented attribute or 
	 * the represented type that contains the attribute
	 * @param <C> the type of the represented attribute or 
	 * the represented type that contains the attribute
	 * @param <D> the type of the expression or
	 * the type of the represented attribute
	 * @return attribute joined to 3 previous parent attributes
	 */
	@SuppressWarnings("unchecked")
	public static <A, B, C, D> Expression<D> getExpression(ExpressionMap expressionMap, Root<A> from,
			Attribute<? super A, B> attribute1, Attribute<? super B, C> attribute2,
			Attribute<? super C, D> attribute3) {

		return (Expression<D>) getExpressionInternal(expressionMap, from, attribute1, attribute2, attribute3);
	}

	/**
	 * Construct expression for a series of joins
	 * 
	 * @param expressionMap	store all {@link Join}s of a query
	 * @param from			a root type in the from clause
	 * @param attribute1	represents an attribute of a Java type
	 * @param attribute2	the type PluralAttribute represent persistent collection-valued attributes
	 * @param attribute3	the type PluralAttribute represent persistent collection-valued attributes
	 * @param <A> the represented type that contains the attribute
	 * @param <B> the type of the represented attribute or
	 * the type the represented collection belongs to
	 * @param <C> the element type of the represented collection or
	 * the represented type that contains the attribute
	 * @param <D> the type of the expression or
	 * the type of the represented attribute
	 * @param <X> the type of the represented collection
	 * @return attribute joined to 3 previous parent attributes
	 */
	@SuppressWarnings("unchecked")
	public static <X, A, B, C, D> Expression<D> getExpression(ExpressionMap expressionMap, Root<A> from,
			Attribute<? super A, B> attribute1, PluralAttribute<? super B, X, C> attribute2,
			Attribute<? super C, D> attribute3) {

		return (Expression<D>) getExpressionInternal(expressionMap, from, attribute1, attribute2, attribute3);
	}

	/**
	 * Construct expression for a series of joins
	 * 
	 * @param expressionMap	store all {@link Join}s of a query
	 * @param from			a root type in the from clause
	 * @param attribute1	the type PluralAttribute represent persistent collection-valued attributes
	 * @param attribute2	the type PluralAttribute represent persistent collection-valued attributes
	 * @param attribute3	represents an attribute of a Java type
	 * @param <A> the type the represented collection belongs to
	 * @param <B> the element type of the represented collection or
	 * the type the represented collection belongs to
	 * @param <C> the element type of the represented collection 
	 * the represented type that contains the attribute
	 * @param <D> the type of the expression or
	 * the type of the represented attribute
	 * @param <X> the type of the represented collection
	 * @param <X2> the type of the represented collection
	 * @return attribute joined to 3 previous parent attributes
	 */
	@SuppressWarnings("unchecked")
	public static <X, X2, A, B, C, D> Expression<D> getExpression(ExpressionMap expressionMap, Root<A> from,
			PluralAttribute<? super A, X, B> attribute1, PluralAttribute<? super B, X2, C> attribute2,
			Attribute<? super C, D> attribute3) {

		return (Expression<D>) getExpressionInternal(expressionMap, from, attribute1, attribute2, attribute3);
	}

	/**
	 * Construct expression for a series of joins
	 * 
	 * @param expressionMap	store all {@link Join}s of a query
	 * @param from			a root type in the from clause
	 * @param attribute1	the type PluralAttribute represent persistent collection-valued attributes
	 * @param attribute2	represents an attribute of a Java type
	 * @param <A> the type the represented collection belongs to
	 * @param <B> the element type of the represented collection or
	 * the represented type that contains the attribute
	 * @param <C> the type of the expression or
	 * the type of the represented attribute
	 * @param <X> the type of the represented collection
	 * @return attribute joined to 2 previous parent attributes
	 */
	@SuppressWarnings("unchecked")
	public static <X, A, B, C> Expression<C> getExpression(ExpressionMap expressionMap, Root<A> from,
			PluralAttribute<? super A, X, B> attribute1, Attribute<? super B, C> attribute2) {
		return (Expression<C>) getExpressionInternal(expressionMap, from, attribute1, attribute2);
	}

	/**
	 * Construct expression for a series of joins
	 * 
	 * @param expressionMap	store all {@link Join}s of a query
	 * @param from			a root type in the from clause
	 * @param attribute1	represents an attribute of a Java type
	 * @param attribute2	represents an attribute of a Java type
	 * @param <A> the represented type that contains the attribute
	 * @param <B> the type of the represented attribute or 
	 * the represented type that contains the attribute
	 * @param <C> the type of the expression or
	 * the type of the represented attribute
	 * @return attribute joined to 2 previous parent attributes
	 */
	@SuppressWarnings("unchecked")
	public static <A, B, C> Expression<C> getExpression(ExpressionMap expressionMap, Root<A> from,
			Attribute<? super A, B> attribute1, Attribute<? super B, C> attribute2) {

		return (Expression<C>) getExpressionInternal(expressionMap, from, attribute1, attribute2);
	}

	/**
	 * Construct expression for a series of joins
	 * 
	 * @param expressionMap	store all {@link Join}s of a query
	 * @param from			a root type in the from clause
	 * @param attribute		represents an attribute of a Java type
	 * @param <A> the represented type that contains the attribute
	 * @param <B> the type of the expression or
	 * the type of the represented attribute
	 * @return attribute joined to 1 previous parent attributes
	 */
	@SuppressWarnings("unchecked")
	public static <A, B> Expression<B> getExpression(ExpressionMap expressionMap, Root<A> from,
			Attribute<? super A, B> attribute) {

		return (Expression<B>) getExpressionInternal(expressionMap, from, attribute);
	}

	/**
	 * @param expressionMap	store all {@link Join}s of a query
	 * @param root			a root type in the from clause
	 * @param a				the type PluralAttribute represent persistent collection-valued attributes
	 * @param b				represents an attribute of a Java type
	 * @param c				represents an attribute of a Java type
	 * @param d				represents an attribute of a Java type
	 * @param e				represents an attribute of a Java type
	 * @param <A> the type the represented collection belongs to
	 * @param <B> the element type of the represented collection or
	 * the represented type that contains the attribute
	 * @param <C> the type of the represented attribute or 
	 * the represented type that contains the attribute
	 * @param <D> the type of the represented attribute or 
	 * the represented type that contains the attribute
	 * @param <E> the type of the represented attribute or 
	 * the represented type that contains the attribute
	 * @param <F> the type of the expression or
	 * the type of the represented attribute
	 * @param <X> the type of the represented collection
	 * @return chained expression
	 */
	@SuppressWarnings("unchecked")
	public static <A, B, C, D, E, F, X> Expression<F> getExpression(ExpressionMap expressionMap, Root<A> root,
			PluralAttribute<? super A, X, B> a, Attribute<? super B, C> b, Attribute<? super C, D> c,
			Attribute<? super D, E> d, Attribute<? super E, F> e) {

		return (Expression<F>) getExpressionInternal(expressionMap, root, a, b, c, d, e);
	}

	/**
	 * Construct expression for a general series of join.
	 *
	 * <p>
	 * <b>Note: Only use this general function if you really need it. Otherwise
	 * use/create a type-safe variant.</b>
	 *
	 * @param expressionMap	store all {@link Join}s of a query
	 * @param from			represents a bound type
	 * @param attributes	represents an attribute of a Java type
	 * @return expression which is the result of chaining all the input expressions
	 *         in the order as specified
	 */
	public static Expression<?> getExpressionGeneral(ExpressionMap expressionMap, From<?, ?> from,
			Attribute<?, ?>... attributes) {
		return getExpressionInternal(expressionMap, true, from, attributes);
	}

	/**
	 * Construct expression for a general series of join.
	 *
	 * <p>
	 * <b>Note: Only use this general function if you really need it. Otherwise
	 * use/create a type-safe variant.</b>
	 *
	 * @param expressionMap store all {@link Join}s of a query
	 * @param create        if true, then expressions missing in the expressionMap
	 *                      will be created, otherwise they will return null.
	 * @param from			represents a bound type
	 * @param attributes	represents an attribute of a Java type
	 * @return expression which is the result of chaining all the input expressions
	 *         in the order as specified
	 */
	public Expression<?> getExpressionGeneral(ExpressionMap expressionMap, boolean create, From<?, ?> from,
			Attribute<?, ?>... attributes) {
		return getExpressionInternal(expressionMap, create, from, attributes);
	}

	/**
	 * Follow a chain of relations.
	 *
	 * For all but the last element perform left outer joins. If the
	 * {@link ExpressionMap} is not null, then all nodes will be cached in this map
	 * and reused in later calls. This is necessary, if subparts of the query are
	 * accessed multiple times and should only cause single joins.
	 *
	 * @param expressionMap
	 * @param from
	 * @param attributes
	 * @return expression which is the result of chaining all the input expressions
	 *         in the order as specified
	 */
	@SuppressWarnings({ })
	private static Expression<?> getExpressionInternal(ExpressionMap expressionMap, From<?, ?> from,
			Attribute<?, ?>... attributes) {
		return getExpressionInternal(expressionMap, true, from, attributes);
	}

	/**
	 * Follow a chain of relations.
	 *
	 * If create == true , then for all but the last element perform left outer
	 * joins. If the {@link ExpressionMap} is not null, then all nodes will be
	 * cached in this map and reused in later calls. This is necessary, if subparts
	 * of the query are accessed multiple times and should only cause single joins.
	 *
	 * @param expressionMap
	 * @param create        if true, then expressions missing in the expressionMap
	 *                      will be created, otherwise they will return null.
	 * @param from
	 * @param attributes
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Expression<?> getExpressionInternal(ExpressionMap expressionMap, boolean create, From<?, ?> from,
			Attribute<?, ?>... attributes) {
		Path<?> tmpPath = from;
		String key = tmpPath.getJavaType().getCanonicalName();
		Expression result = tmpPath;

		for (int i = 0; i < attributes.length; i++) {
			boolean last = i == attributes.length - 1;
			Attribute<?, ?> attribute = attributes[i];

			key += "." + attribute.getName();

			LOG.debug("expression key: {0}", key);

			if (!last) {
				Path cachedPath = null;

				if (expressionMap != null) {
					cachedPath = (Path) expressionMap.get(key);
				}
				if (cachedPath == null) {
					if (create) {
						LOG.debug("creating new expression");
						tmpPath = getNextAttribute(tmpPath, attribute);

						if (expressionMap != null) {
							// cache for future calls
							expressionMap.put(key, tmpPath);
						}
					} else {
						tmpPath = null;
					}
				} else {
					tmpPath = cachedPath;
					LOG.debug("re-using join");
				}
			} else {
				Expression cachedExpression = null;
				if (expressionMap != null) {
					cachedExpression = expressionMap.get(key);
				}
				if (cachedExpression == null) {
					if (create) {
						LOG.debug("creating new expression");
						if (attribute instanceof SingularAttribute) {
							result = tmpPath.get((SingularAttribute) attribute);
						} else if (attribute instanceof ListAttribute) {
							result = tmpPath.get((ListAttribute) attribute);
						}

						if (expressionMap != null) {
							// cache for future calls
							expressionMap.put(key, result);
						}
					} else {
						result = null;
					}
				} else {
					result = cachedExpression;
					LOG.debug("re-using expression");
				}
			}
		}

		return result;
	}

	/**
	 * Navigate to the next attribute.
	 *
	 * @param path
	 * @param attribute
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Path<?> getNextAttribute(Path<?> path, Attribute<?, ?> attribute) {
		Path<?> resultPath = path;
		PersistentAttributeType aType = attribute.getPersistentAttributeType();
		if (attribute instanceof SingularAttribute) {
			if (aType == PersistentAttributeType.EMBEDDED) {
				resultPath = path.get((SingularAttribute) attribute);
			} else {
				if (!(path instanceof From)) {
					LOG.error("When building the JPA expression for a singular attribute " + attribute
							+ " , a Path was needed but instead " + path + " was found, query build will fail");
				}
				resultPath = ((From) path).join((SingularAttribute) attribute, JoinType.LEFT);
			}
		} else if (attribute instanceof ListAttribute) {
			if (!(path instanceof From)) {
				LOG.error("When building the JPA expression for a list attribute " + attribute
						+ " , a Path was needed but instead " + path + " was found, query build will fail");
			}
			resultPath = ((From) path).join((ListAttribute) attribute, JoinType.LEFT);
		} else {
			LOG.error("When building the JPA expression unhandled attribute " + attribute + " was found.");
		}
		return resultPath;
	}

	protected static Attribute<?, ?>[] concat(Attribute<?, ?>[] left, Attribute<?, ?>... right) {
		Attribute<?, ?>[] result = new Attribute<?, ?>[left.length + right.length];
		for (int i = 0; i < left.length; i++) {
			result[i] = left[i];
		}
		for (int i = 0; i < right.length; i++) {
			result[left.length + i] = right[i];
		}
		return result;
	}

	/**
	 * Make a cascading copy of a JPA object.
	 *
	 * Copy columns and relations, if they are of type {@link CascadeType} ALL.
	 *
	 * Do not copy fields annotated with {@link Id}. Finer grained information can
	 * be passed by {@link CascadeCopy} annotations.
	 *
	 * @param src object
	 * @param ignoreGroups groups which should be ignored if a field is annotated
	 * @param <T> the type of the represented object
	 * @return object of same type as src input
	 */
	public <T> T cascadeCopy(T src, Class<?>... ignoreGroups) {
		try {
			Set<Class<?>> groups = new HashSet<>(Arrays.asList(ignoreGroups));

			return cascadeCopy("CC: ", src, groups, new HashMap<>());
		} catch (InstantiationException | IllegalAccessException e) {
			throw new PersistenceException(e);
		}
	}

	private <T> T cascadeCopy(String indent, T src, Set<Class<?>> ignoreGroups, Map<Object, Object> newObjectCache)
			throws InstantiationException, IllegalAccessException {
		// in case we get a proxy object, unproxy it to avoid creating new proxy
		// instances (which confuses Hibernate).
		@SuppressWarnings("unchecked")
		T tmpSrc = unproxy(src);

		// make a new instance only if we do not have one for this src already.
		@SuppressWarnings("unchecked")
		T dst = (T) newObjectCache.get(tmpSrc);

		if (dst == null) {
			Class<? extends Object> srcClass = tmpSrc.getClass();
			String className = srcClass.getSimpleName();

			// new instance
			@SuppressWarnings("unchecked")
			T tmpDst = (T) srcClass.newInstance();
			dst = tmpDst;
			newObjectCache.put(tmpSrc, dst);

			Map<String, Field> fieldMap = ReflectionUtilitities.getFieldMap(tmpSrc);

			List<Entry<String, Field>> entries = new ArrayList<>(fieldMap.entrySet());

			// sort list of fields by name, so debugging will be easier
			entries.sort((left, right) -> left.getKey().compareTo(right.getKey()));

			// move reference fields to the end
			List<Entry<String, Field>> first = new ArrayList<>(entries.size());
			List<Entry<String, Field>> referenced = new ArrayList<>(entries.size());
			for (Entry<String, Field> entry : entries) {
				Field f = entry.getValue();
				f.setAccessible(true);
				CascadeCopy aCascadeCopy = f.getAnnotation(CascadeCopy.class);

				if (aCascadeCopy == null || aCascadeCopy.reference().equals(CascadeCopy.NOREF)) {
					first.add(entry);
				} else {
					referenced.add(entry);
				}
			}

			entries = first;
			entries.addAll(referenced);

			boolean logged = false;
			boolean warn = false;
			String fieldName = "";

			try {
				// scan all fields
				for (Entry<String, Field> entry : entries) {
					// LOG field and action AFTER the action
					logged = false;
					warn = false;
					String action = "unknown action";
					fieldName = className + "." + entry.getKey();

					Field f = entry.getValue();
					f.setAccessible(true);
					Object value = f.get(tmpSrc);

					if (value == null) {
						action = "skipped because null";
					} else {
						Version aVersion = f.getAnnotation(Version.class);
						CascadeCopy aCascadeCopy = f.getAnnotation(CascadeCopy.class);
						Embedded aEmbedded = f.getAnnotation(Embedded.class);
						Id aId = f.getAnnotation(Id.class);
						Column aColumn = f.getAnnotation(Column.class);
						OneToMany aOneToMany = f.getAnnotation(OneToMany.class);
						OneToOne aOneToOne = f.getAnnotation(OneToOne.class);
						ManyToOne aManyToOne = f.getAnnotation(ManyToOne.class);
						ManyToMany aManyToMany = f.getAnnotation(ManyToMany.class);

						boolean ignore = false;

						if (aCascadeCopy != null) {
							// ignore flag is set
							if (aCascadeCopy.ignore()) {
								action = "skipped because of CascadeCopy.ignore annotation";
								ignore = true;
							} else {
								// find out whether any ignore group is matching any annotated ignore group
								// if yes, ignore
								for (Class<?> group : aCascadeCopy.ignoreGroups()) {
									if (ignoreGroups.contains(group)) {
										action = "skipped because of CascadeCopy.ignoreGroups ("
												+ group.getCanonicalName() + ") annotation";
										ignore = true;
										break;
									}
								}
							}
						}

						if (ignore) {
							// name says it all
						} else if (aId != null) {
							action = "skipped primary key";
						} else if (aVersion != null) {
							action = "skipped version";
						} else if (aColumn != null) {
							action = "copied";
							f.set(dst, value);
						} else if (aEmbedded != null) {
							// generate a cascade copy
							action = "created a new instance for embedded";
							f.set(dst, cascadeCopy(indent + INDENT_INC, value, ignoreGroups, newObjectCache));
						} else if (aOneToOne != null) {
							action = copyIfCascade(indent, ignoreGroups, newObjectCache, dst, f, value,
									aOneToOne.cascade());
						} else if (aManyToOne != null) {
							action = copyIfCascade(indent, ignoreGroups, newObjectCache, dst, f, value,
									aManyToOne.cascade());

						} else if (aOneToMany != null) {
							// create a copy of the list
							// if cascade, then insert copies of objects
							// the other side of the relation is handled by ManyToOne
							boolean cascade = isCascade(aOneToMany.cascade());
							if (value instanceof Iterable) {
								Iterable<?> iterable = (Iterable<?>) value;

								List<Object> copyValue = new ArrayList<>();
								f.set(dst, copyValue);

								int done = 0;
								for (Object obj : iterable) {
									LOG.debug("{0}collection object: {1}", indent, obj);
									if (cascade) {
										copyValue.add(
												cascadeCopy(indent + INDENT_INC, obj, ignoreGroups, newObjectCache));
									} else {
										copyValue.add(obj);
									}
									done++;
								}
								action = "copied " + done + " instances";
							} else {
								action = "skipped because value is not iterable";
								warn = true;
							}

						} else if (aManyToMany != null) {
							action = "skipped because ManyToMany not implemented";
							warn = true;
						} else {
							action = "skipped because no DB mapping";
						}
					}

					if (warn) {
						LOG.warn("{0}{1}: {2}", indent, fieldName, action);
					} else {
						LOG.debug("{0}{1}: {2}", indent, fieldName, action);
					}
					logged = true;
				}
			} finally {
				if (!logged) {
					LOG.warn("{0}{1}: did not finish normally", indent, fieldName);
				}
			}

		} else {
			LOG.debug("{0}already created new instance {1} for source {2}, returning same instance again.", indent, dst,
					tmpSrc);
		}
		return dst;
	}

	private <T> String copyIfCascade(String indent, Set<Class<?>> ignoreGroups, Map<Object, Object> newObjectCache,
			T dst, Field f, Object value, CascadeType[] cascadeTypes)
					throws IllegalAccessException, InstantiationException {
		String action;
		if (isCascade(cascadeTypes)) {
			// for cascade generate a deep copy
			action = "created a new instance for cascading One(many)ToOne";
			f.set(dst, cascadeCopy(indent + INDENT_INC, value, ignoreGroups, newObjectCache));
		} else {
			Object copiedValue = newObjectCache.get(value);
			if (copiedValue != null) {
				// no cascade, but value was copied previously, so we must take the copy
				action = "copied because no cascading One(many)ToOne, but using a copy from the cache";
				f.set(dst, copiedValue);
			} else {
				action = "copied because no cascading One(many)ToOne";
				f.set(dst, value);
			}
		}
		return action;
	}

	private boolean isCascade(CascadeType[] ct) {
		for (CascadeType cascadeType : ct) {
			if (cascadeType.equals(CascadeType.ALL)) {
				return true;
			}
			if (cascadeType.equals(CascadeType.MERGE)) {
				return true;
			}
		}
		return false;
	}

}
