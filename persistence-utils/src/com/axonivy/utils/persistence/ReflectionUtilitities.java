package com.axonivy.utils.persistence;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Table;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.axonivy.utils.persistence.beans.GenericEntity;
import com.axonivy.utils.persistence.logging.Logger;

/**
 * Helper metods for java reflection
 *
 */
public class ReflectionUtilitities {

	private static Map<Class<?>, Map<String, Field>> columnFieldsByClass = new HashMap<>();
	private static Map<Class<?>, Map<String, Field>> fieldsByClass = new HashMap<>();
	private static final Logger LOG = Logger.getLogger(ReflectionUtilitities.class);

	private ReflectionUtilitities() {
		// hide the public constructor
	}

	/**
	 * Recursively walk through object and build a map of most specific versions of
	 * {@link Field}s.
	 *
	 * I.e. if a field is defined in a sub-class, the "upper-most" version will be
	 * taken.
	 *
	 * Maps are cached and will be reused if called again.
	 *
	 * @param object the object
	 * @return a map represent of most specific versions of {@link Field}s
	 */
	public static Map<String, Field> getFieldMap(Object object) {
		return getFieldMap(object.getClass());
	}

	/**
	 * Recursively walk through class and build a map of most specific versions of
	 * {@link Field}s.
	 *
	 * Maps are cached and will be reused if called again.
	 *
	 * I.e. if a field is defined in a sub-class, the "upper-most" version will be
	 * taken.
	 *
	 * @param clazz the class
	 * @return a map represent of most specific versions of {@link Field}s
	 */
	public static Map<String, Field> getFieldMap(Class<?> clazz) {
		Map<String, Field> fieldMap = fieldsByClass.get(clazz);
		if (fieldMap == null) {
			fieldMap = internalGetFieldMap(clazz, null);
			synchronized (fieldsByClass) {
				fieldsByClass.put(clazz, fieldMap);
			}
		}

		return fieldMap;
	}

	/**
	 * Recursively walk through class and build a map of most specific versions of
	 * {@link Field}s.
	 *
	 * I.e. if a field is defined in a sub-class, the "upper-most" version will be
	 * taken.
	 *
	 * @param clazz the class
	 * @param annotations if present, only deliver fields with at least one of these annotations
	 * @return a map represent of most specific versions of {@link Field}s
	 */
	public static Map<String, Field> getFieldMap(Class<?> clazz,
			@SuppressWarnings("unchecked") Class<? extends Annotation>... annotations) {

		Map<String, Field> result = new HashMap<>();
		Map<String, Field> fieldMap = getFieldMap(clazz);

		for (Entry<String, Field> entry : fieldMap.entrySet()) {
			String key = entry.getKey();
			Field value = entry.getValue();
			for (Class<? extends Annotation> annotation : annotations) {
				if (value.isAnnotationPresent(annotation)) {
					result.put(key, value);
					break;
				}
			}
		}

		return result;
	}

	/**
	 * Recursively walk through class and collect most specific versions of
	 * {@link Field}s.
	 *
	 * @param clazz the class
	 * @param oldMap the map
	 * @return a map represent of most specific versions of {@link Field}s
	 */
	private static Map<String, Field> internalGetFieldMap(Class<?> clazz,
			Map<String, Field> oldMap) {
		Map<String, Field> map = oldMap;
		if (map == null) {
			map = new HashMap<>();
		}

		for (Field field : clazz.getDeclaredFields()) {
			String name = field.getName();
			Field storedField = map.get(name);

			if (storedField == null) {
				map.put(name, field);
			}
		}

		Class<?> superClass = clazz.getSuperclass();
		if (superClass != null) {
			internalGetFieldMap(superClass, map);
		}

		return map;
	}

