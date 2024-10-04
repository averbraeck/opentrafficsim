package org.opentrafficsim.animation.data;

import org.djutils.draw.point.Point2d;
import org.opentrafficsim.base.geometry.BoundingRectangle;
import org.opentrafficsim.base.geometry.OtsBounds2d;
import org.opentrafficsim.core.gtu.GtuGenerator.GtuGeneratorPosition;
import org.opentrafficsim.draw.road.GtuGeneratorPositionAnimation.GtuGeneratorPositionData;

/**
 * Animation data of a GtuGeneratorPosition.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AnimationGtuGeneratorPositionData implements GtuGeneratorPositionData
{

    /** Position. */
    private final GtuGeneratorPosition position;

    /**
     * Constructor.
     * @param position position within a generator.
     */
    public AnimationGtuGeneratorPositionData(final GtuGeneratorPosition position)
    {
        this.position = position;
    }

    /** {@inheritDoc} */
    @Override
    public Point2d getLocation()
    {
        return this.position.getLocation();
    }

    /** {@inheritDoc} */
    @Override
    public OtsBounds2d getBounds()
    {
        // this correlates to how generators are drawn as three chevrons
        return new BoundingRectangle(0.0, 4.75, -1.0, 1.0);
    }

    /** {@inheritDoc} */
    @Override
    public int getQueueCount()
    {
        return this.position.getQueueCount();
    }

    /**
     * Returns the generator position.
     * @return generator position.
     */
    public GtuGeneratorPosition getGeneratorPosition()
    {
        return this.position;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Generator position " + this.position.getId();
    }

}
