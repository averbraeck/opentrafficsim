package org.opentrafficsim.animation.data;

import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.opentrafficsim.base.StripeElement;
import org.opentrafficsim.base.StripeElement.StripeLateralSync;
import org.opentrafficsim.base.geometry.OtsLocatable;
import org.opentrafficsim.draw.road.StripeAnimation.StripeData;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
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

    /** Link reference line. */
    private PolyLine2d linkReferenceLine = null;

    /**
     * Constructor.
     * @param stripe stripe
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
    public PolyLine2d getReferenceLine()
    {
        return getElement().getLateralSync().equals(StripeLateralSync.NONE) ? getElement().getCenterLine()
                : getLinkReferenceLine();
    }

    /**
     * Return link reference line.
     * @return link reference line
     */
    private PolyLine2d getLinkReferenceLine()
    {
        if (this.linkReferenceLine == null)
        {
            PolyLine2d linkLine = getElement().getLink().getDesignLine();
            double offsetMin0 = Double.POSITIVE_INFINITY;
            double offsetMax0 = Double.NEGATIVE_INFINITY;
            double offsetMin1 = Double.POSITIVE_INFINITY;
            double offsetMax1 = Double.NEGATIVE_INFINITY;
            for (CrossSectionElement element : getElement().getLink().getCrossSectionElementList())
            {
                if (element instanceof Stripe)
                {
                    offsetMin0 = Math.min(offsetMin0, element.getOffsetAtBegin().si);
                    offsetMax0 = Math.max(offsetMax0, element.getOffsetAtBegin().si);
                    offsetMin1 = Math.min(offsetMin1, element.getOffsetAtEnd().si);
                    offsetMax1 = Math.max(offsetMax1, element.getOffsetAtEnd().si);
                }
            }
            PolyLine2d start = linkLine.offsetLine(.5 * (offsetMin0 + offsetMax0));
            PolyLine2d end = linkLine.offsetLine(.5 * (offsetMin1 + offsetMax1));
            this.linkReferenceLine = start.transitionLine(end, (f) -> f);
        }
        return this.linkReferenceLine;
    }

    @Override
    public PolyLine2d getLine()
    {
        return OtsLocatable.transformLine(getElement().getCenterLine(), getLocation());
    }

    @Override
    public List<StripeElement> getElements()
    {
        return getElement().getElements();
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
