package org.opentrafficsim.editor.extensions.map;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.line.Ray2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.draw.road.CrossSectionElementAnimation.CrossSectionElementData;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.road.network.lane.CrossSectionGeometry;

/**
 * Cross section element data for in the editor.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class MapCrossSectionData implements CrossSectionElementData
{

    /** Node representing the element. */
    private final XsdTreeNode linkNode;

    /** Location. */
    private final OrientedPoint2d location;

    /** Geometry. */
    private final CrossSectionGeometry geometry;

    /** Shape (cached). */
    private OtsShape shape;

    /**
     * Constructor.
     * @param linkNode node representing the element
     * @param geometry geometry
     * @param linkLength link length
     */
    public MapCrossSectionData(final XsdTreeNode linkNode, final CrossSectionGeometry geometry, final Length linkLength)
    {
        this.linkNode = linkNode;
        Ray2d ray = geometry.centerLine().getLocationFractionExtended(0.5);
        this.location = new OrientedPoint2d(ray.x, ray.y, ray.phi);
        this.geometry = geometry;
    }

    @Override
    public OrientedPoint2d getLocation()
    {
        return this.location;
    }

    @Override
    public Polygon2d getContour()
    {
        return this.geometry.contour();
    }

    @Override
    public OtsShape getShape()
    {
        if (this.shape == null)
        {
            this.shape = CrossSectionElementData.super.getShape();
        }
        return this.shape;
    }

    @Override
    public PolyLine2d getCenterLine()
    {
        return this.geometry.centerLine();
    }

    /**
     * Returns the link id.
     * @return link id.
     */
    @Override
    public String getLinkId()
    {
        return this.linkNode.getId();
    }

    /**
     * Returns the lane width at the give position.
     * @param position position along the lane.
     * @return lane width at the position.
     */
    public Length getWidth(final Length position)
    {
        return this.geometry.width().apply(position);
    }

    @Override
    public String toString()
    {
        return "Cross section element of " + getLinkId();
    }

}
