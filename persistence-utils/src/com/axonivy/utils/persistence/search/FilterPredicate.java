package com.axonivy.utils.persistence.search;

import com.axonivy.utils.persistence.StringUtilities;


/**
 * A single predicate of a filter.
 *
 * Predicates are simple enumerations with one or more values. They need to be converted
 * to {@link AttributePredicates} to be usable in a search. Most predicates will only
 * have a single value, but sometimes it is necessary to have more (eg. for between queries).
 */
public class FilterPredicate {

	final Enum<?> searchFilter;
	private final String serializedValue;

	/**
	 * Static marker for the special value "NOT NULL".
	 */
	public static final String NOT_NULL = "NOT NULL";

	/**
	 * Construct predicate.
	 *
	 * @param searchFilter enumeration to identify filter
	 * @param value use for queries
	 */
	public FilterPredicate(Enum<?> searchFilter, Object value) {
		this.searchFilter = searchFilter;
		serializedValue = pack(value);
	}

	/**
	 * Construct predicate without value.
	 *
	 * @param searchFilter enumeration to identify filter
	 */
	public FilterPredicate(Enum<?> searchFilter) {
		this(searchFilter, null);
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
	 * Convenience function for unpacking a single String.
	 *
	 * @return the value
	 */
	public String getValue() {
		return unpack(String.class);
	}

	/**
	 * Get the value of this {@link FilterPredicate}.
	 *
	 * It is ok to pass array types. For example, to get an array of enums, you could use:
	 *
	 * <pre>
	 * <code>
	 *   MyEnum[] values = filterPredicate.getValue(MyEnum[].class);
	 * </code>
	 * </pre>
	 *
	 * Of course the type requested must match the type stored before.
	 *
	 * @param clazz class
	 * @param <T> the type of the represented object
	 * @return converted class
	 */
	public <T> T getValue(Class<T> clazz) {
		return unpack(clazz);
	}

	/**
	 * Has this predicate an associated value?
	 *
	 * @return boolean
	 */
	public boolean hasValue() {
		return serializedValue != null;
	}

	/**
	 * Gets the serialized value.
	 *
	 * @return the serializedValue
	 */
	protected String getSerializedValue() {
		return serializedValue;
	}

	/**
	 * Serialize value into Json string
	 *
	 * @param value The value to be serialized
	 * @return
	 */
	protected String pack(Object value) {
		return StringUtilities.fromObjectToJSON(value);
	}

	/**
	 *
	 * @return
	 */
	protected Object unpack() {
		return unpack(Object.class);
	}

	/**
	 * Get a value from the filter.
	 *
	 * @param clazz
	 * @return converted class
	 */
	protected <T extends Object> T unpack(Class<T> clazz) {
		return StringUtilities.fromJSONToObject(getSerializedValue(),clazz);
	}

	/**
	 * See {@link Object#toString()}.
	 *
	 */
	@Override
	public String toString() {
		return "FilterPredicate [searchFilter=" + searchFilter + ", value=" + getSerializedValue() + "]";
	}
}