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
import org.opentrafficsim.importexport.osm.events.ProgressEvent;
import org.opentrafficsim.importexport.osm.events.ProgressListener;
import org.opentrafficsim.importexport.osm.events.WarningEvent;
import org.opentrafficsim.importexport.osm.events.WarningListener;

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
     * @param c - WGS84 Coordinate
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
            Coordinate c2 = new Coordinate(dstPt[1], -dstPt[0]);
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
        if (link.getStart().getOtsNode() == null)
        {
            link.getStart().setOtsNode(convertNode(link.getStart()));
        }
        if (link.getEnd().getOtsNode() == null)
        {
            link.getEnd().setOtsNode(convertNode(link.getEnd()));
        }
        NodeGeotools.STR start = link.getStart().getOtsNode();
        NodeGeotools.STR end = link.getEnd().getOtsNode();
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
        Double elevation = 0D;
        if (node.contains("ele"))
        {
            try
            {
                String ele = node.getTag("ele").getValue();
                String regex1 = "[0-9]+(km)|m";
                String regex2 = "[0-9]+";
                if (ele.matches(regex1))
                {
                    String[] ele2 = ele.split("(km)|m");
                    ele = ele2[0];
                    if (ele2[1].equals("km"))
                    {
                        elevation = Double.parseDouble(ele) * 1000;
                    }
                    else if (ele2[1].equals("m"))
                    {
                        elevation = Double.parseDouble(ele);
                    }
                    else
                    {
                        elevation = 0D;
                    }
                }
                else if (ele.matches(regex2))
                {
                    elevation = Double.parseDouble(ele);
                }
                coordWGS84 = new Coordinate(node.getLongitude(), node.getLatitude(), elevation);
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
     * @param osmLink - The OSM Link on which the conversion is based.
     * @param progressListener 
     * @param warningListener 
     * @return HashMap of the lane structure
     */
    private static HashMap<Double, LaneAttributes> makeStructure(final org.opentrafficsim.importexport.osm.Link osmLink,
        final WarningListener warningListener, final ProgressListener progressListener)
    {
        SortedMap<Integer, LaneAttributes> structure = new TreeMap<Integer, LaneAttributes>();
        HashMap<Double, LaneAttributes> structurewithOffset = new HashMap<Double, LaneAttributes>();
        int forwards = osmLink.getForwardLanes();
        int backwards = osmLink.getLanes() - osmLink.getForwardLanes();
        LaneType<String> lt;
        LaneAttributes la;
        for (org.opentrafficsim.importexport.osm.Tag t : osmLink.getTags())
        {
            if (t.getKey().equals("waterway"))
            {
                switch (t.getValue())
                {
                    case "river":
                        lt = makeLaneType(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.boat);
                    case "canal":
                        lt = makeLaneType(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.boat);
                    default:
                        lt = makeLaneType(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.none);
                }
                la = new LaneAttributes(lt, Color.CYAN, LongitudinalDirectionality.BOTH);
                structure.put(0, la);
            }
        }
        for (org.opentrafficsim.importexport.osm.Tag t : osmLink.getTags())
        {
            if (t.getKey().equals("highway")
                    && (t.getValue().equals("primary") || t.getValue().equals("secondary")
                            || t.getValue().equals("tertiary") || t.getValue().equals("residential")
                            || t.getValue().equals("trunk") || t.getValue().equals("motorway")
                            || t.getValue().equals("service") || t.getValue().equals("unclassified")
                            || t.getValue().equals("motorway_link") || t.getValue().equals("primary_link")
                            || t.getValue().equals("secondary_link") || t.getValue().equals("tertiary_link")
                            || t.getValue().equals("trunk_link") || t.getValue().equals("road")
                            || t.getValue().equals("track") || t.getValue().equals("living_street")))
            {
                lt = makeLaneType(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.car);
                if (osmLink.getLanes() == 1 && !osmLink.isOneway())
                {
                    la = new LaneAttributes(lt, Color.LIGHT_GRAY, LongitudinalDirectionality.BOTH);
                    structure.put(0, la);
                }
                else
                {
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
            }
            else if (t.getKey().equals("highway") && (t.getValue().equals("path") || t.getValue().equals("steps")))
            {
                List<GTUType<String>> types = new ArrayList<GTUType<String>>();
                for (org.opentrafficsim.importexport.osm.Tag t2 : osmLink.getTags())
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
                    if (osmLink.getLanes() == 1 && !osmLink.isOneway())
                    {
                        la = new LaneAttributes(lt, Color.GREEN, LongitudinalDirectionality.BOTH);
                        structure.put(0, la);
                    }
                    else
                    {
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
                types.clear();
            }
        }
        for (org.opentrafficsim.importexport.osm.Tag t : osmLink.getTags())
        {
            if (t.getKey().equals("cycleway"))
            {
                lt = makeLaneType(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.bike);
                switch (t.getValue())
                {
                    case "lane": // cycleway:lane is directly adjacent to the highway.
                        forwards++;
                        backwards++;
                        la = new LaneAttributes(lt, Color.ORANGE, LongitudinalDirectionality.BACKWARD);
                        structure.put(0 - backwards, la);
                        la = new LaneAttributes(lt, Color.ORANGE, LongitudinalDirectionality.FORWARD);
                        structure.put(forwards - 1, la);
                        break;
                    case "track": // cycleway:track is separated by a gap from the highway.
                        forwards++;
                        backwards++;
                        la = new LaneAttributes(lt, Color.ORANGE, LongitudinalDirectionality.BACKWARD);
                        structure.put(0 - backwards, la);
                        la = new LaneAttributes(lt, Color.ORANGE, LongitudinalDirectionality.FORWARD);
                        structure.put(forwards - 1, la);
                        break;
                    case "shared_lane": // cycleway:shared_lane is embedded into the highway.
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
        for (org.opentrafficsim.importexport.osm.Tag t : osmLink.getTags())
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
        for (org.opentrafficsim.importexport.osm.Tag t : osmLink.getTags())
        {
            if (t.getKey().equals("highway")
                    && (t.getValue().equals("cycleway") || t.getValue().equals("footway")
                            || t.getValue().equals("pedestrian") || t.getValue().equals("steps")))
            {
                if (t.getValue().equals("footway") || t.getValue().equals("pedestrian") || t.getValue().equals("steps"))
                {
                    lt = makeLaneType(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.pedestrian);
                    if (osmLink.getLanes() == 1 && !osmLink.isOneway())
                    {
                        la = new LaneAttributes(lt, Color.GREEN, LongitudinalDirectionality.BOTH);
                        structure.put(0, la);
                    }
                    else
                    {
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
                if (t.getValue().equals("cycleway"))
                {
                    lt = makeLaneType(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.bike);
                    if (osmLink.getLanes() == 1 && !osmLink.isOneway())
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
        structurewithOffset = calculateOffsets(structure, osmLink, forwards, backwards, warningListener, progressListener);
        return structurewithOffset;
    }

    /** Calculates the actual offsets of the individual lanes.
     * @param structure - Sorted Map of Lane Positions and Attributes
     * @param osmLink - The osmLink on which the conversion is based.
     * @param forwards - Number of forwards oriented lanes.
     * @param backwards - Number of backwards oriented lanes.
     * @param progressListener 
     * @param warningListener 
     * @return HashMap containing the lane structure with offsets.
     */
    private static HashMap<Double, LaneAttributes> calculateOffsets(final SortedMap<Integer, LaneAttributes> structure,
            final org.opentrafficsim.importexport.osm.Link osmLink, final Integer forwards, final Integer backwards,
            final WarningListener warningListener, final ProgressListener progressListener)
    {
        HashMap<Double, LaneAttributes> structurewithOffset = new HashMap<Double, LaneAttributes>();
        LaneAttributes la;
        double currentOffset = 0.0D;
        if (structure.isEmpty())
        {
            warningListener.warning(new WarningEvent(osmLink, "Empty Structure at Link " + osmLink.getID()));
        }
        if (structure.lastKey() >= 0)
        {
            for (int i = 0; i < forwards; i++)
            {
                la = structure.get(i);
                if (la == null)
                {
                    break;
                }
                double useWidth = laneWidth(la, osmLink, warningListener);
                la.setWidth(useWidth);
                structurewithOffset.put(currentOffset, la);
                currentOffset += useWidth;
            }
        }
        if (structure.firstKey() < 0)
        {
            currentOffset = 0.0D;
            for (int i = -1; i >= (0 - backwards); i--)
            {
                la = structure.get(i);
                if (la == null)
                {
                    break;
                }
                LaneAttributes previousLaneAttributes = null;
                for (int k = i + 1; k <= 0; k++)
                {
                    previousLaneAttributes = structure.get(k);
                    if (null != previousLaneAttributes)
                    {
                        break;
                    }
                }
                if (null == previousLaneAttributes)
                {
                    throw new Error("reverse lane without main lane?");
                }
                double useWidth = laneWidth(la, osmLink, warningListener);
                la.setWidth(useWidth);
                currentOffset -= previousLaneAttributes.getWidth().getSI();
                structurewithOffset.put(currentOffset, la);
            }
        }
        return structurewithOffset;
    }

    /**Figure out a reasonable width for a lane.
     * @param la LaneAttributes; the attributes of the lane
     * @param link org.opentrafficsim.importexport.osm.Link; the link that owns the lane
     * @param warningListener 
     * @return double
     */
    static double laneWidth(final LaneAttributes la, final org.opentrafficsim.importexport.osm.Link link,
        final WarningListener warningListener)
    {
        Double defaultLaneWidth = 3.05D; //TODO This is the German standard car lane width
        boolean widthOverride = false;
        for (org.opentrafficsim.importexport.osm.Tag t : link.getTags())
        {
            if (t.getKey().equals("width"))
            {
                String w = t.getValue().replace(",", ".");
                w = w.replace(" ", "");
                w = w.replace("m", "");
                defaultLaneWidth = Double.parseDouble(w) / link.getLanes();
                widthOverride = true;
            }
        }
        LaneType<?> lt = la.getLaneType();
        if (lt.isCompatible(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.car))
        {
            return defaultLaneWidth;
        }
        else if (lt.isCompatible(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.bike))
        {
            return 0.8D; //TODO German default bikepath width
        }
        else if (lt.isCompatible(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.pedestrian))
        {
            return 0.95d; //TODO German default footpath width
        }
        else if (lt.isCompatible(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.boat))
        {
            for (org.opentrafficsim.importexport.osm.Tag t : link.getTags())
            {
                if (t.getKey().equals("waterway"))
                {
                    switch (t.getValue())
                    {
                        case "riverbank":
                            return 1d;
                        default:
                            return defaultLaneWidth;
                    }
                }
                else
                {
                    return 5d;
                }
            }
        }
        if (!widthOverride)
        {
            warningListener.warning(new WarningEvent(link, "No width given, assuming a default value at Link " + link.getID()));
        }
        return defaultLaneWidth;
    }

    /**
     * This method creates lanes out of an OSM link LaneTypes are not jet extensive and can be further increased through
     * Tags provided by OSM. The standard lane width of 3.05 is an estimation based on the European width limitation for
     * vehicles (2.55m) + 25cm each side.
     * @param osmlink Link - the OSM link to make lanes for.
     * @param simulator - The simulator for the animation.
     * @param warningListener 
     * @param progressListener 
     * @return Lanes 
     * @throws NetworkException 
     * @throws NamingException 
     * @throws RemoteException 
     */
    public static List<Lane> makeLanes(final org.opentrafficsim.importexport.osm.Link osmlink,
            final OTSDEVSSimulatorInterface simulator, final WarningListener warningListener, final ProgressListener progressListener) throws NetworkException, RemoteException, NamingException
    {
        CrossSectionLink<?, ?> otslink = convertLink(osmlink);
        List<Lane> lanes = new ArrayList<Lane>();
        LaneType<?> lt = null;
        Color color = Color.LIGHT_GRAY;
        HashMap<Double, LaneAttributes> structure = makeStructure(osmlink, warningListener, progressListener);

        DoubleScalar.Abs<FrequencyUnit> f2000 = new DoubleScalar.Abs<FrequencyUnit>(2000.0, FrequencyUnit.PER_HOUR);
        /** temporary */
        Iterator<Double> iter = structure.keySet().iterator();
        while (iter.hasNext())
        {
            Double offset = iter.next();
            LaneAttributes la = structure.get(offset);
            if (la == null)
            {
                break;
            }
            lt = la.getLaneType();
            DoubleScalar.Rel<LengthUnit> latPos = new DoubleScalar.Rel<LengthUnit>(offset, LengthUnit.METER);
            Lane newLane = null;
            if (osmlink.hasTag("hasPreceding") && offset >= 0 || osmlink.hasTag("hasFollowing") && offset < 0)
            {
                color = Color.RED;
                newLane = new SinkLane(otslink, latPos, la.getWidth(), lt, la.getDirectionality());
            }
            else if (osmlink.hasTag("hasPreceding") && offset < 0 || osmlink.hasTag("hasFollowing") && offset >= 0)
            {
                color = Color.BLUE;
                newLane = new SourceLane(otslink, latPos, la.getWidth(), lt, la.getDirectionality());
            }
            else
            {
                color = la.getColor();
                newLane =
                        new Lane(otslink, latPos, latPos, la.getWidth(), la.getWidth(), lt, la.getDirectionality(),
                                f2000);
            }
            animateLane(newLane, simulator, color);
            lanes.add(newLane);
        }
        return lanes;
    }

    /**
     * Animates Lane.
     * @param l - The lane that is to be animated.
     * @param simulator - The simulator for the animation.
     * @param color - The color which should be used for the animation.
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
    private static ArrayList<org.opentrafficsim.importexport.osm.Link> findEndpoints(
            final List<org.opentrafficsim.importexport.osm.Node> nodes,
            final List<org.opentrafficsim.importexport.osm.Link> links)
    {
        ArrayList<org.opentrafficsim.importexport.osm.Node> foundEndNodes =
                new ArrayList<org.opentrafficsim.importexport.osm.Node>();
        ArrayList<org.opentrafficsim.importexport.osm.Link> foundEndLinks =
                new ArrayList<org.opentrafficsim.importexport.osm.Link>();

        for (org.opentrafficsim.importexport.osm.Link l : links)
        {
            l.getStart().linksOriginating++;
            l.getEnd().linksTerminating++;
        }
        for (org.opentrafficsim.importexport.osm.Node n : nodes)
        {
            if (0 == n.linksOriginating && n.linksTerminating > 0 || 0 == n.linksTerminating && n.linksOriginating > 0)
            {
                foundEndNodes.add(n);
            }
        }
        for (org.opentrafficsim.importexport.osm.Link l : links)
        {
            if (foundEndNodes.contains(l.getStart()) || foundEndNodes.contains(l.getEnd()))
            {
                foundEndLinks.add(l);
            }
        }
        return foundEndLinks;
    }

    /**
     * @param net The OSM network which is to be searched for Sinks and Sources.
     * @param progressListener 
     * @return Network with all possible sinks and sources tagged.
     */
    public static org.opentrafficsim.importexport.osm.Network findSinksandSources(
            final org.opentrafficsim.importexport.osm.Network net,
            final ProgressListener progressListener)
    {
        progressListener.progress(new ProgressEvent(net, "Starting to find Sinks and Sources"));
        List<org.opentrafficsim.importexport.osm.Node> nodes =
                new ArrayList<org.opentrafficsim.importexport.osm.Node>();
        nodes.addAll(net.getNodes().values());
        ArrayList<org.opentrafficsim.importexport.osm.Link> foundEndpoints = findEndpoints(nodes, net.getLinks());
        for (org.opentrafficsim.importexport.osm.Link l : net.getLinks())
        {
            
            if (foundEndpoints.contains(l))
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
        progressListener.progress(new ProgressEvent(net, "Found " + foundEndpoints.size() + " Sinks and Sources."));
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
    private LongitudinalDirectionality directionality;

    /** */
    private DoubleScalar.Rel<LengthUnit> width;

    /**
     * @param lt - LaneType
     * @param c - Color
     * @param d - LongitudinalDIrectionality
     */
    public LaneAttributes(final LaneType<?> lt, final Color c, final LongitudinalDirectionality d)
    {
        if (lt == null)
        {
            this.laneType = Convert.makeLaneType(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.none);
        }
        else
        {
            this.laneType = lt;
        }
        this.color = c;
        this.directionality = d;
    }

    /**
     * @param lt - LaneType
     * @param c - Color
     * @param d - LongitudinalDIrectionality
     * @param w - width
     */
    public LaneAttributes(final LaneType<?> lt, final Color c, final LongitudinalDirectionality d, final Double w)
    {
        if (lt == null)
        {
            this.laneType = Convert.makeLaneType(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.none);
        }
        else
        {
            this.laneType = lt;
        }
        this.color = c;
        this.directionality = d;
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
        return this.directionality;
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

    /** {@inheritDoc} */
    public String toString()
    {
        return "Lane Attributes: " + this.laneType + "; " + this.color + "; " + this.directionality + "; " + this.width;
    }

    /**
     * @param lt - LaneType
     */
    public void setLaneType(final LaneType<?> lt)
    {
        this.laneType = lt;
    }
}
