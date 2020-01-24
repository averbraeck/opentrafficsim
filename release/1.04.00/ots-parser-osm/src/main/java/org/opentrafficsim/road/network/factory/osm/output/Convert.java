package org.opentrafficsim.road.network.factory.osm.output;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.naming.NamingException;
import javax.xml.crypto.dsig.TransformException;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.locationtech.jts.geom.Coordinate;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.compatibility.GTUCompatibility;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.osm.OSMLink;
import org.opentrafficsim.road.network.factory.osm.OSMNetwork;
import org.opentrafficsim.road.network.factory.osm.OSMNode;
import org.opentrafficsim.road.network.factory.osm.OSMTag;
import org.opentrafficsim.road.network.factory.osm.events.ProgressEvent;
import org.opentrafficsim.road.network.factory.osm.events.ProgressListener;
import org.opentrafficsim.road.network.factory.osm.events.WarningEvent;
import org.opentrafficsim.road.network.factory.osm.events.WarningListener;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-16 19:20:07 +0200 (Wed, 16 Sep 2015) $, @version $Revision: 1405 $, by $Author: averbraeck $,
 * initial version 30.12.2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a>Moritz Bergmann</a>
 */
public final class Convert
{

    /**
     * Construct a converter.
     */
    public Convert()
    {
        baseX = Double.NaN;
    }

    /** Meridian of least distortion. */
    private static double baseX = Double.NaN;

    /**
     * @param c Coordinate; Coordinate in WGS84
     * @return Coordinate in Geocentric Cartesian system
     * @throws TransformException on problems with the coordinate transformation
     */
    public static Coordinate transform(final Coordinate c) throws TransformException
    {
        // final CoordinateReferenceSystem wgs84 = DefaultGeographicCRS.WGS84;
        // final CoordinateReferenceSystem cartesianCRS = DefaultGeocentricCRS.CARTESIAN;
        // final MathTransform mathTransform;
        // mathTransform = CRS.findMathTransform(wgs84, cartesianCRS, false);
        // double[] srcPt = {c.x, c.y};
        // double[] dstPt = new double[mathTransform.getTargetDimensions()];

        // mathTransform.transform(srcPt, 0, dstPt, 0, 1);
        // System.out.println(String.format(Locale.US, "%fkm, %fkm, %fkm", dstPt[0] / 1000, dstPt[1] / 1000, dstPt[2] /
        // 1000));
        // return new Coordinate(dstPt[1], -dstPt[0]);

        // Simple-minded DIY solution
        double radius = 6371000; // Assume Earth is a perfect sphere
        if (Double.isNaN(baseX))
        {
            baseX = c.x; // Use first coordinate as the reference
        }
        double x = radius * Math.toRadians(c.x - baseX) * Math.cos(Math.toRadians(c.y));
        double y = radius * Math.toRadians(c.y);
        // System.out.println(String.format(Locale.US, "%fkm, %fkm, %fkm", x / 1000, y / 1000, radius / 1000));
        return new Coordinate(x, y);
    }

