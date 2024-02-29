package org.opentrafficsim.editor.extensions.map;

import org.djutils.draw.point.Point2d;
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

    /**
     * Constructor.
     * @param linkData MapLinkData; link data.
     */
    public MapPriorityData(final MapLinkData linkData)
    {
        if (linkData.getDesignLine().getLength() > 10.0)
        {
            this.location = linkData.getDesignLine().getLocationExtended(5.0);
        }
        else
        {
            this.location = linkData.getDesignLine().getLocationFractionExtended(0.5);
        }
        this.linkData = linkData;
    }

    /** {@inheritDoc} */
    @Override
    public Point2d getLocation()
    {
        return this.location;
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
