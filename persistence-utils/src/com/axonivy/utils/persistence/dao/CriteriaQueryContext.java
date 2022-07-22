package com.axonivy.utils.persistence.dao;

import java.io.Serializable;
import java.util.function.Supplier;

import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.axonivy.utils.persistence.logging.Logger;

import ch.ivyteam.ivy.bpm.error.BpmError;

/**
 * Simplified query context which start with T type and returns a list of T types
 *
 * @param <T> entity
 */
public abstract class CriteriaQueryContext<T extends  Serializable> extends CriteriaQueryGenericContext<T,T> {
	private static final Logger log = Logger.getLogger(CriteriaQueryContext.class);

	/**
	 * @param cb represents a interface used to construct criteria queries, compound selections, 
	 * expressions, predicates, orderings
	 * @param query a interface defines functionality that is specific 
	 * to top-level queries.
	 * @param root a root type in the from clause
	 */
	public CriteriaQueryContext(CriteriaBuilder cb, CriteriaQuery<T> query, Root<T> root){
		super(cb, query, root);
	}

	/**
	 * @param factory query context
	 */
	public CriteriaQueryContext(CriteriaQueryContext<T> factory){
		this(factory.c, factory.q, factory.r);
	}

	/**
	 *
	 * @param cb represents a interface used to construct criteria queries, compound selections, 
	 * expressions, predicates, orderings
	 * @param query a interface defines functionality that is specific 
	 * to top-level queries.
	 * @param root a root type in the from clause
	 * @param closeable represents a supplier of results
	 * @param <T> the type of the represented object
	 * @return new query context
	 */
	public static <T extends  Serializable> CriteriaQueryContext<T> createFactoryfrom (CriteriaBuilder cb, CriteriaQuery<T> query, Root<T> root, Supplier<Void> closeable){
		return new InternalCriteriaQueryContext<>(cb, query, root, closeable);
	}

	/**
	 * Helper instance used for constructing a new context
	 *
	 * @param <T> the type of the represented object
	 */
	public static class InternalCriteriaQueryContext<T extends  Serializable> extends CriteriaQueryContext<T> {
		private final Supplier<Void> autoCloseable;

		/**
		 * @param cb represents a interface used to construct criteria queries, compound selections, 
		 * expressions, predicates, orderings
		 * @param query a interface defines functionality that is specific 
		 * to top-level queries
		 * @param root a root type in the from clause
		 * @param closeable represents a supplier of results
		 */
		public InternalCriteriaQueryContext(CriteriaBuilder cb, CriteriaQuery<T> query, Root<T> root, Supplier<Void> closeable){
			super(cb,query,root);
			this.autoCloseable = closeable;
		}

		/* (non-Javadoc)
		 * @see java.io.Closeable#close()
		 */
		@Override
		public void close() {
			try{
				autoCloseable.get();
			} catch (PersistenceException | BpmError e) {
				log.warn("auto closing session in InternalCriteriaQueryGenericContext had problem",e);
				//ignore error when close failed
			}
		}
	};

}
