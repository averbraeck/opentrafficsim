package org.opentrafficsim.animation.data;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.line.PolyLine2d;
import org.opentrafficsim.draw.road.StripeAnimation.StripeData;
import org.opentrafficsim.road.network.lane.Stripe;

/**
 * Animation data of a Stripe.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class AnimationStripeData extends AnimationCrossSectionElementData<Stripe> implements StripeData
{

    /**
     * Constructor.
     * @param stripe Stripe; stripe.
     */
    public AnimationStripeData(final Stripe stripe)
    {
        super(stripe);
    }

    /** {@inheritDoc} */
    @Override
    public PolyLine2d getCenterLine()
    {
        return getElement().getCenterLine().getLine2d();
    }

    /** {@inheritDoc} */
    @Override
    public Type getType()
    {
        return Type.valueOf(getElement().getType().name());
    }

    /** {@inheritDoc} */
    @Override
    public Length getWidth()
    {
        return getElement().getWidth(0.5);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Stripe " + getElement().getLink().getId() + " " + getElement().getDesignLineOffsetAtBegin();
    }

}
