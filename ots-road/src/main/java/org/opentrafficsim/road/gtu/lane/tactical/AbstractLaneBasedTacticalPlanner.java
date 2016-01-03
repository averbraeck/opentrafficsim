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
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
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

    /** the strategic planner that has instantiated this tactical planner. */
    private final LaneBasedStrategicalPlanner strategicalPlanner;

    /**
     * @param strategicalPlanner the strategic planner that has instantiated this tactical planner
     */
    public AbstractLaneBasedTacticalPlanner(final LaneBasedStrategicalPlanner strategicalPlanner)
    {
        this.strategicalPlanner = strategicalPlanner;
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
    public Lane getReferenceLane(final LaneBasedGTU gtu) throws GTUException
    {
        Map<Lane, Length.Rel> positions = gtu.positions(gtu.getReference());
        for (Lane lane : positions.keySet())
        {
            double posSI = positions.get(lane).si;
            if (posSI >= 0.0 && posSI <= lane.getLength().si)
            {
                // TODO widest lane in case we are registered on more than one lane with the reference point
                return lane;
            }
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
     * @throws OTSGeometryException when there is a problem with the path construction
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    protected LanePathInfo buildLaneListForward(final LaneBasedGTU gtu, final Length.Rel maxHeadway)
        throws GTUException, OTSGeometryException, NetworkException
    {
        List<Lane> laneListForward = new ArrayList<>();
        Lane lane = getReferenceLane(gtu);
        Lane lastLane = lane;
        GTUDirectionality lastGtuDir = gtu.getLanes().get(lane);
        laneListForward.add(lane);
        Length.Rel lengthForward;
        Length.Rel position = gtu.position(lane, gtu.getReference());
        OTSLine3D path;
        if (lastGtuDir.equals(GTUDirectionality.DIR_PLUS))
        {
            lengthForward = lane.getLength().minus(position);
            path = lane.getCenterLine().extract(position, lane.getLength());
        }
        else
        {
            lengthForward = gtu.position(lane, gtu.getReference());
            path = lane.getCenterLine().extract(Length.Rel.ZERO, position).reverse();
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
            if (lanes.size() == 1)
            {
                lane = lanes.keySet().iterator().next();
            }
            else
            {
                // multiple next lanes; ask the strategical planner where to go
                // note: this is not necessarily a split; it could e.g. be a bike path on a road
                LinkDirection ld =
                    gtu.getStrategicalPlanner().nextLinkDirection(lane.getParentLink(), gtu.getLanes().get(lane));
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
                    path = OTSLine3D.concatenate(0.25, path, lane.getCenterLine());
                    lastGtuDir = GTUDirectionality.DIR_PLUS;
                }
                else
                {
                    // -----> O <-----, GTU moves ---->
                    path = OTSLine3D.concatenate(0.25, path, lane.getCenterLine().reverse());
                    lastGtuDir = GTUDirectionality.DIR_MINUS;
                }
            }
            else
            {
                if (lastLane.getParentLink().getStartNode().equals(lane.getParentLink().getStartNode()))
                {
                    // <----- O ----->, GTU moves ---->
                    path = OTSLine3D.concatenate(0.25, path, lane.getCenterLine());
                    lastGtuDir = GTUDirectionality.DIR_PLUS;
                }
                else
                {
                    // <----- O <-----, GTU moves ---->
                    path = OTSLine3D.concatenate(0.25, path, lane.getCenterLine().reverse());
                    lastGtuDir = GTUDirectionality.DIR_MINUS;
                }
            }
            lastLane = lane;
        }
        return new LanePathInfo(path, laneListForward);
    }

    /**
     * Calculate the next location where the network splits, with a maximum headway relative to the reference point of the GTU.
     * @param gtu the gtu for which to calculate the lane list
     * @param maxHeadway the maximum length for which lanes should be returned
     * @return an instance that provides the following information for an operational plan: whether the network splits, the node
     *         where it splits, and the current lanes that lead to the right node after the split node.
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered
     * @throws OTSGeometryException when there is a problem with the path construction
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    protected NextSplitInfo determineNextSplit(final LaneBasedGTU gtu, final Length.Rel maxHeadway)
        throws GTUException, OTSGeometryException, NetworkException
    {
        OTSNode nextSplitNode = null;
        Set<Lane> correctCurrentLanes = new HashSet<>();
        Lane referenceLane = getReferenceLane(gtu);
        Link lastLink = referenceLane.getParentLink();
        GTUDirectionality lastGtuDir = gtu.getLanes().get(referenceLane);
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

            // see if we have a split
            if (links.size() > 1)
            {
                nextSplitNode = lastNode;
                LinkDirection ld = gtu.getStrategicalPlanner().nextLinkDirection(lastLink, lastGtuDir);
                // which lane(s) we are registered on and adjacent lanes link to a lane
                // that is on the route at the next split?
                for (CrossSectionElement cse : referenceLane.getParentLink().getCrossSectionElementList())
                {
                    if (cse instanceof Lane)
                    {
                        Lane l = (Lane) cse;
                        if (connectsToPath(l, gtu, nextSplitNode))
                        {
                            correctCurrentLanes.add(l);
                        }
                    }
                }
                System.out.println("Split - on lane " + referenceLane + "; good lanes: " + correctCurrentLanes);
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
     * Determine whether the lane is directly connected to our route, in other words: if we would (continue to) drive on the given
     * lane, can we take the right branch at the nextSplitNode without switching lanes?
     * @param lane the lane to examine
     * @param gtu the GTU for which we have to determine the lane suitability
     * @param nextSplitNode the node for which we examine the split
     * @return whether the lane is connected to our path
     */
    private boolean connectsToPath(final Lane lane, final LaneBasedGTU gtu, final OTSNode nextSplitNode)
    {
        return true;
    }

    /**
     * @return the strategicalPlanner
     */
    public final LaneBasedStrategicalPlanner getStrategicalPlanner()
    {
        return this.strategicalPlanner;
    }
}
