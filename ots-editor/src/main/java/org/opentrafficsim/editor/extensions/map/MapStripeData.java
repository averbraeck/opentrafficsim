package org.opentrafficsim.editor.extensions.map;

import java.util.List;

import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.line.PolyLine2d;
import org.opentrafficsim.base.StripeElement;
import org.opentrafficsim.base.StripeElement.StripeLateralSync;
import org.opentrafficsim.base.geometry.OtsLocatable;
import org.opentrafficsim.draw.road.StripeAnimation.DirectionalPolyLine;
import org.opentrafficsim.draw.road.StripeAnimation.StripeData;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.extensions.map.MapLinkData.MiddleOffset;
import org.opentrafficsim.road.network.lane.CrossSectionGeometry;

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

    /** Width. */
    private final Length width;

    /** Dash offset. */
    private final Length dashOffset;

    /** Start offset. */
    private final Length startOffset;

    /** Stripe elements. */
    private final List<StripeElement> elements;

    /** Lateral synchronization. */
    private final StripeLateralSync lateralSync;

    /** Link line. */
    private final PolyLine2d linkLine;

    /** Link reference line. */
    private PolyLine2d linkReferenceLine = null;

    /** Middle offsets to determine link reference line. */
    private final MiddleOffset middleOffset;

    /** Start direction. */
    private final Direction startDirection;

    /** End direction. */
    private final Direction endDirection;

    /** Center line with direction. */
    private DirectionalPolyLine centerLine;

    /**
     * Constructor.
     * @param dashOffset dash offset
     * @param linkNode node representing the element
     * @param geometry geometry
     * @param elements elements
     * @param lateralSync lateral synchronization
     * @param linkLine link line
     * @param middleOffset middle offsets to determine link reference line
     * @param startDirection start direction
     * @param endDirection end direction
     */
    public MapStripeData(final Length dashOffset, final XsdTreeNode linkNode, final CrossSectionGeometry geometry,
            final List<StripeElement> elements, final StripeLateralSync lateralSync, final PolyLine2d linkLine,
            final MiddleOffset middleOffset, final Direction startDirection, final Direction endDirection)
    {
        super(linkNode, geometry);
        Length w = Length.ZERO;
        for (StripeElement element : elements)
        {
            w = w.plus(element.width());
        }
        this.width = w;
        this.dashOffset = dashOffset;
        this.startOffset = Length.instantiateSI(geometry.offset().apply(0.0));
        this.elements = elements;
        this.lateralSync = lateralSync;
        this.linkLine = linkLine;
        this.middleOffset = middleOffset;
        this.startDirection = startDirection;
        this.endDirection = endDirection;
    }

    @Override
    public PolyLine2d getLine()
    {
        return OtsLocatable.transformLine(getCenterLine(), getLocation());
    }

    @Override
    public Length getWidth(final Length position)
    {
        return this.width;
    }

    @Override
    public PolyLine2d getReferenceLine()
    {
        return this.lateralSync.equals(StripeLateralSync.NONE) ? this.getCenterLine() : getLinkReferenceLine();
    }

    @Override
    public DirectionalPolyLine getCenterLine()
    {
        if (this.centerLine == null)
        {
            this.centerLine = new DirectionalPolyLine(super.getCenterLine(), this.startDirection, this.endDirection);
        }
        return this.centerLine;
    }

    /**
     * Return link reference line.
     * @return link reference line
     */
    private PolyLine2d getLinkReferenceLine()
    {
        if (this.linkReferenceLine == null)
        {
            this.linkReferenceLine = new DirectionalPolyLine(this.linkLine, this.startDirection, this.endDirection)
                    .directionalOffsetLine(this.middleOffset.getStartOffset(), this.middleOffset.getEndOffset());
        }
        return this.linkReferenceLine;
    }

    @Override
    public List<StripeElement> getElements()
    {
        return this.elements;
    }

    @Override
    public Length getDashOffset()
    {
        return this.dashOffset;
    }

    @Override
    public String toString()
    {
        return "Stripe " + getLinkId() + " " + this.startOffset;
    }

}
