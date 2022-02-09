package org.opentrafficsim.road.network.lane;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Length;

/**
 * The CrossSectionSlice provides the width and offset at a relative length of a CrossSectionElement.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 26, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CrossSectionSlice implements Serializable
{
    /** */
    private static final long serialVersionUID = 20151126L;

    /** The relative position from the start, measured along the design line of the parent link. */
    private final Length relativeLength;

    /** The lateral offset from the design line of the parentLink at the relative length. */
    private final Length designLineOffset;

    /** The width, positioned <i>symmetrically around</i> the position at the relative length. */
    private final Length width;

    /**
     * Construct a new CrossSectionSlice.
     * @param relativeLength Length; the relative position from the start, measured along the design line of the parent link
     * @param designLineOffset Length; the lateral offset from the design line of the parentLink at the relative length
     * @param width Length; the width, positioned <i>symmetrically around</i> the position at the relative length
     */
    public CrossSectionSlice(final Length relativeLength, final Length designLineOffset, final Length width)
    {
        this.relativeLength = relativeLength;
        this.designLineOffset = designLineOffset;
        this.width = width;
    }

    /**
     * Retrieve the relative length.
     * @return Length; the relativeLength
     */
    public final Length getRelativeLength()
    {
        return this.relativeLength;
    }

    /**
     * Retrieve the design line offset.
     * @return Length; designLineOffset
     */
    public final Length getDesignLineOffset()
    {
        return this.designLineOffset;
    }

    /**
     * Retrieve the width.
     * @return Length; the width
     */
    public final Length getWidth()
    {
        return this.width;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "CrossSectionSlice [relativeLength=" + this.relativeLength + ", designLineOffset=" + this.designLineOffset
                + ", width=" + this.width + "]";
    }
}
