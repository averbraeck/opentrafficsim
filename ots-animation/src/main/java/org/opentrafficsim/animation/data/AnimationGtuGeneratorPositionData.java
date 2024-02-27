package org.opentrafficsim.animation.data;

import org.djutils.draw.point.Point2d;
import org.opentrafficsim.base.geometry.ClickableBounds;
import org.opentrafficsim.base.geometry.OtsBounds2d;
import org.opentrafficsim.core.gtu.GtuGenerator.GtuGeneratorPosition;
import org.opentrafficsim.draw.road.GtuGeneratorPositionAnimation.GtuGeneratorPositionData;

/**
 * Animation data of a GtuGeneratorPosition.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class AnimationGtuGeneratorPositionData implements GtuGeneratorPositionData
{

    /** Position. */
    private final GtuGeneratorPosition position;

    /**
     * Constructor.
     * @param position GtuGeneratorPosition; position within a generator.
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
        return ClickableBounds.get(this.position.getBounds());
    }

    /** {@inheritDoc} */
    @Override
    public int getQueueCount()
    {
        return this.position.getQueueCount();
    }

    /**
     * Returns the generator position.
     * @return GtuGeneratorPosition; generator position.
     */
    public GtuGeneratorPosition getGeneratorPosition()
    {
        return this.position;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Generator position";
    }

}
