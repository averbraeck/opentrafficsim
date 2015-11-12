package org.opentrafficsim.road.network.lane;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Length.Rel;
import org.opentrafficsim.core.gtu.GTUDirectionality;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 11, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class DirectedLanePosition
{
    private final Lane lane;

    private final Length.Rel position;

    private final GTUDirectionality gtuDirection;

    /**
     * @param lane
     * @param position
     * @param gtuDirection
     */
    public DirectedLanePosition(Lane lane, Rel position, GTUDirectionality gtuDirection)
    {
        super();
        this.lane = lane;
        this.position = position;
        this.gtuDirection = gtuDirection;
    }

    /**
     * @return lane
     */
    public final Lane getLane()
    {
        return this.lane;
    }

    /**
     * @return position
     */
    public final Length.Rel getPosition()
    {
        return this.position;
    }

    /**
     * @return gtuDirection
     */
    public final GTUDirectionality getGtuDirection()
    {
        return this.gtuDirection;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.gtuDirection == null) ? 0 : this.gtuDirection.hashCode());
        result = prime * result + ((this.lane == null) ? 0 : this.lane.hashCode());
        result = prime * result + ((this.position == null) ? 0 : this.position.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DirectedLanePosition other = (DirectedLanePosition) obj;
        if (this.gtuDirection != other.gtuDirection)
            return false;
        if (this.lane == null)
        {
            if (other.lane != null)
                return false;
        }
        else if (!this.lane.equals(other.lane))
            return false;
        if (this.position == null)
        {
            if (other.position != null)
                return false;
        }
        else if (!this.position.equals(other.position))
            return false;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "DirectedLanePosition [lane=" + this.lane + ", position=" + this.position + ", gtuDirection="
            + this.gtuDirection + "]";
    }

}
