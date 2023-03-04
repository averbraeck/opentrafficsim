package org.opentrafficsim.road.gtu.lane.perception;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * A light-weight wrapper for LaneRecord search tools (PerceptionIterator). This is suitable for situations where parts of the
 * network not in the LaneStructure need to be perceived, such as conflicting lanes at intersection conflicts. Searches can only
 * be simple upstream or downstream searches, without lateral movement and without regard of a route. This class should not be
 * used whenever the LaneStructure can be used, as this class builds up a new tree each time step.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class LaneRecord implements LaneRecordInterface<LaneRecord>
{

    /** Lane. */
    private final Lane lane;

    /** Distance to start. */
    private final Length startDistance;

    /** GTU type. */
    private final GtuType gtuType;

    /** Stored next lanes. */
    private List<LaneRecord> next;

    /** Stored prev lanes. */
    private List<LaneRecord> prev;

    /**
     * Constructor.
     * @param lane Lane; lane
     * @param startDistance Length; distance to start
     * @param gtuType GtuType; GTU type
     */
    public LaneRecord(final Lane lane, final Length startDistance, final GtuType gtuType)
    {
        this.lane = lane;
        this.startDistance = startDistance;
        this.gtuType = gtuType;
    }

    /** {@inheritDoc} */
    @Override
    public List<LaneRecord> getNext()
    {
        if (this.next == null)
        {
            Set<Lane> set = this.lane.nextLanes(this.gtuType);
            this.next = new ArrayList<>();
            Length distance = this.startDistance.plus(getLength());
            for (Lane down : set)
            {
                this.next.add(new LaneRecord(down, distance, this.gtuType));
            }
        }
        return this.next;
    }

    /** {@inheritDoc} */
    @Override
    public List<LaneRecord> getPrev()
    {
        if (this.prev == null)
        {
            Set<Lane> set = this.lane.prevLanes(this.gtuType);
            this.prev = new ArrayList<>();
            for (Lane up : set)
            {
                this.prev.add(new LaneRecord(up, this.startDistance.minus(up.getLength()), this.gtuType));
            }
        }
        return this.prev;
    }

    /** {@inheritDoc} */
    @Override
    public Length getStartDistance()
    {
        return this.startDistance;
    }

    /** {@inheritDoc} */
    @Override
    public Length getLength()
    {
        return this.lane.getLength();
    }

    /** {@inheritDoc} */
    @Override
    public Lane getLane()
    {
        return this.lane;
    }

}
