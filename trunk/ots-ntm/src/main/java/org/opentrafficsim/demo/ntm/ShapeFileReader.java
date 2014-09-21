package org.opentrafficsim.demo.ntm;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 11, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class ShapeFileReader
{

    /**
     * @param shapeFileName the areas shapefile to read
     * @param centroids the map of centroids
     * @return map of areas with areanr as the key
     * @throws IOException on error
     */
    public static Map<Long, Area> ReadAreas(final String shapeFileName, final Map<Long, ShpNode> centroids)
            throws IOException
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
            url = new File(shapeFileName).toURI().toURL();
        else
            url = ShapeFileReader.class.getResource(shapeFileName);
        ShapefileDataStore storeAreas = (ShapefileDataStore) FileDataStoreFinder.getDataStore(url);

        Map<Long, Area> areas = new HashMap<>();

        SimpleFeatureSource featureSourceAreas = storeAreas.getFeatureSource();
        SimpleFeatureCollection featureCollectionAreas = featureSourceAreas.getFeatures();
        SimpleFeatureIterator iterator = featureCollectionAreas.features();
        long newNr = 100000000L;
        int numberOfAreasWithoutCentroid = 0;
        int numberOfAreasWithCentroid = 0;
        try
        {
            while (iterator.hasNext())
            {
                SimpleFeature feature = iterator.next();

                Geometry geometry = (Geometry) feature.getAttribute("the_geom");
                long nr = (long) feature.getAttribute("AREANR");
                long centroidNr = (long) feature.getAttribute("CENTROIDNR");
                String name = (String) feature.getAttribute("NAME");
                String gemeente = (String) feature.getAttribute("GEMEENTEVM");
                String gebied = (String) feature.getAttribute("GEBIEDSNAA");
                String regio = (String) feature.getAttribute("REGIO");
                double dhb = (double) feature.getAttribute("DHB");
                // search for areas within the centroids (from the "points")
                ShpNode centroid = centroids.get(centroidNr);
                if (centroid == null)
                {
                    // System.out.println("Centroid with number " + centroidNr + " not found for area " + nr + " (" +
                    // name
                    // + ")");
                    numberOfAreasWithoutCentroid++;
                }
                else
                {
                    if (areas.containsKey(nr))
                    {
                        System.out.println("Area number " + nr + "(" + name + ") already exists. Number not unique!");
                        nr = newNr++;
                    }
                    Area area = new Area(geometry, nr, name, gemeente, gebied, regio, dhb, centroid.getPoint());
                    areas.put(nr, area);
                    numberOfAreasWithCentroid++;
                }
            }
            System.out.println("Number of centroids " + centroids.size());
            System.out.println("Number of areas with centroids " + numberOfAreasWithCentroid);
            System.out.println("Number of areas without centroids " + numberOfAreasWithoutCentroid);
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
     * @param shapeFileName the nodes shapefile to read
     * @param numberType
     * @param returnCentroid if true only loop through the centroid/zones (in case of mixed nodes and centroids)
     * @param allCentroids if true: the file contains centroids (a centroid file)
     * @return map of (shape file) nodes with nodenr as the key
     * @throws IOException on error
     */
    public static Map<Long, ShpNode> ReadNodes(final String shapeFileName, final String numberType,
            boolean returnCentroid, boolean allCentroids) throws IOException
    {
        /*-
         * the_geom class com.vividsolutions.jts.geom.Point POINT (190599 325650)
         * NODENR class java.lang.Long 18
         * NAME class java.lang.String 
         * X class java.lang.Double 190599.0
         * Y class java.lang.Double 325650.0
         * ...
         */

        URL url;
        if (new File(shapeFileName).canRead())
            url = new File(shapeFileName).toURI().toURL();
        else
            url = ShapeFileReader.class.getResource(shapeFileName);
        ShapefileDataStore storeNodes = (ShapefileDataStore) FileDataStoreFinder.getDataStore(url);

        Map<Long, ShpNode> nodes = new HashMap<>();

        SimpleFeatureSource featureSourceNodes = storeNodes.getFeatureSource();
        SimpleFeatureCollection featureCollectionNodes = featureSourceNodes.getFeatures();
        SimpleFeatureIterator iterator = featureCollectionNodes.features();
        try
        {
            while (iterator.hasNext())
            {
                SimpleFeature feature = iterator.next();
                Point point = (Point) feature.getAttribute("the_geom");
                String number = String.valueOf(feature.getAttribute(numberType));
                Long nr = null;
                if (returnCentroid)
                {
                    if (number.substring(0, 1).equals("C") || allCentroids)
                    {
                        nr = InspectNodeCentroid(number);
                    }
                }
                else
                {
                    if (!number.substring(0, 1).equals("C"))
                    {
                        nr = (long) Long.parseLong(number);
                    }
                }
                if (nr != null)
                {
                    double x = (double) feature.getAttribute("X");
                    double y = (double) feature.getAttribute("Y");
                    ShpNode node = new ShpNode(point, nr, x, y);
                    nodes.put(nr, node);
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
            storeNodes.dispose();
        }

        return nodes;
    }

    /**
     * @param number
     * @return nr: the number of the Node without characters
     */
    public static Long InspectNodeCentroid(String number)
    {
        Long nr = null;
        number = CsvFileReader.RemoveQuotes(number);
        String[] names = number.split(":");
        String name = names[0];
        if (name.charAt(0) == 'C')
        {
            name = name.substring(1);
            nr = (long) Long.parseLong(name);
        }
        else
        {
            nr = Long.parseLong(name);
        }
        return nr;
    }

    /**
     * @param shapeFileName the nodes shapefile to read
     * @param links : returns the file with real links
     * @param connectors returns the file with artificial links to a centroid/zone
     * @param nodes the map of nodes to retrieve start and end node
     * @param centroids the centroids to check start and end Node
     * @throws IOException on error
     */
    public static void ReadLinks(final String shapeFileName, Map<Long, ShpLink> links, Map<Long, ShpLink> connectors,
            Map<Long, ShpNode> nodes, Map<Long, ShpNode> centroids) throws IOException
    {
        /*-
         * the_geom class com.vividsolutions.jts.geom.MultiLineString MULTILINESTRING ((232250.38755446894 ...
         * LINKNR class java.lang.Long 1
         * NAME class java.lang.String 
         * DIRECTION class java.lang.Long 1
         * LENGTH class java.lang.Double 1.80327678
         * ANODE class java.lang.Long 684088
         * BNODE class java.lang.Long 1090577263
         * LINKTAG class java.lang.String 967536
         * WEGTYPEAB class java.lang.String mvt
         * TYPEWEGVAB class java.lang.String asw 2x2 (8600)
         * TYPEWEG_AB class java.lang.String 12 Autosnelweg 2x2
         * SPEEDAB class java.lang.Double 120.0
         * CAPACITYAB class java.lang.Double 8600.0
         * ...
         */

        URL url;
        if (new File(shapeFileName).canRead())
            url = new File(shapeFileName).toURI().toURL();
        else
            url = ShapeFileReader.class.getResource(shapeFileName);
        ShapefileDataStore storeLinks = (ShapefileDataStore) FileDataStoreFinder.getDataStore(url);
        SimpleFeatureSource featureSourceLinks = storeLinks.getFeatureSource();
        SimpleFeatureCollection featureCollectionLinks = featureSourceLinks.getFeatures();
        SimpleFeatureIterator iterator = featureCollectionLinks.features();
        try
        {
            while (iterator.hasNext())
            {
                SimpleFeature feature = iterator.next();

                Geometry geometry = (Geometry) feature.getAttribute("the_geom");
                long nr = (long) feature.getAttribute("LINKNR");
                String name = String.valueOf(feature.getAttribute("NAME"));
                // the reason to use String.valueOf(...) is that the .dbf files sometimes use double,
                // but also represent LENGTH by a string ....
                double length = Double.parseDouble(String.valueOf(feature.getAttribute("LENGTH")));
                short direction = (short) Long.parseLong(String.valueOf(feature.getAttribute("DIRECTION")));
                Long lNodeA = InspectNodeCentroid(String.valueOf(feature.getAttribute("ANODE")));
                long lNodeB = InspectNodeCentroid(String.valueOf(feature.getAttribute("BNODE")));
                String linkTag = (String) feature.getAttribute("LINKTAG");
                String wegtype = (String) feature.getAttribute("WEGTYPEAB");
                String typeWegVak = (String) feature.getAttribute("TYPEWEGVAB");
                String typeWeg = (String) feature.getAttribute("TYPEWEG_AB");
                double speed = Double.parseDouble(String.valueOf(feature.getAttribute("SPEEDAB")));
                double capacity = Double.parseDouble(String.valueOf(feature.getAttribute("CAPACITYAB")));

                // create the link or connector to a centroid....
                ShpNode centroidA = centroids.get(lNodeA);
                ShpNode centroidB = centroids.get(lNodeB);
                ShpNode nodeA = nodes.get(lNodeA);
                ShpNode nodeB = nodes.get(lNodeB);
                boolean nodeACentroid = false;
                boolean nodeBCentroid = false;

                if (centroidA == null && centroidB == null) // all normal links....
                {
                    if (nodeA != null && nodeB != null) // should not happen
                    {
                        ShpLink link =
                                new ShpLink(geometry, nr, name, direction, length, nodeA, nodeB, linkTag, wegtype,
                                        typeWegVak, typeWeg, speed, capacity);
                        links.put(nr, link);
                    }
                    else
                    {
                        System.out.println("Node lNodeA=" + lNodeA + " or lNodeB=" + lNodeB + " not found for linknr="
                                + nr + ", name=" + name);
                    }
                }
                else
                { // possibly a link that connects to a centroid
                  // but first test the geometry of the node/centroid: is it a node or is it a centroid?
                    if (centroidA != null)
                    {
                        if (testGeometry(geometry.getCoordinates()[0], centroidA.getPoint()))
                        {
                            nodeACentroid = true;
                        }
                    }
                    if (centroidB != null)
                    {
                        if (testGeometry(geometry.getCoordinates()[geometry.getCoordinates().length - 1],
                                centroidA.getPoint()))
                        {
                            nodeBCentroid = true;
                        }
                    }
                    if (nodeACentroid && nodeBCentroid) // should not happen
                    {
                        System.out.println("Strange connector!!!: both Centroids lNodeA= " + centroidA + " or lNodeB= "
                                + centroidB + " connected to linknr=" + nr + ", name=" + name);
                    }
                    else if (nodeACentroid)
                    {
                        ShpLink link =
                                new ShpLink(geometry, nr, name, direction, length, centroidA, nodeB, linkTag, wegtype,
                                        typeWegVak, typeWeg, speed, capacity);
                        connectors.put(nr, link);

                    }
                    else if (nodeBCentroid)
                    {
                        ShpLink link =
                                new ShpLink(geometry, nr, name, direction, length, nodeA, centroidB, linkTag, wegtype,
                                        typeWegVak, typeWeg, speed, capacity);
                        connectors.put(nr, link);

                    }
                    else
                    {
                        ShpLink link =
                                new ShpLink(geometry, nr, name, direction, length, nodeA, nodeB, linkTag, wegtype,
                                        typeWegVak, typeWeg, speed, capacity);
                        links.put(nr, link);
                    }
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
            storeLinks.dispose();
        }

    }

    /**
     * @param coordinate
     * @param centroid
     * @return if TRUE: the points match geographically
     */
    public static boolean testGeometry(Coordinate coordinate, Point centroid)
    {
        boolean geomEqual = false;
        if (Math.abs(coordinate.x - centroid.getX()) < 1)
            if (Math.abs(coordinate.y - centroid.getY()) < 1)
                geomEqual = true;
        return geomEqual;
    }

    /**
     * @param shapeFileName the areas shapefile to read
     * @throws IOException on error
     */
    public static void shapeFileInfo(final String shapeFileName) throws IOException
    {
        URL url;
        if (new File(shapeFileName).canRead())
            url = new File(shapeFileName).toURI().toURL();
        else
            url = ShapeFileReader.class.getResource(shapeFileName);
        ShapefileDataStore store = (ShapefileDataStore) FileDataStoreFinder.getDataStore(url);

        SimpleFeatureSource featureSource = store.getFeatureSource();
        SimpleFeatureCollection featureCollection = featureSource.getFeatures();
        SimpleFeatureIterator iterator = featureCollection.features();
        try
        {
            while (iterator.hasNext())
            {
                SimpleFeature feature = iterator.next();
                Collection<Property> areaProperties = feature.getProperties();
                for (Property p : areaProperties)
                {
                    System.out.println(p.getName() + " " + p.getValue().getClass() + " " + p.getValue().toString());
                }
                return;
            }
        }
        catch (Exception problem)
        {
            problem.printStackTrace();
        }
        finally
        {
            iterator.close();
            store.dispose();
        }
    }
}
