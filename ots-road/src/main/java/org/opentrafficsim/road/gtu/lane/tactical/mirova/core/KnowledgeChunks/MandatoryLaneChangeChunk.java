package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.InfrastructureContext;
import org.opentrafficsim.road.network.LaneChangeInfo;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * KnowledgeChunk representing a mandatory lane change for route following.
 * <p>
 * This chunk becomes active when the current lane will end or the route requires
 * the vehicle to move to another lane within a limited look-ahead distance.
 * It produces a {@link Desire} marked as mandatory.
 * </p>
 */
public class MandatoryLaneChangeChunk extends KnowledgeChunk
{

    /** Constructor.
     * @param vehicle
     * @throws OperationalPlanException */
    public MandatoryLaneChangeChunk(final MirovaTacticalPlanner vehicle) throws OperationalPlanException
    {
        super(vehicle);

        // Link procedural knowledge (possible maneuvers)
//        this.addManeuverPattern(() -> new DefaultLaneChangePattern(this, LateralDirectionality.LEFT));
//        this.addManeuverPattern(() -> new DefaultLaneChangePattern(this, LateralDirectionality.RIGHT));
    }

    @Override
    public boolean isApplicable() throws ParameterException
    {
        return true; // Always applicable
    }

    @Override

    /**
     * Computes LMRS-based mandatory lane change desire.
     * <p>
     * Step 1: compute the desire-to-leave for each relevant lane (LEFT, CURRENT, RIGHT).
     * Step 2: derive directional desires (to LEFT / to RIGHT) by comparing
     * the desire-to-leave values according to Schakel et al. (2012), Eq. 9.
     * </p>
     *
     * @return Desire(keep, left, right)
     * @throws ParameterException if parameters are missing
     */
    public Desire computeDesire() throws ParameterException {

     // --- Access Contexts (instead of direct perception) -----------------------

        EgoContext egoCtx = this.getMirovaTacticalPlanner()
                .getContext(EgoContext.class);

        Parameters p = getParameters();
        double v = egoCtx.getEgoSpeed().si;  // use context for speed

        // --- Step 1: desire-to-leave per lane (d_r,k) --------------------------
        // We'll store them in a small map for clarity.
        Map<RelativeLane, Double> dLeave = new HashMap<>();
        for (RelativeLane lane : new RelativeLane[]{RelativeLane.LEFT, RelativeLane.CURRENT, RelativeLane.RIGHT}) {
            double d_r = 0.0;
            for (LaneChangeInfo info : getInfrastructurePerception().getLegalLaneChangeInfo(lane)) {
                double x = info.remainingDistance().si;
                int n = info.numberOfLaneChanges();

                double d1 = 1.0 - x / (n * p.getParameter(ParameterTypes.LOOKAHEAD).si);
                double d2 = 1.0 - (x / v) / (n * p.getParameter(ParameterTypes.T0).si);
                double d = Math.max(d1, d2);

                d_r = Math.max(d_r, Math.max(0.0, d)); // clamp to [0,1]
            }
            dLeave.put(lane, d_r);
        }

        double dCurrent = dLeave.getOrDefault(RelativeLane.CURRENT, 0.0);
        double dLeftLane = dLeave.getOrDefault(RelativeLane.LEFT, 0.0);
        double dRightLane = dLeave.getOrDefault(RelativeLane.RIGHT, 0.0);

        // --- Step 2: directional desires via Eq. 9 -----------------------------
        double dLeft = 0.0;
        double dRight = 0.0;

        // LMRS Eq. 9: compare desire-to-leave current vs. target
        if (dCurrent > dLeftLane) {
            dLeft = dCurrent;                   // desire to change left
        } else if (dCurrent < dLeftLane) {
            dLeft = -dLeftLane;                 // undesired (target worse)
        }

        if (dCurrent > dRightLane) {
            dRight = dCurrent;                  // desire to change right
        } else if (dCurrent < dRightLane) {
            dRight = -dRightLane;               // undesired (target worse)
        }


        // --- Build Desire vector ------------------------------------------------
        return new Desire(dLeft, dRight, true);
    }


}
