package org.opentrafficsim.core.logger;

import java.util.Set;

import org.opentrafficsim.base.logger.CategoryLogger;
import org.opentrafficsim.base.logger.LogCategory;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.LogEntryForwarder;
import org.pmw.tinylog.writers.Writer;

import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * SimLogger, "extends" the CategoryLogger to be simulator aware and able to print the simulator time. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
@SuppressWarnings({ "checkstyle:finalclass", "checkstyle:needbraces" })
public class SimLogger extends CategoryLogger
{
    /** the simulator of which to include the time in the log messages. */
    private static SimulatorInterface<?, ?, ?> simulator = null;

    /** */
    private SimLogger()
    {
        // Utility class.
    }

    static
    {
        create();
    }

    /**
     * Create a new logger for the system console. Note that this REPLACES current loggers, so create this class before ANY
     * other loggers are created. Note that the initial LogCategory is LogCategory.ALL, so all categories will be logged. This
     * category has to be explicitly removed (or new categories have to be set) to log a limited set of categories.
     */
    protected static void create()
    {
        // nothing to add for now. The CategoryLogger.create() is called with its static initializer.
    }

    /**
     * @param newSimulator set simulator
     */
    public static final void setSimulator(final SimulatorInterface<?, ?, ?> newSimulator)
    {
        simulator = newSimulator;
    }

    /**
     * Set a new logging format for the message lines of all writers. The default message format is:<br>
     * {class_name}.{method}:{line} {message|indent=4}<br>
     * <br>
     * A few popular placeholders that can be used:<br>
     * - {class} Fully-qualified class name where the logging request is issued<br>
     * - {class_name} Class name (without package) where the logging request is issued<br>
     * - {date} Date and time of the logging request, e.g. {date:yyyy-MM-dd HH:mm:ss} [SimpleDateFormat]<br>
     * - {level} Logging level of the created log entry<br>
     * - {line} Line number from where the logging request is issued<br>
     * - {message} Associated message of the created log entry<br>
     * - {method} Method name from where the logging request is issued<br>
     * - {package} Package where the logging request is issued<br>
     * @see <a href="https://tinylog.org/configuration#format">https://tinylog.org/configuration</a>
     * @param newMessageFormat the new formatting pattern to use for all registered writers
     */
    public static void setAllLogMessageFormat(final String newMessageFormat)
    {
        CategoryLogger.setAllLogMessageFormat(newMessageFormat);
    }

    /**
     * Set a new logging level for all registered writers.
     * @param newLevel the new log level for all registered writers
     */
    public static void setAllLogLevel(final Level newLevel)
    {
        CategoryLogger.setAllLogLevel(newLevel);
    }

    /**
     * Set a new logging format for the message lines of a writer. The default message format is:<br>
     * {class_name}.{method}:{line} {message|indent=4}<br>
     * <br>
     * A few popular placeholders that can be used:<br>
     * - {class} Fully-qualified class name where the logging request is issued<br>
     * - {class_name} Class name (without package) where the logging request is issued<br>
     * - {date} Date and time of the logging request, e.g. {date:yyyy-MM-dd HH:mm:ss} [SimpleDateFormat]<br>
     * - {level} Logging level of the created log entry<br>
     * - {line} Line number from where the logging request is issued<br>
     * - {message} Associated message of the created log entry<br>
     * - {method} Method name from where the logging request is issued<br>
     * - {package} Package where the logging request is issued<br>
     * @see <a href="https://tinylog.org/configuration#format">https://tinylog.org/configuration</a>
     * @param writer the writer to change the mesage format for
     * @param newMessageFormat the new formatting pattern to use for all registered writers
     */
    public static void setLogMessageFormat(final Writer writer, final String newMessageFormat)
    {
        CategoryLogger.setLogMessageFormat(writer, newMessageFormat);
    }

    /**
     * Set a new logging level for all registered writers.
     * @param writer the writer to change the mesage format for
     * @param newLevel the new log level for all registered writers
     */
    public static void setAllLogLevel(final Writer writer, final Level newLevel)
    {
        CategoryLogger.setAllLogLevel(writer, newLevel);
    }

