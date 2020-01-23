package org.opentrafficsim.kpi.sampling;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.LaneDataInterface;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 30, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class KpiLaneDirection implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160330L;

    /** The lane. */
    private final LaneDataInterface lane;

    /** The GTU direction to drive on this lane. */
    private final KpiGtuDirectionality direction;

    /**
     * @param lane LaneDataInterface; the lane
     * @param direction KpiGtuDirectionality; the direction to drive on this lane
     */
    public KpiLaneDirection(final LaneDataInterface lane, final KpiGtuDirectionality direction)
    {
        Throw.whenNull(lane, "Lane may not be null.");
        Throw.whenNull(direction, "Direction may not be null.");
        this.lane = lane;
        this.direction = direction;
    }

    /**
     * @return the lane
     */
    public final LaneDataInterface getLaneData()
    {
        return this.lane;
    }

    /**
     * @return the direction to drive on this lane
     */
    public final KpiGtuDirectionality getKpiDirection()
    {
        return this.direction;
    }

    /**
     * Returns the position with increasing value in the direction of travel, i.e. the node to the back of the vehicle is at x =
     * 0 while the node in front of the vehicle is at x = {@code lane.getLength()}, irrespective of the design line direction.
     * @param position Length; the position on the lane irrespective of the direction
     * @return position with increasing value in the direction of travel
     */
    public final Length getPositionInDirection(Length position)
    {
        Throw.whenNull(position, "Position may not be null.");
        return this.direction.equals(KpiGtuDirectionality.DIR_PLUS) ? position : this.lane.getLength().minus(position);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
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
        KpiLaneDirection other = (KpiLaneDirection) obj;
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
