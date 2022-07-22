package com.axonivy.utils.persistence.history.handler;

import java.io.Serializable;

import com.axonivy.utils.persistence.beans.GenericEntity;
import com.axonivy.utils.persistence.dao.CriteriaQueryGenericContext;

/**
 * Audit handler interface.
 * 
 * @author maonguyen
 *
 */
public interface AuditHandler {
	
	/**
	 * Handle for create action
	 * 
	 * @param bean to handle create
	 */
	<T extends GenericEntity<? extends Serializable>> void handleCreate(T bean); 

	/**
	 * Handle for update action
	 * @param current TODO
	 * @param bean to handle update
	 */
	<T extends GenericEntity<? extends Serializable>> void handleUpdate(T current, T bean);

	/**
	 * Handle for delete action
	 * @param bean to handle delete
	 */
	<T extends GenericEntity<? extends Serializable>> void handleDelete(T bean);

	/**
	 * Handle for read action
	 * 
	 * @param query to handle delete
	 */
	void handleRead(CriteriaQueryGenericContext<?, ?> query);
	
	/**
	 * Get persistence unit name for the history table. The persistence unit name is needed in case using a database for
	 * saving history.
	 * 
	 * @return persistence unit name
	 */
	String getHandlerPersistenceUnitName();

}
