package org.opentrafficsim.core.network;

import java.util.LinkedHashMap;
import java.util.Map;

import org.opentrafficsim.core.compatibility.GTUCompatibility;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.object.InvisibleObjectInterface;

/**
 * OTSNetworkCloner makes a deep clone of a network. <br>
 * <br>
 * Copyright (c) 2003-2022 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public final class OTSNetworkUtils
{
    /** */
    private OTSNetworkUtils()
    {
        // utility class
    }

    /**
     * Clone the OTSNetwork.
     * @param network OTSNetwork; the network to clone
     * @param newId String; the new id of the network
     * @param newSimulator OTSSimulatorInterface; the new simulator for this network
     * @return a clone of this network
     * @throws NetworkException in case the cloning fails
     */
    @SuppressWarnings("checkstyle:designforextension")
    public static OTSNetwork clone(final OTSNetwork network, final String newId, final OTSSimulatorInterface newSimulator)
            throws NetworkException
    {
        OTSNetwork newNetwork = new OTSNetwork(newId, false, newSimulator);

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
        for (GTUType gtuType : network.getLinkGraphs().keySet())
        {
            newNetwork.buildGraph(gtuType);
        }

        // clone the routes
        Map<GTUType, Map<String, Route>> newRouteMap = new LinkedHashMap<>();
        for (GTUType gtuType : network.getRouteMap().keySet())
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

        // clone the GTUTypes
        for (GTUType gtuType : network.getGtuTypes().values())
        {
            if (gtuType.getParent() == null)
            {
                new GTUType(gtuType.getId(), newNetwork);
            }
        }
        for (GTUType gtuType : network.getGtuTypes().values())
        {
            if (gtuType.getParent() != null)
            {
                new GTUType(gtuType.getId(), gtuType.getParent());
            }
        }

        // clone the LinkTypes TODO LinkType should clone itself!
        for (LinkType linkType : network.getLinkTypes().values())
        {
            if (linkType.getParent() != null)
            {
                new LinkType(linkType.getId(), linkType.getParent(), new GTUCompatibility<>(linkType.getCompatibility()),
                        newNetwork);
            }
        }

        return newNetwork;
    }

    /**
     * Remove all objects and animation in the network.
     * @param network OTSNetwork; the network to destroy
     */
    public static void destroy(final OTSNetwork network)
    {
        for (GTU gtu : network.getGTUs())
        {
            gtu.destroy();
        }

        network.getRawNodeMap().clear();
        network.getRawLinkMap().clear();
        network.getRawLinkGraphs().clear();
        network.getRawRouteMap().clear();
    }

}
