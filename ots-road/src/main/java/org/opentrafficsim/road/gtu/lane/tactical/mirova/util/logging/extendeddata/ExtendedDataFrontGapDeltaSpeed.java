package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vfloat.scalar.FloatSpeed;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataSpeed;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/** Current speed delta to leading vehicle [m/s]: ego speed minus leader speed. */
public class ExtendedDataFrontGapDeltaSpeed extends ExtendedDataSpeed<GtuData>
{

    /** Single instance. */
    public static final ExtendedDataFrontGapDeltaSpeed INSTANCE = new ExtendedDataFrontGapDeltaSpeed();

    /**
     *
     */
    public ExtendedDataFrontGapDeltaSpeed()
    {
        super("FrontGapDeltaSpeed", "Current speed delta to leading vehicle [m/s]: ego speed minus leader speed");
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
                Speed deltaSpeed = p.getContextManager().getCategory("Neighbors", NeighborsContext.class).getCachedValue(
                        NeighborsContext.FRONT_GAP_DELTA_SPEED, Speed.class);
                if (deltaSpeed == null)
                {
                    return FloatSpeed.instantiateSI(Float.NaN, SpeedUnit.SI);
                }
                return FloatSpeed.instantiateSI((float) deltaSpeed.si, SpeedUnit.SI);
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
        return "Current speed delta to leading vehicle [m/s]: ego speed minus leader speed";
    }
}
