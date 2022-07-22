package com.axonivy.utils.persistence.test.dao;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.dbunit.dataset.DataSetException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import com.axonivy.utils.persistence.dao.QuerySettings;
import com.axonivy.utils.persistence.daos.CarDAO;
import com.axonivy.utils.persistence.entities.Car;
import com.axonivy.utils.persistence.entities.Car_;
import com.axonivy.utils.persistence.test.DemoTestBase;


@RunWith(PowerMockRunner.class)
public class CarDAOTest extends DemoTestBase {
	private static final CarDAO carDAO = CarDAO.getInstance();

	@Before
	public void prepare() throws DataSetException, FileNotFoundException, IOException  {
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
