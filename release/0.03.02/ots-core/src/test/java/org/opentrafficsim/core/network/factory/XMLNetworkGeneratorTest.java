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

import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.junit.Assert;
import org.opentrafficsim.core.OTS_SCALAR;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.core.network.lane.AbstractSensor;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.simulationengine.SimpleAnimator;
import org.xml.sax.SAXException;

/**
 * Test of the XML Parser.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-15 12:52:42 +0200 (Wed, 15 Jul 2015) $, @version $Revision: 1113 $, by $Author: pknoppers $,
 * initial version Jul 17, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class XMLNetworkGeneratorTest implements OTS_SCALAR
{
    /** AssertionError thrown by the sensor trigger. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected AssertionError triggerError = null;

    /**
     * Test an XML model that generates several GTUs quickly after another.
     */
    // Test
    public final void testXMLNetworkSensors()
    {
        try
        {
            TestXMLModel model = new TestXMLModel();
            final SimpleAnimator simulator =
                new SimpleAnimator(new Time.Abs(0.0, SECOND), new Time.Rel(0.0, SECOND), new Time.Rel(120.0, SECOND), model);

            // get nodes, links, and the lanes.
            Node n1 = model.getNetwork().getNodeMap().get("N1");
            Node n2 = model.getNetwork().getNodeMap().get("N2");
            assertNotNull(n1);
            Assert.assertTrue(n1.getLinksOut() != null);
            Assert.assertTrue(n1.getLinksOut().size() > 0);
            Link l12 = n1.getLinksOut().iterator().next();
            assertNotNull(l12);
            CrossSectionLink csl12 = (CrossSectionLink) l12;
            Assert.assertTrue(csl12.getCrossSectionElementList().size() > 0);
            Lane lane12 = (Lane) csl12.getCrossSectionElementList().get(0);
            assertNotNull(lane12);

            assertNotNull(n2);
            Assert.assertTrue(n2.getLinksOut() != null);
            Assert.assertTrue(n2.getLinksOut().size() > 0);
            Link l23 = n2.getLinksOut().iterator().next();
            assertNotNull(l23);
            CrossSectionLink csl23 = (CrossSectionLink) l23;
            Assert.assertTrue(csl23.getCrossSectionElementList().size() > 0);
            Lane lane23 = (Lane) csl23.getCrossSectionElementList().get(0);
            assertNotNull(lane23);

            // add a sensor to check the time the vehicles pass
            lane23.addSensor(new ReportingSensor(lane23, new Length.Rel(1E-4, LengthUnit.SI), RelativePosition.REFERENCE,
                "LANE23.START", simulator), GTUType.ALL);

            simulator.setSpeedFactor(1000);
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
                    Set<LaneBasedGTU> gtus = new HashSet<>();
                    gtus.addAll(lane12.getGtuList());
                    gtus.addAll(lane23.getGtuList());
                    for (LaneBasedGTU gtu : gtus)
                    {
                        // check that all vehicles drive 10 m/s and never have to break
                        boolean not10ms = Math.abs(gtu.getVelocity().getSI() - 10.0) > 0.0001;
                        if (not10ms)
                        {
                            // TODO repair headway in such a way that vehicle does not have to break (safe distance)
                            System.err.println("Velocity of GTU " + gtu + "<> 10 m/s: " + gtu.getVelocity() + ", headway = "
                                + gtu.headway(new Length.Rel(250.0, METER)));
                            // fail("Velocity of GTU " + gtu + "<> 10 m/s: " + gtu.getVelocity() + ", headway = "
                            // + gtu.headway(new Length.Rel(250.0, METER)));
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
        public ReportingSensor(final Lane lane, final Length.Rel longitudinalPosition, final TYPE positionType,
            final String id, final OTSDEVSSimulatorInterface simulator)
        {
            super(lane, longitudinalPosition, positionType, "REPORT@" + lane.toString(), simulator);
            this.id = id;
            this.simulator = simulator;
        }

        /** {@inheritDoc} */
        @Override
        public void trigger(final LaneBasedGTU gtu) throws RemoteException
        {
            try
            {
                int gtuNumber =
                    Integer.parseInt(gtu.getId().toString().substring(gtu.getId().toString().indexOf(':') + 1)) - 1;
                double simTimeSec = this.simulator.getSimulatorTime().getTime().doubleValue();
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
        private OTSNetwork network;

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
            XmlNetworkLaneParser nlp = new XmlNetworkLaneParser(this.simulator);
            try
            {
                this.network = nlp.build(url);
            }
            catch (NetworkException | ParserConfigurationException | SAXException | IOException | NamingException
                | GTUException | OTSGeometryException exception)
            {
                exception.printStackTrace();
            }
        }

        /** {@inheritDoc} */
        @Override
        public SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()
            throws RemoteException
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
