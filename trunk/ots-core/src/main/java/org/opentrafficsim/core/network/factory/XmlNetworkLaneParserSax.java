package org.opentrafficsim.core.network.factory;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.naming.NamingException;
import javax.vecmath.Point3d;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.jstats.distributions.DistBeta;
import nl.tudelft.simulation.jstats.distributions.DistConstant;
import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistErlang;
import nl.tudelft.simulation.jstats.distributions.DistExponential;
import nl.tudelft.simulation.jstats.distributions.DistGamma;
import nl.tudelft.simulation.jstats.distributions.DistLogNormal;
import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.distributions.DistPearson5;
import nl.tudelft.simulation.jstats.distributions.DistPearson6;
import nl.tudelft.simulation.jstats.distributions.DistTriangular;
import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.distributions.DistWeibull;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.IDM;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.gtu.generator.GTUGeneratorIndividual;
import org.opentrafficsim.core.gtu.lane.LaneBlock;
import org.opentrafficsim.core.gtu.lane.changing.Altruistic;
import org.opentrafficsim.core.gtu.lane.changing.Egoistic;
import org.opentrafficsim.core.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.animation.LaneAnimation;
import org.opentrafficsim.core.network.animation.ShoulderAnimation;
import org.opentrafficsim.core.network.animation.StripeAnimation;
import org.opentrafficsim.core.network.lane.CrossSectionElement;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.core.network.lane.NoTrafficLane;
import org.opentrafficsim.core.network.lane.Shoulder;
import org.opentrafficsim.core.network.lane.SinkLane;
import org.opentrafficsim.core.network.lane.Stripe;
import org.opentrafficsim.core.network.lane.Stripe.Permeable;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.core.network.route.FixedLaneBasedRouteGenerator;
import org.opentrafficsim.core.network.route.LaneBasedRouteGenerator;
import org.opentrafficsim.core.unit.AnglePlaneUnit;
import org.opentrafficsim.core.unit.AngleSlopeUnit;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.LinearDensityUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DistContinuousDoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Parse an XML string with a simple representation of a lane-based network. An example of such a network is:
 * 
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;NETWORK xmlns="http://www.opentrafficsim.org/ots-infra" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *   xsi:schemaLocation="http://www.opentrafficsim.org/ots-infra ots-infra.xsd"&gt;
 * 
 *   &lt;GLOBAL WIDTH="3.6m" SPEED="100km/h" /&gt;
 * 
 *   &lt;GTU NAME="CAR" GTUTYPE="CAR" FOLLOWING="IDM+" LENGTH="UNIF(5,7) m" WIDTH="UNIF(1.7, 2) m" 
 *     LANECHANGE="EGOISTIC" MAXSPEED="CONST(120) km/h" /&gt;
 *   &lt;GTU NAME="TRUCK" GTUTYPE="TRUCK" FOLLOWING="IDM+" LENGTH="UNIF(16,24) m" WIDTH="UNIF(2.2, 2.7) m" 
 *     LANECHANGE="ALTRUISTIC" MAXSPEED="CONST(100) km/h" /&gt;
 * 
 *   &lt;GTUMIX NAME="C60T40"&gt;
 *     &lt;GTU NAME="CAR" WEIGHT="60"&gt;&lt;/GTU&gt;
 *     &lt;GTU NAME="TRUCK" WEIGHT="40"&gt;&lt;/GTU&gt;
 *   &lt;/GTUMIX&gt;
 * 
 *   &lt;NODE NAME="N1" COORDINATE="(0.0, 0.0)" ANGLE="90" /&gt;
 *   &lt;NODE NAME="N2" /&gt;
 *   &lt;NODE NAME="NE" /&gt;
 * 
 *   &lt;LINK NAME="L1" FROM="N1" TO="N2" ELEMENTS="S1-X1|A1:D:A2|X2-S2"&gt;
 *     &lt;ARC RADIUS="1000m" ANGLE="180" DIRECTION="L" /&gt;
 *     &lt;LANE NAME="X1" WIDTH="2.5m" /&gt;
 *     &lt;LANE NAME="X2" WIDTH="2.5m" /&gt;
 *     &lt;LANE NAME="S1" WIDTH="2m" /&gt;
 *     &lt;LANE NAME="S2" WIDTH="2m" /&gt;
 *   &lt;/LINK&gt;
 * 
 *   &lt;LINK NAME="L2" FROM="N2" TO="N1" ELEMENTS="S1-X1|A1:D:A2|X2-S2"&gt;
 *     &lt;ARC RADIUS="1000m" ANGLE="180" DIRECTION="L" /&gt;
 *     &lt;LANE NAME="X1" WIDTH="2.5m" /&gt;
 *     &lt;LANE NAME="X2" WIDTH="2.5m" /&gt;
 *     &lt;LANE NAME="S1" WIDTH="2m" /&gt;
 *     &lt;LANE NAME="S2" WIDTH="2m" /&gt;
 *   &lt;/LINK&gt;
 * 
 *   &lt;GTUMIX NAME="C80T20"&gt;
 *     &lt;GTU NAME="CAR" WEIGHT="80" /&gt;
 *     &lt;GTU NAME="TRUCK" WEIGHT="20" /&gt;
 *   &lt;/GTUMIX&gt;
 * 
 *   &lt;LINK NAME="ENTRY" FROM="NE" TO="N1" ELEMENTS="S1-X1|A1:D:A2|X2-S2"&gt;
 *     &lt;STRAIGHT LENGTH="200m" /&gt;
 *     &lt;LANE NAME="A1" SPEED="100 km/h" /&gt;
 *     &lt;LANE NAME="X1" WIDTH="2.5m" /&gt;
 *     &lt;LANE NAME="X2" WIDTH="2.5m" /&gt;
 *     &lt;LANE NAME="S1" WIDTH="2m" /&gt;
 *     &lt;LANE NAME="S2" WIDTH="2m" /&gt;
 *     &lt;GENERATOR LANE="A1" IAT="EXPO(30) s" INITIALSPEED="TRIA(80,90,100) km/h" MAXGTU="50" GTUMIX="C60T40" /&gt;
 *     &lt;GENERATOR LANE="A2" IAT="EXPO(45) s" INITIALSPEED="TRIA(80,90,100) km/h" MAXGTU="25" GTUMIX="C80T20" /&gt;
 *   &lt;/LINK&gt;
 * 
 * &lt;/NETWORK&gt;
 * 
 * </pre>
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Feb 6, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
@SuppressWarnings({"checkstyle:methodlength", "checkstyle:filelength"})
public class XmlNetworkLaneParserSax
{
    /** the ID class of the Network. */
    @SuppressWarnings("visibilitymodifier")
    protected final Class<?> networkIdClass;

    /** the ID class of the Node. */
    @SuppressWarnings("visibilitymodifier")
    protected final Class<?> nodeIdClass;

    /** the ID class of the Link. */
    @SuppressWarnings("visibilitymodifier")
    protected final Class<?> linkIdClass;

    /** the generated network. */
    @SuppressWarnings("rawtypes")
    private OTSNetwork network;

    /** the angle units. */
    private static final Map<String, AnglePlaneUnit> ANGLE_UNITS = new HashMap<>();

    /** the speed units. */
    private static final Map<String, SpeedUnit> SPEED_UNITS = new HashMap<>();

    /** the length units. */
    private static final Map<String, LengthUnit> LENGTH_UNITS = new HashMap<>();

    /** the time units. */
    private static final Map<String, TimeUnit> TIME_UNITS = new HashMap<>();

    /** the per-length units. */
    private static final Map<String, LinearDensityUnit> PER_LENGTH_UNITS = new HashMap<>();

    /** the time units. */
    private static final Map<String, FrequencyUnit> PER_TIME_UNITS = new HashMap<>();

    /** the processed nodes for further reference. */
    @SuppressWarnings({"rawtypes", "visibilitymodifier"})
    protected Map<String, Node> nodes = new HashMap<>();

    /** the UNprocessed nodes for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, NodeTag> nodeTags = new HashMap<>();

    /** the links for further reference. */
    @SuppressWarnings({"rawtypes", "visibilitymodifier"})
    protected Map<String, Link> links = new HashMap<>();

    /** the gtu tags for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, GTUTag> gtuTags = new HashMap<>();

    /** the gtumix tags for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, GTUMixTag> gtuMixTags = new HashMap<>();

    /** the route tags for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, RouteTag> routeTags = new HashMap<>();

    /** the route mix tags for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, RouteMixTag> routeMixTags = new HashMap<>();

    /** the shortest route tags for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, ShortestRouteTag> shortestRouteTags = new HashMap<>();

    /** the shortest route mix tags for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, ShortestRouteMixTag> shortestRouteMixTags = new HashMap<>();

    /** the road type tags for further reference. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, RoadTypeTag> roadTypeTags = new HashMap<>();

    /** the GTUTypes that have been created. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, GTUType<String>> gtuTypes = new HashMap<>();

    /** the LaneTypes that have been created. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, LaneType<String>> laneTypes = new HashMap<>();

    /** the no traffic LaneType. */
    @SuppressWarnings("visibilitymodifier")
    protected LaneType<String> noTrafficLaneType = new LaneType<>("NOTRAFFIC");

    /** the simulator for creating the animation. Null if no animation needed. */
    @SuppressWarnings("visibilitymodifier")
    protected OTSDEVSSimulatorInterface simulator;

    /** the GTUColorer to use. */
    @SuppressWarnings("visibilitymodifier")
    protected final GTUColorer gtuColorer;

    /** the phase of parsing. 0 = parse nodes; 1 = parse rest of the network. */
    @SuppressWarnings("visibilitymodifier")
    protected int phase = 0;

    /** DEBUG information or not. */
    private static final boolean DEBUG = true;

    static
    {
        ANGLE_UNITS.put("deg", AnglePlaneUnit.DEGREE);
        ANGLE_UNITS.put("rad", AnglePlaneUnit.RADIAN);

        SPEED_UNITS.put("km/h", SpeedUnit.KM_PER_HOUR);
        SPEED_UNITS.put("mi/h", SpeedUnit.MILE_PER_HOUR);
        SPEED_UNITS.put("m/s", SpeedUnit.METER_PER_SECOND);
        SPEED_UNITS.put("ft/s", SpeedUnit.FOOT_PER_SECOND);

        LENGTH_UNITS.put("mm", LengthUnit.MILLIMETER);
        LENGTH_UNITS.put("cm", LengthUnit.CENTIMETER);
        LENGTH_UNITS.put("dm", LengthUnit.DECIMETER);
        LENGTH_UNITS.put("dam", LengthUnit.DEKAMETER);
        LENGTH_UNITS.put("hm", LengthUnit.HECTOMETER);
        LENGTH_UNITS.put("m", LengthUnit.METER);
        LENGTH_UNITS.put("km", LengthUnit.KILOMETER);
        LENGTH_UNITS.put("mi", LengthUnit.MILE);
        LENGTH_UNITS.put("y", LengthUnit.YARD);
        LENGTH_UNITS.put("ft", LengthUnit.FOOT);

        TIME_UNITS.put("ms", TimeUnit.MILLISECOND);
        TIME_UNITS.put("s", TimeUnit.SECOND);
        TIME_UNITS.put("m", TimeUnit.MINUTE);
        TIME_UNITS.put("min", TimeUnit.MINUTE);
        TIME_UNITS.put("h", TimeUnit.HOUR);
        TIME_UNITS.put("hr", TimeUnit.HOUR);
        TIME_UNITS.put("d", TimeUnit.DAY);
        TIME_UNITS.put("day", TimeUnit.DAY);
        TIME_UNITS.put("wk", TimeUnit.WEEK);
        TIME_UNITS.put("week", TimeUnit.WEEK);

        PER_LENGTH_UNITS.put("/mm", LinearDensityUnit.PER_MILLIMETER);
        PER_LENGTH_UNITS.put("/cm", LinearDensityUnit.PER_CENTIMETER);
        PER_LENGTH_UNITS.put("/dm", LinearDensityUnit.PER_DECIMETER);
        PER_LENGTH_UNITS.put("/dam", LinearDensityUnit.PER_DEKAMETER);
        PER_LENGTH_UNITS.put("/hm", LinearDensityUnit.PER_HECTOMETER);
        PER_LENGTH_UNITS.put("/m", LinearDensityUnit.PER_METER);
        PER_LENGTH_UNITS.put("/km", LinearDensityUnit.PER_KILOMETER);
        PER_LENGTH_UNITS.put("/mi", LinearDensityUnit.PER_MILE);
        PER_LENGTH_UNITS.put("/y", LinearDensityUnit.PER_YARD);
        PER_LENGTH_UNITS.put("/ft", LinearDensityUnit.PER_FOOT);

        PER_TIME_UNITS.put("/ms", FrequencyUnit.PER_MILLISECOND);
        PER_TIME_UNITS.put("/s", FrequencyUnit.PER_SECOND);
        PER_TIME_UNITS.put("/m", FrequencyUnit.PER_MINUTE);
        PER_TIME_UNITS.put("/min", FrequencyUnit.PER_MINUTE);
        PER_TIME_UNITS.put("/h", FrequencyUnit.PER_HOUR);
        PER_TIME_UNITS.put("/hr", FrequencyUnit.PER_HOUR);
        PER_TIME_UNITS.put("/d", FrequencyUnit.PER_DAY);
        PER_TIME_UNITS.put("/day", FrequencyUnit.PER_DAY);
        PER_TIME_UNITS.put("/wk", FrequencyUnit.PER_WEEK);
        PER_TIME_UNITS.put("/week", FrequencyUnit.PER_WEEK);
    }

    /**
     * @param networkIdClass the ID class of the Network.
     * @param nodeIdClass the ID class of the Node.
     * @param linkIdClass the ID class of the Link.
     * @param gtuColorer the GTUColorer to use
     * @param simulator the simulator for creating the animation. Null if no animation needed.
     */
    public XmlNetworkLaneParserSax(final Class<?> networkIdClass, final Class<?> nodeIdClass, final Class<?> linkIdClass,
        final OTSDEVSSimulatorInterface simulator, final GTUColorer gtuColorer)
    {
        this.networkIdClass = networkIdClass;
        this.nodeIdClass = nodeIdClass;
        this.linkIdClass = linkIdClass;
        this.simulator = simulator;
        this.gtuColorer = gtuColorer;
        this.laneTypes.put(this.noTrafficLaneType.getId(), this.noTrafficLaneType);
    }

    /**
     * @param url the file with the network in the agreed xml-grammar.
     * @return the network with Nodes, Links, and Lanes.
     * @throws NetworkException in case of parsing problems.
     * @throws SAXException in case of parsing problems.
     * @throws ParserConfigurationException in case of parsing problems.
     * @throws IOException in case of file reading problems.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public final OTSNetwork build(final URL url) throws NetworkException, ParserConfigurationException, SAXException,
        IOException
    {
        // parse the Includes and Nodes.
        this.phase = 0;
        SAXParserFactory parserFactor = SAXParserFactory.newInstance();
        SAXParser parser = parserFactor.newSAXParser();
        SAXHandler handler = new SAXHandler();
        parser.parse(url.openStream(), handler);

        // parse the rest of the Network.
        this.phase = 1;
        parserFactor = SAXParserFactory.newInstance();
        parser = parserFactor.newSAXParser();
        handler = new SAXHandler();
        parser.parse(url.openStream(), handler);

        this.network = new OTSNetwork(url.toString());
        for (Node node : this.nodes.values())
        {
            this.network.addNode(node);
        }
        for (Link link : this.links.values())
        {
            this.network.addLink(link);
        }
        // TODO Routes
        return this.network;
    }

    /**
     * @param s the string with the network in the agreed xml-grammar.
     * @return the network with Nodes, Links, and Lanes.
     * @throws NetworkException in case of parsing problems.
     * @throws SAXException in case of parsing problems.
     * @throws ParserConfigurationException in case of parsing problems.
     * @throws IOException in case of file reading problems.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public final OTSNetwork build(final String s) throws NetworkException, ParserConfigurationException, SAXException,
        IOException
    {
        // parse the Includes and Nodes.
        this.phase = 0;
        SAXParserFactory parserFactor = SAXParserFactory.newInstance();
        SAXParser parser = parserFactor.newSAXParser();
        SAXHandler handler = new SAXHandler();
        ByteArrayInputStream bais = new ByteArrayInputStream(s.getBytes());
        parser.parse(bais, handler);

        // parse the rest of the Network.
        this.phase = 1;
        parserFactor = SAXParserFactory.newInstance();
        parser = parserFactor.newSAXParser();
        handler = new SAXHandler();
        bais.reset();
        parser.parse(bais, handler);

        this.network = new OTSNetwork(UUID.randomUUID());
        for (Node node : this.nodes.values())
        {
            this.network.addNode(node);
        }
        for (Link link : this.links.values())
        {
            this.network.addLink(link);
        }
        // TODO Routes
        return this.network;
    }

    /**
     * The Handler for SAX Events.
     */
    class SAXHandler extends DefaultHandler
    {
        /** local storage. */
        // private String content = null;

