package org.opentrafficsim.road.gtu.tactical.lmrs;

import java.util.Optional;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.road.gtu.perception.FilteredIterable;
import org.opentrafficsim.road.gtu.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.categories.BusStopPerception;
import org.opentrafficsim.road.gtu.perception.object.PerceivedBusStop;
import org.opentrafficsim.road.gtu.tactical.TacticalContextEgo;
import org.opentrafficsim.road.gtu.tactical.pt.BusSchedule;
import org.opentrafficsim.road.gtu.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.network.object.BusStop;

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
    public Acceleration accelerate(final TacticalContextEgo context, final RelativeLane lane, final Length mergeDistance)
            throws ParameterException, GtuException
    {
        PerceptionCollectable<PerceivedBusStop, BusStop> stops =
                context.getPerception().getPerceptionCategory(BusStopPerception.class).getBusStops();
        if (stops.isEmpty())
        {
            return NO_REASON;
        }
        BusSchedule busSchedule = (BusSchedule) context.getRoute().orElseThrow(
                () -> new GtuException("Unable to determine acceleration for bus stops for bus without bus schedule."));
        Duration now = context.getTime();
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
                boolean stoppedAtStop =
                        stop.getRelativeLane().isCurrent() && stop.getDistance().le(STOP_DISTANCE) && context.getSpeed().eq0();
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
                        return Acceleration.ZERO;
                    }
                    else
                    {
                        // decelerate to initiate stop
                        return CarFollowingUtil.stop(context, stop.getDistance());
                    }
                }
            }
        }
        return NO_REASON;
    }

    @Override
    public String toString()
    {
        return "AccelerationBusStop";
    }

}
