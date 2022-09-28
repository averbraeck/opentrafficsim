package org.opentrafficsim.road.gtu.lane.perception.mental;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

/**
 * Interface for mental modules operating on perception. This is a first step of perception, where parameters both for the
 * perception and tactical models can be set.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface Mental
{

    /**
     * Apply mental model on perception.
     * @param perception LanePerception; perception
     * @throws ParameterException if a parameter is missing or out of bounds
     * @throws GTUException exceptions pertaining to the GTU
     */
    void apply(LanePerception perception) throws ParameterException, GTUException;

}
