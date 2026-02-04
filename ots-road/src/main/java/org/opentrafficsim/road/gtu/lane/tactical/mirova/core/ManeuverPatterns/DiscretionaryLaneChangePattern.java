//package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns;
//
//import org.djunits.value.vdouble.scalar.Acceleration;
//import org.djunits.value.vdouble.scalar.Duration;
//import org.djunits.value.vdouble.scalar.Length;
//import org.djunits.value.vdouble.scalar.Speed;
//import org.opentrafficsim.base.parameters.ParameterException;
//import org.opentrafficsim.base.parameters.ParameterTypes;
//import org.opentrafficsim.base.parameters.Parameters;
//import org.opentrafficsim.core.gtu.GtuException;
//import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
//import org.opentrafficsim.core.network.LateralDirectionality;
//import org.opentrafficsim.core.network.NetworkException;
//import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
//import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
//import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
//import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.Desire;
//import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;
//import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters;
//import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks.KnowledgeChunk;
//import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;
//import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.InfrastructureContext;
//import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;
//import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
//import org.opentrafficsim.road.network.lane.Lane;
//
///**
// * DiscretionaryLaneChangePattern.
// * <p>
// * Handles voluntary lane changes driven by motivation (e.g., speed gain, or keeping right).
// * This pattern acts as the tactical implementation of the desire computed in the Cognitive Layer.
// * </p>
// * <p>
// * <b>Lifecycle:</b>
// * <ol>
// * <li><b>Check:</b> Activates if Desire > Threshold and Gap is available.</li>
// * <li><b>Perform:</b> Executes the lateral movement.</li>
// * <li><b>Complete:</b> Resets state and hands control back to Pattern Selector.</li>
// * </ol>
// * </p>
// * <p>
// * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
// * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
// * </p>
// * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
// */
//public class DiscretionaryLaneChangePattern extends ManeuverPattern
//{
//    /** The direction of this specific lane change pattern instance. */
//    private final LateralDirectionality targetDirection;
//
//    /**
//     * Constructor.
//     * @param kc The knowledge chunk providing the desire (Cognition).
//     * @param direction The direction (Left/Right) this pattern is responsible for.
//     * @throws ParameterException if parameters are missing.
//     */
//    public DiscretionaryLaneChangePattern(final KnowledgeChunk kc) throws ParameterException
//    {
//        super(PatternType.FREE_LC, kc);
//        this.targetDirection = this.vehicle.getLaneChangeDesire().dominantDirection();
//        this.initialActionState = new PerformLaneChangeState(this, this.targetDirection);
//    }
//
//    // ========================================================================================
//    // APPLICABILITY CHECKS (The Gatekeeper)
//    // ========================================================================================
//
//    /**
//     * Checks if the motivation (Desire) is strong enough to justify this maneuver.
//     * @return true if Desire > dFree and cooldown passed.
//     */
//    @Override
//    public boolean checkContext()
//    {
//       return true; // Desire check is done in checkAbility()
//    }
//
//    /**
//     * Checks if the maneuver is physically possible (Gap Acceptance).
//     * @return true if safe gap exists.
//     */
//    @Override
//    public boolean checkAbility()
//    {
//     // 1. Retrieve Desire from Cognition Layer
//        // Assuming the Vehicle or KnowledgeChunk holds the aggregated desire
//        Desire desire = this.vehicle.getLaneChangeDesire();
//
//        // 2. Check Threshold (dFree)
//        double dFree;
//        try
//        {
//            dFree = this.vehicle.getParameters().getParameter(MirovaParameters.DFREE);
//            double desireValue = (this.targetDirection == LateralDirectionality.LEFT) ? desire.getLeft() : desire.getRight();
//
//            if (desireValue < dFree)
//            {
//                return false;
//            }
//
//            // 3. Basic Gap Existence
//            NeighborsContext neighbors = this.vehicle.getContext(NeighborsContext.class);
//            if (neighbors.getIfLaneChangePossible(this.targetDirection))
//            {
//                return true;
//            }
//
//            return false;
//
//        }
//        catch (ParameterException exception)
//        {
//            exception.printStackTrace();
//        }
//
//        return false;
//        }
//
//
//    public LateralDirectionality getDirection()
//    {
//        return this.targetDirection;
//    }
//
//
//    // ========================================================================================
//    // STATE: PERFORM (Lateral Movement)
//    // ========================================================================================
//
//    public static class PerformLaneChangeState extends ActionState
//    {
//        private final LateralDirectionality direction;
//        private final Lane originLane;
//
//        public PerformLaneChangeState(final ManeuverPattern pattern, final LateralDirectionality direction)
//        {
//            super(pattern);
//            this.direction = direction;
//            this.originLane = this.vehicle.getGtu().getLane();
//        }
//
//        @Override
//        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException
//        {
//            NeighborsContext neighbors = this.vehicle.getContext(NeighborsContext.class);
//            EgoContext ego = this.vehicle.getContext(EgoContext.class);
//            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
//            Parameters params = this.vehicle.getParameters();
//
//            Double safetyDistanceReductionFactorLaneChange = this.vehicle.getParameters().getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange) * 1.1;
//            Duration timeHeadwayReduced = this.vehicle.getParameters().getParameter(ParameterTypes.T).times(safetyDistanceReductionFactorLaneChange);
//            this.vehicle.getParameters().setParameterResettable(ParameterTypes.T, timeHeadwayReduced);
//
//            // 1. Basic ACC from current lane
//            Acceleration acc = ego.getCurrentCarFollowingAcceleration();
//
//            // 2. Dual-Target Consideration: Respect leader on target lane too
//            if (this.vehicle.getGtu().getLane().equals(this.originLane))
//            {
//                HeadwayGtu targetLeader = neighbors.getLeader(this.direction);
//                if (targetLeader != null)
//                {
//                    Acceleration aTarget = CarFollowingUtil.followSingleLeader(
//                            this.vehicle.getCarFollowingModel(),
//                            params,
//                            ego.getEgoSpeed(),
//                            infra.getCurrentSpeedLimit(),
//                            targetLeader.getDistance(),
//                            targetLeader.getSpeed());
//
//                    acc = Acceleration.min(acc, aTarget);
//                }
//            }
//            this.vehicle.getParameters().resetParameter(ParameterTypes.T);
//            // 3. Plan with lateral direction
//            SimpleOperationalPlan plan = new SimpleOperationalPlan(
//                    acc,
//                    params.getParameter(ParameterTypes.DT),
//                    this.direction);
//
//            if (this.direction == LateralDirectionality.LEFT) {
//                plan.setIndicatorIntentLeft();
//            } else {
//                plan.setIndicatorIntentRight();
//            }
//
//            this.vehicle.setRunningManeuver(true); // Lock the maneuver
//            return plan;
//        }
//
//        @Override
//        public SimpleOperationalPlan next() throws ParameterException, GtuException, NetworkException
//        {
//            // Check completion: Have we left the origin lane?
//            boolean changed = !this.vehicle.getGtu().getLane().equals(this.originLane);
//            boolean isChanging = this.vehicle.getLaneChange().isChangingLane();
//
//            if (changed && !isChanging)
//            {
//                this.vehicle.setRunningManeuver(false);
//                EgoContext ego = this.vehicle.getContext(EgoContext.class);
//                Acceleration zeroAcc = ego.getCurrentCarFollowingAcceleration();
//                return new SimpleOperationalPlan(
//                        zeroAcc,
//                        this.vehicle.getGtu().getParameters().getParameter(ParameterTypes.DT));
//            }
//            return null;
//        }
//
//        @Override
//        public SimpleOperationalPlan abort()
//        {
//            // Lane change abort logic is complex (steering back).
//            // For now, we commit once started (safe-to-abort check would go here).
//            return null;
//        }
//
//        @Override
//        public String toString()
//        {
//            return "DLC:Perform[" + this.direction + "]";
//        }
//    }
//
//
//}