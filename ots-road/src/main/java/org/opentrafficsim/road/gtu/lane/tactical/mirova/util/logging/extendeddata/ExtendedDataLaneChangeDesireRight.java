package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.djunits.unit.DimensionlessUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.value.vfloat.scalar.FloatDimensionless;
import org.djunits.value.vfloat.scalar.FloatDuration;
import org.djunits.value.vfloat.vector.FloatDimensionlessVector;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataFloat;
import org.opentrafficsim.kpi.sampling.data.ReferenceSpeed;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/** Desire für Fahrstreifenwechsel nach rechts. */
public class ExtendedDataLaneChangeDesireRight extends ExtendedDataDesire<GtuData>
{
    /** Single instance. */
    public static final ExtendedDataLaneChangeDesireRight INSTANCE = new ExtendedDataLaneChangeDesireRight();

    /**
     *
     */
    public ExtendedDataLaneChangeDesireRight()
    {
        super("LaneChangeDesireRight", "Desire for lane change to the right (not really in seconds, but dimensionless)");
    }

    @Override
    public FloatDuration getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            if (lgtu.getTacticalPlanner() instanceof MirovaTacticalPlanner p)
            {
                double val = p.getLaneChangeDesire() != null ? p.getLaneChangeDesire().getRight() : Double.NaN;
                return FloatDuration.instantiateSI((float) val, DurationUnit.SI);
            }
        }
        return FloatDuration.instantiateSI(Float.NaN, DurationUnit.SI);
    }

    @Override
    public final String toString()
    {
        return "Desire for lane change to the right (not really in seconds, but dimensionless)";
    }
}
