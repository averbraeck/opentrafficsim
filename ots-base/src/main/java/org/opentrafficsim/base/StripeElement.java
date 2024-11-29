package org.opentrafficsim.base;

import java.awt.Color;
import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.vector.LengthVector;
import org.djutils.exceptions.Throw;

/**
 * Data container for an element of a stripe road marking. Elements can be continuous lines, dashed lines, or gaps between
 * lines. For example, this can entail a complicated road marking consisting of a continuous line, a narrow gap, a colored wide
 * area (included as a wide line), a narrow gap, and a dashed line.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param width width
 * @param color color
 * @param dashes dashes
 */
// TODO unit test
public record StripeElement(Length width, Color color, LengthVector dashes) implements Serializable
{

    /* This class is in ots-base, as it is used by both ots-road and ots-draw. */

    /**
     * Creates a continuous stripe element.
     * @param width width
     * @param color color
     * @return continuous stripe element
     */
    public static StripeElement continuous(final Length width, final Color color)
    {
        Throw.whenNull(width, "width");
        Throw.whenNull(width, "color");
        return new StripeElement(width, color, null);
    }

    /**
     * Creates a dashed stripe element.
     * @param width width
     * @param color color
     * @param dashes dashes (gap, dash, gap, dash, ...)
     * @return dashed stripe element
     */
    public static StripeElement dashed(final Length width, final Color color, final LengthVector dashes)
    {
        Throw.whenNull(width, "width");
        Throw.whenNull(width, "color");
        Throw.whenNull(width, "dashes");
        return new StripeElement(width, color, dashes);
    }

    /**
     * Creates a gap stripe element.
     * @param width width
     * @return gap stripe element
     */
    public static StripeElement gap(final Length width)
    {
        Throw.whenNull(width, "width");
        return new StripeElement(width, null, null);
    }

    /**
     * Returns whether this is a continuous stripe element (i.e. a line without dashes).
     * @return whether this is a continuous stripe element
     */
    public boolean isContinuous()
    {
        return this.color != null && this.dashes == null;
    }

    /**
     * Returns whether this is a gap stripe element.
     * @return whether this is a gap stripe element
     */
    public boolean isGap()
    {
        return this.color == null && this.dashes == null;
    }

    /**
     * Returns whether this is a dashed stripe element.
     * @return whether this is a dashed stripe element
     */
    public boolean isDashed()
    {
        return this.color != null && this.dashes != null;
    }

    /**
     * Method to determine how stripes synchronize within a link.
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public enum StripeLateralSync
    {
        /** Dashes applied to stripe line. */
        NONE(false),

        /** Dashes applied to link middle line to determine fractions, which are then used on the stripe line. */
        LINK(true),

        /** Adheres to end-point phase of LINK, but may insert or remove gap/dash pairs to stay close to original length. */
        SNAP(true);
        
        /** Whether the dashes are applied on the median line of the link. */
        private final boolean linkBased;
        
        /**
         * Constructor.
         * @param linkBased whether the dashes are applied on the median line of the link
         */
        StripeLateralSync(final boolean linkBased)
        {
            this.linkBased = linkBased;
        }
        
        /**
         * Returns whether the dashes are applied on the median line of the link.
         * @return whether the dashes are applied on the median line of the link
         */
        public boolean isLinkBased()
        {
            return this.linkBased;
        }
    }

}
