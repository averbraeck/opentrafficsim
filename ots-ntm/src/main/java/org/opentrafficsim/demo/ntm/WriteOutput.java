package org.opentrafficsim.demo.ntm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.demo.ntm.Node.TrafficBehaviourType;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 28 Jan 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class WriteOutput
{

    // /CELLS
    /** */
    static BufferedWriter dataLaneLengthOut = null;

    /** */
    static BufferedWriter dataLanesPerCellOut = null;

    /** */
    static BufferedWriter dataAccumulationCellOut = null;

    /** */
    static BufferedWriter dataParametersFDOut = null;

    /** */
    static Double[][] accumulationCells = new Double[999][999];

    /** */
    static Double[][] parametersFD = new Double[999][3];

    /** */
    static Double[] laneLength = new Double[999];

    /** */
    static Double[] lanesPerCell = new Double[999];

    /** */
    static String[] cellID = new String[999];

    /** */
    static int numberOfCells = 0;

    /** */
    static HashMap<Link, Integer> linkIndex = new HashMap<Link, Integer>();

    /** */
    static HashMap<Integer, Link> indexLink = new HashMap<Integer, Link>();

    /** */
    static HashMap<Node, Integer> nodeIndex = new HashMap<>();

    /** */
    static HashMap<Integer, Node> indexNode = new HashMap<>();

    // Writing output data CELLS
    /**
     * @param model
     * @param steps
     * @param MAXSTEPS
     */
    public static void writeOutputDataFlowLinks(NTMModel model, int steps, int MAXSTEPS)
    {
        // for testing we open a file and write some results:
        // TODO testing

        if (steps == 1)
        {
            File fileLaneLength = new File(model.getSettingsNTM().getPath() + "/output/NTMoutputCellLength.txt");
            dataLaneLengthOut = createWriter(fileLaneLength);
            File fileAccumulation = new File(model.getSettingsNTM().getPath() + "/output/NTMoutputAccumulation.txt");
            dataAccumulationCellOut = createWriter(fileAccumulation);
            File fileLanesPerCell = new File(model.getSettingsNTM().getPath() + "/output/NTMoutputLanesPerCell.txt");
            dataLanesPerCellOut = createWriter(fileLanesPerCell);
            File fileParametersFD = new File(model.getSettingsNTM().getPath() + "/output/NTMoutputParametersFD.txt");
            dataParametersFDOut = createWriter(fileParametersFD);
        }

        if (steps < MAXSTEPS)
        {
            int i = 0;
            int linkNumber = 0;

            for (Link link : model.getDebugLinkList().values())
            {
                if (link.getBehaviourType() == TrafficBehaviourType.FLOW)
                {
                    LinkCellTransmission ctmLink = (LinkCellTransmission) link;
                    for (FlowCell cell : ctmLink.getCells())
                    {
                        if (steps == 1)
                        {
                            laneLength[i] = cell.getCellLength().getSI();
                            lanesPerCell[i] = (double) cell.getNumberOfLanes();
                            cellID[i] = String.valueOf(linkNumber);
                            parametersFD[i][0] =
                                    cell.getCellBehaviourFlow().getParametersFundamentalDiagram().getCapacityPerUnit()
                                            .doubleValue() * 3600;
                            parametersFD[i][1] =
                                    cell.getCellBehaviourFlow().getParametersFundamentalDiagram().getAccCritical()
                                            .get(0);
                            parametersFD[i][2] =
                                    cell.getCellBehaviourFlow().getParametersFundamentalDiagram().getAccCritical()
                                            .get(1);
                        }
                        accumulationCells[i][steps - 1] = cell.getCellBehaviourFlow().getAccumulatedCars();
                        i++;
                    }
                    linkNumber++;
                }

            }
            numberOfCells = i;
        }

        if (steps == MAXSTEPS - 1)
        {
            try
            {
                String textOut;
                // Link transmission

                for (int i = 0; i < numberOfCells; i++)
                {
                    textOut = String.format("%.1f", laneLength[i]);
                    dataLaneLengthOut.write(textOut + " \n");

                    textOut = String.format("%.1f", lanesPerCell[i]);
                    dataLanesPerCellOut.write(textOut + " \n");

                    textOut = String.format("%.1f", parametersFD[i][0]);
                    dataParametersFDOut.write(textOut + ", ");
                    textOut = String.format("%.1f", parametersFD[i][1]);
                    dataParametersFDOut.write(textOut + ", ");
                    textOut = String.format("%.1f", parametersFD[i][2]);
                    dataParametersFDOut.write(textOut + " \n");

                    dataAccumulationCellOut.write("Cell " + i + ", ");
                    for (int j = 0; j < steps; j++)
                    {
                        textOut = String.format("%.5f", accumulationCells[i][j]);
                        dataAccumulationCellOut.write(textOut + ", ");
                    }
                    dataAccumulationCellOut.write(" \n");

                }
                dataLaneLengthOut.close();
                dataLanesPerCellOut.close();
                dataLaneLengthOut.close();
                dataParametersFDOut.close();
                dataAccumulationCellOut.close();
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }
        }
    }

    // AREAS
    /** */
    static BufferedWriter dataParametersNFDOut = null;

    /** */
    static BufferedWriter dataAccumulationNTMOut = null;

    /** */
    static BufferedWriter dataCongestedSpeedNTMOut = null;

    /** */
    static BufferedWriter dataDemandNTMOut = null;

    /** */
    static BufferedWriter dataSupplyNTMOut = null;

    /** */
    static BufferedWriter dataDeparturesNTMOut = null;

    /** */
    static BufferedWriter dataArrivalsNTMOut = null;

    /** */
    static BufferedWriter dataFluxToNeighbourNTMOut = null;

    /** */
    static BufferedWriter dataODDeparturesNTMOut = null;

    /** */
    static BufferedWriter dataODArrivalsNTMOut = null;

    /** */
    static Double[][] accumulationNTM = new Double[999][999];

    /** */
    static Double[][] congestedSpeedNTM = new Double[999][999];

    /** */
    static Double[][] demandNTM = new Double[999][999];

    /** */
    static Double[][] supplyNTM = new Double[999][999];

    /** */
    static Double[][] departuresNTM = new Double[999][999];

    /** */
    static Double[][] arrivalsNTM = new Double[999][999];

    /** */
    static Double[][][] fluxToNeighbourNTM = new Double[9][9][999];

    /** */
    static HashMap<Node, HashMap<Node, Double[]>> fluxToNeighbourNTMMap = new HashMap<>();

    /** */
    static HashMap<Node, HashMap<Node, Double[]>> ODArrivalsNTMMap = new HashMap<>();

    /** */
    static HashMap<Node, HashMap<Node, Double[]>> ODDeparturesNTMMap = new HashMap<>();

    /** */
    static Double[][] parametersNFD = new Double[999][6];

    /** */
    static Double[][] capSpeedRoadLengthNTM = new Double[999][3];

    // Writing output data
    /**
     * @param model
     * @param steps
     * @param MAXSTEPS
     * @param arrivalsPreviousStep
     * @param departuresPreviousStep
     */
    public static void writeOutputDataNTM(NTMModel model, int steps, int MAXSTEPS)
    {
        // for testing we open a file and write some results:
        // TODO testing

        String fileName = "/NTMfluxToNeighbour";
        String description = "fluxes";
        String DATATYPE = "fluxes";
        if (steps == 1)
        {
            File file = new File(model.getSettingsNTM().getPath() + model.getOutput() + fileName + ".txt");
            dataFluxToNeighbourNTMOut = createWriter(file);
        }
        writeHashMap(model, steps, MAXSTEPS, description, dataFluxToNeighbourNTMOut, fluxToNeighbourNTMMap, indexNode,
                DATATYPE);

        fileName = "/NTMdeparturesByOD";
        description = "departures by OD";
        DATATYPE = "departuresOD";
        if (steps == 1)
        {
            File file = new File(model.getSettingsNTM().getPath() + model.getOutput() + fileName + ".txt");
            dataODDeparturesNTMOut = createWriter(file);
        }
        writeHashMap(model, steps, MAXSTEPS, description, dataODDeparturesNTMOut, ODDeparturesNTMMap, indexNode,
                DATATYPE);

        fileName = "/NTMarrivalsByOD";
        description = "arrived by OD";
        DATATYPE = "arrivalsOD";
        if (steps == 1)
        {
            File file = new File(model.getSettingsNTM().getPath() + model.getOutput() + fileName + ".txt");
            dataODArrivalsNTMOut = createWriter(file);
        }
        writeHashMap(model, steps, MAXSTEPS, description, dataODArrivalsNTMOut, ODArrivalsNTMMap, indexNode, DATATYPE);

        fileName = "/NTMarrivals";
        description = "Arrived trips";
        DATATYPE = "arrivals";
        if (steps == 1)
        {
            File file = new File(model.getSettingsNTM().getPath() + model.getOutput() + fileName + ".txt");
            dataArrivalsNTMOut = createWriter(file);
        }
        writeArray(model, steps, MAXSTEPS, description, dataArrivalsNTMOut, arrivalsNTM, DATATYPE);

        fileName = "/NTMdepartures";
        description = "departed trips";
        DATATYPE = "departures";
        if (steps == 1)
        {
            File file = new File(model.getSettingsNTM().getPath() + model.getOutput() + fileName + ".txt");
            dataDeparturesNTMOut = createWriter(file);
        }
        writeArray(model, steps, MAXSTEPS, description, dataDeparturesNTMOut, departuresNTM, DATATYPE);

        fileName = "/NTMaccumulation";
        description = "Accumulation trips";
        DATATYPE = "accumulation";
        if (steps == 1)
        {
            File file = new File(model.getSettingsNTM().getPath() + model.getOutput() + fileName + ".txt");
            dataAccumulationNTMOut = createWriter(file);
        }
        writeArray(model, steps, MAXSTEPS, description, dataAccumulationNTMOut, accumulationNTM, DATATYPE);

        fileName = "/NTMspeed";
        description = "Congested speed";
        DATATYPE = "congestedSpeed";
        if (steps == 1)
        {
            File file = new File(model.getSettingsNTM().getPath() + model.getOutput() + fileName + ".txt");
            dataCongestedSpeedNTMOut = createWriter(file);
        }
        writeArray(model, steps, MAXSTEPS, description, dataCongestedSpeedNTMOut, congestedSpeedNTM, DATATYPE);

        fileName = "/NTMdemand";
        description = "Demand trips";
        DATATYPE = "demand";
        if (steps == 1)
        {
            File file = new File(model.getSettingsNTM().getPath() + model.getOutput() + fileName + ".txt");
            dataDemandNTMOut = createWriter(file);
        }
        writeArray(model, steps, MAXSTEPS, description, dataDemandNTMOut, demandNTM, DATATYPE);

        fileName = "/NTMsupply";
        description = "Supply trips";
        DATATYPE = "supply";
        if (steps == 1)
        {
            File file = new File(model.getSettingsNTM().getPath() + model.getOutput() + fileName + ".txt");
            dataSupplyNTMOut = createWriter(file);
        }
        writeArray(model, steps, MAXSTEPS, description, dataSupplyNTMOut, supplyNTM, DATATYPE);

        fileName = "/NTMparametersNFD";
        description = "Parameters NFD";
        DATATYPE = "parametersNFD";
        if (steps == 1)
        {
            File file = new File(model.getSettingsNTM().getPath() + model.getOutput() + fileName + ".txt");
            dataParametersNFDOut = createWriter(file);
        }
        writeArray(model, steps, MAXSTEPS, description, dataParametersNFDOut, parametersNFD, DATATYPE);
    }

    /**
     * @param model
     * @param steps
     * @param MAXSTEPS
     * @param data
     * @param nodeDoublemap
     * @param intNodeMap
     */
    static void writeHashMap(NTMModel model, int steps, int MAXSTEPS, String description, BufferedWriter data,
            HashMap<Node, HashMap<Node, Double[]>> nodeNodeDoublemap, HashMap<Integer, Node> intNodeMap, String DATATYPE)
    {

        if (steps < MAXSTEPS)
        {
            int i = 0;
            TreeSet<Node> graphVertices = new TreeSet<Node>(model.getAreaGraph().vertexSet());

            for (Node nodeIn : graphVertices)
            {
                BoundedNode node = (BoundedNode) nodeIn;
                CellBehaviourNTM cellBehaviour = (CellBehaviourNTM) node.getCellBehaviour();

                for (TripInfoByDestination tripInfoByDestination : cellBehaviour.getTripInfoByNodeMap().values())
                {
                    Set<BoundedNode> neighbours = tripInfoByDestination.getNeighbourAndRouteShare().keySet();
                    for (BoundedNode neighbour : neighbours)
                    {
                        double trips = 0;
                        if (DATATYPE == "fluxes")
                        {
                            trips =
                                    tripInfoByDestination.getNeighbourAndRouteShare().get(neighbour)
                                            * tripInfoByDestination.getFluxToNeighbour();
                        }
                        else if (DATATYPE == "arrivalsOD")
                        {
                            trips = tripInfoByDestination.getArrivedTrips();
                        }
                        else if (DATATYPE == "departuresOD")
                        {
                            trips = tripInfoByDestination.getDepartedTrips();

                        }

                        if (trips > 0.0)
                        {
                            if (nodeNodeDoublemap.get(node) == null)
                            {
                                HashMap<Node, Double[]> fluxMap = new HashMap<Node, Double[]>();
                                Double[] fluxes = new Double[999];
                                fluxes[steps - 1] = trips;
                                fluxMap.put(neighbour, fluxes);
                                nodeNodeDoublemap.put(node, fluxMap);
                            }
                            else
                            {
                                if (nodeNodeDoublemap.get(node).get(neighbour) == null)
                                {
                                    HashMap<Node, Double[]> fluxMap = new HashMap<Node, Double[]>();
                                    Double[] fluxes = new Double[999];
                                    fluxes[steps - 1] = trips;
                                    fluxMap.put(neighbour, fluxes);
                                    nodeNodeDoublemap.put(node, fluxMap);
                                }
                                else
                                {
                                    Double[] fluxes = nodeNodeDoublemap.get(node).get(neighbour);
                                    fluxes[steps - 1] = trips;
                                }
                            }
                        }
                    }
                }
                i++;
                numberOfCells = i;
            }
        }
        String textOut;

        // Write data to file
        if (steps == MAXSTEPS - 1)
        {
            try
            {
                for (int i = 0; i < numberOfCells; i++)
                {
                    HashMap<Node, Double[]> fluxMap = nodeNodeDoublemap.get(intNodeMap.get(i));
                    if (fluxMap != null)
                    {
                        for (int j = 0; j < numberOfCells; j++)
                        {
                            Double[] trips = fluxMap.get(intNodeMap.get(j));
                            if (trips != null)
                            {
                                data.write(description + " " + intNodeMap.get(i).getId() + intNodeMap.get(j).getId()
                                        + ", ");
                                for (int k = 0; k < steps; k++)
                                {
                                    if (trips[k] == null)
                                    {
                                        trips[k] = 0.0;
                                    }
                                    textOut = String.format("%.5f", trips[k]);
                                    data.write(textOut + ", ");
                                }
                                data.write(" \n");
                            }
                        }
                    }
                }
                data.close();
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }
        }
    }

    /**
     * @param model
     * @param steps
     * @param MAXSTEPS
     * @param description
     * @param data
     * @param dataArray
     * @param DATATYPE
     */
    static void writeArray(NTMModel model, int steps, int MAXSTEPS, String description, BufferedWriter data,
            Double[][] dataArray, String DATATYPE)
    {

        if (steps < MAXSTEPS)
        {
            int i = 0;
            TreeSet<Node> graphVertices = new TreeSet<Node>(model.getAreaGraph().vertexSet());

            for (Node nodeIn : graphVertices)
            {
                BoundedNode node = (BoundedNode) nodeIn;
                CellBehaviourNTM cellBehaviour = (CellBehaviourNTM) node.getCellBehaviour();
                if (steps == 1)
                {
                    nodeIndex.put(node, i);
                    indexNode.put(i, node);
                }
                if (DATATYPE == "arrivals")
                {
                    dataArray[nodeIndex.get(node)][steps - 1] = cellBehaviour.getArrivals();
                }
                else if (DATATYPE == "departures")
                {
                    dataArray[nodeIndex.get(node)][steps - 1] = cellBehaviour.getDepartures();
                }
                else if (DATATYPE == "accumulation")
                {
                    dataArray[nodeIndex.get(node)][steps - 1] = cellBehaviour.getAccumulatedCars();
                }
                else if (DATATYPE == "congestedSpeed")
                {
                    if (steps == 1)
                    {
                        // freeSpeed??
                    }
                    dataArray[nodeIndex.get(node)][steps - 1] =
                            cellBehaviour.getCurrentSpeed().getInUnit(SpeedUnit.KM_PER_HOUR);
                }
                else if (DATATYPE == "demand")
                {
                    dataArray[nodeIndex.get(node)][steps - 1] = cellBehaviour.getDemand();
                }
                else if (DATATYPE == "supply")
                {
                    dataArray[nodeIndex.get(node)][steps - 1] = cellBehaviour.getSupply();
                }
                else if (DATATYPE == "parametersNFD")
                {
                    if (steps == 1)
                    {
                        dataArray[i][0] =
                                cellBehaviour.getParametersNTM().getCapacityPerUnit().getInUnit(FrequencyUnit.PER_HOUR);
                        dataArray[i][1] =
                                cellBehaviour.getParametersNTM().getRoadLength().getInUnit(LengthUnit.KILOMETER);
                        dataArray[i][2] =
                                cellBehaviour.getParametersNTM().getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR);
                        dataArray[i][3] = cellBehaviour.getParametersNTM().getAccCritical().get(0);
                        dataArray[i][4] = cellBehaviour.getParametersNTM().getAccCritical().get(1);
                        dataArray[i][5] = cellBehaviour.getParametersNTM().getAccCritical().get(2);
                    }
                }
                i++;
            }
            numberOfCells = i;
        }

        // Write data to file
        if (DATATYPE != "parametersNFD")
        {
            if (steps == MAXSTEPS - 1)
            {
                try
                {
                    String textOut;
                    // Link transmission
                    data.write("Step:  " + ", ");
                    for (int j = 0; j < steps; j++)
                    {
                        data.write(j + ", ");
                    }
                    data.write(" \n");

                    for (int i = 0; i < numberOfCells; i++)
                    {
                        data.write(description + " " + indexNode.get(i).getId() + ", ");
                        for (int j = 0; j < steps; j++)
                        {
                            textOut = String.format("%.5f", dataArray[i][j]);
                            data.write(textOut + ", ");
                        }
                        data.write(" \n");
                    }
                    data.close();
                }
                catch (IOException exception)
                {
                    exception.printStackTrace();
                }
            }
        }
        else if (DATATYPE == "parametersNFD")
        {
            if (steps == 1)
            {
                try
                {
                    String textOut;
                    // Link transmission
                    data.write("Indicators:     " + ", ");
                    data.write("Capacity (/h)   " + ", ");
                    data.write("RoadLength (km) " + ", ");
                    data.write("FreeSpeed (km/h)" + ", ");
                    data.write("AccCrit maxCap  " + ", ");
                    data.write("AccCrit critical" + ", ");
                    data.write("AccCrit gridlock" + ", ");
                    data.write(" \n");

                    for (int i = 0; i < numberOfCells; i++)
                    {
                        data.write(description + " " + indexNode.get(i).getId() + ", ");
                        for (int j = 0; j < 6; j++)
                        {
                            textOut = String.format("%.5f", dataArray[i][j]);
                            data.write(textOut + ", ");
                        }
                        data.write(" \n");
                    }
                    data.close();
                }
                catch (IOException exception)
                {
                    exception.printStackTrace();
                }
            }
        }

    }

    static BufferedWriter createWriter(File file)
    {
        // if file doesnt exists, then create it
        BufferedWriter bWriter = null;
        if (!file.exists())
        {
            try
            {
                file.createNewFile();
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }
        }

        try
        {
            bWriter = new BufferedWriter(new FileWriter(file));
        }
        catch (IOException exception1)
        {
            exception1.printStackTrace();
        }
        return bWriter;
    }

}
