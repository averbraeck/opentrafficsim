package org.opentrafficsim.road.network.sampling.data;

import java.util.Optional;
import java.util.function.Predicate;

import org.opentrafficsim.kpi.sampling.data.ExtendedDataNumber;
import org.opentrafficsim.road.gtu.perception.mental.Mental;
import org.opentrafficsim.road.gtu.perception.mental.channel.ChannelFuller;
import org.opentrafficsim.road.gtu.perception.mental.channel.ChannelTask;
import org.opentrafficsim.road.network.conflict.Conflict;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Attention trajectory data, specific to channels that a predicate accepts. Static fields are available for attention
 * trajectory data on the default perception channels.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AttentionData extends ExtendedDataNumber<GtuDataRoad>
{

    /** Attention on front channel. */
    public static final AttentionData FRONT = new AttentionData("front", (c) -> ChannelTask.FRONT.equals(c));

    /** Attention on rear channel. */
    public static final AttentionData REAR = new AttentionData("rear", (c) -> ChannelTask.REAR.equals(c));

    /** Attention on left channel. */
    public static final AttentionData LEFT = new AttentionData("left", (c) -> ChannelTask.LEFT.equals(c));

    /** Attention on right channel. */
    public static final AttentionData RIGHT = new AttentionData("right", (c) -> ChannelTask.RIGHT.equals(c));

    /** Attention on in-vehicle channel. */
    public static final AttentionData IN_VEHICLE = new AttentionData("in_vehicle", (c) -> ChannelTask.IN_VEHICLE.equals(c));

    /** Attention on conflicts. */
    public static final AttentionData CONFLICTS = new AttentionData("conflicts", (c) -> c instanceof Conflict);

    /** Channel predicate. */
    private final Predicate<Object> predicate;

    /**
     * Constructor.
     * @param id id
     * @param predicate channel predicate
     */
    public AttentionData(final String id, final Predicate<Object> predicate)
    {
        super(id, "attention_" + id);
        this.predicate = predicate;
    }

    @Override
    public Optional<Float> getValue(final GtuDataRoad gtu)
    {
        if (gtu.getGtu().getStrategicalPlanner() != null)
        {
            Optional<Mental> mental = gtu.getGtu().getTacticalPlanner().getPerception().getMental();
            if (mental.isPresent() && mental.get() instanceof ChannelFuller fuller)
            {
                double result = 0.0f;
                for (Object channel : fuller.getChannels())
                {
                    if (this.predicate.test(channel))
                    {
                        result += fuller.getAttention(channel);
                    }
                }
                return Optional.of((float) result);
            }
        }
        return Optional.empty();
    }
}
