package org.opentrafficsim.road.gtu.lane.tactical;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.OTSClassUtil;
import org.opentrafficsim.base.parameters.ParameterTypeClass;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkDirection;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRS;
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
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 25, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractLaneBasedTacticalPlanner implements LaneBasedTacticalPlanner, Serializable
{

    /** Tactical planner parameter. */
    public static final ParameterTypeClass<LaneBasedTacticalPlanner> TACTICAL_PLANNER;

    static
    {
        Class<LaneBasedTacticalPlanner> type = LaneBasedTacticalPlanner.class;
        TACTICAL_PLANNER = new ParameterTypeClass<>("tactical planner", "Tactical planner class.",
                OTSClassUtil.getTypedClass(type), LMRS.class);
    }

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
    private final LaneBasedGTU gtu;

    /**
     * Instantiates a tactical planner.
     * @param carFollowingModel CarFollowingModel; car-following model
     * @param gtu LaneBasedGTU; GTU
     * @param lanePerception LanePerception; perception
     */
    public AbstractLaneBasedTacticalPlanner(final CarFollowingModel carFollowingModel, final LaneBasedGTU gtu,
            final LanePerception lanePerception)
    {
        setCarFollowingModel(carFollowingModel);
        this.gtu = gtu;
        this.lanePerception = lanePerception;
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
            // System.err.println(gtu + ": " + exception.getMessage());
            // System.err.println(lane + ", len=" + lane.getLength());
            // System.err.println(position);
            // throw new GTUException(exception);

            // section on current lane too short, floating point operations cause only a single point at the end of the lane
            path = null;
            distanceToEndOfLane = Length.ZERO;
            laneListForward.clear();
            startPosition = Length.ZERO;
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
                        path = concatenateNull(path, lane.getCenterLine());
                        // path = OTSLine3D.concatenate(Lane.MARGIN.si, path, lane.getCenterLine());
                        lastGtuDir = GTUDirectionality.DIR_PLUS;
                    }
                    else
                    {
                        // -----> O <-----, GTU moves ---->
                        path = concatenateNull(path, lane.getCenterLine().reverse());
                        // path = OTSLine3D.concatenate(Lane.MARGIN.si, path, lane.getCenterLine().reverse());
                        lastGtuDir = GTUDirectionality.DIR_MINUS;
                    }
                }
                else
                {
                    if (lastLane.getParentLink().getStartNode().equals(lane.getParentLink().getStartNode()))
                    {
                        // <----- O ----->, GTU moves ---->
                        path = concatenateNull(path, lane.getCenterLine());
                        // path = OTSLine3D.concatenate(Lane.MARGIN.si, path, lane.getCenterLine());
                        lastGtuDir = GTUDirectionality.DIR_PLUS;
                    }
                    else
                    {
                        // <----- O <-----, GTU moves ---->
                        path = concatenateNull(path, lane.getCenterLine().reverse());
                        // path = OTSLine3D.concatenate(Lane.MARGIN.si, path, lane.getCenterLine().reverse());
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
     * Concatenate two paths, where the first may be {@code null}.
     * @param path OTSLine3D; path, may be {@code null}
     * @param centerLine OTSLine3D; center line of lane to add
     * @return concatenated line
     * @throws OTSGeometryException when lines are degenerate or too distant
     */
    public static OTSLine3D concatenateNull(final OTSLine3D path, final OTSLine3D centerLine) throws OTSGeometryException
    {
        if (path == null)
        {
            return centerLine;
        }
        return OTSLine3D.concatenate(Lane.MARGIN.si, path, centerLine);
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
        Set<Lane> correctCurrentLanes = new LinkedHashSet<>();
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
                GTUDirectionality drivingDirection =
                        lastNode.equals(link.getStartNode()) ? GTUDirectionality.DIR_PLUS : GTUDirectionality.DIR_MINUS;
                if (!link.getDirectionality(gtu.getGTUType()).getDirectionalities().contains(drivingDirection))
                {
                    linkIterator.remove();
                }
                // if (link.equals(lastLink) || !link.getLinkType().isCompatible(gtu.getGTUType())
                // || (link.getDirectionality(gtu.getGTUType()).isForward() && link.getEndNode().equals(lastNode))
                // || (link.getDirectionality(gtu.getGTUType()).isBackward() && link.getStartNode().equals(lastNode)))
                // {
                // linkIterator.remove();
                // }
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
                        if (connectsToPath(gtu, maxHeadway.plus(l.getLength()), l, Length.ZERO, lastGtuDir, ld.getLink()))
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
                    for (Lane adjLane : wrongLane.accessibleAdjacentLanesLegal(LateralDirectionality.LEFT, gtu.getGTUType(),
                            referenceLaneDirectionality))
                    {
                        if (correctLanes.contains(adjLane))
                        {
                            return new NextSplitInfo(nextSplitNode, correctCurrentLanes, LateralDirectionality.LEFT);
                        }
                    }
                    for (Lane adjLane : wrongLane.accessibleAdjacentLanesLegal(LateralDirectionality.RIGHT, gtu.getGTUType(),
                            referenceLaneDirectionality))
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
            if (lastGtuDir.equals(GTUDirectionality.DIR_PLUS))
            {
                if (lastLink.getEndNode().equals(link.getStartNode()))
                {
                    // -----> O ----->, GTU moves ---->
                    lastGtuDir = GTUDirectionality.DIR_PLUS;
                    lastNode = link.getEndNode();
                }
                else
                {
                    // -----> O <-----, GTU moves ---->
                    lastGtuDir = GTUDirectionality.DIR_MINUS;
                    lastNode = link.getEndNode();
                }
            }
            else
            {
                if (lastLink.getStartNode().equals(link.getStartNode()))
                {
                    // <----- O ----->, GTU moves ---->
                    lastNode = link.getStartNode();
                    lastGtuDir = GTUDirectionality.DIR_PLUS;
                }
                else
                {
                    // <----- O <-----, GTU moves ---->
                    lastNode = link.getStartNode();
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
                GTUDirectionality drivingDirection =
                        lastNode.equals(link.getStartNode()) ? GTUDirectionality.DIR_PLUS : GTUDirectionality.DIR_MINUS;
                if (link.equals(lastLink) || !link.getLinkType().isCompatible(gtu.getGTUType(), drivingDirection))
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
     * @param carFollowingModel CarFollowingModel; Car-following model to set.
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

}
