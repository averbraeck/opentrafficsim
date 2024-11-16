package org.opentrafficsim.animation.data;

import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.vector.LengthVector;
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

    @Override
    public PolyLine2d getCenterLine()
    {
        return getElement().getCenterLine();
    }

    @Override
    public PolyLine2d getLine()
    {
        return OtsLocatable.transformLine(getElement().getCenterLine(), getLocation());
    }

    @Override
    public List<LengthVector> getDashes()
    {
        return getElement().getDashes();
    }

    @Override
    public Length getDashOffset()
    {
        return getElement().getDashOffset();
    }

    @Override
    public OrientedPoint2d getLocation()
    {
        return getElement().getLocation();
    }

    @Override
    public Length getWidth(final Length location)
    {
        return getElement().getWidth(location);
    }

    @Override
    public String toString()
    {
        return "Stripe " + getElement().getLink().getId() + " " + getElement().getOffsetAtBegin();
    }

}
