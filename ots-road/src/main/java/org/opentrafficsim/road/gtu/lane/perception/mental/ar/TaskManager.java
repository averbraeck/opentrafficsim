package org.opentrafficsim.road.gtu.lane.perception.mental.ar;

import org.djutils.immutablecollections.ImmutableSet;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

/**
 * A task manager controls which task has priority and as a result how anticipation reliance is divided over different tasks.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface TaskManager
{

    /**
     * Manage tasks.
     * @param tasks tasks
     * @param perception perception
     * @throws ParameterException if a parameter is missing or out of bounds
     */
    void manage(ImmutableSet<ArTask> tasks, LanePerception perception) throws ParameterException;

    /**
     * Manages a set of tasks without considering anticipation reliance.
     */
    class SummativeTaskManager implements TaskManager
    {
        /**
         * Constructor.
         */
        public SummativeTaskManager()
        {
            //
        }

        @Override
        public void manage(final ImmutableSet<ArTask> tasks, final LanePerception perception) throws ParameterException
        {
            // full demand, no AR
        }
    }

}
