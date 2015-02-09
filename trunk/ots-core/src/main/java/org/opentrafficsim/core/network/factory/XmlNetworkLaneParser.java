package org.opentrafficsim.core.network.factory;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import nl.tudelft.simulation.language.io.URLResource;

import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.geotools.LinkGeotools;
import org.opentrafficsim.core.network.geotools.NodeGeotools;
import org.opentrafficsim.core.network.lane.CrossSectionElement;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.core.network.lane.Shoulder;
import org.opentrafficsim.core.network.point2d.NodePoint2D;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
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
 * &lt;NETWORK xmlns="http://www.example.org/ots-infra" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *     xsi:schemaLocation="http://www.example.org/ots-infra ots-infra.xsd "&gt;
 * 
 *     &lt;GLOBAL WIDTH="4m" SPEED="80km/h" /&gt;
 * 
 *     &lt;NODE NAME="N1" COORDINATE="(0,0)" /&gt;
 *     &lt;NODE NAME="N2" /&gt;
 * 
 *     &lt;LINK NAME="A4_12" FROM="N1" TO="N2" ELEMENTS="X1|V2:V1|D|A1:A2|X2"&gt;
 *         &lt;STRAIGHT LENGTH="50m" SPEED="80km/h" WIDTH="4m" /&gt;
 *         &lt;LANE NAME="X1" WIDTH="1m" /&gt;
 *         &lt;LANE NAME="X2" WIDTH="1m" /&gt;
 *     &lt;/LINK&gt;
 * 
 *     &lt;NODE NAME="N3b2" /&gt;
 * 
 *     &lt;LINK NAME="A4_13" FROM="N2" TO="N3 b2" ELEMENTS="X1|V2:V1|D|A1:A2|X2"&gt;
 *         &lt;ARC RADIUS="100m" ANGLE="90" /&gt;
 *         &lt;LANE NAME="X1" WIDTH="1m" /&gt;
 *         &lt;LANE NAME="X2" WIDTH="1m" /&gt;
 *     &lt;/LINK&gt;
 * 
 *     &lt;NODE NAME="N4" /&gt;
 * 
 *     &lt;LINK NAME="A4_14" FROM="N3b2" TO="N4" ELEMENTS="X1|V2:V1|D|A1:A2|:A3|X2"&gt;
 *         &lt;STRAIGHT LENGTH="50m" SPEED="80km/h" WIDTH="4m" /&gt;
 *         &lt;LANE NAME="X1" WIDTH="1m" /&gt;
 *         &lt;LANE NAME="X2" WIDTH="1m" /&gt;
 *         &lt;LANE NAME="A3" SPEED="60km/h" /&gt;
 *     &lt;/LINK&gt;
 * 
 *     &lt;NODE NAME="N5" /&gt;
 *     &lt;NODE NAME="ENTRY5" /&gt;
 * 
 *     &lt;LINK NAME="A4_15" FROM="N4" TO="N5" ELEMENTS="X1|V2:V1|D|A1:A2|X2"&gt;
 *         &lt;STRAIGHT LENGTH="100m" SPEED="80km/h" WIDTH="4m" /&gt;
 *         &lt;LANE NAME="X1" WIDTH="1m" /&gt;
 *         &lt;LANE NAME="X2" WIDTH="1m" /&gt;
 *     &lt;/LINK&gt;
 * 
 *     &lt;LINK NAME="LE1" FROM="ENTRY5" TO="N3b2(A3)" ELEMENTS="|A|"&gt;
 *         &lt;ARC RADIUS="100m" ANGLE="-45" SPEED="60km/h" /&gt;
 *     &lt;/LINK&gt;
 * 
 *     &lt;NODE NAME="ENTRY6" /&gt;
 *     &lt;LINK NAME="LE2" FROM="ENTRY6" TO="ENTRY5" ELEMENTS="|A|"&gt;
 *         &lt;ARC RADIUS="100m" ANGLE="45" SPEED="60km/h" /&gt;
 *     &lt;/LINK&gt;
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
    private final Class<?> networkIdClass;

    /** the class of the Node. */
    private final Class<?> nodeClass;

    /** the ID class of the Node. */
    private final Class<?> nodeIdClass;

    /** the Point class of the Node. */
    private final Class<?> nodePointClass;

    /** the class of the Link. */
    private final Class<?> linkClass;

    /** the ID class of the Link. */
    private final Class<?> linkIdClass;

    /** the generated network. */
    private Network<?, ?> network;

    /** the speed units. */
    private static final Map<String, SpeedUnit> SPEED_UNITS = new HashMap<>();

    /** the length units. */
    private static final Map<String, LengthUnit> LENGTH_UNITS = new HashMap<>();

    /** the nodes for further reference. */
    @SuppressWarnings({"rawtypes", "visibilitymodifier"})
    protected Map<String, Node> nodes = new HashMap<>();

    /** the links for further reference. */
    @SuppressWarnings({"rawtypes", "visibilitymodifier"})
    protected Map<String, Link> links = new HashMap<>();

    /** TODO incorporate into grammar. */
    private final LaneType<String> laneType = new LaneType<String>("CarLane");

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
    }

    /**
     * @param networkIdClass the ID class of the Network.
     * @param nodeClass the class of the Node.
     * @param nodeIdClass the ID class of the Node.
     * @param nodePointClass the Point class of the Node.
     * @param linkClass the class of the Link.
     * @param linkIdClass the ID class of the Link.
     */
    public XmlNetworkLaneParser(final Class<?> networkIdClass, final Class<?> nodeClass, final Class<?> nodeIdClass,
        final Class<?> nodePointClass, final Class<?> linkClass, final Class<?> linkIdClass)
    {
        this.networkIdClass = networkIdClass;
        this.nodeClass = nodeClass;
        this.nodeIdClass = nodeIdClass;
        this.nodePointClass = nodePointClass;
        this.linkClass = linkClass;
        this.linkIdClass = linkIdClass;
    }

    /**
     * @param is the file with the network in the agreed xml-grammar.
     * @return the network with Nodes, Links, and Lanes.
     * @throws NetworkException in case of parsing problems.
     * @throws SAXException in case of parsing problems.
     * @throws ParserConfigurationException in case of parsing problems.
     * @throws IOException in case of file reading problems.
     */
    public final Network<?, ?> build(final InputStream is) throws NetworkException, ParserConfigurationException,
        SAXException, IOException
    {
        // clear storage.
        this.nodes.clear();

        SAXParserFactory parserFactor = SAXParserFactory.newInstance();
        SAXParser parser = parserFactor.newSAXParser();
        SAXHandler handler = new SAXHandler();
        parser.parse(is, handler);

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

        /** link values from the LINK tag. */
        private LinkTag linkTag;

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
            throws SAXException
        {
            try
            {
                if (!qName.equals("NETWORK"))
                {
                    switch (this.stack.getLast())
                    {
                        case "NETWORK":
                            switch (qName)
                            {
                                case "GLOBAL":
                                    this.globalTag = new GlobalTag();
                                    if (attributes.getValue("SPEED") != null)
                                    {
                                        this.globalTag.speed = parseSpeedAbs(attributes.getValue("SPEED"));
                                    }
                                    if (attributes.getValue("WIDTH") != null)
                                    {
                                        this.globalTag.width = parseLengthRel(attributes.getValue("WIDTH"));
                                    }
                                    break;

                                case "NODE":
                                    String nodeName = null;
                                    Point3d coordinate = null;
                                    if (attributes.getValue("NAME") != null)
                                    {
                                        nodeName = attributes.getValue("NAME");
                                    }
                                    else
                                    {
                                        throw new SAXException("NODE: missing attribute NAME");
                                    }
                                    if (attributes.getValue("COORDINATE") != null)
                                    {
                                        String c = attributes.getValue("COORDINATE");
                                        c = c.replace("(", "");
                                        c = c.replace(")", "");
                                        String[] cc = c.split(",");
                                        double x = Double.parseDouble(cc[0]);
                                        double y = Double.parseDouble(cc[1]);
                                        coordinate = new Point3d(x, y, 0);
                                    }
                                    else
                                    {
                                        coordinate = new Point3d(Double.NaN, Double.NaN, Double.NaN);
                                    }
                                    @SuppressWarnings("rawtypes")
                                    Node node =
                                        makeNode(XmlNetworkLaneParser.this.nodeClass, makeId(
                                            XmlNetworkLaneParser.this.nodeIdClass, nodeName), makePoint(
                                            XmlNetworkLaneParser.this.nodePointClass, coordinate));
                                    XmlNetworkLaneParser.this.nodes.put(node.getId().toString(), node);
                                    break;

                                case "LINK":
                                    this.linkTag = new LinkTag();
                                    if (attributes.getValue("NAME") != null)
                                    {
                                        this.linkTag.name = attributes.getValue("NAME");
                                    }
                                    else
                                    {
                                        throw new SAXException("NODE: missing attribute NAME");
                                    }
                                    if (attributes.getValue("ELEMENTS") != null)
                                    {
                                        this.linkTag.elements = attributes.getValue("ELEMENTS");
                                    }
                                    else
                                    {
                                        throw new SAXException("NODE: missing attribute ELEMENTS");
                                    }
                                    if (attributes.getValue("FROM") != null)
                                    {
                                        String fromNodeStr = attributes.getValue("FROM");
                                        @SuppressWarnings("rawtypes")
                                        Node fromNode = XmlNetworkLaneParser.this.nodes.get(fromNodeStr);
                                        this.linkTag.nodeFrom = fromNode;
                                    }
                                    else
                                    {
                                        throw new SAXException("NODE: missing attribute FROM");
                                    }
                                    if (attributes.getValue("TO") != null)
                                    {
                                        String toNodeStr = attributes.getValue("TO");
                                        @SuppressWarnings("rawtypes")
                                        Node toNode = XmlNetworkLaneParser.this.nodes.get(toNodeStr);
                                        this.linkTag.nodeTo = toNode;
                                    }
                                    else
                                    {
                                        throw new SAXException("NODE: missing attribute FROM");
                                    }
                                    if (attributes.getValue("SPEED") != null)
                                    {
                                        this.linkTag.speed = parseSpeedAbs(attributes.getValue("SPEED"));
                                    }
                                    if (attributes.getValue("WIDTH") != null)
                                    {
                                        this.linkTag.width = parseLengthRel(attributes.getValue("WIDTH"));
                                    }
                                    break;

                                default:
                                    throw new SAXException("NETWORK: Received start tag " + qName + ", but stack contains: "
                                        + this.stack);
                            }
                            break;

                        case "LINK":
                            switch (qName)
                            {
                                case "STRAIGHT":
                                    this.linkTag.straightTag = new StraightTag();
                                    if (attributes.getValue("LENGTH") != null)
                                    {
                                        this.linkTag.straightTag.length = parseLengthRel(attributes.getValue("LENGTH"));
                                    }
                                    else
                                    {
                                        throw new SAXException("STRAIGHT: missing attribute LENGTH");
                                    }
                                    break;

                                case "ARC":
                                    this.linkTag.arcTag = new ArcTag();
                                    if (attributes.getValue("RADIUS") != null)
                                    {
                                        this.linkTag.arcTag.radius = parseLengthRel(attributes.getValue("RADIUS"));
                                    }
                                    else
                                    {
                                        throw new SAXException("ARC: missing attribute RADIUS");
                                    }
                                    if (attributes.getValue("ANGLE") != null)
                                    {
                                        this.linkTag.arcTag.angle = Double.parseDouble(attributes.getValue("ANGLE"));
                                    }
                                    else
                                    {
                                        throw new SAXException("ARC: missing attribute ANGLE");
                                    }
                                    break;

                                case "LANE":
                                    LaneTag laneTag = new LaneTag();
                                    if (attributes.getValue("NAME") != null)
                                    {
                                        laneTag.name = attributes.getValue("NAME");
                                    }
                                    else
                                    {
                                        throw new SAXException("LANE: missing attribute NAME");
                                    }
                                    if (attributes.getValue("SPEED") != null)
                                    {
                                        laneTag.speed = parseSpeedAbs(attributes.getValue("SPEED"));
                                    }
                                    if (attributes.getValue("WIDTH") != null)
                                    {
                                        laneTag.width = parseLengthRel(attributes.getValue("WIDTH"));
                                    }
                                    this.linkTag.laneTags.put(laneTag.name, laneTag);
                                    break;
                                default:
                                    throw new SAXException("LINK: Received start tag " + qName + ", but stack contains: "
                                        + this.stack);
                            }
                            break;

                        default:
                            throw new SAXException("startElement: Received start tag " + qName + ", but stack contains: "
                                + this.stack);
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
                                break;
                            case "LINK":
                                @SuppressWarnings("rawtypes")
                                CrossSectionLink link = makeLink(this.linkTag);
                                parseElements(linkTag.elements, link, linkTag, globalTag);
                                XmlNetworkLaneParser.this.links.put(link.getId().toString(), link);
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
                            default:
                                throw new SAXException("LINK: Received end tag " + qName + ", but stack contains: "
                                    + this.stack);
                        }
                        break;

                    default:
                        throw new SAXException("Received end tag " + qName + ", but stack contains: " + this.stack);
                }
            }
        }
    }

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
     * @param id the id as an object
     * @param point the point as an object
     * @return a constructed node
     * @throws NetworkException when point cannot be instantiated
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected final Node makeNode(final Class<?> clazz, final Object id, final Object point) throws NetworkException
    {
        if (NodeGeotools.class.isAssignableFrom(clazz))
        {
            if (point instanceof Coordinate)
            {
                return new NodeGeotools(id, (Coordinate) point);
            }
            throw new NetworkException("Parsing network. Node class " + clazz.getName()
                + ": cannot instantiate. Wrong Coordinate type: " + point.getClass() + ", coordinate: " + point);
        }
        else if (NodePoint2D.class.isAssignableFrom(clazz))
        {
            if (point instanceof Point2D)
            {
                return new NodePoint2D(id, (Point2D) point);
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
     * FIXME LinkGeotools should extend CrossSectionLink and not the other way around.
     * @param linkTag the link information from XML.
     * @return a constructed link
     * @throws SAXException when point cannot be instantiated
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected final CrossSectionLink makeLink(final LinkTag linkTag) throws SAXException
    {
        try
        {
            if (LinkGeotools.class.isAssignableFrom(this.linkClass))
            {
                Object id = makeId(this.linkIdClass, linkTag.name);
                DoubleScalar.Rel<LengthUnit> length = null;
                if (linkTag.straightTag != null)
                {
                    length = linkTag.straightTag.length;
                }
                else if (linkTag.arcTag != null)
                {
                    length =
                        new DoubleScalar.Rel<LengthUnit>(linkTag.arcTag.radius.mutable().multiply(
                            Math.toRadians(Math.abs(linkTag.arcTag.angle))).getInUnit(), linkTag.arcTag.radius.getUnit());
                }
                CrossSectionLink link =
                    new CrossSectionLink(id, (NodeGeotools) linkTag.nodeFrom, (NodeGeotools) linkTag.nodeTo, length);
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
     * @param elements the string such as "X1|V2:V1|D|A1:A2|:A3|X2"
     * @param linkTag the link with possible information about speed and width.
     * @param globalTag the global tag with possible information about speed and width.
     * @return a list of cross-section elements
     * @throws SAXException for unknown lane type or other inconsistencies.
     */
    private List<CrossSectionElement> parseElements(final String elements, final CrossSectionLink csl,
        final LinkTag linkTag, final GlobalTag globalTag) throws SAXException
    {
        List<CrossSectionElement> cseList = new ArrayList<>();
        String[] names = elements.split("(\\|)|(\\:)|(\\|\\:)|(\\:\\|)|(\\|\\|)");
        List<Double> widthsSI = new ArrayList<>();
        int designIndex = -1;
        int i = -1;
        for (String name : names)
        {
            i++;
            if (name.equals("D")) // TODO design line in the middle of a lane
            {
                widthsSI.add(0.0);
                designIndex = i;
            }
            else
            {
                if (linkTag.laneTags.keySet().contains(name) && (linkTag.laneTags.get(name).width != null))
                {
                    widthsSI.add(linkTag.laneTags.get(name).width.getSI());
                }
                else if (linkTag.width != null)
                {
                    widthsSI.add(linkTag.width.getSI());
                }
                else if (globalTag.width != null)
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
            else if (name.startsWith("X")) // forbidden lane (e.g., grass)
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
                    Shoulder shoulder =
                        new Shoulder(csl, new DoubleScalar.Rel<LengthUnit>(offsetSI[i], LengthUnit.SI),
                            new DoubleScalar.Rel<LengthUnit>(widthsSI.get(i), LengthUnit.SI),
                            new DoubleScalar.Rel<LengthUnit>(widthsSI.get(i), LengthUnit.SI));
                    cseList.add(shoulder);
                }
                else
                {
                    Lane lane =
                        new Lane(csl, new DoubleScalar.Rel<LengthUnit>(offsetSI[i], LengthUnit.SI),
                            new DoubleScalar.Rel<LengthUnit>(offsetSI[i], LengthUnit.SI), new DoubleScalar.Rel<LengthUnit>(
                                widthsSI.get(i), LengthUnit.SI), new DoubleScalar.Rel<LengthUnit>(widthsSI.get(i),
                                LengthUnit.SI), this.laneType, ld, new DoubleScalar.Abs<FrequencyUnit>(Double.MAX_VALUE,
                                FrequencyUnit.PER_HOUR));
                    cseList.add(lane);
                }
            }
            catch (NetworkException ne)
            {
                throw new SAXException(ne);
            }
        }
        return cseList;
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
    }

    /** ARC element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected class ArcTag
    {
        /** lane speed. */
        protected double angle = Double.NaN;

        /** radius. */
        protected DoubleScalar.Rel<LengthUnit> radius = null;
    }

    /** STRAIGHT element. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected class StraightTag
    {
        /** length. */
        protected DoubleScalar.Rel<LengthUnit> length = null;
    }

    /**
     * Test.
     * @param args none.
     * @throws NetworkException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static void main(final String[] args) throws NetworkException, ParserConfigurationException, SAXException,
        IOException
    {
        URL url = URLResource.getResource("/ots-infra-example.xml");
        XmlNetworkLaneParser nlp =
            new XmlNetworkLaneParser(String.class, NodeGeotools.class, String.class, Coordinate.class, LinkGeotools.class,
                String.class);
        Network n = nlp.build(url.openStream());
    }
}
