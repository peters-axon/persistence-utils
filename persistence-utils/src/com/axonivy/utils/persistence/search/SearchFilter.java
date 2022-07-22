package com.axonivy.utils.persistence.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Search filter.
 *
 * A search filter combines {@link FilterPredicate}s and {@link FilterOrder}s.
 *
 * @author peter
 *
 */
public class SearchFilter {

	private final List<FilterPredicate> filterPredicates = new ArrayList<>();
	private final List<FilterOrder> filterOrders = new ArrayList<>();
	private final Map<Enum<?>, FilterPredicate> predicateMap = new HashMap<>();

	/**
	 * Constructor.
	 */
	public SearchFilter() {
		//no init needed

	}

	/**
	 * Create filter and add {@link FilterPredicate}s.
	 *
	 * @param filterPredicates a single predicate of a filter
	 */
	public SearchFilter(FilterPredicate... filterPredicates) {
		internalAddPredicates(Arrays.asList(filterPredicates));
	}

	/**
	 * Add {@link FilterPredicate}s.
	 *
	 * @param filterPredicates A single predicate of a filter
	 * @return object
	 */
	public SearchFilter add(FilterPredicate... filterPredicates) {
		internalAddPredicates(Arrays.asList(filterPredicates));
		return this;
	}

	/**
	 * add {@link FilterOrder}s.
	 *
	 * @param filterOrders a single order of a filter.
	 * @return object
	 */
	public SearchFilter add(FilterOrder... filterOrders) {
		this.filterOrders.addAll(Arrays.asList(filterOrders));
		return this;
	}


	private void internalAddPredicates(List<FilterPredicate> predicates) {
		filterPredicates.addAll(predicates);
		for (FilterPredicate filterPredicate : predicates) {
			predicateMap.put(filterPredicate.getSearchFilter(), filterPredicate);
		}
	}

	/**
	 * Convenience function to add a {@link FilterPredicate}.
	 *
	 * @param searchFilter enumeration to identify filter
	 * @return object
	 */
	public SearchFilter add(Enum<?> searchFilter) {
		add(new FilterPredicate(searchFilter, null));
		return this;
	}

	/**
	 * Convenience function to add a {@link FilterPredicate}.
	 *
	 * @param searchFilter enumeration to identify filter
	 * @param value object
	 * @return constructed searchFilter
	 */
	public SearchFilter add(Enum<?> searchFilter, Object value) {
		add(new FilterPredicate(searchFilter, value));
		return this;
	}

	/**
	 * Convenience function to add a {@link FilterOrder}.
	 *
	 * @param searchFilter enumeration to identify filter
	 * @param ascending the new ascending
	 * @return object
	 */
	public SearchFilter addSort(Enum<?> searchFilter, boolean ascending) {
		add(new FilterOrder(searchFilter, ascending));
		return this;
	}

	/**
	 * Get all {@link FilterPredicate}s.
	 *
	 * @return the filter predicates
	 */
	public List<FilterPredicate> getFilterPredicates() {
		return Collections.unmodifiableList(filterPredicates);
	}

	/**
	 * Get all {@link FilterOrder}s.
	 *
	 * @return the filter orders
	 */
	public List<FilterOrder> getFilterOrders() {
		return Collections.unmodifiableList(filterOrders);
	}

	/**
	 * Get {@link FilterPredicate} by enum value;
	 *
	 * @param searchFilter enumeration to identify filter
	 * @return object
	 */
	public FilterPredicate getFilterPredicate(Enum<?> searchFilter) {
		return predicateMap.get(searchFilter);
	}
}