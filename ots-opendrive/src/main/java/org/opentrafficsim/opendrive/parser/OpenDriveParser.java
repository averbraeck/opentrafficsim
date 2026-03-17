package org.opentrafficsim.opendrive.parser;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.vector.LengthVector;
import org.djutils.draw.curve.Flattener2d;
import org.djutils.draw.curve.OffsetCurve2d;
import org.djutils.draw.curve.OffsetFlattener2d;
import org.djutils.draw.function.ContinuousPiecewiseLinearFunction;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Try;
import org.djutils.immutablecollections.ImmutableMap;
import org.opentrafficsim.base.AlphabeticIdGenerator;
import org.opentrafficsim.base.StripeElement;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.geometry.PolyLineCurve2d;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.opendrive.bindings.SpeedUnitAdapter;
import org.opentrafficsim.opendrive.generated.EContactPoint;
import org.opentrafficsim.opendrive.generated.ELaneType;
import org.opentrafficsim.opendrive.generated.ERoadLinkElementType;
import org.opentrafficsim.opendrive.generated.ERoadMarkRule;
import org.opentrafficsim.opendrive.generated.EUnitSpeed;
import org.opentrafficsim.opendrive.generated.OpenDrive;
import org.opentrafficsim.opendrive.generated.OpenDriveElement;
import org.opentrafficsim.opendrive.generated.TJunction;
import org.opentrafficsim.opendrive.generated.TRoad;
import org.opentrafficsim.opendrive.generated.TRoadLanesLaneSection;
import org.opentrafficsim.opendrive.generated.TRoadLanesLaneSectionLcrLaneRoadMark;
import org.opentrafficsim.opendrive.generated.TRoadLanesLaneSectionLcrLaneRoadMarkTypeLine;
import org.opentrafficsim.opendrive.generated.TRoadLanesLaneSectionLeftLane;
import org.opentrafficsim.opendrive.generated.TRoadLanesLaneSectionLrLane;
import org.opentrafficsim.opendrive.generated.TRoadLanesLaneSectionLrLaneAccess;
import org.opentrafficsim.opendrive.generated.TRoadLanesLaneSectionLrLaneBorder;
import org.opentrafficsim.opendrive.generated.TRoadLanesLaneSectionLrLaneSpeed;
import org.opentrafficsim.opendrive.generated.TRoadLanesLaneSectionLrLaneWidth;
import org.opentrafficsim.opendrive.generated.TRoadLanesLaneSectionRightLane;
import org.opentrafficsim.opendrive.generated.TRoadLinkPredecessorSuccessor;
import org.opentrafficsim.opendrive.generated.TRoadType;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.network.CrossSectionElement;
import org.opentrafficsim.road.network.CrossSectionGeometry;
import org.opentrafficsim.road.network.CrossSectionLink;
import org.opentrafficsim.road.network.Lane;
import org.opentrafficsim.road.network.LaneGeometryUtil;
import org.opentrafficsim.road.network.LaneKeepingPolicy;
import org.opentrafficsim.road.network.LaneType;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.Shoulder;
import org.opentrafficsim.road.network.Stripe;
import org.opentrafficsim.road.network.StripeData;
import org.opentrafficsim.road.network.conflict.ConflictBuilder;
import org.opentrafficsim.road.network.conflict.ConflictBuilder.FixedWidthGenerator;
import org.opentrafficsim.road.network.object.detector.SinkDetector;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

