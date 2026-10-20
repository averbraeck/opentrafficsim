package org.opentrafficsim.road.gtu.perception.structure;

import java.util.ArrayList;
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
import org.opentrafficsim.road.gtu.perception.structure.NavigatingIterable.Entry;

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
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 * @param <T> type of object
 * @param <L> type of lane structure record
 */
public class NavigatingIterable<T, L extends LaneRecordInterface<L>> implements Iterable<Entry<T>>
{

    /** Class of object type to be found from lanes. */
    private final Class<T> clazz;

    /** Range within which objects are included. */
    private final Length range;

    /** Downstream or upstream search. */
    private final boolean downstream;

    /** Start collection of lane records. */
    private final Collection<L> start;

    /** Navigator which gives the next records. */
    private final Function<L, Collection<L>> navigator;

    /** Obtains ordered list of objects from lane. */
    private final Function<L, Iterable<?>> lister;

    /** Returns distance of object. */
    private final BiFunction<T, L, Double> distancer;

    /**
     * Constructor.
     * @param clazz class of lane-based object type
     * @param range range within which objects are included
     * @param downstream downstream or upstream
     * @param start start collection of lane records
     * @param navigator navigator
     * @param lister obtains ordered list of objects from lane
     * @param distancer returns distance of object
     */
    public NavigatingIterable(final Class<T> clazz, final Length range, final boolean downstream, final Collection<L> start,
            final Function<L, Collection<L>> navigator, final Function<L, Iterable<?>> lister,
            final BiFunction<T, L, Double> distancer)
    {
        this.clazz = clazz;
        this.range = range;
        this.downstream = downstream;
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
            /** Next entry as found by {@link #hasNext}. */
            private Entry<T> next;

            private final List<Map.Entry<L, ObjectIterator>> keysBuffer = new ArrayList<>();

            @Override
            public boolean hasNext()
            {
                if (this.next != null)
                {
                    return true;
                }
                // update the map with records that have something to produce
                this.keysBuffer.clear();
                this.keysBuffer.addAll(map.entrySet());
                for (Map.Entry<L, ObjectIterator> mapEntry : this.keysBuffer)
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
                    double minDistance = Double.POSITIVE_INFINITY;
                    ObjectIterator closestObjectIterator = null;
                    for (ObjectIterator objectIterator : map.values())
                    {
                        Entry<T> objectEntry = objectIterator.peek();
                        if (objectEntry.distance().si < minDistance)
                        {
                            minDistance = objectEntry.distance().si;
                            closestObjectIterator = objectIterator;
                        }
                    }
                    this.next = closestObjectIterator.next(); // advance the object iterator; it was only peeked above
                }
                if (this.next != null && this.next.distance().si > NavigatingIterable.this.range.si)
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
                ObjectIterator it = map.get(record);
                if (it == null || it.hasNext())
                {
                    return;
                }
                map.remove(record);
                // stop if end point of record is beyond range
                if ((NavigatingIterable.this.downstream ? record.getStartDistance() + record.getLength().si
                        : -record.getStartDistance()) > NavigatingIterable.this.range.si)
                {
                    return;
                }
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
     * Iterator over objects on a {@link LaneRecordInterface}. This is used by {@link NavigatingIterable} to find object.
     */
    private class ObjectIterator implements Iterator<Entry<T>>
    {

        /** Lane record. */
        private final L record;

        /** Class of lane-based object type. */
        private final Class<T> clazz;

        /** Iterator over objects. */
        private final Iterator<?> iterator;

        /** Next object. */
        private Entry<T> next;

        /** Returns distance of object. */
        private final BiFunction<T, L, Double> distancer;

        /**
         * Constructor. The lister may return objects of any type. This class will check whether objects are of type T.
         * @param record lane record.
         * @param clazz class of lane-based object type.
         * @param lister obtains ordered list of objects from lane.
         * @param distancer returns distance of object.
         */
        ObjectIterator(final L record, final Class<T> clazz, final Function<L, ? extends Iterable<?>> lister,
                final BiFunction<T, L, Double> distancer)
        {
            this.record = record;
            this.clazz = clazz;
            this.iterator = lister.apply(record).iterator();
            this.distancer = distancer;
        }

        @Override
        public boolean hasNext()
        {
            while (this.next == null && this.iterator.hasNext())
            {
                Object object = this.iterator.next();
                if (this.clazz.isInstance(object))
                {
                    @SuppressWarnings("unchecked")
                    T t = (T) object;
                    this.next =
                            new Entry<>(Length.ofSI(this.distancer.apply(t, this.record)), this.record.getMergeDistance(), t);
                }
            }
            return this.next != null;
        }

        @Override
        public Entry<T> next()
        {
            Entry<T> out = peek();
            this.next = null;
            return out;
        }

        /**
         * Returns the entry that {@code next()} will return, without advancing the iterator.
         * @return peek entry.
         */
        public Entry<T> peek()
        {
            Throw.when(!hasNext(), NoSuchElementException.class, "No more object of type %s.", this.clazz);
            return this.next;
        }

    }

    /**
     * Container for a perceived object with the distance towards it and the distance until the road of the object and the road
     * of the perceiving GTU merge.
     * @param <T> type of object
     * @param distance distance to object
     * @param merge ego-distance until the road of the object and the road of the perceiving GTU merge
     * @param object the perceived object
     */
    public record Entry<T>(Length distance, Length merge, T object) implements Comparable<Entry<?>>
    {
        @Override
        public int compareTo(final Entry<?> o)
        {
            return distance().compareTo(o.distance());
        }
    }

}
