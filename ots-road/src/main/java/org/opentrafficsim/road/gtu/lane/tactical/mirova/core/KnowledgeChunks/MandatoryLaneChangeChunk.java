package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks;

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
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.AutobahnFreeDrivingPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.MandatoryLaneChangePattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.InfrastructureContext;
import org.opentrafficsim.road.network.LaneChangeInfo;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
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
        this.addManeuverPattern(() -> {return new MandatoryLaneChangePattern(this);});

        // Link procedural knowledge (possible maneuvers)
//        this.addManeuverPattern(() -> new DefaultLaneChangePattern(this, LateralDirectionality.LEFT));
//        this.addManeuverPattern(() -> new DefaultLaneChangePattern(this, LateralDirectionality.RIGHT));
    }

    @Override
    public boolean isApplicable() throws ParameterException
    {
        InfrastructureContext infraCtx = this.getMirovaTacticalPlanner()
                .getContext(InfrastructureContext.class);
        Length lookAheadDistance = infraCtx.getDistanceToLaneEnd();
        return lookAheadDistance.le(this.vehicle.getParameters().getParameter(MirovaParameters.mandatoryLaneChangeLookAheadDistance));
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
        EgoContext egoCtx = this.getMirovaTacticalPlanner().getContext(EgoContext.class);
        InfrastructurePerception infra = getInfrastructurePerception();
        LanePerception perception = this.getMirovaTacticalPlanner().getPerception();

        Parameters p = getParameters();
        double v = egoCtx.getEgoSpeed().si;  // use context for speed

        // --- Step 1: desire-to-leave per lane (d_r,k) --------------------------
        // We'll store them in a small map for clarity.
        Map<RelativeLane, Double> dLeave = new HashMap<>();
        for (RelativeLane lane : new RelativeLane[]{RelativeLane.LEFT, RelativeLane.CURRENT, RelativeLane.RIGHT}) {
            double d_r = 0.0;
            SortedSet<LaneChangeInfo> info = getInfrastructurePerception().getLegalLaneChangeInfo(lane);
            if (infra.getCrossSection().contains(lane)) {
                for (LaneChangeInfo lcInfo : info) {
                    double d = 0.0;
                    Length x = lcInfo.remainingDistance();
                    int n = lcInfo.numberOfLaneChanges();

                    double d1 = 1.0 - x.si / (n * p.getParameter(ParameterTypes.LOOKAHEAD).si);
                    double d2 = 1.0 - (x.si / v) / (n * p.getParameter(ParameterTypes.T0).si);
                    d = Math.max(d1, d2);

                    d_r = Math.max(d_r, Math.max(0.0, d)); // clamp to [0,1]
                }
            } else {
                continue; // skip lanes not present
            }

            dLeave.put(lane, d_r);
        }

        SortedSet<LaneChangeInfo> currentInfo = infra.getLegalLaneChangeInfo(RelativeLane.CURRENT);
        Length currentFirst = currentInfo.isEmpty() || currentInfo.first().numberOfLaneChanges() == 0 ? Length.POSITIVE_INFINITY
                : currentInfo.first().remainingDistance();
        Double dCurr = dLeave.getOrDefault(RelativeLane.CURRENT, 0.0);
        double dLeft = 0;
        if (perception.getLaneStructure().exists(RelativeLane.LEFT)
                && infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.LEFT).neg().lt(currentFirst))
        {
            // desire to leave left lane
            dLeft = dLeave.getOrDefault(RelativeLane.LEFT, 0.0);
            // desire to leave from current to left lane
            dLeft = dLeft < dCurr ? dCurr : dLeft > dCurr ? -dLeft : 0;
        }
        double dRigh = 0;
        if (perception.getLaneStructure().exists(RelativeLane.RIGHT) && infra
                .getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.RIGHT).neg().lt(currentFirst))
        {
            // desire to leave right lane
            dRigh = dLeave.getOrDefault(RelativeLane.RIGHT, 0.0);
            // desire to leave from current to right lane
            dRigh = dRigh < dCurr ? dCurr : dRigh > dCurr ? -dRigh : 0;
        }


        // --- Build Desire vector ------------------------------------------------
        return new Desire(dLeft, dRigh, true);
    }


}
