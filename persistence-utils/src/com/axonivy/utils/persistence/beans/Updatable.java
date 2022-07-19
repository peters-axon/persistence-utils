package com.axonivy.utils.persistence.beans;

import java.io.Serializable;

/**
 * Mark as updatable
 *
 * @param <S> type of id
 */
@FunctionalInterface
public interface Updatable<S extends Serializable> {

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public S getId();
}
