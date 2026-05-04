package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns;

import java.util.Iterator;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.InfrastructureContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.MacroTrafficContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.InfrastructureContext.ScanDirection;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.following.MirovaCarFollowingUtil;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.exclusive.GapSearchPattern.BreakingEndOfRampState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.exclusive.GapSearchPattern.CongestedGapSearchState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.helpers.GapCandidate;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.helpers.HeuristicGapSelector;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Mandatory lane change pattern with long-range anticipation for merge scenarios.
 * <p>
 * This pattern extends the traditional gap search by adding an early anticipation phase. It actively looks up to
 * extendedLookAheadDistance ahead to determine the average speed in the merge area without globally increasing the continuous
 * car-following look-ahead, thereby preserving simulation performance. It implements a state machine transitioning from early
 * anticipation to active gap searching and execution.
 * </p>
 * <p>
 * Copyright (c) 2026 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class MandatoryLaneChangePattern extends ManeuverPattern
{
    /** The intended lateral direction for the maneuver. */
    private LateralDirectionality targetDirection;

    /** The currently targeted gap on the adjacent lane. */
    private GapCandidate activeGap;

    /** Buffer distance before the end of the lane where emergency braking is enforced. */
    public static final Length RAMP_END_BUFFER = Length.instantiateSI(10.0);

    /** Specific simulation time step for the execution of this maneuver. */
    private final Duration patternSpecificTimestep = Duration.instantiateSI(0.1);

    /** Distance threshold to transition from anticipation to active matching. */
    private static final Length ANTICIPATION_THRESHOLD = Length.instantiateSI(400.0);

    /**
     * Constructs a new MandatoryLaneChangePattern.
     * @param vehicle the tactical planner associated with the ego vehicle
     */
    public MandatoryLaneChangePattern(final MirovaTacticalPlanner vehicle)
    {
        super(PatternType.EXCLUSIVE, vehicle);
        // Start in the early anticipation state
        this.initialActionState = () -> new AnticipateMergeState(this);
        this.targetDirection = this.vehicle.getLaneChangeDesire().dominantDirection();
        this.requiredContextKeys.add("Ego");
        this.requiredContextKeys.add("Neighbors");
        this.requiredContextKeys.add("Infrastructure");
        this.requiredContextKeys.add("MacroTraffic");
    }

    /**
     * Gets the lateral direction of the target lane.
     * @return the target direction
     */
    public LateralDirectionality getTargetDirection()
    {
        return this.vehicle.getLaneChangeDesire().dominantDirection();
    }

    /**
     * Gets the currently active gap candidate.
     * @return the active gap
     */
    public GapCandidate getActiveGap()
    {
        return this.activeGap;
    }

    /**
     * Sets the currently active gap candidate.
     * @param gap the gap to target
     */
    public void setActiveGap(final GapCandidate gap)
    {
        this.activeGap = gap;
    }

    @Override
    public boolean checkContext()
    {
        try
        {
            // Activate earlier than the old GapSearchPattern!
            // E.g., trigger if there is a known merge ahead within the extended lookahead
            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
            Length distToMerge = infra.getDistanceToLaneChangeExtendedLookahead();

            // Trigger if within 1000m OR if standard desire is high
            boolean isApproachingMerge = distToMerge.si > 0 && distToMerge.si < this.vehicle.getParameters()
                    .getParameter(MirovaParameters.extendedLookAheadDistance).si;
            boolean isDesireHigh = this.vehicle.getLaneChangeDesire().magnitude() >= this.vehicle.getParameters()
                    .getParameter(MirovaParameters.DMAND);
            // Lowered threshold for early
            // activation

            return isApproachingMerge || isDesireHigh; // || isDesireHigh;
        }
        catch (Exception exception)
        {
            return false;
        }
    }

    @Override
    public boolean checkAbility()
    {
        return true; // Assume the vehicle is always able to perform the maneuver if the context is right
    }

    /*
     * ========================================================================================= 1) STATE: ANTICIPATE_MERGE
     * =========================================================================================
     */

    /**
     * Early state where the vehicle looks far ahead to determine the speed at the merge bottleneck and softly adapts its speed,
     * without actively forcing a gap search yet.
     */
    public static class AnticipateMergeState extends ActionState
    {
        /** Reference to the parent pattern for accessing shared data and parameters. */
        private final MandatoryLaneChangePattern pattern;

        /** Smoothed anticipated speed to prevent high frequency oscillations (low-pass filter). */
        private Speed smoothedMergeSpeed = null;

        /** Smoothing factor (alpha) for the Exponential Moving Average (EMA). 0.0 < alpha <= 1.0 */
        private double SPEED_SMOOTHING_FACTOR = 0.1;

        /**
         * Constructor for the anticipation state.
         * @param p the parent maneuver pattern
         */
        public AnticipateMergeState(final MandatoryLaneChangePattern p)
        {
            super(p);
            this.pattern = (MandatoryLaneChangePattern) p;
            this.active = true;
            this.maneuverPattern.setRunning(true);
            try
            {
                this.SPEED_SMOOTHING_FACTOR = this.vehicle.getParameters().getParameter(ParameterTypes.DT).si * 0.25;
            }
            catch (ParameterException exception)
            {
                exception.printStackTrace();
            }
        }

        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException
        {
            Acceleration criticalDecelThreshold = Acceleration.instantiateSI(-2.0); // Ggf. aus Parametern holen
            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
            Acceleration aCf = ego.getCurrentCarFollowingAcceleration();

            if (aCf.gt(criticalDecelThreshold))
            {
                Speed vEgo = ego.getEgoSpeed();
                Speed speedLimit = infra.getLegalSpeedLimit();
                if (vEgo.lt(speedLimit))
                {
                    Lane targetLane = infra.getDownstreamAdjacentLane(this.pattern.getTargetDirection());

                    if (targetLane != null)
                    {
                        Speed targetLaneSpeed = infra.getLaneAverageSpeed(targetLane, Length.instantiateSI(0.0),
                                Length.instantiateSI(150.0), 3, ScanDirection.FRONT_TO_BACK);
                        Speed targetSpeed = Speed.max(targetLaneSpeed, new Speed(20.0, SpeedUnit.KM_PER_HOUR));
                        targetSpeed = Speed.min(targetSpeed, speedLimit);
                        if (ego.getEgoSpeed().gt(targetSpeed))
                        {
                            Acceleration aToTarget = MirovaCarFollowingUtil.approachTargetSpeed(this.vehicle,
                                    Length.instantiateSI(10.0), targetSpeed);
                            Acceleration egoDecelThreshold =
                                    this.vehicle.getParameters().getParameter(MirovaParameters.egoDecelerationThreshold);
                            aToTarget = Acceleration.max(aToTarget, egoDecelThreshold);

                            return new SimpleOperationalPlan(aToTarget, this.pattern.patternSpecificTimestep);
                        }

                    }
                }
            }

            return new SimpleOperationalPlan(aCf, this.pattern.patternSpecificTimestep);

        }

        @Override
        public SimpleOperationalPlan next() throws ParameterException, OperationalPlanException, NetworkException, GtuException
        {
            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
            // 1. FIX: Den vergessenen ANTICIPATION_THRESHOLD anwenden!
            boolean isLaneAvailable = infra.getIfLaneAvailable(this.pattern.getTargetDirection());
            if (isLaneAvailable)
            {
                return transitionTo(new EvaluateTargetGapState(this.maneuverPattern));
            }

            return null; // Bleibe in der Antizipation, wenn noch weit weg
        }

        @Override
        public SimpleOperationalPlan abort() throws ParameterException, GtuException, NetworkException
        {
            try
            {
                InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
                if (infra.getDistanceToLaneChangeExtendedLookahead().si >= this.vehicle.getParameters()
                        .getParameter(MirovaParameters.extendedLookAheadDistance).si)
                {
                    return finishManeuver();
                }
            }
            catch (Exception exception)
            {
                return finishManeuver();
            }
            return null;
        }

        @Override
        public double getUtility()
        {
            return this.vehicle.getMandatoryLaneChangeDesire().magnitude();
        }

        @Override
        public String toString()
        {
            return "AnticipateMergeState";
        }
    }

    /*
     * ========================================================================================= STATE 1: EVALUATE TARGET GAP
     * =========================================================================================
     */

    /**
     * State that evaluates the target gap using a safety-first heuristic hierarchy.
     * <p>
     * It strictly evaluates kinematic constraints (Ego and Follower induced decelerations) before resolving spatial conflicts
     * (parallel vehicles). If decelerations are critical, it immediately routes the finite state machine to escape or brake,
     * rendering the parallel vehicle secondary until the speeds are synchronized.
     * </p>
     * <p>
     * Copyright (c) 2026 Marvin Baumann / KIT. All rights reserved. <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
     */
    public static class EvaluateTargetGapState extends ActionState
    {
        /** The parent mandatory lane change pattern. */
        private final MandatoryLaneChangePattern pattern;

        /** Time horizon in seconds to evaluate overtaking maneuvers. */
        private static final double TIME_HORIZON_S = 3.0;

        /**
         * Constructor for the evaluation state.
         * @param p the parent maneuver pattern
         */
        public EvaluateTargetGapState(final ManeuverPattern p)
        {
            super(p);
            this.pattern = (MandatoryLaneChangePattern) p;
            this.active = true;
        }

        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException
        {
            Acceleration criticalDecelThreshold = Acceleration.instantiateSI(-2.0); // Ggf. aus Parametern holen
            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
            Acceleration aCf = ego.getCurrentCarFollowingAcceleration();
            SimpleOperationalPlan plan = null;

            if (aCf.gt(criticalDecelThreshold))
            {
                Speed vEgo = ego.getEgoSpeed();
                Speed speedLimit = infra.getLegalSpeedLimit();
                if (vEgo.lt(speedLimit))
                {
                    MacroTrafficContext macro = this.vehicle.getContext(MacroTrafficContext.class);
                    RelativeLane targetRelativeLane =
                            (this.pattern.getTargetDirection().isLeft()) ? RelativeLane.LEFT : RelativeLane.RIGHT;
                    Speed targetLaneSpeed = macro.getAverageSpeed(targetRelativeLane);

                    Speed targetSpeed = Speed.min(targetLaneSpeed, speedLimit);
                    Acceleration aToTarget =
                            MirovaCarFollowingUtil.approachTargetSpeed(this.vehicle, Length.instantiateSI(10.0), targetSpeed);
                    plan = new SimpleOperationalPlan(aToTarget, this.pattern.patternSpecificTimestep);

                }
            }
            if (plan == null)
            {
                plan = new SimpleOperationalPlan(aCf, this.pattern.patternSpecificTimestep);
            }

            if (this.pattern.getTargetDirection().isLeft())
            {
                plan.setIndicatorIntentLeft();
            }
            else if (this.pattern.getTargetDirection().isRight())
            {
                plan.setIndicatorIntentRight();
            }

            return plan;

        }

        @Override
        public SimpleOperationalPlan next() throws ParameterException, OperationalPlanException, NetworkException, GtuException
        {
            NeighborsContext neigh = this.vehicle.getContext(NeighborsContext.class);
            LateralDirectionality dir = this.pattern.getTargetDirection();

            // 0. Physical execution check: If the gap is perfectly clear, execute immediately
            if (neigh.getIfLaneChangePossible(dir))
            {
                return transitionTo(new ExecuteLaneChangeState(this.maneuverPattern, dir));
            }

            Length distToLaneEnd = this.vehicle.getContext(InfrastructureContext.class).getDistanceToLaneEnd();
            // Notbremse, falls das Ende der Rampe unweigerlich näher rückt
            if (distToLaneEnd != null)
            {
                Acceleration requiredStopAccel =
                        MirovaCarFollowingUtil.stop(this.vehicle, distToLaneEnd.minus(RAMP_END_BUFFER));
                if (requiredStopAccel.si < -5.0)
                {
                    return transitionTo(new DecelEndOfRampState(this.maneuverPattern));
                }
            }

            // --> NEU: Übergang in den Congested Merge State bei zähfließendem Verkehr (< 15 km/h)
            Speed egoSpeed = this.vehicle.getContext(EgoContext.class).getEgoSpeed();
            if (egoSpeed.lt(new Speed(15.0, org.djunits.unit.SpeedUnit.KM_PER_HOUR)))
            {
                return transitionTo(new CongestedMergeState(this.maneuverPattern));
            }

            HeadwayGtu parallel = null;
            HeadwayGtu actualFollower = null;

            Iterable<HeadwayGtu> followers = neigh.getFollowers(dir);
            if (followers != null)
            {
                for (HeadwayGtu gtu : followers)
                {
                    if (gtu.getDistance().si < 0.0)
                    {
                        parallel = gtu;
                    }
                    else if (actualFollower == null)
                    {
                        actualFollower = gtu;
                        break; // Same logic as for leaders: Only the closest follower matters for kinematic safety
                    }
                }
            }

            if (actualFollower != null)
            {
                Acceleration followerInducedDecel = neigh.getGtuDeceleration(actualFollower);

                Parameters params = this.vehicle.getParameters();
                Acceleration followerDecelThreshold = params.getParameter(MirovaParameters.followerDecelerationThreshold);

                if (followerInducedDecel.si > followerDecelThreshold.si)
                {
                    // Follower decel is okay now, so we can focus on the leader
                    return transitionTo(new DownstreamMergeState(this.maneuverPattern));
                }
            }
            return null; // Boundaries safe, no parallel vehicle, waiting for LaneChangePossible
        }

        @Override
        public SimpleOperationalPlan abort()
        {
            try
            {
                if (this.vehicle.getLaneChangeDesire().magnitude() < this.vehicle.getParameters()
                        .getParameter(MirovaParameters.DMAND))
                {
                    return finishManeuver();
                }
            }
            catch (ParameterException | GtuException | NetworkException exception)
            {
                exception.printStackTrace();
            }
            return null;
        }

        @Override
        public double getUtility()
        {
            return this.vehicle.getMandatoryLaneChangeDesire().magnitude();
        }

        @Override
        public String toString()
        {
            return "EvaluateTargetGapState";
        }
    }

    /*
     * ========================================================================================= STATE 2: DOWNSTREAM MERGE STATE
     * (BRAKING) =========================================================================================
     */

    /**
     * State for resolving a downstream merge conflict.
     * <p>
     * This state is triggered when the ego vehicle is too fast for the target gap (EgoDecelerationThreshold is violated). It
     * overrides the standard car-following acceleration with a hard braking maneuver until the target leader can be safely
     * followed.
     * </p>
     * <p>
     * Copyright (c) 2026 Marvin Baumann / KIT. All rights reserved. <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
     */
    public static class DownstreamMergeState extends ActionState
    {
        /** The parent mandatory lane change pattern. */
        private final MandatoryLaneChangePattern pattern;

        /**
         * Constructor for the downstream merge state.
         * @param p the parent maneuver pattern
         */
        public DownstreamMergeState(final ManeuverPattern p)
        {
            super(p);
            this.pattern = (MandatoryLaneChangePattern) p;
            this.active = true;
        }

        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException
        {
            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            NeighborsContext neigh = this.vehicle.getContext(NeighborsContext.class);
            Acceleration aCf = ego.getCurrentCarFollowingAcceleration();

            // Apply hard braking, but if the car-following model demands even harder braking
            // (e.g., to avoid crashing into the ego-lane leader), we must respect that.
            Acceleration egoDecelThreshold =
                    this.vehicle.getParameters().getParameter(MirovaParameters.egoDecelerationThreshold);
            Acceleration inducedDecel = ego.getEgoDecelerationThreshold(this.pattern.getTargetDirection());
            HeadwayGtu adjacentLeader = neigh.getLeader(this.pattern.getTargetDirection());
            if (adjacentLeader != null)
            {
                Acceleration putativeLeaderAccel = MirovaCarFollowingUtil.followSingleLeader(this.vehicle, adjacentLeader);
                inducedDecel = Acceleration.max(inducedDecel, putativeLeaderAccel);
            }
            inducedDecel = Acceleration.max(inducedDecel, egoDecelThreshold);
            Acceleration finalAcc = Acceleration.min(aCf, inducedDecel);

            SimpleOperationalPlan plan = new SimpleOperationalPlan(finalAcc, this.pattern.patternSpecificTimestep);

            if (this.pattern.getTargetDirection().isLeft())
            {
                plan.setIndicatorIntentLeft();
            }
            else if (this.pattern.getTargetDirection().isRight())
            {
                plan.setIndicatorIntentRight();
            }

            return plan;
        }

        @Override
        public SimpleOperationalPlan next() throws ParameterException, OperationalPlanException, NetworkException, GtuException
        {
            NeighborsContext neigh = this.vehicle.getContext(NeighborsContext.class);
            LateralDirectionality dir = this.pattern.getTargetDirection();

            // 1. If the gap suddenly becomes perfectly clear, execute immediately
            if (neigh.getIfLaneChangePossible(dir))
            {
                return transitionTo(new ExecuteLaneChangeState(this.maneuverPattern, dir));
            }

            Length distToLaneEnd = this.vehicle.getContext(InfrastructureContext.class).getDistanceToLaneEnd();
            // Notbremse, falls das Ende der Rampe unweigerlich näher rückt
            if (distToLaneEnd != null)
            {
                Acceleration requiredStopAccel =
                        MirovaCarFollowingUtil.stop(this.vehicle, distToLaneEnd.minus(RAMP_END_BUFFER));
                if (requiredStopAccel.si < -5.0)
                {
                    return transitionTo(new DecelEndOfRampState(this.maneuverPattern));
                }
            }

            // --> NEU: Übergang in den Congested Merge State bei zähfließendem Verkehr (< 15 km/h)
            Speed egoSpeed = this.vehicle.getContext(EgoContext.class).getEgoSpeed();
            if (egoSpeed.lt(new Speed(15.0, org.djunits.unit.SpeedUnit.KM_PER_HOUR)))
            {
                return transitionTo(new CongestedMergeState(this.maneuverPattern));
            }

            HeadwayGtu parallel = null;
            HeadwayGtu leader = neigh.getLeader(dir);
            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            Length safeDistance = ego.getDesiredFrontHeadway(dir);
            Double safetyReductionFactor =
                    this.vehicle.getParameters().getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange);
            if (leader != null && ((leader.isParallel() || leader.getDistance().si < safeDistance.si * safetyReductionFactor)
                    && Math.abs(leader.getSpeed().si - ego.getEgoSpeed().si) < 1))
            {
                parallel = leader;
            }
            else
            {
                HeadwayGtu follower = neigh.getFollower(dir);
                if (follower != null
                        && ((follower.isParallel() || follower.getDistance().si < safeDistance.si * safetyReductionFactor)
                                && Math.abs(follower.getSpeed().si - ego.getEgoSpeed().si) < 1))
                {
                    parallel = follower;
                }
            }

            // --> NEU: Wenn ein paralleles Fahrzeug existiert, in den neuen State wechseln
            if (parallel != null)
            {
                return transitionTo(new SolveParallelVehicleState(this.maneuverPattern));
            }

            return null; // Keep braking
        }

        @Override
        public SimpleOperationalPlan abort()
        {
            try
            {
                if (this.vehicle.getLaneChangeDesire().magnitude() < this.vehicle.getParameters()
                        .getParameter(MirovaParameters.DMAND))
                {
                    return finishManeuver();
                }
            }
            catch (ParameterException | GtuException | NetworkException exception)
            {
                exception.printStackTrace();
            }
            return null;
        }

        @Override
        public double getUtility()
        {
            return this.vehicle.getMandatoryLaneChangeDesire().magnitude();
        }

        @Override
        public String toString()
        {
            return "DownstreamMergeState";
        }
    }

    /*
     * ========================================================================================= STATE: SOLVE PARALLEL VEHICLE
     * =========================================================================================
     */

    /**
     * State to resolve conflicts with a parallel vehicle on the target lane.
     * <p>
     * If a vehicle is driving parallel on the target lane, the ego vehicle typically decelerates slightly (-1.0 m/s&sup2;) to
     * let the parallel vehicle pass. However, if there is sufficient distance to the end of the ramp and the car-following
     * model allows for strong acceleration (&gt; 1.0 m/s&sup2;), the ego vehicle will accelerate maximally to merge ahead of
     * the parallel vehicle.
     * </p>
     * <p>
     * Copyright (c) 2026 Marvin Baumann / KIT. All rights reserved. <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
     */
    public static class SolveParallelVehicleState extends ActionState
    {
        /** The parent mandatory lane change pattern. */
        private final MandatoryLaneChangePattern pattern;

        /** Threshold for sufficient distance to lane end to attempt accelerating ahead [m]. */
        private static final double SUFFICIENT_DISTANCE_THRESHOLD = 200.0;

        private HeadwayGtu parallelVehicle = null;

        /**
         * Constructor for the solve parallel vehicle state.
         * @param p the parent maneuver pattern
         */
        public SolveParallelVehicleState(final ManeuverPattern p)
        {
            super(p);
            this.pattern = (MandatoryLaneChangePattern) p;
            this.active = true;
        }

        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException
        {
            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);

            Acceleration aCf = ego.getCurrentCarFollowingAcceleration();
            Length distToLaneEnd = infra.getDistanceToLaneEnd();

            Acceleration targetAcc = aCf; // Default to car-following acceleration if no parallel vehicle or no room to maneuver

            if (distToLaneEnd != null)
            {
                // Strategy: Check if we have enough room and momentum to overtake the parallel vehicle
                if (distToLaneEnd != null && distToLaneEnd.si > SUFFICIENT_DISTANCE_THRESHOLD && aCf.si > 1.0
                        && !parallelVehicle.isAhead())
                {
                    // Accelerate maximally to merge ahead
                    targetAcc = ego.getMaxPhysicalAcceleration();
                }
                else
                {
                    // Default strategy: Decelerate slightly to drop behind the parallel vehicle.
                    // We use Acceleration.min() with the Car-Following acceleration to ensure
                    // we don't crash into a leader on our CURRENT lane while braking.
                    Acceleration aStop = MirovaCarFollowingUtil.stop(this.vehicle, distToLaneEnd.minus(RAMP_END_BUFFER));
                    if (distToLaneEnd.si < 100.0)
                    {
                        // If we are very close to the end, be more conservative with braking to avoid unnecessary hard stops
                        aStop = Acceleration.min(aStop,
                                this.vehicle.getParameters().getParameter(MirovaParameters.egoDecelerationThreshold));
                    }
                    else
                    {
                        // If we have more room, we can afford a stronger deceleration to ensure we drop back in time
                        aStop = Acceleration.min(aStop, Acceleration.instantiateSI(-1.0));
                    }
                    targetAcc = Acceleration.min(aCf, aStop);
                }
            }
            SimpleOperationalPlan plan = new SimpleOperationalPlan(targetAcc, this.pattern.patternSpecificTimestep);

            // Keep the blinkers running
            if (this.pattern.getTargetDirection().isLeft())
            {
                plan.setIndicatorIntentLeft();
            }
            else if (this.pattern.getTargetDirection().isRight())
            {
                plan.setIndicatorIntentRight();
            }

            return plan;
        }

        @Override
        public SimpleOperationalPlan next() throws ParameterException, OperationalPlanException, NetworkException, GtuException
        {
            NeighborsContext neigh = this.vehicle.getContext(NeighborsContext.class);
            LateralDirectionality dir = this.pattern.getTargetDirection();

            // 1. Physical execution check: If the gap becomes perfectly clear, execute immediately
            if (neigh.getIfLaneChangePossible(dir))
            {
                return transitionTo(new ExecuteLaneChangeState(this.maneuverPattern, dir));
            }

            // 2. Emergency brake check: If the end of the ramp gets critically close
            Length distToLaneEnd = this.vehicle.getContext(InfrastructureContext.class).getDistanceToLaneEnd();
            if (distToLaneEnd != null)
            {
                Acceleration requiredStopAccel =
                        MirovaCarFollowingUtil.stop(this.vehicle, distToLaneEnd.minus(RAMP_END_BUFFER));
                if (requiredStopAccel.si < -5.0)
                {
                    return transitionTo(new DecelEndOfRampState(this.maneuverPattern));
                }
            }

            // --> NEU: Übergang in den Congested Merge State bei zähfließendem Verkehr (< 15 km/h)
            Speed egoSpeed = this.vehicle.getContext(EgoContext.class).getEgoSpeed();
            if (egoSpeed.lt(new Speed(15.0, org.djunits.unit.SpeedUnit.KM_PER_HOUR)))
            {
                return transitionTo(new CongestedMergeState(this.maneuverPattern));
            }

            // 3. Check if the parallel vehicle is still blocking us
            boolean hasParallel = false;
            HeadwayGtu putativeLeader = neigh.getLeader(dir);
            Length safeDistance = this.vehicle.getContext(EgoContext.class).getDesiredFrontHeadway(dir);
            Double safetyReductionFactor =
                    this.vehicle.getParameters().getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange);
            if (putativeLeader != null && (putativeLeader.isParallel()
                    || putativeLeader.getDistance().si < safeDistance.si * safetyReductionFactor))
            {
                hasParallel = true;
                parallelVehicle = putativeLeader;
            }
            else
            {
                HeadwayGtu putativeFollower = neigh.getFollower(dir);
                if (putativeFollower != null && (putativeFollower.isParallel()
                        || putativeFollower.getDistance().si < safeDistance.si * safetyReductionFactor))
                {
                    hasParallel = true;
                    parallelVehicle = putativeFollower;
                }
            }

            // 4. If the parallel vehicle is gone (passed us or we passed it), go back to evaluating the target gap
            if (!hasParallel)
            {
                return transitionTo(new EvaluateTargetGapState(this.maneuverPattern));
            }

            return null; // Stay in this state and continue resolving the conflict
        }

        @Override
        public SimpleOperationalPlan abort()
        {
            try
            {
                if (this.vehicle.getLaneChangeDesire().magnitude() < this.vehicle.getParameters()
                        .getParameter(MirovaParameters.DMAND))
                {
                    return finishManeuver();
                }
            }
            catch (ParameterException | GtuException | NetworkException exception)
            {
                exception.printStackTrace();
            }
            return null;
        }

        @Override
        public double getUtility()
        {
            return this.vehicle.getMandatoryLaneChangeDesire().magnitude();
        }

        @Override
        public String toString()
        {
            return "SolveParallelVehicleState";
        }
    }

    /*
     * ========================================================================================= STATE: CONGESTED MERGE
     * =========================================================================================
     */

    /**
     * State for congested merge situations when the vehicle speed drops below 15 km/h.
     * <p>
     * In this state, the vehicle attempts to maintain a minimum creeping speed of 15 km/h or follow the putative leader on the
     * target lane to synchronize for a merge in heavy traffic. The resulting acceleration is bounded by the car-following
     * acceleration of the current lane to prevent collisions. The state transitions back to gap evaluation if the speed
     * recovers above 20 km/h.
     * </p>
     * <p>
     * Copyright (c) 2026 Marvin Baumann / KIT. All rights reserved. <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
     */
    public static class CongestedMergeState extends ActionState
    {
        /** The parent mandatory lane change pattern. */
        private final MandatoryLaneChangePattern pattern;

        /** The speed threshold [km/h] below which the vehicle enters the creeping logic. */
        private static final Speed CONGESTION_SPEED_THRESHOLD = new Speed(15.0, org.djunits.unit.SpeedUnit.KM_PER_HOUR);

        /** The speed threshold [km/h] above which the vehicle returns to normal gap evaluation. */
        private static final Speed RECOVERY_SPEED_THRESHOLD = new Speed(20.0, org.djunits.unit.SpeedUnit.KM_PER_HOUR);

        /**
         * Constructor for the congested merge state.
         * @param p the parent maneuver pattern
         */
        public CongestedMergeState(final ManeuverPattern p)
        {
            super(p);
            this.pattern = (MandatoryLaneChangePattern) p;
            this.active = true;
        }

        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException
        {
            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            NeighborsContext neigh = this.vehicle.getContext(NeighborsContext.class);

            // 1. Eigene Car-Following Beschleunigung (Sicherheit nach vorne auf der eigenen Spur)
            Acceleration aCf = ego.getCurrentCarFollowingAcceleration();

            // 2. Approach Target Speed (15 km/h)
            Acceleration aApproach = MirovaCarFollowingUtil.approachTargetSpeed(this.vehicle, Length.instantiateSI(10.0),
                    CONGESTION_SPEED_THRESHOLD);

            // 3. Folgen des Putative Leaders auf der Target Lane
            Acceleration aMax = ego.getMaxPhysicalAcceleration(); // Fallback, falls kein Leader da ist
            aApproach = Acceleration.min(aApproach, aMax); // Wir wollen nicht schneller beschleunigen als das aktuelle
                                                           // Car-Following erlaubt
            HeadwayGtu putativeLeader = neigh.getLeader(this.pattern.getTargetDirection());
            if (putativeLeader != null)
            {
                aApproach =
                        Acceleration.max(aApproach, MirovaCarFollowingUtil.followSingleLeader(this.vehicle, putativeLeader));
            }

            // 4. Logik anwenden: max(approach, followTarget), dann min(aCf, max)
            Acceleration finalAcc = Acceleration.min(aCf, aApproach);

            SimpleOperationalPlan plan = new SimpleOperationalPlan(finalAcc, this.pattern.patternSpecificTimestep);

            // Blinker beibehalten
            if (this.pattern.getTargetDirection().isLeft())
            {
                plan.setIndicatorIntentLeft();
            }
            else if (this.pattern.getTargetDirection().isRight())
            {
                plan.setIndicatorIntentRight();
            }

            return plan;
        }

        @Override
        public SimpleOperationalPlan next() throws ParameterException, OperationalPlanException, NetworkException, GtuException
        {
            NeighborsContext neigh = this.vehicle.getContext(NeighborsContext.class);
            LateralDirectionality dir = this.pattern.getTargetDirection();

            // 1. Physischer Ausführungscheck: Wenn Lücke verfügbar, direkt wechseln
            if (neigh.getIfLaneChangePossible(dir))
            {
                return transitionTo(new ExecuteLaneChangeState(this.maneuverPattern, dir));
            }

            // 2. Notbremse am Ende der Rampe
            Length distToLaneEnd = this.vehicle.getContext(InfrastructureContext.class).getDistanceToLaneEnd();
            if (distToLaneEnd != null)
            {
                Acceleration requiredStopAccel =
                        MirovaCarFollowingUtil.stop(this.vehicle, distToLaneEnd.minus(RAMP_END_BUFFER));
                if (requiredStopAccel.si < -5.0)
                {
                    return transitionTo(new BreakingEndOfRampState(this.maneuverPattern));
                }
            }

            // 3. Recovery: Zurück in EvaluateTargetGapState, wenn wir wieder schnell genug sind (> 20 km/h)
            Speed egoSpeed = this.vehicle.getContext(EgoContext.class).getEgoSpeed();
            if (egoSpeed.gt(RECOVERY_SPEED_THRESHOLD))
            {
                return transitionTo(new EvaluateTargetGapState(this.maneuverPattern));
            }

            return null; // Bleibe im CongestedMergeState
        }

        @Override
        public SimpleOperationalPlan abort()
        {
            try
            {
                if (this.vehicle.getLaneChangeDesire().magnitude() < this.vehicle.getParameters()
                        .getParameter(MirovaParameters.DMAND))
                {
                    return finishManeuver();
                }
            }
            catch (ParameterException | GtuException | NetworkException exception)
            {
                exception.printStackTrace();
            }
            return null;
        }

        @Override
        public double getUtility()
        {
            return this.vehicle.getMandatoryLaneChangeDesire().magnitude();
        }

        @Override
        public String toString()
        {
            return "CongestedMergeState";
        }
    }

    /*
     * ========================================================================================= 5) STATE: BREAKING_END_OF_RAMP
     * =========================================================================================
     */

    /**
     * Emergency state to prevent driving off the end of the lane if no gap was found.
     */
    public static class DecelEndOfRampState extends ActionState
    {
        private final MandatoryLaneChangePattern pattern;

        /**
         * Constructor.
         * @param p the parent maneuver pattern
         */
        public DecelEndOfRampState(final ManeuverPattern p)
        {
            super(p);
            this.pattern = (MandatoryLaneChangePattern) p;
            this.active = true;
        }

        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException
        {
            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);

            Length distToLaneEnd = infra.getDistanceToLaneEnd();
            Acceleration a;

            if (distToLaneEnd != null)
            {
                a = MirovaCarFollowingUtil.stop(this.vehicle, distToLaneEnd.minus(RAMP_END_BUFFER));
            }
            else
            {
                // Fallback, falls wir im State sind, aber kein Ende mehr detektiert wird
                a = ego.getCurrentCarFollowingAcceleration();
            }

            SimpleOperationalPlan plan = new SimpleOperationalPlan(a, this.pattern.patternSpecificTimestep);
            if (this.pattern.getTargetDirection().isLeft())
                plan.setIndicatorIntentLeft();
            else if (this.pattern.getTargetDirection().isRight())
                plan.setIndicatorIntentRight();

            return plan;
        }

        @Override
        public SimpleOperationalPlan next() throws ParameterException, OperationalPlanException, NetworkException, GtuException
        {
            NeighborsContext neigh = this.vehicle.getContext(NeighborsContext.class);
            if (neigh.getIfLaneChangePossible(this.pattern.getTargetDirection()))
            {
                return transitionTo(new ExecuteLaneChangeState(this.maneuverPattern, this.pattern.getTargetDirection()));
            }
            return null;
        }

        @Override
        public SimpleOperationalPlan abort()
        {
            try
            {
                if (this.vehicle.getLaneChangeDesire().magnitude() < this.vehicle.getParameters()
                        .getParameter(MirovaParameters.DMAND))
                {
                    return finishManeuver();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public double getUtility()
        {
            return this.vehicle.getMandatoryLaneChangeDesire().magnitude();
        }

        @Override
        public String toString()
        {
            return "DecelEndOfRampState";
        }
    }

    /*
     * ========================================================================================= 6) STATE: EXECUTE_LANE_CHANGE
     * =========================================================================================
     */

    /**
     * Final state where the actual lateral move is executed.
     */
    public static class ExecuteLaneChangeState extends ActionState
    {
        private final LateralDirectionality direction;

        private final Lane originLane;

        private final MandatoryLaneChangePattern pattern;

        private boolean slowLaneChange = false;

        /**
         * Constructor.
         * @param p parent pattern
         * @param direction lateral direction
         * @throws ParameterException if parameter missing
         */
        public ExecuteLaneChangeState(final ManeuverPattern p, final LateralDirectionality direction) throws ParameterException
        {
            super(p);
            this.direction = direction;
            this.pattern = (MandatoryLaneChangePattern) p;
            this.originLane = this.vehicle.getGtu().getLane();

            // if (this.vehicle.getContext(EgoContext.class).getEgoSpeed().si < 7.0)
            // {
            // this.slowLaneChange = true;
            // this.vehicle.getParameters().setParameterResettable(ParameterTypes.LCDUR,
            // this.vehicle.getParameters().getParameter(MirovaParameters.congestedLaneChangeDuration));
            // }
        }

        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException
        {
            this.vehicle.commitToAction(this);
            NeighborsContext neighborsCtx = this.vehicle.getContext(NeighborsContext.class);
            EgoContext egoCtx = this.vehicle.getContext(EgoContext.class);

            HeadwayGtu targetLeader = neighborsCtx.getLeader(this.direction);
            if (targetLeader != null)
            {
                egoCtx.triggerRelaxation(targetLeader);
            }

            // Start with relaxed car-following acceleration (already computed via Macro/Utility)
            Acceleration minAcc = egoCtx.getCurrentCarFollowingAcceleration();

            // Synchronize with leader on the target lane
            if (this.vehicle.getGtu().getLane().equals(this.originLane))
            {
                Iterable<HeadwayGtu> leaders = neighborsCtx.getLeaders(this.direction);
                for (HeadwayGtu leader : leaders)
                {
                    if (!this.vehicle.getLaneChange().isChangingLane())
                    {
                        egoCtx.triggerRelaxation(leader);
                    }
                    Acceleration aTarget = MirovaCarFollowingUtil.followSingleLeader(this.vehicle, leader);
                    minAcc = Acceleration.min(minAcc, aTarget);
                }
            }

            SimpleOperationalPlan plan =
                    new SimpleOperationalPlan(minAcc, this.pattern.patternSpecificTimestep, this.direction);

            if (this.direction == LateralDirectionality.LEFT)
            {
                plan.setIndicatorIntentLeft();
            }
            else if (this.direction == LateralDirectionality.RIGHT)
            {
                plan.setIndicatorIntentRight();
            }

            return plan;
        }

        @Override
        public SimpleOperationalPlan next()
                throws ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException
        {
            boolean finished =
                    !this.vehicle.getLaneChange().isChangingLane() && !this.originLane.equals(this.vehicle.getGtu().getLane());

            if (finished)
            {
                // if (this.slowLaneChange)
                // {
                // this.vehicle.getParameters().resetParameter(ParameterTypes.LCDUR);
                // }
                this.vehicle.releaseActionLock();
                return finishManeuver();
            }
            return null;
        }

        @Override
        public SimpleOperationalPlan abort() throws ParameterException, OperationalPlanException
        {
            if (this.vehicle.getLaneChange().isChangingLane())
            {
                return null;
            }

            try
            {
                if (this.vehicle.getLaneChangeDesire().magnitude() < this.vehicle.getParameters()
                        .getParameter(MirovaParameters.DMAND))
                {
                    // if (this.slowLaneChange)
                    // {
                    // this.vehicle.getParameters().resetParameter(ParameterTypes.LCDUR);
                    // }

                    this.vehicle.releaseActionLock(); // HIER EINFÜGEN
                    return finishManeuver();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public double getUtility()
        {
            return this.vehicle.getMandatoryLaneChangeDesire().magnitude();
        }

        @Override
        public String toString()
        {
            return "ExecuteLaneChange[" + this.direction + "]";
        }
    }
}
