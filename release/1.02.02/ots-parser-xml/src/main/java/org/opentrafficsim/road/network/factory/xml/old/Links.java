package org.opentrafficsim.road.network.factory.xml.old;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.djunits.unit.DirectionUnit;
import org.djunits.value.AngleUtil;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.reflection.ClassUtil;
import org.opentrafficsim.core.animation.DrawingInfoLine;
import org.opentrafficsim.core.animation.DrawingInfoStripe;
import org.opentrafficsim.core.animation.StripeType;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
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
import org.opentrafficsim.draw.road.LaneAnimation;
import org.opentrafficsim.draw.road.ShoulderAnimation;
import org.opentrafficsim.road.network.factory.xml.old.ArcTag.ArcDirection;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.NoTrafficLane;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.lane.Shoulder;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.Stripe.Permeable;
import org.opentrafficsim.road.network.lane.changing.OvertakingConditions;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.xml.sax.SAXException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.AnimatorInterface;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * Build connectors.
     * @param connectorTag ConnectorTag; the connector to process
     * @param parser XmlNetworkLaneParserOld; the parser with the lists of information
     * @param simulator OTSSimulatorInterface; to be able to make the animation
     * @throws OTSGeometryException when both nodes are null
     * @throws NamingException when node animation cannot link to the animation context
     * @throws NetworkException when tag type not filled
     */
    static void buildConnector(final ConnectorTag connectorTag, final XmlNetworkLaneParserOld parser,
            final OTSSimulatorInterface simulator) throws OTSGeometryException, NamingException, NetworkException
    {
        OTSLine3D designLine =
                new OTSLine3D(connectorTag.nodeStartTag.node.getPoint(), connectorTag.nodeEndTag.node.getPoint());
        CrossSectionLink connector = new CrossSectionLink(parser.network, connectorTag.name, 
                (OTSRoadNode) connectorTag.nodeStartTag.node, (OTSRoadNode) connectorTag.nodeEndTag.node, 
                parser.network.getLinkType(LinkType.DEFAULTS.CONNECTOR), designLine, simulator, null);
        if (connectorTag.demandWeight != null)
        {
            connector.setDemandWeight(connectorTag.demandWeight);
        }
        parser.networkAnimation.addDrawingInfoBase(connector, new DrawingInfoLine<CrossSectionLink>(Color.BLACK, 0.5f));
        connectorTag.connector = connector;
    }

    /**
     * calculate node angles based on the STRAIGHT links.
     * @param linkTag LinkTag; the link to process
     */
    static void calculateNodeAngles(final LinkTag linkTag)
    {
        if (linkTag.straightTag != null)
        {
            double direction = Math.atan2(linkTag.nodeEndTag.coordinate.y - linkTag.nodeStartTag.coordinate.y,
                    linkTag.nodeEndTag.coordinate.x - linkTag.nodeStartTag.coordinate.x);
            if (linkTag.nodeStartTag.direction == null)
            {
                linkTag.nodeStartTag.direction = new Direction(direction, DirectionUnit.EAST_RADIAN);
            }
            if (linkTag.nodeEndTag.direction == null)
            {
                linkTag.nodeEndTag.direction = new Direction(direction, DirectionUnit.EAST_RADIAN);
            }
        }
    }

    /**
     * Find the nodes one by one that have one coordinate defined, and one not defined, and try to build the network from there.
     * @param linkTag LinkTag; the link to process
     * @param parser XmlNetworkLaneParserOld; the parser with the lists of information
     * @param simulator OTSSimulatorInterface; to be able to make the animation
     * @throws OTSGeometryException when both nodes are null.
     * @throws NamingException when node animation cannot link to the animation context.
     * @throws NetworkException when tag type not filled
     */
    static void buildLink(final LinkTag linkTag, final XmlNetworkLaneParserOld parser, final OTSSimulatorInterface simulator)
            throws OTSGeometryException, NamingException, NetworkException
    {
        NodeTag from = linkTag.nodeStartTag;
        OTSPoint3D startPoint = new OTSPoint3D(from.coordinate);
        double startAngle = linkTag.nodeStartTag.direction == null ? 0.0 : linkTag.nodeStartTag.direction.getInUnit();
        if (linkTag.offsetStart != null && linkTag.offsetStart.si != 0.0)
        {
            // shift the start point perpendicular to the node direction or read from tag
            double offset = linkTag.offsetStart.getInUnit();
            startPoint = new OTSPoint3D(startPoint.x + offset * Math.cos(startAngle + Math.PI / 2.0),
                    startPoint.y + offset * Math.sin(startAngle + Math.PI / 2.0), startPoint.z);
            System.out
                    .println("fc = " + from.coordinate + ", sa = " + startAngle + ", so = " + offset + ", sp = " + startPoint);
        }

        NodeTag to = linkTag.nodeEndTag;
        OTSPoint3D endPoint = new OTSPoint3D(to.coordinate);
        double endAngle = linkTag.nodeEndTag.direction == null ? 0.0 : linkTag.nodeEndTag.direction.getInUnit();
        if (linkTag.offsetEnd != null && linkTag.offsetEnd.si != 0.0)
        {
            // shift the end point perpendicular to the node direction or read from tag
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
            // calculate the center position
            double radiusSI = linkTag.arcTag.radius.getSI();
            double offsetStart = 0.0;
            if (linkTag.offsetStart != null)
            {
                offsetStart = linkTag.offsetStart.si;
            }
            double offsetEnd = 0.0;
            if (linkTag.offsetEnd != null)
            {
                offsetEnd = linkTag.offsetEnd.si;
            }
            List<OTSPoint3D> center = OTSPoint3D.circleIntersections(from.coordinate, radiusSI + offsetStart, to.coordinate,
                    radiusSI + offsetEnd);
            OTSPoint3D c = linkTag.arcTag.center =
                    (linkTag.arcTag.direction.equals(ArcTag.ArcDirection.RIGHT)) ? center.get(0) : center.get(1);

            // calculate start angle and end angle
            double sa = linkTag.arcTag.startAngle = Math.atan2(from.coordinate.y - c.y, from.coordinate.x - c.x);
            double ea = Math.atan2(to.coordinate.y - c.y, to.coordinate.x - c.x);
            if (linkTag.arcTag.direction.equals(ArcDirection.RIGHT))
            {
                // right -> negative direction, ea should be less than sa
                ea = (sa < ea) ? ea + Math.PI * 2.0 : ea;
            }
            else
            {
                // left -> positive direction, sa should be less than ea
                ea = (ea < sa) ? ea + Math.PI * 2.0 : ea;
            }

            int points = (AngleUtil.normalize(ea - sa) <= Math.PI / 2.0) ? 64 : 128;
            coordinates = new OTSPoint3D[points];
            coordinates[0] = new OTSPoint3D(from.coordinate.x + Math.cos(sa) * offsetStart,
                    from.coordinate.y + Math.sin(sa) * offsetStart, from.coordinate.z);
            coordinates[coordinates.length - 1] = new OTSPoint3D(to.coordinate.x + Math.cos(ea) * offsetEnd,
                    to.coordinate.y + Math.sin(ea) * offsetEnd, to.coordinate.z);
            double angleStep = linkTag.arcTag.angle.getInUnit() / points;
            double slopeStep = (to.coordinate.z - from.coordinate.z) / points;

            if (linkTag.arcTag.direction.equals(ArcDirection.RIGHT))
            {
                for (int p = 1; p < points - 1; p++)
                {
                    double dRad = offsetStart + (offsetEnd - offsetStart) * p / points;
                    coordinates[p] = new OTSPoint3D(
                            linkTag.arcTag.center.x + (radiusSI + dRad) * Math.cos(linkTag.arcTag.startAngle - angleStep * p),
                            linkTag.arcTag.center.y + (radiusSI + dRad) * Math.sin(linkTag.arcTag.startAngle - angleStep * p),
                            from.coordinate.z + slopeStep * p);
                }
            }
            else
            {
                for (int p = 1; p < points - 1; p++)
                {
                    double dRad = offsetStart + (offsetEnd - offsetStart) * p / points;
                    coordinates[p] = new OTSPoint3D(
                            linkTag.arcTag.center.x + (radiusSI + dRad) * Math.cos(linkTag.arcTag.startAngle + angleStep * p),
                            linkTag.arcTag.center.y + (radiusSI + dRad) * Math.sin(linkTag.arcTag.startAngle + angleStep * p),
                            from.coordinate.z + slopeStep * p);
                }
            }
        }

        else if (linkTag.bezierTag != null)
        {
            coordinates = Bezier.cubic(128, new DirectedPoint(startPoint.x, startPoint.y, startPoint.z, 0, 0, startAngle),
                    new DirectedPoint(endPoint.x, endPoint.y, endPoint.z, 0, 0, endAngle), linkTag.bezierTag.shape,
                    linkTag.bezierTag.weighted).getPoints();
        }

        else
        {
            throw new NetworkException(
                    "Making link, but link " + linkTag.name + " has no filled straight, arc, or bezier curve");
        }

        OTSLine3D designLine = OTSLine3D.createAndCleanOTSLine3D(coordinates);

        // TODO: Directionality has to be added later when the lanes and their direction are known.
        CrossSectionLink link = new CrossSectionLink(parser.network, linkTag.name, (OTSRoadNode) linkTag.nodeStartTag.node,
                (OTSRoadNode) linkTag.nodeEndTag.node, parser.network.getLinkType(LinkType.DEFAULTS.FREEWAY), designLine, 
                simulator, linkTag.laneKeepingPolicy);

        if (linkTag.priority != null)
        {
            link.setPriority(linkTag.priority);
        }

        parser.networkAnimation.addDrawingInfoBase(link, new DrawingInfoLine<CrossSectionLink>(Color.BLACK, 0.5f));

        linkTag.link = link;
    }

    /**
     * @param linkTag LinkTag; the link to process
     * @param parser XmlNetworkLaneParserOld; the parser with the lists of information
     * @param simulator OTSSimulatorInterface; to be able to make the animation
     * @throws NetworkException when the stripe cannot be instantiated
     * @throws NamingException when the /animation/2D tree cannot be found in the context
     * @throws SAXException when the stripe type cannot be parsed correctly
     * @throws GTUException when lane block cannot be created
     * @throws OTSGeometryException when construction of the offset-line or contour fails
     * @throws SimRuntimeException when construction of the generator fails
     */
    @SuppressWarnings({"checkstyle:needbraces", "checkstyle:methodlength"})
    static void applyRoadTypeToLink(final LinkTag linkTag, final XmlNetworkLaneParserOld parser,
            final OTSSimulatorInterface simulator)
            throws NetworkException, NamingException, SAXException, GTUException, OTSGeometryException, SimRuntimeException
    {
        CrossSectionLink csl = linkTag.link;
        List<CrossSectionElement> cseList = new ArrayList<>();
        List<Lane> lanes = new ArrayList<>();
        // TODO Map<GTUType, LongitudinalDirectionality> linkDirections = new LinkedHashMap<>();
        LongitudinalDirectionality linkDirection = LongitudinalDirectionality.DIR_NONE;
        for (CrossSectionElementTag cseTag : linkTag.roadLayoutTag.cseTags.values())
        {
            LaneOverrideTag laneOverrideTag = null;
            if (linkTag.laneOverrideTags.containsKey(cseTag.name))
                laneOverrideTag = linkTag.laneOverrideTags.get(cseTag.name);

            Length startOffset = cseTag.offset != null ? cseTag.offset : cseTag.offSetStart;
            Length endOffset = cseTag.offset != null ? cseTag.offset : cseTag.offSetEnd;
            switch (cseTag.elementType)
            {
                case STRIPE:
                    switch (cseTag.stripeType)
                    {
                        case BLOCKED:
                        case DASHED:
                            Stripe dashedLine = new Stripe(csl, startOffset, endOffset, cseTag.width);
                            dashedLine.addPermeability(csl.getNetwork().getGtuType(GTUType.DEFAULTS.VEHICLE), Permeable.BOTH);
                            parser.networkAnimation.addDrawingInfoBase(dashedLine,
                                    new DrawingInfoStripe<Stripe>(Color.BLACK, 0.5f, StripeType.DASHED));
                            cseList.add(dashedLine);
                            break;

                        case DOUBLE:
                            Stripe doubleLine = new Stripe(csl, startOffset, endOffset, cseTag.width);
                            parser.networkAnimation.addDrawingInfoBase(doubleLine,
                                    new DrawingInfoStripe<Stripe>(Color.BLACK, 0.5f, StripeType.DOUBLE));
                            cseList.add(doubleLine);
                            break;

                        case LEFTONLY:
                            Stripe leftOnlyLine = new Stripe(csl, startOffset, endOffset, cseTag.width);
                            leftOnlyLine.addPermeability(csl.getNetwork().getGtuType(GTUType.DEFAULTS.VEHICLE), Permeable.LEFT);
                            // TODO correct?
                            parser.networkAnimation.addDrawingInfoBase(leftOnlyLine,
                                    new DrawingInfoStripe<Stripe>(Color.BLACK, 0.5f, StripeType.LEFTONLY));
                            cseList.add(leftOnlyLine);
                            break;

                        case RIGHTONLY:
                            Stripe rightOnlyLine = new Stripe(csl, startOffset, endOffset, cseTag.width);
                            rightOnlyLine.addPermeability(csl.getNetwork().getGtuType(GTUType.DEFAULTS.VEHICLE),
                                    Permeable.RIGHT);
                            // TODO correct?
                            parser.networkAnimation.addDrawingInfoBase(rightOnlyLine,
                                    new DrawingInfoStripe<Stripe>(Color.BLACK, 0.5f, StripeType.RIGHTONLY));
                            cseList.add(rightOnlyLine);
                            break;

                        case SOLID:
                            Stripe solidLine = new Stripe(csl, startOffset, endOffset, cseTag.width);
                            parser.networkAnimation.addDrawingInfoBase(solidLine,
                                    new DrawingInfoStripe<Stripe>(Color.BLACK, 0.5f, StripeType.SOLID));
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
                        if (laneOverrideTag.color != null)
                            color = laneOverrideTag.color;
                        if (laneOverrideTag.direction != null)
                            direction = laneOverrideTag.direction;
                    }
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

                    // XXX: LaneTypes with compatibilities might have to be defined in a new way -- LaneType.FREEWAY for now...
                    Lane lane = new Lane(csl, cseTag.name, startOffset, endOffset, cseTag.width, cseTag.width,
                            csl.getNetwork().getLaneType(LaneType.DEFAULTS.FREEWAY), cseTag.legalSpeedLimits);
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
                            new LaneAnimation(lane, simulator, color);
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
                        new SinkSensor(lane, position, Compatible.EVERYTHING, simulator);
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
                                        String.class, Lane.class, Length.class, DEVSSimulatorInterface.TimeDoubleUnit.class});
                                Length position = LinkTag.parseBeginEndPosition(trafficLightTag.positionStr, lane);
                                trafficLightConstructor
                                        .newInstance(new Object[] {trafficLightTag.name, lane, position, simulator});
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
                                Constructor<?> sensorConstructor = ClassUtil.resolveConstructor(clazz,
                                        new Class[] {String.class, Lane.class, Length.class, RelativePosition.TYPE.class,
                                                DEVSSimulatorInterface.TimeDoubleUnit.class, Compatible.class});
                                Length position = LinkTag.parseBeginEndPosition(sensorTag.positionStr, lane);
                                // { String.class, Lane.class, Length.class, RelativePosition.TYPE.class,
                                // DEVSSimulatorInterface.TimeDoubleUnit.class }
                                sensorConstructor.newInstance(new Object[] {sensorTag.name, lane, position,
                                        sensorTag.triggerPosition, simulator, Compatible.EVERYTHING});
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
                    Lane lane = new NoTrafficLane(csl, cseTag.name, startOffset, endOffset, cseTag.width, cseTag.width);
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
                            new LaneAnimation(lane, simulator, color);
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
                    Shoulder shoulder = new Shoulder(csl, cseTag.name, startOffset, endOffset, cseTag.width, cseTag.width);
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
    }

}
