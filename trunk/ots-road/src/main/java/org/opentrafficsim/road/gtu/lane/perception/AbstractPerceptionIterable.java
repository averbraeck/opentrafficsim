package org.opentrafficsim.road.gtu.lane.perception;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.Try;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;

/**
 * Abstract iterable that figures out how to find the next nearest object, including splits.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 16 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <H> headway type
 * @param <U> underlying object type
 * @param <R> record type
 * @param <C> counter type
 */
public abstract class AbstractPerceptionIterable<H extends Headway, U, R extends LaneRecord<R>, C>
        extends AbstractPerceptionReiterable<H, U>
{

    /** Root record. */
    final R root;

    /** Initial position. */
    final Length initialPosition;

    /** Search downstream (or upstream). */
    final boolean downstream;

    /** Max distance. */
    final double maxDistance;

    /** Position to which distance are calculated by subclasses. */
    private final RelativePosition relativePosition;

    /** Route of the GTU. */
    final Route route;

    /**
     * Constructor.
     * @param perceivingGtu LaneBasedGTU; perceiving GTU
     * @param root R; root record
     * @param initialPosition Length; initial position
     * @param downstream boolean; search downstream (or upstream)
     * @param maxDistance Length; max distance to search
     * @param relativePosition RelativePosition; position to which distance are calculated by subclasses
     * @param route Route; route of the GTU, may be {@code null}
     */
    public AbstractPerceptionIterable(final LaneBasedGTU perceivingGtu, final R root, final Length initialPosition,
            final boolean downstream, final Length maxDistance, final RelativePosition relativePosition, final Route route)
    {
        super(perceivingGtu);
        this.root = root;
        this.initialPosition = initialPosition;
        this.downstream = downstream;
        this.maxDistance = maxDistance.si;
        this.relativePosition = relativePosition;
        this.route = route;
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<PrimaryIteratorEntry> primaryIterator()
    {
        return new PrimaryIterator();
    }

    /**
     * Returns the next object(s) on the lane represented by the record. This should only consider objects on the given lane.
     * This method should not check the distance towards objects with the maximum distance.
     * @param record R; record representing the lane and direction
     * @param position Length; position to look beyond
     * @param counter C; counter
     * @return next object(s) on the lane or {@code null} if none
     * @throws GTUException on any exception in the process
     */
    protected abstract Entry getNext(R record, Length position, C counter) throws GTUException;

    /**
     * Returns the distance to the object. The position fed in to this method is directly taken from an {@code Entry} returned
     * by {@code getNext}. The two methods need to be consistent with each other.
     * @param object U; underlying object
     * @param record R; record representing the lane and direction
     * @param position Length; position of the object on the lane
     * @return Length; distance to the object
     */
    protected abstract Length getDistance(U object, R record, Length position);

    /**
     * Returns the longitudinal length of the relevant relative position such that distances to this points can be calculated.
     * @return Length; the longitudinal length of the relevant relative position such that distances to this points can be
     *         calculated
     */
    protected Length getDx()
    {
        return this.relativePosition.getDx();
    }

    /**
     * The primary iterator is used by all returned iterators to find the next object. This contains the core algorithm to deal
     * with splits and multiple objects at a single location.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 16 feb. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class PrimaryIterator implements Iterator<PrimaryIteratorEntry>
    {

        /** Map containing the objects found per branch. */
        SortedMap<PrimaryIteratorEntry, R> map;

        /** Position on the lane of each object. */
        Map<U, Length> positions = new HashMap<>();

        /** Sets of remaining objects at the same location. */
        Map<R, Queue<PrimaryIteratorEntry>> queues = new HashMap<>();

        /** Counter objects per lane. */
        Map<R, C> counters = new HashMap<>();

        /** Record regarding a postponed call to {@code getNext()}. */
        private R postponedRecord = null;

        /** Position regarding a postponed call to {@code getNext()}. */
        private Length postponedPosition = null;

        /** Constructor. */
        public PrimaryIterator()
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
            U next = nextEntry.object;
            R record = this.map.get(nextEntry);
            Length position = this.positions.get(next);
            this.map.remove(nextEntry);

            // see if we can obtain the next from a queue
            Queue<PrimaryIteratorEntry> queue = this.queues.get(next);
            if (queue != null)
            {
                PrimaryIteratorEntry nextNext = queue.poll();
                this.map.put(nextNext, record); // next object is made available in the map
                this.positions.put(nextNext.object, position);
                if (queue.isEmpty())
                {
                    this.queues.remove(record);
                }
                return new PrimaryIteratorEntry(nextNext.object, getDistance(nextNext.object, record, position));
            }

            // prepare for next
            this.postponedRecord = record;
            this.postponedPosition = position;
            return new PrimaryIteratorEntry(next, getDistance(next, record, position));
        }

        /**
         * Starts or restarts the process.
         */
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
         * @param record R; record
         * @param position Length; position
         */
        private final void prepareNext(final R record, final Length position)
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
                        for (R nextRecord : record.getNext())
                        {
                            if (isOnRoute(nextRecord))
                            {
                                prepareNext(nextRecord, Length.ZERO);
                            }
                        }
                    }
                    else
                    {
                        for (R nextRecord : record.getPrev())
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
                    Length distance = getDistance(nextNext, record, next.position);
                    if (distance == null // null means the object overlaps and is close
                            || distance.si <= AbstractPerceptionIterable.this.maxDistance)
                    {
                        // next object is made available in the map
                        this.map.put(new PrimaryIteratorEntry(nextNext, distance), record);
                        this.positions.put(nextNext, next.position);
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
                else
                {
                    Length distance = getDistance(next.object, record, next.position);
                    if (distance == null // null means the object overlaps and is close
                            || distance.si <= AbstractPerceptionIterable.this.maxDistance)
                    {
                        // next object is made available in the map
                        this.map.put(new PrimaryIteratorEntry(next.object, distance), record);
                        this.positions.put(next.object, next.position);
                    }
                }
            }
        }

    }

    /**
     * Returns whether the record is on the route.
     * @param record R; record
     * @return boolean; whether the record is on the route
     */
    boolean isOnRoute(final R record)
    {
        if (this.route == null)
        {
            return true;
        }
        Link link = record.getLane().getParentLink();
        int from;
        int to;
        if (record.getDirection().isPlus())
        {
            from = this.route.indexOf(link.getStartNode());
            to = this.route.indexOf(link.getEndNode());
        }
        else
        {
            from = this.route.indexOf(link.getEndNode());
            to = this.route.indexOf(link.getStartNode());
        }
        return from != -1 && to != -1 && to - from == 1;
    }

    /**
     * Class of objects for subclasses to return. This can contain either a single object, or a set if there are multiple
     * objects at a single location.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 16 feb. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    protected class Entry
    {

        /** Set. */
        final Set<U> set;

        /** Object. */
        final U object;

        /** Counter. */
        final C counter;

        /** Position on the lane. */
        final Length position;

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
        boolean isSet()
        {
            return this.set != null;
        }

    }

}
