package org.opentrafficsim.road.gtu.lane.perception.categories;

import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;

/**
 * Perception of general traffic ahead.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface TrafficPerception extends LaneBasedPerceptionCategory
{

    /**
     * Returns the perceived speed on the given lane.
     * @param lane RelativeLane; lane
     * @return Speed perceived speed on the given lane
     * @throws ParameterException on parameter exception
     */
    Speed getSpeed(RelativeLane lane) throws ParameterException;

    /**
     * Returns the perceived density on the given lane.
     * @param lane RelativeLane; lane
     * @return LinearDensity perceived density on the given lane
     * @throws ParameterException on parameter exception
     */
    LinearDensity getDensity(RelativeLane lane) throws ParameterException;

}
