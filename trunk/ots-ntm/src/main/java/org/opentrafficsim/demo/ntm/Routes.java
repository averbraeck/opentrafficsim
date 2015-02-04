package org.opentrafficsim.demo.ntm;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.opentrafficsim.core.network.LinkEdge;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.demo.ntm.Node.TrafficBehaviourType;
import org.opentrafficsim.demo.ntm.trafficdemand.TripInfoTimeDynamic;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
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

    /**
     * @param model
     * @param numberOfRoutes
     * @param weight_newRoutes
     */
    public static void createRoutes(NTMModel model, int numberOfRoutes, double weight_newRoutes,
            boolean initiateSimulation)
    {
        // every route receives a fixed share
        java.lang.Double addShare = (double) (1.0 / numberOfRoutes);

        for (int i = 0; i < numberOfRoutes; i++)
        {
            Collection<GraphPath<BoundedNode, LinkEdge<Link>>> sp1 = null;
            sp1 = new FloydWarshallShortestPaths(model.getAreaGraph()).getShortestPaths();
            model.setDebugLinkList(new LinkedHashMap<String, Link>());

            // create stochastic link edge weights
            for (LinkEdge le : model.getAreaGraph().edgeSet())
            {
                double rateCongestedVersusFreeSpeed = 1.0;
                if (!initiateSimulation)
                {
                    BoundedNode startNode =
                            (BoundedNode) model.getNodeAreaGraphMap().get(le.getLink().getStartNode().getId());
                    BoundedNode endNode =
                            (BoundedNode) model.getNodeAreaGraphMap().get(le.getLink().getEndNode().getId());
                    if (startNode.getBehaviourType() == TrafficBehaviourType.NTM
                            && endNode.getBehaviourType() == TrafficBehaviourType.NTM)
                    {
                        CellBehaviourNTM cellBehaviourStart = (CellBehaviourNTM) startNode.getCellBehaviour();
                        if (cellBehaviourStart.getAccumulatedCars() > 0)
                        {
                            double currentSpeedStart =
                                    cellBehaviourStart.retrieveCurrentSpeed(cellBehaviourStart.getAccumulatedCars())
                                            .getInUnit(SpeedUnit.KM_PER_HOUR);
                            double freeSpeedStart =
                                    cellBehaviourStart.getParametersNTM().getFreeSpeed()
                                            .getInUnit(SpeedUnit.KM_PER_HOUR);
                            rateCongestedVersusFreeSpeed = currentSpeedStart / freeSpeedStart;
                            CellBehaviourNTM cellBehaviourEnd = (CellBehaviourNTM) endNode.getCellBehaviour();
                            double currentSpeedEnd =
                                    cellBehaviourEnd.retrieveCurrentSpeed(cellBehaviourEnd.getAccumulatedCars())
                                            .getInUnit(SpeedUnit.KM_PER_HOUR);
                            double freeSpeedEnd =
                                    cellBehaviourEnd.getParametersNTM().getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR);
                            rateCongestedVersusFreeSpeed = currentSpeedEnd / freeSpeedEnd;
                        }
                    }
                }
                double weight = rateCongestedVersusFreeSpeed * model.getAreaGraph().getEdgeWeight(le) * Gaussian();
                model.getAreaGraph().setEdgeWeight(le, weight);
            }

            for (GraphPath<BoundedNode, LinkEdge<Link>> path : sp1)
            {
                BoundedNode origin = path.getStartVertex();
                BoundedNode destination = path.getEndVertex();
                // System.out.println("Floyd: origin" + origin.getId() + "  dest " + destination.getId());
                // only generate to "real" destinations
                if (destination.getBehaviourType() == TrafficBehaviourType.NTM
                        || destination.getBehaviourType() == TrafficBehaviourType.CORDON)
                {
                    // determine the start and end node of the first edge that starts from the origin
                    // the endNode of this edge is the "Neighbour" area
                    BoundedNode startNode = (BoundedNode) path.getEdgeList().get(0).getLink().getStartNode();
                    BoundedNode endNode = (BoundedNode) path.getEdgeList().get(0).getLink().getEndNode();
                    // the order of endNode and startNode of the edge seems to be not consistent!!!!!!
                    if (origin.equals(endNode))
                    {
                        endNode = startNode;
                    }

                    double weightNew = 0.0;
                    // only at first step of the simulation: initiate variables
                    if (initiateSimulation)
                    {
                        weightNew = 1.0;
                        if (i == 0)
                        {
                            HashMap<BoundedNode, java.lang.Double> neighbours =
                                    new HashMap<BoundedNode, java.lang.Double>();
                            TripInfoByDestination tripInfoByNode = new TripInfoByDestination(neighbours, destination);
                            origin.getCellBehaviour().getTripInfoByNodeMap().put(destination, tripInfoByNode);
                        }
                    }
                    // during the simulation: reset the weight of the old routes (1-weight_newRoutes) and add the new
                    // routes
                    else
                    {
                        weightNew = weight_newRoutes;
                        // reset the "old" shares with the weight_newRoutes
                        for (BoundedNode node : origin.getCellBehaviour().getTripInfoByNodeMap().get(destination)
                                .getNeighbourAndRouteShare().keySet())
                        {
                            java.lang.Double oldShare =
                                    origin.getCellBehaviour().getTripInfoByNodeMap().get(destination)
                                            .getNeighbourAndRouteShare().get(node);
                            origin.getCellBehaviour().getTripInfoByNodeMap().get(destination)
                                    .getNeighbourAndRouteShare().put(node, (1 - weightNew) * oldShare);
                        }
                    }
                    // for all node - destination pairs add information on their first neighbour on the shortest path
                    BoundedNode graphEndNode = (BoundedNode) model.getNodeAreaGraphMap().get(endNode.getId());
                    java.lang.Double share = 0.0;
                    if (origin.getCellBehaviour().getTripInfoByNodeMap().get(destination).getNeighbourAndRouteShare()
                            .containsKey(graphEndNode))
                    {
                        share =
                                origin.getCellBehaviour().getTripInfoByNodeMap().get(destination)
                                        .getNeighbourAndRouteShare().get(graphEndNode);
                    }
                    origin.getCellBehaviour().getTripInfoByNodeMap().get(destination).getNeighbourAndRouteShare()
                            .put(graphEndNode, share + weightNew * addShare);
                    // only for initialisation of routes;
                    if (path.getEdgeList().get(0).getLink().getBehaviourType() == TrafficBehaviourType.FLOW)
                    {
                        // for the flow links we create the trip info by Node also for the flow cells
                        LinkCellTransmission ctmLink =
                                (LinkCellTransmission) model.getAreaGraph().getEdge(origin, graphEndNode).getLink();
                        // Loop through the cells and do transmission
                        for (FlowCell cell : ctmLink.getCells())
                        {
                            // only at first step: initiate variables
                            if (initiateSimulation && i == 0)
                            {
                                HashMap<BoundedNode, java.lang.Double> neighbours =
                                        new HashMap<BoundedNode, java.lang.Double>();
                                TripInfoByDestination tripInfoByNode =
                                        new TripInfoByDestination(neighbours, destination);
                                cell.getCellBehaviourFlow().getTripInfoByNodeMap().put(destination, tripInfoByNode);
                            }
                            // for all node - destination pairs add information on their first neighbour on the shortest
                            // path
                            // if (cell.getCellBehaviourFlow().getTripInfoByNodeMap().get(destination)
                            // .getNeighbourAndRouteShare().containsKey(graphEndNode))
                            // {
                            // share =
                            // cell.getCellBehaviourFlow().getTripInfoByNodeMap().get(destination)
                            // .getNeighbourAndRouteShare().get(graphEndNode);
                            // }
                            // cell.getCellBehaviourFlow().getTripInfoByNodeMap().get(destination)
                            // .getNeighbourAndRouteShare().put(graphEndNode, share + weight_new * addShare);
                            // no route choice within a link!!
                            cell.getCellBehaviourFlow().getTripInfoByNodeMap().get(destination)
                                    .getNeighbourAndRouteShare().put(graphEndNode, 1.0);
                        }
                    }
                    // System.out.println("Floyd: origin" + origin.getId() + "  dest " + destination.getId());

                    // TODO select OD pairs with trips only. to generate the relevant paths
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
                    // for all node - destination pairs add information on their first neighbour on the shortest path

                }
            }
        }
    }

    /**
     * @return
     */
    public static double Gaussian()
    {
        double MEAN = 100.0f;
        double VARIANCE = 5.0f;
        Random fRandom = new Random();
        double number = MEAN + fRandom.nextGaussian() * VARIANCE;
        return number / 100;
    }

}
