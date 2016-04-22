package org.opentrafficsim.road.network.lane;

import java.io.Serializable;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;

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
public class DirectedLanePosition implements Serializable
{
    /** */
    private static final long serialVersionUID = 20151111L;

    /** The lane for the position. */
    private final Lane lane;

    /** The position on the lane, relative to the cross section link (design line). */
    private final Length.Rel position;

    /** The direction the vehicle is driving to -- either in the direction of the design line, or against it. */
    private final GTUDirectionality gtuDirection;

    /**
     * @param lane the lane for the position
     * @param position the position on the lane, relative to the cross section link (design line)
     * @param gtuDirection the direction the vehicle is driving to -- either in the direction of the design line, or against it
     * @throws GTUException when preconditions fail
     */
    public DirectedLanePosition(final Lane lane, final Length.Rel position, final GTUDirectionality gtuDirection)
        throws GTUException
    {
        super();
        Throw.when(lane == null, GTUException.class, "lane is null");
        Throw.when(position == null, GTUException.class, "position is null");
        Throw.when(gtuDirection == null, GTUException.class, "gtuDirection is null");
        this.lane = lane;
        this.position = position;
        this.gtuDirection = gtuDirection;
    }

    /**
     * @return lane the lane for the position
     */
    public final Lane getLane()
    {
        return this.lane;
    }

    /**
     * @return position the position on the lane, relative to the cross section link (design line)
     */
    public final Length.Rel getPosition()
    {
        return this.position;
    }

    /**
     * @return gtuDirection the direction the vehicle is driving to -- either in the direction of the design line, or against it
     */
    public final GTUDirectionality getGtuDirection()
    {
        return this.gtuDirection;
    }

    /**
     * @return the location of the GTU on the lane, in the right direction.
     */
    public DirectedPoint getLocation()
    {
        double fraction = this.position.si / this.lane.getParentLink().getLength().si;
        DirectedPoint p = this.lane.getCenterLine().getLocationFractionExtended(fraction);
        if (this.gtuDirection.equals(GTUDirectionality.DIR_PLUS))
        {
            return p;
        }
        return new DirectedPoint(p.x, p.y, p.z, p.getRotX(), p.getRotY(), p.getRotZ() + Math.PI);
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
