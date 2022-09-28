package org.opentrafficsim.road.gtu.lane.perception.mental;

import java.util.Set;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

/**
 * A task manager controls which task has priority and as a result how anticipation reliance is divided over different tasks.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 30 jan. 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// TODO: rename MentalTaskRegulator as in paper? (also sub-classes)
public interface TaskManager
{
    /**
     * Manage tasks.
     * @param tasks Set&lt;Task&gt;; tasks
     * @param perception LanePerception; perception
     * @param gtu LaneBasedGTU; gtu
     * @param parameters Parameters; parameters
     * @throws ParameterException if a parameter is missing or out of bounds
     * @throws GTUException exceptions pertaining to the GTU
     */
    void manage(Set<Task> tasks, LanePerception perception, LaneBasedGTU gtu, Parameters parameters)
            throws ParameterException, GTUException;

    /**
     * Manages a set of tasks without considering anticipation reliance.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 30 jan. 2019 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    class SummativeTaskManager implements TaskManager
    {
        /** {@inheritDoc} */
        @Override
        public void manage(final Set<Task> tasks, final LanePerception perception, final LaneBasedGTU gtu,
                final Parameters parameters) throws ParameterException, GTUException
        {
            for (Task task : tasks)
            {
                double taskDemand = task.calculateTaskDemand(perception, gtu, parameters);
                task.setTaskDemand(taskDemand);
            }
        }
    }

}
