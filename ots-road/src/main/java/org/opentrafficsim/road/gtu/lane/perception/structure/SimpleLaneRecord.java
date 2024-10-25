package org.opentrafficsim.road.gtu.lane.perception.structure;

import java.util.LinkedHashSet;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * A light-weight wrapper for NavigatingIterable. This is suitable for situations where parts of the network not in the
 * LaneStructure need to be perceived, such as conflicting lanes at intersection conflicts. Searches can only be simple upstream
 * or downstream searches, without lateral movement and without regard of a route. This class should not be used whenever the
 * LaneStructure can be used.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class SimpleLaneRecord implements LaneRecordInterface<SimpleLaneRecord>
{

    /** Lane. */
    private final Lane lane;

    /** Distance to start. */
    private final Length startDistance;

    /** GTU type. */
    private final GtuType gtuType;

    /** Stored next lanes. */
    private Set<SimpleLaneRecord> next;

    /** Stored prev lanes. */
    private Set<SimpleLaneRecord> prev;

    /**
     * Constructor.
     * @param lane lane
     * @param startDistance distance to start
     * @param gtuType GTU type, use {@code null} to find all lanes, including shoulders
     */
    public SimpleLaneRecord(final Lane lane, final Length startDistance, final GtuType gtuType)
    {
        this.lane = lane;
        this.startDistance = startDistance;
        this.gtuType = gtuType;
    }

    @Override
    public Set<SimpleLaneRecord> getNext()
    {
        if (this.next == null)
        {
            Set<Lane> set = this.lane.nextLanes(this.gtuType);
            this.next = new LinkedHashSet<>();
            Length distance = this.startDistance.plus(getLength());
            for (Lane down : set)
            {
                this.next.add(new SimpleLaneRecord(down, distance, this.gtuType));
            }
        }
        return this.next;
    }

    @Override
    public Set<SimpleLaneRecord> getPrev()
    {
        if (this.prev == null)
        {
            Set<Lane> set = this.lane.prevLanes(this.gtuType);
            this.prev = new LinkedHashSet<>();
            for (Lane up : set)
            {
                this.prev.add(new SimpleLaneRecord(up, this.startDistance.minus(up.getLength()), this.gtuType));
            }
        }
        return this.prev;
    }

    @Override
    public Length getStartDistance()
    {
        return this.startDistance;
    }

    @Override
    public Lane getLane()
    {
        return this.lane;
    }

}
