package org.opentrafficsim.core.network;

import java.io.Serializable;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;

/**
 * "1D" implementation. Mapping on the design line (often the center line) of a road.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version Oct 22, 2014 <br>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class LinkLocation implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** The link of the location of a point relative to the GTU. */
    private final Link link;

    /** The fractional position (between 0.0 and 1.0) of the reference point on the lane. */
    private final double fractionalLongitudinalPosition;

    /**
     * @param link Link; The link of the location of a point relative to the GTU.
     * @param fractionalLongitudinalPosition double; The fractional position (between 0.0 and 1.0) of the reference point on the
     *            link.
     */
    public LinkLocation(final Link link, final double fractionalLongitudinalPosition)
    {
        this.link = link;
        this.fractionalLongitudinalPosition = fractionalLongitudinalPosition;
    }

    /**
     * @param link Link; The link of the location of a point relative to the GTU.
     * @param position Length; The position as a length of the reference point on the link.
     */
    public LinkLocation(final Link link, final Length position)
    {
        this.link = link;
        this.fractionalLongitudinalPosition = position.divide(this.link.getLength()).doubleValue();
    }

    /**
     * @return lane.
     */
    public final Link getLink()
    {
        return this.link;
    }

    /**
     * @return fractionalLongitudinalPosition.
     */
    public final double getFractionalLongitudinalPosition()
    {
        return this.fractionalLongitudinalPosition;
    }

    /**
     * @return position as a length as a traveled length on this link.
     */
    public final Length getLongitudinalPosition()
    {
        return new Length(this.link.getLength().getSI() * getFractionalLongitudinalPosition(), LengthUnit.METER);
    }

    /**
     * Returns the distance to another LinkLocation. If the other location is in front of us, the distance is positive. If it is
     * behind us, it is negative.
     * @param loc LinkLocation; the link location to find the distance to.
     * @return the distance to another LinkLocation.
     */
    public final Length distance(final LinkLocation loc)
    {
        if (this.link.equals(loc.getLink()))
        {
            return loc.getLongitudinalPosition().minus(this.getLongitudinalPosition());
        }

        // TODO not on the same link. Find shortest path...
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return String.format("%s %.3f%s", getLink(), getLongitudinalPosition().getInUnit(),
                getLongitudinalPosition().getDisplayUnit());
    }
}
