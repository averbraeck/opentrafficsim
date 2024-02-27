package org.opentrafficsim.core.network;

import java.io.Serializable;
import java.util.Objects;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;

/**
 * Class that stores a link combined with a position on that link.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LinkPosition implements Serializable
{
    /** */
    private static final long serialVersionUID = 20181022L;

    /** the link. */
    private final Link link;
    
    /** The fractional position (between 0.0 and 1.0) of the reference point on the link. */
    private final double fractionalLongitudinalPosition;

    /**
     * Create a link combined with a position on that link.
     * @param link Link; the link
     * @param fractionalLongitudinalPosition double; fractional position
     */
    public LinkPosition(final Link link, final double fractionalLongitudinalPosition)
    {
        this.link = link;
        this.fractionalLongitudinalPosition = fractionalLongitudinalPosition;
    }

    /**
     * Create a link combined with a position on that link.
     * @param link Link; the link
     * @param position Length; position
     */
    public LinkPosition(final Link link, final Length position)
    {
        this(link, position.si / link.getLength().si);
    }

    /**
     * Return the link.
     * @return Link; the link
     */
    public Link getLink()
    {
        return this.link;
    }

    /**
     * Return the length of the link.
     * @return Length; the length of the link
     */
    public Length getLinkLength()
    {
        return this.link.getLength();
    }
    
    /**
     * Return the fractional position on the link.
     * @return the fractional longitudinal position on the link.
     */
    public final double getFractionalLongitudinalPosition()
    {
        return this.fractionalLongitudinalPosition;
    }

    /**
     * Return the position on the link as a Length.
     * @return position as a length on this link.
     */
    public final Length getLongitudinalPosition()
    {
        return new Length(this.link.getLength().getSI() * getFractionalLongitudinalPosition(), LengthUnit.METER);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        return Objects.hash(this.fractionalLongitudinalPosition, this.link);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:needbraces")
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LinkPosition other = (LinkPosition) obj;
        return Double.doubleToLongBits(this.fractionalLongitudinalPosition) == Double
                .doubleToLongBits(other.fractionalLongitudinalPosition) && Objects.equals(this.link, other.link);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "LinkPosition [link=" + this.link + ", fractionalLongitudinalPosition="
                + this.fractionalLongitudinalPosition + "]";
    }

}
