package org.opentrafficsim.road.network.lane.object;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.animation.BusStopAnimation;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.immutablecollections.Immutable;
import nl.tudelft.simulation.immutablecollections.ImmutableHashSet;
import nl.tudelft.simulation.immutablecollections.ImmutableSet;

/**
 * A bus stop is a location on a lane. The stop has a name, and a set of lines. At a single stop in reality, there may be
 * different locations where busses stop for different lines. A {@code BusStop} pertains to only one such location. The bus stop
 * in reality is represented by a shared name over a few {@code BusStop}'s, with different lines. As lines may also be set
 * dynamically, the name and lines are insufficient to identify a specific {@code BusStop}. Hence there is a fixed unique id per
 * {@code BusStop}.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    private final Set<String> lines = new HashSet<>();

    /** Stop name. */
    private final String name;

    /**
     * @param id id
     * @param lane lane
     * @param longitudinalPosition position
     * @param name name of stop
     * @param simulator the simulator to schedule on
     * @throws NetworkException when the position on the lane is out of bounds
     */
    public BusStop(final String id, final Lane lane, final Length longitudinalPosition, final String name,
            final OTSSimulatorInterface simulator) throws NetworkException
    {
        super(id, lane, longitudinalPosition, LaneBasedObject.makeGeometry(lane, longitudinalPosition));
        this.name = name;

        try
        {
            new BusStopAnimation(this, simulator);
        }
        catch (RemoteException | NamingException exception)
        {
            throw new NetworkException(exception);
        }
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
    public final AbstractLaneBasedObject clone(final CrossSectionElement newCSE, final OTSSimulatorInterface newSimulator,
            final boolean animation) throws NetworkException
    {
        BusStop busStop = new BusStop(getId(), getLane(), getLongitudinalPosition(), this.name, newSimulator);
        busStop.setLines(this.lines);
        return busStop;
    }

}
