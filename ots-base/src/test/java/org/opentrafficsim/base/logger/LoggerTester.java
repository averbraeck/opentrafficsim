package org.opentrafficsim.base.logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Supplier;

import org.djutils.logger.CategoryLogger;
import org.djutils.logger.CategoryLogger.CategoryAppenderFactory;
import org.djutils.logger.CategoryLogger.ConsoleAppenderFactory;
import org.djutils.logger.LogCategory;
import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.testUtil.StringListAppender;

/**
 * Test Logger class.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LoggerTester
{

    /** Test appender which stores log lines in a list so they can be checked. */
    private static StringListAppender<ILoggingEvent> appender = new StringListAppender<>();

    /**
     * Tests OTS logger.
     */
    @Test
    public void testLogger()
    {
        Logger.ots(); // adds the OTS log category by loading the class
        CategoryLogger.setLogLevelAll(Level.TRACE);
        CategoryLogger.removeAppender("CONSOLE"); // disable console logging
        CategoryLogger.addAppender("TEST", new CategoryAppenderFactory()
        {
            @Override
            public String id()
            {
                return "TEST";
            }

            @Override
            public Appender<ILoggingEvent> create(final String id, final LogCategory category, final String messageFormat,
                    final LoggerContext ctx)
            {
                PatternLayout layout = new PatternLayout();
                layout.setContext(ctx);
                layout.setPattern(messageFormat);
                layout.start();
                appender.setLayout(layout);
                return appender;
            }
        });

        Logger.ots().trace("test_message");
        assertContains("test_message", "Message did not end up in log line.");

        Supplier<Double> simulationTimeSupplier = () -> 3.14;
        Logger.removeSimTimeSupplier(null); // should do nothing
        Logger.removeSimTimeSupplier(simulationTimeSupplier); // should do nothing

        Logger.setSimTimeSupplier(simulationTimeSupplier);
        Logger.ots().trace("test_message");
        assertContains("[   3.140s]", "Simulation time did not end up in log line, or not correctly formatted.");

        Supplier<Double> anotherSimulationTimeSupplier = () -> 2.14;
        Logger.setSimTimeSupplier(anotherSimulationTimeSupplier);
        Logger.ots().trace("test_message");
        assertContains("[   2.140s]", "Setting another simulation time provider should change the simulation time.");

        Logger.removeSimTimeSupplier(simulationTimeSupplier);
        Logger.ots().trace("test_message");
        assertContains("[   2.140s]", "Removing non-active simulation time provider should remove nothing.");

        Logger.removeSimTimeSupplier(anotherSimulationTimeSupplier);
        Logger.ots().trace("test_message");
        assertNotContains("[   2.140s]", "Removing simulation time provider should remove simulation time.");

        Logger.setSimTimeSupplier(simulationTimeSupplier, "[%6.3fs]");
        Logger.ots().trace("test_message");
        assertContains(" [ 3.140s]", "Dedicated format without blank should format the simulation time.");

        Logger.setSimTimeSupplier(anotherSimulationTimeSupplier, " [%6.3fs]");
        Logger.ots().trace("test_message");
        assertContains(" [ 2.140s]", "Dedicated format with blank should format the simulation time.");

        Logger.setSimTimeSupplier(simulationTimeSupplier, "[%6.@#$3fs]");
        Logger.ots().trace("test_message");
        assertContains(" [FORMAT_ERR]", "Bad format should end up as FORMAT_ERR in log.");
        assertNotContains(" [ 3.140s]", "Simulation time should not end up in log with a bad format.");

        Logger.setSimTimeSupplier(simulationTimeSupplier, "");
        Logger.ots().trace("test_message");
        assertNotContains("  ", "Empty format string should not result in double spaces.");

        CategoryLogger.addAppender("CONSOLE", new ConsoleAppenderFactory("CONSOLE")); // reconstruct console logging
    }

    /**
     * Asserts that the given contains string is contained in the last line of the log.
     * @param contains contains string
     * @param message test fail message
     */
    private void assertContains(final String contains, final String message)
    {
        assertTrue(appender.strList.get(appender.strList.size() - 1).contains(contains), message);
    }

    /**
     * Asserts that the given not-contains string is not contained in the last line of the log.
     * @param notContains not-contains string
     * @param message test fail message
     */
    private void assertNotContains(final String notContains, final String message)
    {
        assertFalse(appender.strList.get(appender.strList.size() - 1).contains(notContains), message);
    }

}
