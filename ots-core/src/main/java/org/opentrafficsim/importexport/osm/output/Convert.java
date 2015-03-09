package org.opentrafficsim.importexport.osm.output;

import java.awt.Color;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.naming.NamingException;

import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeocentricCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.animation.LaneAnimation;
import org.opentrafficsim.core.network.geotools.LinearGeometry;
import org.opentrafficsim.core.network.geotools.NodeGeotools;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.core.network.lane.SinkLane;
import org.opentrafficsim.core.network.lane.SourceLane;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.importexport.osm.Link;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 30.12.2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a>Moritz Bergmann</a>
 */
public final class Convert
{
    /** Do not instantiate this class. */
    private Convert()
    {
        // Cannot be instantiated.
    }

    /**
     * @param c WGS84 Coordinate
     * @return Geocentric Cartesian Coordinate
     * @throws FactoryException
     * @throws TransformException
     */
    public static Coordinate transform(final Coordinate c) throws FactoryException, TransformException
    {
        final CoordinateReferenceSystem wgs84 = DefaultGeographicCRS.WGS84;
        final CoordinateReferenceSystem cartesianCRS = DefaultGeocentricCRS.CARTESIAN;
        final MathTransform mathTransform;
        try
        {
            mathTransform = CRS.findMathTransform(wgs84, cartesianCRS, false);
            double[] srcPt = {c.x, c.y};
            double[] dstPt = new double[mathTransform.getTargetDimensions()];

            mathTransform.transform(srcPt, 0, dstPt, 0, 1);
            Coordinate c2 = new Coordinate(dstPt[0], dstPt[1]);
            return c2;
        }
        catch (FactoryException e)
        {
            throw new FactoryException(e);
        }
        catch (TransformException exception)
        {
            throw new TransformException(exception.getMessage());
        }
    }

    /**
     * This method converts an OSM link to an OTS link.
     * @param link OSM Link to be converted
     * @return OTS Link
     */
    public static CrossSectionLink<?, ?> convertLink(final org.opentrafficsim.importexport.osm.Link link)
    {
        NodeGeotools.STR start = convertNode(link.getStart());
        NodeGeotools.STR end = convertNode(link.getEnd());
        CrossSectionLink<?, ?> l2;
        if (link.getSplineList().isEmpty())
        {
            GeometryFactory factory = new GeometryFactory();
            Coordinate[] coordinates = new Coordinate[2];
            coordinates[0] = new Coordinate(start.getPoint().x, start.getPoint().y, 0);
            coordinates[1] = new Coordinate(end.getPoint().x, end.getPoint().y, 0);
            LineString lineString = factory.createLineString(coordinates);
            l2 =
                    new CrossSectionLink<String, String>(link.getID(), start, end, new DoubleScalar.Rel<LengthUnit>(
                            lineString.getLength(), LengthUnit.METER));
            try
            {
                new LinearGeometry(l2, lineString, null);
            }
            catch (NetworkException exception)
            {
                throw new Error("Network exception in LinearGeometry");
            }

        }
        else
        {
            List<Coordinate> iC = new ArrayList<Coordinate>();
            for (org.opentrafficsim.importexport.osm.Node spline : link.getSplineList())
            {
                Coordinate coord = new Coordinate(spline.getLongitude(), spline.getLatitude());
                iC.add(coord);
            }
            Coordinate[] intermediateCoordinates = new Coordinate[iC.size()];
            iC.toArray(intermediateCoordinates);
            int coordinateCount = 2 + (null == intermediateCoordinates ? 0 : intermediateCoordinates.length);
            Coordinate[] coordinates = new Coordinate[coordinateCount];
            coordinates[0] = new Coordinate(start.getPoint().x, start.getPoint().y, 0);
            coordinates[coordinates.length - 1] = new Coordinate(end.getPoint().x, end.getPoint().y, 0);
            if (null != intermediateCoordinates)
            {
                for (int i = 0; i < intermediateCoordinates.length; i++)
                {
                    coordinates[i + 1] = new Coordinate(intermediateCoordinates[i]);
                }
            }
            GeometryFactory factory = new GeometryFactory();
            LineString lineString = factory.createLineString(coordinates);
            l2 =
                    new CrossSectionLink<String, String>(link.getID(), start, end, new DoubleScalar.Rel<LengthUnit>(
                            lineString.getLength(), LengthUnit.METER));
            try
            {
                new LinearGeometry(l2, lineString, null);
            }
            catch (NetworkException exception)
            {
                throw new Error("Network exception in LinearGeometry");
            }
        }
        return l2;
    }

