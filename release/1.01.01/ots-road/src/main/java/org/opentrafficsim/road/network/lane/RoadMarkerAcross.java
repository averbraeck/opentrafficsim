package org.opentrafficsim.road.network.lane;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Length;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-03 13:38:01 +0200 (Thu, 03 Sep 2015) $, @version $Revision: 1378 $, by $Author: averbraeck $,
 * initial version Aug 21, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
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
     * @param crossSectionElement CrossSectionElement; Cross section element for which this is a road marker. Usually this will
     *            be a Lane.
     * @param longitudinalPosition Length; Longitudinal position on the cross section element.
     */
    public RoadMarkerAcross(final CrossSectionElement crossSectionElement, final Length longitudinalPosition)
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
    public final Length getLongitudinalPosition()
    {
        return this.longitudinalPosition;
    }

}
