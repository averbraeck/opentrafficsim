package org.opentrafficsim.road.gtu.lane.tactical;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterTypeClass;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.geometry.OtsLine2d;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.Lmrs;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;

/**
 * A lane-based tactical planner generates an operational plan for the lane-based GTU. It can ask the strategic planner for
 * assistance on the route to take when the network splits. This abstract class contains a number of helper methods that make it
 * easy to implement a tactical planner.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public abstract class AbstractLaneBasedTacticalPlanner implements LaneBasedTacticalPlanner, Serializable
{

    /** Tactical planner parameter. */
    public static final ParameterTypeClass<LaneBasedTacticalPlanner> LANE_TACTICAL_PLANNER = new ParameterTypeClass<>(
            "lane tactical planner", "Lane-based tactical planner class.", LaneBasedTacticalPlanner.class, Lmrs.class);

    /** */
    private static final long serialVersionUID = 20151125L;

    /** Look ahead parameter type. */
    protected static final ParameterTypeLength LOOKAHEAD = ParameterTypes.LOOKAHEAD;

    /** Time step parameter type. */
    protected static final ParameterTypeDuration DT = ParameterTypes.DT;

    /** The car-following model. */
    private CarFollowingModel carFollowingModel;

    /** The perception. */
    private final LanePerception lanePerception;

    /** GTU. */
    private final LaneBasedGtu gtu;

    /**
     * Instantiates a tactical planner.
     * @param carFollowingModel car-following model
     * @param gtu GTU
     * @param lanePerception perception
     */
    public AbstractLaneBasedTacticalPlanner(final CarFollowingModel carFollowingModel, final LaneBasedGtu gtu,
            final LanePerception lanePerception)
    {
        setCarFollowingModel(carFollowingModel);
        this.gtu = gtu;
        this.lanePerception = lanePerception;
    }

    @Override
    public final LaneBasedGtu getGtu()
    {
        return this.gtu;
    }

    /**
     * Build a list of lanes forward, with a maximum headway relative to the reference point of the GTU.
     * @param gtu the GTU for which to calculate the lane list
     * @param maxHeadway the maximum length for which lanes should be returned
     * @return an instance that provides the following information for an operational plan: the lanes to follow, and the path to
     *         follow when staying on the same lane.
     * @throws GtuException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    public static LanePathInfo buildLanePathInfo(final LaneBasedGtu gtu, final Length maxHeadway)
            throws GtuException, NetworkException
    {
        LanePosition dlp = gtu.getReferencePosition();
        return buildLanePathInfo(gtu, maxHeadway, dlp.lane(), dlp.position());
    }

    /**
     * Build a list of lanes forward, with a maximum headway relative to the reference point of the GTU.
     * @param gtu the GTU for which to calculate the lane list
     * @param maxHeadway the maximum length for which lanes should be returned
     * @param startLane the lane in which the path starts
     * @param position the position on the start lane
     * @return an instance that provides the following information for an operational plan: the lanes to follow, and the path to
     *         follow when staying on the same lane.
     * @throws GtuException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     * @throws GtuException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    public static LanePathInfo buildLanePathInfo(final LaneBasedGtu gtu, final Length maxHeadway, final Lane startLane,
            final Length position) throws GtuException, NetworkException
    {
        List<Lane> laneListForward = new ArrayList<>();
        Lane lane = startLane;
        Length startPosition = position;
        Lane lastLane = lane;
        laneListForward.add(lastLane);
        Length distanceToEndOfLane;
        OtsLine2d path;
        distanceToEndOfLane = lane.getLength().minus(position);
        path = lane.getCenterLine().extract(position, lane.getLength());

        while (distanceToEndOfLane.lt(maxHeadway))
        {
            Set<Lane> lanes = lane.nextLanes(gtu.getType());
            if (lanes.size() == 0)
            {
                // Dead end. Return with the list as is.
                return new LanePathInfo(path, laneListForward, startPosition);
            }
            else if (lanes.size() == 1)
            {
                // Ask the strategical planner what the next link should be (if known), because the strategical planner knows
                // best!
                Link link = gtu.getStrategicalPlanner().nextLink(lane.getLink(), gtu.getType());
                lane = lanes.iterator().next();
                if (link != null && !lane.getLink().equals(link))
                {
                    // Lane not on route anymore. return with the list as is.
                    return new LanePathInfo(path, laneListForward, startPosition);
                }
            }
            else
            {
                // Multiple next lanes; ask the strategical planner where to go.
                // Note: this is not necessarily a split; it could e.g. be a bike path on a road
                Link link;
                try
                {
                    link = gtu.getStrategicalPlanner().nextLink(lane.getLink(), gtu.getType());
                }
                catch (NetworkException ne)
                {
                    // no route found. return the data structure up to this point...
                    return new LanePathInfo(path, laneListForward, startPosition);
                }
                Link nextLink = link;
                Lane newLane = null;
                for (Lane nextLane : lanes)
                {
                    if (nextLane.getLink().equals(nextLink))
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
            path = concatenateNull(path, lane.getCenterLine());
            // path = OtsLine2d.concatenate(Lane.MARGIN.si, path, lane.getCenterLine());

            laneListForward.add(lastLane);
            distanceToEndOfLane = distanceToEndOfLane.plus(lastLane.getLength());

        }
        return new LanePathInfo(path, laneListForward, startPosition);
    }

    /**
     * Concatenate two paths, where the first may be {@code null}.
     * @param path path, may be {@code null}
     * @param centerLine center line of lane to add
     * @return concatenated line
     */
    public static OtsLine2d concatenateNull(final OtsLine2d path, final OtsLine2d centerLine)
    {
        if (path == null)
        {
            return centerLine;
        }
        return OtsLine2d.concatenate(Lane.MARGIN.si, path, centerLine);
    }

    /**
     * Calculate the next location where the network splits, with a maximum headway relative to the reference point of the GTU.
     * Note: a lane drop is also considered a split (!).
     * @param gtu the GTU for which to calculate the lane list
     * @param maxHeadway the maximum length for which lanes should be returned
     * @return an instance that provides the following information for an operational plan: whether the network splits, the node
     *         where it splits, and the current lanes that lead to the right node after the split node.
     * @throws GtuException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    public static NextSplitInfo determineNextSplit(final LaneBasedGtu gtu, final Length maxHeadway)
            throws GtuException, NetworkException
    {
        Node nextSplitNode = null;
        Set<Lane> correctCurrentLanes = new LinkedHashSet<>();
        LanePosition dlp = gtu.getReferencePosition();
        Lane referenceLane = dlp.lane();
        double refFrac = dlp.position().si / referenceLane.getLength().si;
        Link lastLink = referenceLane.getLink();
        Length position = dlp.position();
        Length lengthForward = referenceLane.getLength().minus(position);
        Node lastNode = referenceLane.getLink().getEndNode();

        // see if we have a split within maxHeadway distance
        while (lengthForward.lt(maxHeadway) && nextSplitNode == null)
        {
            // calculate the number of "outgoing" links
            Set<Link> links = lastNode.getLinks().toSet(); // safe copy
            Iterator<Link> linkIterator = links.iterator();
            while (linkIterator.hasNext())
            {
                Link link = linkIterator.next();
                if (!link.getType().isCompatible(gtu.getType()) || link.getEndNode().equals(lastNode))
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
                    if (cse instanceof Lane)
                    {
                        Lane lane = (Lane) cse;
                        if (lane.nextLanes(gtu.getType()).size() == 0)
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
                for (CrossSectionElement cse : referenceLane.getLink().getCrossSectionElementList())
                {
                    if (cse instanceof Lane)
                    {
                        Lane l = (Lane) cse;
                        if (noLaneDrop(gtu, maxHeadway, l, l.getLength().times(refFrac)))
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
                Link nextLink = gtu.getStrategicalPlanner().nextLink(lastLink, gtu.getType());
                // which lane(s) we are registered on and adjacent lanes link to a lane
                // that is on the route at the next split?
                for (CrossSectionElement cse : referenceLane.getLink().getCrossSectionElementList())
                {
                    if (cse instanceof Lane)
                    {
                        Lane l = (Lane) cse;
                        if (connectsToPath(gtu, maxHeadway, l, l.getLength().times(refFrac), nextLink))
                        {
                            correctCurrentLanes.add(l);
                        }
                    }
                }
                if (correctCurrentLanes.size() > 0)
                {
                    return new NextSplitInfo(nextSplitNode, correctCurrentLanes);
                }
                // split, but no lane on current link to right direction
                Set<Lane> correctLanes = new LinkedHashSet<>();
                Set<Lane> wrongLanes = new LinkedHashSet<>();
                for (CrossSectionElement cse : ((CrossSectionLink) lastLink).getCrossSectionElementList())
                {
                    if (cse instanceof Lane)
                    {
                        Lane l = (Lane) cse;
                        if (connectsToPath(gtu, maxHeadway.plus(l.getLength()), l, Length.ZERO, nextLink))
                        {
                            correctLanes.add(l);
                        }
                        else
                        {
                            wrongLanes.add(l);
                        }
                    }
                }
                for (Lane wrongLane : wrongLanes)
                {
                    for (Lane adjLane : wrongLane.accessibleAdjacentLanesLegal(LateralDirectionality.LEFT, gtu.getType()))
                    {
                        if (correctLanes.contains(adjLane))
                        {
                            return new NextSplitInfo(nextSplitNode, correctCurrentLanes, LateralDirectionality.LEFT);
                        }
                    }
                    for (Lane adjLane : wrongLane.accessibleAdjacentLanesLegal(LateralDirectionality.RIGHT, gtu.getType()))
                    {
                        if (correctLanes.contains(adjLane))
                        {
                            return new NextSplitInfo(nextSplitNode, correctCurrentLanes, LateralDirectionality.RIGHT);
                        }
                    }
                }
                return new NextSplitInfo(nextSplitNode, correctCurrentLanes, null);
            }

            if (links.size() == 0)
            {
                return new NextSplitInfo(null, correctCurrentLanes);
            }

            // just one link
            Link link = links.iterator().next();

            // determine direction for the path
            lastNode = link.getEndNode();
            lastLink = links.iterator().next();
            lengthForward = lengthForward.plus(lastLink.getLength());
        }

        return new NextSplitInfo(null, correctCurrentLanes);
    }

    /**
     * Determine whether the lane is directly connected to our route, in other words: if we would (continue to) drive on the
     * given lane, can we take the right branch at the nextSplitNode without switching lanes?
     * @param gtu the GTU for which we have to determine the lane suitability
     * @param maxHeadway the maximum length for use in the calculation
     * @param startLane the first lane in the list
     * @param startLanePosition the position on the start lane
     * @param linkAfterSplit the link after the split to which we should connect
     * @return true if the lane (XXXXX which lane?) is connected to our path
     * @throws GtuException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    protected static boolean connectsToPath(final LaneBasedGtu gtu, final Length maxHeadway, final Lane startLane,
            final Length startLanePosition, final Link linkAfterSplit) throws GtuException, NetworkException
    {
        List<Lane> lanes = buildLanePathInfo(gtu, maxHeadway, startLane, startLanePosition).laneList();
        for (Lane lane : lanes)
        {
            if (lane.getLink().equals(linkAfterSplit))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine whether the lane does not drop, in other words: if we would (continue to) drive on the given lane, can we
     * continue to drive at the nextSplitNode without switching lanes?
     * @param gtu the GTU for which we have to determine the lane suitability
     * @param maxHeadway the maximum length for use in the calculation
     * @param startLane the first lane in the list
     * @param startLanePosition the position on the start lane
     * @return true if the lane (XXX which lane?) is connected to our path
     * @throws GtuException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    protected static boolean noLaneDrop(final LaneBasedGtu gtu, final Length maxHeadway, final Lane startLane,
            final Length startLanePosition) throws GtuException, NetworkException
    {
        LanePathInfo lpi = buildLanePathInfo(gtu, maxHeadway, startLane, startLanePosition);
        if (lpi.path().getTypedLength().lt(maxHeadway))
        {
            return false;
        }
        return true;
    }

    /**
     * Make a list of links on which to drive next, with a maximum headway relative to the reference point of the GTU.
     * @param gtu the GTU for which to calculate the link list
     * @param maxHeadway the maximum length for which links should be returned
     * @return a list of links on which to drive next
     * @throws GtuException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    protected static List<Link> buildLinkListForward(final LaneBasedGtu gtu, final Length maxHeadway)
            throws GtuException, NetworkException
    {
        List<Link> linkList = new ArrayList<>();
        LanePosition dlp = gtu.getReferencePosition();
        Lane referenceLane = dlp.lane();
        Link lastLink = referenceLane.getLink();
        linkList.add(lastLink);
        Length position = dlp.position();
        Length lengthForward = referenceLane.getLength().minus(position);
        Node lastNode = referenceLane.getLink().getEndNode();

        // see if we have a split within maxHeadway distance
        while (lengthForward.lt(maxHeadway))
        {
            // calculate the number of "outgoing" links
            Set<Link> links = lastNode.getLinks().toSet(); // is a safe copy
            Iterator<Link> linkIterator = links.iterator();
            while (linkIterator.hasNext())
            {
                Link link = linkIterator.next();
                if (link.equals(lastLink) || !link.getType().isCompatible(gtu.getType()))
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
                link = gtu.getStrategicalPlanner().nextLink(lastLink, gtu.getType());
            }
            else
            {
                link = links.iterator().next();
            }

            // determine direction for the path
            lastNode = lastLink.getEndNode();
            lastLink = link;
            linkList.add(lastLink);
            lengthForward = lengthForward.plus(lastLink.getLength());
        }
        return linkList;
    }

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

    @Override
    public final LanePerception getPerception()
    {
        return this.lanePerception;
    }

}
