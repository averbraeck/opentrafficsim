package org.opentrafficsim.core.perception;

import java.util.List;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.perception.AbstractHistorical.EventValue;

import nl.tudelft.simulation.language.Throw;

/**
 * Single-valued historical state.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 jan. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> value type
 */
public class Historical<T> extends AbstractHistorical<T, EventValue<T>>
{

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     */
    public Historical(final HistoryManager historyManager)
    {
        super(historyManager);
    }

    /**
     * Set value at the current simulation time. If a value is already given at this time, it is overwritten. Values should be
     * set in chronological order.
     * @param value T; value
     */
    public final synchronized void set(final T value)
    {
        List<EventValue<T>> events = getEvents();
        double time = now().si;
        if (!events.isEmpty() && events.get(events.size() - 1).getTime() == time)
        {
            events.remove(events.size() - 1);
        }
        events.add(new EventValue<>(time, value));
    }

    /**
     * Get value at current simulation time.
     * @return T; value at current simulation time
     */
    public final synchronized T get()
    {
        List<EventValue<T>> events = getEvents();
        if (events.isEmpty())
        {
            return null;
        }
        return events.get(events.size() - 1).getValue();
    }

    /**
     * Get value at given time.
     * @param time T; time to get the value
     * @return T; value at current time
     * @throws NullPointerException when time is null
     */
    public final synchronized T get(final Time time)
    {
        Throw.whenNull(time, "Time may not be null.");
        // find most recent state with start time before time
        for (int i = getEvents().size() - 1; i >= 0; i--)
        {
            if (getEvents().get(i).getTime() <= time.si)
            {
                return getEvents().get(i).getValue();
            }
        }
        // return null if no history available
        if (getEvents().isEmpty())
        {
            return null;
        }
        // return oldest if no state is old enough
        return getEvents().get(0).getValue();
    }

}
