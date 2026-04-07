package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.InfrastructureContext;
import org.opentrafficsim.road.network.LaneChangeInfo;

/**
 * KnowledgeChunk representing a mandatory lane change for route following.
 * <p>
 * Forms a critical part of <b>Layer 2 (Cognition / Motivation)</b> in the MiRoVA architecture.
 * This chunk monitors the route and the network infrastructure. It produces a
 * {@link Desire} to leave a lane if it ends or diverges from the route.
 * Crucially, it also produces negative desires for adjacent lanes that are
 * invalid (e.g., dead ends), preventing discretionary lane changes into unsafe areas.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class MandatoryLaneChangeChunk extends KnowledgeChunk
{

    /**
     * Constructs a new MandatoryLaneChangeChunk.
     *
     * @param vehicle the tactical planner governing the ego agent
     * @throws OperationalPlanException if chunk instantiation fails
     */
    public MandatoryLaneChangeChunk(final MirovaTacticalPlanner vehicle) throws OperationalPlanException
    {
        super(vehicle);
    }

    /**
     * Determines if the mandatory lane change logic is applicable.
     * <p>
     * This chunk is always applicable as it must constantly monitor the
     * validity of current and adjacent lanes to provide veto desires.
     * Even if the current lane is fine, it must prevent changing into a
     * lane that ends by returning negative desires.
     * </p>
     *
     * @return {@code true} as this chunk must always evaluate lane validity
     * @throws ParameterException if parameter evaluation fails
     */
    @Override
    public boolean isApplicable() throws ParameterException
    {
        return true;
    }

    /**
     * Computes the LMRS-based mandatory lane change desire.
     * <p>
     * Step 1: Compute the desire-to-leave for each relevant lane (LEFT, CURRENT, RIGHT).<br>
     * Step 2: Derive directional desires (to LEFT / to RIGHT) by comparing
     * the desire-to-leave values.
     * </p>
     *
     * @return a {@link Desire} object containing mandatory desire values for left and right directions
     * @throws ParameterException if required parameters are missing
     */
    @Override
    public Desire computeDesire() throws ParameterException
    {
        // --- Access Contexts ----------------------------------------------------
        EgoContext egoCtx = this.getMirovaTacticalPlanner().getContext(EgoContext.class);
        InfrastructureContext infraCtx = this.getMirovaTacticalPlanner().getContext(InfrastructureContext.class);
        LanePerception perception = this.getMirovaTacticalPlanner().getPerception();
        InfrastructurePerception infraPerc = getInfrastructurePerception();

        Parameters p = getParameters();
        double v = egoCtx.getEgoSpeed().si;

        // --- Step 1: Desire-to-leave per lane (d_r,k) ---------------------------
        Map<RelativeLane, Double> dLeave = new HashMap<>();

        // Define relevant lanes to check
        RelativeLane[] lanesToCheck = new RelativeLane[]{RelativeLane.LEFT, RelativeLane.CURRENT, RelativeLane.RIGHT};
        double lookAheadParam = p.getParameter(ParameterTypes.LOOKAHEAD).si;
        double t0Param = p.getParameter(ParameterTypes.T0).si;

        for (RelativeLane lane : lanesToCheck)
        {
            double d_r = 0.0;

            // Check if lane exists in cross-section
            if (perception.getLaneStructure().exists(lane))
            {
                // Calculate desire based on remaining distance
                SortedSet<LaneChangeInfo> info = infraPerc.getLegalLaneChangeInfo(lane);

                if (info != null && !info.isEmpty())
                {
                    for (LaneChangeInfo lcInfo : info)
                    {
                        double d = 0.0;
                        Length x = lcInfo.remainingDistance();
                        int n = lcInfo.numberOfLaneChanges();

                        // Prevent division by zero if n=0 (should not happen for mandatory info, but safety first)
                        n = n == 0 ? 1 : n;

                        double d1 = 1.0 - x.si / (n * lookAheadParam);
                        double d2 = 1.0 - (x.si / v) / (n * t0Param);
                        d = Math.max(d1, d2);

                        d_r = Math.max(d_r, Math.max(0.0, d)); // clamp to [0,1]
                    }
                }
                else
                {
                    // Fallback: If no explicit LaneChangeInfo is present, but the lane physically ends
                    // shortly, we must generate a high desire to leave it.
                    Length directDistToEnd = infraCtx.getDistanceToLaneEnd(lane);

                    if (directDistToEnd.si < lookAheadParam)
                    {
                        // Treat as urgent single lane change required
                        double d = 1.0 - directDistToEnd.si / lookAheadParam;
                        d_r = Math.max(d_r, Math.max(0.0, d));
                    }
                }
            }
            dLeave.put(lane, d_r);
        }

        // --- Step 2: Compute Directional Desires --------------------------------

        // Get constraints for changing FROM current lane
        SortedSet<LaneChangeInfo> currentInfo = infraPerc.getLegalLaneChangeInfo(RelativeLane.CURRENT);
        Length currentReqDist = (currentInfo == null || currentInfo.isEmpty() || currentInfo.first().numberOfLaneChanges() == 0)
                ? Length.POSITIVE_INFINITY
                : currentInfo.first().remainingDistance();

        Double dCurr = dLeave.getOrDefault(RelativeLane.CURRENT, 0.0);
        double dLeft = 0.0;
        double dRight = 0.0;

        // Check Left Validity
        if (perception.getLaneStructure().exists(RelativeLane.LEFT)
                && infraCtx.getIfLaneAvailable(LateralDirectionality.LEFT))
        {
            // Check if we are allowed to change (not past the "must change" point of the target?)
            if (infraCtx.getDistanceToLaneEnd(RelativeLane.LEFT).si > lookAheadParam)
            {
                if (infraPerc.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.LEFT).neg().lt(currentReqDist))
                {
                    double dLeaveLeft = dLeave.getOrDefault(RelativeLane.LEFT, 0.0);
                    // Schakel formula: desire to change = (DesireLeaveTarget < DesireLeaveCurrent) ? DesireLeaveCurrent : -DesireLeaveTarget
                    dLeft = dLeaveLeft < dCurr ? dCurr : -dLeaveLeft;
                }
            }
            else
            {
                // Cannot leave current lane anymore, so no desire to go left
                dLeft = -1.0;
            }
        }

        // Check Right Validity
        if (perception.getLaneStructure().exists(RelativeLane.RIGHT)
                && infraCtx.getIfLaneAvailable(LateralDirectionality.RIGHT))
        {
            if (infraCtx.getDistanceToLaneEnd(RelativeLane.RIGHT).si > lookAheadParam)
            {
                if (infraPerc.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.RIGHT).neg().lt(currentReqDist))
                {
                    double dLeaveRight = dLeave.getOrDefault(RelativeLane.RIGHT, 0.0);
                    dRight = dLeaveRight < dCurr ? dCurr : -dLeaveRight;
                }
            }
            else
            {
                // Cannot leave current lane anymore, so no desire to go right
                dRight = -1.0;
            }
        }

        // --- Build Desire vector ------------------------------------------------

        // Return desire with the "mandatory" flag set to true
        return new Desire(dLeft, dRight, true);
    }
}