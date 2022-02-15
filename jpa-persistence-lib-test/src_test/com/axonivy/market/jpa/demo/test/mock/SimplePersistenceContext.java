package com.axonivy.market.jpa.demo.test.mock;

import java.util.Map;

import com.axonivy.market.jpa.demo.test.IvyEntityManager;
import ch.ivyteam.ivy.process.data.persistence.IIvyEntityManager;
import ch.ivyteam.ivy.process.data.persistence.IPersistenceContext;

public class SimplePersistenceContext implements IPersistenceContext {

	public static final String JPA_DEMO = "jpa_demo";
	public static final String JPA_DEMO_TEST = "jpa_demo_test";

	private static final Map<String, String> puTestMap =
			Map.of(
					JPA_DEMO, JPA_DEMO_TEST
					);


	public SimplePersistenceContext() {
		IvyEntityManager.initialize();
	}

	@Override
	public IIvyEntityManager get(String persistenceUnitName) {
		// map PU to test PU
		String mappedPersistenceUnit = puTestMap.get(persistenceUnitName);
		return new IvyEntityManager(mappedPersistenceUnit != null ? mappedPersistenceUnit : persistenceUnitName);	
	}

}
