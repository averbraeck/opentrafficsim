package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LaneStructureRecord;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;

/**
 * Utilities to perceive neighbors.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class NeighborsUtil
{

    /**
     * Constructor.
     */
    private NeighborsUtil()
    {
        //
    }

    /**
     * Returns a set of first leaders per branch, relative to given relative position. Helper method to find first leaders and
     * GTU's alongside.
     * @param startRecord LaneStructureRecord; lane structure record to start the search from
     * @param egoRelativePosition RelativePosition; position of GTU to start search from
     * @param egoFrontPosition RelativePosition; front position of GTU to determine headway
     * @param otherRelativePosition RelativePosition.TYPE; position of other GTU that has to be downstream
     * @param now Time; current time
     * @return set of first leaders per branch
     * @throws GtuException if the GTU was not initialized
     * @throws ParameterException if a parameter was not present or out of bounds
     */
    public static SortedSet<DistanceGTU> getFirstDownstreamGTUs(final LaneStructureRecord startRecord,
            final RelativePosition egoRelativePosition, final RelativePosition egoFrontPosition,
            final RelativePosition.Type otherRelativePosition, final Time now) throws GtuException, ParameterException
    {
        SortedSet<DistanceGTU> headwaySet = new TreeSet<>();
        Set<LaneStructureRecord> currentSet = new LinkedHashSet<>();
        Set<LaneStructureRecord> nextSet = new LinkedHashSet<>();
        Length dxSearch = egoRelativePosition.dx();
        Length dxHeadway = egoFrontPosition.dx();
        LaneStructureRecord record = startRecord;
        branchUpstream(record, dxSearch, currentSet);
        // move downstream over branches as long as no vehicles are found
        while (!currentSet.isEmpty())
        {
            Iterator<LaneStructureRecord> iterator = currentSet.iterator();
            while (iterator.hasNext())
            {
                record = iterator.next();
                /*-
                 *                                             _ _ _ ______________________ _ _ _
                 *                                                             _|___    |
                 * find any vehicle downstream of this point on lane A |      |__o__| A |
                 *                                             _ _ _ ___________|_______|__ _ _ _ 
                 *                                                     (--------) negative distance
                 */
                LaneBasedGtu down = record.getLane().getGtuAhead(record.getStartDistance().neg().plus(dxSearch),
                        otherRelativePosition, now);
                if (down != null)
                {
                    // GTU found, add to set
                    headwaySet.add(new DistanceGTU(down,
                            record.getStartDistance().plus(down.position(record.getLane(), down.getRear())).minus(dxHeadway)));
                }
                else
                {
                    // no GTU found, search on next lanes in next loop and maintain cumulative length
                    for (LaneStructureRecord next : record.getNext())
                    {
                        nextSet.add(next);
                    }
                }
            }
            currentSet = nextSet;
            nextSet = new LinkedHashSet<>();
        }
        return headwaySet;
    }

    /**
     * Returns a set of lanes to start from for a downstream search, upstream of the reference lane if the tail is before this
     * lane.
     * @param record LaneStructureRecord; start record
     * @param dx Length; distance between reference point and point to search from
     * @param set Set&lt;LaneStructureRecord&gt;; set of lanes that is recursively built up, starting with the reference record
     */
    private static void branchUpstream(final LaneStructureRecord record, final Length dx, final Set<LaneStructureRecord> set)
    {
        Length pos = record.getStartDistance().neg().minus(dx);
        if (pos.lt0() && !record.getPrev().isEmpty())
        {
            for (LaneStructureRecord prev : record.getPrev())
            {
                branchUpstream(prev, dx, set);
            }
        }
        else
        {
            set.add(record);
        }
    }

    /**
     * Returns a set of first followers per branch, relative to given relative position. Helper method to find first followers
     * and GTU's alongside.
     * @param startRecord LaneStructureRecord; lane structure record to start the search from
     * @param egoRelativePosition RelativePosition; position of GTU to start search from
     * @param egoRearPosition RelativePosition; rear position of GTU to determine headway
     * @param otherRelativePosition RelativePosition.TYPE; type of position of other GTU that has to be upstream
     * @param now Time; current time
     * @return set of first followers per branch
     * @throws GtuException if the GTU was not initialized
     * @throws ParameterException if a parameter was not present or out of bounds
     */
    public static SortedSet<DistanceGTU> getFirstUpstreamGTUs(final LaneStructureRecord startRecord,
            final RelativePosition egoRelativePosition, final RelativePosition egoRearPosition,
            final RelativePosition.Type otherRelativePosition, final Time now) throws GtuException, ParameterException
    {
        SortedSet<DistanceGTU> headwaySet = new TreeSet<>();
        Set<LaneStructureRecord> currentSet = new LinkedHashSet<>();
        Set<LaneStructureRecord> prevSet = new LinkedHashSet<>();
        Length dxSearch = egoRelativePosition.dx();
        Length dxHeadway = egoRearPosition.dx();
        LaneStructureRecord record = startRecord;
        branchDownstream(record, dxSearch, currentSet);
        // move upstream over branches as long as no vehicles are found
        while (!currentSet.isEmpty())
        {
            Iterator<LaneStructureRecord> iterator = currentSet.iterator();
            while (iterator.hasNext())
            {
                record = iterator.next();
                /*-
                 * _ _ _ ______________________ _ _ _ 
                 *         |    ___|_   
                 *         | A |__o__|      | find any upstream of this point on lane A
                 * _ _ _ __|_______|___________ _ _ _ 
                 *         (----------------) distance
                 */
                LaneBasedGtu up = record.getLane().getGtuBehind(record.getStartDistance().neg().plus(dxSearch),
                        otherRelativePosition, now);
                if (up != null)
                {
                    // GTU found, add to set
                    headwaySet.add(new DistanceGTU(up, record.getStartDistance().neg()
                            .minus(up.position(record.getLane(), up.getFront())).plus(dxHeadway)));
                }
                else
                {
                    // no GTU found, search on next lanes in next loop and maintain cumulative length
                    for (LaneStructureRecord prev : record.getPrev())
                    {
                        prevSet.add(prev);
                    }
                }
            }
            currentSet = prevSet;
            prevSet = new LinkedHashSet<>();
        }
        return headwaySet;
    }

    /**
     * Returns a set of lanes to start from for an upstream search, downstream of the reference lane if the front is after this
     * lane.
     * @param record LaneStructureRecord; start record
     * @param dx Length; distance between reference point and point to search from
     * @param set Set&lt;LaneStructureRecord&gt;; set of lanes that is recursively built up, starting with the reference record
     */
    private static void branchDownstream(final LaneStructureRecord record, final Length dx, final Set<LaneStructureRecord> set)
    {
        Length pos = record.getStartDistance().neg().plus(dx);
        if (pos.gt(record.getLane().getLength()))
        {
            for (LaneStructureRecord next : record.getNext())
            {
                branchDownstream(next, dx, set);
            }
        }
        else
        {
            set.add(record);
        }
    }

    /**
     * Translation from a set of {@code DistanceGTU}'s, to a sorted set of {@code HeadwayGtu}'s. This bridges the gap between a
     * raw network search, and the perceived result.
     * @param base SortedSet&lt;DistanceGTU&gt;; base set of GTU's at distance
     * @param headwayGtuType HeadwayGtuType; headway type for perceived GTU's
     * @param perceivingGtu LaneBasedGtu; perceiving GTU
     * @param downstream boolean; whether the GTU's are downstream
     * @return SortedSet&lt;HeadwayGtu&gt;; set of perceived GTU's
     */
    public static SortedSet<HeadwayGtu> perceive(final SortedSet<DistanceGTU> base, final HeadwayGtuType headwayGtuType,
            final LaneBasedGtu perceivingGtu, final boolean downstream)
    {
        return new SortedNeighborsSet(base, headwayGtuType, perceivingGtu, downstream);
    }

    /**
     * GTU at a distance, as preliminary info towards perceiving it. For instance, as a set from a search algorithm.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public static class DistanceGTU implements Comparable<DistanceGTU>
    {

        /** GTU. */
        private LaneBasedGtu gtu;

        /** Distance. */
        private Length distance;

        /**
         * Constructor.
         * @param gtu LaneBasedGtu; GTU
         * @param distance Length; distance
         */
        DistanceGTU(final LaneBasedGtu gtu, final Length distance)
        {
            this.gtu = gtu;
            this.distance = distance;
        }

        /**
         * Returns the GTU.
         * @return LaneBasedGtu; GTU
         */
        public LaneBasedGtu getGTU()
        {
            return this.gtu;
        }

        /**
         * Returns the distance.
         * @return Length; distance
         */
        public Length getDistance()
        {
            return this.distance;
        }

        /** {@inheritDoc} */
        @Override
        public int compareTo(final DistanceGTU o)
        {
            return this.distance.compareTo(o.distance);
        }
    }

    /**
     * Translation from a set of {@code DistanceGTU}'s, to a sorted set of {@code HeadwayGtu}'s. This bridges the gap between a
     * raw network search, and the perceived result.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    private static class SortedNeighborsSet implements SortedSet<HeadwayGtu>
    {

        /** Base set of GTU's at distance. */
        private final SortedSet<DistanceGTU> base;

        /** Headway type for perceived GTU's. */
        private final HeadwayGtuType headwayGtuType;

        /** Perceiving GTU. */
        private final LaneBasedGtu perceivingGtu;

        /** Whether the GTU's are downstream. */
        private final boolean downstream;

        /** Contains all GTU's perceived so far, to prevent re-perception. */
        private final SortedMap<String, HeadwayGtu> all = new TreeMap<>();

        /**
         * Constructor.
         * @param base SortedSet&lt;DistanceGTU&gt;; base set of GTU's at distance
         * @param headwayGtuType HeadwayGtuType; headway type for perceived GTU's
         * @param perceivingGtu LaneBasedGtu; perceiving GTU
         * @param downstream boolean; whether the GTU's are downstream
         */
        SortedNeighborsSet(final SortedSet<DistanceGTU> base, final HeadwayGtuType headwayGtuType,
                final LaneBasedGtu perceivingGtu, final boolean downstream)
        {
            this.base = base;
            this.headwayGtuType = headwayGtuType;
            this.perceivingGtu = perceivingGtu;
            this.downstream = downstream;
        }

        /** {@inheritDoc} */
        @Override
        public int size()
        {
            return this.base.size();
        }

        /** {@inheritDoc} */
        @Override
        public boolean isEmpty()
        {
            return this.base.isEmpty();
        }

        /**
         * Make sure all GTU are available in perceived for. Helper method.
         */
        private void getAll()
        {
            Iterator<HeadwayGtu> it = iterator();
            while (it.hasNext())
            {
                @SuppressWarnings("unused")
                HeadwayGtu gtu = it.next(); // iterator creates all HeadwayGtu's
            }
        }

        /** {@inheritDoc} */
        @Override
        public boolean contains(final Object o)
        {
            getAll();
            return this.all.containsValue(o);
        }

        /** {@inheritDoc} */
        @Override
        public Iterator<HeadwayGtu> iterator()
        {
            return new Iterator<HeadwayGtu>()
            {
                @SuppressWarnings("synthetic-access")
                private Iterator<DistanceGTU> it = SortedNeighborsSet.this.base.iterator();

                @Override
                public boolean hasNext()
                {
                    return this.it.hasNext();
                }

                @SuppressWarnings("synthetic-access")
                @Override
                public HeadwayGtu next()
                {
                    DistanceGTU next = this.it.next();
                    if (next == null)
                    {
                        throw new ConcurrentModificationException();
                    }
                    HeadwayGtu out = SortedNeighborsSet.this.all.get(next.getGTU().getId());
                    if (out == null)
                    {
                        out = Try.assign(() -> SortedNeighborsSet.this.headwayGtuType.createHeadwayGtu(
                                SortedNeighborsSet.this.perceivingGtu, next.getGTU(), next.getDistance(),
                                SortedNeighborsSet.this.downstream), "Exception while perceiving a neighbor.");
                        SortedNeighborsSet.this.all.put(next.getGTU().getId(), out);
                    }
                    return out;
                }
            };
        }

        /** {@inheritDoc} */
        @Override
        public Object[] toArray()
        {
            getAll();
            return this.all.values().toArray();
        }

        /** {@inheritDoc} */
        @Override
        public <T> T[] toArray(final T[] a)
        {
            getAll();
            return this.all.values().toArray(a);
        }

        /** {@inheritDoc} */
        @Override
        public boolean add(final HeadwayGtu e)
        {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public boolean remove(final Object o)
        {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public boolean containsAll(final Collection<?> c)
        {
            getAll();
            return this.all.values().containsAll(c);
        }

        /** {@inheritDoc} */
        @Override
        public boolean addAll(final Collection<? extends HeadwayGtu> c)
        {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public boolean retainAll(final Collection<?> c)
        {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public boolean removeAll(final Collection<?> c)
        {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public void clear()
        {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public Comparator<? super HeadwayGtu> comparator()
        {
            return null;
        }

        /**
         * Helper method for sub-lists to find distance-GTU from the perceived GTU.
         * @param element HeadwayGtu; perceived GTU
         * @return DistanceGtu; pertaining to given GTU
         */
        private DistanceGTU getGTU(final HeadwayGtu element)
        {
            for (DistanceGTU distanceGtu : this.base)
            {
                if (distanceGtu.getGTU().getId().equals(element.getId()))
                {
                    return distanceGtu;
                }
            }
            throw new IllegalArgumentException("GTU used to obtain a subset is not in the set.");
        }

        /** {@inheritDoc} */
        @Override
        public SortedSet<HeadwayGtu> subSet(final HeadwayGtu fromElement, final HeadwayGtu toElement)
        {
            return new SortedNeighborsSet(this.base.subSet(getGTU(fromElement), getGTU(toElement)), this.headwayGtuType,
                    this.perceivingGtu, this.downstream);
        }

        /** {@inheritDoc} */
        @Override
        public SortedSet<HeadwayGtu> headSet(final HeadwayGtu toElement)
        {
            return new SortedNeighborsSet(this.base.headSet(getGTU(toElement)), this.headwayGtuType, this.perceivingGtu,
                    this.downstream);
        }

        /** {@inheritDoc} */
        @Override
        public SortedSet<HeadwayGtu> tailSet(final HeadwayGtu fromElement)
        {
            return new SortedNeighborsSet(this.base.tailSet(getGTU(fromElement)), this.headwayGtuType, this.perceivingGtu,
                    this.downstream);
        }

        /** {@inheritDoc} */
        @Override
        public HeadwayGtu first()
        {
            return iterator().next();
        }

        /** {@inheritDoc} */
        @Override
        public HeadwayGtu last()
        {
            getAll();
            return this.all.get(this.all.lastKey());
        }

    }

}
