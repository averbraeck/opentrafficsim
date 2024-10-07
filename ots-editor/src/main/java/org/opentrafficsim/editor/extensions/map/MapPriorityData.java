package org.opentrafficsim.editor.extensions.map;

import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.base.geometry.OtsLocatable;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.draw.road.PriorityAnimation.PriorityData;

/**
 * Priority data for in the editor.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class MapPriorityData implements PriorityData
{

    /** Link data. */
    private final MapLinkData linkData;

    /** Location. */
    private final Point2d location;

    /** Bounds. */
    private final Bounds2d bounds = new Bounds2d(2.0, 2.0);

    /** Contour. */
    private final Polygon2d contour;

    /** Shape (cached). */
    private OtsShape shape;

    /**
     * Constructor.
     * @param linkData link data.
     */
    public MapPriorityData(final MapLinkData linkData)
    {
        this.location = linkData.getCenterLine().getLocationFractionExtended(0.5);
        this.linkData = linkData;
        this.contour = OtsLocatable.boundsAsContour(this);
    }

    /** {@inheritDoc} */
    @Override
    public Point2d getLocation()
    {
        return this.location;
    }

    /** {@inheritDoc} */
    @Override
    public Polygon2d getContour()
    {
        return this.contour;
    }

    /** {@inheritDoc} */
    @Override
    public OtsShape getShape()
    {
        if (this.shape == null)
        {
            this.shape = PriorityData.super.getShape();
        }
        return this.shape;
    }

    /** {@inheritDoc} */
    @Override
    public Bounds2d getBounds()
    {
        return this.bounds;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAllStop()
    {
        return "ALL_STOP".equals(this.linkData.getNode().getAttributeValue("Priority"));
    }

    /** {@inheritDoc} */
    @Override
    public boolean isBusStop()
    {
        return "BUS_STOP".equals(this.linkData.getNode().getAttributeValue("Priority"));
    }

    /** {@inheritDoc} */
    @Override
    public boolean isNone()
    {
        return "NONE".equals(this.linkData.getNode().getAttributeValue("Priority"));
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPriority()
    {
        return "PRIORITY".equals(this.linkData.getNode().getAttributeValue("Priority"));
    }

    /** {@inheritDoc} */
    @Override
    public boolean isStop()
    {
        return "STOP".equals(this.linkData.getNode().getAttributeValue("Priority"));
    }

    /** {@inheritDoc} */
    @Override
    public boolean isYield()
    {
        return "YIELD".equals(this.linkData.getNode().getAttributeValue("Priority"));
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Priority " + this.linkData.getId();
    }

}
