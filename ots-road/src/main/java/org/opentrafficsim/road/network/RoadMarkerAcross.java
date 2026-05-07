package org.opentrafficsim.road.network;

import org.djunits.value.vdouble.scalar.Length;

/**
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 */
public abstract class RoadMarkerAcross
{
    /** Cross section element for which this is a road marker. Usually this will be a Lane. */
    private final CrossSectionElement crossSectionElement;

    /** Longitudinal position on the cross section element. */
    private final Length longitudinalPosition;

    /**
     * Constructor.
     * @param crossSectionElement Cross section element for which this is a road marker. Usually this will be a Lane.
     * @param longitudinalPosition Longitudinal position on the cross section element.
     */
    public RoadMarkerAcross(final CrossSectionElement crossSectionElement, final Length longitudinalPosition)
    {
        this.crossSectionElement = crossSectionElement;
        this.longitudinalPosition = longitudinalPosition;
    }

    /**
     * Returns cross section element.
     * @return crossSectionElement.
     */
    public final CrossSectionElement getCrossSectionElement()
    {
        return this.crossSectionElement;
    }

    /**
     * Returns longitudinal position.
     * @return longitudinalPosition.
     */
    public final Length getLongitudinalPosition()
    {
        return this.longitudinalPosition;
    }

}
