package org.opentrafficsim.demo.ntm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Random;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.opentrafficsim.core.network.LinkEdge;
import org.opentrafficsim.demo.ntm.NTMNode.TrafficBehaviourType;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 22 Jan 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class Routes
{
    public final static long seed = 100;

    public static Random fRandom = new Random(seed);

    /**
     * @param model NTMModel;
     * @param numberOfRoutes int;
     * @param weight_newRoutes double;
     * @param VARIANCE double;
     * @param initiateSimulation boolean;
     * @param steps int;
     * @param MAXSTEPS int;
     * @throws IOException
     */
    public static void createRoutes(NTMModel model, int numberOfRoutes, double weight_newRoutes, double VARIANCE,
            final boolean initiateSimulation, int steps, int MAXSTEPS) throws IOException
    {
        // every route receives a fixed share
        java.lang.Double addShare = (double) (1.0 / numberOfRoutes);
        /** */
        BufferedWriter dataRoutesNTMOut = null;
        String fileName = "/ALLroutes";
        File file = new File(model.getSettingsNTM().getPath() + model.getInputNTM().getOutputMap() + fileName + steps + ".txt");
        dataRoutesNTMOut = WriteOutput.createWriter(file);
        for (int i = 0; i < numberOfRoutes; i++)
        {

            // create stochastic link edge weights

            for (LinkEdge<NTMLink> le : model.getAreaGraph().edgeSet())
            {
                double rateCongestedVersusFreeTravelTime = 1.0;
                BoundedNode startNode = (BoundedNode) model.getNodeAreaGraphMap().get(le.getLink().getStartNode().getId());
                BoundedNode endNode = (BoundedNode) model.getNodeAreaGraphMap().get(le.getLink().getEndNode().getId());
                if (!initiateSimulation)
                {

                    if (startNode.getBehaviourType() == TrafficBehaviourType.NTM
                            && endNode.getBehaviourType() == TrafficBehaviourType.NTM)
                    {
                        CellBehaviourNTM cellBehaviourStart = (CellBehaviourNTM) startNode.getCellBehaviour();
                        if (cellBehaviourStart.getAccumulatedCars() > 0)
                        {
                            double currentSpeedStart =
                                    cellBehaviourStart
                                            .retrieveCurrentSpeed(cellBehaviourStart.getAccumulatedCars(),
                                                    cellBehaviourStart.getArea().getRoadLength())
                                            .getInUnit(SpeedUnit.KM_PER_HOUR);
                            double freeSpeedStart =
                                    cellBehaviourStart.getParametersNTM().getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR);
                            rateCongestedVersusFreeTravelTime = 0.5 * freeSpeedStart / currentSpeedStart;
                            CellBehaviourNTM cellBehaviourEnd = (CellBehaviourNTM) endNode.getCellBehaviour();
                            double currentSpeedEnd =
                                    cellBehaviourEnd
                                            .retrieveCurrentSpeed(cellBehaviourEnd.getAccumulatedCars(),
                                                    cellBehaviourStart.getArea().getRoadLength())
                                            .getInUnit(SpeedUnit.KM_PER_HOUR);
                            double freeSpeedEnd =
                                    cellBehaviourEnd.getParametersNTM().getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR);
                            rateCongestedVersusFreeTravelTime += 0.5 * freeSpeedEnd / currentSpeedEnd;
                        }
                    }
                    else if (startNode.getBehaviourType() == TrafficBehaviourType.FLOW
                            && endNode.getBehaviourType() == TrafficBehaviourType.FLOW)
                    {
                        // the endNode of this edge is the "Neighbour" area
                        LinkCellTransmission ctmLink = (LinkCellTransmission) le.getLink();
                        double freeTime = ctmLink.getDuration().getInUnit(DurationUnit.SECOND);
                        double currrentTime = ctmLink.retrieveActualTime().getInUnit(DurationUnit.SECOND);
                        rateCongestedVersusFreeTravelTime = currrentTime / freeTime;

                        if (rateCongestedVersusFreeTravelTime > 10000)
                        {
                            System.out.println("Start: " + startNode.getId() + " End node: " + endNode.getId()
                                    + " currentTime: " + currrentTime + " freeTime: " + currrentTime);
                            double testCurrrentTime = ctmLink.retrieveActualTime().getInUnit(DurationUnit.SECOND);
                        }

                    }
                }

                if (startNode.getBehaviourType() == TrafficBehaviourType.CORDON
                        || endNode.getBehaviourType() == TrafficBehaviourType.CORDON)
                {
                    rateCongestedVersusFreeTravelTime = 999999;
                }
                double speed = le.getLink().getFreeSpeed().getInUnit(SpeedUnit.METER_PER_SECOND);
                double length = le.getLink().getLength().getInUnit(LengthUnit.METER);
                double travelTime = length / speed;
                // double weight =
                // rateCongestedVersusFreeTravelTime * model.getAreaGraph().getEdgeWeight(le) * Gaussian(VARIANCE, 100);
                double weight = rateCongestedVersusFreeTravelTime * travelTime * Gaussian(fRandom, VARIANCE);
                model.getAreaGraph().setEdgeWeight(le, weight);
            }

            FloydWarshallShortestPaths<NTMNode, LinkEdge<NTMLink>> allPaths =
                    new FloydWarshallShortestPaths<>(model.getAreaGraph());
            Collection<GraphPath<NTMNode, LinkEdge<NTMLink>>> sp1 = new LinkedHashSet<>();
            for (NTMNode source : model.getAreaGraph().vertexSet())
            {
                for (NTMNode sink : model.getAreaGraph().vertexSet())
                {
                    sp1.add(allPaths.getPath(source, sink));
                }
            }

            // assign a share to every route (i)
            for (GraphPath<NTMNode, LinkEdge<NTMLink>> path : sp1)
            {
                BoundedNode origin = (BoundedNode) path.getStartVertex();
                BoundedNode destination = (BoundedNode) path.getEndVertex();
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
                    if (origin.getId().equals("1"))
                    {
                        System.out.println("dest " + destination.getId() + " neighbour: " + endNode.getId());
                    }
                    // only at first step of the simulation: initiate variables
                    if (initiateSimulation)
                    {
                        weightNew = 1.0;
                        if (i == 0)
                        {
                            LinkedHashMap<BoundedNode, java.lang.Double> neighbours = new LinkedHashMap<BoundedNode, java.lang.Double>();
                            LinkedHashMap<BoundedNode, java.lang.Double> accumulatedCarsToNeighbour =
                                    new LinkedHashMap<BoundedNode, java.lang.Double>();
                            LinkedHashMap<BoundedNode, java.lang.Double> demandToNeighbour =
                                    new LinkedHashMap<BoundedNode, java.lang.Double>();
                            TripInfoByDestination tripInfoByNode = new TripInfoByDestination(neighbours,
                                    accumulatedCarsToNeighbour, demandToNeighbour, destination);
                            origin.getCellBehaviour().getTripInfoByDestinationMap().put(destination, tripInfoByNode);
                        }
                    }
                    // during the simulation: reset the weight of the old routes (1-weight_newRoutes) and add the new
                    // routes
                    else
                    {
                        weightNew = weight_newRoutes;
                        // reset the "old" shares with the weight_newRoutes
                        if (i == 0)
                        {
                            for (BoundedNode node : origin.getCellBehaviour().getTripInfoByDestinationMap().get(destination)
                                    .getRouteFractionToNeighbours().keySet())
                            {
                                java.lang.Double oldShare = origin.getCellBehaviour().getTripInfoByDestinationMap()
                                        .get(destination).getRouteFractionToNeighbours().get(node);
                                origin.getCellBehaviour().getTripInfoByDestinationMap().get(destination)
                                        .getRouteFractionToNeighbours().put(node, (1 - weightNew) * oldShare);
                            }
                        }
                    }

                    // for all node - destination pairs add information on their first neighbour on the shortest path
                    BoundedNode neighbour = (BoundedNode) model.getNodeAreaGraphMap().get(endNode.getId());
                    java.lang.Double share = 0.0;
                    if (origin.getCellBehaviour().getTripInfoByDestinationMap().get(destination).getRouteFractionToNeighbours()
                            .containsKey(neighbour))
                    {
                        share = origin.getCellBehaviour().getTripInfoByDestinationMap().get(destination)
                                .getRouteFractionToNeighbours().get(neighbour);
                        if (share > 0.0)
                        {
                            System.out.println("Share: " + share);
                        }
                    }
                    origin.getCellBehaviour().getTripInfoByDestinationMap().get(destination).getRouteFractionToNeighbours()
                            .put(neighbour, share + weightNew * addShare);
                    // create a field to store accumulated cars to a certain neighbour on its path to a destination
                    if (!origin.getCellBehaviour().getTripInfoByDestinationMap().get(destination)
                            .getAccumulatedCarsToNeighbour().containsKey(neighbour))
                    {
                        origin.getCellBehaviour().getTripInfoByDestinationMap().get(destination).getAccumulatedCarsToNeighbour()
                                .put(neighbour, 0.0);
                        origin.getCellBehaviour().getTripInfoByDestinationMap().get(destination).getDemandToNeighbour()
                                .put(neighbour, 0.0);
                    }

                    if (startNode == null || neighbour == null || destination == null)
                    {
                        System.out.println("Floyd");
                    }

                    WriteOutput.writeOutputRoutesNTM(model, steps, i, origin, neighbour, destination, MAXSTEPS,
                            dataRoutesNTMOut, share, weightNew * addShare, path.getWeight());

                    // only for initialisation of routes over the flow Links;
                    if (path.getEdgeList().get(0).getLink().getBehaviourType() == TrafficBehaviourType.FLOW)
                    {
                        // for the flow links we create the trip info by Node also for the flow cells
                        LinkCellTransmission ctmLink =
                                (LinkCellTransmission) model.getAreaGraph().getEdge(origin, neighbour).getLink();
                        // Loop through the cells and do transmission
                        for (FlowCell cell : ctmLink.getCells())
                        {
                            // only at first step: initiate variables
                            if (initiateSimulation)
                            {
                                LinkedHashMap<BoundedNode, java.lang.Double> neighbours =
                                        new LinkedHashMap<BoundedNode, java.lang.Double>();
                                LinkedHashMap<BoundedNode, java.lang.Double> accumulatedCarsToNeighbour =
                                        new LinkedHashMap<BoundedNode, java.lang.Double>();
                                LinkedHashMap<BoundedNode, java.lang.Double> demandToNeighbour =
                                        new LinkedHashMap<BoundedNode, java.lang.Double>();
                                TripInfoByDestination tripInfoByNode = new TripInfoByDestination(neighbours,
                                        accumulatedCarsToNeighbour, demandToNeighbour, destination);
                                cell.getCellBehaviourFlow().getTripInfoByDestinationMap().put(destination, tripInfoByNode);
                            }
                            else
                            {
                                if (cell.getCellBehaviourFlow().getTripInfoByDestinationMap().get(destination) == null)
                                {
                                    LinkedHashMap<BoundedNode, java.lang.Double> neighbours =
                                            new LinkedHashMap<BoundedNode, java.lang.Double>();
                                    LinkedHashMap<BoundedNode, java.lang.Double> accumulatedCarsToNeighbour =
                                            new LinkedHashMap<BoundedNode, java.lang.Double>();
                                    LinkedHashMap<BoundedNode, java.lang.Double> demandToNeighbour =
                                            new LinkedHashMap<BoundedNode, java.lang.Double>();
                                    TripInfoByDestination tripInfoByNode = new TripInfoByDestination(neighbours,
                                            accumulatedCarsToNeighbour, demandToNeighbour, destination);
                                    cell.getCellBehaviourFlow().getTripInfoByDestinationMap().put(destination, tripInfoByNode);
                                }
                            }
                            cell.getCellBehaviourFlow().getTripInfoByDestinationMap().get(destination)
                                    .getRouteFractionToNeighbours().put(neighbour, 1.0);
                        }
                    }

                }
            }
        }
        dataRoutesNTMOut.close();
    }

    /**
     * @param fRandom
     * @param VARIANCE double;
     * @return
     */
    public static double Gaussian(Random random, double VARIANCE)
    {
        double MEAN = 100.0f;
        double number = MEAN + random.nextGaussian() * VARIANCE;
        return number / 100;
    }

}
