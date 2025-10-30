package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunk.CongestionChunk;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.categories.TrafficPerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunk.KnowledgeChunk;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;

import java.util.function.Supplier;

/**
 * KnowledgeChunk representing behavior in congestion and merging queues.
 * <p>
 * - Reduces lane-change desires in stop-and-go or jam conditions.
 * - Temporarily increases desired time gap (capacity drop effect).
 * - Provides maneuver patterns for "zipper merge" behavior.
 * </p>
 */
public class CongestionChunk extends KnowledgeChunk
{
    private final Duration capacityDropRelaxationDuration = Duration.instantiateSI(20.0); // relaxation duration after leaving congestion

    private final double capacitDropGapIncreaseFactor = 1.2; // factor to increase time gap in congestion

    public CongestionChunk(final MirovaTacticalPlanner vehicle) throws OperationalPlanException
    {
        super(vehicle);


    }

    @Override
    public boolean isApplicable() throws ParameterException
    {
        TrafficPerception traffic = getTrafficPerception();
        if (getInfrastructurePerception().getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.LEFT).si> 0)
        {
            if (traffic.getSpeed(RelativeLane.CURRENT).si < 0.8 * getParameters().getParameter(ParameterTypes.VCONG).si
                && traffic.getSpeed(RelativeLane.LEFT).si < 0.8 * getParameters().getParameter(ParameterTypes.VCONG).si)

            {
                return true;
            }
        }
        else if (traffic.getSpeed(RelativeLane.CURRENT).si < 0.8 * getParameters().getParameter(ParameterTypes.VCONG).si)
        {
            return true;
        }

        return false;

    }

    @Override
    public Desire computeDesire() throws ParameterException
    {
        double dLeft = - getAbstractMirovaVehicle().getDFree(); // suppress lane changes
        double dRight = - getAbstractMirovaVehicle().getDFree(); // suppress lane changes

        this.desire = new Desire(dLeft, dRight, false);
        return this.desire;
    }

    public void adjustDrivingBehavior() throws ParameterException
    {
        if (getEgoPerception().getSpeed().lt(Speed.instantiateSI(5.0)))
        {
            getAbstractMirovaVehicle().setDesire(this.capacitDropGapIncreaseFactor, this.capacityDropRelaxationDuration);
        }
    }

}
