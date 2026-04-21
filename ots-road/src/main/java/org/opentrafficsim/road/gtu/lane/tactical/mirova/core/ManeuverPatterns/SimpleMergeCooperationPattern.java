package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.exclusive.SimpleLaneChangePattern.PerformLaneChangeState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.InfrastructureContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.MacroTrafficContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.InfrastructureContext.LaneDropInfo;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.InfrastructureContext.ScanDirection;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.following.MirovaCarFollowingUtil;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Executes a streamlined, two-state merge cooperation strategy.
 * <p>
 * This pattern initiates in an anticipation state to smoothly react to downstream congestion and evaluate preemptive lane
 * changes. If a merging neighbor is identified, it transitions into a dedicated gap-opening state using a two-leader
 * car-following approach.
 * </p>
 * <p>
 * Copyright (c) 2026 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class SimpleMergeCooperationPattern extends ManeuverPattern implements Serializable
{

    /** Serial version UID. */
    private static final long serialVersionUID = 20260414L;

    /** The ID of the vehicle we are actively cooperating with. */
    protected String activeMergeCandidateId = null;

    /** The lateral direction from which the candidate is merging. */
    protected LateralDirectionality directionOfMergeCandidate = null;

    /** Distance threshold to consider cooperation near a lane drop. */
    private static final Length DISTANCE_THRESHOLD_MERGE_COOPERATION = Length.instantiateSI(250.0);

    /** Time-to-lane-end threshold to consider cooperation. */
    private static final Duration TIME_THRESHOLD_MERGE_COOPERATION = Duration.instantiateSI(15.0);

    /** List of lateral directions that currently require cooperation monitoring. */
    protected ArrayList<LateralDirectionality> listLanesWithCooperationNeeds = new ArrayList<>();

    /** Cache for anticipated lane drop info to avoid redundant calculations. */
    protected Map<LateralDirectionality, LaneDropInfo> anticipatedLaneDropMap =
            new java.util.EnumMap<>(LateralDirectionality.class);

    /**
     * Constructs a new SimpleMergeCooperationPattern.
     * @param vehicle the tactical planner executing this pattern
     */
    public SimpleMergeCooperationPattern(final MirovaTacticalPlanner vehicle)
    {
        super(PatternType.PARALLEL, vehicle);
        this.initialActionState = () -> new AnticipationState(this);
        this.requiredContextKeys.add("Ego");
        this.requiredContextKeys.add("Neighbors");
        this.requiredContextKeys.add("Infrastructure");
        this.requiredContextKeys.add("MacroTraffic");
    }

    /**
     * Determines if cooperation or anticipation is currently necessary.
     * @return always {@code true}, as this pattern relies on context checks for relevance
     */
    @Override
    public boolean checkAbility()
    {
        return true; // This pattern is always able to run when triggered, as it relies on context checks for relevance
    }

    /**
     * Checks infrastructure and macro context for approaching lane drops or congestion.
     * <p>
     * Utilizes both immediate adjacent lane perception and path-based downstream anticipation to detect merging needs far in
     * advance.
     * </p>
     * @return {@code true} if a relevant infrastructure event is detected, {@code false} otherwise
     * @throws ParameterException if parameter retrieval fails
     */
    @Override
    public boolean checkContext() throws ParameterException
    {
        this.listLanesWithCooperationNeeds.clear();
        this.anticipatedLaneDropMap.clear();

        InfrastructureContext infra =
                this.vehicle.getContextManager().getCategory("Infrastructure", InfrastructureContext.class);
        MacroTrafficContext macro = this.vehicle.getContextManager().getCategory("MacroTraffic", MacroTrafficContext.class);
        EgoContext ego = this.vehicle.getContextManager().getCategory("Ego", EgoContext.class);

        Speed leftLaneSpeed = Speed.POSITIVE_INFINITY;
        Speed rightLaneSpeed = Speed.POSITIVE_INFINITY;
        try
        {
            leftLaneSpeed = macro.getAverageSpeed(RelativeLane.LEFT);
            rightLaneSpeed = macro.getAverageSpeed(RelativeLane.RIGHT);
        }
        catch (OperationalPlanException | ParameterException exception)
        {
            // Context missing, handled gracefully by keeping positive infinity
        }

        Speed egoSpeed = ego.getEgoSpeed();
        Speed vCong = this.vehicle.getParameters().getParameter(ParameterTypes.VCONG);

        // Fetch standard adjacent lane drop distance
        Length distanceToEndLeft = infra.getDistanceToLaneEnd(RelativeLane.LEFT);
        if (distanceToEndLeft.eq(Length.POSITIVE_INFINITY))
        {
            LaneDropInfo dropInfoLeft = infra.getAnticipatedLaneDropInfo(LateralDirectionality.LEFT);
            if (dropInfoLeft != null)
            {
                distanceToEndLeft = dropInfoLeft.getDistance();
                this.anticipatedLaneDropMap.put(LateralDirectionality.LEFT, dropInfoLeft);
            }
        }

        Length distanceToEndRight = infra.getDistanceToLaneEnd(RelativeLane.RIGHT);
        if (distanceToEndRight.eq(Length.POSITIVE_INFINITY))
        {
            LaneDropInfo dropInfoRight = infra.getAnticipatedLaneDropInfo(LateralDirectionality.RIGHT);
            if (dropInfoRight != null)
            {
                distanceToEndRight = dropInfoRight.getDistance();
                this.anticipatedLaneDropMap.put(LateralDirectionality.RIGHT, dropInfoRight);
            }
        }

        if (egoSpeed.si > 15)
        {
            // Check Left
            if (distanceToEndLeft != null)
            {
                Duration timeToEndLeft = Duration.instantiateSI(distanceToEndLeft.si / egoSpeed.si);
                if (timeToEndLeft.lt(TIME_THRESHOLD_MERGE_COOPERATION))
                {
                    this.listLanesWithCooperationNeeds.add(LateralDirectionality.LEFT);
                }
                else if (leftLaneSpeed != null && leftLaneSpeed.lt(vCong) && leftLaneSpeed.si < egoSpeed.si + 3.0)
                {
                    this.listLanesWithCooperationNeeds.add(LateralDirectionality.LEFT);
                }
            }
            // Check Right
            if (distanceToEndRight != null)
            {
                Duration timeToEndRight = Duration.instantiateSI(distanceToEndRight.si / egoSpeed.si);
                if (timeToEndRight.lt(TIME_THRESHOLD_MERGE_COOPERATION))
                {
                    this.listLanesWithCooperationNeeds.add(LateralDirectionality.RIGHT);
                }
                else if (rightLaneSpeed != null && rightLaneSpeed.lt(vCong) && rightLaneSpeed.si < egoSpeed.si + 3.0)
                {
                    this.listLanesWithCooperationNeeds.add(LateralDirectionality.RIGHT);
                }
            }
        }
        else
        {
            // Congested / Slow moving traffic - evaluate purely by distance
            if (distanceToEndLeft != null && distanceToEndLeft.lt(DISTANCE_THRESHOLD_MERGE_COOPERATION))
            {
                this.listLanesWithCooperationNeeds.add(LateralDirectionality.LEFT);
            }
            if (distanceToEndRight != null && distanceToEndRight.lt(DISTANCE_THRESHOLD_MERGE_COOPERATION))
            {
                this.listLanesWithCooperationNeeds.add(LateralDirectionality.RIGHT);
            }
        }

        return !this.listLanesWithCooperationNeeds.isEmpty();
    }

    /**
     * Retrieves the current {@link HeadwayGtu} object for the stored candidate ID.
     * @return the updated HeadwayGtu, or {@code null} if lost or merged
     */
    public HeadwayGtu getActiveMergeCandidate()
    {
        if (this.activeMergeCandidateId == null || this.directionOfMergeCandidate == null)
        {
            return null;
        }
        NeighborsContext neighbors = this.vehicle.getContextManager().getCategory("Neighbors", NeighborsContext.class);
        Iterable<HeadwayGtu> leaders = neighbors.getLeaders(this.directionOfMergeCandidate);

        if (leaders != null)
        {
            for (HeadwayGtu gtu : leaders)
            {
                if (gtu.getId().equals(this.activeMergeCandidateId))
                {
                    return gtu;
                }
            }
        }
        return null;
    }

    /**
     * Helper method to scan for a valid merge candidate indicating a lane change.
     * @param neighbors the neighbors context containing perception data
     * @return {@code true} if a candidate is found and locked, {@code false} otherwise
     * @throws ParameterException if parameter retrieval fails
     */
    protected boolean findNewCandidate(final NeighborsContext neighbors) throws ParameterException
    {
        for (LateralDirectionality dir : this.listLanesWithCooperationNeeds)
        {
            Iterable<HeadwayGtu> adjacentLeaders = neighbors.getLeaders(dir);
            if (adjacentLeaders != null)
            {
                for (HeadwayGtu candidate : adjacentLeaders)
                {
                    boolean indicatesTowardsUs = (dir.isRight() && candidate.isLeftTurnIndicatorOn())
                            || (dir.isLeft() && candidate.isRightTurnIndicatorOn());
                    if (indicatesTowardsUs
                            && candidate.getDistance().gt(this.vehicle.getParameters().getParameter(ParameterTypes.S0)))
                    {
                        this.activeMergeCandidateId = candidate.getId();
                        this.directionOfMergeCandidate = dir;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /*
     * ========================================================================================= STATE 1: ANTICIPATION STATE
     * =========================================================================================
     */

    /**
     * State dedicated to anticipating downstream speeds and checking for preemptive evasions.
     */
    public static class AnticipationState extends ActionState
    {

        /** The parent maneuver pattern. */
        private final SimpleMergeCooperationPattern maneuverPattern;

        /**
         * Constructor.
         * @param pattern the parent maneuver pattern
         */
        public AnticipationState(final SimpleMergeCooperationPattern pattern)
        {
            super(pattern);
            this.maneuverPattern = pattern;
        }

        @Override
        public SimpleOperationalPlan next() throws OperationalPlanException, ParameterException, GtuException, NetworkException
        {
            NeighborsContext neighbors = this.vehicle.getContextManager().getCategory("Neighbors", NeighborsContext.class);
            EgoContext ego = this.vehicle.getContextManager().getCategory("Ego", EgoContext.class);

            // Check for preemptive lane change (Evasion) if approaching a congested merge
            if (ego.getEgoSpeed().gt(this.vehicle.getParameters().getParameter(ParameterTypes.VCONG))
                    && this.vehicle.getParameters().getParameter(MirovaParameters.cooperativeLaneChangesEnabled))
            {

                // Assuming direction based on list structure or default logic
                if (!this.maneuverPattern.listLanesWithCooperationNeeds.isEmpty())
                {
                    LateralDirectionality dir = this.maneuverPattern.listLanesWithCooperationNeeds.get(0);
                    LateralDirectionality oppositeDir = dir.isLeft() ? LateralDirectionality.RIGHT : LateralDirectionality.LEFT;

                    if (this.vehicle.getLaneChangeDesire().getMandatoryDesire(oppositeDir) >= 0
                            && neighbors.checkIfLaneChangeIsPossible(oppositeDir))
                    {
                        return transitionTo(new PerformLaneChangeState(this.maneuverPattern, oppositeDir));
                    }
                }
            }

            // Check for merge candidate, if ending ramp is adjacent and end of lane is in sight
            // transition if found
            InfrastructureContext infra =
                    this.vehicle.getContextManager().getCategory("Infrastructure", InfrastructureContext.class);
            RelativeLane endingLane =
                    this.maneuverPattern.listLanesWithCooperationNeeds.get(0).isLeft() ? RelativeLane.LEFT : RelativeLane.RIGHT;
            // Check if there is an actual neighbor requesting to merge
            if (infra.getDistanceToLaneEnd(endingLane).lt(this.vehicle.getParameters().getParameter(ParameterTypes.LOOKAHEAD))
                    && this.maneuverPattern.findNewCandidate(neighbors))
            {
                return transitionTo(new OpenGapState(this.maneuverPattern));
            }

            return null; // Stay in AnticipationState
        }

        @Override
        public SimpleOperationalPlan executeControl()
                throws ParameterException, OperationalPlanException, GtuException, NetworkException
        {
            this.maneuverPattern.setRunning(true);
            this.maneuverPattern.setCurrentActionState(this);

            EgoContext ego = this.vehicle.getContextManager().getCategory("Ego", EgoContext.class);
            InfrastructureContext infra =
                    this.vehicle.getContextManager().getCategory("Infrastructure", InfrastructureContext.class);

            Acceleration aDirectLeader = ego.getCurrentCarFollowingAcceleration();
            Acceleration aAnticipation = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.METER_PER_SECOND_2);

            // Calculate anticipation based on downstream speed
            if (!this.maneuverPattern.listLanesWithCooperationNeeds.isEmpty())
            {
                LateralDirectionality dir = this.maneuverPattern.listLanesWithCooperationNeeds.get(0);
                RelativeLane relativeLane = dir.isLeft() ? RelativeLane.LEFT : RelativeLane.RIGHT;
                LateralDirectionality oppositeDir = dir.isLeft() ? LateralDirectionality.RIGHT : LateralDirectionality.LEFT;
                LaneDropInfo laneDropInfo = this.maneuverPattern.anticipatedLaneDropMap.get(dir);

                if (laneDropInfo != null)
                {
                    // 1. Antizipation weit im Voraus (Spur endet auf zukünftigen Streckenabschnitten)
                    Lane laneDropLane = laneDropInfo.getLane();
                    Lane mainroadLane = laneDropLane.getAdjacentLane(oppositeDir, null);

                    if (infra.getDistanceToLaneEnd(relativeLane).si > 50.0 && mainroadLane != null)
                    {
                        Length mainroadLaneLength = mainroadLane.getLength();

                        // Absicherung gegen negative Längen, falls die Lane kürzer als 250m ist
                        Length startPos =
                                Length.max(Length.ZERO, mainroadLaneLength.minus(DISTANCE_THRESHOLD_MERGE_COOPERATION));

                        Speed downstreamSpeed = infra.getLaneAverageSpeed(mainroadLane, startPos, mainroadLaneLength, 4,
                                ScanDirection.FRONT_TO_BACK);

                        if (downstreamSpeed.lt(this.vehicle.getParameters().getParameter(ParameterTypes.VCONG)))
                        {
                            aAnticipation = MirovaCarFollowingUtil.approachTargetSpeed(vehicle, Length.ZERO, downstreamSpeed);
                        }
                    }
                }
                // else if (infra.getDistanceToLaneEnd(relativeLane).si > 50.0)
                // {
                // // 2. Antizipation im Nahbereich (Die endende Spur liegt unmittelbar neben uns)
                // // Da wir das kooperierende Fahrzeug auf der Hauptspur sind, müssen wir die
                // // Geschwindigkeit UNSERER Spur antizipieren, da der Stau durch den Einfädler vor uns entsteht.
                // MacroTrafficContext macro =
                // this.vehicle.getContextManager().getCategory("MacroTraffic", MacroTrafficContext.class);
                // Speed downstreamSpeed = macro.getAverageSpeed(RelativeLane.CURRENT);

                // if (downstreamSpeed.lt(this.vehicle.getParameters().getParameter(ParameterTypes.VCONG)))
                // {
                // aAnticipation = MirovaCarFollowingUtil.approachTargetSpeed(vehicle, Length.ZERO, downstreamSpeed);
                // }
                // }
            }
            Acceleration accCoop = this.vehicle.getParameters().getParameter(MirovaParameters.cooperativeDecelerationThreshold);
            Acceleration finalAcceleration = Acceleration.min(aDirectLeader, Acceleration.max(aAnticipation, accCoop));
            return new SimpleOperationalPlan(finalAcceleration, this.vehicle.getParameters().getParameter(ParameterTypes.DT));
        }

        @Override
        public SimpleOperationalPlan abort()
        {
            this.maneuverPattern.setRunning(false);
            return null;
        }

        @Override
        public double getUtility()
        {
            // Utility can be based on the presence of cooperation needs and candidates
            if (!this.maneuverPattern.listLanesWithCooperationNeeds.isEmpty())
            {
                return 0.5; // High utility when there are cooperation needs
            }
            return 0.2; // Moderate utility when just anticipating
        }

        @Override
        public String toString()
        {
            return "AnticipationState";
        }
    }

    /*
     * ========================================================================================= STATE 2: OPEN GAP STATE
     * =========================================================================================
     */

    /**
     * State dedicated to actively cooperative braking for a specific merging neighbor.
     */
    public static class OpenGapState extends ActionState
    {

        /** The parent maneuver pattern. */
        private final SimpleMergeCooperationPattern maneuverPattern;

        /**
         * Constructor.
         * @param pattern the parent maneuver pattern
         */
        public OpenGapState(final SimpleMergeCooperationPattern pattern)
        {
            super(pattern);
            this.maneuverPattern = pattern;
        }

        @Override
        public SimpleOperationalPlan next() throws OperationalPlanException, ParameterException, GtuException, NetworkException
        {
            HeadwayGtu candidate = this.maneuverPattern.getActiveMergeCandidate();

            // Abort if candidate is gone or cancelled the indicator
            if (candidate == null || (!candidate.isLeftTurnIndicatorOn() && !candidate.isRightTurnIndicatorOn()
                    && candidate.getDistance().gt(this.vehicle.getParameters().getParameter(ParameterTypes.S0))))
            {
                return finishManeuver();
            }

            // Evasion check remains active in case a gap on the opposite lane suddenly opens
            EgoContext ego = this.vehicle.getContextManager().getCategory("Ego", EgoContext.class);
            NeighborsContext neighbors = this.vehicle.getContextManager().getCategory("Neighbors", NeighborsContext.class);

            if (ego.getEgoSpeed().gt(this.vehicle.getParameters().getParameter(ParameterTypes.VCONG))
                    && this.vehicle.getParameters().getParameter(MirovaParameters.cooperativeLaneChangesEnabled))
            {

                LateralDirectionality oppositeDir = this.maneuverPattern.directionOfMergeCandidate.isLeft()
                        ? LateralDirectionality.RIGHT : LateralDirectionality.LEFT;
                if (neighbors.checkIfLaneChangeIsPossible(oppositeDir))
                {
                    return transitionTo(new PerformLaneChangeState(this.maneuverPattern, oppositeDir));
                }
            }

            return null; // Stay in OpenGapState
        }

        @Override
        public SimpleOperationalPlan executeControl()
                throws ParameterException, OperationalPlanException, GtuException, NetworkException
        {
            this.maneuverPattern.setRunning(true);
            this.maneuverPattern.setCurrentActionState(this);

            EgoContext ego = this.vehicle.getContextManager().getCategory("Ego", EgoContext.class);
            InfrastructureContext infra =
                    this.vehicle.getContextManager().getCategory("Infrastructure", InfrastructureContext.class);

            Acceleration aDirectLeader = ego.getCurrentCarFollowingAcceleration();
            Acceleration aCooperation = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.METER_PER_SECOND_2);

            HeadwayGtu candidate = this.maneuverPattern.getActiveMergeCandidate();
            if (candidate != null)
            {
                aCooperation = MirovaCarFollowingUtil.followSingleLeader(vehicle, candidate);
            }

            // The vehicle applies the most restrictive acceleration required (Two-Leader Model)
            Acceleration finalAcceleration = Acceleration.min(aDirectLeader, aCooperation);

            // Cap cooperative braking to avoid dangerous emergency stops on the main lane
            Acceleration decelThreshold =
                    this.vehicle.getParameters().getParameter(MirovaParameters.preemptiveCooperativeDeceleration);
            finalAcceleration = aCooperation.gt(decelThreshold) ? aCooperation : decelThreshold;
            finalAcceleration = Acceleration.min(aDirectLeader, finalAcceleration);
            return new SimpleOperationalPlan(finalAcceleration, this.vehicle.getParameters().getParameter(ParameterTypes.DT));
        }

        @Override
        public SimpleOperationalPlan abort()
        {
            this.maneuverPattern.setRunning(false);
            return null;
        }

        @Override
        public double getUtility()
        {
            // High utility when actively cooperating with a candidate
            if (this.maneuverPattern.getActiveMergeCandidate() != null)
            {
                return 0.5;
            }
            return 0.3; // Moderate utility if we are in this state but lost the candidate
        }

        @Override
        public String toString()
        {
            return "OpenGapState";
        }
    }
}
