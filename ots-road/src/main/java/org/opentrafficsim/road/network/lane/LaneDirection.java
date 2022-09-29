package org.opentrafficsim.road.network.lane;

import java.io.Serializable;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;

/**
 * Combines a Lane with its GTUDirectionality.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
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
            return new DirectedPoint(p.x, p.y, p.z, p.dirX, p.dirY, p.dirZ + Math.PI);
        }
        return p;
    }

    /**
     * Returns the adjacent lane and direction.
     * @param gtu LaneBasedGtu; gtu
     * @param laneChangeDirection LateralDirectionality; lane change direction
     * @return LaneDirection; adjacent lane and direction, {@code null} if none
     */
    public final LaneDirection getAdjacentLaneDirection(final LateralDirectionality laneChangeDirection, final LaneBasedGtu gtu)
    {
        Set<Lane> adjLanes = this.lane.accessibleAdjacentLanesLegal(laneChangeDirection, gtu.getGtuType(), this.direction);
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
