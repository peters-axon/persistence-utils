package com.axonivy.utils.persistence;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.utils.persistence.beans.GetterIdString;
import com.axonivy.utils.persistence.logging.Logger;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

/**
 * Utilities and convenience functions for {@link String}s.
 */
public class StringUtilities {

	private static final String CLASS_SEPARATOR_STRING = ".class:";
	private static final String CLASS_SEPARATOR_REGEX = "\\"+CLASS_SEPARATOR_STRING;
	private static final Logger LOG = Logger.getLogger(StringUtilities.class);

	private StringUtilities(){
		//no init needed
	}

	/**
	 * Return all content of a {@link Reader} in a {@link String}.
	 *
	 * @param reader : the {@link Reader} object
	 * @return the returned content
	 */
	public static String readAll(Reader reader) {
		String result = null;
		try (Scanner scanner = new Scanner(reader)) {
			// delimit at end of input, ie. get all.
			scanner.useDelimiter("\\Z");
			result = scanner.next();
		}

		return result;
	}

	/**
	 * Helper for checking empty or null string after trimming
	 * 
	 * @param nullSafeCheckString : String that need to check empty
	 * @return true if empty or null after trimming
	 */
	public static boolean isEmpty(String nullSafeCheckString) {
		return nullSafeCheckString == null || nullSafeCheckString.trim().length()==0;
	}

	/**
	 * Helper for checking empty or null string after trimming
	 * 
	 * @param nullSafeCheckObject : an array of object need to check empty
	 * @return true if all objects are empty or null after trimming
	 */
	public static boolean areEmpty(Object...nullSafeCheckObject) {
		return nullSafeCheckObject == null || nullSafeCheckObject.length==0 || Stream.of(nullSafeCheckObject).allMatch(s->s==null||s.toString().trim().length()==0);
	}

	/**
	 * Replace punctuation in string with replacement characters
	 * 
	 * @param source : source string
	 * @param replacement : replacement string
	 * @return replaced string
	 */
	public static String replacePunctuation(String source, String replacement) {
		return source.replaceAll("[.;:]", replacement);
	}

	/**
	 * Replace all ocurrencies of {key} with value  (key ,value come hashmap)
	 * 
	 * @param templateString  the regular expression to which this string is to be matched
	 * @param values the replacement sequence of values
	 * @return replaced string
	 */
	public static String replaceAll(String templateString, Map<String, Object> values) {
		String result = templateString;
		if(values!=null){
			for (Map.Entry<String, Object> entry : values.entrySet()) {
				if(entry.getValue()!=null){
					result = result.replace("{" + entry.getKey() + "}", entry.getValue().toString());
				}

			}
		}
		if(result!=null){
			return result.replaceAll("\\{.*\\}", "");//remove non mapped template variables
		}else {
			return null;
		}
	}

	/**
	 * Convert a Collection of elements inheriting from {@link GetterIdString} to a comma separated string of ids e.g. [Localbranch1 , localbranch2, locabranch3] -&gt; "213213214242342342343223,31231132132132132132132131,21321331321321321"
	 *
	 * @param collectionOfElementsWithId input of elements with id method
	 * @return String containing only the ids separated with comma -&gt; ,  nulls are ignored
	 */
	public static String convertListOfElementsWithIdToCommaString(Collection<? extends GetterIdString> collectionOfElementsWithId) {
		return collectionOfElementsWithId.stream().filter(Objects::nonNull).map(o->o.getId()).collect(Collectors.joining(","));
	}

	/**
	 *
	 * @param origString the String to pad out
	 * @param desiredLength the size to pad to
	 * @param charToFill the character to pad with
	 * @return resulted string
	 */
	public static String padRight(String origString, int desiredLength, char charToFill) {
		if (desiredLength > 0) {
			return StringUtils.rightPad(origString, desiredLength, charToFill) ;
		} else {
			return origString;
		}
	}

	/**
	 *
	 * @param origString the String to pad out
	 * @param desiredLength the size to pad to
	 * @param charToFill the character to pad with
	 * @return resulted string
	 */
	public static String padLeft(String origString, int desiredLength, char charToFill) {
		if (desiredLength > 0) {
			return StringUtils.leftPad(origString, desiredLength, charToFill);
		} else {
			return origString;
		}
	}

