package com.axonivy.utils.persistence.beans;

import java.io.Serializable;

/**
 * Interface marking a class supporting the getId string method
 *
 */
public interface GetterIdString extends Serializable {

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId();

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(String id);
}
