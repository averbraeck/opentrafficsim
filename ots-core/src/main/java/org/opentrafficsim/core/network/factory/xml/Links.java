package org.opentrafficsim.core.network.factory.xml;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.reflection.ClassUtil;

import org.djunits.unit.AnglePlaneUnit;
import org.djunits.unit.AngleSlopeUnit;
import org.opentrafficsim.core.OTS_SCALAR;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.lane.AbstractTrafficLight;
import org.opentrafficsim.core.gtu.lane.LaneBlock;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.animation.LaneAnimation;
import org.opentrafficsim.core.network.animation.ShoulderAnimation;
import org.opentrafficsim.core.network.animation.StripeAnimation;
import org.opentrafficsim.core.network.factory.xml.ArcTag.ArcDirection;
import org.opentrafficsim.core.network.lane.AbstractSensor;
import org.opentrafficsim.core.network.lane.CrossSectionElement;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.NoTrafficLane;
import org.opentrafficsim.core.network.lane.Sensor;
import org.opentrafficsim.core.network.lane.Shoulder;
import org.opentrafficsim.core.network.lane.SinkSensor;
import org.opentrafficsim.core.network.lane.Stripe;
import org.opentrafficsim.core.network.lane.Stripe.Permeable;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 25, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
final class Links implements OTS_SCALAR
{
    /** Utility class. */
    private Links()
    {
        // do not instantiate
    }

