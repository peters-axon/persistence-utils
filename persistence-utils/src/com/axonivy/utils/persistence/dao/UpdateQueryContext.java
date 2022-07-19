package com.axonivy.utils.persistence.dao;

import java.io.Serializable;
import java.util.function.Supplier;

import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;

import com.axonivy.utils.persistence.logging.Logger;

import ch.ivyteam.ivy.bpm.error.BpmError;

/**
 * Simplified update query context which start with T
 *
 * @param <T> entity
 */
public abstract class UpdateQueryContext <T extends Serializable> extends UpdateQueryGenericContext<T>  {

	private static final Logger log = Logger.getLogger(UpdateQueryContext.class);
	
	public UpdateQueryContext(CriteriaBuilder c, CriteriaUpdate<T> u, Root<T> r) {
		super(c, u, r);
	}

	/**
	 * Create factory
	 *
	 * @param cb represents a interface used to construct criteria queries, compound selections, 
	 * expressions, predicates, orderings
	 * @param query interface defines functionality for performing
	 * bulk update operations using the Criteria API
	 * @param root a root type in the from clause
	 * @param closeable represents a supplier of results
	 * @param <T> the type of the represented object
	 * @return new query context
	 */
	public static <T extends  Serializable> UpdateQueryContext<T> createFactoryfrom (CriteriaBuilder cb, CriteriaUpdate<T> query, Root<T> root, Supplier<Void> closeable){
		return new InternalUpdateQueryContext<>(cb, query, root, closeable);
	}
	
	/**
	 * Helper query context with autoclose features
	 *
	 * @param <T> the type of the represented object
	 */
	public static class InternalUpdateQueryContext<T extends  Serializable> extends UpdateQueryContext<T> {
		private final Supplier<Void>  autoCloseable;

		/**
		 * Initialize with specified inputs and autoclose method
		 * 
		 * @param cb represents a interface used to construct criteria queries, compound selections, 
		 * expressions, predicates, orderings
		 * @param query interface defines functionality for performing
		 * bulk update operations using the Criteria API
		 * @param root a root type in the from clause
		 * @param closeable represents a supplier of results
		 */
		public InternalUpdateQueryContext(CriteriaBuilder cb, CriteriaUpdate<T> query, Root<T> root, Supplier<Void> closeable){
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
			}
		}
	}
}
