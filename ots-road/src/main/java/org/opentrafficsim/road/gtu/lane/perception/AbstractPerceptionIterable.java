package org.opentrafficsim.road.gtu.lane.perception;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;

/**
 * Abstract iterable that figures out how to find the next nearest object, including splits.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <H> headway type
 * @param <U> underlying object type
 * @param <C> counter type
 */
public abstract class AbstractPerceptionIterable<H extends Headway, U, C> extends AbstractPerceptionReiterable<H, U>
{

    /** Root record. */
    private final LaneRecordInterface<?> root;

    /** Initial position. */
    private final Length initialPosition;

    /** Search downstream (or upstream). */
    private final boolean downstream;

    /** Max distance. */
    private final double maxDistance;

    /** Position to which distance are calculated by subclasses. */
    private final RelativePosition relativePosition;

    /** Route of the GTU. */
    private final Route route;

    /**
     * Constructor.
     * @param perceivingGtu LaneBasedGtu; perceiving GTU
     * @param root LaneRecord&lt;?&gt;; root record
     * @param initialPosition Length; initial position
     * @param downstream boolean; search downstream (or upstream)
     * @param maxDistance Length; max distance to search
     * @param relativePosition RelativePosition; position to which distance are calculated by subclasses
     * @param route Route; route of the GTU, may be {@code null}
     */
    public AbstractPerceptionIterable(final LaneBasedGtu perceivingGtu, final LaneRecordInterface<?> root,
            final Length initialPosition, final boolean downstream, final Length maxDistance,
            final RelativePosition relativePosition, final Route route)
    {
        super(perceivingGtu);
        this.root = root;
        this.initialPosition = initialPosition;
        this.downstream = downstream;
        this.maxDistance = maxDistance.si;
        this.relativePosition = relativePosition;
        this.route = route;
    }

