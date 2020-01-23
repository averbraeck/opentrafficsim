package org.opentrafficsim.demo.ntm;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.demo.ntm.NTMNode.TrafficBehaviourType;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Sep 11, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class ShapeFileReader
{

    /**
     * @param shapeFileName String; the areas shapefile to read
     * @param centroids Map&lt;String,NTMNode&gt;; the map of centroids
     * @return map of areas with areanr as the key
     * @throws IOException on error
     */
    public static Map<String, Area> readAreas(final String shapeFileName, final Map<String, NTMNode> centroids,
            double scalingFactorDemand) throws IOException
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

        Map<String, Area> areas = new LinkedHashMap<>();

        SimpleFeatureSource featureSourceAreas = storeAreas.getFeatureSource();
        SimpleFeatureCollection featureCollectionAreas = featureSourceAreas.getFeatures();
        SimpleFeatureIterator iterator = featureCollectionAreas.features();
        Long newNr = 100000000L;
        int numberOfAreasWithoutCentroid = 0;
        int numberOfAreasWithCentroid = 0;

        try
        {
            // loop through the areas
            while (iterator.hasNext())
            {
                SimpleFeature feature = iterator.next();
                Geometry geometry = (Geometry) feature.getAttribute("the_geom");
                // String nr = String.valueOf(feature.getAttribute("AREANR"));
                String centroidNr = "C" + String.valueOf(feature.getAttribute("CENTROIDNR"));
                String name = (String) feature.getAttribute("NAME");
                String gemeente = (String) feature.getAttribute("GEMEENTEVM");
                String gebied = (String) feature.getAttribute("GEBIEDSNAA");
                String regio = (String) feature.getAttribute("REGIO");
                // double dhb = (double) feature.getAttribute("DHB");

                /*
                 * String gemeente = "empty"; String gebied = "empty"; String regio = "empty";
                 */
                double dhb = (double) 0.0;

                NTMNode centroidNode = null;
                if (centroids != null)
                {
                    // search for areas within the centroids (from the "points")
                    centroidNode = centroids.get(centroidNr);
                }
                if (centroidNode == null)
                {
                    for (NTMNode node : centroids.values())
                    {
                        Geometry g = new GeometryFactory().createPoint(node.getPoint().getCoordinate());
                        if (geometry.contains(g))
                        {
                            centroidNode = node;
                            centroidNr = node.getId();
                        }
                    }
                }
                if (centroidNode == null)
                {
                    // System.out.println("Centroid with number " + centroidNr + " not found for area " + nr + " (" +
                    // name
                    // + ")");
                    numberOfAreasWithoutCentroid++;
                }
                else
                {
                    if (areas.containsKey(centroidNr))
                    {
                        System.out.println("Area number " + centroidNr + "(" + name + ") already exists. Number not unique!");
                        newNr++;
                        centroidNr = newNr.toString();
                    }
                    double accCritMaxCapStart = 25;
                    double accCritMaxCapEnd = 50;
                    double accCritJam = 100;
                    double increaseDemandByFactor = scalingFactorDemand;
                    ArrayList<java.lang.Double> accCritical = new ArrayList<java.lang.Double>();
                    accCritical.add(accCritMaxCapStart);
                    accCritical.add(accCritMaxCapEnd);
                    accCritical.add(accCritJam);
                    ParametersNTM parametersNTM = new ParametersNTM(accCritical);
                    Area area = new Area(geometry, centroidNr, name, gemeente, gebied, regio, dhb,
                            centroidNode.getPoint().getCoordinate(), TrafficBehaviourType.NTM, new Length(0, LengthUnit.METER),
                            new Speed(0, SpeedUnit.KM_PER_HOUR), increaseDemandByFactor, parametersNTM);
                    areas.put(centroidNr, area);
                    numberOfAreasWithCentroid++;
                }
            }
            if (centroids != null)
            {
                System.out.println("Number of centroids " + centroids.size());
                System.out.println("Number of areas with centroids " + numberOfAreasWithCentroid);
                System.out.println("Number of areas without centroids " + numberOfAreasWithoutCentroid);
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
        int teller = 0;

        if (centroids != null)
        {
            for (NTMNode centroid : centroids.values())
            {
                boolean found = false;

                if (areas.containsKey(centroid.getId()))
                {
                    found = true;
                    teller++;
                }
                if (!found)
                {
                    areas.put(centroid.getId(), BuildGraph.createMissingArea(centroid));
                    System.out.println("Centroid not found: create area for " + centroid.getId());
                }
            }
        }
        System.out.println("found : " + teller);
        return areas;
    }

    /**
     * @param shapeFileName String; the nodes shapefile to read
     * @param numberType String;
     * @param returnCentroid boolean; , if true only loop through the centroid/zones (in case of mixed nodes and centroids)
     * @param allCentroids boolean; , if true: the file only contains centroids (a centroid file)
     * @return map of (shape file) nodes with nodenr as the key
     * @throws IOException on error
     */
    public static Map<String, NTMNode> ReadNodes(final NTMModel model, final String shapeFileName, final String numberType,
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
        {
            url = new File(shapeFileName).toURI().toURL();
        }
        else
        {
            url = ShapeFileReader.class.getResource(shapeFileName);
        }
        ShapefileDataStore storeNodes = (ShapefileDataStore) FileDataStoreFinder.getDataStore(url);

        Map<String, NTMNode> nodes = new LinkedHashMap<>();

        SimpleFeatureSource featureSourceNodes = storeNodes.getFeatureSource();
        SimpleFeatureCollection featureCollectionNodes = featureSourceNodes.getFeatures();
        SimpleFeatureIterator iterator = featureCollectionNodes.features();
        try
        {
            while (iterator.hasNext())
            {
                SimpleFeature feature = iterator.next();
                Point p = (Point) feature.getAttribute("the_geom");
                Coordinate point = new Coordinate(p.getX(), p.getY());
                String nr = CsvFileReader.removeQuotes(String.valueOf(feature.getAttribute(numberType)));
                boolean addThisNode = false;
                TrafficBehaviourType type = null;
                if (returnCentroid)
                {
                    if (nr.substring(0, 1).equals("C") || allCentroids)
                    {
                        addThisNode = true;
                        type = TrafficBehaviourType.NTM;
                    }
                }
                else
                {
                    if (nr == null)
                    {
                        System.out.println("null found");
                    }
                    if (!nr.substring(0, 1).equals("C"))
                    {
                        addThisNode = true;
                        type = TrafficBehaviourType.ROAD;
                    }
                }
                if (addThisNode)
                {
                    double x;
                    double y;
                    if (feature.getAttribute("X") != null)
                    {
                        x = (double) feature.getAttribute("X");
                        y = (double) feature.getAttribute("Y");
                    }
                    else
                    {
                        x = point.x;
                        y = point.y;
                    }

                    // initially, set the behaviour default to TrafficBehaviourType.ROAD
                    NTMNode node = new NTMNode(model.getNetwork(), nr, point, type);
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
        System.out.println("aantal knopen (353): geteld " + nodes.size());
        return nodes;
    }

    /*    *//**
             * @param number
             * @return nr: the number of the Node without characters
             */

    /*
     * public static String NodeCentroidNumber(String number) { // String nr = null; number =
     * CsvFileReader.removeQuotes(number); String[] names = number.split(":"); String name = names[0]; if (name.charAt(0) ==
     * 'C') { name = name.substring(1); // nr = (long) Long.parseLong(name); } return name; }
     */
    /**
     * @param number String;
     * @return nr: the number of the Node without characters
     */
    public static boolean inspectNodeCentroid(String number)
    {
        boolean isCentroid = false;
        number = CsvFileReader.removeQuotes(number);
        String[] names = number.split(":");
        String name = names[0];
        if (name.charAt(0) == 'C')
        {
            isCentroid = true;
        }
        return isCentroid;
    }

    /**
     * @param shapeFileName String; the nodes shapefile to read
     * @param links Map&lt;String,NTMLink&gt;; : returns the file with real links
     * @param connectors Map&lt;String,NTMLink&gt;; returns the file with artificial links to a centroid/zone
     * @param nodes Map&lt;String,NTMNode&gt;; the map of nodes to retrieve start and end node
     * @param centroids Map&lt;String,NTMNode&gt;; the centroids to check start and end Node
     * @param lengthUnit String;
     * @param linkCapacityNumberOfHours Double;
     * @throws IOException on error
     */
    public static void readLinks(final NTMModel model, final String shapeFileName, Map<String, NTMLink> links,
            Map<String, NTMLink> connectors, Map<String, NTMNode> nodes, Map<String, NTMNode> centroids, String lengthUnit,
            Double linkCapacityNumberOfHours) throws IOException
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
        {
            url = new File(shapeFileName).toURI().toURL();
        }
        else
        {
            url = ShapeFileReader.class.getResource(shapeFileName);
        }

        ShapefileDataStore storeLinks = (ShapefileDataStore) FileDataStoreFinder.getDataStore(url);
        SimpleFeatureSource featureSourceLinks = storeLinks.getFeatureSource();
        SimpleFeatureCollection featureCollectionLinks = featureSourceLinks.getFeatures();
        SimpleFeatureIterator iterator = featureCollectionLinks.features();
        try
        {
            while (iterator.hasNext())
            {
                SimpleFeature feature = iterator.next();
                GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
                Geometry geometry = (Geometry) feature.getAttribute("the_geom");
                Coordinate[] coords = geometry.getCoordinates();
                LineString line = geometryFactory.createLineString(coords);
                String nr = String.valueOf(feature.getAttribute("LINKNR"));
                String nrBA = nr + "_BA";
                String name = String.valueOf(feature.getAttribute("NAME"));
                // the reason to use String.valueOf(...) is that the .dbf files sometimes use double,
                // but also represent LENGTH by a string ....
                double lengthIn = Double.parseDouble(String.valueOf(feature.getAttribute("LENGTH")));
                Length length = null;
                if (lengthUnit.equals("kilometer"))
                {
                    length = new Length(lengthIn, LengthUnit.KILOMETER);
                }
                else if (lengthUnit.equals("meter"))
                {
                    length = new Length(lengthIn, LengthUnit.METER);
                }
                short direction = (short) Long.parseLong(String.valueOf(feature.getAttribute("DIRECTION")));
                String lNodeA = String.valueOf(feature.getAttribute("ANODE"));
                String lNodeB = String.valueOf(feature.getAttribute("BNODE"));
                // long lNodeB = NodeCentroidNumber(String.valueOf(feature.getAttribute("BNODE")));
                String linkTag = (String) feature.getAttribute("LINKTAG");
                String wegtype = (String) feature.getAttribute("WEGTYPEAB");
                String typeWegVak = (String) feature.getAttribute("TYPEWEGVAB");
                String typeWeg = (String) feature.getAttribute("TYPEWEG_AB");
                Double speedIn = Double.parseDouble(String.valueOf(feature.getAttribute("SPEEDAB")));
                Speed speed = new Speed(speedIn, SpeedUnit.KM_PER_HOUR);
                double capacityIn =
                        Double.parseDouble(String.valueOf(feature.getAttribute("CAPACITYAB"))) / linkCapacityNumberOfHours;
                Frequency capacity = new Frequency(capacityIn, FrequencyUnit.PER_HOUR);
                int hierarchy = 0;
                // new DoubleScalar.Abs<LengthUnit>(shpLink.getLength(), LengthUnit.KILOMETER);
                // create the link or connector to a centroid....
                NTMNode centroidA = centroids.get(lNodeA);
                NTMNode centroidB = centroids.get(lNodeB);
                NTMNode nodeA = nodes.get(lNodeA);
                NTMNode nodeB = nodes.get(lNodeB);
                boolean nodeACentroid = false;
                boolean nodeBCentroid = false;

                if (centroidA == null && centroidB == null) // all normal links....
                {
                    if (nodeA != null && nodeB != null)
                    {
                        NTMLink linkAB = null;
                        NTMLink linkBA = null;
                        LinkData linkData = new LinkData(name, linkTag, wegtype, typeWegVak, typeWeg);
                        linkAB = new NTMLink(model.getNetwork(), model.getSimulator(), new OTSLine3D(line), nr, length, nodeA,
                                nodeB, speed, null, capacity, TrafficBehaviourType.ROAD, linkData);
                        linkData = new LinkData(name + "_BA", linkTag, wegtype, typeWegVak, typeWeg);
                        linkBA = new NTMLink(model.getNetwork(), model.getSimulator(), new OTSLine3D(line), nrBA, length, nodeB,
                                nodeA, speed, null, capacity, TrafficBehaviourType.ROAD, linkData);
                        if (direction == 1)
                        {
                            links.put(nr, linkAB);
                        }
                        else if (direction == 2)
                        {
                            links.put(nrBA, linkBA);
                        }
                        else if (direction == 3)
                        {
                            links.put(nr, linkAB);
                            links.put(nrBA, linkBA);
                        }

                    }
                    else
                    {
                        System.out.println("Node lNodeA=" + lNodeA + " or lNodeB=" + lNodeB + " not found for linknr=" + nr
                                + ", name=" + name);
                    }
                }
                else
                { // possibly a link that connects to a centroid
                  // but first test the geometry of the node/centroid: is it a node or is it a centroid?
                    if (centroidA != null)
                    {
                        if (testGeometry(geometry.getCoordinates()[0], centroidA.getPoint().getCoordinate()))
                        {
                            nodeACentroid = true;
                        }
                    }
                    if (centroidB != null)
                    {
                        if (testGeometry(geometry.getCoordinates()[geometry.getCoordinates().length - 1],
                                centroidA.getPoint().getCoordinate()))
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
                        NTMLink linkAB = null;
                        NTMLink linkBA = null;
                        LinkData linkData = new LinkData(name, linkTag, wegtype, typeWegVak, typeWeg);
                        linkAB = new NTMLink(model.getNetwork(), model.getSimulator(), new OTSLine3D(line), nr, length,
                                centroidA, nodeB, speed, null, capacity, TrafficBehaviourType.NTM, linkData);
                        linkData = new LinkData(name + "_BA", linkTag, wegtype, typeWegVak, typeWeg);
                        linkBA = new NTMLink(model.getNetwork(), model.getSimulator(), new OTSLine3D(line), nrBA, length, nodeB,
                                centroidA, speed, null, capacity, TrafficBehaviourType.NTM, linkData);
                        if (direction == 1)
                        {
                            connectors.put(nr, linkAB);
                        }
                        else if (direction == 2)
                        {
                            connectors.put(nrBA, linkBA);
                        }
                        else if (direction == 3)
                        {
                            connectors.put(nr, linkAB);
                            connectors.put(nrBA, linkBA);
                        }

                    }
                    else if (nodeBCentroid)
                    {
                        NTMLink linkAB = null;
                        NTMLink linkBA = null;
                        LinkData linkData = new LinkData(name, linkTag, wegtype, typeWegVak, typeWeg);
                        linkAB = new NTMLink(model.getNetwork(), model.getSimulator(), new OTSLine3D(line), nr, length, nodeA,
                                centroidB, speed, null, capacity, TrafficBehaviourType.NTM, linkData);
                        linkData = new LinkData(name + "_BA", linkTag, wegtype, typeWegVak, typeWeg);
                        linkBA = new NTMLink(model.getNetwork(), model.getSimulator(), new OTSLine3D(line), nrBA, length,
                                centroidB, nodeA, speed, null, capacity, TrafficBehaviourType.NTM, linkData);
                        if (direction == 1)
                        {
                            connectors.put(nr, linkAB);
                        }
                        else if (direction == 2)
                        {
                            connectors.put(nrBA, linkBA);
                        }
                        else if (direction == 3)
                        {
                            connectors.put(nr, linkAB);
                            connectors.put(nrBA, linkBA);
                        }

                    }
                    else
                    // should not happen
                    {
                        LinkData linkData = new LinkData(name, linkTag, wegtype, typeWegVak, typeWeg);
                        NTMLink link = new NTMLink(model.getNetwork(), model.getSimulator(), new OTSLine3D(line), nr, length,
                                nodeA, nodeB, speed, null, capacity, TrafficBehaviourType.ROAD, linkData);
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
     * @param coordinate Coordinate;
     * @param centroid Coordinate;
     * @return if TRUE: the points match geographically
     */
    public static boolean testGeometry(final Coordinate coordinate, final Coordinate centroid)
    {
        boolean geomEqual = false;
        if (Math.abs(coordinate.x - centroid.x) < 1)
        {
            if (Math.abs(coordinate.y - centroid.y) < 1)
            {
                geomEqual = true;
            }
        }
        return geomEqual;
    }

    /**
     * @param shapeFileName String; the areas shapefile to read
     * @throws IOException on error
     */
    public static void shapeFileInfo(final String shapeFileName) throws IOException
    {
        URL url;
        if (new File(shapeFileName).canRead())
        {
            url = new File(shapeFileName).toURI().toURL();
        }
        else
        {
            url = ShapeFileReader.class.getResource(shapeFileName);
        }
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
