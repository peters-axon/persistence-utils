package com.axonivy.utils.persistence.history.beans;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

import com.axonivy.utils.persistence.beans.GenericEntity;

import ch.ivyteam.ivy.environment.Ivy;


/**
 * History entity, default schema must be defined in the application
 * persistence. E.g <b>hibernate.default_schema=dbo</b>
 * 
 * @author maonguyen
 *
 */
@Entity
public class History extends GenericEntity<HistoryPK> {
	private static final long serialVersionUID = 1L;

	@Id
	private HistoryPK id;

	@Column
	private String userName;

	@Column(length = 65535) // As the Lob type defaults to Blob with maximum length of 65,535 bytes
	@Lob
	private String jsonData;

	@Column
	private String updateType;
	
	@Override
	public HistoryPK getId() {
		return id;
	}

	@Override
	public void setId(HistoryPK id) {
		this.id = id;
	}

	@Override
	public String getSessionUsername() {
		return Ivy.session().getSessionUserName();
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getJsonData() {
		return jsonData;
	}

	public void setJsonData(String jsonData) {
		this.jsonData = jsonData;
	}

	public String getUpdateType() {
		return updateType;
	}

	public void setUpdateType(String updateType) {
		this.updateType = updateType;
	}
}
