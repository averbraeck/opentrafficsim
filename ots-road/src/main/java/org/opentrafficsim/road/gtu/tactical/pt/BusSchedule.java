package org.opentrafficsim.road.gtu.tactical.pt;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;

/**
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class BusSchedule extends Route
{

    /** Line of the bus schedule. */
    private final String line;

    /** List of bus stops. */
    private final Map<String, BusStopInfo> schedule = new LinkedHashMap<>();

    /** Map of actual departures stored per bus stop. */
    private final Map<String, Duration> actualDeparturesBusStop = new LinkedHashMap<>();

    /** Map of actual departures stored per conflict. */
    private final Map<String, Duration> actualDeparturesConflict = new LinkedHashMap<>();

    /**
     * Constructor.
     * @param id id
     * @param gtuType the GtuType for which this is a route
     * @param nodes nodes
     * @param line line of the bus schedule
     * @throws NetworkException if intermediate nodes are missing in the route.
     */
    public BusSchedule(final String id, final GtuType gtuType, final List<Node> nodes, final String line)
            throws NetworkException
    {
        super(id, gtuType, nodes);
        this.line = line;
    }

    /**
     * Constructor.
     * @param id id
     * @param gtuType the GtuType for which this is a route
     * @param line line of the bus schedule
     */
    public BusSchedule(final String id, final GtuType gtuType, final String line)
    {
        super(id, gtuType);
        this.line = line;
    }

    /**
     * Adds a stop to the schedule.
     * @param busStopId bus stop id
     * @param departureTime departure time
     * @param dwellTime dwell time
     * @param forceSchedule whether to wait until departure time
     */
    public void addBusStop(final String busStopId, final Duration departureTime, final Duration dwellTime,
            final boolean forceSchedule)
    {
        Throw.whenNull(busStopId, "Bus stop id may not be null.");
        Throw.whenNull(departureTime, "Departure time may not be null.");
        Throw.whenNull(dwellTime, "Dwell time may not be null.");
        this.schedule.put(busStopId, new BusStopInfo(departureTime, dwellTime, forceSchedule));
    }

    /**
     * Whether the bus of this line should stop for this bus stop. False if not the correct line, or already stopped.
     * @param busStopId id of bus stop
     * @param time time to check
     * @return whether the bus of this line should stop for this bus stop
     */
    public boolean isLineStop(final String busStopId, final Duration time)
    {
        return this.schedule.containsKey(busStopId) && (!this.actualDeparturesConflict.containsKey(busStopId)
                || time.lt(this.actualDeparturesConflict.get(busStopId)));
    }

    /**
     * Returns departure time for the given bus stop.
     * @param busStopId id of bus stop
     * @return departure time for the given bus stop
     */
    public Duration getDepartureTime(final String busStopId)
    {
        checkStop(busStopId);
        return this.schedule.get(busStopId).getDepartureTime();
    }

    /**
     * Returns dwell time for the given bus stop.
     * @param busStopId id of bus stop
     * @return dwell time for the given bus stop
     */
    public Duration getDwellTime(final String busStopId)
    {
        checkStop(busStopId);
        return this.schedule.get(busStopId).getDwellTime();
    }

    /**
     * Returns whether the departure time is enforced.
     * @param busStopId id of bus stop
     * @return whether the departure time is enforced
     */
    public boolean isForceSchedule(final String busStopId)
    {
        checkStop(busStopId);
        return this.schedule.get(busStopId).isForceSchedule();
    }

    /**
     * Throws exception when the bus stop is not part of this schedule.
     * @param busStopId id of bus stop
     * @throws IllegalArgumentException if the bus stop is not part of this schedule
     */
    private void checkStop(final String busStopId)
    {
        Throw.when(!this.schedule.containsKey(busStopId), IllegalArgumentException.class, "Bus stop %s is not for schedule %s.",
                busStopId, this);
    }

    /**
     * Set actual departure time.
     * @param busStopId bus stop id
     * @param conflictIds conflicts downstream of the bus stop
     * @param time actual departure time
     */
    public void setActualDeparture(final String busStopId, final Set<String> conflictIds, final Duration time)
    {
        this.actualDeparturesBusStop.put(busStopId, time);
        for (String conflictId : conflictIds)
        {
            this.actualDeparturesConflict.put(conflictId, time);
        }
    }

    /**
     * Return the actual departure time.
     * @param busStopId bus stop id
     * @return actual departure time, empty if not given
     */
    public Optional<Duration> getActualDepartureBusStop(final String busStopId)
    {
        return Optional.ofNullable(this.actualDeparturesBusStop.get(busStopId));
    }

    /**
     * Return the actual departure time.
     * @param conflictId conflict id
     * @return actual departure time, empty if not given
     */
    public Optional<Duration> getActualDepartureConflict(final String conflictId)
    {
        return Optional.ofNullable(this.actualDeparturesConflict.get(conflictId));
    }

    /**
     * Return bus line.
     * @return line.
     */
    public String getLine()
    {
        return this.line;
    }

    @Override
    public String toString()
    {
        return "BusSchedule [id=" + getId() + ", line=" + this.line + "]";
    }

    /**
     * Class to contain info regarding a stop in the schedule.
     * <p>
     * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    private class BusStopInfo
    {

        /** Departure time. */
        private final Duration departureTime;

        /** Dwell time. */
        private final Duration dwellTime;

        /** Whether to wait until departure time. */
        private final boolean forceSchedule;

        /**
         * @param departureTime departure time
         * @param dwellTime dwell time
         * @param forceSchedule whether to wait until departure time
         */
        BusStopInfo(final Duration departureTime, final Duration dwellTime, final boolean forceSchedule)
        {
            this.departureTime = departureTime;
            this.dwellTime = dwellTime;
            this.forceSchedule = forceSchedule;
        }

        /**
         * @return departureTime.
         */
        public final Duration getDepartureTime()
        {
            return this.departureTime;
        }

        /**
         * @return dwellTime.
         */
        public final Duration getDwellTime()
        {
            return this.dwellTime;
        }

        /**
         * @return forceSchedule.
         */
        public final boolean isForceSchedule()
        {
            return this.forceSchedule;
        }

        @Override
        public String toString()
        {
            return "BusStopInfo [departureTime=" + this.departureTime + ", dwellTime=" + this.dwellTime + ", forceSchedule="
                    + this.forceSchedule + "]";
        }

    }

}
