package org.opentrafficsim.road.network.factory.xml.parser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.vector.LengthVector;
import org.djutils.draw.curve.Arc2d;
import org.djutils.draw.curve.Clothoid2d;
import org.djutils.draw.curve.OffsetCurve2d;
import org.djutils.draw.curve.Straight2d;
import org.djutils.draw.function.ContinuousPiecewiseLinearFunction;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Ray2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.eval.Eval;
import org.djutils.exceptions.Throw;
import org.djutils.math.AngleUtil;
import org.opentrafficsim.base.StripeElement;
import org.opentrafficsim.base.geometry.OtsGeometryUtil;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.core.compatibility.GtuCompatibleInfraType;
import org.opentrafficsim.core.definitions.Definitions;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.BezierCubic2d;
import org.opentrafficsim.core.geometry.CurveFlattener;
import org.opentrafficsim.core.geometry.PolyLineCurve2d;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Centroid;
import org.opentrafficsim.core.network.Connector;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.network.CrossSectionElement;
import org.opentrafficsim.road.network.CrossSectionGeometry;
import org.opentrafficsim.road.network.CrossSectionLink;
import org.opentrafficsim.road.network.CrossSectionLink.Priority;
import org.opentrafficsim.road.network.Lane;
import org.opentrafficsim.road.network.LaneKeepingPolicy;
import org.opentrafficsim.road.network.LaneType;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.Shoulder;
import org.opentrafficsim.road.network.Stripe;
import org.opentrafficsim.road.network.StripeData;
import org.opentrafficsim.road.network.StripeData.StripePhaseSync;
import org.opentrafficsim.road.network.conflict.ConflictBuilder;
import org.opentrafficsim.road.network.conflict.ConflictBuilder.FixedWidthGenerator;
import org.opentrafficsim.road.network.conflict.ConflictBuilder.RelativeWidthGenerator;
import org.opentrafficsim.road.network.conflict.ConflictBuilder.WidthGenerator;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.parser.DefinitionsParser.SpeedLimits;
import org.opentrafficsim.road.network.factory.xml.utils.ParseUtil;
import org.opentrafficsim.road.network.factory.xml.utils.RoadLayoutOffsets;
import org.opentrafficsim.road.network.factory.xml.utils.RoadLayoutOffsets.CseData;
import org.opentrafficsim.road.network.factory.xml.utils.StripeSynchronization;
import org.opentrafficsim.road.network.factory.xml.utils.StripeSynchronization.SynchronizableStripe;
import org.opentrafficsim.road.network.object.trafficlight.TrafficLight;
import org.opentrafficsim.road.network.speed.LaneSpeedLimits;
import org.opentrafficsim.xml.bindings.types.ArcDirectionType.ArcDirection;
import org.opentrafficsim.xml.bindings.types.StringType;
import org.opentrafficsim.xml.generated.Compatibility;
import org.opentrafficsim.xml.generated.CseShoulder;
import org.opentrafficsim.xml.generated.FlattenerType;
import org.opentrafficsim.xml.generated.GtuTypeSpeedLimit;
import org.opentrafficsim.xml.generated.LaneCustom;
import org.opentrafficsim.xml.generated.Link;
import org.opentrafficsim.xml.generated.Link.LaneOverride;
import org.opentrafficsim.xml.generated.Link.StripeOverride;
import org.opentrafficsim.xml.generated.Network;
import org.opentrafficsim.xml.generated.RoadLayouts;
import org.opentrafficsim.xml.generated.StripeCompatibility;
import org.opentrafficsim.xml.generated.StripeCustom;
import org.opentrafficsim.xml.generated.StripeElements.Continuous;
import org.opentrafficsim.xml.generated.StripeElements.Dashed;
import org.opentrafficsim.xml.generated.StripeElements.Dashed.GapDash;
import org.opentrafficsim.xml.generated.StripeElements.Gap;
import org.opentrafficsim.xml.generated.StripeType;
import org.opentrafficsim.xml.generated.TemporalSpeedLimit;
import org.opentrafficsim.xml.generated.TrafficLightType;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * NetworkParser parses the Network tag of the OTS network.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Wouter Schakel
 */
public final class NetworkParser
{
    /** Constructor. */
    private NetworkParser()
    {
        // utility class
    }

    /**
     * Parse the Nodes.
     * @param otsNetwork the network to insert the parsed objects in
     * @param network the Network tag
     * @param eval expression evaluator.
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     */
    public static void parseNodes(final RoadNetwork otsNetwork, final Network network, final Eval eval) throws NetworkException
    {
        for (org.opentrafficsim.xml.generated.Centroid xmlCentroid : network.getCentroid())
        {
            new Centroid(otsNetwork, xmlCentroid.getId(), xmlCentroid.getCoordinate().get(eval));
        }
        for (org.opentrafficsim.xml.generated.Node xmlNode : network.getNode())
        {
            new Node(otsNetwork, xmlNode.getId(), xmlNode.getCoordinate().get(eval), xmlNode.getDirection().get(eval));
        }
    }

