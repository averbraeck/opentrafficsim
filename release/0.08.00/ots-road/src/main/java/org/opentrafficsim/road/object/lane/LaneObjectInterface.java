package org.opentrafficsim.road.object.lane;

import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.object.ObjectInterface;
import org.opentrafficsim.road.network.lane.CrossSectionElement;

/**
 * Lane-related object such as a traffic light, road sign, or obstacle.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Dec 16, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface LaneObjectInterface extends ObjectInterface
{
    /**
     * Provide the cross section elements to which this lane object is related.
     * @return the cross section element
     */
    Set<CrossSectionElement> getCrossSectionElements();

    /**
     * Provide the fractional longitudinal position on the design line of the link to which this lane object is related.
     * @return the fractional position on the design line of the link
     */
    double getFractionalPosition();

    /**
     * Provide the longitudinal position on the center line of the cross section element to which this lane object is related.
     * Length is measured in the <i>direction</i> of the design line, but <i>measured along</i> the center line of the cross
     * section element.
     * @return the longitudinal position on the center line of the cross section element
     */
    Length.Rel getPosition();

    /**
     * Provide the lateral position relative to the center line of the cross section element to which this lane object is
     * related. Lateral distance is positive to the left (relative to the direction of the design line of the link) and negative
     * to the right.
     * @return the lateral position relative to the center line of the cross section element
     */
    Length.Rel getlateralPosition();

}
