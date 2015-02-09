package org.opentrafficsim.core.network.factory;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.geotools.NodeGeotools;
import org.opentrafficsim.core.network.lane.CrossSectionElement;
import org.opentrafficsim.core.network.point2d.NodePoint2D;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Parse a String with a simple representation of a lane-based network. An example of such a network is:
 * 
 * <pre>
 * NODE = {NAME=N1, COORDINATE=(0,0)} # the first node
 * NODE = {NAME=N2} # the second node
 * LINK = {NAME="A4_12", FROM=N1, TO=N2, E="S1|V2:V1|D|A1:A2|S2", T=S, L=50m, s=80km/h, w=4m, w(S1)=1m, w(S2=1m)} # a lane
 * NODE = {NAME="N3 b2"}
 * LINK = {NAME="A4_13", FROM=N2, TO="N3 b2", E="S1|V2:V1|D|A1:A2|S2", T=C, R=100m, A=+90, s=80km/h, w=4m, 
 *         w(S1)=1m, w(S2)=1m} # another lane
 * NODE = {NAME="N4"}
 * LINK = {NAME="A4_14", FROM="N3 b2", TO=N4, E="S1|V2:V1|D|A1:A2&lt;A3|S2", T=S, L=50m, s=80km/h, s(A3)=60km/h, w=4m,
 *         w(S1)=1m, w(S2)=1m}
 * NODE = {NAME="N5"}
 * NODE = {NAME="ENTRY5"}
 * LINK = {NAME="A4_15", FROM="N4", TO=N5, E="S1|V2:V1|D|A1:A2|S2", T=S, L=100m, s=80km/h, w=4m,
 *         w(S1)=1m, w(S2)=1m}
 * LINK = {NAME="LE1", FROM="ENTRY5", TO=N4(A3), E="|A|", T=C, R=50m, a=-45, s=60km/h, w=4m}
 * NODE = {NAME="ENTRY6"}
 * LINK = {NAME="LE2", FROM="ENTRY6", TO=ENTRY5, E="|A|", T=C, R=50m, a=-45, s=60km/h, w=4m}
 * </pre>
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Feb 6, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class NetworkLaneParser
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

    /** the tokens. */
    private static final List<String> TOKENS = new ArrayList<>();

    /** the lane tokens. */
    private static final List<String> LINK_TOKENS = new ArrayList<>();

    /** the node tokens. */
    private static final List<String> NODE_TOKENS = new ArrayList<>();

    /** the speed units. */
    private static final Map<String, SpeedUnit> SPEED_UNITS = new HashMap<>();

    /** the length units. */
    private static final Map<String, LengthUnit> LENGTH_UNITS = new HashMap<>();

    /** the nodes for further reference. */
    @SuppressWarnings("rawtypes")
    private Map<String, Node> nodes = new HashMap<>();

    static
    {
        TOKENS.addAll(Arrays.asList(new String[] {"NODE", "N", "LANE", "L", "GTU", "G"}));
        NODE_TOKENS.addAll(Arrays.asList(new String[] {"COORDINATE", "C", "NAME", "N"}));
        LINK_TOKENS.addAll(Arrays.asList(new String[] {"NAME", "N", "ELEMENTS", "E", "TYPE", "T", "LENGTH", "L", "SPEED",
            "S", "RADIUS", "R", "ANGLE", "A", "FROMNODE", "FROM", "F", "TONODE", "TO", "WIDTH", "W"}));

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
    public NetworkLaneParser(final Class<?> networkIdClass, final Class<?> nodeClass, final Class<?> nodeIdClass,
        final Class<?> nodePointClass, final Class<?> linkIdClass)
    {
        this.networkIdClass = networkIdClass;
        this.nodeClass = nodeClass;
        this.nodeIdClass = nodeIdClass;
        this.nodePointClass = nodePointClass;
        this.linkIdClass = linkIdClass;
    }

    /**
     * @param original the network in the agreed grammar.
     * @return the network with Nodes, Links, and Lanes.
     * @throws NetworkException in case of parsing problems.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public final Network<?, ?> build(final String original) throws NetworkException
    {
        // clear storage.
        this.nodes.clear();

        // take out the comments and the special characters we are not interested in
        boolean keep = true;
        boolean inString = false;
        StringBuilder ns = new StringBuilder();
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
                    ns.append(c);
                }
                else
                {
                    ns.append(' ');
                }
            }
        }

        String networkId = "";

        while (ns.length() > 0)
        {
            String token = eatToken(ns, TOKENS);
            String args = eatArgs(ns, token);

            switch (token)
            {
                case "NODE": // new node
                case "N":
                    parseNode(args);
                    break;

                case "LINK": // new link
                case "L":
                    parseLink(args);
                    break;

                default:
                    throw new NetworkException("Parsing network. NODE: unknown token: " + token);
            }
        }

        Network network = new Network(makeId(this.networkIdClass, networkId));
        return network;
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
            String token = eatToken(ns, NODE_TOKENS);
            switch (token)
            {
                case "NAME": // lane type
                case "N":
                    nodeName = eatValue(ns, token);
                    nodeName.replace('"', ' ');
                    break;

                case "COORDINATE": // new node
                case "C":
                    double x = eatDoubleValue(ns, token);
                    double y = eatDoubleValue(ns, token);
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
            String token = eatToken(ls, LINK_TOKENS);
            switch (token)
            {
                case "TYPE": // lane type
                case "T":
                    type = eatValue(ls, token);
                    break;

                case "NAME": // new node
                case "N":
                    linkName = eatValue(ls, token);
                    break;

                case "FROMNODE": // from node
                case "FROM":
                case "F":
                    String fromNodeName = eatValue(ls, token);
                    fromNodeName.replace('"', ' ');
                    fromNode = this.nodes.get(fromNodeName);
                    break;

                case "TONODE": // to node
                case "TO":
                    String toNodeName = eatValue(ls, token);
                    toNodeName.replace('"', ' ');
                    toNode = this.nodes.get(toNodeName);
                    break;

                case "ELEMENTS": // lanes
                case "E":
                    lanes = parseLanes(ls);
                    break;

                case "LENGTH":
                case "L":
                    length = eatLengthRel(ls, token);
                    break;

                case "SPEED":
                case "S":
                    speed = eatSpeedAbs(ls, token);
                    // TODO speed element of a specific lane
                    break;

                case "RADIUS":
                case "R":
                    radius = eatLengthRel(ls, token);
                    break;

                case "ANGLE":
                case "A":
                    angle = eatDoubleValue(ls, token);
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
    private Object makeId(final Class<?> clazz, final String ids) throws NetworkException
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
    private Object makePoint(final Class<?> clazz, final Point3d p) throws NetworkException
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
    private Node makeNode(final Class<?> clazz, final Object id, final Object point) throws NetworkException
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
     * @param token the token for debugging purposes.
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    @SuppressWarnings("checkstyle:finalparameters")
    private String eatValue(StringBuilder args, final String token) throws NetworkException
    {
        args.replace(0, args.length() - 1, args.toString().trim());
        if (args.length() == 0)
        {
            throw new NetworkException("Parsing network. Expected value for token: " + token + ", but none found");
        }
        int space = args.indexOf(" ");
        String value = args.substring(0, space - 1).trim();
        args.delete(0, space + 1);
        return value;
    }

    /**
     * @param args the string to parse
     * @param token the token for debugging purposes.
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    @SuppressWarnings("checkstyle:finalparameters")
    private double eatDoubleValue(StringBuilder args, final String token) throws NetworkException
    {
        String s = eatValue(args, token);
        try
        {
            double value = Double.parseDouble(s);
            return value;
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network. Token " + token + ": cannot instantiate number: " + s, nfe);
        }
    }

    /**
     * @param s the string to parse
     * @param token the token for debugging purposes.
     * @return the unit.
     * @throws NetworkException when parsing fails
     */
    @SuppressWarnings("checkstyle:finalparameters")
    private SpeedUnit eatSpeedUnit(StringBuilder s, final String token) throws NetworkException
    {
        SpeedUnit u = null;
        for (String us : SPEED_UNITS.keySet())
        {
            if (s.toString().contains(us))
            {
                u = SPEED_UNITS.get(us);
                s.delete(s.indexOf(us), s.indexOf(us) + us.length() - 1);
            }
        }
        if (u == null)
        {
            throw new NetworkException("Parsing network. Token " + token + ": cannot instantiate speed unit in: " + s);
        }
        return u;
    }

    /**
     * @param s the string to parse
     * @param token the token for debugging purposes.
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    @SuppressWarnings("checkstyle:finalparameters")
    private DoubleScalar.Abs<SpeedUnit> eatSpeedAbs(StringBuilder s, final String token) throws NetworkException
    {
        SpeedUnit u = eatSpeedUnit(s, token);
        try
        {
            double value = Double.parseDouble(s.toString());
            return new DoubleScalar.Abs<SpeedUnit>(value, u);
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network. Token " + token + ": cannot instantiate scalar: " + s, nfe);
        }
    }

    /**
     * @param s the string to parse
     * @param token the token for debugging purposes.
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    @SuppressWarnings("checkstyle:finalparameters")
    private DoubleScalar.Rel<SpeedUnit> eatSpeedRel(StringBuilder s, final String token) throws NetworkException
    {
        SpeedUnit u = eatSpeedUnit(s, token);
        try
        {
            double value = Double.parseDouble(s.toString());
            return new DoubleScalar.Rel<SpeedUnit>(value, u);
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network. Token " + token + ": cannot instantiate scalar: " + s, nfe);
        }
    }

    /**
     * @param s the string to parse
     * @param token the token for debugging purposes.
     * @return the unit.
     * @throws NetworkException when parsing fails
     */
    @SuppressWarnings("checkstyle:finalparameters")
    private LengthUnit eatLengthUnit(StringBuilder s, final String token) throws NetworkException
    {
        LengthUnit u = null;
        for (String us : LENGTH_UNITS.keySet())
        {
            if (s.toString().contains(us))
            {
                u = LENGTH_UNITS.get(us);
                s.delete(s.indexOf(us), s.indexOf(us) + us.length() - 1);
            }
        }
        if (u == null)
        {
            throw new NetworkException("Parsing network. Token " + token + ": cannot instantiate length unit in: " + s);
        }
        return u;
    }

    /**
     * @param s the string to parse
     * @param token the token for debugging purposes.
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    @SuppressWarnings("checkstyle:finalparameters")
    private DoubleScalar.Abs<LengthUnit> eatLengthAbs(StringBuilder s, final String token) throws NetworkException
    {
        LengthUnit u = eatLengthUnit(s, token);
        try
        {
            double value = Double.parseDouble(s.toString());
            return new DoubleScalar.Abs<LengthUnit>(value, u);
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network. Token " + token + ": cannot instantiate scalar: " + s, nfe);
        }
    }

    /**
     * @param s the string to parse
     * @param token the token for debugging purposes.
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    @SuppressWarnings("checkstyle:finalparameters")
    private DoubleScalar.Rel<LengthUnit> eatLengthRel(StringBuilder s, final String token) throws NetworkException
    {
        LengthUnit u = eatLengthUnit(s, token);
        try
        {
            double value = Double.parseDouble(s.toString());
            return new DoubleScalar.Rel<LengthUnit>(value, u);
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network. Token " + token + ": cannot instantiate scalar: " + s, nfe);
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
     */
    public static void main(final String[] args) throws NetworkException
    {
        String s = "NODE = {NAME=N1, COORDINATE=(0,0)} # the first node\n";
        s += "NODE = {NAME=N2} # the second node\n";
        s += "LINK = {NAME=\"A4_12\", FROM=N1, TO=N2, E=\"S1|V2:V1|D|A1:A2|S2\", T=S, L=50m, s=80km/h, w=4m, \n";
        s += "        w(S1)=1m, w(S2=1m)} # a lane\n";
        s += "NODE = {NAME=\"N3 b2\"}\n";
        s += "LINK = {NAME=\"A4_13\", FROM=N2, TO=\"N3 b2\", E=\"S1|V2:V1|D|A1:A2|S2\", T=C, R=100m, A=+90, \n";
        s += "        s=80km/h, w=4m, w(S1)=1m, w(S2)=1m} # another lane\n";
        s += "NODE = {NAME=\"N4\"}\n";
        s += "LINK = {NAME=\"A4_14\", FROM=\"N3 b2\", TO=N4, E=\"S1|V2:V1|D|A1:A2<A3|S2\", T=S, L=50m, s=80km/h, \n";
        s += "        s(A3)=60km/h, w=4m, w(S1)=1m, w(S2)=1m}\n";
        s += "NODE = {NAME=\"N5\"}\n";
        s += "NODE = {NAME=\"ENTRY5\"}\n";
        s += "LINK = {NAME=\"A4_15\", FROM=N4, TO=N5, E=\"S1|V2:V1|D|A1:A2|S2\", T=S, L=100m, s=80km/h, w=4m,\n";
        s += "        w(S1)=1m, w(S2)=1m}\n";
        s += "LINK = {NAME=\"LE1\", FROM=\"ENTRY5\", TO=N4(A3), E=\"|A|\", T=C, R=50m, a=-45, s=60km/h, w=4m}\n";
        s += "NODE = {NAME=\"ENTRY6\"}\n";
        s += "LINK = {NAME=\"LE2\", FROM=\"ENTRY6\", TO=ENTRY5, E=\"|A|\", T=C, R=50m, a=-45, s=60km/h, w=4m}\n";
        NetworkLaneParser nlp =
            new NetworkLaneParser(String.class, NodeGeotools.class, String.class, Coordinate.class, String.class);
        Network n = nlp.build(s);
    }
}
