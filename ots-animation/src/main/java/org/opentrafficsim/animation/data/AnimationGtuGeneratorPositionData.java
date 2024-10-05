package org.opentrafficsim.animation.data;

import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.OrientedPoint2d;
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
    public OrientedPoint2d getLocation()
    {
        return this.position.getLocation();
    }

    /** {@inheritDoc} */
    @Override
    public Bounds2d getBounds()
    {
        // this correlates to how generators are drawn as three chevrons
        return new Bounds2d(0.0, 4.75, -1.0, 1.0);
    }

    /** {@inheritDoc} */
    @Override
    public Polygon2d getContour()
    {
        throw new UnsupportedOperationException("GtuGeneratorPosition does not have a contour.");
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
