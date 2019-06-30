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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.opentrafficsim.demo.ntm.shapeobjects.ShapeObject;
import org.opentrafficsim.demo.ntm.shapeobjects.ShapeStore;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 3 Nov 2014 <br>
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
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate$, @version $Revision$, by $Author$,
     * initial version 14 Nov 2014 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
     * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
     */
    public enum days
    {
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

        // "D:/gtamminga/My Documents/03 Case The Hague NTM/TNO data/TheHagueNetwork_Unidirectional_v2.shp";

        boolean dataTNO = false;

        if (dataTNO)
        {
            String startMap = "D:/gtamminga/My Documents/03 Case The Hague NTM/TNO data";

            String fileArea = FileDialog.showFileDialog(true, "shp", "Shapefile with Areas", startMap);
            // File file = new File(fileName);
            String fileRoads = FileDialog.showFileDialog(true, "shp", "Shapefile with Roads", startMap);
            String filePathData =
                    "D:/gtamminga/My Documents/03 Case The Hague NTM/TNO data/20141016 Oplevering/Oplevering o.b.v. VLOG data/di-do-2014/";
            String fileNameStarts = "I_";
            boolean fileNameDay = false;
            addArea(fileArea, fileRoads, filePathData, fileNameStarts, fileNameDay, dataTNO);

            filePathData =
                    "D:/gtamminga/My Documents/03 Case The Hague NTM/TNO data/20141016 Oplevering/Oplevering o.b.v. NDW data_v2/";
            fileNameStarts = "I_";
            fileNameDay = true;
            addArea(fileArea, fileRoads, filePathData, fileNameStarts, fileNameDay, dataTNO);

            fileNameStarts = "VL_";
            fileNameDay = true;
            addArea(fileArea, fileRoads, filePathData, fileNameStarts, fileNameDay, dataTNO);
        }
        else
        {
            String startMap = "D:/gtamminga/workspace/ots-ntm/src/main/resources/gis/debug_fork_links";
            String fileArea = FileDialog.showFileDialog(true, "shp", "Shapefile with Areas", startMap);
            // File file = new File(fileName);
            String fileRoads = FileDialog.showFileDialog(true, "shp", "Shapefile with Roads", startMap);
            String filePathData = "D:/gtamminga/workspace/ots-ntm/src/main/resources/gis/debug_fork_links/";
            String fileNameStarts = "I_";
            boolean fileNameDay = false;
            addGeo(fileArea, fileRoads, filePathData, fileNameStarts, fileNameDay, dataTNO);
        }
    }

    /**
     * @param fileArea String;
     * @param fileRoads String;
     * @param pathData String;
     * @param fileNameStarts String;
     * @param fileNameDay boolean;
     * @throws IOException
     */
    public static void addArea(String fileArea, String fileRoads, String pathData, String fileNameStarts, boolean fileNameDay,
            boolean dataTNO) throws IOException
    {
        ShapeStore roads = null;
        // Map<String, Area> areas = new LinkedHashMap<String, Area>();
        ShapeStore areas = null;
        File file = new File(fileArea);
        areas = ShapeStore.openGISFile(file);

        // the specific ID we want to use, and the geometry (the_geom) of the road
        file = new File(fileRoads);
        roads = ShapeStore.openGISFile(file);

        if (dataTNO)
        {
            // we are looking for a specific day!
            String year = "2014";
            String day = null;
            String month = null;
            for (int i = 1; i <= 12; i++)
            // for (int i = 5; i <= 5; i++)
            {
                for (int j = 1; j <= 31; j++)
                // for (int j = 29; j <= 29; j++)
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
                    String centroidOutputFile = pathData + "centroids.csv";

                    if (fileNameDay)
                    {
                        for (days dayName : days.values())
                        {
                            String inputFile = pathData + fileNameStarts + year + month + day + "_" + dayName + "_GV[none].csv";
                            Map<String, ArrayList<Double>> countMap = readData(inputFile, ";", pathData, year, 1);
                            if (countMap.size() > 0)
                            {
                                String outputFile = pathData + "new/" + fileNameStarts + year + month + day + "_" + dayName
                                        + "_area_GV[none].csv";
                                String outputShapeFile =
                                        pathData + "new/" + fileNameStarts + year + month + day + "_" + dayName + ".shp";
                                detectLocationOfObject(outputShapeFile, null, outputFile, countMap, roads, areas, "LINK_ID",
                                        "Name");
                            }

                        }
                    }
                    else
                    {
                        String inputFile = pathData + fileNameStarts + year + month + day + ".csv";
                        Map<String, ArrayList<Double>> countMap = readData(inputFile, ",", pathData, year, 1);
                        if (countMap.size() > 0)
                        {
                            String outputShapeFile = pathData + "new/" + fileNameStarts + year + month + day + ".shp";
                            String outputFile = pathData + "new/" + fileNameStarts + year + month + day + "_area.csv";
                            detectLocationOfObject(outputShapeFile, centroidOutputFile, outputFile, countMap, roads, areas,
                                    "LINK_ID", "Name");
                        }
                    }
                }
            }
        }

    }

    /**
     * @param fileArea String;
     * @param fileRoads String;
     * @param pathData String;
     * @param fileNameStarts String;
     * @param fileNameDay boolean;
     * @throws IOException
     */
    public static void addGeo(String fileArea, String fileRoads, String pathData, String fileNameStarts, boolean fileNameDay,
            boolean dataTNO) throws IOException
    {
        ShapeStore roads = null;
        // Map<String, Area> areas = new LinkedHashMap<String, Area>();
        ShapeStore areas = null;
        File file = new File(fileArea);
        areas = ShapeStore.openGISFile(file);

        // the specific ID we want to use, and the geometry (the_geom) of the road
        file = new File(fileRoads);
        roads = ShapeStore.openGISFile(file);
        String centroidOutputFile = pathData + "centroids.csv";
        // String inputFile = pathData + fileNameStarts + year + month + day + ".csv";
        // Map<String, ArrayList<Double>> countMap = readData(inputFile, ",", pathData, year, 1);
        // if (countMap.size() > 0)
        // {
        // String outputShapeFile = pathData + "new/" + fileNameStarts + year + month + day + ".shp";
        String outputFile = pathData + "geo/" + "AreaGeo.csv";
        detectLocationOfObjectGeo(null, centroidOutputFile, outputFile, null, roads, areas, "LINK_ID", "Name");
        // }
    }

    /**
     * @param outputShapeFile String;
     * @param centroidOutputFile String;
     * @param outputFile String;
     * @param countMap Map&lt;String,ArrayList&lt;Double&gt;&gt;;
     * @param roads ShapeStore;
     * @param areas ShapeStore;
     * @param fieldNameToDetect String;
     * @param fieldNameSearchAreas String;
     * @throws IOException
     */
    public static void detectLocationOfObjectGeo(String outputShapeFile, String centroidOutputFile, String outputFile,
            Map<String, ArrayList<Double>> countMap, ShapeStore roads, ShapeStore areas, String fieldNameToDetect,
            String fieldNameSearchAreas) throws IOException
    {
        File fileNew = new File(outputFile);
        BufferedWriter out = null;
        // if file doesn't exists, then create it...
        if (fileNew.exists())
        {
            fileNew.createNewFile();
        }

        if (centroidOutputFile != null)
        {
            fileNew = new File(centroidOutputFile);
            out = null;
            // if file doesn't exists, then create it...
            if (fileNew.exists())
            {
                fileNew.createNewFile();
            }

            out = new BufferedWriter(new FileWriter(fileNew));
            for (ShapeObject point : areas.getGeoObjects())
            {
                Point centroid = point.getDesignLine().getCentroid();
                Coordinate[] coords = new Coordinate[999];
                // String text = point.getValues().get(0) + " ," + centroid.getCoordinate().x + " ," +
                // centroid.getCoordinate().y ;
                Geometry newArea = point.getDesignLine().buffer(0.0);
                String text = "Area" + point.getValues().get(0) + " ,";
                coords = point.getDesignLine().getCoordinates();
                Coordinate[] coords2 = newArea.getCoordinates();
                for (Coordinate coord : coords)
                {
                    text += coord.x + " ," + coord.y + " ,";
                }
                text = text.substring(0, text.length() - 4);
                text += " \n";
                out.write(text);

            }
            for (ShapeObject point : roads.getGeoObjects())
            {
                Point centroid = point.getDesignLine().getCentroid();
                Coordinate[] coords = new Coordinate[999];
                // String text = point.getValues().get(0) + " ," + centroid.getCoordinate().x + " ," +
                // centroid.getCoordinate().y ;
                Geometry newArea = point.getDesignLine().buffer(0.0);
                String text = "Road " + point.getValues().get(2) + " ,";
                coords = point.getDesignLine().getCoordinates();
                Coordinate[] coords2 = newArea.getCoordinates();
                for (Coordinate coord : coords)
                {
                    text += coord.x + " ," + coord.y + " ,";
                }
                text = text.substring(0, text.length() - 4);
                text += " \n";
                out.write(text);

            }

            out.close();
            /*
             * out = new BufferedWriter(new FileWriter(fileNew)); // add data to the point/line (road) file of attributes within
             * a polygon int indexAttributeAdded0 = objectsToDetect.getVariableNames().size() - 1; // add data to the area
             * (polygon) file int indexAttributeAdded1 = searchLocations.getVariableNames().size() - 1; // we are looking for
             * roads with a specific ID that is included in the data from TNO // step 1: create a LinkedHashMap to find the geometry
             * of a road with a specific ID // step 2: find the corresponding Area LinkedHashMap<String, ShapeObject> mapRoads = new
             * LinkedHashMap<String, ShapeObject>(); LinkedHashMap<String, String> mapRoadLengths = new LinkedHashMap<String, String>();
             * LinkedHashMap<String, String> mapRoadCTM = new LinkedHashMap<String, String>(); LinkedHashMap<String, String> mapRoadSTT_NAAM = new
             * LinkedHashMap<String, String>(); int indexFieldNameToDetect = -1; for (String name :
             * objectsToDetect.getVariableNames()) { if (name.equals(fieldNameToDetect)) // "LINK_ID" { indexFieldNameToDetect =
             * objectsToDetect.getVariableNames().indexOf(name); break; } } for (ShapeObject road :
             * objectsToDetect.getGeoObjects()) { mapRoads.put(road.getValues().get(indexFieldNameToDetect), road); } int
             * indexFieldNameLength = -1;
             *//*
                * for (String name : objectsToDetect.getVariableNames()) { if (name.equals("LENGTH")) { indexFieldNameLength =
                * objectsToDetect.getVariableNames().indexOf(name); break; } } int indexFieldNameWegbeheerder = -1; for (String
                * name : objectsToDetect.getVariableNames()) { if (name.equals("WEGDEELLTR")) { indexFieldNameWegbeheerder =
                * objectsToDetect.getVariableNames().indexOf(name); break; } } int indexFieldNameSTT_NAAM = -1; for (String name
                * : objectsToDetect.getVariableNames()) { if (name.equals("STT_NAAM")) { indexFieldNameSTT_NAAM =
                * objectsToDetect.getVariableNames().indexOf(name); break; } } for (ShapeObject road :
                * objectsToDetect.getGeoObjects()) { mapRoadLengths .put(road.getValues().get(indexFieldNameToDetect),
                * road.getValues().get(indexFieldNameLength)); mapRoadCTM.put(road.getValues().get(indexFieldNameToDetect),
                * road.getValues().get(indexFieldNameWegbeheerder));
                * mapRoadSTT_NAAM.put(road.getValues().get(indexFieldNameToDetect),
                * road.getValues().get(indexFieldNameSTT_NAAM)); }
                */

            // write the data with the corresponding area ID to a new file
            /*
             * Iterator it = countMap.entrySet().iterator(); while (it.hasNext()) { Map.Entry countIdValue = (Map.Entry)
             * it.next(); Geometry geomToDetect = null; Double length = null; String CTM = null; String STT_NAAM = null;
             * Coordinate coordMiddle = null; if (mapRoads.get(countIdValue.getKey()) != null) { geomToDetect =
             * mapRoads.get(countIdValue.getKey()).getDesignLine(); if (geomToDetect != null) { int numberOfCoords =
             * geomToDetect.getCoordinates().length; Coordinate coordA = geomToDetect.getCoordinates()[0]; Coordinate coordB =
             * geomToDetect.getCoordinates()[numberOfCoords - 1]; double x = coordA.x + 0.5 * (coordB.x - coordA.x); double y =
             * coordA.y + 0.5 * (coordB.y - coordA.y); coordMiddle = new Coordinate(x, y, 0); } length =
             * Double.parseDouble(mapRoadLengths.get(countIdValue.getKey())); CTM = mapRoadCTM.get(countIdValue.getKey());
             * STT_NAAM = mapRoadSTT_NAAM.get(countIdValue.getKey()); } else { coordMiddle = new Coordinate(-99, -99, 0); } if
             * (length == null) { length = Double.NaN; } ShapeObject area = null; if (geomToDetect != null) { area =
             * findObjectInPolygon(geomToDetect, searchLocations); } String text = ""; String id = (String)
             * countIdValue.getKey(); String coords = coordMiddle.x + ", " + coordMiddle.y + " "; if (area != null) { Integer
             * counted; if (area.getValues().get(indexAttributeAdded1).equals("NaN")) { counted = -999; } else if
             * (area.getValues().get(indexAttributeAdded1).equals(" ")) { counted = -999; } else { counted =
             * Integer.parseInt(area.getValues().get(indexAttributeAdded1)); } area.getValues().set(indexAttributeAdded1,
             * Integer.toString(counted + 1)); // String ids = area.getValues().get(indexAttributeAdded2); //
             * area.getValues().set(indexAttributeAdded2, ids + id); if (CTM.equals("SW") || CTM.equals("R") ||
             * STT_NAAM.equals("Hubertustunnel")) { text = id + ", " + "Stroomweg" + ", " + length + ", " + coords; } else {
             * text = id + ", " + area.getValues().get(0) + ", " + length + ", " + coords; } } else { text = id +
             * ", no area found" + ", " + length + ", " + coords; } ArrayList<Double> counts = (ArrayList<Double>)
             * countIdValue.getValue(); for (Double count : counts) { text += ", " + count; } text += " \n"; out.write(text); //
             * extend the shape of roads with count data if (mapRoads.get(countIdValue.getKey()) != null) {
             * mapRoads.get(countIdValue.getKey()).getValues() .set(indexAttributeAdded0, Double.toString(counts.get(0))); }
             * it.remove(); // avoids a ConcurrentModificationException } if (new File(outputShapeFile).isAbsolute()) { File
             * file = new File(outputShapeFile); ShapeStore.createShapeFile(searchLocations, file); } if (new
             * File(outputShapeFile).isAbsolute()) { File file = new File(outputShapeFile);
             * ShapeStore.createShapeFile(objectsToDetect, file); } out.close();
             */

        }
    }

    /**
     * @param inputFile String;
     * @param csvSplitBy String;
     * @param path String;
     * @param year String;
     * @return values
     * @throws FileNotFoundException
     */
    public static Map<String, ArrayList<Double>> readData(String inputFile, String csvSplitBy, String path, String year,
            Integer aggregateBy) throws FileNotFoundException
    {
        Map<String, ArrayList<Double>> countMap = new LinkedHashMap<String, ArrayList<Double>>();
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
                            int i = 0;
                            double aggregatedCount = 0.0;
                            for (Double count : counts)
                            {
                                aggregatedCount += count;
                                i++;
                                if (i == aggregateBy)
                                {
                                    aggregatedCounts.add(aggregatedCount);
                                    i = 0;
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

    public static void detectLocationOfObject(String outputShapeFile, String centroidOutputFile, String outputFile,
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

        // we are looking for roads with a specific ID that is included in the data from TNO
        // step 1: create a LinkedHashMap to find the geometry of a road with a specific ID
        // step 2: find the corresponding Area
        LinkedHashMap<String, ShapeObject> mapRoads = new LinkedHashMap<String, ShapeObject>();
        LinkedHashMap<String, String> mapRoadLengths = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> mapRoadCTM = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> mapRoadSTT_NAAM = new LinkedHashMap<String, String>();
        int indexFieldNameToDetect = -1;

        for (String name : objectsToDetect.getVariableNames())
        {
            if (name.equals(fieldNameToDetect)) // "LINK_ID"
            {
                indexFieldNameToDetect = objectsToDetect.getVariableNames().indexOf(name);
                break;
            }
        }

        for (ShapeObject road : objectsToDetect.getGeoObjects())
        {
            mapRoads.put(road.getValues().get(indexFieldNameToDetect), road);
        }

        int indexFieldNameLength = -1;

        for (String name : objectsToDetect.getVariableNames())
        {
            if (name.equals("LENGTH"))
            {
                indexFieldNameLength = objectsToDetect.getVariableNames().indexOf(name);
                break;
            }
        }

        int indexFieldNameWegbeheerder = -1;

        for (String name : objectsToDetect.getVariableNames())
        {
            if (name.equals("WEGDEELLTR"))
            {
                indexFieldNameWegbeheerder = objectsToDetect.getVariableNames().indexOf(name);
                break;
            }
        }

        int indexFieldNameSTT_NAAM = -1;

        for (String name : objectsToDetect.getVariableNames())
        {
            if (name.equals("STT_NAAM"))
            {
                indexFieldNameSTT_NAAM = objectsToDetect.getVariableNames().indexOf(name);
                break;
            }
        }

        for (ShapeObject road : objectsToDetect.getGeoObjects())
        {
            mapRoadLengths.put(road.getValues().get(indexFieldNameToDetect), road.getValues().get(indexFieldNameLength));
            mapRoadCTM.put(road.getValues().get(indexFieldNameToDetect), road.getValues().get(indexFieldNameWegbeheerder));
            mapRoadSTT_NAAM.put(road.getValues().get(indexFieldNameToDetect), road.getValues().get(indexFieldNameSTT_NAAM));
        }

        // write the data with the corresponding area ID to a new file
        Iterator it = countMap.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry countIdValue = (Map.Entry) it.next();
            Geometry geomToDetect = null;
            Double length = null;
            String CTM = null;
            String STT_NAAM = null;
            Coordinate coordMiddle = null;
            if (mapRoads.get(countIdValue.getKey()) != null)
            {
                geomToDetect = mapRoads.get(countIdValue.getKey()).getDesignLine();
                if (geomToDetect != null)
                {
                    int numberOfCoords = geomToDetect.getCoordinates().length;
                    Coordinate coordA = geomToDetect.getCoordinates()[0];
                    Coordinate coordB = geomToDetect.getCoordinates()[numberOfCoords - 1];
                    double x = coordA.x + 0.5 * (coordB.x - coordA.x);
                    double y = coordA.y + 0.5 * (coordB.y - coordA.y);
                    coordMiddle = new Coordinate(x, y, 0);
                }
                length = Double.parseDouble(mapRoadLengths.get(countIdValue.getKey()));
                CTM = mapRoadCTM.get(countIdValue.getKey());
                STT_NAAM = mapRoadSTT_NAAM.get(countIdValue.getKey());
            }
            else
            {
                coordMiddle = new Coordinate(-99, -99, 0);
            }

            if (length == null)
            {
                length = Double.NaN;
            }
            ShapeObject area = null;
            if (geomToDetect != null)
            {
                area = findObjectInPolygon(geomToDetect, searchLocations);
            }

            String text = "";
            String id = (String) countIdValue.getKey();
            String coords = coordMiddle.x + ", " + coordMiddle.y + " ";
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
                // String ids = area.getValues().get(indexAttributeAdded2);
                // area.getValues().set(indexAttributeAdded2, ids + id);

                if (CTM.equals("SW") || CTM.equals("R") || STT_NAAM.equals("Hubertustunnel"))
                {
                    text = id + ", " + "Stroomweg" + ", " + length + ", " + coords;
                }
                else
                {
                    text = id + ", " + area.getValues().get(0) + ", " + length + ", " + coords;
                }
            }
            else
            {
                text = id + ", no area found" + ", " + length + ", " + coords;
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
                mapRoads.get(countIdValue.getKey()).getValues().set(indexAttributeAdded0, Double.toString(counts.get(0)));
            }
            it.remove(); // avoids a ConcurrentModificationException
        }

        /*
         * if (new File(outputShapeFile).isAbsolute()) { File file = new File(outputShapeFile);
         * ShapeStore.createShapeFile(searchLocations, file); } if (new File(outputShapeFile).isAbsolute()) { File file = new
         * File(outputShapeFile); ShapeStore.createShapeFile(objectsToDetect, file); }
         */
        out.close();

        if (centroidOutputFile != null)
        {
            fileNew = new File(centroidOutputFile);
            out = null;
            // if file doesn't exists, then create it...
            if (fileNew.exists())
            {
                fileNew.createNewFile();
            }

            out = new BufferedWriter(new FileWriter(fileNew));
            for (ShapeObject point : searchLocations.getGeoObjects())
            {
                Point centroid = point.getDesignLine().getCentroid();
                Coordinate[] coords = new Coordinate[999];
                // String text = point.getValues().get(0) + " ," + centroid.getCoordinate().x + " ," +
                // centroid.getCoordinate().y ;
                Geometry newArea = point.getDesignLine().buffer(0.0);
                String text = point.getValues().get(0) + " ,";
                coords = point.getDesignLine().getCoordinates();
                Coordinate[] coords2 = newArea.getCoordinates();
                for (Coordinate coord : coords)
                {
                    text += coord.x + " ," + coord.y + " ,";
                }
                text = text.substring(0, text.length() - 4);
                text += " \n";
                out.write(text);

            }
            out.close();
        }
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
            if (a.getDesignLine().intersects(geom))
            {
                area = a;
                break;
            }
        }
        return area;
    }
}
