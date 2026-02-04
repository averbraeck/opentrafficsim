package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.parallel;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

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
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks.KnowledgeChunk;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern.PatternType;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.InfrastructureContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterableSet;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;

/**
 * Handles cooperative behavior towards merging vehicles on adjacent lanes.
 * Implements a strict 3-step verification process to determine cooperation feasibility.
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class MergeCooperationPattern extends ManeuverPattern {

    /** The ID of the vehicle we are actively cooperating with. */
    private String activeMergeCandidateId = null;

    private static final Length DISTANCE_THRESHOLD_MERGE_COOPERATION = Length.instantiateSI(40.0);
    private static final Duration TIME_THRESHOLD_MERGE_COOPERATION = Duration.instantiateSI(15.0);

    private ArrayList<LateralDirectionality> listLanesWithCooperationNeeds = new ArrayList<>();

    public MergeCooperationPattern(final MirovaTacticalPlanner vehicle) {
        super(PatternType.PARALLEL, vehicle);
        this.initialActionState = () -> new OpenGapState(this);
        this.requiredContextKeys.add("Ego");
        this.requiredContextKeys.add("Neighbors");
        this.requiredContextKeys.add("Infrastructure");
    }

    /**
     * Determines if cooperation is necessary and feasible based on the 3-step logic:
     * 1. Desire/Indicator check.
     * 2. Ego-Leader suitability check.
     * 3. Ego braking capability check.
     * @return true if cooperation should be initiated.
     */
    @Override
    public boolean checkAbility() {
        NeighborsContext neighbors = this.vehicle.getContextManager().getCategory("Neighbors", NeighborsContext.class);
        EgoContext ego = this.vehicle.getContextManager().getCategory("Ego", EgoContext.class);
        InfrastructureContext infrastructure = this.vehicle.getContextManager().getCategory("Infrastructure", InfrastructureContext.class);

        HeadwayGtu egoLeader = neighbors.getCurrentLeader(); // Vehicle directly ahead of us

        this.activeMergeCandidateId = null;

        // Iterate left and right to find candidates
        for (LateralDirectionality dir : this.listLanesWithCooperationNeeds) {
            Iterable<HeadwayGtu> adjacentLeaders = neighbors.getLeaders(dir);
            if (adjacentLeaders == null) continue;

            for (HeadwayGtu candidate : adjacentLeaders) {

                // --- CHECK 1: Lane Change Wish (Indicators) ---
                boolean indicatesTowardsUs = (dir.isRight() && candidate.isLeftTurnIndicatorOn())
                                          || (dir.isLeft() && candidate.isRightTurnIndicatorOn());

                // If no indicator, we assume no immediate cooperation need (could be extended by "Lane End" logic)
                if (!indicatesTowardsUs) continue;

                //System.out.println("GTU " + this.vehicle.getGtu().getId()                        + "MergeCooperationPattern: Candidate found: " + candidate.toString());

                // --- CHECK 2: Can Ego-Leader handle it? ---
                // If the candidate is far ahead of us, they might merge in front of our leader.
                boolean leaderSuitable = false;
                if (egoLeader != null) {

                    SortedSet<HeadwayGtu> tempSet = new TreeSet<>();
                    tempSet.add(candidate);
                    PerceptionIterable<HeadwayGtu> iterable = new PerceptionIterableSet<>(tempSet);

                    Acceleration potentialLeaderDeceleration = null;
                    try
                    {
                        Duration temporaryHeadway = egoLeader.getParameters().getParameter(ParameterTypes.T)
                                .times(this.vehicle.getParameters().getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange));
                        egoLeader.getParameters().setParameterResettable(ParameterTypes.T, temporaryHeadway);
                        potentialLeaderDeceleration = CarFollowingUtil.followSingleLeader(
                                egoLeader.getCarFollowingModel(),
                                egoLeader.getParameters(),
                                egoLeader.getSpeed(),
                                infrastructure.getCurrentSpeedLimit(),
                                candidate.getDistance().minus(egoLeader.getDistance()),
                                candidate.getSpeed());

                        egoLeader.getParameters().resetParameter(ParameterTypes.T);
                    }
                    catch (ParameterException exception)
                    {
                        exception.printStackTrace();
                    }


                    try
                    {
                        if (potentialLeaderDeceleration.si > this.vehicle.getParameters().getParameter(MirovaParameters.cooperativeDecelerationThreshold).si) {
                            leaderSuitable = true;
//                            System.out.println("GTU " + this.vehicle.getGtu().getId()
//                                    + " MergeCooperationPattern: Leader " + egoLeader.getId()
//                                    + " can handle merge from candidate " + candidate.getId()
//                                    + " with deceleration " + potentialLeaderDeceleration.toString(AccelerationUnit.SI));
                        }

                    }
                    catch (ParameterException exception)
                    {
                        exception.printStackTrace();
                    }
                    }


                    // If the leader is suitable/better positioned, we do NOT act.
                    if (leaderSuitable) {

                        continue;
                    }

                    // --- CHECK 3: Can Ego brake reasonably? ---
                    // Kinematic check: Can we open a gap within our cooperative Deceleration Threshold?

                    Acceleration requiredDeceleration = null;
                    try
                    {
                        Duration temporaryHeadway = this.vehicle.getParameters().getParameter(ParameterTypes.T)
                                .times(this.vehicle.getParameters().getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange));
                        this.vehicle.getParameters().setParameterResettable(ParameterTypes.T, temporaryHeadway);
                        requiredDeceleration = CarFollowingUtil.followSingleLeader(
                                this.vehicle.getCarFollowingModel(),
                                this.vehicle.getParameters(),
                                ego.getEgoSpeed(),
                                infrastructure.getCurrentSpeedLimit(),
                                candidate);
//                        System.out.println("GTU " + this.vehicle.getGtu().getId()
//                                + " MergeCooperationPattern: Required deceleration to cooperate with candidate "
//                                + candidate.getId() + " is " + requiredDeceleration.toString(AccelerationUnit.SI)
//                                + ". Ego speed: " + ego.getEgoSpeed().toString()+ ", Candidate speed: " + candidate.getSpeed().toString() + ", Distance: " + candidate.getDistance());
                        this.vehicle.getParameters().resetParameter(ParameterTypes.T);
                    }
                    catch (ParameterException exception)
                    {
                        exception.printStackTrace();
                    }

                    try
                    {
                        if (requiredDeceleration.si > this.vehicle.getParameters().getParameter(MirovaParameters.cooperativeDecelerationThreshold).si) {
                            // All checks passed. We are the chosen one.
                            this.activeMergeCandidateId = candidate.getId();
                            //System.out.println("GTU " + this.vehicle.getGtu().getId()                                    + "MergeCooperationPattern: Candidate passed all checks, cooperation initiated: " + candidate.toString());
                            return true;
                        }
                    }
                    catch (ParameterException exception)
                    {
                        exception.printStackTrace();
                    }
                }
            }

        return false;
    }

    /**
     * Checks if the infrastructure context suggests a potential need for cooperation.
     * Scans for merges (incoming lanes), splits (forks), or intersections within the lookahead distance.
     * * @return true if a relevant infrastructure event is detected.
     * @throws ParameterException if parameter retrieval fails.
     */
    @Override
    public boolean checkContext() throws ParameterException {
        this.listLanesWithCooperationNeeds.clear();

        // 1. Determine Lookahead (Horizon)
        // We use the standard perception lookahead or a specific parameter if defined.
        Length lookahead = this.vehicle.getParameters().getParameter(org.opentrafficsim.base.parameters.ParameterTypes.LOOKAHEAD);

        // 2. Access Infrastructure Context
        InfrastructureContext infra = this.vehicle.getContextManager().getCategory("Infrastructure", InfrastructureContext.class);
        EgoContext ego = this.vehicle.getContextManager().getCategory("Ego", EgoContext.class);
        Speed egoSpeed = ego.getEgoSpeed();

        Length distanceToEndLeft = infra.getDistanceToLaneEnd(RelativeLane.LEFT);

        Length distanceToEndRight = infra.getDistanceToLaneEnd(RelativeLane.RIGHT);

        // 3. Check for merges/splits/intersections
        if (egoSpeed.si > 15) {
            if (distanceToEndLeft != null) {
                Duration timeToEndLeft = Duration.instantiateSI(distanceToEndLeft.si / egoSpeed.si);
                if (timeToEndLeft.lt(TIME_THRESHOLD_MERGE_COOPERATION)) {
                    this.listLanesWithCooperationNeeds.add(LateralDirectionality.LEFT);
                }
            }
            if (distanceToEndRight != null) {
                Duration timeToEndRight = Duration.instantiateSI(distanceToEndRight.si / egoSpeed.si);
                if (timeToEndRight.lt(TIME_THRESHOLD_MERGE_COOPERATION)) {
                    this.listLanesWithCooperationNeeds.add(LateralDirectionality.RIGHT);
                }
            }
        }
        else {
            // Stationary vehicle - check only distance
            if (distanceToEndLeft != null && distanceToEndLeft.lt(DISTANCE_THRESHOLD_MERGE_COOPERATION)) {
                this.listLanesWithCooperationNeeds.add(LateralDirectionality.LEFT);
            }
            if (distanceToEndRight != null && distanceToEndRight.lt(DISTANCE_THRESHOLD_MERGE_COOPERATION)) {
                this.listLanesWithCooperationNeeds.add(LateralDirectionality.RIGHT);
            }
        }

        if (!this.listLanesWithCooperationNeeds.isEmpty()) {
            //System.out.println("GTU " + this.vehicle.getGtu().getId()                    + "MergeCooperationPattern: Cooperation needed on lanes: " + this.listLanesWithCooperationNeeds.toString());
            return true;

        }
        else {
            return false;
        }
    }

    /**
     * Retrieves the CURRENT HeadwayGtu object for the stored candidate ID.
     * Scans the NeighborsContext to find the fresh snapshot.
     * @return The updated HeadwayGtu, or null if lost/merged.
     */
    public HeadwayGtu getActiveMergeCandidate() {
        if (this.activeMergeCandidateId == null) return null;

        NeighborsContext neighbors = this.vehicle.getContextManager().getCategory("Neighbors", NeighborsContext.class);

        // Scan all relevant lists (Left and Right) to find the ID.
        // Even if they merged partially, they might appear in one of these lists or as a 'Parallel' leader.
        for (LateralDirectionality dir : this.listLanesWithCooperationNeeds) {
            Iterable<HeadwayGtu> leaders = neighbors.getLeaders(dir);
            if (leaders != null) {
                for (HeadwayGtu gtu : leaders) {
                    if (gtu.getId().equals(this.activeMergeCandidateId)) {
//                        System.out.println("GTU " + this.vehicle.getGtu().getId()
//                                + "MergeCooperationPattern: Active merge candidate located in leaders: " + gtu.toString());
                        return gtu;
                    }
                }
//                System.out.println("GTU " + this.vehicle.getGtu().getId()
//                        + "MergeCooperationPattern: Active merge candidate NOT found in leaders for direction " + dir.toString());
            }
        }
        return null; // Lost contact
    }


    public static class OpenGapState extends ActionState {

        private MergeCooperationPattern maneuverPattern;

        private Acceleration aCoop = Acceleration.NaN;

        private HeadwayGtu mergeCandidate = null;

        public OpenGapState(final MergeCooperationPattern pattern) {
            super(pattern);
            this.maneuverPattern = pattern;

        }


        @Override
        public SimpleOperationalPlan executeControl()
                throws ParameterException, OperationalPlanException, GtuException, NetworkException
        {
            this.maneuverPattern.setRunning(true);
            this.maneuverPattern.setCurrentActionState(this);

            EgoContext ego = this.vehicle.getContextManager().getCategory("Ego", EgoContext.class);

            Acceleration aCF = ego.getCurrentCarFollowingAcceleration();


            // Choose the more conservative (lower) acceleration
            Acceleration chosenAcceleration = aCF.lt(this.aCoop) ? aCF : this.aCoop;

//            System.out.println("GTU " + this.vehicle.getGtu().getId()
//                    + "MergeCooperationPattern: Executing OpenGapState with chosen acceleration: " + chosenAcceleration.toString(AccelerationUnit.SI));

            return new SimpleOperationalPlan(chosenAcceleration, this.vehicle.getParameters().getParameter(ParameterTypes.DT));
        }

        @Override
        public SimpleOperationalPlan next() throws OperationalPlanException, ParameterException, NullPointerException,
                IllegalArgumentException, GtuException, NetworkException
        {
            this.mergeCandidate = this.maneuverPattern.getActiveMergeCandidate();

            if (this.mergeCandidate.isLeftTurnIndicatorOn() == false && this.mergeCandidate.isRightTurnIndicatorOn() == false) {
//                System.out.println("GTU " + this.vehicle.getGtu().getId()
//                        + " MergeCooperationPattern: Merge candidate no longer indicates, ending cooperation: " + this.mergeCandidate.toString());
                return finishManeuver();
            }

            return null;
        }

        @Override
        public SimpleOperationalPlan abort()
                throws ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException
        {
            EgoContext ego = this.vehicle.getContextManager().getCategory("Ego", EgoContext.class);
            this.mergeCandidate = this.maneuverPattern.getActiveMergeCandidate();
            if (this.mergeCandidate == null) {
//                System.out.println("GTU " + this.vehicle.getGtu().getId()
//                        + " MergeCooperationPattern: Merge candidate lost, aborting cooperation.");
                return finishManeuver();
            }


            InfrastructureContext infrastructure = this.vehicle.getContextManager().getCategory("Infrastructure", InfrastructureContext.class);

            this.aCoop = CarFollowingUtil.followSingleLeader(
                    this.vehicle.getCarFollowingModel(),
                    this.vehicle.getParameters(),
                    ego.getEgoSpeed(),
                    infrastructure.getCurrentSpeedLimit(),
                    this.mergeCandidate);

            if (this.aCoop.si < this.vehicle.getParameters().getParameter(MirovaParameters.cooperativeDecelerationThreshold).si) {
                this.maneuverPattern.setRunning(false);
                this.aCoop = Acceleration.POSITIVE_INFINITY;
//                System.out.println("GTU " + this.vehicle.getGtu().getId()
//                        + " MergeCooperationPattern: Cannot maintain cooperation without excessive braking, aborting.");
            }

            return null;
        }

        @Override
        public String toString() {
            return "OpenGapState";
        }
    }

}