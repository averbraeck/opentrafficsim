package org.opentrafficsim.demo.ntm;

import org.jgrapht.graph.SimpleWeightedGraph;
import org.opentrafficsim.core.network.LinkEdge;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.demo.ntm.Node.TrafficBehaviourType;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 30 Nov 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class CTMsimulation
{

    /**
     * @param model
     */
    public static void simulateCellTransmission(NTMModel model)
    {

        for (BoundedNode startNode : model.getAreaGraph().vertexSet())
        {
            try
            {
                if (startNode.getBehaviourType() == TrafficBehaviourType.FLOW)
                {
                    // what are the neighbours
                    for (TripInfoByDestination tripInfoByDestination : startNode.getCellBehaviour()
                            .getTripInfoByNodeMap().values())
                    {
                        if (tripInfoByDestination.getNeighbour().getId() == null
                                || tripInfoByDestination.getNeighbour() == null)
                        {
                            System.out.println("NTMSimulation line 44: Strange and to repair: nodeTo equals null");
                        }
                        else
                        {
                            // retrieve the neighbour area on the path to a certain destination
                            BoundedNode neighbour = (BoundedNode) tripInfoByDestination.getNeighbour();
                            if (neighbour != null)
                            {
                                // only the flow links are considered in the CTM model, so we need a Flow node at the
                                // end of the link
                                if (neighbour.getBehaviourType() == TrafficBehaviourType.FLOW)
                                {
                                    // Do CTM
                                    // In case of Cell Transmission Links, there is an intermediate process of traffic
                                    // moving over a link. The demand to this link is computed.

                                    // In Step 3 see if this demand is below the capacity of the link (supply)
                                    try
                                    {
                                        // Retrieve the cell transmission link
                                        // all cells (should) have identical characteristics
                                        LinkCellTransmission ctmLink =
                                                (LinkCellTransmission) model.getAreaGraph()
                                                        .getEdge(startNode, neighbour).getLink();
                                        // Loop through the cells and do transmission
                                        for (FlowCell cell : ctmLink.getCells())
                                        {
                                            double demandToEnterByDestination;
                                            double demandCell = cell.getCellBehaviour().getDemand();
                                            double accumulationCell = cell.getCellBehaviour().getAccumulatedCars();
                                            // retrieve the current info of trips in this cell
                                            TripInfoByDestination cellInfoByDestination =
                                                    cell.getCellBehaviour().getTripInfoByNodeMap()
                                                            .get(tripInfoByDestination.getDestination());

                                            // at the first cell, demand comes from the FlowNode
                                            if (cell == ctmLink.getCells().get(0))
                                            {
                                                demandToEnterByDestination =
                                                        tripInfoByDestination.getAccumulatedCarsToDestination();
                                                double supplyCell = cell.getCellBehaviour().getSupply();
                                                demandToEnterByDestination =
                                                        Math.min(supplyCell / demandCell, 1.0)
                                                                * demandToEnterByDestination;
                                                cell.getCellBehaviour().addAccumulatedCars(demandToEnterByDestination);
                                                tripInfoByDestination
                                                        .addAccumulatedCarsToDestination(-demandToEnterByDestination);
                                            }

                                            // determine the downstream cell:
                                            if (ctmLink.getCells().indexOf(cell) < ctmLink.getCells().size() - 1)
                                            {
                                                demandToEnterByDestination =
                                                        demandCell
                                                                * cellInfoByDestination
                                                                        .getAccumulatedCarsToDestination()
                                                                / accumulationCell;
                                                FlowCell downStreamCell =
                                                        ctmLink.getCells().get(ctmLink.getCells().indexOf(cell) + 1);
                                                double supplyCell = downStreamCell.getCellBehaviour().getSupply();
                                                demandToEnterByDestination =
                                                        Math.min(supplyCell / demandCell, 1.0)
                                                                * demandToEnterByDestination;
                                                TripInfoByDestination downStreamCellInfoByDestination =
                                                        downStreamCell.getCellBehaviour().getTripInfoByNodeMap()
                                                                .get(tripInfoByDestination.getDestination());
                                                downStreamCell.getCellBehaviour().addAccumulatedCars(
                                                        demandToEnterByDestination);
                                                downStreamCellInfoByDestination
                                                        .addAccumulatedCarsToDestination(demandToEnterByDestination);
                                                cellInfoByDestination
                                                        .addAccumulatedCarsToDestination(-demandToEnterByDestination);

                                            }
                                            else
                                            // last cell
                                            {
                                                demandToEnterByDestination =
                                                        demandCell
                                                                * cellInfoByDestination
                                                                        .getAccumulatedCarsToDestination()
                                                                / accumulationCell;
                                                TripInfoByDestination neighbourInfoByDestination =
                                                        neighbour.getCellBehaviour().getTripInfoByNodeMap()
                                                                .get(tripInfoByDestination.getDestination());
                                                // if the next neighbour is an NTM node: traffic is directly transferred
                                                // to that node
/*                                                BoundedNode nextNeighbour =
                                                        (BoundedNode) neighbourInfoByDestination.getNeighbour();
                                                if (nextNeighbour.getBehaviourType() == TrafficBehaviourType.NTM)
                                                {
                                                    TripInfoByDestination nextNeighbourInfoByDestination =
                                                            nextNeighbour.getCellBehaviour().getTripInfoByNodeMap()
                                                                    .get(tripInfoByDestination.getDestination());
                                                    nextNeighbour.getCellBehaviour().addAccumulatedCars(
                                                            demandToEnterByDestination);
                                                    nextNeighbourInfoByDestination
                                                            .addAccumulatedCarsToDestination(demandToEnterByDestination);
                                                }
                                                else
                                                {*/
                                                    neighbour.getCellBehaviour().addAccumulatedCars(
                                                            demandToEnterByDestination);
                                                    neighbourInfoByDestination
                                                            .addAccumulatedCarsToDestination(demandToEnterByDestination);
                                                //}
                                                cellInfoByDestination
                                                        .addAccumulatedCarsToDestination(-demandToEnterByDestination);
                                                cell.getCellBehaviour().addAccumulatedCars(-demandToEnterByDestination);

                                            }

                                            DoubleScalar.Abs<FrequencyUnit> capacity = ctmLink.getCapacity();
                                            // if the incoming capacity is limiting the flow....

                                            // .setFlowToNeighbour(flowToNeighbour);
                                            // * TimeUnit.HOUR
                                            // .getConversionFactorToStandardUnit() / model
                                            // .getSettingsNTM()
                                            // .getTimeStepDurationCellTransmissionModel()
                                            // .getSI())
                                        }

                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            else
                            {
                                System.out.println("CTMsimulation line 114: no route...");
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
