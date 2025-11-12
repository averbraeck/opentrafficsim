package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataString;
import org.opentrafficsim.kpi.sampling.data.ReferenceSpeed;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/** Aktueller Plan: LaneChange geplant? (true/false) */
public class ExtendedDataLaneChangePlan extends ExtendedDataString<GtuData>
{
    /** Single instance. */
    public static final ExtendedDataLaneChangePlan INSTANCE = new ExtendedDataLaneChangePlan();

    public ExtendedDataLaneChangePlan()
    {
        super("PlanIsLaneChange", "Whether current operational plan is a lane change");
    }

    @Override
    public String getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            if (lgtu.getTacticalPlanner() instanceof MirovaTacticalPlanner p && p.getOperationalPlan() != null)
            {
                return "true";
            }
        }
        return "false";
    }

    @Override
    public final String toString()
    {
        return "Whether current operational plan is a lane change";
    }
}
