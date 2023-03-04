package org.opentrafficsim.core.animation;

import java.awt.Color;

/**
 * DrawingInfoStripe stores the drawing information about a stripe on a road. This can be interpreted by a visualization or
 * animation class.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @param <D> The drawable type for which this is the DrawingInfo
 */
public class DrawingInfoStripe<D extends Drawable> extends DrawingInfoLine<D>
{
    /** the stripe type. */
    private final StripeType stripeType;

    /**
     * @param stripeType StripeType; the stripe type
     */
    public DrawingInfoStripe(final StripeType stripeType)
    {
        this.stripeType = stripeType;
    }

    /**
     * @param lineColor Color; the line color
     * @param lineWidth float; the line width
     * @param stripeType StripeType; the stripe type
     */
    public DrawingInfoStripe(final Color lineColor, final float lineWidth, final StripeType stripeType)
    {
        super(lineColor, lineWidth);
        this.stripeType = stripeType;
    }

    /**
     * @param lineColorer Colorer&lt;D&gt;; the line colorer
     * @param lineWidth float; the line width
     * @param stripeType StripeType; the stripe type
     */
    public DrawingInfoStripe(final Colorer<D> lineColorer, final float lineWidth, final StripeType stripeType)
    {
        super(lineColorer, lineWidth);
        this.stripeType = stripeType;
    }

    /**
     * @return stripeType
     */
    public final StripeType getStripeType()
    {
        return this.stripeType;
    }

}
