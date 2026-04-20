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
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.InfrastructureContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.MacroTrafficContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.following.MirovaCarFollowingUtil;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.exclusive.GapSearchPattern.CongestedGapSearchState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.helpers.GapCandidate;
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
        return this.targetDirection;
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
            // boolean isDesireHigh = this.vehicle.getLaneChangeDesire().magnitude() >= 0.1; // Lowered threshold for early
            // activation

            return isApproachingMerge; // || isDesireHigh;
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
        public AnticipateMergeState(final ManeuverPattern p)
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
            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
            Parameters params = this.vehicle.getParameters();

            Acceleration acc = ego.getCurrentCarFollowingAcceleration();
            Length distToMerge = infra.getDistanceToLaneChangeExtendedLookahead();
            Speed targetSpeed = null;

            // Check if we are approaching a merge within our extended lookahead
            if (distToMerge != null && distToMerge.si > 0.0
                    && distToMerge.si < params.getParameter(MirovaParameters.extendedLookAheadDistance).si)
            {
                // 1. Proactively find the target lane on the main road using the new path-projection method
                Lane targetMainroadLane = infra.getDownstreamAdjacentLane(this.pattern.getTargetDirection());

                if (targetMainroadLane != null)
                {
                    // 2. Measure the speed of the vehicles entering the merge zone on the main road.
                    // Focus only on the first 300m of the main road lane to capture the merge bottleneck without being
                    // influenced by downstream conditions.
                    targetSpeed = infra.getLaneAverageSpeed(targetMainroadLane, Length.ZERO, Length.instantiateSI(300.0),

                            3, // Max 5 vehicles to keep it sharp and reactive
                            InfrastructureContext.ScanDirection.FRONT_TO_BACK);
                }

                // NULL-SAFE-GUARD: Nur berechnen, wenn wir wirklich ein targetSpeed haben
                if (targetSpeed != null)
                {
                    // 3. Apply Exponential Moving Average (EMA) for stabilization
                    if (this.smoothedMergeSpeed == null)
                    {
                        this.smoothedMergeSpeed = targetSpeed;
                    }
                    else
                    {
                        double smoothedSi = (1.0 - this.SPEED_SMOOTHING_FACTOR) * this.smoothedMergeSpeed.si
                                + this.SPEED_SMOOTHING_FACTOR * targetSpeed.si;
                        this.smoothedMergeSpeed = new Speed(smoothedSi, SpeedUnit.SI);
                    }

                    // 4. Softly approach the stabilized anticipated speed over the remaining distance
                    Acceleration aToMatch =
                            MirovaCarFollowingUtil.approachTargetSpeed(this.vehicle, Length.ZERO, this.smoothedMergeSpeed);

                    // Fail-safe: Do not crash into direct leaders on the current ramp while anticipating
                    Acceleration maxDecel =
                            this.vehicle.getParameters().getParameter(MirovaParameters.egoDecelerationThreshold);
                    Acceleration safeAcc = Acceleration.max(Acceleration.min(acc, aToMatch), maxDecel);
                    return new SimpleOperationalPlan(safeAcc, this.pattern.patternSpecificTimestep);
                }
                else
                {
                    this.smoothedMergeSpeed = null;
                }
            }
            else
            {
                // Reset smoothing if we temporarily lose the merge target or haven't reached the threshold yet
                this.smoothedMergeSpeed = null;
            }

            return new SimpleOperationalPlan(acc, this.pattern.patternSpecificTimestep);
        }

        @Override
        public SimpleOperationalPlan next() throws ParameterException, OperationalPlanException, NetworkException, GtuException
        {
            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);

            // Transition to active matching once the target lane is physically adjacent to the ego vehicle
            if (infra.getIfLaneAvailable(this.pattern.getTargetDirection()))
            {
                return transitionTo(new MatchTargetLaneSpeedState(this.maneuverPattern));
            }

            return null;
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
     * ========================================================================================= 2) STATE:
     * MATCH_TARGET_LANE_SPEED =========================================================================================
     */
    // [Hier folgt die Logik aus dem alten MatchTargetLaneSpeedState.
    // Der Einfachheit halber gekürzt, entspricht deinem alten GapSearchPattern]

    public static class MatchTargetLaneSpeedState extends ActionState
    {
        private final MandatoryLaneChangePattern pattern;

        public MatchTargetLaneSpeedState(final ManeuverPattern p)
        {
            super(p);
            this.pattern = (MandatoryLaneChangePattern) p;
        }

        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException
        {
            // Wie gehabt: Geschwindigkeit an den direkten Nebenfluss anpassen
            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
            MacroTrafficContext macro = this.vehicle.getContext(MacroTrafficContext.class);
            Parameters params = this.vehicle.getParameters();

            Acceleration acc = ego.getCurrentCarFollowingAcceleration();
            Speed targetLaneSpeed = infra.getIfLaneAvailable(this.pattern.targetDirection)
                    ? (this.pattern.getTargetDirection().isLeft() ? macro.getAverageSpeedLeft() : macro.getAverageSpeedRight())
                    : macro.getAverageSpeedCurrent();

            Acceleration aToMatch =
                    MirovaCarFollowingUtil.approachTargetSpeed(this.vehicle, Length.instantiateSI(20.0), targetLaneSpeed);

            acc = Acceleration.min(acc,
                    Acceleration.max(aToMatch, ego.getEgoDecelerationThreshold(this.pattern.targetDirection)));

            SimpleOperationalPlan plan = new SimpleOperationalPlan(acc, this.pattern.patternSpecificTimestep);
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
            if (neigh.getIfLaneChangePossible(this.pattern.getTargetDirection()))
                return transitionTo(new ExecuteLaneChangeState(this.maneuverPattern, this.pattern.getTargetDirection()));

            return null;
        }

        @Override
        public double getUtility()
        {
            double mandatoryDesire = this.vehicle.getMandatoryLaneChangeDesire().magnitude();
            return mandatoryDesire; // Higher desire should increase utility, but we can also factor in distance to merge or
                                    // speed difference if desired
        }

        @Override
        public SimpleOperationalPlan abort()
        {
            return null;
        }
    }

    /*
     * ========================================================================================= 3) STATE: SEARCH_FOR_GAP
     * =========================================================================================
     */

    /**
     * State where the agent actively scans the target lane for a feasible gap.
     */
    public static class SearchForGapState extends ActionState
    {
        protected final MandatoryLaneChangePattern pattern;

        /**
         * Constructor.
         * @param p the parent maneuver pattern
         */
        public SearchForGapState(final ManeuverPattern p)
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
            MacroTrafficContext macro = this.vehicle.getContext(MacroTrafficContext.class);

            Acceleration acc = ego.getCurrentCarFollowingAcceleration();

            if (infra.getIfLaneAvailable(this.pattern.targetDirection))
            {
                Speed vTarget =
                        this.pattern.getTargetDirection().isLeft() ? macro.getAverageSpeedLeft() : macro.getAverageSpeedRight();

                Acceleration aMatch =
                        MirovaCarFollowingUtil.approachTargetSpeed(this.vehicle, Length.instantiateSI(20.0), vTarget);

                acc = Acceleration.min(acc, aMatch);
            }

            SimpleOperationalPlan plan = new SimpleOperationalPlan(acc, this.pattern.patternSpecificTimestep);
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

            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
            MacroTrafficContext macro = this.vehicle.getContext(MacroTrafficContext.class);
            RelativeLane targetLane = this.pattern.getTargetDirection().isLeft() ? RelativeLane.LEFT : RelativeLane.RIGHT;
            Speed vCong = this.vehicle.getParameters().getParameter(ParameterTypes.VCONG);

            if (macro.getAverageSpeed(targetLane).si <= vCong.si || infra.getDistanceToLaneEnd().si <= 200.0)
            {
                return transitionTo(new CongestedGapSearchState(this.maneuverPattern));
            }

            GapCandidate bestGap = findFeasibleGap();
            if (bestGap != null)
            {
                this.pattern.setActiveGap(bestGap);
                return transitionTo(new AccelerateToTargetGapState(this.maneuverPattern));
            }

            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            Acceleration requiredStopAccel =
                    MirovaCarFollowingUtil.stop(this.vehicle, infra.getDistanceToLaneEnd().minus(RAMP_END_BUFFER));

            if (requiredStopAccel.si < -5.0)
            {
                return transitionTo(new BreakingEndOfRampState(this.maneuverPattern));
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
            catch (ParameterException | GtuException | NetworkException exception)
            {
                exception.printStackTrace();
            }
            return null;
        }

        /**
         * Searches for a kinematic gap on the target lane.
         * @return a valid gap or null
         * @throws ParameterException if parameters fail
         * @throws GtuException if GTU access fails
         * @throws NetworkException if network access fails
         */
        private GapCandidate findFeasibleGap() throws ParameterException, GtuException, NetworkException
        {
            NeighborsContext neighCtx = this.vehicle.getContext(NeighborsContext.class);
            MacroTrafficContext macro = this.vehicle.getContext(MacroTrafficContext.class);
            EgoContext ego = this.vehicle.getContext(EgoContext.class);

            LateralDirectionality direction = this.pattern.getTargetDirection();
            RelativeLane targetLane = direction.isLeft() ? RelativeLane.LEFT : RelativeLane.RIGHT;

            Speed vEgo = ego.getEgoSpeed();
            Speed vTarget = macro.getAverageSpeed(targetLane);

            if (vEgo.si > vTarget.si)
            {
                // Downstream search
                Iterator<HeadwayGtu> leaderIt = neighCtx.getLeaders(direction).iterator();
                HeadwayGtu potentialFollower = neighCtx.getFollower(direction);
                if (potentialFollower == null)
                    return null;

                while (leaderIt.hasNext())
                {
                    HeadwayGtu potentialLeader = leaderIt.next();
                    GapCandidate candidate = new GapCandidate(potentialLeader, potentialFollower, direction, this.vehicle);
                    if (candidate.computeCurrentAcceleration() != null)
                        return candidate;
                    potentialFollower = potentialLeader;
                }
            }
            else
            {
                // Upstream search
                Iterator<HeadwayGtu> followerIt = neighCtx.getFollowers(direction).iterator();
                HeadwayGtu potentialLeader = neighCtx.getLeader(direction);
                if (potentialLeader == null)
                    return null;

                while (followerIt.hasNext())
                {
                    HeadwayGtu potentialFollower = followerIt.next();
                    GapCandidate candidate = new GapCandidate(potentialLeader, potentialFollower, direction, this.vehicle);
                    if (candidate.computeCurrentAcceleration() != null)
                        return candidate;
                    potentialLeader = potentialFollower;
                }
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
            return "SearchForGapState";
        }
    }

    /*
     * ========================================================================================= 4) STATE:
     * ACCELERATE_TO_TARGET_GAP =========================================================================================
     */

    /**
     * State where the vehicle actively targets the acceleration required to land in the selected gap.
     */
    public static class AccelerateToTargetGapState extends SearchForGapState
    {
        private Acceleration cachedAcceleration;

        /**
         * Constructor.
         * @param p the parent maneuver pattern
         */
        public AccelerateToTargetGapState(final ManeuverPattern p)
        {
            super(p);
        }

        @Override
        public SimpleOperationalPlan abort()
        {
            GapCandidate gap = this.pattern.getActiveGap();
            try
            {
                this.cachedAcceleration = gap.computeCurrentAcceleration();
                if (this.cachedAcceleration == null)
                {
                    return transitionTo(new SearchForGapState(this.maneuverPattern));
                }
            }
            catch (Exception e)
            {
                try
                {
                    return transitionTo(new SearchForGapState(this.maneuverPattern));
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
            return null;
        }

        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException
        {
            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            Acceleration aM = this.cachedAcceleration;
            this.cachedAcceleration = null;

            if (aM == null)
            {
                GapCandidate gap = this.pattern.getActiveGap();
                if (gap != null)
                    aM = gap.computeCurrentAcceleration();
            }

            Acceleration aCF = ego.getCurrentCarFollowingAcceleration();
            Acceleration finalAcc = (aM == null) ? aCF : Acceleration.min(aM, aCF);

            SimpleOperationalPlan plan = new SimpleOperationalPlan(finalAcc, this.pattern.patternSpecificTimestep);
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
        public double getUtility()
        {
            return this.vehicle.getMandatoryLaneChangeDesire().magnitude();
        }

        @Override
        public String toString()
        {
            return "AccelerateToTargetGapState";
        }
    }

    /*
     * ========================================================================================= 5) STATE: BREAKING_END_OF_RAMP
     * =========================================================================================
     */

    /**
     * Emergency state to prevent driving off the end of the lane if no gap was found.
     */
    public static class BreakingEndOfRampState extends ActionState
    {
        private final MandatoryLaneChangePattern pattern;

        /**
         * Constructor.
         * @param p the parent maneuver pattern
         */
        public BreakingEndOfRampState(final ManeuverPattern p)
        {
            super(p);
            this.pattern = (MandatoryLaneChangePattern) p;
            this.active = true;
        }

        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException
        {
            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);

            Acceleration a = MirovaCarFollowingUtil.stop(this.vehicle, infra.getDistanceToLaneEnd().minus(RAMP_END_BUFFER));

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
            return "BreakingEndOfRampState";
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

            if (this.vehicle.getContext(EgoContext.class).getEgoSpeed().si < 7.0)
            {
                this.slowLaneChange = true;
                this.vehicle.getParameters().setParameterResettable(ParameterTypes.LCDUR,
                        this.vehicle.getParameters().getParameter(MirovaParameters.congestedLaneChangeDuration));
            }
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

            // Add target-lane leader constraint cleanly using the new Utility
            if (this.vehicle.getGtu().getLane().equals(this.originLane))
            {
                if (targetLeader != null)
                {
                    // The Utility automatically applies the relaxation buffers we just triggered above!
                    Acceleration aTarget = MirovaCarFollowingUtil.followSingleLeader(this.vehicle, targetLeader);
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
                if (this.slowLaneChange)
                {
                    this.vehicle.getParameters().resetParameter(ParameterTypes.LCDUR);
                }
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
                    if (this.slowLaneChange)
                    {
                        this.vehicle.getParameters().resetParameter(ParameterTypes.LCDUR);
                    }

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
