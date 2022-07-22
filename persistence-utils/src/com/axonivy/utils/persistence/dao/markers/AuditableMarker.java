package com.axonivy.utils.persistence.dao.markers;

import java.io.Serializable;

import com.axonivy.utils.persistence.enums.AuditableStatus;

/**
 * Marker to ignore the deleted status.
 */
public class AuditableMarker implements Serializable, QueryMarker {

	private static final long serialVersionUID = 1L;
	/**
	 * set to see all, active and inactive
	 */
	public static final  AuditableMarker ALL = new AuditableMarker(AuditableStatus.ALL);
	/**
	 * set to see only active
	 */
	public static final AuditableMarker ACTIVE = new AuditableMarker(AuditableStatus.ACTIVE);
	/**
	 * set to see only inactive
	 */
	public static final AuditableMarker DELETED = new AuditableMarker(AuditableStatus.DELETED);
	private AuditableStatus which = AuditableStatus.ALL;

	/**
	 * Constructor
	 * @param which status to set
	 */
	public AuditableMarker(AuditableStatus which) {
		this.which = which;
	}

	/**
	 * Gets the which.
	 *
	 * @return the which
	 */
	public AuditableStatus getWhich() {
		return which;
	}
}