	/**
	 * Gets a Byte List and converts it to a string with the given encoding
	 * 
	 * @param list : a Byte List that need to convert to string
	 * @param encoding : The name of a supported charset
	 * @return resulted string
	 * @throws UnsupportedEncodingException the character encoding is not supported
	 */
	public static String byteListToString(List<Byte> list, String encoding) throws UnsupportedEncodingException {
		byte[] array = new byte[list.size()];
		int i = 0;
		for (Byte current : list) {
			array[i] = current;
			i++;
		}
		
		return new String(array, encoding);
	}

	/**
	 * Join a list of fields to a string similar to {@link StringUtils} but with a maximum of list entries.
	 *
	 * @param objects the array of values to join together
	 * @param separator the separator character to use
	 * @param maxEntries max entries
	 * @return joined string
	 */
	public static String join(Object[] objects, String separator, int maxEntries) {
		Object[] list = objects;
		if(objects.length > maxEntries) {
			list = new Object[maxEntries + 1];
			int left = (maxEntries + 1) / 2;
			int right = maxEntries - left;
			int i;
			for(i=0; i<left; i++) {
				list[i] = objects[i];
			}
			list[left] = "...";

			int listStart = left + 1;
			int objectsStart = objects.length - right;
			for(i=0; i<right; i++) {
				list[listStart + i] = objects[objectsStart + i];
			}

		}

		return StringUtils.join(list, separator);
	}

	/**
	 * Join a list of fields to a string similar to {@link StringUtils} but with a maximum of list entries.
	 *
	 * @param objects the list of values to join together
	 * @param separator the separator character to use
	 * @param maxEntries max entries
	 * @return joined string
	 */
	public static String join(List<? extends Object> objects, String separator, int maxEntries) {
		Object[] list;
		if(objects.size() > maxEntries) {
			list = new Object[maxEntries + 1];
			int left = (maxEntries + 1) / 2;
			int right = maxEntries - left;
			int i;
			for(i=0; i<left; i++) {
				list[i] = objects.get(i);
			}
			list[left] = "...";

			int listStart = left + 1;
			int objectsStart = objects.size() - right;
			for(i=0; i<right; i++) {
				list[listStart + i] = objects.get(objectsStart + i);
			}
		}
		else {
			list = objects.toArray();
		}

		return StringUtils.join(list, separator);
	}

	/**
	 * Join {@link String} representation of objects with given separator and skip nulls.
	 *
	 * @param separator the separator character to use
	 * @param objects the object to append
	 * @return joined string
	 */
	public static String joinIfNotNull(String separator, Object...objects) {
		StringBuilder result = new StringBuilder();
		boolean gotOne = false;
		for (Object object : objects) {
			if(object != null) {
				if(gotOne) {
					result.append(separator);
				}
				result.append(object);
				gotOne = true;
			}
		}
		return result.toString();
	}

	/**
	 * Join {@link String} representation of objects with given separator and skip blanks.
	 *
	 * @param separator the separator character to use
	 * @param objects the object to append
	 * @return joined string
	 */
	public static String joinIfNotBlank(String separator, Object...objects) {
		StringBuilder result = new StringBuilder();
		boolean gotOne = false;
		for (Object object : objects) {
			if(object != null && StringUtils.isNotBlank(object.toString())) {
				if(gotOne) {
					result.append(separator);
				}
				result.append(object);
				gotOne = true;
			}
		}
		return result.toString();
	}

	/**
	 * Compare two objects with equals().
	 *
	 * Both objects may be <code>null</code>. If any objects is <code>null</code> the objects are considered unequal.
	 *
	 * @param left the object
	 * @param right the object
	 * @return true of <b>left</b> object equals <b>right</b> object
	 */
	public static boolean safeEqualsNotNull(Object left, Object right) {
		return left != null && right != null && left.equals(right);
	}

	/**
	 * Replace header text up to a br html tag with a bold header text (via html b tag).
	 *
	 * @param original The character sequence to be matched
	 * @return modified header - now with bold html tag
	 */
	public static String replaceHeaderWithBold(String original) {
		if(original==null) {
			return original;
		}
		Pattern pattern = Pattern.compile("^(.*?)<br",Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
		String result = pattern.matcher(original).replaceFirst("<b>$1</b><br");
		return result;
	}

	/**
	 * Stripping leading 0
	 * E.g  0000123456 --&gt; 123456
	 * 
	 * @param original the String to remove characters from
	 * @return modified string
	 */
	public static String stripLeadingZeros(String original) {
		if(original==null) {
			return original;
		}

		return StringUtils.stripStart(original, "0");
	}


	/**
	 * Convert a byte array to a hex String.
	 *
	 * @param b byte array
	 * @return a string represent hex value of byte array
	 */
	public static String byteArrayToHexString(byte[] b) {
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			int v = b[i] & 0xff;
			if (v < 16) {
				sb.append('0');
			}
			sb.append(Integer.toHexString(v));
		}
		return sb.toString().toUpperCase();
	}


