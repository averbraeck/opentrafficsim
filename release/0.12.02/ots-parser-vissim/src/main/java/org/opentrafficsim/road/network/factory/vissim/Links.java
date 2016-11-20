package org.opentrafficsim.road.network.factory.vissim;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;

import org.djunits.unit.AngleUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.AngleUtil;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.Bezier;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.animation.LaneAnimation;
import org.opentrafficsim.road.network.factory.vissim.ArcTag.ArcDirection;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.object.sensor.SimpleReportingSensor;
import org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight;
import org.xml.sax.SAXException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.AnimatorInterface;
import nl.tudelft.simulation.language.d3.CartesianPoint;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision$, by $Author$, initial
 * version Jul 25, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
final class Links {
    /** Utility class. */
    private Links() {
        // do not instantiate
    }

    /**
     * Find the nodes one by one that have one coordinate defined, and one not defined, and try to build the network from there.
     * @param parser the parser with the lists of information
     * @throws NetworkException when both nodes are null.
     * @throws NamingException when node animation cannot link to the animation context.
     */
    @SuppressWarnings("methodlength")
    static void calculateNodeCoordinates(final VissimNetworkLaneParser parser) throws NetworkException, NamingException {
        // are there straight tags with nodes without an angle?
        for (LinkTag linkTag : parser.linkTags.values()) {
            if (linkTag.straightTag != null && linkTag.nodeStartTag.coordinate != null
                && linkTag.nodeEndTag.coordinate != null) {
                if (linkTag.nodeStartTag.angle == null) {
                    double dx = linkTag.nodeEndTag.coordinate.x - linkTag.nodeStartTag.coordinate.x;
                    double dy = linkTag.nodeEndTag.coordinate.y - linkTag.nodeStartTag.coordinate.y;
                    linkTag.nodeStartTag.angle = new Direction(Math.atan2(dy, dx), AngleUnit.RADIAN);
                }
                if (linkTag.nodeEndTag.angle == null) {
                    double dx = linkTag.nodeEndTag.coordinate.x - linkTag.nodeStartTag.coordinate.x;
                    double dy = linkTag.nodeEndTag.coordinate.y - linkTag.nodeStartTag.coordinate.y;
                    linkTag.nodeEndTag.angle = new Direction(Math.atan2(dy, dx), AngleUnit.RADIAN);
                }
            }
        }

        // see if we can find the coordinates of the nodes that have not yet been fixed.
        Set<NodeTag> nodeTags = new HashSet<>();
        for (LinkTag linkTag : parser.linkTags.values()) {

            if (linkTag.nodeStartTag.coordinate == null) {
                nodeTags.add(linkTag.nodeStartTag);
            }
            if (linkTag.nodeEndTag.coordinate == null) {
                nodeTags.add(linkTag.nodeEndTag);
            }
        }

        while (nodeTags.size() > 0) {
            boolean found = false;
            for (LinkTag linkTag : parser.linkTags.values()) {
                if (linkTag.straightTag != null || linkTag.polyLineTag != null || linkTag.arcTag != null) {
                    if (nodeTags.contains(linkTag.nodeStartTag) == nodeTags.contains(linkTag.nodeEndTag)) {
                        continue;
                    }

                    if (linkTag.straightTag != null) {
                        double lengthSI = linkTag.straightTag.length.getSI();
                        if (linkTag.nodeEndTag.node == null) {
                            CartesianPoint coordinate = new CartesianPoint(linkTag.nodeStartTag.node.getLocation().getX(),
                                linkTag.nodeStartTag.node.getLocation().getY(), linkTag.nodeStartTag.node.getLocation()
                                    .getZ());
                            double angle = linkTag.nodeStartTag.node.getDirection().getSI();
                            double slope = linkTag.nodeStartTag.node.getSlope().getSI();
                            coordinate.x += lengthSI * Math.cos(angle);
                            coordinate.y += lengthSI * Math.sin(angle);
                            coordinate.z += lengthSI * Math.sin(slope);
                            NodeTag nodeTag = linkTag.nodeEndTag;
                            nodeTag.angle = new Direction(angle, AngleUnit.SI);
                            nodeTag.coordinate = new OTSPoint3D(coordinate.x, coordinate.y, coordinate.z);
                            nodeTag.slope = new Direction(slope, AngleUnit.SI);
                            linkTag.nodeEndTag.node = NodeTag.makeOTSNode(nodeTag, parser);
                            nodeTags.remove(linkTag.nodeEndTag);
                        } else if (linkTag.nodeStartTag.node == null) {
                            CartesianPoint coordinate = new CartesianPoint(linkTag.nodeEndTag.node.getLocation().getX(),
                                linkTag.nodeEndTag.node.getLocation().getY(), linkTag.nodeEndTag.node.getLocation().getZ());
                            double angle = linkTag.nodeEndTag.node.getDirection().getSI();
                            double slope = linkTag.nodeEndTag.node.getSlope().getSI();
                            coordinate.x -= lengthSI * Math.cos(angle);
                            coordinate.y -= lengthSI * Math.sin(angle);
                            coordinate.z -= lengthSI * Math.sin(slope);
                            NodeTag nodeTag = linkTag.nodeStartTag;
                            nodeTag.angle = new Direction(angle, AngleUnit.SI);
                            nodeTag.coordinate = new OTSPoint3D(coordinate.x, coordinate.y, coordinate.z);
                            nodeTag.slope = new Direction(slope, AngleUnit.SI);
                            linkTag.nodeStartTag.node = NodeTag.makeOTSNode(nodeTag, parser);
                            nodeTags.remove(linkTag.nodeStartTag);
                        }
                    } else if (linkTag.polyLineTag != null) {
                        double lengthSI = linkTag.polyLineTag.length.getSI();
                        // TODO create for polyLine
                        // if (linkTag.nodeEndTag.node == null) {
                        // CartesianPoint coordinate = new CartesianPoint(linkTag.nodeStartTag.node.getLocation().getX(),
                        // linkTag.nodeStartTag.node.getLocation().getY(), linkTag.nodeStartTag.node.getLocation()
                        // .getZ());
                        // double angle = linkTag.nodeStartTag.node.getDirection().getSI();
                        // double slope = linkTag.nodeStartTag.node.getSlope().getSI();
                        // coordinate.x += lengthSI * Math.cos(angle);
                        // coordinate.y += lengthSI * Math.sin(angle);
                        // coordinate.z += lengthSI * Math.sin(slope);
                        // NodeTag nodeTag = linkTag.nodeEndTag;
                        // nodeTag.angle = new Direction(angle, AngleUnit.SI);
                        // nodeTag.coordinate = new OTSPoint3D(coordinate.x, coordinate.y, coordinate.z);
                        // nodeTag.slope = new Direction(slope, AngleUnit.SI);
                        // linkTag.nodeEndTag.node = NodeTag.makeOTSNode(nodeTag, parser);
                        // nodeTags.remove(linkTag.nodeEndTag);
                        // } else if (linkTag.nodeStartTag.node == null) {
                        // CartesianPoint coordinate = new CartesianPoint(linkTag.nodeEndTag.node.getLocation().getX(),
                        // linkTag.nodeEndTag.node.getLocation().getY(), linkTag.nodeEndTag.node.getLocation().getZ());
                        // double angle = linkTag.nodeEndTag.node.getDirection().getSI();
                        // double slope = linkTag.nodeEndTag.node.getSlope().getSI();
                        // coordinate.x -= lengthSI * Math.cos(angle);
                        // coordinate.y -= lengthSI * Math.sin(angle);
                        // coordinate.z -= lengthSI * Math.sin(slope);
                        // NodeTag nodeTag = linkTag.nodeStartTag;
                        // nodeTag.angle = new Direction(angle, AngleUnit.SI);
                        // nodeTag.coordinate = new OTSPoint3D(coordinate.x, coordinate.y, coordinate.z);
                        // nodeTag.slope = new Direction(slope, AngleUnit.SI);
                        // linkTag.nodeStartTag.node = NodeTag.makeOTSNode(nodeTag, parser);
                        // nodeTags.remove(linkTag.nodeStartTag);
                        // }
                    } else if (linkTag.arcTag != null) {
                        double radiusSI = linkTag.arcTag.radius.getSI();
                        double angle = linkTag.arcTag.angle.getSI();
                        ArcDirection direction = linkTag.arcTag.direction;

                        if (linkTag.nodeEndTag.node == null) {
                            CartesianPoint coordinate = new CartesianPoint(0.0, 0.0, 0.0);
                            double startAngle = linkTag.nodeStartTag.node.getDirection().getSI();
                            double slope = linkTag.nodeStartTag.node.getSlope().getSI();
                            double lengthSI = radiusSI * angle;
                            NodeTag nodeTag = linkTag.nodeEndTag;
                            if (direction.equals(ArcDirection.LEFT)) {
                                linkTag.arcTag.center = new OTSPoint3D(linkTag.nodeStartTag.node.getLocation().getX()
                                    + radiusSI * Math.cos(startAngle + Math.PI / 2.0), linkTag.nodeStartTag.node
                                        .getLocation().getY() + radiusSI * Math.sin(startAngle + Math.PI / 2.0), 0.0);
                                linkTag.arcTag.startAngle = startAngle - Math.PI / 2.0;
                                coordinate.x = linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle
                                    + angle);
                                coordinate.y = linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle
                                    + angle);
                                nodeTag.angle = new Direction(AngleUtil.normalize(startAngle + angle), AngleUnit.SI);
                            } else {
                                linkTag.arcTag.center = new OTSPoint3D(linkTag.nodeStartTag.node.getLocation().getX()
                                    - radiusSI * Math.cos(startAngle + Math.PI / 2.0), linkTag.nodeStartTag.node
                                        .getLocation().getY() - radiusSI * Math.sin(startAngle + Math.PI / 2.0), 0.0);
                                linkTag.arcTag.startAngle = startAngle + Math.PI / 2.0;
                                coordinate.x = linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle
                                    - angle);
                                coordinate.y = linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle
                                    - angle);
                                nodeTag.angle = new Direction(AngleUtil.normalize(startAngle - angle), AngleUnit.SI);
                            }
                            coordinate.z = linkTag.nodeStartTag.node.getLocation().getZ() + lengthSI * Math.sin(slope);
                            nodeTag.slope = new Direction(slope, AngleUnit.SI);
                            nodeTag.coordinate = new OTSPoint3D(coordinate.x, coordinate.y, coordinate.z);
                            linkTag.nodeEndTag.node = NodeTag.makeOTSNode(nodeTag, parser);
                            nodeTags.remove(linkTag.nodeEndTag);
                        }

                        else if (linkTag.nodeStartTag.node == null) {
                            CartesianPoint coordinate = new CartesianPoint(linkTag.nodeEndTag.node.getLocation().getX(),
                                linkTag.nodeEndTag.node.getLocation().getY(), linkTag.nodeEndTag.node.getLocation().getZ());
                            double endAngle = linkTag.nodeEndTag.node.getDirection().getSI();
                            double slope = linkTag.nodeEndTag.node.getSlope().getSI();
                            double lengthSI = radiusSI * angle;
                            NodeTag nodeTag = linkTag.nodeStartTag;
                            if (direction.equals(ArcDirection.LEFT)) {
                                linkTag.arcTag.center = new OTSPoint3D(coordinate.x + radiusSI * Math.cos(endAngle + Math.PI
                                    / 2.0), coordinate.y + radiusSI * Math.sin(endAngle + Math.PI / 2.0), 0.0);
                                linkTag.arcTag.startAngle = endAngle - Math.PI / 2.0 - angle;
                                coordinate.x = linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle);
                                coordinate.y = linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle);
                                nodeTag.angle = new Direction(AngleUtil.normalize(linkTag.arcTag.startAngle + Math.PI / 2.0),
                                    AngleUnit.SI);
                            } else {
                                linkTag.arcTag.center = new OTSPoint3D(coordinate.x + radiusSI * Math.cos(endAngle - Math.PI
                                    / 2.0), coordinate.y + radiusSI * Math.sin(endAngle - Math.PI / 2.0), 0.0);
                                linkTag.arcTag.startAngle = endAngle + Math.PI / 2.0 + angle;
                                coordinate.x = linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle);
                                coordinate.y = linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle);
                                nodeTag.angle = new Direction(AngleUtil.normalize(linkTag.arcTag.startAngle - Math.PI / 2.0),
                                    AngleUnit.SI);
                            }
                            coordinate.z -= lengthSI * Math.sin(slope);
                            nodeTag.coordinate = new OTSPoint3D(coordinate.x, coordinate.y, coordinate.z);
                            nodeTag.slope = new Direction(slope, AngleUnit.SI);
                            linkTag.nodeStartTag.node = NodeTag.makeOTSNode(nodeTag, parser);
                            nodeTags.remove(linkTag.nodeStartTag);
                        }
                    }
                }
            }
            if (!found) {
                throw new NetworkException("Cannot find coordinates of one or more nodes");
            }
        }

        // are there straight tags with nodes without an angle?
        for (LinkTag linkTag : parser.linkTags.values()) {
            if (linkTag.straightTag != null && linkTag.nodeStartTag.coordinate != null
                && linkTag.nodeEndTag.coordinate != null) {
                double dx = linkTag.nodeEndTag.coordinate.x - linkTag.nodeStartTag.coordinate.x;
                double dy = linkTag.nodeEndTag.coordinate.y - linkTag.nodeStartTag.coordinate.y;
                linkTag.nodeStartTag.angle = new Direction(Math.atan2(dy, dx), AngleUnit.RADIAN);
                dx = linkTag.nodeEndTag.coordinate.x - linkTag.nodeStartTag.coordinate.x;
                dy = linkTag.nodeEndTag.coordinate.y - linkTag.nodeStartTag.coordinate.y;
                linkTag.nodeEndTag.angle = new Direction(Math.atan2(dy, dx), AngleUnit.RADIAN);
            }
        }

        // are there polyLine tags with nodes without an angle?

        for (LinkTag linkTag : parser.linkTags.values()) {
            if (linkTag.polyLineTag != null && linkTag.nodeStartTag.coordinate != null
                && linkTag.nodeEndTag.coordinate != null) {
                double dx = linkTag.polyLineTag.vertices[0].x - linkTag.nodeStartTag.coordinate.x;
                double dy = linkTag.polyLineTag.vertices[0].y - linkTag.nodeStartTag.coordinate.y;
                linkTag.nodeStartTag.angle = new Direction(Math.atan2(dy, dx), AngleUnit.RADIAN);
                int arrayLength = linkTag.polyLineTag.vertices.length;
                dx = linkTag.nodeEndTag.coordinate.x - linkTag.polyLineTag.vertices[arrayLength - 1].x;
                dy = linkTag.nodeEndTag.coordinate.y - linkTag.polyLineTag.vertices[arrayLength - 1].y;
                linkTag.nodeEndTag.angle = new Direction(Math.atan2(dy, dx), AngleUnit.RADIAN);
            }
        }

        // which nodes have not yet been created?
        for (NodeTag nodeTag : parser.nodeTags.values()) {
            if (nodeTag.coordinate != null && nodeTag.node == null) {
                if (nodeTag.angle == null) {
                    nodeTag.angle = Direction.ZERO;
                }
                if (nodeTag.slope == null) {
                    nodeTag.slope = Direction.ZERO;
                }
                nodeTag.node = NodeTag.makeOTSNode(nodeTag, parser);
            }
        }

    }

    /**
     * Find the nodes one by one that have one coordinate defined, and one not defined, and try to build the network from there.
     * @param linkTag the link to process
     * @param parser the parser with the lists of information
     * @param simulator to be able to make the animation
     * @throws OTSGeometryException when both nodes are null.
     * @throws NamingException when node animation cannot link to the animation context.
     * @throws NetworkException when tag type not filled
     */
    static void buildLink(final LinkTag linkTag, final VissimNetworkLaneParser parser,
        final OTSDEVSSimulatorInterface simulator) throws OTSGeometryException, NamingException, NetworkException {
        NodeTag from = linkTag.nodeStartTag;
        OTSPoint3D startPoint = new OTSPoint3D(from.coordinate);
        double startAngle = 0.0;
        if (from.angle != null) {
            startAngle = from.angle.si;
        }
        if (linkTag.offsetStart != null && linkTag.offsetStart.si != 0.0) {
            // shift the start point perpendicular to the node direction or read from tag
            double offset = linkTag.offsetStart.si;
            startPoint = new OTSPoint3D(startPoint.x + offset * Math.cos(startAngle + Math.PI / 2.0), startPoint.y + offset
                * Math.sin(startAngle + Math.PI / 2.0), startPoint.z);
            System.out.println("fc = " + from.coordinate + ", sa = " + startAngle + ", so = " + offset + ", sp = "
                + startPoint);
        }

        NodeTag to = linkTag.nodeEndTag;
        OTSPoint3D endPoint = new OTSPoint3D(to.coordinate);
        double endAngle = to.angle.si;
        if (linkTag.offsetEnd != null && linkTag.offsetEnd.si != 0.0) {
            // shift the start point perpendicular to the node direction or read from tag
            double offset = linkTag.offsetEnd.si;
            endPoint = new OTSPoint3D(endPoint.x + offset * Math.cos(endAngle + Math.PI / 2.0), endPoint.y + offset * Math
                .sin(endAngle + Math.PI / 2.0), endPoint.z);
            System.out.println("tc = " + to.coordinate + ", ea = " + endAngle + ", eo = " + offset + ", ep = " + endPoint);
        }

        OTSPoint3D[] coordinates = null;

        if (linkTag.straightTag != null) {
            coordinates = new OTSPoint3D[2];
            coordinates[0] = startPoint;
            coordinates[1] = endPoint;
        }

        else if (linkTag.polyLineTag != null) {
            int intermediatePoints = linkTag.polyLineTag.vertices.length;
            coordinates = new OTSPoint3D[intermediatePoints + 2];
            coordinates[0] = startPoint;
            coordinates[intermediatePoints + 1] = endPoint;
            for (int p = 0; p < intermediatePoints; p++) {
                coordinates[p + 1] = linkTag.polyLineTag.vertices[p];
            }

        } else if (linkTag.arcTag != null) {
            // TODO move the radius if there is an start and end offset? How?
            int points = (Math.abs(linkTag.arcTag.angle.getSI()) <= Math.PI / 2.0) ? 64 : 128;
            coordinates = new OTSPoint3D[points];
            coordinates[0] = new OTSPoint3D(from.coordinate.x, from.coordinate.y, from.coordinate.z);
            coordinates[coordinates.length - 1] = new OTSPoint3D(to.coordinate.x, to.coordinate.y, to.coordinate.z);
            double angleStep = linkTag.arcTag.angle.getSI() / points;
            double slopeStep = (to.coordinate.z - from.coordinate.z) / points;
            double radiusSI = linkTag.arcTag.radius.getSI();
            if (linkTag.arcTag.direction.equals(ArcDirection.RIGHT)) {
                for (int p = 1; p < points - 1; p++) {
                    coordinates[p] = new OTSPoint3D(linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle
                        - angleStep * p), linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle - angleStep
                            * p), from.coordinate.z + slopeStep * p);
                }
            } else {
                for (int p = 1; p < points - 1; p++) {
                    try {
                        System.err.println("linkTag.arcTag.center = " + linkTag.arcTag.center);
                        System.err.println("linkTag.arcTag.startAngle = " + linkTag.arcTag.startAngle);
                        coordinates[p] = new OTSPoint3D(linkTag.arcTag.center.x + radiusSI * Math.cos(
                            linkTag.arcTag.startAngle + angleStep * p), linkTag.arcTag.center.y + radiusSI * Math.sin(
                                linkTag.arcTag.startAngle + angleStep * p), from.coordinate.z + slopeStep * p);
                    } catch (NullPointerException npe) {
                        npe.printStackTrace();
                        System.err.println(npe.getMessage());
                    }
                }
            }
        }

        else if (linkTag.bezierTag != null) {
            if (new DirectedPoint(startPoint.x, startPoint.y, startPoint.z, 0, 0, startAngle).equals(new DirectedPoint(
                endPoint.x, endPoint.y, endPoint.z, 0, 0, endAngle))) {
                System.out.println("   ");
            }

            coordinates = Bezier.cubic(128, new DirectedPoint(startPoint.x, startPoint.y, startPoint.z, 0, 0, startAngle),
                new DirectedPoint(endPoint.x, endPoint.y, endPoint.z, 0, 0, endAngle)).getPoints();
        }

        else {
            throw new NetworkException("Making link, but link " + linkTag.name
                + " has no filled straight, arc, or bezier curve");
        }
        if (coordinates.length < 2) {
            throw new OTSGeometryException("Degenerate OTSLine3D; has " + coordinates.length + " point"
                + (coordinates.length != 1 ? "s" : ""));
        }
        OTSLine3D designLine = OTSLine3D.createAndCleanOTSLine3D(coordinates);

        // Directionality has to be added later when the lanes and their direction are known.
        CrossSectionLink link = new CrossSectionLink(parser.network, linkTag.name, linkTag.nodeStartTag.node,
            linkTag.nodeEndTag.node, LinkType.ALL, designLine, new HashMap<GTUType, LongitudinalDirectionality>(),
            linkTag.laneKeepingPolicy);
        linkTag.link = link;
    }

    /**
     * @param linkTag the link to process
     * @param parser the parser with the lists of information
     * @param simulator to be able to make the animation
     * @throws NetworkException when the stripe cannot be instantiated
     * @throws NamingException when the /animation/2D tree cannot be found in the context
     * @throws SAXException when the stripe type cannot be parsed correctly
     * @throws GTUException when lane block cannot be created
     * @throws OTSGeometryException when construction of the offset-line or contour fails
     * @throws SimRuntimeException when construction of the generator fails
     */
    @SuppressWarnings({"checkstyle:needbraces", "checkstyle:methodlength"})
    static void applyRoadTypeToLink(final LinkTag linkTag, final VissimNetworkLaneParser parser,
        final OTSDEVSSimulatorInterface simulator) throws NetworkException, NamingException, SAXException, GTUException,
            OTSGeometryException, SimRuntimeException {
        CrossSectionLink csl = linkTag.link;

        List<CrossSectionElement> cseList = new ArrayList<>();
        List<Lane> lanes = new ArrayList<>();
        // TODO Map<GTUType, LongitudinalDirectionality> linkDirections = new HashMap<>();
        LongitudinalDirectionality linkDirection = LongitudinalDirectionality.DIR_NONE;

        // add information from the lanes
        // first the total width is computed
        // The offset of the lanes will be computed to match this Vissim layout
        Double roadWidth = 0.0;
        for (LaneTag laneTag : linkTag.laneTags.values()) {
            roadWidth += Double.parseDouble(laneTag.width);
        }

        Color color;
        if (linkTag.connector) {
            color = Color.LIGHT_GRAY;
        } else {
            color = Color.DARK_GRAY;

        }

        // The lanes are ordered from the outside to the inner side of the road
        Double totalLaneWidth = 0.0;

        if (!linkTag.connector) {
            for (LaneTag laneTag : linkTag.laneTags.values()) {
                String name = laneTag.laneNo;
                Double laneWidth = Double.parseDouble(laneTag.width);
                Length thisLaneWidth = new Length(laneWidth, LengthUnit.METER);

                // the road offset is negative if the lanes are at the right side of the median (for right hand rule)
                // Vissim puts the lanes around the centerline of the road (all lanes joined)
                // therefore we use a factor 0.5 * roadWidth....
                Double negativeOffset = -(0.5 * roadWidth - totalLaneWidth - laneWidth / 2);
                Length lateralOffset = new Length(negativeOffset, LengthUnit.METER);

                LaneType laneType = LaneType.ALL;
                linkDirection = LongitudinalDirectionality.DIR_PLUS;
                csl.addDirectionality(GTUType.ALL, linkDirection);
                Speed speedLimit = new Speed(Double.parseDouble(linkTag.legalSpeed), SpeedUnit.KM_PER_HOUR);
                // OvertakingConditions overtakingConditions; TODO (not clear yet)
                Lane lane = new Lane(csl, name, lateralOffset, thisLaneWidth, laneType, linkDirection, speedLimit, null);
                if (!linkTag.sensors.isEmpty()) {
                    for (SensorTag sensorTag : linkTag.sensors) {
                        if (sensorTag.laneName.equals(laneTag.laneNo)) {
                            Length pos = new Length(Double.parseDouble(sensorTag.positionStr), LengthUnit.METER);
                            if (pos.lt(lane.getLength())) {
                                SimpleReportingSensor sensor = new SimpleReportingSensor(sensorTag.name, lane, pos,
                                    RelativePosition.FRONT, simulator);
                                lane.getSensors().add(sensor);
                            }
                        }
                    }
                }
                if (!linkTag.signalHeads.isEmpty()) {
                    for (SignalHeadTag signalHeadTag : linkTag.signalHeads) {
                        if (signalHeadTag.laneName.equals(laneTag.laneNo)) {
                            Length pos = new Length(Double.parseDouble(signalHeadTag.positionStr), LengthUnit.METER);
                            if (pos.lt(lane.getLength())) {
                                SimpleTrafficLight simpleTrafficLight = new SimpleTrafficLight(signalHeadTag.no, lane, pos,
                                    simulator);
                                lane.getLaneBasedObjects().add(simpleTrafficLight);
                            }
                        }
                    }
                }

                cseList.add(lane);

                lanes.add(lane);
                linkTag.lanes.put(name, lane);
                // update totalLaneWidth
                totalLaneWidth += Double.parseDouble(laneTag.width);
                if (simulator != null && simulator instanceof AnimatorInterface) {
                    try {
                        new LaneAnimation(lane, simulator, color, true);
                    } catch (RemoteException exception) {
                        exception.printStackTrace();
                    }
                }

            }
        }

    }

    /**
     * @param linkTag the link to process
     * @param parser the parser with the lists of information
     * @param simulator to be able to make the animation
     * @throws NetworkException when the stripe cannot be instantiated
     * @throws NamingException when the /animation/2D tree cannot be found in the context
     * @throws SAXException when the stripe type cannot be parsed correctly
     * @throws GTUException when lane block cannot be created
     * @throws OTSGeometryException when construction of the offset-line or contour fails
     * @throws SimRuntimeException when construction of the generator fails
     */
    @SuppressWarnings({"checkstyle:needbraces", "checkstyle:methodlength"})
    static void applyRoadTypeToConnector(final LinkTag linkTag, final VissimNetworkLaneParser parser,
        final OTSDEVSSimulatorInterface simulator) throws NetworkException, NamingException, SAXException, GTUException,
            OTSGeometryException, SimRuntimeException {
        CrossSectionLink csl = linkTag.link;

        List<CrossSectionElement> cseList = new ArrayList<>();
        List<Lane> lanes = new ArrayList<>();
        // TODO Map<GTUType, LongitudinalDirectionality> linkDirections = new HashMap<>();
        LongitudinalDirectionality linkDirection = LongitudinalDirectionality.DIR_NONE;

        // add information from the lanes
        // first the total width is computed
        // The offset of the lanes will be computed to match this Vissim layout
        Double roadWidth = 0.0;
        for (LaneTag laneTag : linkTag.laneTags.values()) {
            roadWidth += Double.parseDouble(laneTag.width);
        }

        Color color;
        if (linkTag.connector) {
            color = Color.LIGHT_GRAY;
        } else {
            color = Color.DARK_GRAY;

        }

        // some generic definitions necessary to create a Lane object
        LaneType laneType = LaneType.ALL;
        linkDirection = LongitudinalDirectionality.DIR_PLUS;
        csl.addDirectionality(GTUType.ALL, linkDirection);
        Speed speedLimit = new Speed(Double.parseDouble(linkTag.legalSpeed), SpeedUnit.KM_PER_HOUR);

        // The lanes are ordered from the outside to the inner side of the road
        // only lanes from connectors are being set
        if (linkTag.connector) {
            Double totalFromLaneWidth = -1.75;
            Double totalToLaneWidth = -1.75;
            // find the link and lane downstream
            LinkTag linkToTag = parser.realLinkTags.get(linkTag.connectorTag.toLinkNo);
            Lane laneTo = linkToTag.lanes.get(linkTag.connectorTag.toLaneNo);

            // find the link and lane upstream
            LinkTag linkFromTag = parser.realLinkTags.get(linkTag.connectorTag.fromLinkNo);
            Lane laneFrom = linkFromTag.lanes.get(linkTag.connectorTag.fromLaneNo);

            // loop along all lanes (Tags)
            for (LaneTag connectLaneTag : linkTag.laneTags.values()) {
                // the name (number) of the downstream lane (the order is from outer to center)
                String name = connectLaneTag.laneNo;
                // the width of the current lane
                Length thisLaneWidth = new Length(Double.parseDouble(connectLaneTag.width), LengthUnit.METER);
                // the total lanewidth of all previous lanes that already have been looped through
                Length totfromLaneWidth = new Length(totalFromLaneWidth, LengthUnit.METER);
                Length totToLaneWidth = new Length(totalToLaneWidth, LengthUnit.METER);

                Length lateralOffsetStart;
                Length lateralOffsetEnd = laneTo.getLateralBoundaryPosition(LateralDirectionality.RIGHT, 0).plus(
                    totToLaneWidth);
                // the lateral offset
                if (laneFrom != null) {
                    lateralOffsetStart = laneFrom.getLateralBoundaryPosition(LateralDirectionality.RIGHT, 0).plus(
                        totfromLaneWidth);
                } else {
                    lateralOffsetStart = lateralOffsetEnd;
                }

                // the road offset is negative if the lanes are at the right side of the median (for right hand rule)
                // OvertakingConditions overtakingConditions; TODO (not clear yet)
                Lane lane = new Lane(csl, name, lateralOffsetStart, lateralOffsetEnd, thisLaneWidth, thisLaneWidth, laneType,
                    linkDirection, speedLimit, null);
                cseList.add(lane);
                lanes.add(lane);
                linkTag.lanes.put(name, lane);

                // update totalLaneWidth
                totalFromLaneWidth += Double.parseDouble(connectLaneTag.width);
                totalToLaneWidth += Double.parseDouble(connectLaneTag.width);
                if (simulator != null && simulator instanceof AnimatorInterface) {
                    try {
                        new LaneAnimation(lane, simulator, color, true);
                    } catch (RemoteException exception) {
                        exception.printStackTrace();
                    }
                }

            }
        }

    }

}
