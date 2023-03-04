package org.opentrafficsim.road.gtu.lane.perception.mental;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

/**
 * Interface for mental modules operating on perception. This is a first step of perception, where parameters both for the
 * perception and tactical models can be set.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface Mental
{

    /**
     * Apply mental model on perception.
     * @param perception LanePerception; perception
     * @throws ParameterException if a parameter is missing or out of bounds
     * @throws GtuException exceptions pertaining to the GTU
     */
    void apply(LanePerception perception) throws ParameterException, GtuException;

}
