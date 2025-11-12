package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vfloat.scalar.FloatAcceleration;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/** Acceleration according to current car-following model [m/s²]. */
public class ExtendedDataCurrentCFAcceleration extends ExtendedDataAcceleration<GtuData>
{

    /** Single instance. */
    public static final ExtendedDataCurrentCFAcceleration INSTANCE = new ExtendedDataCurrentCFAcceleration();

    /**
     *
     */
    public ExtendedDataCurrentCFAcceleration()
    {
        super("CurrentCFAcceleration", "Acceleration according to current car-following model [m/s²]");
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
                Acceleration acc = Acceleration.NaN;
                acc = p.getContextManager().getCategory("Ego", EgoContext.class).getCachedValue(EgoContext.CURRENT_CF_ACCELERATION, Acceleration.class);

                if (acc == null)
                {
                    return FloatAcceleration.instantiateSI(Float.NaN, AccelerationUnit.SI);
                }
                return FloatAcceleration.instantiateSI((float) acc.si, AccelerationUnit.SI);
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
        return "Acceleration according to current car-following model [m/s²]";
    }
}
