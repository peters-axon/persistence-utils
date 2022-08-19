package com.axonivy.utils.persistence.test.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Tuple;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axonivy.utils.persistence.daos.PersonDAO;
import com.axonivy.utils.persistence.enums.PersonSearchField;
import com.axonivy.utils.persistence.search.SearchFilter;
import com.axonivy.utils.persistence.test.DemoTestBase;

import ch.ivyteam.ivy.environment.IvyTest;


@IvyTest
public class SearchFilterTest extends DemoTestBase {
	private static final PersonDAO personDAO = PersonDAO.getInstance();

	@BeforeEach
	public void prepare() throws Exception {
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
		assertThat(personsInclUserNameAsc).as("Find tuples, asc").hasSizeGreaterThan(300);
		// search excluding the sort field, sort ascending
		filter = new SearchFilter();
		filter
		.add(PersonSearchField.BIRTHDATE)
		.add(PersonSearchField.SALARY);

		filter.addSort(PersonSearchField.IVY_USER_NAME, true);

		List<Tuple> personsExclUserNameAsc = personDAO.findBySearchFilter(filter);
		assertThat(personsExclUserNameAsc).as("Find tuples, asc").hasSizeGreaterThan(300);

		// sort order should match
		assertThat(personsExclUserNameAsc.size()).as("Same result size, asc").isEqualTo(personsInclUserNameAsc.size());
		Assertions.assertArrayEquals(personsInclUserNameAsc.stream().map(t -> t.get(0)).toArray(Date[]::new), 
				personsExclUserNameAsc.stream().map(t -> t.get(0)).toArray(Date[]::new), 
				"Same birthdates in same order, asc");
		Assertions.assertArrayEquals(personsInclUserNameAsc.stream().map(t -> t.get(1)).toArray(BigDecimal[]::new),
				personsExclUserNameAsc.stream().map(t -> t.get(1)).toArray(BigDecimal[]::new), 
				"Same salaries in same order, asc");
		// now check for descending sort order

		// search including the sort field, sort descending
		filter
		.add(PersonSearchField.BIRTHDATE)
		.add(PersonSearchField.SALARY)
		.add(PersonSearchField.IVY_USER_NAME);

		filter.addSort(PersonSearchField.IVY_USER_NAME, false);

		List<Tuple> personsInclUserNameDesc = personDAO.findBySearchFilter(filter);
		assertThat(personsInclUserNameDesc).as("Find tuples, desc").hasSizeGreaterThan(300);

		// search excluding the sort field, sort descending
		filter = new SearchFilter();
		filter
		.add(PersonSearchField.BIRTHDATE)
		.add(PersonSearchField.SALARY);

		filter.addSort(PersonSearchField.IVY_USER_NAME, false);

		List<Tuple> personsExclUserNameDesc = personDAO.findBySearchFilter(filter);
		assertThat(personsExclUserNameDesc).as("Find tuples, desc").hasSizeGreaterThan(300);
		// sort order should match
		assertThat(personsExclUserNameDesc.size()).as("Same result size, desc").isEqualTo(personsInclUserNameDesc.size());
		Assertions.assertArrayEquals(personsInclUserNameDesc.stream().map(t -> t.get(0)).toArray(Date[]::new), 
				personsExclUserNameDesc.stream().map(t -> t.get(0)).toArray(Date[]::new), 
				"Same birthdates in same order, desc");
		Assertions.assertArrayEquals(personsInclUserNameDesc.stream().map(t -> t.get(1)).toArray(BigDecimal[]::new),
				personsExclUserNameDesc.stream().map(t -> t.get(1)).toArray(BigDecimal[]::new), 
				"Same salaries in same order, desc");
		// now make sure, that sorting was performed at all, asc should not match desc
		assertThat(personsInclUserNameDesc.get(0))
			.as("Ascending and descending should differ")
			.isNotEqualTo(personsInclUserNameAsc.get(0));
	}
}
