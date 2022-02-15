package com.axonivy.market.jpa.demo.daos;

/**
 * Define the persistence unit name to use in DAOs implementing this one.
 */
public interface BaseDAO extends com.axonivy.persistence.dao.BaseDAO {
	@Override
	default public String getPersistenceUnitName() {
		return "jpa_demo";
	}
}