    /** helper class to temporarily store coordinate. */
    private static class XYZ
    {
        /** x coordinate. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        double x;

        /** y coordinate. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        double y;

        /** z coordinate. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        double z;

        /**
         * @param x the x coordinate
         * @param y the y coordinate
         * @param z the z coordinate
         */
        public XYZ(final double x, final double y, final double z)
        {
            super();
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    /**
     * Find the nodes one by one that have one coordinate defined, and one not defined, and try to build the network from there.
     * @param parser the parser with the lists of information
     * @throws NetworkException when both nodes are null.
     * @throws RemoteException when coordinate cannot be reached.
     * @throws NamingException when node animation cannot link to the animation context.
     */
    @SuppressWarnings("methodlength")
    static void calculateNodeCoordinates(final XmlNetworkLaneParser parser) throws RemoteException, NetworkException,
        NamingException
    {
        Set<LinkTag> links = new HashSet<>(parser.linkTags.values());
        while (!links.isEmpty())
        {
            boolean found = false;
            for (LinkTag linkTag : links)
            {
                if (linkTag.nodeStartTag.node != null && linkTag.nodeEndTag.node != null)
                {
                    calculateNodeCoordinates(linkTag, parser);
                    links.remove(linkTag);
                    found = true;
                    break;
                }
                if (linkTag.nodeStartTag.node != null || linkTag.nodeEndTag.node != null)
                {
                    calculateNodeCoordinates(linkTag, parser);
                    links.remove(linkTag);
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                String linkStr = "";
                boolean first = true;
                for (LinkTag linkTag : links)
                {
                    linkStr += first ? "[" : ", ";
                    linkStr += linkTag.name;
                    first = false;
                }
                linkStr += "]";
                throw new NetworkException("Links parser found unconnected links in network: " + linkStr);
            }
        }
    }

    /**
     * One of the nodes probably has a coordinate and the other not. Calculate the other coordinate and save the Node.
     * @param linkTag the parsed information from the XML file.
     * @param parser the parser with the lists of information
     * @throws NetworkException when both nodes are null.
     * @throws RemoteException when coordinate cannot be reached.
     * @throws NamingException when node animation cannot link to the animation context.
     */
    @SuppressWarnings("checkstyle:methodlength")
    static void calculateNodeCoordinates(final LinkTag linkTag, final XmlNetworkLaneParser parser) throws RemoteException,
        NetworkException, NamingException
    {
        // calculate dx, dy and dz for the straight or the arc.
        if (linkTag.nodeStartTag.node != null && linkTag.nodeEndTag.node != null)
        {
            // ARC with both points defined
            if (linkTag.arcTag != null)
            {
                double radiusSI = linkTag.arcTag.radius.getSI();
                ArcDirection direction = linkTag.arcTag.direction;
                OTSPoint3D coordinate =
                    new OTSPoint3D(linkTag.nodeStartTag.node.getLocation().getX(), linkTag.nodeStartTag.node.getLocation()
                        .getY(), linkTag.nodeStartTag.node.getLocation().getZ());
                double startAngle = linkTag.nodeStartTag.node.getDirection().getSI();

                if (direction.equals(ArcDirection.LEFT))
                {
                    linkTag.arcTag.center =
                        new OTSPoint3D(coordinate.x + radiusSI * Math.cos(startAngle + Math.PI / 2.0), coordinate.y
                            + radiusSI * Math.sin(startAngle + Math.PI / 2.0), 0.0);
                    linkTag.arcTag.startAngle = startAngle - Math.PI / 2.0;
                }
                else
                {
                    linkTag.arcTag.center =
                        new OTSPoint3D(coordinate.x + radiusSI * Math.cos(startAngle - Math.PI / 2.0), coordinate.y
                            + radiusSI * Math.sin(startAngle - Math.PI / 2.0), 0.0);
                    linkTag.arcTag.startAngle = startAngle + Math.PI / 2.0;
                }
                return;
            }

            // STRAIGHT with both nodes defined
            if (linkTag.straightTag != null)
            {
                if (linkTag.straightTag.length != null)
                {
                    throw new NetworkException("Parsing network. Link: " + linkTag.name
                        + ", Start node and end node given, but also a length specified");
                }
                linkTag.straightTag.length =
                    linkTag.nodeStartTag.node.getPoint().distance(linkTag.nodeEndTag.node.getPoint());
            }
        }

        if (linkTag.nodeStartTag.node == null && linkTag.nodeEndTag.node == null)
        {
            throw new NetworkException("Parsing network. Link: " + linkTag.name + ", both From-node and To-node are null");
        }

        if (linkTag.straightTag != null)
        {
            double lengthSI = linkTag.straightTag.length.getSI();
            if (linkTag.nodeEndTag.node == null)
            {
                XYZ coordinate =
                    new XYZ(linkTag.nodeStartTag.node.getLocation().getX(), linkTag.nodeStartTag.node.getLocation().getY(),
                        linkTag.nodeStartTag.node.getLocation().getZ());
                double angle = linkTag.nodeStartTag.node.getDirection().getSI();
                double slope = linkTag.nodeStartTag.node.getSlope().getSI();
                coordinate.x += lengthSI * Math.cos(angle);
                coordinate.y += lengthSI * Math.sin(angle);
                coordinate.z += lengthSI * Math.sin(slope);
                NodeTag nodeTag = linkTag.nodeEndTag;
                nodeTag.angle = new AnglePlane.Abs(angle, AnglePlaneUnit.SI);
                nodeTag.coordinate = new OTSPoint3D(coordinate.x, coordinate.y, coordinate.z);
                nodeTag.slope = new AngleSlope.Abs(slope, AngleSlopeUnit.SI);
                linkTag.nodeEndTag.node = NodeTag.makeOTSNode(nodeTag, parser);
            }
            else if (linkTag.nodeStartTag.node == null)
            {
                XYZ coordinate =
                    new XYZ(linkTag.nodeEndTag.node.getLocation().getX(), linkTag.nodeEndTag.node.getLocation().getY(),
                        linkTag.nodeEndTag.node.getLocation().getZ());
                double angle = linkTag.nodeEndTag.node.getDirection().getSI();
                double slope = linkTag.nodeEndTag.node.getSlope().getSI();
                coordinate.x -= lengthSI * Math.cos(angle);
                coordinate.y -= lengthSI * Math.sin(angle);
                coordinate.z -= lengthSI * Math.sin(slope);
                NodeTag nodeTag = linkTag.nodeStartTag;
                nodeTag.angle = new AnglePlane.Abs(angle, AnglePlaneUnit.SI);
                nodeTag.coordinate = new OTSPoint3D(coordinate.x, coordinate.y, coordinate.z);
                nodeTag.slope = new AngleSlope.Abs(slope, AngleSlopeUnit.SI);
                linkTag.nodeStartTag.node = NodeTag.makeOTSNode(nodeTag, parser);
            }
        }
        else if (linkTag.arcTag != null)
        {
            double radiusSI = linkTag.arcTag.radius.getSI();
            double angle = linkTag.arcTag.angle.getSI();
            ArcDirection direction = linkTag.arcTag.direction;

            if (linkTag.nodeEndTag.node == null)
            {
                XYZ coordinate = new XYZ(0.0, 0.0, 0.0);
                double startAngle = linkTag.nodeStartTag.node.getDirection().getSI();
                double slope = linkTag.nodeStartTag.node.getSlope().getSI();
                double lengthSI = radiusSI * angle;
                NodeTag nodeTag = linkTag.nodeEndTag;
                if (direction.equals(ArcDirection.LEFT))
                {
                    linkTag.arcTag.center =
                        new OTSPoint3D(linkTag.nodeStartTag.node.getLocation().getX() + radiusSI
                            * Math.cos(startAngle + Math.PI / 2.0), linkTag.nodeStartTag.node.getLocation().getY()
                            + radiusSI * Math.sin(startAngle + Math.PI / 2.0), 0.0);
                    linkTag.arcTag.startAngle = startAngle - Math.PI / 2.0;
                    coordinate.x = linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle + angle);
                    coordinate.y = linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle + angle);
                    nodeTag.angle = new AnglePlane.Abs(AnglePlaneUnit.normalize(startAngle + angle), AnglePlaneUnit.SI);
                }
                else
                {
                    linkTag.arcTag.center =
                        new OTSPoint3D(linkTag.nodeStartTag.node.getLocation().getX() - radiusSI
                            * Math.cos(startAngle + Math.PI / 2.0), linkTag.nodeStartTag.node.getLocation().getY()
                            - radiusSI * Math.sin(startAngle + Math.PI / 2.0), 0.0);
                    linkTag.arcTag.startAngle = startAngle + Math.PI / 2.0;
                    coordinate.x = linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle - angle);
                    coordinate.y = linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle - angle);
                    nodeTag.angle = new AnglePlane.Abs(AnglePlaneUnit.normalize(startAngle - angle), AnglePlaneUnit.SI);
                }
                coordinate.z = linkTag.nodeStartTag.node.getLocation().getZ() + lengthSI * Math.sin(slope);
                nodeTag.slope = new AngleSlope.Abs(slope, AngleSlopeUnit.SI);
                nodeTag.coordinate = new OTSPoint3D(coordinate.x, coordinate.y, coordinate.z);
                linkTag.nodeEndTag.node = NodeTag.makeOTSNode(nodeTag, parser);
            }

