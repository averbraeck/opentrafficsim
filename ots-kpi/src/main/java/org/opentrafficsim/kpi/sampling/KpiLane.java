package org.opentrafficsim.kpi.sampling;

import java.io.Serializable;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.LaneDataInterface;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class KpiLane implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160330L;

    /** The lane. */
    private final LaneDataInterface lane;

    /**
     * @param lane LaneDataInterface; the lane
     */
    public KpiLane(final LaneDataInterface lane)
    {
        Throw.whenNull(lane, "Lane may not be null.");
        this.lane = lane;
    }

    /**
     * @return the lane
     */
    public LaneDataInterface getLaneData()
    {
        return this.lane;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "[" + this.lane + "]";
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.lane == null) ? 0 : this.lane.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj)
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
        KpiLane other = (KpiLane) obj;
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
