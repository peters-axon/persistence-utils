package com.axonivy.utils.persistence.logging;

import java.text.MessageFormat;
import java.time.LocalDateTime;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;

import com.axonivy.utils.persistence.StringUtilities;

/**
 * Ivy Environment safe ILogger.
 *
 * This ILogger logs to the Ivy Logger if it is available and to a
 * secondary log4j {@link org.apache.log4j.Logger} which allows for better
 * filtering.
 */
public class Logger {

	private org.apache.log4j.Logger logger4j;

	protected Logger() {
	}

	private Logger(String name) {
		logger4j = org.apache.log4j.Logger.getLogger(name);
	}

	private Logger(Class<?> clazz) {
		logger4j = org.apache.log4j.Logger.getLogger(clazz);
	}

	/**
	 * get logger for string, usually a class name
	 * 
	 * @param name string
	 * @return logger instance
	 */
	public static Logger getLogger(String name) {
		return new Logger(name);
	}

	/**
	 * get logger for class with name
	 * 
	 * @param clazz class need logger
	 * @return logger instance
	 */
	public static Logger getLogger(Class<?> clazz) {
		return new Logger(clazz);
	}

	/**
	 * Log reflectively whole object, CAREFULL this causes hibernate
	 * autoinitialization of the object
	 * 
	 * @param level priority for log
	 * @param formattedMessage formatted message
	 * @param args object(s) to format
	 */
	public void logReflective(Priority level, String formattedMessage, Object... args) {
		int i = 0;
		while (args != null && args.length > i && args[i] != null) {// carefull this casues hibernate autoinitialization
																	// of the object
			args[i] = ReflectionToStringBuilder.toString(args[i], ToStringStyle.MULTI_LINE_STYLE, true, false);
			i++;

		}
		log(level, formattedMessage, args);
	}

	/**
	 * Return current time in a short format
	 * 
	 * @return string containing last time
	 */
	public String getStamp() {
		LocalDateTime now = LocalDateTime.now();
		return StringUtilities.padLeft(String.valueOf(now.getSecond()), 2, '0') + "."
				+ String.valueOf(now.getNano() / 100000000).substring(0, 1) + ":";
	}

	public void log(Priority level, String formattedMessage, Object... args) {
		String formattedMessageStr;
		if (formattedMessage == null) {
			formattedMessageStr = "null";
		} else {
			formattedMessageStr = formattedMessage;
		}
		if (level.isGreaterOrEqual(logger4j.getEffectiveLevel())) {
			logger4j.log(level, MessageFormat.format(formattedMessageStr, args));
		}
	}

	public void log(Priority level, String formattedMessage, Throwable throwable, Object... args) {
		String formattedMessageStr;
		if (formattedMessage == null) {
			formattedMessageStr = "null";
		} else {
			formattedMessageStr = formattedMessage;
		}

		if (level.isGreaterOrEqual(logger4j.getEffectiveLevel())) {
			logger4j.log(level, MessageFormat.format(formattedMessageStr, args), throwable);
		}
	}

	protected boolean isLevelEnabled(Level level) {
		return level.isGreaterOrEqual(logger4j.getEffectiveLevel());
	}

	/**
	 * Checks if is debug enabled.
	 *
	 * @return true, if is debug enabled
	 */
	public boolean isDebugEnabled() {
		return isLevelEnabled(Level.DEBUG);
	}

	/**
	 * Checks if is info enabled.
	 *
	 * @return true, if is info enabled
	 */
	public boolean isInfoEnabled() {
		return isLevelEnabled(Level.INFO);
	}

	protected void log(Priority level, Object message) {
		if (message instanceof Throwable) {
			log(level, "Exception: " + ((Throwable) message).getLocalizedMessage(), (Throwable) message);
		} else {
			log(level, message != null ? message.toString() : "<null>", (Object) null/* no varargs */);
		}
	}

	public void debug(String formattedMessage, Object... args) {
		log(Level.DEBUG, formattedMessage, args);
	}

	public void debug(String formattedMessage, Throwable throwable, Object... args) {
		log(Level.DEBUG, formattedMessage, throwable, args);
	}

	public void info(Object message) {
		log(Level.INFO, message);
	}

	public void info(String formattedMessage, Object... args) {
		log(Level.INFO, formattedMessage, args);
	}

	public void info(String formattedMessage, Throwable throwable, Object... args) {
		log(Level.INFO, formattedMessage, throwable, args);
	}

	public void warn(Object message) {
		log(Level.WARN, message);
	}

	public void warn(String formattedMessage, Object... args) {
		log(Level.WARN, formattedMessage, args);
	}

	public void warn(String formattedMessage, Throwable throwable, Object... args) {
		log(Level.WARN, formattedMessage, throwable, args);
	}

	public void error(Object message) {
		log(Level.ERROR, message);
	}

	public void error(String formattedMessage, Object... args) {
		log(Level.ERROR, formattedMessage, args);
	}

	public void error(String formattedMessage, Throwable throwable, Object... args) {
		log(Level.ERROR, formattedMessage, throwable, args);
	}

	public void fatal(String formattedMessage, Object... args) {
		log(Level.FATAL, formattedMessage, args);
	}

	public void fatal(String formattedMessage, Throwable throwable, Object... args) {
		log(Level.FATAL, formattedMessage, throwable, args);
	}
}
