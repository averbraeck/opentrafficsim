package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vfloat.scalar.FloatSpeed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataSpeed;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/** Current desired speed according to car-following model [m/s]. */
public class ExtendedDataCurrentDesiredSpeed extends ExtendedDataSpeed<GtuData>
{

    /** Single instance. */
    public static final ExtendedDataCurrentDesiredSpeed INSTANCE = new ExtendedDataCurrentDesiredSpeed();

    /**
     *
     */
    public ExtendedDataCurrentDesiredSpeed()
    {
        super("CurrentDesiredSpeed", "Current desired speed according to car-following model [m/s]");
    }

    /** Wert je GTU (Sampler-Einstiegspunkt). */
    @Override
    public FloatSpeed getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            if (lgtu.getTacticalPlanner() instanceof MirovaTacticalPlanner p)
            {
                Speed desiredSpeed = Speed.NaN;
                try
                {
                    desiredSpeed = p.getContextManager().getCategory("Ego", EgoContext.class).getCurrentDesiredSpeed();
                }
                catch (ParameterException | GtuException | NetworkException exception)
                {
                    exception.printStackTrace();
                }
                if (desiredSpeed == null)
                {
                    return FloatSpeed.instantiateSI(Float.NaN, SpeedUnit.SI);
                }
                return FloatSpeed.instantiateSI((float) desiredSpeed.si, SpeedUnit.SI);
            }
            else
            {
                return FloatSpeed.instantiateSI(Float.NaN, SpeedUnit.SI);
            }
        }
        return FloatSpeed.instantiateSI(Float.NaN, SpeedUnit.SI);
    }

    @Override
    public final String toString()
    {
        return "Current desired speed according to car-following model [m/s]";
    }
}
