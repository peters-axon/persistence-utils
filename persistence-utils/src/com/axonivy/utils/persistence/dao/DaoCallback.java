package com.axonivy.utils.persistence.dao;

import java.util.function.Consumer;

import com.axonivy.utils.persistence.beans.GenericEntity;

/**
 * Dao CallBack for GenericEntities
 *
 * @param <T> entity
 */
public abstract interface DaoCallback<T extends GenericEntity<?>> {
	/**
	 * @return Consumer which allows modification of T instance before persist
	 */
	public abstract Consumer<T> prePersist();
	/**
	 * @return Consumer which allows modification of T instance after persist
	 */
	public abstract Consumer<T> postPersist();
}
