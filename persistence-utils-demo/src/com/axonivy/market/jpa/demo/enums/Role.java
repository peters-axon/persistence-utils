package com.axonivy.market.jpa.demo.enums;

import com.axonivy.market.jpa.demo.ivy.HasCmsName;

public enum Role implements HasCmsName {
	USER("User"),
	ADMINISTRATOR("Administrator");

	private String ivyRoleName;

	private Role(String ivyRoleName) {
		this.ivyRoleName = ivyRoleName;
	}
	
	public String getIvyRoleName() {
		return ivyRoleName;
	}
}