    /**
     * Build the links with the correct design line.
     * @param otsNetwork the network to insert the parsed objects in
     * @param definitions parsed definitions.
     * @param network the Network tag
     * @param simulator the simulator
     * @param designLines map to store created design lines.
     * @param flatteners flattener per link id.
     * @param eval expression evaluator.
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     */
    static void parseLinks(final RoadNetwork otsNetwork, final Definitions definitions, final Network network,
            final OtsSimulatorInterface simulator, final Map<String, OffsetCurve2d> designLines,
            final Map<String, CurveFlattener> flatteners, final Eval eval) throws NetworkException
    {
        for (org.opentrafficsim.xml.generated.Connector xmlConnector : network.getConnector())
        {
            String nodeId = xmlConnector.getNode().get(eval);
            Node node = getNode(otsNetwork, nodeId);
            String centroidId = xmlConnector.getCentroid().get(eval);
            Node centroid = getNode(otsNetwork, centroidId);
            String id = xmlConnector.getId();
            double demandWeight = xmlConnector.getDemandWeight().get(eval);
            LinkType linkType = definitions.getOrThrow(LinkType.class, xmlConnector.getType().get(eval));
            Connector link = xmlConnector.getOutbound().get(eval) ? new Connector(otsNetwork, id, centroid, node, linkType)
                    : new Connector(otsNetwork, id, node, centroid, linkType);
            link.setDemandWeight(demandWeight);
        }

        CurveFlattener defaultFlattener =
                network.getFlattener() == null ? new CurveFlattener(64) : getFlattener(null, network.getFlattener(), eval);
        for (Link xmlLink : network.getLink())
        {
            Node startNode = getNode(otsNetwork, xmlLink.getNodeStart().get(eval));
            Node endNode = getNode(otsNetwork, xmlLink.getNodeEnd().get(eval));
            Point2d startPoint = startNode.getPoint();
            Point2d endPoint = endNode.getPoint();
            Point2d[] coordinates = null;

            // start and end with heading, adjusted with offset
            double startHeading = startNode.getHeading().si;
            DirectedPoint2d start = new DirectedPoint2d(startPoint.x, startPoint.y, startHeading);
            if (xmlLink.getOffsetStart() != null)
            {
                start = OtsGeometryUtil.offsetPoint(start, xmlLink.getOffsetStart().get(eval).si);
            }
            double endHeading = endNode.getHeading().si;
            DirectedPoint2d end = new DirectedPoint2d(endPoint.x, endPoint.y, endHeading);
            if (xmlLink.getOffsetEnd() != null)
            {
                end = OtsGeometryUtil.offsetPoint(end, xmlLink.getOffsetEnd().get(eval).si);
            }

            OffsetCurve2d designLine;
            CurveFlattener flattener = defaultFlattener;
            if (xmlLink.getStraight() != null)
            {
                designLine = new Straight2d(start, Math.hypot(end.x - start.x, end.y - start.y));
            }
            else if (xmlLink.getPolyLine() != null)
            {
                int intermediatePoints = xmlLink.getPolyLine().getCoordinate().size();
                coordinates = new Point2d[intermediatePoints + 2];
                coordinates[0] = startPoint;
                coordinates[intermediatePoints + 1] = endPoint;
                for (int p = 0; p < intermediatePoints; p++)
                {
                    coordinates[p + 1] = xmlLink.getPolyLine().getCoordinate().get(p).get(eval);
                }
                designLine = new PolyLineCurve2d(new PolyLine2d(0.0, coordinates), startHeading, endHeading);
            }
            else if (xmlLink.getArc() != null)
            {
                flattener = getFlattener(defaultFlattener, xmlLink.getArc().getFlattener(), eval);
                double radius = xmlLink.getArc().getRadius().get(eval).si;
                boolean left = xmlLink.getArc().getDirection().get(eval).equals(ArcDirection.LEFT);
                while (left && endHeading < startHeading)
                {
                    endHeading += 2.0 * Math.PI;
                }
                while (!left && endHeading > startHeading)
                {
                    endHeading -= 2.0 * Math.PI;
                }

                designLine = new Arc2d(start, radius, left,
                        AngleUtil.normalizeAroundPi(left ? endHeading - startHeading : startHeading - endHeading));
            }
            else if (xmlLink.getBezier() != null)
            {
                flattener = getFlattener(defaultFlattener, xmlLink.getBezier().getFlattener(), eval);
                double shape = xmlLink.getBezier().getShape().get(eval);
                boolean weighted = xmlLink.getBezier().getWeighted().get(eval);
                designLine = new BezierCubic2d(new Ray2d(start), new Ray2d(end), shape, weighted);
            }
            else if (xmlLink.getClothoid() != null)
            {
                FlattenerType flattenerType =
                        xmlLink.getClothoid().getFlattener().isEmpty() ? null : xmlLink.getClothoid().getFlattener().get(0);
                flattener = getFlattener(defaultFlattener, flattenerType, eval);
                // fields in getClothoid() appear as lists as StartCurvature and EndCurvature appear in multiple options
                if (!xmlLink.getClothoid().getInterpolated().isEmpty())
                {
                    designLine = new Clothoid2d(start, end);
                }
                else
                {
                    LinearDensity startCurvature = xmlLink.getClothoid().getStartCurvature().get(0).get(eval);
                    LinearDensity endCurvature = xmlLink.getClothoid().getEndCurvature().get(0).get(eval);
                    if (!xmlLink.getClothoid().getLength().isEmpty())
                    {
                        Length length = xmlLink.getClothoid().getLength().get(0).get(eval);
                        designLine = Clothoid2d.withLength(start, length.si, startCurvature.si, endCurvature.si);
                    }
                    else
                    {
                        Throw.when(xmlLink.getClothoid().getA().isEmpty(), NetworkException.class,
                                "Clothoid for link %s is not correctly specified.", xmlLink.getId());
                        Length a = xmlLink.getClothoid().getA().get(0).get(eval);
                        designLine = new Clothoid2d(start, a.si, startCurvature.si, endCurvature.si);
                    }
                }
            }

            else
            {
                throw new NetworkException("Making link, but link " + xmlLink.getId()
                        + " has no filled straight, arc, bezier, polyline, or clothoid definition");
            }
            designLines.put(xmlLink.getId(), designLine);
            flatteners.put(xmlLink.getId(), flattener);

            PolyLine2d flattenedLine = designLine.toPolyLine(flattener);
            LaneKeepingPolicy laneKeepingPolicy = xmlLink.getLaneKeeping().get(eval);
            LinkType linkType = definitions.getOrThrow(LinkType.class, xmlLink.getType().get(eval));
            // TODO: elevation data
            CrossSectionLink link = new CrossSectionLink(otsNetwork, xmlLink.getId(), startNode, endNode, linkType,
                    new OtsLine2d(flattenedLine), null, laneKeepingPolicy);

            if (xmlLink.getPriority() != null)
            {
                Priority priority = xmlLink.getPriority().get(eval);
                link.setPriority(priority);
            }
        }
    }

