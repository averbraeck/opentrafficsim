package org.opentrafficsim.road.network.lane;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Try;
import org.djutils.immutablecollections.ImmutableMap;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.LinkDirection;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Combines a Lane with its GTUDirectionality.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 30, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneDirection implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160330L;

    /** The lane. */
    private final Lane lane;

    /** The GTU direction to drive on this lane. */
    private final GTUDirectionality direction;

    /**
     * @param lane Lane; the lane
     * @param direction GTUDirectionality; the direction to drive on this lane
     */
    public LaneDirection(final Lane lane, final GTUDirectionality direction)
    {
        super();
        this.lane = lane;
        this.direction = direction;
    }

    /**
     * @return the lane
     */
    public final Lane getLane()
    {
        return this.lane;
    }

    /**
     * @return the direction to drive on this lane
     */
    public final GTUDirectionality getDirection()
    {
        return this.direction;
    }

    /**
     * Returns the covered distance driven to the given fractional position.
     * @param fraction double; fractional position
     * @return Length; covered distance driven to the given fractional position
     */
    public final Length coveredDistance(final double fraction)
    {
        if (this.direction.isPlus())
        {
            return getLane().getLength().times(fraction);
        }
        return getLane().getLength().times(1.0 - fraction);
    }

    /**
     * Returns the remaining distance to be driven from the given fractional position.
     * @param fraction double; fractional position
     * @return Length; remaining distance to be driven from the given fractional position
     */
    public final Length remainingDistance(final double fraction)
    {
        if (this.direction.isPlus())
        {
            return getLane().getLength().times(1.0 - fraction);
        }
        return getLane().getLength().times(fraction);
    }

    /**
     * Returns the fraction along the design line for having covered the given distance.
     * @param distance Length; covered distance
     * @return double; fraction along the design line for having covered the given distance
     */
    public final double fractionAtCoveredDistance(final Length distance)
    {
        double f = this.lane.fraction(distance);
        if (this.getDirection().isMinus())
        {
            f = 1.0 - f;
        }
        return f;
    }

    /**
     * Returns the next lane and direction.
     * @param gtu LaneBasedGTU; gtu
     * @return LaneDirection; next lane and direction, {@code null} if none
     */
    public final LaneDirection getNextLaneDirection(final LaneBasedGTU gtu)
    {
        ImmutableMap<Lane, GTUDirectionality> next = this.lane.downstreamLanes(this.direction, gtu.getGTUType());
        if (next.isEmpty())
        {
            return null;
        }
        // ask strategical planner
        Set<LaneDirection> set = getNextForRoute(gtu);
        if (set.size() == 1)
        {
            return set.iterator().next();
        }
        // check of the GTU is registered on any
        for (LaneDirection l : set)
        {
            if (l.getLane().getGtuList().contains(gtu))
            {
                return l;
            }
        }
        // ask tactical planner
        return Try.assign(() -> gtu.getTacticalPlanner().chooseLaneAtSplit(this, set), "Missing parameter.");
    }

    /**
     * Returns a set of {@code LaneDirection}'s that can be followed considering the route.
     * @param gtu LaneBasedGTU; GTU
     * @return set of {@code LaneDirection}'s that can be followed considering the route
     */
    public Set<LaneDirection> getNextForRoute(final LaneBasedGTU gtu)
    {
        ImmutableMap<Lane, GTUDirectionality> next = this.lane.downstreamLanes(this.direction, gtu.getGTUType());
        if (next.isEmpty())
        {
            return null;
        }
        LinkDirection ld;
        try
        {
            ld = gtu.getStrategicalPlanner().nextLinkDirection(this.lane.getParentLink(), this.direction, gtu.getGTUType());
        }
        catch (NetworkException exception)
        {
            throw new RuntimeException("Strategical planner experiences exception on network.", exception);
        }
        Set<LaneDirection> out = new LinkedHashSet<>();
        for (Lane l : next.keySet())
        {
            GTUDirectionality dir = next.get(l);
            if (l.getParentLink().equals(ld.getLink()) && dir.equals(ld.getDirection()))
            {
                out.add(new LaneDirection(l, dir));
            }
        }
        return out;
    }

    /**
     * Returns the length of the lane.
     * @return Length; length of the lane
     */
    public Length getLength()
    {
        return this.lane.getLength();
    }

    /**
     * Returns a directed point at the given fraction, in the direction of travel (not center line).
     * @param fraction double; fractional position
     * @return directed point at the given fraction, in the direction of travel
     * @throws OTSGeometryException in case the fractional position is not correct
     */
    public DirectedPoint getLocationFraction(final double fraction) throws OTSGeometryException
    {
        DirectedPoint p = this.lane.getCenterLine().getLocationFraction(fraction);
        if (this.direction.isMinus())
        {
            p.setRotZ(p.getRotZ() + Math.PI);
        }
        return p;
    }

    /**
     * Returns the adjacent lane and direction.
     * @param gtu LaneBasedGTU; gtu
     * @param laneChangeDirection LateralDirectionality; lane change direction
     * @return LaneDirection; adjacent lane and direction, {@code null} if none
     */
    public final LaneDirection getAdjacentLaneDirection(final LateralDirectionality laneChangeDirection, final LaneBasedGTU gtu)
    {
        Set<Lane> adjLanes = this.lane.accessibleAdjacentLanesLegal(laneChangeDirection, gtu.getGTUType(), this.direction);
        if (!adjLanes.isEmpty())
        {
            return new LaneDirection(adjLanes.iterator().next(), this.direction);
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "[" + this.lane + (this.direction.isPlus() ? " +]" : " -]");
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.direction == null) ? 0 : this.direction.hashCode());
        result = prime * result + ((this.lane == null) ? 0 : this.lane.hashCode());
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
        LaneDirection other = (LaneDirection) obj;
        if (this.direction != other.direction)
        {
            return false;
        }
        if (this.lane == null)
        {
            if (other.lane != null)
            {
                return false;
            }
        }
        else if (!this.lane.equals(other.lane))
        {
            return false;
        }
        return true;
    }

}
