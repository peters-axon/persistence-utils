package com.axonivy.utils.persistence.entities;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Car extends Vehicle {
	private static final long serialVersionUID = 1L;

	@Column
	private double fuel;

	public double getFuel() {
		return fuel;
	}

	public void setFuel(double fuel) {
		this.fuel = fuel;
	}

}
