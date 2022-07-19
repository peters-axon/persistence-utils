package com.axonivy.utils.persistence.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.axonivy.utils.persistence.dao.markers.QueryMarker;
import com.axonivy.utils.persistence.logging.AsciiTree;
import com.axonivy.utils.persistence.logging.Logger;

import ch.ivyteam.ivy.bpm.error.BpmError;

/**
 * Query context for serializable type T , and resulting object R
 *
 * @param <T> root object
 * @param <R> return object
 * @author peter
 */
public abstract class CriteriaQueryGenericContext<T extends Serializable, R extends Object>
		extends QueryGenericContext<T> {

	private static final Logger log = Logger.getLogger(CriteriaQueryGenericContext.class);
	/**
	 * CriteriaQuery for return type R
	 */
	public final CriteriaQuery<R> q;

	private Root<?> temporaryRoot;

	protected QuerySettings<T> querySettings = new QuerySettings<>();

	protected ExpressionMap expressionMap = null;

	private ExpressionMap temporaryExpressionMap;

	protected TypedQueryInterceptor<R> typedQueryInterceptor = null;

	/**
	 * Inititalize with specified parameters
	 * 
	 * @param cb represents a interface used to construct criteria queries, compound selections, 
	 * expressions, predicates, orderings
	 * @param query a interface defines functionality that is specific 
	 * to top-level queries.
	 * @param root a root type in the from clause
	 */
	public CriteriaQueryGenericContext(CriteriaBuilder cb, CriteriaQuery<R> query, Root<T> root) {
		super(cb, root);
		this.q = query;
	}

	/**
	 * Initialize from other instance
	 * 
	 * @param factory query context
	 */
	public CriteriaQueryGenericContext(CriteriaQueryGenericContext<T, R> factory) {
		this(factory.c, factory.q, factory.r);
	}

	/**
	 * Initialize query context
	 * 
	 * @param cb represents a interface used to construct criteria queries, compound selections, 
	 * expressions, predicates, orderings
	 * @param query a interface defines functionality that is specific 
	 * to top-level queries
	 * @param root a root type in the from clause
	 * @param closeable represents a supplier of results
	 * @param <T> the type of the represented root object
	 * @param <R> the type of the represented return object
	 * @return query context initialized from specified parameters
	 */
	public static <T extends Serializable, R extends Object> CriteriaQueryGenericContext<T, R> from(CriteriaBuilder cb,
			CriteriaQuery<R> query, Root<T> root, Supplier<Void> closeable) {
		return new InternalCriteriaQueryGenericContext<>(cb, query, root, closeable);
	}

	/**
	 *
	 * Helper query context with autoclose features
	 *
	 * @param <T> root object
	 * @param <R> return object
	 */
	public static class InternalCriteriaQueryGenericContext<T extends Serializable, R extends Object>
			extends CriteriaQueryGenericContext<T, R> {
		private final Supplier<Void> autoCloseable;

		/**
		 * Initialize with specified inputs and autoclose method
		 * 
		 * @param cb represents a interface used to construct criteria queries, compound selections, 
		 * expressions, predicates, orderings
		 * @param query a interface defines functionality that is specific 
		 * to top-level queries
		 * @param root a root type in the from clause
		 * @param closeable represents a supplier of results
		 */
		public InternalCriteriaQueryGenericContext(CriteriaBuilder cb, CriteriaQuery<R> query, Root<T> root,
				Supplier<Void> closeable) {
			super(cb, query, root);
			this.autoCloseable = closeable;
		}

		@Override
		public void close() {
			try {
				autoCloseable.get();
			} catch (PersistenceException | NullPointerException | BpmError e) {
				log.warn("auto closing session in InternalCriteriaQueryGenericContext had problem", e);
				// ignore error when close failed
			}
		}
	};

	/**
	 * Convenience interceptor for modifying query before getResults, and after
	 * getResults
	 *
	 * @param <T> the type of the represented object
	 */
	public static class TypedQueryInterceptor<T extends Object> {
		/**
		 * Called before getResultList is called on the query.
		 *
		 * Used to manipulate the query before execution. This can be used to set
		 * maximum number of results and the like.
		 *
		 * @param typedQuery interface used to control the execution of typed queries
		 */
		public void beforeGetResultList(TypedQuery<T> typedQuery) {
			/* default implementation does not change anything */
		};

		/**
		 * Called after getResultList is called on the query.
		 *
		 * Used to manipulate the result after execution. This can be used to initialize
		 * lazy objects and the like.
		 *
		 * @param resultList a list with object T
		 * @return list of modified results
		 */
		public List<T> afterGetResultList(List<T> resultList) {
			return resultList;
		};
	};

	/**
	 * unwrap the query string from hibernate, its not the sql but HQL query string
	 * 
	 * @param query represent a interface used to control the execution of typed queries
	 * @return HQL string
	 */
	public String getQueryString(TypedQuery<?> query) {
		return query.unwrap(org.hibernate.Query.class).getQueryString();
	}

	/**
	 * Joins parameters into a string
	 * 
	 * @param query represent a interface used to control the execution of typed queries
	 * @return comma separated parameters used in query
	 */
	public String getQueryParameters(TypedQuery<?> query) {
		return StringUtils.join(query.unwrap(org.hibernate.Query.class).getNamedParameters(), ",");
	}

	public void setQuerySettings(QuerySettings<T> querySettings) {
		this.querySettings = querySettings;
	}

	public QuerySettings<T> getQuerySettings() {
		return querySettings;
	}

	/**
	 * Get the expression map for this context.
	 *
	 * The expression map should be used to avoid duplicate joins. It contains all
	 * known and solved "navigations" and will re-use joins, if they were already
	 * done.
	 *
	 * @return expression map
	 */
	public ExpressionMap getCurrentExpressionMap() {
		if (temporaryExpressionMap != null) {
			return temporaryExpressionMap;
		} else if (expressionMap == null) {
			expressionMap = ExpressionMap.createNewExpressionMap();
		}

		return expressionMap;
	}

	/**
	 * Set a temporary expression map.
	 *
	 * @param temporaryExpressionMap represents a class used to store all {@link Join}s of a query
	 */
	public void setTemporaryExpressionMap(ExpressionMap temporaryExpressionMap) {
		this.temporaryExpressionMap = temporaryExpressionMap;
	}

	/**
	 * Reset the temporary expression map.
	 */
	public void resetTemporaryExpressionMap() {
		temporaryExpressionMap = null;
	}

	/**
	 * adds multiple order bys to the query.
	 *
	 * Per default ascending. Only attributes directly lying on the main root object
	 * of the query
	 * 
	 * @param order list of all attributes in the required sorted positions
	 * @return the modified queryContext self
	 */
	@SafeVarargs
	public final CriteriaQueryGenericContext<T, R> orderBy(SingularAttribute<T, ?>... order) {
		if (order != null) {
			List<Order> collectedOrderbys = Stream.of(order).filter(o -> o != null).map(o -> c.asc(this.r.get(o)))
					.collect(Collectors.toList());
			this.orderBy(collectedOrderbys.toArray(new Order[] {}));
		}

		return this;
	}

	/**
	 * Similar to {@link CriteriaQuery} orderBy, but the old orderBy is preserved
	 * and a next order expression is added
	 * 
	 * @param order an object that defines an ordering over the query results
	 * @return query context with applied order bys, preserving old order bys(old
	 *         are first )
	 */
	public CriteriaQueryGenericContext<T, R> orderBy(Order... order) {
		if (ArrayUtils.isNotEmpty(order)) {
			List<Order> orders = q.getOrderList();
			List<Order> orderList = new ArrayList<>(orders.size() + 1);
			Collections.copy(orderList, orders);

			for (Order o : order) {
				if (o != null) {
					orderList.add(o);
				}
			}

			q.orderBy(orderList);
		}

		return this;
	}

	/**
	 * Order ascending
	 * 
	 * @param field represents persistent single-valued properties or fields
	 * @return ascending order for field
	 */
	public Order asc(SingularAttribute<? super T, ?> field) {
		return c.asc(r.get(field));
	}

	/**
	 * Order Descending
	 * 
	 * @param field represents persistent single-valued properties or fields
	 * @return descending order
	 */
	public Order desc(SingularAttribute<? super T, ?> field) {
		return c.desc(r.get(field));
	}

	/**
	 * Shorthand for null check in where conditions
	 * 
	 * @param property represents a simple or compound attribute path
	 * @return null field from root applied in where clause
	 */
	public CriteriaQueryGenericContext<T, R> whereIsNull(Path<?> property) {
		where(property.isNull());

		return this;
	}

	/**
	 * Shorthand for not null check in where conditions
	 * 
	 * @param property represents a simple or compound attribute path
	 * @return not null field applied in where clause
	 */
	public CriteriaQueryGenericContext<T, R> whereIsNotNull(Path<?> property) {
		where(property.isNotNull());

		return this;
	}

	/**
	 * add new multiple where conditions to old where section via AND
	 * 
	 * @param expressions the type of a simple or compound predicate
	 * @return where conditions added to query context
	 */
	public CriteriaQueryGenericContext<T, R> where(Predicate... expressions) {
		if (expressions != null) {
			Predicate[] resultExpressions = expressions;
			Predicate restrictions = q.getRestriction();
			if (restrictions != null) {
				final int n = resultExpressions.length;
				resultExpressions = java.util.Arrays.copyOf(resultExpressions, n + 1);
				resultExpressions[n] = restrictions;
			}
			restrictions = c.and(resultExpressions);// concatenate the restrictions with AND operator
			q.where(restrictions);
		}

		return this;
	}

	/**
	 * join multiple new expressions via AND, then join them to old where conditions
	 * via OR
	 * 
	 * @param expressions the type of a simple or compound predicate
	 * @return context with applied new predicates , but added to old ones via OR
	 *         expression
	 */
	public CriteriaQueryGenericContext<T, R> orWhere(Predicate... expressions) {
		if (expressions != null) {
			Predicate restriction = q.getRestriction();
			Predicate newconditions = c.and(expressions); // join the newconditions together via AND, then add to old
															// conditions via OR
			if (restriction != null) {
				restriction = c.or(restriction, newconditions);// (old restrictionr) OR (newconditions)
			} else {
				restriction = newconditions;
			}

			q.where(restriction);
		}

		return this;
	}

	/**
	 * Like comparison looking for substring with value, add to where condition 'key
	 * like %value%'
	 * 
	 * @param expression type for query expressions
	 * @param value a string for query
	 * @return context with applied like String expression in where query
	 */
	public CriteriaQueryGenericContext<T, R> whereLike(Expression<String> expression, String value) {
		where(c.like(expression, "%" + value + "%"));

		return this;
	}

	/**
	 * add to where condition 'key like %value%'
	 * 
	 * @param key represents persistent single-valued properties or fields
	 * @param value a string for query
	 * @return String property added via like operator to where query
	 */
	public CriteriaQueryGenericContext<T, R> whereLike(SingularAttribute<? super T, String> key, String value) {
		whereLike(r.get(key), value);

		return this;
	}

	/**
	 * Shorthand for where root SingularAttribute equals value condition
	 * 
	 * @param key represents persistent single-valued properties or fields
	 * @param value a string for query
	 * @param <A> the type of the represented object
	 * @return modified criteria query with where Eq condition
	 */
	public <A extends Object> CriteriaQueryGenericContext<T, R> whereEq(SingularAttribute<? super T, A> key, A value) {
		whereEq(r.get(key), value);

		return this;
	}

	/**
	 * Shorthand for where expression equals value condition
	 * 
	 * @param expression type for query expressions
	 * @param value a object with X type
	 * @param <X> the type of the represented object
	 * @return context with applied where equals condition
	 */
	public <X> CriteriaQueryGenericContext<T, R> whereEq(Expression<X> expression, X value) {
		where(c.equal(expression, value));

		return this;
	}

	/**
	 * index of first DB result to be shown as 0th result ,.
	 *
	 * @deprecated use QuerySettings object instead.
	 * @return index, eg. if 9 then the result list starts with the db row 10
	 */
	@Deprecated
	public Integer getFirstResult() {
		return querySettings.getFirstResult();
	}

	/**
	 * setter.
	 *
	 * @deprecated use QuerySettings object instead.
	 * @param firstResult the new first result
	 */
	@Deprecated
	public void setFirstResult(Integer firstResult) {
		querySettings.withFirstResult(firstResult);
	}

	/**
	 * Specifies the max number of returned rows.
	 *
	 * @deprecated use QuerySettings object instead.
	 * @return the max results
	 */
	@Deprecated
	public Integer getMaxResults() {
		return querySettings.getMaxResults();
	}

	/**
	 * Setter.
	 *
	 * @deprecated use QuerySettings object instead.
	 * @param maxResults the new max results
	 */
	@Deprecated
	public void setMaxResults(Integer maxResults) {
		querySettings.withMaxResults(maxResults);
	}

	/**
	 * Getter for query interceptor.
	 *
	 * @return the typed query interceptor
	 */
	public TypedQueryInterceptor<R> getTypedQueryInterceptor() {
		return typedQueryInterceptor;
	}

	/**
	 * Setter.
	 *
	 * @param typedQueryInterceptor the new typed query interceptor
	 */
	public void setTypedQueryInterceptor(TypedQueryInterceptor<R> typedQueryInterceptor) {
		this.typedQueryInterceptor = typedQueryInterceptor;
	}

	/**
	 * Return a nice formated asciitree of roots and joins of query
	 */
	@Override
	public String toString() {
		AsciiTree tree = new AsciiTree();
		tree.format("%s", getClass().getSimpleName());
		tree.down();
		tree.format("Roots");
		tree.down();

		Set<Root<?>> roots = q.getRoots();
		for (Root<?> root : roots) {
			tree.format("%s", root.getJavaType().getSimpleName());
			Set<?> joins = root.getJoins();
			tree.down();
			tree.format("Joins");
			tree.down();
			
			for (Object object : joins) {
				tree.format("%s", object.toString());
			}

			tree.up();
			tree.up();
		}

		tree.up();
		tree.up();

		return tree.toString();
	}

	/**
	 * Close method which is called at end of try with resources section of this
	 * autocloseable. It Should end the session, or transaction, etc...
	 */
	@Override
	public abstract void close();

	/**
	 * Add a marker object to transport additional information to DAOs.
	 *
	 * There can only be one marker object per marker object class.
	 *
	 * @deprecated use QuerySettings object instead.
	 * @param markerObject a marker object to query
	 */
	@Deprecated
	public void mark(QueryMarker markerObject) {
		querySettings.withMarkers(markerObject);
	}

	/**
	 * Get the marker class.
	 *
	 * @deprecated use QuerySettings object instead.
	 * @param markerClass a marker class to query
	 * @return query marker
	 */
	@Deprecated
	public QueryMarker getMarker(Class<? extends QueryMarker> markerClass) {
		return querySettings.getMarker(markerClass);
	}

	/**
	 * shorthand for selecting a column of root entity. e.g.
	 * this.q.select(this.r.get(column));
	 * 
	 * @param column represents persistent single-valued properties or fields.
	 * @return the same object as being called CriteriaQueryGenericContext
	 */
	public CriteriaQueryGenericContext<T, R> select(SingularAttribute<T, R> column) {
		this.q.select(this.r.get(column));

		return this;
	}

	/**
	 * Used to work with a different root.
	 *
	 * Could be used in subqueries. Must be reset before the query is executed.
	 *
	 * @param temporaryRoot a root type in the from clause
	 */
	public void setTemporaryRoot(Root<?> temporaryRoot) {
		this.temporaryRoot = temporaryRoot;
	}

	/**
	 * Reset the temporary root.
	 */
	public void resetTemporaryRoot() {
		this.temporaryRoot = null;
	}

	/**
	 * Get the current root.
	 *
	 * This might be the temporary root if it was set.
	 *
	 * @return a root type in the from clause
	 */
	public Root<?> getCurrentRoot() {
		return temporaryRoot != null ? temporaryRoot : r;
	}
}
