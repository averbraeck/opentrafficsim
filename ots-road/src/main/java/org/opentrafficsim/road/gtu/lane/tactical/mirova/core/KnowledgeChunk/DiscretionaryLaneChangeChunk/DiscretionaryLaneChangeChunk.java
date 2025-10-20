package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunk.DiscretionaryLaneChangeChunk;

import org.djunits.unit.DimensionlessUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Dimensionless;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.TrafficPerception;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunk.KnowledgeChunk;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.VehicleTypes.AbstractMirovaVehicle;
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


    public DiscretionaryLaneChangeChunk(final AbstractMirovaVehicle vehicle) throws OperationalPlanException
    {
        super(vehicle);

        // procedural maneuvers possibly triggered by discretionary desires
        this.addManeuverPattern(() -> createLaneChangePattern("LEFT"));
        this.addManeuverPattern(() -> createLaneChangePattern("RIGHT"));
    }

    @Override
    public boolean isApplicable() throws ParameterException
    {
        InfrastructurePerception infra = getInfrastructurePerception();
        return true; // Always applicable
    }

    @Override
    public Desire computeDesire() throws ParameterException
    {
        Speed vGain = getAbstractMirovaVehicle().getVGain();

        double leftDist = getInfrastructurePerception().getLegalLaneChangePossibility(RelativeLane.CURRENT,
                LateralDirectionality.LEFT).si;
        double rightDist = getInfrastructurePerception().getLegalLaneChangePossibility(RelativeLane.CURRENT,
                LateralDirectionality.RIGHT).si;
        Dimensionless aGain;
        Speed vCur = getTrafficPerception().getSpeed(RelativeLane.CURRENT);

        Acceleration aCur = getEgoPerception().getAcceleration();

        aCur = Try.assign(() -> getAbstractMirovaVehicle().getGtu().getCarFollowingAcceleration(), "Could not obtain the GTU.");
        if (aCur.si > 0)
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
            Speed vLeft = getTrafficPerception().getSpeed(RelativeLane.LEFT);
            dLeft = aGain.si * (vLeft.si - vCur.si) / vGain.si;
        }
        else
        {
            dLeft = 0.0;
        }

        // right desire
         double dRight;
         if (rightDist > 0.0 && getInfrastructurePerception().getCrossSection().contains(RelativeLane.RIGHT))
         {
         Speed vRight = getTrafficPerception().getSpeed(RelativeLane.RIGHT);
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

         double dFree = getAbstractMirovaVehicle().getDFree(); // getAbstractMirovaVehicle().getGtu().getParameters().getParameter(DFREE);

         Parameters params = getParameters();

         SortedSet<LaneChangeInfo> rightLaneLCInfo =
                 getInfrastructurePerception().getLegalLaneChangeInfo(RelativeLane.RIGHT);
         Length rightLaneLCRemainingDistance =
                 rightLaneLCInfo.isEmpty() || rightLaneLCInfo.first().numberOfLaneChanges() == 0 ? Length.POSITIVE_INFINITY
                         : rightLaneLCInfo.first().remainingDistance();

         if (getTrafficPerception().getSpeed(RelativeLane.RIGHT).ge(getAbstractMirovaVehicle().getGtu().getDesiredSpeed()) && rightLaneLCRemainingDistance
                 .ge(params.getParameter(ParameterTypes.LOOKAHEAD)))
         {
             dRight = dRight + dFree;
         }



        double dKeep = 0.0;
        this.desire = new Desire(dKeep, dLeft, dRight, false);
        return this.desire;
    }

    // ----------------------------------------------------------------------
    // helper: procedural knowledge stubs
    // ----------------------------------------------------------------------

    private ManeuverPattern createLaneChangePattern(final String direction)
    {
        return new ManeuverPattern()
        {
            @Override
            public void calculateActivation() throws ParameterException
            {
                setActivation(0.5); // moderate activation for discretionary changes
            }

            @Override
            public String toString()
            {
                return "DiscretionaryLaneChangePattern[" + direction + "]";
            }
        };
    }
}
