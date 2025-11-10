package org.opentrafficsim.road.gtu.lane.perception.object;

import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.immutablecollections.ImmutableSet;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.BusStop;

/**
 * Perceived bus stop.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class PerceivedBusStop extends PerceivedLaneBasedObjectBase
{

    /** Relative lane. */
    private final RelativeLane relativeLane;

    /** Lines. */
    private final ImmutableSet<String> lines;

    /** Conflicts downstream of the bus stop. */
    private final Set<String> conflictIds;

    /**
     * Constructor.
     * @param busStop bus stop
     * @param distance distance
     * @param relativeLane relative lane
     * @param conflictIds conflicts downstream of the bus stop
     * @param lane lane
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    public PerceivedBusStop(final BusStop busStop, final Length distance, final RelativeLane relativeLane,
            final Set<String> conflictIds, final Lane lane) throws GtuException
    {
        super(busStop.getId(), ObjectType.BUSSTOP, Length.ZERO, Kinematics.staticAhead(distance), lane);
        this.relativeLane = relativeLane;
        this.lines = busStop.getLines();
        this.conflictIds = conflictIds;
    }

    /**
     * Returns relative lane.
     * @return relativeLane.
     */
    public final RelativeLane getRelativeLane()
    {
        return this.relativeLane;
    }

    /**
     * Returns lines.
     * @return lines.
     */
    public final ImmutableSet<String> getLines()
    {
        return this.lines;
    }

    /**
     * Returns conflict ids.
     * @return conflictIds.
     */
    public final Set<String> getConflictIds()
    {
        return this.conflictIds;
    }

    @Override
    public final String toString()
    {
        return "PerceivedBusStop [relativeLane=" + this.relativeLane + ", lines=" + this.lines + ", conflictIds="
                + this.conflictIds + "]";
    }

}
