package com.axonivy.utils.persistence.history.beans;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * History entity, default schema must be defined in the application
 * persistence. E.g <b>hibernate.default_schema=dbo</b>
 * 
 * @author maonguyen
 *
 */
@Embeddable
public class HistoryPK implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column
	private String entityType;

	@Column
	private Timestamp timestamp;

	@Column
	private String entityId;

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

}
