package com.axonivy.utils.persistence.test.dao;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Level;
import org.dbunit.dataset.DataSetException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import com.axonivy.utils.persistence.daos.DepartmentDAO;
import com.axonivy.utils.persistence.entities.Department;
import com.axonivy.utils.persistence.test.DemoTestBase;


@RunWith(PowerMockRunner.class)
public class DepartmentDAOTest extends DemoTestBase {
	private static final DepartmentDAO departmentDAO = DepartmentDAO.getInstance();

	@Before
	public void prepare() throws Exception {
		prepareTestDataAndMocking(true);
		switchToSystemUser();
	}

	@Test
	public void testData() throws DataSetException, FileNotFoundException, IOException {
		switchOnLogging(Level.INFO, packageLevel("com.axonivy", Level.INFO));

		Department einkauf = departmentDAO.findByName("Einkauf");
		Department leitung = departmentDAO.findByName("Leitung");
		Department marketing = departmentDAO.findByName("Marketing");
		Department produktion = departmentDAO.findByName("Produktion");
		Department vertrieb = departmentDAO.findByName("Vertrieb");

		Assert.assertNotNull("Find Einkauf", einkauf);
		Assert.assertNotNull("Find Leitung", leitung);
		Assert.assertNotNull("Find Marketing", marketing);
		Assert.assertNotNull("Find Produktion", produktion);
		Assert.assertNotNull("Find Vertrieb", vertrieb);
	}
}
