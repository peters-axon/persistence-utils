package com.axonivy.utils.persistence.search;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.axonivy.utils.persistence.ReflectionUtilitities;
import com.axonivy.utils.persistence.beans.GenericEntity;

/**
 * Wrapper to allow findbyExample searches, but remain safe and decoupled
 *
 * @param <E> any GenericEntity
 */
public class FindByExample<E extends GenericEntity<? extends Serializable>> {

	private E e;
	private static final Logger LOG = Logger.getLogger(FindByExample.class);

	protected FindByExample(){
		//NO INIT NEEDED
	}

	protected FindByExample(E entity){
		this.setE(entity);

	}

	/**
	 * Construct a findByEXample instance suitable for searching for the generic class type.
	 * This find by example search does not support id , header  or Boolean properties
	 * @param clazz this is a bean class
	 * @param <E> the type of the instance
	 * @return return
	 */
	public static <E extends GenericEntity<?>> FindByExample<E> getInstance(Class<E> clazz){
		try {
			E newInstance = clazz.getDeclaredConstructor().newInstance();

			Map<String, Field> fieldMap = ReflectionUtilitities.getFieldMap(clazz);
			fieldMap.values().forEach(f->{ //remove boolean fields from findbyExample searches, they are often autoinitialized and cause the search not to work
				if(f.getType().equals(Boolean.class)){
					ReflectionUtilitities.clearField(newInstance, f);
				}
			});
			return new FindByExample<>(newInstance);
		} catch (IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			LOG.debug("getFindByExampleInstance could not create emptied example instance...",e);
		}

		return null;
	}

	/**
	 * Gets the e.
	 *
	 * @return the entity which is the example instance prefilled with properties to look for
	 */
	public E getE() {
		return e;
	}

	/**
	 * Sets the e.
	 *
	 * @param entity the entity to set
	 */
	public void setE(E entity) {
		this.e = entity;
	}
}
