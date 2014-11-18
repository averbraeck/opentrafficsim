package org.opentrafficsim.demo.ntm.IO;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.opentrafficsim.demo.ntm.shapeobjects.ShapeObject;
import org.opentrafficsim.demo.ntm.shapeobjects.ShapeStore;

import com.vividsolutions.jts.geom.Geometry;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 3 Nov 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class DataIO
{
    /**
     * <p>
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
     * reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version 14 Nov 2014 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
     * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
     */
    public enum days {
        Sat,
        Sun,
        Mon,
        Tue,
        Wed,
        Thu,
        Fri
    };

    public static void main(String[] args) throws IOException
    {
        String startMap = "D:/gtamminga/My Documents/03 Case The Hague NTM/TNO data";

        String fileArea = FileDialog.showFileDialog(true, "shp", "Shapefile with Areas", startMap);
        // File file = new File(fileName);
        String fileRoads = FileDialog.showFileDialog(true, "shp", "Shapefile with Roads", startMap);
        // "D:/gtamminga/My Documents/03 Case The Hague NTM/TNO data/TheHagueNetwork_Unidirectional_v2.shp";

        String filePathData =
                "D:/gtamminga/My Documents/03 Case The Hague NTM/TNO data/20141016 Oplevering/Oplevering o.b.v. VLOG data/di-do-2014/";
        String fileNameStarts = "I_";
        boolean fileNameDay = false;
        addArea(fileArea, fileRoads, filePathData, fileNameStarts, fileNameDay);

        filePathData =
                "D:/gtamminga/My Documents/03 Case The Hague NTM/TNO data/20141016 Oplevering/Oplevering o.b.v. NDW data_v2/";
        fileNameStarts = "I_";
        fileNameDay = true;
        addArea(fileArea, fileRoads, filePathData, fileNameStarts, fileNameDay);

        fileNameStarts = "VL_";
        fileNameDay = true;
        addArea(fileArea, fileRoads, filePathData, fileNameStarts, fileNameDay);
    }

    /**
     * @param fileArea
     * @param fileRoads
     * @param pathData
     * @param fileNameStarts
     * @param fileNameDay
     * @throws IOException
     */
    public static void addArea(String fileArea, String fileRoads, String pathData, String fileNameStarts,
            boolean fileNameDay) throws IOException
    {
        ShapeStore roads = null;
        // Map<String, Area> areas = new HashMap<String, Area>();
        ShapeStore areas = null;
        File file = new File(fileArea);
        areas = ShapeStore.openGISFile(file);

        // the specific ID we want to use, and the geometry (the_geom) of the road
        file = new File(fileRoads);
        roads = ShapeStore.openGISFile(file);
        // we are looking for a specific day!
        String year = "2014";
        String day = null;
        String month = null;
         for (int i = 1; i <= 12; i++)
//        for (int i = 5; i <= 5; i++)
        {
            for (int j = 1; j <= 31; j++)
            //for (int j = 29; j <= 29; j++)
            {
                if (i <= 10)
                {
                    month = "0" + i;
                }
                else
                {
                    month = Integer.toString(i);
                }

                if (j <= 10)
                {
                    day = "0" + j;
                }
                else
                {
                    day = Integer.toString(j);
                }
                if (fileNameDay)
                {
                    for (days dayName : days.values())
                    {
                        String inputFile =
                                pathData + fileNameStarts + year + month + day + "_" + dayName + "_GV[none].csv";
                        Map<String, ArrayList<Double>> countMap = readData(inputFile, ";", pathData, year, 5);
                        if (countMap.size() > 0)
                        {
                            String outputFile =
                                    pathData + "new/" + fileNameStarts + year + month + day + "_" + dayName
                                            + "_area_GV[none].csv";
                            String outputShapeFile =
                                    pathData + "new/" + fileNameStarts + year + month + day + "_" + dayName + ".shp";
                            detectLocationOfObject(outputShapeFile, outputFile, countMap, roads, areas, "LINK_ID",
                                    "Name");
                        }

                    }
                }
                else
                {
                    String inputFile = pathData + fileNameStarts + year + month + day + ".csv";
                    Map<String, ArrayList<Double>> countMap = readData(inputFile, ",", pathData, year, 5);
                    if (countMap.size() > 0)
                    {
                        String outputShapeFile = pathData + "new/" + fileNameStarts + year + month + day + ".shp";
                        String outputFile = pathData + "new/" + fileNameStarts + year + month + day + "_area.csv";
                        detectLocationOfObject(outputShapeFile, outputFile, countMap, roads, areas, "LINK_ID", "Name");
                    }
                }
            }
        }
    }

    /**
     * @param inputFile
     * @param csvSplitBy
     * @param path
     * @param year
     * @return values
     * @throws FileNotFoundException
     */
    public static Map<String, ArrayList<Double>> readData(String inputFile, String csvSplitBy, String path,
            String year, Integer aggregateBy) throws FileNotFoundException
    {
        Map<String, ArrayList<Double>> countMap = new HashMap<String, ArrayList<Double>>();
        BufferedReader in = null;
        String line = "";

        if (new File(inputFile).canRead())
        {
            File file = new File(inputFile);
            in = new BufferedReader(new FileReader(file));

            // read all lines: first column contains the name of the origin
            // this can be either a link or a centroid (starts with "C")
            boolean header = true;
            ArrayList<String> segment = new ArrayList<String>();
            try
            {
                while ((line = in.readLine()) != null)
                {
                    String[] completeLine = line.split(csvSplitBy);

                    // first line contains the column identifiers
                    if (header)
                    {
                        int columnNumber = 0;
                        for (String lineSegment : completeLine)
                        {
                            lineSegment = lineSegment.trim();
                            if (columnNumber > 0)
                            {
                                SimpleDateFormat sdf = new SimpleDateFormat("mm:ss", Locale.ENGLISH);
                                String time = lineSegment;
                                segment.add(time);
                            }
                            columnNumber++;
                        }
                        header = false;
                    }
                    else
                    {
                        int columnNumber = 0;
                        ArrayList<Double> counts = new ArrayList<Double>();
                        String id = null;
                        for (String lineSegment : completeLine)
                        {
                            lineSegment = lineSegment.trim();

                            if (columnNumber > 0)
                            {
                                String counted = lineSegment;
                                counts.add(Double.parseDouble(counted));
                            }
                            else if (columnNumber == 0)
                            {
                                id = lineSegment;
                            }
                            columnNumber++;
                        }

                        ArrayList<Double> aggregatedCounts = new ArrayList<Double>();
                        if (aggregateBy > 1)
                        {
                            int i = 1;
                            double aggregatedCount = 0.0;
                            for (Double count : counts)
                            {
                                aggregatedCount += count;
                                i++;
                                if (i == aggregateBy)
                                {
                                    aggregatedCounts.add(aggregatedCount);
                                    i = 1;
                                    aggregatedCount = 0.0;
                                }
                            }
                        }
                        else
                        {
                            aggregatedCounts = counts;
                        }

                        countMap.put(id, aggregatedCounts);
                    }
                }
                in.close();
            }
            catch (NumberFormatException exception)
            {
                exception.printStackTrace();
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }
        }

        return countMap;
    }

    /**
     * @param outputFile
     * @param outputFile
     * @param countMap
     * @param objectsToDetect
     * @param searchLocations
     * @param fieldNameToDetect
     * @param fieldNameSearchAreas
     * @throws IOException
     */

    public static void detectLocationOfObject(String outputShapeFile, String outputFile,
            Map<String, ArrayList<Double>> countMap, ShapeStore objectsToDetect, ShapeStore searchLocations,
            String fieldNameToDetect, String fieldNameSearchAreas) throws IOException
    {
        File fileNew = new File(outputFile);
        BufferedWriter out = null;
        // if file doesn't exists, then create it...
        if (fileNew.exists())
        {
            fileNew.createNewFile();
        }

        out = new BufferedWriter(new FileWriter(fileNew));
        // add data to the point/line (road) file of attributes within a polygon
        objectsToDetect.addAttribute("Count", "Double");
        int indexAttributeAdded0 = objectsToDetect.getVariableNames().size() - 1;

        // add data to the area (polygon) file
        searchLocations.addAttribute("Counted", "Integer");
        int indexAttributeAdded1 = searchLocations.getVariableNames().size() - 1;
        searchLocations.addAttribute("CountedIDs", "String");
        int indexAttributeAdded2 = indexAttributeAdded1 + 1;

        // we are looking for roads with a specific ID that is included in the data from TNO
        // step 1: create a HashMap to find the geometry of a road with a specific ID
        // step 2: find the corresponding Area
        HashMap<String, ShapeObject> mapRoads = new HashMap<String, ShapeObject>();
        int indexFieldNameToDetect = -1;

        for (String name : objectsToDetect.getVariableNames())
        {
            if (name.equals(fieldNameToDetect))
            {
                indexFieldNameToDetect = objectsToDetect.getVariableNames().indexOf(name);
                break;
            }
        }
        for (ShapeObject road : objectsToDetect.getGeoObjects())
        {
            mapRoads.put(road.getValues().get(indexFieldNameToDetect), road);
        }

        // write the data with the corresponding area ID to a new file
        Iterator it = countMap.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry countIdValue = (Map.Entry) it.next();
            Geometry geomToDetect = null;
            if (mapRoads.get(countIdValue.getKey()) != null)
            {
                geomToDetect = mapRoads.get(countIdValue.getKey()).getGeometry();
            }
            ShapeObject area = null;
            if (geomToDetect != null)
            {
                area = findObjectInPolygon(geomToDetect, searchLocations);
            }

            String text = "";
            String id = (String) countIdValue.getKey();
            if (area != null)
            {
                Integer counted;
                if (area.getValues().get(indexAttributeAdded1).equals("NaN"))
                {
                    counted = -999;
                }
                else if (area.getValues().get(indexAttributeAdded1).equals(" "))
                {
                    counted = -999;
                }
                else
                {
                    counted = Integer.parseInt(area.getValues().get(indexAttributeAdded1));
                }
                area.getValues().set(indexAttributeAdded1, Integer.toString(counted + 1));
//                String ids = area.getValues().get(indexAttributeAdded2);
//                area.getValues().set(indexAttributeAdded2, ids + id);
                text = id + ", " + area.getValues().get(0);
            }
            else
            {
                text = id + " no area found";
            }
            ArrayList<Double> counts = (ArrayList<Double>) countIdValue.getValue();
            for (Double count : counts)
            {
                text += ", " + count;
            }
            text += " \n";
            out.write(text);

            // extend the shape of roads with count data
            if (mapRoads.get(countIdValue.getKey()) != null)
            {
                mapRoads.get(countIdValue.getKey()).getValues()
                        .set(indexAttributeAdded0, Double.toString(counts.get(0)));
            }
            it.remove(); // avoids a ConcurrentModificationException
        }

/*        if (new File(outputShapeFile).isAbsolute())
        {
            File file = new File(outputShapeFile);
            ShapeStore.createShapeFile(searchLocations, file);
        }
        if (new File(outputShapeFile).isAbsolute())
        {
            File file = new File(outputShapeFile);
            ShapeStore.createShapeFile(objectsToDetect, file);
        }*/
        out.close();
    }

    /**
     * @param p the point to search.
     * @return the area that contains point p, or null if not found.
     */
    private static ShapeObject findObjectInPolygon(final Geometry geom, ShapeStore areas)
    {
        ShapeObject area = null;
        for (ShapeObject a : areas.getGeoObjects())
        {
            // could also be contains....
            if (a.getGeometry().intersects(geom))
            {
                area = a;
                break;
            }
        }
        return area;
    }
}
