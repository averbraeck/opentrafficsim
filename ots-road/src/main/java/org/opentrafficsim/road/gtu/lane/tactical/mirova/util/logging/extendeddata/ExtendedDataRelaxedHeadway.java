package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vfloat.scalar.FloatDuration;
import org.djunits.value.vfloat.vector.FloatDurationVector;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataDuration;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataFloat;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataSpeed;
import org.opentrafficsim.kpi.sampling.data.ReferenceSpeed;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/** Relaxed headway T (in Sekunden). */
public class ExtendedDataRelaxedHeadway extends ExtendedDataDuration<GtuData>
{

    /** Single instance. */
    public static final ExtendedDataRelaxedHeadway INSTANCE = new ExtendedDataRelaxedHeadway();

    /**
     *
     */
    public ExtendedDataRelaxedHeadway()
    {
        super("RelaxedHeadway", "Current relaxed headway T [s]");
    }

    /** Wert je GTU (Sampler-Einstiegspunkt). */
    @Override
    public FloatDuration getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            if (lgtu.getTacticalPlanner() instanceof MirovaTacticalPlanner p && p.getCurrentRelaxedHeadway() != null)
            {
                return FloatDuration.instantiateSI((float) p.getCurrentRelaxedHeadway().si, DurationUnit.SI);
            }
            else
            {
                return FloatDuration.instantiateSI(Float.NaN, DurationUnit.SI);
            }
        }
        return FloatDuration.instantiateSI(Float.NaN, DurationUnit.SI);
    }

    @Override
    public final String toString()
    {
        return "Current relaxed headway T [s]";
    }
}
