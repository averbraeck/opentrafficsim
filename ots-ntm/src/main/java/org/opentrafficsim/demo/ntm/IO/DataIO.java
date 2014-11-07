package org.opentrafficsim.demo.ntm.IO;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.opentrafficsim.demo.ntm.Area;
import org.opentrafficsim.demo.ntm.BuildGraph;
import org.opentrafficsim.demo.ntm.Node;
import org.opentrafficsim.demo.ntm.ShapeFileReader;

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
    public enum days {
        Sat,
        Sun,
        Mon,
        Tue,
        Wed,
        Thu,
        Fri
    };

    public static void main(String[] args)
    {

        String pathArea = "/gis/select.shp";
//        String pathArea = "/gis/areas.shp";
        String pathRoads =
                "D:/gtamminga/My Documents/03 Case The Hague NTM/TNO data/TheHagueNetwork_Unidirectional_v2.shp";
        String pathData =
                "D:/gtamminga/My Documents/03 Case The Hague NTM/TNO data/20141016 Oplevering/Oplevering o.b.v. VLOG data/di-do-2014/";
        String fileNameStarts = "I_";
        boolean fileNameDay = false;
        addArea(pathArea, pathRoads, pathData, fileNameStarts, fileNameDay);
        pathData =
                "D:/gtamminga/My Documents/03 Case The Hague NTM/TNO data/20141016 Oplevering/Oplevering o.b.v. NDW data_v2/";
        fileNameStarts = "I_";
        fileNameDay = true;
        addArea(pathArea, pathRoads, pathData, fileNameStarts, fileNameDay);
        fileNameStarts = "VL_";
        fileNameDay = true;
        addArea(pathArea, pathRoads, pathData, fileNameStarts, fileNameDay);
    }

    public static void addArea(String pathArea, String pathRoads, String pathData, String fileNameStarts,
            boolean fileNameDay)
    {
        Map<String, Geometry> roads = new HashMap<String, Geometry>();
        Map<String, Area> areas = new HashMap<String, Area>();
        try
        {
            areas = readAreas(pathArea);
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
        try
        {
            // the specific ID we want to use, and the geometry (the_geom) of the road
            roads = readTheHague(pathRoads, "LINK_ID");
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }

        String year = "2014";
        String day = null;
        String month = null;
//        for (int i = 1; i <= 12; i++)
        for (int i = 5; i <= 5; i++)
        {
//            for (int j = 1; j <= 31; j++)
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
                        String inputFile = pathData + fileNameStarts + year + month + day + "_" + dayName + "_GV[none].csv";
                        Map<String, ArrayList<Double>> countMap = readData(inputFile, ";", pathData, year);
                        if (countMap.size() > 0)
                        {
                            String outputFile =
                                    pathData + "new/" + fileNameStarts + year + month + day + "_" + dayName
                                            + "_area_GV[none].csv";
                            writeToCsv(outputFile, countMap, roads, areas);
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
                        writeToCsv(outputFile, countMap, roads, areas);
                    }

                }
            }
        }
    }

    public static Map<String, ArrayList<Double>> readData(String inputFile, String csvSplitBy, String path, String year)
    {
        Map<String, ArrayList<Double>> countMap = new HashMap<String, ArrayList<Double>>();
        BufferedReader in = null;
        String line = "";

        if (new File(inputFile).canRead())
        {
            File file = new File(inputFile);
            try
            {
                in = new BufferedReader(new FileReader(file));
            }
            catch (FileNotFoundException exception1)
            {
                exception1.printStackTrace();
            }
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
                        String ID = null;
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
                                ID = lineSegment;
                            }
                            columnNumber++;
                        }
                        countMap.put(ID, count);
                    }
                }
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

    public static void writeToCsv(String outputFile, Map<String, ArrayList<Double>> countMap,
            Map<String, Geometry> roads, Map<String, Area> areas)
    {
        File fileNew = new File(outputFile);
        BufferedWriter out = null;
        // if file doesnt exists, then create it
        if (fileNew.exists())
        {
            {
                try
                {
                    fileNew.createNewFile();
                }
                catch (IOException exception)
                {
                    exception.printStackTrace();
                }
            }
        }

        try
        {
            out = new BufferedWriter(new FileWriter(fileNew));
        }
        catch (IOException exception1)
        {
            exception1.printStackTrace();
        }

        // write the data with the corresponding area ID to a new file
        // Iterator it = roads.entrySet().iterator();
        int u = 1;
        Iterator it = countMap.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry pairs = (Map.Entry) it.next();
            // Geometry geom = (Geometry) pairs.getValue();
            Geometry geom = roads.get(pairs.getKey());
            Area area = null;
            if (geom != null)
            {
                area = findRoadInArea(geom, areas);
            }
            else
            {
                u++;
            }

            String text = "";
            String id = (String) pairs.getKey();
            // watch out: convert from LinkID to WVKID!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            id = id.substring(0, id.length() - 1);
            if (area != null)
            {
                text =
                // pairs.getKey() + ", " + " \n";
//                        id + ", " + area.getCentroidNr();
                        id + ", " + area.getName();
            }
            else
            {
                text =
                // pairs.getKey() + ", " + " \n";
                        id + " no area found";
            }
            // ArrayList<Double> counts = countMap.get(pairs.getKey());
            ArrayList<Double> counts = (ArrayList<Double>) pairs.getValue();
            for (Double count : counts)
            {
                text += ", " + count;
            }
            text += " \n";
            try
            {
                out.write(text);
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }
            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    /**
     * @param shapeFileName the areas shapefile to read
     * @param centroids the map of centroids
     * @return map of areas with areanr as the key
     * @throws IOException on error
     */
    public static Map<String, Geometry> readTheHague(final String shapeFileName, String Id) throws IOException
    {
        /*-
        the_geom class com.vividsolutions.jts.geom.MultiPolygon MULTIPOLYGON (((81816.4228569232, ...
        AREANR class java.lang.Long 15127
        NAME class java.lang.String 70 Oostduinen
        CENTROIDNR class java.lang.Long 1
        NAMENR class java.lang.Long 175
        GEMEENTE_N class java.lang.String S Gravenhage
        GEMEENTEVM class java.lang.String sGravenhage
        GEBIEDSNAA class java.lang.String Studiegebied
        REGIO class java.lang.String Den_Haag
        MATCOMPRES class java.lang.String Scheveningen
        DHB class java.lang.Double 70.0
        PARKEERTAR class java.lang.String 
        AREATAG class java.lang.String 
         */

        URL url;
        if (new File(shapeFileName).canRead())
        {
            url = new File(shapeFileName).toURI().toURL();
        }
        else
        {
            url = ShapeFileReader.class.getResource(shapeFileName);
        }
        ShapefileDataStore storeRoads = (ShapefileDataStore) FileDataStoreFinder.getDataStore(url);

        Map<String, Geometry> roads = new HashMap<>();

        SimpleFeatureSource featureSourceRoads = storeRoads.getFeatureSource();
        SimpleFeatureCollection featureCollectionRoads = featureSourceRoads.getFeatures();
        SimpleFeatureIterator iterator = featureCollectionRoads.features();

        try
        {
            while (iterator.hasNext())
            {
                SimpleFeature feature = iterator.next();
                Geometry geometry = (Geometry) feature.getAttribute("the_geom");
                String ID = String.valueOf(feature.getAttribute(Id));
                roads.put(ID, geometry);
            }
        }
        catch (Exception problem)
        {
            problem.printStackTrace();
        }
        finally
        {
            iterator.close();
            storeRoads.dispose();
        }
        return roads;
    }

    /**
     * @param shapeFileName the areas shapefile to read
     * @param centroids the map of centroids
     * @return map of areas with areanr as the key
     * @throws IOException on error
     */
    public static Map<String, Area> readAreas(final String shapeFileName) throws IOException
    {
        /*-
        the_geom class com.vividsolutions.jts.geom.MultiPolygon MULTIPOLYGON (((81816.4228569232, ...
        AREANR class java.lang.Long 15127
        NAME class java.lang.String 70 Oostduinen
        CENTROIDNR class java.lang.Long 1
        NAMENR class java.lang.Long 175
        GEMEENTE_N class java.lang.String S Gravenhage
        GEMEENTEVM class java.lang.String sGravenhage
        GEBIEDSNAA class java.lang.String Studiegebied
        REGIO class java.lang.String Den_Haag
        MATCOMPRES class java.lang.String Scheveningen
        DHB class java.lang.Double 70.0
        PARKEERTAR class java.lang.String 
        AREATAG class java.lang.String 
         */

        URL url;
        if (new File(shapeFileName).canRead())
        {
            url = new File(shapeFileName).toURI().toURL();
        }
        else
        {
            url = ShapeFileReader.class.getResource(shapeFileName);
        }
        ShapefileDataStore storeAreas = (ShapefileDataStore) FileDataStoreFinder.getDataStore(url);

        Map<String, Area> areas = new HashMap<>();

        SimpleFeatureSource featureSourceAreas = storeAreas.getFeatureSource();
        SimpleFeatureCollection featureCollectionAreas = featureSourceAreas.getFeatures();
        SimpleFeatureIterator iterator = featureCollectionAreas.features();
        Long newNr = 100000000L;
        boolean OLD = false;
        try
        {
            while (iterator.hasNext())
            {
                SimpleFeature feature = iterator.next();
                Geometry geometry = (Geometry) feature.getAttribute("the_geom");
                if (OLD)
                    {
                    String areaNr = String.valueOf(feature.getAttribute("AREANR"));
                    String centroidNr = "C" + String.valueOf(feature.getAttribute("CENTROIDNR"));
                    String name = (String) feature.getAttribute("NAME");
                    String gemeente = (String) feature.getAttribute("GEMEENTEVM");
                    String gebied = (String) feature.getAttribute("GEBIEDSNAA");
                    String regio = (String) feature.getAttribute("REGIO");
                    double dhb = (double) feature.getAttribute("NUMBER");
    

                    if (areas.containsKey(centroidNr))
                    {
                        System.out.println("Area number " + centroidNr + "(" + name
                                + ") already exists. Number not unique!");
                        newNr++;
                        centroidNr = newNr.toString();
                    }
                    Area area = new Area(geometry, centroidNr, name, areaNr, gebied, regio, dhb, null, null);

                }
                else
                {
                    String name = (String) feature.getAttribute("Name");
                    int number = (int) feature.getAttribute("Number");
    
                    Area area = new Area(geometry, null, name, null, null, null, number, null, null);
                    areas.put(name, area);
                    
                }
            }
        }
        catch (Exception problem)
        {
            problem.printStackTrace();
        }
        finally
        {
            iterator.close();
            storeAreas.dispose();
        }
        return areas;
    }

    /**
     * @param p the point to search.
     * @return the area that contains point p, or null if not found.
     */
    private static Area findRoadInArea(final Geometry geom, Map<String, Area> areas)
    {
        Area area = null;
        for (Area a : areas.values())
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
