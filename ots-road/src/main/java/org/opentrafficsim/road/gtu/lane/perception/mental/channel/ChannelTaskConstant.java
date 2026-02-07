package org.opentrafficsim.road.gtu.lane.perception.mental.channel;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.mental.AbstractTask;

/**
 * Constant task demand.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ChannelTaskConstant extends AbstractTask implements ChannelTask
{

    /** Channel. */
    private final Object channel;

    /** Task demand. */
    private final double taskDemand;

    /**
     * Constructor.
     * @param id id
     * @param channel channel
     * @param taskDemand task demand
     */
    public ChannelTaskConstant(final String id, final Object channel, final double taskDemand)
    {
        super(id);
        this.channel = channel;
        this.taskDemand = taskDemand;
    }

    @Override
    public Object getChannel()
    {
        return this.channel;
    }

    @Override
    protected double calculateTaskDemand(final LanePerception perception) throws ParameterException
    {
        return this.taskDemand;
    }

}
