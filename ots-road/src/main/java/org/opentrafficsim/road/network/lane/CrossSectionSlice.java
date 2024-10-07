package org.opentrafficsim.road.network.lane;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Length;

/**
 * The CrossSectionSlice provides the width and offset at a relative length of a CrossSectionElement.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class CrossSectionSlice implements Serializable
{
    /** */
    private static final long serialVersionUID = 20151126L;

    /** The relative position from the start, measured along the design line of the parent link. */
    private final Length relativeLength;

    /** The lateral offset from the design line of the parentLink at the relative length. */
    private final Length offset;

    /** The width, positioned <i>symmetrically around</i> the position at the relative length. */
    private final Length width;

    /**
     * Construct a new CrossSectionSlice.
     * @param relativeLength the relative position from the start, measured along the design line of the parent link
     * @param offset the lateral offset from the design line of the parentLink at the relative length
     * @param width the width, positioned <i>symmetrically around</i> the position at the relative length
     */
    public CrossSectionSlice(final Length relativeLength, final Length offset, final Length width)
    {
        this.relativeLength = relativeLength;
        this.offset = offset;
        this.width = width;
    }

    /**
     * Retrieve the relative length.
     * @return the relativeLength
     */
    public final Length getRelativeLength()
    {
        return this.relativeLength;
    }

    /**
     * Retrieve the design line offset.
     * @return offset
     */
    public final Length getOffset()
    {
        return this.offset;
    }

    /**
     * Retrieve the width.
     * @return the width
     */
    public final Length getWidth()
    {
        return this.width;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "CrossSectionSlice [relativeLength=" + this.relativeLength + ", offset=" + this.offset + ", width=" + this.width
                + "]";
    }
}
