package com.axonivy.utils.persistence.demo.ui;

import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import com.axonivy.utils.persistence.dao.QuerySettings;
import com.axonivy.utils.persistence.demo.daos.PersonDAO;
import com.axonivy.utils.persistence.demo.entities.Person;
import com.axonivy.utils.persistence.demo.enums.PersonSearchField;
import com.axonivy.utils.persistence.search.SearchFilter;


public class PersonSearchLazyDataModel extends LazyDataModel<Tuple> {
	private static final long serialVersionUID = 1L;
	private List<Tuple> personTuples;


	public PersonSearchLazyDataModel() {
		super();
	}

	@Override
	public Tuple getRowData(String rowKey) {
		for (Tuple tuple : personTuples) {
			// Field 0 contains the ID.
			if (tuple.get(0).equals(rowKey)) {
				return tuple;
			}
		}
		return null;
	}

	@Override
	public String getRowKey(Tuple tuple) {
		// Field 0 contains the ID.
		return (String) tuple.get(0);
	}

	@Override
	public int count(Map<String, FilterMeta> filterBy) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Tuple> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
		PersonDAO dao = PersonDAO.getInstance();

		SearchFilter filter = new SearchFilter();

		// First field is used as row key and must be the ID (not displayed, therefore not searchable).

		filter
		.add(PersonSearchField.ID)
		.add(PersonSearchField.IVY_USER_NAME, filterValue(filterBy.get(PersonSearchField.IVY_USER_NAME.name())))
		.add(PersonSearchField.FIRST_NAME, filterValue(filterBy.get(PersonSearchField.FIRST_NAME.name())))
		.add(PersonSearchField.LAST_NAME, filterValue(filterBy.get(PersonSearchField.LAST_NAME.name())))
		.add(PersonSearchField.BIRTHDATE, filterValue(filterBy.get(PersonSearchField.BIRTHDATE.name())))
		.add(PersonSearchField.MARITAL_STATUS, filterValue(filterBy.get(PersonSearchField.MARITAL_STATUS.name())))
		.add(PersonSearchField.MARITAL_STATUS_LIKE, filterValue(filterBy.get(PersonSearchField.MARITAL_STATUS_LIKE.name())))
		.add(PersonSearchField.SALARY, filterValue(filterBy.get(PersonSearchField.SALARY.name())))
		.add(PersonSearchField.DEPARTMENT_NAME, filterValue(filterBy.get(PersonSearchField.DEPARTMENT_NAME.name())))
		.add(PersonSearchField.SYNC_TO_IVY, filterValue(filterBy.get(PersonSearchField.SYNC_TO_IVY.name())));

		if(sortBy != null) {
			for(SortMeta sortMeta : sortBy.values()) {
				filter.addSort(PersonSearchField.valueOf(sortMeta.getField()), sortMeta.getOrder() == SortOrder.ASCENDING ? true : false);
			}
		}

		// Handle paging.
		QuerySettings<Person> querySettings =
				new QuerySettings<Person>().withFirstResult(first).withMaxResults(pageSize);

		// Do the search.
		personTuples = dao.findBySearchFilter(filter, querySettings);

		// Do the counting with the same filter.
		long dataSize = dao.countBySearchFilter(filter);
		this.setRowCount((int) dataSize);

		return personTuples;
	}

	private Object filterValue(FilterMeta filterMeta) {
		return filterMeta != null ? filterMeta.getFilterValue() : null;
	}
}
