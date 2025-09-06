package org.opentrafficsim.animation.data;

import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.line.PolyLine2d;
import org.opentrafficsim.base.StripeElement;
import org.opentrafficsim.base.StripeElement.StripeLateralSync;
import org.opentrafficsim.base.geometry.DirectionalPolyLine;
import org.opentrafficsim.base.geometry.OtsShape;
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

    /** Center line. */
    private DirectionalPolyLine directionalCenterLine = null;

    /**
     * Constructor.
     * @param stripe stripe
     */
    public AnimationStripeData(final Stripe stripe)
    {
        super(stripe);
    }

    @Override
    public DirectionalPolyLine getCenterLine()
    {
        if (this.directionalCenterLine == null)
        {
            this.directionalCenterLine = new DirectionalPolyLine(getObject().getCenterLine(),
                    getObject().getLink().getStartNode().getHeading(), getObject().getLink().getEndNode().getHeading());
        }
        return this.directionalCenterLine;
    }

    @Override
    public PolyLine2d getReferenceLine()
    {
        return getObject().getLateralSync().equals(StripeLateralSync.NONE) ? getObject().getCenterLine()
                : getObject().getLinkReferenceLine();
    }

    @Override
    public PolyLine2d getLine()
    {
        return OtsShape.transformLine(getObject().getCenterLine(), getLocation());
    }

    @Override
    public List<StripeElement> getElements()
    {
        return getObject().getElements();
    }

    @Override
    public Length getDashOffset()
    {
        return getObject().getDashOffset();
    }

    @Override
    public Length getWidth(final Length location)
    {
        return getObject().getWidth(location);
    }

    @Override
    public String toString()
    {
        return "Stripe " + getObject().getLink().getId() + " " + getObject().getOffsetAtBegin();
    }

}
