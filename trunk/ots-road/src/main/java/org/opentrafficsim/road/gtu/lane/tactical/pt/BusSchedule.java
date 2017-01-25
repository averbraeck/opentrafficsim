package org.opentrafficsim.road.gtu.lane.tactical.pt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.network.lane.object.BusStop;

import nl.tudelft.simulation.language.Throw;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    private final Map<BusStop, BusStopInfo> schedule = new HashMap<>();

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
     * @param busStop bus stop
     * @param departureTime departure time
     * @param dwellTime dwell time
     * @param forceSchedule whether to wait until departure time
     */
    public final void addBusStop(final BusStop busStop, final Time departureTime, final Duration dwellTime,
            final boolean forceSchedule)
    {
        Throw.whenNull(busStop, "Bus stop may not be null.");
        Throw.whenNull(departureTime, "Departure time may not be null.");
        Throw.whenNull(dwellTime, "Dwell time may not be null.");
        Throw.when(!busStop.isForLine(this.line), IllegalArgumentException.class, "Bus stop %s is not for schedule of line %s.",
                busStop, this.line);
        this.schedule.put(busStop, new BusStopInfo(departureTime, dwellTime, forceSchedule));
    }
    
    /**
     * Whether the bus of this line should stop for this bus stop.
     * @param busStop bus stop
     * @return whether the bus of this line should stop for this bus stop
     */
    public final boolean isLineStop(final BusStop busStop)
    {
        return this.schedule.containsKey(busStop);
    }
    
    /**
     * Returns departure time for the given bus stop.
     * @param busStop bus stop 
     * @return departure time for the given bus stop
     */
    public final Time getDepartureTime(final BusStop busStop)
    {
        checkStop(busStop);
        return this.schedule.get(busStop).getDepartureTime();
    }
    
    /**
     * Returns dwell time for the given bus stop.
     * @param busStop bus stop 
     * @return dwell time for the given bus stop
     */
    public final Duration getDwellTime(final BusStop busStop)
    {
        checkStop(busStop);
        return this.schedule.get(busStop).getDwellTime();
    }
    
    /**
     * Returns whether the departure time is enforced.
     * @param busStop bus stop 
     * @return whether the departure time is enforced
     */
    public final boolean isForceSchedule(final BusStop busStop)
    {
        checkStop(busStop);
        return this.schedule.get(busStop).isForceSchedule();
    }
    
    /**
     * Throws exception when the bus stop is not part of this schedule.
     * @param busStop bus stop
     * @throws IllegalArgumentException if the bus stop is not part of this schedule
     */
    private void checkStop(final BusStop busStop)
    {
        Throw.when(!isLineStop(busStop), IllegalArgumentException.class, "Bus stop %s is not for schedule %s.", busStop, this);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "BusSchedule [" + this.line + "]";
    }

    /**
     * Class to contain info regarding a stop in the schedule.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
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

    }

}
