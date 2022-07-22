package com.axonivy.utils.persistence.dao;

import java.io.Serializable;

import com.axonivy.utils.persistence.beans.GenericEntity;
import com.axonivy.utils.persistence.enums.UpdateType;

/**
 * Used to mark a class (typically a DAO) which implements caching.
 * @param <T> entity
 */
public interface Caching<T extends GenericEntity<? extends Serializable>> {
	/**
	 * Called whenever an object is created, updated or deleted.
	 *
	 * @param updateType enum for all possible hibernate update types
	 * @param bean represents a bean
	 */
	public void invalidateCache(UpdateType updateType, T bean);
}
