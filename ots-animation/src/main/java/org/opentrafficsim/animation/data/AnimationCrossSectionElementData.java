package org.opentrafficsim.animation.data;

import java.rmi.RemoteException;
import java.util.List;

import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.draw.ClickableBounds;
import org.opentrafficsim.draw.road.CrossSectionElementAnimation.CrossSectionElementData;
import org.opentrafficsim.road.network.lane.CrossSectionElement;

/**
 * Animation data of a CrossSectionElement.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <T> cross-section element type.
 */
public class AnimationCrossSectionElementData<T extends CrossSectionElement> implements CrossSectionElementData
{

    /** Cross section element. */
    private final T element;

    /** Contour. */
    private List<Point2d> contour = null;

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
    public Bounds<?, ?, ?> getBounds() throws RemoteException
    {
        return ClickableBounds.get(this.element.getBounds());
    }

    /** {@inheritDoc} */
    @Override
    public List<Point2d> getContour()
    {
        if (this.contour == null)
        {
            // this creates a new list every time, so we cache it
            this.contour = this.element.getContour().getPointList();
        }
        return this.contour;
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

}
