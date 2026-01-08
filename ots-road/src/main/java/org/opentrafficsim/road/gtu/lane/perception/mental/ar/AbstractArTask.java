package org.opentrafficsim.road.gtu.lane.perception.mental.ar;

import org.opentrafficsim.road.gtu.lane.perception.mental.AbstractTask;

/**
 * Has id, task demand and anticipation reliance as internal variables.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class AbstractArTask extends AbstractTask implements ArTask
{

    /** Anticipation reliance. */
    private double anticipationReliance;

    /**
     * Constructor.
     * @param id id
     */
    public AbstractArTask(final String id)
    {
        super(id);
    }

    @Override
    public void setAnticipationReliance(final double anticipationReliance)
    {
        this.anticipationReliance = anticipationReliance;
    }

    @Override
    public double getAnticipationReliance()
    {
        return this.anticipationReliance;
    }

    @Override
    public String toString()
    {
        return "ArTask (" + getId() + ")";
    }
}
