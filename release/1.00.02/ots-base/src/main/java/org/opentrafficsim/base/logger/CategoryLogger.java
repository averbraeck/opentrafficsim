package org.opentrafficsim.base.logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.LogEntryForwarder;
import org.pmw.tinylog.writers.ConsoleWriter;
import org.pmw.tinylog.writers.Writer;

/**
 * The CategoryLogger can log for specific Categories. The way to call the logger for messages that always need to be logged,
 * such as an error with an exception is:
 * 
 * <pre>
 * CategoryLogger.always().error(exception, "Parameter {} did not initialize correctly", param1.toString());
 * </pre>
 * 
 * It is also possible to indicate the category / categories for the message, which will only be logged if at least one of the
 * indicated categories is turned on with addLogCategory() or setLogCategories(), or if one of the added or set LogCategories is
 * LogCategory.ALL:
 * 
 * <pre>
 * CategoryLogger.filter(Cat.BASE).debug("Parameter {} initialized correctly", param1.toString());
 * </pre>
 * 
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
@SuppressWarnings({ "checkstyle:visibilitymodifier", "checkstyle:finalclass", "checkstyle:needbraces" })
public class CategoryLogger
{
    /** The default message format. */
    public static final String DEFAULT_MESSAGE_FORMAT = "{class_name}.{method}:{line} {message|indent=4}";

    /** The current message format. */
    private static String defaultMessageFormat = DEFAULT_MESSAGE_FORMAT;

    /** The current logging level. */
    private static Level defaultLevel = Level.INFO;

    /** The writers registered with this CategoryLogger. */
    private static Set<Writer> writers = new HashSet<>();

    /** The log level per Writer. */
    private static Map<Writer, Level> writerLevels = new HashMap<>();

    /** The message format per Writer. */
    private static Map<Writer, String> writerFormats = new HashMap<>();

    /** The categories to log. */
    protected static Set<LogCategory> categories = new HashSet<>(256);

    /** The console writer, replacing the default one. */
    private static Writer consoleWriter;

    /** The delegate logger instance that does the actual logging work, after a positive filter outcome. */
    protected static DelegateLogger delegateLogger = new DelegateLogger(true);

    /** The delegate logger that returns immediately after a negative filter outcome. */
    protected static DelegateLogger noLogger = new DelegateLogger(false);

    /** */
    protected CategoryLogger()
    {
        // Utility class.
    }

    static
    {
        create();
    }

    /**
     * Create a new logger for the system console. Note that this REPLACES current writers. Note that the initial LogCategory is
     * LogCategory.ALL, so all categories will be logged. This category has to be explicitly removed (or new categories have to
     * be set) to log a limited set of categories.
     */
    protected static void create()
    {
        consoleWriter = new ConsoleWriter();
        writers.add(consoleWriter);
        Configurator.currentConfig().writer(consoleWriter, defaultLevel, defaultMessageFormat).activate();
        categories.add(LogCategory.ALL);
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
        for (Writer writer : writers)
        {
            Configurator.currentConfig().removeWriter(writer).activate();
            defaultMessageFormat = newMessageFormat;
            writerFormats.put(writer, newMessageFormat);
            Configurator.currentConfig().addWriter(writer, defaultLevel, defaultMessageFormat).activate();
        }
    }

    /**
     * Set a new logging level for all registered writers.
     * @param newLevel the new log level for all registered writers
     */
    public static void setAllLogLevel(final Level newLevel)
    {
        for (Writer writer : writers)
        {
            Configurator.currentConfig().removeWriter(writer).activate();
            defaultLevel = newLevel;
            writerLevels.put(writer, newLevel);
            Configurator.currentConfig().addWriter(writer, defaultLevel, defaultMessageFormat).activate();
        }
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
     * @param writer the writer to change the message format for
     * @param newMessageFormat the new formatting pattern to use for all registered writers
     */
    public static void setLogMessageFormat(final Writer writer, final String newMessageFormat)
    {
        Configurator.currentConfig().removeWriter(writer).activate();
        writerFormats.put(writer, newMessageFormat);
        Configurator.currentConfig().addWriter(writer, writerLevels.get(writer), newMessageFormat).activate();
    }

    /**
     * Set a new logging level for one of the registered writers.
     * @param writer the writer to change the log level for
     * @param newLevel the new log level for the writer
     */
    public static void setLogLevel(final Writer writer, final Level newLevel)
    {
        Configurator.currentConfig().removeWriter(writer).activate();
        writerLevels.put(writer, newLevel);
        Configurator.currentConfig().addWriter(writer, newLevel, writerFormats.get(writer)).activate();
    }

    /**
     * Add a category to be logged to the Writers.
     * @param logCategory the LogCategory to add
     */
    public static void addLogCategory(final LogCategory logCategory)
    {
        categories.add(logCategory);
    }

    /**
     * Remove a category to be logged to the Writers.
     * @param logCategory the LogCategory to remove
     */
    public static void removeLogCategory(final LogCategory logCategory)
    {
        categories.remove(logCategory);
    }

    /**
     * Set the categories to be logged to the Writers.
     * @param newLogCategories the LogCategories to set, replacing the previous ones
     */
    public static void setLogCategories(final LogCategory... newLogCategories)
    {
        categories.clear();
        categories.addAll(Arrays.asList(newLogCategories));
    }

    /* ****************************************** FILTER ******************************************/

    /**
     * The "pass" filter that will result in always trying to log.
     * @return the logger that tries to execute logging (delegateLogger)
     */
    public static DelegateLogger always()
    {
        return delegateLogger;
    }

    /**
     * Check whether the provided category needs to be logged. Note that when LogCategory.ALL is contained in the categories,
     * filter will return true.
     * @param logCategory the category to check for.
     * @return the logger that either tries to log (delegateLogger), or returns without logging (noLogger)
     */
    public static DelegateLogger filter(final LogCategory logCategory)
    {
        if (categories.contains(LogCategory.ALL))
            return delegateLogger;
        if (categories.contains(logCategory))
            return delegateLogger;
        return noLogger;
    }

    /**
     * Check whether the provided categories contain one or more categories that need to be logged. Note that when
     * LogCategory.ALL is contained in the categories, filter will return true.
     * @param logCategories LogCategory...; elements or array with the categories to check for
     * @return the logger that either tries to log (delegateLogger), or returns without logging (noLogger)
     */
    public static DelegateLogger filter(final LogCategory... logCategories)
    {
        if (categories.contains(LogCategory.ALL))
            return delegateLogger;
        for (LogCategory logCategory : logCategories)
        {
            if (categories.contains(logCategory))
                return delegateLogger;
        }
        return noLogger;
    }

    /**
     * Check whether the provided categories contain one or more categories that need to be logged. Note that when
     * LogCategory.ALL is contained in the categories, filter will return true.
     * @param logCategories Set&lt;LogCategory&gt;; the categories to check for
     * @return the logger that either tries to log (delegateLogger), or returns without logging (noLogger)
     */
    public static DelegateLogger filter(final Set<LogCategory> logCategories)
    {
        if (categories.contains(LogCategory.ALL))
            return delegateLogger;
        for (LogCategory logCategory : logCategories)
        {
            if (categories.contains(logCategory))
                return delegateLogger;
        }
        return noLogger;
    }

    /**
     * DelegateLogger class that takes care of actually logging the message and/or exception. <br>
     * <br>
     * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
     * See for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>.
     * The source code and binary code of this software is proprietary information of Delft University of Technology.
     * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
     */
    public static class DelegateLogger
    {
        /** Should we try to log or not? */
        private final boolean log;

        /**
         * @param log indicate whether we should log or not.
         */
        public DelegateLogger(final boolean log)
        {
            super();
            this.log = log;
        }

        /* ****************************************** TRACE ******************************************/

        /**
         * Create a trace log entry that will always be output, independent of LogCategory settings.
         * @param object Object; the result of the <code>toString()</code> method of <code>object</code> will be logged
         */
        public void trace(final Object object)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.TRACE, object);
        }

        /**
         * Create a trace log entry that will always be output, independent of LogCategory settings.
         * @param message String; the message to log
         */
        public void trace(final String message)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.TRACE, message);
        }

        /**
         * Create a trace log entry that will always be output, independent of LogCategory settings.
         * @param message String; the message to be logged, where {} entries will be replaced by arguments
         * @param arguments Object...; the arguments to substitute for the {} entries in the message string
         */
        public void trace(final String message, final Object... arguments)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.TRACE, message, arguments);
        }

        /**
         * Create a trace log entry that will always be output, independent of LogCategory settings.
         * @param exception Throwable; the exception to log
         */
        public void trace(final Throwable exception)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.TRACE, exception);
        }

        /**
         * Create a trace log entry that will always be output, independent of LogCategory settings.
         * @param exception Throwable; the exception to log
         * @param message String; the message to log
         */
        public void trace(final Throwable exception, final String message)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.TRACE, exception, message);
        }

        /**
         * Create a trace log entry that will always be output, independent of LogCategory settings.
         * @param exception Throwable; the exception to log
         * @param message String; the message to log, where {} entries will be replaced by arguments
         * @param arguments Object...; the arguments to substitute for the {} entries in the message string
         */
        public void trace(final Throwable exception, final String message, final Object... arguments)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.TRACE, exception, message, arguments);
        }

        /* ****************************************** DEBUG ******************************************/

        /**
         * Create a debug log entry that will always be output, independent of LogCategory settings.
         * @param object Object; the result of the <code>toString()</code> method of <code>object</code> will be logged
         */
        public void debug(final Object object)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.DEBUG, object);
        }

        /**
         * Create a debug log entry that will always be output, independent of LogCategory settings.
         * @param message String; the message to log
         */
        public void debug(final String message)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.DEBUG, message);
        }

        /**
         * Create a debug log entry that will always be output, independent of LogCategory settings.
         * @param message String; the message to be logged, where {} entries will be replaced by arguments
         * @param arguments Object...; the arguments to substitute for the {} entries in the message string
         */
        public void debug(final String message, final Object... arguments)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.DEBUG, message, arguments);
        }

        /**
         * Create a debug log entry that will always be output, independent of LogCategory settings.
         * @param exception Throwable; the exception to log
         */
        public void debug(final Throwable exception)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.DEBUG, exception);
        }

        /**
         * Create a debug log entry that will always be output, independent of LogCategory settings.
         * @param exception Throwable; the exception to log
         * @param message String; the message to log
         */
        public void debug(final Throwable exception, final String message)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.DEBUG, exception, message);
        }

        /**
         * Create a debug log entry that will always be output, independent of LogCategory settings.
         * @param exception Throwable; the exception to log
         * @param message String; the message to log, where {} entries will be replaced by arguments
         * @param arguments Object...; the arguments to substitute for the {} entries in the message string
         */
        public void debug(final Throwable exception, final String message, final Object... arguments)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.DEBUG, exception, message, arguments);
        }

        /* ****************************************** INFO ******************************************/

        /**
         * Create a info log entry that will always be output, independent of LogCategory settings.
         * @param object Object; the result of the <code>toString()</code> method of <code>object</code> will be logged
         */
        public void info(final Object object)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.INFO, object);
        }

        /**
         * Create a info log entry that will always be output, independent of LogCategory settings.
         * @param message String; the message to log
         */
        public void info(final String message)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.INFO, message);
        }

        /**
         * Create a info log entry that will always be output, independent of LogCategory settings.
         * @param message String; the message to be logged, where {} entries will be replaced by arguments
         * @param arguments Object...; the arguments to substitute for the {} entries in the message string
         */
        public void info(final String message, final Object... arguments)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.INFO, message, arguments);
        }

        /**
         * Create a info log entry that will always be output, independent of LogCategory settings.
         * @param exception Throwable; the exception to log
         */
        public void info(final Throwable exception)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.INFO, exception);
        }

        /**
         * Create a info log entry that will always be output, independent of LogCategory settings.
         * @param exception Throwable; the exception to log
         * @param message String; the message to log
         */
        public void info(final Throwable exception, final String message)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.INFO, exception, message);
        }

        /**
         * Create a info log entry that will always be output, independent of LogCategory settings.
         * @param exception Throwable; the exception to log
         * @param message String; the message to log, where {} entries will be replaced by arguments
         * @param arguments Object...; the arguments to substitute for the {} entries in the message string
         */
        public void info(final Throwable exception, final String message, final Object... arguments)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.INFO, exception, message, arguments);
        }

        /* ****************************************** WARN ******************************************/

        /**
         * Create a warn log entry that will always be output, independent of LogCategory settings.
         * @param object Object; the result of the <code>toString()</code> method of <code>object</code> will be logged
         */
        public void warn(final Object object)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.WARNING, object);
        }

        /**
         * Create a warn log entry that will always be output, independent of LogCategory settings.
         * @param message String; the message to log
         */
        public void warn(final String message)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.WARNING, message);
        }

        /**
         * Create a warn log entry that will always be output, independent of LogCategory settings.
         * @param message String; the message to be logged, where {} entries will be replaced by arguments
         * @param arguments Object...; the arguments to substitute for the {} entries in the message string
         */
        public void warn(final String message, final Object... arguments)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.WARNING, message, arguments);
        }

        /**
         * Create a warn log entry that will always be output, independent of LogCategory settings.
         * @param exception Throwable; the exception to log
         */
        public void warn(final Throwable exception)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.WARNING, exception);
        }

        /**
         * Create a warn log entry that will always be output, independent of LogCategory settings.
         * @param exception Throwable; the exception to log
         * @param message String; the message to log
         */
        public void warn(final Throwable exception, final String message)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.WARNING, exception, message);
        }

        /**
         * Create a warn log entry that will always be output, independent of LogCategory settings.
         * @param exception Throwable; the exception to log
         * @param message String; the message to log, where {} entries will be replaced by arguments
         * @param arguments Object...; the arguments to substitute for the {} entries in the message string
         */
        public void warn(final Throwable exception, final String message, final Object... arguments)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.WARNING, exception, message, arguments);
        }

        /* ****************************************** ERROR ******************************************/

        /**
         * Create a error log entry that will always be output, independent of LogCategory settings.
         * @param object Object; the result of the <code>toString()</code> method of <code>object</code> will be logged
         */
        public void error(final Object object)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.ERROR, object);
        }

        /**
         * Create a error log entry that will always be output, independent of LogCategory settings.
         * @param message String; the message to log
         */
        public void error(final String message)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.ERROR, message);
        }

        /**
         * Create a error log entry that will always be output, independent of LogCategory settings.
         * @param message String; the message to be logged, where {} entries will be replaced by arguments
         * @param arguments Object...; the arguments to substitute for the {} entries in the message string
         */
        public void error(final String message, final Object... arguments)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.ERROR, message, arguments);
        }

        /**
         * Create a error log entry that will always be output, independent of LogCategory settings.
         * @param exception Throwable; the exception to log
         */
        public void error(final Throwable exception)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.ERROR, exception);
        }

        /**
         * Create a error log entry that will always be output, independent of LogCategory settings.
         * @param exception Throwable; the exception to log
         * @param message String; the message to log
         */
        public void error(final Throwable exception, final String message)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.ERROR, exception, message);
        }

        /**
         * Create a error log entry that will always be output, independent of LogCategory settings.
         * @param exception Throwable; the exception to log
         * @param message String; the message to log, where {} entries will be replaced by arguments
         * @param arguments Object...; the arguments to substitute for the {} entries in the message string
         */
        public void error(final Throwable exception, final String message, final Object... arguments)
        {
            if (this.log)
                LogEntryForwarder.forward(1, Level.ERROR, exception, message, arguments);
        }
    }
}
