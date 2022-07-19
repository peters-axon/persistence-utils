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
 * Update query context for serializable type T
 *
 * @param <T> entity
 */
public abstract class UpdateQueryGenericContext<T extends  Serializable> extends QueryGenericContext<T> {
	
	/**
	 * CriteriaUpdate for return type T
	 */
	public final CriteriaUpdate<T> u;
	
	private static final Logger log = Logger.getLogger(UpdateQueryGenericContext.class);
	
	public UpdateQueryGenericContext(CriteriaBuilder c, CriteriaUpdate<T> u, Root<T> r) {
		super(c, r);
		this.u = u;
	}
	
	/**
	 * Initialize query context
	 * 
	 * @param cb represents a interface used to construct criteria queries, compound selections, 
	 * expressions, predicates, orderings
	 * @param query interface defines functionality for performing
	 * bulk update operations using the Criteria API
	 * @param root a root type in the from clause
	 * @param closeable represents a supplier of results
	 * @param <T> the type of the represented object
	 * @return update query context initialized from specified parameters
	 */
	public static <T extends  Serializable> UpdateQueryGenericContext<T> from (CriteriaBuilder cb, CriteriaUpdate<T> query, Root<T> root, Supplier<Void> closeable){
		return new InternalUpdateQueryGenericContext<>(cb, query, root, closeable);
	}
	
	/**
	 * Helper query context with autoclose features
	 * 
	 * @param <T> the type of the represented object
	 */
	public static class InternalUpdateQueryGenericContext<T extends  Serializable> extends UpdateQueryGenericContext<T> {
		private final Supplier<Void>  autoCloseable;
		
		/**
		 * Initialize with specified inputs and autoclose method
		 * 
		 * @param c represents a interface used to construct criteria queries, compound selections, 
		 * expressions, predicates, orderings
		 * @param u interface defines functionality for performing
		 * bulk update operations using the Criteria API
		 * @param r a root type in the from clause
		 * @param closeable represents a supplier of results
		 */
		public InternalUpdateQueryGenericContext(CriteriaBuilder c,
				CriteriaUpdate<T> u, Root<T> r, Supplier<Void>  closeable) {
			super(c, u, r);
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

	/**
	 * Close method which is called at end of try with resources section of this autocloseable.
	 * It Should end the session, or transaction, etc...
	 */
	@Override
	public abstract void close();
}
