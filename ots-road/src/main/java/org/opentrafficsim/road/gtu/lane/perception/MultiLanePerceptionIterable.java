package org.opentrafficsim.road.gtu.lane.perception;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedObject;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

/**
 * Iterable class to search over multiple lanes.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <O> perceiving object type (an {@code O} is perceiving a {@code U} as a {@code P})
 * @param <P> perceived object type (an {@code O} is perceiving a {@code U} as a {@code P})
 * @param <U> underlying object type (an {@code O} is perceiving a {@code U} as a {@code P})
 */
public class MultiLanePerceptionIterable<O extends LaneBasedObject, P extends PerceivedObject, U>
        extends AbstractPerceptionReiterable<O, P, U>
{

    /** Set of iterators per lane. */
    private final Map<RelativeLane, Iterator<UnderlyingDistance<U>>> iterators = new LinkedHashMap<>();

    /** Map of lane per object. */
    private final Map<U, RelativeLane> laneMap = new LinkedHashMap<>();

    /** Map of iterable per lane. */
    private final Map<RelativeLane, AbstractPerceptionReiterable<O, P, U>> iterables = new LinkedHashMap<>();

    /**
     * Constructor.
     * @param perceivingObject perceiving object
     */
    public MultiLanePerceptionIterable(final O perceivingObject)
    {
        super(perceivingObject);
    }

    /**
     * Adds an iterable for a lane.
     * @param lane lane
     * @param iterable iterable
     */
    public void addIterable(final RelativeLane lane, final AbstractPerceptionReiterable<O, P, U> iterable)
    {
        this.iterators.put(lane, iterable.getPrimaryIterator());
        this.iterables.put(lane, iterable);
    }

    @Override
    public Iterator<UnderlyingDistance<U>> primaryIterator()
    {
        return new MultiLaneIterator();
    }

    /**
     * Iterator that returns the closest element from a set of lanes.
     * <p>
     * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    private class MultiLaneIterator implements Iterator<UnderlyingDistance<U>>
    {

        /** Sorted elements per lane. */
        private SortedMap<UnderlyingDistance<U>, RelativeLane> elements;

        /** Constructor. */
        MultiLaneIterator()
        {
            //
        }

        @Override
        public boolean hasNext()
        {
            assureNext();
            return !this.elements.isEmpty();
        }

        @SuppressWarnings("synthetic-access")
        @Override
        public UnderlyingDistance<U> next()
        {
            assureNext();
            if (this.elements.isEmpty())
            {
                throw new NoSuchElementException();
            }

            // get and remove next
            UnderlyingDistance<U> next = this.elements.firstKey();
            RelativeLane lane = this.elements.get(next);
            this.elements.remove(next);

            // prepare next
            Iterator<UnderlyingDistance<U>> laneIterator = MultiLanePerceptionIterable.this.iterators.get(lane);
            if (laneIterator != null)
            {
                if (laneIterator.hasNext())
                {
                    this.elements.put(laneIterator.next(), lane);
                }
                else
                {
                    // remove it, it has no more elements to offer
                    MultiLanePerceptionIterable.this.iterators.remove(lane);
                }
            }

            MultiLanePerceptionIterable.this.laneMap.put(next.object(), lane);
            return next;
        }

        /**
         * Starts the process.
         */
        @SuppressWarnings("synthetic-access")
        public void assureNext()
        {
            if (this.elements == null)
            {
                this.elements = new TreeMap<>();
                for (RelativeLane lane : MultiLanePerceptionIterable.this.iterators.keySet())
                {
                    Iterator<UnderlyingDistance<U>> laneIterator = MultiLanePerceptionIterable.this.iterators.get(lane);
                    if (laneIterator.hasNext())
                    {
                        this.elements.put(laneIterator.next(), lane);
                    }
                }
            }
        }

    }

    @Override
    public P perceive(final U object, final Length distance) throws GtuException, ParameterException
    {
        return this.iterables.get(this.laneMap.get(object)).perceive(object, distance);
    }
}