	/**
	 * Get a list of method names with a given prefix.
	 *
	 * Each method name will only appear once, even if there exist different methods
	 * with the same name. The list is sorted by name.
	 *
	 * @param clazz the class
	 * @param prefix the prefix
	 * @return list of method names with a given prefix
	 */
	public static List<String> getPrefixedMethodNames(Class<?> clazz,
			String prefix) {
		@SuppressWarnings("unchecked")
		Set<String> methodNameSet = new HashSet<>(CollectionUtils.collect(
				getPrefixedMethods(clazz, prefix),
				method -> ((Method) method).getName()));

		List<String> methodList = new ArrayList<>(methodNameSet);
		Collections.sort(methodList);

		return methodList;
	}

	/**
	 * Get a list of methods with a given prefix.
	 *
	 * @param clazz the class
	 * @param prefix the prefix
	 * @return list of method names with a given prefix
	 */
	public static List<Method> getPrefixedMethods(Class<?> clazz, String prefix) {
		Method[] methods = clazz.getMethods();

		List<Method> methodList = new ArrayList<>();

		for (Method method : methods) {
			if (method.getName().startsWith(prefix)) {
				methodList.add(method);
			}
		}
		return methodList;
	}

	/**
	 * Get the name of a fields getter method.
	 *
	 * @param fieldName field name of the field that need to get the getter
	 * @return the name of getter method
	 */
	public static String getGetterMethodName(String fieldName) {
		return "get" + StringUtils.capitalize(fieldName);
	}

	/**
	 * Get the name of a field setter method.
	 *
	 * @param fieldName field name of the field that need to get the setter
	 * @return the name of a fields setter method
	 */
	public static String getSetterMethodName(String fieldName) {
		return "set" + StringUtils.capitalize(fieldName);
	}

	/**
	 * Get the name of a field getter method.
	 *
	 * @param field: field name that need to get the getter
	 * @return the name of a fields getter method
	 */
	public static String getGetterMethodName(Field field) {
		return getGetterMethodName(field.getName());
	}

	/**
	 * Get a fields getter method.
	 * 
	 * @param clazz the class
	 * @param fieldName field name that need to get the getter
	 * @return getter Method for fieldName
	 *
	 * @throws SecurityException thrown by the security manager to indicate a security violation
	 * @throws NoSuchMethodException thrown when a particular method cannot be found
	 */
	public static Method getGetterMethod(Class<?> clazz, String fieldName)
			throws NoSuchMethodException {
		return clazz.getMethod(getGetterMethodName(fieldName));
	}

	/**
	 * Get a fields setter method.
	 *
	 * @param clazz the class
	 * @param fieldName field name that need to get the setter
	 * @param type the list of parameters
	 * @return setter Method for fieldName
	 * @throws NoSuchMethodException thrown when a particular method cannot be found
	 */
	public static Method getSetterMethod(Class<?> clazz, String fieldName,
			Class<?> type) throws NoSuchMethodException {
		return clazz.getMethod(getSetterMethodName(fieldName), type);
	}

	/**
	 * Get a fields getter method.
	 *
	 * @param clazz the class
	 * @param field object that need to get the getter
	 * @return getter Method for fieldName
	 * @throws NoSuchMethodException thrown when a particular method cannot be found
	 */
	public static Method getGetterMethod(Class<?> clazz, Field field)
			throws NoSuchMethodException {
		return getGetterMethod(clazz, field.getName());
	}

	/**
	 * Return a field by invoking its getter method.
	 *
	 * @param object the object
	 * @param fieldName field name that need to get the value
	 * @return the result of dispatching the method represented by this object on obj with parameters args
	 * @throws NoSuchMethodException thrown when a particular method cannot be found
	 */
	public static Object getValue(Object object, String fieldName)
			throws NoSuchMethodException {
		Method method = getGetterMethod(object.getClass(), fieldName);
		if (method != null) {
			try {
				return method.invoke(object);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				LOG.debug("getter method problem ", e);
			}
		}
		throw new NoSuchMethodException("getter method for "
				+ object.getClass().getCanonicalName() + "." + fieldName
				+ " is null");

	}

