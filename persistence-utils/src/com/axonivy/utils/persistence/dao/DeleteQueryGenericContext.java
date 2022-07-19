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
 * Delete query context for serializable type T
 *
 * @param <T> entity
 */
public abstract class DeleteQueryGenericContext<T extends Serializable> extends QueryGenericContext<T> {
	/**
	 * CriteriaDelete for return type T
	 */
	public final CriteriaDelete<T> d;
	
	private static final Logger log = Logger.getLogger(DeleteQueryGenericContext.class);
	
	public DeleteQueryGenericContext(CriteriaBuilder c, CriteriaDelete<T> d, Root<T> r) {
		super(c, r);
		this.d = d;
	}

	/**
	 * Initialize query context
	 * @param cb represents a interface used to construct criteria queries, compound selections, 
	 * expressions, predicates, orderings
	 * @param query a interface defines functionality for performing
	 * bulk delete operations using the Criteria API
	 * @param root a root type in the from clause
	 * @param closeable represents a supplier of results
	 * @param <T> the entity type that is the target of the delete
	 * @return update query context initialized from specified parameters
	 */
	public static <T extends  Serializable> DeleteQueryGenericContext<T> from (CriteriaBuilder cb, CriteriaDelete<T> query, Root<T> root, Supplier<Void> closeable){
		return new InternalDeleteQueryGenericContext<>(cb, query, root, closeable);
	}
	
	/**
	 * Helper query context with autoclose features
	 *
	 * @param <T> the type of the represented root object
	 */
	public static class InternalDeleteQueryGenericContext<T extends  Serializable> extends DeleteQueryGenericContext<T> {
		private final Supplier<Void>  autoCloseable;
		
		/**
		 * Initialize with specified inputs and autoclose method
		 * 
		 * @param c represents a interface used to construct criteria queries, compound selections, 
		 * expressions, predicates, orderings
		 * @param d a interface defines functionality for performing
		 * bulk delete operations using the Criteria API
		 * @param r a root type in the from clause
		 * @param closeable represents a supplier of results
		 */
		public InternalDeleteQueryGenericContext(CriteriaBuilder c,
				CriteriaDelete<T> d, Root<T> r, Supplier<Void>  closeable) {
			super(c, d, r);
			this.autoCloseable = closeable;
		}

		@Override
		public void close() {
			try{
				autoCloseable.get();
			} catch (PersistenceException | NullPointerException | BpmError e) {
				log.warn("auto closing session in InternalUpdateQueryGenericContext had problem",e);
				//ignore error when close failed
			}
		}
		
	}

}
