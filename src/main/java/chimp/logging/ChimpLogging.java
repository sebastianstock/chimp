package chimp.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Minimal logging utility replacing {@code org.metacsp.utility.logging.MetaCSPLogging}.
 * Provides class-based loggers with a uniform {@code [ClassName] message} formatter
 * and global/per-class level control.
 */
public final class ChimpLogging {

	private static final Map<Class<?>, Logger> loggers = new HashMap<>();
	private static final Map<Class<?>, Level> tempLevels = new HashMap<>();
	private static Level globalLevel = null;

	private ChimpLogging() {}

	/**
	 * Set a desired log level for all loggers.
	 * @param level The desired log level.
	 */
	public static void setLevel(Level level) {
		for (Logger log : loggers.values()) log.setLevel(level);
		globalLevel = level;
	}

	/**
	 * Set a desired log level for a specific class.
	 * @param clazz The class to set the log level for.
	 * @param level The desired log level.
	 */
	public static void setLevel(Class<?> clazz, Level level) {
		if (!loggers.containsKey(clazz)) tempLevels.put(clazz, level);
		else loggers.get(clazz).setLevel(level);
	}

	/**
	 * Provides a reference to a {@link Logger} which will format log messages as
	 * {@code [ClassName] message}.
	 * @param clazz The class within which the logger should be used.
	 * @return A {@link Logger} for the given class.
	 */
	public static Logger getLogger(final Class<?> clazz) {
		Logger logger = loggers.get(clazz);
		if (logger != null) return logger;

		logger = Logger.getLogger(clazz.getSimpleName());
		loggers.put(clazz, logger);
		Logger parent = logger.getParent();
		if (parent != null) {
			for (Handler h : parent.getHandlers()) parent.removeHandler(h);
		}
		String loggerName = clazz.getSimpleName();
		ConsoleHandler h = new ConsoleHandler();
		h.setFormatter(new Formatter() {
			@Override
			public String format(LogRecord record) {
				return "[" + loggerName + "] " + record.getMessage() + "\n";
			}
		});
		h.setLevel(Level.ALL);
		logger.addHandler(h);
		logger.setUseParentHandlers(false);

		if (tempLevels.containsKey(clazz)) {
			logger.setLevel(tempLevels.remove(clazz));
		}
		if (globalLevel != null) logger.setLevel(globalLevel);
		return logger;
	}
}
