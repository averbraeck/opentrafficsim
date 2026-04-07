package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.exclusive;

import java.util.Iterator;

import org.djunits.unit.AccelerationUnit;
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
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectInfrastructurePerception;
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
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.helpers.GapCandidate;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Mandatory lane change pattern implementing a tactical gap selection and targeted acceleration planning.
 * <p>
 * This class represents a Finite State Machine (FSM) within <b>Layer 4 (Procedure & Action)</b> of the
 * MiRoVA architecture. It handles the complex process of finding and reaching a gap in the target lane
 * when a mandatory change is required (e.g., lane end or route following).
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class GapSearchPattern extends ManeuverPattern
{
    /** The intended lateral direction for the maneuver. */
    private LateralDirectionality targetDirection;

    /** The currently targeted gap on the adjacent lane. */
    private GapCandidate activeGap;

    /** Buffer distance before the end of the lane where emergency braking is enforced. */
    public static final Length RAMP_END_BUFFER = Length.instantiateSI(10.0);

    /** Specific simulation time step for the execution of this maneuver. */
    private final Duration patternSpecificTimestep = Duration.instantiateSI(0.1);

    /**
     * Constructs a new GapSearchPattern.
     *
     * @param vehicle the tactical planner associated with the ego vehicle
     */
    public GapSearchPattern(final MirovaTacticalPlanner vehicle)
    {
        super(PatternType.EXCLUSIVE, vehicle);
        this.initialActionState = () -> new MatchTargetLaneSpeedState(this);
        this.targetDirection = this.vehicle.getLaneChangeDesire().dominantDirection();
        this.requiredContextKeys.add("Ego");
        this.requiredContextKeys.add("Neighbors");
        this.requiredContextKeys.add("Infrastructure");
        this.requiredContextKeys.add("MacroTraffic");
    }

    /**
     * Gets the lateral direction of the target lane.
     *
     * @return the target direction
     */
    public LateralDirectionality getTargetDirection()
    {
        return this.targetDirection;
    }

    /**
     * Gets the currently active gap candidate.
     *
     * @return the active gap
     */
    public GapCandidate getActiveGap()
    {
        return this.activeGap;
    }

    /**
     * Sets the currently active gap candidate.
     *
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
            // Trigger if mandatory desire exceeds threshold
            return this.vehicle.getLaneChangeDesire().magnitude() >= this.vehicle.getParameters()
                    .getParameter(MirovaParameters.DMAND);
        }
        catch (ParameterException exception)
        {
            return false;
        }
    }

    @Override
    public boolean checkAbility()
    {
        try
        {
            DirectInfrastructurePerception infra =
                    this.vehicle.getPerception().getPerceptionCategory(DirectInfrastructurePerception.class);
            this.targetDirection = this.vehicle.getLaneChangeDesire().dominantDirection();
            // Ability is given if a lane change in the desired direction is physically/legally possible
            return infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, this.targetDirection).si > 0.0;
        }
        catch (OperationalPlanException exception)
        {
            return false;
        }
    }

    /* =========================================================================================
     * 1) STATE: MATCH_TARGET_LANE_SPEED
     * ========================================================================================= */

    /**
     * State where the vehicle adjusts its speed to match the average flow speed of the target lane.
     */
    public static class MatchTargetLaneSpeedState extends ActionState
    {
        private final GapSearchPattern pattern;

        /**
         * Constructor.
         * @param p the parent maneuver pattern
         */
        public MatchTargetLaneSpeedState(final ManeuverPattern p)
        {
            super(p);
            this.pattern = (GapSearchPattern) p;
            this.active = true;
            this.vehicle.setRunningManeuver(true);
        }

        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException
        {
            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
            MacroTrafficContext macro = this.vehicle.getContext(MacroTrafficContext.class);
            Parameters params = this.vehicle.getParameters();

            Acceleration acc = ego.getCurrentCarFollowingAcceleration();
            Speed targetLaneSpeed;

            if (infra.getIfLaneAvailable(this.pattern.targetDirection))
            {
                targetLaneSpeed = this.pattern.getTargetDirection().isLeft() ? macro.getAverageSpeedLeft()
                        : macro.getAverageSpeedRight();
            }
            else
            {
                targetLaneSpeed = macro.getAverageSpeedCurrent();
            }

            Acceleration aToMatch = CarFollowingUtil.approachTargetSpeed(this.vehicle.getCarFollowingModel(), params,
                    ego.getEgoSpeed(), infra.getCurrentSpeedLimit(), Length.instantiateSI(20.0), targetLaneSpeed);

            Acceleration egoDecel = ego.getEgoDecelerationThreshold(this.pattern.targetDirection);
            acc = Acceleration.min(acc, Acceleration.max(aToMatch, egoDecel));

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
            Desire mand = this.vehicle.getMandatoryLaneChangeDesire();

            if (neigh.getIfLaneChangePossible(this.pattern.getTargetDirection()))
                return transitionTo(new ExecuteLaneChangeState(this.maneuverPattern, this.pattern.getTargetDirection()));

            MacroTrafficContext macro = this.vehicle.getContext(MacroTrafficContext.class);
            Speed targetLaneSpeed =
                    this.pattern.getTargetDirection().isLeft() ? macro.getAverageSpeedLeft() : macro.getAverageSpeedRight();
            Speed vCong = this.vehicle.getParameters().getParameter(ParameterTypes.VCONG);

            if (mand.magnitude() >= this.vehicle.getParameters().getParameter(MirovaParameters.DMAND))
            {
                if (targetLaneSpeed.si > vCong.si)
                    return transitionTo(new SearchForGapState(this.maneuverPattern));
                else
                    return transitionTo(new CongestedGapSearchState(this.maneuverPattern));
            }

            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            Acceleration stopAccel = CarFollowingUtil.stop(this.vehicle.getCarFollowingModel(), this.vehicle.getParameters(),
                    ego.getEgoSpeed(), infra.getCurrentSpeedLimit(), infra.getDistanceToLaneEnd().minus(RAMP_END_BUFFER));

            if (stopAccel.si < -5.0)
                return transitionTo(new BreakingEndOfRampState(this.maneuverPattern));

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

        @Override
        public String toString() { return "MatchTargetLaneSpeedState"; }
    }

    /* =========================================================================================
     * 2) STATE: SEARCH_FOR_GAP
     * ========================================================================================= */

    /**
     * State where the agent actively scans the target lane for a feasible gap.
     */
    public static class SearchForGapState extends ActionState
    {
        protected final GapSearchPattern pattern;

        /**
         * Constructor.
         * @param p the parent maneuver pattern
         */
        public SearchForGapState(final ManeuverPattern p)
        {
            super(p);
            this.pattern = (GapSearchPattern) p;
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
                Speed vTarget = this.pattern.getTargetDirection().isLeft() ? macro.getAverageSpeedLeft()
                        : macro.getAverageSpeedRight();

                Acceleration aMatch = CarFollowingUtil.approachTargetSpeed(this.vehicle.getCarFollowingModel(),
                        this.vehicle.getParameters(), ego.getEgoSpeed(), infra.getCurrentSpeedLimit(),
                        Length.instantiateSI(20.0), vTarget);

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
            Acceleration requiredStopAccel = CarFollowingUtil.stop(this.vehicle.getCarFollowingModel(),
                    this.vehicle.getParameters(), ego.getEgoSpeed(), infra.getCurrentSpeedLimit(),
                    infra.getDistanceToLaneEnd().minus(RAMP_END_BUFFER));

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
        public String toString() { return "SearchForGapState"; }
    }

    /* =========================================================================================
     * 3) STATE: ACCELERATE_TO_TARGET_GAP
     * ========================================================================================= */

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
                try { return transitionTo(new SearchForGapState(this.maneuverPattern)); }
                catch (Exception ex) { ex.printStackTrace(); }
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
                if (gap != null) aM = gap.computeCurrentAcceleration();
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
        public String toString() { return "AccelerateToTargetGapState"; }
    }

    /* =========================================================================================
     * 4) STATE: BREAKING_END_OF_RAMP
     * ========================================================================================= */

    /**
     * Emergency state to prevent driving off the end of the lane if no gap was found.
     */
    public static class BreakingEndOfRampState extends ActionState
    {
        private final GapSearchPattern pattern;

        /**
         * Constructor.
         * @param p the parent maneuver pattern
         */
        public BreakingEndOfRampState(final ManeuverPattern p)
        {
            super(p);
            this.pattern = (GapSearchPattern) p;
            this.active = true;
        }

        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException
        {
            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);

            Acceleration a = CarFollowingUtil.stop(this.vehicle.getCarFollowingModel(), this.vehicle.getParameters(),
                    ego.getEgoSpeed(), infra.getCurrentSpeedLimit(), infra.getDistanceToLaneEnd().minus(RAMP_END_BUFFER));

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
            catch (Exception e) { e.printStackTrace(); }
            return null;
        }

        @Override
        public String toString() { return "BreakingEndOfRampState"; }
    }

    /* =========================================================================================
     * 5) STATE: EXECUTE_LANE_CHANGE
     * ========================================================================================= */

    /**
     * Final state where the actual lateral move is executed.
     */
    public static class ExecuteLaneChangeState extends ActionState
    {
        private final LateralDirectionality direction;
        private final Lane originLane;
        private final GapSearchPattern pattern;
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
            this.pattern = (GapSearchPattern) p;
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
            InfrastructureContext infraCtx = this.vehicle.getContext(InfrastructureContext.class);
            NeighborsContext neighborsCtx = this.vehicle.getContext(NeighborsContext.class);
            EgoContext egoCtx = this.vehicle.getContext(EgoContext.class);

            this.vehicle.setTargetDesiredHeadway(this.vehicle.getParameters().getParameter(ParameterTypes.T).times(
                    this.vehicle.getParameters().getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange)));

            Acceleration minAcc = egoCtx.getCurrentCarFollowingAcceleration();

            if (this.vehicle.getGtu().getLane().equals(this.originLane))
            {
                HeadwayGtu targetLeader = neighborsCtx.getLeader(this.direction);
                if (targetLeader != null)
                {
                    Acceleration aTarget = CarFollowingUtil.followSingleLeader(this.vehicle.getCarFollowingModel(),
                            this.vehicle.getParameters(), egoCtx.getEgoSpeed(), infraCtx.getCurrentSpeedLimit(),
                            targetLeader.getDistance(), targetLeader.getSpeed());
                    minAcc = Acceleration.min(minAcc, aTarget);
                }
            }

            SimpleOperationalPlan plan =
                    new SimpleOperationalPlan(minAcc, this.pattern.patternSpecificTimestep, this.direction);
            if (this.direction.isLeft())
                plan.setIndicatorIntentLeft();
            else
                plan.setIndicatorIntentRight();

            return plan;
        }

        @Override
        public SimpleOperationalPlan next() throws ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException
        {
            boolean finished = !this.vehicle.getLaneChange().isChangingLane()
                    && !this.originLane.equals(this.vehicle.getGtu().getLane());

            if (finished)
            {
                if (this.slowLaneChange)
                    this.vehicle.getParameters().resetParameter(ParameterTypes.LCDUR);
                return finishManeuver();
            }
            return null;
        }

        @Override
        public SimpleOperationalPlan abort() throws ParameterException, OperationalPlanException
        {
            if (this.vehicle.getLaneChange().isChangingLane())
                return null;

            try
            {
                if (this.vehicle.getLaneChangeDesire().magnitude() < this.vehicle.getParameters()
                        .getParameter(MirovaParameters.DMAND))
                {
                    if (this.slowLaneChange)
                        this.vehicle.getParameters().resetParameter(ParameterTypes.LCDUR);
                    return finishManeuver();
                }
            }
            catch (Exception e) { e.printStackTrace(); }
            return null;
        }

        @Override
        public String toString() { return "ExecuteLaneChange[" + this.direction + "]"; }
    }

    /* =========================================================================================
     * 6) STATE: CONGESTED_GAP_SEARCH
     * ========================================================================================= */

    /**
     * State for gap searching in low-speed, congested conditions.
     */
    public static class CongestedGapSearchState extends ActionState
    {
        private String targetLeaderId = null;
        private final GapSearchPattern pattern;

        /**
         * Constructor.
         * @param p the parent maneuver pattern
         */
        public CongestedGapSearchState(final ManeuverPattern p)
        {
            super(p);
            this.pattern = (GapSearchPattern) p;
        }

        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException
        {
            NeighborsContext neigh = this.vehicle.getContext(NeighborsContext.class);
            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            Iterable<HeadwayGtu> leaderIt = neigh.getLeaders(this.pattern.getTargetDirection());

            Acceleration aCf = ego.getCurrentCarFollowingAcceleration();

            if (this.targetLeaderId == null)
            {
                Acceleration egoDecelThreshold = ego.getEgoDecelerationThreshold(this.pattern.getTargetDirection());
                for (HeadwayGtu leader : leaderIt)
                {
                    Length distToTarget = leader.getSpeed().si > 3.0 ? leader.getDistance()
                            : leader.getDistance().minus(this.vehicle.getGtu().getLength());
                    Acceleration aTarget = CarFollowingUtil.followSingleLeader(this.vehicle.getCarFollowingModel(),
                            this.vehicle.getParameters(), ego.getEgoSpeed(),
                            this.vehicle.getContext(InfrastructureContext.class).getCurrentSpeedLimit(), distToTarget,
                            leader.getSpeed());

                    if (aTarget.si > egoDecelThreshold.si && aTarget.si < aCf.si)
                    {
                        this.targetLeaderId = leader.getId();
                        aCf = Acceleration.min(aCf, aTarget);
                        break;
                    }
                }
            }
            else
            {
                for (HeadwayGtu leader : leaderIt)
                {
                    if (leader.getId().equals(this.targetLeaderId))
                    {
                        Acceleration aTarget = (ego.getEgoSpeed().si < 7.0)
                                ? CarFollowingUtil.followSingleLeader(this.vehicle.getCarFollowingModel(),
                                        this.vehicle.getParameters(), ego.getEgoSpeed(),
                                        this.vehicle.getContext(InfrastructureContext.class).getCurrentSpeedLimit(),
                                        leader.getDistance().minus(this.vehicle.getGtu().getLength()), leader.getSpeed())
                                : CarFollowingUtil.stop(this.vehicle.getCarFollowingModel(), this.vehicle.getParameters(),
                                        ego.getEgoSpeed(),
                                        this.vehicle.getContext(InfrastructureContext.class).getCurrentSpeedLimit(),
                                        leader.getDistance().minus(this.vehicle.getGtu().getLength()));

                        aTarget = Acceleration.max(aTarget, ego.getEgoDecelerationThreshold(this.pattern.getTargetDirection()));
                        aCf = Acceleration.min(aCf, aTarget);
                        break;
                    }
                }
            }

            SimpleOperationalPlan plan = new SimpleOperationalPlan(aCf, this.pattern.patternSpecificTimestep);
            if (this.pattern.getTargetDirection().isLeft())
                plan.setIndicatorIntentLeft();
            else
                plan.setIndicatorIntentRight();

            return plan;
        }

        @Override
        public SimpleOperationalPlan next() throws ParameterException, OperationalPlanException, NetworkException, GtuException
        {
            NeighborsContext neigh = this.vehicle.getContext(NeighborsContext.class);
            if (neigh.getIfLaneChangePossible(this.pattern.getTargetDirection()))
                return transitionTo(new ExecuteLaneChangeState(this.maneuverPattern, this.pattern.getTargetDirection()));

            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            HeadwayGtu leader = neigh.getLeader(this.pattern.getTargetDirection());

            if (ego.getEgoSpeed().si < 2.0 && leader != null && leader.getAcceleration().si > 0.5)
                return transitionTo(new ExecuteZipperMergingState(this.maneuverPattern));

            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
            Acceleration stopAccel = CarFollowingUtil.stop(this.vehicle.getCarFollowingModel(), this.vehicle.getParameters(),
                    ego.getEgoSpeed(), infra.getCurrentSpeedLimit(), infra.getDistanceToLaneEnd().minus(RAMP_END_BUFFER));

            if (stopAccel.si < -5.0)
                return transitionTo(new BreakingEndOfRampState(this.maneuverPattern));

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
            catch (Exception e) { e.printStackTrace(); }
            return null;
        }

        @Override
        public String toString() { return "CongestedGapSearchState"; }
    }

    /**
     * State where the vehicle waits for the leader in the target lane to start moving.
     */
    public static class ExecuteZipperMergingState extends ActionState
    {
        private final GapSearchPattern pattern;

        /**
         * Constructor.
         * @param p parent maneuver pattern
         */
        public ExecuteZipperMergingState(final ManeuverPattern p)
        {
            super(p);
            this.pattern = (GapSearchPattern) p;
        }

        @Override
        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException
        {
            NeighborsContext neigh = this.vehicle.getContext(NeighborsContext.class);
            EgoContext ego = this.vehicle.getContext(EgoContext.class);
            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);

            HeadwayGtu leader = neigh.getLeader(this.pattern.getTargetDirection());
            Acceleration aTarget = CarFollowingUtil.followSingleLeader(this.vehicle.getCarFollowingModel(),
                    this.vehicle.getParameters(), ego.getEgoSpeed(), infra.getCurrentSpeedLimit(), leader.getDistance(),
                    leader.getSpeed());

            aTarget = Acceleration.max(aTarget,
                    Acceleration.min(Acceleration.instantiateSI(0.5), leader.getAcceleration()));
            aTarget = Acceleration.min(aTarget, ego.getCurrentCarFollowingAcceleration());

            return new SimpleOperationalPlan(aTarget, this.pattern.patternSpecificTimestep);
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
            catch (Exception e) { e.printStackTrace(); }
            return null;
        }
    }
}