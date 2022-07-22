package com.axonivy.utils.persistence.beans;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

@MappedSuperclass
public abstract class VersionableEntity<ID extends Serializable> extends GenericEntity<ID> {

	private static final long serialVersionUID = -5710812302031332814L;

	@Version
	@Column
	private Integer version;

	/**
	 * Get the version of the entity.
	 *
	 * The version is used to implement optimistic locking.
	 *
	 * @return the version of the object
	 */
	public Integer getVersion() {
		return version;
	}

	/**
	 * Set the version of the object.
	 *
	 * This function is for internal use.
	 *
	 * @param version the version of the object
	 */
	public void setVersion(Integer version) {
		this.version = version;
	}
}
