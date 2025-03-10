package org.opentrafficsim.core.dsol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.dsol.AbstractOtsModelTest.OtsModel;
import org.opentrafficsim.core.perception.HistoryManagerDevs;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Test the OtsSimulator class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public final class OtsSimulatorTest
{
    /** Store argument of eventReceiver. */
    private String receivedArgument = null;

    /** */
    private OtsSimulatorTest()
    {
        // do not instantiate test class
    }

    /**
     * Test the OtsSimulator class.
     * @throws NamingException if that happens uncaught; this test has failed
     * @throws SimRuntimeException if that happens uncaught; this test has failed
     * @throws InterruptedException if that happens uncaught; this test has failed
     */
    @Test
    public void otsSimulatorTest() throws SimRuntimeException, NamingException, InterruptedException
    {
        String id = "Simulator for OtsSimulator test";
        OtsSimulator simulator = new OtsSimulator(id);
        Time startTime = new Time(10, TimeUnit.BASE_SECOND);
        Duration warmupDuration = new Duration(20, DurationUnit.SECOND);
        Duration runLength = new Duration(500, DurationUnit.SECOND);
        OtsModel model = new OtsModel(simulator);
        simulator.initialize(startTime, warmupDuration, runLength, model, HistoryManagerDevs.noHistory(simulator));
        assertEquals(startTime, simulator.getStartTimeAbs(), "startTime is returned");
        assertEquals(warmupDuration, simulator.getReplication().getWarmupPeriod(), "warmupDuration is returned");
        assertEquals(runLength, simulator.getReplication().getRunLength(), "runLength is returned");
        assertTrue(simulator.toString().startsWith("OtsSimulator"), "toString returns something descriptive");
        String testArgument = "test argument";
        simulator.scheduleEventAbsTime(new Time(400, TimeUnit.BASE_SECOND), (short) 0, this, "eventReceiver",
                new Object[] {testArgument});
        simulator.start();
        while (simulator.isStartingOrRunning())
        {
            Thread.sleep(100);
        }
        assertFalse(simulator.isStartingOrRunning(), "simulator has stopped");
        assertEquals(runLength, simulator.getSimulatorAbsTime().minus(startTime), "simulator time is runLength");
        assertEquals(testArgument, this.receivedArgument, "event has been executed");
    }

    /**
     * Tests that scheduled event gets executed.
     * @param argument argument of this method
     */
    public void eventReceiver(final String argument)
    {
        this.receivedArgument = argument;
    }

}
