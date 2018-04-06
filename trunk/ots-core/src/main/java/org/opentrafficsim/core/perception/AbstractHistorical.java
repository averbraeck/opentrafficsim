package org.opentrafficsim.core.perception;

import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.perception.AbstractHistorical.Event;
import org.opentrafficsim.core.perception.HistoryManager.HistoricalElement;

import nl.tudelft.simulation.language.Throw;

/**
 * Base class for objects or properties that can be perceived from their actual state in the past. The principle by which a past
 * state is determined is by storing an internal event for each change to the object. Each event can be reversed, and by working
 * from a current state backwards, any previous state within the available history can be restored.<br>
 * <br>
 * This class couples the historical to a {@code HistoryManager} and in response to a request from the {@code HistoryManager}
 * will clear old events. Subclasses need to define their own events as extensions to {@code AbstractHistorical.Event}. This
 * class provides the following methods to subclasses to work with the events.
 * <ul>
 * <li>{@code now()}, returns the current time from the {@code HistoryManager}, which needs to be stored with each event.</li>
 * <li>{@code getEvents(Time)}, returns all events between now and the given time, ordered from recent to old.</li>
 * <li>{@code getEvent(Time)}, returns the most recent event from before the given time.</li>
 * <li>{@code getLastEvent()}, returns the most recent event.</li>
 * <li>{@code removeEvent(Event)}, removes (oldest occurrence off) the event.</li>
 * <li>{@code addEvent(Event)}, add the event.</li>
 * </ul>
 * Typically, any change results in a new event which is added with {@code addEvent(Event)}, where the event stores information
 * such that the event can be restored. When an old state is requested, one or more events can be obtained with either of the
 * get methods, after which they are applied to restore a previous state in a manner depending on the nature of the
 * subclass.<br>
 * <br>
 * This class is defined with a single event type parameter {@code E}. Subclasses can use different event classes, so long as
 * all of them derive from a common ancestor. For instance an 'add' and a 'remove' event that inherit from an abstract super.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 jan. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> value type
 * @param <E> event type
 */
public abstract class AbstractHistorical<T, E extends Event<T>> implements HistoricalElement
{

    /** History manager. */
    private final HistoryManager historyManager;

    /** List of events to determine the value at a previous time. */
    private final List<E> events = new ArrayList<>();

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     */
    protected AbstractHistorical(final HistoryManager historyManager)
    {
        Throw.whenNull(historyManager, "History manager may not be null.");
        this.historyManager = historyManager;
        historyManager.registerHistorical(this);
    }

    /**
     * Returns the current time.
     * @return Time; current time
     */
    protected final Time now()
    {
        return this.historyManager.now();
    }

    /**
     * Returns a list of events, ordered last to first, that includes all events <i>after</i> {@code time}.
     * @param time Time; past time up to which to include events
     * @return List; list of events, ordered last to first, that includes all events <i>after</i> {@code time}
     */
    protected final List<E> getEvents(final Time time)
    {
        List<E> list = new ArrayList<>();
        int i = this.events.size() - 1;
        while (i >= 0 && this.events.get(i).getTime() > time.si)
        {
            list.add(this.events.get(i));
            i--;
        }
        return list;
    }

    /**
     * Returns the most recent event from <i>before</i> {@code time}, or the oldest if no such event.
     * @param time Time; past time at which to obtain event
     * @return E; most recent event from <i>before</i> {@code time}
     */
    protected final E getEvent(final Time time)
    {
        E prev = null;
        for (E event : this.events)
        {
            if (event.getTime() > time.si)
            {
                break;
            }
            prev = event;
        }
        if (prev == null && !this.events.isEmpty())
        {
            return this.events.get(0);
        }
        return prev;
    }

    /**
     * Returns the last event.
     * @return E; last event
     */
    protected final E getLastEvent()
    {
        return this.events.isEmpty() ? null : this.events.get(this.events.size() - 1);
    }
    
    /**
     * Returns whether the state at the given time is equal to the state at the current time.
     * @param time Time; time
     * @return boolean; whether the state at the given time is equal to the state at the current time
     */
    protected final boolean isLastState(final Time time)
    {
        return this.events.isEmpty() ? true : this.events.get(this.events.size() - 1).getTime() <= time.si;
    }

    /**
     * Removes the given event.
     * @param event E; event to remove
     */
    protected final void removeEvent(final E event)
    {
        this.events.remove(event);
    }

    /**
     * Adds the event to the list of events.
     * @param event E; event to add
     */
    protected final void addEvent(final E event)
    {
        this.events.add(event);
    }

    /** {@inheritDoc} */
    @Override
    public final void cleanUpHistory(final Duration history)
    {
        double past = now().si - history.si;
        while (this.events.size() > 1 && this.events.get(0).getTime() < past)
        {
            this.events.remove(0);
        }
    }

    /**
     * Interface for event types.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 jan. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <T> value type
     */
    static interface Event<T>
    {

        /**
         * Returns the time of this event.
         * @return double; time of this event
         */
        public abstract double getTime();

        /**
         * Returns the value of this event.
         * @return T; value of this event
         */
        public abstract T getValue();

    }

    /**
     * Standard event which stores a time and value.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 jan. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <T> value type
     */
    public static class EventValue<T> implements Event<T>
    {

        /** Time of event. */
        private final double time;

        /** Value of event. */
        private final T value;

        /**
         * Constructor.
         * @param time double; time of event
         * @param value T; value of event
         */
        public EventValue(final double time, final T value)
        {
            this.time = time;
            this.value = value;
        }

        /** {@inheritDoc} */
        @Override
        public double getTime()
        {
            return this.time;
        }

        /** {@inheritDoc} */
        @Override
        public T getValue()
        {
            return this.value;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "EventValue [time=" + this.time + ", value=" + this.value + "]";
        }

    }

}