	/**
	 * Convert a hex String to a byte array.
	 *
	 * @param s the string to hex
	 * @return a byte array represent hex string
	 */
	public static byte[] hexStringToByteArray(String s) {
		byte[] b = new byte[s.length() / 2];
		for (int i = 0; i < b.length; i++) {
			int index = i * 2;
			int v = Integer.parseInt(s.substring(index, index + 2), 16);
			b[i] = (byte) v;
		}
		return b;
	}

	/**
	 * Remove characters which are not allowed to be used as a CMS name
	 * 
	 * @param toBeReplaced the string to modified
	 * @return modified String which contains only alphanumeric characters and . or _
	 */
	public static String replaceNonCMSNameCharacters(String toBeReplaced) {
		if(toBeReplaced==null){
			return null;
		}
		return toBeReplaced.replaceAll("[^\\w.]", "");//remove non alphanumeric chars, allow dot and underscore
	}

	/**
	 * Convert json string to object of clazz
	 * 
	 * @param jsonValue the string json
	 * @param clazz the class
	 * @param <T> the type of the represented object
	 * @return converted class
	 */
	public static <T> T fromJSONToObject(String jsonValue, Class<T> clazz) {
		return fromJSONToObject(jsonValue, clazz, null);
	}
	
	/**
	 * Convert json string to object
	 * If clazz is a Collection-like clazz and the collection contains entries of type clazzEntry, the result object is a collection
	 * else only clazz is considered (no conllection support)
	 * 
	 * @param jsonValue the string json
	 * @param clazz the class
	 * @param clazzEntry the class
	 * @param <U> the type of the represented object
	 * @param <T> the type of the represented object
	 * @return converted class
	 */
	public static <T,U> T fromJSONToObject(String jsonValue, Class<T> clazz, Class<U> clazzEntry) {
		ObjectMapper objMap = new ObjectMapper();
		objMap.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		T deserializedInstance;
		if(jsonValue!= null){
			String jsonString = jsonValue.replaceFirst(".*" + CLASS_SEPARATOR_REGEX, "");

			try {
				if(clazz!=null&& clazzEntry!=null&& Collection.class.isAssignableFrom(clazz) ){

					@SuppressWarnings("unchecked")
					CollectionType type = objMap.getTypeFactory()
					.constructCollectionType((Class<? extends Collection<?>>) clazz, clazzEntry);
					deserializedInstance = objMap.readValue(jsonString, type);
				}else{
					deserializedInstance = objMap.readValue(jsonString, clazz);
				}
				return deserializedInstance;
			} catch (IOException e) {
				LOG.error("Could not deserialize value {0} into {1}", e, jsonString, clazz);
			}
		}

		return null;
	}


	/**
	 * Convert an object to JSON represent as a {@link String}
	 * 
	 * @param value JSON value of the object
	 * @return a string represent JSON value of the object
	 */
	public static String fromObjectToJSON(Object value) {
		if(value != null){
			try {
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.setSerializationInclusion(Include.NON_EMPTY);
				return objectMapper.writeValueAsString(value);
			} catch (JsonProcessingException e) {
				LOG.error("Could not serialize value {0} into json string", e, value);
			}
		}

		return null;
	}

	/**
	 * Remove leading and trailing spaces, replace multiple space-characters by single space.
	 *
	 * @param in the string need to clean spaces
	 * @return replaced string
	 */
	public static String cleanSpaces(String in) {
		return in != null ? in.trim().replaceAll("\\s+", " ") : null;
	}

	/**
	 * Compare two {@link String}s ignoring spaces or null/empty difference.
	 *
	 * @param left the string
	 * @param right the string
	 * @return true if left string equals right string
	 */
	public static boolean equalsIgnoreSpaces(String left, String right) {
		return cleanSpaces(left == null ? "" : left).equals(cleanSpaces(right == null ? "" : right));
	}
}
