package org.opentrafficsim.road.gtu.lane.tactical;

import java.util.Map;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanBuilder;
import org.opentrafficsim.core.gtu.plan.tactical.TacticalPlanner;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.tactical.following.AccelerationStep;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.tactical.lanechange.LaneChangeModel;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
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
    private final LaneBasedStrategicalPlanner strategicalPlanner;

    /**
     * @param strategicalPlanner the strategic planner that has instantiated this tactical planner
     */
    public LaneBasedCFLCTacticalPlanner(final LaneBasedStrategicalPlanner strategicalPlanner)
    {
        this.strategicalPlanner = strategicalPlanner;
    }

    /** {@inheritDoc} */
    @Override
    public OperationalPlan generateOperationalPlan(final GTU gtu, final Time.Abs startTime,
        final DirectedPoint locationAtStartTime) throws NetworkException, GTUException
    {
        // ask Perception for the local situation
        LaneBasedGTU laneBasedGTU = (LaneBasedGTU) gtu;
        LanePerception perception = laneBasedGTU.getPerception();

        if (!perception.isInitialized())
        {
            perception.perceive();
        }

        // get some models to help us make a plan
        GTUFollowingModel gtuFollowingModel =
            this.strategicalPlanner.getDrivingCharacteristics().getGTUFollowingModel();
        LaneChangeModel laneChangeModel = this.strategicalPlanner.getDrivingCharacteristics().getLaneChangeModel();

        // look at the conditions for headway
        HeadwayGTU headwayGTU = perception.getForwardHeadwayGTU();
        AccelerationStep accelerationStep = null;
        try
        {
            if (headwayGTU.getOtherGTU() == null)
            {
                accelerationStep =
                    gtuFollowingModel.computeAccelerationWithNoLeader(laneBasedGTU, perception.getSpeedLimit());
            }
            else
            {
                // TODO do not use the velocity of the other GTU, but the PERCEIVED velocity
                accelerationStep =
                    gtuFollowingModel.computeAcceleration(laneBasedGTU, headwayGTU.getOtherGTU().getVelocity(),
                        headwayGTU.getDistance(), perception.getSpeedLimit());
            }
        }
        catch (NetworkException exception)
        {
            // brake if an error happens...
            OTSLine3D path = buildLanePathToFollowLane(laneBasedGTU, new Length.Rel(100.0, LengthUnit.METER));
            return OperationalPlanBuilder.buildStopPlan(path, startTime, laneBasedGTU.getVelocity(), new Acceleration(
                -1.0, AccelerationUnit.METER_PER_SECOND_2));
        }

        OTSLine3D path = buildLanePathToFollowLane(laneBasedGTU, new Length.Rel(100.0, LengthUnit.METER));
        if (accelerationStep.getAcceleration().si < 0.0)
        {
            return OperationalPlanBuilder.buildMaximumAccelerationPlan(path, startTime, gtu.getVelocity(), Speed.ZERO,
                gtu.getMaximumAcceleration(), gtu.getMaximumDeceleration());
        }
        else
        {
            return OperationalPlanBuilder.buildMaximumAccelerationPlan(path, startTime, gtu.getVelocity(),
                gtu.getMaximumVelocity(), gtu.getMaximumAcceleration(), gtu.getMaximumDeceleration());
        }
    }

    /**
     * @param gtu the GTU to generate the path for
     * @param distance the length of the line to be generated
     * @return a driving line for the next 'distance' meters
     * @throws NetworkException when the driving path cannot be generated
     */
    private OTSLine3D buildLanePathToFollowLane(final LaneBasedGTU gtu, final Length.Rel distance)
        throws NetworkException
    {
        Map<Lane, Length.Rel> positions = gtu.positions(gtu.getReference());
        for (Lane lane : positions.keySet())
        {
            double posSI = positions.get(lane).si;
            if (Math.abs(posSI - lane.getLength().si) < 0.00001) // TODO DELTA SHOULD BE BASED ON ulp()
            {
                posSI = lane.getLength().si;
            }
            if (posSI >= 0.0 && posSI <= lane.getLength().si)
            {
                try
                {
                    // this lane is a good base
                    OTSLine3D path = null;

                    if (gtu.getLanes().get(lane).equals(GTUDirectionality.DIR_PLUS) && posSI != positions.get(lane).si)
                    {
                        // if we are at the end, don't create to avoid a degenerate line...
                        path = lane.getCenterLine().extractFractional(posSI / lane.getParentLink().getLength().si, 1.0);
                    }
                    else if (gtu.getLanes().get(lane).equals(GTUDirectionality.DIR_MINUS) && posSI != 0.0)
                    {
                        // if we are at the start, don't create, don't create to avoid a degenerate line...
                        path =
                            lane.getCenterLine().extractFractional(0.0, posSI / lane.getParentLink().getLength().si)
                                .reverse();
                        path = path.reverse();
                    }
                    while (path == null || path.getLength().si < distance.si)
                    {
                        Map<Lane, GTUDirectionality> nextLanes = lane.nextLanes(gtu.getGTUType());
                        if (nextLanes.size() == 1)
                        {
                            if (path == null)
                            {
                                path = nextLanes.keySet().iterator().next().getCenterLine();
                            }
                            else
                            {
                                path = concat(path, nextLanes.keySet().iterator().next().getCenterLine());
                            }
                        }
                        else
                        {
                            // TODO
                            if (path == null)
                            {
                                path = nextLanes.keySet().iterator().next().getCenterLine();
                            }
                            else
                            {
                                path = concat(path, nextLanes.keySet().iterator().next().getCenterLine());
                            }
                        }
                    }
                    return path;
                }
                catch (OTSGeometryException geometryException)
                {
                    throw new NetworkException(geometryException);
                }
            }
        }

        // we could not find a lane to continue driving on
        throw new NetworkException("GTU " + gtu + " could not find a lane with its reference point on it among lanes: "
            + positions.keySet());
    }

    private OTSLine3D concat(OTSLine3D line1, OTSLine3D line2) throws OTSGeometryException
    {
        if (line1.getLast().equals(line2.getFirst()))
        {
            return OTSLine3D.concatenate(line1, line2);
        }
        if (line1.getLast().equals(line2.getLast()))
        {
            return OTSLine3D.concatenate(line1, line2.reverse());
        }
        throw new OTSGeometryException("OTSLine3D concat - Lane center lines cannot be connected");
    }

    // /**
    // * Build a set of Lanes that is adjacent to the given lane that this GTU can enter, for both lateral directions.
    // * @param lane Lane; the lane for which to add the accessible lanes.
    // */
    // private void addAccessibleAdjacentLanes(final Lane lane)
    // {
    // EnumMap<LateralDirectionality, Set<Lane>> adjacentMap = new EnumMap<>(LateralDirectionality.class);
    // for (LateralDirectionality lateralDirection : LateralDirectionality.values())
    // {
    // Set<Lane> adjacentLanes = new HashSet<Lane>(1);
    // adjacentLanes.addAll(lane.accessibleAdjacentLanes(lateralDirection, getGTUType()));
    // adjacentMap.put(lateralDirection, adjacentLanes);
    // }
    // this.accessibleAdjacentLanes.put(lane, adjacentMap);
    // }
    //
    // /**
    // * Remove the set of adjacent lanes when we leave the lane.
    // * @param lane Lane; the lane for which to remove the accessible lanes.
    // */
    // private void removeAccessibleAdjacentLanes(final Lane lane)
    // {
    // this.accessibleAdjacentLanes.remove(lane);
    // }

}
