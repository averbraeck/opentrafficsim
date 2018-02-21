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
 * @param <R> record type
 * @param <C> counter type
 */
public abstract class AbstractPerceptionIterable<H extends Headway, R extends LaneRecord<R>, C>
        extends AbstractPerceptionReiterable<H>
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
     * @param root R; root record
     * @param initialPosition Length; initial position
     * @param downstream boolean; search downstream (or upstream)
     * @param maxDistance Length; max distance to search
     * @param relativePosition RelativePosition; position to which distance are calculated by subclasses
     * @param route Route; route of the GTU, may be {@code null}
     */
    public AbstractPerceptionIterable(final R root, final Length initialPosition, final boolean downstream,
            final Length maxDistance, final RelativePosition relativePosition, final Route route)
    {
        this.root = root;
        this.initialPosition = initialPosition;
        this.downstream = downstream;
        this.maxDistance = maxDistance.si;
        this.relativePosition = relativePosition;
        this.route = route;
    }

    /** {@inheritDoc} */
    @Override
    protected Iterator<H> primaryIterator()
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
    private class PrimaryIterator implements Iterator<H>
    {

        /** Map containing the objects found per branch. */
        SortedMap<H, R> map;

        /** Position on the lane of each object. */
        Map<H, Length> positions = new HashMap<>();

        /** Sets of remaining objects at the same location. */
        Map<R, Queue<H>> queues = new HashMap<>();

        /** Counter objects per lane. */
        Map<R, C> counters = new HashMap<>();

        /** Constructor. */
        public PrimaryIterator()
        {
            //
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext()
        {
            // start the process
            if (this.map == null)
            {
                this.map = new TreeMap<>();
                prepareNext(AbstractPerceptionIterable.this.root, AbstractPerceptionIterable.this.initialPosition);
            }
            // check next value
            return !this.map.isEmpty();
        }

        /** {@inheritDoc} */
        @Override
        public H next()
        {
            // start the process
            if (this.map == null)
            {
                this.map = new TreeMap<>();
                prepareNext(AbstractPerceptionIterable.this.root, AbstractPerceptionIterable.this.initialPosition);
            }

            // get and remove next
            H next = this.map.firstKey();
            R record = this.map.get(next);
            Length position = this.positions.get(next);
            this.map.remove(next);
            this.positions.remove(next);

            // see if we can obtain the next from a queue
            Queue<H> queue = this.queues.get(next);
            if (queue != null)
            {
                H nextNext = queue.poll();
                this.map.put(nextNext, record); // next object is made available in the map
                this.positions.put(nextNext, position);
                if (queue.isEmpty())
                {
                    this.queues.remove(record);
                }
                return next;
            }

            // prepare the next object
            prepareNext(record, position);
            return next;
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
                        for (R nextRecord : AbstractPerceptionIterable.this.downstream ? record.getNext() : record.getPrev())
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
                    Iterator<H> it = next.set.iterator();
                    H nextNext = iterator().next();
                    if (nextNext.getDistance().si <= AbstractPerceptionIterable.this.maxDistance)
                    {
                        this.map.put(nextNext, record); // next object is made available in the map
                        this.positions.put(nextNext, next.position);
                        if (next.set.size() > 1)
                        {
                            Queue<H> queue = new LinkedList<>(); // remaining at this location are made available in a queue
                            while (it.hasNext())
                            {
                                queue.add(it.next());
                            }
                            this.queues.put(record, queue);
                        }
                    }
                }
                else
                {
                    if (next.value.getDistance() == null
                            || next.value.getDistance().si <= AbstractPerceptionIterable.this.maxDistance)
                    {
                        this.map.put(next.value, record); // next object is made available in the map
                        this.positions.put(next.value, next.position);
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
        final Set<H> set;

        /** Value. */
        final H value;

        /** Counter. */
        final C counter;

        /** Position on the lane. */
        final Length position;

        /**
         * Constructor.
         * @param value T; value
         * @param counter C; counter, may be {@code null}
         * @param position Length; position
         */
        public Entry(final H value, final C counter, final Length position)
        {
            this.set = null;
            this.value = value;
            this.counter = counter;
            this.position = position;
        }

        /**
         * Constructor.
         * @param set Set&lt;T&gt;; set
         * @param counter C; counter, may be {@code null}
         * @param position Length; position
         */
        public Entry(final Set<H> set, final C counter, final Length position)
        {
            this.set = set;
            this.value = null;
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
