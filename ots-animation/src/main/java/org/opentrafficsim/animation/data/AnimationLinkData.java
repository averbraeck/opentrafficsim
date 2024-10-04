package org.opentrafficsim.animation.data;

import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.opentrafficsim.base.geometry.OtsBounds2d;
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

    /**
     * Constructor.
     * @param link link.
     */
    public AnimationLinkData(final Link link)
    {
        this.link = link;
    }

    /** {@inheritDoc} */
    @Override
    public OtsBounds2d getBounds()
    {
        return this.link.getBounds();
    }

    /** {@inheritDoc} */
    @Override
    public String getId()
    {
        return this.link.getId();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConnector()
    {
        return this.link.isConnector();
    }

    /** {@inheritDoc} */
    @Override
    public PolyLine2d getDesignLine()
    {
        return this.link.getDesignLine().getLine2d();
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Link " + this.link.getId();
    }

}