    /**
     * Build the links with the correct design line.
     * @param otsNetwork the network to insert the parsed objects in
     * @param definitions parsed definitions.
     * @param network the Network tag
     * @param roadLayoutMap the map of the tags of the predefined RoadLayout tags in Definitions
     * @param infraSpeedLimitMap map of speed limits per link type
     * @param designLines design lines per link id.
     * @param stripes defined stripes
     * @param flatteners flattener per link id.
     * @param eval expression evaluator.
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws XmlParserException when the stripe type cannot be recognized
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws GtuException when construction of the Strategical Planner failed
     */
    @SuppressWarnings("checkstyle:methodlength")
    static void applyRoadLayouts(final RoadNetwork otsNetwork, final Definitions definitions, final Network network,
            final Map<String, RoadLayouts.RoadLayout> roadLayoutMap,
            final Map<GtuCompatibleInfraType<?, ?>, SpeedLimits> infraSpeedLimitMap,
            final Map<String, OffsetCurve2d> designLines, final Map<String, CurveFlattener> flatteners,
            final Map<String, StripeType> stripes, final Eval eval)
            throws NetworkException, XmlParserException, SimRuntimeException, GtuException
    {
        Map<GtuType, Speed> networkGtuTypeSpeedLimits = new LinkedHashMap<GtuType, Speed>();
        if (network.getGtuTypeSpeedLimits() != null)
        {
            for (GtuTypeSpeedLimit gtuTypeSpeedLimit : network.getGtuTypeSpeedLimits().getGtuTypeSpeedLimit())
            {
                GtuType gtuType = definitions.getOrThrow(GtuType.class, gtuTypeSpeedLimit.getId().get(eval));
                Speed speed = gtuTypeSpeedLimit.getValue().get(eval);
                networkGtuTypeSpeedLimits.put(gtuType, speed);
            }
        }
        SpeedLimits networkSpeedLimits = new SpeedLimits(null, null, networkGtuTypeSpeedLimits);

        // Virtual LaneTypes mirror the LinkType hierarchy and are shared by lanes on links with the same LinkType.
        Map<String, LaneType> virtualLaneTypes = new LinkedHashMap<>();

        Map<Stripe, SynchronizableStripe<Stripe>> stripesSync = new LinkedHashMap<>();
        for (Link xmlLink : network.getLink())
        {
            CrossSectionLink csl = getLink(otsNetwork, xmlLink.getId());
            List<CrossSectionElement> cseList = new ArrayList<>();
            Map<String, Lane> lanes = new LinkedHashMap<>();
            Map<Link.RoadLayout.Lane, LaneType> laneTypes = new LinkedHashMap<>();
            Map<Link.RoadLayout.Lane, SpeedLimits> overruleSpeedLimits = new LinkedHashMap<>();

            // Get the RoadLayout (either defined here, or via pointer to Definitions)
            Link.RoadLayout roadLayoutTag;
            if (xmlLink.getDefinedLayout() != null)
            {
                Throw.when(xmlLink.getRoadLayout() != null, XmlParserException.class,
                        "Link %s Ambiguous RoadLayout; both DefinedRoadLayout and RoadLayout defined", xmlLink.getId());
                String definedLayoutId = xmlLink.getDefinedLayout().get(eval);
                RoadLayouts.RoadLayout roadLayoutTagBase = roadLayoutMap.get(definedLayoutId);
                Throw.when(roadLayoutTagBase == null, XmlParserException.class, "Link %s Could not find defined RoadLayout %s",
                        xmlLink.getId(), definedLayoutId);
                roadLayoutTag = convertDefinitionLayout(roadLayoutTagBase);
                setAllStripesCustom(roadLayoutTag, stripes, eval);
                applyStripeOverrides(xmlLink, roadLayoutTag, stripes, eval);
            }
            else
            {
                roadLayoutTag = xmlLink.getRoadLayout();
                if (roadLayoutTag == null)
                {
                    throw new XmlParserException("Link " + xmlLink.getId() + " No RoadLayout defined");
                }
                setAllStripesCustom(roadLayoutTag, stripes, eval);
            }

            // Resolve the LaneType for every lane. Lanes without a defined type derive their type from the LinkType.
            for (Link.RoadLayout.Lane laneTag : ParseUtil
                    .getObjectsOfType(roadLayoutTag.getStripeOrLaneOrShoulder(), Link.RoadLayout.Lane.class))
            {
                applyLaneChoice(laneTag, csl.getType(), definitions, eval, laneTypes, overruleSpeedLimits, virtualLaneTypes);
            }
            if (xmlLink.getDefinedLayout() != null)
            {
                applyLaneOverrides(xmlLink, csl.getType(), definitions, eval, roadLayoutTag, laneTypes, overruleSpeedLimits,
                        virtualLaneTypes);
            }

            // calculate for each lane and stripe what the start and end offset is
            List<CseData> cseDataList = new ArrayList<>();
            Map<Object, Integer> cseTagMap = new LinkedHashMap<>();
            RoadLayoutOffsets.calculateOffsets(roadLayoutTag, cseDataList, cseTagMap, eval);

            // Stripe
            OffsetCurve2d designLine = designLines.get(xmlLink.getId());
            CurveFlattener flattener = flatteners.get(xmlLink.getId());
            for (Link.RoadLayout.Stripe stripeTag : ParseUtil
                    .getObjectsOfType(roadLayoutTag.getStripeOrLaneOrShoulder(), Link.RoadLayout.Stripe.class))
            {
                CseData cseData = cseDataList.get(cseTagMap.get(stripeTag));
                makeStripe(csl, designLine, flattener, cseData.centerOffsetStart, cseData.centerOffsetEnd, stripeTag, cseList,
                        stripesSync, definitions, eval);
            }

            // Other CrossSectionElement
            for (org.opentrafficsim.xml.generated.CrossSectionElement cseTag : ParseUtil.getObjectsOfType(
                    roadLayoutTag.getStripeOrLaneOrShoulder(), org.opentrafficsim.xml.generated.CrossSectionElement.class))
            {
                CseData cseData = cseDataList.get(cseTagMap.get(cseTag));

                ContinuousPiecewiseLinearFunction offset = ContinuousPiecewiseLinearFunction.of(0.0,
                        cseData.centerOffsetStart.si, 1.0, cseData.centerOffsetEnd.si);
                ContinuousPiecewiseLinearFunction width =
                        ContinuousPiecewiseLinearFunction.of(0.0, cseData.widthStart.si, 1.0, cseData.widthEnd.si);
                CrossSectionGeometry geometry = CrossSectionGeometry.of(designLine, flattener, offset, width);

                // Lane
                if (cseTag instanceof Link.RoadLayout.Lane laneTag)
                {
                    LaneType laneType = laneTypes.get(laneTag);
                    Throw.when(laneType == null, XmlParserException.class, "Link %s Lane %s has no LaneType", xmlLink.getId(),
                            laneTag.getId());

                    // collect overruled speed limits
                    LinkType linkType = csl.getType();
                    SpeedLimits speedLimits = networkSpeedLimits;
                    if (infraSpeedLimitMap.containsKey(linkType))
                    {
                        speedLimits = speedLimits.overrule(infraSpeedLimitMap.get(linkType));
                    }
                    if (infraSpeedLimitMap.containsKey(laneType))
                    {
                        speedLimits = speedLimits.overrule(infraSpeedLimitMap.get(laneType));
                    }
                    if (overruleSpeedLimits.containsKey(laneTag))
                    {
                        speedLimits = speedLimits.overrule(overruleSpeedLimits.get(laneTag));
                    }
                    // write speed limits in object for lane
                    LaneSpeedLimits laneSpeedLimits = speedLimits.gtuTypeSpeedLimits() == null ? new LaneSpeedLimits()
                            : new LaneSpeedLimits(speedLimits.gtuTypeSpeedLimits());
                    if (speedLimits.speedLimit() != null)
                    {
                        Speed speed = speedLimits.speedLimit().getValue().get(eval);
                        boolean enforced = speedLimits.speedLimit().getEnforced().get(eval);
                        boolean gtuTypeAware = speedLimits.speedLimit().getGtuTypeAware().get(eval);
                        laneSpeedLimits.addSpeedLimit(speed, enforced, gtuTypeAware);
                    }
                    else if (speedLimits.temporalSpeedLimit() != null)
                    {
                        for (TemporalSpeedLimit.SpeedLimit speedLimit : speedLimits.temporalSpeedLimit().getSpeedLimit())
                        {
                            Duration timeOfDay = Duration.ofSI(speedLimit.getStartTimeOfDay().get(eval).si % 86400.0);
                            Speed speed = speedLimit.getValue().get(eval);
                            boolean enforced = speedLimit.getEnforced().get(eval);
                            boolean gtuTypeAware = speedLimit.getGtuTypeAware().get(eval);
                            laneSpeedLimits.addSpeedLimit(timeOfDay, speed, enforced, gtuTypeAware);
                        }
                    }

                    Lane lane = new Lane(csl, laneTag.getId(), geometry, laneType, laneSpeedLimits);
                    cseList.add(lane);
                    lanes.put(lane.getId(), lane);
                }

                // Shoulder
                else if (cseTag instanceof CseShoulder shoulderTag)
                {
                    LaneType laneType = SHOULDER;
                    String id = shoulderTag.getId() != null ? shoulderTag.getId() : UUID.randomUUID().toString();
                    CrossSectionElement shoulder = new Shoulder(csl, id, geometry, laneType);
                    cseList.add(shoulder);
                }
            }

            // TrafficLight
            for (TrafficLightType trafficLight : xmlLink.getTrafficLight())
            {
                String laneId = trafficLight.getLane().get(eval);
                Throw.when(!lanes.containsKey(laneId), NetworkException.class,
                        "Link: %s, TrafficLight with id %s on Lane %s - Lane not found", xmlLink.getId(), trafficLight.getId(),
                        laneId);
                Lane lane = lanes.get(laneId);
                Length position = ParseUtil.parseLengthBeginEnd(trafficLight.getPosition().get(eval), lane.getLength());
                TrafficLight obj = new TrafficLight(trafficLight.getId(), lane, position);
                for (StringType nodeId : trafficLight.getTurnOnRed())
                {
                    obj.addTurnOnRed(getNode(otsNetwork, nodeId.get(eval)));
                }
            }
        }
        StripeSynchronization.synchronize(stripesSync);
    }

