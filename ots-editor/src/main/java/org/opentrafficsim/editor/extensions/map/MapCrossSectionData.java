package org.opentrafficsim.editor.extensions.map;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.line.Ray2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.opentrafficsim.base.geometry.BoundingPolygon;
import org.opentrafficsim.base.geometry.OtsBounds2d;
import org.opentrafficsim.draw.road.CrossSectionElementAnimation.CrossSectionElementData;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.road.network.lane.SliceInfo;

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
    
    /** Center line. */
    protected final PolyLine2d centerLine;

    /** Bounds. */
    private final OtsBounds2d bounds;

    /** Slice info. */
    private SliceInfo sliceInfo;

    /**
     * Constructor.
     * @param linkNode XsdTreeNode; node representing the element.
     * @param centerLine PolyLine2d; center line.
     * @param contour PolyLine2d; contour.
     * @param sliceInfo SliceInfo; slice info.
     */
    public MapCrossSectionData(final XsdTreeNode linkNode, final PolyLine2d centerLine, final Polygon2d contour,
            final SliceInfo sliceInfo)
    {
        this.linkNode = linkNode;
        Ray2d ray = centerLine.getLocationFractionExtended(0.5);
        this.location = new OrientedPoint2d(ray.x, ray.y, ray.phi);
        this.centerLine = centerLine;
        this.bounds = BoundingPolygon.geometryToBounds(this.location, contour);
        this.sliceInfo = sliceInfo;
    }

    /** {@inheritDoc} */
    @Override
    public OtsBounds2d getBounds()
    {
        return this.bounds;
    }

    /** {@inheritDoc} */
    @Override
    public OrientedPoint2d getLocation()
    {
        return this.location;
    }

    /** {@inheritDoc} */
    @Override
    public PolyLine2d getCenterLine()
    {
        return this.centerLine;
    }

    /**
     * Returns the link id.
     * @return String; link id.
     */
    @Override
    public String getLinkId()
    {
        return this.linkNode.getId();
    }

    /**
     * Returns the lane width at the give position.
     * @param position Length; position along the lane.
     * @return Length; lane width at the position.
     */
    public Length getWidth(final Length position)
    {
        return this.sliceInfo.getWidth(position.si / this.centerLine.getLength());
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Cross section element of " + getLinkId();
    }

}