        /** depth list. */
        private Deque<String> stack = new ArrayDeque<String>();

        /** global values from the GLOBAL tag. */
        private GlobalTag globalTag;

        /** current LINK tag being parsed. */
        private LinkTag linkTag;

        /** current NODE tag being parsed. */
        private NodeTag nodeTag;

        /** current ROADTYPE tag being parsed. */
        private RoadTypeTag roadTypeTag;

        /** current GTUMIX tag being parsed. */
        private GTUMixTag gtuMixTag;

        /** current ROUTEMIX tag being parsed. */
        private RouteMixTag routeMixTag;

        /** current SHORTESTROUTEMIX tag being parsed. */
        private ShortestRouteMixTag shortestRouteMixTag;

        @Override
        @SuppressWarnings("checkstyle:methodlength")
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
            throws SAXException
        {
            try
            {
                // phase = 0 means: parse the includes and the node names.
                if (XmlNetworkLaneParserSax.this.phase == 0)
                {
                    if (qName.equals("NODE") && this.stack.size() > 0 && this.stack.getLast().equals("NETWORK"))
                    {
                        NodeTag nt = new NodeTag();
                        nt.name = attributes.getValue("NAME");
                        XmlNetworkLaneParserSax.this.nodeTags.put(nt.name.trim(), nt);
                    }

                    if (qName.equals("INCLUDE") && this.stack.size() > 0 && this.stack.getLast().equals("NETWORK"))
                    {
                        String name = attributes.getValue("FILE");
                        URI includeURI = new URI(name);
                        XmlNetworkLaneParserSax includeParser =
                            new XmlNetworkLaneParserSax(XmlNetworkLaneParserSax.this.networkIdClass,
                                XmlNetworkLaneParserSax.this.nodeIdClass, XmlNetworkLaneParserSax.this.linkIdClass,
                                XmlNetworkLaneParserSax.this.simulator, XmlNetworkLaneParserSax.this.gtuColorer);
                        includeParser.build(includeURI.toURL());
                        transferIncludes(includeParser);
                    }
                }

                else

                // phase = 1 means: parse everything, except the includes.
                {
                    if (DEBUG)
                    {
                        String name = attributes.getValue("NAME") == null ? "" : attributes.getValue("NAME").trim();
                        System.out.println("          ".substring(0, this.stack.size()) + qName + " " + name);
                    }

                    if (!qName.equals("NETWORK"))
                    {
                        switch (this.stack.getLast())
                        {
                            case "NETWORK":
                                switch (qName)
                                {
                                    case "GLOBAL":
                                        parseGlobalTag(attributes);
                                        break;

                                    case "INCLUDE":
                                        break;

                                    case "COMPATIBILITY":
                                        break;

                                    case "NODE":
                                        parseNodeTag(attributes);
                                        break;

                                    case "LINK":
                                        parseLinkTag(attributes);
                                        break;

                                    case "GTU":
                                        parseGTUTag(attributes);
                                        break;

                                    case "GTUMIX":
                                        parseGTUMixTag(attributes);
                                        break;

                                    case "ROUTE":
                                        parseRouteTag(attributes);
                                        break;

                                    case "ROADTYPE":
                                        parseRoadTypeTag(attributes);
                                        break;

                                    case "ROUTEMIX":
                                        parseRouteMixTag(attributes);
                                        break;

                                    case "SHORTESTROUTE":
                                        parseShortestRouteTag(attributes);
                                        break;

                                    case "SHORTESTROUTEMIX":
                                        parseShortestRouteMixTag(attributes);
                                        break;

                                    default:
                                        throw new SAXException("NETWORK: Received start tag " + qName
                                            + ", but stack contains: " + this.stack);
                                }
                                break;

                            case "COMPATIBILITY":
                                switch (qName)
                                {
                                    case "LANETYPE":
                                        parseLaneTypeTag(attributes);
                                        break;
                                    default:
                                        throw new SAXException("COMPATIBILITY: Received end tag " + qName
                                            + ", but stack contains: " + this.stack);
                                }
                                break;

                            case "LINK":
                                switch (qName)
                                {
                                    case "STRAIGHT":
                                        parseStraightTag(attributes);
                                        break;

                                    case "ARC":
                                        parseArcTag(attributes);
                                        break;

                                    case "LANEOVERRIDE":
                                        parseLaneOverrideTag(attributes);
                                        break;

                                    case "ROADTYPE":
                                        parseRoadTypeTag(attributes);
                                        break;

                                    case "GENERATOR":
                                        parseGeneratorTag(attributes);
                                        break;

                                    case "SINK":
                                        parseSinkTag(attributes);
                                        break;

                                    case "LISTGENERATOR":
                                        parseListGeneratorTag(attributes);
                                        break;

                                    case "BLOCK":
                                        parseBlockTag(attributes);
                                        break;

                                    case "FILL":
                                        parseFillTag(attributes);
                                        break;

                                    default:
                                        throw new SAXException("LINK: Received start tag " + qName
                                            + ", but stack contains: " + this.stack);
                                }
                                break;

                            case "ROADTYPE":
                                switch (qName)
                                {
                                    case "LANE":
                                        parseRoadTypeLaneTag(attributes);
                                        break;

                                    case "NOTRAFFICLANE":
                                        parseRoadTypeNoTrafficLaneTag(attributes);
                                        break;

                                    case "SHOULDER":
                                        parseRoadTypeShoulderTag(attributes);
                                        break;

                                    case "STRIPE":
                                        parseRoadTypeStripeTag(attributes);
                                        break;

                                    default:
                                        throw new SAXException("ROADTYPE: Received start tag " + qName
                                            + ", but stack contains: " + this.stack);
                                }
                                break;

                            case "GTUMIX":
                                switch (qName)
                                {
                                    case "GTU":
                                        parseGTUMixGTUTag(attributes);
                                        break;

                                    default:
                                        throw new SAXException("GTUMIX: Received start tag " + qName
                                            + ", but stack contains: " + this.stack);
                                }
                                break;

                            case "ROUTEMIX":
                                switch (qName)
                                {
                                    case "ROUTE":
                                        parseRouteMixRouteTag(attributes);
                                        break;

                                    default:
                                        throw new SAXException("ROUTEMIX: Received start tag " + qName
                                            + ", but stack contains: " + this.stack);
                                }
                                break;

                            case "SHORTESTROUTEMIX":
                                switch (qName)
                                {
                                    case "SHORTESTROUTE":
                                        parseShortestRouteMixShortestRouteTag(attributes);
                                        break;

                                    default:
                                        throw new SAXException("SHORTESTROUTEMIX: Received start tag " + qName
                                            + ", but stack contains: " + this.stack);
                                }
                                break;

                            default:
                                throw new SAXException("startElement: Received start tag " + qName
                                    + ", but stack contains: " + this.stack);
                        }
                    }
                }
                this.stack.addLast(qName);
            }
            catch (Exception e)
            {
                throw new SAXException(e);
            }
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException
        {
            if (!this.stack.getLast().equals(qName))
            {
                throw new SAXException("endElement: Received /" + qName + ", but stack contains: " + this.stack);
            }
            this.stack.removeLast();

            if (XmlNetworkLaneParserSax.this.phase == 0)
            {
                return;
            }

            try
            {
                if (DEBUG)
                {
                    System.out.println("          ".substring(0, this.stack.size()) + "/" + qName);
                }

                if (!qName.equals("NETWORK"))
                {
                    switch (this.stack.getLast())
                    {
                        case "NETWORK":
                            switch (qName)
                            {
                                case "GLOBAL":
                                    break;

                                case "INCLUDE":
                                    break;

                                case "COMPATIBILITY":
                                    break;

                                case "NODE":
                                    XmlNetworkLaneParserSax.this.nodeTags.put(this.nodeTag.name, this.nodeTag);
                                    if (this.nodeTag.coordinate != null)
                                    {
                                        // only make a node if we know the coordinate. Otherwise, wait till we can
                                        // calculate it.
                                        makeNode(this.nodeTag);
                                    }
                                    this.nodeTag = null;
                                    break;

                                case "LINK":
                                    if (this.linkTag.roadTypeTag == null)
                                    {
                                        throw new SAXException("LINK: " + this.linkTag.name
                                            + " does not have a ROADTYPE defined");
                                    }
                                    calculateNodeCoordinates(this.linkTag);
                                    @SuppressWarnings("rawtypes")
                                    CrossSectionLink link = makeLink(this.linkTag);
                                    applyRoadTypeToLink(this.linkTag.roadTypeTag, link, this.linkTag, this.globalTag);
                                    XmlNetworkLaneParserSax.this.links.put(link.getId().toString(), link);
                                    this.linkTag = null;
                                    break;

                                case "GTU":
                                    break;

                                case "GTUMIX":
                                    this.gtuMixTag = null;
                                    break;

                                case "ROADTYPE":
                                    calculateRoadTypeOffsets(this.roadTypeTag, this.globalTag);
                                    XmlNetworkLaneParserSax.this.roadTypeTags.put(this.roadTypeTag.name, this.roadTypeTag);
                                    break;

                                case "ROUTE":
                                    break;

                                case "ROUTEMIX":
                                    this.routeMixTag = null;
                                    break;

                                case "SHORTESTROUTE":
                                    break;

                                case "SHORTESTROUTEMIX":
                                    this.shortestRouteMixTag = null;
                                    break;

                                default:
                                    throw new SAXException("NETWORK: Received end tag " + qName + ", but stack contains: "
                                        + this.stack);
                            }
                            break;

                        case "COMPATIBILITY":
                            switch (qName)
                            {
                                case "LANETYPE":
                                    break;
                                default:
                                    throw new SAXException("COMPATIBILITY: Received end tag " + qName
                                        + ", but stack contains: " + this.stack);
                            }
                            break;

                        case "LINK":
                            switch (qName)
                            {
                                case "STRAIGHT":
                                    break;
                                case "ARC":
                                    break;
                                case "ROADTYPE":
                                    this.linkTag.roadTypeTag = this.roadTypeTag;
                                    this.roadTypeTag = null;
                                    break;
                                case "LANEOVERRIDE":
                                    break;
                                case "GENERATOR":
                                    break;
                                case "SINK":
                                    break;
                                case "LISTGENERATOR":
                                    break;
                                case "BLOCK":
                                    break;
                                case "FILL":
                                    break;
                                default:
                                    throw new SAXException("LINK: Received end tag " + qName + ", but stack contains: "
                                        + this.stack);
                            }
                            break;

                        case "GTUMIX":
                            switch (qName)
                            {
                                case "GTU":
                                    break;
                                default:
                                    throw new SAXException("GTUMIX: Received end tag " + qName + ", but stack contains: "
                                        + this.stack);
                            }
                            break;

                        case "ROADTYPE":
                            switch (qName)
                            {
                                case "LANE":
                                    break;
                                case "NOTRAFFICLANE":
                                    break;
                                case "SHOULDER":
                                    break;
                                case "STRIPE":
                                    break;
                                default:
                                    throw new SAXException("ROADTYPE: Received end tag " + qName + ", but stack contains: "
                                        + this.stack);
                            }
                            break;

                        case "ROUTEMIX":
                            switch (qName)
                            {
                                case "ROUTE":
                                    break;
                                default:
                                    throw new SAXException("ROUTEMIX: Received end tag " + qName + ", but stack contains: "
                                        + this.stack);
                            }
                            break;

                        case "SHORTESTROUTEMIX":
                            switch (qName)
                            {
                                case "SHORTESTROUTE":
                                    break;
                                default:
                                    throw new SAXException("SHORTESTROUTEMIX: Received end tag " + qName
                                        + ", but stack contains: " + this.stack);
                            }
                            break;

                        default:
                            throw new SAXException("Received end tag " + qName + ", but stack contains: " + this.stack);
                    }
                }
            }
            catch (Exception e)
            {
                throw new SAXException(e);
            }
        }

        /**
         * Parse the GLOBAL tag with global values.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseGlobalTag(final Attributes attributes) throws NetworkException
        {
            this.globalTag = new GlobalTag();
            if (attributes.getValue("SPEED") != null)
                this.globalTag.speed = parseSpeedAbs(attributes.getValue("SPEED").trim());
            if (attributes.getValue("WIDTH") != null)
                this.globalTag.width = parseLengthRel(attributes.getValue("WIDTH").trim());
        }

        /**
         * Transfer the include tags from the include line to this XmlParser.
         * @param includeParser the parser that has the include tags.
         */
        private void transferIncludes(final XmlNetworkLaneParserSax includeParser)
        {
            XmlNetworkLaneParserSax.this.gtuMixTags.putAll(includeParser.gtuMixTags);
            XmlNetworkLaneParserSax.this.gtuTags.putAll(includeParser.gtuTags);
            XmlNetworkLaneParserSax.this.gtuTypes.putAll(includeParser.gtuTypes);
            XmlNetworkLaneParserSax.this.links.putAll(includeParser.links);
            XmlNetworkLaneParserSax.this.laneTypes.putAll(includeParser.laneTypes);
            XmlNetworkLaneParserSax.this.nodes.putAll(includeParser.nodes);
            XmlNetworkLaneParserSax.this.nodeTags.putAll(includeParser.nodeTags);
            XmlNetworkLaneParserSax.this.roadTypeTags.putAll(includeParser.roadTypeTags);
            XmlNetworkLaneParserSax.this.routeMixTags.putAll(includeParser.routeMixTags);
            XmlNetworkLaneParserSax.this.routeTags.putAll(includeParser.routeTags);
            XmlNetworkLaneParserSax.this.shortestRouteMixTags.putAll(includeParser.shortestRouteMixTags);
            XmlNetworkLaneParserSax.this.shortestRouteTags.putAll(includeParser.shortestRouteTags);
        }

        /**
         * Parse the COMPATIBILITY.LANETYPE tag with laneType - GTUType compatibility.
         * @param attributes the attributes of the XML-tag.
         * @throws SAXException in case of missing tags.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseLaneTypeTag(final Attributes attributes) throws SAXException
        {
            LaneTypeTag laneTypeTag = new LaneTypeTag();
            if (attributes.getValue("NAME") == null)
                throw new SAXException("COMPATIBILITY.LANETYPE: missing attribute NAME");
            laneTypeTag.laneType = attributes.getValue("NAME");
            if (XmlNetworkLaneParserSax.this.laneTypes.keySet().contains(laneTypeTag.laneType))
                throw new SAXException("COMPATIBILITY.LANETYPE: NAME " + laneTypeTag.laneType + " defined twice");

            if (attributes.getValue("GTULIST") == null)
                throw new SAXException("COMPATIBILITY.LANETYPE: missing attribute GTULIST for LANETYPE="
                    + laneTypeTag.laneType);
            laneTypeTag.gtuList = parseGTUList(attributes.getValue("GTULIST"));

            addLaneType(laneTypeTag);
        }

        /**
         * Parse the NODE tag with attributes of a Node.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseNodeTag(final Attributes attributes) throws NetworkException, SAXException
        {
            this.nodeTag = new NodeTag();

            String name = attributes.getValue("NAME");
            if (name == null)
                throw new SAXException("NODE: missing attribute NAME");
            this.nodeTag.name = name.trim();
            if (XmlNetworkLaneParserSax.this.nodes.keySet().contains(this.nodeTag.name))
                throw new SAXException("NODE: NAME " + this.nodeTag.name + " defined twice");

            if (attributes.getValue("COORDINATE") != null)
                this.nodeTag.coordinate = parseCoordinate(attributes.getValue("COORDINATE"));

            if (attributes.getValue("ANGLE") != null)
                this.nodeTag.angle = parseAngleAbs(attributes.getValue("ANGLE"));
        }

        /**
         * Parse the LINK tag with attributes of a Link.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseLinkTag(final Attributes attributes) throws NetworkException, SAXException
        {
            this.linkTag = new LinkTag();

            String name = attributes.getValue("NAME");
            if (name == null)
                throw new SAXException("LINK: missing attribute NAME");
            this.linkTag.name = name.trim();
            if (XmlNetworkLaneParserSax.this.links.keySet().contains(this.linkTag.name))
                throw new SAXException("LINK: NAME " + this.linkTag.name + " defined twice");

            String roadTypeName = attributes.getValue("ROADTYPE");
            if (!XmlNetworkLaneParserSax.this.roadTypeTags.containsKey(roadTypeName))
                throw new SAXException("LINK: ROADTYPE " + roadTypeName + " not found for link " + name);
            this.linkTag.roadTypeTag = XmlNetworkLaneParserSax.this.roadTypeTags.get(roadTypeName);

            String fromNodeStr = attributes.getValue("FROM");
            if (fromNodeStr == null)
                throw new SAXException("NODE: missing attribute FROM for link " + name);
            this.linkTag.nodeFromName = fromNodeStr.trim();
            @SuppressWarnings("rawtypes")
            Node fromNode = XmlNetworkLaneParserSax.this.nodes.get(fromNodeStr.trim());
            this.linkTag.nodeFrom = fromNode;

            String toNodeStr = attributes.getValue("TO");
            if (toNodeStr == null)
                throw new SAXException("NODE: missing attribute TO for link " + name);
            this.linkTag.nodeToName = toNodeStr.trim();
            @SuppressWarnings("rawtypes")
            Node toNode = XmlNetworkLaneParserSax.this.nodes.get(toNodeStr.trim());
            this.linkTag.nodeTo = toNode;
        }

        /**
         * Parse the GTU tag with attributes of a GTU.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseGTUTag(final Attributes attributes) throws NetworkException, SAXException
        {
            GTUTag gtuTag = new GTUTag();

            String name = attributes.getValue("NAME");
            if (name == null)
                throw new SAXException("GTU: missing attribute NAME");
            gtuTag.name = name.trim();
            if (XmlNetworkLaneParserSax.this.gtuTypes.keySet().contains(gtuTag.name))
                throw new SAXException("GTU: NAME " + gtuTag.name + " defined twice");

            String gtuType = attributes.getValue("GTUTYPE");
            if (gtuType == null)
                throw new SAXException("GTU: missing attribute GTUTYPE");
            gtuTag.gtuType = parseGTUType(attributes.getValue("GTUTYPE").trim());

            String length = attributes.getValue("LENGTH");
            if (length == null)
                throw new SAXException("GTU: missing attribute LENGTH");
            gtuTag.lengthDist = parseLengthDistRel(attributes.getValue("LENGTH"));

            String width = attributes.getValue("WIDTH");
            if (width == null)
                throw new SAXException("GTU: missing attribute WIDTH");
            gtuTag.widthDist = parseLengthDistRel(attributes.getValue("WIDTH"));

            String following = attributes.getValue("FOLLOWING");
            if (following == null)
                throw new SAXException("GTU: missing attribute FOLLOWING");
            gtuTag.followingModel = parseFollowingModel(attributes.getValue("FOLLOWING"));

            String laneChange = attributes.getValue("LANECHANGE");
            if (laneChange == null)
                throw new SAXException("GTU: missing attribute LANECHANGE");
            gtuTag.laneChangeModel = parseLaneChangeModel(attributes.getValue("LANECHANGE"));

            String maxSpeed = attributes.getValue("MAXSPEED");
            if (maxSpeed == null)
                throw new SAXException("GTU: missing attribute LENGTH");
            gtuTag.maxSpeedDist = parseSpeedDistAbs(attributes.getValue("MAXSPEED"));

            XmlNetworkLaneParserSax.this.gtuTags.put(gtuTag.name, gtuTag);
        }

        /**
         * Parse the STRAIGHT tag with straight attributes for a Link.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseStraightTag(final Attributes attributes) throws NetworkException, SAXException
        {
            this.linkTag.straightTag = new StraightTag();
            String length = attributes.getValue("LENGTH");
            if (length == null)
                throw new SAXException("STRAIGHT: missing attribute LENGTH");
            this.linkTag.straightTag.length = parseLengthRel(length);
        }

        /**
         * Parse the ARC tag with arc attributes for a Link.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseArcTag(final Attributes attributes) throws NetworkException, SAXException
        {
            this.linkTag.arcTag = new ArcTag();

            String radius = attributes.getValue("RADIUS");
            if (radius == null)
                throw new SAXException("ARC: missing attribute RADIUS");
            this.linkTag.arcTag.radius = parseLengthRel(radius);

            String angle = attributes.getValue("ANGLE");
            if (angle == null)
                throw new SAXException("ARC: missing attribute ANGLE");
            this.linkTag.arcTag.angle = parseAngleAbs(angle);

            String dir = attributes.getValue("DIRECTION");
            if (dir == null)
                throw new SAXException("ARC: missing attribute DIRECTION");
            this.linkTag.arcTag.direction =
                (dir.equals("L") || dir.equals("LEFT") || dir.equals("COUNTERCLOCKWISE")) ? ArcDirection.LEFT
                    : ArcDirection.RIGHT;
        }

        /**
         * Parse the ROADTYPE tag. As it can appear inside a LINK tag or separate as a macro, don't add it automatically to the
         * Map of RoadType tags in the main class.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseRoadTypeTag(final Attributes attributes) throws NetworkException, SAXException
        {
            String name = attributes.getValue("NAME");
            if (name == null)
                throw new SAXException("ROADTYPE: missing attribute NAME");
            this.roadTypeTag = new RoadTypeTag();
            this.roadTypeTag.name = name;
            if (XmlNetworkLaneParserSax.this.roadTypeTags.keySet().contains(this.roadTypeTag.name))
                throw new SAXException("ROADTYPE: NAME " + this.roadTypeTag.name + " defined twice");

            if (attributes.getValue("WIDTH") != null)
                this.roadTypeTag.width = parseLengthRel(attributes.getValue("WIDTH"));

            if (attributes.getValue("SPEED") != null)
                this.roadTypeTag.speed = parseSpeedAbs(attributes.getValue("SPEED"));
        }

        /**
         * Parse the ROADTYPE.LANE tag.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseRoadTypeLaneTag(final Attributes attributes) throws NetworkException, SAXException
        {
            String name = attributes.getValue("NAME");
            if (name == null)
                throw new SAXException("ROADTYPE.LANE: missing attribute NAME for ROADTYPE " + this.roadTypeTag.name);
            if (this.roadTypeTag.cseTags.containsKey(name))
                throw new SAXException("ROADTYPE.LANE: LANE NAME " + name + " defined twice");

            CrossSectionElementTag cseTag = new CrossSectionElementTag();
            cseTag.name = name;
            cseTag.elementType = ElementType.LANE;

            if (attributes.getValue("TYPE") == null)
                throw new SAXException("ROADTYPE.LANE: missing attribute TYPE for lane " + this.roadTypeTag.name + "."
                    + name);
            cseTag.laneTypeString = attributes.getValue("TYPE");
            if (!XmlNetworkLaneParserSax.this.laneTypes.containsKey(cseTag.laneTypeString))
                throw new SAXException("ROADTYPE.LANE: TYPE " + cseTag.laneTypeString + " for lane " + this.roadTypeTag.name
                    + "." + name + " does not have compatible GTUs defined in a COMPATIBILITY element");
            cseTag.laneType = XmlNetworkLaneParserSax.this.laneTypes.get(cseTag.laneTypeString);

            if (attributes.getValue("OFFSET") != null)
                cseTag.offset = parseLengthRel(attributes.getValue("OFFSET"));

            if (attributes.getValue("WIDTH") != null)
                cseTag.width = parseLengthRel(attributes.getValue("WIDTH"));
            else if (this.roadTypeTag.width != null)
                cseTag.width = this.roadTypeTag.width;
            else if (this.globalTag.width != null)
                cseTag.width = this.globalTag.width;
            else
                throw new SAXException("ROADTYPE.LANE: cannot determine WIDTH for lane: " + this.roadTypeTag.name + "."
                    + name);

            if (attributes.getValue("SPEED") != null)
                cseTag.speed = parseSpeedAbs(attributes.getValue("SPEED"));
            else if (this.roadTypeTag.speed != null)
                cseTag.speed = this.roadTypeTag.speed;
            else if (this.globalTag.speed != null)
                cseTag.speed = this.globalTag.speed;
            else
                throw new SAXException("ROADTYPE.LANE: cannot determine SPEED for lane: " + this.roadTypeTag.name + "."
                    + name);

            if (attributes.getValue("DIRECTION") == null)
                throw new SAXException("ROADTYPE.LANE: missing attribute DIRECTION for lane " + this.roadTypeTag.name + "."
                    + name);
            cseTag.direction = parseDirection(attributes.getValue("DIRECTION"));

            if (attributes.getValue("COLOR") != null)
                cseTag.color = parseColor(attributes.getValue("COLOR"));
            else
                cseTag.color = Color.LIGHT_GRAY;

            this.roadTypeTag.cseTags.put(cseTag.name, cseTag);
        }

        /**
         * Parse the ROADTYPE.NOTRAFFICLANE tag.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseRoadTypeNoTrafficLaneTag(final Attributes attributes) throws NetworkException, SAXException
        {
            String name = attributes.getValue("NAME");
            if (name == null)
                name = UUID.randomUUID().toString();
            if (this.roadTypeTag.cseTags.containsKey(name))
                throw new SAXException("ROADTYPE.NOTRAFFICLANE: LANE NAME " + name + " defined twice");

            CrossSectionElementTag cseTag = new CrossSectionElementTag();
            cseTag.name = name;
            cseTag.elementType = ElementType.NOTRAFFICLANE;

            if (attributes.getValue("OFFSET") != null)
                cseTag.offset = parseLengthRel(attributes.getValue("OFFSET"));

            if (attributes.getValue("WIDTH") != null)
                cseTag.width = parseLengthRel(attributes.getValue("WIDTH"));
            else if (this.roadTypeTag.width != null)
                cseTag.width = this.roadTypeTag.width;
            else if (this.globalTag.width != null)
                cseTag.width = this.globalTag.width;
            else
                throw new SAXException("ROADTYPE.NOTRAFFICLANE: cannot determine WIDTH for NOTRAFFICLANE: "
                    + this.roadTypeTag.name + "." + name);

            if (attributes.getValue("COLOR") != null)
                cseTag.color = parseColor(attributes.getValue("COLOR"));
            else
                cseTag.color = Color.GRAY;

            this.roadTypeTag.cseTags.put(cseTag.name, cseTag);
        }

        /**
         * Parse the ROADTYPE.SHOULDER tag.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseRoadTypeShoulderTag(final Attributes attributes) throws NetworkException, SAXException
        {
            String name = attributes.getValue("NAME");
            if (name == null)
                name = UUID.randomUUID().toString();
            if (this.roadTypeTag.cseTags.containsKey(name))
                throw new SAXException("ROADTYPE.SHOULDER: LANE NAME " + name + " defined twice");

            CrossSectionElementTag cseTag = new CrossSectionElementTag();
            cseTag.name = name;
            cseTag.elementType = ElementType.SHOULDER;

            if (attributes.getValue("OFFSET") != null)
                cseTag.offset = parseLengthRel(attributes.getValue("OFFSET"));

            if (attributes.getValue("WIDTH") != null)
                cseTag.width = parseLengthRel(attributes.getValue("WIDTH"));
            else if (this.roadTypeTag.width != null)
                cseTag.width = this.roadTypeTag.width;
            else if (this.globalTag.width != null)
                cseTag.width = this.globalTag.width;
            else
                throw new SAXException("ROADTYPE.SHOULDER: cannot determine WIDTH for NOTRAFFICLANE: "
                    + this.roadTypeTag.name + "." + name);

            if (attributes.getValue("COLOR") != null)
                cseTag.color = parseColor(attributes.getValue("COLOR"));
            else
                cseTag.color = Color.GREEN;

            this.roadTypeTag.cseTags.put(cseTag.name, cseTag);
        }

        /**
         * Parse the ROADTYPE.STRIPE tag.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseRoadTypeStripeTag(final Attributes attributes) throws NetworkException, SAXException
        {
            String name = attributes.getValue("NAME");
            if (name == null)
                name = UUID.randomUUID().toString();
            if (this.roadTypeTag.cseTags.containsKey(name))
                throw new SAXException("ROADTYPE.STRIPE: LANE NAME " + name + " defined twice");

            CrossSectionElementTag cseTag = new CrossSectionElementTag();
            cseTag.name = name;
            cseTag.elementType = ElementType.STRIPE;

            if (attributes.getValue("TYPE") != null)
                cseTag.stripeType = parseStripeType(attributes.getValue("TYPE"));

            if (attributes.getValue("OFFSET") != null)
                cseTag.offset = parseLengthRel(attributes.getValue("OFFSET"));

            if (attributes.getValue("WIDTH") != null)
                cseTag.width = parseLengthRel(attributes.getValue("WIDTH"));
            else
                cseTag.width = new DoubleScalar.Rel<LengthUnit>(0.2, LengthUnit.METER);

            if (attributes.getValue("COLOR") != null)
                cseTag.color = parseColor(attributes.getValue("COLOR"));
            else
                cseTag.color = Color.WHITE;

            this.roadTypeTag.cseTags.put(cseTag.name, cseTag);
        }

        /**
         * Parse the LANEOVERRIDE tag with lane attributes for a Link.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseLaneOverrideTag(final Attributes attributes) throws NetworkException, SAXException
        {
            String name = attributes.getValue("NAME");
            if (name == null)
                throw new SAXException("LANEOVERRIDE: missing attribute NAME" + " for link " + this.linkTag.name);
            if (this.linkTag.roadTypeTag == null)
                throw new NetworkException("LANEOVERRIDE: NAME " + name.trim() + " no ROADTYPE for link "
                    + this.linkTag.name);
            CrossSectionElementTag laneTag = this.linkTag.roadTypeTag.cseTags.get(name.trim());
            if (laneTag == null)
                throw new NetworkException("LANEOVERRIDE: Lane with NAME " + name.trim() + " not found in elements of link "
                    + this.linkTag.name + " - roadtype " + this.linkTag.roadTypeTag.name);
            if (this.linkTag.laneOverrideTags.containsKey(name))
                throw new SAXException("LANEOVERRIDE: LANE OVERRIDE with NAME " + name + " defined twice");

            LaneOverrideTag laneOverrideTag = new LaneOverrideTag();

            if (attributes.getValue("SPEED") != null)
                laneOverrideTag.speed = parseSpeedAbs(attributes.getValue("SPEED"));

            if (attributes.getValue("DIRECTION") != null)
                laneOverrideTag.direction = parseDirection(attributes.getValue("DIRECTION"));

            if (attributes.getValue("COLOR") != null)
                laneOverrideTag.color = parseColor(attributes.getValue("COLOR"));

            this.linkTag.laneOverrideTags.put(name.trim(), laneOverrideTag);
        }

        /**
         * Parse the GENERATOR tag with GTU generation attributes for a Lane. Note: Only one generator can be defined for the
         * same lane.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseGeneratorTag(final Attributes attributes) throws NetworkException, SAXException
        {
            GeneratorTag generatorTag = new GeneratorTag();

            String laneName = attributes.getValue("LANE");
            if (laneName == null)
                throw new SAXException("GENERATOR: missing attribute LANE" + " for link " + this.linkTag.name);
            if (this.linkTag.roadTypeTag == null)
                throw new NetworkException("GENERATOR: LANE " + laneName.trim() + " no ROADTYPE for link "
                    + this.linkTag.name);
            CrossSectionElementTag cseTag = this.linkTag.roadTypeTag.cseTags.get(laneName.trim());
            if (cseTag == null)
                throw new NetworkException("GENERATOR: LANE " + laneName.trim() + " not found in elements of link "
                    + this.linkTag.name + " - roadtype " + this.linkTag.roadTypeTag.name);
            if (cseTag.elementType != ElementType.LANE)
                throw new NetworkException("GENERATOR: LANE " + laneName.trim() + " not a real GTU lane for link "
                    + this.linkTag.name + " - roadtype " + this.linkTag.roadTypeTag.name);
            if (this.linkTag.generatorTags.containsKey(laneName))
                throw new SAXException("GENERATOR for LANE with NAME " + laneName + " defined twice");

            String posStr = attributes.getValue("POSITION");
            generatorTag.position = parseBeginEndPosition(posStr == null ? "END" : posStr, this.linkTag);

            String gtuName = attributes.getValue("GTU");
            if (gtuName != null)
            {
                if (!XmlNetworkLaneParserSax.this.gtuTags.containsKey(gtuName.trim()))
                    throw new NetworkException("GENERATOR: LANE " + laneName + " GTU " + gtuName.trim() + " in link "
                        + this.linkTag.name + " not defined");
                generatorTag.gtuTag = XmlNetworkLaneParserSax.this.gtuTags.get(gtuName.trim());
            }

            String gtuMixName = attributes.getValue("GTUMIX");
            if (gtuMixName != null)
            {
                if (!XmlNetworkLaneParserSax.this.gtuMixTags.containsKey(gtuMixName.trim()))
                    throw new NetworkException("GENERATOR: LANE " + laneName + " GTUMIX " + gtuMixName.trim() + " in link "
                        + this.linkTag.name + " not defined");
                generatorTag.gtuMixTag = XmlNetworkLaneParserSax.this.gtuMixTags.get(gtuMixName.trim());
            }

            if (generatorTag.gtuTag == null && generatorTag.gtuMixTag == null)
                throw new SAXException("GENERATOR: missing attribute GTU or GTUMIX for Lane with NAME " + laneName
                    + " of link " + this.linkTag.name);

            if (generatorTag.gtuTag != null && generatorTag.gtuMixTag != null)
                throw new SAXException("GENERATOR: both attribute GTU and GTUMIX defined for Lane with NAME " + laneName
                    + " of link " + this.linkTag.name);

            String iat = attributes.getValue("IAT");
            if (iat == null)
                throw new SAXException("GENERATOR: missing attribute IAT");
            generatorTag.iatDist = parseTimeDistRel(iat);

            String initialSpeed = attributes.getValue("INITIALSPEED");
            if (initialSpeed == null)
                throw new SAXException("GENERATOR: missing attribute INITIALSPEED");
            generatorTag.initialSpeedDist = parseSpeedDistAbs(initialSpeed);

            String maxGTU = attributes.getValue("MAXGTU");
            generatorTag.maxGTUs = maxGTU == null ? Integer.MAX_VALUE : Integer.parseInt(maxGTU);

            if (attributes.getValue("STARTTIME") != null)
                generatorTag.startTime = parseTimeAbs(attributes.getValue("STARTTIME"));

            if (attributes.getValue("ENDTIME") != null)
                generatorTag.endTime = parseTimeAbs(attributes.getValue("ENDTIME"));

            int numberRouteTags = 0;

            String routeName = attributes.getValue("ROUTE");
            if (routeName != null)
            {
                if (!XmlNetworkLaneParserSax.this.routeTags.containsKey(routeName.trim()))
                    throw new NetworkException("GENERATOR: LANE " + laneName + " ROUTE " + routeName.trim() + " in link "
                        + this.linkTag.name + " not defined");
                generatorTag.routeTag = XmlNetworkLaneParserSax.this.routeTags.get(routeName.trim());
                numberRouteTags++;
            }

            String routeMixName = attributes.getValue("ROUTEMIX");
            if (routeMixName != null)
            {
                if (!XmlNetworkLaneParserSax.this.routeMixTags.containsKey(routeMixName.trim()))
                    throw new NetworkException("GENERATOR: LANE " + laneName + " ROUTEMIX " + routeMixName.trim()
                        + " in link " + this.linkTag.name + " not defined");
                generatorTag.routeMixTag = XmlNetworkLaneParserSax.this.routeMixTags.get(routeMixName.trim());
                numberRouteTags++;
            }

            String shortestRouteName = attributes.getValue("SHORTESTROUTE");
            if (shortestRouteName != null)
            {
                if (!XmlNetworkLaneParserSax.this.shortestRouteTags.containsKey(shortestRouteName.trim()))
                    throw new NetworkException("GENERATOR: LANE " + laneName + " SHORTESTROUTE " + shortestRouteName.trim()
                        + " in link " + this.linkTag.name + " not defined");
                generatorTag.shortestRouteTag = XmlNetworkLaneParserSax.this.shortestRouteTags.get(shortestRouteName.trim());
                numberRouteTags++;
            }

            String shortestRouteMixName = attributes.getValue("SHORTESTROUTEMIX");
            if (shortestRouteMixName != null)
            {
                if (!XmlNetworkLaneParserSax.this.shortestRouteMixTags.containsKey(shortestRouteMixName.trim()))
                    throw new NetworkException("GENERATOR: LANE " + laneName + " SHORTESTROUTEMIX "
                        + shortestRouteMixName.trim() + " in link " + this.linkTag.name + " not defined");
                generatorTag.shortestRouteMixTag =
                    XmlNetworkLaneParserSax.this.shortestRouteMixTags.get(shortestRouteMixName.trim());
                numberRouteTags++;
            }

            if (numberRouteTags > 1)
                throw new SAXException("GENERATOR: multiple ROUTE tags defined for Lane with NAME " + laneName + " of link "
                    + this.linkTag.name);

            // TODO GTUColorer

            this.linkTag.generatorTags.put(laneName, generatorTag);
        }

        /**
         * Parse the SINK tag for a Lane. Note: Only one sink can be defined for the same lane.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseSinkTag(final Attributes attributes) throws NetworkException, SAXException
        {
            String laneName = attributes.getValue("LANE");
            if (laneName == null)
                throw new SAXException("SINK: missing attribute LANE" + " for link " + this.linkTag.name);
            if (this.linkTag.roadTypeTag == null)
                throw new NetworkException("SINK: LANE " + laneName.trim() + " no ROADTYPE for link " + this.linkTag.name);
            CrossSectionElementTag cseTag = this.linkTag.roadTypeTag.cseTags.get(laneName.trim());
            if (cseTag == null)
                throw new NetworkException("SINK: LANE " + laneName.trim() + " not found in elements of link "
                    + this.linkTag.name + " - roadtype " + this.linkTag.roadTypeTag.name);
            if (cseTag.elementType != ElementType.LANE)
                throw new NetworkException("SINK: LANE " + laneName.trim() + " not a real GTU lane for link "
                    + this.linkTag.name + " - roadtype " + this.linkTag.roadTypeTag.name);
            if (this.linkTag.sinkLanes.contains(laneName))
                throw new SAXException("SINK for LANE with NAME " + laneName + " defined twice");

            this.linkTag.sinkLanes.add(laneName);
        }

        /**
         * Parse the LISTGENERATOR tag with GTU generation attributes for a Lane. Note: Only one generator can be defined for
         * the same lane.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseListGeneratorTag(final Attributes attributes) throws NetworkException, SAXException
        {
            ListGeneratorTag listGeneratorTag = new ListGeneratorTag();

            String uriStr = attributes.getValue("URI");
            try
            {
                listGeneratorTag.uri = new URI(uriStr);
            }
            catch (URISyntaxException exception)
            {
                throw new NetworkException("LISTGENERATOR: URI " + uriStr + " is not valid", exception);
            }

            String laneName = attributes.getValue("LANE");
            if (laneName == null)
                throw new SAXException("LISTGENERATOR: missing attribute LANE" + " for link " + this.linkTag.name);
            if (this.linkTag.roadTypeTag == null)
                throw new NetworkException("LISTGENERATOR: LANE " + laneName.trim() + " no ROADTYPE for link "
                    + this.linkTag.name);
            CrossSectionElementTag cseTag = this.linkTag.roadTypeTag.cseTags.get(laneName.trim());
            if (cseTag == null)
                throw new NetworkException("LISTGENERATOR: LANE " + laneName.trim() + " not found in elements of link "
                    + this.linkTag.name + " - roadtype " + this.linkTag.roadTypeTag.name);
            if (cseTag.elementType != ElementType.LANE)
                throw new NetworkException("LISTGENERATOR: LANE " + laneName.trim() + " not a real GTU lane for link "
                    + this.linkTag.name + " - roadtype " + this.linkTag.roadTypeTag.name);
            if (this.linkTag.generatorTags.containsKey(laneName))
                throw new SAXException("LISTGENERATOR for LANE with NAME " + laneName + " defined twice");

            String posStr = attributes.getValue("POSITION");
            listGeneratorTag.position = parseBeginEndPosition(posStr == null ? "END" : posStr, this.linkTag);

            String gtuName = attributes.getValue("GTU");
            if (gtuName != null)
            {
                if (!XmlNetworkLaneParserSax.this.gtuTags.containsKey(gtuName.trim()))
                    throw new NetworkException("LISTGENERATOR: LANE " + laneName + " GTU " + gtuName.trim() + " in link "
                        + this.linkTag.name + " not defined");
                listGeneratorTag.gtuTag = XmlNetworkLaneParserSax.this.gtuTags.get(gtuName.trim());
            }

            String gtuMixName = attributes.getValue("GTUMIX");
            if (gtuMixName != null)
            {
                if (!XmlNetworkLaneParserSax.this.gtuMixTags.containsKey(gtuMixName.trim()))
                    throw new NetworkException("LISTGENERATOR: LANE " + laneName + " GTUMIX " + gtuMixName.trim()
                        + " in link " + this.linkTag.name + " not defined");
                listGeneratorTag.gtuMixTag = XmlNetworkLaneParserSax.this.gtuMixTags.get(gtuMixName.trim());
            }

            if (listGeneratorTag.gtuTag == null && listGeneratorTag.gtuMixTag == null)
                throw new SAXException("LISTGENERATOR: missing attribute GTU or GTUMIX for Lane with NAME " + laneName
                    + " of link " + this.linkTag.name);

            if (listGeneratorTag.gtuTag != null && listGeneratorTag.gtuMixTag != null)
                throw new SAXException("LISTGENERATOR: both attribute GTU and GTUMIX defined for Lane with NAME " + laneName
                    + " of link " + this.linkTag.name);

            String initialSpeed = attributes.getValue("INITIALSPEED");
            if (initialSpeed == null)
                throw new SAXException("LISTGENERATOR: missing attribute INITIALSPEED");
            listGeneratorTag.initialSpeedDist = parseSpeedDistAbs(initialSpeed);

            // TODO GTUColorer

            // TODO this.linkTag.listGeneratorTags.put(laneName, listGeneratorTag);
        }

        /**
         * Parse the FILL tag with GTU fill attributes for a Lane. Note: Only one FILL can be defined for the same lane.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseFillTag(final Attributes attributes) throws NetworkException, SAXException
        {
            FillTag fillTag = new FillTag();

            String laneName = attributes.getValue("LANE");
            if (laneName == null)
                throw new SAXException("FILL: missing attribute LANE" + " for link " + this.linkTag.name);
            if (this.linkTag.roadTypeTag == null)
                throw new NetworkException("FILL: NAME " + laneName.trim() + " no ROADTYPE for link " + this.linkTag.name);
            CrossSectionElementTag cseTag = this.linkTag.roadTypeTag.cseTags.get(laneName.trim());
            if (cseTag == null)
                throw new NetworkException("FILL: Lane with NAME " + laneName.trim() + " not found in elements of link "
                    + this.linkTag.name + " - roadtype " + this.linkTag.roadTypeTag.name);
            if (cseTag.elementType != ElementType.LANE)
                throw new NetworkException("FILL: Lane with NAME " + laneName.trim() + " not a real GTU lane for link "
                    + this.linkTag.name + " - roadtype " + this.linkTag.roadTypeTag.name);
            if (this.linkTag.fillTags.containsKey(laneName))
                throw new SAXException("FILL for LANE with NAME " + laneName + " defined twice");

            String gtuName = attributes.getValue("GTU");
            if (gtuName != null)
            {
                if (!XmlNetworkLaneParserSax.this.gtuTags.containsKey(gtuName.trim()))
                    throw new NetworkException("FILL: LANE " + laneName + " GTU " + gtuName.trim() + " in link "
                        + this.linkTag.name + " not defined");
                fillTag.gtuTag = XmlNetworkLaneParserSax.this.gtuTags.get(gtuName.trim());
            }

            String gtuMixName = attributes.getValue("GTUMIX");
            if (gtuMixName != null)
            {
                if (!XmlNetworkLaneParserSax.this.gtuMixTags.containsKey(gtuMixName.trim()))
                    throw new NetworkException("FILL: LANE " + laneName + " GTUMIX " + gtuMixName.trim() + " in link "
                        + this.linkTag.name + " not defined");
                fillTag.gtuMixTag = XmlNetworkLaneParserSax.this.gtuMixTags.get(gtuMixName.trim());
            }

            if (fillTag.gtuTag == null && fillTag.gtuMixTag == null)
                throw new SAXException("FILL: missing attribute GTU or GTUMIX for Lane with NAME " + laneName.trim()
                    + " of link " + this.linkTag.name);

            if (fillTag.gtuTag != null && fillTag.gtuMixTag != null)
                throw new SAXException("FILL: both attribute GTU and GTUMIX defined for Lane with NAME " + laneName
                    + " of link " + this.linkTag.name);

            String distance = attributes.getValue("DISTANCE");
            if (distance == null)
                throw new SAXException("FILL: missing attribute DISTANCE");
            fillTag.distanceDist = parseLengthDistRel(distance);

            String initialSpeed = attributes.getValue("INITIALSPEED");
            if (initialSpeed == null)
                throw new SAXException("FILL: missing attribute INITIALSPEED");
            fillTag.initialSpeedDist = parseSpeedDistAbs(initialSpeed);

            String maxGTU = attributes.getValue("MAXGTU");
            fillTag.maxGTUs = maxGTU == null ? Integer.MAX_VALUE : Integer.parseInt(maxGTU);

            int numberRouteTags = 0;

            String routeName = attributes.getValue("ROUTE");
            if (routeName != null)
            {
                if (!XmlNetworkLaneParserSax.this.routeTags.containsKey(routeName.trim()))
                    throw new NetworkException("FILL: LANE " + laneName + " ROUTE " + routeName.trim() + " in link "
                        + this.linkTag.name + " not defined");
                fillTag.routeTag = XmlNetworkLaneParserSax.this.routeTags.get(routeName.trim());
                numberRouteTags++;
            }

            String routeMixName = attributes.getValue("ROUTEMIX");
            if (routeMixName != null)
            {
                if (!XmlNetworkLaneParserSax.this.routeMixTags.containsKey(routeMixName.trim()))
                    throw new NetworkException("FILL: LANE " + laneName + " ROUTEMIX " + routeMixName.trim() + " in link "
                        + this.linkTag.name + " not defined");
                fillTag.routeMixTag = XmlNetworkLaneParserSax.this.routeMixTags.get(routeMixName.trim());
                numberRouteTags++;
            }

            String shortestRouteName = attributes.getValue("SHORTESTROUTE");
            if (shortestRouteName != null)
            {
                if (!XmlNetworkLaneParserSax.this.shortestRouteTags.containsKey(shortestRouteName.trim()))
                    throw new NetworkException("FILL: LANE " + laneName + " SHORTESTROUTE " + shortestRouteName.trim()
                        + " in link " + this.linkTag.name + " not defined");
                fillTag.shortestRouteTag = XmlNetworkLaneParserSax.this.shortestRouteTags.get(shortestRouteName.trim());
                numberRouteTags++;
            }

            String shortestRouteMixName = attributes.getValue("SHORTESTROUTEMIX");
            if (shortestRouteMixName != null)
            {
                if (!XmlNetworkLaneParserSax.this.shortestRouteMixTags.containsKey(shortestRouteMixName.trim()))
                    throw new NetworkException("FILL: LANE " + laneName + " SHORTESTROUTEMIX " + shortestRouteMixName.trim()
                        + " in link " + this.linkTag.name + " not defined");
                fillTag.shortestRouteMixTag =
                    XmlNetworkLaneParserSax.this.shortestRouteMixTags.get(shortestRouteMixName.trim());
                numberRouteTags++;
            }

            if (numberRouteTags > 1)
                throw new SAXException("FILL: multiple ROUTE tags defined for Lane with NAME " + laneName + " of link "
                    + this.linkTag.name);

            this.linkTag.fillTags.put(laneName, fillTag);
        }

        /**
         * Parse the BLOCK tag with a Lane end or blockage.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseBlockTag(final Attributes attributes) throws NetworkException, SAXException
        {
            BlockTag blockTag = new BlockTag();

            String laneName = attributes.getValue("LANE");
            if (laneName == null)
                throw new SAXException("BLOCK: missing attribute LANE" + " for link " + this.linkTag.name);
            CrossSectionElementTag cseTag = this.linkTag.roadTypeTag.cseTags.get(laneName.trim());
            if (cseTag == null)
                throw new NetworkException("BLOCK: Lane with NAME " + laneName.trim() + " not found in elements of link "
                    + this.linkTag.name + " - roadtype " + this.linkTag.roadTypeTag.name);
            if (cseTag.elementType != ElementType.LANE)
                throw new NetworkException("BLOCK: Lane with NAME " + laneName.trim() + " not a real GTU lane for link "
                    + this.linkTag.name + " - roadtype " + this.linkTag.roadTypeTag.name);
            if (this.linkTag.blockTags.containsKey(laneName))
                throw new SAXException("BLOCK for LANE with NAME " + laneName + " defined twice");

            String posStr = attributes.getValue("POSITION");
            if (posStr == null)
                throw new SAXException("BLOCK: missing attribute POSITION for link " + this.linkTag.name);
            blockTag.position = parseBeginEndPosition(posStr, this.linkTag);

            this.linkTag.blockTags.put(laneName, blockTag);
        }

        /**
         * Parse the GTUMIX tag.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseGTUMixTag(final Attributes attributes) throws NetworkException, SAXException
        {
            this.gtuMixTag = new GTUMixTag();

            String name = attributes.getValue("NAME");
            if (name == null)
                throw new SAXException("GTUMIX: missing attribute NAME");
            this.gtuMixTag.name = name.trim();
            if (XmlNetworkLaneParserSax.this.gtuMixTags.keySet().contains(this.gtuMixTag.name))
                throw new SAXException("GTUMIX: NAME " + this.gtuMixTag.name + " defined twice");

            XmlNetworkLaneParserSax.this.gtuMixTags.put(name.trim(), this.gtuMixTag);
        }

        /**
         * Parse the GTUMIX's GTU tag.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseGTUMixGTUTag(final Attributes attributes) throws NetworkException, SAXException
        {
            if (this.gtuMixTag == null)
                throw new NetworkException("GTUMIX: parse error");

            String gtuName = attributes.getValue("NAME");
            if (gtuName == null)
                throw new NetworkException("GTUMIX: No GTU NAME defined");
            if (!XmlNetworkLaneParserSax.this.gtuTags.containsKey(gtuName.trim()))
                throw new NetworkException("GTUMIX: " + this.gtuMixTag.name + " GTU " + gtuName.trim() + " not defined");
            this.gtuMixTag.gtus.add(XmlNetworkLaneParserSax.this.gtuTags.get(gtuName.trim()));

            String weight = attributes.getValue("WEIGHT");
            if (weight == null)
                throw new NetworkException("GTUMIX: " + this.gtuMixTag.name + " GTU " + gtuName.trim()
                    + ": weight not defined");
            this.gtuMixTag.weights.add(Double.parseDouble(weight));
        }

        /**
         * Parse the ROUTE tag.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseRouteTag(final Attributes attributes) throws NetworkException, SAXException
        {
            RouteTag routeTag = new RouteTag();

            String name = attributes.getValue("NAME");
            if (name == null)
                throw new SAXException("ROUTE: missing attribute NAME");
            routeTag.name = name.trim();
            if (XmlNetworkLaneParserSax.this.routeTags.keySet().contains(routeTag.name))
                throw new SAXException("ROUTE: NAME " + routeTag.name + " defined twice");

            String routeNodes = attributes.getValue("NODELIST");
            if (routeNodes == null)
                throw new SAXException("ROUTE " + name.trim() + ": missing attribute NODELIST");
            routeTag.routeNodeTags = parseNodeList(routeNodes);

            XmlNetworkLaneParserSax.this.routeTags.put(name.trim(), routeTag);
        }

        /**
         * Parse the ROUTEMIX tag.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseRouteMixTag(final Attributes attributes) throws NetworkException, SAXException
        {
            this.routeMixTag = new RouteMixTag();

            String name = attributes.getValue("NAME");
            if (name == null)
                throw new SAXException("ROUTEMIX: missing attribute NAME");
            this.routeMixTag.name = name.trim();
            if (XmlNetworkLaneParserSax.this.routeMixTags.keySet().contains(this.routeMixTag.name))
                throw new SAXException("ROUTEMIX: NAME " + this.routeMixTag.name + " defined twice");

            XmlNetworkLaneParserSax.this.routeMixTags.put(name.trim(), this.routeMixTag);
        }

        /**
         * Parse the ROUTEMIX's ROUTE tag.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseRouteMixRouteTag(final Attributes attributes) throws NetworkException, SAXException
        {
            if (this.routeMixTag == null)
                throw new NetworkException("ROUTEMIX: parse error");

            String routeName = attributes.getValue("NAME");
            if (routeName == null)
                throw new NetworkException("ROUTEMIX: No ROUTE NAME defined");
            if (!XmlNetworkLaneParserSax.this.routeTags.containsKey(routeName.trim()))
                throw new NetworkException("ROUTEMIX: " + this.routeMixTag.name + " ROUTE " + routeName.trim()
                    + " not defined");
            this.routeMixTag.routes.add(XmlNetworkLaneParserSax.this.routeTags.get(routeName.trim()));

            String weight = attributes.getValue("WEIGHT");
            if (weight == null)
                throw new NetworkException("ROUTEMIX: " + this.routeMixTag.name + " ROUTE " + routeName.trim()
                    + ": weight not defined");
            this.routeMixTag.weights.add(Double.parseDouble(weight));
        }

        /**
         * Parse the SHORTESTROUTE tag.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseShortestRouteTag(final Attributes attributes) throws NetworkException, SAXException
        {
            ShortestRouteTag shortestRouteTag = new ShortestRouteTag();

            String name = attributes.getValue("NAME");
            if (name == null)
                throw new SAXException("SHORTESTROUTE: missing attribute NAME");
            shortestRouteTag.name = name.trim();
            if (XmlNetworkLaneParserSax.this.shortestRouteTags.keySet().contains(shortestRouteTag.name))
                throw new SAXException("SHORTESTROUTE: NAME " + shortestRouteTag.name + " defined twice");

            String fromNode = attributes.getValue("FROM");
            if (fromNode == null)
                throw new SAXException("SHORTESTROUTE: missing attribute FROM");
            if (!XmlNetworkLaneParserSax.this.nodeTags.containsKey(fromNode.trim()))
                throw new SAXException("SHORTESTROUTE " + name + ": FROM node " + fromNode.trim() + " not found");
            shortestRouteTag.from = XmlNetworkLaneParserSax.this.nodeTags.get(fromNode.trim());

            String viaNodes = attributes.getValue("NODELIST");
            if (viaNodes != null)
                shortestRouteTag.via = parseNodeList(viaNodes);

            String toNode = attributes.getValue("TO");
            if (toNode == null)
                throw new SAXException("SHORTESTROUTE: missing attribute TO");
            if (!XmlNetworkLaneParserSax.this.nodeTags.containsKey(toNode.trim()))
                throw new SAXException("SHORTESTROUTE " + name + ": TO node " + toNode.trim() + " not found");
            shortestRouteTag.to = XmlNetworkLaneParserSax.this.nodeTags.get(toNode.trim());

            String distanceCost = attributes.getValue("DISTANCECOST");
            if (distanceCost == null)
                throw new SAXException("SHORTESTROUTE: missing attribute DISTANCECOST");
            shortestRouteTag.costPerDistance = parsePerLengthAbs(distanceCost);

            String timeCost = attributes.getValue("TIMECOST");
            if (timeCost == null)
                throw new SAXException("SHORTESTROUTE: missing attribute TIMECOST");
            shortestRouteTag.costPerTime = parsePerTimeAbs(timeCost);

            XmlNetworkLaneParserSax.this.shortestRouteTags.put(name.trim(), shortestRouteTag);
        }

        /**
         * Parse the SHORTESTROUTEMIX tag.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseShortestRouteMixTag(final Attributes attributes) throws NetworkException, SAXException
        {
            this.shortestRouteMixTag = new ShortestRouteMixTag();

            String name = attributes.getValue("NAME");
            if (name == null)
                throw new SAXException("SHORTESTROUTEMIX: missing attribute NAME");
            this.shortestRouteMixTag.name = name.trim();
            if (XmlNetworkLaneParserSax.this.shortestRouteMixTags.keySet().contains(this.shortestRouteMixTag.name))
                throw new SAXException("SHORTESTROUTEMIX: NAME " + this.shortestRouteMixTag.name + " defined twice");

            XmlNetworkLaneParserSax.this.shortestRouteMixTags.put(name.trim(), this.shortestRouteMixTag);
        }

        /**
         * Parse the SHORTESTROUTEMIX's SHORTESTROUTE tag.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseShortestRouteMixShortestRouteTag(final Attributes attributes) throws NetworkException,
            SAXException
        {
            if (this.shortestRouteMixTag == null)
                throw new NetworkException("SHORTESTROUTEMIX: parse error");

            String shortestRouteName = attributes.getValue("NAME");
            if (shortestRouteName == null)
                throw new NetworkException("SHORTESTROUTEMIX: No SHORTESTROUTE NAME defined");
            if (!XmlNetworkLaneParserSax.this.shortestRouteTags.containsKey(shortestRouteName.trim()))
                throw new NetworkException("SHORTESTROUTEMIX: " + this.shortestRouteMixTag.name + " SHORTESTROUTE "
                    + shortestRouteName.trim() + " not defined");
            this.shortestRouteMixTag.shortestRoutes.add(XmlNetworkLaneParserSax.this.shortestRouteTags.get(shortestRouteName
                .trim()));

            String weight = attributes.getValue("WEIGHT");
            if (weight == null)
                throw new NetworkException("SHORTESTROUTEMIX: " + this.shortestRouteMixTag.name + " SHORTESTROUTE "
                    + shortestRouteName.trim() + ": weight not defined");
            this.shortestRouteMixTag.weights.add(Double.parseDouble(weight));
        }

    }

    /*************************************************************************************************/
    /****************************************** PARSING CLASSES **************************************/
    /*************************************************************************************************/

    /**
     * Generate an ID of the right type.
     * @param clazz the class to instantiate.
     * @param ids the id as a String.
     * @return the object as an instance of the right class.
     * @throws NetworkException when id cannot be instantiated
     */
    protected final Object makeId(final Class<?> clazz, final String ids) throws NetworkException
    {
        Object id = null;
        try
        {
            if (String.class.isAssignableFrom(clazz))
            {
                id = new String(ids);
            }
            else if (int.class.isAssignableFrom(clazz))
            {
                id = Integer.valueOf(ids);
            }
            else if (long.class.isAssignableFrom(clazz))
            {
                id = Long.valueOf(ids);
            }
            else
            {
                throw new NetworkException("Parsing network. ID class " + clazz.getName() + ": cannot instantiate.");
            }
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network. ID class " + clazz.getName() + ": cannot instantiate number: "
                + ids, nfe);
        }
        return id;
    }

