package org.opentrafficsim.road.gtu.lane.perception;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;

/**
 * Iterable class to search over multiple lanes.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 20 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <H> headway type
 * @param <U> underlying headway type
 */
public class MultiLanePerceptionIterable<H extends Headway, U> extends AbstractPerceptionReiterable<H, U>
{

    /** Set of iterators per lane. */
    final Map<RelativeLane, Iterator<PrimaryIteratorEntry>> iterators = new HashMap<>();

    /** Map of lane per object. */
    final Map<U, RelativeLane> laneMap = new HashMap<>();

    /** Map of iterable per lane. */
    final Map<RelativeLane, AbstractPerceptionReiterable<H, U>> iterables = new HashMap<>();

    /**
     * Constructor.
     * @param perceivingGtu LaneBasedGTU; perceiving GTU
     */
    public MultiLanePerceptionIterable(final LaneBasedGTU perceivingGtu)
    {
        super(perceivingGtu);
    }

    /**
     * Adds an iterable for a lane.
     * @param lane Lane; lane
     * @param iterable AbstractPerceptionReiterable; iterable
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
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 21 feb. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class MultiLaneIterator implements Iterator<PrimaryIteratorEntry>
    {

        /** Sorted elements per lane. */
        SortedMap<PrimaryIteratorEntry, RelativeLane> elements;

        /** Constructor. */
        public MultiLaneIterator()
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

            MultiLanePerceptionIterable.this.laneMap.put(next.object, lane);
            return next;
        }

        /**
         * Starts the process.
         */
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
    public H perceive(final LaneBasedGTU perceivingGtu, final U object, final Length distance)
            throws GTUException, ParameterException
    {
        return this.iterables.get(this.laneMap.get(object)).perceive(perceivingGtu, object, distance);
    }
}
