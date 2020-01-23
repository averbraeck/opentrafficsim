package org.opentrafficsim.road.network.factory.vissim;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.Bezier;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.factory.vissim.ArcTag.ArcDirection;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.object.sensor.SimpleReportingSensor;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight;
import org.xml.sax.SAXException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision$, by $Author$,
 * initial version Jul 25, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
final class Links
{
    /** Utility class. */
    private Links()
    {
        // do not instantiate
    }

    /**
     * Find the nodes one by one that have one coordinate defined, and one not defined, and try to build the network from there.
     * @param linkTag LinkTag; the link to process
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @param simulator OTSSimulatorInterface; to be able to make the animation
     * @throws OTSGeometryException when both nodes are null.
     * @throws NamingException when node animation cannot link to the animation context.
     * @throws NetworkException when tag type not filled
     */
    static void buildLink(final LinkTag linkTag, final VissimNetworkLaneParser parser, final OTSSimulatorInterface simulator)
            throws OTSGeometryException, NamingException, NetworkException
    {
        NodeTag from = linkTag.nodeStartTag;
        OTSPoint3D startPoint = new OTSPoint3D(from.coordinate);
        double startAngle = 0.0;
        if (linkTag.offsetStart != null && linkTag.offsetStart.si != 0.0)
        {
            // shift the start point perpendicular to the node direction or read from tag
            double offset = linkTag.offsetStart.si;
            startPoint = new OTSPoint3D(startPoint.x + offset * Math.cos(startAngle + Math.PI / 2.0),
                    startPoint.y + offset * Math.sin(startAngle + Math.PI / 2.0), startPoint.z);
            System.out
                    .println("fc = " + from.coordinate + ", sa = " + startAngle + ", so = " + offset + ", sp = " + startPoint);
        }

        NodeTag to = linkTag.nodeEndTag;
        OTSPoint3D endPoint = new OTSPoint3D(to.coordinate);
        double endAngle = 0.0;
        if (linkTag.offsetEnd != null && linkTag.offsetEnd.si != 0.0)
        {
            // shift the start point perpendicular to the node direction or read from tag
            double offset = linkTag.offsetEnd.si;
            endPoint = new OTSPoint3D(endPoint.x + offset * Math.cos(endAngle + Math.PI / 2.0),
                    endPoint.y + offset * Math.sin(endAngle + Math.PI / 2.0), endPoint.z);
            System.out.println("tc = " + to.coordinate + ", ea = " + endAngle + ", eo = " + offset + ", ep = " + endPoint);
        }

        OTSPoint3D[] coordinates = null;

        if (linkTag.straightTag != null)
        {
            coordinates = new OTSPoint3D[2];
            coordinates[0] = startPoint;
            coordinates[1] = endPoint;
        }

        else if (linkTag.polyLineTag != null)
        {
            int intermediatePoints = linkTag.polyLineTag.vertices.length;
            coordinates = new OTSPoint3D[intermediatePoints + 2];
            coordinates[0] = startPoint;
            coordinates[intermediatePoints + 1] = endPoint;
            for (int p = 0; p < intermediatePoints; p++)
            {
                coordinates[p + 1] = linkTag.polyLineTag.vertices[p];
            }

        }
        else if (linkTag.arcTag != null)
        {
            // TODO move the radius if there is an start and end offset? How?
            int points = (Math.abs(linkTag.arcTag.angle.getInUnit()) <= Math.PI / 2.0) ? 64 : 128;
            coordinates = new OTSPoint3D[points];
            coordinates[0] = new OTSPoint3D(from.coordinate.x, from.coordinate.y, from.coordinate.z);
            coordinates[coordinates.length - 1] = new OTSPoint3D(to.coordinate.x, to.coordinate.y, to.coordinate.z);
            double angleStep = linkTag.arcTag.angle.getInUnit() / points;
            double slopeStep = (to.coordinate.z - from.coordinate.z) / points;
            double radiusSI = linkTag.arcTag.radius.getSI();
            if (linkTag.arcTag.direction.equals(ArcDirection.RIGHT))
            {
                for (int p = 1; p < points - 1; p++)
                {
                    coordinates[p] = new OTSPoint3D(
                            linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle - angleStep * p),
                            linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle - angleStep * p),
                            from.coordinate.z + slopeStep * p);
                }
            }
            else
            {
                for (int p = 1; p < points - 1; p++)
                {
                    try
                    {
                        System.err.println("linkTag.arcTag.center = " + linkTag.arcTag.center);
                        System.err.println("linkTag.arcTag.startAngle = " + linkTag.arcTag.startAngle);
                        coordinates[p] = new OTSPoint3D(
                                linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle + angleStep * p),
                                linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle + angleStep * p),
                                from.coordinate.z + slopeStep * p);
                    }
                    catch (NullPointerException npe)
                    {
                        npe.printStackTrace();
                        System.err.println(npe.getMessage());
                    }
                }
            }
        }

        else if (linkTag.bezierTag != null)
        {
            if (new DirectedPoint(startPoint.x, startPoint.y, startPoint.z, 0, 0, startAngle)
                    .equals(new DirectedPoint(endPoint.x, endPoint.y, endPoint.z, 0, 0, endAngle)))
            {
                System.out.println("   ");
            }

            coordinates = Bezier.cubic(128, new DirectedPoint(startPoint.x, startPoint.y, startPoint.z, 0, 0, startAngle),
                    new DirectedPoint(endPoint.x, endPoint.y, endPoint.z, 0, 0, endAngle)).getPoints();
        }

        else
        {
            throw new NetworkException(
                    "Making link, but link " + linkTag.name + " has no filled straight, arc, or bezier curve");
        }
        if (coordinates.length < 2)
        {
            throw new OTSGeometryException(
                    "Degenerate OTSLine3D; has " + coordinates.length + " point" + (coordinates.length != 1 ? "s" : ""));
        }
        OTSLine3D designLine = OTSLine3D.createAndCleanOTSLine3D(coordinates);

        // Directionality has to be added later when the lanes and their direction are known.
        CrossSectionLink link =
                new CrossSectionLink(parser.getNetwork(), linkTag.name, linkTag.nodeStartTag.node, linkTag.nodeEndTag.node,
                        parser.network.getLinkType(LinkType.DEFAULTS.ROAD), designLine, simulator, linkTag.laneKeepingPolicy);
        linkTag.link = link;
    }

    /**
     * @param linkTag LinkTag; the link to process
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; to be able to make the animation
     * @throws NetworkException when the stripe cannot be instantiated
     * @throws NamingException when the /animation/2D tree cannot be found in the context
     * @throws SAXException when the stripe type cannot be parsed correctly
     * @throws GTUException when lane block cannot be created
     * @throws OTSGeometryException when construction of the offset-line or contour fails
     * @throws SimRuntimeException when construction of the generator fails
     */
    @SuppressWarnings({"checkstyle:needbraces", "checkstyle:methodlength"})
    static void applyRoadTypeToLink(final LinkTag linkTag, final VissimNetworkLaneParser parser,
            final DEVSSimulatorInterface.TimeDoubleUnit simulator)
            throws NetworkException, NamingException, SAXException, GTUException, OTSGeometryException, SimRuntimeException
    {
        CrossSectionLink csl = linkTag.link;

        List<CrossSectionElement> cseList = new ArrayList<>();
        List<Lane> lanes = new ArrayList<>();
        // TODO Map<GTUType, LongitudinalDirectionality> linkDirections = new LinkedHashMap<>();
        LongitudinalDirectionality linkDirection = LongitudinalDirectionality.DIR_NONE;

        // add information from the lanes
        // first the total width is computed
        // The offset of the lanes will be computed to match this Vissim layout
        Double roadWidth = 0.0;
        for (LaneTag laneTag : linkTag.laneTags.values())
        {
            roadWidth += Double.parseDouble(laneTag.width);
        }

        Color color;
        if (linkTag.connector)
        {
            color = Color.LIGHT_GRAY;
        }
        else
        {
            color = Color.DARK_GRAY;

        }

        // The lanes are ordered from the outside to the inner side of the road
        Double totalLaneWidth = 0.0;

        if (!linkTag.connector)
        {
            for (LaneTag laneTag : linkTag.laneTags.values())
            {
                String name = laneTag.laneNo;
                Double laneWidth = Double.parseDouble(laneTag.width);
                Length thisLaneWidth = new Length(laneWidth, LengthUnit.METER);

                // the road offset is negative if the lanes are at the right side of the median (for right hand rule)
                // Vissim puts the lanes around the centerline of the road (all lanes joined)
                // therefore we use a factor 0.5 * roadWidth....
                Double negativeOffset = -(0.5 * roadWidth - totalLaneWidth - laneWidth / 2);
                Length lateralOffset = new Length(negativeOffset, LengthUnit.METER);

                LaneType laneType = parser.network.getLaneType(LaneType.DEFAULTS.FREEWAY);
                Speed speedLimit = new Speed(Double.parseDouble(linkTag.legalSpeed), SpeedUnit.KM_PER_HOUR);
                // OvertakingConditions overtakingConditions; TODO (not clear yet)
                Lane lane = new Lane(csl, name, lateralOffset, thisLaneWidth, laneType, speedLimit);
                if (!linkTag.sensors.isEmpty())
                {
                    for (SensorTag sensorTag : linkTag.sensors)
                    {
                        if (sensorTag.laneName.equals(laneTag.laneNo))
                        {
                            Length pos = new Length(Double.parseDouble(sensorTag.positionStr), LengthUnit.METER);
                            if (pos.lt(lane.getLength()))
                            {
                                try
                                {
                                    new SimpleReportingSensor(sensorTag.laneName + ".S" + sensorTag.name, lane, pos,
                                            RelativePosition.FRONT, simulator, Compatible.EVERYTHING);
                                }
                                catch (Exception exception)
                                {
                                    System.err.println(exception.getMessage());
                                }
                            }
                        }
                    }
                }

                if (!linkTag.signalHeads.isEmpty())
                {
                    for (SignalHeadTag signalHeadTag : linkTag.signalHeads)
                    {
                        if (signalHeadTag.laneName.equals(laneTag.laneNo))
                        {
                            Length pos = new Length(Double.parseDouble(signalHeadTag.positionStr), LengthUnit.METER);
                            if (pos.lt(lane.getLength()))
                            {
                                new SimpleTrafficLight(signalHeadTag.no, lane, pos, simulator);
                            }
                        }
                    }
                }

                cseList.add(lane);

                lanes.add(lane);
                linkTag.lanes.put(name, lane);
                // update totalLaneWidth
                totalLaneWidth += Double.parseDouble(laneTag.width);

                // TODO: parser.network.addDrawingInfoBase(lane, new DrawingInfoShape<Lane>(color));
            }
        }

    }

    /**
     * @param linkTag LinkTag; the link to process
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; to be able to make the animation
     * @throws NetworkException when the stripe cannot be instantiated
     * @throws NamingException when the /animation/2D tree cannot be found in the context
     * @throws SAXException when the stripe type cannot be parsed correctly
     * @throws GTUException when lane block cannot be created
     * @throws OTSGeometryException when construction of the offset-line or contour fails
     * @throws SimRuntimeException when construction of the generator fails
     */
    @SuppressWarnings({"checkstyle:needbraces", "checkstyle:methodlength"})
    static void applyRoadTypeToConnector(final LinkTag linkTag, final VissimNetworkLaneParser parser,
            final DEVSSimulatorInterface.TimeDoubleUnit simulator)
            throws NetworkException, NamingException, SAXException, GTUException, OTSGeometryException, SimRuntimeException
    {
        CrossSectionLink csl = linkTag.link;

        List<CrossSectionElement> cseList = new ArrayList<>();
        List<Lane> lanes = new ArrayList<>();

        // add information from the lanes
        // first the total width is computed
        // The offset of the lanes will be computed to match this Vissim layout
        Double roadWidth = 0.0;
        for (LaneTag laneTag : linkTag.laneTags.values())
        {
            roadWidth += Double.parseDouble(laneTag.width);
        }

        Color color;
        if (linkTag.connector)
        {
            color = Color.LIGHT_GRAY;
        }
        else
        {
            color = Color.DARK_GRAY;

        }

        // some generic definitions necessary to create a Lane object
        LaneType laneType = parser.network.getLaneType(LaneType.DEFAULTS.FREEWAY);
        Speed speedLimit = new Speed(Double.parseDouble(linkTag.legalSpeed), SpeedUnit.KM_PER_HOUR);

        // The lanes are ordered from the outside to the inner side of the road
        // only lanes from connectors are being set
        if (linkTag.connector)
        {
            Double totalFromLaneWidth = -1.75;
            Double totalToLaneWidth = -1.75;
            // find the link and lane downstream
            LinkTag linkToTag = parser.getRealLinkTags().get(linkTag.connectorTag.toLinkNo);
            Lane laneTo = linkToTag.lanes.get(linkTag.connectorTag.toLaneNo);

            // find the link and lane upstream
            LinkTag linkFromTag = parser.getRealLinkTags().get(linkTag.connectorTag.fromLinkNo);
            Lane laneFrom = linkFromTag.lanes.get(linkTag.connectorTag.fromLaneNo);

            // loop along all lanes (Tags)
            for (LaneTag connectLaneTag : linkTag.laneTags.values())
            {
                // the name (number) of the downstream lane (the order is from outer to center)
                String name = connectLaneTag.laneNo;
                // the width of the current lane
                Length thisLaneWidth = new Length(Double.parseDouble(connectLaneTag.width), LengthUnit.METER);
                // the total lanewidth of all previous lanes that already have been looped through
                Length totfromLaneWidth = new Length(totalFromLaneWidth, LengthUnit.METER);
                Length totToLaneWidth = new Length(totalToLaneWidth, LengthUnit.METER);

                Length lateralOffsetStart;
                Length lateralOffsetEnd =
                        laneTo.getLateralBoundaryPosition(LateralDirectionality.RIGHT, 0).plus(totToLaneWidth);
                // the lateral offset
                if (laneFrom != null)
                {
                    lateralOffsetStart =
                            laneFrom.getLateralBoundaryPosition(LateralDirectionality.RIGHT, 0).plus(totfromLaneWidth);
                }
                else
                {
                    lateralOffsetStart = lateralOffsetEnd;
                }

                // the road offset is negative if the lanes are at the right side of the median (for right hand rule)
                // OvertakingConditions overtakingConditions; TODO (not clear yet)
                Lane lane = new Lane(csl, name, lateralOffsetStart, lateralOffsetEnd, thisLaneWidth, thisLaneWidth, laneType,
                        speedLimit);
                cseList.add(lane);
                lanes.add(lane);
                linkTag.lanes.put(name, lane);

                // update totalLaneWidth
                totalFromLaneWidth += Double.parseDouble(connectLaneTag.width);
                totalToLaneWidth += Double.parseDouble(connectLaneTag.width);

                // TODO: parser.network.addDrawingInfoBase(lane, new DrawingInfoShape<Lane>(color));

            }
        }

    }

    /**
     * @param realLinkTag LinkTag;
     * @param vissimNetworkLaneParser VissimNetworkLaneParser;
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit;
     * @throws NetworkException
     */
    public static void createSinkSensor(LinkTag realLinkTag, VissimNetworkLaneParser vissimNetworkLaneParser,
            DEVSSimulatorInterface.TimeDoubleUnit simulator) throws NetworkException
    {
        if (!realLinkTag.connector)
        {
            boolean deadLink = true;
            for (LinkTag linkTag : vissimNetworkLaneParser.getLinkTags().values())
            {
                if (realLinkTag.nodeEndTag.name.equals(linkTag.nodeStartTag.name) && !realLinkTag.equals(linkTag))
                {
                    deadLink = false;
                }
            }

            if (deadLink)
            {
                for (Lane lane : realLinkTag.lanes.values())
                {
                    Double smallest = Math.min(10, lane.getLength().getInUnit(LengthUnit.METER) - 1);
                    Length beforeEnd = new Length(smallest, LengthUnit.METER);
                    Length pos = lane.getLength().minus(beforeEnd);
                    SinkSensor sensor = new SinkSensor(lane, pos, Compatible.EVERYTHING, simulator);
                    lane.getSensors().add(sensor);
                }
            }
        }

    }

}
