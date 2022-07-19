package com.axonivy.utils.persistence.dao;

/**
 * DAOs must implement this interface.
 */
public interface BaseDAO {

	/**
	 * Get the persistence unit name of an Ivy project. This will be used for
	 * persistence selection for each query DAO.
	 * 
	 * @return the persistence unit name
	 */
	public String getPersistenceUnitName();
}
