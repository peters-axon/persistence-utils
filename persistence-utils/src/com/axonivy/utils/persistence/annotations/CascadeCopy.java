package com.axonivy.utils.persistence.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.persistence.CascadeType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;


/**
 * Fields annotated won't be copied with a deep copy.
 *
 * This annotation is honored by AbstractDAO.#deepCopy(Object).
 *
 * @author peter
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CascadeCopy {
	/**
	 * empty null object , no reference
	 */
	public static final String NOREF = "";


	/**
	 * Ignore this field in cascade copy.
	 *
	 * @return boolean
	 */
	boolean ignore() default false;

	/**
	 * This field should be a reference to a previously cascade copied field.
	 *
	 *  Example:
	 *
	 *  A {@link OneToMany} relation with {@link CascadeType#ALL} and a specific
	 *  {@link OneToOne} relation to one of the list elements. The {@link OneToOne}
	 *  relation should not be annotated with {@link CascadeType#ALL} but with
	 *  reference=true. This will cause the algorithm to copy the field in the
	 *  end, when the list elements were already copied and the value can be found
	 *  in the field cache.
	 *
	 *  The field will therefore be a reference to the previously copied list field.
	 *
	 * @return string
	 */
	String reference() default NOREF;

	/**
	 * Ignore this field if the copy call has any of these ignore groups parameter.
	 *
	 * @return class
	 */
	public Class<?>[] ignoreGroups() default {};
}
