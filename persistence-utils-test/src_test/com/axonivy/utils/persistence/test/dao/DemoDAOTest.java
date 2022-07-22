package com.axonivy.utils.persistence.test.dao;


public class DemoDAOTest extends DemoDAO {
	private static final DemoDAOTest instance = new DemoDAOTest();

	private DemoDAOTest() {
	}

	public static DemoDAOTest getInstance() {
		return instance;
	}
}
