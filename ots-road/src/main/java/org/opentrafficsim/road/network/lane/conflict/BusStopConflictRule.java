package org.opentrafficsim.road.network.lane.conflict;

import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.lane.CrossSectionLink.Priority;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Conflict rule for conflicts where busses enter the lane after a stop.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class BusStopConflictRule implements ConflictRule
{

    /** Simulator. */
    private final OtsSimulatorInterface simulator;

    /** GTU type for buses. */
    private final GtuType busType;

    /**
     * Constructor.
     * @param simulator OtsSimulatorInterface; simulator
     * @param busType GtuType; GTU type for buses.
     */
    public BusStopConflictRule(final OtsSimulatorInterface simulator, final GtuType busType)
    {
        this.simulator = simulator;
        this.busType = busType;
    }

    /** {@inheritDoc} */
    @Override
    public ConflictPriority determinePriority(final Conflict conflict)
    {

        // determine if we request from bus stop, or not
        boolean requestingFromBusStop;
        Conflict busConflict;
        // conflict builder enforces that only one of the two links has priority BUS_STOP
        if (conflict.getLane().getLink().getPriority().equals(Priority.BUS_STOP))
        {
            Throw.when(conflict.getOtherConflict().getLane().getLink().getPriority().equals(Priority.BUS_STOP),
                    IllegalArgumentException.class,
                    "BusStopConflictRule does not support a conflict between two links with priority BUS_STOP.");
            requestingFromBusStop = true;
            busConflict = conflict;
        }
        else
        {
            requestingFromBusStop = false;
            busConflict = conflict.getOtherConflict();
        }

        // find bus and determine if it has priority
        Lane lane = busConflict.getLane();
        Length pos = busConflict.getLongitudinalPosition();
        LaneBasedGtu gtu = null;
        try
        {
            while (gtu == null && lane != null)
            {
                gtu = lane.getGtuBehind(pos, RelativePosition.FRONT, this.simulator.getSimulatorAbsTime());
                if (gtu == null)
                {
                    Set<Lane> set = lane.prevLanes(this.busType);
                    if (set.size() == 1)
                    {
                        lane = set.iterator().next();
                        // only on bus stop
                        if (lane.getLink().getPriority().isBusStop())
                        {
                            pos = lane.getLength();
                        }
                        else
                        {
                            lane = null;
                        }
                    }
                    else
                    {
                        lane = null;
                    }
                }
            }
        }
        catch (GtuException exception)
        {
            throw new RuntimeException("Error while looking for GTU upstream of merge at bus stop.", exception);
        }
        boolean busHasPriority = gtu != null && gtu.getType().isOfType(this.busType) && gtu.getTurnIndicatorStatus().isLeft();

        // if bus has priority and bus is asking, PRIORITY
        // if bus has no priority and bus is not asking (i.e. car is asking), PRIORITY
        return busHasPriority == requestingFromBusStop ? ConflictPriority.PRIORITY : ConflictPriority.YIELD;

    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "BusStopConflictRule";
    }

}
