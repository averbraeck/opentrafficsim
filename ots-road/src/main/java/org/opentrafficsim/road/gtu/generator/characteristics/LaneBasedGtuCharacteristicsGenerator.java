package org.opentrafficsim.road.gtu.generator.characteristics;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.gtu.GtuException;

/**
 * Interface for objects that can generate a LaneBasedGtuCharacteristics object.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public interface LaneBasedGtuCharacteristicsGenerator
{
    /**
     * Generate a LaneBasedGtuCharacteristics object.
     * @return LaneBasedGtuCharacteristics
     * @throws ProbabilityException when the generator is improperly configured
     * @throws ParameterException in case of a parameter problem.
     * @throws GtuException if strategical planner cannot be created
     */
    LaneBasedGtuCharacteristics draw() throws ProbabilityException, ParameterException, GtuException;

}
