package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.exclusive;

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
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.InfrastructureContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.following.MirovaCarFollowingUtil;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * A dedicated Maneuver Pattern for executing a simple, direct lane change.
 * <p>
 * This pattern represents a Finite State Machine (FSM) in <b>Layer 4 (Procedure & Action)</b>.
 * It is typically invoked when a lane change decision has been finalized and safety has been verified.
 * It manages the physical transition between lanes, including speed adaptation to target leaders.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class SimpleLaneChangePattern extends ManeuverPattern
{
    /** The target direction for the lane change. */
    private LateralDirectionality targetDirection = LateralDirectionality.NONE;

    /**
     * Constructs a new SimpleLaneChangePattern.
     *
     * @param vehicle the tactical planner associated with the ego vehicle
     */
    public SimpleLaneChangePattern(final MirovaTacticalPlanner vehicle)
    {
        super(PatternType.EXCLUSIVE, vehicle);
        this.targetDirection = this.vehicle.getLaneChangeDesire().dominantDirection();
        this.initialActionState = () -> new PerformLaneChangeState(this);
        this.requiredContextKeys.add("Ego");
        this.requiredContextKeys.add("Neighbors");
        this.requiredContextKeys.add("Infrastructure");
    }

    /**
     * Prepares the pattern for a specific direction.
     *
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
            // Trigger if discretionary desire exceeds the threshold
            return this.vehicle.getLaneChangeDesire().magnitude() >= this.vehicle.getParameters()
                    .getParameter(MirovaParameters.DFREE);
        }
        catch (ParameterException exception)
        {
            return false;
        }
    }

    @Override
    public boolean checkAbility() throws ParameterException
    {
        this.targetDirection = this.vehicle.getLaneChangeDesire().dominantDirection();
        NeighborsContext neigh = this.vehicle.getContext(NeighborsContext.class);

        try
        {
            // Verify if the gap and legal conditions allow for a safe transition
            return (this.targetDirection.isLeft() || this.targetDirection.isRight())
                    && neigh.getIfLaneChangePossible(this.targetDirection);
        }
        catch (GtuException | NetworkException exception)
        {
            return false;
        }
    }

    /* =========================================================================================
     * STATE: PERFORM_LANE_CHANGE
     * ========================================================================================= */

    /**
     * Action state responsible for the actual lateral movement and longitudinal synchronization.
     */
    public static class PerformLaneChangeState extends ActionState
    {
        /** Target direction of the lane change. */
        private final LateralDirectionality direction;

        /** Origin lane used to detect when the vehicle has fully crossed over. */
        private final Lane originLane;

        /** Flag to prevent starting the move if speed is too low or gaps closed in the last micro-tick. */
        private Boolean startCondition = true;

        /** Indicates if a slower lane change duration is used (congested mode). */
        private boolean slowLaneChange = false;

        /**
         * Constructor using the dominant desire direction.
         *
         * @param p the parent maneuver pattern
         */
        public PerformLaneChangeState(final ManeuverPattern p)
        {
            this(p, p.getMirovaTacticalPlanner().getLaneChangeDesire().dominantDirection());
        }

        /**
         * Constructor for a specific direction.
         *
         * @param p         the parent maneuver pattern
         * @param direction the lateral direction
         */
        public PerformLaneChangeState(final ManeuverPattern p, final LateralDirectionality direction)
        {
            super(p);
            this.direction = direction;
            this.originLane = this.vehicle.getGtu().getLane();

            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            if (ego.getEgoSpeed().si < 7.0)
            {
                this.slowLaneChange = true;
                try
                {
                    // Use longer duration for congested merging efficiency
                    this.vehicle.getParameters().setParameterResettable(ParameterTypes.LCDUR,
                            this.vehicle.getParameters().getParameter(MirovaParameters.congestedLaneChangeDuration));
                }
                catch (ParameterException exception)
                {
                    exception.printStackTrace();
                }
            }
        }

        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException
        {
            this.vehicle.setRunningManeuver(true);
            this.maneuverPattern.setRunning(true);

            InfrastructureContext infraCtx = this.vehicle.getContext(InfrastructureContext.class);
            NeighborsContext neighborsCtx = this.vehicle.getContext(NeighborsContext.class);
            EgoContext egoCtx = this.vehicle.getContext(EgoContext.class);

            Speed egoSpeed = egoCtx.getEgoSpeed();
            Parameters params = this.vehicle.getParameters();

            // Adjust target headway during the maneuver for increased safety/compaction
            this.vehicle.setTargetDesiredHeadway(params.getParameter(ParameterTypes.T).times(
                    params.getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange)));

            // Base acceleration from current lane car-following
            Acceleration minAcc = egoCtx.getCurrentCarFollowingAcceleration();

            // Synchronize with leader on the target lane
            if (this.vehicle.getGtu().getLane().equals(this.originLane))
            {
                HeadwayGtu targetLeader = neighborsCtx.getLeader(this.direction);
                if (targetLeader != null)
                {
                    Acceleration aTarget = MirovaCarFollowingUtil.followSingleLeader(this.vehicle, targetLeader);
                    minAcc = Acceleration.min(minAcc, aTarget);
                }
            }

            SimpleOperationalPlan plan = new SimpleOperationalPlan(minAcc,
                    this.maneuverPattern.getPatternSpecificTimestep(), this.direction);

            // Safety check before initiating lateral move
            if (!this.vehicle.getLaneChange().isChangingLane())
            {
                Speed resultingSpeed = egoSpeed.plus(minAcc.times(this.maneuverPattern.getPatternSpecificTimestep()));
                this.startCondition = (resultingSpeed.gt(Speed.instantiateSI(5.0))
                        || neighborsCtx.getIfLaneChangePossible(this.direction));
            }

            if (!this.startCondition)
            {
                plan = new SimpleOperationalPlan(minAcc, this.maneuverPattern.getPatternSpecificTimestep(),
                        LateralDirectionality.NONE);
            }

            // Set turn indicators
            if (this.direction.isLeft())
                plan.setIndicatorIntentLeft();
            else if (this.direction.isRight())
                plan.setIndicatorIntentRight();

            return plan;
        }

        @Override
        public SimpleOperationalPlan next()
                throws ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException
        {
            // Pattern completes when the vehicle is no longer laterally moving and has reached a new lane
            boolean finished = !this.vehicle.getLaneChange().isChangingLane()
                    && !this.originLane.equals(this.vehicle.getGtu().getLane());

            if (finished)
            {
                if (this.slowLaneChange)
                {
                    this.vehicle.getParameters().resetParameter(ParameterTypes.LCDUR);
                }
                return finishManeuver();
            }
            return null;
        }

        @Override
        public SimpleOperationalPlan abort() throws ParameterException, GtuException, NetworkException
        {
            // If the start condition failed before the move began, terminate the pattern
            if (!this.startCondition)
            {
                if (this.slowLaneChange)
                {
                    this.vehicle.getParameters().resetParameter(ParameterTypes.LCDUR);
                }
                return finishManeuver();
            }
            return null;
        }

        @Override
        public double getUtility()
        {
            // Utility can be based on the lane change desire magnitude, with a small penalty for slow maneuvers
            Desire desire = this.maneuverPattern.getMirovaTacticalPlanner().getLaneChangeDesire();
            double baseUtility = desire.getDirectionalDesire(this.direction);
            return this.slowLaneChange ? baseUtility * 0.8 : baseUtility;
        }

        @Override
        public String toString()
        {
            return "PerformLaneChangeState[" + this.direction + "]";
        }
    }
}