    /**
     * Add a category to be logged to the Writers.
     * @param logCategory the LogCategory to add
     */
    public static void addLogCategory(final LogCategory logCategory)
    {
        CategoryLogger.addLogCategory(logCategory);
    }

    /**
     * Remove a category to be logged to the Writers.
     * @param logCategory the LogCategory to remove
     */
    public static void removeLogCategory(final LogCategory logCategory)
    {
        CategoryLogger.removeLogCategory(logCategory);
    }

    /**
     * Set the categories to be logged to the Writers.
     * @param newLogCategories the LogCategories to set, replacing the previous ones
     */
    public static void setLogCategories(final LogCategory... newLogCategories)
    {
        CategoryLogger.setLogCategories(newLogCategories);
    }

    /**
     * Check whether the provided category needs to be logged. Note that when LogCategory.ALL is contained in the categories,
     * checkCategories will return true.
     * @param logCategory the category to check for.
     * @return whether the provided category needs to be logged
     */
    protected static boolean checkCategories(final LogCategory logCategory)
    {
        return CategoryLogger.checkCategories(logCategory);
    }

    /**
     * Check whether the provided categories contain one or more categories that need to be logged. Note that when
     * LogCategory.ALL is contained in the categories, checkCategories will return true.
     * @param logCategories the categories to check for.
     * @return whether the provided category needs to be logged
     */
    protected static boolean checkCategories(final LogCategory[] logCategories)
    {
        return CategoryLogger.checkCategories(logCategories);
    }

    /**
     * Check whether the provided categories contain one or more categories that need to be logged. Note that when
     * LogCategory.ALL is contained in the categories, checkCategories will return true.
     * @param logCategories the categories to check for.
     * @return whether the provided category needs to be logged
     */
    protected static boolean checkCategories(final Set<LogCategory> logCategories)
    {
        return CategoryLogger.checkCategories(logCategories);
    }

    /**
     * Insert the simulator time into the message.
     * @param message the original message
     * @return the message with the simulator time at the front
     */
    private static String insertSimTime(final String message)
    {
        if (simulator == null)
            return message;
        return simulator.getSimulatorTime() + " - " + message;
    }

    /* ****************************************** TRACE ******************************************/

    /**
     * Create a trace log entry that will always be output, independent of LogCategory settings.
     * @param object the result of the <code>toString()</code> method will be logged
     */
    public static void trace(final Object object)
    {
        LogEntryForwarder.forward(1, Level.TRACE, insertSimTime(object.toString()));
    }

    /**
     * Create a trace log entry that will always be output, independent of LogCategory settings.
     * @param message The message to log
     */
    public static void trace(final String message)
    {
        LogEntryForwarder.forward(1, Level.TRACE, insertSimTime(message));
    }

    /**
     * Create a trace log entry that will always be output, independent of LogCategory settings.
     * @param message The message to be logged, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void trace(final String message, final Object... arguments)
    {
        LogEntryForwarder.forward(1, Level.TRACE, insertSimTime(message), arguments);
    }

    /**
     * Create a trace log entry that will always be output, independent of LogCategory settings.
     * @param exception the exception to log
     */
    public static void trace(final Throwable exception)
    {
        LogEntryForwarder.forward(1, Level.TRACE, exception, insertSimTime(exception.getMessage()));
    }

    /**
     * Create a trace log entry that will always be output, independent of LogCategory settings.
     * @param exception the exception to log
     * @param message The message to log
     */
    public static void trace(final Throwable exception, final String message)
    {
        LogEntryForwarder.forward(1, Level.TRACE, exception, insertSimTime(message));
    }

