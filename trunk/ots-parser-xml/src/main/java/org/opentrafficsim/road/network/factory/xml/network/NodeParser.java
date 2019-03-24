package org.opentrafficsim.road.network.factory.xml.network;

import java.util.HashMap;
import java.util.Map;

import org.djunits.unit.DirectionUnit;
import org.djunits.value.vdouble.scalar.Direction;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.xml.generated.LINK;
import org.opentrafficsim.xml.generated.NETWORK;
import org.opentrafficsim.xml.generated.NODE;

/**
 * NodeParser takes care of parsing the NODE tags in the XML network. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public final class NodeParser
{
    /** */
    public NodeParser()
    {
        // utility class
    }

    /**
     * Parse the Nodes.
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param network NETWORK; the NETWORK tag
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     */
    public static void parseNodes(final OTSRoadNetwork otsNetwork, final NETWORK network) throws NetworkException
    {
        for (NODE xmlNode : network.getNODE())
            new OTSNode(otsNetwork, xmlNode.getNAME(), new OTSPoint3D(xmlNode.getCOORDINATE()));
    }

    /**
     * Calculate the default angles of the Nodes, in case they have not been set. This is based on the STRAIGHT LINK elements in
     * the XML file.
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param network NETWORK; the NETWORK tag
     * @return a map of nodes and their default direction
     */
    public static Map<String, Direction> calculateNodeAngles(final OTSRoadNetwork otsNetwork, final NETWORK network)
    {
        Map<String, Direction> nodeDirections = new HashMap<>();
        for (NODE xmlNode : network.getNODE())
        {
            if (xmlNode.getDIRECTION() != null)
            {
                nodeDirections.put(xmlNode.getNAME(), xmlNode.getDIRECTION());
            }
        }

        for (LINK xmlLink : network.getLINK())
        {
            if (xmlLink.getSTRAIGHT() != null)
            {
                Node startNode = otsNetwork.getNode(xmlLink.getNODESTART());
                Node endNode = otsNetwork.getNode(xmlLink.getNODEEND());
                double direction = Math.atan2(endNode.getPoint().y - startNode.getPoint().y,
                        endNode.getPoint().x - startNode.getPoint().x);
                if (!nodeDirections.containsKey(startNode.getId()))
                {
                    nodeDirections.put(startNode.getId(), new Direction(direction, DirectionUnit.EAST_RADIAN));
                }
                if (!nodeDirections.containsKey(endNode.getId()))
                {
                    nodeDirections.put(endNode.getId(), new Direction(direction, DirectionUnit.EAST_RADIAN));
                }
            }
        }

        for (NODE xmlNode : network.getNODE())
        {
            if (!nodeDirections.containsKey(xmlNode.getNAME()))
            {
                System.err.println("Warning: Node " + xmlNode.getNAME() + " does not have a (calculated) direction");
            }
        }

        return nodeDirections;
    }

}
