package com.axonivy.utils.persistence.search;


/**
 * Interface marking an enum with cms support.
 *
 * @param <T> type of enum
 *
 */
public interface EnumWithFields<T extends Enum<T>> {

	/**
	 * Get CMS entry of enum.
	 *
	 * @return the cms name
	 */
	public String getCmsName();


	/**
	 * This methods provides the enum name in a programmatic friendly way
	 * @return String enum value
	 */
	@Override
	public String toString();

}
