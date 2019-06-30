package org.opentrafficsim.road.network.lane.object;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableHashSet;
import org.djutils.immutablecollections.ImmutableMap;
import org.djutils.immutablecollections.ImmutableSet;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.conflict.BusStopConflictRule;
import org.opentrafficsim.road.network.lane.conflict.Conflict;

import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * A bus stop is a location on a lane. The stop has a name, and a set of lines. At a single stop in reality, there may be
 * different locations where busses stop for different lines. A {@code BusStop} pertains to only one such location. The bus stop
 * in reality is represented by a shared name over a few {@code BusStop}'s, with different lines. As lines may also be set
 * dynamically, the name and lines are insufficient to identify a specific {@code BusStop}. Hence there is a fixed unique id per
 * {@code BusStop}.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 24 jan. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class BusStop extends AbstractLaneBasedObject
{

    /** */
    private static final long serialVersionUID = 20170124L;

    /** Line numbers. */
    private final Set<String> lines = new LinkedHashSet<>();

    /** Stop name. */
    private final String name;

    /** Stored conflicts downstream. */
    private Set<Conflict> conflicts = null;

    /**
     * @param id String; id
     * @param lane Lane; lane
     * @param longitudinalPosition Length; position
     * @param name String; name of stop
     * @param simulator SimulatorInterface.TimeDoubleUnit; the simulator to schedule on
     * @throws NetworkException when the position on the lane is out of bounds
     */
    public BusStop(final String id, final Lane lane, final Length longitudinalPosition, final String name,
            final SimulatorInterface.TimeDoubleUnit simulator) throws NetworkException
    {
        super(id, lane, LongitudinalDirectionality.DIR_PLUS, longitudinalPosition,
                LaneBasedObject.makeGeometry(lane, longitudinalPosition), Length.ZERO);
        this.name = name;
    }

    /**
     * Sets the lines.
     * @param lines Set&lt;String&gt;; lines that stop at this location
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
            GTUDirectionality dir = getDirection().isForward() ? GTUDirectionality.DIR_PLUS : GTUDirectionality.DIR_MINUS;
            Length position = getLongitudinalPosition();
            while (lane != null)
            {
                List<LaneBasedObject> objects = lane.getObjectAhead(position, dir);
                while (objects != null)
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
                    objects = lane.getObjectAhead(objects.get(0).getLongitudinalPosition(), dir);
                }
                ImmutableMap<Lane, GTUDirectionality> downstreamLanes =
                        lane.downstreamLanes(dir, lane.getNetwork().getGtuType(GTUType.DEFAULTS.BUS));
                int numLanes = 0;
                for (Lane nextLane : downstreamLanes.keySet())
                {
                    if (nextLane.getParentLink().getPriority().isBusStop())
                    {
                        numLanes++;
                        lane = nextLane;
                        dir = downstreamLanes.get(lane);
                        position = dir.isPlus() ? Length.ZERO : lane.getLength();
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

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.getId().hashCode();
        return result;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public final AbstractLaneBasedObject clone(final CrossSectionElement newCSE,
            final SimulatorInterface.TimeDoubleUnit newSimulator) throws NetworkException
    {
        BusStop busStop = new BusStop(getId(), (Lane) newCSE, getLongitudinalPosition(), this.name, newSimulator);
        busStop.setLines(this.lines);
        return busStop;
    }

}
