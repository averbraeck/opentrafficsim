package org.opentrafficsim.editor.extensions.map;

import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.vector.LengthVector;
import org.djutils.draw.line.PolyLine2d;
import org.opentrafficsim.base.geometry.OtsLocatable;
import org.opentrafficsim.draw.road.StripeAnimation.StripeData;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.road.network.lane.CrossSectionGeometry;
import org.opentrafficsim.road.network.lane.Stripe.StripeType;

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
    private final StripeType type;

    /** Width. */
    private final Length width;

    /** Dash offset. */
    private final Length dashOffset;

    /** Start offset. */
    private final Length startOffset;

    /**
     * Constructor.
     * @param type stripe type
     * @param width width
     * @param dashOffset dash offset
     * @param linkNode node representing the element
     * @param geometry geometry
     * @param linkLength link length
     */
    public MapStripeData(final StripeType type, final Length width, final Length dashOffset, final XsdTreeNode linkNode,
            final CrossSectionGeometry geometry, final Length linkLength)
    {
        super(linkNode, geometry, linkLength);
        this.type = type;
        this.width = width;
        this.dashOffset = dashOffset;
        this.startOffset = Length.instantiateSI(geometry.offset().apply(0.0));
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
    public List<LengthVector> getDashes()
    {
        return this.type.dashes();
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
