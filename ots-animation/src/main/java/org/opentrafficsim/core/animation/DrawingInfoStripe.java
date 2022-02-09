package org.opentrafficsim.core.animation;

import java.awt.Color;

/**
 * DrawingInfoStripe stores the drawing information about a stripe on a road. This can be interpreted by a visualization or
 * animation class. <br>
 * <br>
 * Copyright (c) 2003-2022 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
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