	/**
	 * Set a field by invoking its setter method.
	 *
	 * @param object the object
	 * @param fieldName field name that need to set the value
	 * @param value the list of parameters
	 * @param <T> the type of the represented object
	 * @throws NoSuchMethodException thrown when a particular method cannot be found
	 */
	public static <T extends Object> void setValue(Object object,
			String fieldName, T value) throws NoSuchMethodException {
		Method method = getSetterMethod(object.getClass(), fieldName,
				value.getClass());
		if (method != null) {
			try {
				method.invoke(object, value);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				LOG.error("setter method error", e);
			}
		}

		throw new NoSuchMethodException("setter method for "
				+ object.getClass().getCanonicalName() + "." + fieldName
				+ " is null");

	}

	/**
	 * Return a field by invoking its getter method.
	 *
	 * @param object the object
	 * @param field field name that need to get the value
	 * @return the result of dispatching the method represented by this object on obj with parameters args
	 * @throws NoSuchMethodException thrown when a particular method cannot be found
	 */
	public static Object getValue(Object object, Field field)
			throws NoSuchMethodException {
		return getValue(object, field.getName());
	}

	/**
	 * Recursively walk through class and collect most specific versions of
	 * {@link Field}s which are annotated as being {@link Column}s.
	 *
	 * Cache result.
	 *
	 * @param clazz the class
	 * @return column field map
	 */
	public static Map<String, Field> getColumnFieldMap(Class<?> clazz) {
		Map<String, Field> columnFieldMap = columnFieldsByClass.get(clazz);
		if (columnFieldMap == null) {
			columnFieldMap = getColumnFieldMap(clazz,
					new HashMap<String, Field>());
			synchronized (columnFieldsByClass) {
				columnFieldsByClass.put(clazz, columnFieldMap);
			}
		}
		return columnFieldMap;
	}

	private static Map<String, Field> getColumnFieldMap(Class<?> clazz,
			Map<String, Field> columnFieldMap) {
		Map<String, Field> fieldMap = internalGetFieldMap(clazz, null);

		for (Field field : fieldMap.values()) {
			Column columnAnnotation = field.getAnnotation(Column.class);
			if (columnAnnotation != null) {
				String name = columnAnnotation.name();
				if (name != null) {
					columnFieldMap.put(name, field);
				}
			} else if (field.getAnnotation(EmbeddedId.class) != null
					|| field.getAnnotation(Embedded.class) != null) {
				// recursive call
				getColumnFieldMap(field.getType(), columnFieldMap);
			}
		}
		return columnFieldMap;
	}

	/**
	 * Get name of the table by class type of that table
	 * 
	 * @param type the class
	 * @return the name of table
	 */
	public static String getTablename(Class<?> type) {
		String tablename = "";

		Table annotation = type.getAnnotation(Table.class);
		if (annotation != null) {
			tablename = annotation.name();
		}

		return tablename;
	}

	/**
	 *
	 * @param histBean destination bean whose properties are modified
	 * @param current origin bean whose properties are retrieved
	 */
	public static void copyProperties(Object histBean, Object current) {
		try {
			PropertyUtils.copyProperties(histBean, current);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			LOG.error("Error copying properties of object {0}", current, e);
		}
	}

	/**
	 *
	 * @param attribute the object SingularAttributeAndStringName
	 * @param <T> the type of the represented object
	 * @param <E> the type of the represented object
	 * @return attribute name
	 */
	public static <E extends GenericEntity<?>, T extends Serializable> String attrName(
			SingularAttributeAndStringName<E, T> attribute) {
		return attrName(attribute.getAttribute(), attribute.getAttributeName());
	}

	/**
	 *
	 * @param attribute the object SingularAttribute
	 * @param attributeName the string attribute name
	 * @param <T> the type of the represented object
	 * @param <E> the type of the represented object
	 * @return attribute name 
	 */
	public static <E extends GenericEntity<?>, T extends Serializable> String attrName(
			SingularAttribute<E, T> attribute, String attributeName) {
		if (attribute != null) {
			return attribute.getName();
		} else {
			return attributeName;
		}
	}