    /**
     * Converts a lane/stripe layout from Definitions into the equivalent network layout tags. Definition lanes deliberately do
     * not carry a LaneType; those are resolved from the LinkType of the link that uses the layout.
     * @param layoutTag defined road layout
     * @return network road layout
     */
    private static Link.RoadLayout convertDefinitionLayout(final RoadLayouts.RoadLayout layoutTag)
    {
        Link.RoadLayout layout = new Link.RoadLayout();
        layout.setLaneKeeping(layoutTag.getLaneKeeping());
        for (Serializable serializable : layoutTag.getStripeOrLaneOrShoulder())
        {
            if (serializable instanceof RoadLayouts.RoadLayout.Stripe stripeTag)
            {
                Link.RoadLayout.Stripe stripe = new Link.RoadLayout.Stripe();
                stripe.setCenterOffset(stripeTag.getCenterOffset());
                stripe.setCenterOffsetStart(stripeTag.getCenterOffsetStart());
                stripe.setCenterOffsetEnd(stripeTag.getCenterOffsetEnd());
                stripe.setId(stripeTag.getId());
                stripe.setDefinedStripe(stripeTag.getStripeType());
                layout.getStripeOrLaneOrShoulder().add(stripe);
            }
            else if (serializable instanceof RoadLayouts.RoadLayout.Lane laneTag)
            {
                Link.RoadLayout.Lane lane = new Link.RoadLayout.Lane();
                copyCrossSectionElement(laneTag, lane);
                lane.setId(laneTag.getId());
                layout.getStripeOrLaneOrShoulder().add(lane);
            }
            else if (serializable instanceof CseShoulder shoulderTag)
            {
                CseShoulder shoulder = new CseShoulder();
                copyCrossSectionElement(shoulderTag, shoulder);
                shoulder.setId(shoulderTag.getId());
                layout.getStripeOrLaneOrShoulder().add(shoulder);
            }
        }
        return layout;
    }

