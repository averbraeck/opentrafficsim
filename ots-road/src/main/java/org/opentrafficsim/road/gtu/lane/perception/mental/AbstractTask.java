package org.opentrafficsim.road.gtu.lane.perception.mental;

/**
 * Has id, task demand and anticipation reliance as internal variables.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class AbstractTask implements Task
{

    /** Id. */
    private final String id;

    /** Task demand. */
    private double taskDemand;

    /** Anticipation reliance. */
    private double anticipationReliance;

    /**
     * Constructor.
     * @param id id
     */
    public AbstractTask(final String id)
    {
        this.id = id;
    }

    /** {@inheritDoc} */
    @Override
    public String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public final void setTaskDemand(final double taskDemand)
    {
        this.taskDemand = taskDemand;
    }

    /** {@inheritDoc} */
    @Override
    public final double getTaskDemand()
    {
        return this.taskDemand;
    }

    /** {@inheritDoc} */
    @Override
    public final void setAnticipationReliance(final double anticipationReliance)
    {
        this.anticipationReliance = anticipationReliance;
    }

    /** {@inheritDoc} */
    @Override
    public final double getAnticipationReliance()
    {
        return this.anticipationReliance;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Task (" + getId() + ")";
    }
}
