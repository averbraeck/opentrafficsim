package org.opentrafficsim.road.gtu.lane.perception;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;

/**
 * Iterable class to search over multiple lanes.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 20 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <H> headway type
 */
public class MultiLanePerceptionIterable<H extends Headway> extends AbstractPerceptionReiterable<H>
{

    /** Set of iterators per lane. */
    Map<RelativeLane, Iterator<H>> iterators = new HashMap<>();
    
    /**
     * Adds an iterable for a lane.
     * @param lane Lane; lane
     * @param iterable Iterable; iterable
     */
    public void addIterable(final RelativeLane lane, final Iterable<H> iterable)
    {
        this.iterators.put(lane, iterable.iterator());
    }
    
    /** {@inheritDoc} */
    @Override
    protected Iterator<H> primaryIterator()
    {
        return new MultiLaneIterator();
    }
    
    /**
     * Iterator that returns the closest element from a set of lanes.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 21 feb. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class MultiLaneIterator implements Iterator<H>
    {

        /** Sorted elements per lane. */
        SortedMap<H, RelativeLane> elements;
        
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
        public H next()
        {
            assureNext();
            if (this.elements.isEmpty())
            {
                throw new NoSuchElementException();
            }
            
            // get and remove next
            H next = this.elements.firstKey();
            RelativeLane lane = this.elements.get(next);
            this.elements.remove(next);
            
            // prepare next
            Iterator<H> laneIterator = MultiLanePerceptionIterable.this.iterators.get(lane);
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
                    Iterator<H> it = MultiLanePerceptionIterable.this.iterators.get(lane);
                    if (it.hasNext())
                    {
                        this.elements.put(it.next(), lane);
                    }
                }
            }
        }
        
    }
}