    /**
     * This method converts an OSM node to an OTS node.
     * @param node OSM Node to be converted
     * @return OTS Node
     */
    public static NodeGeotools.STR convertNode(final org.opentrafficsim.importexport.osm.Node node)
    {
        Coordinate coordWGS84;
        Coordinate coordGCC;
        if (node.contains("ele"))
        {
            try
            {
                coordWGS84 =
                        new Coordinate(node.getLongitude(), node.getLatitude(), Double.parseDouble(node.getTag("ele")
                                .getValue()));
                try
                {
                    coordGCC = Convert.transform(coordWGS84);
                    NodeGeotools.STR n2 = new NodeGeotools.STR(Objects.toString(node.getID()), coordGCC);
                    return n2;
                }
                catch (FactoryException exception)
                {
                    exception.printStackTrace();
                }
                catch (TransformException exception)
                {
                    exception.printStackTrace();
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
        else
        {
            coordWGS84 = new Coordinate(node.getLongitude(), node.getLatitude(), 0D);
            try
            {
                coordGCC = Convert.transform(coordWGS84);
                NodeGeotools.STR n2 = new NodeGeotools.STR(Objects.toString(node.getID()), coordGCC);
                return n2;
            }
            catch (FactoryException exception)
            {
                exception.printStackTrace();
            }
            catch (TransformException exception)
            {
                exception.printStackTrace();
            }
        }
        return null;
    }

    /**
     * @param osmlink
     * @return HashMap of the lane structure
     */
    private static HashMap<Double, LaneAttributes> makeStructure(final org.opentrafficsim.importexport.osm.Link osmlink)
    {
        SortedMap<Integer, LaneAttributes> structure = new TreeMap<Integer, LaneAttributes>();
        HashMap<Double, LaneAttributes> structurewithOffset = new HashMap<Double, LaneAttributes>();
        int forwards = osmlink.getForwardLanes();
        int backwards = osmlink.getLanes() - osmlink.getForwardLanes();
        LaneType<String> lt;
        LaneAttributes la;

        for (org.opentrafficsim.importexport.osm.Tag t : osmlink.getTags())
        {
            if (t.getKey().equals("highway")
                    && (t.getValue().equals("primary") || t.getValue().equals("secondary")
                            || t.getValue().equals("tertiary") || t.getValue().equals("residential")
                            || t.getValue().equals("trunk") || t.getValue().equals("motorway")
                            || t.getValue().equals("service") || t.getValue().equals("unclassified")
                            || t.getValue().equals("motorway_link") || t.getValue().equals("primary_link")
                            || t.getValue().equals("secondary_link") || t.getValue().equals("tertiary_link")
                            || t.getValue().equals("trunk_link") || t.getValue().equals("road")))
            {
                lt = makeLaneType(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.car);
                if (osmlink.getLanes() == 1 && !osmlink.isOneway())
                {
                    la = new LaneAttributes(lt, Color.LIGHT_GRAY, LongitudinalDirectionality.BOTH);
                    structure.put(0, la);
                }
                for (int i = 0 - backwards; i < forwards; i++)
                {
                    if (i < 0)
                    {
                        la = new LaneAttributes(lt, Color.LIGHT_GRAY, LongitudinalDirectionality.BACKWARD);
                        structure.put(i, la);
                    }
                    if (i >= 0)
                    {
                        la = new LaneAttributes(lt, Color.LIGHT_GRAY, LongitudinalDirectionality.FORWARD);
                        structure.put(i, la);
                    }
                }
            }
            else if (t.getKey().equals("highway") && t.getValue().equals("path"))
            {
                List<GTUType<String>> types = new ArrayList<GTUType<String>>();
                for (org.opentrafficsim.importexport.osm.Tag t2 : osmlink.getTags())
                {
                    if (t2.getKey().equals("bicycle"))
                    {
                        types.add(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.bike);
                    }
                    /*
                     * if (t2.getKey().equals("foot")) {
                     * types.add(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.pedestrian); }
                     */
                }
                lt = makeLaneType(types);
                types.add(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.pedestrian);
                if (!types.isEmpty())
                {
                    if (osmlink.getLanes() == 1 && !osmlink.isOneway())
                    {
                        la = new LaneAttributes(lt, Color.GREEN, LongitudinalDirectionality.BOTH);
                        structure.put(0, la);
                    }
                    for (int i = 0 - backwards; i < forwards; i++)
                    {
                        if (i < 0)
                        {
                            la = new LaneAttributes(lt, Color.GREEN, LongitudinalDirectionality.BACKWARD);
                            structure.put(i, la);
                        }
                        if (i >= 0)
                        {
                            la = new LaneAttributes(lt, Color.GREEN, LongitudinalDirectionality.FORWARD);
                            structure.put(i, la);
                        }
                    }
                }
                types.clear();
            }
        }
        for (org.opentrafficsim.importexport.osm.Tag t : osmlink.getTags())
        {
            if (t.getKey().equals("cycleway"))
            {
                lt = makeLaneType(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.bike);
                switch (t.getValue())
                {
                    case "lane":
                        forwards++;
                        backwards++;
                        la = new LaneAttributes(lt, Color.ORANGE, LongitudinalDirectionality.BACKWARD);
                        structure.put(0 - backwards, la);
                        la = new LaneAttributes(lt, Color.ORANGE, LongitudinalDirectionality.FORWARD);
                        structure.put(forwards - 1, la);
                        break;
                    case "track":
                        forwards++;
                        backwards++;
                        la = new LaneAttributes(lt, Color.ORANGE, LongitudinalDirectionality.BACKWARD);
                        structure.put(0 - backwards, la);
                        la = new LaneAttributes(lt, Color.ORANGE, LongitudinalDirectionality.FORWARD);
                        structure.put(forwards - 1, la);
                        break;
                    case "shared_lane":
                        List<GTUType<String>> types = new ArrayList<GTUType<String>>();
                        types.add(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.bike);
                        types.add(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.car);
                        lt = makeLaneType(types);
                        la = new LaneAttributes(lt, Color.ORANGE, LongitudinalDirectionality.BACKWARD);
                        structure.put(0 - backwards, la);
                        la = new LaneAttributes(lt, Color.ORANGE, LongitudinalDirectionality.FORWARD);
                        structure.put(forwards - 1, la);
                        break;
                    default:
                        break;
                }
            }
        }
        for (org.opentrafficsim.importexport.osm.Tag t : osmlink.getTags())
        {
            if (t.getKey().equals("sidewalk"))
            {
                lt = makeLaneType(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.pedestrian);
                switch (t.getValue())
                {
                    case "both":
                        forwards++;
                        backwards++;
                        la = new LaneAttributes(lt, Color.YELLOW, LongitudinalDirectionality.BACKWARD);
                        structure.put(0 - backwards, la);
                        la = new LaneAttributes(lt, Color.YELLOW, LongitudinalDirectionality.FORWARD);
                        structure.put(forwards - 1, la);
                        break;
                    case "left":
                        backwards++;
                        la = new LaneAttributes(lt, Color.YELLOW, LongitudinalDirectionality.BOTH);
                        structure.put(0 - backwards, la);
                        break;
                    case "right":
                        forwards++;
                        la = new LaneAttributes(lt, Color.YELLOW, LongitudinalDirectionality.BOTH);
                        structure.put(forwards - 1, la);
                        break;
                    default:
                        break;
                }
            }
        }
        for (org.opentrafficsim.importexport.osm.Tag t : osmlink.getTags())
        {
            if (t.getKey().equals("highway")
                    && (t.getValue().equals("cycleway") || t.getValue().equals("footway")
                            || t.getValue().equals("pedestrian") || t.getValue().equals("steps")))
            {
                if (t.getValue().equals("footway") || t.getValue().equals("pedestrian") || t.getValue().equals("steps"))
                {
                    lt = makeLaneType(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.pedestrian);
                    if (osmlink.getLanes() == 1 && !osmlink.isOneway())
                    {
                        la = new LaneAttributes(lt, Color.GREEN, LongitudinalDirectionality.BOTH);
                        structure.put(0, la);
                    }
                    for (int i = 0 - backwards; i < forwards; i++)
                    {
                        if (i < 0)
                        {
                            la = new LaneAttributes(lt, Color.GREEN, LongitudinalDirectionality.BACKWARD);
                            structure.put(i, la);
                        }
                        if (i >= 0)
                        {
                            la = new LaneAttributes(lt, Color.GREEN, LongitudinalDirectionality.FORWARD);
                            structure.put(i, la);
                        }
                    }
                }
                if (t.getValue().equals("cycleway"))
                {
                    lt = makeLaneType(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.bike);
                    if (osmlink.getLanes() == 1 && !osmlink.isOneway())
                    {
                        la = new LaneAttributes(lt, Color.GREEN, LongitudinalDirectionality.BOTH);
                        structure.put(0, la);
                    }
                    for (int i = 0 - backwards; i < forwards; i++)
                    {
                        if (i < 0)
                        {
                            la = new LaneAttributes(lt, Color.GREEN, LongitudinalDirectionality.BACKWARD);
                            structure.put(i, la);
                        }
                        if (i >= 0)
                        {
                            la = new LaneAttributes(lt, Color.GREEN, LongitudinalDirectionality.FORWARD);
                            structure.put(i, la);
                        }
                    }
                }
            }
        }
        structurewithOffset = calculateOffsets(structure, osmlink, forwards, backwards);
        return structurewithOffset;
    }

    /**
     * @param structure
     * @param osmlink
     * @param forwards
     * @param backwards
     * @return HashMap containing the lane structure with offsets.
     */
    private static HashMap<Double, LaneAttributes> calculateOffsets(final SortedMap<Integer, LaneAttributes> structure,
            final org.opentrafficsim.importexport.osm.Link osmlink, final Integer forwards, final Integer backwards)
    {
        HashMap<Double, LaneAttributes> structurewithOffset = new HashMap<Double, LaneAttributes>();
        LaneType<?> lt;
        Double width = 3.05D;
        LaneAttributes la;
        // boolean widthOverride = false;

        for (org.opentrafficsim.importexport.osm.Tag t : osmlink.getTags())
        {
            if (t.getKey().equals("width"))
            {
                String w = t.getValue().replace(",", ".");
                width = Double.parseDouble(w) / osmlink.getLanes();
                // widthOverride = true;
            }
        }
        double currentOffset = 0.0D;
        if (structure.lastKey() >= 0)
        {
            for (int i = 0; i < forwards; i++)
            {
                la = structure.get(i);
                lt = la.getLaneType();
                /*
                 * if (lt == null) { String s = "awww shucks"; System.out.println(s); }
                 */
                if (lt.isCompatible(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.car))
                {
                    la.setWidth(width);
                    structurewithOffset.put(currentOffset, la);
                    currentOffset += width;
                }
                else if (lt.isCompatible(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.bike))
                {
                    la.setWidth(0.8D);
                    structurewithOffset.put(currentOffset, la);
                    currentOffset += 0.8D;
                }
                else if (lt.isCompatible(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.pedestrian))
                {
                    la.setWidth(0.95D);
                    structurewithOffset.put(currentOffset, la);
                    currentOffset += 0.95D;
                }
            }
        }
        if (structure.firstKey() < 0)
        {
            currentOffset = 0.0D;
            for (int i = -1; i >= (0 - backwards); i--)
            {
                la = structure.get(i);
                LaneAttributes la2 = structure.get(i + 1);
                lt = la.getLaneType();
                LaneType<?> lt2 = la2.getLaneType();
                if (lt2.isCompatible(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.car))
                {
                    la.setWidth(width);
                    currentOffset -= width;
                    structurewithOffset.put(currentOffset, la);
                }
                else if (lt2.isCompatible(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.bike))
                {
                    la.setWidth(0.8D);
                    currentOffset -= 0.8D;
                    structurewithOffset.put(currentOffset, la);
                }
                else if (lt2.isCompatible(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.pedestrian))
                {
                    la.setWidth(0.95D);
                    currentOffset -= 0.95D;
                    structurewithOffset.put(currentOffset, la);
                }
            }
        }
        return structurewithOffset;
    }

    /**
     * This method creates lanes out of an OSM link LaneTypes are not jet extensive and can be further increased through
     * Tags provided by OSM. The standard lane width of 3.05 is an estimation based on the Wuropean width limitation for
     * vehicles (2.55m) + 25cm each side.
     * @param osmlink Link; the OSM link to make lanes for
     * @param simulator
     * @return Lanes
     * @throws NetworkException
     * @throws NamingException
     * @throws RemoteException
     */
    public static List<Lane> makeLanes(final org.opentrafficsim.importexport.osm.Link osmlink,
            final OTSDEVSSimulatorInterface simulator) throws NetworkException, RemoteException, NamingException
    {
        CrossSectionLink<?, ?> otslink = convertLink(osmlink);
        List<Lane> lanes = new ArrayList<Lane>();
        LaneType<?> lt = null;
        Lane result = null;
        Color color = Color.LIGHT_GRAY;
        HashMap<Double, LaneAttributes> structure = makeStructure(osmlink);

        DoubleScalar.Abs<FrequencyUnit> f2000 = new DoubleScalar.Abs<FrequencyUnit>(2000.0, FrequencyUnit.PER_HOUR);
        /** temporary */
        Iterator<Double> iter = structure.keySet().iterator();
        while (iter.hasNext())
        {
            Double i = iter.next();
            LaneAttributes la = structure.get(i);
            lt = la.getLaneType();
            Double offSet = i;
            DoubleScalar.Rel<LengthUnit> latPos = new DoubleScalar.Rel<LengthUnit>(offSet, LengthUnit.METER);
            if (osmlink.hasTag("hasPreceding") && i >= 0)
            {
                color = Color.RED;
                result = new SinkLane(otslink, latPos, la.getWidth(), lt, la.getDirectionality());
            }
            else if (osmlink.hasTag("hasPreceding") && i < 0)
            {
                color = Color.BLUE;
                result = new SourceLane(otslink, latPos, la.getWidth(), lt, la.getDirectionality());
            }
            else if (osmlink.hasTag("hasFollowing") && i >= 0)
            {
                color = Color.BLUE;
                result = new SourceLane(otslink, latPos, la.getWidth(), lt, la.getDirectionality());
            }
            else if (osmlink.hasTag("hasFollowing") && i < 0)
            {
                color = Color.RED;
                result = new SinkLane(otslink, latPos, la.getWidth(), lt, la.getDirectionality());
            }
            else
            {
                color = la.getColor();
                result =
                        new Lane(otslink, latPos, latPos, la.getWidth(), la.getWidth(), lt, la.getDirectionality(),
                                f2000);
            }
            animateLane(result, simulator, color);
            lanes.add(result);
        }
        return lanes;
    }

    /**
     * Animates Lane.
     * @param l
     * @param simulator
     * @param color
     * @throws RemoteException
     * @throws NamingException
     */
    private static void animateLane(final Lane l, final OTSDEVSSimulatorInterface simulator, final Color color)
            throws RemoteException, NamingException
    {
        if (simulator instanceof OTSAnimatorInterface)
        {
            new LaneAnimation(l, simulator, color);
        }
    }

    /**
     * This method creates a LaneType which supports all GTUTypes that have been specified in the GTUType List "GTUs".
     * @param gtuTypes List&lt;GTUType&lt;String&gt;&gt;; list of GTUTypes
     * @return LaneType permeable for all of the specific GTUTypes
     */
    public static LaneType<String> makeLaneType(final List<GTUType<String>> gtuTypes)
    {
        String iD = "";
        for (GTUType<String> gtu : gtuTypes)
        {
            iD += gtu.getId() + "|";
        }
        LaneType<String> lt = new LaneType<String>(iD);
        for (GTUType<String> gtu : gtuTypes)
        {
            lt.addPermeability(gtu);
        }
        return lt;
    }

    /**
     * This method creates a LaneType which supports the specified GTUType.
     * @param gtuType GTUType; the type of GTU that can travel on the new LaneType
     * @return LaneType
     */
    public static LaneType<String> makeLaneType(final GTUType<String> gtuType)
    {
        String iD = gtuType.getId();
        LaneType<String> lt = new LaneType<String>(iD);
        lt.addPermeability(gtuType);
        return lt;
    }

    /**
     * @param nodes List of Nodes
     * @param links List of Links
     * @return List of Links which are candidates for becoming sinks/sources.
     */
    private static ArrayList<org.opentrafficsim.importexport.osm.Link> findPossibleSinks(
            final List<org.opentrafficsim.importexport.osm.Node> nodes,
            final List<org.opentrafficsim.importexport.osm.Link> links)
    {
        ArrayList<org.opentrafficsim.importexport.osm.Node> foundSinkNodes =
                new ArrayList<org.opentrafficsim.importexport.osm.Node>();
        ArrayList<org.opentrafficsim.importexport.osm.Link> foundSinkLinks =
                new ArrayList<org.opentrafficsim.importexport.osm.Link>();
        for (org.opentrafficsim.importexport.osm.Node n : nodes)
        {
            int count = 0;
            for (org.opentrafficsim.importexport.osm.Link l : links)
            {
                if (l.getStart().equals(n) || l.getEnd().equals(n) || l.getSplineList().contains(n))
                {
                    count += 1;
                    if (count > 1)
                    {
                        break;
                    }
                }
            }
            if (count == 1)
            {
                foundSinkNodes.add(n);
            }
        }
        for (org.opentrafficsim.importexport.osm.Link l : links)
        {
            if (foundSinkNodes.contains(l.getEnd()) || foundSinkNodes.contains(l.getStart()))
            {
                foundSinkLinks.add(l);
            }
            else
            {
                for (org.opentrafficsim.importexport.osm.Node n : l.getSplineList())
                {
                    if (foundSinkNodes.contains(n))
                    {
                        foundSinkLinks.add(l);
                    }
                }
            }
        }
        /*
         * for (org.opentrafficsim.importexport.osm.Node n: foundSinkNodes) { System.out.println(n.getID()); } for
         * (org.opentrafficsim.importexport.osm.Link l : foundSinkLinks) { System.out.println(l.getID()); }
         */
        return foundSinkLinks;
    }

    /**
     * @param net
     * @return Network with all possible sinks and sources tagged.
     */
    public static org.opentrafficsim.importexport.osm.Network findSinksandSources(
            final org.opentrafficsim.importexport.osm.Network net)
    {
        List<org.opentrafficsim.importexport.osm.Node> nodes =
                new ArrayList<org.opentrafficsim.importexport.osm.Node>();
        nodes.addAll(net.getNodes().values());
        ArrayList<org.opentrafficsim.importexport.osm.Link> foundSinkLinks = findPossibleSinks(nodes, net.getLinks());
        for (org.opentrafficsim.importexport.osm.Link l : net.getLinks())
        {
            if (foundSinkLinks.contains(l))
            {
                if (net.hasFollowingLink(l))
                {
                    l.addTag(new org.opentrafficsim.importexport.osm.Tag("hasFollowing", ""));
                }
                else if (net.hasPrecedingLink(l))
                {
                    l.addTag(new org.opentrafficsim.importexport.osm.Tag("hasPreceding", ""));
                }
            }
        }
        return net;
    }
}

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Mar 3, 2015 <br>
 * @author <a>Moritz Bergmann</a>
 */
class LaneAttributes
{
    /** */
    private LaneType<?> laneType;

    /** */
    private Color color;

    /** */
    private LongitudinalDirectionality directionaility;

    /** */
    private DoubleScalar.Rel<LengthUnit> width;

    /**
     * @param lt
     * @param c
     * @param d
     */
    public LaneAttributes(final LaneType<?> lt, final Color c, final LongitudinalDirectionality d)
    {
        this.laneType = lt;
        this.color = c;
        this.directionaility = d;
    }

    /**
     * @param lt
     * @param c
     * @param d
     * @param w
     */
    public LaneAttributes(final LaneType<?> lt, final Color c, final LongitudinalDirectionality d, final Double w)
    {
        this.laneType = lt;
        this.color = c;
        this.directionaility = d;
        this.setWidth(w);
    }

    /**
     * @return LaneType<?>
     */
    public LaneType<?> getLaneType()
    {
        return this.laneType;
    }

    /**
     * @return Color
     */
    public Color getColor()
    {
        return this.color;
    }

    /**
     * @return LongitudinalDirectionality
     */
    public LongitudinalDirectionality getDirectionality()
    {
        return this.directionaility;
    }

    /**
     * @return width.
     */
    public DoubleScalar.Rel<LengthUnit> getWidth()
    {
        return this.width;
    }

    /**
     * @param width set width.
     */
    public void setWidth(final Double width)
    {
        DoubleScalar.Rel<LengthUnit> w = new DoubleScalar.Rel<LengthUnit>(width, LengthUnit.METER);
        this.width = w;
    }
}
