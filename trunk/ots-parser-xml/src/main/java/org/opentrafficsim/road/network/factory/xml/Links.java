package org.opentrafficsim.road.network.factory.xml;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.djunits.unit.AngleUnit;
import org.djunits.value.AngleUtil;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.Bezier;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.animation.LaneAnimation;
import org.opentrafficsim.road.network.animation.ShoulderAnimation;
import org.opentrafficsim.road.network.animation.StripeAnimation;
import org.opentrafficsim.road.network.factory.xml.ArcTag.ArcDirection;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.NoTrafficLane;
import org.opentrafficsim.road.network.lane.Shoulder;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.Stripe.Permeable;
import org.opentrafficsim.road.network.lane.changing.OvertakingConditions;
import org.opentrafficsim.road.network.lane.object.sensor.AbstractSensor;
import org.opentrafficsim.road.network.lane.object.sensor.Sensor;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight;
import org.xml.sax.SAXException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.AnimatorInterface;
import nl.tudelft.simulation.language.d3.CartesianPoint;
import nl.tudelft.simulation.language.d3.DirectedPoint;
import nl.tudelft.simulation.language.reflection.ClassUtil;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
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
     * @param parser the parser with the lists of information
     * @throws NetworkException when both nodes are null.
     * @throws NamingException when node animation cannot link to the animation context.
     */
    @SuppressWarnings("methodlength")
    static void calculateNodeCoordinates(final XmlNetworkLaneParser parser) throws NetworkException, NamingException
    {
        // are there straight tags with nodes without an angle?
        for (LinkTag linkTag : parser.linkTags.values())
        {
            if (linkTag.straightTag != null && linkTag.nodeStartTag.coordinate != null && linkTag.nodeEndTag.coordinate != null)
            {
                if (linkTag.nodeStartTag.angle == null)
                {
                    double dx = linkTag.nodeEndTag.coordinate.x - linkTag.nodeStartTag.coordinate.x;
                    double dy = linkTag.nodeEndTag.coordinate.y - linkTag.nodeStartTag.coordinate.y;
                    linkTag.nodeStartTag.angle = new Direction(Math.atan2(dy, dx), AngleUnit.RADIAN);
                }
                if (linkTag.nodeEndTag.angle == null)
                {
                    double dx = linkTag.nodeEndTag.coordinate.x - linkTag.nodeStartTag.coordinate.x;
                    double dy = linkTag.nodeEndTag.coordinate.y - linkTag.nodeStartTag.coordinate.y;
                    linkTag.nodeEndTag.angle = new Direction(Math.atan2(dy, dx), AngleUnit.RADIAN);
                }
            }
        }

        // see if we can find the coordinates of the nodes that have not yet been fixed.
        Set<NodeTag> nodeTags = new HashSet<>();
        for (LinkTag linkTag : parser.linkTags.values())
        {
            if (linkTag.nodeStartTag.coordinate == null)
            {
                nodeTags.add(linkTag.nodeStartTag);
            }
            if (linkTag.nodeEndTag.coordinate == null)
            {
                nodeTags.add(linkTag.nodeEndTag);
            }
        }

        while (nodeTags.size() > 0)
        {
            boolean found = false;
            for (LinkTag linkTag : parser.linkTags.values())
            {
                if (linkTag.straightTag != null || linkTag.arcTag != null)
                {
                    if (nodeTags.contains(linkTag.nodeStartTag) == nodeTags.contains(linkTag.nodeEndTag))
                    {
                        continue;
                    }

                    if (linkTag.straightTag != null)
                    {
                        double lengthSI = linkTag.straightTag.length.getSI();
                        if (linkTag.nodeEndTag.node == null)
                        {
                            CartesianPoint coordinate = new CartesianPoint(linkTag.nodeStartTag.node.getLocation().getX(),
                                    linkTag.nodeStartTag.node.getLocation().getY(),
                                    linkTag.nodeStartTag.node.getLocation().getZ());
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
                        }
                        else if (linkTag.nodeStartTag.node == null)
                        {
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
                    }
                    else if (linkTag.arcTag != null)
                    {
                        double radiusSI = linkTag.arcTag.radius.getSI();
                        double angle = linkTag.arcTag.angle.getSI();
                        ArcDirection direction = linkTag.arcTag.direction;

                        if (linkTag.nodeEndTag.node == null)
                        {
                            CartesianPoint coordinate = new CartesianPoint(0.0, 0.0, 0.0);
                            double startAngle = linkTag.nodeStartTag.node.getDirection().getSI();
                            double slope = linkTag.nodeStartTag.node.getSlope().getSI();
                            double lengthSI = radiusSI * angle;
                            NodeTag nodeTag = linkTag.nodeEndTag;
                            if (direction.equals(ArcDirection.LEFT))
                            {
                                linkTag.arcTag.center = new OTSPoint3D(
                                        linkTag.nodeStartTag.node.getLocation().getX()
                                                + radiusSI * Math.cos(startAngle + Math.PI / 2.0),
                                        linkTag.nodeStartTag.node.getLocation().getY()
                                                + radiusSI * Math.sin(startAngle + Math.PI / 2.0),
                                        0.0);
                                linkTag.arcTag.startAngle = startAngle - Math.PI / 2.0;
                                coordinate.x = linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle + angle);
                                coordinate.y = linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle + angle);
                                nodeTag.angle = new Direction(AngleUtil.normalize(startAngle + angle), AngleUnit.SI);
                            }
                            else
                            {
                                linkTag.arcTag.center = new OTSPoint3D(
                                        linkTag.nodeStartTag.node.getLocation().getX()
                                                - radiusSI * Math.cos(startAngle + Math.PI / 2.0),
                                        linkTag.nodeStartTag.node.getLocation().getY()
                                                - radiusSI * Math.sin(startAngle + Math.PI / 2.0),
                                        0.0);
                                linkTag.arcTag.startAngle = startAngle + Math.PI / 2.0;
                                coordinate.x = linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle - angle);
                                coordinate.y = linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle - angle);
                                nodeTag.angle = new Direction(AngleUtil.normalize(startAngle - angle), AngleUnit.SI);
                            }
                            coordinate.z = linkTag.nodeStartTag.node.getLocation().getZ() + lengthSI * Math.sin(slope);
                            nodeTag.slope = new Direction(slope, AngleUnit.SI);
                            nodeTag.coordinate = new OTSPoint3D(coordinate.x, coordinate.y, coordinate.z);
                            linkTag.nodeEndTag.node = NodeTag.makeOTSNode(nodeTag, parser);
                            nodeTags.remove(linkTag.nodeEndTag);
                        }

                        else if (linkTag.nodeStartTag.node == null)
                        {
                            CartesianPoint coordinate = new CartesianPoint(linkTag.nodeEndTag.node.getLocation().getX(),
                                    linkTag.nodeEndTag.node.getLocation().getY(), linkTag.nodeEndTag.node.getLocation().getZ());
                            double endAngle = linkTag.nodeEndTag.node.getDirection().getSI();
                            double slope = linkTag.nodeEndTag.node.getSlope().getSI();
                            double lengthSI = radiusSI * angle;
                            NodeTag nodeTag = linkTag.nodeStartTag;
                            if (direction.equals(ArcDirection.LEFT))
                            {
                                linkTag.arcTag.center =
                                        new OTSPoint3D(coordinate.x + radiusSI * Math.cos(endAngle + Math.PI / 2.0),
                                                coordinate.y + radiusSI * Math.sin(endAngle + Math.PI / 2.0), 0.0);
                                linkTag.arcTag.startAngle = endAngle - Math.PI / 2.0 - angle;
                                coordinate.x = linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle);
                                coordinate.y = linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle);
                                nodeTag.angle = new Direction(AngleUtil.normalize(linkTag.arcTag.startAngle + Math.PI / 2.0),
                                        AngleUnit.SI);
                            }
                            else
                            {
                                linkTag.arcTag.center =
                                        new OTSPoint3D(coordinate.x + radiusSI * Math.cos(endAngle - Math.PI / 2.0),
                                                coordinate.y + radiusSI * Math.sin(endAngle - Math.PI / 2.0), 0.0);
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
            if (!found)
            {
                throw new NetworkException("Cannot find coordinates of one or more nodes");
            }
        }

        // are there straight tags with nodes without an angle?
        for (LinkTag linkTag : parser.linkTags.values())
        {
            if (linkTag.straightTag != null && linkTag.nodeStartTag.coordinate != null && linkTag.nodeEndTag.coordinate != null)
            {
                if (linkTag.nodeStartTag.angle == null)
                {
                    double dx = linkTag.nodeEndTag.coordinate.x - linkTag.nodeStartTag.coordinate.x;
                    double dy = linkTag.nodeEndTag.coordinate.y - linkTag.nodeStartTag.coordinate.y;
                    linkTag.nodeStartTag.angle = new Direction(Math.atan2(dy, dx), AngleUnit.RADIAN);
                }
                if (linkTag.nodeEndTag.angle == null)
                {
                    double dx = linkTag.nodeEndTag.coordinate.x - linkTag.nodeStartTag.coordinate.x;
                    double dy = linkTag.nodeEndTag.coordinate.y - linkTag.nodeStartTag.coordinate.y;
                    linkTag.nodeEndTag.angle = new Direction(Math.atan2(dy, dx), AngleUnit.RADIAN);
                }
            }
        }

        // which nodes have not yet been created?
        for (NodeTag nodeTag : parser.nodeTags.values())
        {
            if (nodeTag.coordinate != null && nodeTag.node == null)
            {
                if (nodeTag.angle == null)
                {
                    nodeTag.angle = Direction.ZERO;
                }
                if (nodeTag.slope == null)
                {
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
    static void buildLink(final LinkTag linkTag, final XmlNetworkLaneParser parser, final OTSDEVSSimulatorInterface simulator)
            throws OTSGeometryException, NamingException, NetworkException
    {
        NodeTag from = linkTag.nodeStartTag;
        OTSPoint3D startPoint = new OTSPoint3D(from.coordinate);
        double startAngle = from.angle.si;
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
        double endAngle = to.angle.si;
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
            int intermediatePoints = linkTag.polyLineTag.coordinates.length;
            coordinates = new OTSPoint3D[intermediatePoints + 2];
            coordinates[0] = startPoint;
            coordinates[intermediatePoints + 1] = endPoint;
            for (int p = 0; p < intermediatePoints; p++)
            {
                coordinates[p + 1] = linkTag.polyLineTag.coordinates[p];
            }

        }
        else if (linkTag.arcTag != null)
        {
            // TODO move the radius if there is an start and end offset? How?
            int points = (Math.abs(linkTag.arcTag.angle.getSI()) <= Math.PI / 2.0) ? 64 : 128;
            coordinates = new OTSPoint3D[points];
            coordinates[0] = new OTSPoint3D(from.coordinate.x, from.coordinate.y, from.coordinate.z);
            coordinates[coordinates.length - 1] = new OTSPoint3D(to.coordinate.x, to.coordinate.y, to.coordinate.z);
            double angleStep = linkTag.arcTag.angle.getSI() / points;
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
            coordinates = Bezier.cubic(128, new DirectedPoint(startPoint.x, startPoint.y, startPoint.z, 0, 0, startAngle),
                    new DirectedPoint(endPoint.x, endPoint.y, endPoint.z, 0, 0, endAngle)).getPoints();
        }

        else
        {
            throw new NetworkException(
                    "Making link, but link " + linkTag.name + " has no filled straight, arc, or bezier curve");
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
    @SuppressWarnings({ "checkstyle:needbraces", "checkstyle:methodlength" })
    static void applyRoadTypeToLink(final LinkTag linkTag, final XmlNetworkLaneParser parser,
            final OTSDEVSSimulatorInterface simulator)
            throws NetworkException, NamingException, SAXException, GTUException, OTSGeometryException, SimRuntimeException
    {
        CrossSectionLink csl = linkTag.link;
        List<CrossSectionElement> cseList = new ArrayList<>();
        List<Lane> lanes = new ArrayList<>();
        // TODO Map<GTUType, LongitudinalDirectionality> linkDirections = new HashMap<>();
        LongitudinalDirectionality linkDirection = LongitudinalDirectionality.DIR_NONE;
        for (CrossSectionElementTag cseTag : linkTag.roadLayoutTag.cseTags.values())
        {
            LaneOverrideTag laneOverrideTag = null;
            if (linkTag.laneOverrideTags.containsKey(cseTag.name))
                laneOverrideTag = linkTag.laneOverrideTags.get(cseTag.name);

            switch (cseTag.elementType)
            {
                case STRIPE:
                    switch (cseTag.stripeType)
                    {
                        case BLOCKED:
                        case DASHED:
                            Stripe dashedLine = new Stripe(csl, cseTag.offset, cseTag.width);
                            dashedLine.addPermeability(GTUType.ALL, Permeable.BOTH);
                            if (simulator != null && simulator instanceof AnimatorInterface)
                            {
                                try
                                {
                                    new StripeAnimation(dashedLine, simulator, StripeAnimation.TYPE.DASHED);
                                }
                                catch (RemoteException exception)
                                {
                                    exception.printStackTrace();
                                }
                            }
                            cseList.add(dashedLine);
                            break;

                        case DOUBLE:
                            Stripe doubleLine = new Stripe(csl, cseTag.offset, cseTag.width);
                            if (simulator != null && simulator instanceof AnimatorInterface)
                            {
                                try
                                {
                                    new StripeAnimation(doubleLine, simulator, StripeAnimation.TYPE.DOUBLE);
                                }
                                catch (RemoteException exception)
                                {
                                    exception.printStackTrace();
                                }
                            }
                            cseList.add(doubleLine);
                            break;

                        case LEFTONLY:
                            Stripe leftOnlyLine = new Stripe(csl, cseTag.offset, cseTag.width);
                            leftOnlyLine.addPermeability(GTUType.ALL, Permeable.LEFT); // TODO correct?
                            if (simulator != null && simulator instanceof AnimatorInterface)
                            {
                                try
                                {
                                    new StripeAnimation(leftOnlyLine, simulator, StripeAnimation.TYPE.LEFTONLY);
                                }
                                catch (RemoteException exception)
                                {
                                    exception.printStackTrace();
                                }
                            }
                            cseList.add(leftOnlyLine);
                            break;

                        case RIGHTONLY:
                            Stripe rightOnlyLine = new Stripe(csl, cseTag.offset, cseTag.width);
                            rightOnlyLine.addPermeability(GTUType.ALL, Permeable.RIGHT); // TODO correct?
                            if (simulator != null && simulator instanceof AnimatorInterface)
                            {
                                try
                                {
                                    new StripeAnimation(rightOnlyLine, simulator, StripeAnimation.TYPE.RIGHTONLY);
                                }
                                catch (RemoteException exception)
                                {
                                    exception.printStackTrace();
                                }
                            }
                            cseList.add(rightOnlyLine);
                            break;

                        case SOLID:
                            Stripe solidLine = new Stripe(csl, cseTag.offset, cseTag.width);
                            if (simulator != null && simulator instanceof AnimatorInterface)
                            {
                                try
                                {
                                    new StripeAnimation(solidLine, simulator, StripeAnimation.TYPE.SOLID);
                                }
                                catch (RemoteException exception)
                                {
                                    exception.printStackTrace();
                                }
                            }
                            cseList.add(solidLine);
                            break;

                        default:
                            throw new SAXException("Unknown Stripe type: " + cseTag.stripeType.toString());
                    }
                    break;

                case LANE:
                {
                    LongitudinalDirectionality direction = cseTag.direction;
                    Color color = cseTag.color;
                    OvertakingConditions overtakingConditions = cseTag.overtakingConditions;
                    if (laneOverrideTag != null)
                    {
                        if (laneOverrideTag.overtakingConditions != null)
                            overtakingConditions = laneOverrideTag.overtakingConditions;
                        if (laneOverrideTag.color != null)
                            color = laneOverrideTag.color;
                        if (laneOverrideTag.direction != null)
                            direction = laneOverrideTag.direction;
                    }
                    Map<GTUType, LongitudinalDirectionality> directionality = new LinkedHashMap<>();
                    directionality.put(GTUType.ALL, direction);
                    if (linkDirection.equals(LongitudinalDirectionality.DIR_NONE))
                    {
                        linkDirection = direction;
                    }
                    else if (linkDirection.isForward())
                    {
                        if (direction.isBackwardOrBoth())
                        {
                            linkDirection = LongitudinalDirectionality.DIR_BOTH;
                        }
                    }
                    else if (linkDirection.isBackward())
                    {
                        if (direction.isForwardOrBoth())
                        {
                            linkDirection = LongitudinalDirectionality.DIR_BOTH;
                        }
                    }

                    // XXX: Quick hack to solve the error that the lane directionality has not (yet) been registered at the link
                    csl.addDirectionality(GTUType.ALL, linkDirection);

                    // XXX: LaneTypes with compatibilities might have to be defined in a new way -- LaneType.ALL for now...
                    Lane lane = new Lane(csl, cseTag.name, cseTag.offset, cseTag.offset, cseTag.width, cseTag.width,
                            LaneType.ALL, directionality, cseTag.legalSpeedLimits, overtakingConditions);
                    // System.out.println(OTSGeometry.printCoordinates("#link design line: \nc1,0,0\n#",
                    // lane.getParentLink().getDesignLine(), "\n "));
                    // System.out.println(OTSGeometry.printCoordinates("#lane center line: \nc0,1,0\n#", lane.getCenterLine(),
                    // "\n "));
                    // System.out.println(OTSGeometry.printCoordinates("#lane contour: \nc0,0,1\n#", lane.getContour(),
                    // "\n "));
                    cseList.add(lane);
                    lanes.add(lane);
                    linkTag.lanes.put(cseTag.name, lane);
                    if (simulator != null && simulator instanceof AnimatorInterface)
                    {
                        try
                        {
                            new LaneAnimation(lane, simulator, color, false);
                        }
                        catch (RemoteException exception)
                        {
                            exception.printStackTrace();
                        }
                    }

                    // SINK
                    if (linkTag.sinkTags.keySet().contains(cseTag.name))
                    {
                        SinkTag sinkTag = linkTag.sinkTags.get(cseTag.name);
                        Length position = LinkTag.parseBeginEndPosition(sinkTag.positionStr, lane);
                        Sensor sensor = new SinkSensor(lane, position, simulator);
                        lane.addSensor(sensor, GTUType.ALL);
                    }

                    // BLOCK
                    if (linkTag.blockTags.containsKey(cseTag.name))
                    {
                        BlockTag blockTag = linkTag.blockTags.get(cseTag.name);
                        Length position = LinkTag.parseBeginEndPosition(blockTag.positionStr, lane);
                        // TODO new CSEBlock(lane, position, simulator, null, parser.network);
                    }

                    // TRAFFICLIGHT
                    if (linkTag.trafficLightTags.containsKey(cseTag.name))
                    {
                        for (TrafficLightTag trafficLightTag : linkTag.trafficLightTags.get(cseTag.name))
                        {
                            try
                            {
                                Class<?> clazz = Class.forName(trafficLightTag.className);
                                Constructor<?> trafficLightConstructor = ClassUtil.resolveConstructor(clazz, new Class[] {
                                        String.class, Lane.class, Length.class, OTSDEVSSimulatorInterface.class });
                                Length position = LinkTag.parseBeginEndPosition(trafficLightTag.positionStr, lane);
                                SimpleTrafficLight trafficLight = (SimpleTrafficLight) trafficLightConstructor
                                        .newInstance(new Object[] { trafficLightTag.name, lane, position, simulator });
                            }
                            catch (ClassNotFoundException | NoSuchMethodException | InstantiationException
                                    | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                                    | NetworkException exception)
                            {
                                throw new NetworkException("TRAFFICLIGHT: CLASS NAME " + trafficLightTag.className
                                        + " for traffic light " + trafficLightTag.name + " on lane " + lane.toString()
                                        + " -- class not found or constructor not right", exception);
                            }
                        }
                    }

                    // GENERATOR
                    if (linkTag.generatorTags.containsKey(cseTag.name))
                    {
                        GeneratorTag generatorTag = linkTag.generatorTags.get(cseTag.name);
                        GeneratorTag.makeGenerator(generatorTag, parser, linkTag, simulator);
                    }

                    // TODO LISTGENERATOR

                    // SENSOR
                    if (linkTag.sensorTags.containsKey(cseTag.name))
                    {
                        for (SensorTag sensorTag : linkTag.sensorTags.get(cseTag.name))
                        {
                            try
                            {
                                Class<?> clazz = Class.forName(sensorTag.className);
                                Constructor<?> sensorConstructor =
                                        ClassUtil.resolveConstructor(clazz, new Class[] { String.class, Lane.class,
                                                Length.class, RelativePosition.TYPE.class, OTSDEVSSimulatorInterface.class });
                                Length position = LinkTag.parseBeginEndPosition(sensorTag.positionStr, lane);
                                // { String.class, Lane.class, Length.class, RelativePosition.TYPE.class,
                                // OTSDEVSSimulatorInterface.class }
                                AbstractSensor sensor = (AbstractSensor) sensorConstructor.newInstance(
                                        new Object[] { sensorTag.name, lane, position, sensorTag.triggerPosition, simulator });
                                lane.addSensor(sensor, GTUType.ALL);
                            }
                            catch (ClassNotFoundException | NoSuchMethodException | InstantiationException
                                    | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                                    | NetworkException exception)
                            {
                                throw new NetworkException("SENSOR: CLASS NAME " + sensorTag.className + " for sensor "
                                        + sensorTag.name + " on lane " + lane.toString()
                                        + " -- class not found or constructor not right", exception);
                            }
                        }
                    }

                    // FILL
                    if (linkTag.fillTags.containsKey(cseTag.name))
                    {
                        FillTag fillTag = linkTag.fillTags.get(cseTag.name);
                        FillTag.makeFill(fillTag, parser, linkTag, simulator);
                    }
                    break;
                }

                case NOTRAFFICLANE:
                {
                    Lane lane = new NoTrafficLane(csl, cseTag.name, cseTag.offset, cseTag.offset, cseTag.width, cseTag.width);
                    cseList.add(lane);
                    if (simulator != null && simulator instanceof AnimatorInterface)
                    {
                        try
                        {
                            Color color = cseTag.color;
                            if (laneOverrideTag != null)
                            {
                                if (laneOverrideTag.color != null)
                                    color = laneOverrideTag.color;
                            }
                            new LaneAnimation(lane, simulator, color, false);
                        }
                        catch (RemoteException exception)
                        {
                            exception.printStackTrace();
                        }
                    }
                    break;
                }

                case SHOULDER:
                {
                    Shoulder shoulder = new Shoulder(csl, cseTag.name, cseTag.offset, cseTag.width);
                    cseList.add(shoulder);
                    if (simulator != null && simulator instanceof AnimatorInterface)
                    {
                        try
                        {
                            Color color = cseTag.color;
                            if (laneOverrideTag != null)
                            {
                                if (laneOverrideTag.color != null)
                                    color = laneOverrideTag.color;
                            }
                            new ShoulderAnimation(shoulder, simulator, color);
                        }
                        catch (RemoteException exception)
                        {
                            exception.printStackTrace();
                        }
                    }
                    break;
                }

                default:
                    throw new SAXException("Unknown Element type: " + cseTag.elementType.toString());
            }

        } // for (CrossSectionElementTag cseTag : roadTypeTag.cseTags.values())

        // add the calculated direction to the link
        csl.addDirectionality(GTUType.ALL, linkDirection);
    }
}
