package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vfloat.scalar.FloatAcceleration;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/** Resulting deceleration in case of lane change to the right [m/s²]. */
public class ExtendedDataEgoDecelRight extends ExtendedDataAcceleration<GtuData>
{

    /** Single instance. */
    public static final ExtendedDataEgoDecelRight INSTANCE = new ExtendedDataEgoDecelRight();

    /**
     *
     */
    public ExtendedDataEgoDecelRight()
    {
        super("EgoDecelRight", "Resulting deceleration in case of lane change to the right [m/s²]");
    }

    /** Wert je GTU (Sampler-Einstiegspunkt). */
    @Override
    public FloatAcceleration getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            if (lgtu.getTacticalPlanner() instanceof MirovaTacticalPlanner p)
            {
                Acceleration decel = p.getContextManager().getCategory("Neighbors", NeighborsContext.class).getCachedValue(
                        NeighborsContext.EGO_DECEL_RIGHT, Acceleration.class);
                if (decel == null)
                {
                    return FloatAcceleration.instantiateSI(Float.NaN, AccelerationUnit.SI);
                }
                return FloatAcceleration.instantiateSI((float) decel.si, AccelerationUnit.SI);
            }
            else
            {
                return FloatAcceleration.instantiateSI(Float.NaN, AccelerationUnit.SI);
            }
        }
        return FloatAcceleration.instantiateSI(Float.NaN, AccelerationUnit.SI);
    }

    @Override
    public final String toString()
    {
        return "Resulting deceleration in case of lane change to the right [m/s²]";
    }
}
