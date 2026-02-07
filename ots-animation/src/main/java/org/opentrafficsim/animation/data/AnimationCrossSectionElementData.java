package org.opentrafficsim.animation.data;

import org.djutils.draw.line.PolyLine2d;
import org.opentrafficsim.draw.road.CrossSectionElementAnimation.CrossSectionElementData;
import org.opentrafficsim.road.network.lane.CrossSectionElement;

/**
 * Animation data of a CrossSectionElement.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> cross-section element type.
 */
public class AnimationCrossSectionElementData<T extends CrossSectionElement> extends AnimationIdentifiableShape<T>
        implements CrossSectionElementData
{

    /**
     * Constructor.
     * @param element cross section element.
     */
    public AnimationCrossSectionElementData(final T element)
    {
        super(element);
    }

    @Override
    public PolyLine2d getCenterLine()
    {
        return getObject().getCenterLine();
    }

    @Override
    public String getLinkId()
    {
        return getObject().getLink().getId();
    }

    @Override
    public String toString()
    {
        return "Cross section element " + getId();
    }

}