    /**
     * Copies the offset and width attributes of a cross-section element.
     * @param source source tag
     * @param target target tag
     */
    private static void copyCrossSectionElement(final org.opentrafficsim.xml.generated.CrossSectionElement source,
            final org.opentrafficsim.xml.generated.CrossSectionElement target)
    {
        target.setCenterOffset(source.getCenterOffset());
        target.setLeftOffset(source.getLeftOffset());
        target.setRightOffset(source.getRightOffset());
        target.setCenterOffsetStart(source.getCenterOffsetStart());
        target.setLeftOffsetStart(source.getLeftOffsetStart());
        target.setRightOffsetStart(source.getRightOffsetStart());
        target.setCenterOffsetEnd(source.getCenterOffsetEnd());
        target.setLeftOffsetEnd(source.getLeftOffsetEnd());
        target.setRightOffsetEnd(source.getRightOffsetEnd());
        target.setWidth(source.getWidth());
        target.setWidthStart(source.getWidthStart());
        target.setWidthEnd(source.getWidthEnd());
    }

    /**
     * Resolves the LaneType for a lane in a network road layout.
     * @param laneTag lane tag
     * @param linkType LinkType of the link
     * @param definitions parsed definitions
     * @param eval expression evaluator
     * @param laneTypes resolved LaneTypes, modified
     * @param overruleSpeedLimits speed limits from lane customizations, modified
     * @param virtualLaneTypes cache of virtual LaneTypes that mirror the LinkType hierarchy, modified
     * @throws XmlParserException when a referred definition cannot be found
     */
    private static void applyLaneChoice(final Link.RoadLayout.Lane laneTag, final LinkType linkType,
            final Definitions definitions, final Eval eval, final Map<Link.RoadLayout.Lane, LaneType> laneTypes,
            final Map<Link.RoadLayout.Lane, SpeedLimits> overruleSpeedLimits, final Map<String, LaneType> virtualLaneTypes)
            throws XmlParserException
    {
        if (laneTag.getDefinedLane() != null)
        {
            laneTypes.put(laneTag, definitions.getOrThrow(LaneType.class, laneTag.getDefinedLane().get(eval)));
        }
        else
        {
            LaneType laneType = deriveLaneType(linkType, laneTag.getId(), definitions, virtualLaneTypes);
            if (laneTag.getCustom() != null)
            {
                applyLaneCustom(laneType, laneTag.getCustom(), laneTag, definitions, eval, overruleSpeedLimits);
            }
            laneTypes.put(laneTag, laneType);
        }
    }

    /**
     * Applies LaneOverrides to a converted defined road layout.
     * @param xmlLink link tag
     * @param linkType LinkType of the link
     * @param definitions parsed definitions
     * @param eval expression evaluator
     * @param roadLayoutTag converted road layout
     * @param laneTypes resolved LaneTypes, modified
     * @param overruleSpeedLimits speed limits from lane overrides, modified
     * @param virtualLaneTypes cache of virtual LaneTypes that mirror the LinkType hierarchy, modified
     * @throws XmlParserException when a referred definition cannot be found
     */
    private static void applyLaneOverrides(final Link xmlLink, final LinkType linkType, final Definitions definitions,
            final Eval eval, final Link.RoadLayout roadLayoutTag, final Map<Link.RoadLayout.Lane, LaneType> laneTypes,
            final Map<Link.RoadLayout.Lane, SpeedLimits> overruleSpeedLimits, final Map<String, LaneType> virtualLaneTypes)
            throws XmlParserException
    {
        for (LaneOverride laneOverride : xmlLink.getLaneOverride())
        {
            String laneId = laneOverride.getLane().get(eval);
            for (Link.RoadLayout.Lane lane : ParseUtil
                    .getObjectsOfType(roadLayoutTag.getStripeOrLaneOrShoulder(), Link.RoadLayout.Lane.class))
            {
                if (!lane.getId().equals(laneId))
                {
                    continue;
                }
                if (laneOverride.getDefinedLane() != null)
                {
                    laneTypes.put(lane, definitions.getOrThrow(LaneType.class, laneOverride.getDefinedLane().get(eval)));
                }
                else if (laneOverride.getCustom() != null)
                {
                    LaneType laneType = deriveLaneType(linkType, lane.getId(), definitions, virtualLaneTypes);
                    applyLaneCustom(laneType, laneOverride.getCustom(), lane, definitions, eval, overruleSpeedLimits);
                    laneTypes.put(lane, laneType);
                }
                else
                {
                    throw new XmlParserException("Link " + xmlLink.getId() + " LaneOverride " + laneId + " has no lane type");
                }
            }
        }
    }

    /**
     * Creates a simulation LaneType for a specific lane. Its parent is a cached virtual LaneType that mirrors the LinkType
     * hierarchy, so compatibility declared on any LinkType ancestor is retained. The lane type is not added to the Definitions
     * as it only applies to this specific lane.
     * @param linkType LinkType of the link
     * @param laneId lane id, used as id of the LaneType
     * @param definitions parsed definitions, used to obtain all GtuTypes
     * @param virtualLaneTypes cache of virtual LaneTypes that mirror the LinkType hierarchy, modified
     * @return generated LaneType
     */
    private static LaneType deriveLaneType(final LinkType linkType, final String laneId, final Definitions definitions,
            final Map<String, LaneType> virtualLaneTypes)
    {
        return new LaneType(laneId, createVirtualLaneType(linkType, definitions, virtualLaneTypes));
    }

    /**
     * Creates, or reuses, the virtual LaneType that mirrors a LinkType. The virtual LaneType has the same Id as the LinkType
     * and is cached by that Id. Its parent is the virtual LaneType of the LinkType parent, if any, which is created
     * recursively. Only compatibility declared directly on the LinkType is copied; compatibility inherited from ancestors
     * follows from the parent hierarchy.
     * @param linkType LinkType to mirror
     * @param definitions parsed definitions, used to obtain all GtuTypes
     * @param virtualLaneTypes cache of virtual LaneTypes that mirror the LinkType hierarchy, modified
     * @return virtual LaneType mirroring the LinkType
     */
    private static LaneType createVirtualLaneType(final LinkType linkType, final Definitions definitions,
            final Map<String, LaneType> virtualLaneTypes)
    {
        LaneType cached = virtualLaneTypes.get(linkType.getId());
        if (cached != null)
        {
            return cached;
        }
        LaneType parent = linkType.getParent()
                .map((parentLinkType) -> createVirtualLaneType(parentLinkType, definitions, virtualLaneTypes)).orElse(null);
        LaneType virtualLaneType = new LaneType(linkType.getId(), parent);
        for (GtuType gtuType : definitions.getAll(GtuType.class).values())
        {
            Boolean compatible = linkType.isCompatibleOnInfraLevel(gtuType);
            if (compatible != null)
            {
                if (compatible)
                {
                    virtualLaneType.addCompatibleGtuType(gtuType);
                }
                else
                {
                    virtualLaneType.addIncompatibleGtuType(gtuType);
                }
            }
        }
        virtualLaneTypes.put(linkType.getId(), virtualLaneType);
        return virtualLaneType;
    }

