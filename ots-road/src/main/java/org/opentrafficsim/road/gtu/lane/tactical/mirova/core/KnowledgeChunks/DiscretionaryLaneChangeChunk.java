package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks;

import org.djunits.unit.DimensionlessUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Dimensionless;
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
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.MacroTrafficContext;

/**
 * KnowledgeChunk representing discretionary lane change incentives (speed gain, keep-right).
 * <p>
 * This component forms part of <b>Layer 2 (Cognition / Motivation)</b> in the MiRoVA architecture.
 * It produces desires toward faster or more comfortable lanes based on macroscopic traffic states
 * and ego vehicle dynamics. The logic is largely based on Schakel et al. (2012) – LMRS Equations (6–7) and (10).
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class DiscretionaryLaneChangeChunk extends KnowledgeChunk
{

    /**
     * Constructs a new DiscretionaryLaneChangeChunk.
     *
     * @param vehicle the tactical planner governing the ego agent
     * @throws OperationalPlanException if the planner cannot be initialized
     * @throws ParameterException       if required parameters are missing
     */
    public DiscretionaryLaneChangeChunk(final MirovaTacticalPlanner vehicle) throws OperationalPlanException, ParameterException
    {
        super(vehicle);
    }

    /**
     * Determines if the discretionary lane change logic is applicable.
     * <p>
     * Discretionary lane changes for speed gain or keeping right are generally always applicable
     * during normal driving operations.
     * </p>
     *
     * @return {@code true} as this chunk is always applicable
     * @throws ParameterException if parameter evaluation fails
     */
    @Override
    public boolean isApplicable() throws ParameterException
    {
        return true; // Always applicable
    }

    /**
     * Computes the discretionary desire to change lanes for speed gains or to adhere to a keep-right rule.
     *
     * @return a {@link Desire} object containing discretionary desire values for left and right directions
     * @throws ParameterException if required parameters for the calculation are missing
     * @throws GtuException       if GTU state cannot be accessed
     * @throws NetworkException   if network state cannot be accessed
     */
    @Override
    public Desire computeDesire() throws ParameterException, GtuException, NetworkException
    {
        Speed vGain = getMirovaTacticalPlanner().getVGain();
        Speed vCong = getParameters().getParameter(ParameterTypes.VCONG);
        MacroTrafficContext macroContext = getMirovaTacticalPlanner().getContext(MacroTrafficContext.class);
        EgoContext egoContext = getMirovaTacticalPlanner().getContext(EgoContext.class);

        double leftDist = getInfrastructurePerception().getLegalLaneChangePossibility(RelativeLane.CURRENT,
                LateralDirectionality.LEFT).si;
        double rightDist = getInfrastructurePerception().getLegalLaneChangePossibility(RelativeLane.CURRENT,
                LateralDirectionality.RIGHT).si;

        Dimensionless aGain;
        Speed vCur = macroContext.getAverageSpeed(RelativeLane.CURRENT);

        // Adjust speed gain threshold in congested situations
        if (vCur.si < getParameters().getParameter(ParameterTypes.VCONG).si)
        {
            vGain = vGain.times(1.3);
        }

        Acceleration aCur = egoContext.getCurrentCarFollowingAcceleration();

        // Use logical AND (&&) instead of bitwise AND (&)
        if (aCur.si > 0 && egoContext.getEgoSpeed().si > vCong.si)
        {
            Acceleration a = getParameters().getParameter(ParameterTypes.A);
            aGain = a.minus(aCur).divide(a);
        }
        else
        {
            aGain = new Dimensionless(1, DimensionlessUnit.SI);
        }

        // ---------------------------------------------------------
        // Left Desire Computation (Speed Gain)
        // ---------------------------------------------------------
        double dLeft;
        if (leftDist > 0.0 && getInfrastructurePerception().getCrossSection().contains(RelativeLane.LEFT))
        {
            Speed vLeft = macroContext.getAverageSpeed(RelativeLane.LEFT);
            dLeft = aGain.si * (vLeft.si - vCur.si) / vGain.si;

            if (vLeft.si >= vCong.si)
            {
                // lower speed gain incentive if target lane is congested
                dLeft = dLeft * 0.7;
            }
        }
        else
        {
            dLeft = 0.0;
        }

        // ---------------------------------------------------------
        // Right Desire Computation (Speed Gain & Keep Right)
        // ---------------------------------------------------------
        double dRight;
        if (rightDist > 0.0 && getInfrastructurePerception().getCrossSection().contains(RelativeLane.RIGHT))
        {
            Speed vRight = macroContext.getAverageSpeed(RelativeLane.RIGHT);
            // no speed gain incentive to the right lane in non-congested situations
            if (vCur.si >= getParameters().getParameter(ParameterTypes.VCONG).si)
            {
                dRight = aGain.si * Math.min(vRight.si - vCur.si, 0) / vGain.si;
            }
            else
            {
                dRight = aGain.si * (vRight.si - vCur.si) / vGain.si;
            }
        }
        else
        {
            dRight = 0.0;
        }

        Parameters params = getParameters();

        // Keep-right incentive (Autobahn logic)
        if (rightDist > 0.0)
        {
            Speed rightSpeed = macroContext.getAverageSpeed(RelativeLane.RIGHT);
            if (rightSpeed.ge(getMirovaTacticalPlanner().getGtu().getDesiredSpeed())
                    && Length.instantiateSI(rightDist).ge(params.getParameter(ParameterTypes.LOOKAHEAD))
                    && rightSpeed.gt(vCong))
            {
                dRight = dRight + getMirovaTacticalPlanner().getParameters().getParameter(MirovaParameters.DFREE);
            }
        }

        // Create and return the computed non-mandatory desire
        this.desire = new Desire(dLeft, dRight, false);
        return this.desire;
    }
}