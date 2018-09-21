package org.opentrafficsim.core.logger;

import java.util.Set;

import org.opentrafficsim.base.logger.CategoryLogger;
import org.opentrafficsim.base.logger.LogCategory;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.LogEntryForwarder;
import org.pmw.tinylog.writers.Writer;

import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * SimLogger, "extends" the CategoryLogger to be simulator aware and able to print the simulator time as part of the log
 * message. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
@SuppressWarnings({ "checkstyle:finalclass", "checkstyle:needbraces" })
public class SimLogger extends CategoryLogger
{
    /** The simulator of which to include the time in the log messages. */
    private static SimulatorInterface<?, ?, ?> simulator = null;

    /** The delegate logger instance that does the actual logging work, after a positive filter outcome. */
    private static DelegateSimLogger delegateSimLogger = new DelegateSimLogger();

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
     * Set the simulator of which to include the time in the log messages.
     * @param newSimulator the simulator to set
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
     * Set a new logging level for one of the registered writers.
     * @param writer the writer to change the log level for
     * @param newLevel the new log level for the writer
     */
    public static void setLogLevel(final Writer writer, final Level newLevel)
    {
        CategoryLogger.setLogLevel(writer, newLevel);
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
            return delegateSimLogger;
        if (categories.contains(logCategory))
            return delegateSimLogger;
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
            return delegateSimLogger;
        for (LogCategory logCategory : logCategories)
        {
            if (categories.contains(logCategory))
                return delegateSimLogger;
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
            return delegateSimLogger;
        for (LogCategory logCategory : logCategories)
        {
            if (categories.contains(logCategory))
                return delegateSimLogger;
        }
        return noLogger;
    }

    /**
     * Insert the simulator time into the message.
     * @param message the original message
     * @return the message with the simulator time at the front
     */
    static String insertSimTime(final String message)
    {
        if (simulator == null)
            return message;
        return simulator.getSimulatorTime() + " - " + message;
    }

    /**
     * DelegateSimLogger class that takes care of actually logging the message and/or exception. The methods take care of
     * inserting the simulation time in the message.<br>
     * <br>
     * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
     * See for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>.
     * The source code and binary code of this software is proprietary information of Delft University of Technology.
     * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
     */
    public static class DelegateSimLogger extends DelegateLogger
    {
        /**
         * Create an instance of the DelegateSimLogger that takes care of actually logging the message and/or exception.
         */
        public DelegateSimLogger()
        {
            super(true);
        }

        /* ****************************************** TRACE ******************************************/

        /** {@inheritDoc} */
        @Override
        public void trace(final Object object)
        {
            LogEntryForwarder.forward(1, Level.TRACE, insertSimTime(object.toString()));
        }

        /** {@inheritDoc} */
        @Override
        public void trace(final String message)
        {
            LogEntryForwarder.forward(1, Level.TRACE, insertSimTime(message));
        }

        /** {@inheritDoc} */
        @Override
        public void trace(final String message, final Object... arguments)
        {
            LogEntryForwarder.forward(1, Level.TRACE, insertSimTime(message), arguments);
        }

        /** {@inheritDoc} */
        @Override
        public void trace(final Throwable exception)
        {
            LogEntryForwarder.forward(1, Level.TRACE, exception, insertSimTime(exception.getMessage()));
        }

        /** {@inheritDoc} */
        @Override
        public void trace(final Throwable exception, final String message)
        {
            LogEntryForwarder.forward(1, Level.TRACE, exception, insertSimTime(message));
        }

        /** {@inheritDoc} */
        @Override
        public void trace(final Throwable exception, final String message, final Object... arguments)
        {
            LogEntryForwarder.forward(1, Level.TRACE, exception, insertSimTime(message), arguments);
        }

        /* ****************************************** DEBUG ******************************************/

        /** {@inheritDoc} */
        @Override
        public void debug(final Object object)
        {
            LogEntryForwarder.forward(1, Level.DEBUG, insertSimTime(object.toString()));
        }

        /** {@inheritDoc} */
        @Override
        public void debug(final String message)
        {
            LogEntryForwarder.forward(1, Level.DEBUG, insertSimTime(message));
        }

        /** {@inheritDoc} */
        @Override
        public void debug(final String message, final Object... arguments)
        {
            LogEntryForwarder.forward(1, Level.DEBUG, insertSimTime(message), arguments);
        }

        /** {@inheritDoc} */
        @Override
        public void debug(final Throwable exception)
        {
            LogEntryForwarder.forward(1, Level.DEBUG, exception, insertSimTime(exception.getMessage()));
        }

        /** {@inheritDoc} */
        @Override
        public void debug(final Throwable exception, final String message)
        {
            LogEntryForwarder.forward(1, Level.DEBUG, exception, insertSimTime(message));
        }

        /** {@inheritDoc} */
        @Override
        public void debug(final Throwable exception, final String message, final Object... arguments)
        {
            LogEntryForwarder.forward(1, Level.DEBUG, exception, insertSimTime(message), arguments);
        }

        /* ****************************************** INFO ******************************************/

        /** {@inheritDoc} */
        @Override
        public void info(final Object object)
        {
            LogEntryForwarder.forward(1, Level.INFO, insertSimTime(object.toString()));
        }

        /** {@inheritDoc} */
        @Override
        public void info(final String message)
        {
            LogEntryForwarder.forward(1, Level.INFO, insertSimTime(message));
        }

        /** {@inheritDoc} */
        @Override
        public void info(final String message, final Object... arguments)
        {
            LogEntryForwarder.forward(1, Level.INFO, insertSimTime(message), arguments);
        }

        /** {@inheritDoc} */
        @Override
        public void info(final Throwable exception)
        {
            LogEntryForwarder.forward(1, Level.INFO, exception, insertSimTime(exception.getMessage()));
        }

        /** {@inheritDoc} */
        @Override
        public void info(final Throwable exception, final String message)
        {
            LogEntryForwarder.forward(1, Level.INFO, exception, insertSimTime(message));
        }

        /** {@inheritDoc} */
        @Override
        public void info(final Throwable exception, final String message, final Object... arguments)
        {
            LogEntryForwarder.forward(1, Level.INFO, exception, insertSimTime(message), arguments);
        }

        /* ****************************************** WARN ******************************************/

        /** {@inheritDoc} */
        @Override
        public void warn(final Object object)
        {
            LogEntryForwarder.forward(1, Level.WARNING, insertSimTime(object.toString()));
        }

        /** {@inheritDoc} */
        @Override
        public void warn(final String message)
        {
            LogEntryForwarder.forward(1, Level.WARNING, insertSimTime(message));
        }

        /** {@inheritDoc} */
        @Override
        public void warn(final String message, final Object... arguments)
        {
            LogEntryForwarder.forward(1, Level.WARNING, insertSimTime(message), arguments);
        }

        /** {@inheritDoc} */
        @Override
        public void warn(final Throwable exception)
        {
            LogEntryForwarder.forward(1, Level.WARNING, exception, insertSimTime(exception.getMessage()));
        }

        /** {@inheritDoc} */
        @Override
        public void warn(final Throwable exception, final String message)
        {
            LogEntryForwarder.forward(1, Level.WARNING, exception, insertSimTime(message));
        }

        /** {@inheritDoc} */
        @Override
        public void warn(final Throwable exception, final String message, final Object... arguments)
        {
            LogEntryForwarder.forward(1, Level.WARNING, exception, insertSimTime(message), arguments);
        }

        /* ****************************************** ERROR ******************************************/

        /** {@inheritDoc} */
        @Override
        public void error(final Object object)
        {
            LogEntryForwarder.forward(1, Level.ERROR, insertSimTime(object.toString()));
        }

        /** {@inheritDoc} */
        @Override
        public void error(final String message)
        {
            LogEntryForwarder.forward(1, Level.ERROR, insertSimTime(message));
        }

        /** {@inheritDoc} */
        @Override
        public void error(final String message, final Object... arguments)
        {
            LogEntryForwarder.forward(1, Level.ERROR, insertSimTime(message), arguments);
        }

        /** {@inheritDoc} */
        @Override
        public void error(final Throwable exception)
        {
            LogEntryForwarder.forward(1, Level.ERROR, exception, insertSimTime(exception.getMessage()));
        }

        /** {@inheritDoc} */
        @Override
        public void error(final Throwable exception, final String message)
        {
            LogEntryForwarder.forward(1, Level.ERROR, exception, insertSimTime(message));
        }

        /** {@inheritDoc} */
        @Override
        public void error(final Throwable exception, final String message, final Object... arguments)
        {
            LogEntryForwarder.forward(1, Level.ERROR, exception, insertSimTime(message), arguments);
        }

    }
}
