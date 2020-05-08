package org.sim0mq.publisher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.naming.NamingException;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.event.EventInterface;
import org.djutils.event.EventListenerInterface;
import org.djutils.io.URLResource;
import org.junit.Test;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSReplication;
import org.opentrafficsim.core.dsol.OTSSimulator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.parser.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.lane.conflict.ConflictBuilder;
import org.opentrafficsim.road.network.lane.conflict.LaneCombinationList;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;
import nl.tudelft.simulation.dsol.model.outputstatistics.OutputStatistic;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Unit tests. This requires half of OTS in the imports because it sets up a simulation and runs that for a couple of seconds.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2020-02-13 11:08:16 +0100 (Thu, 13 Feb 2020) $, @version $Revision: 6383 $, by $Author: pknoppers $,
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class PublisherTest implements OTSModelInterface
{

    /** ... */
    private static final long serialVersionUID = 20200505L;

    /**
     * Test the Publisher class.
     * @throws RemoteException when that happens this test has failed
     * @throws NetworkException if that happens uncaught; this test has failed
     * @throws OTSGeometryException if that happens uncaught; this test has failed
     * @throws NamingException on context error
     * @throws SimRuntimeException on DSOL error
     */
    @Test
    public void testPublisher()
            throws RemoteException, NetworkException, OTSGeometryException, SimRuntimeException, NamingException
    {
        OTSSimulatorInterface simulator = new OTSSimulator("test simulator for PublisherTest");
        OTSRoadNetwork network = new OTSRoadNetwork("test network for PublisherTest", true, simulator);
        Publisher publisher = new Publisher(network);
        assertTrue("id of publisher contains id of network", publisher.getId().contains(network.getId()));
        Object[] transceiverNames = publisher.getIdSource(0).get(null);
        assertNotNull("result of getIdSource should not be null", transceiverNames);
        assertTrue("result of getIdSource should not be empty", transceiverNames.length > 0);
        for (Object o : transceiverNames)
        {
            assertTrue("transceiver name is a String", o instanceof String);
            // System.out.println("transceiver: " + o);
        }
        // See if we can obtain the GTUIdTransceiver
        Object[] gtuIdTransceiver = publisher.get(new Object[] { "GTU id transceiver" });
        assertNotNull("result of get should not be null", gtuIdTransceiver);
        assertEquals("result should contain one element", 1, gtuIdTransceiver.length);
        assertTrue("result should contain a TransceiverInterface", gtuIdTransceiver[0] instanceof GTUIdTransceiver);
        // See if we can obtain the GTUTransceiver
        Object[] gtuTransceiver = publisher.get(new Object[] { "GTU transceiver" });
        assertNotNull("result of get should not be null", gtuTransceiver);
        assertEquals("result should contain one element", 1, gtuTransceiver.length);
        assertTrue("result should contain a TransceiverInterface", gtuTransceiver[0] instanceof GTUTransceiver);
        assertNull("request for non existent transceiver should return null",
                publisher.get(new Object[] { "No such transceiver" }));
        try
        {
            publisher.getIdSource(1);
            fail("should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            // Ignore expected exception
        }

        try
        {
            publisher.getIdSource(-1);
            fail("should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            // Ignore expected exception
        }

        Object[] result = publisher.getIdSource(0).get(null);
        for (Object o : result)
        {
            System.out.println(o + ": " + o.getClass().getName());
            Object[] t = publisher.get(new Object[] { o });
            for (Object o2 : t)
            {
                TransceiverInterface ti = (TransceiverInterface) o2;
                System.out.println("\t" + ti.getId());
            }
        }

        LinkTransceiver lt = (LinkTransceiver) publisher.get(new Object[] { "Link transceiver" })[0];
        TransceiverInterface lit = lt.getIdSource(0);
        assertNotNull("got the link id transceiver", lit);
        // Network has 0 links
        result = lit.get(new Object[0]);
        assertEquals("there are 0 links", 0, result.length);

        simulator.initialize(
                OTSReplication.create("rep1", Time.ZERO, Duration.ZERO, new Duration(1800.0, DurationUnit.SECOND), this),
                ReplicationMode.TERMINATING);
        LinkType linkType = network.getLinkType(LinkType.DEFAULTS.ROAD);
        OTSPoint3D nodeAPoint = new OTSPoint3D(10, 100, 1000);
        OTSRoadNode nodeA = new OTSRoadNode(network, "NodeA", nodeAPoint, new Direction(0.1, DirectionUnit.EAST_RADIAN));
        OTSPoint3D nodeBPoint = new OTSPoint3D(20, 200, 2000);
        OTSRoadNode nodeB = new OTSRoadNode(network, "NodeB", nodeBPoint, new Direction(0.2, DirectionUnit.EAST_RADIAN));
        CrossSectionLink link = new CrossSectionLink(network, "Id of test link", nodeA, nodeB, linkType,
                new OTSLine3D(nodeAPoint, nodeBPoint), null);
        LaneType laneType = network.getLaneType(LaneType.DEFAULTS.RURAL_ROAD_LANE);

        new Lane(link, "LaneId", new Length(2.0, LengthUnit.METER), new Length(3.0, LengthUnit.METER), laneType,
                new Speed(50, SpeedUnit.KM_PER_HOUR));

        result = lit.get(new Object[0]);
        assertEquals("there is 1 link", 1, result.length);
        assertEquals("returned link is our link", link.getId(), result[0]);
        result = lt.get(new Object[] { link.getId() });
        assertEquals("link data refers to our link", link.getId(), result[0]);
        assertEquals("link type id", linkType.getId(), result[1]);
        assertEquals("start node id", nodeA.getId(), result[2]);
        assertEquals("end node id", nodeB.getId(), result[3]);
        assertEquals("design line has 2 points", 2, result[4]);
        assertEquals("cross element count is 0", 0, result[5]);

    }

    @Override
    public void constructModel() throws SimRuntimeException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public OTSSimulatorInterface getSimulator()
    {
        return null;
    }

    @Override
    public InputParameterMap getInputParameterMap()
    {
        return null;
    }

    @Override
    public List<OutputStatistic<?>> getOutputStatistics()
    {
        return null;
    }

    @Override
    public OTSNetwork getNetwork()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getShortName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDescription()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Open an URL, read it and store the contents in a string. Adapted from
     * https://stackoverflow.com/questions/4328711/read-url-to-string-in-few-lines-of-java-code
     * @param url URL; the URL
     * @return String
     * @throws IOException when reading the file fails
     */
    public static String readStringFromURL(final URL url) throws IOException
    {
        try (Scanner scanner = new Scanner(url.openStream(), StandardCharsets.UTF_8.toString()))
        {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    /**
     * Construct an OTS simulation experiment from an XML description.
     * @param xml String; the XML encoded network
     * @param simulationDuration Duration; total duration of the simulation
     * @param warmupTime Duration; warm up time of the simulation
     * @param seed Long; seed for the experiment
     * @return OTSSimulator; the simulator
     * @throws NamingException on context error
     * @throws SimRuntimeException on DSOL error
     */
    private OTSSimulator loadNetwork(final String xml, final Duration simulationDuration, final Duration warmupTime,
            final Long seed) throws SimRuntimeException, NamingException
    {
        OTSSimulator simulator = new OTSSimulator("OTS Simulator");
        TestModel model = new TestModel(simulator, "Test model", "Test model for unit test", xml);
        Map<String, StreamInterface> map = new LinkedHashMap<>();
        map.put("generation", new MersenneTwister(seed));
        simulator.initialize(Time.ZERO, warmupTime, simulationDuration, model);
        return simulator;
    }

    /**
     * The Model.
     */
    class TestModel extends AbstractOTSModel implements EventListenerInterface
    {
        /** */
        private static final long serialVersionUID = 20170419L;

        /** The network. */
        private OTSRoadNetwork network;

        /** The XML. */
        private final String xml;

        /**
         * @param simulator OTSSimulatorInterface; the simulator
         * @param shortName String; the model name
         * @param description String; the model description
         * @param xml String; the XML description of the simulation model
         */
        TestModel(final OTSSimulatorInterface simulator, final String shortName, final String description, final String xml)
        {
            super(simulator, shortName, description);
            this.xml = xml;
        }

        /** {@inheritDoc} */
        @Override
        public void notify(final EventInterface event) throws RemoteException
        {
            System.err.println("Received event " + event);
        }

        /** {@inheritDoc} */
        @Override
        public void constructModel() throws SimRuntimeException
        {
            this.network = new OTSRoadNetwork(getShortName(), true, getSimulator());
            try
            {
                XmlNetworkLaneParser.build(new ByteArrayInputStream(this.xml.getBytes(StandardCharsets.UTF_8)), this.network,
                        false);
                LaneCombinationList ignoreList = new LaneCombinationList();
                LaneCombinationList permittedList = new LaneCombinationList();
                ConflictBuilder.buildConflictsParallel(this.network, this.network.getGtuType(GTUType.DEFAULTS.VEHICLE),
                        getSimulator(), new ConflictBuilder.FixedWidthGenerator(Length.instantiateSI(2.0)), ignoreList,
                        permittedList);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        /** {@inheritDoc} */
        @Override
        public OTSNetwork getNetwork()
        {
            return this.network;
        }

        /** {@inheritDoc} */
        @Override
        public Serializable getSourceId()
        {
            return "Sim0MQOTSModel";
        }

    }

    /**
     * Test that makes the network using the XML parser.
     * @throws IOException if that happens uncaught; this test has failed
     * @throws NamingException if that happens uncaught; this test has failed
     * @throws SimRuntimeException if that happens uncaught; this test has failed
     */
    @Test
    public void testUsingXMLParser() throws IOException, SimRuntimeException, NamingException
    {
        String networkFile = "/Temp.xml";
        URL url = URLResource.getResource(networkFile);
        // System.out.println("url is " + url);
        String xml = readStringFromURL(url);
        OTSSimulatorInterface simulation = loadNetwork(xml, new Duration(3600, DurationUnit.SECOND), Duration.ZERO, 123456L);
        OTSNetwork network = ((TestModel) simulation.getReplication().getExperiment().getModel()).getNetwork();
        System.out.println(network);
        Publisher publisher = new Publisher(network);
        Time stopTime = new Time(30, TimeUnit.BASE_SECOND);
        simulation.runUpTo(stopTime); // ensure there will be a few GTUs
        while (simulation.isRunning())
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        System.out.println("time is now " + simulation.getSimulatorTime());
        assertEquals("Simulation has stopped at stop time", stopTime, simulation.getSimulatorTime());
        LinkIdTransceiver lit = (LinkIdTransceiver) publisher.get(new Object[] { "Link id transceiver" })[0];
        LinkTransceiver lt = (LinkTransceiver) publisher.get(new Object[] { "Link transceiver" })[0];
        LinkGTUIdTransceiver lgit = (LinkGTUIdTransceiver) publisher.get(new Object[] { "Link GTU id transceiver" })[0];
        LaneGTUIdTransceiver lanegid = (LaneGTUIdTransceiver) publisher.get(new Object[] { "Lane GTU id transceiver" })[0];
        GTUTransceiver gt = (GTUTransceiver) publisher.get(new Object[] { "GTU transceiver" })[0];
        CrossSectionElementTransceiver cset =
                (CrossSectionElementTransceiver) publisher.get(new Object[] { "CrossSectionElement transceiver" })[0];
        Object[] linkIds = (Object[]) (lit.get(null));
        for (Object linkIdObject : linkIds)
        {
            String linkId = (String) linkIdObject;
            System.out.println("Link " + linkId);
            Object[] linkData = lt.get(new Object[] { linkId });
            assertNotNull("Link data should not be null", linkData);
            for (int i = 0; i < linkData.length; i++)
            {
                System.out.println("\t" + lt.getResultFields().getFieldName(i) + ": " + linkData[i]);
                if (5 == i)
                {
                    int gtuCount = (Integer) linkData[i];
                    Object[] gtuIds = lgit.get(new Object[] { linkId });
                    assertEquals("gtu count", gtuCount, gtuIds.length);
                    for (int j = 0; j < gtuCount; j++)
                    {
                        Object[] gtuData = gt.get(new Object[] { gtuIds[j] });
                        for (int k = 0; k < gtuData.length; k++)
                        {
                            System.out.println("\t" + j + "\t" + gt.getResultFields().getFieldName(k) + ": " + gtuData[k]);
                        }
                    }
                }
                if (6 == i)
                {
                    int cseCount = (Integer) linkData[i];
                    for (int j = 0; j < cseCount; j++)
                    {
                        Object[] cseData = cset.get(new Object[] { linkId, j });
                        for (int k = 0; k < cseData.length; k++)
                        {
                            System.out.println("\t" + j + "\t" + cset.getResultFields().getFieldName(k) + ": " + cseData[k]);
                            if (1 == k && "org.opentrafficsim.road.network.lane.Lane".equals(cseData[k]))
                            {
                                String laneId = (String) cseData[0];
                                Object[] gtuIdObjects = lanegid.get(new Object[] { linkId, laneId });
                                for (int l = 0; l < gtuIdObjects.length; l++)
                                {
                                    Object[] gtuData = gt.get(new Object[] { gtuIdObjects[l] });
                                    for (int m = 0; m < gtuData.length; m++)
                                    {
                                        System.out.println(
                                                "\t\t" + l + "\t" + gt.getResultFields().getFieldName(m) + ": " + gtuData[m]);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        assertNull("non existent link id returns null", cset.get(new Object[] { "Non existent link id", 0 }));
        assertNull("non existent link id returns null", lgit.get(new Object[] { "Non existent link id" }));
        assertNull("non existent link id returns null", lanegid.get(new Object[] { "Non existent link id", "FORWARD" }));
        assertNull("non existent link id returns null", lanegid.get(new Object[] { "NCEC", "FORWARD123" }));
        assertNull("non existent link id returns null", lt.get(new Object[] { "Non existent link id" }));
        assertNull("Non existent GTU id returns null", gt.get(new Object[] { "Non existent GTU" }));
        assertNull("Out of range index returns null", cset.get(new Object[] { "NCEC", -1 }));
        assertNull("Out of range index returns null", cset.get(new Object[] { "NCEC", 9999 }));

        assertTrue("CrossSectionElementTransceiver has descriptive toString",
                cset.toString().startsWith("CrossSectionElementTransceiver"));
        assertTrue("LinkGTUIdTransceiver has descriptive toString", lgit.toString().startsWith("LinkGTUIdTransceiver"));
        assertTrue("LaneGTUIdTransceiver has descriptive toString", lanegid.toString().startsWith("LaneGTUIdTransceiver"));
        assertTrue("LinkIdTransceiver has descriptive toString", lit.toString().startsWith("LinkIdTransceiver"));
        try
        {
            lt.getIdSource(1);
            fail("Should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            // Ignore expected exception
        }

        try
        {
            lt.getIdSource(-1);
            fail("Should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            // Ignore expected exception
        }

        assertTrue("LinkTransceiver has descriptive toString", lt.toString().startsWith("LinkTransceiver"));
        assertTrue("GTUTransceiver has descriptive toString", gt.toString().startsWith("GTUTransceiver"));
        GTUIdTransceiver git = (GTUIdTransceiver) publisher.get(new Object[] { "GTU id transceiver" })[0];
        assertEquals("gtu id transceiver can also be obtained from gtu transceiver", git, gt.getIdSource(0));
        assertTrue("GTUIdTransceiver has descriptive toString", git.toString().startsWith("GTUIdTransceiver"));
        Object[] gtuObjects = git.get(null);
        assertNotNull("GTU array should not be null", gtuObjects);
        // Check that each entry is a GTU
        for (Object o : gtuObjects)
        {
            String gtuId = (String) o;
            Object[] gtuData = gt.get(new Object[] { o });
            assertNotNull("GTU data should not be null", gtuData);
            assertEquals("first element of gtuData is GTU id", gtuId, gtuData[0]);
        }
        try
        {
            gt.getIdSource(1);
            fail("Should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            // Ignore expected exception
        }

        try
        {
            gt.getIdSource(-1);
            fail("Should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            // Ignore expected exception
        }

        NodeIdTransceiver nit = (NodeIdTransceiver) publisher.get(new Object[] { "Node id transceiver" })[0];
        assertNotNull("Node id transceiver was returned", nit);
        assertTrue("toString of NodeIdTransceiver returns something descriptive",
                nit.toString().startsWith("NodeIdTransceiver"));
        NodeTransceiver nt = (NodeTransceiver) publisher.get(new Object[] { "Node transceiver" })[0];
        assertNotNull("Node transceiver was returned", nt);
        assertTrue("toString of NodeTransceiver returns something descriptive", nt.toString().startsWith("NodeTransceiver"));
        assertEquals("nit should be retrievable from nt", nit, nt.getIdSource(0));
        try
        {
            nt.getIdSource(1);
            fail("Should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            // Ignore expected exception
        }

        try
        {
            nt.getIdSource(-1);
            fail("Should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            // Ignore expected exception
        }

        for (Object object : nit.get(null))
        {
            assertNotNull("Node ids should not be null", object);
            String nodeId = (String) object;
            System.out.println("Node " + nodeId);
            Object[] nodeData = nt.get(new Object[] { object });
            assertNotNull("nodeData should not be null", nodeData);
            for (int i = 0; i < nodeData.length; i++)
            {
                System.out.println("\t" + i + "\t" + nt.getResultFields().getFieldName(i) + "\t" + nodeData[i]);
            }

        }

        assertNull("non existent node returns null", nt.get(new Object[] { "Non existend node id" }));
    }

    /** The test network. */
    // @formatter:off
    static final String TEST_NETWORK_XML = "<?xml version='1.0' encoding='UTF-8'?>\r\n" 
            + "<OTS xmlns=\"http://www.opentrafficsim.org/ots\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" 
            + "  xsi:schemaLocation=\"http://www.opentrafficsim.org/ots "
            + "../../../../../ots-xsd/src/main/resources/xsd/1.03.00/ots.xsd\" "
            + "xmlns:xi=\"http://www.w3.org/2001/XInclude\">\r\n" 
            + "\r\n" 
            + "  <DEFINITIONS>\r\n" 
            + "    <GTUTYPES xmlns=\"http://www.opentrafficsim.org/ots\" "
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" 
            + "      xsi:schemaLocation=\"http://www.opentrafficsim.org/ots "
            + "../ots-definitions.xsd http://www.opentrafficsim.org/ots "
            + "https://opentrafficsim.org/docs/xsd/1.03.00/ots-definitions.xsd\">\r\n" 
            + "      <GTUTYPE ID=\"NONE\" DEFAULT=\"true\" />\r\n" 
            + "      <GTUTYPE ID=\"ROAD_USER\" DEFAULT=\"true\" />\r\n" 
            + "      <GTUTYPE ID=\"WATERWAY_USER\" DEFAULT=\"true\" />\r\n" 
            + "      <GTUTYPE ID=\"RAILWAY_USER\" DEFAULT=\"true\" />\r\n" 
            + "      <GTUTYPE ID=\"SHIP\" PARENT=\"WATERWAY_USER\" DEFAULT=\"true\" />\r\n" 
            + "      <GTUTYPE ID=\"TRAIN\" PARENT=\"RAILWAY_USER\" DEFAULT=\"true\" />\r\n" 
            + "      <GTUTYPE ID=\"PEDESTRIAN\" PARENT=\"ROAD_USER\" DEFAULT=\"true\" />\r\n" 
            + "      <GTUTYPE ID=\"BICYCLE\" PARENT=\"ROAD_USER\" DEFAULT=\"true\" />\r\n" 
            + "      <GTUTYPE ID=\"MOPED\" PARENT=\"BICYCLE\" DEFAULT=\"true\" />\r\n" 
            + "      <GTUTYPE ID=\"VEHICLE\" PARENT=\"ROAD_USER\" DEFAULT=\"true\" />\r\n" 
            + "      <GTUTYPE ID=\"EMERGENCY_VEHICLE\" PARENT=\"VEHICLE\" DEFAULT=\"true\" />\r\n" 
            + "      <GTUTYPE ID=\"CAR\" PARENT=\"VEHICLE\" DEFAULT=\"true\" />\r\n" 
            + "      <GTUTYPE ID=\"VAN\" PARENT=\"VEHICLE\" DEFAULT=\"true\" />\r\n" 
            + "      <GTUTYPE ID=\"BUS\" PARENT=\"VEHICLE\" DEFAULT=\"true\" />\r\n" 
            + "      <GTUTYPE ID=\"TRUCK\" PARENT=\"VEHICLE\" DEFAULT=\"true\" />\r\n" 
            + "      <GTUTYPE ID=\"SCHEDULED_BUS\" PARENT=\"BUS\" DEFAULT=\"true\" />\r\n" 
            + "    </GTUTYPES>\r\n" 
            + "\r\n" 
            + "    <LINKTYPES xmlns:ots=\"http://www.opentrafficsim.org/ots\" "
            + "xmlns=\"http://www.opentrafficsim.org/ots\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" 
            + "      xsi:schemaLocation=\"http://www.opentrafficsim.org/ots "
            + "../ots-definitions.xsd http://www.opentrafficsim.org/ots "
            + "https://opentrafficsim.org/docs/xsd/1.03.00/ots-definitions.xsd\">\r\n" 
            + "      <LINKTYPE ID=\"NONE\" DEFAULT=\"true\">\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"NONE\" DIRECTION=\"NONE\" />\r\n" 
            + "      </LINKTYPE>\r\n" 
            + "      <LINKTYPE ID=\"ROAD\" DEFAULT=\"true\">\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"ROAD_USER\" DIRECTION=\"FORWARD\" />\r\n" 
            + "      </LINKTYPE>\r\n" 
            + "      <LINKTYPE ID=\"FREEWAY\" PARENT=\"ROAD\" DEFAULT=\"true\">\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"ROAD_USER\" DIRECTION=\"FORWARD\" />\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"PEDESTRIAN\" DIRECTION=\"NONE\" />\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"BICYCLE\" DIRECTION=\"NONE\" />\r\n" 
            + "      </LINKTYPE>\r\n" 
            + "      <LINKTYPE ID=\"WATERWAY\" DEFAULT=\"true\">\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"WATERWAY_USER\" DIRECTION=\"BOTH\" />\r\n" 
            + "      </LINKTYPE>\r\n" 
            + "      <LINKTYPE ID=\"RAILWAY\" DEFAULT=\"true\">\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"RAILWAY_USER\" DIRECTION=\"FORWARD\" />\r\n" 
            + "      </LINKTYPE>\r\n" 
            + "      <LINKTYPE ID=\"CONNECTOR\" DEFAULT=\"true\">\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"ROAD_USER\" DIRECTION=\"FORWARD\" />\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"WATERWAY_USER\" DIRECTION=\"FORWARD\" />\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"RAILWAY_USER\" DIRECTION=\"FORWARD\" />\r\n" 
            + "      </LINKTYPE>\r\n" 
            + "    </LINKTYPES>\r\n" 
            + "\r\n" 
            + "    <LANETYPES xmlns=\"http://www.opentrafficsim.org/ots\" "
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
            + "xsi:schemaLocation=\"http://www.opentrafficsim.org/ots ../ots-definitions.xsd\">\r\n" 
            + "      <LANETYPE ID=\"NONE\" DEFAULT=\"true\">\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"NONE\" DIRECTION=\"NONE\" />\r\n" 
            + "      </LANETYPE>\r\n" 
            + "      <LANETYPE ID=\"TWO_WAY_LANE\" DEFAULT=\"true\">\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"ROAD_USER\" DIRECTION=\"FORWARD\" />\r\n" 
            + "      </LANETYPE>\r\n" 
            + "      <LANETYPE ID=\"RURAL_ROAD\" PARENT=\"TWO_WAY_LANE\" DEFAULT=\"true\">\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"ROAD_USER\" DIRECTION=\"FORWARD\" />\r\n" 
            + "      </LANETYPE>\r\n" 
            + "      <LANETYPE ID=\"URBAN_ROAD\" PARENT=\"TWO_WAY_LANE\" DEFAULT=\"true\">\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"ROAD_USER\" DIRECTION=\"FORWARD\" />\r\n" 
            + "      </LANETYPE>\r\n" 
            + "      <LANETYPE ID=\"RESIDENTIAL_ROAD\" PARENT=\"TWO_WAY_LANE\" DEFAULT=\"true\">\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"ROAD_USER\" DIRECTION=\"FORWARD\" />\r\n" 
            + "      </LANETYPE>\r\n" 
            + "      <LANETYPE ID=\"ONE_WAY_LANE\" DEFAULT=\"true\">\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"ROAD_USER\" DIRECTION=\"FORWARD\" />\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"PEDESTRIAN\" DIRECTION=\"BOTH\" />\r\n" 
            + "      </LANETYPE>\r\n" 
            + "      <LANETYPE ID=\"FREEWAY\" DEFAULT=\"true\">\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"ROAD_USER\" DIRECTION=\"FORWARD\" />\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"PEDESTRIAN\" DIRECTION=\"NONE\" />\r\n" 
            + "      </LANETYPE>\r\n" 
            + "      <LANETYPE ID=\"HIGHWAY\" DEFAULT=\"true\">\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"ROAD_USER\" DIRECTION=\"FORWARD\" />\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"PEDESTRIAN\" DIRECTION=\"NONE\" />\r\n" 
            + "      </LANETYPE>\r\n" 
            + "      <LANETYPE ID=\"BUS_LANE\" DEFAULT=\"true\">\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"ROAD_USER\" DIRECTION=\"NONE\" />\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"BUS\" DIRECTION=\"FORWARD\" />\r\n" 
            + "      </LANETYPE>\r\n" 
            + "      <LANETYPE ID=\"MOPED_PATH\" DEFAULT=\"true\">\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"ROAD_USER\" DIRECTION=\"NONE\" />\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"BICYCLE\" DIRECTION=\"FORWARD\" />\r\n" 
            + "        <!-- a MOPED is a special BICYCLE -->\r\n" 
            + "      </LANETYPE>\r\n" 
            + "      <LANETYPE ID=\"BICYCLE_PATH\" DEFAULT=\"true\">\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"ROAD_USER\" DIRECTION=\"NONE\" />\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"BICYCLE\" DIRECTION=\"FORWARD\" />\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"MOPED\" DIRECTION=\"NONE\" />\r\n" 
            + "      </LANETYPE>\r\n" 
            + "      <LANETYPE ID=\"FOOTPATH\" DEFAULT=\"true\">\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"ROAD_USER\" DIRECTION=\"NONE\" />\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"PEDESTRIAN\" DIRECTION=\"BOTH\" />\r\n" 
            + "      </LANETYPE>\r\n" 
            + "    </LANETYPES>\r\n" 
            + "\r\n" 
            + "    <GTUTEMPLATES xmlns=\"http://www.opentrafficsim.org/ots\" "
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" 
            + "      xsi:schemaLocation=\"http://www.opentrafficsim.org/ots "
            + "../ots-definitions.xsd http://www.opentrafficsim.org/ots "
            + "https://opentrafficsim.org/docs/xsd/1.03.00/ots-definitions.xsd\">\r\n" 
            + "      <GTUTEMPLATE ID=\"CAR\" GTUTYPE=\"CAR\" DEFAULT=\"true\">\r\n" 
            + "        <LENGTHDIST LENGTHUNIT=\"m\">\r\n" 
            + "          <CONSTANT C=\"4.19\" />\r\n" 
            + "        </LENGTHDIST>\r\n" 
            + "        <WIDTHDIST LENGTHUNIT=\"m\">\r\n" 
            + "          <CONSTANT C=\"1.7\" />\r\n" 
            + "        </WIDTHDIST>\r\n" 
            + "        <MAXSPEEDDIST SPEEDUNIT=\"km/h\">\r\n" 
            + "          <CONSTANT C=\"180\" />\r\n" 
            + "        </MAXSPEEDDIST>\r\n" 
            + "      </GTUTEMPLATE>\r\n" 
            + "      <GTUTEMPLATE ID=\"TRUCK\" GTUTYPE=\"TRUCK\" DEFAULT=\"true\">\r\n" 
            + "        <LENGTHDIST LENGTHUNIT=\"m\">\r\n" 
            + "          <CONSTANT C=\"12.0\" />\r\n" 
            + "        </LENGTHDIST>\r\n" 
            + "        <WIDTHDIST LENGTHUNIT=\"m\">\r\n" 
            + "          <CONSTANT C=\"2.55\" />\r\n" 
            + "        </WIDTHDIST>\r\n" 
            + "        <MAXSPEEDDIST SPEEDUNIT=\"km/h\">\r\n" 
            + "          <NORMAL MU=\"85.0\" SIGMA=\"2.5\" />\r\n" 
            + "        </MAXSPEEDDIST>\r\n" 
            + "      </GTUTEMPLATE>\r\n" 
            + "      <GTUTEMPLATE ID=\"BUS\" GTUTYPE=\"BUS\" DEFAULT=\"true\">\r\n" 
            + "        <LENGTHDIST LENGTHUNIT=\"m\">\r\n" 
            + "          <CONSTANT C=\"12.0\" />\r\n" 
            + "        </LENGTHDIST>\r\n" 
            + "        <WIDTHDIST LENGTHUNIT=\"m\">\r\n" 
            + "          <CONSTANT C=\"2.55\" />\r\n" 
            + "        </WIDTHDIST>\r\n" 
            + "        <MAXSPEEDDIST SPEEDUNIT=\"km/h\">\r\n" 
            + "          <CONSTANT C=\"90\" />\r\n" 
            + "        </MAXSPEEDDIST>\r\n" 
            + "      </GTUTEMPLATE>\r\n" 
            + "      <GTUTEMPLATE ID=\"VAN\" GTUTYPE=\"VAN\" DEFAULT=\"true\">\r\n" 
            + "        <LENGTHDIST LENGTHUNIT=\"m\">\r\n" 
            + "          <CONSTANT C=\"5.0\" />\r\n" 
            + "        </LENGTHDIST>\r\n" 
            + "        <WIDTHDIST LENGTHUNIT=\"m\">\r\n" 
            + "          <CONSTANT C=\"2.4\" />\r\n" 
            + "        </WIDTHDIST>\r\n" 
            + "        <MAXSPEEDDIST SPEEDUNIT=\"km/h\">\r\n" 
            + "          <CONSTANT C=\"180\" />\r\n" 
            + "        </MAXSPEEDDIST>\r\n" 
            + "      </GTUTEMPLATE>\r\n" 
            + "      <GTUTEMPLATE ID=\"EMERGENCY_VEHICLE\" GTUTYPE=\"EMERGENCY_VEHICLE\" DEFAULT=\"true\">\r\n" 
            + "        <LENGTHDIST LENGTHUNIT=\"m\">\r\n" 
            + "          <CONSTANT C=\"5.0\" />\r\n" 
            + "        </LENGTHDIST>\r\n" 
            + "        <WIDTHDIST LENGTHUNIT=\"m\">\r\n" 
            + "          <CONSTANT C=\"2.4\" />\r\n" 
            + "        </WIDTHDIST>\r\n" 
            + "        <MAXSPEEDDIST SPEEDUNIT=\"km/h\">\r\n" 
            + "          <CONSTANT C=\"180\" />\r\n" 
            + "        </MAXSPEEDDIST>\r\n" 
            + "      </GTUTEMPLATE>\r\n" 
            + "    </GTUTEMPLATES>\r\n" 
            + "\r\n" 
            + "    <LANETYPES>\r\n" 
            + "      <LANETYPE ID=\"STREET\">\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"ROAD_USER\" DIRECTION=\"FORWARD\" />\r\n" 
            + "      </LANETYPE>\r\n" 
            + "    </LANETYPES>\r\n" 
            + "\r\n" 
            + "    <GTUTYPES>\r\n" 
            + "      <!-- <GTUTYPE PARENT=\"ROAD_USER\" ID=\"CAR\" /> <GTUTYPE PARENT=\"ROAD_USER\" ID=\"TRUCK\" /> -->\r\n" 
            + "    </GTUTYPES>\r\n" 
            + "\r\n" 
            + "    <GTUTEMPLATES>\r\n" 
            + "      <GTUTEMPLATE GTUTYPE=\"CAR\" ID=\"CARS\">\r\n" 
            + "        <LENGTHDIST LENGTHUNIT=\"m\">\r\n" 
            + "          <UNIFORM MIN=\"4\" MAX=\"7\" />\r\n" 
            + "        </LENGTHDIST>\r\n" 
            + "        <WIDTHDIST LENGTHUNIT=\"m\">\r\n" 
            + "          <UNIFORM MIN=\"1.7\" MAX=\"2\" />\r\n" 
            + "        </WIDTHDIST>\r\n" 
            + "        <MAXSPEEDDIST SPEEDUNIT=\"km/h\">\r\n" 
            + "          <CONSTANT C=\"120\" />\r\n" 
            + "        </MAXSPEEDDIST>\r\n" 
            + "      </GTUTEMPLATE>\r\n" 
            + "      <GTUTEMPLATE GTUTYPE=\"TRUCK\" ID=\"TRUCKS\">\r\n" 
            + "        <LENGTHDIST LENGTHUNIT=\"m\">\r\n" 
            + "          <UNIFORM MIN=\"16\" MAX=\"24\" />\r\n" 
            + "        </LENGTHDIST>\r\n" 
            + "        <WIDTHDIST LENGTHUNIT=\"m\">\r\n" 
            + "          <UNIFORM MIN=\"2.2\" MAX=\"2.7\" />\r\n" 
            + "        </WIDTHDIST>\r\n" 
            + "        <MAXSPEEDDIST SPEEDUNIT=\"km/h\">\r\n" 
            + "          <CONSTANT C=\"100\" />\r\n" 
            + "        </MAXSPEEDDIST>\r\n" 
            + "\r\n" 
            + "      </GTUTEMPLATE>\r\n" 
            + "    </GTUTEMPLATES>\r\n" 
            + "\r\n" 
            + "    <LINKTYPES>\r\n" 
            + "      <LINKTYPE ID=\"STREET\">\r\n" 
            + "        <COMPATIBILITY GTUTYPE=\"ROAD_USER\" DIRECTION=\"FORWARD\" />\r\n" 
            + "      </LINKTYPE>\r\n" 
            + "    </LINKTYPES>\r\n" 
            + "\r\n" 
            + "    <ROADLAYOUTS>\r\n" 
            + "      <ROADLAYOUT ID=\"r1\" LINKTYPE=\"STREET\">\r\n" 
            + "        <SHOULDER>\r\n" 
            + "          <WIDTH>2m</WIDTH>\r\n" 
            + "        </SHOULDER>\r\n" 
            + "        <STRIPE TYPE=\"SOLID\">\r\n" 
            + "        </STRIPE>\r\n" 
            + "        <LANE ID=\"FORWARD3\" LANETYPE=\"STREET\" DESIGNDIRECTION=\"true\">\r\n" 
            + "          <WIDTH>3.3m</WIDTH>\r\n" 
            + "          <SPEEDLIMIT GTUTYPE=\"CAR\" LEGALSPEEDLIMIT=\"50 km/h\"></SPEEDLIMIT>\r\n" 
            + "        </LANE>\r\n" 
            + "        <STRIPE TYPE=\"DASHED\">\r\n" 
            + "        </STRIPE>\r\n" 
            + "        <LANE ID=\"FORWARD2\" LANETYPE=\"STREET\" DESIGNDIRECTION=\"true\">\r\n" 
            + "          <CENTEROFFSET>-6.2m</CENTEROFFSET>\r\n" 
            + "          <WIDTH>3.3m</WIDTH>\r\n" 
            + "          <SPEEDLIMIT GTUTYPE=\"CAR\" LEGALSPEEDLIMIT=\"50 km/h\"></SPEEDLIMIT>\r\n" 
            + "        </LANE>\r\n" 
            + "        <STRIPE TYPE=\"DASHED\">\r\n" 
            + "        </STRIPE>\r\n" 
            + "        <LANE ID=\"FORWARD1\" LANETYPE=\"STREET\" DESIGNDIRECTION=\"true\">\r\n" 
            + "          <WIDTH>3.3m</WIDTH>\r\n" 
            + "          <SPEEDLIMIT GTUTYPE=\"CAR\" LEGALSPEEDLIMIT=\"50 km/h\"></SPEEDLIMIT>\r\n" 
            + "        </LANE>\r\n" 
            + "        <STRIPE TYPE=\"SOLID\">\r\n" 
            + "        </STRIPE>\r\n" 
            + "      </ROADLAYOUT>\r\n" 
            + "\r\n" 
            + "      <ROADLAYOUT ID=\"r1g\" LINKTYPE=\"STREET\">\r\n" 
            + "        <SHOULDER>\r\n" 
            + "          <WIDTH>2m</WIDTH>\r\n" 
            + "        </SHOULDER>\r\n" 
            + "        <STRIPE TYPE=\"SOLID\">\r\n" 
            + "        </STRIPE>\r\n" 
            + "        <LANE ID=\"FORWARD3\" LANETYPE=\"STREET\" DESIGNDIRECTION=\"true\">\r\n" 
            + "          <CENTEROFFSETSTART>-6.2m</CENTEROFFSETSTART>\r\n" 
            + "          <CENTEROFFSETEND>-9.5m</CENTEROFFSETEND>\r\n" 
            + "          <WIDTH>3.3m</WIDTH>\r\n" 
            + "          <SPEEDLIMIT GTUTYPE=\"CAR\" LEGALSPEEDLIMIT=\"50 km/h\"></SPEEDLIMIT>\r\n" 
            + "        </LANE>\r\n" 
            + "        <STRIPE TYPE=\"DASHED\">\r\n" 
            + "          <CENTEROFFSET>-7.85m</CENTEROFFSET>\r\n" 
            + "        </STRIPE>\r\n" 
            + "        <LANE ID=\"FORWARD2\" LANETYPE=\"STREET\" DESIGNDIRECTION=\"true\">\r\n" 
            + "          <CENTEROFFSET>-6.2m</CENTEROFFSET>\r\n" 
            + "          <WIDTH>3.3m</WIDTH>\r\n" 
            + "          <SPEEDLIMIT GTUTYPE=\"CAR\" LEGALSPEEDLIMIT=\"50 km/h\"></SPEEDLIMIT>\r\n" 
            + "        </LANE>\r\n" 
            + "        <STRIPE TYPE=\"DASHED\">\r\n" 
            + "          <CENTEROFFSET>-4.55m</CENTEROFFSET>\r\n" 
            + "        </STRIPE>\r\n" 
            + "        <LANE ID=\"FORWARD1\" LANETYPE=\"STREET\" DESIGNDIRECTION=\"true\">\r\n" 
            + "          <CENTEROFFSETSTART>-6.2m</CENTEROFFSETSTART>\r\n" 
            + "          <CENTEROFFSETEND>-2.9m</CENTEROFFSETEND>\r\n" 
            + "          <WIDTH>3.3m</WIDTH>\r\n" 
            + "          <SPEEDLIMIT GTUTYPE=\"CAR\" LEGALSPEEDLIMIT=\"50 km/h\"></SPEEDLIMIT>\r\n" 
            + "        </LANE>\r\n" 
            + "        <STRIPE TYPE=\"SOLID\">\r\n" 
            + "        </STRIPE>\r\n" 
            + "      </ROADLAYOUT>\r\n" 
            + "\r\n" 
            + "      <ROADLAYOUT ID=\"r2r\" LINKTYPE=\"STREET\">\r\n" 
            + "        <SHOULDER>\r\n" 
            + "          <CENTEROFFSET>-8.8m</CENTEROFFSET>\r\n" 
            + "          <WIDTH>2m</WIDTH>\r\n" 
            + "        </SHOULDER>\r\n" 
            + "        <STRIPE TYPE=\"SOLID\">\r\n" 
            + "          <CENTEROFFSET>-7.8m</CENTEROFFSET>\r\n" 
            + "        </STRIPE>\r\n" 
            + "        <LANE ID=\"FORWARD\" LANETYPE=\"STREET\" DESIGNDIRECTION=\"true\">\r\n" 
            + "          <CENTEROFFSET>-6.2m</CENTEROFFSET>\r\n" 
            + "          <WIDTH>3.3m</WIDTH>\r\n" 
            + "          <SPEEDLIMIT GTUTYPE=\"CAR\" LEGALSPEEDLIMIT=\"50 km/h\"></SPEEDLIMIT>\r\n" 
            + "        </LANE>\r\n" 
            + "        <STRIPE TYPE=\"SOLID\">\r\n" 
            + "          <CENTEROFFSET>-4.6m</CENTEROFFSET>\r\n" 
            + "        </STRIPE>\r\n" 
            + "      </ROADLAYOUT>\r\n" 
            + "\r\n" 
            + "      <ROADLAYOUT ID=\"r2l\" LINKTYPE=\"STREET\">\r\n" 
            + "        <SHOULDER>\r\n" 
            + "          <CENTEROFFSET>-8.8m</CENTEROFFSET>\r\n" 
            + "          <WIDTH>2m</WIDTH>\r\n" 
            + "        </SHOULDER>\r\n" 
            + "        <STRIPE TYPE=\"SOLID\">\r\n" 
            + "          <CENTEROFFSET>-7.8m</CENTEROFFSET>\r\n" 
            + "        </STRIPE>\r\n" 
            + "        <LANE ID=\"FORWARD\" LANETYPE=\"STREET\" DESIGNDIRECTION=\"true\">\r\n" 
            + "          <CENTEROFFSET>-6.2m</CENTEROFFSET>\r\n" 
            + "          <WIDTH>3.3m</WIDTH>\r\n" 
            + "          <SPEEDLIMIT GTUTYPE=\"CAR\" LEGALSPEEDLIMIT=\"50 km/h\"></SPEEDLIMIT>\r\n" 
            + "        </LANE>\r\n" 
            + "        <STRIPE TYPE=\"SOLID\">\r\n" 
            + "          <CENTEROFFSET>-4.6m</CENTEROFFSET>\r\n" 
            + "        </STRIPE>\r\n" 
            + "        <!-- <NOTRAFFICLANE> <CENTEROFFSET>0m</CENTEROFFSET> <WIDTH>2.5m</WIDTH> </NOTRAFFICLANE> -->\r\n" 
            + "      </ROADLAYOUT>\r\n" 
            + "\r\n" 
            + "      <ROADLAYOUT ID=\"r3\" LINKTYPE=\"STREET\">\r\n" 
            + "        <STRIPE TYPE=\"SOLID\">\r\n" 
            + "          <CENTEROFFSET>-4.6m</CENTEROFFSET>\r\n" 
            + "        </STRIPE>\r\n" 
            + "        <LANE ID=\"FORWARD\" LANETYPE=\"STREET\" DESIGNDIRECTION=\"true\">\r\n" 
            + "          <CENTEROFFSET>-6.2m</CENTEROFFSET>\r\n" 
            + "          <WIDTH>3.3m</WIDTH>\r\n" 
            + "          <SPEEDLIMIT GTUTYPE=\"CAR\" LEGALSPEEDLIMIT=\"50 km/h\"></SPEEDLIMIT>\r\n" 
            + "        </LANE>\r\n" 
            + "        <STRIPE TYPE=\"SOLID\">\r\n" 
            + "          <CENTEROFFSET>-7.8m</CENTEROFFSET>\r\n" 
            + "        </STRIPE>\r\n" 
            + "      </ROADLAYOUT>\r\n" 
            + "\r\n" 
            + "      <ROADLAYOUT ID=\"r4\" LINKTYPE=\"STREET\">\r\n" 
            + "        <!-- <NOTRAFFICLANE> <CENTEROFFSET>-2.3m</CENTEROFFSET> <WIDTH>4.6m</WIDTH> </NOTRAFFICLANE> -->\r\n" 
            + "        <STRIPE TYPE=\"SOLID\">\r\n" 
            + "          <CENTEROFFSET>-4.6m</CENTEROFFSET>\r\n" 
            + "        </STRIPE>\r\n" 
            + "        <LANE ID=\"FORWARD\" LANETYPE=\"STREET\" DESIGNDIRECTION=\"true\">\r\n" 
            + "          <CENTEROFFSET>-6.2m</CENTEROFFSET>\r\n" 
            + "          <WIDTH>3.3m</WIDTH>\r\n" 
            + "          <SPEEDLIMIT GTUTYPE=\"CAR\" LEGALSPEEDLIMIT=\"50 km/h\"></SPEEDLIMIT>\r\n" 
            + "        </LANE>\r\n" 
            + "        <STRIPE TYPE=\"SOLID\">\r\n" 
            + "          <CENTEROFFSET>-7.8m</CENTEROFFSET>\r\n" 
            + "        </STRIPE>\r\n" 
            + "        <SHOULDER>\r\n" 
            + "          <CENTEROFFSET>-8.8m</CENTEROFFSET>\r\n" 
            + "          <WIDTH>2m</WIDTH>\r\n" 
            + "        </SHOULDER>\r\n" 
            + "      </ROADLAYOUT>\r\n" 
            + "    </ROADLAYOUTS>\r\n" 
            + "  </DEFINITIONS>\r\n" 
            + "  <NETWORK>\r\n" 
            + "    <NODE ID=\"N\" COORDINATE=\"(0,300)\" DIRECTION=\"270 deg(E)\" />\r\n" 
            + "    <NODE ID=\"E\" COORDINATE=\"(300,0)\" DIRECTION=\"180 deg(E)\" />\r\n" 
            + "    <NODE ID=\"S\" COORDINATE=\"(0,-300)\" DIRECTION=\"90 deg(E)\" />\r\n" 
            + "    <NODE ID=\"W\" COORDINATE=\"(-300,0)\" DIRECTION=\"0 deg(E)\" />\r\n" 
            + "    <NODE ID=\"NO\" COORDINATE=\"(0,300)\" DIRECTION=\"90 deg(E)\" />\r\n" 
            + "    <NODE ID=\"EO\" COORDINATE=\"(300,0)\" DIRECTION=\"0 deg(E)\" />\r\n" 
            + "    <NODE ID=\"SO\" COORDINATE=\"(0,-300)\" DIRECTION=\"270 deg(E)\" />\r\n" 
            + "    <NODE ID=\"WO\" COORDINATE=\"(-300,0)\" DIRECTION=\"180 deg(E)\" />\r\n" 
            + "    <NODE ID=\"N1\" COORDINATE=\"(0,240)\" DIRECTION=\"270 deg(E)\" />\r\n" 
            + "    <NODE ID=\"E1\" COORDINATE=\"(240,0)\" DIRECTION=\"180 deg(E)\" />\r\n" 
            + "    <NODE ID=\"S1\" COORDINATE=\"(0,-240)\" DIRECTION=\"90 deg(E)\" />\r\n" 
            + "    <NODE ID=\"W1\" COORDINATE=\"(-240,0)\" DIRECTION=\"0 deg(E)\" />\r\n" 
            + "    <NODE ID=\"N2\" COORDINATE=\"(0,200)\" DIRECTION=\"270 deg(E)\" />\r\n" 
            + "    <NODE ID=\"E2\" COORDINATE=\"(200,0)\" DIRECTION=\"180 deg(E)\" />\r\n" 
            + "    <NODE ID=\"S2\" COORDINATE=\"(0,-200)\" DIRECTION=\"90 deg(E)\" />\r\n" 
            + "    <NODE ID=\"W2\" COORDINATE=\"(-200,0)\" DIRECTION=\"0 deg(E)\" />\r\n" 
            + "    <NODE ID=\"NC\" COORDINATE=\"(0,20)\" DIRECTION=\"270 deg(E)\" />\r\n" 
            + "    <NODE ID=\"EC\" COORDINATE=\"(20,0)\" DIRECTION=\"180 deg(E)\" />\r\n" 
            + "    <NODE ID=\"SC\" COORDINATE=\"(0,-20)\" DIRECTION=\"90 deg(E)\" />\r\n" 
            + "    <NODE ID=\"WC\" COORDINATE=\"(-20,0)\" DIRECTION=\"0 deg(E)\" />\r\n" 
            + "    <NODE ID=\"NCO\" COORDINATE=\"(0,20)\" DIRECTION=\"90 deg(E)\" />\r\n" 
            + "    <NODE ID=\"ECO\" COORDINATE=\"(20,0)\" DIRECTION=\"0 deg(E)\" />\r\n" 
            + "    <NODE ID=\"SCO\" COORDINATE=\"(0,-20)\" DIRECTION=\"270 deg(E)\" />\r\n" 
            + "    <NODE ID=\"WCO\" COORDINATE=\"(-20,0)\" DIRECTION=\"180 deg(E)\" />\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"NN1\" NODESTART=\"N\" NODEEND=\"N1\" TYPE=\"STREET\">\r\n" 
            + "      <STRAIGHT />\r\n" 
            + "      <DEFINEDLAYOUT>r2l</DEFINEDLAYOUT>\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"N1N2\" NODESTART=\"N1\" NODEEND=\"N2\" TYPE=\"STREET\">\r\n" 
            + "      <STRAIGHT />\r\n" 
            + "      <DEFINEDLAYOUT>r1g</DEFINEDLAYOUT>\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"N2NC\" NODESTART=\"N2\" NODEEND=\"NC\" TYPE=\"STREET\">\r\n" 
            + "      <STRAIGHT />\r\n" 
            + "      <DEFINEDLAYOUT>r1</DEFINEDLAYOUT>\r\n" 
            + "      <TRAFFICLIGHT LANE=\"FORWARD3\" CLASS=\"org.opentrafficsim.road.network.lane.object.trafficlight"
            + ".SimpleTrafficLight\" ID=\"10\" POSITION=\"END-0.00m\" />\r\n" 
            + "      <TRAFFICLIGHT LANE=\"FORWARD2\" CLASS=\"org.opentrafficsim.road.network.lane.object.trafficlight"
            + ".SimpleTrafficLight\" ID=\"11\" POSITION=\"END-0.00m\" />\r\n" 
            + "      <TRAFFICLIGHT LANE=\"FORWARD1\" CLASS=\"org.opentrafficsim.road.network.lane.object.trafficlight"
            + ".SimpleTrafficLight\" ID=\"12\" POSITION=\"END-0.00m\" />\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"EE1\" NODESTART=\"E\" NODEEND=\"E1\" TYPE=\"STREET\">\r\n" 
            + "      <STRAIGHT />\r\n" 
            + "      <DEFINEDLAYOUT>r2l</DEFINEDLAYOUT>\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"E1E2\" NODESTART=\"E1\" NODEEND=\"E2\" TYPE=\"STREET\">\r\n" 
            + "      <STRAIGHT />\r\n" 
            + "      <DEFINEDLAYOUT>r1g</DEFINEDLAYOUT>\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"E2EC\" NODESTART=\"E2\" NODEEND=\"EC\" TYPE=\"STREET\">\r\n" 
            + "      <STRAIGHT />\r\n" 
            + "      <DEFINEDLAYOUT>r1</DEFINEDLAYOUT>\r\n" 
            + "      <TRAFFICLIGHT LANE=\"FORWARD3\" CLASS=\"org.opentrafficsim.road.network.lane.object.trafficlight"
            + ".SimpleTrafficLight\" ID=\"01\" POSITION=\"END-0.00m\" />\r\n" 
            + "      <TRAFFICLIGHT LANE=\"FORWARD2\" CLASS=\"org.opentrafficsim.road.network.lane.object.trafficlight"
            + ".SimpleTrafficLight\" ID=\"02\" POSITION=\"END-0.00m\" />\r\n" 
            + "      <TRAFFICLIGHT LANE=\"FORWARD1\" CLASS=\"org.opentrafficsim.road.network.lane.object.trafficlight"
            + ".SimpleTrafficLight\" ID=\"03\" POSITION=\"END-0.00m\" />\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"SS1\" NODESTART=\"S\" NODEEND=\"S1\" TYPE=\"STREET\">\r\n" 
            + "      <STRAIGHT />\r\n" 
            + "      <DEFINEDLAYOUT>r2l</DEFINEDLAYOUT>\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"S1S2\" NODESTART=\"S1\" NODEEND=\"S2\" TYPE=\"STREET\">\r\n" 
            + "      <STRAIGHT />\r\n" 
            + "      <DEFINEDLAYOUT>r1g</DEFINEDLAYOUT>\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"S2SC\" NODESTART=\"S2\" NODEEND=\"SC\" TYPE=\"STREET\">\r\n" 
            + "      <STRAIGHT />\r\n" 
            + "      <DEFINEDLAYOUT>r1</DEFINEDLAYOUT>\r\n" 
            + "      <TRAFFICLIGHT LANE=\"FORWARD3\" CLASS=\"org.opentrafficsim.road.network.lane.object.trafficlight"
            + ".SimpleTrafficLight\" ID=\"04\" POSITION=\"END-0.00m\" />\r\n" 
            + "      <TRAFFICLIGHT LANE=\"FORWARD2\" CLASS=\"org.opentrafficsim.road.network.lane.object.trafficlight"
            + ".SimpleTrafficLight\" ID=\"05\" POSITION=\"END-0.00m\" />\r\n" 
            + "      <TRAFFICLIGHT LANE=\"FORWARD1\" CLASS=\"org.opentrafficsim.road.network.lane.object.trafficlight"
            + ".SimpleTrafficLight\" ID=\"06\" POSITION=\"END-0.00m\" />\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"WW1\" NODESTART=\"W\" NODEEND=\"W1\" TYPE=\"STREET\">\r\n" 
            + "      <STRAIGHT />\r\n" 
            + "      <DEFINEDLAYOUT>r2l</DEFINEDLAYOUT>\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"W1W2\" NODESTART=\"W1\" NODEEND=\"W2\" TYPE=\"STREET\">\r\n" 
            + "      <STRAIGHT />\r\n" 
            + "      <DEFINEDLAYOUT>r1g</DEFINEDLAYOUT>\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"W2WC\" NODESTART=\"W2\" NODEEND=\"WC\" TYPE=\"STREET\">\r\n" 
            + "      <STRAIGHT />\r\n" 
            + "      <DEFINEDLAYOUT>r1</DEFINEDLAYOUT>\r\n" 
            + "      <TRAFFICLIGHT LANE=\"FORWARD3\" CLASS=\"org.opentrafficsim.road.network.lane.object.trafficlight"
            + ".SimpleTrafficLight\" ID=\"07\" POSITION=\"END-0.00m\" />\r\n" 
            + "      <TRAFFICLIGHT LANE=\"FORWARD2\" CLASS=\"org.opentrafficsim.road.network.lane.object.trafficlight"
            + ".SimpleTrafficLight\" ID=\"08\" POSITION=\"END-0.00m\" />\r\n" 
            + "      <TRAFFICLIGHT LANE=\"FORWARD1\" CLASS=\"org.opentrafficsim.road.network.lane.object.trafficlight"
            + ".SimpleTrafficLight\" ID=\"09\" POSITION=\"END-0.00m\" />\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"ECNC\" NODESTART=\"EC\" NODEEND=\"NCO\" TYPE=\"STREET\" OFFSETSTART=\"-3.3m\" "
            + "OFFSETEND=\"0m\">\r\n" 
            + "      <BEZIER SHAPE=\"0.5\" />\r\n" 
            + "      <DEFINEDLAYOUT>r2r</DEFINEDLAYOUT>\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"ECSC\" NODESTART=\"EC\" NODEEND=\"SCO\" TYPE=\"STREET\" OFFSETSTART=\"3.3m\" "
            + "OFFSETEND=\"0m\">\r\n" 
            + "      <BEZIER SHAPE=\"1.0\" />\r\n" 
            + "      <DEFINEDLAYOUT>r2l</DEFINEDLAYOUT>\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"NCWC\" NODESTART=\"NC\" NODEEND=\"WCO\" TYPE=\"STREET\" OFFSETSTART=\"-3.3m\" "
            + "OFFSETEND=\"0m\">\r\n" 
            + "      <BEZIER SHAPE=\"0.5\" />\r\n" 
            + "      <DEFINEDLAYOUT>r2r</DEFINEDLAYOUT>\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"NCEC\" NODESTART=\"NC\" NODEEND=\"ECO\" TYPE=\"STREET\" OFFSETSTART=\"3.3m\" "
            + "OFFSETEND=\"0m\">\r\n" 
            + "      <BEZIER SHAPE=\"1.0\" />\r\n" 
            + "      <DEFINEDLAYOUT>r2l</DEFINEDLAYOUT>\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"WCSC\" NODESTART=\"WC\" NODEEND=\"SCO\" TYPE=\"STREET\" OFFSETSTART=\"-3.3m\" "
            + "OFFSETEND=\"0m\">\r\n" 
            + "      <BEZIER SHAPE=\"0.5\" />\r\n" 
            + "      <DEFINEDLAYOUT>r2l</DEFINEDLAYOUT>\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"WCNC\" NODESTART=\"WC\" NODEEND=\"NCO\" TYPE=\"STREET\" OFFSETSTART=\"3.3m\" "
            + "OFFSETEND=\"0m\">\r\n" 
            + "      <BEZIER SHAPE=\"1.0\" />\r\n" 
            + "      <DEFINEDLAYOUT>r2l</DEFINEDLAYOUT>\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"SCEC\" NODESTART=\"SC\" NODEEND=\"ECO\" TYPE=\"STREET\" OFFSETSTART=\"-3.3m\" "
            + "OFFSETEND=\"0m\">\r\n" 
            + "      <BEZIER SHAPE=\"0.5\" />\r\n" 
            + "      <DEFINEDLAYOUT>r2l</DEFINEDLAYOUT>\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"SCWC\" NODESTART=\"SC\" NODEEND=\"WCO\" TYPE=\"STREET\" OFFSETSTART=\"3.3m\" "
            + "OFFSETEND=\"0m\">\r\n" 
            + "      <BEZIER SHAPE=\"1.0\" />\r\n" 
            + "      <DEFINEDLAYOUT>r2l</DEFINEDLAYOUT>\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"NCSC\" NODESTART=\"NC\" NODEEND=\"SCO\" TYPE=\"STREET\">\r\n" 
            + "      <STRAIGHT />\r\n" 
            + "      <DEFINEDLAYOUT>r3</DEFINEDLAYOUT>\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"WCEC\" NODESTART=\"WC\" NODEEND=\"ECO\" TYPE=\"STREET\">\r\n" 
            + "      <STRAIGHT />\r\n" 
            + "      <DEFINEDLAYOUT>r3</DEFINEDLAYOUT>\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"SCNC\" NODESTART=\"SC\" NODEEND=\"NCO\" TYPE=\"STREET\">\r\n" 
            + "      <STRAIGHT />\r\n" 
            + "      <DEFINEDLAYOUT>r3</DEFINEDLAYOUT>\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"ECWC\" NODESTART=\"EC\" NODEEND=\"WCO\" TYPE=\"STREET\">\r\n" 
            + "      <STRAIGHT />\r\n" 
            + "      <DEFINEDLAYOUT>r3</DEFINEDLAYOUT>\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"ECOEO\" NODESTART=\"ECO\" NODEEND=\"EO\" TYPE=\"STREET\">\r\n" 
            + "      <STRAIGHT />\r\n" 
            + "      <DEFINEDLAYOUT>r4</DEFINEDLAYOUT>\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"NCONO\" NODESTART=\"NCO\" NODEEND=\"NO\" TYPE=\"STREET\">\r\n" 
            + "      <STRAIGHT />\r\n" 
            + "      <DEFINEDLAYOUT>r4</DEFINEDLAYOUT>\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"WCOWO\" NODESTART=\"WCO\" NODEEND=\"WO\" TYPE=\"STREET\">\r\n" 
            + "      <STRAIGHT />\r\n" 
            + "      <DEFINEDLAYOUT>r4</DEFINEDLAYOUT>\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "    <LINK ID=\"SCOSO\" NODESTART=\"SCO\" NODEEND=\"SO\" TYPE=\"STREET\">\r\n" 
            + "      <STRAIGHT />\r\n" 
            + "      <DEFINEDLAYOUT>r4</DEFINEDLAYOUT>\r\n" 
            + "    </LINK>\r\n" 
            + "\r\n" 
            + "  </NETWORK>\r\n" 
            + "  <NETWORKDEMAND>\r\n" 
            + "    <OD ID=\"Traffic Demand\" GLOBALINTERPOLATION=\"STEPWISE\" GLOBALFACTOR=\"1.000\">\r\n" 
            + "      <CATEGORY GTUTYPE=\"CAR\" ID=\"CAR\" />\r\n" 
            + "      <CATEGORY GTUTYPE=\"TRUCK\" ID=\"TRUCK\" />\r\n" 
            + "      <DEMAND CATEGORY=\"CAR\" ORIGIN=\"W\" DESTINATION=\"NO\">\r\n" 
            + "        <LEVEL TIME=\"0s\">200.00veh/h</LEVEL>\r\n" 
            + "        <LEVEL TIME=\"3600s\">200.00veh/h</LEVEL>\r\n" 
            + "      </DEMAND>\r\n" 
            + "      <DEMAND CATEGORY=\"CAR\" ORIGIN=\"W\" DESTINATION=\"EO\">\r\n" 
            + "        <LEVEL TIME=\"0s\">300.00veh/h</LEVEL>\r\n" 
            + "        <LEVEL TIME=\"3600s\">300.00veh/h</LEVEL>\r\n" 
            + "      </DEMAND>\r\n" 
            + "      <DEMAND CATEGORY=\"CAR\" ORIGIN=\"W\" DESTINATION=\"SO\">\r\n" 
            + "        <LEVEL TIME=\"0s\">200.00veh/h</LEVEL>\r\n" 
            + "        <LEVEL TIME=\"3600s\">200.00veh/h</LEVEL>\r\n" 
            + "      </DEMAND>\r\n" 
            + "      <DEMAND CATEGORY=\"CAR\" ORIGIN=\"N\" DESTINATION=\"EO\">\r\n" 
            + "        <LEVEL TIME=\"0s\">200.00veh/h</LEVEL>\r\n" 
            + "        <LEVEL TIME=\"3600s\">200.00veh/h</LEVEL>\r\n" 
            + "      </DEMAND>\r\n" 
            + "      <DEMAND CATEGORY=\"CAR\" ORIGIN=\"N\" DESTINATION=\"SO\">\r\n" 
            + "        <LEVEL TIME=\"0s\">300.00veh/h</LEVEL>\r\n" 
            + "        <LEVEL TIME=\"3600s\">300.00veh/h</LEVEL>\r\n" 
            + "      </DEMAND>\r\n" 
            + "      <DEMAND CATEGORY=\"CAR\" ORIGIN=\"N\" DESTINATION=\"WO\">\r\n" 
            + "        <LEVEL TIME=\"0s\">200.00veh/h</LEVEL>\r\n" 
            + "        <LEVEL TIME=\"3600s\">200.00veh/h</LEVEL>\r\n" 
            + "      </DEMAND>\r\n" 
            + "      <DEMAND CATEGORY=\"CAR\" ORIGIN=\"E\" DESTINATION=\"SO\">\r\n" 
            + "        <LEVEL TIME=\"0s\">200.00veh/h</LEVEL>\r\n" 
            + "        <LEVEL TIME=\"3600s\">200.00veh/h</LEVEL>\r\n" 
            + "      </DEMAND>\r\n" 
            + "      <DEMAND CATEGORY=\"CAR\" ORIGIN=\"E\" DESTINATION=\"WO\">\r\n" 
            + "        <LEVEL TIME=\"0s\">300.00veh/h</LEVEL>\r\n" 
            + "        <LEVEL TIME=\"3600s\">300.00veh/h</LEVEL>\r\n" 
            + "      </DEMAND>\r\n" 
            + "      <DEMAND CATEGORY=\"CAR\" ORIGIN=\"E\" DESTINATION=\"NO\">\r\n" 
            + "        <LEVEL TIME=\"0s\">200.00veh/h</LEVEL>\r\n" 
            + "        <LEVEL TIME=\"3600s\">200.00veh/h</LEVEL>\r\n" 
            + "      </DEMAND>\r\n" 
            + "      <DEMAND CATEGORY=\"CAR\" ORIGIN=\"S\" DESTINATION=\"WO\">\r\n" 
            + "        <LEVEL TIME=\"0s\">200.00veh/h</LEVEL>\r\n" 
            + "        <LEVEL TIME=\"3600s\">200.00veh/h</LEVEL>\r\n" 
            + "      </DEMAND>\r\n" 
            + "      <DEMAND CATEGORY=\"CAR\" ORIGIN=\"S\" DESTINATION=\"NO\">\r\n" 
            + "        <LEVEL TIME=\"0s\">300.00veh/h</LEVEL>\r\n" 
            + "        <LEVEL TIME=\"3600s\">300.00veh/h</LEVEL>\r\n" 
            + "      </DEMAND>\r\n" 
            + "      <DEMAND CATEGORY=\"CAR\" ORIGIN=\"S\" DESTINATION=\"EO\">\r\n" 
            + "        <LEVEL TIME=\"0s\">200.00veh/h</LEVEL>\r\n" 
            + "        <LEVEL TIME=\"3600s\">200.00veh/h</LEVEL>\r\n" 
            + "      </DEMAND>\r\n" 
            + "    </OD>\r\n" 
            + "    <SINK LANE=\"FORWARD\" LINK=\"NCONO\" POSITION=\"END-20m\" DIRECTION=\"DIR_PLUS\" />\r\n" 
            + "    <SINK LANE=\"FORWARD\" LINK=\"ECOEO\" POSITION=\"END-20m\" DIRECTION=\"DIR_PLUS\" />\r\n" 
            + "    <SINK LANE=\"FORWARD\" LINK=\"SCOSO\" POSITION=\"END-20m\" DIRECTION=\"DIR_PLUS\" />\r\n" 
            + "    <SINK LANE=\"FORWARD\" LINK=\"WCOWO\" POSITION=\"END-20m\" DIRECTION=\"DIR_PLUS\" />\r\n" 
            + "  </NETWORKDEMAND>\r\n" 
            + "  <MODEL />\r\n" 
            + "  <SCENARIO />\r\n" 
            + "  <RUN>\r\n" 
            + "    <RUNLENGTH>3600s</RUNLENGTH>\r\n" 
            + "    <NUMBERREPLICATIONS>1</NUMBERREPLICATIONS>\r\n" 
            + "    <RANDOMSTREAMS>\r\n" 
            + "      <RANDOMSTREAM ID=\"default\">\r\n" 
            + "        <REPLICATION SEED=\"1\" ID=\"1\" />\r\n" 
            + "      </RANDOMSTREAM>\r\n" 
            + "      <RANDOMSTREAM ID=\"generation\">\r\n" 
            + "        <REPLICATION SEED=\"1\" ID=\"1\" />\r\n" 
            + "      </RANDOMSTREAM>\r\n" 
            + "    </RANDOMSTREAMS>\r\n" 
            + "  </RUN>\r\n" 
            + "\r\n" 
            + "</OTS>\r\n";
    // @formatter:on

}
