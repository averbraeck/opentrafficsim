package org.opentrafficsim.road.gtu.lane.perception.mental;

import org.djutils.base.Identifiable;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

/**
 * Interface for tasks, where each describes a fundamental relation between exogenous inputs causing a mental task demand.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface Task extends Identifiable
{

    /**
     * Returns the gross task demand.
     * @param perception perception
     * @return gross task demand
     * @throws ParameterException if a parameter is missing or out of bounds
     */
    double getTaskDemand(LanePerception perception) throws ParameterException;

    /**
     * Returns the gross task demand as most recently returned by {@code getTaskDemand(LanePerception)}.
     * @return gross task demand
     */
    double getTaskDemand();

}
