package com.axonivy.utils.persistence.beans;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Base for all entities which can be enabled and disabled.
 *
 */
@MappedSuperclass
public abstract class ToggleableEntity extends AuditableEntity {

	private static final long serialVersionUID = 5872211233738039349L;

	@Column(nullable = false)
	private Boolean isEnabled;

	@Column
	@Temporal(TemporalType.DATE)
	private Date expiryDate;

	
	/**
	 * default constructor
	 */
	public ToggleableEntity() {
		isEnabled = true;
	}

	/**
	 * Should not be used for querying if toggleable is really allowed to be used, there is also a expiry date to be considered, thats why you should usually use isActive.
	 *
	 * @return true if enabled flag is set to true
	 */
	public Boolean getIsEnabled() {
		return isEnabled;
	}

	/**
	 * Sets the checks if is enabled.
	 *
	 * @param isEnabled the new checks if is enabled
	 */
	public void setIsEnabled(Boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	/**
	 * Gets the expiry date.
	 *
	 * @return the expiry date
	 */
	public Date getExpiryDate() {
		return expiryDate;
	}

	/**
	 * Sets the expire date.
	 *
	 * @param expiryDate the new expire date
	 */
	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	/**
	 * Checks if is expired.
	 *
	 * @return true if expiryDate is set and is before
	 */
	public boolean isExpired() {
		return expiryDate != null && expiryDate.before(new Date());
	}

	/**
	 * Combines the active state and the expire date.
	 *
	 * @return true, if is active
	 */
	public boolean isActive() {
		return getIsEnabled() != null && getIsEnabled() && !isExpired();
	}

}