    /**
     * This method converts an OSM link to an OTS link.
     * @param network OTSRoadNetwork; the network
     * @param link OSMLink; OSM Link to be converted
     * @param simulator OTSSimulatorInterface; the simulator that will animate the generates lanes (if it happens to be an
     *            instance of AnimatorInterface)
     * @return OTS Link
     * @throws OTSGeometryException on failure
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    public CrossSectionLink convertLink(final OTSRoadNetwork network, final OSMLink link, final OTSSimulatorInterface simulator)
            throws OTSGeometryException, NetworkException
    {
        if (null == link.getStart().getOtsNode())
        {
            link.getStart().setOtsNode(convertNode(network, link.getStart()));
        }
        if (null == link.getEnd().getOtsNode())
        {
            link.getEnd().setOtsNode(convertNode(network, link.getEnd()));
        }
        CrossSectionLink result;
        Coordinate[] coordinates;
        List<OSMNode> nodes = link.getSplineList();
        int coordinateCount = 2 + nodes.size();
        coordinates = new Coordinate[coordinateCount];
        OTSRoadNode start = link.getStart().getOtsNode();
        coordinates[0] = new Coordinate(start.getPoint().x, start.getPoint().y, 0);
        for (int i = 0; i < nodes.size(); i++)
        {
            coordinates[i + 1] = new Coordinate(nodes.get(i).getLongitude(), nodes.get(i).getLatitude());
        }
        OTSRoadNode end = link.getEnd().getOtsNode();
        coordinates[coordinates.length - 1] = new Coordinate(end.getPoint().x, end.getPoint().y, 0);
        OTSLine3D designLine = new OTSLine3D(coordinates);
        // XXX How to figure out whether to keep left, right or keep lane?
        // XXX How to figure out if this is a lane in one or two directions? For now, two is assumed...
        for (int subId = 0;; subId++)
        {
            String linkId = String.format("%s_%d", link.getId(), subId);
            if (network.containsLink(linkId))
            {
                continue;
            }
            result = new CrossSectionLink(network, linkId, start, end, network.getLinkType(LinkType.DEFAULTS.ROAD), designLine,
                    simulator, LaneKeepingPolicy.KEEPRIGHT);
            return result;
        }
    }

    /**
     * This method converts an OSM node to an OTS node.
     * @param network OTSRoadNetwork; the network
     * @param node OSMNode; OSM Node to be converted
     * @return OTSRoadNode
     * @throws NetworkException if node already exists in the network, or if name of the node is not unique
     */
    public OTSRoadNode convertNode(final OTSRoadNetwork network, final OSMNode node) throws NetworkException
    {
        OSMTag tag = node.getTag("ele");
        if (null != tag)
        {
            try
            {
                String ele = tag.getValue();
                Double elevation = 0d;
                if (ele.matches("[0-9]+(km)|m"))
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
                        throw new NumberFormatException("Cannot parse elevation value\"" + ele + "\"");
                    }
                }
                else if (ele.matches("[0-9]+"))
                {
                    elevation = Double.parseDouble(ele);
                }
                Coordinate coordWGS84 = new Coordinate(node.getLongitude(), node.getLatitude(), elevation);
                try
                {
                    return new OTSRoadNode(network, Objects.toString(node.getId()), new OTSPoint3D(transform(coordWGS84)),
                            Direction.instantiateSI(Double.NaN));
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
        }
        // No elevation specified, or we could not parse it; assume elevation is 0
        Coordinate coordWGS84 = new Coordinate(node.getLongitude(), node.getLatitude(), 0d);
        try
        {
            String nodeName = Objects.toString(node.getId());
            OTSRoadNode otsNode = (OTSRoadNode) network.getNode(nodeName);
            if (null == otsNode)
            {
                otsNode = new OTSRoadNode(network, nodeName, new OTSPoint3D(Convert.transform(coordWGS84)), 
                        Direction.instantiateSI(Double.NaN));
            }
            return otsNode;
        }
        catch (TransformException exception)
        {
            exception.printStackTrace();
            // FIXME: how does the caller deal with a null result? (Answer: not!)
            return null;
        }
    }

