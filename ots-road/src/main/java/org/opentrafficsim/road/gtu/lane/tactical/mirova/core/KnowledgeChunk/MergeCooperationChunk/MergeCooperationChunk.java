package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunk.MergeCooperationChunk;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunk.KnowledgeChunk;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.VehicleTypes.AbstractMirovaVehicle;

import java.util.function.Supplier;

/**
 * KnowledgeChunk representing cooperative behavior near merging areas.
 * <p>
 * When an adjacent lane (left or right) ends within the look-ahead distance,
 * the vehicle is encouraged to clear that lane by moving away from it
 * (i.e. leftward if the right lane ends, rightward if the left lane ends).
 * </p>
 */
public class MergeCooperationChunk extends KnowledgeChunk
{

    public MergeCooperationChunk(final AbstractMirovaVehicle vehicle) throws OperationalPlanException
    {
        super(vehicle);

        // procedural patterns for cooperative maneuvers
        this.addManeuverPattern(() -> createManeuverPattern("COOPERATIVE_LEFT"));
        this.addManeuverPattern(() -> createManeuverPattern("COOPERATIVE_RIGHT"));
    }

    @Override
    public boolean isApplicable() throws ParameterException
    {
        // TODO: check adjacent lanes for indicating -> indicating vehicle requests cooperation? -> ego only checks requests?
        InfrastructurePerception infra = getInfrastructurePerception();
        Length lookAheadDistance = getParameters().getParameter(ParameterTypes.LOOKAHEAD);
        if (infra.getPhysicalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.LEFT).lt(lookAheadDistance)
                || infra.getPhysicalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.RIGHT).lt(lookAheadDistance))

        {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public Desire computeDesire() throws ParameterException
    {
     // TODO: check adjacent lanes for indicating -> indicating vehicle requests cooperation? -> ego only checks requests?
        InfrastructurePerception infra = getInfrastructurePerception();
        Length lookAheadDistance = getParameters().getParameter(ParameterTypes.LOOKAHEAD);
        double dLeft = 0.0;
        double dRight = 0.0;
        if (infra.getPhysicalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.LEFT).lt(lookAheadDistance))
        {
            dRight = getAbstractMirovaVehicle().getDFree();
        }
        if (infra.getPhysicalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.RIGHT).lt(lookAheadDistance))
        {
            dLeft = getAbstractMirovaVehicle().getDFree();
        }

        this.desire = new Desire(0.0, dLeft, dRight, false);
        return this.desire;
    }

    // ----------------------------------------------------------------------
    // Helper: procedural pattern stubs
    // ----------------------------------------------------------------------

    private ManeuverPattern createManeuverPattern(final String direction)
    {
        return new ManeuverPattern()
        {
            @Override
            public void calculateActivation() throws ParameterException
            {
                // Cooperative changes have moderate activation by default
                setActivation(0.7);
            }

            @Override
            public String toString()
            {
                return "MergeCooperationPattern[" + direction + "]";
            }
        };
    }
}
