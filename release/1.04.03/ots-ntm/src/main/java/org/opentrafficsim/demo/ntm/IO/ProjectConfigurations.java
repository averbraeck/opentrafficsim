package org.opentrafficsim.demo.ntm.IO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.demo.ntm.CsvFileReader;
import org.opentrafficsim.demo.ntm.NTMModel;
import org.opentrafficsim.demo.ntm.ShapeFileReader;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 22 Feb 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class ProjectConfigurations
{

    /**
     * @param startMap String;
     * @param model NTMModel;
     * @throws IOException
     * @throws ParseException
     */
    public static void readConfigurations(final String startMap, NTMModel model) throws IOException, ParseException
    {
        String fileProject = org.opentrafficsim.demo.ntm.IO.FileDialog.showFileDialog(true, "", "", startMap);
        URL url;
        if (new File(fileProject).canRead())
        {
            url = new File(fileProject).toURI().toURL();
        }
        else
        {
            url = ShapeFileReader.class.getResource(fileProject);
        }
        String path = url.getPath();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
        String line = "";
        String csvSplitBy = ",";
        while ((line = bufferedReader.readLine()) != null && !line.isEmpty())
        {
            String[] config = line.split(csvSplitBy);
            int index = 0;
            // first we inspect if it is a centroid
            String name = config[0].trim();
            name = CsvFileReader.removeQuotes(name);
            if (name.equals("WRITEDATA"))
            {
                if (config[1].trim().equals("true"))
                {
                    model.getInputNTM().WRITEDATA = true;
                }
                else
                {
                    model.getInputNTM().WRITEDATA = false;
                }
            }
            else if (name.equals("COMPRESS_AREAS"))
            {
                if (config[1].trim().equals("true"))
                {
                    model.getInputNTM().COMPRESS_AREAS = true;
                }
                else
                {
                    model.getInputNTM().COMPRESS_AREAS = false;
                }
            }
            else if (name.equals("paint"))
            {
                if (config[1].trim().equals("true"))
                {
                    model.getInputNTM().paint = true;
                }
                else
                {
                    model.getInputNTM().paint = false;
                }
            }
            else if (name.equals("path"))
            {
                model.getInputNTM().setInputMap(config[1].trim());
            }
            else if (name.equals("centroids"))
            {
                String file = config[1].trim();
                model.getInputNTM().setFileCentroids(file);
                String type = config[2].trim();
                boolean returnCentroids;
                if (config[3].trim().equals("true"))
                {
                    returnCentroids = true;
                }
                else
                {
                    returnCentroids = false;
                }
                boolean allCentroids;
                model.getInputNTM().setReturnCentroidsCentroid(returnCentroids);
                if (config[4].trim().equals("true"))
                {
                    allCentroids = true;
                }
                else
                {
                    allCentroids = false;
                }
                model.getInputNTM().setOnlyCentroidsFileCentroid(allCentroids);
            }
            else if (name.equals("nodes"))
            {
                String file = config[1].trim();
                model.getInputNTM().setFileNodes(file);
                String type = config[2].trim();
                boolean returnCentroids;
                if (config[3].trim().equals("true"))
                {
                    returnCentroids = true;
                }
                else
                {
                    returnCentroids = false;
                }
                model.getInputNTM().setReturnCentroidsNode(returnCentroids);
                boolean allCentroids;
                if (config[4].trim().equals("true"))
                {
                    allCentroids = true;
                }
                else
                {
                    allCentroids = false;
                }
                model.getInputNTM().setOnlyCentroidsFileNode(allCentroids);
            }
            else if (name.equals("areas"))
            {
                String file = config[1].trim();
                model.getInputNTM().setFileAreas(file);
            }
            else if (name.equals("areasBig"))
            {
                String file = config[1].trim();
                model.getInputNTM().setFileAreasBig(file);
            }
            else if (name.equals("links"))
            {
                String file = config[1].trim();
                model.getInputNTM().setFileLinks(file);
            }
            else if (name.equals("lengthUnitLink"))
            {
                String value = config[1].trim();
                model.getInputNTM().setLengthUnitLink(value);
            }
            else if (name.equals("feederLinks"))
            {
                String file = config[1].trim();
                model.getInputNTM().setFileFeederLinks(file);
            }
            else if (name.equals("scalingFactorDemand"))
            {
                String value = config[1].trim();
                Double valueDouble = Double.parseDouble(value);
                model.getInputNTM().setScalingFactorDemand(valueDouble);
            }
            else if (name.equals("increaseDemandAreaByFactor"))
            {
                String value = config[1].trim();
                if (value.equals("true"))
                {
                    model.getInputNTM().setIncreaseDemandAreaByFactor(true);
                }
                else
                {
                    model.getInputNTM().setIncreaseDemandAreaByFactor(false);
                }
            }
            else if (name.equals("fileDemand"))
            {
                String value = config[1].trim();
                model.getInputNTM().setFileDemand(value);
            }
            /*
             * else if (name.equals("fileCompressedDemand")) { String value = config[1].trim();
             * model.getInputNTM().setFileCompressedDemand(value); }
             */

            else if (name.equals("fileProfiles"))
            {
                String value = config[1].trim();
                model.getInputNTM().setFileProfiles(value);

            }
            else if (name.equals("fileNameCapacityRestraint"))
            {
                String value = config[1].trim();
                model.getInputNTM().setFileNameCapacityRestraint(value);
            }
            else if (name.equals("fileNameCapacityRestraintFactor"))
            {
                String value = config[1].trim();
                model.getInputNTM().setFileNameCapacityRestraintFactor(value);
            }
            else if (name.equals("fileNameParametersNTM"))
            {
                String value = config[1].trim();
                model.getInputNTM().setFileNameParametersNTM(value);
            }
            else if (name.equals("fileNameCapacityRestraintBig"))
            {
                String value = config[1].trim();
                model.getInputNTM().setFileNameCapacityRestraintBig(value);
            }
            else if (name.equals("fileNameCapacityRestraintFactorBig"))
            {
                String value = config[1].trim();
                model.getInputNTM().setFileNameCapacityRestraintFactorBig(value);
            }
            else if (name.equals("fileNameParametersNTMBig"))
            {
                String value = config[1].trim();
                model.getInputNTM().setFileNameParametersNTMBig(value);
            }
            else if (name.equals("numberOfRoutes"))
            {
                String value = config[1].trim();
                int valueDouble = Integer.parseInt(value);
                model.getInputNTM().setNumberOfRoutes(valueDouble);
            }
            else if (name.equals("weightNewRoutes"))
            {
                String value = config[1].trim();
                Double valueDouble = Double.parseDouble(value);
                model.getInputNTM().setWeightNewRoutes(valueDouble);
            }
            else if (name.equals("varianceRoutes"))
            {
                String value = config[1].trim();
                Double valueDouble = Double.parseDouble(value);
                model.getInputNTM().setVarianceRoutes(valueDouble);
            }
            else if (name.equals("scalingFactorDemand"))
            {
                String value = config[1].trim();
                Double valueDouble = Double.parseDouble(value);
                model.getInputNTM().setScalingFactorDemand(valueDouble);
            }
            else if (name.equals("reRoute"))
            {
                String value = config[1].trim();
                if (value.equals("true"))
                {
                    model.getInputNTM().setReRoute(true);
                }
                else
                {
                    model.getInputNTM().setReRoute(false);
                }
            }
            else if (name.equals("reRouteTimeInterval"))
            {
                String value = config[1].trim();
                Double valueDouble = Double.parseDouble(value);
                Duration time = new Duration(300, DurationUnit.SECOND);
                model.getInputNTM().setReRouteTimeInterval(time);
            }
            else if (name.equals("linkCapacityNumberOfHours"))
            {
                String value = config[1].trim();
                Double valueDouble = Double.parseDouble(value);
                model.getInputNTM().setLinkCapacityNumberOfHours(valueDouble);
            }
            else if (name.equals("maxSpeed"))
            {
                String value = config[1].trim();
                Double valueDouble = Double.parseDouble(value);
                Speed speed = new Speed(valueDouble, SpeedUnit.KM_PER_HOUR);
                model.getInputNTM().setMaxSpeed(speed);
            }
            else if (name.equals("maxCapacity"))
            {
                String value = config[1].trim();
                Double valueDouble = Double.parseDouble(value);
                Frequency maxCapacity = new Frequency(valueDouble, FrequencyUnit.PER_HOUR);
                model.getInputNTM().setMaxCapacity(maxCapacity);
            }
            else if (name.equals("variantNumber"))
            {
                String value = config[1].trim();
                model.getInputNTM().setVariantNumber(value);
            }

        }

    }
}
