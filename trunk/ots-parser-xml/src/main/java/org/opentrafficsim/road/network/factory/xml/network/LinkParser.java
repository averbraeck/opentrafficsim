package org.opentrafficsim.road.network.factory.xml.network;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.AngleUtil;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.logger.CategoryLogger;
import org.djutils.reflection.ClassUtil;
import org.opentrafficsim.base.logger.Cat;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.Bezier;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.factory.xml.units.SpeedUnits;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.CrossSectionLink.Priority;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.NoTrafficLane;
import org.opentrafficsim.road.network.lane.Shoulder;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.Stripe.Permeable;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.changing.OvertakingConditions;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.xml.bindings.types.ArcDirection;
import org.opentrafficsim.xml.generated.CROSSSECTIONELEMENT;
import org.opentrafficsim.xml.generated.CSELANE;
import org.opentrafficsim.xml.generated.CSENOTRAFFICLANE;
import org.opentrafficsim.xml.generated.CSESHOULDER;
import org.opentrafficsim.xml.generated.CSESTRIPE;
import org.opentrafficsim.xml.generated.LINK;
import org.opentrafficsim.xml.generated.LINK.LANEOVERRIDE;
import org.opentrafficsim.xml.generated.NETWORK;
import org.opentrafficsim.xml.generated.ROADLAYOUT;
import org.opentrafficsim.xml.generated.TRAFFICLIGHTTYPE;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * LinkParser parses the LINK tags in the XML network.. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public final class LinkParser
{
    /** */
    public LinkParser()
    {
        // utility class
    }

    /**
     * Build the links with the correct design line.
     * @param otsNetwork OTSNetwork; the network to insert the parsed objects in
     * @param network NETWORK; the NETWORK tag
     * @param nodeDirections Map&lt;String,Direction&gt;; a map of the node ids and their default directions
     * @param simulator OTSSimulatorInterface; the simulator
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OTSGeometryException when the design line is invalid
     */
    static void parseLinks(final OTSNetwork otsNetwork, final NETWORK network, Map<String, Direction> nodeDirections,
            OTSSimulatorInterface simulator) throws NetworkException, OTSGeometryException
    {
        for (LINK xmlLink : network.getLINK())
        {
            Node startNode = otsNetwork.getNode(xmlLink.getNODESTART().getNAME());
            Node endNode = otsNetwork.getNode(xmlLink.getNODEEND().getNAME());
            double startDirection =
                    nodeDirections.containsKey(startNode.getId()) ? nodeDirections.get(startNode.getId()).getInUnit() : 0.0;
            double endDirection =
                    nodeDirections.containsKey(endNode.getId()) ? nodeDirections.get(endNode.getId()).getInUnit() : 0.0;
            OTSPoint3D startPoint = new OTSPoint3D(startNode.getPoint());
            OTSPoint3D endPoint = new OTSPoint3D(endNode.getPoint());

            if (!xmlLink.getOFFSETSTART().eq0())
            {
                // shift the start point perpendicular to the node direction or read from tag
                double offset = xmlLink.getOFFSETSTART().si;
                startPoint = new OTSPoint3D(startPoint.x + offset * Math.cos(startDirection + Math.PI / 2.0),
                        startPoint.y + offset * Math.sin(startDirection + Math.PI / 2.0), startPoint.z);
                CategoryLogger.filter(Cat.PARSER).debug("fc = " + startNode.getPoint() + ", sa = " + startDirection + ", so = "
                        + offset + ", sp = " + startPoint);
            }

            if (!xmlLink.getOFFSETEND().eq0())
            {
                // shift the end point perpendicular to the node direction or read from tag
                double offset = xmlLink.getOFFSETEND().si;
                endPoint = new OTSPoint3D(endPoint.x + offset * Math.cos(endDirection + Math.PI / 2.0),
                        endPoint.y + offset * Math.sin(endDirection + Math.PI / 2.0), endPoint.z);
                CategoryLogger.filter(Cat.PARSER).debug(
                        "tc = " + endNode.getPoint() + ", ea = " + endDirection + ", eo = " + offset + ", ep = " + endPoint);
            }

            OTSPoint3D[] coordinates = null;

            if (xmlLink.getSTRAIGHT() != null)
            {
                coordinates = new OTSPoint3D[2];
                coordinates[0] = startPoint;
                coordinates[1] = endPoint;
            }

            else if (xmlLink.getPOLYLINE() != null)
            {
                int intermediatePoints = xmlLink.getPOLYLINE().getINTERMEDIATEPOINTS().size();
                coordinates = new OTSPoint3D[intermediatePoints + 2];
                coordinates[0] = startPoint;
                coordinates[intermediatePoints + 1] = endPoint;
                for (int p = 0; p < intermediatePoints; p++)
                {
                    coordinates[p + 1] = new OTSPoint3D(xmlLink.getPOLYLINE().getINTERMEDIATEPOINTS().get(p));
                }

            }
            else if (xmlLink.getARC() != null)
            {
                // calculate the center position
                double radiusSI = xmlLink.getARC().getRADIUS().getSI();
                double offsetStart = 0.0;
                if (xmlLink.getOFFSETSTART() != null)
                {
                    offsetStart = xmlLink.getOFFSETSTART().si;
                }
                double offsetEnd = 0.0;
                if (xmlLink.getOFFSETEND() != null)
                {
                    offsetEnd = xmlLink.getOFFSETEND().si;
                }
                List<OTSPoint3D> centerList = OTSPoint3D.circleIntersections(startNode.getPoint(), radiusSI + offsetStart,
                        endNode.getPoint(), radiusSI + offsetEnd);
                OTSPoint3D center =
                        (xmlLink.getARC().getDIRECTION().equals(ArcDirection.RIGHT)) ? centerList.get(0) : centerList.get(1);

                // calculate start angle and end angle
                double sa = Math.atan2(startNode.getPoint().y - center.y, startNode.getPoint().x - center.x);
                double ea = Math.atan2(endNode.getPoint().y - center.y, endNode.getPoint().x - center.x);
                if (xmlLink.getARC().getDIRECTION().equals(ArcDirection.RIGHT))
                {
                    // right -> negative direction, ea should be less than sa
                    ea = (sa < ea) ? ea + Math.PI * 2.0 : ea;
                }
                else
                {
                    // left -> positive direction, sa should be less than ea
                    ea = (ea < sa) ? ea + Math.PI * 2.0 : ea;
                }

                // TODO: user defined #points
                int points = (AngleUtil.normalize(ea - sa) <= Math.PI / 2.0) ? 64 : 128;
                coordinates = new OTSPoint3D[points];
                coordinates[0] = new OTSPoint3D(startNode.getPoint().x + Math.cos(sa) * offsetStart,
                        startNode.getPoint().y + Math.sin(sa) * offsetStart, startNode.getPoint().z);
                coordinates[coordinates.length - 1] = new OTSPoint3D(endNode.getPoint().x + Math.cos(ea) * offsetEnd,
                        endNode.getPoint().y + Math.sin(ea) * offsetEnd, endNode.getPoint().z);
                double angleStep = Math.abs((ea - sa)) / points;
                double slopeStep = (endNode.getPoint().z - startNode.getPoint().z) / points;

                if (xmlLink.getARC().getDIRECTION().equals(ArcDirection.RIGHT))
                {
                    for (int p = 1; p < points - 1; p++)
                    {
                        double dRad = offsetStart + (offsetEnd - offsetStart) * p / points;
                        coordinates[p] = new OTSPoint3D(center.x + (radiusSI + dRad) * Math.cos(sa - angleStep * p),
                                center.y + (radiusSI + dRad) * Math.sin(sa - angleStep * p),
                                startNode.getPoint().z + slopeStep * p);
                    }
                }
                else
                {
                    for (int p = 1; p < points - 1; p++)
                    {
                        double dRad = offsetStart + (offsetEnd - offsetStart) * p / points;
                        coordinates[p] = new OTSPoint3D(center.x + (radiusSI + dRad) * Math.cos(sa + angleStep * p),
                                center.y + (radiusSI + dRad) * Math.sin(sa + angleStep * p),
                                startNode.getPoint().z + slopeStep * p);
                    }
                }
            }

            else if (xmlLink.getBEZIER() != null)
            {
                // TODO: user defined #points

                coordinates = Bezier
                        .cubic(128, new DirectedPoint(startPoint.x, startPoint.y, startPoint.z, 0, 0, startDirection),
                                new DirectedPoint(endPoint.x, endPoint.y, endPoint.z, 0, 0, endDirection), 1.0, false)
                        .getPoints();

                // TODO: Bezier shape factor and weighted factor
            }

            else if (xmlLink.getCLOTHOID() != null)
            {
                // TODO: user defined #points
                // TODO: Clothoid parsing
            }

            else
            {
                throw new NetworkException("Making link, but link " + xmlLink.getNAME()
                        + " has no filled straight, arc, bezier, polyline, or clothoid definition");
            }

            OTSLine3D designLine = OTSLine3D.createAndCleanOTSLine3D(coordinates);

            // TODO: Directionality has to be added later when the lanes and their direction are known.
            LaneKeepingPolicy laneKeepingPolicy = LaneKeepingPolicy.valueOf(xmlLink.getLANEKEEPING().name());
            CrossSectionLink link = new CrossSectionLink(otsNetwork, xmlLink.getNAME(), startNode, endNode, LinkType.FREEWAY,
                    designLine, simulator, laneKeepingPolicy);

            if (xmlLink.getPRIORITY() != null)
            {
                Priority priority = Priority.valueOf(xmlLink.getPRIORITY());
                link.setPriority(priority);
            }

            // TODO: rotationstart and rotationend of a link, or leave out of xsd?
            // TODO: networkAnimation.addDrawingInfoBase(link, new DrawingInfoLine<CrossSectionLink>(Color.BLACK, 0.5f));
        }
    }

    /**
     * Build the links with the correct design line.
     * @param otsNetwork OTSNetwork; the network to insert the parsed objects in
     * @param network NETWORK; the NETWORK tag
     * @param simulator OTSSimulatorInterface; the simulator
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OTSGeometryException when the design line is invalid
     * @throws XmlParserException when the stripe type cannot be recognized
     */
    static void applyRoadTypes(final OTSNetwork otsNetwork, final NETWORK network, OTSSimulatorInterface simulator)
            throws NetworkException, OTSGeometryException, XmlParserException
    {
        for (LINK xmlLink : network.getLINK())
        {
            CrossSectionLink csl = (CrossSectionLink) otsNetwork.getLink(xmlLink.getNAME());
            List<CrossSectionElement> cseList = new ArrayList<>();
            Map<String, Lane> lanes = new HashMap<>();
            // TODO: Map<GTUType, LongitudinalDirectionality> linkDirections = new HashMap<>();

            System.out.println(xmlLink.getNAME());

            // CROSSSECTIONELEMENT
            // XXX: does not work! Simplify the network.xsd...
            ROADLAYOUT roadlayout = Parser.findObject(network.getDEFINITIONS(), ROADLAYOUT.class, new Predicate<ROADLAYOUT>()
            {
                @Override
                public boolean test(ROADLAYOUT t)
                {
                    return t.getNAME().equals(xmlLink.getROADLAYOUT());
                }
            });
            for (CROSSSECTIONELEMENT cse : roadlayout.getLANEOrNOTRAFFICLANEOrSHOULDER())
            {
                LANEOVERRIDE laneOverride = null;
                for (LANEOVERRIDE lo : xmlLink.getLANEOVERRIDE())
                {
                    if (lo.getLANE().equals(cse.getNAME()))
                        laneOverride = lo;
                }
                Length startOffset = cse.getOFFSET() != null ? cse.getOFFSET() : xmlLink.getOFFSETSTART();
                Length endOffset = cse.getOFFSET() != null ? cse.getOFFSET() : xmlLink.getOFFSETEND();

                if (cse instanceof CSESTRIPE)
                {
                    makeStripe(csl, startOffset, endOffset, cse, cseList);
                }
                else if (cse instanceof CSELANE)
                {
                    CSELANE cseLane = (CSELANE) cse;
                    LongitudinalDirectionality direction = LongitudinalDirectionality.valueOf(cseLane.getDIRECTION().name());
                    // TODO: The LaneType should be defined in the XML...
                    // TODO: how to handle cseLane.getSPEEDLIMIT()? GTUType specific...
                    Lane lane = new Lane(csl, cseLane.getNAME(), startOffset, endOffset, cseLane.getWIDTH(), cseLane.getWIDTH(),
                            LaneType.FREEWAY, new Speed(100.0, SpeedUnit.KM_PER_HOUR),
                            parseOvertakingConditions(cseLane.getOVERTAKING()));
                    cseList.add(lane);
                    lanes.put(lane.getId(), lane);
                    // TODO: deal with cse.getCOLOR() where the laneOverrideTag can also have a color
                }
                else if (cse instanceof CSENOTRAFFICLANE)
                {
                    Lane lane = new NoTrafficLane(csl, cse.getNAME(), startOffset, endOffset, cse.getWIDTH(), cse.getWIDTH());
                    cseList.add(lane);
                    // TODO: deal with cse.getCOLOR() where the laneOverrideTag can also have a color
                }
                else if (cse instanceof CSESHOULDER)
                {
                    Shoulder shoulder =
                            new Shoulder(csl, cse.getNAME(), startOffset, endOffset, cse.getWIDTH(), cse.getWIDTH());
                    cseList.add(shoulder);
                    // TODO: deal with cse.getCOLOR() where the laneOverrideTag can also have a color
                }
            }

            // SINK
            for (LINK.SINK sink : xmlLink.getSINK())
            {
                if (!lanes.containsKey(sink.getLANE()))
                    throw new NetworkException(
                            "LINK: " + xmlLink.getNAME() + ", Sink on Lane " + sink.getLANE() + " - Lane not found");
                Lane lane = lanes.get(sink.getLANE());
                Length position = Transformer.parseLengthBeginEnd(sink.getPOSITION(), lane.getLength());
                new SinkSensor(lane, position, simulator);
            }

            // TRAFFICLIGHT
            for (TRAFFICLIGHTTYPE trafficLight : xmlLink.getTRAFFICLIGHT())
            {
                if (!lanes.containsKey(trafficLight.getLANE()))
                    throw new NetworkException("LINK: " + xmlLink.getNAME() + ", TrafficLight with id " + trafficLight.getNAME()
                            + " on Lane " + trafficLight.getLANE() + " - Lane not found");
                Lane lane = lanes.get(trafficLight.getLANE());
                Length position = Transformer.parseLengthBeginEnd(trafficLight.getPOSITION(), lane.getLength());
                try
                {
                    Constructor<?> trafficLightConstructor = ClassUtil.resolveConstructor(trafficLight.getCLASS(),
                            new Class[] {String.class, Lane.class, Length.class, DEVSSimulatorInterface.TimeDoubleUnit.class});
                    trafficLightConstructor.newInstance(new Object[] {trafficLight.getNAME(), lane, position, simulator});
                }
                catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException exception)
                {
                    throw new NetworkException("TRAFFICLIGHT: CLASS NAME " + trafficLight.getCLASS().getName()
                            + " for traffic light " + trafficLight.getNAME() + " on lane " + lane.toString()
                            + " -- class not found or constructor not right", exception);
                }
            }

            // GENERATOR
            for (LINK.GENERATOR generator : xmlLink.getGENERATOR())
            {
                if (!lanes.containsKey(generator.getLANE()))
                    throw new NetworkException(
                            "LINK: " + xmlLink.getNAME() + ", Generator on Lane " + generator.getLANE() + " - Lane not found");
                Lane lane = lanes.get(generator.getLANE());
                Length position = Transformer.parseLengthBeginEnd(generator.getPOSITION(), lane.getLength());
                // TODO: makeGenerator(generator, xmlLink, simulator);
            }

            // TODO: LISTGENERATOR

            // SENSOR
            for (LINK.SENSOR sensor : xmlLink.getSENSOR())
            {
                if (!lanes.containsKey(sensor.getLANE()))
                    throw new NetworkException("LINK: " + xmlLink.getNAME() + ", Sensor with id " + sensor.getNAME()
                            + "  on Lane " + sensor.getLANE() + " - Lane not found");
                Lane lane = lanes.get(sensor.getLANE());
                Length position = Transformer.parseLengthBeginEnd(sensor.getPOSITION(), lane.getLength());
                try
                {
                    Constructor<?> sensorConstructor = ClassUtil.resolveConstructor(sensor.getCLASS(),
                            new Class[] {String.class, Lane.class, Length.class, RelativePosition.TYPE.class,
                                    DEVSSimulatorInterface.TimeDoubleUnit.class, Compatible.class});
                    sensorConstructor.newInstance(new Object[] {sensor.getNAME(), lane, position,
                            Transformer.parseTriggerPosition(sensor.getTRIGGER()), simulator, Compatible.EVERYTHING});
                }
                catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException exception)
                {
                    throw new NetworkException(
                            "SENSOR: CLASS NAME " + sensor.getCLASS().getName() + " for sensor " + sensor.getNAME()
                                    + " on lane " + lane.toString() + " -- class not found or constructor not right",
                            exception);
                }
            }

            // FILL
            for (LINK.FILL fill : xmlLink.getFILL())
            {
                if (!lanes.containsKey(fill.getLANE()))
                    throw new NetworkException(
                            "LINK: " + xmlLink.getNAME() + ", Fill on Lane " + fill.getLANE() + " - Lane not found");
                Lane lane = lanes.get(fill.getLANE());

                // TODO: parseFill(fill, xmlLink, simulator);
            }
        }
    }

    /**
     * Parse a stripe on a road.
     * @param csl CrossSectionLink; the CrossSectionLine
     * @param startOffset Length; the offset of the start node
     * @param endOffset Length; the offset of the end node
     * @param cse CROSSSECTIONELEMENT; the CROSSECTIONELEMENT tag in the XML file
     * @param cseList List&lt;CrossSectionElement&gt;; the list of CrossSectionElements to which the stripes should be added
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id of the stripe not unique
     * @throws XmlParserException when the stripe type cannot be recognized
     */
    private static void makeStripe(final CrossSectionLink csl, final Length startOffset, final Length endOffset,
            final CROSSSECTIONELEMENT cse, final List<CrossSectionElement> cseList)
            throws OTSGeometryException, NetworkException, XmlParserException
    {
        switch (((CSESTRIPE) cse).getTYPE())
        {
            case BLOCKED:
            case DASHED:
                Stripe dashedLine = new Stripe(csl, startOffset, endOffset, cse.getWIDTH());
                dashedLine.addPermeability(GTUType.VEHICLE, Permeable.BOTH);
                // TODO: parser.networkAnimation.addDrawingInfoBase(dashedLine,
                // TODO: new DrawingInfoStripe<Stripe>(Color.BLACK, 0.5f, StripeType.DASHED));
                cseList.add(dashedLine);
                break;

            case DOUBLE:
                Stripe doubleLine = new Stripe(csl, startOffset, endOffset, cse.getWIDTH());
                // TODO: parser.networkAnimation.addDrawingInfoBase(doubleLine,
                // TODO: new DrawingInfoStripe<Stripe>(Color.BLACK, 0.5f, StripeType.DOUBLE));
                cseList.add(doubleLine);
                break;

            case LEFTONLY:
                Stripe leftOnlyLine = new Stripe(csl, startOffset, endOffset, cse.getWIDTH());
                leftOnlyLine.addPermeability(GTUType.VEHICLE, Permeable.LEFT); // TODO correct?
                // TODO: parser.networkAnimation.addDrawingInfoBase(leftOnlyLine,
                // TODO: new DrawingInfoStripe<Stripe>(Color.BLACK, 0.5f, StripeType.LEFTONLY));
                cseList.add(leftOnlyLine);
                break;

            case RIGHTONLY:
                Stripe rightOnlyLine = new Stripe(csl, startOffset, endOffset, cse.getWIDTH());
                rightOnlyLine.addPermeability(GTUType.VEHICLE, Permeable.RIGHT); // TODO correct?
                // TODO: parser.networkAnimation.addDrawingInfoBase(rightOnlyLine,
                // TODO: new DrawingInfoStripe<Stripe>(Color.BLACK, 0.5f, StripeType.RIGHTONLY));
                cseList.add(rightOnlyLine);
                break;

            case SOLID:
                Stripe solidLine = new Stripe(csl, startOffset, endOffset, cse.getWIDTH());
                // TODO: parser.networkAnimation.addDrawingInfoBase(solidLine,
                // TODO: new DrawingInfoStripe<Stripe>(Color.BLACK, 0.5f, StripeType.SOLID));
                cseList.add(solidLine);
                break;

            default:
                throw new XmlParserException("Unknown Stripe type: " + ((CSESTRIPE) cse).getTYPE().toString());
        }
    }

    /**
     * @param ocStr String; the overtaking conditions string.
     * @return the overtaking conditions.
     * @throws NetworkException in case of unknown overtaking conditions.
     */
    private static OvertakingConditions parseOvertakingConditions(String ocStr) throws NetworkException
    {
        if (ocStr.equals("LEFTONLY"))
        {
            return new OvertakingConditions.LeftOnly();
        }
        else if (ocStr.equals("RIGHTONLY"))
        {
            return new OvertakingConditions.RightOnly();
        }
        else if (ocStr.equals("LEFTANDRIGHT"))
        {
            return new OvertakingConditions.LeftAndRight();
        }
        else if (ocStr.equals("NONE"))
        {
            return new OvertakingConditions.None();
        }
        else if (ocStr.equals("SAMELANERIGHT"))
        {
            return new OvertakingConditions.SameLaneRight();
        }
        else if (ocStr.equals("SAMELANELEFT"))
        {
            return new OvertakingConditions.SameLaneLeft();
        }
        else if (ocStr.equals("SAMELANEBOTH"))
        {
            return new OvertakingConditions.SameLaneBoth();
        }
        else if (ocStr.startsWith("LEFTALWAYS RIGHTSPEED"))
        {
            int lb = ocStr.indexOf('(');
            int rb = ocStr.indexOf(')');
            if (lb == -1 || rb == -1 || rb - lb < 3)
            {
                throw new NetworkException("Speed in overtaking conditions string: '" + ocStr + "' not coded right");
            }
            Speed speed = SpeedUnits.parseSpeed(ocStr.substring(lb + 1, rb));
            return new OvertakingConditions.LeftAlwaysRightSpeed(speed);
        }
        else if (ocStr.startsWith("RIGHTALWAYS LEFTSPEED"))
        {
            int lb = ocStr.indexOf('(');
            int rb = ocStr.indexOf(')');
            if (lb == -1 || rb == -1 || rb - lb < 3)
            {
                throw new NetworkException("Speed in overtaking conditions string: '" + ocStr + "' not coded right");
            }
            Speed speed = SpeedUnits.parseSpeed(ocStr.substring(lb + 1, rb));
            return new OvertakingConditions.RightAlwaysLeftSpeed(speed);
        }

        // TODO SETs and JAM
        /*-
        else if (ocStr.startsWith("LEFTSET"))
        {
            int lset1 = ocStr.indexOf('[') + 1;
            int rset1 = ocStr.indexOf(']', lset1);
            int lset2 = ocStr.indexOf('[', ocStr.indexOf("OVERTAKE")) + 1;
            int rset2 = ocStr.indexOf(']', lset2);
            if (lset1 == -1 || rset1 == -1 || rset1 - lset1 < 3 || lset2 == -1 || rset2 == -1 || rset2 - lset2 < 3)
            {
                throw new NetworkException("Sets in overtaking conditions string: '" + ocStr + "' not coded right");
            }
            Set<GTUType> overtakingGTUs = parseGTUTypeSet(ocStr.substring(lset1, rset1));
            Set<GTUType> overtakenGTUs = parseGTUTypeSet(ocStr.substring(lset2, rset2));
            if (ocStr.contains("RIGHTSPEED"))
            {
                int i = ocStr.indexOf("RIGHTSPEED");
                int lb = ocStr.indexOf('(', i);
                int rb = ocStr.indexOf(')', i);
                if (lb == -1 || rb == -1 || rb - lb < 3)
                {
                    throw new NetworkException("Speed in overtaking conditions string: '" + ocStr + "' not coded right");
                }
                Speed speed = SpeedUnits.parseSpeed(ocStr.substring(lb + 1, rb));
                return new OvertakingConditions.LeftSetRightSpeed(overtakingGTUs, overtakenGTUs, speed);
            }
            return new OvertakingConditions.LeftSet(overtakingGTUs, overtakenGTUs);
        }
        else if (ocStr.startsWith("RIGHTSET"))
        {
            int lset1 = ocStr.indexOf('[') + 1;
            int rset1 = ocStr.indexOf(']', lset1);
            int lset2 = ocStr.indexOf('[', ocStr.indexOf("OVERTAKE")) + 1;
            int rset2 = ocStr.indexOf(']', lset2);
            if (lset1 == -1 || rset1 == -1 || rset1 - lset1 < 3 || lset2 == -1 || rset2 == -1 || rset2 - lset2 < 3)
            {
                throw new NetworkException("Sets in overtaking conditions string: '" + ocStr + "' not coded right");
            }
            Set<GTUType> overtakingGTUs = parseGTUTypeSet(ocStr.substring(lset1, rset1));
            Set<GTUType> overtakenGTUs = parseGTUTypeSet(ocStr.substring(lset2, rset2));
            if (ocStr.contains("LEFTSPEED"))
            {
                int i = ocStr.indexOf("LEFTSPEED");
                int lb = ocStr.indexOf('(', i);
                int rb = ocStr.indexOf(')', i);
                if (lb == -1 || rb == -1 || rb - lb < 3)
                {
                    throw new NetworkException("Speed in overtaking conditions string: '" + ocStr + "' not coded right");
                }
                Speed speed = SpeedUnits.parseSpeed(ocStr.substring(lb + 1, rb));
                return new OvertakingConditions.RightSetLeftSpeed(overtakingGTUs, overtakenGTUs, speed);
            }
            return new OvertakingConditions.RightSet(overtakingGTUs, overtakenGTUs);
        }
        */
        throw new NetworkException("Unknown overtaking conditions string: " + ocStr);
    }

}
