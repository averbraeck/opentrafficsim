package org.opentrafficsim.road.gtu.lane.tactical;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.plan.tactical.TacticalPlanner;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkDirection;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * A lane-based tactical planner generates an operational plan for the lane-based GTU. It can ask the strategic planner for
 * assistance on the route to take when the network splits. This abstract class contains a number of helper methods that make it
 * easy to implement a tactical planner.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 25, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractLaneBasedTacticalPlanner implements TacticalPlanner
{
    /** */
    private static final long serialVersionUID = 20151125L;

    /**
     * Instantiated a tactical planner.
     */
    public AbstractLaneBasedTacticalPlanner()
    {
        super();
    }

    /**
     * The reference lane is the widest lane on which the reference point of the GTU is fully registered. If the reference point
     * is not fully registered on one of the lanes, return a lane where the reference point is not fully registered as a
     * fallback option. This can g=for instance happen when the GTU has just been generated, or when the GTU is about to be
     * destroyed at the end of a lane.
     * @param gtu the GTU for which to determine the lane on which the GTU's reference point lies
     * @return the widest lane on which the reference point lies between start and end, or any lane where the GTU is registered
     *         as a fallback option.
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
                return lane;
            }
        }

        // TODO lane closest to length or 0
        System.err.println(gtu + " does not have a reference lane with pos between 0 and length...");
        if (gtu.getLanes().size() > 0)
        {
            return gtu.getLanes().keySet().iterator().next();
        }

        throw new GTUException("The reference point of GTU " + gtu
            + " is not on any of the lanes on which it is registered");
    }

    /**
     * Build a list of lanes forward, with a maximum headway relative to the reference point of the GTU.
     * @param gtu the gtu for which to calculate the lane list
     * @param maxHeadway the maximum length for which lanes should be returned
     * @return an instance that provides the following information for an operational plan: the lanes to follow, and the path to
     *         follow when staying on the same lane.
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    public static LanePathInfo buildLaneListForward(final LaneBasedGTU gtu, final Length.Rel maxHeadway)
        throws GTUException, NetworkException
    {
        Lane startLane = getReferenceLane(gtu);
        Length.Rel startPosition = gtu.position(startLane, gtu.getReference());
        return buildLaneListForward(gtu, startLane, startPosition, maxHeadway);
    }

    /**
     * Build a list of lanes forward, with a maximum headway relative to the reference point of the GTU.
     * @param gtu the gtu for which to calculate the lane list
     * @param startLane the lane in which the path starts
     * @param startPosition the start position on the start lane with the Vehicle's reference point
     * @param maxHeadway the maximum length for which lanes should be returned
     * @return an instance that provides the following information for an operational plan: the lanes to follow, and the path to
     *         follow when staying on the same lane.
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    public static LanePathInfo buildLaneListForward(final LaneBasedGTU gtu, final Lane startLane,
        final Length.Rel startPosition, final Length.Rel maxHeadway) throws GTUException, NetworkException
    {
        return buildLaneListForward(gtu, maxHeadway, startLane, startLane.fraction(startPosition),
            gtu.getLanes().get(startLane));
    }

    /**
     * Build a list of lanes forward, with a maximum headway relative to the reference point of the GTU.
     * @param gtu the gtu for which to calculate the lane list
     * @param startLane the lane in which the path starts
     * @param startLaneFractionalPosition the fractional position on the start lane
     * @param startDirectionality the driving direction on the start lane
     * @param maxHeadway the maximum length for which lanes should be returned
     * @return an instance that provides the following information for an operational plan: the lanes to follow, and the path to
     *         follow when staying on the same lane.
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    public static LanePathInfo buildLaneListForward(final LaneBasedGTU gtu, final Length.Rel maxHeadway,
        final Lane startLane, final double startLaneFractionalPosition, final GTUDirectionality startDirectionality)
        throws GTUException, NetworkException
    {
        List<Lane> laneListForward = new ArrayList<>();
        Lane lane = startLane;
        GTUDirectionality lastGtuDir = startDirectionality;
        Length.Rel position = lane.position(startLaneFractionalPosition);
        Lane lastLane = lane;
        laneListForward.add(lane);
        Length.Rel lengthForward;
        OTSLine3D path;
        try
        {
            if (lastGtuDir.equals(GTUDirectionality.DIR_PLUS))
            {
                lengthForward = lane.getLength().minus(position);
                path = lane.getCenterLine().extract(position, lane.getLength());
            }
            else
            {
                lengthForward = position;
                path = lane.getCenterLine().extract(Length.Rel.ZERO, position).reverse();
            }
        }
        catch (OTSGeometryException exception)
        {
            System.err.println(gtu + ": " + exception.getMessage());
            System.err.println(lane + ", len=" + lane.getLength());
            System.err.println(position);
            throw new GTUException(exception);
        }

        while (lengthForward.lt(maxHeadway))
        {
            Map<Lane, GTUDirectionality> lanes =
                lastGtuDir.equals(GTUDirectionality.DIR_PLUS) ? lane.nextLanes(gtu.getGTUType()) : lane.prevLanes(gtu
                    .getGTUType());
            if (lanes.size() == 0)
            {
                // dead end. return with the list as is.
                return new LanePathInfo(path, laneListForward);
            }
            else
            {
                // multiple next lanes; ask the strategical planner where to go
                // note: this is not necessarily a split; it could e.g. be a bike path on a road
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
                    return new LanePathInfo(path, laneListForward);
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
                    return new LanePathInfo(path, laneListForward);
                }
                // otherwise: continue!
                lane = newLane;
            }
            laneListForward.add(lane);
            lengthForward = lengthForward.plus(lane.getLength());

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

        }
        return new LanePathInfo(path, laneListForward);
    }

    /**
     * Build a list of lanes forward, with a maximum headway.
     * @param gtu the gtu for which to calculate the lane list
     * @param maxHeadway the maximum length for which lanes should be returned
     * @param startLane the first lane in the list
     * @param startLaneFractionalPosition the fractional position on the start lane
     * @param startDirectionality the driving direction on the start lane
     * @return a list of lanes, connected to the startLane and following the path of the StrategicalPlanner.
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    // TODO this method can probably disappear (lots of duplicated code)
    protected static List<Lane> buildLaneListForwardXXX(final LaneBasedGTU gtu, final Length.Rel maxHeadway,
        final Lane startLane, final double startLaneFractionalPosition, final GTUDirectionality startDirectionality)
        throws GTUException, NetworkException
    {
        List<Lane> laneListForward = new ArrayList<>();
        Lane lane = startLane;
        Lane lastLane = startLane;
        GTUDirectionality lastGtuDir = startDirectionality;
        laneListForward.add(lane);
        Length.Rel position = lane.position(startLaneFractionalPosition);
        Length.Rel lengthForward =
            lastGtuDir.equals(GTUDirectionality.DIR_PLUS) ? lane.getLength().minus(position) : position;

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
     * @param gtu the gtu for which to calculate the lane list
     * @param maxHeadway the maximum length for which lanes should be returned
     * @return an instance that provides the following information for an operational plan: whether the network splits, the node
     *         where it splits, and the current lanes that lead to the right node after the split node.
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    public static NextSplitInfo determineNextSplit(final LaneBasedGTU gtu, final Length.Rel maxHeadway)
        throws GTUException, NetworkException
    {
        OTSNode nextSplitNode = null;
        Set<Lane> correctCurrentLanes = new HashSet<>();
        Lane referenceLane = getReferenceLane(gtu);
        Link lastLink = referenceLane.getParentLink();
        GTUDirectionality lastGtuDir = gtu.getLanes().get(referenceLane);
        GTUDirectionality referenceLaneDirectionality = lastGtuDir;
        Length.Rel lengthForward;
        Length.Rel position = gtu.position(referenceLane, gtu.getReference());
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
                LinkDirection ld =
                    gtu.getStrategicalPlanner().nextLinkDirection(nextSplitNode, lastLink, gtu.getGTUType());
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
                LinkDirection ld =
                    gtu.getStrategicalPlanner().nextLinkDirection(nextSplitNode, lastLink, gtu.getGTUType());
                // which lane(s) we are registered on and adjacent lanes link to a lane
                // that is on the route at the next split?
                for (CrossSectionElement cse : referenceLane.getParentLink().getCrossSectionElementList())
                {
                    if (cse instanceof Lane)
                    {
                        Lane l = (Lane) cse;
                        if (connectsToPath(gtu, maxHeadway, l, referenceLaneFractionalPosition,
                            referenceLaneDirectionality, ld.getLink()))
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
     * @param gtu the GTU for which we have to determine the lane suitability
     * @param maxHeadway the maximum length for use in the calculation
     * @param startLane the first lane in the list
     * @param startLaneFractionalPosition the fractional position on the start lane
     * @param startDirectionality the driving direction on the start lane
     * @param linkAfterSplit the link after the split to which we should connect
     * @return whether the lane is connected to our path
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    protected static boolean
        connectsToPath(final LaneBasedGTU gtu, final Length.Rel maxHeadway, final Lane startLane,
            final double startLaneFractionalPosition, final GTUDirectionality startDirectionality,
            final Link linkAfterSplit) throws GTUException, NetworkException
    {
        List<Lane> lanes =
            buildLaneListForward(gtu, maxHeadway, startLane, startLaneFractionalPosition, startDirectionality)
                .getLaneList();
        for (Lane lane : lanes)
        {
            if (lane.getParentLink().equals(linkAfterSplit))
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
     * @param startLaneFractionalPosition the fractional position on the start lane
     * @param startDirectionality the driving direction on the start lane
     * @return whether the lane is connected to our path
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    protected static boolean noLaneDrop(final LaneBasedGTU gtu, final Length.Rel maxHeadway, final Lane startLane,
        final double startLaneFractionalPosition, final GTUDirectionality startDirectionality) throws GTUException,
        NetworkException
    {
        LanePathInfo lpi =
            buildLaneListForward(gtu, maxHeadway, startLane, startLaneFractionalPosition, startDirectionality);
        if (lpi.getPath().getLength().lt(maxHeadway))
        {
            return false;
        }
        return true;
    }

    /**
     * Make a list of links on which to drive next, with a maximum headway relative to the reference point of the GTU.
     * @param gtu the gtu for which to calculate the link list
     * @param maxHeadway the maximum length for which links should be returned
     * @return a list of links on which to drive next
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    protected static List<LinkDirection> buildLinkListForward(final LaneBasedGTU gtu, final Length.Rel maxHeadway)
        throws GTUException, NetworkException
    {
        List<LinkDirection> linkList = new ArrayList<>();
        Lane referenceLane = getReferenceLane(gtu);
        Link lastLink = referenceLane.getParentLink();
        GTUDirectionality lastGtuDir = gtu.getLanes().get(referenceLane);
        linkList.add(new LinkDirection(lastLink, lastGtuDir));
        Length.Rel lengthForward;
        Length.Rel position = gtu.position(referenceLane, gtu.getReference());
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
                LinkDirection ld =
                    gtu.getStrategicalPlanner().nextLinkDirection(lastLink, lastGtuDir, gtu.getGTUType());
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
}
