package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataString;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/** Indikator, ob das Fahrzeug gerade den Fahrstreifen wechselt. */
public class ExtendedDataIsChangingLane extends ExtendedDataString<GtuData>
{
    /** Single instance. */
    public static final ExtendedDataIsChangingLane INSTANCE = new ExtendedDataIsChangingLane();

    public ExtendedDataIsChangingLane()
    {
        super("IsChangingLane", "true/false indicator for active lane change");
    }

    /** Wert je GTU (Sampler-Einstiegspunkt). */
    @Override
    public String getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            if (lgtu.getTacticalPlanner() instanceof MirovaTacticalPlanner p)
            {
                return Boolean.toString(p.getLaneChange().isChangingLane());
            }
        }
        return "false";
    }

    @Override
    public String toString()
    {
        return "true/false indicator for active lane change";
    }
}
