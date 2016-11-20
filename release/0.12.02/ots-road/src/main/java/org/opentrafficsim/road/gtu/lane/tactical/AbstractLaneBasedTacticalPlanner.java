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
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkDirection;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.CategorialLanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneBasedOperationalPlan;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneOperationalPlanBuilder;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneOperationalPlanBuilder.LaneChange;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
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
        this.lanePerception = new CategorialLanePerception(gtu);
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedGTU getGtu()
    {
        return this.gtu;
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
    public static LanePathInfo buildLanePathInfo(final LaneBasedGTU gtu, final Length maxHeadway)
            throws GTUException, NetworkException
    {
        DirectedLanePosition dlp = gtu.getReferencePosition();
        return buildLanePathInfo(gtu, maxHeadway, dlp.getLane(), dlp.getPosition(), dlp.getGtuDirection());
    }

    /**
     * Build a list of lanes forward, with a maximum headway relative to the reference point of the GTU.
     * @param gtu LaneBasedGTU; the GTU for which to calculate the lane list
     * @param maxHeadway Length; the maximum length for which lanes should be returned
     * @param startLane Lane; the lane in which the path starts
     * @param position Length; the position on the start lane
     * @param startDirectionality GTUDirectionality; the driving direction on the start lane
     * @return LanePathInfo; an instance that provides the following information for an operational plan: the lanes to follow,
     *         and the path to follow when staying on the same lane.
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    public static LanePathInfo buildLanePathInfo(final LaneBasedGTU gtu, final Length maxHeadway, final Lane startLane,
            final Length position, final GTUDirectionality startDirectionality) throws GTUException, NetworkException
    {
        List<LaneDirection> laneListForward = new ArrayList<>();
        Lane lane = startLane;
        GTUDirectionality lastGtuDir = startDirectionality;
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
            Map<Lane, GTUDirectionality> lanes = lastGtuDir.equals(GTUDirectionality.DIR_PLUS)
                    ? lane.nextLanes(gtu.getGTUType()) : lane.prevLanes(gtu.getGTUType());
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
     * Calculate the next location where the network splits, with a maximum headway relative to the reference point of the GTU.
     * Note: a lane drop is also considered a split (!).
     * @param gtu LaneBasedGTU; the GTU for which to calculate the lane list
     * @param maxHeadway Length; the maximum length for which lanes should be returned
     * @return NextSplitInfo; an instance that provides the following information for an operational plan: whether the network
     *         splits, the node where it splits, and the current lanes that lead to the right node after the split node.
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    public static NextSplitInfo determineNextSplit(final LaneBasedGTU gtu, final Length maxHeadway)
            throws GTUException, NetworkException
    {
        Node nextSplitNode = null;
        Set<Lane> correctCurrentLanes = new HashSet<>();
        DirectedLanePosition dlp = gtu.getReferencePosition();
        Lane referenceLane = dlp.getLane();
        double refFrac = dlp.getPosition().si / referenceLane.getLength().si;
        Link lastLink = referenceLane.getParentLink();
        GTUDirectionality lastGtuDir = dlp.getGtuDirection();
        GTUDirectionality referenceLaneDirectionality = lastGtuDir;
        Length lengthForward;
        Length position = dlp.getPosition();
        Node lastNode;
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
        while (lengthForward.lt(maxHeadway) && nextSplitNode == null)
        {
            // calculate the number of "outgoing" links
            Set<Link> links = lastNode.getLinks().toSet(); // safe copy
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
                        // if (noLaneDrop(gtu, maxHeadway, l, position, referenceLaneDirectionality))
                        if (noLaneDrop(gtu, maxHeadway, l, l.getLength().multiplyBy(refFrac), referenceLaneDirectionality))
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
                        // if (connectsToPath(gtu, maxHeadway, l, position, referenceLaneDirectionality, ld.getLink()))
                        if (connectsToPath(gtu, maxHeadway, l, l.getLength().multiplyBy(refFrac), referenceLaneDirectionality,
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
                    lastNode = lastLink.getEndNode();
                }
                else
                {
                    // -----> O <-----, GTU moves ---->
                    lastGtuDir = GTUDirectionality.DIR_MINUS;
                    lastNode = lastLink.getEndNode();
                }
            }
            else
            {
                if (lastLink.getStartNode().equals(link.getStartNode()))
                {
                    // <----- O ----->, GTU moves ---->
                    lastNode = lastLink.getStartNode();
                    lastGtuDir = GTUDirectionality.DIR_PLUS;
                }
                else
                {
                    // <----- O <-----, GTU moves ---->
                    lastNode = lastLink.getStartNode();
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
     * @param startLanePosition Length; the position on the start lane
     * @param startDirectionality GTUDirectionality; the driving direction on the start lane
     * @param linkAfterSplit Link; the link after the split to which we should connect
     * @return boolean; true if the lane (XXXXX which lane?) is connected to our path
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    protected static boolean connectsToPath(final LaneBasedGTU gtu, final Length maxHeadway, final Lane startLane,
            final Length startLanePosition, final GTUDirectionality startDirectionality, final Link linkAfterSplit)
            throws GTUException, NetworkException
    {
        List<LaneDirection> laneDirections =
                buildLanePathInfo(gtu, maxHeadway, startLane, startLanePosition, startDirectionality).getLaneDirectionList();
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
     * @param startLanePosition Length; the position on the start lane
     * @param startDirectionality GTUDirectionality; the driving direction on the start lane
     * @return boolean; true if the lane (XXX which lane?) is connected to our path
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    protected static boolean noLaneDrop(final LaneBasedGTU gtu, final Length maxHeadway, final Lane startLane,
            final Length startLanePosition, final GTUDirectionality startDirectionality) throws GTUException, NetworkException
    {
        LanePathInfo lpi = buildLanePathInfo(gtu, maxHeadway, startLane, startLanePosition, startDirectionality);
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
        DirectedLanePosition dlp = gtu.getReferencePosition();
        Lane referenceLane = dlp.getLane();
        Link lastLink = referenceLane.getParentLink();
        GTUDirectionality lastGtuDir = dlp.getGtuDirection();
        linkList.add(new LinkDirection(lastLink, lastGtuDir));
        Length lengthForward;
        Length position = dlp.getPosition();
        Node lastNode;
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
            Set<Link> links = lastNode.getLinks().toSet(); // is a safe copy
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
                    lastNode = lastLink.getEndNode();
                }
                else
                {
                    // -----> O <-----, GTU moves ---->
                    lastGtuDir = GTUDirectionality.DIR_MINUS;
                    lastNode = lastLink.getEndNode();
                }
            }
            else
            {
                if (lastLink.getStartNode().equals(link.getStartNode()))
                {
                    // <----- O ----->, GTU moves ---->
                    lastNode = lastLink.getStartNode();
                    lastGtuDir = GTUDirectionality.DIR_PLUS;
                }
                else
                {
                    // <----- O <-----, GTU moves ---->
                    lastNode = lastLink.getStartNode();
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
        List<Lane> lanes = null;
        if (!laneChange.isChangingLane())
        {
            lanes = buildLanePathInfo(gtu, forwardHeadway).getLanes();
        }
        else
        {
            // during a lane change take the lanes on the from lane
            // look in the opposite lateral direction relative to the reference lane, if that lane exists and the gtu is
            // registered on it, the reference lane is the to lane, while the found adjacent lane is the required from lane
            // TODO this is wrong, the other direction may not be allowed, i.e. is not accessible
            Map<Lane, Length> positions = gtu.positions(gtu.getReference());
            LateralDirectionality lat = laneChange.isChangingLeft() ? LateralDirectionality.LEFT : LateralDirectionality.RIGHT;
            DirectedLanePosition dlp = gtu.getReferencePosition();
            Iterator<Lane> iterator = dlp.getLane().accessibleAdjacentLanes(lat, gtu.getGTUType()).iterator();
            Lane adjLane = iterator.hasNext() ? iterator.next() : null;
            if (adjLane != null && positions.containsKey(adjLane))
            {
                // reference lane is from lane, this is ok
                lanes = buildLanePathInfo(gtu, forwardHeadway).getLanes();
            }
            else
            {
                // reference lane is to lane, this should be accounted for
                for (Lane lane : positions.keySet())
                {
                    if (lane.accessibleAdjacentLanes(lat, gtu.getGTUType()).contains(dlp.getLane()))
                    {
                        lanes = buildLanePathInfo(gtu, forwardHeadway, lane, positions.get(lane), dlp.getGtuDirection())
                                .getLanes();
                    }
                }
            }
            if (lanes == null)
            {
                throw new RuntimeException("From lane could not be determined during lane change.");
            }
        }
        if ((!simplePlan.isLaneChange() && !laneChange.isChangingLane())
                || (gtu.getSpeed().si == 0.0 && simplePlan.getAcceleration().si <= 0.0))
        {
            Length firstLanePosition = gtu.getReferencePosition().getPosition();
            try
            {
                return LaneOperationalPlanBuilder.buildAccelerationPlan(gtu, lanes, firstLanePosition, startTime,
                        gtu.getSpeed(), simplePlan.getAcceleration(), bc.getParameter(ParameterTypes.DT));
            }
            catch (OTSGeometryException exception)
            {
                throw new OperationalPlanException(exception);
            }
        }

        try
        {
            return LaneOperationalPlanBuilder.buildAccelerationLaneChangePlan(gtu, lanes, simplePlan.getLaneChangeDirection(),
                    gtu.getLocation(), startTime, gtu.getSpeed(), simplePlan.getAcceleration(),
                    bc.getParameter(ParameterTypes.DT), laneChange);
        }
        catch (OTSGeometryException exception)
        {
            throw new OperationalPlanException(exception);
        }
    }
}
