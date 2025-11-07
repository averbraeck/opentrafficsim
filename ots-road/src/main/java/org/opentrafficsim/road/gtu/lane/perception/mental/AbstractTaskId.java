package org.opentrafficsim.road.gtu.lane.perception.mental;

/**
 * Has id and task demand as internal variables.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class AbstractTaskId extends AbstractTask
{

    /** Id. */
    private final String id;

    /**
     * Constructor.
     * @param id id
     */
    public AbstractTaskId(final String id)
    {
        this.id = id;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

}
