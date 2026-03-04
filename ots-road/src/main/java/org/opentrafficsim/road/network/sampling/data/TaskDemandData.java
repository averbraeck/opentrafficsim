package org.opentrafficsim.road.network.sampling.data;

import java.util.Optional;

import org.opentrafficsim.kpi.sampling.data.ExtendedDataNumber;
import org.opentrafficsim.road.gtu.perception.mental.Fuller;
import org.opentrafficsim.road.gtu.perception.mental.Mental;
import org.opentrafficsim.road.gtu.perception.mental.Task;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Task demand trajectory data, specific to a task.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TaskDemandData extends ExtendedDataNumber<GtuDataRoad>
{

    /** Task id. */
    private String taskId;

    /**
     * Constructor.
     * @param taskId task id
     */
    public TaskDemandData(final String taskId)
    {
        super(taskId + "_TD", "Task demand of task " + taskId);
        this.taskId = taskId;
    }

    @Override
    public Optional<Float> getValue(final GtuDataRoad gtu)
    {
        Optional<Mental> mental = gtu.getGtu().getTacticalPlanner().getPerception().getMental();
        if (mental.isPresent() && mental.get() instanceof Fuller fuller)
        {
            Optional<Task> task = fuller.getTask(this.taskId);
            if (task.isPresent())
            {
                Optional.of((float) task.get().getTaskDemand());
            }
        }
        return Optional.empty();
    }

}
