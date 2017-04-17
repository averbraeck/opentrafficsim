package org.opentrafficsim.road.network.lane;

import java.io.Serializable;

import org.opentrafficsim.core.gtu.GTUDirectionality;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * @param lane the lane
     * @param direction the direction to drive on this lane
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