/**
 * OpenDRIVE parser, which can parse .xodr files, strings or other input stream that represents the same byte information.
 * Current known constraints are:
 * <ul>
 * <li>Links are not allowed to overlap.</li>
 * <li>Road discontinuities (lane sections, road type change, road mark change, lane access change, lane speed change) are not
 * allowed in parts of lanes that should become a conflict with other lanes. Conflicts are when lanes from different links
 * split, merge or cross, i.e. have overlap.</li>
 * </ul>
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class OpenDriveParser
{

    /** Deviation for line flattening. Also used to filter point on (nearly) straight lines. */
    private static final double MAX_DEVIATION = 0.01;

    /** S-fractions of offset data within this epsilon of each other are filtered. */
    private static final double EPS_S_OFFSET = 1e-6;

    /** Discontinuities that are this close will not results in an intermediate link. */
    private static final double EPS_DISCONTINUITIES = 1e-3;

    /** Flattener. */
    private static final Flattener2d FLATTENER = new Flattener2d.MaxDeviation(MAX_DEVIATION);

    /** Flattener. */
    private static final OffsetFlattener2d OFFSET_FLATTENER = new OffsetFlattener2d.MaxDeviation(MAX_DEVIATION);

    /** Lane types that are included. */
    private static final Set<ELaneType> LANE_TYPES = Set.of(ELaneType.NONE, ELaneType.BIKING, ELaneType.BUS,
            ELaneType.CONNECTING_RAMP, ELaneType.DRIVING, ELaneType.ENTRY, ELaneType.EXIT, ELaneType.HOV, ELaneType.OFF_RAMP,
            ELaneType.ON_RAMP, ELaneType.SHOULDER, ELaneType.BORDER, ELaneType.STOP, ELaneType.RESTRICTED);

    /** Lane types that are parsed to shoulders. */
    private static final Set<ELaneType> SHOULDER_TYPES =
            Set.of(ELaneType.NONE, ELaneType.SHOULDER, ELaneType.BORDER, ELaneType.STOP, ELaneType.RESTRICTED);

    /** Shoulder lane type. */
    private static final LaneType SHOULDER = new LaneType("Shoulder");

    /** Default link types. */
    private static final Map<String, LinkType> LINK_TYPES = new LinkedHashMap<>();

    static
    {
        LINK_TYPES.put("UNKNOWN", DefaultsNl.ROAD);
        LINK_TYPES.put("RURAL", DefaultsNl.PROVINCIAL);
        LINK_TYPES.put("MOTORWAY", DefaultsNl.HIGHWAY);
        LINK_TYPES.put("TOWN", DefaultsNl.URBAN);
        LINK_TYPES.put("LOW_SPEED", DefaultsNl.RESIDENTIAL);
        LINK_TYPES.put("PEDESTRIAN", new LinkType("NL.PEDESTRIAN", DefaultsNl.URBAN));
        LINK_TYPES.put("BICYCLE", new LinkType("NL.BICYCLE", DefaultsNl.URBAN));
        LINK_TYPES.put("TOWN_EXPRESSWAY", DefaultsNl.FREEWAY);
        LINK_TYPES.put("TOWN_COLLECTOR", DefaultsNl.RURAL);
        LINK_TYPES.put("TOWN_ATERIAL", DefaultsNl.RURAL);
        LINK_TYPES.put("TOWN_PRIVATE", DefaultsNl.RURAL);
        LINK_TYPES.put("TOWN_LOCAL", DefaultsNl.RURAL);
        LINK_TYPES.put("TOWN_PLAY_STREET", DefaultsNl.RESIDENTIAL);
    };

    /** Node id generator. */
    private final AlphabeticIdGenerator nodeIdGenerator = new AlphabeticIdGenerator("Node");

    /** Link id generator. */
    private final AlphabeticIdGenerator linkIdGenerator = new AlphabeticIdGenerator("Link");

    /** Open drive tag. */
    private final OpenDrive openDrive;

    /** Network. */
    private RoadNetwork net;

    /** List of roads by id. */
    private Map<String, TRoad> roadMap = new LinkedHashMap<>();

    /** List of junctions by id. */
    private Map<String, TJunction> junctionMap = new LinkedHashMap<>();

    /** Stored nodes by their connection definition, so links can share nodes where they connect between roads. */
    private Map<Connection, Node> nodeMap = new LinkedHashMap<>();

    /** Whether to use the road name. */
    private boolean useRoadName = false;

    /** Origin nodes by their road id. */
    private Map<String, Map<Boolean, Node>> origins = new LinkedHashMap<>();

    /** Destination nodes by their road id. */
    private Map<String, Map<Boolean, Node>> destinations = new LinkedHashMap<>();

    /**
     * Constructor.
     * @param openDrive open drive tag
     */
    private OpenDriveParser(final OpenDrive openDrive)
    {
        this.openDrive = openDrive;
    }

    /**
     * Sets whether to use the road name to identify origins and destinations.
     * @param useRoadName whether to use the road name to identify origins and destinations
     * @return parser for method chaining
     */
    public OpenDriveParser setUseRoadName(final boolean useRoadName)
    {
        this.useRoadName = useRoadName;
        return this;
    }

    /**
     * Parse OpenDrive XML (.xodr) input file and build OpenDRIVE object.
     * @param filename file name, including path.
     * @return parser
     * @throws MalformedURLException if the file cannot be made in to a URL
     * @throws JAXBException when the parsing fails
     * @throws SAXException on error creating SAX parser
     * @throws ParserConfigurationException on error with parser configuration
     * @throws IOException if the file does no exist or is not accessible
     */
    public static OpenDriveParser parseXodr(final String filename)
            throws MalformedURLException, JAXBException, SAXException, ParserConfigurationException, IOException
    {
        return parseStream(new File(filename).toURI().toURL().openStream());
    }

    /**
     * Parse OpenDrive XML (.xodr) string and build OpenDRIVE object using UTF-8 character encoding.
     * @param string the xml string
     * @return parser
     * @throws JAXBException when the parsing fails
     * @throws ParserConfigurationException on error with parser configuration
     * @throws SAXException on error creating SAX parser
     */
    public static OpenDriveParser parseFileString(final String string)
            throws JAXBException, SAXException, ParserConfigurationException
    {
        return parseString(string, StandardCharsets.UTF_8);
    }

    /**
     * Parse OpenDrive XML (.xodr) string and build OpenDRIVE object.
     * @param string the xml string
     * @param charset character set
     * @return parser
     * @throws JAXBException when the parsing fails
     * @throws ParserConfigurationException on error with parser configuration
     * @throws SAXException on error creating SAX parser
     */
    public static OpenDriveParser parseString(final String string, final Charset charset)
            throws JAXBException, SAXException, ParserConfigurationException
    {
        return parseStream(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Parse OpenDrive XML (.xodr) input stream and build OpenDRIVE object.
     * @param xmlStream the xml stream
     * @return Open Drive tag
     * @throws JAXBException when the parsing fails
     * @throws ParserConfigurationException on error with parser configuration
     * @throws SAXException on error creating SAX parser
     */
    public static OpenDriveParser parseStream(final InputStream xmlStream)
            throws JAXBException, SAXException, ParserConfigurationException
    {
        Locale locale = Locale.getDefault();
        Locale.setDefault(Locale.US);
        JAXBContext jc = JAXBContext.newInstance(OpenDrive.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setXIncludeAware(false);
        spf.setNamespaceAware(true);
        spf.setValidating(true);
        XMLReader xmlReader = spf.newSAXParser().getXMLReader();
        XMLFilterImpl xmlFilter = new XmlNamespaceFilter(xmlReader);
        xmlReader.setContentHandler(unmarshaller.getUnmarshallerHandler());
        SAXSource saxSource = new SAXSource(xmlFilter, new InputSource(xmlStream));
        OpenDrive result = (OpenDrive) unmarshaller.unmarshal(saxSource);
        Locale.setDefault(locale);
        return new OpenDriveParser(result);
    }

    /**
     * This class adds name space to elements, so .xodr that do not include the name space can still be parsed.
     */
    private static class XmlNamespaceFilter extends XMLFilterImpl
    {
        /**
         * Constructor.
         * @param xmlReader XML reader
         */
        XmlNamespaceFilter(final XMLReader xmlReader)
        {
            super(xmlReader);
        }

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
                throws SAXException
        {
            // Compensate for missing xmlns="http://code.asam.net/simulation/standard/opendrive_schema" in OpenDRIVE tag
            super.startElement("http://code.asam.net/simulation/standard/opendrive_schema", localName, qName, attributes);
        }
    }

    /**
     * Build network.
     * @param network network
     * @throws NetworkException network exception
     */
    public void build(final RoadNetwork network) throws NetworkException
    {
        build(network, (roadType) -> LINK_TYPES
                .get((roadType.contains(".") ? roadType.substring(roadType.indexOf(".") + 1) : roadType).toUpperCase()));
    }

    /**
     * Build network.
     * @param network network
     * @param linkTypeFunction produces link types for OpenDRIVE link types. String contains country code if provided, e.g.
     *            DE.RURAL.
     * @throws NetworkException network exception
     */
    public void build(final RoadNetwork network, final Function<String, LinkType> linkTypeFunction) throws NetworkException
    {
        this.net = network;
        this.roadMap.clear();
        this.junctionMap.clear();

        this.openDrive.getRoad().forEach((road) -> this.roadMap.put(road.getId(), road));
        this.openDrive.getJunction().forEach((junction) -> this.junctionMap.put(junction.getId(), junction));

        buildNetwork(linkTypeFunction);
    }

    /**
     * Returns the node that was created at the side of the road of given id from which traffic can enter the network.
     * @param roadId road id
     * @param designDirection direction on road for origin, may be {@code null} if the road is not an origin in both directions
     * @return node that was created at the side of the road of given id from which traffic can enter the network
     */
    public Node getOrigin(final String roadId, final Boolean designDirection)
    {
        Map<Boolean, Node> map = this.origins.get(roadId);
        if (map.size() == 1)
        {
            return map.values().iterator().next();
        }
        return map.get(designDirection);
    }

    /**
     * Returns the node that was created at the side of the road of given id from which traffic can exit the network.
     * @param roadId road id
     * @param designDirection direction on road for destination, may be {@code null} if the road is not a destination in both
     *            directions
     * @return node that was created at the side of the road of given id from which traffic can exit the network
     */
    public Node getDestination(final String roadId, final Boolean designDirection)
    {
        Map<Boolean, Node> map = this.destinations.get(roadId);
        if (map.size() == 1)
        {
            return map.values().iterator().next();
        }
        return map.get(designDirection);
    }

    /**
     * Returns the identifier for the road by which origins and destinations are identified.
     * @param road road tag
     * @return the identifier for the road by which origins and destinations are identified
     */
    private String odRoadIdentifier(final TRoad road)
    {
        return this.useRoadName && road.getName() != null && !road.getName().isBlank() ? road.getName() : road.getId();
    }

    /**
     * Build the nodes, links and lanes in the network.
     * @param linkTypeFunction produces link types for OpenDRIVE link types
     * @throws NetworkException on network exception
     */
    private void buildNetwork(final Function<String, LinkType> linkTypeFunction) throws NetworkException
    {
        for (Entry<String, TRoad> roadEntry : this.roadMap.entrySet())
        {
            TRoad road = roadEntry.getValue();

            // gather discontinuities
            NavigableMap<Double, TRoadLanesLaneSection> laneSections = new TreeMap<>();
            NavigableMap<Double, TRoadType> roadTypes = new TreeMap<>();
            List<Boolean> directions = new ArrayList<>();
            NavigableSet<Double> discontinuities = getDiscontinuities(road, laneSections, roadTypes, directions);
            boolean forward = directions.get(0);
            boolean backward = directions.get(1);

            // id supplier
            Supplier<String> id;
            if (road.getId() != null && !road.getId().isBlank())
            {
                id = discontinuities.size() > 1 || (forward && backward) ? new AlphabeticIdGenerator(road.getId() + "_")
                        : () -> road.getId(); // just '1', or '1A', '1B', etc. when multiple links from this road
            }
            else
            {
                id = this.linkIdGenerator;
            }

            // design line of the entire road
            SegmentedCurve roadDesignLine = new SegmentedCurve(road.getPlanView().getGeometry(), road.getLength());
            // TODO sub-curve of continuous roadDesignLine
            PolyLine2d roadCenterLine = roadDesignLine.toPolyLine(FLATTENER);
            ContinuousPiecewiseLinearFunction roadOffset = new OffsetData(road.getLanes().getLaneOffset(), road.getLength());
            double lengthFactor = roadCenterLine.getLength() / road.getLength().si;

            // gather nodes at start and end of road (these may be the same as on other roads)
            Node startNodeForward = null;
            Node startNodeBackward = null;
            Node endNodeForward = null;
            Node endNodeBackward = null;
            DirectedPoint2d startRay = roadCenterLine.getLocationFraction(0.0);
            DirectedPoint2d startPointForward = new DirectedPoint2d(startRay.x, startRay.y, roadDesignLine.getStartDirection());
            DirectedPoint2d startPointBackward = startPointForward.rotate(Math.PI);
            if (forward)
            {
                Connection startConnectionForward = getConnection(road, true, true);
                DirectedPoint2d p = startPointForward; // effectively final
                startNodeForward = this.nodeMap.computeIfAbsent(startConnectionForward,
                        (c) -> createNode(this.net, this.nodeIdGenerator.get(), p));
                if (road.getLink() == null || road.getLink().getPredecessor() == null)
                {
                    this.origins.computeIfAbsent(odRoadIdentifier(road), (s) -> new LinkedHashMap<>()).put(true,
                            startNodeForward);
                }
            }
            if (backward)
            {
                Connection startConnectionBackward = getConnection(road, true, false);
                DirectedPoint2d p = startPointBackward; // effectively final
                startNodeBackward = this.nodeMap.computeIfAbsent(startConnectionBackward,
                        (c) -> createNode(this.net, this.nodeIdGenerator.get(), p));
                if (road.getLink() == null || road.getLink().getPredecessor() == null)
                {
                    this.destinations.computeIfAbsent(odRoadIdentifier(road), (s) -> new LinkedHashMap<>()).put(false,
                            startNodeBackward);
                }
            }

            // loop sections on road to create individual links
            boolean last = false;
            for (double sFrom : discontinuities)
            {
                // get elements in section
                TRoadLanesLaneSection laneSection = laneSections.floorEntry(sFrom).getValue();
                Double sEndLaneSection = laneSections.higherKey(sFrom);
                if (sEndLaneSection == null)
                {
                    sEndLaneSection = road.getLength().si;
                }
                TRoadType roadType = roadTypes.floorEntry(sFrom).getValue();
                String roadTypeId = roadType.getCountry() == null ? roadType.getType().name()
                        : roadType.getCountry() + "." + roadType.getType().name();
                LinkType linkType = linkTypeFunction.apply(roadTypeId);
                Speed roadSpeed = roadType.getSpeed() == null ? null
                        : getSpeed(roadType.getSpeed().getMax(), roadType.getSpeed().getUnit());

                // subtract geometry from road
                Double sTo = discontinuities.higher(sFrom);
                if (sTo == null)
                {
                    last = true;
                    sTo = road.getLength().si;
                }
                List<Point2d> points = roadCenterLine
                        .extract(sFrom * lengthFactor, last ? roadCenterLine.getLength() : sTo * lengthFactor).getPointList();
                removePointsOnStraight(points);
                PolyLine2d flatLinkLine = new PolyLine2d(points);
                DirectedPoint2d endRay = flatLinkLine.getLocationFraction(1.0);
                DirectedPoint2d endPointForward =
                        last ? roadDesignLine.getEndPoint() : new DirectedPoint2d(endRay.x, endRay.y, endRay.dirZ);
                DirectedPoint2d endPointBackward = endPointForward.rotate(Math.PI);

                // continuous definition from flat segment, with directions possibly overridden at road end points
                OffsetCurve2d linkDesignLine = new PolyLineCurve2d(flatLinkLine,
                        sFrom == 0.0 ? roadDesignLine.getStartPoint().dirZ : startPointForward.dirZ,
                        last ? roadDesignLine.getEndPoint().dirZ : endPointForward.dirZ);

                // make link and the lanes and stripes on it
                LinkData linkData = new LinkData(road, id, linkType, roadTypeId, roadSpeed, roadOffset, linkDesignLine, sFrom,
                        sTo, sEndLaneSection, laneSection);
                if (forward)
                {
                    endNodeForward = makeLink(linkData, startNodeForward, endPointForward, true);
                }
                if (backward)
                {
                    endNodeBackward = makeLink(linkData, startNodeBackward, endPointBackward, false);
                }
                startPointForward = endPointForward;
                startPointBackward = endPointBackward;
                startNodeForward = endNodeForward;
                startNodeBackward = endNodeBackward;
            }

            if (forward && (road.getLink() == null || road.getLink().getSuccessor() == null))
            {
                this.destinations.computeIfAbsent(odRoadIdentifier(road), (s) -> new LinkedHashMap<>()).put(true,
                        endNodeForward);
                for (Link link : endNodeForward.getLinks())
                {
                    if (link.getEndNode().equals(endNodeForward) && link instanceof CrossSectionLink cLink)
                    {
                        for (Lane lane : cLink.getLanes())
                        {
                            Length pos = Length.max(Length.ZERO, lane.getLength().minus(Length.ofSI(20.0)));
                            new SinkDetector(lane, pos, DefaultsNl.ROAD_USERS);
                        }
                    }
                }
            }
            if (backward && (road.getLink() == null || road.getLink().getSuccessor() == null))
            {
                this.origins.computeIfAbsent(odRoadIdentifier(road), (s) -> new LinkedHashMap<>()).put(false, endNodeBackward);
            }
        }

    }

    /**
     * Create link.
     * @param linkData relevant data for the link
     * @param startNode start node
     * @param endPoint end point
     * @param forward whether the link is in the forward direction
     * @return node used or created at the end of the link
     * @throws NetworkException network exception
     */
    private Node makeLink(final LinkData linkData, final Node startNode, final DirectedPoint2d endPoint, final boolean forward)
            throws NetworkException
    {
        // end node
        Node endNode;
        if (linkData.sTo != linkData.road.getLength().si)
        {
            endNode = new Node(this.net, this.nodeIdGenerator.get(), endPoint);
        }
        else
        {
            Connection endConnection = getConnection(linkData.road, false, forward);
            endNode = this.nodeMap.computeIfAbsent(endConnection,
                    (c) -> createNode(this.net, this.nodeIdGenerator.get(), endPoint));
        }

        // link
        // TODO elevation road.getElevationProfile()
        ContinuousPiecewiseLinearFunction elevation = ContinuousPiecewiseLinearFunction.of(0.0, 0.0);
        PolyLine2d linkLine = forward ? linkData.linkDesignLine.toPolyLine(FLATTENER)
                : linkData.linkDesignLine.toPolyLine(FLATTENER).reverse();
        // OpenDRIVE standard: if "rule" not given in <road>, then RHT is assumed
        LaneKeepingPolicy laneKeeping = linkData.road.getRule() == null ? LaneKeepingPolicy.KEEPRIGHT : linkData.road.getRule();
        CrossSectionLink link = new CrossSectionLink(this.net, linkData.id.get(), forward ? startNode : endNode,
                forward ? endNode : startNode, linkData.linkType, new OtsLine2d(linkLine), elevation, laneKeeping);

        // center mark
        ContinuousPiecewiseLinearFunction roadOffset = OffsetData.sub(linkData.roadOffset,
                linkData.sFrom / linkData.road.getLength().si, linkData.sTo / linkData.road.getLength().si);

        double offsetSign = forward ? -1.0 : 1.0;
        ContinuousPiecewiseLinearFunction prevEdgeOffset =
                getEdgeOffset(linkData.laneSection.getCenter().getLane().get(0).getBorderOrWidth(), linkData.sFrom,
                        linkData.sTo, linkData.laneSection.getS(), linkData.sEndLaneSection, roadOffset, offsetSign);
        PolyLine2d prevEdge = forward ? linkData.linkDesignLine.toPolyLine(OFFSET_FLATTENER, prevEdgeOffset)
                : linkData.linkDesignLine.toPolyLine(OFFSET_FLATTENER, prevEdgeOffset).reverse();
        TRoadLanesLaneSectionLcrLaneRoadMark centerMark =
                getLaneProperty(linkData.laneSection, linkData.laneSection.getCenter().getLane().get(0), linkData.sFrom,
                        linkData.laneSection.getCenter().getLane().get(0).getRoadMark(), (rm) -> rm.getSOffset());

        List<? extends TRoadLanesLaneSectionLrLane> lanes =
                forward ? linkData.laneSection.getRight().getLane() : linkData.laneSection.getLeft().getLane();
        // check center line requirement before we reverse the lanes
        boolean doCenter = forward ? LANE_TYPES.contains(lanes.get(0).getType())
                : !LANE_TYPES.contains(linkData.laneSection.getLeft().getLane().get(0).getType());
        if (!forward)
        {
            lanes = new ArrayList<>(lanes);
            Collections.reverse(lanes);
        }
        if (doCenter)
        {
            // only solid when stripe type is null, if not on a junction and the first lane has a valid lane type
            boolean solidWhenNull = !(linkData.road.getJunction() != null && !linkData.road.getJunction().isBlank()
                    && !linkData.road.getJunction().strip().equals("-1")) && !SHOULDER_TYPES.contains(lanes.get(0).getType());
            makeStripe(linkData.linkDesignLine, link, prevEdgeOffset, prevEdge, centerMark, solidWhenNull, forward);
        }

        // lanes
        for (TRoadLanesLaneSectionLrLane lane : lanes)
        {
            String id = forward ? ((TRoadLanesLaneSectionRightLane) lane).getId().toString()
                    : ((TRoadLanesLaneSectionLeftLane) lane).getId().toString();
            ContinuousPiecewiseLinearFunction nextEdgeOffset = getEdgeOffset(lane.getBorderOrWidth(), linkData.sFrom,
                    linkData.sTo, linkData.laneSection.getS(), linkData.sEndLaneSection, prevEdgeOffset, offsetSign);
            PolyLine2d nextEdge = makeLane(lane, id, link, linkData, prevEdgeOffset, prevEdge, nextEdgeOffset, forward);

            TRoadLanesLaneSectionLcrLaneRoadMark mark =
                    getLaneProperty(linkData.laneSection, lane, linkData.sFrom, lane.getRoadMark(), (rm) -> rm.getSOffset());
            makeStripe(linkData.linkDesignLine, link, nextEdgeOffset, nextEdge, mark, false, forward);

            prevEdgeOffset = nextEdgeOffset;
            prevEdge = nextEdge;
        }
        return endNode;
    }

    /**
     * Record that holds data for makeLane.
     * @param road road tag
     * @param id link id supplier
     * @param linkType link type
     * @param roadTypeId e.g. DE.URBAN or MOTORWAY
     * @param roadSpeed speed on road
     * @param roadOffset offset on road level
     * @param linkDesignLine design line of the link
     * @param sFrom link from distance on road
     * @param sTo link to distance on road
     * @param sEndLaneSection fraction on road where the lane section stops (can be &gt; sTo due to other discontinuities)
     * @param laneSection lane section on road
     */
    private record LinkData(TRoad road, Supplier<String> id, LinkType linkType, String roadTypeId, Speed roadSpeed,
            ContinuousPiecewiseLinearFunction roadOffset, OffsetCurve2d linkDesignLine, double sFrom, Double sTo,
            double sEndLaneSection, TRoadLanesLaneSection laneSection)
    {
    }

    /**
     * Creates a lane, or shoulder, based on a lane tag.
     * @param lane lane tag
     * @param id lane id
     * @param link link
     * @param linkData link data
     * @param prevEdgeOffset offsets of previous edge
     * @param prevEdge previous edge
     * @param nextEdgeOffset offsets of next edge
     * @param forward forward
     * @return next edge
     * @throws NetworkException when no cross-section slice is defined
     */
    private static PolyLine2d makeLane(final TRoadLanesLaneSectionLrLane lane, final String id, final CrossSectionLink link,
            final LinkData linkData, final ContinuousPiecewiseLinearFunction prevEdgeOffset, final PolyLine2d prevEdge,
            final ContinuousPiecewiseLinearFunction nextEdgeOffset, final boolean forward) throws NetworkException
    {
        if (Math.abs(prevEdgeOffset.get(0.0) - nextEdgeOffset.get(0.0)) < MAX_DEVIATION
                && Math.abs(prevEdgeOffset.get(0.5) - nextEdgeOffset.get(0.5)) < MAX_DEVIATION
                && Math.abs(prevEdgeOffset.get(1.0) - nextEdgeOffset.get(1.0)) < MAX_DEVIATION)
        {
            Logger.ots().info("Skip creating lane {} on link {} because its width is 0.", id, link.getId());
            return prevEdge;
        }

        // TODO lane type and speed map
        TRoadLanesLaneSectionLrLaneAccess access =
                getLaneProperty(linkData.laneSection, lane, linkData.sFrom, lane.getAccess(), (ac) -> ac.getSOffset());
        TRoadLanesLaneSectionLrLaneSpeed speed =
                getLaneProperty(linkData.laneSection, lane, linkData.sFrom, lane.getSpeed(), (sp) -> sp.getSOffset());

        ELaneType laneType = lane.getType();
        String roadTypeId = linkData.roadTypeId(); // ERoadType with possible country before it

        Map<GtuType, Speed> laneSpeeds = speed == null ? Collections.emptyMap()
                : Map.of(DefaultsNl.ROAD_USER, new Speed(speed.getMax(), speed.getUnit()));
        Speed roadSpeed = linkData.roadSpeed;

        PolyLine2d nextEdge = id.startsWith("-") ? linkData.linkDesignLine.toPolyLine(OFFSET_FLATTENER, nextEdgeOffset)
                : linkData.linkDesignLine.toPolyLine(OFFSET_FLATTENER, nextEdgeOffset).reverse(); // negative id's are forward
        if (LANE_TYPES.contains(lane.getType()))
        {
            CrossSectionGeometry geometry =
                    getCrossSectionGeometry(linkData.linkDesignLine, prevEdgeOffset, nextEdgeOffset, forward);
            if (SHOULDER_TYPES.contains(lane.getType()))
            {
                // Shoulder(final CrossSectionLink link, final String id, final CrossSectionGeometry geometry, final LaneType
                // laneType)
                new Shoulder(link, id, geometry, SHOULDER);
            }
            else
            {
                // TODO Use mapper from linkData.roadTypeId & lane.getType() to lane type
                // TODO In case of restriction, create child lane type following standard name addition: FREEWAY_DENY_BUS
                new Lane(link, id, geometry, DefaultsRoadNl.FREEWAY, laneSpeeds);
            }
        }
        return nextEdge;
    }

    /**
     * Returns speed from max speed information.
     * @param max value in unit, "undefined", or "no limit"
     * @param unit unit
     * @return speed value
     */
    private Speed getSpeed(final String max, final EUnitSpeed unit)
    {
        if (max.equals("undefined"))
        {
            return null;
        }
        else if (max.equals("no limit"))
        {
            return Speed.POSITIVE_INFINITY;
        }
        else
        {
            return new Speed(Double.valueOf(max), new SpeedUnitAdapter().unmarshal(unit));
        }
    }

    /**
     * Creates a stripe based on a road mark.
     * @param linkDesignLine design line of link
     * @param link link
     * @param centerOffsetData offset data of stripe center line
     * @param centerLine flattened center line
     * @param mark mark tag
     * @param solidWhenNull draw solid line when mark or mark's type is null, as this is sometimes omitted for median lines
     * @param forward direction of design line
     * @throws NetworkException when no cross-section slice is defined
     */
    private static void makeStripe(final OffsetCurve2d linkDesignLine, final CrossSectionLink link,
            final ContinuousPiecewiseLinearFunction centerOffsetData, final PolyLine2d centerLine,
            final TRoadLanesLaneSectionLcrLaneRoadMark mark, final boolean solidWhenNull, final boolean forward)
            throws NetworkException
    {
        StripeData stripeData;
        String id;
        double w;
        if (mark != null && mark.getType() != null)
        {
            id = mark.getType().getName() != null ? mark.getType().getName() : UUID.randomUUID().toString();
            w = mark.getType().getWidth();
            stripeData = customRoadMark(mark);
        }
        else if (mark != null && mark.getRoadMarkType() != null)
        {
            stripeData = mark.getRoadMarkType();
            id = UUID.randomUUID().toString();
            double wStandard = stripeData.getElements().stream().mapToDouble((e) -> e.width().si).sum();
            if (mark.getWidth() == null)
            {
                w = wStandard;
            }
            else
            {
                w = mark.getWidth();
                if (w != wStandard)
                {
                    double factor = w / wStandard;
                    List<StripeElement> newStripeElements = new ArrayList<>();
                    for (int i = 0; i < stripeData.getElements().size(); i++)
                    {
                        StripeElement element = stripeData.getElements().get(i);
                        StripeElement newElement =
                                new StripeElement(element.width().times(factor), element.color(), element.dashes());
                        newStripeElements.add(newElement);
                    }
                    stripeData.setElements(newStripeElements);
                }
            }
        }
        else if (solidWhenNull)
        {
            stripeData = DefaultsRoadNl.SOLID;
            id = UUID.randomUUID().toString();
            w = mark != null && mark.getWidth() != null ? mark.getWidth() : 0.2;
        }
        else
        {
            return;
        }
        if (w == 0.0)
        {
            // virtual center marks with no width were encountered
            return;
        }
        ContinuousPiecewiseLinearFunction prevOffset =
                OffsetData.add(centerOffsetData, ContinuousPiecewiseLinearFunction.of(0.0, -w * .5));
        ContinuousPiecewiseLinearFunction nextOffset =
                OffsetData.add(centerOffsetData, ContinuousPiecewiseLinearFunction.of(0.0, w * .5));
        CrossSectionGeometry geometry = getCrossSectionGeometry(linkDesignLine, prevOffset, nextOffset, forward);

        new Stripe(id, stripeData, link, geometry);
    }

    /**
     * Constructs {@link StripeData} from a custom defined road mark.
     * @param mark road mark
     * @return custom defined road mark
     */
    private static StripeData customRoadMark(final TRoadLanesLaneSectionLcrLaneRoadMark mark)
    {
        StripeData stripeData;
        SortedMap<Double, TRoadLanesLaneSectionLcrLaneRoadMarkTypeLine> lateral = new TreeMap<>();
        mark.getType().getLine().forEach((line) -> lateral.put(line.getTOffset(), line));
        List<StripeElement> elements = new ArrayList<>();
        Double prevOffset = null;
        double prevWidth = 0.0;
        boolean passing = true;
        for (Entry<Double, TRoadLanesLaneSectionLcrLaneRoadMarkTypeLine> lineEntry : lateral.entrySet())
        {
            TRoadLanesLaneSectionLcrLaneRoadMarkTypeLine line = lineEntry.getValue();
            passing &= !line.getRule().equals(ERoadMarkRule.NO_PASSING);
            if (prevOffset != null)
            {
                // insert gap
                double lateralGap = Math.abs(line.getTOffset() - prevOffset) - 0.5 * line.getWidth().si - 0.5 * prevWidth;
                if (lateralGap > 0.0)
                {
                    elements.add(StripeElement.gap(Length.ofSI(lateralGap)));
                }
            }
            if (line.getSpace() == 0.0)
            {
                // continuous
                elements.add(StripeElement.continuous(line.getWidth(), getColor(line.getColor().value())));
            }
            else
            {
                // dashed
                LengthVector dashes = new LengthVector(new double[] {line.getSpace(), line.getLength().si});
                elements.add(StripeElement.dashed(line.getWidth(), getColor(line.getColor().value()), dashes));
            }
            prevOffset = line.getTOffset();
            prevWidth = line.getWidth().si;
        }
        stripeData = new StripeData(elements, passing, passing);
        return stripeData;
    }

    /**
     * Returns color from string by taking the value from the respective {@link Color} field. The name is made upper case,
     * {@code "STANDARD"} is changed to {@code "WHITE"}, and if the color is not found {@code Color.WHITE} is returned.
     * @param colorName color name
     * @return color
     */
    private static Color getColor(final String colorName)
    {
        try
        {
            return (Color) Color.class.getField(colorName.toUpperCase().replace("STANDARD", "WHITE")).get(null);
        }
        catch (IllegalAccessException | NoSuchFieldException ex)
        {
            return Color.WHITE;
        }
    }

    /**
     * Gathers road discontinuities for which separate links need to be defined within OTS.
     * @param road road tag
     * @param laneSections map to store lane sections in, ordered by their s-coordinates
     * @param roadTypes map to store road types in, ordered by their s-coordinates
     * @param directions list to store whether the positive (index 0) and negative (index 1) road direction need to be included
     * @return set of discontinuities
     */
    private static NavigableSet<Double> getDiscontinuities(final TRoad road,
            final NavigableMap<Double, TRoadLanesLaneSection> laneSections, final NavigableMap<Double, TRoadType> roadTypes,
            final List<Boolean> directions)
    {
        directions.add(false);
        directions.add(false);
        road.getLanes().getLaneSection().forEach((l) -> laneSections.put(l.getS(), l));
        road.getType().forEach((r) -> roadTypes.put(r.getS(), r));
        NavigableSet<Double> discontinuities = new TreeSet<>();
        discontinuities.addAll(laneSections.keySet());
        discontinuities.addAll(roadTypes.keySet());
        for (TRoadLanesLaneSection laneSection : road.getLanes().getLaneSection())
        {
            Set<TRoadLanesLaneSectionLrLane> lanes = new LinkedHashSet<>();
            lanes.addAll(laneSection.getLeft() == null ? Collections.emptySet() : laneSection.getLeft().getLane());
            lanes.addAll(laneSection.getCenter() == null ? Collections.emptySet() : laneSection.getCenter().getLane());
            lanes.addAll(laneSection.getRight() == null ? Collections.emptySet() : laneSection.getRight().getLane());
            for (TRoadLanesLaneSectionLrLane lane : lanes)
            {
                if (LANE_TYPES.contains(lane.getType()))
                {
                    directions.set(0, directions.get(0) || lane instanceof TRoadLanesLaneSectionRightLane);
                    directions.set(1, directions.get(1) || lane instanceof TRoadLanesLaneSectionLeftLane);
                }
                for (TRoadLanesLaneSectionLcrLaneRoadMark mark : lane.getRoadMark())
                {
                    if (mark.getSOffset() > 0.0)
                    {
                        discontinuities.add(laneSection.getS() + mark.getSOffset());
                    }
                }
                for (TRoadLanesLaneSectionLrLaneAccess access : lane.getAccess())
                {
                    if (access.getSOffset() > 0.0)
                    {
                        discontinuities.add(laneSection.getS() + access.getSOffset());
                    }
                }
                for (TRoadLanesLaneSectionLrLaneSpeed speed : lane.getSpeed())
                {
                    if (speed.getSOffset() > 0.0)
                    {
                        discontinuities.add(laneSection.getS() + speed.getSOffset());
                    }
                }
            }
        }
        // filter discontinuities that are too close together
        Iterator<Double> it = discontinuities.iterator();
        double prev = Double.NEGATIVE_INFINITY;
        while (it.hasNext())
        {
            double next = it.next();
            if (next - prev < EPS_DISCONTINUITIES)
            {
                Logger.ots().debug("Removing s={} as next is {} on road {}", prev, next, road.getName());
                it.remove();
            }
            prev = next;
        }
        // sometimes there are road marks defined starting at the very end, skip these
        return discontinuities.subSet(0.0, true, road.getLength().si, false);
    }

    /**
     * Create node. This method captures exception for easier access.
     * @param network network
     * @param id id
     * @param point point
     * @return created node
     */
    private Node createNode(final RoadNetwork network, final String id, final DirectedPoint2d point)
    {
        return Try.assign(() -> new Node(network, id, point), "Duplicate node id in network.");
    }

    /**
     * Cleans points that are essentially on the straight between the neighbor points. This prevents duplicate points in offset
     * lines that are about an ulp different, but turn out equal after transformation.
     * @param points point list
     */
    private static void removePointsOnStraight(final List<Point2d> points)
    {
        int i = 0;
        while (i < points.size() - 2)
        {
            if (!points.get(i).equals(points.get(i + 2)))
            {
                PolyLine2d part = new PolyLine2d(points.get(i), points.get(i + 2));
                Point2d closest = part.closestPointOnPolyLine(points.get(i + 1));
                if (closest.distance(points.get(i + 1)) < MAX_DEVIATION / 10.0)
                {
                    points.remove(i + 1);
                }
                else
                {
                    i++;
                }
            }
            else
            {
                i++;
            }
        }
    }

    /**
     * Build conflicts while dealing with possible IllegalArgumentException due to duplicate points on inside curve.
     * @param network network
     */
    public static void buildConflicts(final RoadNetwork network)
    {
        List<Lane> lanes = getLanes(network);
        for (int i = 0; i < lanes.size(); i++)
        {
            for (int j = i + 1; j < lanes.size(); j++)
            {
                double width = -1.0;
                while (true)
                {
                    try
                    {
                        if (width < 0.0)
                        {
                            ConflictBuilder.buildConflicts(lanes.get(i), lanes.get(j), network.getSimulator(),
                                    ConflictBuilder.DEFAULT_WIDTH_GENERATOR);
                        }
                        else
                        {
                            ConflictBuilder.buildConflicts(lanes.get(i), lanes.get(j), network.getSimulator(),
                                    new FixedWidthGenerator(Length.ofSI(width)));
                        }
                        break;
                    }
                    catch (IllegalArgumentException ex)
                    {
                        if (width < 0.0)
                        {
                            width = 0.8 * Math.min(lanes.get(i).getWidth(0.5).si, lanes.get(j).getWidth(0.5).si);
                        }
                        else
                        {
                            width -= 0.1;
                            if (width < 0.0)
                            {
                                Logger.ots().warn("Unable to create conflict between lanes " + lanes.get(i).getFullId()
                                        + " and " + lanes.get(j).getFullId());
                                break;
                            }
                        }
                        Logger.ots().info("Unable to create conflict between lanes " + lanes.get(i).getFullId() + " and "
                                + lanes.get(j).getFullId() + ". Reattempting at width " + width + ".");
                    }
                }
            }
        }
    }

    /**
     * Returns all the lanes in the network.
     * @param network network
     * @return list if all lanes
     */
    private static List<Lane> getLanes(final RoadNetwork network)
    {
        ImmutableMap<String, Link> links = network.getLinkMap();
        List<Lane> lanes = new ArrayList<>();
        for (String linkId : links.keySet())
        {
            Link link = links.get(linkId);
            if (link instanceof CrossSectionLink)
            {
                for (CrossSectionElement element : ((CrossSectionLink) link).getCrossSectionElementList())
                {
                    if (element instanceof Lane lane && !(element instanceof Shoulder))
                    {
                        lanes.add(lane);
                    }
                }
            }
        }
        return lanes;
    }

    /**
     * Find the lane property applicable at the given s-coordinate.
     * @param <T> property type
     * @param laneSection lane section
     * @param lane lane tag
     * @param s s-coordinate
     * @param objects list of properties on the lane
     * @param obtainOffetS function to obtain offset s-coordinate from object
     * @return lane property applicable at the given s-coordinate
     */
    private static <T> T getLaneProperty(final TRoadLanesLaneSection laneSection, final TRoadLanesLaneSectionLrLane lane,
            final double s, final List<T> objects, final Function<T, Double> obtainOffetS)
    {
        T object = null;
        for (T t : objects)
        {
            if (laneSection.getS() + obtainOffetS.apply(t) <= s)
            {
                object = t;
            }
        }
        return object;
    }

    /**
     * Obtain edge offset information for part of a lane section.
     * @param borderOrWidth all borders and offsets along the lane section
     * @param sFrom from s-coordinate
     * @param sTo to s-coordinate
     * @param sOffsetLaneSection offset of start of current lane section, to which poly's are defined
     * @param sEndLaneSection offset at end of current lane section, to which poly's are defined
     * @param soFar offsets so far (regarding more inner lanes)
     * @param sign -1 or 1
     * @return cumulative edge offset data, fractions normalized [0...1] between sFrom and sTo
     */
    private static ContinuousPiecewiseLinearFunction getEdgeOffset(final List<OpenDriveElement> borderOrWidth,
            final double sFrom, final double sTo, final double sOffsetLaneSection, final double sEndLaneSection,
            final ContinuousPiecewiseLinearFunction soFar, final double sign)
    {
        NavigableMap<Double, Double> map = new TreeMap<>();
        double length = sTo - sFrom;
        for (int i = 0; i < borderOrWidth.size(); i++)
        {
            // skip if part is before sFrom
            double sBorderOrWidthMax = i == borderOrWidth.size() - 1 ? sEndLaneSection
                    : (borderOrWidth.get(i + 1) instanceof TRoadLanesLaneSectionLrLaneBorder nextBorder
                            ? nextBorder.getSOffset() + sOffsetLaneSection
                            : ((TRoadLanesLaneSectionLrLaneWidth) borderOrWidth.get(i + 1)).getSOffset()) + sOffsetLaneSection;
            if (sBorderOrWidthMax < sFrom)
            {
                continue;
            }

            OpenDriveElement element = borderOrWidth.get(i);
            double sBorderOrWidthMin;
            double a;
            double b;
            double c;
            double d;
            boolean isWidth;
            if (element instanceof TRoadLanesLaneSectionLrLaneBorder border)
            {
                // border defines an offset directly
                sBorderOrWidthMin = border.getSOffset() + sOffsetLaneSection;
                a = border.getA();
                b = border.getB();
                c = border.getC();
                d = border.getD();
                isWidth = false;
            }
            else
            {
                TRoadLanesLaneSectionLrLaneWidth width = (TRoadLanesLaneSectionLrLaneWidth) element;
                // width defines border relative to offset so far
                sBorderOrWidthMin = width.getSOffset() + sOffsetLaneSection;
                a = width.getA();
                b = width.getB();
                c = width.getC();
                d = width.getD();
                isWidth = true;
            }
            double borderOrWidthLength = sBorderOrWidthMax - sBorderOrWidthMin;
            Set<Double> s = new TreeSet<>();
            // by incorporating all soFar fractions, we obtain any granularity needed due to possible earlier curves
            soFar.forEach((st) ->
            {
                double sSoFar = sFrom + st.s() * length;
                if (sBorderOrWidthMin <= sSoFar && sSoFar <= sBorderOrWidthMax)
                {
                    s.add(sSoFar);
                }
            });
            if (b == 0.0 && c == 0.0 && d == 0.0)
            {
                // constant
                s.add(Math.max(sFrom, sBorderOrWidthMin));
                s.add(Math.min(sTo, sBorderOrWidthMax));
            }
            else if (c == 0 && d == 0)
            {
                // linear
                s.add(Math.max(sFrom, sBorderOrWidthMin));
                s.add(Math.min(sTo, sBorderOrWidthMax));
            }
            else
            {
                // curve, add 100 parts to cover the curve
                for (int j = 0; j <= 100; j++)
                {
                    double f = ((double) j) / 100.0;
                    double ds = sBorderOrWidthMin + f * borderOrWidthLength;
                    if (sFrom <= ds && ds <= sTo)
                    {
                        s.add(ds);
                    }
                }
            }
            for (double sValue : s)
            {
                double f = (sValue - sFrom) / length;
                double ds = sValue - sBorderOrWidthMin;
                double bOrW = a + b * ds + c * ds * ds + d * ds * ds * ds;
                map.put(f, isWidth ? soFar.get(f) + sign * bOrW : sign * bOrW);
            }

            // skip remainder when beyond sTo
            if (sBorderOrWidthMax > sTo)
            {
                break;
            }
        }
        map = map.subMap(0.0, true, 1.0, true);
        if (map.isEmpty())
        {
            return soFar;
        }
        cleanLinearFunctionMap(map);
        return new ContinuousPiecewiseLinearFunction(map);
    }

    /**
     * Clean map with function data. Function values that are equal to the previous and next value are removed. All fractions
     * within a small margin of a previous point are also removed.
     * @param map map with offset data
     */
    private static void cleanLinearFunctionMap(final SortedMap<Double, Double> map)
    {
        int nEqual = 0;
        double fPrev = Double.NaN;
        double vPrev = Double.NaN;
        Set<Double> remove = new LinkedHashSet<>();
        for (Entry<Double, Double> entry : map.entrySet())
        {
            if (vPrev == entry.getValue())
            {
                nEqual++;
            }
            else
            {
                nEqual = 0;
            }
            if (nEqual > 1)
            {
                remove.add(fPrev);
            }
            fPrev = entry.getKey();
            vPrev = entry.getValue();
        }
        fPrev = Double.NaN;
        for (Entry<Double, Double> entry : map.entrySet())
        {
            if (Math.abs(fPrev - entry.getKey()) < EPS_S_OFFSET)
            {
                remove.add(entry.getKey());
            }
            else
            {
                fPrev = entry.getKey();
            }
        }
        remove.forEach((f) -> map.remove(f));
    }

    /**
     * Return slices based on previous and next offsets.
     * @param curve curve
     * @param prev previous offsets
     * @param next next offsets
     * @param forward forward direction
     * @return list of slices
     */
    private static CrossSectionGeometry getCrossSectionGeometry(final OffsetCurve2d curve,
            final ContinuousPiecewiseLinearFunction prev, final ContinuousPiecewiseLinearFunction next, final boolean forward)
    {
        SortedMap<Double, Double> offset = new TreeMap<>();
        SortedMap<Double, Double> width = new TreeMap<>();
        SortedMap<Double, Double> left = new TreeMap<>();
        SortedMap<Double, Double> right = new TreeMap<>();
        SortedSet<Double> ss = new TreeSet<>();
        prev.forEach((st) -> ss.add(st.s()));
        next.forEach((st) -> ss.add(st.s()));
        for (double s : ss)
        {
            double oPrev = prev.get(s);
            double oNext = next.get(s);
            double o = (oPrev + oNext) / 2.0;
            offset.put(s, o);
            double w = Math.abs(oNext - oPrev);
            width.put(s, w);
            left.put(s, o + .5 * w);
            right.put(s, o - .5 * w);
        }
        cleanLinearFunctionMap(offset);
        cleanLinearFunctionMap(width);
        cleanLinearFunctionMap(left);
        cleanLinearFunctionMap(right);

        ContinuousPiecewiseLinearFunction offsetFunc = new ContinuousPiecewiseLinearFunction(offset);
        PolyLine2d line = forward ? curve.toPolyLine(OFFSET_FLATTENER, offsetFunc)
                : curve.toPolyLine(OFFSET_FLATTENER, offsetFunc).reverse();
        PolyLine2d leftLine = curve.toPolyLine(OFFSET_FLATTENER, new ContinuousPiecewiseLinearFunction(left));
        PolyLine2d rightLine = curve.toPolyLine(OFFSET_FLATTENER, new ContinuousPiecewiseLinearFunction(right));
        Polygon2d cont = LaneGeometryUtil.getContour(leftLine, rightLine);

        return new CrossSectionGeometry(new OtsLine2d(line), cont, offsetFunc, new ContinuousPiecewiseLinearFunction(width));
    }

    /**
     * Record of a connection. A connection is defined as:
     * <ul>
     * <li>On a junction it is always the connecting point of the other road.</li>
     * <li>If both end-points are START or END, it is defined by the road with lower id.</li>
     * <li>Else it is defined by the road with START end-point.</li>
     * </ul>
     * @param id road id
     * @param contactPoint contact point on road
     * @param forward whether the direction is in the design line direction, or opposite
     */
    private record Connection(String id, EContactPoint contactPoint, boolean forward)
    {
    }

    /**
     * Return connection between two links. A connection is defined as:
     * <ul>
     * <li>On a junction it is always the connecting point of the other road.</li>
     * <li>If both end-points are START or END, it is defined by the road with lower id.</li>
     * <li>Else it is defined by the road with START end-point.</li>
     * </ul>
     * @param road road we are considering
     * @param start start of design line of considered road (end otherwise)
     * @param forward whether we are considering the design line direction of the considered road
     * @return connection
     */
    private Connection getConnection(final TRoad road, final boolean start, final boolean forward)
    {
        EContactPoint contactPoint = start ? EContactPoint.START : EContactPoint.END;
        TRoadLinkPredecessorSuccessor other =
                road.getLink() == null ? null : (start ? road.getLink().getPredecessor() : road.getLink().getSuccessor());
        boolean isOnJunction = road.getJunction() != null && !road.getJunction().isBlank() && !road.getJunction().equals("-1");
        if (!isOnJunction
                && (road.getLink() == null || other == null || other.getElementType().equals(ERoadLinkElementType.JUNCTION)
                        || (start && other.getContactPoint().equals(EContactPoint.END))
                        || (other.getContactPoint().equals(contactPoint) && other.getElementId().compareTo(road.getId()) > 0)))
        {
            return new Connection(road.getId(), contactPoint, forward);
        }
        boolean forwardOnOtherRoad = (forward && ((start && other.getContactPoint().equals(EContactPoint.END))
                || (!start && other.getContactPoint().equals(EContactPoint.START))))
                || (!forward && ((start && other.getContactPoint().equals(EContactPoint.START))
                        || (!start && other.getContactPoint().equals(EContactPoint.END))));
        return new Connection(other.getElementId(), other.getContactPoint(), forwardOnOtherRoad);
    }

}
