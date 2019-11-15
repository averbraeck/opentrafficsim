package org.opentrafficsim.demo.geometry.shape;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.naming.NamingException;

import org.djunits.unit.util.UNITS;
import org.djunits.value.vdouble.scalar.Direction;
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
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.draw.network.LinkAnimation;
import org.opentrafficsim.draw.road.LaneAnimation;
import org.opentrafficsim.draw.road.ShoulderAnimation;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.NoTrafficLane;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.lane.Shoulder;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;

import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Sep 11, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public final class ShapeFileReader implements UNITS
{
    /** Do not instantiate this class. */
    private ShapeFileReader()
    {
        // Cannot be instantiated.
    }

    /**
     * @param network Network; Network, the network
     * @param shapeFileName String; the nodes shapefile to read
     * @param numberType String; ???
     * @param returnCentroid boolean; if true only loop through the centroid/zones (in case of mixed nodes and centroids)
     * @param allCentroids boolean; if true: the file contains centroids (a centroid file)
     * @return map of (shape file) nodes with nodenr as the key
     * @throws IOException on error
     */
    public static Map<String, OTSRoadNode> readNodes(final Network network, final String shapeFileName, final String numberType,
            final boolean returnCentroid, final boolean allCentroids) throws IOException
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

        Map<String, OTSRoadNode> nodes = new LinkedHashMap<>();

        SimpleFeatureSource featureSourceNodes = storeNodes.getFeatureSource();
        SimpleFeatureCollection featureCollectionNodes = featureSourceNodes.getFeatures();
        SimpleFeatureIterator iterator = featureCollectionNodes.features();
        try
        {
            while (iterator.hasNext())
            {
                SimpleFeature feature = iterator.next();
                Coordinate coordinate = ((Point) feature.getAttribute("the_geom")).getCoordinate();
                String nr = removeQuotes(String.valueOf(feature.getAttribute(numberType)));
                boolean addThisNode = false;
                if (returnCentroid)
                {
                    if (nr.substring(0, 1).equals("C") || allCentroids)
                    {
                        addThisNode = true;
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
                    }
                }
                if (addThisNode)
                {
                    OTSRoadNode node =
                            new OTSRoadNode(network, nr, new OTSPoint3D(coordinate), Direction.instantiateSI(Double.NaN));
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

    /**
     * @param number String; number string
     * @return boolean: true if the number refers to a Centroid; false otherwise
     */
    public static boolean inspectNodeCentroid(final String number)
    {
        boolean isCentroid = false;
        String[] names = removeQuotes(number).split(":");
        String name = names[0];
        if (name.charAt(0) == 'C')
        {
            isCentroid = true;
        }
        return isCentroid;
    }

    /**
     * @param network OTSRoadNetwork; the network
     * @param shapeFileName String; the nodes shapefile to read
     * @param links Map&lt;String,Link&gt;; : returns the file with real links
     * @param nodes Map&lt;String,OTSNode&gt;; the map of nodes to retrieve start and end node
     * @param simulator OTSSimulatorInterface; simulator for the animation registration
     * @throws IOException on error
     */
    public static void readLinks(final OTSRoadNetwork network, final String shapeFileName, final Map<String, Link> links,
            final Map<String, OTSRoadNode> nodes, final OTSSimulatorInterface simulator) throws IOException
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
                Length length = new Length(lengthIn, KILOMETER);
                short direction = (short) Long.parseLong(String.valueOf(feature.getAttribute("DIRECTION")));
                String lNodeA = String.valueOf(feature.getAttribute("ANODE"));
                String lNodeB = String.valueOf(feature.getAttribute("BNODE"));
                // long lNodeB = NodeCentroidNumber(String.valueOf(feature.getAttribute("BNODE")));
                String linkTag = (String) feature.getAttribute("LINKTAG");
                String wegtype = (String) feature.getAttribute("WEGTYPEAB");
                String typeWegVak = (String) feature.getAttribute("TYPEWEGVAB");
                String typeWeg = (String) feature.getAttribute("TYPEWEG_AB");
                Double speedIn = Double.parseDouble(String.valueOf(feature.getAttribute("SPEEDAB")));
                Speed speed = new Speed(speedIn, KM_PER_HOUR);
                double capacityIn = Double.parseDouble(String.valueOf(feature.getAttribute("CAPACITYAB")));
                Frequency capacity = new Frequency(capacityIn, PER_HOUR);
                // new DoubleScalar.Abs<LengthUnit>(shpLink.getLength(), KILOMETER);
                // create the link or connector to a centroid....
                OTSRoadNode nodeA = nodes.get(lNodeA);
                OTSRoadNode nodeB = nodes.get(lNodeB);

                if (nodeA != null && nodeB != null)
                {
                    CrossSectionLink linkAB = null;
                    CrossSectionLink linkBA = null;
                    linkAB = new CrossSectionLink(network, nr, nodeA, nodeB, network.getLinkType(LinkType.DEFAULTS.ROAD),
                            new OTSLine3D(new OTSPoint3D[] {nodeA.getPoint(), nodeB.getPoint()}), simulator,
                            LaneKeepingPolicy.KEEPRIGHT);
                    animate(linkAB, typeWegVak, simulator);
                    linkBA = new CrossSectionLink(network, nrBA, nodeB, nodeA, network.getLinkType(LinkType.DEFAULTS.ROAD),
                            new OTSLine3D(new OTSPoint3D[] {nodeB.getPoint(), nodeA.getPoint()}), simulator,
                            LaneKeepingPolicy.KEEPRIGHT);
                    animate(linkBA, typeWegVak, simulator);
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

    /**
     * @param name String; the name with quotes
     * @return name without quotes
     */
    public static String removeQuotes(final String name)
    {
        String newName = name;
        if (newName.length() >= 2 && newName.charAt(0) == '"' && newName.charAt(newName.length() - 1) == '"')
        {
            newName = newName.substring(1, newName.length() - 1);
        }
        return newName;
    }

    /**
     * @param link CrossSectionLink; the link
     * @param wegType String; wegtype
     * @param simulator SimulatorInterface.TimeDoubleUnit; animator
     * @throws NamingException in case of context error
     * @throws RemoteException in case of context error
     * @throws NetworkException on network inconsistency
     */
    private static void animate(final CrossSectionLink link, final String wegType,
            final SimulatorInterface.TimeDoubleUnit simulator) throws NamingException, NetworkException, RemoteException
    {
        // leave out if center line not needed.
        new LinkAnimation(link, simulator, 0.1f);
        if (wegType.startsWith("asw") || wegType.startsWith("80"))
        {
            int spits = 0;
            int n = 1;
            if (wegType.contains("2x2"))
            {
                n = 2;
            }
            if (wegType.contains("2x3"))
            {
                n = 3;
            }
            if (wegType.contains("2x4"))
            {
                n = 4;
            }
            if (wegType.contains("2x5"))
            {
                n = 5;
            }
            if (wegType.contains("+ 1") || wegType.contains("+1"))
            {
                spits = 1;
            }
            if (wegType.contains("+ 2") || wegType.contains("+2"))
            {
                spits = 2;
            }
            addNLanes(n, spits, link, simulator);
        }
        if (wegType.startsWith("stads"))
        {
            int n = 1;
            if (wegType.contains("2x2"))
            {
                n = 2;
            }
            if (wegType.contains("2x3"))
            {
                n = 3;
            }
            boolean middenberm = wegType.contains("met middenberm");
            addCityStreetLanes(n, middenberm, link, simulator);
        }
        else
        {
            addCityStreet(link, simulator);
        }
    }

    /**
     * @param n int; aantal stroken per zijde
     * @param spits int; aantal spitsstroken
     * @param link CrossSectionLink; link
     * @param simulator SimulatorInterface.TimeDoubleUnit; animator
     * @throws NetworkException on network inconsistency
     */
    private static void addNLanes(final int n, final int spits, final CrossSectionLink link,
            final SimulatorInterface.TimeDoubleUnit simulator) throws NetworkException
    {
        // 2 x n lanes, grass underneath, lines between lanes, barrier in center
        // lane is 3.5 meters wide. gap in middle is one meter. outside 0.5 meters on both sides
        Length m05 = new Length(0.5, METER);
        Length m10 = new Length(1.0, METER);
        Length m35 = new Length(3.5, METER);
        Speed speedLimit = new Speed(100, KM_PER_HOUR);

        try
        {
            // middenberm
            Shoulder sM = new Shoulder(link, "sM", new Length(0.0, METER), m10);
            new ShoulderAnimation(sM, simulator, Color.GREEN);
            for (int i = -1; i <= 1; i += 2)
            {
                LongitudinalDirectionality dir =
                        (i < 0) ? LongitudinalDirectionality.DIR_PLUS : LongitudinalDirectionality.DIR_MINUS;
                // TODO Lane doesn't take direction anymore, this is given in the LaneType
                String lr = i < 0 ? "L" : "R";
                //
                Lane laneEM = new NoTrafficLane(link, lr + "." + "EM", new Length(i * 0.75, METER), new Length(i * 0.75, METER),
                        m05, m05);
                new LaneAnimation(laneEM, simulator, Color.LIGHT_GRAY);
                double lat = 1;
                for (int j = 0; j < n; j++)
                {
                    lat += i * 1.75;
                    Lane lane = new Lane(link, "lane." + lr + "." + j, new Length(lat, METER), new Length(lat, METER), m35, m35,
                            null, speedLimit);
                    new LaneAnimation(lane, simulator, Color.GRAY);
                    lat += i * 1.75;
                }
                // spitsstroken
                for (int j = 0; j < spits; j++)
                {
                    lat += i * 1.75;
                    Lane lane = new NoTrafficLane(link, "extra." + lr + "." + j, new Length(lat, METER), new Length(lat, METER),
                            m35, m35);
                    new LaneAnimation(lane, simulator, Color.LIGHT_GRAY);
                    lat += i * 1.75;
                }
                Lane laneEO = new NoTrafficLane(link, lr + "." + "EO", new Length(lat + i * 0.25, METER),
                        new Length(lat + i * 0.25, METER), m05, m05);
                new LaneAnimation(laneEO, simulator, Color.LIGHT_GRAY);
                lat += i * 0.5;
                Shoulder sO = new Shoulder(link, lr + "." + "sO", new Length(lat, METER), m10);
                new ShoulderAnimation(sO, simulator, Color.GREEN);
            }
        }
        catch (NamingException | RemoteException | OTSGeometryException ne)
        {
            //
        }
    }

    /**
     * @param n int; aantal stroken per zijde
     * @param middenberm boolean; aanwezig of niet
     * @param link CrossSectionLink; link
     * @param simulator SimulatorInterface.TimeDoubleUnit; animator
     * @throws NetworkException on network inconsistency
     */
    private static void addCityStreetLanes(final int n, final boolean middenberm, final CrossSectionLink link,
            final SimulatorInterface.TimeDoubleUnit simulator) throws NetworkException
    {
        // 2 x n lanes, grass underneath, lines between lanes, barrier in center
        // lane is 3.0 meters wide. gap in middle is one meter. outside 0.5 meters on both sides
        Length m10 = new Length(1.0, METER);
        Length m30 = new Length(3.0, METER);
        Speed speedLimit = new Speed(100, KM_PER_HOUR);

        try
        {
            if (middenberm)
            {
                Shoulder sM = new Shoulder(link, "sM", new Length(0.0, METER), m10);
                new ShoulderAnimation(sM, simulator, Color.GREEN);
            }
            for (int i = -1; i <= 1; i += 2)
            {
                // TODO Lane doesn't take direction anymore, this is given in the LaneType
                LongitudinalDirectionality dir =
                        (i < 0) ? LongitudinalDirectionality.DIR_PLUS : LongitudinalDirectionality.DIR_MINUS;
                double lat = middenberm ? 0.5 : 0.0;
                for (int j = 0; j < n; j++)
                {
                    lat += i * 1.5;
                    String lr = i < 0 ? "L" : "R";
                    Lane lane = new Lane(link, "lane." + lr + "." + j, new Length(lat, METER), new Length(lat, METER), m30, m30,
                            null, speedLimit);
                    new LaneAnimation(lane, simulator, Color.DARK_GRAY);
                    lat += i * 1.5;
                }
            }
        }
        catch (NamingException | RemoteException | ArrayIndexOutOfBoundsException | OTSGeometryException ne)
        {
            ne.printStackTrace();
        }
    }

    /**
     * @param link CrossSectionLink; link
     * @param simulator SimulatorInterface.TimeDoubleUnit; animator
     * @throws NetworkException on network inconsistency
     */
    private static void addCityStreet(final CrossSectionLink link, final SimulatorInterface.TimeDoubleUnit simulator)
            throws NetworkException
    {
        Length m60 = new Length(6.0, METER);
        Speed speedLimit = new Speed(100, KM_PER_HOUR);

        try
        {
            Lane lane = new Lane(link, "lane", new Length(0.0, METER), new Length(0.0, METER), m60, m60, null, speedLimit);
            new LaneAnimation(lane, simulator, Color.DARK_GRAY);
        }
        catch (NamingException | RemoteException | OTSGeometryException ne)
        {
            //
        }
    }

}
