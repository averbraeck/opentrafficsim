package org.opentrafficsim.core.perception;

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
     * Constructor.
     * @param historyManager HistoryManager; history manager
     * @param initialValue T; initial value
     */
    public Historical(final HistoryManager historyManager, final T initialValue)
    {
        super(historyManager);
        set(initialValue);
    }

    /**
     * Set value at the current simulation time. If a value is already given at this time, it is overwritten. Values should be
     * set in chronological order.
     * @param value T; value
     */
    public final synchronized void set(final T value)
    {
        EventValue<T> event = getLastEvent();
        if (event != null && event.getTime() == now().si)
        {
            removeEvent(event);
        }
        addEvent(new EventValue<>(now().si, value));
    }

    /**
     * Get value at current simulation time.
     * @return T; value at current simulation time
     */
    public final synchronized T get()
    {
        EventValue<T> event = getLastEvent();
        return event == null ? null : event.getValue();
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
        EventValue<T> event = getEvent(time);
        return event == null ? null : event.getValue();
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Historical [current=" + get() + "]";
    }
    
}
