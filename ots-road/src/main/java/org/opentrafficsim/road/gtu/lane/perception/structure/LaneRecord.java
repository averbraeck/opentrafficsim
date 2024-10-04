package org.opentrafficsim.road.gtu.lane.perception.structure;

import java.util.LinkedHashSet;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Record of a lane within the lane structure.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LaneRecord implements LaneRecordInterface<LaneRecord>
{

    /** Lane. */
    private final Lane lane;

    /** Relative lane. */
    private final RelativeLane relativeLane;

    /** Start distance. */
    private final Length startDistance;

    /** Merge distance, i.e. the distance after which this road merges with the road the GTU is at. */
    private final Length mergeDistance;

    /** Set of downstream records. */
    private final Set<LaneRecord> next = new LinkedHashSet<>();

    /** Set of upstream records. */
    private final Set<LaneRecord> prev = new LinkedHashSet<>();

    /** Set of lateral records. */
    private final Set<LaneRecord> lateral = new LinkedHashSet<>();

    /**
     * Constructor.
     * @param lane lane.
     * @param relativeLane relative lane.
     * @param startDistance start distance.
     * @param mergeDistance merge distance, i.e. the distance after which this road merges with the road the GTU is at.
     */
    public LaneRecord(final Lane lane, final RelativeLane relativeLane, final Length startDistance, final Length mergeDistance)
    {
        this.lane = lane;
        this.relativeLane = relativeLane;
        this.startDistance = startDistance;
        this.mergeDistance = mergeDistance;
    }

    /**
     * Returns the lane.
     * @return lane.
     */
    @Override
    public Lane getLane()
    {
        return this.lane;
    }

    /**
     * Returns the relative lane.
     * @return relative lane.
     */
    public RelativeLane getRelativeLane()
    {
        return this.relativeLane;
    }

    /**
     * Returns the start distance. This value is negative for anything upstream of the reference point of the GTU.
     * @return start distance.
     */
    @Override
    public Length getStartDistance()
    {
        return this.startDistance;
    }

    /**
     * Returns the end distance. This value is negative for anything upstream of the reference point of the GTU.
     * @return end distance.
     */
    public Length getEndDistance()
    {
        return this.startDistance.plus(this.lane.getLength());
    }

    /** {@inheritDoc} */
    @Override
    public Length getLength()
    {
        return this.lane.getLength();
    }

    /**
     * Returns the merge distance, i.e. the distance after which this road merges with the road the GTU is at.
     * @return merge distance.
     */
    public Length getMergeDistance()
    {
        return this.mergeDistance;
    }

    /**
     * Add downstream lane.
     * @param downstream downstream lane.
     */
    public void addNext(final LaneRecord downstream)
    {
        this.next.add(downstream);
    }

    /** {@inheritDoc} */
    @Override
    public Set<LaneRecord> getNext()
    {
        return this.next;
    }

    /**
     * Add downstream lane.
     * @param upstream downstream lane.
     */
    public void addPrev(final LaneRecord upstream)
    {
        this.prev.add(upstream);
    }

    /** {@inheritDoc} */
    @Override
    public Set<LaneRecord> getPrev()
    {
        return this.prev;
    }

    /**
     * Add lateral lane.
     * @param lateral downstream lane.
     */
    public void addLateral(final LaneRecord lateral)
    {
        this.lateral.add(lateral);
    }

    /**
     * Get lateral lanes.
     * @return lateral lanes.
     */
    public Set<LaneRecord> lateral()
    {
        return this.lateral;
    }

    /**
     * Returns whether the record is on the route.
     * @param route route.
     * @return whether the record is on the route
     */
    public boolean isOnRoute(final Route route)
    {
        if (route == null)
        {
            return true;
        }
        Link link = getLane().getLink();
        int from;
        int to;
        from = route.indexOf(link.getStartNode());
        to = route.indexOf(link.getEndNode());
        return from != -1 && to != -1 && to - from == 1;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDownstreamBranch()
    {
        return this.mergeDistance.eq0() || this.getEndDistance().gt0();
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "LaneRecord [lane=" + this.lane + ", relativeLane=" + this.relativeLane + ", startDistance=" + this.startDistance
                + ", mergeDistance=" + this.mergeDistance + "]";
    }

}
