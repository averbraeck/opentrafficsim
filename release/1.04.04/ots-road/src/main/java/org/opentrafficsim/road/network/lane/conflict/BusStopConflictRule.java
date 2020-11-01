package org.opentrafficsim.road.network.lane.conflict;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.ImmutableMap;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.CrossSectionLink.Priority;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * Conflict rule for conflicts where busses enter the lane after a stop.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 27 jan. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class BusStopConflictRule implements ConflictRule
{

    /** Simulator. */
    private final SimulatorInterface.TimeDoubleUnit simulator;

    /**
     * Constructor.
     * @param simulator SimulatorInterface.TimeDoubleUnit; simulator
     */
    public BusStopConflictRule(final SimulatorInterface.TimeDoubleUnit simulator)
    {
        this.simulator = simulator;
    }

    /** {@inheritDoc} */
    @Override
    public ConflictPriority determinePriority(final Conflict conflict)
    {

        // determine if we request from bus stop, or not
        boolean requestingFromBusStop;
        Conflict busConflict;
        // conflict builder enforces that only one of the two links has priority BUS_STOP
        if (conflict.getLane().getParentLink().getPriority().equals(Priority.BUS_STOP))
        {
            Throw.when(conflict.getOtherConflict().getLane().getParentLink().getPriority().equals(Priority.BUS_STOP),
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
        // conflict forces that LongitudinalDirection is DIR_PLUS or DIR_MINUS
        Lane lane = busConflict.getLane();
        GTUDirectionality dir =
                busConflict.getDirection().isForward() ? GTUDirectionality.DIR_PLUS : GTUDirectionality.DIR_MINUS;
        Length pos = busConflict.getLongitudinalPosition();
        LaneBasedGTU gtu = null;
        try
        {
            while (gtu == null && lane != null)
            {
                gtu = lane.getGtuBehind(pos, dir, RelativePosition.FRONT, this.simulator.getSimulatorTime());
                if (gtu == null)
                {
                    ImmutableMap<Lane, GTUDirectionality> map =
                            lane.upstreamLanes(dir, lane.getNetwork().getGtuType(GTUType.DEFAULTS.BUS));
                    if (map.size() == 1)
                    {
                        lane = map.keySet().iterator().next();
                        // only on bus stop
                        if (lane.getParentLink().getPriority().isBusStop())
                        {
                            dir = map.get(lane);
                            pos = dir.isPlus() ? lane.getLength() : Length.ZERO;
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
        catch (GTUException exception)
        {
            throw new RuntimeException("Error while looking for GTU upstream of merge at bus stop.", exception);
        }
        boolean busHasPriority =
                gtu != null && gtu.getGTUType().isOfType(GTUType.DEFAULTS.BUS) && gtu.getTurnIndicatorStatus().isLeft();

        // if bus has priority and bus is asking, PRIORITY
        // if bus has no priority and bus is not asking (i.e. car is asking), PRIORITY
        return busHasPriority == requestingFromBusStop ? ConflictPriority.PRIORITY : ConflictPriority.YIELD;

    }

    /** {@inheritDoc} */
    @Override
    public final ConflictRule clone(final SimulatorInterface.TimeDoubleUnit newSimulator)
    {
        return new BusStopConflictRule(newSimulator);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "BusStopConflictRule";
    }

}
