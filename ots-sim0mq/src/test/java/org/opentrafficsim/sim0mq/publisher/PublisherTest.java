package org.opentrafficsim.sim0mq.publisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Scanner;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.serialization.SerializationException;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.factory.xml.parser.XmlParser;
import org.opentrafficsim.road.network.lane.conflict.ConflictBuilder;
import org.opentrafficsim.road.network.lane.conflict.LaneCombinationList;
import org.sim0mq.Sim0MQException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.StreamInformation;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;
import nl.tudelft.simulation.dsol.statistics.SimulationStatistic;

/**
 * Unit tests. This requires half of OTS in the imports because it sets up a simulation and runs that for a couple of seconds.
 * <p>
 * Copyright (c) 2020-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public final class PublisherTest implements OtsModelInterface
{

    /** ... */
    private static final long serialVersionUID = 20200505L;

    /** Storage for the last result submitted to the ReturnWrapper. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Object[] lastResult = null;

    /** */
    private PublisherTest()
    {
        // do not instantiate test class
    }

    /**
     * Test the Publisher class.
     * @throws RemoteException when that happens this test has failed
     * @throws NetworkException if that happens uncaught; this test has failed
     * @throws NamingException on context error
     * @throws SimRuntimeException on DSOL error
     * @throws SerializationException - when encoding an error message fails
     * @throws Sim0MQException - when encoding an error message fails
     */
    @Test
    public void testPublisher() throws RemoteException, NetworkException, SimRuntimeException, NamingException, Sim0MQException,
            SerializationException
    {
        ReturnWrapper storeLastResult = new ReturnWrapper()
        {
            @Override
            public void encodeReplyAndTransmit(final Boolean ackNack, final Object[] payload)
            {
                PublisherTest.this.lastResult = payload;
            }
        };
        OtsSimulatorInterface simulator = new OtsSimulator("test simulator for PublisherTest");
        RoadNetwork network = new RoadNetwork("test network for PublisherTest", simulator);
        Publisher publisher = new Publisher(network);
        assertTrue(publisher.getId().contains(network.getId()), "id of publisher contains id of network");
        TransceiverInterface transceiverInterface = publisher.getIdSource(0, storeLastResult);
        assertNotNull(transceiverInterface, "result of getIdSource should not be null");
        Object[] transceiverNames = transceiverInterface.get(null, storeLastResult);
        assertTrue(transceiverNames.length > 0, "result of getIdSource should not be empty");
        for (Object o : transceiverNames)
        {
            assertTrue(o instanceof String, "transceiver name is a String");
            // System.out.println("transceiver: " + o);
        }
        // See if we can obtain the GtuIdTransceiver
        Object[] subscriptionHandler = publisher.get(new Object[] {"GTUs in network"}, storeLastResult);
        assertNotNull(subscriptionHandler, "result of get should not be null");
        assertEquals(1, subscriptionHandler.length, "result should contain one elements");
        // System.out.println(subscriptionHandler[0]);
        assertTrue(subscriptionHandler[0] instanceof SubscriptionHandler, "Result should contain a String");
        this.lastResult = null;
        assertNull(publisher.get(new Object[] {"No such transceiver"}, storeLastResult),
                "request for non existent transceiver should return null");
        checkLastResult("No transceiver with name \"No such transceiver\"");
        assertNull(publisher.getIdSource(1, storeLastResult), "getIdSource with wrong index returns null");
        checkLastResult("Address should be 0");
        assertNull(publisher.getIdSource(-1, storeLastResult), "getIdSource with wrong index returns null");
        checkLastResult("Address should be 0");
    }

    /**
     * Verify that <code>lastResult</code> is not null, an Object array of length 1 and the one and only element is a String
     * with the expected text.
     * @param expectedText the expected text
     */
    public void checkLastResult(final String expectedText)
    {
        assertNotNull(this.lastResult, "returnWrapper has stored something");
        assertEquals(1, this.lastResult.length, "returnWrapper has stored one object error message");
        assertEquals(expectedText, this.lastResult[0], "Stored result is expected string");
        this.lastResult = null;
    }

    @Override
    public void constructModel() throws SimRuntimeException
    {
        // Ignore
    }

    @Override
    public OtsSimulatorInterface getSimulator()
    {
        return null;
    }

    @Override
    public InputParameterMap getInputParameterMap()
    {
        return null;
    }

    @Override
    public List<SimulationStatistic<Duration>> getOutputStatistics()
    {
        return null;
    }

    @Override
    public Network getNetwork()
    {
        return null;
    }

    @Override
    public String getShortName()
    {
        return null;
    }

    @Override
    public String getDescription()
    {
        return null;
    }

    @Override
    public void setStreamInformation(final StreamInformation streamInformation)
    {
    }

    @Override
    public StreamInformation getStreamInformation()
    {
        return null;
    }

    /**
     * Open an URL, read it and store the contents in a string. Adapted from
     * https://stackoverflow.com/questions/4328711/read-url-to-string-in-few-lines-of-java-code
     * @param url the URL
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
    class TestModel extends AbstractOtsModel implements EventListener
    {
        /** */
        private static final long serialVersionUID = 20170419L;

        /** The network. */
        private RoadNetwork network;

        /** The XML. */
        private final String xml;

        /**
         * Constructor.
         * @param simulator the simulator
         * @param shortName the model name
         * @param description the model description
         * @param xml the XML description of the simulation model
         */
        TestModel(final OtsSimulatorInterface simulator, final String shortName, final String description, final String xml)
        {
            super(simulator, shortName, description, AbstractOtsModel.defaultInitialStreams());
            this.xml = xml;
        }

        @Override
        public void notify(final Event event)
        {
            System.err.println("Received event " + event);
        }

        @Override
        public void constructModel() throws SimRuntimeException
        {
            this.network = new RoadNetwork(getShortName(), getSimulator());
            try
            {
                new XmlParser(this.network).setStream(new ByteArrayInputStream(this.xml.getBytes(StandardCharsets.UTF_8)))
                        .build();
                LaneCombinationList ignoreList = new LaneCombinationList();
                LaneCombinationList permittedList = new LaneCombinationList();
                ConflictBuilder.buildConflictsParallel(this.network, getSimulator(),
                        new ConflictBuilder.FixedWidthGenerator(Length.ofSI(2.0)), ignoreList, permittedList);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public Network getNetwork()
        {
            return this.network;
        }

        @Override
        public void setStreamInformation(final StreamInformation streamInformation)
        {
            //
        }

        @Override
        public StreamInformation getStreamInformation()
        {
            return null;
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
            + "  <Definitions>\r\n"
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
            + "    <LinkTypes xmlns:ots=\"http://www.opentrafficsim.org/ots\" "
            + "xmlns=\"http://www.opentrafficsim.org/ots\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
            + "      xsi:schemaLocation=\"http://www.opentrafficsim.org/ots "
            + "../ots-definitions.xsd http://www.opentrafficsim.org/ots "
            + "https://opentrafficsim.org/docs/xsd/1.03.00/ots-definitions.xsd\">\r\n"
            + "      <LinkType ID=\"NONE\" DEFAULT=\"true\">\r\n"
            + "        <Compatibility GTUTYPE=\"NONE\" DIRECTION=\"NONE\" />\r\n"
            + "      </LinkType>\r\n"
            + "      <LinkType ID=\"ROAD\" DEFAULT=\"true\">\r\n"
            + "        <Compatibility GTUTYPE=\"ROAD_USER\" DIRECTION=\"FORWARD\" />\r\n"
            + "      </LinkType>\r\n"
            + "      <LinkType ID=\"FREEWAY\" PARENT=\"ROAD\" DEFAULT=\"true\">\r\n"
            + "        <Compatibility GTUTYPE=\"ROAD_USER\" DIRECTION=\"FORWARD\" />\r\n"
            + "        <Compatibility GTUTYPE=\"PEDESTRIAN\" DIRECTION=\"NONE\" />\r\n"
            + "        <Compatibility GTUTYPE=\"BICYCLE\" DIRECTION=\"NONE\" />\r\n"
            + "      </LinkType>\r\n"
            + "      <LinkType ID=\"WATERWAY\" DEFAULT=\"true\">\r\n"
            + "        <Compatibility GTUTYPE=\"WATERWAY_USER\" DIRECTION=\"BOTH\" />\r\n"
            + "      </LinkType>\r\n"
            + "      <LinkType ID=\"RAILWAY\" DEFAULT=\"true\">\r\n"
            + "        <Compatibility GTUTYPE=\"RAILWAY_USER\" DIRECTION=\"FORWARD\" />\r\n"
            + "      </LinkType>\r\n"
            + "      <LinkType ID=\"CONNECTOR\" DEFAULT=\"true\">\r\n"
            + "        <Compatibility GTUTYPE=\"ROAD_USER\" DIRECTION=\"FORWARD\" />\r\n"
            + "        <Compatibility GTUTYPE=\"WATERWAY_USER\" DIRECTION=\"FORWARD\" />\r\n"
            + "        <Compatibility GTUTYPE=\"RAILWAY_USER\" DIRECTION=\"FORWARD\" />\r\n"
            + "      </LinkType>\r\n"
            + "    </LinkTypes>\r\n"
            + "\r\n"
            + "    <LaneTypes xmlns=\"http://www.opentrafficsim.org/ots\" "
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
            + "xsi:schemaLocation=\"http://www.opentrafficsim.org/ots ../ots-definitions.xsd\">\r\n"
            + "      <LaneType ID=\"NONE\" DEFAULT=\"true\">\r\n"
            + "        <Compatibility GTUTYPE=\"NONE\" DIRECTION=\"NONE\" />\r\n"
            + "      </LaneType>\r\n"
            + "      <LaneType ID=\"TWO_WAY_LANE\" DEFAULT=\"true\">\r\n"
            + "        <Compatibility GTUTYPE=\"ROAD_USER\" DIRECTION=\"FORWARD\" />\r\n"
            + "      </LaneType>\r\n"
            + "      <LaneType ID=\"RURAL_ROAD\" PARENT=\"TWO_WAY_LANE\" DEFAULT=\"true\">\r\n"
            + "        <Compatibility GTUTYPE=\"ROAD_USER\" DIRECTION=\"FORWARD\" />\r\n"
            + "      </LaneType>\r\n"
            + "      <LaneType ID=\"URBAN_ROAD\" PARENT=\"TWO_WAY_LANE\" DEFAULT=\"true\">\r\n"
            + "        <Compatibility GTUTYPE=\"ROAD_USER\" DIRECTION=\"FORWARD\" />\r\n"
            + "      </LaneType>\r\n"
            + "      <LaneType ID=\"RESIDENTIAL_ROAD\" PARENT=\"TWO_WAY_LANE\" DEFAULT=\"true\">\r\n"
            + "        <Compatibility GTUTYPE=\"ROAD_USER\" DIRECTION=\"FORWARD\" />\r\n"
            + "      </LaneType>\r\n"
            + "      <LaneType ID=\"ONE_WAY_LANE\" DEFAULT=\"true\">\r\n"
            + "        <Compatibility GTUTYPE=\"ROAD_USER\" DIRECTION=\"FORWARD\" />\r\n"
            + "        <Compatibility GTUTYPE=\"PEDESTRIAN\" DIRECTION=\"BOTH\" />\r\n"
            + "      </LaneType>\r\n"
            + "      <LaneType ID=\"FREEWAY\" DEFAULT=\"true\">\r\n"
            + "        <Compatibility GTUTYPE=\"ROAD_USER\" DIRECTION=\"FORWARD\" />\r\n"
            + "        <Compatibility GTUTYPE=\"PEDESTRIAN\" DIRECTION=\"NONE\" />\r\n"
            + "      </LaneType>\r\n"
            + "      <LaneType ID=\"HIGHWAY\" DEFAULT=\"true\">\r\n"
            + "        <Compatibility GTUTYPE=\"ROAD_USER\" DIRECTION=\"FORWARD\" />\r\n"
            + "        <Compatibility GTUTYPE=\"PEDESTRIAN\" DIRECTION=\"NONE\" />\r\n"
            + "      </LaneType>\r\n"
            + "      <LaneType ID=\"BUS_LANE\" DEFAULT=\"true\">\r\n"
            + "        <Compatibility GTUTYPE=\"ROAD_USER\" DIRECTION=\"NONE\" />\r\n"
            + "        <Compatibility GTUTYPE=\"BUS\" DIRECTION=\"FORWARD\" />\r\n"
            + "      </LaneType>\r\n"
            + "      <LaneType ID=\"MOPED_PATH\" DEFAULT=\"true\">\r\n"
            + "        <Compatibility GTUTYPE=\"ROAD_USER\" DIRECTION=\"NONE\" />\r\n"
            + "        <Compatibility GTUTYPE=\"BICYCLE\" DIRECTION=\"FORWARD\" />\r\n"
            + "        <!-- a MOPED is a special BICYCLE -->\r\n"
            + "      </LaneType>\r\n"
            + "      <LaneType ID=\"BICYCLE_PATH\" DEFAULT=\"true\">\r\n"
            + "        <Compatibility GTUTYPE=\"ROAD_USER\" DIRECTION=\"NONE\" />\r\n"
            + "        <Compatibility GTUTYPE=\"BICYCLE\" DIRECTION=\"FORWARD\" />\r\n"
            + "        <Compatibility GTUTYPE=\"MOPED\" DIRECTION=\"NONE\" />\r\n"
            + "      </LaneType>\r\n"
            + "      <LaneType ID=\"FOOTPATH\" DEFAULT=\"true\">\r\n"
            + "        <Compatibility GTUTYPE=\"ROAD_USER\" DIRECTION=\"NONE\" />\r\n"
            + "        <Compatibility GTUTYPE=\"PEDESTRIAN\" DIRECTION=\"BOTH\" />\r\n"
            + "      </LaneType>\r\n"
            + "    </LaneTypes>\r\n"
            + "\r\n"
            + "    <GtuTemplates xmlns=\"http://www.opentrafficsim.org/ots\" "
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
            + "      xsi:schemaLocation=\"http://www.opentrafficsim.org/ots "
            + "../ots-definitions.xsd http://www.opentrafficsim.org/ots "
            + "https://opentrafficsim.org/docs/xsd/1.03.00/ots-definitions.xsd\">\r\n"
            + "      <GtuTemplate ID=\"CAR\" GTUTYPE=\"CAR\" DEFAULT=\"true\">\r\n"
            + "        <LengthDist LENGTHUNIT=\"m\">\r\n"
            + "          <CONSTANT C=\"4.19\" />\r\n"
            + "        </LengthDist>\r\n"
            + "        <WidthDist LENGTHUNIT=\"m\">\r\n"
            + "          <CONSTANT C=\"1.7\" />\r\n"
            + "        </WidthDist>\r\n"
            + "        <MaxSpeedDist SPEEDUNIT=\"km/h\">\r\n"
            + "          <CONSTANT C=\"180\" />\r\n"
            + "        </MaxSpeedDist>\r\n"
            + "      </GtuTemplate>\r\n"
            + "      <GtuTemplate ID=\"TRUCK\" GTUTYPE=\"TRUCK\" DEFAULT=\"true\">\r\n"
            + "        <LengthDist LENGTHUNIT=\"m\">\r\n"
            + "          <CONSTANT C=\"12.0\" />\r\n"
            + "        </LengthDist>\r\n"
            + "        <WidthDist LENGTHUNIT=\"m\">\r\n"
            + "          <CONSTANT C=\"2.55\" />\r\n"
            + "        </WidthDist>\r\n"
            + "        <MaxSpeedDist SPEEDUNIT=\"km/h\">\r\n"
            + "          <NORMAL MU=\"85.0\" SIGMA=\"2.5\" />\r\n"
            + "        </MaxSpeedDist>\r\n"
            + "      </GtuTemplate>\r\n"
            + "      <GtuTemplate ID=\"BUS\" GTUTYPE=\"BUS\" DEFAULT=\"true\">\r\n"
            + "        <LengthDist LENGTHUNIT=\"m\">\r\n"
            + "          <CONSTANT C=\"12.0\" />\r\n"
            + "        </LengthDist>\r\n"
            + "        <WidthDist LENGTHUNIT=\"m\">\r\n"
            + "          <CONSTANT C=\"2.55\" />\r\n"
            + "        </WidthDist>\r\n"
            + "        <MaxSpeedDist SPEEDUNIT=\"km/h\">\r\n"
            + "          <CONSTANT C=\"90\" />\r\n"
            + "        </MaxSpeedDist>\r\n"
            + "      </GtuTemplate>\r\n"
            + "      <GtuTemplate ID=\"VAN\" GTUTYPE=\"VAN\" DEFAULT=\"true\">\r\n"
            + "        <LengthDist LENGTHUNIT=\"m\">\r\n"
            + "          <CONSTANT C=\"5.0\" />\r\n"
            + "        </LengthDist>\r\n"
            + "        <WidthDist LENGTHUNIT=\"m\">\r\n"
            + "          <CONSTANT C=\"2.4\" />\r\n"
            + "        </WidthDist>\r\n"
            + "        <MaxSpeedDist SPEEDUNIT=\"km/h\">\r\n"
            + "          <CONSTANT C=\"180\" />\r\n"
            + "        </MaxSpeedDist>\r\n"
            + "      </GtuTemplate>\r\n"
            + "      <GtuTemplate ID=\"EMERGENCY_VEHICLE\" GTUTYPE=\"EMERGENCY_VEHICLE\" DEFAULT=\"true\">\r\n"
            + "        <LengthDist LENGTHUNIT=\"m\">\r\n"
            + "          <CONSTANT C=\"5.0\" />\r\n"
            + "        </LengthDist>\r\n"
            + "        <WidthDist LENGTHUNIT=\"m\">\r\n"
            + "          <CONSTANT C=\"2.4\" />\r\n"
            + "        </WidthDist>\r\n"
            + "        <MaxSpeedDist SPEEDUNIT=\"km/h\">\r\n"
            + "          <CONSTANT C=\"180\" />\r\n"
            + "        </MaxSpeedDist>\r\n"
            + "      </GtuTemplate>\r\n"
            + "    </GtuTemplates>\r\n"
            + "\r\n"
            + "    <LaneTypes>\r\n"
            + "      <LaneType ID=\"STREET\">\r\n"
            + "        <Compatibility GTUTYPE=\"ROAD_USER\" DIRECTION=\"FORWARD\" />\r\n"
            + "      </LaneType>\r\n"
            + "    </LaneTypes>\r\n"
            + "\r\n"
            + "    <GTUTYPES>\r\n"
            + "      <!-- <GTUTYPE PARENT=\"ROAD_USER\" ID=\"CAR\" /> <GTUTYPE PARENT=\"ROAD_USER\" ID=\"TRUCK\" /> -->\r\n"
            + "    </GTUTYPES>\r\n"
            + "\r\n"
            + "    <GtuTemplates>\r\n"
            + "      <GtuTemplate GTUTYPE=\"CAR\" ID=\"CARS\">\r\n"
            + "        <LengthDist LENGTHUNIT=\"m\">\r\n"
            + "          <UNIFORM MIN=\"4\" MAX=\"7\" />\r\n"
            + "        </LengthDist>\r\n"
            + "        <WidthDist LENGTHUNIT=\"m\">\r\n"
            + "          <UNIFORM MIN=\"1.7\" MAX=\"2\" />\r\n"
            + "        </WidthDist>\r\n"
            + "        <MaxSpeedDist SPEEDUNIT=\"km/h\">\r\n"
            + "          <CONSTANT C=\"120\" />\r\n"
            + "        </MaxSpeedDist>\r\n"
            + "      </GtuTemplate>\r\n"
            + "      <GtuTemplate GTUTYPE=\"TRUCK\" ID=\"TRUCKS\">\r\n"
            + "        <LengthDist LENGTHUNIT=\"m\">\r\n"
            + "          <UNIFORM MIN=\"16\" MAX=\"24\" />\r\n"
            + "        </LengthDist>\r\n"
            + "        <WidthDist LENGTHUNIT=\"m\">\r\n"
            + "          <UNIFORM MIN=\"2.2\" MAX=\"2.7\" />\r\n"
            + "        </WidthDist>\r\n"
            + "        <MaxSpeedDist SPEEDUNIT=\"km/h\">\r\n"
            + "          <CONSTANT C=\"100\" />\r\n"
            + "        </MaxSpeedDist>\r\n"
            + "\r\n"
            + "      </GtuTemplate>\r\n"
            + "    </GtuTemplates>\r\n"
            + "\r\n"
            + "    <LinkTypes>\r\n"
            + "      <LinkType ID=\"STREET\">\r\n"
            + "        <Compatibility GTUTYPE=\"ROAD_USER\" DIRECTION=\"FORWARD\" />\r\n"
            + "      </LinkType>\r\n"
            + "    </LinkTypes>\r\n"
            + "\r\n"
            + "    <RoadLayouts>\r\n"
            + "      <RoadLayout ID=\"r1\" LinkType=\"STREET\">\r\n"
            + "        <SHOULDER>\r\n"
            + "          <WIDTH>2m</WIDTH>\r\n"
            + "        </SHOULDER>\r\n"
            + "        <STRIPE TYPE=\"SOLID\">\r\n"
            + "        </STRIPE>\r\n"
            + "        <LANE ID=\"FORWARD3\" LaneType=\"STREET\" DESIGNDIRECTION=\"true\">\r\n"
            + "          <WIDTH>3.3m</WIDTH>\r\n"
            + "          <SPEEDLIMIT GTUTYPE=\"CAR\" LegalSpeedLimit=\"50 km/h\"></SPEEDLIMIT>\r\n"
            + "        </LANE>\r\n"
            + "        <STRIPE TYPE=\"DASHED\">\r\n"
            + "        </STRIPE>\r\n"
            + "        <LANE ID=\"FORWARD2\" LaneType=\"STREET\" DESIGNDIRECTION=\"true\">\r\n"
            + "          <CenterOffset>-6.2m</CenterOffset>\r\n"
            + "          <WIDTH>3.3m</WIDTH>\r\n"
            + "          <SPEEDLIMIT GTUTYPE=\"CAR\" LegalSpeedLimit=\"50 km/h\"></SPEEDLIMIT>\r\n"
            + "        </LANE>\r\n"
            + "        <STRIPE TYPE=\"DASHED\">\r\n"
            + "        </STRIPE>\r\n"
            + "        <LANE ID=\"FORWARD1\" LaneType=\"STREET\" DESIGNDIRECTION=\"true\">\r\n"
            + "          <WIDTH>3.3m</WIDTH>\r\n"
            + "          <SPEEDLIMIT GTUTYPE=\"CAR\" LegalSpeedLimit=\"50 km/h\"></SPEEDLIMIT>\r\n"
            + "        </LANE>\r\n"
            + "        <STRIPE TYPE=\"SOLID\">\r\n"
            + "        </STRIPE>\r\n"
            + "      </RoadLayout>\r\n"
            + "\r\n"
            + "      <RoadLayout ID=\"r1g\" LinkType=\"STREET\">\r\n"
            + "        <SHOULDER>\r\n"
            + "          <WIDTH>2m</WIDTH>\r\n"
            + "        </SHOULDER>\r\n"
            + "        <STRIPE TYPE=\"SOLID\">\r\n"
            + "        </STRIPE>\r\n"
            + "        <LANE ID=\"FORWARD3\" LaneType=\"STREET\" DESIGNDIRECTION=\"true\">\r\n"
            + "          <CenterOffsetStart>-6.2m</CenterOffsetStart>\r\n"
            + "          <CenterOffsetEnd>-9.5m</CenterOffsetEnd>\r\n"
            + "          <WIDTH>3.3m</WIDTH>\r\n"
            + "          <SPEEDLIMIT GTUTYPE=\"CAR\" LegalSpeedLimit=\"50 km/h\"></SPEEDLIMIT>\r\n"
            + "        </LANE>\r\n"
            + "        <STRIPE TYPE=\"DASHED\">\r\n"
            + "          <CenterOffset>-7.85m</CenterOffset>\r\n"
            + "        </STRIPE>\r\n"
            + "        <LANE ID=\"FORWARD2\" LaneType=\"STREET\" DESIGNDIRECTION=\"true\">\r\n"
            + "          <CenterOffset>-6.2m</CenterOffset>\r\n"
            + "          <WIDTH>3.3m</WIDTH>\r\n"
            + "          <SPEEDLIMIT GTUTYPE=\"CAR\" LegalSpeedLimit=\"50 km/h\"></SPEEDLIMIT>\r\n"
            + "        </LANE>\r\n"
            + "        <STRIPE TYPE=\"DASHED\">\r\n"
            + "          <CenterOffset>-4.55m</CenterOffset>\r\n"
            + "        </STRIPE>\r\n"
            + "        <LANE ID=\"FORWARD1\" LaneType=\"STREET\" DESIGNDIRECTION=\"true\">\r\n"
            + "          <CenterOffsetStart>-6.2m</CenterOffsetStart>\r\n"
            + "          <CenterOffsetEnd>-2.9m</CenterOffsetEnd>\r\n"
            + "          <WIDTH>3.3m</WIDTH>\r\n"
            + "          <SPEEDLIMIT GTUTYPE=\"CAR\" LegalSpeedLimit=\"50 km/h\"></SPEEDLIMIT>\r\n"
            + "        </LANE>\r\n"
            + "        <STRIPE TYPE=\"SOLID\">\r\n"
            + "        </STRIPE>\r\n"
            + "      </RoadLayout>\r\n"
            + "\r\n"
            + "      <RoadLayout ID=\"r2r\" LinkType=\"STREET\">\r\n"
            + "        <SHOULDER>\r\n"
            + "          <CenterOffset>-8.8m</CenterOffset>\r\n"
            + "          <WIDTH>2m</WIDTH>\r\n"
            + "        </SHOULDER>\r\n"
            + "        <STRIPE TYPE=\"SOLID\">\r\n"
            + "          <CenterOffset>-7.8m</CenterOffset>\r\n"
            + "        </STRIPE>\r\n"
            + "        <LANE ID=\"FORWARD\" LaneType=\"STREET\" DESIGNDIRECTION=\"true\">\r\n"
            + "          <CenterOffset>-6.2m</CenterOffset>\r\n"
            + "          <WIDTH>3.3m</WIDTH>\r\n"
            + "          <SPEEDLIMIT GTUTYPE=\"CAR\" LegalSpeedLimit=\"50 km/h\"></SPEEDLIMIT>\r\n"
            + "        </LANE>\r\n"
            + "        <STRIPE TYPE=\"SOLID\">\r\n"
            + "          <CenterOffset>-4.6m</CenterOffset>\r\n"
            + "        </STRIPE>\r\n"
            + "      </RoadLayout>\r\n"
            + "\r\n"
            + "      <RoadLayout ID=\"r2l\" LinkType=\"STREET\">\r\n"
            + "        <SHOULDER>\r\n"
            + "          <CenterOffset>-8.8m</CenterOffset>\r\n"
            + "          <WIDTH>2m</WIDTH>\r\n"
            + "        </SHOULDER>\r\n"
            + "        <STRIPE TYPE=\"SOLID\">\r\n"
            + "          <CenterOffset>-7.8m</CenterOffset>\r\n"
            + "        </STRIPE>\r\n"
            + "        <LANE ID=\"FORWARD\" LaneType=\"STREET\" DESIGNDIRECTION=\"true\">\r\n"
            + "          <CenterOffset>-6.2m</CenterOffset>\r\n"
            + "          <WIDTH>3.3m</WIDTH>\r\n"
            + "          <SPEEDLIMIT GTUTYPE=\"CAR\" LegalSpeedLimit=\"50 km/h\"></SPEEDLIMIT>\r\n"
            + "        </LANE>\r\n"
            + "        <STRIPE TYPE=\"SOLID\">\r\n"
            + "          <CenterOffset>-4.6m</CenterOffset>\r\n"
            + "        </STRIPE>\r\n"
            + "        <!-- <NoTrafficLane> <CenterOffset>0m</CenterOffset> <WIDTH>2.5m</WIDTH> </NoTrafficLane> -->\r\n"
            + "      </RoadLayout>\r\n"
            + "\r\n"
            + "      <RoadLayout ID=\"r3\" LinkType=\"STREET\">\r\n"
            + "        <STRIPE TYPE=\"SOLID\">\r\n"
            + "          <CenterOffset>-4.6m</CenterOffset>\r\n"
            + "        </STRIPE>\r\n"
            + "        <LANE ID=\"FORWARD\" LaneType=\"STREET\" DESIGNDIRECTION=\"true\">\r\n"
            + "          <CenterOffset>-6.2m</CenterOffset>\r\n"
            + "          <WIDTH>3.3m</WIDTH>\r\n"
            + "          <SPEEDLIMIT GTUTYPE=\"CAR\" LegalSpeedLimit=\"50 km/h\"></SPEEDLIMIT>\r\n"
            + "        </LANE>\r\n"
            + "        <STRIPE TYPE=\"SOLID\">\r\n"
            + "          <CenterOffset>-7.8m</CenterOffset>\r\n"
            + "        </STRIPE>\r\n"
            + "      </RoadLayout>\r\n"
            + "\r\n"
            + "      <RoadLayout ID=\"r4\" LinkType=\"STREET\">\r\n"
            + "        <!-- <NoTrafficLane> <CenterOffset>-2.3m</CenterOffset> <WIDTH>4.6m</WIDTH> </NoTrafficLane> -->\r\n"
            + "        <STRIPE TYPE=\"SOLID\">\r\n"
            + "          <CenterOffset>-4.6m</CenterOffset>\r\n"
            + "        </STRIPE>\r\n"
            + "        <LANE ID=\"FORWARD\" LaneType=\"STREET\" DESIGNDIRECTION=\"true\">\r\n"
            + "          <CenterOffset>-6.2m</CenterOffset>\r\n"
            + "          <WIDTH>3.3m</WIDTH>\r\n"
            + "          <SPEEDLIMIT GTUTYPE=\"CAR\" LegalSpeedLimit=\"50 km/h\"></SPEEDLIMIT>\r\n"
            + "        </LANE>\r\n"
            + "        <STRIPE TYPE=\"SOLID\">\r\n"
            + "          <CenterOffset>-7.8m</CenterOffset>\r\n"
            + "        </STRIPE>\r\n"
            + "        <SHOULDER>\r\n"
            + "          <CenterOffset>-8.8m</CenterOffset>\r\n"
            + "          <WIDTH>2m</WIDTH>\r\n"
            + "        </SHOULDER>\r\n"
            + "      </RoadLayout>\r\n"
            + "    </RoadLayouts>\r\n"
            + "  </Definitions>\r\n"
            + "  <Network>\r\n"
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
            + "      <Straight />\r\n"
            + "      <DefinedLayout>r2l</DefinedLayout>\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "    <LINK ID=\"N1N2\" NODESTART=\"N1\" NODEEND=\"N2\" TYPE=\"STREET\">\r\n"
            + "      <Straight />\r\n"
            + "      <DefinedLayout>r1g</DefinedLayout>\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "    <LINK ID=\"N2NC\" NODESTART=\"N2\" NODEEND=\"NC\" TYPE=\"STREET\">\r\n"
            + "      <Straight />\r\n"
            + "      <DefinedLayout>r1</DefinedLayout>\r\n"
            + "      <TRAFFICLIGHT LANE=\"FORWARD3\" CLASS=\"org.opentrafficsim.road.network.lane.object.trafficlight"
            + ".SimpleTrafficLight\" ID=\"10\" POSITION=\"END-0.00m\" />\r\n"
            + "      <TRAFFICLIGHT LANE=\"FORWARD2\" CLASS=\"org.opentrafficsim.road.network.lane.object.trafficlight"
            + ".SimpleTrafficLight\" ID=\"11\" POSITION=\"END-0.00m\" />\r\n"
            + "      <TRAFFICLIGHT LANE=\"FORWARD1\" CLASS=\"org.opentrafficsim.road.network.lane.object.trafficlight"
            + ".SimpleTrafficLight\" ID=\"12\" POSITION=\"END-0.00m\" />\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "    <LINK ID=\"EE1\" NODESTART=\"E\" NODEEND=\"E1\" TYPE=\"STREET\">\r\n"
            + "      <Straight />\r\n"
            + "      <DefinedLayout>r2l</DefinedLayout>\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "    <LINK ID=\"E1E2\" NODESTART=\"E1\" NODEEND=\"E2\" TYPE=\"STREET\">\r\n"
            + "      <Straight />\r\n"
            + "      <DefinedLayout>r1g</DefinedLayout>\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "    <LINK ID=\"E2EC\" NODESTART=\"E2\" NODEEND=\"EC\" TYPE=\"STREET\">\r\n"
            + "      <Straight />\r\n"
            + "      <DefinedLayout>r1</DefinedLayout>\r\n"
            + "      <TRAFFICLIGHT LANE=\"FORWARD3\" CLASS=\"org.opentrafficsim.road.network.lane.object.trafficlight"
            + ".SimpleTrafficLight\" ID=\"01\" POSITION=\"END-0.00m\" />\r\n"
            + "      <TRAFFICLIGHT LANE=\"FORWARD2\" CLASS=\"org.opentrafficsim.road.network.lane.object.trafficlight"
            + ".SimpleTrafficLight\" ID=\"02\" POSITION=\"END-0.00m\" />\r\n"
            + "      <TRAFFICLIGHT LANE=\"FORWARD1\" CLASS=\"org.opentrafficsim.road.network.lane.object.trafficlight"
            + ".SimpleTrafficLight\" ID=\"03\" POSITION=\"END-0.00m\" />\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "    <LINK ID=\"SS1\" NODESTART=\"S\" NODEEND=\"S1\" TYPE=\"STREET\">\r\n"
            + "      <Straight />\r\n"
            + "      <DefinedLayout>r2l</DefinedLayout>\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "    <LINK ID=\"S1S2\" NODESTART=\"S1\" NODEEND=\"S2\" TYPE=\"STREET\">\r\n"
            + "      <Straight />\r\n"
            + "      <DefinedLayout>r1g</DefinedLayout>\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "    <LINK ID=\"S2SC\" NODESTART=\"S2\" NODEEND=\"SC\" TYPE=\"STREET\">\r\n"
            + "      <Straight />\r\n"
            + "      <DefinedLayout>r1</DefinedLayout>\r\n"
            + "      <TRAFFICLIGHT LANE=\"FORWARD3\" CLASS=\"org.opentrafficsim.road.network.lane.object.trafficlight"
            + ".SimpleTrafficLight\" ID=\"04\" POSITION=\"END-0.00m\" />\r\n"
            + "      <TRAFFICLIGHT LANE=\"FORWARD2\" CLASS=\"org.opentrafficsim.road.network.lane.object.trafficlight"
            + ".SimpleTrafficLight\" ID=\"05\" POSITION=\"END-0.00m\" />\r\n"
            + "      <TRAFFICLIGHT LANE=\"FORWARD1\" CLASS=\"org.opentrafficsim.road.network.lane.object.trafficlight"
            + ".SimpleTrafficLight\" ID=\"06\" POSITION=\"END-0.00m\" />\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "    <LINK ID=\"WW1\" NODESTART=\"W\" NODEEND=\"W1\" TYPE=\"STREET\">\r\n"
            + "      <Straight />\r\n"
            + "      <DefinedLayout>r2l</DefinedLayout>\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "    <LINK ID=\"W1W2\" NODESTART=\"W1\" NODEEND=\"W2\" TYPE=\"STREET\">\r\n"
            + "      <Straight />\r\n"
            + "      <DefinedLayout>r1g</DefinedLayout>\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "    <LINK ID=\"W2WC\" NODESTART=\"W2\" NODEEND=\"WC\" TYPE=\"STREET\">\r\n"
            + "      <Straight />\r\n"
            + "      <DefinedLayout>r1</DefinedLayout>\r\n"
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
            + "      <DefinedLayout>r2r</DefinedLayout>\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "    <LINK ID=\"ECSC\" NODESTART=\"EC\" NODEEND=\"SCO\" TYPE=\"STREET\" OFFSETSTART=\"3.3m\" "
            + "OFFSETEND=\"0m\">\r\n"
            + "      <BEZIER SHAPE=\"1.0\" />\r\n"
            + "      <DefinedLayout>r2l</DefinedLayout>\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "    <LINK ID=\"NCWC\" NODESTART=\"NC\" NODEEND=\"WCO\" TYPE=\"STREET\" OFFSETSTART=\"-3.3m\" "
            + "OFFSETEND=\"0m\">\r\n"
            + "      <BEZIER SHAPE=\"0.5\" />\r\n"
            + "      <DefinedLayout>r2r</DefinedLayout>\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "    <LINK ID=\"NCEC\" NODESTART=\"NC\" NODEEND=\"ECO\" TYPE=\"STREET\" OFFSETSTART=\"3.3m\" "
            + "OFFSETEND=\"0m\">\r\n"
            + "      <BEZIER SHAPE=\"1.0\" />\r\n"
            + "      <DefinedLayout>r2l</DefinedLayout>\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "    <LINK ID=\"WCSC\" NODESTART=\"WC\" NODEEND=\"SCO\" TYPE=\"STREET\" OFFSETSTART=\"-3.3m\" "
            + "OFFSETEND=\"0m\">\r\n"
            + "      <BEZIER SHAPE=\"0.5\" />\r\n"
            + "      <DefinedLayout>r2l</DefinedLayout>\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "    <LINK ID=\"WCNC\" NODESTART=\"WC\" NODEEND=\"NCO\" TYPE=\"STREET\" OFFSETSTART=\"3.3m\" "
            + "OFFSETEND=\"0m\">\r\n"
            + "      <BEZIER SHAPE=\"1.0\" />\r\n"
            + "      <DefinedLayout>r2l</DefinedLayout>\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "    <LINK ID=\"SCEC\" NODESTART=\"SC\" NODEEND=\"ECO\" TYPE=\"STREET\" OFFSETSTART=\"-3.3m\" "
            + "OFFSETEND=\"0m\">\r\n"
            + "      <BEZIER SHAPE=\"0.5\" />\r\n"
            + "      <DefinedLayout>r2l</DefinedLayout>\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "    <LINK ID=\"SCWC\" NODESTART=\"SC\" NODEEND=\"WCO\" TYPE=\"STREET\" OFFSETSTART=\"3.3m\" "
            + "OFFSETEND=\"0m\">\r\n"
            + "      <BEZIER SHAPE=\"1.0\" />\r\n"
            + "      <DefinedLayout>r2l</DefinedLayout>\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "    <LINK ID=\"NCSC\" NODESTART=\"NC\" NODEEND=\"SCO\" TYPE=\"STREET\">\r\n"
            + "      <Straight />\r\n"
            + "      <DefinedLayout>r3</DefinedLayout>\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "    <LINK ID=\"WCEC\" NODESTART=\"WC\" NODEEND=\"ECO\" TYPE=\"STREET\">\r\n"
            + "      <Straight />\r\n"
            + "      <DefinedLayout>r3</DefinedLayout>\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "    <LINK ID=\"SCNC\" NODESTART=\"SC\" NODEEND=\"NCO\" TYPE=\"STREET\">\r\n"
            + "      <Straight />\r\n"
            + "      <DefinedLayout>r3</DefinedLayout>\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "    <LINK ID=\"ECWC\" NODESTART=\"EC\" NODEEND=\"WCO\" TYPE=\"STREET\">\r\n"
            + "      <Straight />\r\n"
            + "      <DefinedLayout>r3</DefinedLayout>\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "    <LINK ID=\"ECOEO\" NODESTART=\"ECO\" NODEEND=\"EO\" TYPE=\"STREET\">\r\n"
            + "      <Straight />\r\n"
            + "      <DefinedLayout>r4</DefinedLayout>\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "    <LINK ID=\"NCONO\" NODESTART=\"NCO\" NODEEND=\"NO\" TYPE=\"STREET\">\r\n"
            + "      <Straight />\r\n"
            + "      <DefinedLayout>r4</DefinedLayout>\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "    <LINK ID=\"WCOWO\" NODESTART=\"WCO\" NODEEND=\"WO\" TYPE=\"STREET\">\r\n"
            + "      <Straight />\r\n"
            + "      <DefinedLayout>r4</DefinedLayout>\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "    <LINK ID=\"SCOSO\" NODESTART=\"SCO\" NODEEND=\"SO\" TYPE=\"STREET\">\r\n"
            + "      <Straight />\r\n"
            + "      <DefinedLayout>r4</DefinedLayout>\r\n"
            + "    </LINK>\r\n"
            + "\r\n"
            + "  </Network>\r\n"
            + "  <Demand>\r\n"
            + "    <OD ID=\"Traffic Demand\" GlobalInterpolation=\"STEPWISE\" GlobalFactor=\"1.000\">\r\n"
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
            + "  </Demand>\r\n"
            + "  <MODEL />\r\n"
            + "  <SCENARIO />\r\n"
            + "  <RUN>\r\n"
            + "    <RUNLENGTH>3600s</RUNLENGTH>\r\n"
            + "    <NUMBERREPLICATIONS>1</NUMBERREPLICATIONS>\r\n"
            + "    <RandomStreams>\r\n"
            + "      <RandomStream ID=\"default\">\r\n"
            + "        <REPLICATION SEED=\"1\" ID=\"1\" />\r\n"
            + "      </RandomStream>\r\n"
            + "      <RandomStream ID=\"generation\">\r\n"
            + "        <REPLICATION SEED=\"1\" ID=\"1\" />\r\n"
            + "      </RandomStream>\r\n"
            + "    </RandomStreams>\r\n"
            + "  </RUN>\r\n"
            + "\r\n"
            + "</OTS>\r\n";
    // @formatter:on

    @Override
    public void setResetApplicationExecutable(final Runnable resetApplicationExecutable)
    {
    }

    @Override
    public Runnable getResetApplicationExecutable()
    {
        return null;
    }

    @Override
    public void resetApplication()
    {
    }

    @Override
    public void setInputParameterMap(final InputParameterMap inputParameterMap)
    {
    }

}
