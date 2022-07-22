package com.axonivy.utils.persistence.test.dao;

import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.persistence.PersistenceException;
import javax.persistence.Tuple;
import javax.transaction.TransactionRolledbackException;

import org.apache.log4j.Level;
import org.dbunit.dataset.DataSetException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import com.axonivy.utils.persistence.daos.PersonDAO;
import com.axonivy.utils.persistence.entities.Department;
import com.axonivy.utils.persistence.entities.Person;
import com.axonivy.utils.persistence.enums.PersonSearchField;
import com.axonivy.utils.persistence.search.SearchFilter;
import com.axonivy.utils.persistence.test.DemoTestBase;


@RunWith(PowerMockRunner.class)
public class PersonDAOTest extends DemoTestBase {
	private static final PersonDAO personDAO = PersonDAO.getInstance();
	private static final String userLeitung = "gera.dewegs";

	@Before
	public void prepare() throws DataSetException, FileNotFoundException, IOException  {
		switchToSystemUser();
		prepareTestDataAndMocking(true);
	}

	@Test
	public void testLoadTestdata() {
		switchOnLogging(Level.INFO);

		List<Person> all = personDAO.findAll();
		Assert.assertTrue("Found entries", all.size() > 300);

		for (Person person : all) {
			LOG.info(String.format("Person: %-20s %-20s %tF %-20s %9.2f",
					person.getFirstName(), person.getLastName(), person.getBirthdate(),
					person.getMaritalStatus(), person.getSalary()));
		}
	}

	@Test
	public void testLoadPermissions() {
		switchOnLogging(Level.INFO, packageLevelCombine(packageLevelHibernateSqlStatements(), packageLevelHibernateSqlParameters()));
		createUser(userLeitung, "Hans", "Huber", "password");
		switchToUser(userLeitung);
		List<Person> all = personDAO.findAll();
		Assert.assertTrue("Found entries", all.size() > 5);

		for (Person person : all) {
			LOG.info(String.format("Person: %-20s %-20s %tF %-20s %9.2f",
					person.getFirstName(), person.getLastName(), person.getBirthdate(),
					person.getMaritalStatus(), person.getSalary()));
			Assert.assertEquals("Correct department", "Leitung", person.getDepartment().getName());
		}
	}


	@Test
	@Ignore
	public void testData() throws DataSetException, FileNotFoundException, IOException {
		LOG.info("Writig Excel file");
		testDemoDao.exportTablesToExcel("T:/tmp/exported.xls", Person.class.getSimpleName(), Department.class.getSimpleName());
		LOG.info("Wrote Excel file");
	}

	@Test
	public void testSearch() {
		SearchFilter filter = new SearchFilter();

		filter
		.add(PersonSearchField.ID)
		.add(PersonSearchField.IVY_USER_NAME)
		.add(PersonSearchField.FIRST_NAME)
		.add(PersonSearchField.LAST_NAME)
		.add(PersonSearchField.BIRTHDATE)
		.add(PersonSearchField.MARITAL_STATUS)
		.add(PersonSearchField.SALARY)
		.add(PersonSearchField.DEPARTMENT_NAME);

		filter.addSort(PersonSearchField.LAST_NAME, true).addSort(PersonSearchField.FIRST_NAME, true);

		List<Tuple> persons = personDAO.findBySearchFilter(filter);

		logTuples("Persons", persons, -30);

		Assert.assertTrue("Find tuples", persons.size() > 300);
	}

	@Test
	public void testSearchIndividual() {
		switchOnLogging(Level.INFO,
				packageLevelHibernateSqlStatements(),
				packageLevelHibernateSqlParameters(),
				packageLevel("com.axonivy", Level.INFO));

		SearchFilter filter = new SearchFilter();

		filter.add(PersonSearchField.ID)
		.add(PersonSearchField.IVY_USER_NAME, "er");

		List<Tuple> persons = personDAO.findBySearchFilter(filter);

		logTuples("Persons", persons, -30);

		Assert.assertTrue("Find tuples", persons.size() > 100);
	}

	@Test
	public void testTransactions() throws TransactionRolledbackException {

		Person person = personDAO.findByIvyUserName(userLeitung, null);
		Assert.assertNotNull("Find person.", person);
		Department leitung = person.getDepartment();
		Assert.assertNotNull("Find department.", leitung);

		personDAO.beginSession();
		personDAO.beginTransaction();

		person = new Person();
		person.setIvyUserName("ivyname1");
		person.setDepartment(leitung);
		personDAO.save(person);

		person = new Person();
		person.setIvyUserName("ivyname2");
		person.setDepartment(leitung);
		personDAO.save(person);

		Assert.assertNotNull("Find person.", personDAO.findByIvyUserName("ivyname1", null));
		Assert.assertNotNull("Find person.", personDAO.findByIvyUserName("ivyname2", null));

		personDAO.rollbackTransaction();
		personDAO.closeSession();

		Assert.assertNull("Rolledback person.", personDAO.findByIvyUserName("ivyname1", null));
		Assert.assertNull("Rolledback person.", personDAO.findByIvyUserName("ivyname2", null));
	}

	@Test
	public void testConstraintViolation() {
		Person person = personDAO.findByIvyUserName(userLeitung, null);
		Assert.assertNotNull("Find person.", person);
		Department leitung = person.getDepartment();
		Assert.assertNotNull("Find department.", leitung);

		person = new Person();
		person.setIvyUserName("duplicatename");
		person.setDepartment(leitung);
		personDAO.save(person);

		person = new Person();
		person.setIvyUserName("duplicatename");
		person.setDepartment(leitung);

		try {
			personDAO.save(person);
			fail("Expected " + PersistenceException.class.getSimpleName() + " while saving person with duplicate ivy username");
		} catch(PersistenceException e) {
			LOG.info("Found expected Exception: {0}", e.getMessage());
		}

		person.setId(null);
		person.setVersion(null);
		person.setIvyUserName("uniquename");
		person = personDAO.save(person);

		try {
			person.setIvyUserName("duplicatename");
			person = personDAO.save(person);
			fail("Expected " + PersistenceException.class.getSimpleName() + " while updating person with duplicate ivy username");
		} catch(PersistenceException e) {
			LOG.info("Found expected Exception: {0}", e.getMessage());
		}
	}

	@Test
	public void testUnproxy() {
		Person person = personDAO.findByIvyUserName(userLeitung, null);
		Assert.assertNotNull("Find person.", person);
		Department leitung = person.getDepartment();
		Assert.assertNotEquals("Not a Department", Department.class, leitung.getClass());

		leitung = personDAO.unproxy(leitung);
		Assert.assertEquals("A Department", Department.class, leitung.getClass());
	}

}
