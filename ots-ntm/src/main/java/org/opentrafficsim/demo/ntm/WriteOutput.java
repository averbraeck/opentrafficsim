package org.opentrafficsim.demo.ntm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

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

    /** */
    static BufferedWriter debugOut = null;

    /** */
    static BufferedWriter dataLaneLengthOut = null;

    /** */
    static BufferedWriter dataLanesPerCellOut = null;

    /** */
    static BufferedWriter dataAccumulationCellOut = null;

    /** */
    static BufferedWriter dataParametersFDOut = null;

    /** */
    static BufferedWriter dataParametersNFDOut = null;

    /** */
    static BufferedWriter dataFDOut = null;

    /** */
    static Double[][] accumulation = new Double[999][999];

    /** */
    static Double[][] accumulationNTM = new Double[999][999];

    /** */
    static Double[][] demandNTM = new Double[999][999];

    /** */
    static Double[][] supplyNTM = new Double[999][999];

    /** */
    static Double[][] parametersFD = new Double[999][3];

    /** */
    static Double[][] parametersNFD = new Double[999][3];

    /** */
    static Double[][] capSpeedRoadLengthNTM = new Double[999][3];

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

    // Writing output data
    /**
     * @param model
     * @param steps
     * @param MAXSTEPS
     */
    public static void writeOutputData(NTMModel model, int steps, int MAXSTEPS)
    {
        // for testing we open a file and write some results:
        // TODO testing

/*        if (model.DEBUG && steps == 1)
        {
            File file = new File(model.getSettingsNTM().getPath() + "/Output/NTMoutputTest.txt");

            // if file doesnt exists, then create it
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
                debugOut = new BufferedWriter(new FileWriter(file));
            }
            catch (IOException exception1)
            {
                exception1.printStackTrace();
            }
        }*/

        if (model.DEBUG && steps == 1)
        {
            File fileLaneLength = new File(model.getSettingsNTM().getPath() + "/output/NTMoutputCellLength.txt");
            dataLaneLengthOut = createWriter(fileLaneLength);
            File fileAccumulation = new File(model.getSettingsNTM().getPath() + "/output/NTMoutputAccumulation.txt");
            dataAccumulationCellOut = createWriter(fileAccumulation);
            File fileLanesPerCell = new File(model.getSettingsNTM().getPath() + "/output/NTMoutputLanesPerCell.txt");
            dataLanesPerCellOut = createWriter(fileLanesPerCell);
            File fileParametersFD = new File(model.getSettingsNTM().getPath() + "/output/NTMoutputParametersFD.txt");
            dataParametersFDOut = createWriter(fileParametersFD);
            File fileParametersNFD = new File(model.getSettingsNTM().getPath() + "/output/NTMoutputParametersNFD.txt");
            dataParametersNFDOut = createWriter(fileParametersNFD);
        }

        if (model.DEBUG && steps < MAXSTEPS)
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
                                    cell.getCellBehaviourFlow().getParametersFundamentalDiagram().getCapacity()
                                            .doubleValue() * 3600;
                            parametersFD[i][1] =
                                    cell.getCellBehaviourFlow().getParametersFundamentalDiagram().getAccCritical()
                                            .get(0);
                            parametersFD[i][2] =
                                    cell.getCellBehaviourFlow().getParametersFundamentalDiagram().getAccCritical()
                                            .get(1);
                        }
                        accumulation[i][steps - 1] = cell.getCellBehaviourFlow().getAccumulatedCars();
                        i++;
                    }
                    linkNumber++;
                }
                else if (link.getBehaviourType() == TrafficBehaviourType.NTM)
                {
                    BoundedNode startNode = (BoundedNode) model.getNodeAreaGraphMap().get(link.getStartNode().getId());
                    CellBehaviourNTM cellBehaviour = (CellBehaviourNTM) startNode.getCellBehaviour();
                    if (steps == 1)
                    {
                        capSpeedRoadLengthNTM[i][0] =
                                cellBehaviour.getParametersNTM().getCapacity().getInUnit(FrequencyUnit.PER_HOUR);
                        capSpeedRoadLengthNTM[i][1] =
                                cellBehaviour.getParametersNTM().getRoadLength().getInUnit(LengthUnit.KILOMETER);
                        capSpeedRoadLengthNTM[i][2] =
                                cellBehaviour.getParametersNTM().getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR);
                        // cellBehaviour.getParametersNTM().getAccCritical().;
                        linkIndex.put(link, i);
                        indexLink.put(i, link);
                    }
                    accumulationNTM[linkIndex.get(link)][steps - 1] = cellBehaviour.getAccumulatedCars();
                    demandNTM[linkIndex.get(link)][steps - 1] = cellBehaviour.getDemand();
                    supplyNTM[linkIndex.get(link)][steps - 1] = cellBehaviour.getSupply();

                    Abs<FrequencyUnit> tripByHour =
                            cellBehaviour.retrieveDemand(cellBehaviour.getAccumulatedCars(),
                                    cellBehaviour.getMaxCapacity(), cellBehaviour.getParametersNTM());
                    double tripByTimeStep =
                            model.getSettingsNTM().getTimeStepDurationNTM().getSI() * tripByHour.getSI();

                    i++;
                    linkNumber++;
                }

            }
            numberOfCells = i;
        }

        if (model.DEBUG && steps == MAXSTEPS - 1)
        {
            try
            {
                boolean cellWrite = false;
                String textOut;
                // Link transmission
                if (cellWrite)
                {
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
                            if (accumulation[i][j] > 0)
                            {
                                textOut = String.format("%.5f", accumulation[i][j]);
                            }
                            else
                            {
                                textOut = " NaN";
                            }
                            dataAccumulationCellOut.write(textOut + ", ");
                        }
                        dataAccumulationCellOut.write(" \n");
                    }
                }

                // NTM data
                else
                {
                    for (int i = 0; i < numberOfCells; i++)
                    {
                        textOut = "Area parameters Cap/Length/Speed " + indexLink.get(i).getStartNode().getId() + ", ";
                        textOut += String.format("%.5f", capSpeedRoadLengthNTM[i][0]) + ", ";
                        textOut += String.format("%.5f", capSpeedRoadLengthNTM[i][1]) + ", ";
                        textOut += String.format("%.5f", capSpeedRoadLengthNTM[i][2]) + ", ";
                        dataParametersNFDOut.write(textOut + " \n");
                        
                        textOut =  "Link Accumulation " + indexLink.get(i).getStartNode().getId() + ", ";
                        for (int j = 0; j < steps; j++)
                        {
                            if (accumulationNTM[i][j] > 0)
                            {
                                textOut += String.format("%.5f", accumulationNTM[i][j]);
                            }
                            else
                            {
                                textOut += " NaN";
                            }
                            textOut += ", ";
                        }
                        dataAccumulationCellOut.write(textOut + " \n");

                    }
                    for (int i = 0; i < numberOfCells; i++)
                    {
                        //textOut = String.format("%.1f", laneLength[i]);
                        
                        textOut =  "Link Demand " + indexLink.get(i).getStartNode().getId() + ", ";
                        for (int j = 0; j < steps; j++)
                        {
                            if (demandNTM[i][j] > 0)
                            {
                                textOut += String.format("%.5f", demandNTM[i][j]);
                            }
                            else
                            {
                                textOut += " NaN";
                            }
                            textOut += ", ";
                        }
                        dataAccumulationCellOut.write(textOut +" \n");
                    }
                    for (int i = 0; i < numberOfCells; i++)
                    {
                        textOut = String.format("%.1f", laneLength[i]);
                        dataAccumulationCellOut.write("Link Supply " + indexLink.get(i).getStartNode().getId() + ", ");
                        for (int j = 0; j < steps; j++)
                        {
                            if (supplyNTM[i][j] > 0)
                            {
                                textOut = String.format("%.5f", supplyNTM[i][j]);
                            }
                            else
                            {
                                textOut = " NaN";
                            }
                            dataAccumulationCellOut.write(textOut + ", ");
                        }
                        dataAccumulationCellOut.write(" \n");
                    }
                }

                //debugOut.close();
                dataLaneLengthOut.close();
                dataLanesPerCellOut.close();
                dataLaneLengthOut.close();
                dataParametersFDOut.close();
                dataParametersNFDOut.close();
                dataAccumulationCellOut.close();
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
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
