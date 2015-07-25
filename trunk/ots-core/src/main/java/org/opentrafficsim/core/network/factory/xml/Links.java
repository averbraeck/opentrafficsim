package org.opentrafficsim.core.network.factory.xml;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.lane.LaneBlock;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.animation.LaneAnimation;
import org.opentrafficsim.core.network.animation.ShoulderAnimation;
import org.opentrafficsim.core.network.animation.StripeAnimation;
import org.opentrafficsim.core.network.factory.xml.ArcTag.ArcDirection;
import org.opentrafficsim.core.network.lane.CrossSectionElement;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.NoTrafficLane;
import org.opentrafficsim.core.network.lane.Shoulder;
import org.opentrafficsim.core.network.lane.SinkLane;
import org.opentrafficsim.core.network.lane.Stripe;
import org.opentrafficsim.core.network.lane.Stripe.Permeable;
import org.opentrafficsim.core.unit.AnglePlaneUnit;
import org.opentrafficsim.core.unit.AngleSlopeUnit;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
                if (linkTag.nodeFromTag.node != null && linkTag.nodeToTag.node != null)
                {
                    calculateNodeCoordinates(linkTag, parser);
                    links.remove(linkTag);
                    found = true;
                    break;
                }
                if (linkTag.nodeFromTag.node != null || linkTag.nodeToTag.node != null)
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
        if (linkTag.nodeFromTag.node != null && linkTag.nodeToTag.node != null)
        {
            if (linkTag.arcTag != null)
            {
                double radiusSI = linkTag.arcTag.radius.getSI();
                ArcDirection direction = linkTag.arcTag.direction;
                OTSPoint3D coordinate =
                    new OTSPoint3D(linkTag.nodeFromTag.node.getLocation().getX(), linkTag.nodeFromTag.node.getLocation()
                        .getY(), linkTag.nodeFromTag.node.getLocation().getZ());
                double startAngle = linkTag.nodeFromTag.node.getDirection().getSI();
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
            }
            return;
        }

        if (linkTag.nodeFromTag.node == null && linkTag.nodeToTag.node == null)
        {
            throw new NetworkException("Parsing network. Link: " + linkTag.name + ", both From-node and To-node are null");
        }

        if (linkTag.straightTag != null)
        {
            double lengthSI = linkTag.straightTag.length.getSI();
            if (linkTag.nodeToTag.node == null)
            {
                XYZ coordinate =
                    new XYZ(linkTag.nodeFromTag.node.getLocation().getX(), linkTag.nodeFromTag.node.getLocation().getY(),
                        linkTag.nodeFromTag.node.getLocation().getZ());
                double angle = linkTag.nodeFromTag.node.getDirection().getSI();
                double slope = linkTag.nodeFromTag.node.getSlope().getSI();
                coordinate.x += lengthSI * Math.cos(angle);
                coordinate.y += lengthSI * Math.sin(angle);
                coordinate.z += lengthSI * Math.sin(slope);
                NodeTag nodeTag = linkTag.nodeToTag;
                nodeTag.angle = new DoubleScalar.Abs<AnglePlaneUnit>(angle, AnglePlaneUnit.SI);
                nodeTag.coordinate = new OTSPoint3D(coordinate.x, coordinate.y, coordinate.z);
                nodeTag.slope = new DoubleScalar.Abs<AngleSlopeUnit>(slope, AngleSlopeUnit.SI);
                linkTag.nodeToTag.node = NodeTag.makeOTSNode(nodeTag, parser);
            }
            else if (linkTag.nodeFromTag.node == null)
            {
                XYZ coordinate =
                    new XYZ(linkTag.nodeToTag.node.getLocation().getX(), linkTag.nodeToTag.node.getLocation().getY(),
                        linkTag.nodeToTag.node.getLocation().getZ());
                double angle = linkTag.nodeToTag.node.getDirection().getSI();
                double slope = linkTag.nodeToTag.node.getSlope().getSI();
                coordinate.x -= lengthSI * Math.cos(angle);
                coordinate.y -= lengthSI * Math.sin(angle);
                coordinate.z -= lengthSI * Math.sin(slope);
                NodeTag nodeTag = linkTag.nodeFromTag;
                nodeTag.angle = new DoubleScalar.Abs<AnglePlaneUnit>(angle, AnglePlaneUnit.SI);
                nodeTag.coordinate = new OTSPoint3D(coordinate.x, coordinate.y, coordinate.z);
                nodeTag.slope = new DoubleScalar.Abs<AngleSlopeUnit>(slope, AngleSlopeUnit.SI);
                linkTag.nodeFromTag.node = NodeTag.makeOTSNode(nodeTag, parser);
            }
        }
        else if (linkTag.arcTag != null)
        {
            double radiusSI = linkTag.arcTag.radius.getSI();
            double angle = linkTag.arcTag.angle.getSI();
            ArcDirection direction = linkTag.arcTag.direction;
            if (linkTag.nodeToTag.node == null)
            {
                XYZ coordinate = new XYZ(0.0, 0.0, 0.0);
                double startAngle = linkTag.nodeFromTag.node.getDirection().getSI();
                double slope = linkTag.nodeFromTag.node.getSlope().getSI();
                double lengthSI = radiusSI * angle;
                NodeTag nodeTag = linkTag.nodeToTag;
                if (direction.equals(ArcDirection.LEFT))
                {
                    linkTag.arcTag.center =
                        new OTSPoint3D(linkTag.nodeFromTag.node.getLocation().getX() + radiusSI
                            * Math.cos(startAngle + Math.PI / 2.0), linkTag.nodeFromTag.node.getLocation().getY() + radiusSI
                            * Math.sin(startAngle + Math.PI / 2.0), 0.0);
                    linkTag.arcTag.startAngle = startAngle - Math.PI / 2.0;
                    coordinate.x = linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle + angle);
                    coordinate.y = linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle + angle);
                    nodeTag.angle =
                        new DoubleScalar.Abs<AnglePlaneUnit>(AnglePlaneUnit.normalize(startAngle + angle), AnglePlaneUnit.SI);
                }
                else
                {
                    linkTag.arcTag.center =
                        new OTSPoint3D(coordinate.x + radiusSI * Math.cos(startAngle - Math.PI / 2.0), coordinate.y
                            + radiusSI * Math.sin(startAngle - Math.PI / 2.0), 0.0);
                    linkTag.arcTag.startAngle = startAngle;
                    coordinate.x = linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle - angle);
                    coordinate.y = linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle - angle);
                    nodeTag.angle =
                        new DoubleScalar.Abs<AnglePlaneUnit>(AnglePlaneUnit.normalize(startAngle - angle), AnglePlaneUnit.SI);
                }
                coordinate.z = linkTag.nodeFromTag.node.getLocation().getZ() + lengthSI * Math.sin(slope);
                nodeTag.slope = new DoubleScalar.Abs<AngleSlopeUnit>(slope, AngleSlopeUnit.SI);
                nodeTag.coordinate = new OTSPoint3D(coordinate.x, coordinate.y, coordinate.z);
                linkTag.nodeToTag.node = NodeTag.makeOTSNode(nodeTag, parser);
            }

            else if (linkTag.nodeFromTag.node == null)
            {
                XYZ coordinate =
                    new XYZ(linkTag.nodeToTag.node.getLocation().getX(), linkTag.nodeToTag.node.getLocation().getY(),
                        linkTag.nodeToTag.node.getLocation().getZ());
                double endAngle = linkTag.nodeToTag.node.getDirection().getSI();
                double slope = linkTag.nodeToTag.node.getSlope().getSI();
                double lengthSI = radiusSI * angle;
                NodeTag nodeTag = linkTag.nodeFromTag;
                if (direction.equals(ArcDirection.LEFT))
                {
                    linkTag.arcTag.center =
                        new OTSPoint3D(coordinate.x + radiusSI + Math.cos(endAngle + Math.PI / 2.0), coordinate.y + radiusSI
                            * Math.sin(endAngle + Math.PI / 2.0), 0.0);
                    linkTag.arcTag.startAngle = endAngle - Math.PI / 2.0 - angle;
                    coordinate.x = linkTag.arcTag.center.x + radiusSI * Math.cos(linkTag.arcTag.startAngle);
                    coordinate.y = linkTag.arcTag.center.y + radiusSI * Math.sin(linkTag.arcTag.startAngle);
                    nodeTag.angle =
                        new DoubleScalar.Abs<AnglePlaneUnit>(AnglePlaneUnit.normalize(linkTag.arcTag.startAngle + Math.PI
                            / 2.0), AnglePlaneUnit.SI);
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
                        new DoubleScalar.Abs<AnglePlaneUnit>(AnglePlaneUnit.normalize(linkTag.arcTag.startAngle - Math.PI
                            / 2.0), AnglePlaneUnit.SI);
                }
                coordinate.z -= lengthSI * Math.sin(slope);
                nodeTag.coordinate = new OTSPoint3D(coordinate.x, coordinate.y, coordinate.z);
                nodeTag.slope = new DoubleScalar.Abs<AngleSlopeUnit>(slope, AngleSlopeUnit.SI);
                linkTag.nodeFromTag.node = NodeTag.makeOTSNode(nodeTag, parser);
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
            points = (Math.abs(linkTag.arcTag.angle.getSI()) <= Math.PI / 2.0) ? 32 : 64;
        }
        NodeTag from = linkTag.nodeFromTag;
        NodeTag to = linkTag.nodeToTag;
        Coordinate[] coordinates = new Coordinate[points];
        coordinates[0] = new Coordinate(from.coordinate.x, from.coordinate.y, from.coordinate.z);
        coordinates[coordinates.length - 1] = new Coordinate(to.coordinate.x, to.coordinate.y, to.coordinate.z);
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
                        new Coordinate(linkTag.arcTag.center.x + radiusSI
                            * Math.cos(linkTag.arcTag.startAngle - angleStep * p), linkTag.arcTag.center.y + radiusSI
                            * Math.sin(linkTag.arcTag.startAngle - angleStep * p), from.coordinate.z + slopeStep * p);
                }
            }
            else
            {
                for (int p = 1; p < points - 1; p++)
                {
                    coordinates[p] =
                        new Coordinate(linkTag.arcTag.center.x + radiusSI
                            * Math.cos(linkTag.arcTag.startAngle + angleStep * p), linkTag.arcTag.center.y + radiusSI
                            * Math.sin(linkTag.arcTag.startAngle + angleStep * p), from.coordinate.z + slopeStep * p);
                }
            }
        }
        OTSLine3D designLine = new OTSLine3D(coordinates);
        CrossSectionLink<String, String> link =
            new CrossSectionLink<>(linkTag.name, linkTag.nodeFromTag.node, linkTag.nodeToTag.node, designLine);
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
     */
    @SuppressWarnings({"checkstyle:needbraces", "checkstyle:methodlength"})
    static void applyRoadTypeToLink(final LinkTag linkTag, final XmlNetworkLaneParser parser,
        final OTSDEVSSimulatorInterface simulator) throws NetworkException, RemoteException, NamingException, SAXException,
        GTUException
    {
        CrossSectionLink<String, String> csl = linkTag.link;
        List<CrossSectionElement<String, String>> cseList = new ArrayList<>();
        List<Lane<String, String>> lanes = new ArrayList<>();
        for (CrossSectionElementTag cseTag : linkTag.roadTypeTag.cseTags.values())
        {
            switch (cseTag.elementType)
            {
                case STRIPE:
                    switch (cseTag.stripeType)
                    {
                        case BLOCKED:
                        case DASHED:
                            Stripe<String, String> dashedLine = new Stripe<>(csl, cseTag.offset, cseTag.width);
                            dashedLine.addPermeability(GTUType.ALL, Permeable.BOTH);
                            if (simulator != null)
                            {
                                new StripeAnimation(dashedLine, simulator, StripeAnimation.TYPE.DASHED);
                            }
                            cseList.add(dashedLine);
                            break;

                        case DOUBLE:
                            Stripe<String, String> doubleLine = new Stripe<>(csl, cseTag.offset, cseTag.width);
                            if (simulator != null)
                            {
                                new StripeAnimation(doubleLine, simulator, StripeAnimation.TYPE.DOUBLE);
                            }
                            cseList.add(doubleLine);
                            break;

                        case LEFTONLY:
                            Stripe<String, String> leftOnlyLine = new Stripe<>(csl, cseTag.offset, cseTag.width);
                            leftOnlyLine.addPermeability(GTUType.ALL, Permeable.LEFT); // TODO correct?
                            if (simulator != null)
                            {
                                new StripeAnimation(leftOnlyLine, simulator, StripeAnimation.TYPE.LEFTONLY);
                            }
                            cseList.add(leftOnlyLine);
                            break;

                        case RIGHTONLY:
                            Stripe<String, String> rightOnlyLine = new Stripe<>(csl, cseTag.offset, cseTag.width);
                            rightOnlyLine.addPermeability(GTUType.ALL, Permeable.RIGHT); // TODO correct?
                            if (simulator != null)
                            {
                                new StripeAnimation(rightOnlyLine, simulator, StripeAnimation.TYPE.RIGHTONLY);
                            }
                            cseList.add(rightOnlyLine);
                            break;

                        case SOLID:
                            Stripe<String, String> solidLine = new Stripe<>(csl, cseTag.offset, cseTag.width);
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
                    if (linkTag.sinkTags.keySet().contains(cseTag.name))
                    {
                        // SINKLANE
                        SinkLane<String, String> sinkLane =
                            new SinkLane<>(csl, cseTag.offset, cseTag.width, cseTag.laneType, cseTag.direction, cseTag.speed);
                        cseList.add(sinkLane);
                        lanes.add(sinkLane);
                        linkTag.lanes.put(cseTag.name, sinkLane);
                        if (simulator != null)
                        {
                            new LaneAnimation(sinkLane, simulator, cseTag.color);
                        }
                    }

                    else

                    {
                        // TODO LANEOVERRIDE
                        Lane<String, String> lane =
                            new Lane<>(csl, cseTag.offset, cseTag.offset, cseTag.width, cseTag.width, cseTag.laneType,
                                cseTag.direction, new DoubleScalar.Abs<FrequencyUnit>(Double.MAX_VALUE,
                                    FrequencyUnit.PER_HOUR), cseTag.speed);
                        cseList.add(lane);
                        lanes.add(lane);
                        linkTag.lanes.put(cseTag.name, lane);
                        if (simulator != null)
                        {
                            new LaneAnimation(lane, simulator, cseTag.color);
                        }

                        // BLOCK
                        if (linkTag.blockTags.containsKey(cseTag.name))
                        {
                            BlockTag blockTag = linkTag.blockTags.get(cseTag.name);
                            new LaneBlock(lane, blockTag.position, simulator, null);
                        }

                        // GENERATOR
                        if (linkTag.generatorTags.containsKey(cseTag.name))
                        {
                            GeneratorTag generatorTag = linkTag.generatorTags.get(cseTag.name);
                            // TODO Generators.makeGenerator(generatorTag, lane, cseTag.name);
                        }

                        // TODO FILL

                    }
                    break;
                }

                case NOTRAFFICLANE:
                {
                    // TODO Override
                    Lane<String, String> lane =
                        new NoTrafficLane<>(csl, cseTag.offset, cseTag.offset, cseTag.width, cseTag.width);
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
                    Shoulder<String, String> shoulder = new Shoulder<>(csl, cseTag.offset, cseTag.width, cseTag.width);
                    cseList.add(shoulder);
                    if (simulator != null)
                    {
                        new ShoulderAnimation(shoulder, simulator);
                        // TODO color
                    }
                    break;
                }

                default:
                    throw new SAXException("Unknown Element type: " + cseTag.elementType.toString());
            }
        } // for (CrossSectionElementTag cseTag : roadTypeTag.cseTags.values())

        // make adjacent lanes
        for (int laneIndex = 1; laneIndex < lanes.size(); laneIndex++)
        {
            lanes.get(laneIndex - 1).addAccessibleAdjacentLane(lanes.get(laneIndex), LateralDirectionality.RIGHT);
            lanes.get(laneIndex).addAccessibleAdjacentLane(lanes.get(laneIndex - 1), LateralDirectionality.LEFT);
        }
    }

}
