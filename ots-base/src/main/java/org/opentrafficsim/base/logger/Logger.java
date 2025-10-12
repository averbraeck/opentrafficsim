package org.opentrafficsim.base.logger;

import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.function.Supplier;

import org.djutils.logger.CategoryLogger;
import org.djutils.logger.CategoryLogger.DelegateLogger;
import org.djutils.logger.LogCategory;

/**
 * Logger for within OTS context. This logger uses category OTS and a format that may include simulation time. Every simulator
 * in OTS can attach itself as simulation time supplier through {@code Logger.setSimtimeSupplier()}. Example usage:
 *
 * <pre>
 * Logger.ots().trace("Logging example");
 * </pre>
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class Logger
{

    /** Default OTS log category. */
    public static final LogCategory OTS = new LogCategory("OTS");

    /** OTS logging pattern which includes the simulation time. */
    private static final String PATTERN = "%date{HH:mm:ss}%X{time} %-5level %-3logger{0} %class{0}.%method:%line - %msg%n";

    /** Delegate logger with OTS category. */
    private static final DelegateLogger LOGGER;

    /** Supplier of simulation time. */
    private static Supplier<? extends Number> simTimeSupplier;

    /** Format to display the time. */
    private static String timeFormatString;

    static
    {
        CategoryLogger.addLogCategory(OTS);
        CategoryLogger.setPattern(OTS, PATTERN);
        CategoryLogger.addFormatter(OTS, "time", Logger::getSimTimeString);
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
     * Returns the string of the simulation time for the log message.
     * @return the string of the simulation time for the log message
     */
    private static String getSimTimeString()
    {
        if (simTimeSupplier == null)
        {
            return "";
        }
        try
        {
            return String.format(Locale.US, timeFormatString, simTimeSupplier.get().doubleValue());
        }
        catch (IllegalFormatException ex)
        {
            return " [FORMAT_ERR]";
        }
    }

    /**
     * Sets supplier of the simulation time. The value is formatted with {@code [%8.3fs]}.
     * @param timeSupplier supplier of the simulation time
     */
    public static void setSimTimeSupplier(final Supplier<? extends Number> timeSupplier)
    {
        setSimTimeSupplier(timeSupplier, " [%8.3fs]");
    }

    /**
     * Sets supplier of the simulation time with dedicated format string. If the format string does not start with a blank, a
     * blank is added at the beginning of the format string.
     * @param timeSupplier supplier of the simulation time
     * @param timeFormat format string for the time value
     */
    public static void setSimTimeSupplier(final Supplier<? extends Number> timeSupplier, final String timeFormat)
    {
        simTimeSupplier = timeSupplier;
        timeFormatString = timeFormat.length() == 0 || timeFormat.charAt(0) == ' ' ? timeFormat : " " + timeFormat;
    }

    /**
     * Removes the supplier of the simulation time, but only if it is the same as the input argument.
     * @param timeSupplier supplier of the simulation time
     */
    public static void removeSimTimeSupplier(final Supplier<? extends Number> timeSupplier)
    {
        if (timeSupplier != null && timeSupplier.equals(simTimeSupplier))
        {
            simTimeSupplier = null;
        }
    }

}
