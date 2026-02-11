package org.opentrafficsim.road.gtu.lane.perception.mental.channel;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.DistancedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.mental.AbstractTask;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;

/**
 * Task demand for potential cut-in or cooperation. This is defined as the maximum value of {@code d*(1-s/x0)} where {@code d}
 * is lane change desire towards the ego lane of any leader in either the left or right adjacent lane, {@code s} is the distance
 * to the leader with lane change desire and {@code x0} is the look-ahead distance.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class ChannelTaskCooperation extends AbstractTask implements ChannelTask
{

    /** Distance discount range. */
    public static final ParameterTypeLength X0_D = ChannelMental.X0_D;

    /** Standard set of left and right cooperation task. */
    private static final Set<ChannelTask> SET = Set.of(new ChannelTaskCooperation(true), new ChannelTaskCooperation(false));

    /** Standard supplier that supplies instances for left and right cooperation task. */
    public static final Function<LanePerception, Set<ChannelTask>> SUPPLIER = (p) -> SET;

    /** Whether this task instance regards the left side. */
    private final boolean left;

    /**
     * Constructor.
     * @param left whether this task instance regards the left side.
     */
    private ChannelTaskCooperation(final boolean left)
    {
        super("cooperation");
        this.left = left;
    }

    @Override
    public String getId()
    {
        return this.left ? "cooperation (left)" : "cooperation (right)";
    }

    @Override
    public Object getChannel()
    {
        return this.left ? LEFT : RIGHT;
    }

    @Override
    public double calculateTaskDemand(final LanePerception perception)
    {
        NeighborsPerception neighbors = perception.getPerceptionCategoryOptional(NeighborsPerception.class)
                .orElseThrow(() -> new NoSuchElementException("NeighborsPerception not present."));
        Iterator<DistancedObject<LaneBasedGtu>> leaders =
                neighbors.getLeaders(this.left ? RelativeLane.LEFT : RelativeLane.RIGHT).underlyingWithDistance();
        ParameterTypeDouble param = this.left ? LmrsParameters.DRIGHT : LmrsParameters.DLEFT;
        Length x0 = Try.assign(() -> perception.getGtu().getParameters().getParameter(X0_D), "Parameter x0_d not present.");
        double dMax = 0.0;
        while (leaders.hasNext())
        {
            DistancedObject<LaneBasedGtu> leader = leaders.next();
            if (leader.distance().gt(x0))
            {
                break;
            }
            double d;
            try
            {
                d = leader.object().getParameters().getParameter(param) * (1.0 - leader.distance().si / x0.si);
                dMax = Math.max(dMax, d);
            }
            catch (ParameterException ex)
            {
                // leader does not provide lane change desire, ignore
            }
        }
        return Math.min(0.999, dMax);
    }

}
