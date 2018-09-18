package org.opentrafficsim.road.gtu.lane.tactical.pt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;

import nl.tudelft.simulation.language.Throw;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 24 jan. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class BusSchedule extends Route
{

    /** */
    private static final long serialVersionUID = 20170124L;

    /** Line of the bus schedule. */
    private final String line;

    /** List of bus stops. */
    private final Map<String, BusStopInfo> schedule = new HashMap<>();

    /** Map of actual departures stored per bus stop. */
    private final Map<String, Time> actualDeparturesBusStop = new HashMap<>();

    /** Map of actual departures stored per conflict. */
    private final Map<String, Time> actualDeparturesConflict = new HashMap<>();

    /**
     * @param id id
     * @param nodes nodes
     * @param line line of the bus schedule
     */
    public BusSchedule(final String id, final List<Node> nodes, final String line)
    {
        super(id, nodes);
        this.line = line;
    }

    /**
     * @param id id
     * @param line line of the bus schedule
     */
    public BusSchedule(final String id, final String line)
    {
        super(id);
        this.line = line;
    }

    /**
     * Adds a stop to the schedule.
     * @param busStopId bus stop id
     * @param departureTime departure time
     * @param dwellTime dwell time
     * @param forceSchedule whether to wait until departure time
     */
    public final void addBusStop(final String busStopId, final Time departureTime, final Duration dwellTime,
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
    public final boolean isLineStop(final String busStopId, final Time time)
    {
        return this.schedule.containsKey(busStopId) && (!this.actualDeparturesConflict.containsKey(busStopId)
                || time.lt(this.actualDeparturesConflict.get(busStopId)));
    }

    /**
     * Returns departure time for the given bus stop.
     * @param busStopId id of bus stop
     * @return departure time for the given bus stop
     */
    public final Time getDepartureTime(final String busStopId)
    {
        checkStop(busStopId);
        return this.schedule.get(busStopId).getDepartureTime();
    }

    /**
     * Returns dwell time for the given bus stop.
     * @param busStopId id of bus stop
     * @return dwell time for the given bus stop
     */
    public final Duration getDwellTime(final String busStopId)
    {
        checkStop(busStopId);
        return this.schedule.get(busStopId).getDwellTime();
    }

    /**
     * Returns whether the departure time is enforced.
     * @param busStopId id of bus stop
     * @return whether the departure time is enforced
     */
    public final boolean isForceSchedule(final String busStopId)
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
    public final void setActualDeparture(final String busStopId, final Set<String> conflictIds, final Time time)
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
     * @return actual departure time, {@code null} if not given
     */
    public final Time getActualDepartureBusStop(final String busStopId)
    {
        return this.actualDeparturesBusStop.get(busStopId);
    }

    /**
     * Return the actual departure time.
     * @param conflictId conflict id
     * @return actual departure time, {@code null} if not given
     */
    public final Time getActualDepartureConflict(final String conflictId)
    {
        return this.actualDeparturesConflict.get(conflictId);
    }

    /**
     * @return line.
     */
    public final String getLine()
    {
        return this.line;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "BusSchedule [id=" + getId() + ", line=" + this.line + "]";
    }

    /**
     * Class to contain info regarding a stop in the schedule.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 24 jan. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class BusStopInfo
    {

        /** Departure time. */
        private final Time departureTime;

        /** Dwell time. */
        private final Duration dwellTime;

        /** Whether to wait until departure time. */
        private final boolean forceSchedule;

        /**
         * @param departureTime departure time
         * @param dwellTime dwell time
         * @param forceSchedule whether to wait until departure time
         */
        BusStopInfo(final Time departureTime, final Duration dwellTime, final boolean forceSchedule)
        {
            this.departureTime = departureTime;
            this.dwellTime = dwellTime;
            this.forceSchedule = forceSchedule;
        }

        /**
         * @return departureTime.
         */
        public final Time getDepartureTime()
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

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "BusStopInfo [departureTime=" + this.departureTime + ", dwellTime=" + this.dwellTime + ", forceSchedule="
                    + this.forceSchedule + "]";
        }

    }

}
