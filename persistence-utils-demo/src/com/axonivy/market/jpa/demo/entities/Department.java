package com.axonivy.market.jpa.demo.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.axonivy.persistence.beans.AuditableEntity;

import com.axonivy.market.jpa.demo.validation.IvyNotBlank;
import com.axonivy.market.jpa.demo.validation.IvySizeMax;
import com.axonivy.market.jpa.demo.validation.groups.SaveErrorGroup;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "UC_Department_name", columnNames = {"name"}))
public class Department extends AuditableEntity {
	private static final long serialVersionUID = 1L;

	@Column(length = 64)
	@IvySizeMax(max = 32, clientId="departmentName", groups = {SaveErrorGroup.class})
	@IvyNotBlank(clientId="departmentName", groups = {SaveErrorGroup.class})
	private String name;

	@OneToMany(mappedBy = "department")
	private List<Person> persons;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


}
