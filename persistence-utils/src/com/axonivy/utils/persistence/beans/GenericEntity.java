package com.axonivy.utils.persistence.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.MappedSuperclass;

/**
 * Generic entity used a base for all persistent objects.
 *
 * This entity merely defines, that all persistent objects must have a primary key.
 */
@MappedSuperclass
public abstract class GenericEntity<S extends Serializable> implements Serializable {

	private static final long serialVersionUID = -6403037530514858618L;

	/**
	 * Get the bean's primary key.
	 *
	 * @return the id
	 */
	public abstract S getId();

	/**
	 * Set the bean's primary key.
	 *
	 * @param id the new id
	 */
	public abstract void setId(S id);
	
	/**
	 * Get the session username for modification information.
	 * 
	 * @return session username
	 */
	public abstract String getSessionUsername();

	/* (non-Javadoc)
	 * @see {@link Object#hashCode()}.
	 */
	@Override
	public int hashCode() {
		// 31 is prime number
		return 31 + (getId() == null ? 0 : getId().hashCode());
	}

	/**
	 * Equals method which allows to specify if null properties are considered equal
	 *  
	 * @param obj obj
	 * @param trueIfNull trueIfNull
	 * @return true if equal, or if getId null and trueIfNull param is specified
	 */
	public boolean equalsId(Object obj, boolean trueIfNull) {
		if (obj == this) {
			return true;
		} else if (obj == null || !this.getClass().isInstance(obj)) { //hibernate proxies dont work with  !getClass().isAssignableFrom(obj.getClass())
			return false;
		}

		GenericEntity<?> other = (GenericEntity<?>) obj;

		return getId() != null && other.getId() != null ? getId().equals(other.getId()) : trueIfNull;
	}
	
	/* (non-Javadoc)
	 * @see {@link Object#equals(Object)}.
	 */
	@Override
	public boolean equals(Object obj) {
		return equalsId(obj,false);// return true only if getId is filled
	}

	/**
	 * Convert a {@link List} of entities to a {@link Map} of entities.
	 *
	 * Use the primary key as the map-key.
	 *
	 * @param list list
	 * @param <I> the key of map
	 * @param <G> the value of map
	 * @return return
	 */
	public <I extends Serializable, G extends GenericEntity<I>> Map<I, G> listToMap(List<G> list) {
		Map<I, G> map = new HashMap<>();
		for (G entry : list) {
			map.put(entry.getId(), entry);
		}

		return map;
	}

	/**
	 * @return true if getId() is not null
	 */
	public boolean hasValidId() {
		return getId() != null;
	}

}
