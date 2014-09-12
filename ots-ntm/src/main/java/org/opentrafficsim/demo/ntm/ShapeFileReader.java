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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 * @version Sep 11, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class ShapeFileReader
{
    /**
     * @param shapeFileName the centroids shapefile to read
     * @return map of areas with centroidnr as the key
     * @throws IOException on error
     */
    public static Map<Long, Point> ReadCentroids(final String shapeFileName) throws IOException
    {
        /*-
        the_geom class com.vividsolutions.jts.geom.Point POINT (80758.02142846212 459363.3950000014)
        CENTROIDNR class java.lang.Long 1
        NAME class java.lang.String 70 Oostduinen
        X class java.lang.Double 80758.02142846
        Y class java.lang.Double 459363.395
        NAMENR class java.lang.Long 935
        GEMEENTE_N class java.lang.String S Gravenhage
        GEMEENTEVM class java.lang.String sGravenhage
        GEBIEDSNAA class java.lang.String Studiegebied
        REGIO class java.lang.String Den_Haag
        MATCOMPRES class java.lang.String Scheveningen
        PARKEERTAR class java.lang.String 
        BIJZLOCATI class java.lang.String 
        RITTEN class java.lang.String 
        CENTROIDTA class java.lang.String 
        PROGNOSEAA class java.lang.String 
        BRONMODEL class java.lang.String 
         */

        URL url;
        if (new File(shapeFileName).canRead())
            url = new File(shapeFileName).toURI().toURL();
        else
            url = ShapeFileReader.class.getResource(shapeFileName);
        ShapefileDataStore storeCentroids = (ShapefileDataStore) FileDataStoreFinder.getDataStore(url);

        Map<Long, Point> centroids = new HashMap<>();

        SimpleFeatureSource featureSourceCentroids = storeCentroids.getFeatureSource();
        SimpleFeatureCollection featureCollectionCentroids = featureSourceCentroids.getFeatures();
        SimpleFeatureIterator iterator = featureCollectionCentroids.features();
        try
        {
            while (iterator.hasNext())
            {
                SimpleFeature feature = iterator.next();

                Point point = (Point) feature.getAttribute("the_geom");
                long nr = (long) feature.getAttribute("CENTROIDNR");
                centroids.put(nr, point);
            }
        }
        catch (Exception problem)
        {
            problem.printStackTrace();
        }
        finally
        {
            iterator.close();
            storeCentroids.dispose();
        }

        return centroids;
    }

    /**
     * @param shapeFileName the areas shapefile to read
     * @param centroids the map of centroids
     * @return map of areas with areanr as the key
     * @throws IOException on error
     */
    public static Map<Long, Area> ReadAreas(final String shapeFileName, final Map<Long, Point> centroids)
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

                Point centroid = centroids.get(centroidNr);
                if (centroid == null)
                {
                    System.out.println("Centroid with number " + centroidNr + " not found for area" + name);
                }
                else
                {
                    Area area = new Area(geometry, nr, name, gemeente, gebied, regio, dhb, centroid);
                    areas.put(nr, area);
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
     * @param shapeFileName the nodes shapefile to read
     * @return map of (shape file) nodes with nodenr as the key
     * @throws IOException on error
     */
    public static Map<Long, ShpNode> ReadNodes(final String shapeFileName) throws IOException
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

                Geometry geometry = (Geometry) feature.getAttribute("the_geom");
                long nr = (long) feature.getAttribute("NODENR");
                double x = (double) feature.getAttribute("X");
                double y = (double) feature.getAttribute("Y");

                ShpNode node = new ShpNode(geometry, nr, x, y);
                nodes.put(nr, node);
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
     * @param shapeFileName the nodes shapefile to read
     * @param nodes the map of nodes to retrieve start and end node
     * @return map of (shape file) links with linknr as the key
     * @throws IOException on error
     */
    public static Map<Long, ShpLink> ReadLinks(final String shapeFileName, Map<Long, ShpNode> nodes) throws IOException
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

        Map<Long, ShpLink> links = new HashMap<>();

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
                String name = (String) feature.getAttribute("NAME");
                short direction = (short) (long) feature.getAttribute("DIRECTION");
                double length = (double) feature.getAttribute("LENGTH");
                long lNodeA = (long) feature.getAttribute("ANODE");
                long lNodeB = (long) feature.getAttribute("BNODE");
                String linkTag = (String) feature.getAttribute("LINKTAG");
                String wegtype = (String) feature.getAttribute("WEGTYPEAB");
                String typeWegVak = (String) feature.getAttribute("TYPEWEGVAB");
                String typeWeg = (String) feature.getAttribute("TYPEWEG_AB");
                double speed = (double) feature.getAttribute("SPEEDAB");
                double capacity = (double) feature.getAttribute("CAPACITYAB");

                ShpNode nodeA = nodes.get(lNodeA);
                ShpNode nodeB = nodes.get(lNodeB);

                if (nodeA == null || nodeB == null)
                {
                    System.out.println("Node lNodeA=" + lNodeA + " or lNodeB=" + lNodeB + " not found for linknr=" + nr
                            + ", name=" + name);
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
        catch (Exception problem)
        {
            problem.printStackTrace();
        }
        finally
        {
            iterator.close();
            storeLinks.dispose();
        }

        return links;
    }

    /**
     * @param shapeFileName the areas shapefile to read
     * @throws IOException
     * @throws Exception on error
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
