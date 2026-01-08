package org.opentrafficsim.road.gtu.lane.perception.mental;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;

/**
 * Behavioral adaptation by changing parameter values.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@FunctionalInterface
public interface BehavioralAdaptation
{

    /**
     * Adapt to task saturation by changing parameter values.
     * @param parameters parameters
     * @throws ParameterException if a parameter is missing or out of bounds
     */
    void adapt(Parameters parameters) throws ParameterException;

}
