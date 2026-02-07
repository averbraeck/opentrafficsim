package org.opentrafficsim.base.logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.djutils.logger.CategoryLogger;
import org.djutils.logger.CategoryLogger.CategoryAppenderFactory;
import org.djutils.logger.LogCategory;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.OtsRuntimeException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.testUtil.StringListAppender;

/**
 * Test Logger class.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class LoggerTester
{

    /** Test appender which stores log lines in a list so they can be checked. */
    private static StringListAppender<ILoggingEvent> appender = new StringListAppender<>();

    static
    {
        Logger.ots(); // adds the OTS log category by loading the class
        Logger.setLogLevel(Level.TRACE);
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
    }

    /**
     * Constructor.
     */
    private LoggerTester()
    {
        //
    }

    /**
     * Tests OTS logger.
     */
    @Test
    public void testTime()
    {
        // Setting log level
        int preLength = appender.strList.size();
        Logger.setLogLevel(Level.ERROR);
        Logger.ots().warn("level_message");
        assertEquals(preLength, appender.strList.size());
        Logger.setLogLevel(Level.TRACE);

        // Test a message
        Logger.ots().trace("test_message");
        assertContains(-1, "test_message", "Message did not end up in log line.");

        // Test removing no or not previously added supplier; should do nothing
        Supplier<Double> simulationTimeSupplier = () -> 3.14;
        Logger.removeSimTimeSupplier(null);
        Logger.removeSimTimeSupplier(simulationTimeSupplier);

        // Test time in log
        Logger.setSimTimeSupplier(simulationTimeSupplier);
        Logger.ots().trace("test_message");
        assertContains(-1, "[   3.140s]", "Simulation time did not end up in log line, or not correctly formatted.");

        // Test time from other supplier
        Supplier<Double> anotherSimulationTimeSupplier = () -> 2.14;
        Logger.setSimTimeSupplier(anotherSimulationTimeSupplier);
        Logger.ots().trace("test_message");
        assertContains(-1, "[   2.140s]", "Setting another simulation time provider should change the simulation time.");

        // Test time remains after removing old supplier (which should do nothing)
        Logger.removeSimTimeSupplier(simulationTimeSupplier);
        Logger.ots().trace("test_message");
        assertContains(-1, "[   2.140s]", "Removing non-active simulation time provider should remove nothing.");

        // Test no time after removing new supplier
        Logger.removeSimTimeSupplier(anotherSimulationTimeSupplier);
        Logger.ots().trace("test_message");
        assertNotContains("[   2.140s]", "Removing simulation time provider should remove simulation time.");

        // Test custom format without blank
        Logger.setSimTimeSupplier(simulationTimeSupplier, "[%6.2fs]");
        Logger.ots().trace("test_message");
        assertContains(-1, " [  3.14s]", "Dedicated format without blank should format the simulation time.");

        // Test custom format with blank
        Logger.setSimTimeSupplier(anotherSimulationTimeSupplier, " [%6.2fs]");
        Logger.ots().trace("test_message");
        assertContains(-1, " [  2.14s]", "Dedicated format with blank should format the simulation time.");

        // Test format problem
        Logger.setSimTimeSupplier(simulationTimeSupplier, "[%6.@#$3fs]");
        Logger.ots().trace("test_message");
        assertContains(-1, " T_FORMAT_ERR", "Bad format should end up as FORMAT_ERR in log.");
        assertNotContains(" [ 3.140s]", "Simulation time should not end up in log with a bad format.");

        // Test empty format meaning no time (and no blank) is included
        Logger.setSimTimeSupplier(simulationTimeSupplier, "");
        Logger.ots().trace("test_message");
        assertNotContains("  ", "Empty format string should not result in double spaces.");
    }

    /**
     * Tests that parallel simulations and the threads that run them report time specific to the simulation.
     */
    @Test
    public void testParallel()
    {
        // Store results of threads (assertTrue in other than JUNIT thread does nothing for the test itself)
        List<Integer> indexList = new ArrayList<>();
        List<String> matchList = new ArrayList<>();
        List<String> messageList = new ArrayList<>();

        // Create simulation threads (parents) and for each a child thread, with logging in all these threads
        List<Thread> threads = new ArrayList<>();
        List<Double> time = new ArrayList<>(); // test time per thread (also for its child thread)
        for (int i = 0; i < 5; i++)
        {
            time.add((double) i);
            final int j = i; // final for in lambda
            threads.add(new Thread(() ->
            {
                // Set time supplier and string to match for this parent thread
                Logger.setSimTimeSupplier(() -> time.get(j), "[%" + (8 + j) + ".3fs]");
                String match = " [   " + " ".repeat(j) + j + ".000s]";
                synchronized (appender)
                {
                    Logger.ots().info("test_message");
                    indexList.add(appender.strList.size() - 1);
                    matchList.add(match);
                    messageList.add("Thread did not log its own time.");
                }
                // Child thread logging hopefully on the same time
                Thread child = new Thread(() ->
                {
                    synchronized (appender)
                    {
                        Logger.ots().info("test_message");
                        indexList.add(appender.strList.size() - 1);
                        matchList.add(match);
                        messageList.add("Thread child did not log its parent time.");
                    }
                });
                child.run();
                // Wait for child to finish
                try
                {
                    child.join();
                }
                catch (InterruptedException exception)
                {
                    throw new OtsRuntimeException(exception);
                }
            }));
        }

        // Start all threads
        for (Thread thread : threads)
        {
            thread.start();
        }

        // The test thread, which created all simulation threads, should not have a time
        synchronized (appender)
        {
            Logger.ots().trace("test_message");
            assertNotContains("[", "Non-simulation thread should not contain time.");
        }

        // Join all threads (wait for them to finish)
        for (Thread thread : threads)
        {
            try
            {
                thread.join();
            }
            catch (InterruptedException exception)
            {
                throw new OtsRuntimeException(exception);
            }
        }

        // Check results
        for (int i = 0; i < indexList.size(); i++)
        {
            assertContains(indexList.get(i), matchList.get(i), messageList.get(i));
        }
    }

    /**
     * Tests that PID and Thread id are reported.
     */
    @Test
    public void testIds()
    {
        // Differently formatted PID's
        Logger.includePid(true);
        Logger.ots().trace("test_message");
        assertContains(-1, "pid=" + String.valueOf(ProcessHandle.current().pid()), "PID not logged.");
        Logger.includePid("pid:%d");
        Logger.ots().trace("test_message");
        assertNotContains("pid=" + String.valueOf(ProcessHandle.current().pid()), "PID logged with old format.");
        assertContains(-1, "pid:" + String.valueOf(ProcessHandle.current().pid()), "PID not logged with new format.");
        Logger.includePid(false);
        Logger.ots().trace("test_message");
        assertNotContains("pid", "PID logged although logging it was disabled.");

        // Bad PID format
        Logger.includePid("pid:%@#$d");
        Logger.ots().trace("test_message");
        assertContains(-1, "PID_FORMAT_ERR", "PID format error not reported.");
        assertNotContains("pid", "Bad format still results in a reported pid.");
        Logger.includePid(false); // reset

        // Differently formatted thread ids
        Logger.includeThreadId(true);
        Logger.ots().trace("test_message");
        assertContains(-1, "thread=" + String.valueOf(Thread.currentThread().getId()), "Thread id not logged.");
        Logger.includeThreadId("thread:%d");
        Logger.ots().trace("test_message");
        assertNotContains("thread=" + String.valueOf(Thread.currentThread().getId()), "Thread id logged with old format.");
        assertContains(-1, "thread:" + String.valueOf(Thread.currentThread().getId()), "Thread id not logged with new format.");
        Logger.includeThreadId(false);
        Logger.ots().trace("test_message");
        assertNotContains("thread", "Thread id logged although logging it was disabled.");

        // Bad Thread id format
        Logger.includeThreadId("thread:%@#$d");
        Logger.ots().trace("test_message");
        assertContains(-1, "THREAD_FORMAT_ERR", "Thread id format error not reported.");
        assertNotContains("pid", "Bad format still results in a reported thread id.");
        Logger.includeThreadId(false); // reset
    }

    /**
     * Asserts that the given contains string is contained in the last line of the log.
     * @param index index of the log message, use {@code -1} for the last
     * @param contains contains string
     * @param message test fail message
     */
    private void assertContains(final int index, final String contains, final String message)
    {
        int ndx = index < 0 ? appender.strList.size() - 1 : index;
        assertTrue(appender.strList.get(ndx).contains(contains), message);
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
