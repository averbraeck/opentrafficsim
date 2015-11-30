package org.opentrafficsim.road.gtu.lane.tactical;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.strategical.StrategicalPlanner;
import org.opentrafficsim.core.gtu.plan.tactical.TacticalPlanner;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.lanechange.LaneChangeModel;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Lane-based tactical planner that implements car following and lane change behavior. This tactical planner accepts a car
 * following model and a lane change model and will generate an operational plan for the GTU.
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
public class LaneBasedCFLCTacticalPlanner implements TacticalPlanner
{
    /** */
    private static final long serialVersionUID = 20151125L;

    /** the strategic planner that has instantiated this tactical planner. */
    private final StrategicalPlanner strategicalPlanner;

    /**
     * The adjacent lanes that are accessible for this GTU per lane where the GTU drives. This information is cached, because it
     * might be requested multiple times. The set of lanes is stored per LateralDirectionality (LEFT, RIGHT).
     */
    private final Map<Lane, EnumMap<LateralDirectionality, Set<Lane>>> accessibleAdjacentLanes = new HashMap<>();

    /**
     * @param strategicalPlanner the strategic planner that has instantiated this tactical planner
     */
    public LaneBasedCFLCTacticalPlanner(final StrategicalPlanner strategicalPlanner)
    {
        this.strategicalPlanner = strategicalPlanner;
    }

    /** {@inheritDoc} */
    @Override
    public OperationalPlan generateOperationalPlan(final GTU gtu, final Time.Abs startTime,
        final DirectedPoint locationAtStartTime)
    {
        // ask Perception for the local situation
        LaneBasedGTU laneBasedGTU = (LaneBasedGTU) gtu;
        LanePerception perception = laneBasedGTU.getPerception();
        
        
        
        return null;
    }

    
    
    
    
    
    

//    /**
//     * Build a set of Lanes that is adjacent to the given lane that this GTU can enter, for both lateral directions.
//     * @param lane Lane; the lane for which to add the accessible lanes.
//     */
//    private void addAccessibleAdjacentLanes(final Lane lane)
//    {
//        EnumMap<LateralDirectionality, Set<Lane>> adjacentMap = new EnumMap<>(LateralDirectionality.class);
//        for (LateralDirectionality lateralDirection : LateralDirectionality.values())
//        {
//            Set<Lane> adjacentLanes = new HashSet<Lane>(1);
//            adjacentLanes.addAll(lane.accessibleAdjacentLanes(lateralDirection, getGTUType()));
//            adjacentMap.put(lateralDirection, adjacentLanes);
//        }
//        this.accessibleAdjacentLanes.put(lane, adjacentMap);
//    }
//
//    /**
//     * Remove the set of adjacent lanes when we leave the lane.
//     * @param lane Lane; the lane for which to remove the accessible lanes.
//     */
//    private void removeAccessibleAdjacentLanes(final Lane lane)
//    {
//        this.accessibleAdjacentLanes.remove(lane);
//    }


}
