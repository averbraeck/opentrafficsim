package org.opentrafficsim.demo.ntm;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.opentrafficsim.core.network.LinkEdge;
import org.opentrafficsim.demo.ntm.Node.TrafficBehaviourType;
import org.opentrafficsim.demo.ntm.trafficdemand.TripInfoTimeDynamic;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 22 Jan 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class Routes
{

    public static void createRoutes(NTMModel model)
    {
        Collection<GraphPath<BoundedNode, LinkEdge<Link>>> sp1 = null;
        sp1 = new FloydWarshallShortestPaths(model.getAreaGraph()).getShortestPaths();
        model.setDebugLinkList(new LinkedHashMap<String, Link>());
        for (GraphPath<BoundedNode, LinkEdge<Link>> path : sp1)
        {
            BoundedNode origin = path.getStartVertex();
            BoundedNode destination = path.getEndVertex();
            System.out.println("Floyd: origin" + origin.getId() + "  dest " + destination.getId());
            // only generate to "real" destinations
            if (destination.getBehaviourType() == TrafficBehaviourType.NTM
                    || destination.getBehaviourType() == TrafficBehaviourType.CORDON)
            {
                // determine the start and endnode of the first edge that starts from the origin
                // the endNode of this edge is the "Neighbour" area
                BoundedNode startNode = (BoundedNode) path.getEdgeList().get(0).getLink().getStartNode();
                // BoundedNode startNode = new BoundedNode(node.getPoint(), node.getId(), null,
                // node.getBehaviourType());
                BoundedNode endNode = (BoundedNode) path.getEdgeList().get(0).getLink().getEndNode();
                // BoundedNode endNode = new BoundedNode(node.getPoint(), node.getId(), null,
                // node.getBehaviourType());

                // the order of endNode and startNode of the edge seems to be not consistent!!!!!!
                if (origin.equals(endNode))
                {
                    endNode = startNode;
                }
                // for all node - destination pairs add information on their first neighbour on the shortest path
                BoundedNode graphEndNode = (BoundedNode) model.getNodeAreaGraphMap().get(endNode.getId());
                origin.getCellBehaviour().getTripInfoByNodeMap().get(destination).setNeighbour(graphEndNode);

                if (path.getEdgeList().get(0).getLink().getBehaviourType() == TrafficBehaviourType.FLOW)
                {
                    // for the flow links we create the trip info by Node also for the flow cells
                    LinkCellTransmission ctmLink =
                            (LinkCellTransmission) model.getAreaGraph().getEdge(origin, graphEndNode).getLink();
                    // Loop through the cells and do transmission
                    for (FlowCell cell : ctmLink.getCells())
                    {
                        cell.getCellBehaviourFlow().getTripInfoByNodeMap().get(destination).setNeighbour(graphEndNode);
                    }
                }
            }
        }
    }
    
    /*
    // select OD pairs with trips only. to generate the relevant paths
    Map<String, Map<String, TripInfoTimeDynamic>> trips;
    if (model.COMPRESS_AREAS)
    {
        trips = model.getCompressedTripDemand().getTripInfo();
    }
    else
    {
        trips = model.getTripDemand().getTripInfo();
    }

    if (trips.get(origin.getId()) != null)
    {
        if (trips.get(origin.getId()).get(destination.getId()) != null)
        {
            double trip = trips.get(origin.getId()).get(destination.getId()).getNumberOfTrips();
            // generate the paths between origins and destinations only
            if (trip > 0.0)
            {
                for (LinkEdge<Link> edge : path.getEdgeList())
                {
                    Link link = edge.getLink();
                    model.getDebugLinkList().put(edge.getLink().getId(), link);
                }
            }
        }
    }
    */
    
}
