package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.BusStopPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayBusStop;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.pt.BusSchedule;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 27 jan. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class AccelerationBusStop implements AccelerationIncentive
{

    /** Acceleration to ignore. */
    private static final Acceleration IGNORE = new Acceleration(Double.MAX_VALUE, AccelerationUnit.SI);

    /** Distance within which the bus can open the doors. */
    // TODO this is much more complex: tail blocking other traffic? other bus in front? many people at bus stop?
    private static final Length STOP_DISTANCE = new Length(15.0, LengthUnit.SI);

    /** Map of time per stop that we can leave or have left. */
    private final Map<String, Time> departures = new HashMap<>();

    /** {@inheritDoc} */
    @Override
    public Acceleration acceleration(final LaneBasedGTU gtu, final LanePerception perception,
            final CarFollowingModel carFollowingModel, final Speed speed, final BehavioralCharacteristics bc,
            final SpeedLimitInfo speedLimitInfo) throws OperationalPlanException, ParameterException, GTUException
    {
        SortedSet<HeadwayBusStop> stops = perception.getPerceptionCategory(BusStopPerception.class).getBusStops();
        if (stops.isEmpty())
        {
            return IGNORE;
        }
        BusSchedule busSchedule = (BusSchedule) gtu.getStrategicalPlanner().getRoute();
        for (HeadwayBusStop stop : stops)
        {
            String busStopId = stop.getId();
            if (busSchedule.isLineStop(busStopId))
            {

                // check when to leave
                Time now = gtu.getSimulator().getSimulatorTime().getTime();
                boolean standingStill = perception.getPerceptionCategory(EgoPerception.class).getSpeed().eq0()
                        && stop.getDistance().le(STOP_DISTANCE);
                if (!this.departures.containsKey(busStopId))
                {
                    if (standingStill)
                    {
                        // bus just stopped
                        Time departureTime = now.plus(busSchedule.getDwellTime(busStopId));
                        if (busSchedule.isForceSchedule(busStopId))
                        {
                            departureTime = Time.max(departureTime, busSchedule.getDepartureTime(busStopId));
                        }
                        this.departures.put(busStopId, departureTime);
                    }
                }

                // stop if not known yet, or before departure time
                if (!this.departures.containsKey(busStopId) || now.lt(this.departures.get(busStopId)))
                {
                    if (standingStill)
                    {
                        // stand still at location where stop was initiated
                        return Acceleration.ZERO;
                    }
                    // decelerate to initiate stop
                    return CarFollowingUtil.stop(carFollowingModel, bc, speed, speedLimitInfo, stop.getDistance());
                }
            }
        }

        return IGNORE;
    }

}
