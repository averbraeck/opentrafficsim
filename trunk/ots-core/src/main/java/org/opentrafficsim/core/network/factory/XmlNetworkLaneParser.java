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

import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.geotools.NodeGeotools;
import org.opentrafficsim.core.network.lane.CrossSectionElement;
import org.opentrafficsim.core.network.point2d.NodePoint2D;
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

    /** the ID class of the Link. */
    private final Class<?> linkIdClass;

    /** the generated network. */
    private Network<?, ?> network;

    /** the speed units. */
    private static final Map<String, SpeedUnit> SPEED_UNITS = new HashMap<>();

    /** the length units. */
    private static final Map<String, LengthUnit> LENGTH_UNITS = new HashMap<>();

    /** the nodes for further reference. */
    @SuppressWarnings("rawtypes")
    private Map<String, Node> nodes = new HashMap<>();

    static
    {
        SPEED_UNITS.put("km/h", SpeedUnit.KM_PER_HOUR);
        SPEED_UNITS.put("mi/h", SpeedUnit.MILE_PER_HOUR);
        SPEED_UNITS.put("m/s", SpeedUnit.METER_PER_SECOND);

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
     * @param linkIdClass the ID class of the Link.
     */
    public XmlNetworkLaneParser(final Class<?> networkIdClass, final Class<?> nodeClass, final Class<?> nodeIdClass,
        final Class<?> nodePointClass, final Class<?> linkIdClass)
    {
        this.networkIdClass = networkIdClass;
        this.nodeClass = nodeClass;
        this.nodeIdClass = nodeIdClass;
        this.nodePointClass = nodePointClass;
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
        private String content = null;

        /** depth list. */
        private Deque<String> stack = new ArrayDeque<String>();

        /** global values from the GLOBAL tag. */
        private GlobalTag global;

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
                                    this.global = new GlobalTag();
                                    if (attributes.getValue("SPEED") != null)
                                    {
                                        this.global.setSpeed(parseSpeedAbs(attributes.getValue("SPEED")));
                                    }
                                    if (attributes.getValue("WIDTH") != null)
                                    {
                                        this.global.setWidth(parseLengthRel(attributes.getValue("WIDTH")));
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
                                    System.out.println("LINK");
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
                                    System.out.println("STRAIGHT");
                                    break;
                                case "ARC":
                                    System.out.println("ARC");
                                    break;
                                case "LANE":
                                    System.out.println("LANE");
                                    // emp.id = attributes.getValue("id");
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
            catch (NetworkException ne)
            {
                throw new SAXException(ne);
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
                                System.out.println("/LINK");
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
                                System.out.println("/STRAIGHT");
                                break;
                            case "ARC":
                                System.out.println("/ARC");
                                break;
                            case "LANE":
                                System.out.println("/LANE");
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

            // // empList.add(emp);
            // break;
            // // For all other end tags the employee has to be updated.
            // case "firstName":
            // // emp.firstName = content;
            // break;
        }

        @Override
        public void characters(final char[] ch, final int start, final int length) throws SAXException
        {
            this.content = String.copyValueOf(ch, start, length).trim();
        }
    }

    /** GLOBAL element. */
    protected class GlobalTag
    {
        /** default speed. */
        private DoubleScalar.Abs<SpeedUnit> speed = null;

        /** default lane width. */
        private DoubleScalar.Rel<LengthUnit> width = null;

        /**
         * @return speed.
         */
        public final DoubleScalar.Abs<SpeedUnit> getSpeed()
        {
            return this.speed;
        }

        /**
         * @param speed set speed.
         */
        public final void setSpeed(final DoubleScalar.Abs<SpeedUnit> speed)
        {
            this.speed = speed;
        }

        /**
         * @return width.
         */
        public final DoubleScalar.Rel<LengthUnit> getWidth()
        {
            return this.width;
        }

        /**
         * @param width set width.
         */
        public final void setWidth(final DoubleScalar.Rel<LengthUnit> width)
        {
            this.width = width;
        }
    }

    /**
     * @param original the original string
     * @return the cleaned string for further parsing.
     */
    protected final String clean(final String original)
    {
        // clear storage.
        this.nodes.clear();

        // take out the comments and the special characters we are not interested in
        boolean keep = true;
        boolean inString = false;
        String ns = "";
        for (int i = 0; i < original.length(); i++)
        {
            char c = original.charAt(i);
            if (c == '"')
            {
                inString = !inString;
            }
            if (c == '#')
            {
                keep = false;
            }
            else if (c == '\r' || c == '\n')
            {
                keep = true;
            }
            if (keep)
            {
                if (!inString && c != ',' && c != ';' & c != '\t' && c != '\r' && c != '\n')
                {
                    ns += c;
                }
                else
                {
                    ns += ' ';
                }
            }
        }
        return ns;
    }

    /**
     * @param nodeArgs the lane arguments to parse.
     * @return the constructed node.
     * @throws NetworkException in case of parsing problems.
     */
    @SuppressWarnings("rawtypes")
    private Node parseNode(final String nodeArgs) throws NetworkException
    {
        StringBuilder ns = new StringBuilder(nodeArgs);
        String nodeName = null;
        Point3d coordinate = null;

        while (ns.length() > 0)
        {
            String token = eatToken(ns, new ArrayList<String>()); // XXX
            switch (token)
            {
                case "NAME": // lane type
                case "N":
                    nodeName = eatValue(ns);
                    nodeName.replace('"', ' ');
                    break;

                case "COORDINATE": // new node
                case "C":
                    double x = eatDoubleValue(ns);
                    double y = eatDoubleValue(ns);
                    coordinate = new Point3d(x, y, 0);
                    break;

                default:
                    throw new NetworkException("Parsing network. NODE: unknown token: " + token);
            }
        }
        if (coordinate == null)
        {
            throw new NetworkException("Parsing network. NODE " + nodeName + ", no known coordinate");
        }
        if (nodeName == null)
        {
            throw new NetworkException("Parsing network. NODE has no name");
        }
        Node node = makeNode(this.nodeClass, makeId(this.nodeIdClass, nodeName), makePoint(this.nodePointClass, coordinate));
        this.nodes.put(node.getId().toString(), node);
        return node;
    }

    /**
     * @param laneArgs the lane arguments to parse.
     * @throws NetworkException in case of parsing problems.
     */
    @SuppressWarnings("rawtypes")
    private void parseLink(final String laneArgs) throws NetworkException
    {
        StringBuilder ls = new StringBuilder(laneArgs);

        String linkName = null;
        Node fromNode = null;
        Node toNode = null;
        String type = null;
        DoubleScalar.Rel<LengthUnit> length = null;
        DoubleScalar.Abs<SpeedUnit> speed = null;
        DoubleScalar.Rel<LengthUnit> radius = null;
        double angle = Double.NaN;
        Map<String, CrossSectionElement> lanes = null;

        while (ls.length() > 0)
        {
            String token = eatToken(ls, new ArrayList<String>()); // LINK_TOKENS);
            switch (token)
            {
                case "TYPE": // lane type
                case "T":
                    type = eatValue(ls);
                    break;

                case "NAME": // new node
                case "N":
                    linkName = eatValue(ls);
                    break;

                case "FROMNODE": // from node
                case "FROM":
                case "F":
                    String fromNodeName = eatValue(ls);
                    fromNodeName.replace('"', ' ');
                    fromNode = this.nodes.get(fromNodeName);
                    break;

                case "TONODE": // to node
                case "TO":
                    String toNodeName = eatValue(ls);
                    toNodeName.replace('"', ' ');
                    toNode = this.nodes.get(toNodeName);
                    break;

                case "ELEMENTS": // lanes
                case "E":
                    lanes = parseLanes(ls);
                    break;

                case "LENGTH":
                case "L":
                    // length = eatLengthRel(ls, token);
                    break;

                case "SPEED":
                case "S":
                    // speed = eatSpeedAbs(ls, token);
                    // TODO speed element of a specific lane
                    break;

                case "RADIUS":
                case "R":
                    // radius = eatLengthRel(ls, token);
                    break;

                case "ANGLE":
                case "A":
                    angle = eatDoubleValue(ls);
                    break;

                case "WIDTH":
                case "W":
                    // TODO width element
                    // TODO width element of a specific lane
                    break;

                default:
                    throw new NetworkException("Parsing network. LINK: unknown token: " + token);
            }
        }

        if (linkName == null)
        {
            throw new NetworkException("Parsing network. LINK has no name");
        }
        if (fromNode == null || toNode == null || type == null || lanes == null)
        {
            throw new NetworkException("Parsing network. LINK " + linkName + " has missing elements");
        }
        if (type.equals("S") && length == null)
        {
            throw new NetworkException("Parsing network. LINK " + linkName + " (S) has missing length");
        }
        if (type.equals("C") && (radius == null || angle == Double.NaN))
        {
            throw new NetworkException("Parsing network. LINK " + linkName + " (C) has missing radius or angle");
        }

        // TODO create lanes and link
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
     * @param ns the string to parse
     * @param tokens the tokens to scan for
     * @return the token.
     * @throws NetworkException when token not correct
     */
    @SuppressWarnings("checkstyle:finalparameters")
    private String eatToken(StringBuilder ns, final List<String> tokens) throws NetworkException
    {
        ns.replace(0, ns.length() - 1, ns.toString().trim());
        int eq = ns.indexOf("=");
        String token = ns.substring(0, eq - 1).trim();
        ns.delete(0, eq + 1);
        if (!tokens.contains(token))
        {
            throw new NetworkException("Parsing network. Got token:" + token + ", expected one of:" + tokens);
        }
        return token;
    }

    /**
     * @param args the string to parse
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    @SuppressWarnings("checkstyle:finalparameters")
    protected final String eatValue(StringBuilder args) throws NetworkException
    {
        args.replace(0, args.length() - 1, args.toString().trim());
        if (args.length() == 0)
        {
            throw new NetworkException("Parsing network. Expected value, but none found");
        }
        int space = args.indexOf(" ");
        String value = args.substring(0, space - 1).trim();
        args.delete(0, space + 1);
        return value;
    }

    /**
     * @param args the string to parse
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    @SuppressWarnings("checkstyle:finalparameters")
    protected final double eatDoubleValue(StringBuilder args) throws NetworkException
    {
        String s = eatValue(args);
        try
        {
            double value = Double.parseDouble(s);
            return value;
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network: cannot instantiate number: " + s, nfe);
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
     * @param ns the string to parse
     * @param token the token for debugging purposes.
     * @return the arguments after a token.
     * @throws NetworkException when brackets not correct
     */
    @SuppressWarnings("checkstyle:finalparameters")
    private String eatArgs(StringBuilder ns, final String token) throws NetworkException
    {
        ns.replace(0, ns.length() - 1, ns.toString().trim());
        char bs;
        char be;
        if (ns.charAt(0) == '(')
        {
            bs = '(';
            be = ')';
        }
        else if (ns.charAt(0) == '{')
        {
            bs = '{';
            be = '}';
        }
        else if (ns.charAt(0) == '[')
        {
            bs = '[';
            be = ']';
        }
        else
        {
            throw new NetworkException("Parsing network. After token:" + token + ", expected (, { or [ but got :"
                + ns.charAt(0));
        }
        int index = 0;
        int nrBracket = 0;
        for (int i = 0; i < ns.length() && index == 0; i++)
        {
            if (ns.charAt(i) == bs)
            {
                nrBracket++;
            }
            else if (ns.charAt(i) == be)
            {
                nrBracket--;
            }
            if (nrBracket == 0)
            {
                index = i;
            }
        }

        String args = ns.substring(0, index - 1).trim();
        ns.replace(0, ns.length() - 1, ns.toString().trim());
        return args;
    }

    /**
     * @param elements the lane element string to parse, e.g "|V1:V2|D|A2:A1|"
     * @return map of cross section elements
     */
    private Map<String, CrossSectionElement> parseLanes(final StringBuilder elements)
    {
        // TODO parse lane elements in the link
        return null;
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
            new XmlNetworkLaneParser(String.class, NodeGeotools.class, String.class, Coordinate.class, String.class);
        Network n = nlp.build(url.openStream());
    }
}
