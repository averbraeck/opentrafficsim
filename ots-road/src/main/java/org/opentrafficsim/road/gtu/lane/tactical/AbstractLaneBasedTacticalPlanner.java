package org.opentrafficsim.road.gtu.lane.tactical;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.plan.tactical.TacticalPlanner;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkDirection;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Lane-based tactical planner that can generate an operational plan for the lane-based GTU.
 * <p>
 * This lane-based tactical planner makes decisions based on headway (GTU following model) and lane change (Lane Change model).
 * It can ask the strategic planner for assistance on the overarching route to take.
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
     * @param gtu the GTU for which to retermine the lane on which the GTU's reference point lies
     * @return a lane on which the reference point is between start and end.
     * @throws GTUException when the reference point of the GTU is not on any of the lanes on which it is registered
     */
    protected Lane getReferenceLane(final LaneBasedGTU gtu) throws GTUException
    {
        Map<Lane, Length.Rel> positions = gtu.positions(gtu.getReference());
        for (Lane lane : positions.keySet())
        {
            double posSI = positions.get(lane).si;
            if (posSI >= 0.0 && posSI <= lane.getLength().si)
            {
                return lane;
            }
        }
        throw new GTUException("The reference point of GTU " + gtu
            + " is not on any of the lanes on which it is registered");
    }

    /**
     * Build a list of lanes forward, with a maximum headway relative to the reference point of the GTU.
     * @param gtu the gtu for shich to calculate the lane list
     * @param maxHeadway the maximum length for which lanes should be returned
     * @return thhe path to follow when staying in the same lane
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered
     * @throws OTSGeometryException when there is a problem with the path construction
     * @throws NetworkException when the strategic planner is not able to return a next node in the route
     */
    protected OTSLine3D buildLaneListForward(final LaneBasedGTU gtu, final Length.Rel maxHeadway) throws GTUException,
        OTSGeometryException, NetworkException
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
                return path;
            }
            if (lanes.size() == 1)
            {
                lane = lanes.keySet().iterator().next();
            }
            else
            {
                // multiple next lanes; ask the strategical planner where to go
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
                    path = OTSLine3D.concatenate(path, lane.getCenterLine());
                    lastGtuDir = GTUDirectionality.DIR_PLUS;
                }
                else
                {
                    // -----> O <-----, GTU moves ---->
                    path = OTSLine3D.concatenate(path, lane.getCenterLine().reverse());
                    lastGtuDir = GTUDirectionality.DIR_MINUS;
                }
            }
            else
            {
                if (lastLane.getParentLink().getStartNode().equals(lane.getParentLink().getStartNode()))
                {
                    // <----- O ----->, GTU moves ---->
                    path = OTSLine3D.concatenate(path, lane.getCenterLine());
                    lastGtuDir = GTUDirectionality.DIR_PLUS;
                }
                else
                {
                    // <----- O <-----, GTU moves ---->
                    path = OTSLine3D.concatenate(path, lane.getCenterLine().reverse());
                    lastGtuDir = GTUDirectionality.DIR_MINUS;
                }
            }
            lastLane = lane;
        }
        return path;
    }

    /**
     * @return the strategicalPlanner
     */
    public final LaneBasedStrategicalPlanner getStrategicalPlanner()
    {
        return this.strategicalPlanner;
    }
}
