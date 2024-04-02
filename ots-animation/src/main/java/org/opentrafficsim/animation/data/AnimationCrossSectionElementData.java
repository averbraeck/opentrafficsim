package org.opentrafficsim.animation.data;

import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.base.geometry.OtsBounds2d;
import org.opentrafficsim.draw.road.CrossSectionElementAnimation.CrossSectionElementData;
import org.opentrafficsim.road.network.lane.CrossSectionElement;

/**
 * Animation data of a CrossSectionElement.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> cross-section element type.
 */
public class AnimationCrossSectionElementData<T extends CrossSectionElement> implements CrossSectionElementData
{

    /** Cross section element. */
    private final T element;

    /**
     * Constructor.
     * @param element T; cross section element.
     */
    public AnimationCrossSectionElementData(final T element)
    {
        this.element = element;
    }

    /** {@inheritDoc} */
    @Override
    public OtsBounds2d getBounds()
    {
        return this.element.getBounds();
    }

    /** {@inheritDoc} */
    @Override
    public PolyLine2d getCenterLine()
    {
        return this.element.getCenterLine().getLine2d();
    }

    /** {@inheritDoc} */
    @Override
    public String getLinkId()
    {
        return this.element.getId();
    }

    /** {@inheritDoc} */
    @Override
    public Point2d getLocation()
    {
        return this.element.getLocation();
    }

    /**
     * Returns the cross section element.
     * @return T; cross-section element.
     */
    public T getElement()
    {
        return this.element;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Cross section element " + getElement().getId();
    }

}
