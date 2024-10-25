package org.opentrafficsim.animation.data;

import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.opentrafficsim.base.geometry.OtsLocatable;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.draw.network.LinkAnimation.LinkData;

/**
 * Animation data of a Link.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AnimationLinkData implements LinkData
{

    /** Link. */
    private final Link link;

    /** Shape (cached). */
    private OtsShape shape;

    /**
     * Constructor.
     * @param link link.
     */
    public AnimationLinkData(final Link link)
    {
        this.link = link;
    }

    @Override
    public Polygon2d getContour()
    {
        return this.link.getContour();
    }

    @Override
    public OtsShape getShape()
    {
        if (this.shape == null)
        {
            this.shape = LinkData.super.getShape();
        }
        return this.shape;
    }

    @Override
    public String getId()
    {
        return this.link.getId();
    }

    @Override
    public boolean isConnector()
    {
        return this.link.isConnector();
    }

    @Override
    public PolyLine2d getCenterLine()
    {
        return this.link.getDesignLine();
    }

    @Override
    public PolyLine2d getLine()
    {
        return OtsLocatable.transformLine(getCenterLine(), getLocation());
    }

    @Override
    public OrientedPoint2d getLocation()
    {
        return this.link.getLocation();
    }

    /**
     * Returns the link.
     * @return link.
     */
    public Link getLink()
    {
        return this.link;
    }

    @Override
    public String toString()
    {
        return "Link " + this.link.getId();
    }

}
