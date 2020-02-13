package org.opentrafficsim.core.dsol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.Test;
import org.opentrafficsim.core.dsol.AbstractOTSModelTest.OTSModel;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Test the OTSSimulator class.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Feb 13, 2020 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class OTSSimulatorTest
{
    /** Store argument of eventReceiver. */
    private String receivedArgument = null;
    
    /**
     * Test the OTSSimulator class.
     * @throws NamingException if that happens uncaught; this test has failed
     * @throws SimRuntimeException if that happens uncaught; this test has failed
     * @throws InterruptedException if that happens uncaught; this test has failed
     */
    @Test
    public void otsSimulatorTest() throws SimRuntimeException, NamingException, InterruptedException
    {
        String id = "Simulator for OTSSimulator test";
        OTSSimulator simulator = new OTSSimulator(id);
        assertEquals("id can be retrieved", id, simulator.getSourceId());
        Time startTime = new Time(10, TimeUnit.BASE_SECOND);
        Duration warmupDuration = new Duration(20, DurationUnit.SECOND);
        Duration runLength = new Duration(500, DurationUnit.SECOND);
        OTSModel model = new OTSModel(simulator);
        simulator.initialize(startTime, warmupDuration, runLength, model);
        assertEquals("startTime is returned", startTime, simulator.getReplication().getTreatment().getStartTime());
        assertEquals("warmupDuration is returned", warmupDuration, simulator.getReplication().getTreatment().getWarmupPeriod());
        assertEquals("model is returned", model, simulator.getReplication().getExperiment().getModel());
        assertEquals("runLength is returned", runLength, simulator.getReplication().getTreatment().getRunLength());
        assertTrue("toString returns something descriptive", simulator.toString().startsWith("OTSSimulator"));
        String testArgument = "test argument";
        simulator.scheduleEvent(new Time(400, TimeUnit.BASE_SECOND), (short) 0, this, this, "eventReceiver",
                new Object[] { testArgument });
        simulator.start();
        while (simulator.isRunning())
        {
            Thread.sleep(100);
        }
        assertFalse("simulator has stopped", simulator.isRunning());
        assertEquals("simulator time is runLength", runLength, simulator.getSimTime().get().minus(startTime));
        assertEquals("event has been executed", testArgument, this.receivedArgument);
    }
    
    /**
     * Tests that scheduled event gets executed.
     * @param argument String; argument of this method
     */
    public void eventReceiver(final String argument)
    {
        this.receivedArgument = argument;
    }

}