    /**
     * Create a trace log entry that will always be output, independent of LogCategory settings.
     * @param exception the exception to log
     * @param message The message to log, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void trace(final Throwable exception, final String message, final Object... arguments)
    {
        LogEntryForwarder.forward(1, Level.TRACE, exception, insertSimTime(message), arguments);
    }

    /**
     * Create a trace log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param object the result of the <code>toString()</code> method will be logged
     */
    public static void trace(final LogCategory logCategory, final Object object)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.TRACE, insertSimTime(object.toString()));
    }

    /**
     * Create a trace log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param message The message to log
     */
    public static void trace(final LogCategory logCategory, final String message)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.TRACE, insertSimTime(message));
    }

    /**
     * Create a trace log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param message The message to be logged, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void trace(final LogCategory logCategory, final String message, final Object... arguments)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.TRACE, insertSimTime(message), arguments);
    }

    /**
     * Create a trace log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param exception the exception to log
     */
    public static void trace(final LogCategory logCategory, final Throwable exception)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.TRACE, exception, insertSimTime(exception.getMessage()));
    }

    /**
     * Create a trace log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log
     */
    public static void trace(final LogCategory logCategory, final Throwable exception, final String message)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.TRACE, exception, insertSimTime(message));
    }

    /**
     * Create a trace log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void trace(final LogCategory logCategory, final Throwable exception, final String message,
            final Object... arguments)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.TRACE, exception, insertSimTime(message), arguments);
    }

    /**
     * Create a trace log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param object the result of the <code>toString()</code> method will be logged
     */
    public static void trace(final LogCategory[] logCategories, final Object object)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.TRACE, insertSimTime(object.toString()));
    }

    /**
     * Create a trace log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param message The message to log
     */
    public static void trace(final LogCategory[] logCategories, final String message)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.TRACE, insertSimTime(message));
    }

    /**
     * Create a trace log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param message The message to be logged, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void trace(final LogCategory[] logCategories, final String message, final Object... arguments)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.TRACE, insertSimTime(message), arguments);
    }

    /**
     * Create a trace log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     */
    public static void trace(final LogCategory[] logCategories, final Throwable exception)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.TRACE, exception, insertSimTime(exception.getMessage()));
    }

    /**
     * Create a trace log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log
     */
    public static void trace(final LogCategory[] logCategories, final Throwable exception, final String message)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.TRACE, exception, insertSimTime(message));
    }

    /**
     * Create a trace log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void trace(final LogCategory[] logCategories, final Throwable exception, final String message,
            final Object... arguments)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.TRACE, exception, insertSimTime(message), arguments);
    }

    /**
     * Create a trace log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param object the result of the <code>toString()</code> method will be logged
     */
    public static void trace(final Set<LogCategory> logCategories, final Object object)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.TRACE, insertSimTime(object.toString()));
    }

    /**
     * Create a trace log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param message The message to log
     */
    public static void trace(final Set<LogCategory> logCategories, final String message)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.TRACE, insertSimTime(message));
    }

    /**
     * Create a trace log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param message The message to be logged, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void trace(final Set<LogCategory> logCategories, final String message, final Object... arguments)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.TRACE, insertSimTime(message), arguments);
    }

    /**
     * Create a trace log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     */
    public static void trace(final Set<LogCategory> logCategories, final Throwable exception)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.TRACE, exception, insertSimTime(exception.getMessage()));
    }

    /**
     * Create a trace log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log
     */
    public static void trace(final Set<LogCategory> logCategories, final Throwable exception, final String message)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.TRACE, exception, insertSimTime(message));
    }

    /**
     * Create a trace log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void trace(final Set<LogCategory> logCategories, final Throwable exception, final String message,
            final Object... arguments)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.TRACE, exception, insertSimTime(message), arguments);
    }

    /* ****************************************** DEBUG ******************************************/

    /**
     * Create a debug log entry that will always be output, independent of LogCategory settings.
     * @param object the result of the <code>toString()</code> method will be logged
     */
    public static void debug(final Object object)
    {
        LogEntryForwarder.forward(1, Level.DEBUG, insertSimTime(object.toString()));
    }

    /**
     * Create a debug log entry that will always be output, independent of LogCategory settings.
     * @param message The message to log
     */
    public static void debug(final String message)
    {
        LogEntryForwarder.forward(1, Level.DEBUG, insertSimTime(message));
    }

    /**
     * Create a debug log entry that will always be output, independent of LogCategory settings.
     * @param message The message to be logged, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void debug(final String message, final Object... arguments)
    {
        LogEntryForwarder.forward(1, Level.DEBUG, insertSimTime(message), arguments);
    }

    /**
     * Create a debug log entry that will always be output, independent of LogCategory settings.
     * @param exception the exception to log
     */
    public static void debug(final Throwable exception)
    {
        LogEntryForwarder.forward(1, Level.DEBUG, exception, insertSimTime(exception.getMessage()));
    }

    /**
     * Create a debug log entry that will always be output, independent of LogCategory settings.
     * @param exception the exception to log
     * @param message The message to log
     */
    public static void debug(final Throwable exception, final String message)
    {
        LogEntryForwarder.forward(1, Level.DEBUG, exception, insertSimTime(message));
    }

    /**
     * Create a debug log entry that will always be output, independent of LogCategory settings.
     * @param exception the exception to log
     * @param message The message to log, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void debug(final Throwable exception, final String message, final Object... arguments)
    {
        LogEntryForwarder.forward(1, Level.DEBUG, exception, insertSimTime(message), arguments);
    }

    /**
     * Create a debug log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param object the result of the <code>toString()</code> method will be logged
     */
    public static void debug(final LogCategory logCategory, final Object object)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.DEBUG, insertSimTime(object.toString()));
    }

    /**
     * Create a debug log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param message The message to log
     */
    public static void debug(final LogCategory logCategory, final String message)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.DEBUG, insertSimTime(message));
    }

    /**
     * Create a debug log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param message The message to be logged, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void debug(final LogCategory logCategory, final String message, final Object... arguments)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.DEBUG, insertSimTime(message), arguments);
    }

    /**
     * Create a debug log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param exception the exception to log
     */
    public static void debug(final LogCategory logCategory, final Throwable exception)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.DEBUG, exception, insertSimTime(exception.getMessage()));
    }

    /**
     * Create a debug log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log
     */
    public static void debug(final LogCategory logCategory, final Throwable exception, final String message)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.DEBUG, exception, insertSimTime(message));
    }

    /**
     * Create a debug log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void debug(final LogCategory logCategory, final Throwable exception, final String message,
            final Object... arguments)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.DEBUG, exception, insertSimTime(message), arguments);
    }

    /**
     * Create a debug log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param object the result of the <code>toString()</code> method will be logged
     */
    public static void debug(final LogCategory[] logCategories, final Object object)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.DEBUG, insertSimTime(object.toString()));
    }

    /**
     * Create a debug log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param message The message to log
     */
    public static void debug(final LogCategory[] logCategories, final String message)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.DEBUG, insertSimTime(message));
    }

    /**
     * Create a debug log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param message The message to be logged, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void debug(final LogCategory[] logCategories, final String message, final Object... arguments)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.DEBUG, insertSimTime(message), arguments);
    }

    /**
     * Create a debug log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     */
    public static void debug(final LogCategory[] logCategories, final Throwable exception)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.DEBUG, exception, insertSimTime(exception.getMessage()));
    }

    /**
     * Create a debug log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log
     */
    public static void debug(final LogCategory[] logCategories, final Throwable exception, final String message)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.DEBUG, exception, insertSimTime(message));
    }

    /**
     * Create a debug log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void debug(final LogCategory[] logCategories, final Throwable exception, final String message,
            final Object... arguments)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.DEBUG, exception, insertSimTime(message), arguments);
    }

    /**
     * Create a debug log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param object the result of the <code>toString()</code> method will be logged
     */
    public static void debug(final Set<LogCategory> logCategories, final Object object)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.DEBUG, insertSimTime(object.toString()));
    }

    /**
     * Create a debug log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param message The message to log
     */
    public static void debug(final Set<LogCategory> logCategories, final String message)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.DEBUG, insertSimTime(message));
    }

    /**
     * Create a debug log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param message The message to be logged, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void debug(final Set<LogCategory> logCategories, final String message, final Object... arguments)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.DEBUG, insertSimTime(message), arguments);
    }

    /**
     * Create a debug log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     */
    public static void debug(final Set<LogCategory> logCategories, final Throwable exception)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.DEBUG, exception, insertSimTime(exception.getMessage()));
    }

    /**
     * Create a debug log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log
     */
    public static void debug(final Set<LogCategory> logCategories, final Throwable exception, final String message)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.DEBUG, exception, insertSimTime(message));
    }

    /**
     * Create a debug log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void debug(final Set<LogCategory> logCategories, final Throwable exception, final String message,
            final Object... arguments)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.DEBUG, exception, insertSimTime(message), arguments);
    }

    /* ****************************************** INFO ******************************************/

    /**
     * Create a info log entry that will always be output, independent of LogCategory settings.
     * @param object the result of the <code>toString()</code> method will be logged
     */
    public static void info(final Object object)
    {
        LogEntryForwarder.forward(1, Level.INFO, insertSimTime(object.toString()));
    }

    /**
     * Create a info log entry that will always be output, independent of LogCategory settings.
     * @param message The message to log
     */
    public static void info(final String message)
    {
        LogEntryForwarder.forward(1, Level.INFO, insertSimTime(message));
    }

    /**
     * Create a info log entry that will always be output, independent of LogCategory settings.
     * @param message The message to be logged, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void info(final String message, final Object... arguments)
    {
        LogEntryForwarder.forward(1, Level.INFO, insertSimTime(message), arguments);
    }

    /**
     * Create a info log entry that will always be output, independent of LogCategory settings.
     * @param exception the exception to log
     */
    public static void info(final Throwable exception)
    {
        LogEntryForwarder.forward(1, Level.INFO, exception, insertSimTime(exception.getMessage()));
    }

    /**
     * Create a info log entry that will always be output, independent of LogCategory settings.
     * @param exception the exception to log
     * @param message The message to log
     */
    public static void info(final Throwable exception, final String message)
    {
        LogEntryForwarder.forward(1, Level.INFO, exception, insertSimTime(message));
    }

    /**
     * Create a info log entry that will always be output, independent of LogCategory settings.
     * @param exception the exception to log
     * @param message The message to log, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void info(final Throwable exception, final String message, final Object... arguments)
    {
        LogEntryForwarder.forward(1, Level.INFO, exception, insertSimTime(message), arguments);
    }

    /**
     * Create a info log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param object the result of the <code>toString()</code> method will be logged
     */
    public static void info(final LogCategory logCategory, final Object object)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.INFO, insertSimTime(object.toString()));
    }

    /**
     * Create a info log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param message The message to log
     */
    public static void info(final LogCategory logCategory, final String message)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.INFO, insertSimTime(message));
    }

    /**
     * Create a info log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param message The message to be logged, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void info(final LogCategory logCategory, final String message, final Object... arguments)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.INFO, insertSimTime(message), arguments);
    }

    /**
     * Create a info log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param exception the exception to log
     */
    public static void info(final LogCategory logCategory, final Throwable exception)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.INFO, exception, insertSimTime(exception.getMessage()));
    }

    /**
     * Create a info log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log
     */
    public static void info(final LogCategory logCategory, final Throwable exception, final String message)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.INFO, exception, insertSimTime(message));
    }

    /**
     * Create a info log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void info(final LogCategory logCategory, final Throwable exception, final String message,
            final Object... arguments)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.INFO, exception, insertSimTime(message), arguments);
    }

    /**
     * Create a info log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param object the result of the <code>toString()</code> method will be logged
     */
    public static void info(final LogCategory[] logCategories, final Object object)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.INFO, insertSimTime(object.toString()));
    }

    /**
     * Create a info log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param message The message to log
     */
    public static void info(final LogCategory[] logCategories, final String message)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.INFO, insertSimTime(message));
    }

    /**
     * Create a info log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param message The message to be logged, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void info(final LogCategory[] logCategories, final String message, final Object... arguments)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.INFO, insertSimTime(message), arguments);
    }

    /**
     * Create a info log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     */
    public static void info(final LogCategory[] logCategories, final Throwable exception)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.INFO, exception, insertSimTime(exception.getMessage()));
    }

    /**
     * Create a info log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log
     */
    public static void info(final LogCategory[] logCategories, final Throwable exception, final String message)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.INFO, exception, insertSimTime(message));
    }

    /**
     * Create a info log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void info(final LogCategory[] logCategories, final Throwable exception, final String message,
            final Object... arguments)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.INFO, exception, insertSimTime(message), arguments);
    }

    /**
     * Create a info log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param object the result of the <code>toString()</code> method will be logged
     */
    public static void info(final Set<LogCategory> logCategories, final Object object)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.INFO, insertSimTime(object.toString()));
    }

    /**
     * Create a info log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param message The message to log
     */
    public static void info(final Set<LogCategory> logCategories, final String message)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.INFO, insertSimTime(message));
    }

    /**
     * Create a info log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param message The message to be logged, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void info(final Set<LogCategory> logCategories, final String message, final Object... arguments)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.INFO, insertSimTime(message), arguments);
    }

    /**
     * Create a info log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     */
    public static void info(final Set<LogCategory> logCategories, final Throwable exception)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.INFO, exception, insertSimTime(exception.getMessage()));
    }

    /**
     * Create a info log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log
     */
    public static void info(final Set<LogCategory> logCategories, final Throwable exception, final String message)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.INFO, exception, insertSimTime(message));
    }

    /**
     * Create a info log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void info(final Set<LogCategory> logCategories, final Throwable exception, final String message,
            final Object... arguments)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.INFO, exception, insertSimTime(message), arguments);
    }

    /* ****************************************** WARN ******************************************/

    /**
     * Create a warn log entry that will always be output, independent of LogCategory settings.
     * @param object the result of the <code>toString()</code> method will be logged
     */
    public static void warn(final Object object)
    {
        LogEntryForwarder.forward(1, Level.WARNING, insertSimTime(object.toString()));
    }

    /**
     * Create a warn log entry that will always be output, independent of LogCategory settings.
     * @param message The message to log
     */
    public static void warn(final String message)
    {
        LogEntryForwarder.forward(1, Level.WARNING, insertSimTime(message));
    }

    /**
     * Create a warn log entry that will always be output, independent of LogCategory settings.
     * @param message The message to be logged, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void warn(final String message, final Object... arguments)
    {
        LogEntryForwarder.forward(1, Level.WARNING, insertSimTime(message), arguments);
    }

    /**
     * Create a warn log entry that will always be output, independent of LogCategory settings.
     * @param exception the exception to log
     */
    public static void warn(final Throwable exception)
    {
        LogEntryForwarder.forward(1, Level.WARNING, exception, insertSimTime(exception.getMessage()));
    }

    /**
     * Create a warn log entry that will always be output, independent of LogCategory settings.
     * @param exception the exception to log
     * @param message The message to log
     */
    public static void warn(final Throwable exception, final String message)
    {
        LogEntryForwarder.forward(1, Level.WARNING, exception, insertSimTime(message));
    }

    /**
     * Create a warn log entry that will always be output, independent of LogCategory settings.
     * @param exception the exception to log
     * @param message The message to log, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void warn(final Throwable exception, final String message, final Object... arguments)
    {
        LogEntryForwarder.forward(1, Level.WARNING, exception, insertSimTime(message), arguments);
    }

    /**
     * Create a warn log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param object the result of the <code>toString()</code> method will be logged
     */
    public static void warn(final LogCategory logCategory, final Object object)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.WARNING, insertSimTime(object.toString()));
    }

    /**
     * Create a warn log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param message The message to log
     */
    public static void warn(final LogCategory logCategory, final String message)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.WARNING, insertSimTime(message));
    }

    /**
     * Create a warn log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param message The message to be logged, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void warn(final LogCategory logCategory, final String message, final Object... arguments)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.WARNING, insertSimTime(message), arguments);
    }

    /**
     * Create a warn log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param exception the exception to log
     */
    public static void warn(final LogCategory logCategory, final Throwable exception)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.WARNING, exception, insertSimTime(exception.getMessage()));
    }

    /**
     * Create a warn log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log
     */
    public static void warn(final LogCategory logCategory, final Throwable exception, final String message)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.WARNING, exception, insertSimTime(message));
    }

    /**
     * Create a warn log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void warn(final LogCategory logCategory, final Throwable exception, final String message,
            final Object... arguments)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.WARNING, exception, insertSimTime(message), arguments);
    }

    /**
     * Create a warn log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param object the result of the <code>toString()</code> method will be logged
     */
    public static void warn(final LogCategory[] logCategories, final Object object)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.WARNING, insertSimTime(object.toString()));
    }

    /**
     * Create a warn log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param message The message to log
     */
    public static void warn(final LogCategory[] logCategories, final String message)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.WARNING, insertSimTime(message));
    }

    /**
     * Create a warn log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param message The message to be logged, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void warn(final LogCategory[] logCategories, final String message, final Object... arguments)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.WARNING, insertSimTime(message), arguments);
    }

    /**
     * Create a warn log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     */
    public static void warn(final LogCategory[] logCategories, final Throwable exception)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.WARNING, exception, insertSimTime(exception.getMessage()));
    }

    /**
     * Create a warn log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log
     */
    public static void warn(final LogCategory[] logCategories, final Throwable exception, final String message)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.WARNING, exception, insertSimTime(message));
    }

    /**
     * Create a warn log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void warn(final LogCategory[] logCategories, final Throwable exception, final String message,
            final Object... arguments)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.WARNING, exception, insertSimTime(message), arguments);
    }

    /**
     * Create a warn log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param object the result of the <code>toString()</code> method will be logged
     */
    public static void warn(final Set<LogCategory> logCategories, final Object object)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.WARNING, insertSimTime(object.toString()));
    }

    /**
     * Create a warn log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param message The message to log
     */
    public static void warn(final Set<LogCategory> logCategories, final String message)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.WARNING, insertSimTime(message));
    }

    /**
     * Create a warn log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param message The message to be logged, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void warn(final Set<LogCategory> logCategories, final String message, final Object... arguments)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.WARNING, insertSimTime(message), arguments);
    }

    /**
     * Create a warn log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     */
    public static void warn(final Set<LogCategory> logCategories, final Throwable exception)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.WARNING, exception, insertSimTime(exception.getMessage()));
    }

    /**
     * Create a warn log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log
     */
    public static void warn(final Set<LogCategory> logCategories, final Throwable exception, final String message)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.WARNING, exception, insertSimTime(message));
    }

    /**
     * Create a warn log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void warn(final Set<LogCategory> logCategories, final Throwable exception, final String message,
            final Object... arguments)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.WARNING, exception, insertSimTime(message), arguments);
    }

    /* ****************************************** ERROR ******************************************/

    /**
     * Create a error log entry that will always be output, independent of LogCategory settings.
     * @param object the result of the <code>toString()</code> method will be logged
     */
    public static void error(final Object object)
    {
        LogEntryForwarder.forward(1, Level.ERROR, insertSimTime(object.toString()));
    }

    /**
     * Create a error log entry that will always be output, independent of LogCategory settings.
     * @param message The message to log
     */
    public static void error(final String message)
    {
        LogEntryForwarder.forward(1, Level.ERROR, insertSimTime(message));
    }

    /**
     * Create a error log entry that will always be output, independent of LogCategory settings.
     * @param message The message to be logged, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void error(final String message, final Object... arguments)
    {
        LogEntryForwarder.forward(1, Level.ERROR, insertSimTime(message), arguments);
    }

    /**
     * Create a error log entry that will always be output, independent of LogCategory settings.
     * @param exception the exception to log
     */
    public static void error(final Throwable exception)
    {
        LogEntryForwarder.forward(1, Level.ERROR, exception, insertSimTime(exception.getMessage()));
    }

    /**
     * Create a error log entry that will always be output, independent of LogCategory settings.
     * @param exception the exception to log
     * @param message The message to log
     */
    public static void error(final Throwable exception, final String message)
    {
        LogEntryForwarder.forward(1, Level.ERROR, exception, insertSimTime(message));
    }

    /**
     * Create a error log entry that will always be output, independent of LogCategory settings.
     * @param exception the exception to log
     * @param message The message to log, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void error(final Throwable exception, final String message, final Object... arguments)
    {
        LogEntryForwarder.forward(1, Level.ERROR, exception, insertSimTime(message), arguments);
    }

    /**
     * Create a error log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param object the result of the <code>toString()</code> method will be logged
     */
    public static void error(final LogCategory logCategory, final Object object)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.ERROR, insertSimTime(object.toString()));
    }

    /**
     * Create a error log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param message The message to log
     */
    public static void error(final LogCategory logCategory, final String message)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.ERROR, insertSimTime(message));
    }

    /**
     * Create a error log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param message The message to be logged, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void error(final LogCategory logCategory, final String message, final Object... arguments)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.ERROR, insertSimTime(message), arguments);
    }

    /**
     * Create a error log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param exception the exception to log
     */
    public static void error(final LogCategory logCategory, final Throwable exception)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.ERROR, exception, insertSimTime(exception.getMessage()));
    }

    /**
     * Create a error log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log
     */
    public static void error(final LogCategory logCategory, final Throwable exception, final String message)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.ERROR, exception, insertSimTime(message));
    }

    /**
     * Create a error log entry that will always be output, independent of LogCategory settings.
     * @param logCategory the category of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void error(final LogCategory logCategory, final Throwable exception, final String message,
            final Object... arguments)
    {
        if (checkCategories(logCategory))
            LogEntryForwarder.forward(1, Level.ERROR, exception, insertSimTime(message), arguments);
    }

    /**
     * Create a error log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param object the result of the <code>toString()</code> method will be logged
     */
    public static void error(final LogCategory[] logCategories, final Object object)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.ERROR, insertSimTime(object.toString()));
    }

    /**
     * Create a error log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param message The message to log
     */
    public static void error(final LogCategory[] logCategories, final String message)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.ERROR, insertSimTime(message));
    }

    /**
     * Create a error log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param message The message to be logged, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void error(final LogCategory[] logCategories, final String message, final Object... arguments)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.ERROR, insertSimTime(message), arguments);
    }

    /**
     * Create a error log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     */
    public static void error(final LogCategory[] logCategories, final Throwable exception)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.ERROR, exception, insertSimTime(exception.getMessage()));
    }

    /**
     * Create a error log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log
     */
    public static void error(final LogCategory[] logCategories, final Throwable exception, final String message)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.ERROR, exception, insertSimTime(message));
    }

    /**
     * Create a error log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void error(final LogCategory[] logCategories, final Throwable exception, final String message,
            final Object... arguments)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.ERROR, exception, insertSimTime(message), arguments);
    }

    /**
     * Create a error log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param object the result of the <code>toString()</code> method will be logged
     */
    public static void error(final Set<LogCategory> logCategories, final Object object)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.ERROR, insertSimTime(object.toString()));
    }

    /**
     * Create a error log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param message The message to log
     */
    public static void error(final Set<LogCategory> logCategories, final String message)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.ERROR, insertSimTime(message));
    }

    /**
     * Create a error log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param message The message to be logged, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void error(final Set<LogCategory> logCategories, final String message, final Object... arguments)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.ERROR, insertSimTime(message), arguments);
    }

    /**
     * Create a error log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     */
    public static void error(final Set<LogCategory> logCategories, final Throwable exception)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.ERROR, exception, insertSimTime(exception.getMessage()));
    }

    /**
     * Create a error log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log
     */
    public static void error(final Set<LogCategory> logCategories, final Throwable exception, final String message)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.ERROR, exception, insertSimTime(message));
    }

    /**
     * Create a error log entry that will always be output, independent of LogCategory settings.
     * @param logCategories the categories of the log message; will be logged if set or added
     * @param exception the exception to log
     * @param message The message to log, where {} entries will be replaced by arguments
     * @param arguments the arguments to fill the {} entries in the message string
     */
    public static void error(final Set<LogCategory> logCategories, final Throwable exception, final String message,
            final Object... arguments)
    {
        if (checkCategories(logCategories))
            LogEntryForwarder.forward(1, Level.ERROR, exception, insertSimTime(message), arguments);
    }

}
