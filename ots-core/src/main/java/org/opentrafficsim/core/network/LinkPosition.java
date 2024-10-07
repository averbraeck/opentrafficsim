package org.opentrafficsim.core.network;

import java.io.Serializable;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;

/**
 * Class that stores a link combined with a position on that link.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param link the link
 * @param fractionalLongitudinalPosition fractional position
 */
public record LinkPosition(Link link, double fractionalLongitudinalPosition) implements Serializable
{
    /** */
    private static final long serialVersionUID = 20181022L;

    /**
     * Create a link combined with a position on that link.
     * @param link the link
     * @param position position
     */
    public LinkPosition(final Link link, final Length position)
    {
        this(link, position.si / link.getLength().si);
    }

    /**
     * Return the length of the link.
     * @return the length of the link
     */
    public Length getLinkLength()
    {
        return this.link.getLength();
    }

    /**
     * Return the position on the link as a Length.
     * @return position as a length on this link.
     */
    public final Length getLongitudinalPosition()
    {
        return new Length(this.link.getLength().getSI() * fractionalLongitudinalPosition(), LengthUnit.METER);
    }
}
