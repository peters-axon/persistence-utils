package com.axonivy.utils.persistence.test.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axonivy.utils.persistence.dao.QuerySettings;
import com.axonivy.utils.persistence.daos.CarDAO;
import com.axonivy.utils.persistence.entities.Car;
import com.axonivy.utils.persistence.entities.Car_;
import com.axonivy.utils.persistence.test.DemoTestBase;

import ch.ivyteam.ivy.environment.IvyTest;


@IvyTest
public class CarDAOTest extends DemoTestBase {
	private static final CarDAO carDAO = CarDAO.getInstance();

	@BeforeEach
	public void prepare() throws Exception {
		switchToSystemUser();
		prepareTestDataAndMocking(true);
	}

	/**
	 * Test problem described in TG-781
	 * 
	 * Test, that an Order attribute can come from a base class.
	 * 
	 * @see TG-781
	 */
	@Test
	public void testSyntax() {
		carDAO.findAll(new QuerySettings<Car>().withOrderAttributes(Car_.name));
	}
}
