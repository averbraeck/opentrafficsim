package org.opentrafficsim.core.dsol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.Test;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.perception.HistoryManager;
import org.opentrafficsim.core.perception.HistoryManagerDEVS;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.ReplicationInterface;

/**
 * Test the OTSReplication class.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Feb 13, 2020 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class OTSReplicationTest
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
        OTSSimulatorInterface simulator = new OTSSimulator("Simulator for OTSReplicationTest");
        OTSModel model = new OTSModel(simulator);
        OTSReplication replication = new OTSReplication(id, startTime, warmupPeriod, runLength);
        assertEquals("startTime can be retrieved", startTime, replication.getStartTime());
        assertEquals("warmupPeriod can be retrieved", warmupPeriod,
                replication.getWarmupPeriod());
        assertEquals("runLength can be retrieved", runLength, replication.getRunLength());
        simulator.initialize(model, replication);
        int listenerCount = simulator.numberOfListeners(ReplicationInterface.END_REPLICATION_EVENT);
        HistoryManagerDEVS hm = (HistoryManagerDEVS) replication.getHistoryManager(simulator);
        assertEquals("history manager knows time of simulator", simulator.getSimulatorTime(), hm.now());
        assertEquals("history manager has subscribed to our simulator", listenerCount + 1,
                simulator.numberOfListeners(ReplicationInterface.END_REPLICATION_EVENT));
        Duration history = new Duration(123, DurationUnit.SECOND);
        Duration cleanupInterval = new Duration(234, DurationUnit.SECOND);
        HistoryManager ourHM = new HistoryManagerDEVS(simulator, history, cleanupInterval);
        replication.setHistoryManager(ourHM);
        hm = (HistoryManagerDEVS) replication.getHistoryManager(simulator);
        assertEquals("Our manually set history manager is returned", ourHM, hm);
        assertTrue("toString method returns something descriptive", replication.toString().startsWith("OTSReplication"));
    }

    /**
     * OTS model for testing.
     */
    static class OTSModel extends AbstractOTSModel
    {
        /** ... */
        private static final long serialVersionUID = 1L;

        /**
         * Construct the instrumented OTSModel.
         * @param simulator the simulator
         * @param shortName the short name of the model
         * @param description the description of the model
         */
        OTSModel(final OTSSimulatorInterface simulator, final String shortName, final String description)
        {
            super(simulator, shortName, description);
        }

        /**
         * Construct the instrumented OTSModel.
         * @param simulator the simulator
         */
        OTSModel(final OTSSimulatorInterface simulator)
        {
            super(simulator);
        }

        @Override
        public OTSNetwork getNetwork()
        {
            return null;
        }

        @Override
        public void constructModel() throws SimRuntimeException
        {
            // Do nothing
        }

        @Override
        public Serializable getSourceId()
        {
            return "sourceID";
        }

    }

}
