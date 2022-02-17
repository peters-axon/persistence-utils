package com.axonivy.market.jpa.demo.enums;

import com.axonivy.market.jpa.demo.ivy.HasCmsName;

public enum MaritalStatus implements HasCmsName {
	SINGLE,
	MARRIED,
	WIDOWED,
	DIVORCED,
	PARTNERSHIP,
	PARTNER_PASSED_AWAY,
	PARTNERSHIP_CANCELED;
}
