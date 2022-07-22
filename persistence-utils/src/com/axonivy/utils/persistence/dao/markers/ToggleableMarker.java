package com.axonivy.utils.persistence.dao.markers;

import java.io.Serializable;

import com.axonivy.utils.persistence.enums.ToggleableStatus;

/**
 * Marker to ignore the enabled flag.
 */
public class ToggleableMarker implements Serializable, QueryMarker {

	private static final long serialVersionUID = 1L;

	/**
	 * set to see all, active and inactive
	 */
	public static final ToggleableMarker ALL = new ToggleableMarker(ToggleableStatus.ALL);

	/**
	 * set to see only active
	 */
	public static final ToggleableMarker ACTIVE = new ToggleableMarker(ToggleableStatus.ACTIVE);

	/**
	 * set to see only inactive
	 */
	public static final ToggleableMarker INACTIVE = new ToggleableMarker(ToggleableStatus.INACTIVE);

	private ToggleableStatus which = ToggleableStatus.ALL;

	/**
	 * Constructor
	 * @param which status to set
	 */
	public ToggleableMarker(ToggleableStatus which) {
		this.which = which;
	}

	/**
	 * Gets the which.
	 *
	 * @return the which
	 */
	public ToggleableStatus getWhich() {
		return which;
	}
}