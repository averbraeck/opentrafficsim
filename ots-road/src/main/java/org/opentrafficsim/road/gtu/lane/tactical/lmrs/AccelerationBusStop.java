package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.util.Optional;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.FilteredIterable;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.BusStopPerception;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedBusStop;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.pt.BusSchedule;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.network.lane.object.BusStop;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Bus stop acceleration incentive.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class AccelerationBusStop implements AccelerationIncentive, Stateless<AccelerationIncentive>
{

    /** Distance within which the bus can open the doors. */
    // TODO this process is much more complex: tail blocking other traffic? other bus in front? many people at bus stop?
    private static final Length STOP_DISTANCE = new Length(15.0, LengthUnit.SI);

    /** Singleton instance. */
    public static final AccelerationBusStop SINGLETON = new AccelerationBusStop();

    @Override
    public AccelerationBusStop get()
    {
        return SINGLETON;
    }

    /**
     * Constructor.
     */
    private AccelerationBusStop()
    {
        //
    }

    @Override
    @SuppressWarnings("checkstyle:parameternumber")
    public void accelerate(final SimpleOperationalPlan simplePlan, final RelativeLane lane, final Length mergeDistance,
            final LaneBasedGtu gtu, final LanePerception perception, final CarFollowingModel carFollowingModel,
            final Speed speed, final Parameters params, final SpeedLimitInfo speedLimitInfo)
            throws ParameterException, GtuException
    {
        PerceptionCollectable<PerceivedBusStop, BusStop> stops =
                perception.getPerceptionCategory(BusStopPerception.class).getBusStops();
        if (stops.isEmpty())
        {
            return;
        }
        BusSchedule busSchedule = (BusSchedule) gtu.getStrategicalPlanner().getRoute().orElseThrow(
                () -> new GtuException("Unable to determine acceleration for bus stops for bus without bus schedule."));
        Duration now = gtu.getSimulator().getSimulatorTime();
        Iterable<PerceivedBusStop> it = lane.isCurrent() ? stops : new FilteredIterable<>(stops, (busStop) ->
        {
            return busStop.getDistance().gt(mergeDistance);
        });
        for (PerceivedBusStop stop : it)
        {
            String busStopId = stop.getId();
            if (busSchedule.isLineStop(busStopId, now))
            {

                // check when to leave
                boolean stoppedAtStop = stop.getRelativeLane().isCurrent() && stop.getDistance().le(STOP_DISTANCE)
                        && perception.getPerceptionCategory(EgoPerception.class).getSpeed().eq0();
                if (busSchedule.getActualDepartureBusStop(busStopId).isEmpty())
                {
                    if (stoppedAtStop)
                    {
                        // bus just stopped
                        Duration departureTime = now.plus(busSchedule.getDwellTime(busStopId));
                        if (busSchedule.isForceSchedule(busStopId))
                        {
                            departureTime = Duration.max(departureTime, busSchedule.getDepartureTime(busStopId));
                        }
                        busSchedule.setActualDeparture(busStopId, stop.getConflictIds(), departureTime);
                    }
                }

                // stop if not known yet, or before departure time
                Optional<Duration> actualDeparture = busSchedule.getActualDepartureBusStop(busStopId);
                if (actualDeparture.isEmpty() || now.lt(actualDeparture.get()))
                {
                    if (stoppedAtStop)
                    {
                        // stand still at location where stop was initiated
                        simplePlan.minimizeAcceleration(Acceleration.ZERO);
                    }
                    else
                    {
                        // decelerate to initiate stop
                        simplePlan.minimizeAcceleration(
                                CarFollowingUtil.stop(carFollowingModel, params, speed, speedLimitInfo, stop.getDistance()));
                    }
                }
            }
        }
    }

    @Override
    public String toString()
    {
        return "AccelerationBusStop";
    }

}
