package org.opentrafficsim.demo.ntm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.linearref.LengthIndexedLine;
import org.opentrafficsim.core.network.LinkEdge;
import org.opentrafficsim.demo.ntm.NTMNode.TrafficBehaviourType;
import org.opentrafficsim.demo.ntm.animation.TimeSeriesChart;
import org.opentrafficsim.demo.ntm.trafficdemand.TripInfo;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 28 Jan 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class WriteOutput
{

    // INPUTDATA
    /** */
    static final int MAXSTEPS = 1080;

    /** */
    static final int MAXZONES = 500;

    // /CELLS
    /** */
    static BufferedWriter dataLaneLengthOut = null;

    /** */
    static BufferedWriter dataLanesPerCellOut = null;

    /** */
    static BufferedWriter dataAccumulationCellsOut = null;

    /** */
    static BufferedWriter dataSpeedCellsOut = null;

    /** */
    static BufferedWriter dataParametersFDOut = null;

    /** */
    static Double[][] accumulationCells = new Double[MAXZONES][MAXSTEPS];

    /** */
    static Double[][] speedCells = new Double[MAXZONES][MAXSTEPS];

    /** */
    static Double[][] parametersFD = new Double[MAXZONES][3];

    /** */
    static Double[][] laneData = new Double[MAXZONES][MAXSTEPS];

    /** */
    static String[] cellID = new String[MAXZONES];

    /** */
    static int numberOfCells = 0;

    /** */
    static LinkedHashMap<NTMLink, Integer> linkIndex = new LinkedHashMap<NTMLink, Integer>();

    /** */
    static LinkedHashMap<Integer, NTMLink> indexLink = new LinkedHashMap<Integer, NTMLink>();

    /** */
    static LinkedHashMap<Integer, NTMNode> indexStartNode = new LinkedHashMap<Integer, NTMNode>();

    /** */
    static LinkedHashMap<Integer, NTMNode> indexEndNode = new LinkedHashMap<Integer, NTMNode>();

    /** */
    static LinkedHashMap<NTMNode, Integer> nodeIndex = new LinkedHashMap<>();

    /** */
    static LinkedHashMap<Integer, NTMNode> indexNode = new LinkedHashMap<>();

    public static void writeInputData(NTMModel model) throws IOException
    {
        createDir(model.getInputNTM().getInputMap() + model.getInputNTM().getOutputMap());
        BufferedWriter data = null;
        boolean big = false;
        if (big == true)
        {
            String fileName = "/ALLTripsBigArea";
            String description = "TripsBigArea";
            File fileTripsBigArea =
                    new File(model.getInputNTM().getInputMap() + model.getInputNTM().getOutputMap() + fileName + ".txt");
            data = createWriter(fileTripsBigArea);

            data.write("Matrix Big Areas " + ", ");
            for (NTMNode from : model.getBigCentroids().values())
            {
                data.write(from.getId() + ", ");
            }
            data.write(" \n");

            for (NTMNode from : model.getBigCentroids().values())
            {
                data.write(from.getId() + ", ");
                for (NTMNode to : model.getBigCentroids().values())
                {
                    if (model.tripDemandToUse.getTripDemandOriginToDestination(from.getId(), to.getId()) != null)
                    {
                        data.write(model.tripDemandToUse.getTripDemandOriginToDestination(from.getId(), to.getId())
                                .getNumberOfTrips() + ", ");
                    }
                    else
                    {
                        data.write("NaN " + ", ");
                    }
                }
                data.write(" \n");
            }
            data.close();
        }

        String fileName = "/ALLTrips";
        String description = "Trips";
        File fileTrips = new File(model.getInputNTM().getInputMap() + model.getInputNTM().getOutputMap() + fileName + ".txt");
        data = createWriter(fileTrips);
        data.write("Matrix " + ", ");
        for (NTMNode from : model.getCentroids().values())
        {
            data.write(from.getId() + ", ");
        }
        data.write(" \n");

        for (NTMNode from : model.getCentroids().values())
        {
            data.write(from.getId() + ", ");
            for (NTMNode to : model.getCentroids().values())
            {
                if (model.getTripDemand().getTripDemandOriginToDestination(from.getId(), to.getId()) != null)
                {
                    TripInfo ti = (TripInfo) model.getTripDemand().getTripDemandOriginToDestination(from.getId(), to.getId());
                    data.write(ti.getNumberOfTrips() + ", ");
                }
                else
                {
                    data.write("NaN " + ", ");
                }
            }
            data.write(" \n");
        }
        data.close();

    }

    // Writing output data CELLS
    /**
     * @param model NTMModel;
     * @param steps int;
     * @param MAXSTEPS int;
     */
    public static void writeOutputDataFlowLinks(NTMModel model, int steps, int MAXSTEPS)
    {
        // for testing we open a file and write some results:
        // TODO testing
        String fileName = "/CTMCelldata";
        String description = "CellData";
        String DATATYPE = "cellData";
        createDir(model.getInputNTM().getInputMap() + model.getInputNTM().getOutputMap());

        if (steps == 1)
        {
            File fileLaneLength =
                    new File(model.getInputNTM().getInputMap() + model.getInputNTM().getOutputMap() + fileName + ".txt");
            dataLaneLengthOut = createWriter(fileLaneLength);
        }
        writeCellInfo(model, steps, MAXSTEPS, description, dataLaneLengthOut, laneData, DATATYPE);

        fileName = "/CTMParametersFD";
        description = "ParametersFD";
        DATATYPE = "parametersFD";
        if (steps == 1)
        {
            File fileLaneLength =
                    new File(model.getInputNTM().getInputMap() + model.getInputNTM().getOutputMap() + fileName + ".txt");
            dataParametersFDOut = createWriter(fileLaneLength);
        }
        writeCellInfo(model, steps, MAXSTEPS, description, dataParametersFDOut, parametersFD, DATATYPE);

        fileName = "/CTMAccumulationCells";
        description = "AccumulationCells";
        DATATYPE = "accumulationCells";
        if (steps == 1)
        {
            File fileLaneLength =
                    new File(model.getInputNTM().getInputMap() + model.getInputNTM().getOutputMap() + fileName + ".txt");
            dataAccumulationCellsOut = createWriter(fileLaneLength);
        }
        writeCellInfo(model, steps, MAXSTEPS, description, dataAccumulationCellsOut, accumulationCells, DATATYPE);

        fileName = "/CTMSpeedCells";
        description = "SpeedCells";
        DATATYPE = "speedCells";
        if (steps == 1)
        {
            File fileLaneLength =
                    new File(model.getInputNTM().getInputMap() + model.getInputNTM().getOutputMap() + fileName + ".txt");
            dataSpeedCellsOut = createWriter(fileLaneLength);
        }
        writeCellInfo(model, steps, MAXSTEPS, description, dataSpeedCellsOut, speedCells, DATATYPE);
    }

    /**
     * @param model NTMModel;
     * @param steps int;
     * @param MAXSTEPS int;
     */
    public static void writeCellInfo(NTMModel model, int steps, int MAXSTEPS, String description, BufferedWriter data,
            Double[][] dataArray, String DATATYPE)
    {
        if (steps < MAXSTEPS)
        {
            int i = 0;
            int linkNumber = 0;

            for (LinkEdge le : model.getAreaGraph().edgeSet())
            {
                NTMLink link = (NTMLink) le.getLink();
                if (link.getBehaviourType() == TrafficBehaviourType.FLOW)
                {
                    LinkCellTransmission ctmLink = (LinkCellTransmission) link;
                    for (FlowCell cell : ctmLink.getCells())
                    {
                        if (steps == 1)
                        {
                            linkIndex.put(link, i);
                            indexLink.put(i, link);
                            indexStartNode.put(i, (NTMNode) link.getStartNode());
                            indexEndNode.put(i, (NTMNode) link.getEndNode());
                            cellID[i] = String.valueOf(linkNumber);

                            if (DATATYPE == "cellData")
                            {
                                ArrayList<Coordinate> cellPoints = new ArrayList<Coordinate>();
                                cellPoints = retrieveCellXY(ctmLink, cell, ctmLink.getCells().indexOf(cell),
                                        ctmLink.getCells().size());
                                Coordinate cellPoint = new Coordinate();
                                cellPoint = cellPoints.get(0);
                                dataArray[i][0] = cellPoint.x;
                                dataArray[i][1] = cellPoint.y;
                                cellPoint = cellPoints.get(1);
                                dataArray[i][2] = cellPoint.x;
                                dataArray[i][3] = cellPoint.y;
                                cellPoint = cellPoints.get(2);
                                dataArray[i][4] = cellPoint.x;
                                dataArray[i][5] = cellPoint.y;

                                dataArray[i][6] = cell.getCellLength().getSI();
                                dataArray[i][7] = (double) cell.getNumberOfLanes();
                            }

                            if (DATATYPE == "parametersFD")
                            {
                                dataArray[i][0] = cell.getCellBehaviourFlow().getParametersFundamentalDiagram().getCapacity()
                                        .doubleValue() * 3600;
                                dataArray[i][1] =
                                        cell.getCellBehaviourFlow().getParametersFundamentalDiagram().getAccCritical().get(0);
                                dataArray[i][2] =
                                        cell.getCellBehaviourFlow().getParametersFundamentalDiagram().getAccCritical().get(1);
                            }
                        }
                        if (DATATYPE == "accumulationCells")
                        {
                            accumulationCells[i][steps - 1] = cell.getCellBehaviourFlow().getAccumulatedCars();
                        }
                        if (DATATYPE == "speedCells")
                        {
                            if (steps > 1)
                            {
                                speedCells[i][steps - 1] = cell
                                        .retrieveCurrentSpeed(cell.getCellBehaviourFlow().getAccumulatedCars()
                                                / cell.getCellLength().getInUnit(LengthUnit.KILOMETER))
                                        .getInUnit(SpeedUnit.KM_PER_HOUR);
                                if (speedCells[i][steps - 1] < cell.getCellBehaviourFlow().getParametersFundamentalDiagram()
                                        .getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR))
                                {
                                    System.out.println("SpeedLower");
                                }
                            }
                            else
                            {
                                speedCells[i][steps - 1] = cell.getCellBehaviourFlow().getParametersFundamentalDiagram()
                                        .getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR);
                            }
                        }
                        i++;
                    }
                    linkNumber++;
                }

            }
            numberOfCells = i;
        }

        // Write data to file
        if (DATATYPE == "accumulationCells" || DATATYPE == "speedCells")
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
                    char character = 'a';
                    for (int i = 0; i < numberOfCells; i++)
                    {
                        if (i > 0 && (indexStartNode.get(i) != indexStartNode.get(i - 1)
                                || indexEndNode.get(i) != indexEndNode.get(i - 1)))
                        {
                            character = 'a';
                        }
                        data.write(description + " " + indexStartNode.get(i).getId() + "-" + indexEndNode.get(i).getId()
                                + character + ", ");
                        for (int j = 0; j < steps; j++)
                        {
                            if (dataArray[i][j] == null)
                            {
                                dataArray[i][j] = Double.NaN;
                            }

                            textOut = String.format("%.5f", dataArray[i][j]);
                            data.write(textOut + ", ");
                        }
                        character++;
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
        else if (DATATYPE == "parametersFD")
        {
            if (steps == 1)
            {
                try
                {
                    String textOut;
                    // Link transmission
                    data.write("Indicators:   " + ", ");
                    data.write("Max. capacity" + ", ");
                    data.write("Crit AccMax" + ", ");
                    data.write("Crit AccJam" + ", ");
                    data.write(" \n");

                    for (int i = 0; i < numberOfCells; i++)
                    {
                        data.write(description + " " + indexLink.get(i).getId() + ", ");
                        for (int j = 0; j < 3; j++)
                        {
                            if (dataArray[i][j] == null)
                            {
                                dataArray[i][j] = Double.NaN;
                            }

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
        else if (DATATYPE == "cellData")
        {
            if (steps == 1)
            {
                try
                {
                    String textOut;
                    // Link transmission
                    data.write("Indicators:   " + ", ");
                    data.write("X start       " + ", ");
                    data.write("Y start       " + ", ");
                    data.write("X end         " + ", ");
                    data.write("Y end         " + ", ");
                    data.write("X middle      " + ", ");
                    data.write("Y middle      " + ", ");
                    data.write("Cell Length" + ", ");
                    data.write("Number of Lanes" + ", ");
                    data.write(" \n");

                    for (int i = 0; i < numberOfCells; i++)
                    {
                        data.write(description + " " + indexLink.get(i).getId() + ", ");
                        for (int j = 0; j < 8; j++)
                        {
                            if (dataArray[i][j] == null)
                            {
                                dataArray[i][j] = Double.NaN;
                            }

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
    static BufferedWriter dataDemandToNeighbourNTMOut = null;

    /** */
    static BufferedWriter dataAccumulationToNeighbourNTMOut = null;

    /** */
    static BufferedWriter dataRouteFractionToNeighbourNTMOut = null;

    /** */
    static BufferedWriter dataDemandVersusCapacityToNeighbourNTMOut = null;

    /** */
    static BufferedWriter dataTimeToDestinationNTMOut = null;

    /** */
    static BufferedWriter dataODDeparturesNTMOut = null;

    /** */
    static BufferedWriter dataODArrivalsNTMOut = null;

    static Double[] criticalCapacity = new Double[99];

    /** */
    static Double[][] accumulationNTM = new Double[MAXZONES][MAXSTEPS];

    /** */
    static Double[][] congestedSpeedNTM = new Double[MAXZONES][MAXSTEPS];

    /** */
    static Double[][] demandNTM = new Double[MAXZONES][MAXSTEPS];

    /** */
    static Double[][] routesNTM = new Double[MAXZONES][MAXSTEPS];

    /** */
    static Double[][] supplyNTM = new Double[MAXZONES][MAXSTEPS];

    /** */
    static Double[][] departuresNTM = new Double[MAXZONES][MAXSTEPS];

    /** */
    static Double[][] arrivalsNTM = new Double[MAXZONES][MAXSTEPS];

    /** */
    static Double[][][] fluxToNeighbourNTM = new Double[9][9][MAXSTEPS];

    /** */
    static LinkedHashMap<NTMNode, LinkedHashMap<NTMNode, LinkedHashMap<NTMNode, Double[]>>> fluxToNeighbourNTMMap = new LinkedHashMap<>();

    /** */
    static LinkedHashMap<NTMNode, LinkedHashMap<NTMNode, LinkedHashMap<NTMNode, Double[]>>> demandToNeighbourNTMMap = new LinkedHashMap<>();

    /** */
    static LinkedHashMap<NTMNode, LinkedHashMap<NTMNode, LinkedHashMap<NTMNode, Double[]>>> accumulationToNeighbourNTMMap = new LinkedHashMap<>();

    /** */
    static LinkedHashMap<NTMNode, LinkedHashMap<NTMNode, LinkedHashMap<NTMNode, Double[]>>> routeFractionToNeighbourNTMMap = new LinkedHashMap<>();

    /** */
    static LinkedHashMap<NTMNode, LinkedHashMap<NTMNode, LinkedHashMap<NTMNode, Double[]>>> demandVersusCapacityToNeighbourNTMMap =
            new LinkedHashMap<>();

    /** */
    static LinkedHashMap<NTMNode, LinkedHashMap<NTMNode, Double[]>> timeToDestinationNTMMap = new LinkedHashMap<>();

    /** */
    static LinkedHashMap<NTMNode, LinkedHashMap<NTMNode, Double[]>> ODArrivalsNTMMap = new LinkedHashMap<>();

    /** */
    static LinkedHashMap<NTMNode, LinkedHashMap<NTMNode, Double[]>> ODDeparturesNTMMap = new LinkedHashMap<>();

    /** */
    static Double[][] parametersNFD = new Double[MAXZONES][9];

    /** */
    static Double[][] capSpeedRoadLengthNTM = new Double[MAXZONES][9];

    static boolean startRoute = true;

    // Writing output data
    /**
     * @param model NTMModel;
     * @param steps int;
     * @param MAXSTEPS int;
     * @param arrivalsPreviousStep
     * @param departuresPreviousStep
     * @throws Exception
     */
    public static void writeOutputDataNTM(NTMModel model, int steps, int MAXSTEPS) throws Exception
    {
        // for testing we open a file and write some results:
        // TODO testing
        createDir(model.getInputNTM().getInputMap() + model.getInputNTM().getOutputMap());

        String fileName = "/NTMfluxToNeighbour";
        String description = "fluxes";
        String DATATYPE = "fluxes";
        if (steps == 1)
        {
            File file = new File(model.getInputNTM().getInputMap() + model.getInputNTM().getOutputMap() + fileName + ".txt");
            dataFluxToNeighbourNTMOut = createWriter(file);
        }
        writeHashMap(model, steps, MAXSTEPS, description, dataFluxToNeighbourNTMOut, null, fluxToNeighbourNTMMap, indexNode,
                DATATYPE);

        fileName = "/NTMdemandToNeighbour";
        description = "demandToNeighbour";
        DATATYPE = "demandToNeighbour";
        if (steps == 1)
        {
            File file = new File(model.getInputNTM().getInputMap() + model.getInputNTM().getOutputMap() + fileName + ".txt");
            dataDemandToNeighbourNTMOut = createWriter(file);
        }
        writeHashMap(model, steps, MAXSTEPS, description, dataDemandToNeighbourNTMOut, null, demandToNeighbourNTMMap, indexNode,
                DATATYPE);

        fileName = "/NTMaccumulationToNeighbour";
        description = "accumulationToNeighbour";
        DATATYPE = "accumulationToNeighbour";
        if (steps == 1)
        {
            File file = new File(model.getInputNTM().getInputMap() + model.getInputNTM().getOutputMap() + fileName + ".txt");
            dataAccumulationToNeighbourNTMOut = createWriter(file);
        }
        writeHashMap(model, steps, MAXSTEPS, description, dataAccumulationToNeighbourNTMOut, null,
                accumulationToNeighbourNTMMap, indexNode, DATATYPE);

        fileName = "/NTMrouteFractionToNeighbour";
        description = "routeFractionToNeighbour";
        DATATYPE = "routeFractionToNeighbour";
        if (steps == 1)
        {
            File file = new File(model.getInputNTM().getInputMap() + model.getInputNTM().getOutputMap() + fileName + ".txt");
            dataRouteFractionToNeighbourNTMOut = createWriter(file);
        }
        writeHashMap(model, steps, MAXSTEPS, description, dataRouteFractionToNeighbourNTMOut, null,
                routeFractionToNeighbourNTMMap, indexNode, DATATYPE);

        fileName = "/NTMdemandVersusCapacityToNeighbour";
        description = "demandVersusCapacity";
        DATATYPE = "demandVersusCapacity";
        if (steps == 1)
        {
            File file = new File(model.getInputNTM().getInputMap() + model.getInputNTM().getOutputMap() + fileName + ".txt");
            dataDemandVersusCapacityToNeighbourNTMOut = createWriter(file);
        }
        writeHashMap(model, steps, MAXSTEPS, description, dataDemandVersusCapacityToNeighbourNTMOut, null,
                demandVersusCapacityToNeighbourNTMMap, indexNode, DATATYPE);

        fileName = "/NTMtravelTimeToDestination";
        description = "timeToDestination";
        DATATYPE = "travelTime";
        if (steps == 1)
        {
            // File file = new File(model.getSettingsNTM().getPath() + model.getOutput() + fileName + ".txt");
            // dataTimeToDestinationNTMOut = createWriter(file);
        }
        // writeHashMap(model, steps, MAXSTEPS, description, dataTimeToDestinationNTMOut, timeToDestinationNTMMap,
        // indexNode,
        // DATATYPE);

        fileName = "/NTMdeparturesByOD";
        description = "departures by OD";
        DATATYPE = "departuresOD";
        if (steps == 1)
        {
            File file = new File(model.getInputNTM().getInputMap() + model.getInputNTM().getOutputMap() + fileName + ".txt");
            dataODDeparturesNTMOut = createWriter(file);
        }
        writeHashMap(model, steps, MAXSTEPS, description, dataODDeparturesNTMOut, ODDeparturesNTMMap, null, indexNode,
                DATATYPE);

        fileName = "/NTMarrivalsByOD";
        description = "arrived by OD";
        DATATYPE = "arrivalsOD";
        if (steps == 1)
        {
            File file = new File(model.getInputNTM().getInputMap() + model.getInputNTM().getOutputMap() + fileName + ".txt");
            dataODArrivalsNTMOut = createWriter(file);
        }
        writeHashMap(model, steps, MAXSTEPS, description, dataODArrivalsNTMOut, ODArrivalsNTMMap, null, indexNode, DATATYPE);

        fileName = "/ALLarrivals";
        description = "Arrived trips";
        DATATYPE = "arrivals";
        if (steps == 1)
        {
            File file = new File(model.getInputNTM().getInputMap() + model.getInputNTM().getOutputMap() + fileName + ".txt");
            dataArrivalsNTMOut = createWriter(file);
        }
        writeArray(model, steps, MAXSTEPS, description, dataArrivalsNTMOut, arrivalsNTM, DATATYPE);

        fileName = "/ALLdepartures";
        description = "departed trips";
        DATATYPE = "departures";
        if (steps == 1)
        {
            File file = new File(model.getInputNTM().getInputMap() + model.getInputNTM().getOutputMap() + fileName + ".txt");
            dataDeparturesNTMOut = createWriter(file);
        }
        writeArray(model, steps, MAXSTEPS, description, dataDeparturesNTMOut, departuresNTM, DATATYPE);

        fileName = "/NTMaccumulation";
        description = "Accumulation trips";
        DATATYPE = "accumulation";
        if (steps == 1)
        {
            File file = new File(model.getInputNTM().getInputMap() + model.getInputNTM().getOutputMap() + fileName + ".txt");
            dataAccumulationNTMOut = createWriter(file);
        }
        writeArray(model, steps, MAXSTEPS, description, dataAccumulationNTMOut, accumulationNTM, DATATYPE);

        fileName = "/NTMspeed";
        description = "Congested speed";
        DATATYPE = "congestedSpeed";
        if (steps == 1)
        {
            File file = new File(model.getInputNTM().getInputMap() + model.getInputNTM().getOutputMap() + fileName + ".txt");
            dataCongestedSpeedNTMOut = createWriter(file);
        }
        writeArray(model, steps, MAXSTEPS, description, dataCongestedSpeedNTMOut, congestedSpeedNTM, DATATYPE);

        fileName = "/ALLdemand";
        description = "Demand trips";
        DATATYPE = "demand";
        if (steps == 1)
        {
            File file = new File(model.getInputNTM().getInputMap() + model.getInputNTM().getOutputMap() + fileName + ".txt");
            dataDemandNTMOut = createWriter(file);
        }
        writeArray(model, steps, MAXSTEPS, description, dataDemandNTMOut, demandNTM, DATATYPE);

        fileName = "/ALLsupply";
        description = "Supply trips";
        DATATYPE = "supply";
        if (steps == 1)
        {
            File file = new File(model.getInputNTM().getInputMap() + model.getInputNTM().getOutputMap() + fileName + ".txt");
            dataSupplyNTMOut = createWriter(file);
        }
        writeArray(model, steps, MAXSTEPS, description, dataSupplyNTMOut, supplyNTM, DATATYPE);

        fileName = "/NTMparametersNFD";
        description = "Parameters NFD";
        DATATYPE = "parametersNFD";
        if (steps == 1)
        {
            File file = new File(model.getInputNTM().getInputMap() + model.getInputNTM().getOutputMap() + fileName + ".txt");
            dataParametersNFDOut = createWriter(file);
        }
        writeArray(model, steps, MAXSTEPS, description, dataParametersNFDOut, parametersNFD, DATATYPE);
        if (steps == MAXSTEPS - 1)
        {
            NTMTestApplication.textArea.append("Finished writing data, \n");
        }

    }

    // Writing output data
    /**
     * @param model NTMModel;
     * @param steps int;
     * @param pathIterator int;
     * @param startNode NTMNode;
     * @param neighbour NTMNode;
     * @param destination NTMNode;
     * @param routeI
     * @param MAXSTEPS int;
     * @throws IOException
     */
    public static void writeOutputRoutesNTM(NTMModel model, int steps, int pathIterator, NTMNode startNode, NTMNode neighbour,
            NTMNode destination, int MAXSTEPS, BufferedWriter data, double oldShare, double addShare, double pathWeight)
            throws IOException
    {
        // for testing we open a file and write some results:
        // TODO testing
        String description = "routesNTM";
        ArrayList<NTMNode> path = new ArrayList<>();
        path.add(startNode);
        path.add(neighbour);
        path.add(destination);
        writeRoutes(model, steps, pathIterator, MAXSTEPS, data, description, path, oldShare, addShare, pathWeight);

    }

    /**
     * @param model NTMModel;
     * @param steps int;
     * @param MAXSTEPS int;
     * @param description String;
     * @param data BufferedWriter;
     * @param dataArray ArrayList&lt;NTMNode&gt;;
     * @param DATATYPE
     * @throws IOException
     */
    static void writeRoutes(NTMModel model, int steps, int pathIterator, int MAXSTEPS, BufferedWriter data, String description,
            ArrayList<NTMNode> dataArray, double oldShare, double addShare, double pathWeight) throws IOException
    {
        if (description == "routesNTM")
        {

            if (steps <= MAXSTEPS - 1 && dataArray.get(2).getId().equals("C1017") && dataArray.get(0).getId().startsWith("C70"))
            {
                if (startRoute)
                {
                    data.write("SimulationStep  " + ", ");
                    data.write("RouteIter       " + ", ");
                    data.write("Origin          " + ", ");
                    data.write("Neighbour       " + ", ");
                    data.write("destination     " + ", ");
                    data.write("oldShare        " + ", ");
                    data.write("added Weight    " + ", ");
                    data.write("travel Time     " + ", ");

                    data.write(" \n");
                    startRoute = false;
                }

                data.write(description + " " + steps + ", ");
                data.write(pathIterator + ", ");
                for (int j = 0; j < 3; j++)
                {
                    data.write(dataArray.get(j).getId() + ", ");
                }
                data.write(oldShare + ", ");
                data.write(addShare + ", ");
                data.write(pathWeight + ", ");

                data.write(" \n");

            }
        }

    }

    /**
     * @param model NTMModel;
     * @param steps int;
     * @param MAXSTEPS int;
     * @param data BufferedWriter;
     * @param nodeDoublemap
     * @param intNodeMap LinkedHashMap&lt;Integer,NTMNode&gt;;
     */
    static void writeHashMap(NTMModel model, int steps, int MAXSTEPS, String description, BufferedWriter data,
            LinkedHashMap<NTMNode, LinkedHashMap<NTMNode, Double[]>> nodeNodeDoublemap,
            LinkedHashMap<NTMNode, LinkedHashMap<NTMNode, LinkedHashMap<NTMNode, Double[]>>> nodeNodeNodeDoublemap,
            LinkedHashMap<Integer, NTMNode> intNodeMap, String DATATYPE)
    {

        if (steps < MAXSTEPS)
        {
            int i = 0;
            TreeSet<NTMNode> graphVertices = new TreeSet<NTMNode>(model.getAreaGraph().vertexSet());

            for (NTMNode nodeIn : graphVertices)
            {
                BoundedNode origin = (BoundedNode) nodeIn;
                if (origin.getBehaviourType() == TrafficBehaviourType.NTM
                        || origin.getBehaviourType() == TrafficBehaviourType.CORDON)
                {
                    CellBehaviour cellBehaviour = origin.getCellBehaviour();
                    for (TripInfoByDestination tripInfoByDestination : cellBehaviour.getTripInfoByDestinationMap().values())
                    {
                        BoundedNode destination = (BoundedNode) tripInfoByDestination.getDestination();
                        double trips = 0;
                        if (DATATYPE == "arrivalsOD")
                        {
                            trips = tripInfoByDestination.getArrivedTrips();
                        }
                        else if (DATATYPE == "departuresOD")
                        {
                            trips = tripInfoByDestination.getDepartedTrips();

                        }
                        if (trips > 0.0)
                        {
                            if (nodeNodeDoublemap != null)
                            {
                                if (nodeNodeDoublemap.get(origin) == null)
                                {
                                    LinkedHashMap<NTMNode, Double[]> fluxMap = new LinkedHashMap<NTMNode, Double[]>();
                                    Double[] fluxes = new Double[MAXSTEPS];
                                    fluxes[steps - 1] = trips;
                                    fluxMap.put(destination, fluxes);
                                    nodeNodeDoublemap.put(origin, fluxMap);
                                }
                                else
                                {
                                    if (nodeNodeDoublemap.get(origin).get(destination) == null)
                                    {
                                        Double[] fluxes = new Double[MAXSTEPS];
                                        fluxes[steps - 1] = trips;
                                        nodeNodeDoublemap.get(origin).put(destination, fluxes);
                                    }
                                    else
                                    {
                                        Double[] fluxes = nodeNodeDoublemap.get(origin).get(destination);
                                        fluxes[steps - 1] = trips;
                                        nodeNodeDoublemap.get(origin).put(destination, fluxes);
                                    }
                                }
                            }
                        }

                        Set<BoundedNode> neighbours = tripInfoByDestination.getRouteFractionToNeighbours().keySet();
                        for (BoundedNode neighbour : neighbours)
                        {
                            trips = 0;
                            if (DATATYPE == "demandVersusCapacity")
                            {
                                if (cellBehaviour.getBorderDemand() != null)
                                {
                                    if (cellBehaviour.getBorderDemand().get(neighbour) != null
                                            && cellBehaviour.getBorderCapacity().get(neighbour) != null)
                                    {
                                        if (cellBehaviour.getBorderDemand().get(neighbour)
                                                .getInUnit(FrequencyUnit.PER_HOUR) > 0)
                                        {
                                            trips = Math.min(1.0,
                                                    cellBehaviour.getBorderCapacity().get(neighbour)
                                                            .getInUnit(FrequencyUnit.PER_HOUR)
                                                            / cellBehaviour.getBorderDemand().get(neighbour)
                                                                    .getInUnit(FrequencyUnit.PER_HOUR));
                                        }
                                    }
                                }
                            }
                            else if (DATATYPE == "fluxes")
                            {
                                trips = tripInfoByDestination.getRouteFractionToNeighbours().get(neighbour)
                                        * tripInfoByDestination.getFluxToNeighbour();
                            }
                            else if (DATATYPE == "demandToNeighbour")
                            {
                                trips = tripInfoByDestination.getDemandToNeighbour().get(neighbour);
                            }
                            else if (DATATYPE == "accumulationToNeighbour")
                            {
                                trips = tripInfoByDestination.getAccumulatedCarsToNeighbour().get(neighbour);
                            }
                            else if (DATATYPE == "routeFractionToNeighbour")
                            {
                                trips = tripInfoByDestination.getRouteFractionToNeighbours().get(neighbour);
                            }

                            if (trips > 0.0)
                            {
                                if (nodeNodeNodeDoublemap != null)
                                {
                                    if (nodeNodeNodeDoublemap.get(origin) == null)
                                    {
                                        LinkedHashMap<NTMNode, LinkedHashMap<NTMNode, Double[]>> nodeFluxMap =
                                                new LinkedHashMap<NTMNode, LinkedHashMap<NTMNode, Double[]>>();
                                        LinkedHashMap<NTMNode, Double[]> fluxMap = new LinkedHashMap<NTMNode, Double[]>();
                                        Double[] fluxes = new Double[MAXSTEPS];
                                        fluxes[steps - 1] = trips;
                                        fluxMap.put(neighbour, fluxes);
                                        nodeFluxMap.put(destination, fluxMap);
                                        nodeNodeNodeDoublemap.put(origin, nodeFluxMap);
                                    }
                                    else
                                    {
                                        if (nodeNodeNodeDoublemap.get(origin).get(destination) == null)
                                        {
                                            LinkedHashMap<NTMNode, Double[]> fluxMap = new LinkedHashMap<NTMNode, Double[]>();
                                            Double[] fluxes = new Double[MAXSTEPS];
                                            fluxes[steps - 1] = trips;
                                            fluxMap.put(neighbour, fluxes);
                                            nodeNodeNodeDoublemap.get(origin).put(destination, fluxMap);
                                        }
                                        else if (nodeNodeNodeDoublemap.get(origin).get(destination).get(neighbour) == null)
                                        {
                                            Double[] fluxes = new Double[MAXSTEPS];
                                            fluxes[steps - 1] = trips;
                                            nodeNodeNodeDoublemap.get(origin).get(destination).put(neighbour, fluxes);
                                        }
                                        else
                                        {
                                            Double[] fluxes = nodeNodeNodeDoublemap.get(origin).get(destination).get(neighbour);
                                            fluxes[steps - 1] = trips;
                                            nodeNodeNodeDoublemap.get(origin).get(destination).put(neighbour, fluxes);
                                        }
                                    }
                                }

                            }
                        }
                    }
                    i++;
                    numberOfCells = i;
                }
            }
        }

        String textOut;
        // Write data to file
        if (steps == MAXSTEPS - 1)
        {
            try
            {
                if (nodeNodeDoublemap != null)
                {
                    for (int i = 0; i < numberOfCells; i++)
                    {
                        LinkedHashMap<NTMNode, Double[]> fluxMap = nodeNodeDoublemap.get(intNodeMap.get(i));
                        if (fluxMap != null)
                        {
                            for (int j = 0; j < numberOfCells; j++)
                            {
                                Double[] trips = fluxMap.get(intNodeMap.get(j));
                                if (trips != null)
                                {
                                    data.write(
                                            description + " " + intNodeMap.get(i).getId() + intNodeMap.get(j).getId() + ", ");
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
                }
                if (nodeNodeNodeDoublemap != null)
                {
                    for (int i = 0; i < numberOfCells; i++)
                    {
                        LinkedHashMap<NTMNode, LinkedHashMap<NTMNode, Double[]>> nodeFluxMap = nodeNodeNodeDoublemap.get(intNodeMap.get(i));
                        if (nodeFluxMap != null)
                        {
                            for (int j = 0; j < numberOfCells; j++)
                            {
                                LinkedHashMap<NTMNode, Double[]> fluxMap = nodeFluxMap.get(intNodeMap.get(j));
                                if (fluxMap != null)
                                {
                                    for (int k = 0; k < numberOfCells; k++)
                                    {
                                        Double[] trips = fluxMap.get(intNodeMap.get(k));
                                        if (trips != null)
                                        {
                                            data.write(description + " " + intNodeMap.get(i).getId() + " "
                                                    + intNodeMap.get(j).getId() + " " + intNodeMap.get(k).getId() + ", ");
                                            for (int l = 0; l < steps; l++)
                                            {
                                                if (trips[l] == null)
                                                {
                                                    trips[l] = 0.0;
                                                }
                                                textOut = String.format("%.5f", trips[l]);
                                                data.write(textOut + ", ");
                                            }
                                            data.write(" \n");
                                        }
                                    }
                                }
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
     * @param model NTMModel;
     * @param steps int;
     * @param MAXSTEPS int;
     * @param description String;
     * @param data BufferedWriter;
     * @param dataArray Double[][];
     * @param DATATYPE String;
     * @throws Exception
     */
    static void writeArray(NTMModel model, int steps, int MAXSTEPS, String description, BufferedWriter data,
            Double[][] dataArray, String DATATYPE) throws Exception
    {

        if (steps < MAXSTEPS)
        {

            int i = 0;
            TreeSet<NTMNode> graphVertices = new TreeSet<NTMNode>(model.getAreaGraph().vertexSet());

            for (NTMNode nodeIn : graphVertices)
            {
                BoundedNode node = (BoundedNode) nodeIn;
                if (node.getBehaviourType() == TrafficBehaviourType.NTM
                        || node.getBehaviourType() == TrafficBehaviourType.CORDON)
                {
                    CellBehaviour cellBehaviour = node.getCellBehaviour();
                    if (steps == 1)
                    {
                        nodeIndex.put(node, i);
                        indexNode.put(i, node);
                    }
                    if (DATATYPE == "arrivals")
                    {
                        if (steps > 1)
                        {
                            dataArray[nodeIndex.get(node)][steps - 1] =
                                    cellBehaviour.getArrivals() + dataArray[nodeIndex.get(node)][steps - 2];
                        }
                        else
                        {
                            dataArray[nodeIndex.get(node)][steps - 1] = cellBehaviour.getArrivals();
                        }
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
                        if (node.getBehaviourType() == TrafficBehaviourType.NTM)
                        {
                            CellBehaviourNTM cellBehaviourNTM = (CellBehaviourNTM) node.getCellBehaviour();
                            if (steps == 1)
                            {
                                dataArray[nodeIndex.get(node)][steps - 1] =
                                        cellBehaviourNTM.getParametersNTM().getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR);
                            }
                            else
                            {
                                if (cellBehaviourNTM.getCurrentSpeed() != null)
                                {
                                    dataArray[nodeIndex.get(node)][steps - 1] =
                                            cellBehaviourNTM.getCurrentSpeed().getInUnit(SpeedUnit.KM_PER_HOUR);
                                }
                            }
                        }
                    }
                    else if (DATATYPE == "demand")
                    {
                        dataArray[nodeIndex.get(node)][steps - 1] = cellBehaviour.getDemand();
                    }
                    else if (DATATYPE == "supply")
                    {
                        dataArray[nodeIndex.get(node)][steps - 1] = cellBehaviour.getSupply();
                    }

                    if (DATATYPE == "parametersNFD")
                    {
                        if (node.getBehaviourType() == TrafficBehaviourType.NTM)
                        {
                            CellBehaviourNTM cellBehaviourNTM = (CellBehaviourNTM) node.getCellBehaviour();

                            if (steps == 1)
                            {
                                dataArray[i][0] =
                                        cellBehaviourNTM.getParametersNTM().getCapacity().getInUnit(FrequencyUnit.PER_HOUR);
                                dataArray[i][1] =
                                        cellBehaviourNTM.getParametersNTM().getRoadLength().getInUnit(LengthUnit.KILOMETER);
                                dataArray[i][2] =
                                        cellBehaviourNTM.getParametersNTM().getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR);
                                dataArray[i][3] = cellBehaviourNTM.getParametersNTM().getAccCritical().get(0);
                                dataArray[i][4] = cellBehaviourNTM.getParametersNTM().getAccCritical().get(1);
                                dataArray[i][5] = cellBehaviourNTM.getParametersNTM().getAccCritical().get(2);
                                /*
                                 * for (int k = 0; k < 6; k++) { if (dataArray[i][k] == null) { dataArray[i][k] = Double.NaN; }
                                 * }
                                 */
                            }
                        }

                    }
                    i++;
                }
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
                            if (dataArray[i][j] == null)
                            {
                                dataArray[i][j] = Double.NaN;
                            }
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
                    // here the variables start
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
                            if (dataArray[i][j] == null)
                            {
                                dataArray[i][j] = Double.NaN;
                            }

                            textOut = String.format("%.5f", dataArray[i][j]);
                            data.write(textOut + ", ");
                        }
                        criticalCapacity[i] = dataArray[i][1] * dataArray[i][3];

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
        if (steps == MAXSTEPS - 1)
        {
            if (DATATYPE == "accumulation")
            {
                final TimeSeries series = new TimeSeries("Accumulation");
                Second current = new Second();

                String[] categoryMax = new String[99];
                final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                for (int i = 0; i < numberOfCells; i++)
                {
                    // categoryMax[i] = Integer.toString(i);
                    if (indexNode.get(i).getBehaviourType() == TrafficBehaviourType.NTM)
                    {
                        categoryMax[i] = indexNode.get(i).getId();
                        Double maxValue = 0.0;
                        for (int j = 0; j < steps; j++)
                        {
                            if (dataArray[i][j] == null)
                            {
                                dataArray[i][j] = Double.NaN;
                            }
                            try
                            {
                                double value = dataArray[i][j];
                                maxValue = Math.max(maxValue, value);
                                series.add(current, new Double(value));
                                current = (Second) current.next();
                            }
                            catch (SeriesException e)
                            {
                                System.err.println("Error adding to series");
                            }
                        }
                        dataset.addValue(maxValue, "maxDensity", categoryMax[i]);
                        dataset.addValue(criticalCapacity[i], "criticalCapacity", categoryMax[i]);
                    }
                }
                String fileName = "/TimeSeries.jpeg";
                TimeSeriesChart.TimeSeries(model.getInputNTM().getInputMap() + model.getInputNTM().getOutputMap() + fileName,
                        series);
                fileName = "/AccumulationChart.jpeg";
                TimeSeriesChart.BarChart(model.getInputNTM().getInputMap() + model.getInputNTM().getOutputMap() + fileName,
                        dataset);

            }
        }
    }

    /**
     * @param ctmLink LinkCellTransmission;
     * @param cell FlowCell;
     * @param index int;
     * @param totalNumberOfCells int;
     * @return
     */
    static ArrayList<Coordinate> retrieveCellXY(LinkCellTransmission ctmLink, FlowCell cell, int index, int totalNumberOfCells)
    {
        Coordinate pointA = null;
        Coordinate pointMiddle = null;
        Coordinate pointB = null;
        LineString line = ctmLink.getDesignLine().getLineString();
        LengthIndexedLine indexedLine = new LengthIndexedLine(line);
        if (ctmLink.getStartNode().getPoint().getCoordinate().x == line.getCoordinates()[0].x
                && ctmLink.getStartNode().getPoint().getCoordinate().y == line.getCoordinates()[0].y)
        {
            pointA = indexedLine.extractPoint(line.getLength() * (index) / totalNumberOfCells);
            pointB = indexedLine.extractPoint(line.getLength() * (index + 1) / totalNumberOfCells);
            pointMiddle = indexedLine.extractPoint(line.getLength() * (index + 0.5) / totalNumberOfCells);
        }
        else
        {
            pointA = indexedLine.extractPoint(line.getLength() * (1.0 - ((index) / totalNumberOfCells)));
            pointB = indexedLine.extractPoint(line.getLength() * (1.0 - ((index + 1.0) / totalNumberOfCells)));
            pointMiddle = indexedLine.extractPoint(line.getLength() * (1.0 - ((index + 0.5) / totalNumberOfCells)));
        }
        ArrayList<Coordinate> points = new ArrayList<>();
        points.add(pointA);
        points.add(pointB);
        points.add(pointMiddle);
        return points;
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

    static File createDir(String directory)
    {
        File theDir = new File(directory);

        // if the directory does not exist, create it
        if (!theDir.exists())
        {
            boolean result = false;

            try
            {
                theDir.mkdir();
                result = true;
            }
            catch (SecurityException se)
            {
                // handle it
            }
        }
        return theDir;
    }

}
