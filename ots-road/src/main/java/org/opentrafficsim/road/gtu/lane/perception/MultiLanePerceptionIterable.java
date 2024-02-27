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
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;

/**
 * Iterable class to search over multiple lanes.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <H> headway type
 * @param <U> underlying headway type
 */
public class MultiLanePerceptionIterable<H extends Headway, U> extends AbstractPerceptionReiterable<H, U>
{

    /** Set of iterators per lane. */
    private final Map<RelativeLane, Iterator<PrimaryIteratorEntry>> iterators = new LinkedHashMap<>();

    /** Map of lane per object. */
    private final Map<U, RelativeLane> laneMap = new LinkedHashMap<>();

    /** Map of iterable per lane. */
    private final Map<RelativeLane, AbstractPerceptionReiterable<H, U>> iterables = new LinkedHashMap<>();

    /**
     * Constructor.
     * @param perceivingGtu LaneBasedGtu; perceiving GTU
     */
    public MultiLanePerceptionIterable(final LaneBasedGtu perceivingGtu)
    {
        super(perceivingGtu);
    }

    /**
     * Adds an iterable for a lane.
     * @param lane RelativeLane; lane
     * @param iterable AbstractPerceptionReiterable&lt;H, U&gt;; iterable
     */
    public void addIterable(final RelativeLane lane, final AbstractPerceptionReiterable<H, U> iterable)
    {
        this.iterators.put(lane, iterable.getPrimaryIterator());
        this.iterables.put(lane, iterable);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<PrimaryIteratorEntry> primaryIterator()
    {
        return new MultiLaneIterator();
    }

    /**
     * Iterator that returns the closest element from a set of lanes.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    private class MultiLaneIterator implements Iterator<PrimaryIteratorEntry>
    {

        /** Sorted elements per lane. */
        private SortedMap<PrimaryIteratorEntry, RelativeLane> elements;

        /** Constructor. */
        MultiLaneIterator()
        {
            //
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext()
        {
            assureNext();
            return !this.elements.isEmpty();
        }

        /** {@inheritDoc} */
        @SuppressWarnings("synthetic-access")
        @Override
        public PrimaryIteratorEntry next()
        {
            assureNext();
            if (this.elements.isEmpty())
            {
                throw new NoSuchElementException();
            }

            // get and remove next
            PrimaryIteratorEntry next = this.elements.firstKey();
            RelativeLane lane = this.elements.get(next);
            this.elements.remove(next);

            // prepare next
            Iterator<PrimaryIteratorEntry> laneIterator = MultiLanePerceptionIterable.this.iterators.get(lane);
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

            MultiLanePerceptionIterable.this.laneMap.put(next.getObject(), lane);
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
                    Iterator<PrimaryIteratorEntry> laneIterator = MultiLanePerceptionIterable.this.iterators.get(lane);
                    if (laneIterator.hasNext())
                    {
                        this.elements.put(laneIterator.next(), lane);
                    }
                }
            }
        }

    }

    /** {@inheritDoc} */
    @Override
    public H perceive(final LaneBasedGtu perceivingGtu, final U object, final Length distance)
            throws GtuException, ParameterException
    {
        return this.iterables.get(this.laneMap.get(object)).perceive(perceivingGtu, object, distance);
    }
}
