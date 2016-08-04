package org.opentrafficsim.road.gtu.lane.tactical;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkDirection;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.CategorialLanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DefaultAlexander;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructureCategory;
import org.opentrafficsim.road.gtu.lane.perception.categories.IntersectionCategory;
import org.opentrafficsim.road.gtu.lane.perception.categories.NeighborsCategory;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneBasedOperationalPlan;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneOperationalPlanBuilder;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneOperationalPlanBuilder.LaneChange;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;

/**
 * A lane-based tactical planner generates an operational plan for the lane-based GTU. It can ask the strategic planner for
 * assistance on the route to take when the network splits. This abstract class contains a number of helper methods that make it
 * easy to implement a tactical planner.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 25, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractLaneBasedTacticalPlanner implements LaneBasedTacticalPlanner, Serializable
{
    /** */
    private static final long serialVersionUID = 20151125L;

    /** The car-following model. */
    private CarFollowingModel carFollowingModel;

    /** The perception. */
    private final LanePerception lanePerception;

    /** GTU. */
    private final LaneBasedGTU gtu;

    /**
     * Instantiates a tactical planner.
     * @param carFollowingModel car-following model
     * @param gtu GTU
     */
    public AbstractLaneBasedTacticalPlanner(final CarFollowingModel carFollowingModel, final LaneBasedGTU gtu)
    {
        setCarFollowingModel(carFollowingModel);
        this.gtu = gtu;
        CategorialLanePerception perception = new CategorialLanePerception(gtu);
        perception.addPerceptionCategory(new DefaultAlexander(perception));
        perception.addPerceptionCategory(new InfrastructureCategory(perception));
        perception.addPerceptionCategory(new NeighborsCategory(perception));
        perception.addPerceptionCategory(new IntersectionCategory(perception));
        this.lanePerception = perception;
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedGTU getGtu()
    {
        return this.gtu;
    }

    /**
     * The reference lane is the widest lane on which the reference point of the GTU is fully registered. If the reference point
     * is not fully registered on one of the lanes, return a lane where the reference point is not fully registered as a
     * fallback option. This can, for instance happen when the GTU has just been generated, or when the GTU is about to be
     * destroyed at the end of a lane.
     * @param gtu LaneBasedGTU; the GTU for which to determine the lane on which the GTU's reference point lies
     * @return Lane; the widest lane on which the reference point lies between start and end, or any lane where the GTU is
     *         registered as a fallback option.
     * @throws GTUException when the GTU's positions cannot be determined or when the GTU is not registered on any lane.
     */
    public static Lane getReferenceLane(final LaneBasedGTU gtu) throws GTUException
    {
        for (Lane lane : gtu.getLanes().keySet())
        {
            double fraction = gtu.fractionalPosition(lane, gtu.getReference());
            if (fraction >= 0.0 && fraction <= 1.0)
            {
                // TODO widest lane in case we are registered on more than one lane with the reference point
                // TODO lane that leads to our location or not if we are registered on parallel lanes?
                return lane;
            }
        }

        // TODO lane closest to length or 0
        System.err.println(gtu + " does not have a reference lane with pos between 0 and length...");
        if (gtu.getLanes().size() > 0)
        {
            return gtu.getLanes().keySet().iterator().next();
        }

        throw new GTUException("The reference point of GTU " + gtu + " is not on any of the lanes on which it is registered");
    }

    /**
     * Build a list of lanes forward, with a maximum headway relative to the reference point of the GTU.
     * @param gtu LaneBasedGTU; the GTU for which to calculate the lane list
     * @param maxHeadway Length; the maximum length for which lanes should be returned
     * @return LanePathInfo; an instance that provides the following information for an operational plan: the lanes to follow,
     *         and the path to follow when staying on the same lane.
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    public static LanePathInfo buildLanePathInfo(final LaneBasedGTU gtu, final Length maxHeadway) throws GTUException,
        NetworkException
    {
        Lane startLane = getReferenceLane(gtu);
        Length startPosition = gtu.position(startLane, gtu.getReference());
        return buildLanePathInfo(gtu, startLane, startPosition, maxHeadway);
    }

    /**
     * Build a list of lanes forward, with a maximum headway relative to the reference point of the GTU.
     * @param gtu LaneBasedGTU; the GTU for which to calculate the lane list
     * @param startLane Lane; the lane in which the path starts
     * @param startPosition Length; the start position on the start lane with the Vehicle's reference point
     * @param maxHeadway Length; the maximum length for which lanes should be returned
     * @return LanePathInfo; an instance that provides the following information for an operational plan: the lanes to follow,
     *         and the path to follow when staying on the same lane.
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    public static LanePathInfo buildLanePathInfo(final LaneBasedGTU gtu, final Lane startLane, final Length startPosition,
        final Length maxHeadway) throws GTUException, NetworkException
    {
        return buildLanePathInfo(gtu, maxHeadway, startLane, startLane.fraction(startPosition), gtu.getLanes()
            .get(startLane));
    }

    /**
     * Build a list of lanes forward, with a maximum headway relative to the reference point of the GTU.
     * @param gtu LaneBasedGTU; the GTU for which to calculate the lane list
     * @param maxHeadway Length; the maximum length for which lanes should be returned
     * @param startLane Lane; the lane in which the path starts
     * @param startLaneFractionalPosition double; the fractional position on the start lane
     * @param startDirectionality GTUDirectionality; the driving direction on the start lane
     * @return LanePathInfo; an instance that provides the following information for an operational plan: the lanes to follow,
     *         and the path to follow when staying on the same lane.
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    public static LanePathInfo buildLanePathInfo(final LaneBasedGTU gtu, final Length maxHeadway, final Lane startLane,
        final double startLaneFractionalPosition, final GTUDirectionality startDirectionality) throws GTUException,
        NetworkException
    {
        List<LaneDirection> laneListForward = new ArrayList<>();
        Lane lane = startLane;
        GTUDirectionality lastGtuDir = startDirectionality;
        Length position = lane.position(startLaneFractionalPosition);
        Length startPosition = position;
        Lane lastLane = lane;
        laneListForward.add(new LaneDirection(lastLane, lastGtuDir));
        Length distanceToEndOfLane;
        OTSLine3D path;
        try
        {
            if (lastGtuDir.equals(GTUDirectionality.DIR_PLUS))
            {
                distanceToEndOfLane = lane.getLength().minus(position);
                path = lane.getCenterLine().extract(position, lane.getLength());
            }
            else
            {
                distanceToEndOfLane = position;
                path = lane.getCenterLine().extract(Length.ZERO, position).reverse();
            }
        }
        catch (OTSGeometryException exception)
        {
            System.err.println(gtu + ": " + exception.getMessage());
            System.err.println(lane + ", len=" + lane.getLength());
            System.err.println(position);
            throw new GTUException(exception);
        }

        while (distanceToEndOfLane.lt(maxHeadway))
        {
            Map<Lane, GTUDirectionality> lanes =
                lastGtuDir.equals(GTUDirectionality.DIR_PLUS) ? lane.nextLanes(gtu.getGTUType()) : lane.prevLanes(gtu
                    .getGTUType());
            if (lanes.size() == 0)
            {
                // Dead end. Return with the list as is.
                return new LanePathInfo(path, laneListForward, startPosition);
            }
            else if (lanes.size() == 1)
            {
                // Ask the strategical planner what the next link should be (if known), because the strategical planner knows
                // best!
                LinkDirection ld = null;
                ld = gtu.getStrategicalPlanner().nextLinkDirection(lane.getParentLink(), lastGtuDir, gtu.getGTUType());
                lane = lanes.keySet().iterator().next();
                if (ld != null && !lane.getParentLink().equals(ld.getLink()))
                {
                    // Lane not on route anymore. return with the list as is.
                    return new LanePathInfo(path, laneListForward, startPosition);
                }
            }
            else
            {
                // Multiple next lanes; ask the strategical planner where to go.
                // Note: this is not necessarily a split; it could e.g. be a bike path on a road
                LinkDirection ld;
                try
                {
                    ld = gtu.getStrategicalPlanner().nextLinkDirection(lane.getParentLink(), /* gtu.getLanes().get(lane), */
                    lastGtuDir, gtu.getGTUType());
                }
                catch (NetworkException ne)
                {
                    // no route found.
                    // return the data structure up to this point...
                    return new LanePathInfo(path, laneListForward, startPosition);
                }
                Link nextLink = ld.getLink();
                Lane newLane = null;
                for (Lane nextLane : lanes.keySet())
                {
                    if (nextLane.getParentLink().equals(nextLink))
                    {
                        newLane = nextLane;
                        break;
                    }
                }
                if (newLane == null)
                {
                    // we cannot reach the next node on this lane -- we have to make a lane change!
                    // return the data structure up to this point...
                    return new LanePathInfo(path, laneListForward, startPosition);
                }
                // otherwise: continue!
                lane = newLane;
            }

            // determine direction for the path
            try
            {
                if (lastGtuDir.equals(GTUDirectionality.DIR_PLUS))
                {
                    if (lastLane.getParentLink().getEndNode().equals(lane.getParentLink().getStartNode()))
                    {
                        // -----> O ----->, GTU moves ---->
                        path = OTSLine3D.concatenate(Lane.MARGIN.si, path, lane.getCenterLine());
                        lastGtuDir = GTUDirectionality.DIR_PLUS;
                    }
                    else
                    {
                        // -----> O <-----, GTU moves ---->
                        path = OTSLine3D.concatenate(Lane.MARGIN.si, path, lane.getCenterLine().reverse());
                        lastGtuDir = GTUDirectionality.DIR_MINUS;
                    }
                }
                else
                {
                    if (lastLane.getParentLink().getStartNode().equals(lane.getParentLink().getStartNode()))
                    {
                        // <----- O ----->, GTU moves ---->
                        path = OTSLine3D.concatenate(Lane.MARGIN.si, path, lane.getCenterLine());
                        lastGtuDir = GTUDirectionality.DIR_PLUS;
                    }
                    else
                    {
                        // <----- O <-----, GTU moves ---->
                        path = OTSLine3D.concatenate(Lane.MARGIN.si, path, lane.getCenterLine().reverse());
                        lastGtuDir = GTUDirectionality.DIR_MINUS;
                    }
                }
                lastLane = lane;
            }
            catch (OTSGeometryException exception)
            {
                throw new GTUException(exception);
            }

            laneListForward.add(new LaneDirection(lastLane, lastGtuDir));
            distanceToEndOfLane = distanceToEndOfLane.plus(lastLane.getLength());

        }
        return new LanePathInfo(path, laneListForward, startPosition);
    }

    /**
     * Build a list of lanes forward, with a maximum headway.
     * @param gtu LaneBasedGTU; the GTU for which to calculate the lane list
     * @param maxHeadway Length; the maximum length for which lanes should be returned
     * @param startLane Lane; the first lane in the list
     * @param startLaneFractionalPosition double; the fractional position on the start lane
     * @param startDirectionality GTUDirectionality; the driving direction on the start lane
     * @return List&lt;Lane&gt;; a list of lanes, connected to the startLane and following the path of the StrategicalPlanner.
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    // TODO this method can probably disappear (lots of duplicated code)
    protected static List<Lane> buildLaneListForwardXXX(final LaneBasedGTU gtu, final Length maxHeadway,
        final Lane startLane, final double startLaneFractionalPosition, final GTUDirectionality startDirectionality)
        throws GTUException, NetworkException
    {
        List<Lane> laneListForward = new ArrayList<>();
        Lane lane = startLane;
        Lane lastLane = startLane;
        GTUDirectionality lastGtuDir = startDirectionality;
        laneListForward.add(lane);
        Length position = lane.position(startLaneFractionalPosition);
        Length lengthForward = lastGtuDir.equals(GTUDirectionality.DIR_PLUS) ? lane.getLength().minus(position) : position;

        while (lengthForward.lt(maxHeadway))
        {
            Map<Lane, GTUDirectionality> lanes =
                lastGtuDir.equals(GTUDirectionality.DIR_PLUS) ? lane.nextLanes(gtu.getGTUType()) : lane.prevLanes(gtu
                    .getGTUType());
            if (lanes.size() == 0)
            {
                // dead end. return with the list as is.
                return laneListForward;
            }
            if (lanes.size() == 1)
            {
                lane = lanes.keySet().iterator().next();
            }
            else
            {
                // multiple next lanes; ask the strategical planner where to go
                // note: this is not necessarily a split; it could e.g. be a bike path on a road
                LinkDirection ld =
                    gtu.getStrategicalPlanner().nextLinkDirection(lane.getParentLink(), gtu.getLanes().get(lane),
                        gtu.getGTUType());
                Link nextLink = ld.getLink();
                for (Lane nextLane : lanes.keySet())
                {
                    if (nextLane.getParentLink().equals(nextLink))
                    {
                        lane = nextLane;
                        break;
                    }
                }
            }
            laneListForward.add(lane);
            lengthForward = lengthForward.plus(lane.getLength());

            // determine direction for the path
            if (lastGtuDir.equals(GTUDirectionality.DIR_PLUS))
            {
                if (lastLane.getParentLink().getEndNode().equals(lane.getParentLink().getStartNode()))
                {
                    // -----> O ----->, GTU moves ---->
                    lastGtuDir = GTUDirectionality.DIR_PLUS;
                }
                else
                {
                    // -----> O <-----, GTU moves ---->
                    lastGtuDir = GTUDirectionality.DIR_MINUS;
                }
            }
            else
            {
                if (lastLane.getParentLink().getStartNode().equals(lane.getParentLink().getStartNode()))
                {
                    // <----- O ----->, GTU moves ---->
                    lastGtuDir = GTUDirectionality.DIR_PLUS;
                }
                else
                {
                    // <----- O <-----, GTU moves ---->
                    lastGtuDir = GTUDirectionality.DIR_MINUS;
                }
            }
            lastLane = lane;
        }
        return laneListForward;
    }

    /**
     * Calculate the next location where the network splits, with a maximum headway relative to the reference point of the GTU.
     * Note: a lane drop is also considered a split (!).
     * @param gtu LaneBasedGTU; the GTU for which to calculate the lane list
     * @param maxHeadway Length; the maximum length for which lanes should be returned
     * @return NextSplitInfo; an instance that provides the following information for an operational plan: whether the network
     *         splits, the node where it splits, and the current lanes that lead to the right node after the split node.
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    public static NextSplitInfo determineNextSplit(final LaneBasedGTU gtu, final Length maxHeadway) throws GTUException,
        NetworkException
    {
        OTSNode nextSplitNode = null;
        Set<Lane> correctCurrentLanes = new HashSet<>();
        Lane referenceLane = getReferenceLane(gtu);
        Link lastLink = referenceLane.getParentLink();
        GTUDirectionality lastGtuDir = gtu.getLanes().get(referenceLane);
        GTUDirectionality referenceLaneDirectionality = lastGtuDir;
        Length lengthForward;
        Length position = gtu.position(referenceLane, gtu.getReference());
        OTSNode lastNode;
        if (lastGtuDir.equals(GTUDirectionality.DIR_PLUS))
        {
            lengthForward = referenceLane.getLength().minus(position);
            lastNode = referenceLane.getParentLink().getEndNode();
        }
        else
        {
            lengthForward = gtu.position(referenceLane, gtu.getReference());
            lastNode = referenceLane.getParentLink().getStartNode();
        }
        double referenceLaneFractionalPosition = position.si / referenceLane.getLength().si;

        // see if we have a split within maxHeadway distance
        while (lengthForward.lt(maxHeadway) && nextSplitNode == null)
        {
            // calculate the number of "outgoing" links
            Set<Link> links = lastNode.getLinks(); // safe copy
            Iterator<Link> linkIterator = links.iterator();
            while (linkIterator.hasNext())
            {
                Link link = linkIterator.next();
                if (link.equals(lastLink) || !link.getLinkType().isCompatible(gtu.getGTUType()))
                {
                    linkIterator.remove();
                }
            }

            // calculate the number of incoming and outgoing lanes on the link
            boolean laneChange = false;
            if (links.size() == 1)
            {
                for (CrossSectionElement cse : ((CrossSectionLink) lastLink).getCrossSectionElementList())
                {
                    if (cse instanceof Lane && lastGtuDir.isPlus())
                    {
                        Lane lane = (Lane) cse;
                        if (lane.nextLanes(gtu.getGTUType()).size() == 0)
                        {
                            laneChange = true;
                        }
                    }
                    if (cse instanceof Lane && lastGtuDir.isMinus())
                    {
                        Lane lane = (Lane) cse;
                        if (lane.prevLanes(gtu.getGTUType()).size() == 0)
                        {
                            laneChange = true;
                        }
                    }
                }
            }

            // see if we have a lane drop
            if (laneChange)
            {
                nextSplitNode = lastNode;
                // which lane(s) we are registered on and adjacent lanes link to a lane
                // that does not drop?
                for (CrossSectionElement cse : referenceLane.getParentLink().getCrossSectionElementList())
                {
                    if (cse instanceof Lane)
                    {
                        Lane l = (Lane) cse;
                        if (noLaneDrop(gtu, maxHeadway, l, referenceLaneFractionalPosition, referenceLaneDirectionality))
                        {
                            correctCurrentLanes.add(l);
                        }
                    }
                }
                return new NextSplitInfo(nextSplitNode, correctCurrentLanes);
            }

            // see if we have a split
            if (links.size() > 1)
            {
                nextSplitNode = lastNode;
                LinkDirection ld = gtu.getStrategicalPlanner().nextLinkDirection(nextSplitNode, lastLink, gtu.getGTUType());
                // which lane(s) we are registered on and adjacent lanes link to a lane
                // that is on the route at the next split?
                for (CrossSectionElement cse : referenceLane.getParentLink().getCrossSectionElementList())
                {
                    if (cse instanceof Lane)
                    {
                        Lane l = (Lane) cse;
                        if (connectsToPath(gtu, maxHeadway, l, referenceLaneFractionalPosition, referenceLaneDirectionality,
                            ld.getLink()))
                        {
                            correctCurrentLanes.add(l);
                        }
                    }
                }
                return new NextSplitInfo(nextSplitNode, correctCurrentLanes);
            }

            if (links.size() == 0)
            {
                return new NextSplitInfo(null, correctCurrentLanes);
            }

            // just one link
            Link link = links.iterator().next();

            // determine direction for the path
            if (lastGtuDir.equals(GTUDirectionality.DIR_PLUS))
            {
                if (lastLink.getEndNode().equals(link.getStartNode()))
                {
                    // -----> O ----->, GTU moves ---->
                    lastGtuDir = GTUDirectionality.DIR_PLUS;
                    lastNode = (OTSNode) lastLink.getEndNode();
                }
                else
                {
                    // -----> O <-----, GTU moves ---->
                    lastGtuDir = GTUDirectionality.DIR_MINUS;
                    lastNode = (OTSNode) lastLink.getEndNode();
                }
            }
            else
            {
                if (lastLink.getStartNode().equals(link.getStartNode()))
                {
                    // <----- O ----->, GTU moves ---->
                    lastNode = (OTSNode) lastLink.getStartNode();
                    lastGtuDir = GTUDirectionality.DIR_PLUS;
                }
                else
                {
                    // <----- O <-----, GTU moves ---->
                    lastNode = (OTSNode) lastLink.getStartNode();
                    lastGtuDir = GTUDirectionality.DIR_MINUS;
                }
            }
            lastLink = links.iterator().next();
            lengthForward = lengthForward.plus(lastLink.getLength());
        }

        return new NextSplitInfo(null, correctCurrentLanes);
    }

    /**
     * Determine whether the lane is directly connected to our route, in other words: if we would (continue to) drive on the
     * given lane, can we take the right branch at the nextSplitNode without switching lanes?
     * @param gtu LaneBasedGTU; the GTU for which we have to determine the lane suitability
     * @param maxHeadway Length; the maximum length for use in the calculation
     * @param startLane Lane; the first lane in the list
     * @param startLaneFractionalPosition double; the fractional position on the start lane
     * @param startDirectionality GTUDirectionality; the driving direction on the start lane
     * @param linkAfterSplit Link; the link after the split to which we should connect
     * @return boolean; true if the lane (XXXXX which lane?) is connected to our path
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    protected static boolean connectsToPath(final LaneBasedGTU gtu, final Length maxHeadway, final Lane startLane,
        final double startLaneFractionalPosition, final GTUDirectionality startDirectionality, final Link linkAfterSplit)
        throws GTUException, NetworkException
    {
        List<LaneDirection> laneDirections =
            buildLanePathInfo(gtu, maxHeadway, startLane, startLaneFractionalPosition, startDirectionality)
                .getLaneDirectionList();
        for (LaneDirection laneDirection : laneDirections)
        {
            if (laneDirection.getLane().getParentLink().equals(linkAfterSplit))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine whether the lane does not drop, in other words: if we would (continue to) drive on the given lane, can we
     * continue to drive at the nextSplitNode without switching lanes?
     * @param gtu LaneBasedGTU; the GTU for which we have to determine the lane suitability
     * @param maxHeadway Length; the maximum length for use in the calculation
     * @param startLane Lane; the first lane in the list
     * @param startLaneFractionalPosition double; the fractional position on the start lane
     * @param startDirectionality GTUDirectionality; the driving direction on the start lane
     * @return boolean; true if the lane (XXXXX which lane?) is connected to our path
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    protected static boolean noLaneDrop(final LaneBasedGTU gtu, final Length maxHeadway, final Lane startLane,
        final double startLaneFractionalPosition, final GTUDirectionality startDirectionality) throws GTUException,
        NetworkException
    {
        LanePathInfo lpi = buildLanePathInfo(gtu, maxHeadway, startLane, startLaneFractionalPosition, startDirectionality);
        if (lpi.getPath().getLength().lt(maxHeadway))
        {
            return false;
        }
        return true;
    }

    /**
     * Make a list of links on which to drive next, with a maximum headway relative to the reference point of the GTU.
     * @param gtu LaneBasedGTU; the GTU for which to calculate the link list
     * @param maxHeadway Length; the maximum length for which links should be returned
     * @return List&lt;LinkDirection&gt;; a list of links on which to drive next
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    protected static List<LinkDirection> buildLinkListForward(final LaneBasedGTU gtu, final Length maxHeadway)
        throws GTUException, NetworkException
    {
        List<LinkDirection> linkList = new ArrayList<>();
        Lane referenceLane = getReferenceLane(gtu);
        Link lastLink = referenceLane.getParentLink();
        GTUDirectionality lastGtuDir = gtu.getLanes().get(referenceLane);
        linkList.add(new LinkDirection(lastLink, lastGtuDir));
        Length lengthForward;
        Length position = gtu.position(referenceLane, gtu.getReference());
        OTSNode lastNode;
        if (lastGtuDir.equals(GTUDirectionality.DIR_PLUS))
        {
            lengthForward = referenceLane.getLength().minus(position);
            lastNode = referenceLane.getParentLink().getEndNode();
        }
        else
        {
            lengthForward = gtu.position(referenceLane, gtu.getReference());
            lastNode = referenceLane.getParentLink().getStartNode();
        }

        // see if we have a split within maxHeadway distance
        while (lengthForward.lt(maxHeadway))
        {
            // calculate the number of "outgoing" links
            Set<Link> links = lastNode.getLinks(); // is a safe copy
            Iterator<Link> linkIterator = links.iterator();
            while (linkIterator.hasNext())
            {
                Link link = linkIterator.next();
                if (link.equals(lastLink) || !link.getLinkType().isCompatible(gtu.getGTUType()))
                {
                    linkIterator.remove();
                }
            }

            if (links.size() == 0)
            {
                return linkList; // the path stops here...
            }

            Link link;
            if (links.size() > 1)
            {
                LinkDirection ld = gtu.getStrategicalPlanner().nextLinkDirection(lastLink, lastGtuDir, gtu.getGTUType());
                link = ld.getLink();
            }
            else
            {
                link = links.iterator().next();
            }

            // determine direction for the path
            if (lastGtuDir.equals(GTUDirectionality.DIR_PLUS))
            {
                if (lastLink.getEndNode().equals(link.getStartNode()))
                {
                    // -----> O ----->, GTU moves ---->
                    lastGtuDir = GTUDirectionality.DIR_PLUS;
                    lastNode = (OTSNode) lastLink.getEndNode();
                }
                else
                {
                    // -----> O <-----, GTU moves ---->
                    lastGtuDir = GTUDirectionality.DIR_MINUS;
                    lastNode = (OTSNode) lastLink.getEndNode();
                }
            }
            else
            {
                if (lastLink.getStartNode().equals(link.getStartNode()))
                {
                    // <----- O ----->, GTU moves ---->
                    lastNode = (OTSNode) lastLink.getStartNode();
                    lastGtuDir = GTUDirectionality.DIR_PLUS;
                }
                else
                {
                    // <----- O <-----, GTU moves ---->
                    lastNode = (OTSNode) lastLink.getStartNode();
                    lastGtuDir = GTUDirectionality.DIR_MINUS;
                }
            }
            lastLink = link;
            linkList.add(new LinkDirection(lastLink, lastGtuDir));
            lengthForward = lengthForward.plus(lastLink.getLength());
        }
        return linkList;
    }

    /** {@inheritDoc} */
    @Override
    public final CarFollowingModel getCarFollowingModel()
    {
        return this.carFollowingModel;
    }

    /**
     * Sets the car-following model.
     * @param carFollowingModel Car-following model to set.
     */
    public final void setCarFollowingModel(final CarFollowingModel carFollowingModel)
    {
        this.carFollowingModel = carFollowingModel;
    }

    /** {@inheritDoc} */
    @Override
    public final LanePerception getPerception()
    {
        return this.lanePerception;
    }

    /**
     * Build an operational plan based on a simple operational plan and status info.
     * @param gtu gtu
     * @param startTime start time for plan
     * @param bc behavioral characteristics
     * @param simplePlan simple operational plan
     * @param laneChange lane change status
     * @return operational plan
     * @throws ParameterException if parameter is not defined
     * @throws GTUException gtu exception
     * @throws NetworkException network exception
     * @throws OperationalPlanException operational plan exeption
     */
    public static LaneBasedOperationalPlan buildPlanFromSimplePlan(final LaneBasedGTU gtu, final Time startTime,
        final BehavioralCharacteristics bc, final SimpleOperationalPlan simplePlan, final LaneChange laneChange)
        throws ParameterException, GTUException, NetworkException, OperationalPlanException
    {
        Length forwardHeadway = bc.getParameter(ParameterTypes.LOOKAHEAD);
        List<Lane> lanes = buildLanePathInfo(gtu, forwardHeadway).getLanes();
        if (simplePlan.getLaneChangeDirection() == null)
        {
            Length firstLanePosition = gtu.position(getReferenceLane(gtu), RelativePosition.REFERENCE_POSITION);
            try
            {
                return LaneOperationalPlanBuilder.buildAccelerationPlan(gtu, lanes, firstLanePosition, startTime, gtu
                    .getSpeed(), simplePlan.getAcceleration(), bc.getParameter(ParameterTypes.DT));
            }
            catch (OTSGeometryException exception)
            {
                throw new OperationalPlanException(exception);
            }
        }

        try
        {
            return LaneOperationalPlanBuilder.buildAccelerationLaneChangePlan(gtu, lanes, simplePlan
                .getLaneChangeDirection(), gtu.getLocation(), startTime, gtu.getSpeed(), simplePlan.getAcceleration(), bc
                .getParameter(ParameterTypes.DT), laneChange);
        }
        catch (OTSGeometryException exception)
        {
            throw new OperationalPlanException(exception);
        }
    }
}
