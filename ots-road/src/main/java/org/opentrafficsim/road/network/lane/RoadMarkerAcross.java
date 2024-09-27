package org.opentrafficsim.road.network.lane;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Length;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public abstract class RoadMarkerAcross implements Serializable
{
    /** */
    private static final long serialVersionUID = 20141021L;

    /** Cross section element for which this is a road marker. Usually this will be a Lane. */
    private final CrossSectionElement crossSectionElement;

    /** Longitudinal position on the cross section element. */
    private final Length longitudinalPosition;

    /**
     * @param crossSectionElement Cross section element for which this is a road marker. Usually this will be a Lane.
     * @param longitudinalPosition Longitudinal position on the cross section element.
     */
    public RoadMarkerAcross(final CrossSectionElement crossSectionElement, final Length longitudinalPosition)
    {
        this.crossSectionElement = crossSectionElement;
        this.longitudinalPosition = longitudinalPosition;
    }

    /**
     * @return crossSectionElement.
     */
    public final CrossSectionElement getCrossSectionElement()
    {
        return this.crossSectionElement;
    }

    /**
     * @return longitudinalPosition.
     */
    public final Length getLongitudinalPosition()
    {
        return this.longitudinalPosition;
    }

}