    /**
     * Applies compatibility and speed limits from a Custom lane tag to a generated LaneType.
     * @param laneType generated LaneType, modified
     * @param custom Custom lane tag
     * @param laneTag lane that owns the Custom tag
     * @param definitions parsed definitions
     * @param eval expression evaluator
     * @param overruleSpeedLimits speed limits, modified
     * @throws XmlParserException when a referred definition cannot be found
     */
    private static void applyLaneCustom(final LaneType laneType, final LaneCustom custom, final Link.RoadLayout.Lane laneTag,
            final Definitions definitions, final Eval eval, final Map<Link.RoadLayout.Lane, SpeedLimits> overruleSpeedLimits)
            throws XmlParserException
    {
        for (Compatibility compatibility : custom.getCompatibility())
        {
            GtuType gtuType = definitions.getOrThrow(GtuType.class, compatibility.getGtuType().get(eval));
            if (compatibility.getCompatible().get(eval))
            {
                laneType.addCompatibleGtuType(gtuType);
            }
            else
            {
                laneType.addIncompatibleGtuType(gtuType);
            }
        }
        Map<GtuType, Speed> gtuTypeSpeedLimits = new LinkedHashMap<>();
        for (GtuTypeSpeedLimit gtuTypeSpeedLimit : custom.getGtuTypeSpeedLimit())
        {
            GtuType gtuType = definitions.getOrThrow(GtuType.class, gtuTypeSpeedLimit.getId().get(eval));
            gtuTypeSpeedLimits.put(gtuType, gtuTypeSpeedLimit.getValue().get(eval));
        }
        overruleSpeedLimits.put(laneTag,
                new SpeedLimits(custom.getSpeedLimit(), custom.getTemporalSpeedLimit(), gtuTypeSpeedLimits));
    }

    /**
     * Applies StripeOverrides to a road layout whose stripes have already been made custom.
     * @param xmlLink link tag
     * @param roadLayoutTag road layout
     * @param stripes defined stripes
     * @param eval expression evaluator
     * @throws XmlParserException when a defined stripe cannot be found
     */
    private static void applyStripeOverrides(final Link xmlLink, final Link.RoadLayout roadLayoutTag,
            final Map<String, StripeType> stripes, final Eval eval) throws XmlParserException
    {
        for (StripeOverride stripeOverride : xmlLink.getStripeOverride())
        {
            String stripeId = stripeOverride.getStripe().get(eval);
            for (Link.RoadLayout.Stripe stripe : ParseUtil
                    .getObjectsOfType(roadLayoutTag.getStripeOrLaneOrShoulder(), Link.RoadLayout.Stripe.class))
            {
                if (!stripe.getId().equals(stripeId))
                {
                    continue;
                }
                if (stripeOverride.getDefinedStripe() != null)
                {
                    String stripeTypeId = stripeOverride.getDefinedStripe().get(eval);
                    StripeType stripeType = stripes.get(stripeTypeId);
                    Throw.when(stripeType == null, XmlParserException.class,
                            "Link %s Stripe %s could not find defined StripeType %s", xmlLink.getId(), stripeId, stripeTypeId);
                    setDefinedStripe(stripe, stripeType);
                }
                else if (stripeOverride.getCustom() != null)
                {
                    overlayStripeCustom(stripe.getCustom(), stripeOverride.getCustom());
                }
                else
                {
                    throw new XmlParserException(
                            "Link " + xmlLink.getId() + " StripeOverride " + stripeId + " has no stripe type");
                }
            }
        }
    }

    /**
     * Sets all defined stripes in the road layout as custom, based on the referred stripe type.
     * @param roadLayoutTag road layout tag
     * @param stripes defined stripes
     * @param eval expression evaluator
     * @throws XmlParserException when a defined stripe cannot be found
     */
    private static void setAllStripesCustom(final Link.RoadLayout roadLayoutTag, final Map<String, StripeType> stripes,
            final Eval eval) throws XmlParserException
    {
        for (Link.RoadLayout.Stripe stripe : ParseUtil
                .getObjectsOfType(roadLayoutTag.getStripeOrLaneOrShoulder(), Link.RoadLayout.Stripe.class))
        {
            if (stripe.getDefinedStripe() != null)
            {
                String stripeTypeId = stripe.getDefinedStripe().get(eval);
                StripeType stripeType = stripes.get(stripeTypeId);
                Throw.when(stripeType == null, XmlParserException.class, "Stripe %s could not find defined StripeType %s",
                        stripe.getId(), stripeTypeId);
                setDefinedStripe(stripe, stripeType);
            }
        }
    }

    /**
     * Set defined stripe type in stripe tag.
     * @param stripe stripe tag
     * @param stripeType defined stripe type
     */
    private static void setDefinedStripe(final Link.RoadLayout.Stripe stripe, final StripeType stripeType)
    {
        StripeCustom custom = new StripeCustom();
        stripe.setCustom(custom);
        stripe.setDefinedStripe(null);
        custom.setElements(stripeType.getElements());
        custom.setDashOffset(stripeType.getDashOffset());
        custom.setLateralSync(stripeType.getLateralSync());
        custom.setLeftChangeLane(stripeType.getLeftChangeLane());
        custom.setRightChangeLane(stripeType.getRightChangeLane());
        custom.setBlock(stripeType.getBlock());
    }

