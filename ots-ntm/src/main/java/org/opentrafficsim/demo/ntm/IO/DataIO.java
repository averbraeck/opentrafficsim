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
        try
        {
            File file = new File(fileArea);
            areas = ShapeStore.openGISFile(file);
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
        try
        {
            // the specific ID we want to use, and the geometry (the_geom) of the road
            File file = new File(fileRoads);
            roads = ShapeStore.openGISFile(file);
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }

        // we are looking for a specific day!
        String year = "2014";
        String day = null;
        String month = null;
        // for (int i = 1; i <= 12; i++)
        for (int i = 5; i <= 5; i++)
        {
            // for (int j = 1; j <= 31; j++)
            for (int j = 29; j <= 29; j++)
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
                        Map<String, ArrayList<Double>> countMap = readData(inputFile, ";", pathData, year);
                        if (countMap.size() > 0)
                        {
                            String outputFile =
                                    pathData + "new/" + fileNameStarts + year + month + day + "_" + dayName
                                            + "_area_GV[none].csv";
                            detectLocationOfObject(outputFile, countMap, roads, areas, "LINK_ID", "Name");
                        }

                    }
                }
                else
                {
                    String inputFile = pathData + fileNameStarts + year + month + day + ".csv";
                    Map<String, ArrayList<Double>> countMap = readData(inputFile, ",", pathData, year);
                    if (countMap.size() > 0)
                    {
                        String outputFile = pathData + "new/" + fileNameStarts + year + month + day + "_area.csv";
                        detectLocationOfObject(outputFile, countMap, roads, areas, "LINK_ID", "Name");
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
    public static Map<String, ArrayList<Double>> readData(String inputFile, String csvSplitBy, String path, String year)
            throws FileNotFoundException
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
                        ArrayList<Double> count = new ArrayList<Double>();
                        String iD = null;
                        for (String lineSegment : completeLine)
                        {
                            lineSegment = lineSegment.trim();

                            if (columnNumber > 0)
                            {
                                String counted = lineSegment;
                                count.add(Double.parseDouble(counted));
                            }
                            else if (columnNumber == 0)
                            {
                                iD = lineSegment;
                            }
                            columnNumber++;
                        }
                        countMap.put(iD, count);
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
     * @param countMap
     * @param objectsToDetect
     * @param searchLocations
     * @param fieldNameToDetect
     * @param fieldNameSearchAreas
     * @throws IOException
     */
    public static void detectLocationOfObject(String outputFile, Map<String, ArrayList<Double>> countMap, ShapeStore objectsToDetect,
            ShapeStore searchLocations, String fieldNameToDetect, String fieldNameSearchAreas) throws IOException
    {
        File fileNew = new File(outputFile);
        BufferedWriter out = null;
        // if file doesnt exists, then create it
        if (fileNew.exists())
        {
            fileNew.createNewFile();
        }

        out = new BufferedWriter(new FileWriter(fileNew));
        String newField = "Count";
        objectsToDetect.getVariableTypeMap().put("Count", "Double");
        objectsToDetect.getVariableNames().add(newField);
        objectsToDetect.getAttributeClassTypes().put(newField, Double.class);
        // we are looking for roads with a specific ID that is included in the data from TNO
        // step 1: create a HashMap to find the geometry of a road with a specific ID
        // step 2: find the corresponding Area

        HashMap<String, ShapeObject> mapRoads = new HashMap<String, ShapeObject>();
        int indexName = -1;
        for (String name : objectsToDetect.getVariableNames())
        {
            if (name.equals(fieldNameToDetect))
            {
                indexName = objectsToDetect.getVariableNames().indexOf(name);
                break;
            }
        }
        for (ShapeObject road : objectsToDetect.getGeoObjects())
        {
            mapRoads.put(road.getValues().get(indexName), road);
        }
        
        // write the data with the corresponding area ID to a new file
        // Iterator it = roads.entrySet().iterator();
        Iterator it = countMap.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry pairs = (Map.Entry) it.next();
            // Geometry geom = (Geometry) pairs.getValue();
            Geometry geom = null;
            if (mapRoads.get(pairs.getKey()) !=null)
            {
                geom = mapRoads.get(pairs.getKey()).getGeometry();
            }
            ShapeObject area = null;
            if (geom != null)
            {
                area = findObjectInPolygon(geom, searchLocations);
            }

            for (String name : searchLocations.getVariableNames())
            {
                if (name.equals(fieldNameSearchAreas))
                {
                    indexName = searchLocations.getVariableNames().indexOf(name);
                }
            }
            String text = "";
            String id = (String) pairs.getKey();
            id = id.substring(0, id.length() - 1);
            if (area != null)
            {
                text = id + ", " + area.getValues().get(indexName);
            }
            else
            {
                text = id + " no area found";
            }
            ArrayList<Double> counts = (ArrayList<Double>) pairs.getValue();
            for (Double count : counts)
            {
                text += ", " + count;
            }
            text += " \n";
            out.write(text);
            
            // extend the shape of roads with count data
            
            ShapeObject shape = mapRoads.get(pairs.getKey());
            if (shape != null)
            {
                shape.getValues().add(counts.get(0).toString());
            }
            it.remove(); // avoids a ConcurrentModificationException
        }
        String startMap = "D:/gtamminga/My Documents/03 Case The Hague NTM/TNO data";
        ShapeStore.createShapeFile(objectsToDetect, startMap);
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
