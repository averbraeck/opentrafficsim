package org.opentrafficsim.core.network.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.io.URLResource;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.core.network.lane.AbstractSensor;
import org.opentrafficsim.core.network.lane.CrossSectionElement;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.simulationengine.SimpleAnimator;
import org.xml.sax.SAXException;

/**
 * Test of the XML Parser.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-15 12:52:42 +0200 (Wed, 15 Jul 2015) $, @version $Revision: 1113 $, by $Author: pknoppers $,
 * initial version Jul 17, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class XMLNetworkSensorTest
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
        for (double speedFactor : new double[]{10, 100, 1000})
        {
            try
            {
                TestXMLModel model = new TestXMLModel();
                final SimpleAnimator simulator =
                    new SimpleAnimator(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(
                        0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(120.0, TimeUnit.SECOND), model);

                // get the nodes in the network.
                Node<String> n1 = model.getNetwork().getNodeMap().get("N1");
                assertNotNull(n1);
                Node<String> n2 = model.getNetwork().getNodeMap().get("N2");
                assertNotNull(n2);
                Node<String> n3 = model.getNetwork().getNodeMap().get("N3");
                assertNotNull(n3);

                Link<?, String> l12 = model.getNetwork().getLink(n1, n2);
                assertNotNull(l12);
                Link<?, String> l23 = model.getNetwork().getLink(n2, n3);
                assertNotNull(l23);

                // get the lanes in the network
                Map<String, Lane<String, String>> laneMap = new HashMap<>();
                for (Link<?, String> link : model.getNetwork().getLinkMap().values())
                {
                    int nr = 1;
                    CrossSectionLink<?, String> csl = (CrossSectionLink<?, String>) link;
                    for (CrossSectionElement<?, String> cse : csl.getCrossSectionElementList())
                    {
                        if (cse instanceof Lane)
                        {
                            laneMap.put(link.getId() + "." + nr, (Lane<String, String>) cse);
                            nr++;
                        }
                    }
                }
                Lane<String, String> lane12 = laneMap.get("N1-N2.1");
                assertNotNull(lane12);
                Lane<String, String> lane23 = laneMap.get("N2-N3.1");
                assertNotNull(lane23);

                // add the sensors
                lane12.addSensor(new ReportingSensor(lane12, new DoubleScalar.Rel<LengthUnit>(
                    lane12.getLength().getSI() - 1E-4, LengthUnit.SI), RelativePosition.FRONT, "12.E.F", simulator));
                lane12.addSensor(new ReportingSensor(lane12, new DoubleScalar.Rel<LengthUnit>(
                    lane12.getLength().getSI() - 1E-4, LengthUnit.SI), RelativePosition.REAR, "12.E.R", simulator));
                lane23.addSensor(new ReportingSensor(lane23, new DoubleScalar.Rel<LengthUnit>(Math.ulp(0.0), LengthUnit.SI),
                    RelativePosition.FRONT, "23.B.F", simulator));
                lane23.addSensor(new ReportingSensor(lane23, new DoubleScalar.Rel<LengthUnit>(Math.ulp(0.0), LengthUnit.SI),
                    RelativePosition.REAR, "23.B.R", simulator));

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
                        Set<LaneBasedGTU<?>> gtus = new HashSet<>();
                        gtus.addAll(lane12.getGtuList());
                        gtus.addAll(lane23.getGtuList());
                        Assert.assertTrue("More than one GTU in the model: " + gtus.size(), gtus.size() <= 1);
                        for (LaneBasedGTU<?> gtu : gtus)
                        {
                            Assert.assertEquals("Velocity of GTU " + gtu + "<> 10 m/s: " + gtu.getVelocity(), 10.0, gtu
                                .getVelocity().getSI(), 0.00001);
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
            catch (RemoteException | SimRuntimeException | NamingException | NetworkException exception)
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

        /** the sensor id. */
        private final String id;

        /** the simulator. */
        private final OTSDEVSSimulatorInterface simulator;

        /**
         * @param lane the lane for the ReportingSensor
         * @param longitudinalPosition the position on the lane
         * @param positionType the type of trigger (REAR, FRONT, etc.)
         * @param id the sensor id
         * @param simulator the simulator
         */
        public ReportingSensor(final Lane<String, String> lane, final DoubleScalar.Rel<LengthUnit> longitudinalPosition,
            final TYPE positionType, final String id, final OTSDEVSSimulatorInterface simulator)
        {
            super(lane, longitudinalPosition, positionType, "REPORT@" + lane.toString(), simulator);
            this.id = id;
            this.simulator = simulator;
        }

        /** {@inheritDoc} */
        @Override
        public void trigger(final LaneBasedGTU<?> gtu) throws RemoteException
        {
            try
            {
                int gtuNumber =
                    Integer.parseInt(gtu.getId().toString().substring(gtu.getId().toString().indexOf(':') + 1)) - 1;
                double simTimeSec = this.simulator.getSimulatorTime().get().doubleValue();
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
    }

    /**
     * Model to test the XML parser.
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
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

        /** the simulator. */
        private OTSDEVSSimulatorInterface simulator;

        /** the generated network. */
        private OTSNetwork<String, String, String> network;

        /** */
        public TestXMLModel()
        {
            super();
        }

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        public final void constructModel(
            final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> pSimulator)
            throws SimRuntimeException, RemoteException
        {
            this.simulator = (OTSDEVSSimulatorInterface) pSimulator;
            URL url = URLResource.getResource("/org/opentrafficsim/core/network/factory/sensor-test.xml");
            XmlNetworkLaneParser nlp = new XmlNetworkLaneParser(this.simulator);
            try
            {
                this.network = nlp.build(url);
            }
            catch (NetworkException | ParserConfigurationException | SAXException | IOException | NamingException
                | GTUException | OTSGeometryException exception)
            {
                exception.printStackTrace();
                System.exit(-1);
            }
        }

        /** {@inheritDoc} */
        @Override
        public SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> getSimulator() throws RemoteException
        {
            return this.simulator;
        }

        /**
         * @return network.
         */
        public final OTSNetwork<String, String, String> getNetwork()
        {
            return this.network;
        }

    }
}