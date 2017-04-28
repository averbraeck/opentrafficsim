package org.opentrafficsim.road.network.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.sensor.AbstractSensor;
import org.opentrafficsim.simulationengine.SimpleAnimator;
import org.xml.sax.SAXException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.Throw;
import nl.tudelft.simulation.language.io.URLResource;

/**
 * Test of the XML Parser.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-15 12:52:42 +0200 (Wed, 15 Jul 2015) $, @version $Revision: 1113 $, by $Author: pknoppers $,
 * initial version Jul 17, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class XMLNetworkSensorTest implements UNITS
{
    /** AssertionError thrown by the sensor trigger. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected AssertionError triggerError = null;

    /**
     * Test an XML model with several sensors at different speeds of the RealTimeSimulator.
     */
    // Test
    public final void testXMLNetworkSensors()
    {
        for (double speedFactor : new double[] { 10, 100, 1000 })
        {
            try
            {
                TestXMLModel model = new TestXMLModel();
                final SimpleAnimator simulator = new SimpleAnimator(Time.ZERO, Duration.ZERO,
                        new Duration(120.0, SECOND), model);

                // get the nodes in the network.
                Node n1 = model.getNetwork().getNodeMap().get("N1");
                assertNotNull(n1);
                Node n2 = model.getNetwork().getNodeMap().get("N2");
                assertNotNull(n2);
                Node n3 = model.getNetwork().getNodeMap().get("N3");
                assertNotNull(n3);

                Link l12 = model.getNetwork().getLink(n1, n2);
                assertNotNull(l12);
                Link l23 = model.getNetwork().getLink(n2, n3);
                assertNotNull(l23);

                // get the lanes in the network
                Map<String, Lane> laneMap = new HashMap<>();
                for (Link link : model.getNetwork().getLinkMap().values())
                {
                    int nr = 1;
                    CrossSectionLink csl = (CrossSectionLink) link;
                    for (CrossSectionElement cse : csl.getCrossSectionElementList())
                    {
                        if (cse instanceof Lane)
                        {
                            laneMap.put(link.getId() + "." + nr, (Lane) cse);
                            nr++;
                        }
                    }
                }
                Lane lane12 = laneMap.get("N1-N2.1");
                assertNotNull(lane12);
                Lane lane23 = laneMap.get("N2-N3.1");
                assertNotNull(lane23);

                // add the sensors
                new ReportingSensor("12.E.F", lane12, new Length(lane12.getLength().getSI() - 1E-4, LengthUnit.SI),
                        RelativePosition.FRONT, simulator);
                new ReportingSensor("12.E.R", lane12, new Length(lane12.getLength().getSI() - 1E-4, LengthUnit.SI),
                        RelativePosition.REAR, simulator);
                new ReportingSensor("23.B.F", lane23, new Length(Math.ulp(0.0), LengthUnit.SI), RelativePosition.FRONT,
                        simulator);
                new ReportingSensor("23.B.R", lane23, new Length(Math.ulp(0.0), LengthUnit.SI), RelativePosition.REAR,
                        simulator);

                simulator.setSpeedFactor(speedFactor);
                simulator.start();

                // wait till the simulation is over
                while (simulator.isRunning())
                {
                    try
                    {
                        Thread.sleep(1, 0);
                        if (this.triggerError != null)
                        {
                            throw this.triggerError;
                        }
                        /*-
                        Set<LaneBasedGTU> gtus = new HashSet<>();
                        gtus.addAll(lane12.getGtuList());
                        gtus.addAll(lane23.getGtuList());
                        Assert.assertTrue("More than one GTU in the model: " + gtus.size(), gtus.size() <= 1);
                        for (LaneBasedGTU gtu : gtus)
                        {
                            Assert.assertEquals("Speed of GTU " + gtu + "<> 10 m/s: " + gtu.getSpeed(), 10.0, gtu
                                .getSpeed().getSI(), 0.00001);
                            gtu.getLocation();
                            gtu.positions(gtu.getFront());
                            gtu.positions(gtu.getRear());
                        }
                         */
                    }
                    catch (InterruptedException ie)
                    {
                        // ignore
                    }
                }
            }
            catch (SimRuntimeException | NamingException | NetworkException | PropertyException exception)
            {
                fail(exception.toString());
            }
        }
    }

    /**
     * Reporting sensor.
     */
    private class ReportingSensor extends AbstractSensor
    {
        /** */
        private static final long serialVersionUID = 1L;

        /** The sensor id. */
        private final String id;

        /** The simulator. */
        private final OTSDEVSSimulatorInterface simulator;

        /**
         * @param lane the lane for the ReportingSensor
         * @param longitudinalPosition the position on the lane
         * @param positionType the type of trigger (REAR, FRONT, etc.)
         * @param id the sensor id
         * @param simulator the simulator
         * @throws NetworkException when the position on the lane is out of bounds
         */
        public ReportingSensor(final String id, final Lane lane, final Length longitudinalPosition, final TYPE positionType,
                final OTSDEVSSimulatorInterface simulator) throws NetworkException
        {
            super("REPORT@" + lane.toString(), lane, longitudinalPosition, positionType, simulator);
            this.id = id;
            this.simulator = simulator;
        }

        /** {@inheritDoc} */
        @Override
        protected void triggerResponse(final LaneBasedGTU gtu)
        {
            try
            {
                int gtuNumber = Integer.parseInt(gtu.getId().toString().substring(gtu.getId().toString().indexOf(':') + 1)) - 1;
                double simTimeSec = this.simulator.getSimulatorTime().getTime().doubleValue();
                if ("12.E.F".equals(this.id))
                {
                    // first lane, end, front of GTU
                    // gen at t=0 at 50 m with back, front needs to travel 45 m = 4.5 sec
                    // next vehicle is 20 seconds later
                    assertEquals(20.0 * gtuNumber + 4.5, simTimeSec, 0.0001);
                }
                if ("12.E.R".equals(this.id))
                {
                    // first lane, end, rear of GTU
                    // gen at t=0 at 50 m with back, needs to travel 50 m = 5 sec
                    // next vehicle is 20 seconds later
                    assertEquals(20.0 * gtuNumber + 5.0, simTimeSec, 0.0001);
                }
                if ("23.B.F".equals(this.id))
                {
                    // second lane, start, front of GTU
                    // gen at t=0 at 50 m with back, front needs to travel 45 m = 4.5 sec
                    // next vehicle is 20 seconds later
                    assertEquals(20.0 * gtuNumber + 4.5, simTimeSec, 0.0001);
                }
                if ("23.B.R".equals(this.id))
                {
                    // second lane, end, rear of GTU
                    // gen at t=0 at 50 m with back, needs to travel 50 m = 5 sec
                    // next vehicle is 20 seconds later
                    assertEquals(20.0 * gtuNumber + 5.0, simTimeSec, 0.0001);
                }
            }
            catch (AssertionError ae)
            {
                XMLNetworkSensorTest.this.triggerError = ae;
            }
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "ReportingSensor [id=" + this.id + "]";
        }

        /** {@inheritDoc} */
        @Override
        @SuppressWarnings("checkstyle:designforextension")
        public ReportingSensor clone(final CrossSectionElement newCSE, final OTSSimulatorInterface newSimulator,
                final boolean animation) throws NetworkException
        {
            Throw.when(!(newCSE instanceof Lane), NetworkException.class, "sensors can only be cloned for Lanes");
            Throw.when(!(newSimulator instanceof OTSDEVSSimulatorInterface), NetworkException.class,
                    "simulator should be a DEVSSimulator");
            return new ReportingSensor(getId(), (Lane) newCSE, getLongitudinalPosition(), getPositionType(),
                    (OTSDEVSSimulatorInterface) newSimulator);
        }

    }

    /**
     * Model to test the XML parser.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim
     * License</a>.
     * <p>
     * $LastChangedDate: 2015-07-15 12:52:42 +0200 (Wed, 15 Jul 2015) $, @version $Revision: 1113 $, by $Author: pknoppers $,
     * initial version un 27, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class TestXMLModel implements OTSModelInterface
    {
        /** */
        private static final long serialVersionUID = 20150717L;

        /** The simulator. */
        private OTSDEVSSimulatorInterface simulator;

        /** The generated network. */
        private OTSNetwork network;

        /** */
        public TestXMLModel()
        {
            super();
        }

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        public final void constructModel(final SimulatorInterface<Time, Duration, OTSSimTimeDouble> pSimulator)
                throws SimRuntimeException
        {
            this.simulator = (OTSDEVSSimulatorInterface) pSimulator;
            URL url = URLResource.getResource("/org/opentrafficsim/core/network/factory/sensor-test.xml");
            XmlNetworkLaneParser nlp = new XmlNetworkLaneParser(this.simulator);
            try
            {
                this.network = nlp.build(url);
            }
            catch (NetworkException | ParserConfigurationException | SAXException | IOException | NamingException | GTUException
                    | OTSGeometryException exception)
            {
                exception.printStackTrace();
                System.exit(-1);
            }
        }

        /** {@inheritDoc} */
        @Override
        public SimulatorInterface<Time, Duration, OTSSimTimeDouble> getSimulator()

        {
            return this.simulator;
        }

        /**
         * @return network.
         */
        public final OTSNetwork getNetwork()
        {
            return this.network;
        }

    }
}
