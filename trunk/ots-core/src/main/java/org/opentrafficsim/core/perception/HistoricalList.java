package org.opentrafficsim.core.perception;

import java.util.List;

/**
 * Extension of {@code HistoryCollection} with index support.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 2 jan. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> value type
 * @param <L> list type
 */
public class HistoricalList<T, L extends List<T>> extends HistoricalCollection<T, L>
{

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     * @param list C; empty initial internal collection
     */
    public HistoricalList(final HistoryManager historyManager, final L list)
    {
        super(historyManager, list);
    }

    /**
     * Adds a value at the current simulation time. Values should be added or removed in chronological order. Multiple events at
     * one time are accepted.
     * @param index int; index to add the value
     * @param value T; value
     */
    public final void add(final int index, final T value)
    {
        getEvents().add(new AddEvent<>(now().si, value, index, getCollection()));
    }

    /**
     * Adds a value at the current simulation time, at the end of the list. Values should be added or removed in chronological
     * order. Multiple events at one time are accepted.
     * @param value T; value
     */
    @Override
    public final synchronized void add(final T value)
    {
        getEvents().add(new AddEvent<>(now().si, value, getCollection().size(), getCollection()));
    }

    /**
     * Removes a value at the current simulation time. Values should be added or removed in chronological order. Multiple events
     * at one time are accepted.
     * @param value T; value
     */
    @Override
    public final synchronized void remove(final T value)
    {
        getEvents().add(new RemoveEvent<>(now().si, value, getCollection())); // note: different RemoveEvent class than at super
    }

    /**
     * Abstract super class for events that add or remove a value from the collection.
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
     * @param <L> list type
     */
    abstract private static class EventList<T, L extends List<T>> extends EventCollection<T, L>
    {

        /** Index of the value. */
        private final int index;

        /**
         * Constructor.
         * @param time double; time of event
         * @param value T; value of event
         * @param index int; index
         */
        public EventList(final double time, final T value, final int index)
        {
            super(time, value);
            this.index = index;
        }

        /**
         * Returns the index.
         * @return index
         */
        final int getIndex()
        {
            return this.index;
        }

    }

    /**
     * Class for events that add a value to the list.
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
     * @param <L> list type
     */
    private static class AddEvent<T, L extends List<T>> extends EventList<T, L>
    {

        /**
         * Constructor.
         * @param time double; time of event
         * @param value T; value of event
         * @param index int; index
         * @param list L; internal list to apply the event on
         */
        public AddEvent(final double time, final T value, final int index, final L list)
        {
            super(time, value, index);
            list.add(index, value);
        }

        /** {@inheritDoc} */
        @Override
        public void restore(final L list)
        {
            list.remove(getIndex());
        }

    }

    /**
     * Class for events that remove a value from the list.
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
     * @param <L> list type
     */
    private static class RemoveEvent<T, L extends List<T>> extends EventList<T, L>
    {

        /**
         * Constructor.
         * @param time double; time of event
         * @param value T; value of event
         * @param list L; internal list to apply the event on
         */
        public RemoveEvent(final double time, final T value, final L list)
        {
            super(time, value, list.indexOf(value));
            list.remove(value);
        }

        /** {@inheritDoc} */
        @Override
        public void restore(final L list)
        {
            if (getIndex() >= 0)
            {
                list.add(getIndex(), getValue());
            }
        }

    }

}
