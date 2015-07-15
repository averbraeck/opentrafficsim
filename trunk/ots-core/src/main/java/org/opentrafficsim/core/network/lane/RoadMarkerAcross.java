package org.opentrafficsim.core.network.lane;

import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial versionAug 21, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class RoadMarkerAcross
{
    /** Cross section element for which this is a road marker. Usually this will be a Lane. */
    private final CrossSectionElement crossSectionElement;

    /** Longitudinal position on the cross section element. */
    private final DoubleScalar.Rel<LengthUnit> longitudinalPosition;

    /**
     * @param crossSectionElement Cross section element for which this is a road marker. Usually this will be a Lane.
     * @param longitudinalPosition Longitudinal position on the cross section element.
     */
    public RoadMarkerAcross(final CrossSectionElement crossSectionElement,
            final DoubleScalar.Rel<LengthUnit> longitudinalPosition)
    {
        super();
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
    public final DoubleScalar.Rel<LengthUnit> getLongitudinalPosition()
    {
        return this.longitudinalPosition;
    }

}
