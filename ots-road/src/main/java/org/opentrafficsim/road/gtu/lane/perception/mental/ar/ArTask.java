package org.opentrafficsim.road.gtu.lane.perception.mental.ar;

import org.opentrafficsim.road.gtu.lane.perception.mental.Task;

/**
 * Interface for tasks within in an Anticipation Reliance context where a task manager determines level of Anticipation Reliance
 * and task demand.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface ArTask extends Task
{

    /**
     * Set anticipation reliance.
     * @param anticipationReliance set anticipation reliance
     */
    void setAnticipationReliance(double anticipationReliance);

    /**
     * Returns the level of anticipation reliance.
     * @return anticipation reliance
     */
    double getAnticipationReliance();

}
