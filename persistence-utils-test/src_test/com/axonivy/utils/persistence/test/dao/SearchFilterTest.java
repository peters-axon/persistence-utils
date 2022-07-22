package com.axonivy.utils.persistence.test.dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Tuple;

import org.dbunit.dataset.DataSetException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import com.axonivy.utils.persistence.daos.PersonDAO;
import com.axonivy.utils.persistence.enums.PersonSearchField;
import com.axonivy.utils.persistence.search.SearchFilter;
import com.axonivy.utils.persistence.test.DemoTestBase;


@RunWith(PowerMockRunner.class)
public class SearchFilterTest extends DemoTestBase {
	private static final PersonDAO personDAO = PersonDAO.getInstance();

	@Before
	public void prepare() throws DataSetException, FileNotFoundException, IOException  {
		switchToSystemUser();
		prepareTestDataAndMocking(true);
	}

	/**
	 * Test TG-871: SearchFilter: Sorting does not work for fields which are not part of the result tuple.
	 */
	@Test
	public void testSearchSortingWithoutResultField() {

		// first check for ascending sort order

		SearchFilter filter = new SearchFilter();

		// search including the sort field, sort ascending
		filter
		.add(PersonSearchField.BIRTHDATE)
		.add(PersonSearchField.SALARY)
		.add(PersonSearchField.IVY_USER_NAME);

		filter.addSort(PersonSearchField.IVY_USER_NAME, true);

		List<Tuple> personsInclUserNameAsc = personDAO.findBySearchFilter(filter);
		Assert.assertTrue("Find tuples, asc", personsInclUserNameAsc.size() > 300);

		// search excluding the sort field, sort ascending
		filter = new SearchFilter();
		filter
		.add(PersonSearchField.BIRTHDATE)
		.add(PersonSearchField.SALARY);

		filter.addSort(PersonSearchField.IVY_USER_NAME, true);

		List<Tuple> personsExclUserNameAsc = personDAO.findBySearchFilter(filter);
		Assert.assertTrue("Find tuples, asc", personsExclUserNameAsc.size() > 300);

		// sort order should match
		Assert.assertEquals("Same result size, asc", personsInclUserNameAsc.size(), personsExclUserNameAsc.size());
		Assert.assertArrayEquals("Same birthdates in same order, asc",
				personsInclUserNameAsc.stream().map(t -> t.get(0)).toArray(Date[]::new),
				personsExclUserNameAsc.stream().map(t -> t.get(0)).toArray(Date[]::new)
				);
		Assert.assertArrayEquals("Same salaries in same order, asc",
				personsInclUserNameAsc.stream().map(t -> t.get(1)).toArray(BigDecimal[]::new),
				personsExclUserNameAsc.stream().map(t -> t.get(1)).toArray(BigDecimal[]::new)
				);

		// now check for descending sort order

		// search including the sort field, sort descending
		filter
		.add(PersonSearchField.BIRTHDATE)
		.add(PersonSearchField.SALARY)
		.add(PersonSearchField.IVY_USER_NAME);

		filter.addSort(PersonSearchField.IVY_USER_NAME, false);

		List<Tuple> personsInclUserNameDesc = personDAO.findBySearchFilter(filter);
		Assert.assertTrue("Find tuples, desc", personsInclUserNameDesc.size() > 300);

		// search excluding the sort field, sort descending
		filter = new SearchFilter();
		filter
		.add(PersonSearchField.BIRTHDATE)
		.add(PersonSearchField.SALARY);

		filter.addSort(PersonSearchField.IVY_USER_NAME, false);

		List<Tuple> personsExclUserNameDesc = personDAO.findBySearchFilter(filter);
		Assert.assertTrue("Find tuples, desc", personsExclUserNameDesc.size() > 300);

		// sort order should match
		Assert.assertEquals("Same result size, desc", personsInclUserNameDesc.size(), personsExclUserNameDesc.size());
		Assert.assertArrayEquals("Same birthdates in same order, desc",
				personsInclUserNameDesc.stream().map(t -> t.get(0)).toArray(Date[]::new),
				personsExclUserNameDesc.stream().map(t -> t.get(0)).toArray(Date[]::new)
				);
		Assert.assertArrayEquals("Same salaries in same order, desc",
				personsInclUserNameDesc.stream().map(t -> t.get(1)).toArray(BigDecimal[]::new),
				personsExclUserNameDesc.stream().map(t -> t.get(1)).toArray(BigDecimal[]::new)
				);

		// now make sure, that sorting was performed at all, asc should not match desc
		Assert.assertNotEquals("Ascending and descending should differ", personsInclUserNameAsc.get(0), personsInclUserNameDesc.get(0));
	}
}
