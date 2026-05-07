package org.opentrafficsim.cosim;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.djutils.exceptions.Throw;
import org.djutils.reflection.ClassUtil;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.road.gtu.perception.mental.AdaptationHeadway;
import org.opentrafficsim.road.gtu.perception.mental.AdaptationLaneChangeDesire;
import org.opentrafficsim.road.gtu.perception.mental.AdaptationSituationalAwareness;
import org.opentrafficsim.road.gtu.perception.mental.AdaptationSpeed;
import org.opentrafficsim.road.gtu.perception.mental.SumFuller;
import org.opentrafficsim.road.gtu.perception.mental.ar.ArFuller;
import org.opentrafficsim.road.gtu.perception.mental.ar.ArTaskCarFollowingExp;
import org.opentrafficsim.road.gtu.perception.mental.channel.AdaptationUpdateTime;
import org.opentrafficsim.road.gtu.perception.mental.channel.ChannelFuller;
import org.opentrafficsim.road.gtu.perception.mental.channel.ChannelTaskIntersection;
import org.opentrafficsim.road.gtu.perception.mental.channel.ChannelTaskLaneChange;
import org.opentrafficsim.road.gtu.perception.mental.channel.ChannelTaskScan;
import org.opentrafficsim.road.gtu.perception.mental.channel.ChannelTaskSignal;
import org.opentrafficsim.road.gtu.tactical.following.AbstractIdm;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.LmrsParameters;

/**
 * Supported parameters for co-simulation.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public final class Parameters
{

    /**
     * Constructor.
     */
    private Parameters()
    {
        //
    }

    /** Map of parameter types by their id. */
    public static final Map<String, ParameterType<?>> PARAMETER_MAP = new LinkedHashMap<>();

    /**
     * Add parameter type to map.
     * @param parameterType parameter type
     */
    private static void add(final ParameterType<?> parameterType)
    {
        PARAMETER_MAP.put(parameterType.getId(), parameterType);
    }

    /**
     * Add all parameter types of class.
     * @param clazz class with static parameter types
     */
    private static void add(final Class<?> clazz)
    {
        // add all parameter types using reflection
        Set<Field> fields = ClassUtil.getAllFields(clazz);

        for (Field field : fields)
        {
            if (ParameterType.class.isAssignableFrom(field.getType()))
            {
                try
                {
                    field.setAccessible(true);
                    add((ParameterType<?>) field.get(clazz));
                }
                catch (IllegalArgumentException | IllegalAccessException ex)
                {
                    // should not happen, field and clazz are related
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    static
    {
        add(ParameterTypes.class);
        add(AbstractIdm.class);
        add(LmrsParameters.class);
        add(ArFuller.class);
        add(SumFuller.class);
        add(ArTaskCarFollowingExp.class);
        add(AdaptationHeadway.class);
        add(AdaptationSpeed.class);
        add(AdaptationSituationalAwareness.class);
        add(AdaptationUpdateTime.class);
        add(AdaptationLaneChangeDesire.class);
        add(ChannelFuller.class);
        add(ChannelTaskScan.class);
        add(ChannelTaskSignal.class);
        add(ChannelTaskIntersection.class);
        add(ChannelTaskLaneChange.class);
        // remove status variables and old parameters
        PARAMETER_MAP.remove("ATT");
        PARAMETER_MAP.remove("f_est");
        PARAMETER_MAP.remove("TS");
        PARAMETER_MAP.remove("SA");
        PARAMETER_MAP.remove("dLaneChange");
        PARAMETER_MAP.remove("dLeft");
        PARAMETER_MAP.remove("dRight");
        PARAMETER_MAP.remove("lcDur");
        PARAMETER_MAP.remove("Look-back old");
        PARAMETER_MAP.remove("Look-back");
        PARAMETER_MAP.remove("T");
        PARAMETER_MAP.remove("dt");
        PARAMETER_MAP.remove("Tr");
        PARAMETER_MAP.remove("lambda_v");
    }

    /**
     * Get parameter type by id.
     * @param parameterTypeId parameter type id
     * @return parameter type
     */
    public static ParameterType<?> get(final String parameterTypeId)
    {
        Throw.when(!PARAMETER_MAP.containsKey(parameterTypeId), IllegalArgumentException.class,
                "Parameter %s is not supported.", parameterTypeId);
        return PARAMETER_MAP.get(parameterTypeId);
    }

}
