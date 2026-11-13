package org.opentrafficsim.animation.data;

import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.line.PolyLine2d;
import org.opentrafficsim.animation.road.StripeAnimation.StripeData;
import org.opentrafficsim.base.StripeElement;
import org.opentrafficsim.base.StripeElement.StripeLateralSync;
import org.opentrafficsim.base.geometry.DirectionalPolyLine;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.network.CrossSectionLink;
import org.opentrafficsim.road.network.Stripe;

/**
 * Animation data of a Stripe.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
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
    public boolean isPartialDashAtStartSkipped()
    {
        return getNodeLinkCount(getObject().getLink().getStartNode()) >= 3;
    }

    @Override
    public boolean isPartialDashAtEndSkipped()
    {
        return getNodeLinkCount(getObject().getLink().getEndNode()) >= 3;
    }

    /**
     * Returns the number of road links that connect to the given node, excluding connectors.
     * @param node node to count connected road links of
     * @return number of road links that connect to the node
     */
    private static long getNodeLinkCount(final Node node)
    {
        return node.getLinks().stream().filter(link -> link instanceof CrossSectionLink).count();
    }

    @Override
    public String toString()
    {
        return "Stripe " + getObject().getLink().getId() + " " + getObject().getOffsetAtBegin();
    }

}
