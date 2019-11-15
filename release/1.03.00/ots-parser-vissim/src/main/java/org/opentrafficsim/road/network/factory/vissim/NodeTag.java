package org.opentrafficsim.road.network.factory.vissim;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Direction;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class NodeTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** Coordinate (null at first, can be calculated later when connected to a link. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    OTSPoint3D coordinate = null;

    /** Coordinate (null at first, can be calculated later when connected to a link. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    // TODO: The direction is not yet calculated
    Direction direction = null;

    /** The calculated Node, either through a coordinate or after calculation. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    OTSRoadNode node = null;

    /**
     * @param parser VissimNetworkLaneParser; VissimParser
     * @param fromNode String; coming from Node
     * @param toNode String; going to Node
     * @param points OTSPoint3D[]; geometry
     * @throws SAXException
     * @throws NetworkException
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseNodes(final VissimNetworkLaneParser parser, String fromNode, String toNode, OTSPoint3D[] points)
            throws SAXException, NetworkException
    {
        NodeTag nodeFromTag = new NodeTag();
        nodeFromTag.name = fromNode;
        nodeFromTag.coordinate = points[0];
        // TODO slope for the Node.
        generateOTSNode(parser, nodeFromTag);
        parser.getNodeTags().put(nodeFromTag.name, nodeFromTag);

        NodeTag nodeToTag = new NodeTag();
        nodeToTag.name = toNode;
        nodeToTag.coordinate = points[points.length - 1];
        // TODO slope for the Node.
        generateOTSNode(parser, nodeToTag);
        parser.getNodeTags().put(nodeToTag.name, nodeToTag);
    }

    /**
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @param nodeTag NodeTag; node Info
     * @throws NetworkException
     */
    private static void generateOTSNode(final VissimNetworkLaneParser parser, final NodeTag nodeTag) throws NetworkException
    {
        if (nodeTag.coordinate != null)
        {
            // only make a node if we know the coordinate. Otherwise, wait till we can calculate it.
            try
            {
                makeOTSNode(nodeTag, parser);
            }
            catch (NamingException exception)
            {
                throw new NetworkException(exception);
            }
        }
    }

    /**
     * Parse a list of Nodes, e.g. for a ROUTE.
     * @param nodeNames String; the space separated String with the node names
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @return a list of NodeTags
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    static List<NodeTag> parseNodeList(final String nodeNames, final VissimNetworkLaneParser parser)
            throws SAXException, NetworkException
    {
        List<NodeTag> nodeList = new ArrayList<>();
        String[] ns = nodeNames.split("\\s");
        for (String s : ns)
        {
            if (!parser.getNodeTags().containsKey(s))
            {
                throw new SAXException("Node " + s + " from node list [" + nodeNames + "] was not defined");
            }
            nodeList.add(parser.getNodeTags().get(s));
        }
        return nodeList;

    }

    /**
     * @param nodeTag NodeTag; the tag with the info for the node.
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @return a constructed node
     * @throws NetworkException when point cannot be instantiated
     * @throws NamingException when animation context cannot be found.
     */
    static OTSRoadNode makeOTSNode(final NodeTag nodeTag, final VissimNetworkLaneParser parser)
            throws NetworkException, NamingException
    {
        if (nodeTag.node != null)
        {
            nodeTag.node.getNetwork().removeNode(nodeTag.node);
        }
        String id = nodeTag.name;
        OTSRoadNode node = new OTSRoadNode(parser.getNetwork(), id, nodeTag.coordinate, nodeTag.direction);
        nodeTag.node = node;
        return node;
    }

    /**
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     */
    public static void removeDuplicateNodes(final VissimNetworkLaneParser parser)
    {
        // the map with NodeTag (key) that should be deleted and replaced by another nodeTag (value)
        Map<String, String> replaceNodeMap = new LinkedHashMap<>();
        Iterator<NodeTag> nodeTagValues = parser.getNodeTags().values().iterator();

        // determine identical nodes
        while (nodeTagValues.hasNext())
        {
            NodeTag nodeTag = nodeTagValues.next();
            // compare to other nodeTags
            Iterator<NodeTag> nodeTagValuesCopy = parser.getNodeTags().values().iterator();
            while (nodeTagValuesCopy.hasNext())
            {
                NodeTag nodeTagCopy = nodeTagValuesCopy.next();
                // if the coordinates are equal (distance == 0)
                if (nodeTagCopy.coordinate.distance(nodeTag.coordinate).si == 0 && !nodeTagCopy.name.equals(nodeTag.name))
                {
                    // is there an already found and handled duplicate node?
                    if (replaceNodeMap.get(nodeTag.name) == null)
                    {
                        replaceNodeMap.put(nodeTagCopy.name, nodeTag.name);
                    }
                }
            }
        }

        // rename nodes in linkTags nodeTags
        Iterator<LinkTag> linkTagValues = parser.getLinkTags().values().iterator();
        while (linkTagValues.hasNext())
        {
            LinkTag linkTag = linkTagValues.next();
            NodeTag nodeTag = linkTag.nodeEndTag;
            if (replaceNodeMap.get(nodeTag.name) != null)
            {
                linkTag.nodeEndTag = parser.getNodeTags().get(replaceNodeMap.get(nodeTag.name));
            }

            NodeTag node2Tag = linkTag.nodeStartTag;
            if (replaceNodeMap.get(node2Tag.name) != null)
            {
                linkTag.nodeStartTag = parser.getNodeTags().get(replaceNodeMap.get(node2Tag.name));
            }
        }

        // remove duplicate nodes from nodeTags
        Iterator<String> nodeTagNames = replaceNodeMap.keySet().iterator();
        while (nodeTagNames.hasNext())
        {
            String nodeTagName = nodeTagNames.next();
            parser.getNodeTags().remove(nodeTagName);
        }

    }

    /**
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     */
    public static void removeRedundantNodeTags(final VissimNetworkLaneParser parser)
    {
        Iterator<NodeTag> nodeTagValues;
        Iterator<LinkTag> linkTagValues;
        Iterator<LinkTag> connectoTagValues;
        // remove redundant nodes from nodeTags
        Map<String, NodeTag> removeNodeMap = new LinkedHashMap<>();
        nodeTagValues = parser.getNodeTags().values().iterator();
        while (nodeTagValues.hasNext())
        {
            NodeTag nodeTag = nodeTagValues.next();
            linkTagValues = parser.getLinkTags().values().iterator();
            boolean found = false;
            while (linkTagValues.hasNext())
            {
                LinkTag linkTag = linkTagValues.next();
                if (linkTag.nodeStartTag.name.equals(nodeTag.name) || linkTag.nodeEndTag.name.equals(nodeTag.name))
                {
                    found = true;
                }
            }
            // connectoTagValues = parser.connectorTags.values().iterator();
            // while (connectoTagValues.hasNext()) {
            // LinkTag linkTag = connectoTagValues.next();
            // if (linkTag.nodeStartTag.equals(nodeTag.name) || linkTag.nodeEndTag.equals(nodeTag.name)) {
            // found = true;
            // }
            // }
            if (!found)
            {
                removeNodeMap.put(nodeTag.name, nodeTag);
            }
        }
        for (NodeTag nodeTag : removeNodeMap.values())
        {
            parser.getNodeTags().remove(nodeTag.name);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "NodeTag [name=" + this.name + ", coordinate=" + this.coordinate + ", node=" + this.node + "]";
    }

    /**
     * @param linkTag LinkTag; link with info
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @param position Double; position at the link where a node is created
     * @return NodeTag
     * @throws OTSGeometryException
     * @throws NetworkException
     */
    public static NodeTag createNewNodeAtLinkPosition(final LinkTag linkTag, final VissimNetworkLaneParser parser,
            final Double position) throws OTSGeometryException, NetworkException
    {
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
        String nodeName = "" + parser.getUpperNodeNr();
        parser.setUpperNodeNr(parser.getUpperNodeNr() + 1);
        nodeTag.name = nodeName;
        // TODO slope for the Node.
        generateOTSNode(parser, nodeTag);
        parser.getNodeTags().put(nodeTag.name, nodeTag);
        return nodeTag;
    }

}
