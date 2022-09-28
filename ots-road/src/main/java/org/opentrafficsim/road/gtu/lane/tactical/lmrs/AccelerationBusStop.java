package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.FilteredIterable;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.BusStopPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayBusStop;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.pt.BusSchedule;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.network.lane.object.BusStop;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class AccelerationBusStop implements AccelerationIncentive
{

    /** Distance within which the bus can open the doors. */
    // TODO this process is much more complex: tail blocking other traffic? other bus in front? many people at bus stop?
    private static final Length STOP_DISTANCE = new Length(15.0, LengthUnit.SI);

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:parameternumber")
    public final void accelerate(final SimpleOperationalPlan simplePlan, final RelativeLane lane, final Length mergeDistance,
            final LaneBasedGTU gtu, final LanePerception perception, final CarFollowingModel carFollowingModel,
            final Speed speed, final Parameters params, final SpeedLimitInfo speedLimitInfo)
            throws OperationalPlanException, ParameterException, GtuException
    {
        PerceptionCollectable<HeadwayBusStop, BusStop> stops =
                perception.getPerceptionCategory(BusStopPerception.class).getBusStops();
        if (stops.isEmpty())
        {
            return;
        }
        BusSchedule busSchedule = (BusSchedule) gtu.getStrategicalPlanner().getRoute();
        Time now = gtu.getSimulator().getSimulatorAbsTime();
        Iterable<HeadwayBusStop> it = lane.isCurrent() ? stops : new FilteredIterable<>(stops, (
                busStop
        ) ->
        { return busStop.getDistance().gt(mergeDistance); });
        for (HeadwayBusStop stop : it)
        {
            String busStopId = stop.getId();
            if (busSchedule.isLineStop(busStopId, now))
            {

                // check when to leave
                boolean stoppedAtStop = stop.getRelativeLane().isCurrent() && stop.getDistance().le(STOP_DISTANCE)
                        && perception.getPerceptionCategory(EgoPerception.class).getSpeed().eq0();
                if (busSchedule.getActualDepartureBusStop(busStopId) == null)
                {
                    if (stoppedAtStop)
                    {
                        // bus just stopped
                        Time departureTime = now.plus(busSchedule.getDwellTime(busStopId));
                        if (busSchedule.isForceSchedule(busStopId))
                        {
                            departureTime = Time.max(departureTime, busSchedule.getDepartureTime(busStopId));
                        }
                        busSchedule.setActualDeparture(busStopId, stop.getConflictIds(), departureTime);
                    }
                }

                // stop if not known yet, or before departure time
                if (busSchedule.getActualDepartureBusStop(busStopId) == null
                        || now.lt(busSchedule.getActualDepartureBusStop(busStopId)))
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

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "AccelerationBusStop";
    }

}
