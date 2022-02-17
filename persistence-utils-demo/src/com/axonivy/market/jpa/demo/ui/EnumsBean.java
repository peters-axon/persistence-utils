package com.axonivy.market.jpa.demo.ui;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import com.axonivy.market.jpa.demo.enums.MaritalStatus;

@ManagedBean(name = "enums")
@ApplicationScoped
public class EnumsBean {

	public MaritalStatus[] getMaritalStatus() {
		return MaritalStatus.values();
	}
}