    /**
     * Overlays a partial custom stripe definition on an existing custom stripe.
     * @param target existing custom stripe, modified
     * @param source partial custom stripe
     */
    private static void overlayStripeCustom(final StripeCustom target, final StripeCustom source)
    {
        if (source.getElements() != null)
        {
            target.setElements(source.getElements());
        }
        if (source.getDashOffset() != null)
        {
            target.setDashOffset(source.getDashOffset());
        }
        if (source.getLateralSync() != null)
        {
            target.setLateralSync(source.getLateralSync());
        }
        if (source.getLeftChangeLane() != null)
        {
            target.setLeftChangeLane(source.getLeftChangeLane());
        }
        if (source.getRightChangeLane() != null)
        {
            target.setRightChangeLane(source.getRightChangeLane());
        }
        if (source.getBlock() != null)
        {
            target.setBlock(source.getBlock());
        }
        target.getCompatibility().addAll(source.getCompatibility());
    }

    /** Temporary fix for CseShoulder.getLaneType() always being null. */
    // FIXME
    private static final LaneType SHOULDER = new LaneType("Shoulder");

    /**
     * Parse a stripe on a road.
     * @param csl the CrossSectionLine
     * @param designLine design line.
     * @param flattener flattener.
     * @param startOffset the offset of the start node
     * @param endOffset the offset of the end node
     * @param stripeTag the Link.RoadLayout.Stripe tag in the XML file
     * @param cseList the list of CrossSectionElements to which the stripes should be added
     * @param stripesSync stripes
     * @param definitions definitions
     * @param eval expression evaluator.
     * @throws NetworkException when id of the stripe not unique
     * @throws XmlParserException when the stripe type cannot be recognized
     */
    private static void makeStripe(final CrossSectionLink csl, final OffsetCurve2d designLine, final CurveFlattener flattener,
            final Length startOffset, final Length endOffset, final Link.RoadLayout.Stripe stripeTag,
            final List<CrossSectionElement> cseList, final Map<Stripe, SynchronizableStripe<Stripe>> stripesSync,
            final Definitions definitions, final Eval eval) throws NetworkException, XmlParserException
    {
        StripeCustom custom = stripeTag.getCustom();
        Throw.when(
                custom == null || custom.getElements() == null || custom.getElements().getContinuousOrDashedOrGap().isEmpty(),
                XmlParserException.class, "Stripe %s has no elements", stripeTag.getId());
        Length width = Length.ZERO;
        List<StripeElement> elements = new ArrayList<>();
        for (Serializable serializable : custom.getElements().getContinuousOrDashedOrGap())
        {
            if (serializable instanceof Gap gap)
            {
                Length w = gap.getWidth().get(eval);
                width = width.plus(w);
                elements.add(StripeElement.gap(w));
            }
            else if (serializable instanceof Continuous line)
            {
                Length w = line.getWidth().get(eval);
                width = width.plus(w);
                elements.add(StripeElement.continuous(w, line.getColor().get(eval)));
            }
            else if (serializable instanceof Dashed dashed)
            {
                Length w = dashed.getWidth().get(eval);
                width = width.plus(w);
                elements.add(StripeElement.dashed(w, dashed.getColor().get(eval), getDashes(dashed, eval)));
            }
        }
        ContinuousPiecewiseLinearFunction offsetFunc =
                ContinuousPiecewiseLinearFunction.of(0.0, startOffset.si, 1.0, endOffset.si);
        ContinuousPiecewiseLinearFunction widthFunc = ContinuousPiecewiseLinearFunction.of(0.0, width.si, 1.0, width.si);

        boolean leftLaneChange = false;
        boolean rightLaneChange = false;
        boolean block = false;
        if (custom.getLeftChangeLane() != null)
        {
            leftLaneChange = custom.getLeftChangeLane().get(eval);
        }
        if (custom.getRightChangeLane() != null)
        {
            rightLaneChange = custom.getRightChangeLane().get(eval);
        }
        if (custom.getBlock() != null)
        {
            block = custom.getBlock().get(eval);
        }
        StripeData stripeData = new StripeData(elements, leftLaneChange, rightLaneChange, block);
        Stripe stripe = new Stripe(stripeTag.getId(), stripeData, csl,
                CrossSectionGeometry.of(designLine, flattener, offsetFunc, widthFunc));

        if (custom.getDashOffset() != null)
        {
            if (custom.getDashOffset().getFixed() != null)
            {
                stripe.setDashOffset(custom.getDashOffset().getFixed().getOffset().get(eval));
                stripe.setPhaseSync(StripePhaseSync.NONE);
            }
            else if (custom.getDashOffset().getSyncUpstream() != null)
            {
                stripe.setPhaseSync(StripePhaseSync.UPSTREAM);
            }
            else if (custom.getDashOffset().getSyncDownstream() != null)
            {
                stripe.setPhaseSync(StripePhaseSync.DOWNSTREAM);
            }
        }

        stripesSync.put(stripe, StripeSynchronization.of(stripe));

        if (custom.getLateralSync() != null)
        {
            stripe.setLateralSync(custom.getLateralSync().get(eval));
        }

        for (StripeCompatibility compatibility : custom.getCompatibility())
        {
            String dir = compatibility.getDirection().get(eval);
            GtuType gtuType = definitions.getOrThrow(GtuType.class, compatibility.getGtuType().get(eval));
            if ("LEFT".equals(dir) || "BOTH".equals(dir))
            {
                stripe.addPermeability(gtuType, LateralDirectionality.LEFT);
            }
            if ("RIGHT".equals(dir) || "BOTH".equals(dir))
            {
                stripe.addPermeability(gtuType, LateralDirectionality.RIGHT);
            }
            else if ("NONE".equals(dir))
            {
                stripe.addPermeability(gtuType, LateralDirectionality.NONE);
            }
        }

        cseList.add(stripe);
    }

    /**
     * Collects series of gaps and dashes in to a length vector.
     * @param dashed dashed
     * @param eval evaluator
     * @return length vector of gaps and dashes
     */
    private static LengthVector getDashes(final Dashed dashed, final Eval eval)
    {
        List<Double> dashes = new ArrayList<>();
        for (GapDash gapDash : dashed.getGapDash())
        {
            dashes.add(gapDash.getGap().get(eval).si);
            dashes.add(gapDash.getDash().get(eval).si);
        }
        return new LengthVector(dashes.stream().mapToDouble(v -> v).toArray());
    }