            else if (linkTag.nodeStartTag.node == null)
            {
                XYZ coordinate =
                    new XYZ(linkTag.nodeEndTag.node.getLocation().getX(), linkTag.nodeEndTag.node.getLocation().getY(),
                        linkTag.nodeEndTag.node.getLocation().getZ());
                double endAngle = linkTag.nodeEndTag.node.getDirection().getSI();
                double slope = linkTag.nodeEndTag.node.getSlope().getSI();
                double lengthSI = radiusSI * angle;
                NodeTag nodeTag = linkTag.nodeStartTag;
                if (direction.equals(ArcDirection.LEFT))
                {
                    linkTag.arcTag.center =
                        new OTSPoint3D(coordinate.x + radiusSI * Math.cos(endAngle + Math.PI / 2.0), coordinate.y + radiusSI
                            * Math.sin(endAngle + Math.PI / 2.0), 0.0);
                    linkTag.arcTag.startAngle = endAngle - Math.PI / 2.0 - angle;
                    coordinate.x = linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle);
                    coordinate.y = linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle);
                    nodeTag.angle =
                        new AnglePlane.Abs(AnglePlaneUnit.normalize(linkTag.arcTag.startAngle + Math.PI / 2.0),
                            AnglePlaneUnit.SI);
                }
                else
                {
                    linkTag.arcTag.center =
                        new OTSPoint3D(coordinate.x + radiusSI * Math.cos(endAngle - Math.PI / 2.0), coordinate.y + radiusSI
                            * Math.sin(endAngle - Math.PI / 2.0), 0.0);
                    linkTag.arcTag.startAngle = endAngle + Math.PI / 2.0 + angle;
                    coordinate.x = linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle);
                    coordinate.y = linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle);
                    nodeTag.angle =
                        new AnglePlane.Abs(AnglePlaneUnit.normalize(linkTag.arcTag.startAngle - Math.PI / 2.0),
                            AnglePlaneUnit.SI);
                }
                coordinate.z -= lengthSI * Math.sin(slope);
                nodeTag.coordinate = new OTSPoint3D(coordinate.x, coordinate.y, coordinate.z);
                nodeTag.slope = new AngleSlope.Abs(slope, AngleSlopeUnit.SI);
                linkTag.nodeStartTag.node = NodeTag.makeOTSNode(nodeTag, parser);
            }
        }
    }

    /**
     * Find the nodes one by one that have one coordinate defined, and one not defined, and try to build the network from there.
     * @param linkTag the link to process
     * @param parser the parser with the lists of information
     * @param simulator to be able to make the animation
     * @throws NetworkException when both nodes are null.
     * @throws RemoteException when coordinate cannot be reached.
     * @throws NamingException when node animation cannot link to the animation context.
     */
    static void
        buildLink(final LinkTag linkTag, final XmlNetworkLaneParser parser, final OTSDEVSSimulatorInterface simulator)
            throws RemoteException, NetworkException, NamingException
    {
        int points = 2;
        if (linkTag.arcTag != null)
        {
            points = (Math.abs(linkTag.arcTag.angle.getSI()) <= Math.PI / 2.0) ? 64 : 128;
        }
        NodeTag from = linkTag.nodeStartTag;
        NodeTag to = linkTag.nodeEndTag;
        OTSPoint3D[] coordinates = new OTSPoint3D[points];
        coordinates[0] = new OTSPoint3D(from.coordinate.x, from.coordinate.y, from.coordinate.z);
        coordinates[coordinates.length - 1] = new OTSPoint3D(to.coordinate.x, to.coordinate.y, to.coordinate.z);
        if (linkTag.arcTag != null)
        {
            double angleStep = linkTag.arcTag.angle.getSI() / points;
            double slopeStep = (to.coordinate.z - from.coordinate.z) / points;
            double radiusSI = linkTag.arcTag.radius.getSI();
            if (linkTag.arcTag.direction.equals(ArcDirection.RIGHT))
            {
                for (int p = 1; p < points - 1; p++)
                {
                    coordinates[p] =
                        new OTSPoint3D(linkTag.arcTag.center.x + radiusSI
                            * Math.cos(linkTag.arcTag.startAngle - angleStep * p), linkTag.arcTag.center.y + radiusSI
                            * Math.sin(linkTag.arcTag.startAngle - angleStep * p), from.coordinate.z + slopeStep * p);
                }
            }
            else
            {
                for (int p = 1; p < points - 1; p++)
                {
                    coordinates[p] =
                        new OTSPoint3D(linkTag.arcTag.center.x + radiusSI
                            * Math.cos(linkTag.arcTag.startAngle + angleStep * p), linkTag.arcTag.center.y + radiusSI
                            * Math.sin(linkTag.arcTag.startAngle + angleStep * p), from.coordinate.z + slopeStep * p);
                }
            }
        }
        OTSLine3D designLine = new OTSLine3D(coordinates);
        CrossSectionLink link =
            new CrossSectionLink(linkTag.name, linkTag.nodeStartTag.node, linkTag.nodeEndTag.node, designLine);
        linkTag.link = link;
    }

    /**
     * @param linkTag the link to process
     * @param parser the parser with the lists of information
     * @param simulator to be able to make the animation
     * @throws NetworkException when the stripe cannot be instantiated
     * @throws RemoteException when the (remote) animator cannot be reached to create the animation
     * @throws NamingException when the /animation/2D tree cannot be found in the context
     * @throws SAXException when the stripe type cannot be parsed correctly
     * @throws GTUException when lane block cannot be created
     * @throws OTSGeometryException when construction of the offset-line or contour fails
     * @throws SimRuntimeException when construction of the generator fails
     */
    @SuppressWarnings({"checkstyle:needbraces", "checkstyle:methodlength"})
    static void applyRoadTypeToLink(final LinkTag linkTag, final XmlNetworkLaneParser parser,
        final OTSDEVSSimulatorInterface simulator) throws NetworkException, RemoteException, NamingException, SAXException,
        GTUException, OTSGeometryException, SimRuntimeException
    {
        CrossSectionLink csl = linkTag.link;
        List<CrossSectionElement> cseList = new ArrayList<>();
        List<Lane> lanes = new ArrayList<>();
        for (CrossSectionElementTag cseTag : linkTag.roadTypeTag.cseTags.values())
        {
            switch (cseTag.elementType)
            {
                case STRIPE:
                    switch (cseTag.stripeType)
                    {
                        case BLOCKED:
                        case DASHED:
                            Stripe dashedLine = new Stripe(csl, cseTag.offset, cseTag.width);
                            dashedLine.addPermeability(GTUType.ALL, Permeable.BOTH);
                            if (simulator != null)
                            {
                                new StripeAnimation(dashedLine, simulator, StripeAnimation.TYPE.DASHED);
                            }
                            cseList.add(dashedLine);
                            break;

                        case DOUBLE:
                            Stripe doubleLine = new Stripe(csl, cseTag.offset, cseTag.width);
                            if (simulator != null)
                            {
                                new StripeAnimation(doubleLine, simulator, StripeAnimation.TYPE.DOUBLE);
                            }
                            cseList.add(doubleLine);
                            break;

                        case LEFTONLY:
                            Stripe leftOnlyLine = new Stripe(csl, cseTag.offset, cseTag.width);
                            leftOnlyLine.addPermeability(GTUType.ALL, Permeable.LEFT); // TODO correct?
                            if (simulator != null)
                            {
                                new StripeAnimation(leftOnlyLine, simulator, StripeAnimation.TYPE.LEFTONLY);
                            }
                            cseList.add(leftOnlyLine);
                            break;

                        case RIGHTONLY:
                            Stripe rightOnlyLine = new Stripe(csl, cseTag.offset, cseTag.width);
                            rightOnlyLine.addPermeability(GTUType.ALL, Permeable.RIGHT); // TODO correct?
                            if (simulator != null)
                            {
                                new StripeAnimation(rightOnlyLine, simulator, StripeAnimation.TYPE.RIGHTONLY);
                            }
                            cseList.add(rightOnlyLine);
                            break;

                        case SOLID:
                            Stripe solidLine = new Stripe(csl, cseTag.offset, cseTag.width);
                            if (simulator != null)
                            {
                                new StripeAnimation(solidLine, simulator, StripeAnimation.TYPE.SOLID);
                            }
                            cseList.add(solidLine);
                            break;

                        default:
                            throw new SAXException("Unknown Stripe type: " + cseTag.stripeType.toString());
                    }
                    break;

                case LANE:
                {
                    // TODO LANEOVERRIDE
                    Map<GTUType, LongitudinalDirectionality> directionality = new LinkedHashMap<>();
                    directionality.put(GTUType.ALL, cseTag.direction);
                    Map<GTUType, Speed.Abs> speedLimit = new LinkedHashMap<>();
                    speedLimit.put(GTUType.ALL, cseTag.speed);
                    Lane lane =
                        new Lane(csl, cseTag.name, cseTag.offset, cseTag.offset, cseTag.width, cseTag.width,
                            cseTag.laneType, directionality, speedLimit);
                    cseList.add(lane);
                    lanes.add(lane);
                    linkTag.lanes.put(cseTag.name, lane);
                    if (simulator != null)
                    {
                        new LaneAnimation(lane, simulator, cseTag.color);
                    }

                    // SINK
                    if (linkTag.sinkTags.keySet().contains(cseTag.name))
                    {
                        SinkTag sinkTag = linkTag.sinkTags.get(cseTag.name);
                        Length.Rel position = LinkTag.parseBeginEndPosition(sinkTag.positionStr, lane);
                        Sensor sensor = new SinkSensor(lane, position, simulator);
                        lane.addSensor(sensor, GTUType.ALL);
                    }

                    // BLOCK
                    if (linkTag.blockTags.containsKey(cseTag.name))
                    {
                        BlockTag blockTag = linkTag.blockTags.get(cseTag.name);
                        Length.Rel position = LinkTag.parseBeginEndPosition(blockTag.positionStr, lane);
                        new LaneBlock(lane, position, simulator, null);
                    }

                    // TRAFFICLIGHT
                    if (linkTag.trafficLightTags.containsKey(cseTag.name))
                    {
                        for (TrafficLightTag trafficLightTag : linkTag.trafficLightTags.get(cseTag.name))
                        {
                            try
                            {
                                Class<?> clazz = Class.forName(trafficLightTag.className);
                                Constructor<?> trafficLightConstructor =
                                    ClassUtil.resolveConstructor(clazz, new Class[]{String.class, Lane.class,
                                        Length.Rel.class, OTSDEVSSimulatorInterface.class});
                                Length.Rel position = LinkTag.parseBeginEndPosition(trafficLightTag.positionStr, lane);
                                AbstractTrafficLight trafficLight =
                                    (AbstractTrafficLight) trafficLightConstructor.newInstance(new Object[]{
                                        trafficLightTag.name, lane, position, simulator});
                            }
                            catch (ClassNotFoundException | NoSuchMethodException | InstantiationException
                                | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                                | NetworkException exception)
                            {
                                throw new NetworkException("SENSOR: CLASS NAME " + trafficLightTag.className
                                    + " for sensor " + trafficLightTag.name + " on lane " + lane.toString()
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
                                    ClassUtil.resolveConstructor(clazz, new Class[]{Lane.class, Length.Rel.class,
                                        RelativePosition.TYPE.class, String.class, OTSDEVSSimulatorInterface.class});
                                Length.Rel position = LinkTag.parseBeginEndPosition(sensorTag.positionStr, lane);
                                AbstractSensor sensor =
                                    (AbstractSensor) sensorConstructor.newInstance(new Object[]{lane, position,
                                        sensorTag.triggerPosition, sensorTag.name, simulator});
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
                    // TODO LANEOVERRIDE
                    Lane lane =
                        new NoTrafficLane(csl, cseTag.name, cseTag.offset, cseTag.offset, cseTag.width, cseTag.width);
                    cseList.add(lane);
                    if (simulator != null)
                    {
                        new LaneAnimation(lane, simulator, cseTag.color);
                    }
                    break;
                }

                case SHOULDER:
                {
                    // TODO Override
                    Shoulder shoulder = new Shoulder(csl, cseTag.name, cseTag.offset, cseTag.width, cseTag.width);
                    cseList.add(shoulder);
                    if (simulator != null)
                    {
                        new ShoulderAnimation(shoulder, simulator, cseTag.color);
                    }
                    break;
                }

                default:
                    throw new SAXException("Unknown Element type: " + cseTag.elementType.toString());
            }

        } // for (CrossSectionElementTag cseTag : roadTypeTag.cseTags.values())
    }
}
