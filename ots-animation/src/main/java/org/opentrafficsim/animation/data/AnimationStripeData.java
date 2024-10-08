package org.opentrafficsim.animation.data;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.opentrafficsim.base.geometry.OtsLocatable;
import org.opentrafficsim.draw.road.StripeAnimation.StripeData;
import org.opentrafficsim.road.network.lane.Stripe;

/**
 * Animation data of a Stripe.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AnimationStripeData extends AnimationCrossSectionElementData<Stripe> implements StripeData
{

    /**
     * Constructor.
     * @param stripe stripe.
     */
    public AnimationStripeData(final Stripe stripe)
    {
        super(stripe);
    }

    /** {@inheritDoc} */
    @Override
    public PolyLine2d getCenterLine()
    {
        return getElement().getCenterLine();
    }

    /** {@inheritDoc} */
    @Override
    public PolyLine2d getLine()
    {
        return OtsLocatable.transformLine(getElement().getCenterLine(), getLocation());
    }

    /** {@inheritDoc} */
    @Override
    public Type getType()
    {
        return Type.valueOf(getElement().getType().name());
    }

    /** {@inheritDoc} */
    @Override
    public OrientedPoint2d getLocation()
    {
        return getElement().getLocation();
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
        return "Stripe " + getElement().getLink().getId() + " " + getElement().getOffsetAtBegin();
    }

}
