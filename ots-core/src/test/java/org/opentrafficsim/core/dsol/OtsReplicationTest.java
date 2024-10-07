package org.opentrafficsim.core.dsol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.perception.HistoryManager;
import org.opentrafficsim.core.perception.HistoryManagerDevs;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.Replication;

/**
 * Test the OTSReplication class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class OtsReplicationTest
{
    /**
     * Test the OTSReplication class.
     * @throws NamingException if that happens uncaught; this test has failed
     * @throws RemoteException if that happens uncaught; this test has failed
     */
    @Test
    public void otsReplicationTest() throws NamingException, RemoteException
    {
        String id = "id";
        Time startTime = new Time(100, TimeUnit.BASE_SECOND);
        Duration warmupPeriod = new Duration(200, DurationUnit.SECOND);
        Duration runLength = new Duration(500, DurationUnit.SECOND);
        OtsSimulatorInterface simulator = new OtsSimulator("Simulator for OTSReplicationTest");
        OtsModel model = new OtsModel(simulator);
        OtsReplication replication = new OtsReplication(id, startTime, warmupPeriod, runLength);
        assertEquals(startTime, replication.getStartTimeAbs(), "startTime can be retrieved");
        assertEquals(warmupPeriod, replication.getWarmupPeriod(), "warmupPeriod can be retrieved");
        assertEquals(runLength, replication.getRunLength(), "runLength can be retrieved");
        simulator.initialize(model, replication);
        int listenerCount = simulator.numberOfListeners(Replication.END_REPLICATION_EVENT);
        HistoryManagerDevs hm = (HistoryManagerDevs) replication.getHistoryManager(simulator);
        assertEquals(simulator.getSimulatorAbsTime(), hm.now(), "history manager knows time of simulator");
        assertEquals(listenerCount + 1, simulator.numberOfListeners(Replication.END_REPLICATION_EVENT),
                "history manager has subscribed to our simulator");
        Duration history = new Duration(123, DurationUnit.SECOND);
        Duration cleanupInterval = new Duration(234, DurationUnit.SECOND);
        HistoryManager ourHM = new HistoryManagerDevs(simulator, history, cleanupInterval);
        replication.setHistoryManager(ourHM);
        hm = (HistoryManagerDevs) replication.getHistoryManager(simulator);
        assertEquals(ourHM, hm, "Our manually set history manager is returned");
        assertTrue(replication.toString().startsWith("OTSReplication"), "toString method returns something descriptive");
    }

    /**
     * OTS model for testing.
     */
    static class OtsModel extends AbstractOtsModel
    {
        /** ... */
        private static final long serialVersionUID = 1L;

        /**
         * Construct the instrumented OtsModel.
         * @param simulator the simulator
         * @param shortName the short name of the model
         * @param description the description of the model
         */
        OtsModel(final OtsSimulatorInterface simulator, final String shortName, final String description)
        {
            super(simulator, shortName, description);
        }

        /**
         * Construct the instrumented OtsModel.
         * @param simulator the simulator
         */
        OtsModel(final OtsSimulatorInterface simulator)
        {
            super(simulator);
        }

        @Override
        public Network getNetwork()
        {
            return null;
        }

        @Override
        public void constructModel() throws SimRuntimeException
        {
            // Do nothing
        }

    }

}