    /**
     * Determine the positions of the various lanes on an OSMLink.
     * @param network the network
     * @param osmLink OSMLink; - The OSM Link on which the conversion is based.
     * @param warningListener WarningListener; the warning listener that receives warning events
     * @return Map&lt;Double, LaneAttributes&gt;; the lane structure
     * @throws NetworkException on failure
     */
    private static Map<Double, LaneAttributes> makeStructure(final OTSRoadNetwork network, final OSMLink osmLink,
            final WarningListener warningListener) throws NetworkException
    {
        SortedMap<Integer, LaneAttributes> structure = new TreeMap<Integer, LaneAttributes>();
        int forwards = osmLink.getForwardLanes();
        int backwards = osmLink.getLanes() - osmLink.getForwardLanes();
        LaneType laneType;
        LaneAttributes laneAttributes;
        for (OSMTag tag : osmLink.getTags())
        {
            if (tag.getKey().equals("waterway"))
            {
                switch (tag.getValue())
                {
                    case "river":
                        laneType = makeLaneType(network, network.getGtuType(GTUType.DEFAULTS.SHIP));
                        break;
                    case "canal":
                        laneType = makeLaneType(network, network.getGtuType(GTUType.DEFAULTS.SHIP));
                        break;
                    default:
                        laneType = makeLaneType(network, network.getGtuType(GTUType.DEFAULTS.WATERWAY_USER));
                        break;
                }
                laneAttributes = new LaneAttributes(network, laneType, Color.CYAN, LongitudinalDirectionality.DIR_BOTH);
                structure.put(0, laneAttributes);
            }
        }
        for (OSMTag tag : osmLink.getTags())
        {
            if (tag.getKey().equals("highway") && (tag.getValue().equals("primary") || tag.getValue().equals("secondary")
                    || tag.getValue().equals("tertiary") || tag.getValue().equals("residential")
                    || tag.getValue().equals("trunk") || tag.getValue().equals("motorway") || tag.getValue().equals("service")
                    || tag.getValue().equals("unclassified") || tag.getValue().equals("motorway_link")
                    || tag.getValue().equals("primary_link") || tag.getValue().equals("secondary_link")
                    || tag.getValue().equals("tertiary_link") || tag.getValue().equals("trunk_link")
                    || tag.getValue().equals("road") || tag.getValue().equals("track")
                    || tag.getValue().equals("living_street")))
            {
                laneType = makeLaneType(network, network.getGtuType(GTUType.DEFAULTS.CAR));
                if (osmLink.getLanes() == 1 && !osmLink.isOneway())
                {
                    laneAttributes =
                            new LaneAttributes(network, laneType, Color.LIGHT_GRAY, LongitudinalDirectionality.DIR_BOTH);
                    structure.put(0, laneAttributes);
                }
                else
                {
                    for (int i = 0 - backwards; i < forwards; i++)
                    {
                        if (i < 0)
                        {
                            laneAttributes = new LaneAttributes(network, laneType, Color.LIGHT_GRAY,
                                    LongitudinalDirectionality.DIR_MINUS);
                            structure.put(i, laneAttributes);
                        }
                        if (i >= 0)
                        {
                            laneAttributes = new LaneAttributes(network, laneType, Color.LIGHT_GRAY,
                                    LongitudinalDirectionality.DIR_PLUS);
                            structure.put(i, laneAttributes);
                        }
                    }
                }
            }
            else if (tag.getKey().equals("highway") && (tag.getValue().equals("path") || tag.getValue().equals("steps")))
            {
                List<GTUType> types = new ArrayList<GTUType>();
                for (OSMTag t2 : osmLink.getTags())
                {
                    if (t2.getKey().equals("bicycle"))
                    {
                        types.add(network.getGtuType(GTUType.DEFAULTS.BICYCLE));
                    }
                    /*
                     * if (t2.getKey().equals("foot")) {
                     * types.add(org.opentrafficsim.importexport.osm.PredefinedGTUTypes.pedestrian); }
                     */
                }
                laneType = makeLaneType(network, types);
                types.add(network.getGtuType(GTUType.DEFAULTS.PEDESTRIAN));
                if (!types.isEmpty())
                {
                    if (osmLink.getLanes() == 1 && !osmLink.isOneway())
                    {
                        laneAttributes =
                                new LaneAttributes(network, laneType, Color.GREEN, LongitudinalDirectionality.DIR_BOTH);
                        structure.put(0, laneAttributes);
                    }
                    else
                    {
                        for (int i = 0 - backwards; i < forwards; i++)
                        {
                            if (i < 0)
                            {
                                laneAttributes = new LaneAttributes(network, laneType, Color.GREEN,
                                        LongitudinalDirectionality.DIR_MINUS);
                                structure.put(i, laneAttributes);
                            }
                            if (i >= 0)
                            {
                                laneAttributes =
                                        new LaneAttributes(network, laneType, Color.GREEN, LongitudinalDirectionality.DIR_PLUS);
                                structure.put(i, laneAttributes);
                            }
                        }
                    }
                }
                types.clear();
            }
        }
        for (OSMTag tag : osmLink.getTags())
        {
            if (tag.getKey().equals("cycleway"))
            {
                laneType = makeLaneType(network, network.getGtuType(GTUType.DEFAULTS.BICYCLE));
                switch (tag.getValue())
                {
                    case "lane": // cycleway:lane is directly adjacent to the highway.
                        forwards++;
                        backwards++;
                        laneAttributes =
                                new LaneAttributes(network, laneType, Color.ORANGE, LongitudinalDirectionality.DIR_MINUS);
                        structure.put(0 - backwards, laneAttributes);
                        laneAttributes =
                                new LaneAttributes(network, laneType, Color.ORANGE, LongitudinalDirectionality.DIR_PLUS);
                        structure.put(forwards - 1, laneAttributes);
                        break;
                    case "track": // cycleway:track is separated by a gap from the highway.
                        forwards++;
                        backwards++;
                        laneAttributes =
                                new LaneAttributes(network, laneType, Color.ORANGE, LongitudinalDirectionality.DIR_MINUS);
                        structure.put(0 - backwards, laneAttributes);
                        laneAttributes =
                                new LaneAttributes(network, laneType, Color.ORANGE, LongitudinalDirectionality.DIR_PLUS);
                        structure.put(forwards - 1, laneAttributes);
                        break;
                    case "shared_lane": // cycleway:shared_lane is embedded into the highway.
                        List<GTUType> types = new ArrayList<GTUType>();
                        types.add(network.getGtuType(GTUType.DEFAULTS.BICYCLE));
                        types.add(network.getGtuType(GTUType.DEFAULTS.CAR));
                        laneType = makeLaneType(network, types);
                        laneAttributes =
                                new LaneAttributes(network, laneType, Color.ORANGE, LongitudinalDirectionality.DIR_MINUS);
                        structure.put(0 - backwards, laneAttributes);
                        laneAttributes =
                                new LaneAttributes(network, laneType, Color.ORANGE, LongitudinalDirectionality.DIR_PLUS);
                        structure.put(forwards - 1, laneAttributes);
                        break;
                    default:
                        break;
                }
            }
        }
        for (OSMTag tag : osmLink.getTags())
        {
            if (tag.getKey().equals("sidewalk"))
            {
                laneType = makeLaneType(network, network.getGtuType(GTUType.DEFAULTS.PEDESTRIAN));
                switch (tag.getValue())
                {
                    case "both":
                        forwards++;
                        backwards++;
                        laneAttributes =
                                new LaneAttributes(network, laneType, Color.YELLOW, LongitudinalDirectionality.DIR_MINUS);
                        structure.put(0 - backwards, laneAttributes);
                        laneAttributes =
                                new LaneAttributes(network, laneType, Color.YELLOW, LongitudinalDirectionality.DIR_PLUS);
                        structure.put(forwards - 1, laneAttributes);
                        break;
                    case "left":
                        backwards++;
                        laneAttributes =
                                new LaneAttributes(network, laneType, Color.YELLOW, LongitudinalDirectionality.DIR_BOTH);
                        structure.put(0 - backwards, laneAttributes);
                        break;
                    case "right":
                        forwards++;
                        laneAttributes =
                                new LaneAttributes(network, laneType, Color.YELLOW, LongitudinalDirectionality.DIR_BOTH);
                        structure.put(forwards - 1, laneAttributes);
                        break;
                    default:
                        break;
                }
            }
        }
        for (OSMTag tag : osmLink.getTags())
        {
            if (tag.getKey().equals("highway") && (tag.getValue().equals("cycleway") || tag.getValue().equals("footway")
                    || tag.getValue().equals("pedestrian") || tag.getValue().equals("steps")))
            {
                if (tag.getValue().equals("footway") || tag.getValue().equals("pedestrian") || tag.getValue().equals("steps"))
                {
                    laneType = makeLaneType(network, network.getGtuType(GTUType.DEFAULTS.PEDESTRIAN));
                    if (osmLink.getLanes() == 1 && !osmLink.isOneway())
                    {
                        laneAttributes =
                                new LaneAttributes(network, laneType, Color.GREEN, LongitudinalDirectionality.DIR_BOTH);
                        structure.put(0, laneAttributes);
                    }
                    else
                    {
                        for (int i = 0 - backwards; i < forwards; i++)
                        {
                            if (i < 0)
                            {
                                laneAttributes = new LaneAttributes(network, laneType, Color.GREEN,
                                        LongitudinalDirectionality.DIR_MINUS);
                                structure.put(i, laneAttributes);
                            }
                            if (i >= 0)
                            {
                                laneAttributes =
                                        new LaneAttributes(network, laneType, Color.GREEN, LongitudinalDirectionality.DIR_PLUS);
                                structure.put(i, laneAttributes);
                            }
                        }
                    }
                }
                if (tag.getValue().equals("cycleway"))
                {
                    laneType = makeLaneType(network, network.getGtuType(GTUType.DEFAULTS.BICYCLE));
                    if (osmLink.getLanes() == 1 && !osmLink.isOneway())
                    {
                        laneAttributes =
                                new LaneAttributes(network, laneType, Color.GREEN, LongitudinalDirectionality.DIR_BOTH);
                        structure.put(0, laneAttributes);
                    }
                    for (int i = 0 - backwards; i < forwards; i++)
                    {
                        if (i < 0)
                        {
                            laneAttributes =
                                    new LaneAttributes(network, laneType, Color.GREEN, LongitudinalDirectionality.DIR_MINUS);
                            structure.put(i, laneAttributes);
                        }
                        if (i >= 0)
                        {
                            laneAttributes =
                                    new LaneAttributes(network, laneType, Color.GREEN, LongitudinalDirectionality.DIR_PLUS);
                            structure.put(i, laneAttributes);
                        }
                    }
                }
            }
        }
        return calculateOffsets(network, structure, osmLink, forwards, backwards, warningListener);
    }

