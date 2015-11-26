package org.opentrafficsim.road.network.lane;

import org.djunits.value.vdouble.scalar.Length;

/**
 * The CrossSectionSlice provides the width and offset at a relative length of a CrossSectionElement.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 26, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CrossSectionSlice
{
    /** The relative position from the start, measured along the design line of the parent link. */
    private final Length.Rel relativeLength;

    /** The lateral offset from the design line of the parentLink at the relative length. */
    private final Length.Rel designLineOffset;

    /** The width, positioned <i>symmetrically around</i> the position at the relative length. */
    private final Length.Rel width;

    /**
     * @param relativeLength the relative position from the start, measured along the design line of the parent link
     * @param designLineOffset the lateral offset from the design line of the parentLink at the relative length
     * @param width the width, positioned <i>symmetrically around</i> the position at the relative length
     */
    public CrossSectionSlice(final Length.Rel relativeLength, final Length.Rel designLineOffset, final Length.Rel width)
    {
        super();
        this.relativeLength = relativeLength;
        this.designLineOffset = designLineOffset;
        this.width = width;
    }

    /**
     * @return relativeLength
     */
    public final Length.Rel getRelativeLength()
    {
        return this.relativeLength;
    }

    /**
     * @return designLineOffset
     */
    public final Length.Rel getDesignLineOffset()
    {
        return this.designLineOffset;
    }

    /**
     * @return width
     */
    public final Length.Rel getWidth()
    {
        return this.width;
    }
}
