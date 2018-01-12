package org.opentrafficsim.core.perception;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.perception.AbstractHistorical.Event;

import nl.tudelft.simulation.language.Throw;

/**
 * Base class for objects or properties that can be perceived from their actual state in the past.
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
public abstract class AbstractHistorical<T, E extends Event<T>>
{

    /** History manager. */
    private final HistoryManager historyManager;

    /** List of events to determine the value at a previous time. */
    private final List<E> events = new ArrayList<>();

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     */
    public AbstractHistorical(final HistoryManager historyManager)
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
     * Returns the list of events for sub classes to use.
     * @return List;&lt;E&gt; list of events for sub classes to use
     */
    protected final List<E> getEvents()
    {
        return this.events;
    }

    /**
     * Removes all events pertaining to the given value.
     * @param value T; value to clear
     */
    public synchronized void clear(final T value)
    {
        Iterator<E> iterator = this.events.iterator();
        while (iterator.hasNext())
        {
            if (iterator.next().getValue().equals(value))
            {
                iterator.remove();
            }
        }
    }
    
    /**
     * Removes events that are no longer needed to guarantee the history time. This is invoked by the history manager.
     * @param history Duration; history time to keep
     */
    protected final void cleanUpHistory(final Duration history)
    {
        double past = now().si - history.si;
        while (getEvents().size() > 1 && getEvents().get(1).getTime() < past)
        {
            getEvents().remove(0);
        }
    }

    /**
     * Interface for event types.
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
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 jan. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <T> value type
     */
    static class EventValue<T> implements Event<T>
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

    }

}
