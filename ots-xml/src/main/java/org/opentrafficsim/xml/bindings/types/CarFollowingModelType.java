package org.opentrafficsim.xml.bindings.types;

import java.util.function.BiFunction;

import org.opentrafficsim.road.gtu.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.tactical.following.DesiredHeadwayModel;
import org.opentrafficsim.road.gtu.tactical.following.DesiredSpeedModel;
import org.opentrafficsim.road.gtu.tactical.following.Idm;
import org.opentrafficsim.road.gtu.tactical.following.IdmPlus;

/**
 * Type for car-following models based on {@link DesiredHeadwayModel} and {@link DesiredSpeedModel}.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class CarFollowingModelType extends ExpressionType<BiFunction<DesiredHeadwayModel, DesiredSpeedModel, CarFollowingModel>>
{

    /** Serialization version UID. */
    private static final long serialVersionUID = 20260219L;

    /** Function to convert output from expression to the right type. */
    @SuppressWarnings("unchecked")
    public static final SerializableFunction<Object,
            BiFunction<DesiredHeadwayModel, DesiredSpeedModel, CarFollowingModel>> TO_TYPE = SerializableFunction.of(
                    (Class<BiFunction<DesiredHeadwayModel, DesiredSpeedModel, CarFollowingModel>>) (Class<?>) BiFunction.class,
                    CarFollowingModelType::valueOf);

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public CarFollowingModelType(final BiFunction<DesiredHeadwayModel, DesiredSpeedModel, CarFollowingModel> value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public CarFollowingModelType(final String expression)
    {
        super(expression, TO_TYPE);
    }

    /**
     * Parses {@code String} to to the right type.
     * @param str input string
     * @return parsed output
     */
    public static BiFunction<DesiredHeadwayModel, DesiredSpeedModel, CarFollowingModel> valueOf(final String str)
    {
        if ("idm".equalsIgnoreCase(str))
        {
            return Idm::new;
        }
        if ("idm_plus".equalsIgnoreCase(str))
        {
            return IdmPlus::new;
        }
        throw new IllegalArgumentException("Unable to parse car-following model " + str);
    }

}
