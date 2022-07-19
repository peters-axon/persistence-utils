package com.axonivy.utils.persistence.history.util;

import static com.axonivy.utils.persistence.StringUtilities.fromJSONToObject;
import static com.axonivy.utils.persistence.StringUtilities.fromObjectToJSON;

import java.io.BufferedReader;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;

import javax.sql.rowset.serial.SerialException;

import org.apache.commons.io.IOUtils;

import com.axonivy.utils.persistence.logging.Logger;

/**
 * Data type parser utilities.
 * 
 * @author hunghx
 *
 */
public class ClobUtil {
	
	private ClobUtil() {}
	
	private static Logger LOG = Logger.getLogger(ClobUtil.class);

	/**
	 * Convert Object to Clob data type to store in DB
	 * 
	 * @param object need to convert
	 * @return clob data type
	 */
	public static Clob convertObjectToClobData(Object object) {
		Clob clobData = null;
		try {
			clobData = new javax.sql.rowset.serial.SerialClob(fromObjectToJSON(object).toCharArray());
		} catch (SerialException e) {
			LOG.error(e);
		} catch (SQLException e) {
			LOG.error(e);
		}

		return clobData;
	}
	
	/**
	 * Convert Clob data type to object destination by type class
	 * 
	 * @param clobData object store in DB
	 * @param clazz type of class
	 * @param <T> object
	 * @return object converted
	 */
	public static <T> T convertClobDataToObject(Clob clobData, Class<T> clazz) {
		T object = null;
		try {
			object = fromJSONToObject(clobData.getSubString(1, (int) clobData.length()), clazz);
		} catch (Exception e) {
			LOG.error(e);
		}

		return object;
	}
	
	
	/**
	 * Convert Clob data to JSON string.
	 * 
	 * @param clobData
	 * @return JSON string
	 */
	public static String convertClobToJSON(Clob clobData) {
		if (clobData != null) {
			BufferedReader bufferedReader = null;
			Reader reader = null;
            try {
                reader = clobData.getCharacterStream();
                bufferedReader = new BufferedReader(reader);
                
                return IOUtils.toString(bufferedReader);
            } catch (Exception e) {
                throw new RuntimeException("Error while reading String from CLOB", e);
            } finally {
                IOUtils.closeQuietly(reader);
                IOUtils.closeQuietly(bufferedReader);
            }
		}
		
		return null;
	}
}
