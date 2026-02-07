package org.opentrafficsim.road.gtu.lane.perception.mental.channel;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;
import org.opentrafficsim.road.gtu.lane.perception.mental.Mental;

/**
 * Interface for mental modules that implement perception channels.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface ChannelMental extends Mental
{

    /** Distance discount. */
    ParameterTypeLength X0_D =
            new ParameterTypeLength("x0_d", "Distance discount range", Length.ofSI(119.98), NumericConstraint.POSITIVEZERO);

    /**
     * Returns the perception delay belonging to a perception channel.
     * @param obj object that is a channel key, or that is mapped to a channel key.
     * @return perception delay belonging to a perception channel.
     */
    Duration getPerceptionDelay(Object obj);

    /**
     * Returns the level of attention of a perception channel.
     * @param obj object that is a channel key, or that is mapped to a channel key.
     * @return level of attention of a perception channel.
     */
    double getAttention(Object obj);

    /**
     * Maps an object to a channel key.
     * @param obj object.
     * @param channel channel key.
     */
    void mapToChannel(Object obj, Object channel);

}
