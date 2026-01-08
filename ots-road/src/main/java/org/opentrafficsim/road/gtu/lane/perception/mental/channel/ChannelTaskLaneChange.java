package org.opentrafficsim.road.gtu.lane.perception.mental.channel;

import java.util.Set;
import java.util.function.Function;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.mental.AbstractTask;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;

/**
 * Task demand for lane changing left and right. This is defined as the level of lane change desire if the lane change desire in
 * the relevant direction is equal or larger to the other direction. Otherwise it is 0.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class ChannelTaskLaneChange extends AbstractTask implements ChannelTask
{

    /** Current left lane change desire. */
    private static final ParameterTypeDouble DLEFT = LmrsParameters.DLEFT;

    /** Current right lane change desire. */
    private static final ParameterTypeDouble DRIGHT = LmrsParameters.DRIGHT;

    /** Standard set of left and right lane-change task. */
    private static final Set<ChannelTask> SET = Set.of(new ChannelTaskLaneChange(true), new ChannelTaskLaneChange(false));

    /** Standard supplier that supplies instances for left and right lane-change task. */
    public static final Function<LanePerception, Set<ChannelTask>> SUPPLIER = (p) -> SET;

    /** Whether this task instance regards the left side. */
    private final boolean left;

    /**
     * Constructor.
     * @param left whether this task instance regards the left side.
     */
    private ChannelTaskLaneChange(final boolean left)
    {
        super("lane-changing");
        this.left = left;
    }

    @Override
    public String getId()
    {
        return String.format("lane-changing (%s)", this.left ? "left" : "right");
    }

    @Override
    public Object getChannel()
    {
        return this.left ? LEFT : RIGHT;
    }

    @Override
    public double calculateTaskDemand(final LanePerception perception) throws ParameterException
    {
        double dLeft = perception.getGtu().getParameters().getParameter(DLEFT);
        double dRight = perception.getGtu().getParameters().getParameter(DRIGHT);
        return Math.min(0.999,
                this.left ? (dLeft >= dRight && dLeft > 0.0 ? dLeft : 0.0) : (dRight >= dLeft && dRight > 0.0 ? dRight : 0.0));
    }

}
