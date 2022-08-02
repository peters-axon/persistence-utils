package com.axonivy.utils.persistence.daos;

import com.axonivy.utils.persistence.history.handler.DefaultAbstractAuditHandler;

public class AuditHandler extends DefaultAbstractAuditHandler {

	@Override
	public String getHandlerPersistenceUnitName() {
		return "jpa_demo_test";
	}

}
