package org.opentrafficsim.road.gtu.lane.perception.headway;

import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.immutablecollections.ImmutableSet;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.BusStop;

/**
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class HeadwayBusStop extends AbstractHeadwayLaneBasedObject
{

    /** */
    private static final long serialVersionUID = 20170127L;

    /** Relative lane. */
    private final RelativeLane relativeLane;

    /** Lines. */
    private final ImmutableSet<String> lines;

    /** Conflicts downstream of the bus stop. */
    private final Set<String> conflictIds;

    /**
     * @param busStop BusStop; bus stop
     * @param distance Length; distance
     * @param relativeLane RelativeLane; relative lane
     * @param conflictIds Set&lt;String&gt;; conflicts downstream of the bus stop
     * @param lane Lane; lane
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    public HeadwayBusStop(final BusStop busStop, final Length distance, final RelativeLane relativeLane,
            final Set<String> conflictIds, final Lane lane) throws GtuException
    {
        super(ObjectType.BUSSTOP, busStop.getId(), distance, lane);
        this.relativeLane = relativeLane;
        this.lines = busStop.getLines();
        this.conflictIds = conflictIds;
    }

    /**
     * @return relativeLane.
     */
    public final RelativeLane getRelativeLane()
    {
        return this.relativeLane;
    }

    /**
     * @return lines.
     */
    public final ImmutableSet<String> getLines()
    {
        return this.lines;
    }

    /**
     * @return conflictIds.
     */
    public final Set<String> getConflictIds()
    {
        return this.conflictIds;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "HeadwayBusStop [relativeLane=" + this.relativeLane + ", lines=" + this.lines + ", conflictIds="
                + this.conflictIds + "]";
    }

}
