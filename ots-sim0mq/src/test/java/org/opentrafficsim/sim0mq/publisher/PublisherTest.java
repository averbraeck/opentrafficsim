package org.opentrafficsim.sim0mq.publisher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Scanner;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.event.EventInterface;
import org.djutils.event.EventListenerInterface;
import org.djutils.serialization.SerializationException;
import org.junit.Test;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.parser.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.conflict.ConflictBuilder;
import org.opentrafficsim.road.network.lane.conflict.LaneCombinationList;
import org.sim0mq.Sim0MQException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.StreamInformation;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;
import nl.tudelft.simulation.dsol.statistics.StatisticsInterface;

/**
 * Unit tests. This requires half of OTS in the imports because it sets up a simulation and runs that for a couple of seconds.
 * <p>
 * Copyright (c) 2020-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class PublisherTest implements OTSModelInterface
{

    /** ... */
    private static final long serialVersionUID = 20200505L;

    /** Storage for the last result submitted to the ReturnWrapper. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Object[] lastResult = null;

    /**
     * Test the Publisher class.
     * @throws RemoteException when that happens this test has failed
     * @throws NetworkException if that happens uncaught; this test has failed
     * @throws OTSGeometryException if that happens uncaught; this test has failed
     * @throws NamingException on context error
     * @throws SimRuntimeException on DSOL error
     * @throws SerializationException - when encoding an error message fails
     * @throws Sim0MQException - when encoding an error message fails
     */
    @Test
    public void testPublisher() throws RemoteException, NetworkException, OTSGeometryException, SimRuntimeException,
            NamingException, Sim0MQException, SerializationException
    {
        ReturnWrapper storeLastResult = new ReturnWrapper()
        {
            @Override
            public void encodeReplyAndTransmit(final Boolean ackNack, final Object[] payload)
            {
                PublisherTest.this.lastResult = payload;
            }
        };
        OTSSimulatorInterface simulator = new OTSSimulator("test simulator for PublisherTest");
        OTSRoadNetwork network = new OTSRoadNetwork("test network for PublisherTest", true, simulator);
        Publisher publisher = new Publisher(network);
        assertTrue("id of publisher contains id of network", publisher.getId().contains(network.getId()));
        TransceiverInterface transceiverInterface = publisher.getIdSource(0, storeLastResult);
        assertNotNull("result of getIdSource should not be null", transceiverInterface);
        Object[] transceiverNames = transceiverInterface.get(null, storeLastResult);
        assertTrue("result of getIdSource should not be empty", transceiverNames.length > 0);
        for (Object o : transceiverNames)
        {
            assertTrue("transceiver name is a String", o instanceof String);
            // System.out.println("transceiver: " + o);
        }
        // See if we can obtain the GTUIdTransceiver
        Object[] subscriptionHandler = publisher.get(new Object[] {"GTUs in network"}, storeLastResult);
        assertNotNull("result of get should not be null", subscriptionHandler);
        assertEquals("result should contain one elements", 1, subscriptionHandler.length);
        System.out.println(subscriptionHandler[0]);
        assertTrue("Result should contain a String", subscriptionHandler[0] instanceof SubscriptionHandler);
        this.lastResult = null;
        assertNull("request for non existent transceiver should return null",
                publisher.get(new Object[] {"No such transceiver"}, storeLastResult));
        checkLastResult("No transceiver with name \"No such transceiver\"");
        assertNull("getIdSource with wrong index returns null", publisher.getIdSource(1, storeLastResult));
        checkLastResult("Address should be 0");
        assertNull("getIdSource with wrong index returns null", publisher.getIdSource(-1, storeLastResult));
        checkLastResult("Address should be 0");
    }

    /**
     * Verify that <code>lastResult</code> is not null, an Object array of length 1 and the one and only element is a String
     * with the expected text.
     * @param expectedText String; the expected text
     */
    public void checkLastResult(final String expectedText)
    {
        assertNotNull("returnWrapper has stored something", this.lastResult);
        assertEquals("returnWrapper has stored one object error message", 1, this.lastResult.length);
        assertEquals("Stored result is expected string", expectedText, this.lastResult[0]);
        this.lastResult = null;
    }

    @Override
    public void constructModel() throws SimRuntimeException
    {
        // Ignore
    }

    @Override
    public final OTSSimulatorInterface getSimulator()
    {
        return null;
    }

    @Override
    public final InputParameterMap getInputParameterMap()
    {
        return null;
    }

    @Override
    public final List<StatisticsInterface<Duration>> getOutputStatistics()
    {
        return null;
    }

    @Override
    public final OTSNetwork getNetwork()
    {
        return null;
    }

    @Override
    public final String getShortName()
    {
        return null;
    }

    @Override
    public final String getDescription()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void setStreamInformation(final StreamInformation streamInformation)
    {
    }

    /** {@inheritDoc} */
    @Override
    public StreamInformation getStreamInformation()
    {
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
        public void setStreamInformation(final StreamInformation streamInformation)
        {
            //
        }

        /** {@inheritDoc} */
        @Override
        public StreamInformation getStreamInformation()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Serializable getSourceId()
        {
            return "Sim0MQOTSModel";
        }

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
