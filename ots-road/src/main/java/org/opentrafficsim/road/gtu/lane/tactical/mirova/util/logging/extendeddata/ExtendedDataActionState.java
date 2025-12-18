package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataString;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/** Aktueller MIROVA-ActionState. */
public class ExtendedDataActionState extends ExtendedDataString<GtuData>
{
    /** Single instance. */
    public static final ExtendedDataActionState INSTANCE = new ExtendedDataActionState();

    /**
     *
     */
    public ExtendedDataActionState()
    {
        super("ActionState", "Current MIROVA ActionState");
    }

    /** Wert je GTU (Sampler-Einstiegspunkt). */
    @Override
    public String getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            if (lgtu.getTacticalPlanner() instanceof MirovaTacticalPlanner p && p.getCurrentActionState() != null)
            {
                return p.getCurrentActionState().toString();
            }
        }
        return "none";
    }

    @Override
    public String toString()
    {
        return "Current MIROVA ActionState";
    }
}
