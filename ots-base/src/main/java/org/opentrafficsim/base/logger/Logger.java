package org.opentrafficsim.base.logger;

import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.function.Supplier;

import org.djutils.logger.CategoryLogger;
import org.djutils.logger.CategoryLogger.DelegateLogger;
import org.djutils.logger.LogCategory;

import ch.qos.logback.classic.Level;

/**
 * Logger for within OTS context. This logger uses category OTS and a format that may include simulation time, PID and thread
 * id. Every simulator in OTS can attach itself as simulation time supplier through {@code Logger.setSimtimeSupplier()}. The
 * time supplier only applies to the {@link Thread} that creates it, and all threads created downstream of this thread. This
 * means that when different simulations run within a single JVM, each log line will report the time of the relevant simulation.
 * This does require that the simulator (or animator) is created in a dedicated thread for the parallel simulation. Example
 * usage:
 *
 * <pre>
 * Logger.ots().trace("Logging example");
 * </pre>
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class Logger
{

    /** Default OTS log category. */
    public static final LogCategory OTS = new LogCategory("OTS");

    /** OTS logging pattern which includes the simulation time. */
    private static final String PATTERN =
            "%date{HH:mm:ss}%X{time}%X{pid}%X{thread} %-5level %-3logger{0} %class{0}.%method:%line - %msg%n";

    /** Delegate logger with OTS category. */
    private static final DelegateLogger LOGGER;

    /** Default time format. */
    private static final String DEFAULT_TIME_FORMAT = "[%8.3fs]";

    /** Supplier of simulation time. */
    private static InheritableThreadLocal<Supplier<? extends Number>> simTimeSupplier = new InheritableThreadLocal<>();

    /** Format to display the time. */
    private static InheritableThreadLocal<String> timeFormatString = new InheritableThreadLocal<>();

    /** Default PID format. */
    private static final String DEFAULT_PID_FORMAT = "pid=%d";

    /** Include PID. */
    private static boolean includePid = false;

    /** PID format. */
    private static String pidFormat;

    /** Default thread id format. */
    private static final String DEFAULT_THREAD_ID_FORMAT = "thread=%d";

    /** Include thread id. */
    private static boolean includeThreadId = false;

    /** Thread id format. */
    private static String threadIdFormat;

    static
    {
        CategoryLogger.addLogCategory(OTS);
        CategoryLogger.setPattern(OTS, PATTERN);
        CategoryLogger.addFormatter(OTS, "time", Logger::getSimTimeString);
        CategoryLogger.addFormatter(OTS, "pid", Logger::getPidString);
        CategoryLogger.addFormatter(OTS, "thread", Logger::getThreadIdString);
        LOGGER = CategoryLogger.with(OTS);
    }

    /**
     * Constructor.
     */
    private Logger()
    {
        //
    }

    /**
     * Returns logger with the OTS category.
     * @return logger with the OTS category
     */
    public static DelegateLogger ots()
    {
        return LOGGER;
    }

    /**
     * Set log level of the OTS logger.
     * @param level log level
     */
    public static void setLogLevel(final Level level)
    {
        CategoryLogger.setLogLevel(OTS, level);
    }

    /**
     * Sets supplier of the simulation time. The value is formatted with {@code [%8.3fs]}. The time supplier only applies to the
     * {@code Thread} that creates it, and all threads created downstream of this thread.
     * @param timeSupplier supplier of the simulation time
     */
    public static void setSimTimeSupplier(final Supplier<? extends Number> timeSupplier)
    {
        setSimTimeSupplier(timeSupplier, DEFAULT_TIME_FORMAT);
    }

    /**
     * Sets supplier of the simulation time with dedicated format string. If the format string does not start with a blank, a
     * blank is added at the beginning of the format string. The time supplier only applies to the {@code Thread} that creates
     * it, and all threads created downstream of this thread.
     * @param timeSupplier supplier of the simulation time
     * @param timeFormat format string for the time value
     */
    public static void setSimTimeSupplier(final Supplier<? extends Number> timeSupplier, final String timeFormat)
    {
        simTimeSupplier.set(timeSupplier);
        timeFormatString.set(formatWithBlank(timeFormat));
    }

    /**
     * Removes the supplier of the simulation time, but only if it is the same as the input argument.
     * @param timeSupplier supplier of the simulation time
     */
    public static void removeSimTimeSupplier(final Supplier<? extends Number> timeSupplier)
    {
        if (timeSupplier != null && timeSupplier.equals(simTimeSupplier.get()))
        {
            simTimeSupplier.remove();
        }
    }

    /**
     * Returns the string of the simulation time for the log message.
     * @return the string of the simulation time for the log message
     */
    private static String getSimTimeString()
    {
        Supplier<? extends Number> timeSupplier = simTimeSupplier.get();
        try
        {
            return timeSupplier == null ? ""
                    : String.format(Locale.US, timeFormatString.get(), timeSupplier.get().doubleValue());
        }
        catch (IllegalFormatException ex)
        {
            return " T_FORMAT_ERR";
        }
    }

    /**
     * Sets the PID to be included in formatting or not. The default format is "pid=%d". To provide a format string use
     * {@code includePid(String)}.
     * @param include whether to include the PID in formatting
     */
    public static void includePid(final boolean include)
    {
        if (include)
        {
            includePid(DEFAULT_PID_FORMAT);
            return;
        }
        includePid = false;
    }

    /**
     * Sets the PID to be included in formatting with the given format.
     * @param format format for PID
     */
    public static void includePid(final String format)
    {
        includePid = true;
        pidFormat = formatWithBlank(format);
    }

    /**
     * Returns the pid (process id).
     * @return pid
     */
    private static String getPidString()
    {
        try
        {
            return includePid ? String.format(pidFormat, ProcessHandle.current().pid()) : "";
        }
        catch (IllegalFormatException ex)
        {
            return " PID_FORMAT_ERR";
        }
    }

    /**
     * Sets the thread id to be included in formatting or not. The default format is "thread=%d". To provide a format string use
     * {@code includeThreadId(String)}.
     * @param include whether to include the thread id in formatting
     */
    public static void includeThreadId(final boolean include)
    {
        if (include)
        {
            includeThreadId(DEFAULT_THREAD_ID_FORMAT);
            return;
        }
        includeThreadId = false;
    }

    /**
     * Sets the PID to be included in formatting with the given format.
     * @param format format for PID
     */
    public static void includeThreadId(final String format)
    {
        includeThreadId = true;
        threadIdFormat = formatWithBlank(format);
    }

    /**
     * Returns the thread id.
     * @return thread id
     */
    private static String getThreadIdString()
    {
        try
        {
            return includeThreadId ? String.format(threadIdFormat, Thread.currentThread().getId()) : "";
        }
        catch (IllegalFormatException ex)
        {
            return " THREAD_FORMAT_ERR";
        }
    }

    /**
     * Prepends a non-empty format with a blank space if it does not already starts with a blank.
     * @param format format
     * @return format that starts with a blank (if not empty)
     */
    private static String formatWithBlank(final String format)
    {
        return format.length() == 0 || format.charAt(0) == ' ' ? format : " " + format;
    }

}
