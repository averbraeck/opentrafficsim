package org.opentrafficsim.core.network.factory;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.vecmath.Point2d;
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
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.IDM;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.gtu.generator.GTUGeneratorIndividual;
import org.opentrafficsim.core.gtu.lane.changing.Altruistic;
import org.opentrafficsim.core.gtu.lane.changing.Egoistic;
import org.opentrafficsim.core.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.animation.LaneAnimation;
import org.opentrafficsim.core.network.animation.ShoulderAnimation;
import org.opentrafficsim.core.network.animation.StripeAnimation;
import org.opentrafficsim.core.network.geotools.LinearGeometry;
import org.opentrafficsim.core.network.geotools.LinkGeotools;
import org.opentrafficsim.core.network.geotools.NodeGeotools;
import org.opentrafficsim.core.network.lane.CrossSectionElement;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.core.network.lane.NoTrafficLane;
import org.opentrafficsim.core.network.lane.Shoulder;
import org.opentrafficsim.core.network.lane.Stripe;
import org.opentrafficsim.core.network.lane.Stripe.Permeable;
import org.opentrafficsim.core.network.point2d.NodePoint2D;
import org.opentrafficsim.core.network.route.FixedRouteGenerator;
import org.opentrafficsim.core.network.route.RouteGenerator;
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
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

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
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Feb 6, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class XmlNetworkLaneParser
{
    /** the ID class of the Network. */
    @SuppressWarnings("visibilitymodifier")
    protected final Class<?> networkIdClass;

    /** the class of the Node. */
    @SuppressWarnings("visibilitymodifier")
    protected final Class<?> nodeClass;

    /** the ID class of the Node. */
    @SuppressWarnings("visibilitymodifier")
    protected final Class<?> nodeIdClass;

    /** the Point class of the Node. */
    @SuppressWarnings("visibilitymodifier")
    protected final Class<?> nodePointClass;

    /** the class of the Link. */
    @SuppressWarnings("visibilitymodifier")
    protected final Class<?> linkClass;

    /** the ID class of the Link. */
    @SuppressWarnings("visibilitymodifier")
    protected final Class<?> linkIdClass;

    /** the generated network. */
    private Network<?, ?> network;

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

    /** the GTUTypes that have been created. */
    @SuppressWarnings("visibilitymodifier")
    protected Map<String, GTUType<String>> gtuTypes = new HashMap<>();

    /** TODO incorporate into grammar. */
    private final LaneType<String> laneType = new LaneType<String>("CarLane");

    /** the simulator for creating the animation. Null if no animation needed. */
    private OTSDEVSSimulatorInterface simulator;

    /** the phase of parsing. 0 = parse nodes; 1 = parse rest of the network. */
    @SuppressWarnings("visibilitymodifier")
    protected int phase = 0;

    /** DEBUG information or not. */
    private static final boolean DEBUG = true;

    static
    {
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
     * @param nodeClass the class of the Node.
     * @param nodeIdClass the ID class of the Node.
     * @param nodePointClass the Point class of the Node.
     * @param linkClass the class of the Link.
     * @param linkIdClass the ID class of the Link.
     * @param simulator the simulator for creating the animation. Null if no animation needed.
     */
    public XmlNetworkLaneParser(final Class<?> networkIdClass, final Class<?> nodeClass, final Class<?> nodeIdClass,
        final Class<?> nodePointClass, final Class<?> linkClass, final Class<?> linkIdClass,
        final OTSDEVSSimulatorInterface simulator)
    {
        this.networkIdClass = networkIdClass;
        this.nodeClass = nodeClass;
        this.nodeIdClass = nodeIdClass;
        this.nodePointClass = nodePointClass;
        this.linkClass = linkClass;
        this.linkIdClass = linkIdClass;
        this.simulator = simulator;
    }

    /**
     * @param url the file with the network in the agreed xml-grammar.
     * @return the network with Nodes, Links, and Lanes.
     * @throws NetworkException in case of parsing problems.
     * @throws SAXException in case of parsing problems.
     * @throws ParserConfigurationException in case of parsing problems.
     * @throws IOException in case of file reading problems.
     */
    public final Network<?, ?> build(final URL url) throws NetworkException, ParserConfigurationException, SAXException,
        IOException
    {
        // parse the Nodes.
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

        // this.network = new Network(makeId(this.networkIdClass, networkId));
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
    public final Network<?, ?> build(final String s) throws NetworkException, ParserConfigurationException, SAXException,
        IOException
    {
        // parse the Nodes.
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

        // this.network = new Network(makeId(this.networkIdClass, networkId));
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
                // phase = 0 means: parse only the node names.
                if (XmlNetworkLaneParser.this.phase == 0)
                {
                    if (qName.equals("NODE") && this.stack.size() > 0 && this.stack.getLast().equals("NETWORK"))
                    {
                        NodeTag nt = new NodeTag();
                        nt.name = attributes.getValue("NAME");
                        XmlNetworkLaneParser.this.nodeTags.put(nt.name.trim(), nt);
                    }
                }

                else

                // phase = 1 means: parse everything.
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

                            case "LINK":
                                switch (qName)
                                {
                                    case "STRAIGHT":
                                        parseStraightTag(attributes);
                                        break;

                                    case "ARC":
                                        parseArcTag(attributes);
                                        break;

                                    case "LANE":
                                        parseLaneTag(attributes);
                                        break;

                                    case "GENERATOR":
                                        parseGeneratorTag(attributes);
                                        break;

                                    case "FILL":
                                        parseFillTag(attributes);
                                        break;

                                    default:
                                        throw new SAXException("LINK: Received start tag " + qName
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

            if (XmlNetworkLaneParser.this.phase == 0)
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

                                case "NODE":
                                    XmlNetworkLaneParser.this.nodeTags.put(this.nodeTag.name, this.nodeTag);
                                    if (this.nodeTag.coordinate != null)
                                    {
                                        // only make a node if we know the coordinate. Otherwise, wait till we can
                                        // calculate it.
                                        @SuppressWarnings("rawtypes")
                                        Node node = makeNode(XmlNetworkLaneParser.this.nodeClass, this.nodeTag);
                                        XmlNetworkLaneParser.this.nodes.put(node.getId().toString(), node);
                                    }
                                    this.nodeTag = null;
                                    break;

                                case "LINK":
                                    calculateNodeCoordinates(this.linkTag);
                                    @SuppressWarnings("rawtypes")
                                    CrossSectionLink link = makeLink(this.linkTag);
                                    parseElements(this.linkTag.elements, link, this.linkTag, this.globalTag);
                                    XmlNetworkLaneParser.this.links.put(link.getId().toString(), link);

                                    // when lane(s) of the link have been built, activate the possible generator(s).
                                    for (LaneTag laneTag : this.linkTag.laneTags.values())
                                    {
                                        for (GeneratorTag generatorTag : laneTag.generatorTags)
                                        {
                                            makeGenerator(generatorTag);
                                        }
                                    }
                                    this.linkTag = null;
                                    break;

                                case "GTU":
                                    break;

                                case "GTUMIX":
                                    this.gtuMixTag = null;
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

                        case "LINK":
                            switch (qName)
                            {
                                case "STRAIGHT":
                                    break;
                                case "ARC":
                                    break;

                                case "LANE":
                                    break;

                                case "GENERATOR":
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
                                    throw new SAXException("GTUMIX: Received start tag " + qName + ", but stack contains: "
                                        + this.stack);
                            }
                            break;

                        case "ROUTEMIX":
                            switch (qName)
                            {
                                case "ROUTE":
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
                                    break;
                                default:
                                    throw new SAXException("SHORTESTROUTEMIX: Received start tag " + qName
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

            if (attributes.getValue("COORDINATE") != null)
                this.nodeTag.coordinate = parseCoordinate(attributes.getValue("COORDINATE"));

            if (attributes.getValue("ANGLE") != null)
                this.nodeTag.angle =
                    new DoubleScalar.Abs<AnglePlaneUnit>(Double.parseDouble(attributes.getValue("ANGLE")),
                        AnglePlaneUnit.DEGREE);
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
                throw new SAXException("NODE: missing attribute NAME");
            this.linkTag.name = name.trim();

            String elements = attributes.getValue("ELEMENTS");
            if (elements == null)
                throw new SAXException("NODE: missing attribute ELEMENTS");
            this.linkTag.elements = attributes.getValue("ELEMENTS").trim();
            String[] nameStrings = elements.split("(\\-)|(\\|)|(\\:)|(\\<)|(\\>)|(\\#)");
            for (String laneName : nameStrings)
            {
                if (laneName.length() > 0 && !laneName.equals("D"))
                {
                    LaneTag laneTag = new LaneTag();
                    laneTag.name = laneName;
                    this.linkTag.laneTags.put(laneName, laneTag);
                }
            }

            String fromNodeStr = attributes.getValue("FROM");
            if (fromNodeStr == null)
                throw new SAXException("NODE: missing attribute FROM for link " + name);
            this.linkTag.nodeFromName = fromNodeStr.trim();
            @SuppressWarnings("rawtypes")
            Node fromNode = XmlNetworkLaneParser.this.nodes.get(fromNodeStr.trim());
            this.linkTag.nodeFrom = fromNode;

            String toNodeStr = attributes.getValue("TO");
            if (toNodeStr == null)
                throw new SAXException("NODE: missing attribute TO for link " + name);
            this.linkTag.nodeToName = toNodeStr.trim();
            @SuppressWarnings("rawtypes")
            Node toNode = XmlNetworkLaneParser.this.nodes.get(toNodeStr.trim());
            this.linkTag.nodeTo = toNode;

            if (attributes.getValue("SPEED") != null)
                this.linkTag.speed = parseSpeedAbs(attributes.getValue("SPEED"));

            if (attributes.getValue("WIDTH") != null)
                this.linkTag.width = parseLengthRel(attributes.getValue("WIDTH"));
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

            XmlNetworkLaneParser.this.gtuTags.put(gtuTag.name, gtuTag);
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
            this.linkTag.arcTag.angle =
                new DoubleScalar.Abs<AnglePlaneUnit>(Double.parseDouble(angle), AnglePlaneUnit.DEGREE);

            String dir = attributes.getValue("DIRECTION");
            if (dir == null)
                throw new SAXException("ARC: missing attribute ANGLE");
            this.linkTag.arcTag.direction =
                (dir.equals("L") || dir.equals("LEFT") || dir.equals("COUNTERCLOCKWISE")) ? ArcDirection.LEFT
                    : ArcDirection.RIGHT;
        }

        /**
         * Parse the LANE tag with lane attributes for a Link.
         * @param attributes the attributes of the XML-tag.
         * @throws NetworkException in case of OTS logic error.
         * @throws SAXException in case of parse error.
         */
        @SuppressWarnings("checkstyle:needbraces")
        private void parseLaneTag(final Attributes attributes) throws NetworkException, SAXException
        {
            String name = attributes.getValue("NAME");
            if (name == null)
                throw new SAXException("LANE: missing attribute NAME");
            LaneTag laneTag = this.linkTag.laneTags.get(name.trim());
            if (laneTag == null)
                throw new NetworkException("LANE: Lane with NAME " + name.trim() + "not found in elements of link "
                    + this.linkTag.name);

            if (attributes.getValue("SPEED") != null)
                laneTag.speed = parseSpeedAbs(attributes.getValue("SPEED"));

            if (attributes.getValue("WIDTH") != null)
                laneTag.width = parseLengthRel(attributes.getValue("WIDTH"));
        }

        /**
         * Parse the GENERATOR tag with GTU generation attributes for a Lane.
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
                throw new SAXException("GENERATOR: missing attribute LANE");
            LaneTag laneTag = this.linkTag.laneTags.get(laneName.trim());
            if (laneTag == null)
                throw new NetworkException("LANE: Lane with NAME " + laneName.trim() + "not found in elements of link "
                    + this.linkTag.name);
            generatorTag.laneTag = laneTag;

            String gtuName = attributes.getValue("GTU");
            if (gtuName != null)
            {
                if (!XmlNetworkLaneParser.this.gtuTags.containsKey(gtuName.trim()))
                    throw new NetworkException("GENERATOR: LANE " + laneName + " GTU " + gtuName.trim() + " in link "
                        + this.linkTag.name + " not defined");
                generatorTag.gtuTag = XmlNetworkLaneParser.this.gtuTags.get(gtuName.trim());
            }

            String gtuMixName = attributes.getValue("GTUMIX");
            if (gtuMixName != null)
            {
                if (!XmlNetworkLaneParser.this.gtuMixTags.containsKey(gtuMixName.trim()))
                    throw new NetworkException("GENERATOR: LANE " + laneName + " GTUMIX " + gtuMixName.trim() + " in link "
                        + this.linkTag.name + " not defined");
                generatorTag.gtuMixTag = XmlNetworkLaneParser.this.gtuMixTags.get(gtuMixName.trim());
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
                if (!XmlNetworkLaneParser.this.routeTags.containsKey(routeName.trim()))
                    throw new NetworkException("GENERATOR: LANE " + laneName + " ROUTE " + routeName.trim() + " in link "
                        + this.linkTag.name + " not defined");
                generatorTag.routeTag = XmlNetworkLaneParser.this.routeTags.get(routeName.trim());
                numberRouteTags++;
            }

            String routeMixName = attributes.getValue("ROUTEMIX");
            if (routeMixName != null)
            {
                if (!XmlNetworkLaneParser.this.routeMixTags.containsKey(routeMixName.trim()))
                    throw new NetworkException("GENERATOR: LANE " + laneName + " ROUTEMIX " + routeMixName.trim()
                        + " in link " + this.linkTag.name + " not defined");
                generatorTag.routeMixTag = XmlNetworkLaneParser.this.routeMixTags.get(routeMixName.trim());
                numberRouteTags++;
            }

            String shortestRouteName = attributes.getValue("SHORTESTROUTE");
            if (shortestRouteName != null)
            {
                if (!XmlNetworkLaneParser.this.shortestRouteTags.containsKey(shortestRouteName.trim()))
                    throw new NetworkException("GENERATOR: LANE " + laneName + " SHORTESTROUTE " + shortestRouteName.trim()
                        + " in link " + this.linkTag.name + " not defined");
                generatorTag.shortestRouteTag = XmlNetworkLaneParser.this.shortestRouteTags.get(shortestRouteName.trim());
                numberRouteTags++;
            }

            String shortestRouteMixName = attributes.getValue("SHORTESTROUTEMIX");
            if (shortestRouteMixName != null)
            {
                if (!XmlNetworkLaneParser.this.shortestRouteMixTags.containsKey(shortestRouteMixName.trim()))
                    throw new NetworkException("GENERATOR: LANE " + laneName + " SHORTESTROUTEMIX "
                        + shortestRouteMixName.trim() + " in link " + this.linkTag.name + " not defined");
                generatorTag.shortestRouteMixTag =
                    XmlNetworkLaneParser.this.shortestRouteMixTags.get(shortestRouteMixName.trim());
                numberRouteTags++;
            }

            if (numberRouteTags > 1)
                throw new SAXException("GENERATOR: multiple ROUTE tags defined for Lane with NAME " + laneName + " of link "
                    + this.linkTag.name);

            generatorTag.laneTag.generatorTags.add(generatorTag);
        }

        /**
         * Parse the FILL tag with GTU fill attributes for a Lane.
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
                throw new SAXException("FILL: missing attribute LANE");
            if (!this.linkTag.laneTags.containsKey(laneName.trim()))
                throw new NetworkException("FILL: LANE " + laneName.trim() + " not defined in link " + this.linkTag.name);
            fillTag.laneTag = this.linkTag.laneTags.get(laneName.trim());

            String gtuName = attributes.getValue("GTU");
            if (gtuName != null)
            {
                if (!XmlNetworkLaneParser.this.gtuTags.containsKey(gtuName.trim()))
                    throw new NetworkException("FILL: LANE " + laneName + " GTU " + gtuName.trim() + " in link "
                        + this.linkTag.name + " not defined");
                fillTag.gtuTag = XmlNetworkLaneParser.this.gtuTags.get(gtuName.trim());
            }

            String gtuMixName = attributes.getValue("GTUMIX");
            if (gtuMixName != null)
            {
                if (!XmlNetworkLaneParser.this.gtuMixTags.containsKey(gtuMixName.trim()))
                    throw new NetworkException("FILL: LANE " + laneName + " GTUMIX " + gtuMixName.trim() + " in link "
                        + this.linkTag.name + " not defined");
                fillTag.gtuMixTag = XmlNetworkLaneParser.this.gtuMixTags.get(gtuMixName.trim());
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
                if (!XmlNetworkLaneParser.this.routeTags.containsKey(routeName.trim()))
                    throw new NetworkException("FILL: LANE " + laneName + " ROUTE " + routeName.trim() + " in link "
                        + this.linkTag.name + " not defined");
                fillTag.routeTag = XmlNetworkLaneParser.this.routeTags.get(routeName.trim());
                numberRouteTags++;
            }

            String routeMixName = attributes.getValue("ROUTEMIX");
            if (routeMixName != null)
            {
                if (!XmlNetworkLaneParser.this.routeMixTags.containsKey(routeMixName.trim()))
                    throw new NetworkException("FILL: LANE " + laneName + " ROUTEMIX " + routeMixName.trim() + " in link "
                        + this.linkTag.name + " not defined");
                fillTag.routeMixTag = XmlNetworkLaneParser.this.routeMixTags.get(routeMixName.trim());
                numberRouteTags++;
            }

            String shortestRouteName = attributes.getValue("SHORTESTROUTE");
            if (shortestRouteName != null)
            {
                if (!XmlNetworkLaneParser.this.shortestRouteTags.containsKey(shortestRouteName.trim()))
                    throw new NetworkException("FILL: LANE " + laneName + " SHORTESTROUTE " + shortestRouteName.trim()
                        + " in link " + this.linkTag.name + " not defined");
                fillTag.shortestRouteTag = XmlNetworkLaneParser.this.shortestRouteTags.get(shortestRouteName.trim());
                numberRouteTags++;
            }

            String shortestRouteMixName = attributes.getValue("SHORTESTROUTEMIX");
            if (shortestRouteMixName != null)
            {
                if (!XmlNetworkLaneParser.this.shortestRouteMixTags.containsKey(shortestRouteMixName.trim()))
                    throw new NetworkException("FILL: LANE " + laneName + " SHORTESTROUTEMIX " + shortestRouteMixName.trim()
                        + " in link " + this.linkTag.name + " not defined");
                fillTag.shortestRouteMixTag =
                    XmlNetworkLaneParser.this.shortestRouteMixTags.get(shortestRouteMixName.trim());
                numberRouteTags++;
            }

            if (numberRouteTags > 1)
                throw new SAXException("FILL: multiple ROUTE tags defined for Lane with NAME " + laneName + " of link "
                    + this.linkTag.name);

            fillTag.laneTag.fillTags.add(fillTag);
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

            XmlNetworkLaneParser.this.gtuMixTags.put(name.trim(), this.gtuMixTag);
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
            if (!XmlNetworkLaneParser.this.gtuTags.containsKey(gtuName.trim()))
                throw new NetworkException("GTUMIX: " + this.gtuMixTag.name + " GTU " + gtuName.trim() + " not defined");
            this.gtuMixTag.gtus.add(XmlNetworkLaneParser.this.gtuTags.get(gtuName.trim()));

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

            String routeNodes = attributes.getValue("NODELIST");
            if (routeNodes == null)
                throw new SAXException("ROUTE " + name.trim() + ": missing attribute NODELIST");
            routeTag.routeNodeTags = parseNodeList(routeNodes);

            XmlNetworkLaneParser.this.routeTags.put(name.trim(), routeTag);
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

            XmlNetworkLaneParser.this.routeMixTags.put(name.trim(), this.routeMixTag);
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
            if (!XmlNetworkLaneParser.this.routeTags.containsKey(routeName.trim()))
                throw new NetworkException("ROUTEMIX: " + this.routeMixTag.name + " ROUTE " + routeName.trim()
                    + " not defined");
            this.routeMixTag.routes.add(XmlNetworkLaneParser.this.routeTags.get(routeName.trim()));

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

            String fromNode = attributes.getValue("FROM");
            if (fromNode == null)
                throw new SAXException("SHORTESTROUTE: missing attribute FROM");
            if (!XmlNetworkLaneParser.this.nodeTags.containsKey(fromNode.trim()))
                throw new SAXException("SHORTESTROUTE " + name + ": FROM node " + fromNode.trim() + " not found");
            shortestRouteTag.from = XmlNetworkLaneParser.this.nodeTags.get(fromNode.trim());

            String viaNodes = attributes.getValue("NODELIST");
            if (viaNodes != null)
                shortestRouteTag.via = parseNodeList(viaNodes);

            String toNode = attributes.getValue("TO");
            if (toNode == null)
                throw new SAXException("SHORTESTROUTE: missing attribute TO");
            if (!XmlNetworkLaneParser.this.nodeTags.containsKey(toNode.trim()))
                throw new SAXException("SHORTESTROUTE " + name + ": TO node " + toNode.trim() + " not found");
            shortestRouteTag.to = XmlNetworkLaneParser.this.nodeTags.get(toNode.trim());

            String distanceCost = attributes.getValue("DISTANCECOST");
            if (distanceCost == null)
                throw new SAXException("SHORTESTROUTE: missing attribute DISTANCECOST");
            shortestRouteTag.costPerDistance = parsePerLengthAbs(distanceCost);

            String timeCost = attributes.getValue("TIMECOST");
            if (timeCost == null)
                throw new SAXException("SHORTESTROUTE: missing attribute TIMECOST");
            shortestRouteTag.costPerTime = parsePerTimeAbs(timeCost);

            XmlNetworkLaneParser.this.shortestRouteTags.put(name.trim(), shortestRouteTag);
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

            XmlNetworkLaneParser.this.shortestRouteMixTags.put(name.trim(), this.shortestRouteMixTag);
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
            if (!XmlNetworkLaneParser.this.shortestRouteTags.containsKey(shortestRouteName.trim()))
                throw new NetworkException("SHORTESTROUTEMIX: " + this.shortestRouteMixTag.name + " SHORTESTROUTE "
                    + shortestRouteName.trim() + " not defined");
            this.shortestRouteMixTag.shortestRoutes.add(XmlNetworkLaneParser.this.shortestRouteTags.get(shortestRouteName
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
     * Generate an ID of the right type.
     * @param clazz the class to instantiate.
     * @param p the point as a String.
     * @return the object as an instance of the right class.
     * @throws NetworkException when point cannot be instantiated
     */
    protected final Object makePoint(final Class<?> clazz, final Point3d p) throws NetworkException
    {
        Object point = null;
        if (Point3d.class.isAssignableFrom(clazz))
        {
            point = p;
        }
        else if (Point2D.class.isAssignableFrom(clazz))
        {
            point = new Point2D.Double(p.x, p.y);
        }
        else if (Point2d.class.isAssignableFrom(clazz))
        {
            point = new Point2d(new double[] {p.x, p.y});
        }
        else if (Coordinate.class.isAssignableFrom(clazz))
        {
            point = new Coordinate(p.x, p.y, p.z);
        }
        else
        {
            throw new NetworkException("Parsing network. Point class " + clazz.getName() + ": cannot instantiate.");
        }
        return point;
    }

    /**
     * @param clazz the node class
     * @param nodeTag the tag with the infor for the node.
     * @return a constructed node
     * @throws NetworkException when point cannot be instantiated
     * @throws NamingException when animation context cannot be found.
     * @throws RemoteException when communication error occurs when trying to find animation context.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected final Node makeNode(final Class<?> clazz, final NodeTag nodeTag) throws NetworkException, RemoteException,
        NamingException
    {
        Object id = makeId(this.nodeIdClass, nodeTag.name);
        Object point = makePoint(this.nodePointClass, nodeTag.coordinate);
        DoubleScalar.Abs<AnglePlaneUnit> angle =
            nodeTag.angle == null ? new DoubleScalar.Abs<AnglePlaneUnit>(0.0, AnglePlaneUnit.SI) : nodeTag.angle;
        DoubleScalar.Abs<AngleSlopeUnit> slope =
            nodeTag.slope == null ? new DoubleScalar.Abs<AngleSlopeUnit>(0.0, AngleSlopeUnit.SI) : nodeTag.slope;
        if (NodeGeotools.class.isAssignableFrom(clazz))
        {
            if (point instanceof Coordinate)
            {
                Node node = new NodeGeotools(id, (Coordinate) point, angle, slope);
                this.nodes.put(id.toString(), node);
                return node;
            }
            throw new NetworkException("Parsing network. Node class " + clazz.getName()
                + ": cannot instantiate. Wrong Coordinate type: " + point.getClass() + ", coordinate: " + point);
        }
        else if (NodePoint2D.class.isAssignableFrom(clazz))
        {
            if (point instanceof Point2D)
            {
                Node node = new NodePoint2D(id, (Point2D) point, angle, slope);
                this.nodes.put(id.toString(), node);
                return node;
            }
            throw new NetworkException("Parsing network. Node class " + clazz.getName()
                + ": cannot instantiate. Wrong Point2D type: " + point.getClass() + ", coordinate: " + point);
        }
        else
        {
            throw new NetworkException("Parsing network. Node class " + clazz.getName() + ": cannot instantiate.");
        }
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
                Node node = makeNode(this.nodeClass, nodeTag);
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
                Node node = makeNode(this.nodeClass, nodeTag);
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
                Point3d coordinate =
                    new Point3d(linkTag.nodeFrom.getLocation().getX(), linkTag.nodeFrom.getLocation().getY(),
                        linkTag.nodeFrom.getLocation().getZ());
                double startAngle = linkTag.nodeFrom.getDirection().getSI();
                double slope = linkTag.nodeFrom.getSlope().getSI();
                double lengthSI = radiusSI * angle;
                if (direction.equals(ArcDirection.LEFT))
                {
                    linkTag.arcTag.center =
                        new Point3d(coordinate.x + radiusSI * Math.cos(startAngle + Math.PI / 2.0), coordinate.y + radiusSI
                            * Math.sin(startAngle + Math.PI / 2.0), 0.0);
                    linkTag.arcTag.startAngle = startAngle - Math.PI / 2.0;
                    coordinate.x = linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle + angle);
                    coordinate.y = linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle + angle);
                }
                else
                {
                    linkTag.arcTag.center =
                        new Point3d(coordinate.x + radiusSI * Math.cos(startAngle - Math.PI / 2.0), coordinate.y + radiusSI
                            * Math.sin(startAngle - Math.PI / 2.0), 0.0);
                    linkTag.arcTag.startAngle = startAngle + Math.PI / 2.0;
                    coordinate.x = linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle - angle);
                    coordinate.y = linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle - angle);
                }
                coordinate.z += lengthSI * Math.sin(slope);
                NodeTag nodeTag = this.nodeTags.get(linkTag.nodeToName);
                nodeTag.angle = new DoubleScalar.Abs<AnglePlaneUnit>(norm(startAngle - angle), AnglePlaneUnit.SI);
                nodeTag.coordinate = coordinate;
                nodeTag.slope = new DoubleScalar.Abs<AngleSlopeUnit>(slope, AngleSlopeUnit.SI);
                @SuppressWarnings("rawtypes")
                Node node = makeNode(this.nodeClass, nodeTag);
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
                        new DoubleScalar.Abs<AnglePlaneUnit>(norm(linkTag.arcTag.startAngle + Math.PI / 2.0),
                            AnglePlaneUnit.SI);
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
                        new DoubleScalar.Abs<AnglePlaneUnit>(norm(linkTag.arcTag.startAngle - Math.PI / 2.0),
                            AnglePlaneUnit.SI);
                }
                coordinate.z -= lengthSI * Math.sin(slope);
                nodeTag.coordinate = coordinate;
                nodeTag.slope = new DoubleScalar.Abs<AngleSlopeUnit>(slope, AngleSlopeUnit.SI);
                @SuppressWarnings("rawtypes")
                Node node = makeNode(this.nodeClass, nodeTag);
                linkTag.nodeFrom = node;
            }
        }

    }

    // FIXME put in utility class. Also exists in CrossSectionElement.
    /**
     * normalize an angle between 0 and 2 * PI.
     * @param angle original angle.
     * @return angle between 0 and 2 * PI.
     */
    private double norm(final double angle)
    {
        double normalized = angle % (2 * Math.PI);
        if (normalized < 0.0)
        {
            normalized += 2 * Math.PI;
        }
        return normalized;
    }

    /**
     * FIXME LinkGeotools should extend CrossSectionLink and not the other way around.
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
            if (LinkGeotools.class.isAssignableFrom(this.linkClass))
            {
                Object id = makeId(this.linkIdClass, linkTag.name);
                DoubleScalar.Rel<LengthUnit> length = null;
                LinearGeometry geometry = null;
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
                if (linkTag.straightTag != null)
                {
                    length = linkTag.straightTag.length;
                }
                else if (linkTag.arcTag != null)
                {
                    length =
                        new DoubleScalar.Rel<LengthUnit>(linkTag.arcTag.radius.getInUnit() * linkTag.arcTag.angle.getSI(),
                            linkTag.arcTag.radius.getUnit());
                    double angleStep = linkTag.arcTag.angle.getSI() / points;
                    double slopeStep = (to.coordinate.z - from.coordinate.z) / points;
                    double radiusSI = linkTag.arcTag.radius.getSI();
                    if (linkTag.arcTag.direction.equals(ArcDirection.RIGHT))
                    {
                        for (int p = 1; p < points - 1; p++)
                        {
                            coordinates[p] =
                                new Coordinate(linkTag.arcTag.center.x + radiusSI
                                    * Math.cos(linkTag.arcTag.startAngle - angleStep * p), linkTag.arcTag.center.y
                                    + radiusSI * Math.sin(linkTag.arcTag.startAngle - angleStep * p), from.coordinate.z
                                    + slopeStep * p);
                        }
                    }
                    else
                    {
                        for (int p = 1; p < points - 1; p++)
                        {
                            coordinates[p] =
                                new Coordinate(linkTag.arcTag.center.x + radiusSI
                                    * Math.cos(linkTag.arcTag.startAngle + angleStep * p), linkTag.arcTag.center.y
                                    + radiusSI * Math.sin(linkTag.arcTag.startAngle + angleStep * p), from.coordinate.z
                                    + slopeStep * p);
                        }
                    }
                }
                CrossSectionLink link =
                    new CrossSectionLink(id, (NodeGeotools) linkTag.nodeFrom, (NodeGeotools) linkTag.nodeTo, length);
                GeometryFactory factory = new GeometryFactory();
                LineString lineString = factory.createLineString(coordinates);
                geometry = new LinearGeometry(link, lineString, null);
                link.setGeometry(geometry);
                return link;
            }
            else
            {
                throw new SAXException("Parsing network. Link class " + this.linkClass.getName() + ": cannot instantiate.");
            }
        }
        catch (NetworkException ne)
        {
            throw new SAXException("Error building Link", ne);
        }
    }

    /**
     * Make a generator.
     * @param generatorTag
     * @throws SimRuntimeException
     * @throws RemoteException
     */
    private void makeGenerator(final GeneratorTag generatorTag) throws SimRuntimeException, RemoteException
    {
        Class<?> gtuClass = LaneBasedIndividualCar.class;
        List<Node<?, ?>> nodeList = new ArrayList<>();
        for (NodeTag nodeTag : generatorTag.routeTag.routeNodeTags)
        {
            nodeList.add(this.nodes.get(nodeTag.name));
        }
        DoubleScalar.Abs<TimeUnit> startTime =
            generatorTag.startTime != null ? generatorTag.startTime : new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SI);
        DoubleScalar.Abs<TimeUnit> endTime =
            generatorTag.endTime != null ? generatorTag.endTime : new DoubleScalar.Abs<TimeUnit>(Double.MAX_VALUE,
                TimeUnit.SI);
        RouteGenerator rg = new FixedRouteGenerator(nodeList);
        new GTUGeneratorIndividual<String>(generatorTag.laneTag.name, this.simulator, generatorTag.gtuTag.gtuType, gtuClass,
            generatorTag.gtuTag.followingModel, generatorTag.gtuTag.laneChangeModel, generatorTag.initialSpeedDist,
            generatorTag.iatDist, generatorTag.gtuTag.lengthDist, generatorTag.gtuTag.widthDist,
            generatorTag.gtuTag.maxSpeedDist, generatorTag.maxGTUs, startTime, endTime, (Lane) generatorTag.laneTag.cse, rg);
    }

    /**
     * @param elements the string such as "X1|V2:V1|D|A1:A2|:A3|X2"
     * @param csl the cross-section link to which the cross-section elements belong.
     * @param linkTag the link with possible information about speed and width.
     * @param globalTag the global tag with possible information about speed and width.
     * @return a list of cross-section elements
     * @throws SAXException for unknown lane type or other inconsistencies.
     * @throws NamingException when animation context cannot be found.
     * @throws RemoteException when animation context cannot be reached.
     * @throws NetworkException
     */
    @SuppressWarnings({"rawtypes", "checkstyle:methodlength"})
    protected final List<CrossSectionElement> parseElements(final String elements, final CrossSectionLink csl,
        final LinkTag linkTag, final GlobalTag globalTag) throws SAXException, RemoteException, NamingException,
        NetworkException
    {
        List<CrossSectionElement> cseList = new ArrayList<>();
        Set<Character> stripeSet = new HashSet<>();
        stripeSet.add('<');
        stripeSet.add('>');
        stripeSet.add('-');
        stripeSet.add(':');
        stripeSet.add('|');
        stripeSet.add('#');

        String[] nameStrings = elements.split("(\\-)|(\\|)|(\\:)|(\\<)|(\\>)|(\\#)");

        List<String> names = new ArrayList<>();
        for (String s : nameStrings)
        {
            if (s.length() > 0) // to take out potential empty strings at the start and end.
            {
                names.add(s);
                int i = elements.indexOf(s);
                if (i == -1)
                {
                    throw new SAXException("Inconsistent elements tag " + elements + " - could not find name " + s);
                }
            }
        }

        List<Double> widthsSI = new ArrayList<>();
        int designIndex = -1;
        int i = -1;
        for (String name : names)
        {
            i++;
            if (name.equals("D")) // TODO design line in the middle of a lane (AD, XD, VD, SD)
            {
                widthsSI.add(0.0);
                designIndex = i;
            }
            else
            {
                LaneTag laneTag = linkTag.laneTags.get(name);
                if (laneTag == null)
                {
                    laneTag = new LaneTag();
                    laneTag.name = name;
                    linkTag.laneTags.put(laneTag.name, laneTag);
                }
                if (laneTag.width != null)
                {
                    widthsSI.add(laneTag.width.getSI());
                }
                else if (linkTag.width != null)
                {
                    widthsSI.add(linkTag.width.getSI());
                }
                else if (globalTag != null && globalTag.width != null)
                {
                    widthsSI.add(globalTag.width.getSI());
                }
                else
                {
                    throw new SAXException("width not set for lane type in " + elements + ": " + name.charAt(0));
                }
            }
        }

        // TODO tapered and design line offset changes.
        double[] offsetSI = new double[widthsSI.size()];
        double cumSI = 0.0;
        for (int j = designIndex; j >= 0; j--)
        {
            offsetSI[j] = cumSI;
            cumSI = cumSI - widthsSI.get(j) / 2.0 - ((j > 0) ? widthsSI.get(j - 1) / 2.0 : 0.0);
        }
        cumSI = 0.0;
        for (int j = designIndex; j < widthsSI.size(); j++)
        {
            offsetSI[j] = cumSI;
            cumSI = cumSI + widthsSI.get(j) / 2.0 + ((j < widthsSI.size() - 1) ? widthsSI.get(j + 1) / 2.0 : 0.0);
        }

        String s = elements;
        i = -1;
        double posSI = offsetSI[0] - widthsSI.get(0) / 2.0;
        while (s.length() > 0)
        {
            if (stripeSet.contains(s.charAt(0)))
            {
                DoubleScalar.Rel<LengthUnit> lateralCenterPosition = new DoubleScalar.Rel<LengthUnit>(posSI, LengthUnit.SI);
                DoubleScalar.Rel<LengthUnit> width = new DoubleScalar.Rel<LengthUnit>(0.1, LengthUnit.METER);
                switch (s.charAt(0))
                {
                    case '|':
                        Stripe solidLine = new Stripe(csl, lateralCenterPosition, width);
                        if (this.simulator != null)
                        {
                            new StripeAnimation(solidLine, this.simulator, StripeAnimation.TYPE.SOLID);
                        }
                        cseList.add(solidLine);
                        break;

                    case '<':
                        Stripe leftOnlyLine = new Stripe(csl, lateralCenterPosition, width);
                        leftOnlyLine.addPermeability(GTUType.ALL, Permeable.LEFT); // TODO: correct?
                        if (this.simulator != null)
                        {
                            new StripeAnimation(leftOnlyLine, this.simulator, StripeAnimation.TYPE.LEFTONLY);
                        }
                        cseList.add(leftOnlyLine);
                        break;

                    case '>':
                        Stripe rightOnlyLine = new Stripe(csl, lateralCenterPosition, width);
                        rightOnlyLine.addPermeability(GTUType.ALL, Permeable.RIGHT); // TODO: correct?
                        if (this.simulator != null)
                        {
                            new StripeAnimation(rightOnlyLine, this.simulator, StripeAnimation.TYPE.RIGHTONLY);
                        }
                        cseList.add(rightOnlyLine);
                        break;

                    case ':':
                        Stripe dashedLine = new Stripe(csl, lateralCenterPosition, width);
                        dashedLine.addPermeability(GTUType.ALL, Permeable.BOTH);
                        if (this.simulator != null)
                        {
                            new StripeAnimation(dashedLine, this.simulator, StripeAnimation.TYPE.DASHED);
                        }
                        cseList.add(dashedLine);
                        break;

                    case '#':
                        width = new DoubleScalar.Rel<LengthUnit>(0.2, LengthUnit.METER);
                        Stripe doubleLine = new Stripe(csl, lateralCenterPosition, width);
                        if (this.simulator != null)
                        {
                            new StripeAnimation(doubleLine, this.simulator, StripeAnimation.TYPE.DOUBLE);
                        }
                        cseList.add(doubleLine);
                        break;

                    default:
                        // TODO: what about permeability if there is no line?
                        break;
                }
                s = s.substring(1);
            }

            else

            {
                i++;
                String name = names.get(i);
                posSI += widthsSI.get(i);
                if (!s.startsWith(name))
                {
                    throw new SAXException("When parsing elements " + elements + ", expected " + name + " at start of " + s);
                }
                s = s.substring(name.length());

                LongitudinalDirectionality ld = null;
                if (name.startsWith("A")) // lane going in the design direction
                {
                    ld = LongitudinalDirectionality.FORWARD;
                }
                else if (name.startsWith("V")) // lane going in the opposite direction
                {
                    ld = LongitudinalDirectionality.BACKWARD;
                }
                else if (name.startsWith("B")) // lane going in both directions
                {
                    ld = LongitudinalDirectionality.BOTH;
                }
                else if (name.startsWith("X")) // forbidden lane (e.g., emergency lane)
                {
                    ld = LongitudinalDirectionality.NONE;
                }
                else if (name.startsWith("S")) // forbidden lane (e.g., grass)
                {
                    ld = LongitudinalDirectionality.NONE;
                }
                else if (name.equals("D")) // design line
                {
                    ld = LongitudinalDirectionality.NONE;
                }
                else
                {
                    throw new SAXException("unknown lane type in " + elements + ": " + name.charAt(0));
                }

                try
                {
                    if (ld.equals(LongitudinalDirectionality.NONE))
                    {
                        if (name.startsWith("S"))
                        {
                            Shoulder shoulder =
                                new Shoulder(csl, new DoubleScalar.Rel<LengthUnit>(offsetSI[i], LengthUnit.SI),
                                    new DoubleScalar.Rel<LengthUnit>(widthsSI.get(i), LengthUnit.SI),
                                    new DoubleScalar.Rel<LengthUnit>(widthsSI.get(i), LengthUnit.SI));
                            linkTag.laneTags.get(name).cse = shoulder;
                            cseList.add(shoulder);
                            if (this.simulator != null)
                            {
                                new ShoulderAnimation(shoulder, this.simulator);
                            }
                        }
                        else if (name.startsWith("X"))
                        {
                            DoubleScalar.Abs<SpeedUnit> speedLimit =
                                new DoubleScalar.Abs<SpeedUnit>(250.0, SpeedUnit.KM_PER_HOUR);
                            if (linkTag.laneTags.get(name).speed != null)
                            {
                                speedLimit = linkTag.laneTags.get(name).speed;
                            }
                            else if (globalTag.speed != null)
                            {
                                speedLimit = globalTag.speed;
                            }
                            Lane lane =
                                new NoTrafficLane(csl, new DoubleScalar.Rel<LengthUnit>(offsetSI[i], LengthUnit.SI),
                                    new DoubleScalar.Rel<LengthUnit>(offsetSI[i], LengthUnit.SI),
                                    new DoubleScalar.Rel<LengthUnit>(widthsSI.get(i), LengthUnit.SI),
                                    new DoubleScalar.Rel<LengthUnit>(widthsSI.get(i), LengthUnit.SI), this.laneType, ld,
                                    new DoubleScalar.Abs<FrequencyUnit>(0.0, FrequencyUnit.PER_HOUR), speedLimit);
                            linkTag.laneTags.get(name).cse = lane;
                            cseList.add(lane);
                            if (this.simulator != null)
                            {
                                new LaneAnimation(lane, this.simulator, Color.LIGHT_GRAY);
                            }
                        }
                    }
                    else
                    {
                        DoubleScalar.Abs<SpeedUnit> speedLimit =
                            new DoubleScalar.Abs<SpeedUnit>(250.0, SpeedUnit.KM_PER_HOUR);
                        if (linkTag.laneTags.get(name).speed != null)
                        {
                            speedLimit = linkTag.laneTags.get(name).speed;
                        }
                        else if (globalTag.speed != null)
                        {
                            speedLimit = globalTag.speed;
                        }
                        Lane lane =
                            new Lane(csl, new DoubleScalar.Rel<LengthUnit>(offsetSI[i], LengthUnit.SI),
                                new DoubleScalar.Rel<LengthUnit>(offsetSI[i], LengthUnit.SI),
                                new DoubleScalar.Rel<LengthUnit>(widthsSI.get(i), LengthUnit.SI),
                                new DoubleScalar.Rel<LengthUnit>(widthsSI.get(i), LengthUnit.SI), this.laneType, ld,
                                new DoubleScalar.Abs<FrequencyUnit>(Double.MAX_VALUE, FrequencyUnit.PER_HOUR), speedLimit);
                        linkTag.laneTags.get(name).cse = lane;
                        cseList.add(lane);
                        if (this.simulator != null)
                        {
                            new LaneAnimation(lane, this.simulator, Color.GRAY);
                        }
                    }
                }
                catch (NetworkException ne)
                {
                    throw new SAXException(ne);
                }
            }
        }

        return cseList;

        /*-
        i = -1;
        for (String name : names)
        {
            i++;
            LongitudinalDirectionality ld = null;
            if (name.startsWith("A")) // lane going in the design direction
            {
                ld = LongitudinalDirectionality.FORWARD;
            }
            else if (name.startsWith("V")) // lane going in the opposite direction
            {
                ld = LongitudinalDirectionality.BACKWARD;
            }
            else if (name.startsWith("B")) // lane going in both directions
            {
                ld = LongitudinalDirectionality.BOTH;
            }
            else if (name.startsWith("X")) // forbidden lane (e.g., emergency lane)
            {
                ld = LongitudinalDirectionality.NONE;
            }
            else if (name.startsWith("S")) // forbidden lane (e.g., grass)
            {
                ld = LongitudinalDirectionality.NONE;
            }
            else if (name.equals("D")) // design line
            {
                ld = LongitudinalDirectionality.NONE;
            }
            else
            {
                throw new SAXException("unknown lane type in " + elements + ": " + name.charAt(0));
            }

            try
            {
                if (ld.equals(LongitudinalDirectionality.NONE))
                {
                    if (name.startsWith("S"))
                    {
                        Shoulder shoulder =
                            new Shoulder(csl, new DoubleScalar.Rel<LengthUnit>(offsetSI[i], LengthUnit.SI),
                                new DoubleScalar.Rel<LengthUnit>(widthsSI.get(i), LengthUnit.SI),
                                new DoubleScalar.Rel<LengthUnit>(widthsSI.get(i), LengthUnit.SI));
                        linkTag.laneTags.get(name).cse = shoulder;
                        cseList.add(shoulder);
                        if (this.simulator != null)
                        {
                            new ShoulderAnimation(shoulder, this.simulator);
                        }
                    }
                    else if (name.startsWith("X"))
                    {
                        Lane lane =
                            new NoTrafficLane(csl, new DoubleScalar.Rel<LengthUnit>(offsetSI[i], LengthUnit.SI),
                                new DoubleScalar.Rel<LengthUnit>(offsetSI[i], LengthUnit.SI),
                                new DoubleScalar.Rel<LengthUnit>(widthsSI.get(i), LengthUnit.SI),
                                new DoubleScalar.Rel<LengthUnit>(widthsSI.get(i), LengthUnit.SI), this.laneType, ld,
                                new DoubleScalar.Abs<FrequencyUnit>(0.0, FrequencyUnit.PER_HOUR));
                        linkTag.laneTags.get(name).cse = lane;
                        cseList.add(lane);
                        if (this.simulator != null)
                        {
                            new LaneAnimation(lane, this.simulator, Color.LIGHT_GRAY);
                        }
                    }
                }
                else
                {
                    Lane lane =
                        new Lane(csl, new DoubleScalar.Rel<LengthUnit>(offsetSI[i], LengthUnit.SI),
                            new DoubleScalar.Rel<LengthUnit>(offsetSI[i], LengthUnit.SI), new DoubleScalar.Rel<LengthUnit>(
                                widthsSI.get(i), LengthUnit.SI), new DoubleScalar.Rel<LengthUnit>(widthsSI.get(i),
                                LengthUnit.SI), this.laneType, ld, new DoubleScalar.Abs<FrequencyUnit>(Double.MAX_VALUE,
                                FrequencyUnit.PER_HOUR));
                    linkTag.laneTags.get(name).cse = lane;
                    cseList.add(lane);
                    if (this.simulator != null)
                    {
                        new LaneAnimation(lane, this.simulator, Color.GRAY);
                    }
                }
            }
            catch (NetworkException ne)
            {
                throw new SAXException(ne);
            }
        }

        // road markers
        for (int j = 0; j < markerStrings.length(); j++)
        {
            DoubleScalar.Rel<LengthUnit> lateralCenterPosition = new DoubleScalar.Rel<LengthUnit>();
            DoubleScalar.Rel<LengthUnit> width = new DoubleScalar.Rel<LengthUnit>(0.1, LengthUnit.METER);
            switch (markerStrings.charAt(j))
            {
                case '-':
                    break;

                case '|':
                    Stripe solidLine = new Stripe(csl, lateralCenterPosition, width);
                    if (this.simulator != null)
                    {
                        new StripeAnimation(solidLine, this.simulator, StripeAnimation.type.SOLID);
                    }
                    break;

                default:
                    break;
            }
        }
         */
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
     * @param typeName the name of the GTU type.
     * @return the GTUType that was retrieved or created.
     */
    protected final GTUType<String> parseGTUType(final String typeName)
    {
        if (!this.gtuTypes.containsKey(typeName))
        {
            GTUType<String> gtuType = new GTUType<>(typeName);
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
                u = us;
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
                u = us;
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
                u = us;
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
                u = us;
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
                u = us;
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

    /** LINK element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected class LinkTag
    {
        /** name. */
        protected String name;

        /** default speed. */
        protected DoubleScalar.Abs<SpeedUnit> speed = null;

        /** default lane width on this link. */
        protected DoubleScalar.Rel<LengthUnit> width = null;

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

        /** elements. */
        protected String elements = null;

        /** lane info. */
        protected Map<String, LaneTag> laneTags = new HashMap<>();

        /** straight. */
        protected StraightTag straightTag = null;

        /** arc. */
        protected ArcTag arcTag = null;
    }

    /** LANE element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected class LaneTag
    {
        /** name. */
        protected String name;

        /** lane speed. */
        protected DoubleScalar.Abs<SpeedUnit> speed = null;

        /** lane width. */
        protected DoubleScalar.Rel<LengthUnit> width = null;

        /** generators. */
        protected Set<GeneratorTag> generatorTags = new HashSet<>();

        /** fill at t=0. */
        protected Set<FillTag> fillTags = new HashSet<>();

        /** the lane that was created. */
        protected CrossSectionElement cse = null;
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
        protected String name;

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
        protected String name;

        /** GTUs. */
        protected List<GTUTag> gtus = new ArrayList<GTUTag>();

        /** weights. */
        protected List<Double> weights = new ArrayList<Double>();
    }

    /** Generator element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected class GeneratorTag
    {
        /** lane. */
        protected LaneTag laneTag = null;

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
    }

    /** Fill element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected class FillTag
    {
        /** lane. */
        protected LaneTag laneTag = null;

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
        protected String name;

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
        protected String name;

        /** shortest routes. */
        protected List<ShortestRouteTag> shortestRoutes = new ArrayList<ShortestRouteTag>();

        /** weights. */
        protected List<Double> weights = new ArrayList<Double>();
    }

}
