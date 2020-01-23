package org.opentrafficsim.core.perception;

import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.perception.AbstractHistorical.EventValue;

/**
 * Single-valued historical state.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 jan. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> value type
 */
public class HistoricalValue<T> extends AbstractHistorical<T, EventValue<T>> implements Historical<T>
{

    /** Store last value for quick access. */
    private T lastValue;

    /** Store last time for quick access. */
    private double lastTime;

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     */
    public HistoricalValue(final HistoryManager historyManager)
    {
        super(historyManager);
    }

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     * @param initialValue T; initial value
     */
    public HistoricalValue(final HistoryManager historyManager, final T initialValue)
    {
        super(historyManager);
        set(initialValue);
    }

    /** {@inheritDoc} */
    @Override
    public final void set(final T value)
    {
        this.lastValue = value;
        this.lastTime = now().si;
        EventValue<T> event = getLastEvent();
        if (event != null && event.getTime() == this.lastTime)
        {
            removeEvent(event);
        }
        addEvent(new EventValue<>(this.lastTime, value));
    }

    /** {@inheritDoc} */
    @Override
    public final T get()
    {
        return this.lastValue;
    }

    /** {@inheritDoc} */
    @Override
    public final T get(final Time time)
    {
        Throw.whenNull(time, "Time may not be null.");
        if (time.si >= this.lastTime)
        {
            return this.lastValue;
        }
        EventValue<T> event = getEvent(time);
        return event == null ? null : event.getValue();
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "HistoricalValue [current=" + get() + "]";
    }

}
