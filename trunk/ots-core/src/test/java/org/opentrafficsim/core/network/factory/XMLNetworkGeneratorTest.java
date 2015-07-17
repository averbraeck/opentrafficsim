package org.opentrafficsim.core.network.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.io.URLResource;

import org.junit.Assert;
import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.gtu.animation.IDGTUColorer;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.geotools.LinkGeotools;
import org.opentrafficsim.core.network.geotools.NodeGeotools;
import org.opentrafficsim.core.network.lane.AbstractSensor;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.simulationengine.SimpleAnimator;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Test of the XML Parser.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-15 12:52:42 +0200 (Wed, 15 Jul 2015) $, @version $Revision: 1113 $, by $Author: pknoppers $,
 * initial version Jul 17, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class XMLNetworkGeneratorTest
{
    /** AssertionError thrown by the sensor trigger. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected AssertionError triggerError = null;

    /**
     * Test an XML model that generates several GTUs quickly after another.
     */
    @Test
    public final void testXMLNetworkSensors()
    {
        try
        {
            TestXMLModel model = new TestXMLModel();
            final SimpleAnimator simulator =
                new SimpleAnimator(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(0.0,
                    TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(120.0, TimeUnit.SECOND), model);

            // get nodes, links, and the lanes.
            NodeGeotools<String> n1 = null;
            NodeGeotools<String> n2 = null;
            Set<NodeGeotools<String>> nodeSet = model.getNetwork().getNodeSet();
            for (NodeGeotools<String> node : nodeSet)
            {
                if ("N1".equals(node.getId()))
                {
                    n1 = node;
                }
                if ("N2".equals(node.getId()))
                {
                    n2 = node;
                }
            }

            assertNotNull(n1);
            Assert.assertTrue(n1.getLinksOut() != null);
            Assert.assertTrue(n1.getLinksOut().size() > 0);
            @SuppressWarnings("unchecked")
            LinkGeotools<String, String> l12 = (LinkGeotools<String, String>) n1.getLinksOut().iterator().next();
            assertNotNull(l12);
            CrossSectionLink<String, String> csl12 = (CrossSectionLink<String, String>) l12;
            Assert.assertTrue(csl12.getCrossSectionElementList().size() > 0);
            Lane lane12 = (Lane) csl12.getCrossSectionElementList().get(0);
            assertNotNull(lane12);

            assertNotNull(n2);
            Assert.assertTrue(n2.getLinksOut() != null);
            Assert.assertTrue(n2.getLinksOut().size() > 0);
            @SuppressWarnings("unchecked")
            LinkGeotools<String, String> l23 = (LinkGeotools<String, String>) n2.getLinksOut().iterator().next();
            assertNotNull(l23);
            CrossSectionLink<String, String> csl23 = (CrossSectionLink<String, String>) l23;
            Assert.assertTrue(csl23.getCrossSectionElementList().size() > 0);
            Lane lane23 = (Lane) csl23.getCrossSectionElementList().get(0);
            assertNotNull(lane23);

            // add a sensor to check the time the vehicles pass
            lane23.addSensor(new ReportingSensor(lane23, new DoubleScalar.Rel<LengthUnit>(1E-4, LengthUnit.SI),
                RelativePosition.REFERENCE, "LANE23.START", simulator));

            simulator.setSpeedFactor(1000);
            simulator.start();

            // wait till the simulation is over
            while (simulator.isRunning())
            {
                try
                {
                    Thread.sleep(0, 1);
                    if (this.triggerError != null)
                    {
                        throw this.triggerError;
                    }
                    Set<LaneBasedGTU<?>> gtus = new HashSet<>();
                    gtus.addAll(lane12.getGtuList());
                    gtus.addAll(lane23.getGtuList());
                    for (LaneBasedGTU<?> gtu : gtus)
                    {
                        // check that all vehicles drive 10 m/s and never have to break
                        boolean not10ms = Math.abs(gtu.getVelocity().getSI() - 10.0) > 0.0001;
                        if (not10ms)
                        {
                            fail("Velocity of GTU " + gtu + "<> 10 m/s: " + gtu.getVelocity() + ", headway = "
                                + gtu.headway(new DoubleScalar.Rel<LengthUnit>(250.0, LengthUnit.METER)));
                        }
                    }
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

        /** last time car passed. */
        private double lastSimTimeCheck = 0.0;

        /**
         * @param lane the lane for the ReportingSensor
         * @param longitudinalPosition the position on the lane
         * @param positionType the type of trigger (REAR, FRONT, etc.)
         * @param id the sensor id
         * @param simulator the simulator
         */
        public ReportingSensor(final Lane lane, final DoubleScalar.Rel<LengthUnit> longitudinalPosition,
            final TYPE positionType, final String id, final OTSDEVSSimulatorInterface simulator)
        {
            super(lane, longitudinalPosition, positionType);
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
                if ("LANE23.START".equals(this.id))
                {
                    // second lane, start, reference point of the GTU
                    // gen at t=0 at 50 m with back, front needs to travel 50 m = 5 sec
                    // next vehicle has to be later -- more than the 1 sec interarrival time
                    if (gtuNumber == 0)
                    {
                        assertEquals(5.0, simTimeSec, 0.001);
                        this.lastSimTimeCheck = 5.0;
                    }
                    else
                    {
                        System.out.println("IAT for " + gtu + " is " + (simTimeSec - this.lastSimTimeCheck) + " s");
                        Assert.assertTrue("IAT for " + gtu + " is " + (simTimeSec - this.lastSimTimeCheck) + " s",
                            simTimeSec - this.lastSimTimeCheck > 1.0);
                        this.lastSimTimeCheck = simTimeSec;
                    }
                }
            }
            catch (AssertionError ae)
            {
                XMLNetworkGeneratorTest.this.triggerError = ae;
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
     * All rights reserved. BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
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
        private Network<String, NodeGeotools<String>, LinkGeotools<String, String>> network;

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
            URL url = URLResource.getResource("/org/opentrafficsim/core/network/factory/gen-overlap-test.xml");
            XmlNetworkLaneParser nlp =
                new XmlNetworkLaneParser(String.class, NodeGeotools.class, String.class, Coordinate.class,
                    LinkGeotools.class, String.class, this.simulator, new IDGTUColorer());
            try
            {
                this.network = nlp.build(url);
            }
            catch (NetworkException | ParserConfigurationException | SAXException | IOException exception1)
            {
                exception1.printStackTrace();
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
        public final Network<String, NodeGeotools<String>, LinkGeotools<String, String>> getNetwork()
        {
            return this.network;
        }

    }
}
