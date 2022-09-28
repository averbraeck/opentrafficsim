package org.opentrafficsim.road.network;

import java.util.LinkedHashMap;
import java.util.Map;

import org.opentrafficsim.core.compatibility.GtuCompatibility;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSLink;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.object.InvisibleObjectInterface;
import org.opentrafficsim.road.network.lane.LaneType;

/**
 * OTSRoadNetworkCloner makes a deep clone of a network.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public final class OTSRoadNetworkUtils
{
    /** */
    private OTSRoadNetworkUtils()
    {
        // utility class
    }

    /**
     * Clone the OTSRoadNetwork.
     * @param network OTSRoadNetwork; the network to clone
     * @param newId String; the new id of the network
     * @param oldSimulator OTSSimulatorInterface; the old simulator for this network
     * @param newSimulator OTSSimulatorInterface; the new simulator for this network
     * @return a clone of this network
     * @throws NetworkException in case the cloning fails
     */
    @SuppressWarnings("checkstyle:designforextension")
    public static OTSRoadNetwork clone(final OTSRoadNetwork network, final String newId,
            final OTSSimulatorInterface oldSimulator, final OTSSimulatorInterface newSimulator) throws NetworkException
    {
        OTSRoadNetwork newNetwork = new OTSRoadNetwork(newId, false, newSimulator);

        // clone the nodes
        for (Node node : network.getNodeMap().values())
        {
            ((OTSNode) node).clone1(newNetwork);
        }

        // clone the links
        for (Link oldLink : network.getLinkMap().values())
        {
            ((OTSLink) oldLink).clone(newNetwork);
        }

        // make the link-connections for the cloned nodes
        for (Node oldNode : network.getNodeMap().values())
        {
            ((OTSNode) oldNode).clone2(newNetwork);
        }

        // clone the graphs that had been created for the old network
        for (GtuType gtuType : network.getLinkGraphs().keySet())
        {
            newNetwork.buildGraph(gtuType);
        }

        // clone the routes
        Map<GtuType, Map<String, Route>> newRouteMap = new LinkedHashMap<>();
        for (GtuType gtuType : network.getRouteMap().keySet())
        {
            Map<String, Route> newRoutes = new LinkedHashMap<>();
            for (Route route : network.getRouteMap().get(gtuType).values())
            {
                newRoutes.put(route.getId(), route.clone(newNetwork));
            }
            newRouteMap.put(gtuType, newRoutes);
        }
        newNetwork.setRawRouteMap(newRouteMap);

        // clone the traffic lights
        for (InvisibleObjectInterface io : network.getInvisibleObjectMap().values())
        {
            InvisibleObjectInterface clonedIO = io.clone(newSimulator, newNetwork);
            newNetwork.addInvisibleObject(clonedIO);
        }

        // TODO clone the visible objects

        // clone the GtuTypes
        for (GtuType gtuType : network.getGtuTypes().values())
        {
            if (gtuType.getParent() == null)
            {
                new GtuType(gtuType.getId(), newNetwork);
            }
        }
        for (GtuType gtuType : network.getGtuTypes().values())
        {
            if (gtuType.getParent() != null)
            {
                new GtuType(gtuType.getId(), gtuType.getParent());
            }
        }

        // clone the LinkTypes
        for (LinkType linkType : network.getLinkTypes().values())
        {
            if (linkType.getParent() != null)
            {
                new LinkType(linkType.getId(), linkType.getParent(), new GtuCompatibility<>(linkType.getGtuCompatibility()),
                        newNetwork);
            }
        }

        // clone the LaneTypes
        for (LaneType laneType : network.getLaneTypes().values())
        {
            if (laneType.getParent() != null)
            {
                new LaneType(laneType.getId(), laneType.getParent(), new GtuCompatibility<>(laneType.getGtuCompatibility()),
                        newNetwork);
            }
        }

        return newNetwork;
    }

    /**
     * Remove all objects and animation in the road network.
     * @param network OTSRoadNetwork; the network to destroy
     * @param simulator OTSSimulatorInterface; the simulator of the old network
     */
    public static void destroy(final OTSRoadNetwork network, final OTSSimulatorInterface simulator)
    {
        OTSRoadNetworkUtils.destroy(network, simulator);
    }

}
