package org.opentrafficsim.road.gtu.lane.perception.structure;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.road.gtu.lane.perception.structure.NavigatingIterable.Entry;

/**
 * Iterable over entries (with distance and merge distance stored) of objects. The iterable uses a navigator, lister and
 * distancer.
 * <ul>
 * <li><i>navigator</i>; returns a collection of lane records to continue a search from a covered lane record.</li>
 * <li><i>lister</i>; returns a list of objects from a lane record. The list must be ordered in the search direction (close to
 * far). Objects of any type may be returned as the navigating iterator will check whether objects are of type {@code T}. In
 * order to only include objects in the correct range, the lister must account for the start distance of the record, and any
 * possible relative position of a GTU.</li>
 * <li><i>distancer</i>; returns distance of an object. The distancer must account for any possible relative position of the
 * GTUs.</li>
 * </ul>
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> type of object
 * @param <L> type of lane structure record
 */
public class NavigatingIterable<T, L extends LaneRecordInterface<L>> implements Iterable<Entry<T>>
{

    /** Class of object type to be found from lanes. */
    private final Class<T> clazz;

    /** Range within which objects are included. */
    private final Length range;

    /** Start collection of lane records. */
    private final Collection<L> start;

    /** Navigator which gives the next records. */
    private final Function<L, Collection<L>> navigator;

    /** Obtains ordered list of objects from lane. */
    private final Function<L, List<?>> lister;

    /** Returns distance of object. */
    private final BiFunction<T, L, Length> distancer;

    /**
     * Constructor.
     * @param clazz class of lane-based object type.
     * @param range range within which objects are included.
     * @param start start collection of lane records.
     * @param navigator navigator.
     * @param lister obtains ordered list of objects from lane.
     * @param distancer returns distance of object.
     */
    public NavigatingIterable(final Class<T> clazz, final Length range, final Collection<L> start,
            final Function<L, Collection<L>> navigator, final Function<L, List<?>> lister,
            final BiFunction<T, L, Length> distancer)
    {
        this.clazz = clazz;
        this.range = range;
        this.start = start;
        this.navigator = navigator;
        this.lister = lister;
        this.distancer = distancer;
    }

    @Override
    public Iterator<Entry<T>> iterator()
    {
        // map of currently iterated records and their object iterators, create map from initial start
        Map<L, ObjectIterator> map = new LinkedHashMap<>();
        for (L startRecord : this.start)
        {
            map.put(startRecord, new ObjectIterator(startRecord, NavigatingIterable.this.clazz, NavigatingIterable.this.lister,
                    NavigatingIterable.this.distancer));
        }
        return new Iterator<Entry<T>>()
        {
            /** Next entry as found by {@code hasNext()}. */
            private Entry<T> next;

            @Override
            public boolean hasNext()
            {
                if (this.next != null)
                {
                    return true;
                }
                // update the map with records that have something to produce
                Map<L, ObjectIterator> mapCopy = new LinkedHashMap<>(map);
                for (Map.Entry<L, ObjectIterator> mapEntry : mapCopy.entrySet())
                {
                    if (!mapEntry.getValue().hasNext())
                    {
                        updateMapRecursive(mapEntry.getKey());
                    }
                }
                if (map.isEmpty())
                {
                    this.next = null;
                }
                else if (map.size() == 1)
                {
                    this.next = map.values().iterator().next().next();
                }
                else
                {
                    // loop map and find closest object
                    Length minDistance = Length.POSITIVE_INFINITY;
                    ObjectIterator closestObjectIterator = null;
                    for (ObjectIterator objectIterator : map.values())
                    {
                        Entry<T> entry = objectIterator.poll();
                        if (entry.distance().lt(minDistance))
                        {
                            minDistance = entry.distance();
                            closestObjectIterator = objectIterator;
                        }
                    }
                    this.next = closestObjectIterator.next(); // advance the object iterator; it was only polled above
                }
                if (this.next != null && this.next.distance().gt(NavigatingIterable.this.range))
                {
                    this.next = null; // next object out of range
                }
                return this.next != null;
            }

            @Override
            public Entry<T> next()
            {
                Throw.when(!hasNext(), NoSuchElementException.class, "No more object of type %s.",
                        NavigatingIterable.this.clazz);
                Entry<T> n = this.next;
                this.next = null;
                return n;
            }

            /**
             * Updates the map so it contains a record only if it has an object to return. If not, further records are added to
             * the map through the navigator and consecutively checked.
             * @param record lane record.
             */
            private void updateMapRecursive(final L record)
            {
                if (!map.containsKey(record) || map.get(record).hasNext())
                {
                    return;
                }
                map.remove(record);
                for (L next : NavigatingIterable.this.navigator.apply(record))
                {
                    map.put(next, new ObjectIterator(next, NavigatingIterable.this.clazz, NavigatingIterable.this.lister,
                            NavigatingIterable.this.distancer));
                    updateMapRecursive(next);
                }
            }
        };
    }

    /**
     * Iterator over objects on a {@code LaneRecordInterface}. This is used by {@code NavigatingIterable} to find object.
     * <p>
     * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    private class ObjectIterator implements Iterator<Entry<T>>
    {

        /** Lane record. */
        private final L record;

        /** Class of lane-based object type. */
        private final Class<T> clazz;

        /** List of objects from lane, within correct range for downstream. */
        private final List<?> list;

        /** Index of current entry. */
        private int index;

        /** Returns distance of object. */
        private final BiFunction<T, L, Length> distancer;

        /** Poll entry, that next will return. */
        private Entry<T> poll;

        /**
         * Constructor. The lister may return objects of any type. This class will check whether objects are of type T.
         * @param record lane record.
         * @param clazz class of lane-based object type.
         * @param lister obtains ordered list of objects from lane.
         * @param distancer returns distance of object.
         */
        ObjectIterator(final L record, final Class<T> clazz, final Function<L, List<?>> lister,
                final BiFunction<T, L, Length> distancer)
        {
            this.record = record;
            this.clazz = clazz;
            this.list = lister.apply(record);
            this.distancer = distancer;
        }

        @Override
        public boolean hasNext()
        {
            while (this.index < this.list.size() && !this.list.get(this.index).getClass().isAssignableFrom(this.clazz))
            {
                this.index++;
            }
            return this.index < this.list.size();
        }

        @Override
        public Entry<T> next()
        {
            Entry<T> entry = poll();
            this.index++;
            this.poll = null;
            return entry;
        }

        /**
         * Returns the entry that {@code next()} will return, without advancing the iterator.
         * @return poll entry.
         */
        public Entry<T> poll()
        {
            Throw.when(!hasNext(), NoSuchElementException.class, "No more object of type %s.", this.clazz);
            if (this.poll == null)
            {
                @SuppressWarnings("unchecked") // isAssignableFrom in hasNext() checks this
                T t = (T) this.list.get(this.index);
                this.poll = new Entry<>(this.distancer.apply(t, this.record), this.record.getMergeDistance(), t);
            }
            return this.poll;
        }

    }

    /**
     * Container for a perceived object with the distance towards it and the distance until the road of the object and the road
     * of the perceiving GTU merge.
     * <p>
     * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param <T> type of object
     * @param distance distance to object
     * @param merge ego-distance until the road of the object and the road of the perceiving GTU merge
     * @param object the perceived object
     */
    public record Entry<T>(Length distance, Length merge, T object)
    {
    }

}
