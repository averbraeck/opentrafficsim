package org.opentrafficsim.road.gtu.lane.perception.mental;

import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

/**
 * Interface for tasks, where each describes a fundamental relation between exogenous inputs causing a mental task demand. The
 * concept of anticipation reliance can be included, which is a reduction of mental task demand by prioritizing a primary task
 * and relying more on anticipation regarding secondary tasks. Control over the amount of anticipation reliance is
 * implementation dependent, but is typically not part of the task itself.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 apr. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface Task extends Identifiable
{

    /**
     * Returns the gross task demand to be managed by a task manager.
     * @param perception LanePerception; perception
     * @param gtu LaneBasedGTU; gtu
     * @param parameters Parameters; parameters
     * @return double; gross task demand
     * @throws ParameterException if a parameter is missing or out of bounds
     * @throws GTUException exceptions pertaining to the GTU
     */
    double calculateTaskDemand(LanePerception perception, LaneBasedGTU gtu, Parameters parameters)
            throws ParameterException, GTUException;

    /**
     * Sets (gross) task demand.
     * @param taskDemand double; set task demand
     */
    void setTaskDemand(double taskDemand);

    /**
     * Returns the gross demand of this task, i.e without considering anticipation reliance.
     * @return double; gross demand of this task, i.e. without considering anticipation reliance
     */
    double getTaskDemand();

    /**
     * Set anticipation reliance.
     * @param anticipationReliance double; set anticipation reliance
     */
    void setAnticipationReliance(double anticipationReliance);

    /**
     * Returns the level of anticipation reliance.
     * @return double; anticipation reliance
     */
    double getAnticipationReliance();

}
