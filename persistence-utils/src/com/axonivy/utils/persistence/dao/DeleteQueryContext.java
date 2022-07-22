package com.axonivy.utils.persistence.dao;

import java.io.Serializable;
import java.util.function.Supplier;

import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

import com.axonivy.utils.persistence.logging.Logger;

import ch.ivyteam.ivy.bpm.error.BpmError;

/**
 * Simplified delete query context which start with T type
 *
 * @param <T> entity
 */
public abstract class DeleteQueryContext <T extends Serializable> extends DeleteQueryGenericContext<T>  {

	private static final Logger log = Logger.getLogger(DeleteQueryContext.class);
	
	/**
	 * Constructor
	 * 
	 * @param c represents a interface used to construct criteria queries, compound selections, 
	 * expressions, predicates, orderings
	 * @param d a interface defines functionality for performing
	 * bulk delete operations using the Criteria API.
	 * @param r a root type in the from clause
	 */
	public DeleteQueryContext(CriteriaBuilder c, CriteriaDelete<T> d, Root<T> r) {
		super(c, d, r);
	}

	/**
	 * Create factory
	 *
	 * @param cb represents a interface used to construct criteria queries, compound selections, 
	 * expressions, predicates, orderings
	 * @param query a interface defines functionality for performing
	 * bulk delete operations using the Criteria API
	 * @param root a root type in the from clause
	 * @param closeable represents a supplier of results
	 * @param <T> the entity type that is the target of the delete
	 * @return new query context
	 */
	public static <T extends  Serializable> DeleteQueryContext<T> createFactoryfrom(CriteriaBuilder cb, CriteriaDelete<T> query, Root<T> root, Supplier<Void> closeable){
		return new InternalDeleteQueryContext<>(cb, query, root, closeable);
	}
	
	/**
	 * Helper query context with autoclose features
	 *
	 * @param <T> the type of the represented object
	 */
	public static class InternalDeleteQueryContext<T extends  Serializable> extends DeleteQueryContext<T> {
		private final Supplier<Void> autoCloseable;

		/**
		 * Initialize with specified inputs and autoclose method
		 * 
		 * @param cb represents a interface used to construct criteria queries, compound selections, 
		 * expressions, predicates, orderings
		 * @param query a interface defines functionality for performing
		 * bulk delete operations using the Criteria API
		 * @param root a root type in the from clause
		 * @param closeable represents a supplier of results
		 */
		public InternalDeleteQueryContext(CriteriaBuilder cb, CriteriaDelete<T> query, Root<T> root, Supplier<Void> closeable){
			super(cb,query,root);
			this.autoCloseable = closeable;
		}

		/* (non-Javadoc)
		 * @see java.io.Closeable#close()
		 */
		@Override
		public void close() {
			try {
				autoCloseable.get();
			} catch (PersistenceException | BpmError e) {
				log.warn("auto closing session in InternalDeleteQueryContext had problem", e);
			}
		}
	}
}
