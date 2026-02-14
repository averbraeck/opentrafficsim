package org.opentrafficsim.road.network.object;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableHashSet;
import org.djutils.immutablecollections.ImmutableSet;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.Lane;
import org.opentrafficsim.road.network.conflict.BusStopConflictRule;
import org.opentrafficsim.road.network.conflict.Conflict;

/**
 * A bus stop is a location on a lane. The stop has a name, and a set of lines. At a single stop in reality, there may be
 * different locations where busses stop for different lines. A {@code BusStop} pertains to only one such location. The bus stop
 * in reality is represented by a shared name over a few {@code BusStop}'s, with different lines. As lines may also be set
 * dynamically, the name and lines are insufficient to identify a specific {@code BusStop}. Hence there is a fixed unique id per
 * {@code BusStop}.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class BusStop extends AbstractLaneBasedObject
{

    /** Line numbers. */
    private final Set<String> lines = new LinkedHashSet<>();

    /** Stop name. */
    private final String name;

    /** Stored conflicts downstream. */
    private Set<Conflict> conflicts = null;

    /** Bus type. */
    private final GtuType busType;

    /**
     * Constructor.
     * @param id id
     * @param lane lane
     * @param longitudinalPosition position
     * @param name name of stop
     * @param busType bus type.
     * @throws NetworkException when the position on the lane is out of bounds
     */
    public BusStop(final String id, final Lane lane, final Length longitudinalPosition, final String name,
            final GtuType busType) throws NetworkException
    {
        super(id, lane, longitudinalPosition, LaneBasedObject.makeLine(lane, longitudinalPosition), Length.ZERO);
        this.name = name;
        this.busType = busType;
        init();
    }

    /**
     * Sets the lines.
     * @param lines lines that stop at this location
     */
    public final void setLines(final Set<String> lines)
    {
        this.lines.clear();
        this.lines.addAll(lines);
    }

    /**
     * Returns the lines set.
     * @return whether the lines belongs to this stop
     */
    public final ImmutableSet<String> getLines()
    {
        return new ImmutableHashSet<>(this.lines, Immutable.COPY);
    }

    /**
     * Returns the downstream conflicts of the bus stop. Search is only performed over links with BUS_STOP priority.
     * @return downstream conflicts of the given conflict
     */
    public final Set<Conflict> getConflicts()
    {
        if (this.conflicts == null)
        {
            this.conflicts = new LinkedHashSet<>();
            Lane lane = getLane();
            // conflict forces only plus or minus as direction
            Length position = getLongitudinalPosition();
            while (lane != null)
            {
                List<LaneBasedObject> objects = lane.getObjectAhead(position);
                while (!objects.isEmpty())
                {
                    for (LaneBasedObject object : objects)
                    {
                        if (object instanceof Conflict)
                        {
                            Conflict conflict = (Conflict) object;
                            if (conflict.getConflictRule() instanceof BusStopConflictRule)
                            {
                                this.conflicts.add(conflict);
                            }
                        }
                    }
                    objects = lane.getObjectAhead(objects.get(0).getLongitudinalPosition());
                }
                //
                Set<Lane> downstreamLanes = lane.nextLanes(this.busType);
                int numLanes = 0;
                for (Lane nextLane : downstreamLanes)
                {
                    if (nextLane.getLink().getPriority().isBusStop())
                    {
                        numLanes++;
                        lane = nextLane;
                        position = Length.ZERO;
                    }
                }
                if (numLanes != 1)
                {
                    lane = null;
                }
            }
        }
        return this.conflicts;
    }

    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.getId().hashCode();
        return result;
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        BusStop other = (BusStop) obj;
        if (!this.getId().equals(other.getId()))
        {
            return false;
        }
        return true;
    }

    @Override
    public final String toString()
    {
        String out = "BusStop [id=" + getId() + ", lines=";
        String delim = "";
        for (String line : this.lines)
        {
            out = out + delim + line;
            delim = "/";
        }
        return out + "]";
    }

}
