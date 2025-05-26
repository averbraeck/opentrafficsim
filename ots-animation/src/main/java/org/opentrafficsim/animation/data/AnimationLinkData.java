package org.opentrafficsim.animation.data;

import org.djutils.draw.line.PolyLine2d;
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
public class AnimationLinkData extends AnimationIdentifiableShape<Link> implements LinkData
{

    /**
     * Constructor.
     * @param link link.
     */
    public AnimationLinkData(final Link link)
    {
        super(link);
    }

    @Override
    public boolean isConnector()
    {
        return getObject().isConnector();
    }

    @Override
    public PolyLine2d getCenterLine()
    {
        return getObject().getDesignLine();
    }

    @Override
    public PolyLine2d getLine()
    {
        return OtsShape.transformLine(getCenterLine(), getLocation());
    }

    @Override
    public String toString()
    {
        return "Link " + getId();
    }

}
