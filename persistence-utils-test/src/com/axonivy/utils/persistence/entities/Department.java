package com.axonivy.utils.persistence.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.axonivy.utils.persistence.beans.AuditableEntity;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "UC_Department_name", columnNames = {"name"}))
public class Department extends AuditableEntity {
	private static final long serialVersionUID = 1L;

	@Column(length = 64)
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
