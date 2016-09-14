package org.opentrafficsim.road.gtu.lane;

import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;

/**
 * Interface for objects that can generate a LaneBasedGTUCharacteristics object.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
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

    /**
     * Return the simulator.
     * @return OTSDEVSSimulatorInterface; the simulator
     * @throws ProbabilityException when the generator cannot retrieve the simulator
     */
    OTSDEVSSimulatorInterface getSimulator() throws ProbabilityException;
}