    /**
     * @param nodeTag the tag with the info for the node.
     * @return a constructed node
     * @throws NetworkException when point cannot be instantiated
     * @throws NamingException when animation context cannot be found.
     * @throws RemoteException when communication error occurs when trying to find animation context.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected final Node makeNode(final NodeTag nodeTag) throws NetworkException, RemoteException, NamingException
    {
        Object id = makeId(this.nodeIdClass, nodeTag.name);
        DoubleScalar.Abs<AnglePlaneUnit> angle =
            nodeTag.angle == null ? new DoubleScalar.Abs<AnglePlaneUnit>(0.0, AnglePlaneUnit.SI) : nodeTag.angle;
        DoubleScalar.Abs<AngleSlopeUnit> slope =
            nodeTag.slope == null ? new DoubleScalar.Abs<AngleSlopeUnit>(0.0, AngleSlopeUnit.SI) : nodeTag.slope;
        Node node = new OTSNode(id, new OTSPoint3D(nodeTag.coordinate), angle, slope);
        this.nodes.put(node.getId().toString(), node);
        return node;
    }

    /**
     * One of the nodes probably has a coordinate and the other not. Calculate the other coordinate and save the Node.
     * @param linkTag the parsed information from the XML file.
     * @throws NetworkException when both nodes are null.
     * @throws RemoteException when coordinate cannot be reached.
     * @throws NamingException when node animation cannot link to the animation context.
     */
    @SuppressWarnings("methodlength")
    protected final void calculateNodeCoordinates(final LinkTag linkTag) throws RemoteException, NetworkException,
        NamingException
    {
        // calculate dx, dy and dz for the straight or the arc.
        if (linkTag.nodeFrom != null && linkTag.nodeTo != null)
        {
            if (linkTag.arcTag != null)
            {
                double radiusSI = linkTag.arcTag.radius.getSI();
                ArcDirection direction = linkTag.arcTag.direction;
                Point3d coordinate =
                    new Point3d(linkTag.nodeFrom.getLocation().getX(), linkTag.nodeFrom.getLocation().getY(),
                        linkTag.nodeFrom.getLocation().getZ());
                double startAngle = linkTag.nodeFrom.getDirection().getSI();
                if (direction.equals(ArcDirection.LEFT))
                {
                    linkTag.arcTag.center =
                        new Point3d(coordinate.x + radiusSI * Math.cos(startAngle + Math.PI / 2.0), coordinate.y + radiusSI
                            * Math.sin(startAngle + Math.PI / 2.0), 0.0);
                    linkTag.arcTag.startAngle = startAngle - Math.PI / 2.0;
                }
                else
                {
                    linkTag.arcTag.center =
                        new Point3d(coordinate.x + radiusSI * Math.cos(startAngle - Math.PI / 2.0), coordinate.y + radiusSI
                            * Math.sin(startAngle - Math.PI / 2.0), 0.0);
                    linkTag.arcTag.startAngle = startAngle + Math.PI / 2.0;
                }
            }
            return;
        }

        if (linkTag.nodeFrom == null && linkTag.nodeTo == null)
        {
            throw new NetworkException("Parsing network. Link: " + linkTag.name + ", both From-node and To-node are null");
        }

        if (linkTag.straightTag != null)
        {
            double lengthSI = linkTag.straightTag.length.getSI();
            if (linkTag.nodeTo == null)
            {
                Point3d coordinate =
                    new Point3d(linkTag.nodeFrom.getLocation().getX(), linkTag.nodeFrom.getLocation().getY(),
                        linkTag.nodeFrom.getLocation().getZ());
                double angle = linkTag.nodeFrom.getDirection().getSI();
                double slope = linkTag.nodeFrom.getSlope().getSI();
                coordinate.x += lengthSI * Math.cos(angle);
                coordinate.y += lengthSI * Math.sin(angle);
                coordinate.z += lengthSI * Math.sin(slope);
                NodeTag nodeTag = this.nodeTags.get(linkTag.nodeToName);
                nodeTag.angle = new DoubleScalar.Abs<AnglePlaneUnit>(angle, AnglePlaneUnit.SI);
                nodeTag.coordinate = coordinate;
                nodeTag.slope = new DoubleScalar.Abs<AngleSlopeUnit>(slope, AngleSlopeUnit.SI);
                @SuppressWarnings("rawtypes")
                Node node = makeNode(nodeTag);
                linkTag.nodeTo = node;
            }
            else if (linkTag.nodeFrom == null)
            {
                Point3d coordinate =
                    new Point3d(linkTag.nodeTo.getLocation().getX(), linkTag.nodeTo.getLocation().getY(), linkTag.nodeTo
                        .getLocation().getZ());
                double angle = linkTag.nodeTo.getDirection().getSI();
                double slope = linkTag.nodeTo.getSlope().getSI();
                coordinate.x -= lengthSI * Math.cos(angle);
                coordinate.y -= lengthSI * Math.sin(angle);
                coordinate.z -= lengthSI * Math.sin(slope);
                NodeTag nodeTag = this.nodeTags.get(linkTag.nodeFromName);
                nodeTag.angle = new DoubleScalar.Abs<AnglePlaneUnit>(angle, AnglePlaneUnit.SI);
                nodeTag.coordinate = coordinate;
                nodeTag.slope = new DoubleScalar.Abs<AngleSlopeUnit>(slope, AngleSlopeUnit.SI);
                @SuppressWarnings("rawtypes")
                Node node = makeNode(nodeTag);
                linkTag.nodeFrom = node;
            }
        }
        else if (linkTag.arcTag != null)
        {
            double radiusSI = linkTag.arcTag.radius.getSI();
            double angle = linkTag.arcTag.angle.getSI();
            ArcDirection direction = linkTag.arcTag.direction;
            if (linkTag.nodeTo == null)
            {
                Point3d coordinate = new Point3d();
                double startAngle = linkTag.nodeFrom.getDirection().getSI();
                double slope = linkTag.nodeFrom.getSlope().getSI();
                double lengthSI = radiusSI * angle;
                NodeTag nodeTag = this.nodeTags.get(linkTag.nodeToName);
                if (direction.equals(ArcDirection.LEFT))
                {
                    linkTag.arcTag.center =
                        new Point3d(linkTag.nodeFrom.getLocation().getX() + radiusSI * Math.cos(startAngle + Math.PI / 2.0),
                            linkTag.nodeFrom.getLocation().getY() + radiusSI * Math.sin(startAngle + Math.PI / 2.0), 0.0);
                    linkTag.arcTag.startAngle = startAngle - Math.PI / 2.0;
                    coordinate.x = linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle + angle);
                    coordinate.y = linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle + angle);
                    nodeTag.angle =
                        new DoubleScalar.Abs<AnglePlaneUnit>(AnglePlaneUnit.normalize(startAngle + angle), AnglePlaneUnit.SI);
                }
                else
                {
                    linkTag.arcTag.center =
                        new Point3d(coordinate.x + radiusSI * Math.cos(startAngle - Math.PI / 2.0), coordinate.y + radiusSI
                            * Math.sin(startAngle - Math.PI / 2.0), 0.0);
                    linkTag.arcTag.startAngle = startAngle;
                    coordinate.x = linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle - angle);
                    coordinate.y = linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle - angle);
                    nodeTag.angle =
                        new DoubleScalar.Abs<AnglePlaneUnit>(AnglePlaneUnit.normalize(startAngle - angle), AnglePlaneUnit.SI);
                }
                coordinate.z = linkTag.nodeFrom.getLocation().getZ() + lengthSI * Math.sin(slope);
                nodeTag.slope = new DoubleScalar.Abs<AngleSlopeUnit>(slope, AngleSlopeUnit.SI);
                nodeTag.coordinate = coordinate;
                @SuppressWarnings("rawtypes")
                Node node = makeNode(nodeTag);
                linkTag.nodeTo = node;
            }

            else if (linkTag.nodeFrom == null)
            {
                Point3d coordinate =
                    new Point3d(linkTag.nodeTo.getLocation().getX(), linkTag.nodeTo.getLocation().getY(), linkTag.nodeTo
                        .getLocation().getZ());
                double endAngle = linkTag.nodeTo.getDirection().getSI();
                double slope = linkTag.nodeTo.getSlope().getSI();
                double lengthSI = radiusSI * angle;
                NodeTag nodeTag = this.nodeTags.get(linkTag.nodeFromName);
                if (direction.equals(ArcDirection.LEFT))
                {
                    linkTag.arcTag.center =
                        new Point3d(coordinate.x + radiusSI + Math.cos(endAngle + Math.PI / 2.0), coordinate.y + radiusSI
                            * Math.sin(endAngle + Math.PI / 2.0), 0.0);
                    linkTag.arcTag.startAngle = endAngle - Math.PI / 2.0 - angle;
                    coordinate.x = linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle);
                    coordinate.y = linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle);
                    nodeTag.angle =
                        new DoubleScalar.Abs<AnglePlaneUnit>(AnglePlaneUnit.normalize(linkTag.arcTag.startAngle + Math.PI
                            / 2.0), AnglePlaneUnit.SI);
                }
                else
                {
                    linkTag.arcTag.center =
                        new Point3d(coordinate.x + radiusSI * Math.cos(endAngle - Math.PI / 2.0), coordinate.y + radiusSI
                            * Math.sin(endAngle - Math.PI / 2.0), 0.0);
                    linkTag.arcTag.startAngle = endAngle + Math.PI / 2.0 + angle;
                    coordinate.x = linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle);
                    coordinate.y = linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle);
                    nodeTag.angle =
                        new DoubleScalar.Abs<AnglePlaneUnit>(AnglePlaneUnit.normalize(linkTag.arcTag.startAngle - Math.PI
                            / 2.0), AnglePlaneUnit.SI);
                }
                coordinate.z -= lengthSI * Math.sin(slope);
                nodeTag.coordinate = coordinate;
                nodeTag.slope = new DoubleScalar.Abs<AngleSlopeUnit>(slope, AngleSlopeUnit.SI);
                @SuppressWarnings("rawtypes")
                Node node = makeNode(nodeTag);
                linkTag.nodeFrom = node;
            }
        }

    }

    /**
     * @param linkTag the link information from XML.
     * @return a constructed link
     * @throws SAXException when point cannot be instantiated
     * @throws NamingException when animation context cannot be found.
     * @throws RemoteException when communication error occurs when reaching animation context.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected final CrossSectionLink makeLink(final LinkTag linkTag) throws SAXException, RemoteException, NamingException
    {
        try
        {
            Object id = makeId(this.linkIdClass, linkTag.name);
            int points = 2;
            if (linkTag.arcTag != null)
            {
                points = (Math.abs(linkTag.arcTag.angle.getSI()) <= Math.PI / 2.0) ? 32 : 64;
            }
            NodeTag from = this.nodeTags.get(linkTag.nodeFromName);
            NodeTag to = this.nodeTags.get(linkTag.nodeToName);
            Coordinate[] coordinates = new Coordinate[points];
            coordinates[0] = new Coordinate(from.coordinate.x, from.coordinate.y, from.coordinate.z);
            coordinates[coordinates.length - 1] = new Coordinate(to.coordinate.x, to.coordinate.y, to.coordinate.z);
            if (linkTag.arcTag != null)
            {
                double angleStep = linkTag.arcTag.angle.getSI() / points;
                double slopeStep = (to.coordinate.z - from.coordinate.z) / points;
                double radiusSI = linkTag.arcTag.radius.getSI();
                if (linkTag.arcTag.direction.equals(ArcDirection.RIGHT))
                {
                    for (int p = 1; p < points - 1; p++)
                    {
                        coordinates[p] =
                            new Coordinate(linkTag.arcTag.center.x + radiusSI
                                * Math.cos(linkTag.arcTag.startAngle - angleStep * p), linkTag.arcTag.center.y + radiusSI
                                * Math.sin(linkTag.arcTag.startAngle - angleStep * p), from.coordinate.z + slopeStep * p);
                    }
                }
                else
                {
                    for (int p = 1; p < points - 1; p++)
                    {
                        coordinates[p] =
                            new Coordinate(linkTag.arcTag.center.x + radiusSI
                                * Math.cos(linkTag.arcTag.startAngle + angleStep * p), linkTag.arcTag.center.y + radiusSI
                                * Math.sin(linkTag.arcTag.startAngle + angleStep * p), from.coordinate.z + slopeStep * p);
                    }
                }
            }
            OTSLine3D designLine = new OTSLine3D(coordinates);
            CrossSectionLink link = new CrossSectionLink(id, linkTag.nodeFrom, linkTag.nodeTo, designLine);
            return link;
        }
        catch (NetworkException ne)
        {
            throw new SAXException("Error building Link", ne);
        }
    }

    /**
     * Make a generator.
     * @param generatorTag XML tag for the generator to build
     * @param lane the lane of the generator
     * @param name the name of the generator
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws RemoteException in case of network problems building the car generator
     * @throws NetworkException when route generator cannot be instantiated
     */
    protected final void makeGenerator(final GeneratorTag generatorTag, final Lane<?, ?> lane, final String name)
        throws SimRuntimeException, RemoteException, NetworkException
    {
        Class<?> gtuClass = LaneBasedIndividualCar.class;
        List<Node<?>> nodeList = new ArrayList<>();
        for (NodeTag nodeTag : generatorTag.routeTag.routeNodeTags)
        {
            nodeList.add(this.nodes.get(nodeTag.name));
        }
        DoubleScalar.Abs<TimeUnit> startTime =
            generatorTag.startTime != null ? generatorTag.startTime : new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SI);
        DoubleScalar.Abs<TimeUnit> endTime =
            generatorTag.endTime != null ? generatorTag.endTime : new DoubleScalar.Abs<TimeUnit>(Double.MAX_VALUE,
                TimeUnit.SI);
        LaneBasedRouteGenerator rg = new FixedLaneBasedRouteGenerator(new CompleteRoute("fixed route", nodeList));
        new GTUGeneratorIndividual<String>(name, this.simulator, generatorTag.gtuTag.gtuType, gtuClass,
            generatorTag.gtuTag.followingModel, generatorTag.gtuTag.laneChangeModel, generatorTag.initialSpeedDist,
            generatorTag.iatDist, generatorTag.gtuTag.lengthDist, generatorTag.gtuTag.widthDist,
            generatorTag.gtuTag.maxSpeedDist, generatorTag.maxGTUs, startTime, endTime, lane, generatorTag.position, rg,
            this.gtuColorer);
    }

    /**
     * Calculates the offsets and widths of the lanes and stripes in a RoadTypeTag by a forward pass from the first found
     * offset, and then a backward pass from the end to the beginning. Offsets are always in the middle of a lane or stripe.
     * Also give each lane a speed.
     * @param roadTypeTag the road type tag to calculate the offsets for.
     * @param globalTag to provide the global length.
     * @throws SAXException when the width of a lane cannot be calculated.
     */
    @SuppressWarnings("checkstyle:needbraces")
    protected final void calculateRoadTypeOffsets(final RoadTypeTag roadTypeTag, final GlobalTag globalTag)
        throws SAXException
    {
        // cseTags in a LinkedHashMap to guarantee the order.
        DoubleScalar.Rel<LengthUnit> lastOffset = null;
        DoubleScalar.Rel<LengthUnit> lastEdge = null;
        List<String> cseNames = new ArrayList<>();

        // forward pass
        for (String cseName : roadTypeTag.cseTags.keySet())
        {
            cseNames.add(cseName);
            CrossSectionElementTag cseTag = roadTypeTag.cseTags.get(cseName);

            // set the width
            double widthSI = Double.NaN;
            if (cseTag.width != null)
                widthSI = cseTag.width.getSI();
            else
            {
                if (roadTypeTag.width != null)
                    widthSI = roadTypeTag.width.getSI();
                else if (globalTag.width != null)
                    widthSI = globalTag.width.getSI();
                else
                    throw new SAXException("calculateRoadTypeOffsets for road type " + roadTypeTag.name
                        + ", no width for CSE " + cseName);
                cseTag.width = new DoubleScalar.Rel<LengthUnit>(widthSI, LengthUnit.SI);
            }

            // set the speed
            if (cseTag.elementType.equals(ElementType.LANE) && cseTag.speed == null)
            {
                if (roadTypeTag.speed != null)
                    cseTag.speed = roadTypeTag.speed.copy();
                else if (globalTag.speed != null)
                    cseTag.speed = globalTag.speed;
                else
                    throw new SAXException("calculateRoadTypeOffsets for road type " + roadTypeTag.name
                        + ", no speed for CSE " + cseName);
            }

            // set the offsets
            if (cseTag.offset != null)
            {
                lastOffset = cseTag.offset.copy();
                if (cseTag.elementType.equals(ElementType.STRIPE))
                    lastEdge = lastOffset.copy();
                else
                    lastEdge = new DoubleScalar.Rel<LengthUnit>(lastOffset.getSI() + widthSI / 2.0, LengthUnit.SI);
            }
            else
            {
                if (lastOffset != null)
                {
                    if (cseTag.elementType.equals(ElementType.STRIPE))
                        cseTag.offset = lastEdge.copy();
                    else
                    {
                        cseTag.offset = new DoubleScalar.Rel<LengthUnit>(lastEdge.getSI() + widthSI / 2.0, LengthUnit.SI);
                        lastEdge = new DoubleScalar.Rel<LengthUnit>(lastEdge.getSI() + widthSI, LengthUnit.SI);
                    }
                }
            }
        }

        // backward pass for the offsets
        lastOffset = null;
        lastEdge = null;
        for (int i = cseNames.size() - 1; i >= 0; i--)
        {
            String cseName = cseNames.get(i);
            CrossSectionElementTag cseTag = roadTypeTag.cseTags.get(cseName);
            double widthSI = cseTag.width.getSI();
            if (cseTag.offset != null)
            {
                lastOffset = cseTag.offset.copy();
                if (cseTag.elementType.equals(ElementType.STRIPE))
                    lastEdge = lastOffset.copy();
                else
                    lastEdge = new DoubleScalar.Rel<LengthUnit>(lastOffset.getSI() - widthSI / 2.0, LengthUnit.SI);
            }
            else
            {
                if (lastOffset != null)
                {
                    if (cseTag.elementType.equals(ElementType.STRIPE))
                        cseTag.offset = lastEdge.copy();
                    else
                    {
                        cseTag.offset = new DoubleScalar.Rel<LengthUnit>(lastEdge.getSI() - widthSI / 2.0, LengthUnit.SI);
                        lastEdge = new DoubleScalar.Rel<LengthUnit>(lastEdge.getSI() - widthSI, LengthUnit.SI);
                    }
                }
            }
        }
    }

    /**
     * At the end of a link tag, make sure the road type information is applied to the link.
     * @param roadTypeTag the road type information
     * @param csl the cross section link on which the data will be applied
     * @param linkTag the link information from XML
     * @param globalTag the global information for data not specified at the link
     * @throws NetworkException when the stripe cannot be instantiated
     * @throws RemoteException when the (remote) animator cannot be reached to create the animation
     * @throws NamingException when the /animation/2D tree cannot be found in the context
     * @throws SAXException when the stripe type cannot be parsed correctly
     * @throws GTUException when lane block cannot be created
     * @throws SimRuntimeException when generator cannot be created
     */
    @SuppressWarnings({"checkstyle:needbraces", "rawtypes", "unchecked"})
    protected final void applyRoadTypeToLink(final RoadTypeTag roadTypeTag, final CrossSectionLink csl,
        final LinkTag linkTag, final GlobalTag globalTag) throws NetworkException, RemoteException, NamingException,
        SAXException, GTUException, SimRuntimeException
    {
        List<CrossSectionElement> cseList = new ArrayList<>();
        List<Lane> lanes = new ArrayList<>();
        for (CrossSectionElementTag cseTag : roadTypeTag.cseTags.values())
        {
            switch (cseTag.elementType)
            {
                case STRIPE:
                    switch (cseTag.stripeType)
                    {
                        case BLOCKED:
                        case DASHED:
                            Stripe dashedLine = new Stripe(csl, cseTag.offset, cseTag.width);
                            dashedLine.addPermeability(GTUType.ALL, Permeable.BOTH);
                            if (this.simulator != null)
                            {
                                new StripeAnimation(dashedLine, this.simulator, StripeAnimation.TYPE.DASHED);
                            }
                            cseList.add(dashedLine);
                            break;

                        case DOUBLE:
                            Stripe doubleLine = new Stripe(csl, cseTag.offset, cseTag.width);
                            if (this.simulator != null)
                            {
                                new StripeAnimation(doubleLine, this.simulator, StripeAnimation.TYPE.DOUBLE);
                            }
                            cseList.add(doubleLine);
                            break;

                        case LEFTONLY:
                            Stripe leftOnlyLine = new Stripe(csl, cseTag.offset, cseTag.width);
                            leftOnlyLine.addPermeability(GTUType.ALL, Permeable.LEFT); // TODO: correct?
                            if (this.simulator != null)
                            {
                                new StripeAnimation(leftOnlyLine, this.simulator, StripeAnimation.TYPE.LEFTONLY);
                            }
                            cseList.add(leftOnlyLine);
                            break;

                        case RIGHTONLY:
                            Stripe rightOnlyLine = new Stripe(csl, cseTag.offset, cseTag.width);
                            rightOnlyLine.addPermeability(GTUType.ALL, Permeable.RIGHT); // TODO: correct?
                            if (this.simulator != null)
                            {
                                new StripeAnimation(rightOnlyLine, this.simulator, StripeAnimation.TYPE.RIGHTONLY);
                            }
                            cseList.add(rightOnlyLine);
                            break;

                        case SOLID:
                            Stripe solidLine = new Stripe(csl, cseTag.offset, cseTag.width);
                            if (this.simulator != null)
                            {
                                new StripeAnimation(solidLine, this.simulator, StripeAnimation.TYPE.SOLID);
                            }
                            cseList.add(solidLine);
                            break;

                        default:
                            throw new SAXException("Unknown Stripe type: " + cseTag.stripeType.toString());
                    }
                    break;

                case LANE:
                {
                    if (linkTag.sinkLanes.contains(cseTag.name))
                    {
                        // SINKLANE
                        SinkLane sinkLane =
                            new SinkLane(csl, cseTag.offset, cseTag.width, cseTag.laneType, cseTag.direction, cseTag.speed);
                        cseList.add(sinkLane);
                        lanes.add(sinkLane);
                        linkTag.lanes.put(cseTag.name, sinkLane);
                        if (this.simulator != null)
                        {
                            new LaneAnimation(sinkLane, this.simulator, cseTag.color);
                        }
                    }

                    else

                    {
                        // TODO LANEOVERRIDE
                        Lane<?, ?> lane =
                            new Lane.STR(csl, cseTag.offset, cseTag.offset, cseTag.width, cseTag.width, cseTag.laneType,
                                cseTag.direction, new DoubleScalar.Abs<FrequencyUnit>(Double.MAX_VALUE,
                                    FrequencyUnit.PER_HOUR), cseTag.speed);
                        cseList.add(lane);
                        lanes.add(lane);
                        linkTag.lanes.put(cseTag.name, lane);
                        if (this.simulator != null)
                        {
                            new LaneAnimation(lane, this.simulator, cseTag.color);
                        }

                        // BLOCK
                        if (linkTag.blockTags.containsKey(cseTag.name))
                        {
                            BlockTag blockTag = linkTag.blockTags.get(cseTag.name);
                            new LaneBlock(lane, blockTag.position, this.simulator, null);
                        }

                        // GENERATOR
                        if (linkTag.generatorTags.containsKey(cseTag.name))
                        {
                            GeneratorTag generatorTag = linkTag.generatorTags.get(cseTag.name);
                            makeGenerator(generatorTag, lane, cseTag.name);
                        }

                        // TODO FILL

                    }
                    break;
                }

                case NOTRAFFICLANE:
                {
                    // TODO Override
                    Lane<?, ?> lane = new NoTrafficLane.STR(csl, cseTag.offset, cseTag.offset, cseTag.width, cseTag.width);
                    cseList.add(lane);
                    if (this.simulator != null)
                    {
                        new LaneAnimation(lane, this.simulator, cseTag.color);
                    }
                    break;
                }

                case SHOULDER:
                {
                    // TODO Override
                    Shoulder shoulder = new Shoulder(csl, cseTag.offset, cseTag.width, cseTag.width);
                    cseList.add(shoulder);
                    if (this.simulator != null)
                    {
                        new ShoulderAnimation(shoulder, this.simulator);
                        // TODO color
                    }
                    break;
                }

                default:
                    throw new SAXException("Unknown Element type: " + cseTag.elementType.toString());
            }
        } // for (CrossSectionElementTag cseTag : roadTypeTag.cseTags.values())

        // make adjacent lanes
        for (int laneIndex = 1; laneIndex < lanes.size(); laneIndex++)
        {
            lanes.get(laneIndex - 1).addAccessibleAdjacentLane(lanes.get(laneIndex), LateralDirectionality.RIGHT);
            lanes.get(laneIndex).addAccessibleAdjacentLane(lanes.get(laneIndex - 1), LateralDirectionality.LEFT);
        }
    }

    /**
     * @param nodeNames the node names as a space-separated String
     * @return a list of NodeTags
     * @throws SAXException when node could not be found
     */
    protected final List<NodeTag> parseNodeList(final String nodeNames) throws SAXException
    {
        List<NodeTag> nodeList = new ArrayList<>();
        String[] ns = nodeNames.split("\\s");
        for (String s : ns)
        {
            if (!this.nodeTags.containsKey(s))
            {
                throw new SAXException("Node " + s + " from node list [" + nodeNames + "] was not defined");
            }
            nodeList.add(this.nodeTags.get(s));
        }
        return nodeList;
    }

    /**
     * @param gtuNames the GTU names as a space-separated String
     * @return a list of GTUTags
     * @throws SAXException when node could not be found
     */
    protected final List<GTUTag> parseGTUList(final String gtuNames) throws SAXException
    {
        List<GTUTag> gtuList = new ArrayList<>();
        String[] ns = gtuNames.split("\\s");
        for (String s : ns)
        {
            if (!this.gtuTags.containsKey(s))
            {
                throw new SAXException("GTU " + s + " from GTU list [" + gtuNames + "] was not defined");
            }
            gtuList.add(this.gtuTags.get(s));
        }
        return gtuList;
    }

    /**
     * Add a compatibility type for this lane type.
     * @param laneTypeTag the parsed LaneTypeTag that contains a relation between a LaneType and one or more GTUTypes.
     */
    protected final void addLaneType(final LaneTypeTag laneTypeTag)
    {
        if (!this.laneTypes.containsKey(laneTypeTag.laneType))
        {
            LaneType<String> laneType = new LaneType<String>(laneTypeTag.laneType);
            this.laneTypes.put(laneTypeTag.laneType, laneType);
        }
        for (GTUTag gtuTag : laneTypeTag.gtuList)
        {
            this.laneTypes.get(laneTypeTag.laneType).addCompatibility(gtuTag.gtuType);
        }
    }

    /**
     * @param typeName the name of the GTU type.
     * @return the GTUType that was retrieved or created.
     */
    protected final GTUType<String> parseGTUType(final String typeName)
    {
        if (!this.gtuTypes.containsKey(typeName))
        {
            GTUType<String> gtuType = GTUType.makeGTUType(typeName);
            this.gtuTypes.put(typeName, gtuType);
        }
        return this.gtuTypes.get(typeName);
    }

    /**
     * XXX probably ok to generate a new model for each GTU 'type'.
     * @param modelName the name of the GTU following model.
     * @return the model.
     * @throws NetworkException in case of unknown model.
     */
    protected final GTUFollowingModel parseFollowingModel(final String modelName) throws NetworkException
    {
        if (modelName.equals("IDM"))
        {
            return new IDM();
        }
        else if (modelName.equals("IDM+"))
        {
            return new IDMPlus();
        }
        throw new NetworkException("Unknown GTU following model: " + modelName);
    }

    /**
     * XXX probably ok to generate a new model for each GTU 'type'.
     * @param modelName the name of the lane change model.
     * @return the model.
     * @throws NetworkException in case of unknown model.
     */
    protected final LaneChangeModel parseLaneChangeModel(final String modelName) throws NetworkException
    {
        if (modelName.equals("EGOISTIC"))
        {
            return new Egoistic();
        }
        else if (modelName.equals("ALTRUISTIC"))
        {
            return new Altruistic();
        }
        throw new NetworkException("Unknown lane change model: " + modelName);
    }

    /**
     * Parse a coordinate with (x,y) or (x,y,z).
     * @param cs the string containing the coordinate.
     * @return a Point3d contaiing the x,y or x,y,z values.
     */
    protected final Point3d parseCoordinate(final String cs)
    {
        String c = cs.replace("(", "");
        c = c.replace(")", "");
        String[] cc = c.split(",");
        double x = Double.parseDouble(cc[0]);
        double y = Double.parseDouble(cc[1]);
        double z = cc.length > 2 ? Double.parseDouble(cc[1]) : 0.0;
        return new Point3d(x, y, z);
    }

    /**
     * @param dirStr the direction.
     * @return the directionality.
     * @throws NetworkException in case of unknown model.
     */
    protected final LongitudinalDirectionality parseDirection(final String dirStr) throws NetworkException
    {
        if (dirStr.equals("FORWARD"))
        {
            return LongitudinalDirectionality.FORWARD;
        }
        else if (dirStr.equals("BACKWARD"))
        {
            return LongitudinalDirectionality.BACKWARD;
        }
        else if (dirStr.equals("BOTH"))
        {
            return LongitudinalDirectionality.BOTH;
        }
        throw new NetworkException("Unknown directionality: " + dirStr);
    }

    /**
     * @param stripeStr the stripe string.
     * @return the stripe type.
     * @throws NetworkException in case of unknown model.
     */
    protected final StripeType parseStripeType(final String stripeStr) throws NetworkException
    {
        if (stripeStr.equals("SOLID"))
        {
            return StripeType.SOLID;
        }
        else if (stripeStr.equals("DASHED"))
        {
            return StripeType.DASHED;
        }
        else if (stripeStr.equals("BLOCKED"))
        {
            return StripeType.BLOCKED;
        }
        else if (stripeStr.equals("DOUBLE"))
        {
            return StripeType.DOUBLE;
        }
        else if (stripeStr.equals("LEFTONLY"))
        {
            return StripeType.LEFTONLY;
        }
        else if (stripeStr.equals("RIGHTONLY"))
        {
            return StripeType.RIGHTONLY;
        }
        throw new NetworkException("Unknown stripe type: " + stripeStr);
    }

    /**
     * @param colorStr the color as a string.
     * @return the color.
     * @throws NetworkException in case of unknown model.
     */
    @SuppressWarnings("checkstyle:needbraces")
    protected final Color parseColor(final String colorStr) throws NetworkException
    {
        if (colorStr.startsWith("#"))
            return Color.decode(colorStr);

        if (colorStr.startsWith("RGB"))
        {
            String c = colorStr.substring(3).replace("(", "").replace(")", "");
            String[] rgb = c.split(",");
            int r = Integer.parseInt(rgb[0].trim());
            int g = Integer.parseInt(rgb[1].trim());
            int b = Integer.parseInt(rgb[2].trim());
            return new Color(r, g, b);
        }

        if (colorStr.equals("BLACK"))
            return Color.BLACK;
        if (colorStr.equals("BLUE"))
            return Color.BLUE;
        if (colorStr.equals("CYAN"))
            return Color.CYAN;
        if (colorStr.equals("DARK_GRAY"))
            return Color.DARK_GRAY;
        if (colorStr.equals("GRAY"))
            return Color.GRAY;
        if (colorStr.equals("GREEN"))
            return Color.GREEN;
        if (colorStr.equals("LIGHT_GRAY"))
            return Color.LIGHT_GRAY;
        if (colorStr.equals("MAGENTA"))
            return Color.MAGENTA;
        if (colorStr.equals("ORANGE"))
            return Color.ORANGE;
        if (colorStr.equals("PINK"))
            return Color.PINK;
        if (colorStr.equals("RED"))
            return Color.RED;
        if (colorStr.equals("WHITE"))
            return Color.WHITE;
        if (colorStr.equals("YELLOW"))
            return Color.YELLOW;

        throw new NetworkException("Unknown color: " + colorStr);
    }

    /**
     * @param s the string to parse
     * @return the unit as a String in the Map.
     * @throws NetworkException when parsing fails
     */
    private String parseAngleUnit(final String s) throws NetworkException
    {
        String u = null;
        for (String us : ANGLE_UNITS.keySet())
        {
            if (s.toString().contains(us))
            {
                if (u == null || us.length() > u.length())
                {
                    u = us;
                }
            }
        }
        if (u == null)
        {
            throw new NetworkException("Parsing network: cannot instantiate angle unit in: " + s);
        }
        return u;
    }

    /**
     * @param s the string to parse
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    protected final DoubleScalar.Abs<AnglePlaneUnit> parseAngleAbs(final String s) throws NetworkException
    {
        String us = parseAngleUnit(s);
        AnglePlaneUnit u = ANGLE_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            DoubleScalar.Abs<AnglePlaneUnit> angle = new DoubleScalar.Abs<AnglePlaneUnit>(value, u);
            return AnglePlaneUnit.normalize(angle);
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network: cannot instantiate scalar: " + s, nfe);
        }
    }

    /**
     * @param s the string to parse
     * @return the unit as a String in the Map.
     * @throws NetworkException when parsing fails
     */
    private String parseSpeedUnit(final String s) throws NetworkException
    {
        String u = null;
        for (String us : SPEED_UNITS.keySet())
        {
            if (s.toString().contains(us))
            {
                if (u == null || us.length() > u.length())
                {
                    u = us;
                }
            }
        }
        if (u == null)
        {
            throw new NetworkException("Parsing network: cannot instantiate speed unit in: " + s);
        }
        return u;
    }

    /**
     * @param s the string to parse
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    protected final DoubleScalar.Abs<SpeedUnit> parseSpeedAbs(final String s) throws NetworkException
    {
        String us = parseSpeedUnit(s);
        SpeedUnit u = SPEED_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            return new DoubleScalar.Abs<SpeedUnit>(value, u);
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network: cannot instantiate scalar: " + s, nfe);
        }
    }

    /**
     * @param s the string to parse
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    protected final DoubleScalar.Rel<SpeedUnit> parseSpeedRel(final String s) throws NetworkException
    {
        String us = parseSpeedUnit(s);
        SpeedUnit u = SPEED_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            return new DoubleScalar.Rel<SpeedUnit>(value, u);
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network: cannot instantiate scalar: " + s, nfe);
        }
    }

    /**
     * @param s the string to parse
     * @return the unit as a String in the Map.
     * @throws NetworkException when parsing fails
     */
    private String parseLengthUnit(final String s) throws NetworkException
    {
        String u = null;
        for (String us : LENGTH_UNITS.keySet())
        {
            if (s.toString().contains(us))
            {
                if (u == null || us.length() > u.length())
                {
                    u = us;
                }
            }
        }
        if (u == null)
        {
            throw new NetworkException("Parsing network: cannot instantiate length unit in: " + s);
        }
        return u;
    }

    /**
     * @param s the string to parse
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    protected final DoubleScalar.Abs<LengthUnit> parseLengthAbs(final String s) throws NetworkException
    {
        String us = parseLengthUnit(s);
        LengthUnit u = LENGTH_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            return new DoubleScalar.Abs<LengthUnit>(value, u);
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network: cannot instantiate scalar: " + s, nfe);
        }
    }

    /**
     * @param s the string to parse
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    protected final DoubleScalar.Rel<LengthUnit> parseLengthRel(final String s) throws NetworkException
    {
        String us = parseLengthUnit(s);
        LengthUnit u = LENGTH_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            return new DoubleScalar.Rel<LengthUnit>(value, u);
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network: cannot instantiate scalar: " + s, nfe);
        }
    }

    /**
     * This method parses a length string that can have values such as: BEGIN, END, 10m, END-10m, 98%.
     * @param posStr the position string to parse. Lengths are relative to the design line.
     * @param linkTag the link to retrieve the design line
     * @return the corresponding position as a length on the design line
     * @throws NetworkException when parsing fails
     */
    protected final DoubleScalar.Rel<LengthUnit> parseBeginEndPosition(final String posStr, final LinkTag linkTag)
        throws NetworkException
    {
        if (posStr.trim().equals("BEGIN"))
        {
            return new DoubleScalar.Rel<LengthUnit>(0.0, LengthUnit.METER);
        }

        double length;
        if (linkTag.arcTag != null)
        {
            length = linkTag.arcTag.radius.getSI() * linkTag.arcTag.angle.getSI();
        }
        else if (linkTag.straightTag != null)
        {
            length = linkTag.straightTag.length.getSI();
        }
        else
        {
            throw new NetworkException("parseBeginEndPosition - attribute POSITION with value " + posStr
                + " invalid for link " + linkTag.name + ": link is neither arc nor straight");
        }

        if (posStr.trim().equals("END"))
        {
            return new DoubleScalar.Rel<LengthUnit>(length, LengthUnit.METER);
        }

        if (posStr.endsWith("%"))
        {
            String s = posStr.substring(0, posStr.length() - 1).trim();
            try
            {
                double fraction = Double.parseDouble(s) / 100.0;
                if (fraction < 0.0 || fraction > 1.0)
                {
                    throw new NetworkException("parseBeginEndPosition: attribute POSITION with value " + posStr
                        + " invalid for link " + linkTag.name + ", should be a percentage between 0 and 100%");
                }
                return new DoubleScalar.Rel<LengthUnit>(length * fraction, LengthUnit.METER);
            }
            catch (NumberFormatException nfe)
            {
                throw new NetworkException("parseBeginEndPosition: attribute POSITION with value " + posStr
                    + " invalid for link " + linkTag.name + ", should be a percentage between 0 and 100%", nfe);
            }
        }

        if (posStr.trim().startsWith("END-"))
        {
            String s = posStr.substring(4).trim();
            double offset = parseLengthRel(s).getSI();
            if (offset > length)
            {
                throw new NetworkException("parseBeginEndPosition - attribute POSITION with value " + posStr
                    + " invalid for link " + linkTag.name + ": provided negative offset greater than than link length");
            }
            return new DoubleScalar.Rel<LengthUnit>(length - offset, LengthUnit.METER);
        }

        DoubleScalar.Rel<LengthUnit> offset = parseLengthRel(posStr);
        if (offset.getSI() > length)
        {
            throw new NetworkException("parseBeginEndPosition - attribute POSITION with value " + posStr
                + " invalid for link " + linkTag.name + ": provided offset greater than than link length");
        }
        return offset;
    }

    /**
     * @param s the string to parse
     * @return the unit as a String in the Map.
     * @throws NetworkException when parsing fails
     */
    private String parseTimeUnit(final String s) throws NetworkException
    {
        String u = null;
        for (String us : TIME_UNITS.keySet())
        {
            if (s.toString().contains(us))
            {
                if (u == null || us.length() > u.length())
                {
                    u = us;
                }
            }
        }
        if (u == null)
        {
            throw new NetworkException("Parsing network: cannot instantiate time unit in: " + s);
        }
        return u;
    }

    /**
     * @param s the string to parse
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    protected final DoubleScalar.Abs<TimeUnit> parseTimeAbs(final String s) throws NetworkException
    {
        String us = parseTimeUnit(s);
        TimeUnit u = TIME_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            return new DoubleScalar.Abs<TimeUnit>(value, u);
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network: cannot instantiate scalar: " + s, nfe);
        }
    }

    /**
     * @param s the string to parse
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    protected final DoubleScalar.Rel<TimeUnit> parseTimeRel(final String s) throws NetworkException
    {
        String us = parseTimeUnit(s);
        TimeUnit u = TIME_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            return new DoubleScalar.Rel<TimeUnit>(value, u);
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network: cannot instantiate scalar: " + s, nfe);
        }
    }

    /**
     * @param s the string to parse
     * @return the unit as a String in the Map.
     * @throws NetworkException when parsing fails
     */
    private String parsePerTimeUnit(final String s) throws NetworkException
    {
        String u = null;
        for (String us : PER_TIME_UNITS.keySet())
        {
            if (s.toString().contains(us))
            {
                if (u == null || us.length() > u.length())
                {
                    u = us;
                }
            }
        }
        if (u == null)
        {
            throw new NetworkException("Parsing network: cannot instantiate per-time unit in: " + s);
        }
        return u;
    }

    /**
     * @param s the string to parse
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    protected final DoubleScalar.Abs<FrequencyUnit> parsePerTimeAbs(final String s) throws NetworkException
    {
        String us = parsePerTimeUnit(s);
        FrequencyUnit u = PER_TIME_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            return new DoubleScalar.Abs<FrequencyUnit>(value, u);
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network: cannot instantiate scalar: " + s, nfe);
        }
    }

    /**
     * @param s the string to parse
     * @return the unit as a String in the Map.
     * @throws NetworkException when parsing fails
     */
    private String parsePerLengthUnit(final String s) throws NetworkException
    {
        String u = null;
        for (String us : PER_LENGTH_UNITS.keySet())
        {
            if (s.toString().contains(us))
            {
                if (u == null || us.length() > u.length())
                {
                    u = us;
                }
            }
        }
        if (u == null)
        {
            throw new NetworkException("Parsing network: cannot instantiate per-length unit in: " + s);
        }
        return u;
    }

    /**
     * @param s the string to parse
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    protected final DoubleScalar.Abs<LinearDensityUnit> parsePerLengthAbs(final String s) throws NetworkException
    {
        String us = parsePerLengthUnit(s);
        LinearDensityUnit u = PER_LENGTH_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            return new DoubleScalar.Abs<LinearDensityUnit>(value, u);
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network: cannot instantiate scalar: " + s, nfe);
        }
    }

    /**
     * parse a set of comma-separated values, e.g., <code>10.0, 4, 5.23</code>.
     * @param s the string to parse.
     * @return array of double values.
     */
    private double[] parseDoubleArgs(final String s)
    {
        String[] ss = s.split(",");
        double[] d = new double[ss.length];
        for (int i = 0; i < ss.length; i++)
        {
            d[i] = Double.parseDouble(ss[i]);
        }
        return d;
    }

    /** TODO include in GLOBAL tag. */
    private static final StreamInterface STREAM = new MersenneTwister();

    /**
     * Parse a continuous distribution.
     * @param ds the name of the distribution, e.g. UNIF.
     * @param args the parameters of the distribution, e.g. {1.0, 2.0}.
     * @return the generated distribution.
     * @throws NetworkException in case distribution unknown or parameter number does not match.
     */
    private DistContinuous makeDistContinuous(final String ds, final double[] args) throws NetworkException
    {
        try
        {
            switch (ds)
            {
                case "CONST":
                case "CONSTANT":
                    return new DistConstant(STREAM, args[0]);

                case "EXPO":
                case "EXPONENTIAL":
                    return new DistExponential(STREAM, args[0]);

                case "TRIA":
                case "TRIANGULAR":
                    return new DistTriangular(STREAM, args[0], args[1], args[2]);

                case "NORM":
                case "NORMAL":
                    return new DistNormal(STREAM, args[0], args[1]);

                case "BETA":
                    return new DistBeta(STREAM, args[0], args[1]);

                case "ERLANG":
                    return new DistErlang(STREAM, (int) args[0], args[1]);

                case "GAMMA":
                    return new DistGamma(STREAM, args[0], args[1]);

                case "LOGN":
                case "LOGNORMAL":
                    return new DistLogNormal(STREAM, args[0], args[1]);

                case "PEARSON5":
                    return new DistPearson5(STREAM, args[0], args[1]);

                case "PEARSON6":
                    return new DistPearson6(STREAM, args[0], args[1], args[2]);

                case "UNIF":
                case "UNIFORM":
                    return new DistUniform(STREAM, args[0], args[1]);

                case "WEIB":
                case "WEIBULL":
                    return new DistWeibull(STREAM, args[0], args[1]);

                default:
                    throw new NetworkException("makeDistContinuous - unknown distribution function " + ds);
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new NetworkException("makeDistContinuous - wrong number of parameters for distribution function " + ds);
        }
    }

    /**
     * Parse a relative length distribution, e.g. <code>UNIFORM(1, 3) m</code>.
     * @param s the string to be parsed.
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    protected final DistContinuousDoubleScalar.Rel<LengthUnit> parseLengthDistRel(final String s) throws NetworkException
    {
        String[] s1 = s.split("\\(");
        String ds = s1[0];
        String[] s2 = s1[1].split("\\)");
        String unit = parseLengthUnit(s2[1]);
        double[] args = parseDoubleArgs(s2[0]);
        DistContinuous dist = makeDistContinuous(ds, args);
        return new DistContinuousDoubleScalar.Rel<LengthUnit>(dist, LENGTH_UNITS.get(unit));
    }

    /**
     * Parse an absolute length distribution, e.g. <code>UNIFORM(1, 3) m</code>.
     * @param s the string to be parsed.
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    protected final DistContinuousDoubleScalar.Abs<LengthUnit> parseLengthDistAbs(final String s) throws NetworkException
    {
        String[] s1 = s.split("\\(");
        String ds = s1[0];
        String[] s2 = s1[1].split("\\)");
        String unit = parseLengthUnit(s2[1]);
        double[] args = parseDoubleArgs(s2[0]);
        DistContinuous dist = makeDistContinuous(ds, args);
        return new DistContinuousDoubleScalar.Abs<LengthUnit>(dist, LENGTH_UNITS.get(unit));
    }

    /**
     * Parse a relative time distribution, e.g. <code>UNIFORM(1, 3) s</code>.
     * @param s the string to be parsed.
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    protected final DistContinuousDoubleScalar.Rel<TimeUnit> parseTimeDistRel(final String s) throws NetworkException
    {
        String[] s1 = s.split("\\(");
        String ds = s1[0];
        String[] s2 = s1[1].split("\\)");
        String unit = parseTimeUnit(s2[1]);
        double[] args = parseDoubleArgs(s2[0]);
        DistContinuous dist = makeDistContinuous(ds, args);
        return new DistContinuousDoubleScalar.Rel<TimeUnit>(dist, TIME_UNITS.get(unit));
    }

    /**
     * Parse an absolute time distribution, e.g. <code>UNIFORM(1, 3) s</code>.
     * @param s the string to be parsed.
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    protected final DistContinuousDoubleScalar.Abs<TimeUnit> parseTimeDistAbs(final String s) throws NetworkException
    {
        String[] s1 = s.split("\\(");
        String ds = s1[0];
        String[] s2 = s1[1].split("\\)");
        String unit = parseTimeUnit(s2[1]);
        double[] args = parseDoubleArgs(s2[0]);
        DistContinuous dist = makeDistContinuous(ds, args);
        return new DistContinuousDoubleScalar.Abs<TimeUnit>(dist, TIME_UNITS.get(unit));
    }

    /**
     * Parse a relative speed distribution, e.g. <code>TRIANGULAR(80, 90, 110) km/h</code>.
     * @param s the string to be parsed.
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    protected final DistContinuousDoubleScalar.Rel<SpeedUnit> parseSpeedDistRel(final String s) throws NetworkException
    {
        String[] s1 = s.split("\\(");
        String ds = s1[0];
        String[] s2 = s1[1].split("\\)");
        String unit = parseSpeedUnit(s2[1]);
        double[] args = parseDoubleArgs(s2[0]);
        DistContinuous dist = makeDistContinuous(ds, args);
        return new DistContinuousDoubleScalar.Rel<SpeedUnit>(dist, SPEED_UNITS.get(unit));
    }

    /**
     * Parse an absolute speed distribution, e.g. <code>TRIANGULAR(80, 90, 110) km/h</code>.
     * @param s the string to be parsed.
     * @return a typed continuous random distribution.
     * @throws NetworkException in case of a parse error.
     */
    protected final DistContinuousDoubleScalar.Abs<SpeedUnit> parseSpeedDistAbs(final String s) throws NetworkException
    {
        String[] s1 = s.split("\\(");
        String ds = s1[0];
        String[] s2 = s1[1].split("\\)");
        String unit = parseSpeedUnit(s2[1]);
        double[] args = parseDoubleArgs(s2[0]);
        DistContinuous dist = makeDistContinuous(ds, args);
        return new DistContinuousDoubleScalar.Abs<SpeedUnit>(dist, SPEED_UNITS.get(unit));
    }

    /*************************************************************************************************/
    /****************************** TAG CLASSES TO KEEP THE XML INFORMATION **************************/
    /*************************************************************************************************/

    /** GLOBAL element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected class GlobalTag
    {
        /** default speed. */
        protected DoubleScalar.Abs<SpeedUnit> speed = null;

        /** default lane width. */
        protected DoubleScalar.Rel<LengthUnit> width = null;
    }

    /** ROADTYPE element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected class RoadTypeTag
    {
        /** name. */
        protected String name = null;

        /** default speed. */
        protected DoubleScalar.Abs<SpeedUnit> speed = null;

        /** default lane width. */
        protected DoubleScalar.Rel<LengthUnit> width = null;

        /** CrossSectionElementTags, order is important, so a LinkedHashMap. */
        protected Map<String, CrossSectionElementTag> cseTags = new LinkedHashMap<>();
    }

    /** element types. */
    @SuppressWarnings({"javadoc", "checkstyle:javadocvariable"})
    protected enum ElementType
    {
        LANE, NOTRAFFICLANE, SHOULDER, STRIPE
    };

    /** stripe types. */
    @SuppressWarnings({"javadoc", "checkstyle:javadocvariable"})
    protected enum StripeType
    {
        SOLID, DASHED, BLOCKED, DOUBLE, LEFTONLY, RIGHTONLY
    };

    /** CROSSSECTION element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected class CrossSectionElementTag implements Cloneable
    {
        /** type. */
        protected ElementType elementType = null;

        /** name. */
        protected String name = null;

        /** lane type name in case elementType is a LANE. */
        protected String laneTypeString = null;

        /** lane type in case elementType is a LANE. */
        protected LaneType<String> laneType = XmlNetworkLaneParserSax.this.noTrafficLaneType;

        /** stripe type. */
        protected StripeType stripeType = null;

        /** offset. */
        protected DoubleScalar.Rel<LengthUnit> offset = null;

        /** speed limit. */
        protected DoubleScalar.Abs<SpeedUnit> speed = null;

        /** lane width. */
        protected DoubleScalar.Rel<LengthUnit> width = null;

        /** direction. */
        protected LongitudinalDirectionality direction;

        /** animation color. */
        protected Color color;
    }

    /** LANEOVERRIDE element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected class LaneOverrideTag
    {
        /** speed limit. */
        protected DoubleScalar.Abs<SpeedUnit> speed = null;

        /** direction. */
        protected LongitudinalDirectionality direction;

        /** animation color. */
        protected Color color;
    }

    /** LINK element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected class LinkTag
    {
        /** name. */
        protected String name = null;

        /** from node. */
        @SuppressWarnings("rawtypes")
        protected Node nodeFrom = null;

        /** to node. */
        @SuppressWarnings("rawtypes")
        protected Node nodeTo = null;

        /** from node name. */
        protected String nodeFromName = null;

        /** to node name. */
        protected String nodeToName = null;

        /** road type. */
        protected RoadTypeTag roadTypeTag = null;

        /** straight. */
        protected StraightTag straightTag = null;

        /** arc. */
        protected ArcTag arcTag = null;

        /** map of lane name to lane override. */
        protected Map<String, LaneOverrideTag> laneOverrideTags = new HashMap<>();

        /** map of lane name to generators. */
        protected Map<String, GeneratorTag> generatorTags = new HashMap<>();

        /** map of lane name to blocks. */
        protected Map<String, BlockTag> blockTags = new HashMap<>();

        /** map of lane name to fill at t=0. */
        protected Map<String, FillTag> fillTags = new HashMap<>();

        /** map of lane name to generated lanes. */
        protected Map<String, Lane> lanes = new HashMap<>();

        /** sink lane names. */
        protected Set<String> sinkLanes = new HashSet<>();
    }

    /** ARC element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected class ArcTag
    {
        /** angle. */
        protected DoubleScalar.Abs<AnglePlaneUnit> angle = null;

        /** radius. */
        protected DoubleScalar.Rel<LengthUnit> radius = null;

        /** direction. */
        protected ArcDirection direction = null;

        /** the center coordinate of the arc. Will be filled after parsing. */
        protected Point3d center;

        /** the startAngle in radians compared to the center coordinate. Will be filled after parsing. */
        protected double startAngle;
    }

    /** STRAIGHT element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected class StraightTag
    {
        /** length. */
        protected DoubleScalar.Rel<LengthUnit> length = null;
    }

    /** NODE element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected class NodeTag
    {
        /** name. */
        String name = null;

        /** coordinate (null at first, can be calculated later when connected to a link. */
        Point3d coordinate = null;

        /** absolute angle of the node. 0 is "East", pi/2 = "North". */
        DoubleScalar.Abs<AnglePlaneUnit> angle = null;

        /** slope as an angle. */
        DoubleScalar.Abs<AngleSlopeUnit> slope = null;
    }

    /** direction of the arc; LEFT or RIGHT. */
    protected enum ArcDirection
    {
        /** Left = counter-clockwise. */
        LEFT,
        /** Right = clockwise. */
        RIGHT;
    }

    /** GTU element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected class GTUTag
    {
        /** name. */
        protected String name = null;

        /** type. */
        protected GTUType<String> gtuType = null;

        /** GTU length. */
        protected DistContinuousDoubleScalar.Rel<LengthUnit> lengthDist = null;

        /** GTU width. */
        protected DistContinuousDoubleScalar.Rel<LengthUnit> widthDist = null;

        /** GTU following model. */
        protected GTUFollowingModel followingModel = null;

        /** lane change model. */
        protected LaneChangeModel laneChangeModel = null;

        /** max speed. */
        protected DistContinuousDoubleScalar.Abs<SpeedUnit> maxSpeedDist = null;
    }

    /** GTUMIX element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected class GTUMixTag
    {
        /** name. */
        protected String name = null;

        /** GTUs. */
        protected List<GTUTag> gtus = new ArrayList<GTUTag>();

        /** weights. */
        protected List<Double> weights = new ArrayList<Double>();
    }

    /** Generator element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected class GeneratorTag
    {
        /** position of the generator on the link, relative to the design line. */
        DoubleScalar.Rel<LengthUnit> position = null;

        /** GTU tag. */
        protected GTUTag gtuTag = null;

        /** GTU mix tag. */
        protected GTUMixTag gtuMixTag = null;

        /** interarrival time. */
        protected DistContinuousDoubleScalar.Rel<TimeUnit> iatDist = null;

        /** initial speed. */
        protected DistContinuousDoubleScalar.Abs<SpeedUnit> initialSpeedDist = null;

        /** max number of generated GTUs. */
        protected int maxGTUs = Integer.MAX_VALUE;

        /** start time of generation. */
        protected DoubleScalar.Abs<TimeUnit> startTime = null;

        /** end time of generation. */
        protected DoubleScalar.Abs<TimeUnit> endTime = null;

        /** Route tag. */
        protected RouteTag routeTag = null;

        /** Route mix tag. */
        protected RouteMixTag routeMixTag = null;

        /** Shortest route tag. */
        protected ShortestRouteTag shortestRouteTag = null;

        /** Shortest route mix tag. */
        protected ShortestRouteMixTag shortestRouteMixTag = null;

        /** GTU colorer. */
        protected String gtuColorer;
    }

    /** ListGenerator element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected class ListGeneratorTag
    {
        /** URI of the list. */
        protected URI uri = null;

        /** position of the generator on the link, relative to the design line. */
        DoubleScalar.Rel<LengthUnit> position = null;

        /** GTU tag. */
        protected GTUTag gtuTag = null;

        /** GTU mix tag. */
        protected GTUMixTag gtuMixTag = null;

        /** initial speed. */
        protected DistContinuousDoubleScalar.Abs<SpeedUnit> initialSpeedDist = null;

        /** GTU colorer. */
        protected String gtuColorer;
    }

    /** Fill element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected class FillTag
    {
        /** GTU tag. */
        protected GTUTag gtuTag = null;

        /** GTU mix tag. */
        protected GTUMixTag gtuMixTag = null;

        /** inter-vehicle distance. */
        protected DistContinuousDoubleScalar.Rel<LengthUnit> distanceDist = null;

        /** initial speed. */
        protected DistContinuousDoubleScalar.Abs<SpeedUnit> initialSpeedDist = null;

        /** max number of generated GTUs. */
        protected int maxGTUs = Integer.MAX_VALUE;

        /** Route tag. */
        protected RouteTag routeTag = null;

        /** Route mix tag. */
        protected RouteMixTag routeMixTag = null;

        /** Shortest route tag. */
        protected ShortestRouteTag shortestRouteTag = null;

        /** Shortest route mix tag. */
        protected ShortestRouteMixTag shortestRouteMixTag = null;
    }

    /** BLOCK element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected class BlockTag
    {
        /** position of the block. */
        DoubleScalar.Rel<LengthUnit> position = null;
    }

    /** ROUTE element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected class RouteTag
    {
        /** name. */
        protected String name = null;

        /** Nodes. */
        protected List<NodeTag> routeNodeTags = new ArrayList<NodeTag>();
    }

    /** SHORTESTROUTE element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected class ShortestRouteTag
    {
        /** name. */
        protected String name = null;

        /** From Node. */
        protected NodeTag from = null;

        /** Via Nodes. */
        protected List<NodeTag> via = new ArrayList<NodeTag>();

        /** To Node. */
        protected NodeTag to = null;

        /** time unit for the "cost" per time. */
        protected DoubleScalar.Abs<FrequencyUnit> costPerTime = null;

        /** distance unit for the "cost" per time. */
        protected DoubleScalar.Abs<LinearDensityUnit> costPerDistance = null;
    }

    /** ROUTEMIX element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected class RouteMixTag
    {
        /** name. */
        protected String name = null;

        /** routes. */
        protected List<RouteTag> routes = new ArrayList<RouteTag>();

        /** weights. */
        protected List<Double> weights = new ArrayList<Double>();
    }

    /** SHORTESTROUTEMIX element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected class ShortestRouteMixTag
    {
        /** name. */
        protected String name = null;

        /** shortest routes. */
        protected List<ShortestRouteTag> shortestRoutes = new ArrayList<ShortestRouteTag>();

        /** weights. */
        protected List<Double> weights = new ArrayList<Double>();
    }

    /** COMPATIBILITY.LANETYPE element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected class LaneTypeTag
    {
        /** lane type name. */
        protected String laneType = null;

        /** compatible gtu types. */
        protected List<GTUTag> gtuList = new ArrayList<GTUTag>();
    }

}
