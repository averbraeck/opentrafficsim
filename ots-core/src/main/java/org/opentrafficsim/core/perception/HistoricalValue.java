package org.opentrafficsim.core.perception;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.perception.AbstractHistorical.EventValue;

import com.google.common.base.Objects;

/**
 * Single-valued historical state.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
     * @param historyManager history manager
     * @param owner object that owns the historical value
     */
    public HistoricalValue(final HistoryManager historyManager, final Object owner)
    {
        super(historyManager, owner);
    }

    /**
     * Constructor.
     * @param historyManager history manager
     * @param owner object that owns the historical value
     * @param initialValue initial value
     */
    public HistoricalValue(final HistoryManager historyManager, final Object owner, final T initialValue)
    {
        super(historyManager, owner);
        set(initialValue);
    }

    @Override
    public final void set(final T value)
    {
        if (Objects.equal(value, this.lastValue))
        {
            return;
        }
        this.lastValue = value;
        this.lastTime = now().si;
        EventValue<T> event = getLastEvent();
        if (event != null && event.getTime() == this.lastTime)
        {
            removeEvent(event);
        }
        addEvent(new EventValue<>(this.lastTime, value));
    }

    @Override
    public final T get()
    {
        return this.lastValue;
    }

    @Override
    public final T get(final Duration time)
    {
        Throw.whenNull(time, "Time may not be null.");
        if (time.si >= this.lastTime)
        {
            return this.lastValue;
        }
        EventValue<T> event = getEvent(time);
        return event == null ? null : event.getValue();
    }

    @Override
    public String toString()
    {
        return "HistoricalValue [current=" + get() + ", lastTime=" + this.lastTime + "]";
    }

}
