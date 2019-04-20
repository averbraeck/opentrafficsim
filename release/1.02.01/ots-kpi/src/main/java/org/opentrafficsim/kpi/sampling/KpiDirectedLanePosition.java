package org.opentrafficsim.kpi.sampling;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.LaneDataInterface;

/**
 * Store one position, direction and lane of a GTU.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 11, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class KpiDirectedLanePosition implements Serializable
{
    /** */
    private static final long serialVersionUID = 20151111L;

    /** The lane for the position. */
    private final LaneDataInterface lane;

    /** The position on the lane, relative to the cross section link (design line). */
    private final Length position;

    /** The direction the vehicle is driving to -- either in the direction of the design line, or against it. */
    private final KpiGtuDirectionality gtuDirection;

    /**
     * Construct a new DirectedLanePosition.
     * @param lane LaneDataInterface; the lane for the position
     * @param position Length; the position on the lane, relative to the cross section link (design line)
     * @param gtuDirection KpiGtuDirectionality; the direction the vehicle is driving to -- either in the direction of the
     *            design line, or against it
     */
    public KpiDirectedLanePosition(final LaneDataInterface lane, final Length position, final KpiGtuDirectionality gtuDirection)
    {
        Throw.whenNull(lane, "lane is null");
        Throw.whenNull(position, "position is null");
        Throw.whenNull(gtuDirection, "gtuDirection is null");
        this.lane = lane;
        this.position = position;
        this.gtuDirection = gtuDirection;
    }

    /**
     * Retrieve the lane.
     * @return LaneDataInterface; the lane for the position
     */
    public final LaneDataInterface getLaneData()
    {
        return this.lane;
    }

    /**
     * Retrieve the position on the lane.
     * @return Length; the position on the lane, relative to the cross section link (design line)
     */
    public final Length getPosition()
    {
        return this.position;
    }

    /**
     * Retrieve the gtuDirection.
     * @return KpiGtuDirectionality; gtuDirection the direction the vehicle is driving to -- either in the direction of the
     *         design line, or against it
     */
    public final KpiGtuDirectionality getKpiGtuDirection()
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
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        KpiDirectedLanePosition other = (KpiDirectedLanePosition) obj;
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
        return "DirectedLanePosition [lane=" + this.lane + ", position=" + this.position + ", gtuDirection=" + this.gtuDirection
                + "]";
    }

}
