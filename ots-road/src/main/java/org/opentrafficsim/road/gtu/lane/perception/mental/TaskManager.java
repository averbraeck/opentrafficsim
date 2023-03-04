package org.opentrafficsim.road.gtu.lane.perception.mental;

import java.util.Set;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

/**
 * A task manager controls which task has priority and as a result how anticipation reliance is divided over different tasks.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
// TODO: rename MentalTaskRegulator as in paper? (also sub-classes)
public interface TaskManager
{
    /**
     * Manage tasks.
     * @param tasks Set&lt;Task&gt;; tasks
     * @param perception LanePerception; perception
     * @param gtu LaneBasedGtu; gtu
     * @param parameters Parameters; parameters
     * @throws ParameterException if a parameter is missing or out of bounds
     * @throws GtuException exceptions pertaining to the GTU
     */
    void manage(Set<Task> tasks, LanePerception perception, LaneBasedGtu gtu, Parameters parameters)
            throws ParameterException, GtuException;

    /**
     * Manages a set of tasks without considering anticipation reliance.
     * <p>
     * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    class SummativeTaskManager implements TaskManager
    {
        /** {@inheritDoc} */
        @Override
        public void manage(final Set<Task> tasks, final LanePerception perception, final LaneBasedGtu gtu,
                final Parameters parameters) throws ParameterException, GtuException
        {
            for (Task task : tasks)
            {
                double taskDemand = task.calculateTaskDemand(perception, gtu, parameters);
                task.setTaskDemand(taskDemand);
            }
        }
    }

}
