package com.axonivy.utils.persistence;

import java.io.Serializable;

import javax.persistence.metamodel.SingularAttribute;

import com.axonivy.utils.persistence.beans.GenericEntity;
/**
 *
 * @author Peter
 *
 * @param <T> entity
 */
public class SingularAttributeAndStringName<E extends GenericEntity<?>,T extends Serializable> {

	private SingularAttribute<E,T> attribute;
	private String attributeName;

	/**
	 * Initialize constructor with properties as specified
	 * 
	 * @param attribute represents persistent single-valued properties or fields
	 * @param attributeName the string name of attribute
	 */
	public SingularAttributeAndStringName(SingularAttribute<E,T> attribute, String attributeName) {
		this.attribute = attribute;
		this.attributeName = attributeName;
	}

	/**
	 *  constructor with all properties per default null
	 */
	public SingularAttributeAndStringName() {
		this.attribute = null;
		this.attributeName = null;
	}

	/**
	 * Gets the attribute.
	 *
	 * @return the attribute
	 */
	public SingularAttribute<E,T> getAttribute() {
		return attribute;
	}

	/**
	 * Sets the attribute.
	 *
	 * @param attribute the attribute to set
	 */
	public void setAttribute(SingularAttribute<E,T> attribute) {
		this.attribute = attribute;
	}

	/**
	 * Gets the attribute name.
	 *
	 * @return the attributeName
	 */
	public String getAttributeName() {
		return attributeName;
	}

	/**
	 * Sets the attribute name.
	 *
	 * @param attributeName the attributeName to set
	 */
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	/**
	 * See {@link Object#toString()}.
	 */
	@Override
	public String toString() {
		return getAttributeName();
	}

}
