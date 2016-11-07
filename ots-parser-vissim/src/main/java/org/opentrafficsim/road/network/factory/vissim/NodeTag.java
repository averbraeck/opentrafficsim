package org.opentrafficsim.road.network.factory.vissim;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.djunits.unit.AngleUnit;
import org.djunits.value.vdouble.scalar.Direction;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNode;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

import nl.tudelft.simulation.language.Throw;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class NodeTag implements Serializable {
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** Coordinate (null at first, can be calculated later when connected to a link. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    OTSPoint3D coordinate = null;

    /** Absolute angle of the node. 0 is "East", pi/2 = "North". */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Direction angle = null;

    /** TODO slope as an angle. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Direction slope = null;

    /** The calculated Node, either through a coordinate or after calculation. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    OTSNode node = null;

    /**
     * @param nodeList nodeList the top-level nodes of the XML-file
     * @param parser the parser with the lists of information
     * @throws SAXException when parsing of GTU tag fails
     * @throws NetworkException when parsing of GTU tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseNodes(final VissimNetworkLaneParser parser, String fromNode, String toNode, OTSPoint3D[] points)
        throws SAXException, NetworkException {
        NodeTag nodeFromTag = new NodeTag();
        nodeFromTag.name = fromNode;
        nodeFromTag.coordinate = points[0];
        // TODO slope for the Node.
        generateOTSNode(parser, nodeFromTag);
        parser.nodeTags.put(nodeFromTag.name, nodeFromTag);

        NodeTag nodeToTag = new NodeTag();
        nodeToTag.name = toNode;
        nodeToTag.coordinate = points[points.length - 1];
        // TODO slope for the Node.
        generateOTSNode(parser, nodeToTag);
        parser.nodeTags.put(nodeToTag.name, nodeToTag);
    }

    /**
     * @param parser
     * @param nodeTag
     * @throws NetworkException
     */
    private static void generateOTSNode(final VissimNetworkLaneParser parser, NodeTag nodeTag) throws NetworkException {
        if (nodeTag.coordinate != null && nodeTag.angle != null) {
            // only make a node if we know the coordinate and angle. Otherwise, wait till we can calculate it.
            try {
                makeOTSNode(nodeTag, parser);
            } catch (NamingException exception) {
                throw new NetworkException(exception);
            }
        }
    }

    /**
     * Parse a list of Nodes, e.g. for a ROUTE.
     * @param nodeNames the space separated String with the node names
     * @param parser the parser with the lists of information
     * @return a list of NodeTags
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    static List<NodeTag> parseNodeList(final String nodeNames, final VissimNetworkLaneParser parser) throws SAXException,
        NetworkException {
        List<NodeTag> nodeList = new ArrayList<>();
        String[] ns = nodeNames.split("\\s");
        for (String s : ns) {
            if (!parser.nodeTags.containsKey(s)) {
                throw new SAXException("Node " + s + " from node list [" + nodeNames + "] was not defined");
            }
            nodeList.add(parser.nodeTags.get(s));
        }
        return nodeList;

    }

    /**
     * @param nodeTag the tag with the info for the node.
     * @param parser the parser with the lists of information
     * @return a constructed node
     * @throws NetworkException when point cannot be instantiated
     * @throws NamingException when animation context cannot be found.
     */
    static OTSNode makeOTSNode(final NodeTag nodeTag, final VissimNetworkLaneParser parser) throws NetworkException,
        NamingException {
        Throw.whenNull(nodeTag.angle, "NodeTag: " + nodeTag.name + " angle == null");
        if (nodeTag.node != null) {
            nodeTag.node.getNetwork().removeNode(nodeTag.node);
        }
        String id = nodeTag.name;
        Direction angle = nodeTag.angle;
        Direction slope = nodeTag.slope == null ? new Direction(0.0, AngleUnit.SI) : nodeTag.slope;
        OTSNode node = new OTSNode(parser.network, id, nodeTag.coordinate, angle, slope);
        nodeTag.node = node;
        return node;
    }

    public static void removeDuplicateNodes(final VissimNetworkLaneParser parser) {
        // the map with NodeTag (key) that should be deleted and replaced by another nodeTag (value)
        Map<String, String> replaceNodeMap = new HashMap<>();
        Iterator<NodeTag> nodeTagValues = parser.nodeTags.values().iterator();

        // determine identical nodes
        while (nodeTagValues.hasNext()) {
            NodeTag nodeTag = nodeTagValues.next();
            // compare to other nodeTags
            Iterator<NodeTag> nodeTagValuesCopy = parser.nodeTags.values().iterator();
            while (nodeTagValuesCopy.hasNext()) {
                NodeTag nodeTagCopy = nodeTagValuesCopy.next();
                // if the coordinates are equal (distance == 0)
                if (nodeTagCopy.coordinate.distance(nodeTag.coordinate).si == 0 && !nodeTagCopy.name.equals(nodeTag.name)) {
                    // is there an already found and handled duplicate node?
                    if (replaceNodeMap.get(nodeTag.name) == null) {
                        replaceNodeMap.put(nodeTagCopy.name, nodeTag.name);
                    }
                }
            }
        }

        // rename nodes in linkTags nodeTags
        Iterator<LinkTag> linkTagValues = parser.linkTags.values().iterator();
        while (linkTagValues.hasNext()) {
            LinkTag linkTag = linkTagValues.next();
            NodeTag nodeTag = linkTag.nodeEndTag;
            if (replaceNodeMap.get(nodeTag.name) != null) {
                linkTag.nodeEndTag = parser.nodeTags.get(replaceNodeMap.get(nodeTag.name));
            }

            NodeTag node2Tag = linkTag.nodeStartTag;
            if (replaceNodeMap.get(node2Tag.name) != null) {
                linkTag.nodeStartTag = parser.nodeTags.get(replaceNodeMap.get(node2Tag.name));
            }
        }

        // remove duplicate nodes from nodeTags
        Iterator<String> nodeTagNames = replaceNodeMap.keySet().iterator();
        while (nodeTagNames.hasNext()) {
            String nodeTagName = nodeTagNames.next();
            parser.nodeTags.remove(nodeTagName);
        }

    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "NodeTag [name=" + this.name + ", coordinate=" + this.coordinate + ", node=" + this.node + "]";
    }

    /**
     * @param linkTag
     * @param parser
     * @param position
     * @return
     * @throws OTSGeometryException
     * @throws NetworkException
     */
    public static NodeTag createNewNodeAtLinkPosition(LinkTag linkTag, VissimNetworkLaneParser parser, Double position)
        throws OTSGeometryException, NetworkException {
        // make a new Node
        NodeTag nodeTag = new NodeTag();
        // generate a LineString
        OTSLine3D designLineOTS = LinkTag.createLineString(linkTag);
        LineString designLine = designLineOTS.getLineString();
        // find the coordinates at the position
        LineString designLineStart = SubstringLine.getSubstring(designLine, 0.0, position);
        Coordinate[] points = designLineStart.getCoordinates();
        OTSPoint3D point = new OTSPoint3D(points[points.length - 1]);
        nodeTag.coordinate = point;
        // add a unique name
        String nodeName = "" + parser.upperNodeNr;
        parser.upperNodeNr++;
        nodeTag.name = nodeName;
        // TODO slope for the Node.
        generateOTSNode(parser, nodeTag);
        parser.nodeTags.put(nodeTag.name, nodeTag);
        return nodeTag;
    }

}
