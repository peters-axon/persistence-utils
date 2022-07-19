package com.axonivy.utils.persistence.beans;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.annotations.Formula;


/**
 * Header to use for auditing persistent entities.
 *
 * The header can be embedded into a bean and stores information about creation, modification and deletion of a bean.
 */
@Embeddable
public class Header implements Serializable {

	private static final long serialVersionUID = -6330206771644279896L;

	@Formula("0")//any value as initialization of header is accepted
	private final int HEADERINITIALIZER = 0;//JPA nulls the header instance in auditable, if all properties in header are null, we have to create a dummy property so that header stays initialized all the time http://www.coderanch.com/t/629485/ORM/databases/columns-Embedded-field-NULL-JPA

	@Column(insertable = true, updatable = false)
	private Date createdDate;

	@Column
	private Date modifiedDate;

	@Column
	private Date flaggedDeletedDate;

	@Column(length = 255)
	private String createdByUserName;

	@Column(length = 255)
	private String modifiedByUserName;

	@Column(length = 255)
	private String flaggedDeletedByUserName;

	/**
	 * {@link Date} of creation.
	 *
	 * @return date of creation.
	 */
	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 * Set the creation date.
	 *
	 * @param createdDate creation date
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	/**
	 * {@link Date} of last modification.
	 *
	 * @return last modification date
	 */
	public Date getModifiedDate() {
		return modifiedDate;
	}

	/**
	 * Set last modification date.
	 *
	 * @param modifiedDate last modification date
	 */
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	/**
	 * {@link Date} of deletion.
	 *
	 * @return deletion date
	 */
	public Date getFlaggedDeletedDate() {
		return flaggedDeletedDate;
	}

	/**
	 * Set deletion date.
	 *
	 * @param flaggedDeletedDate deletion date
	 */
	public void setFlaggedDeletedDate(Date flaggedDeletedDate) {
		this.flaggedDeletedDate = flaggedDeletedDate;
	}

	/**
	 * Name of user who created the bean.
	 *
	 * @return name of bean creator
	 */
	public String getCreatedByUserName() {
		return createdByUserName;
	}

	/**
	 * Set name of user who created the bean.
	 *
	 * @param createdByUserName name of bean creator
	 */
	public void setCreatedByUserName(String createdByUserName) {
		this.createdByUserName = createdByUserName;
	}

	/**
	 * Name of user who last modified the bean.
	 *
	 * @return name of last bean modifier
	 */
	public String getModifiedByUserName() {
		return modifiedByUserName;
	}

	/**
	 * Set name of user who last modified the bean.
	 *
	 * @param modifiedByUserName name of last bean modifier
	 */
	public void setModifiedByUserName(String modifiedByUserName) {
		this.modifiedByUserName = modifiedByUserName;
	}

	/**
	 * Name of user who deleted the bean.
	 *
	 * @return name of user who deleted the bean
	 */
	public String getFlaggedDeletedByUserName() {
		return flaggedDeletedByUserName;
	}

	/**
	 * Set name of user who deleted the bean.
	 *
	 * @param flaggedDeletedByUserName name of user who deleted the bean
	 */
	public void setFlaggedDeletedByUserName(String flaggedDeletedByUserName) {
		this.flaggedDeletedByUserName = flaggedDeletedByUserName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return " [createdDate=" + createdDate + ", modifiedDate="
				+ modifiedDate + ", flaggedDeletedDate=" + flaggedDeletedDate
				+ ", createdByUserName=" + createdByUserName
				+ ", modifiedByUserName=" + modifiedByUserName
				+ ", flaggedDeletedByUserName=" + flaggedDeletedByUserName
				+ "]";
	}

}
