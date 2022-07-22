package com.axonivy.utils.persistence.entities;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.axonivy.utils.persistence.annotations.Audit;
import com.axonivy.utils.persistence.beans.AuditableEntity;
import com.axonivy.utils.persistence.daos.AuditHandler;

@Entity
@Audit(handler = AuditHandler.class)
public class HistorizedPerson extends AuditableEntity {

	/**
	 * auto generated id
	 */
	private static final long serialVersionUID = -8255457957691314129L;
	
	@Column(length = 32)
	private String firstName;
	
	@Column(length = 64)
	private String LastName;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return LastName;
	}

	public void setLastName(String lastName) {
		LastName = lastName;
	}
	
}
