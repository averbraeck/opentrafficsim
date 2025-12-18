package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vfloat.scalar.FloatDuration;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataDuration;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/** Target desired time headway of the current headway relaxation process[s]. */
public class ExtendedDataRelaxationTargetHeadway extends ExtendedDataDuration<GtuData>
{

    /** Single instance. */
    public static final ExtendedDataRelaxationTargetHeadway INSTANCE = new ExtendedDataRelaxationTargetHeadway();

    /**
     *
     */
    public ExtendedDataRelaxationTargetHeadway()
    {
        super("RelaxationTargetHeadway", "Target desired time headway of the current headway relaxation process[s]");
    }

    /** Wert je GTU (Sampler-Einstiegspunkt). */
    @Override
    public FloatDuration getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            if (lgtu.getTacticalPlanner() instanceof MirovaTacticalPlanner p)
            {
                Duration timeHeadway = p.getTargetDesiredHeadway();
                if (timeHeadway == null)
                {
                    return FloatDuration.instantiateSI(Float.NaN, DurationUnit.SI);
                }
                return FloatDuration.instantiateSI((float) timeHeadway.si, DurationUnit.SI);
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
        return "Target desired time headway of the current headway relaxation process[s]";
    }
}
