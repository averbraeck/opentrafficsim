package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.categories.BusStopPerception;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedBusStop;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.pt.BusSchedule;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.MandatoryIncentive;
import org.opentrafficsim.road.network.lane.object.BusStop;

/**
 * Mandatory lane change incentive for bus stops.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class IncentiveBusStop implements MandatoryIncentive, Stateless<IncentiveBusStop>
{

    /** Singleton instance. */
    public static final IncentiveBusStop SINGLETON = new IncentiveBusStop();

    @Override
    public IncentiveBusStop get()
    {
        return SINGLETON;
    }

    /**
     * Constructor.
     */
    private IncentiveBusStop()
    {
        //
    }

    @Override
    public Desire determineDesire(final Parameters parameters, final LanePerception perception,
            final CarFollowingModel carFollowingModel, final Desire mandatoryDesire)
            throws ParameterException, OperationalPlanException
    {
        PerceivedBusStop firstStop = null;
        PerceptionCollectable<PerceivedBusStop, BusStop> stops =
                perception.getPerceptionCategory(BusStopPerception.class).getBusStops();
        Time now = perception.getGtu().getSimulator().getSimulatorAbsTime();
        for (PerceivedBusStop stop : stops)
        {
            if (((BusSchedule) perception.getGtu().getStrategicalPlanner().getRoute()).isLineStop(stop.getId(), now))
            {
                firstStop = stop;
                break;
            }
        }

        if (firstStop == null)
        {
            return new Desire(0, 0);
        }
        Speed speed = perception.getPerceptionCategory(EgoPerception.class).getSpeed();
        if (firstStop.getRelativeLane().isCurrent())
        {
            double d = -IncentiveRoute.getDesireToLeave(parameters, firstStop.getDistance(), 1, speed);
            return new Desire(d, d);
        }

        int n = firstStop.getRelativeLane().getNumLanes();

        double dNotGood = -IncentiveRoute.getDesireToLeave(parameters, firstStop.getDistance(), n + 1, speed);
        double dGood = IncentiveRoute.getDesireToLeave(parameters, firstStop.getDistance(), n, speed);
        return firstStop.getRelativeLane().getLateralDirectionality().isRight() ? new Desire(dNotGood, dGood)
                : new Desire(dGood, dNotGood);

    }

    @Override
    public String toString()
    {
        return "IncentiveBusStop";
    }

}
