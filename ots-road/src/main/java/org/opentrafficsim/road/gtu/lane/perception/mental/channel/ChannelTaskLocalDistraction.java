package org.opentrafficsim.road.gtu.lane.perception.mental.channel;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.UnderlyingDistance;
import org.opentrafficsim.road.gtu.lane.perception.categories.LocalDistractionPerception;
import org.opentrafficsim.road.gtu.lane.perception.mental.AbstractTask;
import org.opentrafficsim.road.network.lane.object.LocalDistraction;

/**
 * Task demand from local distraction.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ChannelTaskLocalDistraction extends AbstractTask implements ChannelTask
{

    /** Channel key. */
    private final Object channel;

    /** Side of road in driving direction of distractions to include. */
    private final LateralDirectionality side;

    /** Standard set of left, right and front local distraction task. */
    private static final Set<ChannelTask> SET = Set.of(new ChannelTaskLocalDistraction(LEFT, LateralDirectionality.LEFT),
            new ChannelTaskLocalDistraction(RIGHT, LateralDirectionality.RIGHT),
            new ChannelTaskLocalDistraction(FRONT, LateralDirectionality.NONE));

    /** Standard supplier that supplies instances for left, right and front local distraction task. */
    public static final Function<LanePerception, Set<ChannelTask>> SUPPLIER = (p) -> SET;

    /**
     * Constructor.
     * @param channel channel key
     * @param side side of road in driving direction of distractions to include
     */
    public ChannelTaskLocalDistraction(final Object channel, final LateralDirectionality side)
    {
        super("local-distraction");
        this.channel = channel;
        this.side = side;
    }

    @Override
    public String getId()
    {
        return "local_distraction_(" + this.side + ")";
    }

    @Override
    public Object getChannel()
    {
        return this.channel;
    }

    @Override
    public double calculateTaskDemand(final LanePerception perception) throws ParameterException
    {
        double td = 0.0;
        try
        {
            Iterator<UnderlyingDistance<LocalDistraction>> distractions = perception
                    .getPerceptionCategory(LocalDistractionPerception.class).getDistractions().underlyingWithDistance();
            while (distractions.hasNext())
            {
                UnderlyingDistance<LocalDistraction> distraction = distractions.next();
                if (distraction.distance().le(distraction.object().getRange()) && distraction.distance().ge0()
                        && distraction.object().getSide().equals(this.side))
                {
                    td = Math.max(td, distraction.object().getDistractionLevel());
                }
            }
        }
        catch (OperationalPlanException ex)
        {
            throw new OtsRuntimeException(ex);
        }
        return td;
    }

}