    /**
     * Build conflicts.
     * @param otsNetwork the network to insert the parsed objects in
     * @param network the Network tag
     * @param eval expression evaluator.
     * @throws XmlParserException if Conflicts tag contains no valid element
     * @throws NetworkException if link cannot be found
     */
    static void buildConflicts(final RoadNetwork otsNetwork, final Network network, final Eval eval)
            throws XmlParserException, NetworkException
    {

        WidthGenerator widthGenerator;
        if (network.getConflicts() == null || network.getConflicts().getDefaultWidth() != null)
        {
            widthGenerator = new FixedWidthGenerator(Length.ofSI(2.0));
        }
        else if (network.getConflicts().getNone() != null)
        {
            return;
        }
        else if (network.getConflicts().getFixedWidth() != null)
        {
            widthGenerator = new FixedWidthGenerator(network.getConflicts().getFixedWidth().get(eval));
        }
        else if (network.getConflicts().getRelativeWidth() != null)
        {
            widthGenerator = new RelativeWidthGenerator(network.getConflicts().getRelativeWidth().get(eval));
        }
        else
        {
            throw new XmlParserException("Conflicts tag contains no valid element.");
        }

        Logger.ots().info("Generating conflicts");
        Map<String, Set<org.opentrafficsim.core.network.Link>> conflictCandidateMap = new LinkedHashMap<>();
        for (Link link : network.getLink())
        {
            if (link.getConflictId() != null)
            {
                if (!conflictCandidateMap.containsKey(link.getConflictId().get(eval)))
                {
                    conflictCandidateMap.put(link.getConflictId().get(eval), new LinkedHashSet<>());
                }
                conflictCandidateMap.get(link.getConflictId().get(eval)).add(getLink(otsNetwork, link.getId()));
            }
        }
        Logger.ots().info("Map size of conflict candidate regions = {}", conflictCandidateMap.size());

        // TODO: if there is any conflict ID specified, conflictCandidateMap is filled, and no other conflict anywhere will
        // be generated. How can we combine generation and specifying conflict IDs?
        // TODO: specify where conflicts are directly?
        if (conflictCandidateMap.isEmpty())
        {
            ConflictBuilder.buildConflictsParallel(otsNetwork, otsNetwork.getSimulator(), widthGenerator);
        }
        else
        {
            ConflictBuilder.buildConflictsParallel(otsNetwork, conflictCandidateMap, otsNetwork.getSimulator(), widthGenerator);
        }
        Logger.ots().info("Object map size = {}", otsNetwork.getObjectMap().size());
    }

    /**
     * Returns a flattener. If the flattener tag is empty, the flattener is obtained from the network.
     * @param defaultFlattener default flattener from the network, may be {@code null} to parse default flattener.
     * @param flattenerType XML tag of flattener.
     * @param eval evaluator.
     * @return Flattener.
     * @throws NetworkException if the flattener is not correctly defined.
     */
    private static CurveFlattener getFlattener(final CurveFlattener defaultFlattener, final FlattenerType flattenerType,
            final Eval eval) throws NetworkException
    {
        if (flattenerType == null)
        {
            return defaultFlattener;
        }
        if (flattenerType.getNumSegments() != null)
        {
            return new CurveFlattener(flattenerType.getNumSegments().get(eval));
        }
        else if (flattenerType.getDeviationAndAngle() != null)
        {
            if (flattenerType.getDeviationAndAngle().getMaxDeviation() != null)
            {
                if (flattenerType.getDeviationAndAngle().getMaxAngle() != null)
                {
                    return new CurveFlattener(getDeviation(flattenerType.getDeviationAndAngle().getMaxDeviation().get(eval)),
                            getAngle(flattenerType.getDeviationAndAngle().getMaxAngle().get(eval)));
                }
                return new CurveFlattener(getDeviation(flattenerType.getDeviationAndAngle().getMaxDeviation().get(eval)));
            }
            else if (flattenerType.getDeviationAndAngle().getMaxAngle() != null)
            {
                return new CurveFlattener(Angle.ofSI(getAngle(flattenerType.getDeviationAndAngle().getMaxAngle().get(eval))));
            }
            throw new NetworkException("No deviation and/or angle for flattener specified.");
        }
        throw new NetworkException("No flattener specified.");
    }

    /**
     * Returns a safe deviation value (>=0.001).
     * @param length deviation.
     * @return safe deviation.
     */
    private static double getDeviation(final Length length)
    {
        return length.si < 0.001 ? 0.001 : length.si;
    }

    /**
     * Returns a safe angle value (>=0.01).
     * @param angle angle.
     * @return safe angle.
     */
    private static double getAngle(final Angle angle)
    {
        return angle.si < 0.01 ? 0.01 : angle.si;
    }

    /**
     * Obtain node with id.
     * @param otsNetwork network
     * @param id id
     * @return node with id
     * @throws NetworkException if the node does not exist
     */
    public static Node getNode(final RoadNetwork otsNetwork, final String id) throws NetworkException
    {
        return otsNetwork.getNode(id).orElseThrow(() -> new NetworkException("Link with id " + id + " does not exist."));
    }

    /**
     * Obtain link with id.
     * @param otsNetwork network
     * @param id id
     * @return link with id
     * @throws NetworkException if the link does not exist
     */
    public static CrossSectionLink getLink(final RoadNetwork otsNetwork, final String id) throws NetworkException
    {
        return (CrossSectionLink) otsNetwork.getLink(id)
                .orElseThrow(() -> new NetworkException("Link with id " + id + " does not exist."));
    }

    /**
     * Obtain route with id.
     * @param otsNetwork network
     * @param id id
     * @return route with id
     * @throws NetworkException if the route does not exist
     */
    public static Route getRoute(final RoadNetwork otsNetwork, final String id) throws NetworkException
    {
        return otsNetwork.getRoute(id).orElseThrow(() -> new NetworkException("Route with id " + id + " does not exist."));
    }

}
