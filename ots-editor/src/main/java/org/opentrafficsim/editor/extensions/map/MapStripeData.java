package org.opentrafficsim.editor.extensions.map;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.opentrafficsim.draw.road.StripeAnimation.StripeData;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.road.network.lane.SliceInfo;

/**
 * Stripe data for in the editor.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class MapStripeData extends MapCrossSectionData implements StripeData
{

    /** Stripe type. */
    private final Type type;

    /** Width. */
    private final Length width;

    /** Start offset. */
    private final Length startOffset;

    /**
     * Constructor.
     * @param type stripe type.
     * @param width width.
     * @param startOffset start offset.
     * @param linkNode node representing the element.
     * @param centerLine center line.
     * @param contour contour.
     * @param sliceInfo slice info.
     */
    public MapStripeData(final Type type, final Length width, final Length startOffset, final XsdTreeNode linkNode,
            final PolyLine2d centerLine, final Polygon2d contour, final SliceInfo sliceInfo)
    {
        super(linkNode, centerLine, contour, sliceInfo);
        this.type = type;
        this.width = width;
        this.startOffset = startOffset;
    }

    /** {@inheritDoc} */
    @Override
    public PolyLine2d getCenterLine()
    {
        return this.centerLine;
    }

    /** {@inheritDoc} */
    @Override
    public Type getType()
    {
        return this.type;
    }

    /** {@inheritDoc} */
    @Override
    public Length getWidth()
    {
        return this.width;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Stripe " + getLinkId() + " " + this.startOffset;
    }

}
