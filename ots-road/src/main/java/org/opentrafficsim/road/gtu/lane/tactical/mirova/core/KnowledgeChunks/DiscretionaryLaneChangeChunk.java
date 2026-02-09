package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks;

import org.djunits.unit.DimensionlessUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Dimensionless;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.TrafficPerception;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.parallel.PreventUndercuttingPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.MacroTrafficContext;
import org.opentrafficsim.road.network.LaneChangeInfo;

import java.util.SortedSet;
import java.util.function.Supplier;

/**
 * KnowledgeChunk representing discretionary lane change incentives (speed gain, keep-right).
 * <p>
 * This chunk produces desires toward faster or more comfortable lanes.
 * Based on Schakel et al. (2012) – LMRS Equations (6–7) and (10).
 * </p>
 */
public class DiscretionaryLaneChangeChunk extends KnowledgeChunk
{


    public DiscretionaryLaneChangeChunk(final MirovaTacticalPlanner vehicle) throws OperationalPlanException, ParameterException
    {
        super(vehicle);

//        // procedural maneuvers possibly triggered by discretionary desires
//        this.addManeuverPattern(() -> {
//            try
//            {
//                return new AutobahnFreeDrivingPattern(this);
//            }
//            catch (ParameterException exception)
//            {
//                exception.printStackTrace();
//            }
//            return null;
//        });
//        this.addManeuverPattern(() -> {
//            try
//            {
//                return new PreventUndercuttingPattern(this);
//            }
//            catch (ParameterException exception)
//            {
//                exception.printStackTrace();
//            }
//            return null;
//        });
//        this.addManeuverPattern(() -> {
//            try
//            {
//                return new DiscretionaryLaneChangePattern(this);
//            }
//            catch (ParameterException exception)
//            {
//                exception.printStackTrace();
//            }
//            return null;
//        });

    }

    @Override
    public boolean isApplicable() throws ParameterException
    {
        return true; // Always applicable
    }

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

        if (vCur.si < getParameters().getParameter(ParameterTypes.VCONG).si)
        {
            // lower speed gain incentive in congested situations
            vGain = vGain.times(1.3);
        }


        Acceleration aCur = egoContext.getCurrentCarFollowingAcceleration();
        if (aCur.si > 0 & egoContext.getEgoSpeed().si > vCong.si)
        {
            Acceleration a = getParameters().getParameter(ParameterTypes.A);
            aGain = a.minus(aCur).divide(a);
        }
        else
        {
            aGain = new Dimensionless(1, DimensionlessUnit.SI);
        }

        // left desire
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
        // right desire
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
         double dFree = getMirovaTacticalPlanner().getDFree(); // getAbstractMirovaVehicle().getGtu().getParameters().getParameter(DFREE);
         Parameters params = getParameters();
//         SortedSet<LaneChangeInfo> rightLaneLCInfo =
//                 getInfrastructurePerception().getLegalLaneChangeInfo(RelativeLane.RIGHT);
//         System.out.println("Right lane LC info: " + rightLaneLCInfo.toString());
//         Length rightLaneLCRemainingDistance =
//                 rightLaneLCInfo.isEmpty() || rightLaneLCInfo.first().numberOfLaneChanges() == 0 ? Length.POSITIVE_INFINITY
//                         : rightLaneLCInfo.first().remainingDistance();
//


         if (rightDist > 0.0)
         {
             Speed rightSpeed = macroContext.getAverageSpeed(RelativeLane.RIGHT);
             if (rightSpeed.ge(getMirovaTacticalPlanner().getGtu().getDesiredSpeed())
                     && Length.instantiateSI(rightDist).ge(params.getParameter(ParameterTypes.LOOKAHEAD))
                     && rightSpeed.gt(vCong))
             {
                 dRight = dRight + this.vehicle.getParameters().getParameter(MirovaParameters.DFREE);
                                 //*  this.vehicle.getTimeSinceLastLaneChange().si / this.vehicle.getParameters().getParameter(MirovaParameters.socialInteractionCooldown).si)

             }

         }


        this.desire = new Desire(dLeft, dRight, false);
        return this.desire;
    }


}
