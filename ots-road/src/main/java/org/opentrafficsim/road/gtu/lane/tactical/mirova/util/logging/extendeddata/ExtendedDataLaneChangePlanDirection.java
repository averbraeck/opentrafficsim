package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataString;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/** Richtung des aktuellen LaneChange-Plans (LEFT/RIGHT/NONE). */
public class ExtendedDataLaneChangePlanDirection extends ExtendedDataString<GtuData>
{
    /** Single instance. */
    public static final ExtendedDataLaneChangePlanDirection INSTANCE = new ExtendedDataLaneChangePlanDirection();

    public ExtendedDataLaneChangePlanDirection()
    {
        super("PlanDirection", "Direction of lane change plan (LEFT/RIGHT/NONE)");
    }

    @Override
    public String getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            if (lgtu.getTacticalPlanner() instanceof MirovaTacticalPlanner p && p.getOperationalPlan() != null)
            {
                return p.getOperationalPlan().getLaneChangeDirection() != null
                        ? p.getOperationalPlan().getLaneChangeDirection().toString()
                        : "NONE";
            }
        }
        return "NONE";
    }

    @Override
    public String toString()
    {
        return "Direction of lane change plan (LEFT/RIGHT/NONE)";
    }
}