	/**
	 *
	 * @param attribute the object represent persistent collection-valued attributes
	 * @param attributeName the string attribute name
	 * @param <T> the type of the represented object
	 * @param <E> the type of the represented object
	 * @return attribute name
	 */
	public static <E extends GenericEntity<?>, T extends Serializable> String attrName(
			PluralAttribute<E, List<T>, T> attribute, String attributeName) {
		if (attribute != null) {
			return attribute.getName();
		} else {
			return attributeName;
		}
	}

	/**
	 * Convenience call for {@link minMaxComparison} where BigDecimal is replaced
	 * with Long
	 *
	 * @param lower lower limit
	 * @param lowerAttr the attribute 
	 * @param upper upper limit
	 * @param upperAttr the attribute 
	 * @param errorFields contains result, a list of String - names of not abiding
	 *                    attributes (fieldnames)
	 * @param allowEqual if allowEqual is null or false the lower limit has to be
	 *                    smaller then the upper limit
	 * @param allowNull if not initialized or nulls not allowed report error
	 * @param <T> the type of the represented object
	 * @param <N> the type of the represented object
	 */
	public static <T extends GenericEntity<?>, N extends Number> void minMaxComparison(
			final Long lower, SingularAttributeAndStringName<T, N> lowerAttr,
			Long upper, SingularAttributeAndStringName<T, N> upperAttr,
			List<String> errorFields, Boolean allowEqual, Boolean allowNull) {
		BigDecimal lowerBig;
		if (lower != null) {
			lowerBig = BigDecimal.valueOf(lower);
		} else {
			lowerBig = null;
		}
		BigDecimal upperBig;
		if (upper != null) {
			upperBig = BigDecimal.valueOf(upper);
		} else {
			upperBig = null;
		}
		minMaxComparison(lowerBig, lowerAttr, upperBig, upperAttr, errorFields,
				allowEqual, allowNull);
	}

	/**
	 * Fills the errorFields if the lower or upper attributes of attributeParent are
	 * not OK
	 * 
	 * @param lower lower limit
	 * @param lowerAttr the attribute
	 * @param upper upper limit
	 * @param upperAttr the attribute
	 * @param errorFields contains result, a list of String - names of not abiding
	 *                    attributes (fieldnames)
	 * @param allowEqual  if allowEqual is null or false the lower limit has to be
	 *                    smaller then the upper limit
	 * @param allowNull if not initialized or nulls not allowed report error
	 * @param <T> the type of the represented object
	 * @param <N> the type of the represented object
	 */
	public static <T extends GenericEntity<?>, N extends Number> void minMaxComparison(
			final BigDecimal lower,
			SingularAttributeAndStringName<T, N> lowerAttr, BigDecimal upper,
			SingularAttributeAndStringName<T, N> upperAttr,
			List<String> errorFields, Boolean allowEqual, Boolean allowNull) {
		boolean allowEqualTmpBool;
		if (allowEqual == null) {
			allowEqualTmpBool = false;
		} else {
			allowEqualTmpBool = allowEqual;
		}
		int compareBoundary;
		if (allowEqualTmpBool) {
			compareBoundary = 0;
		} else {
			compareBoundary = -1;
		}
		if (lower != null || upper != null) {
			if (lower != null && upper != null
					&& !(lower.compareTo(upper) <= compareBoundary)) {
				errorFields.add(ReflectionUtilitities.attrName(lowerAttr));
				errorFields.add(ReflectionUtilitities.attrName(upperAttr));
			}
		} else if (allowNull == null || !allowNull) {// if not initialized or
			// nulls not allowed
			// report error
			errorFields.add(ReflectionUtilitities.attrName(lowerAttr));
		}
	}

	/**
	 * Clear a field per reflection
	 * 
	 * @param instance the object whose field should be modified
	 * @param field the object
	 */
	public static void clearField(GenericEntity<?> instance, Field field) {
		try {
			field.setAccessible(true);
			field.set(instance, null);
			field.setAccessible(false);
		} catch (Exception e) {
			LOG.debug("Could not set field to null", e);
		}
	}

}
