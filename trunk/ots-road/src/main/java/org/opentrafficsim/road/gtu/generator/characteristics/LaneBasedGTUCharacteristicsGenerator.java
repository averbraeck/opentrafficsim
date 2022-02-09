package org.opentrafficsim.road.gtu.generator.characteristics;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.gtu.GTUException;

/**
 * Interface for objects that can generate a LaneBasedGTUCharacteristics object.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 9, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface LaneBasedGTUCharacteristicsGenerator
{
    /**
     * Generate a LaneBasedGTUCharacteristics object.
     * @return LaneBasedGTUCharacteristics
     * @throws ProbabilityException when the generator is improperly configured
     * @throws ParameterException in case of a parameter problem.
     * @throws GTUException if strategical planner cannot be created
     */
    LaneBasedGTUCharacteristics draw() throws ProbabilityException, ParameterException, GTUException;

}