    /**
     * Whether the iterable searches downstream.
     * @return boolean; whether the iterable searches downstream
     */
    public boolean isDownstream()
    {
        return this.downstream;
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<PrimaryIteratorEntry> primaryIterator()
    {
        return new PrimaryIterator();
    }

    /**
     * Returns the next object(s) on the lane represented by the record. This should only consider objects on the given lane.
     * This method should not check the distance towards objects with the maximum distance. The counter will be {@code null} for
     * the first object(s). For following object(s) it is whatever value is given with the previous output {@code Entry}. Hence,
     * this method maintains its own counting system.
     * @param record LaneRecord&lt;?&gt;; record representing the lane and direction
     * @param position Length; position to look beyond
     * @param counter C; counter
     * @return next object(s) on the lane or {@code null} if none
     * @throws GtuException on any exception in the process
     */
    protected abstract Entry getNext(LaneRecordInterface<?> record, Length position, C counter) throws GtuException;

    /**
     * Returns the distance to the object. The position fed in to this method is directly taken from an {@code Entry} returned
     * by {@code getNext}. The two methods need to be consistent with each other.
     * @param object U; underlying object
     * @param record LaneRecord&lt;?&gt;; record representing the lane and direction
     * @param position Length; position of the object on the lane
     * @return Length; distance to the object
     */
    protected abstract Length getDistance(U object, LaneRecordInterface<?> record, Length position);

    /**
     * Returns the longitudinal length of the relevant relative position such that distances to this points can be calculated.
     * @return Length; the longitudinal length of the relevant relative position such that distances to this points can be
     *         calculated
     */
    protected Length getDx()
    {
        return this.relativePosition.dx();
    }

    /**
     * The primary iterator is used by all returned iterators to find the next object. This contains the core algorithm to deal
     * with splits and multiple objects at a single location.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    private class PrimaryIterator implements Iterator<PrimaryIteratorEntry>
    {

        /** Map containing the objects found per branch. */
        private SortedMap<PrimaryIteratorEntry, LaneRecordInterface<?>> map;

        /** Position per record where the search was halted. */
        private Map<LaneRecordInterface<?>, Length> positions = new LinkedHashMap<>();

        /** Items returned to prevent duplicates. */
        private Set<U> returnedItems = new LinkedHashSet<>();

        /** Sets of remaining objects at the same location. */
        private Map<LaneRecordInterface<?>, Queue<PrimaryIteratorEntry>> queues = new LinkedHashMap<>();

        /** Counter objects per lane. */
        private Map<LaneRecordInterface<?>, C> counters = new LinkedHashMap<>();

        /** Record regarding a postponed call to {@code getNext()}. */
        private LaneRecordInterface<?> postponedRecord = null;

        /** Position regarding a postponed call to {@code getNext()}. */
        private Length postponedPosition = null;

        /** Constructor. */
        PrimaryIterator()
        {
            //
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext()
        {
            // (re)start the process
            startProcess();

            // check next value
            return !this.map.isEmpty();
        }

        /** {@inheritDoc} */
        @Override
        public PrimaryIteratorEntry next()
        {
            // (re)start the process
            startProcess();

            // get and remove next
            PrimaryIteratorEntry nextEntry = this.map.firstKey();
            U next = nextEntry.getObject();
            LaneRecordInterface<?> record = this.map.get(nextEntry);
            this.map.remove(nextEntry);

            // see if we can obtain the next from a queue
            Queue<PrimaryIteratorEntry> queue = this.queues.get(next);
            if (queue != null)
            {
                PrimaryIteratorEntry nextNext = queue.poll();
                this.map.put(nextNext, record); // next object is made available in the map
                if (queue.isEmpty())
                {
                    this.queues.remove(record);
                }
                preventDuplicateEntries(nextEntry.getObject());
                return nextNext;
            }

            // prepare for next
            this.postponedRecord = record;
            this.postponedPosition = this.positions.get(record); // position;
            preventDuplicateEntries(nextEntry.getObject());
            return nextEntry;
        }

        /**
         * Prevents that duplicate (and further) records are returned for the given object as splits later on merge.
         * @param object U; object for which a {@code PrimaryIteratorEntry} will be returned
         */
        private void preventDuplicateEntries(final U object)
        {
            this.returnedItems.add(object); // prevents new items to be added over alive branches (that should die out)
            Iterator<PrimaryIteratorEntry> it = this.map.keySet().iterator();
            while (it.hasNext())
            {
                PrimaryIteratorEntry entry = it.next();
                if (entry.getObject().equals(object))
                {
                    it.remove();
                }
            }
        }

        /**
         * Starts or restarts the process.
         */
        @SuppressWarnings("synthetic-access")
        private void startProcess()
        {
            if (this.postponedRecord != null)
            {
                // restart the process; perform prepareNext() that was postponed
                prepareNext(this.postponedRecord, this.postponedPosition);
                this.postponedRecord = null;
                this.postponedPosition = null;
            }
            else if (this.map == null)
            {
                // start the process
                this.map = new TreeMap<>();
                prepareNext(AbstractPerceptionIterable.this.root, AbstractPerceptionIterable.this.initialPosition);
            }
        }

        /**
         * Iterative method that continues a search on the next lanes if no object is found.
         * @param record LaneRecord&lt;?&gt;; record
         * @param position Length; position
         */
        @SuppressWarnings("synthetic-access")
        private void prepareNext(final LaneRecordInterface<?> record, final Length position)
        {
            Entry next = Try.assign(() -> AbstractPerceptionIterable.this.getNext(record, position, this.counters.get(record)),
                    "Exception while deriving next object.");
            if (next == null)
            {
                this.counters.remove(record);
                double distance = AbstractPerceptionIterable.this.downstream
                        ? record.getStartDistance().si + record.getLength().si : -record.getStartDistance().si;
                // TODO this let's us ignore an object that is registered on the next lane, but who's tail may be on this lane
                if (distance < AbstractPerceptionIterable.this.maxDistance)
                {
                    if (AbstractPerceptionIterable.this.downstream)
                    {
                        for (LaneRecordInterface<?> nextRecord : record.getNext())
                        {
                            if (isOnRoute(nextRecord))
                            {
                                prepareNext(nextRecord, Length.instantiateSI(-1e-9));
                            }
                        }
                    }
                    else
                    {
                        for (LaneRecordInterface<?> nextRecord : record.getPrev())
                        {
                            if (isOnRoute(nextRecord))
                            {
                                prepareNext(nextRecord, nextRecord.getLength());
                            }
                        }
                    }
                }
            }
            else
            {
                this.counters.put(record, next.counter);
                if (next.isSet())
                {
                    Iterator<U> it = next.set.iterator();
                    U nextNext = it.next();
                    if (!this.returnedItems.contains(nextNext))
                    {
                        Length distance = getDistance(nextNext, record, next.position);
                        if (distance == null // null means the object overlaps and is close
                                || distance.si <= AbstractPerceptionIterable.this.maxDistance)
                        {
                            // next object is made available in the map
                            this.map.put(new PrimaryIteratorEntry(nextNext, distance), record);
                            this.positions.put(record, next.position);
                            if (next.set.size() > 1)
                            {
                                // remaining at this location are made available in a queue
                                Queue<PrimaryIteratorEntry> queue = new LinkedList<>();
                                while (it.hasNext())
                                {
                                    nextNext = it.next();
                                    queue.add(new PrimaryIteratorEntry(nextNext, getDistance(nextNext, record, next.position)));
                                }
                                this.queues.put(record, queue);
                            }
                        }
                    }
                }
                else if (!this.returnedItems.contains(next.object))
                {
                    Length distance = getDistance(next.object, record, next.position);
                    if (distance == null // null means the object overlaps and is close
                            || distance.si <= AbstractPerceptionIterable.this.maxDistance)
                    {
                        // next object is made available in the map
                        this.map.put(new PrimaryIteratorEntry(next.object, distance), record);
                        this.positions.put(record, next.position);
                    }
                }
            }
        }

    }

    /**
     * Returns whether the record is on the route.
     * @param record LaneRecord&lt;?&gt;; record
     * @return boolean; whether the record is on the route
     */
    final boolean isOnRoute(final LaneRecordInterface<?> record)
    {
        if (this.route == null)
        {
            return true;
        }
        Link link = record.getLane().getLink();
        int from;
        int to;
        from = this.route.indexOf(link.getStartNode());
        to = this.route.indexOf(link.getEndNode());
        return from != -1 && to != -1 && to - from == 1;
    }

    /**
     * Class of objects for subclasses to return. This can contain either a single object, or a set if there are multiple
     * objects at a single location.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    protected class Entry
    {

        /** Set. */
        private final Set<U> set;

        /** Object. */
        private final U object;

        /** Counter. */
        private final C counter;

        /** Position on the lane. */
        private final Length position;

        /**
         * Constructor.
         * @param object U; object
         * @param counter C; counter, may be {@code null}
         * @param position Length; position
         */
        public Entry(final U object, final C counter, final Length position)
        {
            this.set = null;
            this.object = object;
            this.counter = counter;
            this.position = position;
        }

        /**
         * Constructor.
         * @param set Set&lt;U&gt;; set
         * @param counter C; counter, may be {@code null}
         * @param position Length; position
         */
        public Entry(final Set<U> set, final C counter, final Length position)
        {
            this.set = set;
            this.object = null;
            this.counter = counter;
            this.position = position;
        }

        /**
         * Returns whether this entry contains a set.
         * @return whether this entry contains a set
         */
        final boolean isSet()
        {
            return this.set != null;
        }

        /**
         * Returns the underlying object. Use {@code !isSet()} to check whether there is an object.
         * @return U; underlying set
         */
        public U getObject()
        {
            return this.object;
        }

        /**
         * Returns the underlying set. Use {@code isSet()} to check whether there is a set.
         * @return Set&lt;U&gt;; underlying set
         */
        public Set<U> getSet()
        {
            return this.set;
        }

    }

}
