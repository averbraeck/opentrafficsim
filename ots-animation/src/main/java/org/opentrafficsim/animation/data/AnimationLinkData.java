package org.opentrafficsim.animation.data;

import java.rmi.RemoteException;

import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.Point;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.draw.ClickableBounds;
import org.opentrafficsim.draw.network.LinkAnimation.LinkData;

/**
 * Animation data of a Link.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class AnimationLinkData implements LinkData
{

    /** Link. */
    private final Link link;

    /**
     * Constructor.
     * @param link Link; link.
     */
    public AnimationLinkData(final Link link)
    {
        this.link = link;
    }

    /** {@inheritDoc} */
    @Override
    public Bounds<?, ?, ?> getBounds() throws RemoteException
    {
        return ClickableBounds.get(this.link.getBounds());
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
    public Point<?> getLocation()
    {
        return this.link.getLocation();
    }

    /**
     * Returns the link.
     * @return Link; link.
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
