package org.opentrafficsim.road.network.factory.xml.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.perception.HistoryManagerDevs;
import org.opentrafficsim.road.network.RoadNetwork;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * ParserTest tests the XML parser.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class ParserTest
{

    /** */
    private ParserTest()
    {
        // do not instantiate test class
    }

    /**
     * test the XML parser.
     * @throws NamingException on error
     * @throws SimRuntimeException on error
     */
    @Test
    public void testParser() throws SimRuntimeException, NamingException
    {
        OtsSimulator simulator = new OtsSimulator("Test");
        final TestModel testModel = new TestModel(simulator);
        simulator.initialize(Time.ZERO, Duration.ZERO, Duration.ofSI(3600.0), testModel,
                HistoryManagerDevs.noHistory(simulator));

        // test node
        RoadNetwork nw = testModel.getNetwork();
        assertEquals(300.0, nw.getNode("BLE").get().getPoint().x, 0.0001);
        assertEquals(0.0, nw.getNode("BLE").get().getPoint().y, 0.0001);

        // test link
        Link wwc = nw.getLink("WWC").get();
        assertEquals("URBAN", wwc.getType().getId());
        assertEquals("BLW", wwc.getStartNode().getId());
        assertEquals("BLWC", wwc.getEndNode().getId());
        assertEquals(300.0 - 18.8, wwc.getLength().si, 0.001);
    }

    /**
     * The simulation model.
     */
    public static class TestModel extends AbstractOtsModel
    {
        /** The network. */
        private RoadNetwork network;

        /**
         * Constructor.
         * @param simulator the simulator for this model
         */
        public TestModel(final OtsSimulatorInterface simulator)
        {
            super(simulator);
        }

        @Override
        public void constructModel() throws SimRuntimeException
        {
            try
            {
                this.network = new RoadNetwork("Test", getSimulator());
                new XmlParser(this.network).setResource("/resources/test-network.xml").build();
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }

        @Override
        public RoadNetwork getNetwork()
        {
            return this.network;
        }
    }

}
