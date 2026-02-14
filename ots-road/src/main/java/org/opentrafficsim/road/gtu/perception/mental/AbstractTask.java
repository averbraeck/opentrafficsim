package org.opentrafficsim.road.gtu.perception.mental;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.road.gtu.perception.LanePerception;

/**
 * Has task demand as internal variable.
 * <p>
 * Copyright (c) 2025-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class AbstractTask implements Task
{

    /** Id. */
    private final String id;

    /** Task demand. */
    private double taskDemand;

    /**
     * Constructor.
     * @param id id
     */
    public AbstractTask(final String id)
    {
        this.id = id;
    }

    @Override
    public double getTaskDemand(final LanePerception perception) throws ParameterException
    {
        this.taskDemand = calculateTaskDemand(perception);
        return this.taskDemand;
    }

    /**
     * Calculates the task demand.
     * @param perception perception
     * @return task demand
     * @throws ParameterException if a parameter is missing or out of bounds
     */
    protected abstract double calculateTaskDemand(LanePerception perception) throws ParameterException;

    @Override
    public double getTaskDemand()
    {
        return this.taskDemand;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

}