    /**
     * Calculates the actual offsets of the individual lanes.
     * @param network the network
     * @param structure SortedMap&lt;Integer,LaneAttributes&gt;; - Sorted Map of Lane Positions and Attributes
     * @param osmLink OSMLink; - The osmLink on which the conversion is based.
     * @param forwards Integer; - Number of forwards oriented lanes.
     * @param backwards Integer; - Number of backwards oriented lanes.
     * @param warningListener WarningListener; the warning listener that receives warning events
     * @return Map containing the lane structure with offsets.
     * @throws NetworkException on failure
     */
    private static Map<Double, LaneAttributes> calculateOffsets(final OTSRoadNetwork network,
            final SortedMap<Integer, LaneAttributes> structure, final OSMLink osmLink, final Integer forwards,
            final Integer backwards, final WarningListener warningListener) throws NetworkException
    {
        LinkedHashMap<Double, LaneAttributes> structurewithOffset = new LinkedHashMap<Double, LaneAttributes>();
        LaneAttributes laneAttributes;
        double currentOffset = 0.0D;
        if (structure.isEmpty())
        {
            warningListener.warning(new WarningEvent(osmLink, "Empty Structure at Link " + osmLink.getId()));
        }
        if (structure.lastKey() >= 0)
        {
            for (int i = 0; i < forwards; i++)
            {
                laneAttributes = structure.get(i);
                if (null == laneAttributes)
                {
                    break;
                }
                double useWidth = laneWidth(network, laneAttributes, osmLink, warningListener);
                laneAttributes.setWidth(useWidth);
                structurewithOffset.put(currentOffset, laneAttributes);
                currentOffset += useWidth;
            }
        }
        if (structure.firstKey() < 0)
        {
            currentOffset = 0.0d;
            for (int i = -1; i >= (0 - backwards); i--)
            {
                laneAttributes = structure.get(i);
                if (null == laneAttributes)
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
                    throw new NetworkException("reverse lane without main lane?");
                }
                double useWidth = laneWidth(network, laneAttributes, osmLink, warningListener);
                laneAttributes.setWidth(useWidth);
                currentOffset -= previousLaneAttributes.getWidth().getSI();
                structurewithOffset.put(currentOffset, laneAttributes);
            }
        }
        return structurewithOffset;
    }

    /**
     * Figure out a reasonable width for a lane.
     * @param network the network
     * @param laneAttributes LaneAttributes; the attributes of the lane
     * @param link OSMLink; the link that owns the lane
     * @param warningListener WarningListener; the warning listener that receives warning events
     * @return double; the width (in meters) of the lane
     */
    static double laneWidth(final OTSRoadNetwork network, final LaneAttributes laneAttributes, final OSMLink link,
            final WarningListener warningListener)
    {
        Double defaultLaneWidth = 3.05d; // TODO This is the German standard car lane width
        boolean widthOverride = false;
        for (OSMTag tag : link.getTags())
        {
            if (tag.getKey().equals("width"))
            {
                String w = tag.getValue().replace(",", ".");
                w = w.replace(" ", "");
                w = w.replace("m", "");
                w = w.replace("Meter", "");
                try
                {
                    defaultLaneWidth = Double.parseDouble(w) / link.getLanes();
                }
                catch (NumberFormatException nfe)
                {
                    System.err.println("Bad lanewidth: \"" + tag.getValue() + "\"");
                }
                widthOverride = true;
            }
        }
        LaneType laneType = laneAttributes.getLaneType();
        if (laneType.isCompatible(network.getGtuType(GTUType.DEFAULTS.CAR), GTUDirectionality.DIR_PLUS)
                || laneType.isCompatible(network.getGtuType(GTUType.DEFAULTS.CAR), GTUDirectionality.DIR_MINUS))
        {
            return defaultLaneWidth;
        }
        else if (laneType.isCompatible(network.getGtuType(GTUType.DEFAULTS.BICYCLE), GTUDirectionality.DIR_PLUS)
                || laneType.isCompatible(network.getGtuType(GTUType.DEFAULTS.BICYCLE), GTUDirectionality.DIR_MINUS))
        {
            return 0.8d; // TODO German default bikepath width
        }
        else if (laneType.isCompatible(network.getGtuType(GTUType.DEFAULTS.PEDESTRIAN), GTUDirectionality.DIR_PLUS)
                || laneType.isCompatible(network.getGtuType(GTUType.DEFAULTS.PEDESTRIAN), GTUDirectionality.DIR_MINUS))
        {
            return 0.95d; // TODO German default footpath width
        }
        else if (laneType.isCompatible(network.getGtuType(GTUType.DEFAULTS.SHIP), GTUDirectionality.DIR_PLUS)
                || laneType.isCompatible(network.getGtuType(GTUType.DEFAULTS.SHIP), GTUDirectionality.DIR_MINUS))
        {
            for (OSMTag tag : link.getTags())
            {
                if (tag.getKey().equals("waterway"))
                {
                    switch (tag.getValue())
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
            warningListener.warning(new WarningEvent(link, "No width given; using default laneWidth for Link " + link.getId()));
        }
        return defaultLaneWidth;
    }

    /**
     * This method creates lanes out of an OSM link LaneTypes are not yet extensive and can be further increased through Tags
     * provided by OSM. The standard lane width of 3.05 is an estimation based on the European width limitation for vehicles
     * (2.55m) + 25cm each side.
     * @param network OTSRoadNetwork; the network
     * @param osmlink Link OSMLink; the OSM link to make lanes for
     * @param simulator OTSSimulatorInterface; the simulator that will animate the generates lanes (if it happens to be an
     *            instance of AnimatorInterface)
     * @param warningListener WarningListener; the warning listener that will receive warning events
     * @return List&lt;Lane&gt;
     * @throws NetworkException on network inconsistency
     * @throws NamingException on naming problems (in the animator)
     * @throws OTSGeometryException when lane contour or center line cannot be instantiated
     */
    public List<Lane> makeLanes(final OTSRoadNetwork network, final OSMLink osmlink, final OTSSimulatorInterface simulator,
            final WarningListener warningListener) throws NetworkException, NamingException, OTSGeometryException
    {
        CrossSectionLink otslink = convertLink(network, osmlink, simulator);
        List<Lane> lanes = new ArrayList<Lane>();
        Map<Double, LaneAttributes> structure = makeStructure(network, osmlink, warningListener);

        int laneNum = 0;
        for (Double offset : structure.keySet())
        {
            laneNum++;
            LaneAttributes laneAttributes = structure.get(offset);
            if (laneAttributes == null)
            {
                break;
            }
            Color color = Color.LIGHT_GRAY;
            LaneType laneType = laneAttributes.getLaneType();
            Length latPos = new Length(offset, LengthUnit.METER);
            Map<GTUType, Speed> speedLimit = new LinkedHashMap<>();
            speedLimit.put(network.getGtuType(GTUType.DEFAULTS.VEHICLE), new Speed(100, SpeedUnit.KM_PER_HOUR));
            Lane newLane = null;
            // FIXME the following code assumes right-hand-side driving.
            if (osmlink.hasTag("hasPreceding") && offset >= 0 || osmlink.hasTag("hasFollowing") && offset < 0)
            {
                color = Color.RED;
                // FIXME overtaking conditions per country and/or type of road?
                newLane = new Lane(otslink, "lane." + laneNum, latPos, latPos, laneAttributes.getWidth(),
                        laneAttributes.getWidth(), laneType, speedLimit);
                new SinkSensor(newLane, new Length(0.25, LengthUnit.METER), Compatible.EVERYTHING, simulator);
            }
            else if (osmlink.hasTag("hasPreceding") && offset < 0 || osmlink.hasTag("hasFollowing") && offset >= 0)
            {
                color = Color.BLUE;
                // FIXME overtaking conditions per country and/or type of road?
                newLane = new Lane(otslink, "lane." + laneNum, latPos, latPos, laneAttributes.getWidth(),
                        laneAttributes.getWidth(), laneType, speedLimit);
            }
            else
            {
                color = laneAttributes.getColor();
                // FIXME overtaking conditions per country and/or type of road?
                newLane = new Lane(otslink, "lane." + laneNum, latPos, latPos, laneAttributes.getWidth(),
                        laneAttributes.getWidth(), laneType, speedLimit);
            }

            // TODO: network.addDrawingInfoBase(newLane, new DrawingInfoShape<Lane>(color));

            lanes.add(newLane);
        }
        return lanes;
    }

    /**
     * This method creates a LaneType which supports all GTUTypes that have been specified in the GTUType List "GTUs".
     * @param network the network
     * @param gtuTypes List&lt;GTUType&gt;; list of GTUTypes
     * @return LaneType permeable for all of the specific GTUTypes
     */
    public static LaneType makeLaneType(final OTSRoadNetwork network, final List<GTUType> gtuTypes)
    {
        StringBuilder name = new StringBuilder();
        for (GTUType gtu : gtuTypes)
        {
            if (name.length() > 0)
            {
                name.append("|");
            }
            name.append(gtu.getId());
        }
        GTUCompatibility<LaneType> compatibility = new GTUCompatibility<>((LaneType) null);
        LaneType result = new LaneType(name.toString(), compatibility, network);
        return result;
    }

    /**
     * This method creates a LaneType which supports the specified GTUType.
     * @param network the network
     * @param gtuType GTUType; the type of GTU that can travel on the new LaneType
     * @return LaneType
     */
    public static LaneType makeLaneType(final OTSRoadNetwork network, final GTUType gtuType)
    {
        List<GTUType> gtuTypes = new ArrayList<GTUType>(1);
        gtuTypes.add(gtuType);
        return makeLaneType(network, gtuTypes);
        // String name = gtuType.getId();
        // LaneType result = new LaneType(name);
        // result.addPermeability(gtuType);
        // return result;
    }

    /**
     * Identify Links that are sources or sinks.
     * @param nodes List&lt;OSMNode&gt;; List of Nodes
     * @param links List&lt;OSMLink&gt;; List of Links
     * @return List of Links which are candidates for becoming sinks/sources.
     */
    private static ArrayList<OSMLink> findBoundaryLinks(final List<OSMNode> nodes, final List<OSMLink> links)
    {
        // TODO: test performance (memory- and time-wise) when the counters are replaced by ArrayList<OSMLink> which
        // would obviate the need to do full searches over all Links to find OSMNodes that are source or sink.
        // Reset the counters (should not be necessary unless this method is called more than once)
        for (OSMNode node : nodes)
        {
            node.linksOriginating = 0;
            node.linksTerminating = 0;
        }
        for (OSMLink link : links)
        {
            link.getStart().linksOriginating++;
            link.getEnd().linksTerminating++;
        }
        ArrayList<OSMNode> foundEndNodes = new ArrayList<OSMNode>();
        for (OSMNode node : nodes)
        {
            if (0 == node.linksOriginating && node.linksTerminating > 0
                    || 0 == node.linksTerminating && node.linksOriginating > 0)
            {
                foundEndNodes.add(node);
            }
        }
        ArrayList<OSMLink> result = new ArrayList<OSMLink>();
        for (OSMLink link : links)
        {
            if (foundEndNodes.contains(link.getStart()) || foundEndNodes.contains(link.getEnd()))
            {
                result.add(link);
            }
        }
        return result;
    }

    /**
     * @param net OSMNetwork; The OSM network which is to be searched for Sinks and Sources.
     * @param progressListener ProgressListener; the progress listener that will receive progress events
     * @return Network with all possible sinks and sources tagged.
     */
    public static OSMNetwork findSinksandSources(final OSMNetwork net, final ProgressListener progressListener)
    {
        progressListener.progress(new ProgressEvent(net, "Counting number of links at each node"));
        List<OSMNode> nodes = new ArrayList<OSMNode>();
        nodes.addAll(net.getNodes().values());
        ArrayList<OSMLink> foundEndpoints = findBoundaryLinks(nodes, net.getLinks());
        progressListener.progress(new ProgressEvent(net, "Adding tags to non-sinks and non-sources"));
        int progress = 0;
        final int progressReportStep = 5000;
        // As tags are immutable we make ONE for following and ONE for preceding
        final OSMTag hasFollowing = new OSMTag("hasFollowing", "");
        final OSMTag hasPreceding = new OSMTag("hasPreceding", "");
        for (OSMLink l : net.getLinks())
        {
            if (foundEndpoints.contains(l))
            {
                if (net.hasFollowingLink(l))
                {
                    l.addTag(hasFollowing);
                }
                else if (net.hasPrecedingLink(l))
                {
                    l.addTag(hasPreceding);
                }
            }
            if (0 == ++progress % progressReportStep)
            {
                progressListener.progress(new ProgressEvent(net, String.format(Locale.US, "%d of %d links processed (%.1f%%)",
                        progress, net.getLinks().size(), 100.0 * progress / net.getLinks().size())));
            }
        }
        progressListener.progress(new ProgressEvent(net, "Found " + foundEndpoints.size() + " Sinks and Sources."));
        return net;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Convert []";
    }
}

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-16 19:20:07 +0200 (Wed, 16 Sep 2015) $, @version $Revision: 1405 $, by $Author: averbraeck $,
 * initial version Mar 3, 2015 <br>
 * @author <a>Moritz Bergmann</a>
 */
class LaneAttributes implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150303L;

    /** Type of the lane (immutable). */
    private final LaneType laneType;

    /** Drawing color of the lane (immutable). */
    private final Color color;

    /** LongitudinalDirectionality of the lane (immutable). */
    private final LongitudinalDirectionality directionality;

    /** Width of the lane. */
    private Length width;

    /**
     * @param network the network
     * @param lt LaneType; - LaneType
     * @param c Color; - Color
     * @param d LongitudinalDirectionality; - LongitudinalDIrectionality
     */
    LaneAttributes(final OTSRoadNetwork network, final LaneType lt, final Color c, final LongitudinalDirectionality d)
    {
        if (lt == null)
        {
            this.laneType = Convert.makeLaneType(network, network.getGtuType(GTUType.DEFAULTS.VEHICLE));
        }
        else
        {
            this.laneType = lt;
        }
        this.color = c;
        this.directionality = d;
    }

    /**
     * @param network the network
     * @param laneType LaneType; - LaneType
     * @param color Color; - Color
     * @param directionality LongitudinalDirectionality; - LongitudinalDIrectionality
     * @param width Double; - width
     */
    LaneAttributes(final OTSRoadNetwork network, final LaneType laneType, final Color color,
            final LongitudinalDirectionality directionality, final Double width)
    {
        if (laneType == null)
        {
            this.laneType = Convert.makeLaneType(network, network.getGtuType(GTUType.DEFAULTS.VEHICLE));
        }
        else
        {
            this.laneType = laneType;
        }
        this.color = color;
        this.directionality = directionality;
        this.setWidth(width);
    }

    /**
     * @return LaneType
     */
    public LaneType getLaneType()
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
    public Length getWidth()
    {
        return this.width;
    }

    /**
     * @param width Double; set width.
     */
    public void setWidth(final Double width)
    {
        Length w = new Length(width, LengthUnit.METER);
        this.width = w;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Lane Attributes: " + this.laneType + "; " + this.color + "; " + this.directionality + "; " + this.width;
    }

}
