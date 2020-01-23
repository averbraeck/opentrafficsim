package org.opentrafficsim.road.gtu.lane.perception.categories;

import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;

/**
 * Perception of general traffic ahead.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 13 mrt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
