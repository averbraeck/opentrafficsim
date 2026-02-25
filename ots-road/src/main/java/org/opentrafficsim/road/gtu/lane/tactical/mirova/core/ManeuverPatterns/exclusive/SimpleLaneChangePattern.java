package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.exclusive;

import static org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters.DMAND;

import java.util.Set;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks.KnowledgeChunk;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.DiscretionaryLaneChangePatternOld.ActionStateCompleteLaneChange;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.InfrastructureContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters;

/**
 * A dedicated Maneuver Pattern for executing a simple lane change.
 * <p>
 * This pattern is typically invoked via delegation from a tactical pattern (e.g. AutobahnFreeDriving)
 * when a lane change decision has been made and a gap found.
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class SimpleLaneChangePattern extends ManeuverPattern
{
    /** The target direction for the lane change. Defaults to NONE until set. */
    private LateralDirectionality targetDirection = LateralDirectionality.NONE;

    /** State to indicate the pattern has completed its job. */
    private boolean finished = false;

    /**
     * Constructor.
     * @param knowledgeChunk The context chunk (can be null/dummy if strictly used for delegation).
     */
    public SimpleLaneChangePattern(final MirovaTacticalPlanner vehicle)
    {
        super(PatternType.EXCLUSIVE, vehicle);
        this.targetDirection = this.vehicle.getLaneChangeDesire().dominantDirection();
        this.initialActionState = () -> new PerformLaneChangeState(this);
        // Required context: None specific, assumes caller checked feasibility
        this.requiredContextKeys.add("Ego");
    }

    /**
     * Prepares the pattern for a specific direction.
     * Should be called by the Planner before setting this as active.
     * @param direction LateralDirectionality (LEFT or RIGHT)
     */
    public void setLaneChangeDirection(final LateralDirectionality direction)
    {
        this.targetDirection = direction;

    }

    @Override
    public boolean checkContext() throws ParameterException
    {
    try
    {
        InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
        EgoContext ego = this.vehicle.getContext(EgoContext.class);
        Speed speedLimit = infra.getLegalSpeedLimit();
        Speed egoSpeed = ego.getEgoSpeed();
        if (this.vehicle.getLaneChangeDesire().magnitude() >= this.vehicle.getParameters().getParameter(MirovaParameters.DFREE)
               // && egoSpeed.gt(speedLimit.times(0.25))
                ){
            return true;
        }
    }
    catch (ParameterException exception)
    {
        exception.printStackTrace();
    }

    return false; }

    @Override
    public boolean checkAbility() throws ParameterException
    {
        this.targetDirection = this.vehicle.getLaneChangeDesire().dominantDirection();
        NeighborsContext neigh = this.vehicle.getContext(NeighborsContext.class);

        try
        {
            if (neigh.getIfLaneChangePossible(this.targetDirection))
            {
                return true;
            }
        }
        catch (GtuException | NetworkException exception)
        {
            exception.printStackTrace();
        }
        return false;
    }


    public static class PerformLaneChangeState extends ActionState {

        /** Target direction of the lane change (LEFT or RIGHT). */
        private final LateralDirectionality direction;


        /** Cached origin lane to detect completion. */
        private final Lane originLane;

        private Boolean startCondition = true;

        private boolean slowLaneChange = false;


        // ----------------------------------------------------------------------
        // Construction
        // ----------------------------------------------------------------------

        /** ActionStatePerformLaneChange constructor.
         * @param pattern
         * @param direction
         * @throws ParameterException
         */
        public PerformLaneChangeState(final ManeuverPattern p) {
            super(p);
            this.direction = this.vehicle.getLaneChangeDesire().dominantDirection();
            this.originLane = this.vehicle.getGtu().getLane();

            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            if (ego.getEgoSpeed().si < 7.0) {
                this.slowLaneChange = true;
                // Reduce the lane change duration to 1.5s for more efficient merging in congested conditions.
                try
                {
                    this.vehicle.getParameters().setParameterResettable(ParameterTypes.LCDUR, this.vehicle.getParameters().getParameter(MirovaParameters.congestedLaneChangeDuration));
                }
                catch (ParameterException exception)
                {
                    exception.printStackTrace();
                }
            }
        }

        /** ActionStatePerformLaneChange constructor.
         * @param pattern
         * @param direction
         */
        public PerformLaneChangeState(final ManeuverPattern p, final LateralDirectionality direction) {
            super(p);
            this.direction = direction;
            this.originLane = this.vehicle.getGtu().getLane();

            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            if (ego.getEgoSpeed().si < 7.0) {
                this.slowLaneChange = true;
                // Reduce the lane change duration to 1.5s for more efficient merging in congested conditions.
                try
                {
                    this.vehicle.getParameters().setParameterResettable(ParameterTypes.LCDUR, this.vehicle.getParameters().getParameter(MirovaParameters.congestedLaneChangeDuration));
                }
                catch (ParameterException exception)
                {
                    exception.printStackTrace();
                }
            }
        }

        // ----------------------------------------------------------------------
        // Core control logic
        // ----------------------------------------------------------------------

        /**
         * Executes longitudinal control using a simplified Two-Leader Car-Following logic.
         * <p>
         * The ego vehicle simultaneously considers the leader on its current lane and
         * the leader on the target lane. The resulting acceleration is the most restrictive
         * (minimum) across these influences.
         * </p>
         * @throws NetworkException
         * @throws GtuException
         */
        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException {
            this.vehicle.setRunningManeuver(true);
            this.maneuverPattern.setRunning(true);
            this.maneuverPattern.setCurrentActionState(this);

            InfrastructureContext infraCtx = this.vehicle.getContext(InfrastructureContext.class);
            NeighborsContext neighborsCtx = this.vehicle.getContext(NeighborsContext.class);
            EgoContext egoCtx = this.vehicle.getContext(EgoContext.class);

            Speed egoSpeed = egoCtx.getEgoSpeed();
            Parameters params = this.vehicle.getGtu().getParameters();

            this.vehicle.setTargetDesiredHeadway(this.vehicle.getParameters().getParameter(ParameterTypes.T)
                    .times(this.vehicle.getParameters().getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange)));

            // Start with relaxed car-following acceleration (already includes Desire effects)
            Acceleration minAcc = egoCtx.getCurrentCarFollowingAcceleration();


            // Add target-lane leader constraint
            if (this.vehicle.getGtu().getLane() == this.originLane) {
                // Only consider target-lane leader if still on origin lane;
                HeadwayGtu targetLeader = neighborsCtx.getLeader(this.direction);
                if (targetLeader != null) {
                    Acceleration aTarget = CarFollowingUtil.followSingleLeader(
                            this.vehicle.getCarFollowingModel(),
                            params,
                            egoSpeed,
                            infraCtx.getCurrentSpeedLimit(),
                            targetLeader.getDistance(),
                            targetLeader.getSpeed());
                    minAcc = Acceleration.min(minAcc, aTarget);
                }
            }

            SimpleOperationalPlan plan = new SimpleOperationalPlan(
                    minAcc,
                    this.maneuverPattern.getPatternSpecificTimestep(),
                    this.direction);

            if (!this.vehicle.getLaneChange().isChangingLane()) {
                Speed resultingSpeed = egoSpeed.plus(minAcc.times(this.maneuverPattern.getPatternSpecificTimestep()));
                this.startCondition = (resultingSpeed.gt(Speed.instantiateSI(5.0)) || neighborsCtx.getIfLaneChangePossible(this.direction));
            }

            if (!this.startCondition) {
                plan = new SimpleOperationalPlan(
                        minAcc,
                        this.maneuverPattern.getPatternSpecificTimestep(),
                        LateralDirectionality.NONE);
            }

//            System.out.println("GTU " + this.vehicle.getGtu().getId() + " performing lane change to " + this.direction
//                    + " with lateral position " + this.vehicle.getGtu().getLateralPosition(this.vehicle.getGtu().getLane())
//                    + " with acceleration " + minAcc);

            if (this.direction == LateralDirectionality.LEFT) {
                plan.setIndicatorIntentLeft();
            } else if (this.direction == LateralDirectionality.RIGHT) {
                plan.setIndicatorIntentRight();
            }

            return plan;
            }

        // ----------------------------------------------------------------------
        // Transitions
        // ----------------------------------------------------------------------

        /**
         * Proceeds to {@link ActionStateCompleteLaneChange} when the lane change is completed.
         * @return
         * @throws NetworkException
         * @throws GtuException
         * @throws IllegalArgumentException
         * @throws NullPointerException
         */
        @Override
        public SimpleOperationalPlan next() throws ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException {
            boolean finished = !this.vehicle.getLaneChange().isChangingLane()
                    && !this.originLane.equals(this.vehicle.getGtu().getLane());

            if (finished) {
                if (this.slowLaneChange) {
                    this.vehicle.getParameters().resetParameter(ParameterTypes.LCDUR);
                }
                return finishManeuver();
            }
            return null;
        }

        /**
         * Checks whether the lane-change should be aborted (safety or desire violation).
         * @return
         * @throws NetworkException
         * @throws GtuException
         */
        @Override
        public SimpleOperationalPlan abort() throws ParameterException, GtuException, NetworkException {

            if (!this.startCondition) {
                if (this.slowLaneChange) {
                    this.vehicle.getParameters().resetParameter(ParameterTypes.LCDUR);
                }
                return finishManeuver();
            }
            return null;
        }

        @Override
        public String toString() {
            return "PerformLaneChangeState[" + this.direction + "]";
        }
}


}