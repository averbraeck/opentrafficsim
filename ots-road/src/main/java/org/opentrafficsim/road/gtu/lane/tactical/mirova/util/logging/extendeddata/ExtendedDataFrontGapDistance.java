package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vfloat.scalar.FloatLength;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataLength;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/** Relaxed headway T (in Sekunden). */
public class ExtendedDataFrontGapDistance extends ExtendedDataLength<GtuData>
{

    /** Single instance. */
    public static final ExtendedDataFrontGapDistance INSTANCE = new ExtendedDataFrontGapDistance();

    /**
     *
     */
    public ExtendedDataFrontGapDistance()
    {
        super("FrontGapDistance", "Current front gap distance headway to leading vehicle [m]");
    }

    /** Wert je GTU (Sampler-Einstiegspunkt). */
    @Override
    public FloatLength getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            if (lgtu.getTacticalPlanner() instanceof MirovaTacticalPlanner p)
            {
                Length distanceHeadway = p.getContextManager().getCategory("Neighbors", NeighborsContext.class).getCachedValue(NeighborsContext.FRONT_GAP_DISTANCE_CURRENT, Length.class);
                if (distanceHeadway == null)
                {
                    return FloatLength.instantiateSI(Float.NaN, LengthUnit.SI);
                }
                return FloatLength.instantiateSI((float) distanceHeadway.si, LengthUnit.SI);
            }
            else
            {
                return FloatLength.instantiateSI(Float.NaN, LengthUnit.SI);
            }
        }
        return FloatLength.instantiateSI(Float.NaN, LengthUnit.SI);
    }

    @Override
    public final String toString()
    {
        return "Current front gap distance headway to leading vehicle [m]";
    }
}
