package com.axonivy.utils.persistence.search;

/**
 * A single order of a filter.
 */
public class FilterOrder {

	private Enum<?> searchFilter;
	private boolean ascending;

	/**
	 * Constructor
	 */
	public FilterOrder() { // FilterOrder
	}

	/**
	 * Construct order.
	 *
	 * @param searchFilter enumeration to identify filter
	 * @param ascending the new ascending
	 */
	public FilterOrder(Enum<?> searchFilter, boolean ascending) {
		this.searchFilter = searchFilter;
		this.ascending = ascending;
	}

	/**
	 * Get new ascending order for search filter.
	 *
	 * @param searchFilter enumeration to identify filter
	 * @return the search filter
	 */
	public static FilterOrder getAscendingOrder(Enum<?> searchFilter) {
		return new FilterOrder(searchFilter, true);
	}

	/**
	 * Get new descending order for search filter.
	 *
	 * @param searchFilter enumeration to identify filter
	 * @return the search filter
	 */
	public static FilterOrder getDescendingOrder(Enum<?> searchFilter) {
		return new FilterOrder(searchFilter, false);
	}

	/**
	 * Get the filter enumeration.
	 *
	 * @return the search filter
	 */
	@SuppressWarnings("rawtypes")
	public Enum getSearchFilter() {
		return searchFilter;
	}

	/**
	 * Set the filter enumeration.
	 *
	 * @param searchFilter the new search filter
	 */
	public void setSearchFilter(Enum<?> searchFilter) {
		this.searchFilter = searchFilter;
	}

	/**
	 * Is this filter ascending?.
	 *
	 * @return <code>true</code> if ascending, <code>false</code> if descending.
	 */
	public boolean isAscending() {
		return ascending;
	}

	/**
	 * Set filter to ascending.
	 *
	 * @param ascending the new ascending
	 */
	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}

	/**
	 * See {@link Object#toString()}.
	 *
	 */
	@Override
	public String toString() {
		return "FilterOrder [searchFilter=" + searchFilter + ", ascending=" + ascending + "]";
	}
}