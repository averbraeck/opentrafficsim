package org.opentrafficsim.road.gtu.lane.perception.mental.channel;

import java.util.Set;
import java.util.function.Function;

import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.mental.AbstractTask;

/**
 * Task demand for scanning. This is a constant value.
 * <p>
 * Copyright (c) 2024-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ChannelTaskScan extends AbstractTask implements ChannelTask
{

    /** Scanning task demand. */
    public static final ParameterTypeDouble TDSCAN =
            new ParameterTypeDouble("td_scan", "Scanning task demand", 0.0279, NumericConstraint.POSITIVEZERO);

    /** Standard set of left, right, front and rear scan task. */
    private static final Set<ChannelTask> SET = Set.of(new ChannelTaskScan(LEFT), new ChannelTaskScan(RIGHT),
            new ChannelTaskScan(FRONT), new ChannelTaskScan(REAR));

    /** Standard supplier that supplies instances for left, right, front and rear scan task. */
    public static final Function<LanePerception, Set<ChannelTask>> SUPPLIER = (p) -> SET;

    /** Channel. */
    private final Object channel;

    /**
     * Constructor.
     * @param channel channel.
     */
    public ChannelTaskScan(final Object channel)
    {
        super("scan");
        this.channel = channel;
    }

    @Override
    public String getId()
    {
        return String.format("scan (%s)", this.channel);
    }

    @Override
    public Object getChannel()
    {
        return this.channel;
    }

    @Override
    public double calculateTaskDemand(final LanePerception perception)
    {
        return Try.assign(() -> perception.getGtu().getParameters().getParameter(TDSCAN), "Parameter td_scan not available.");
    }

}
