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
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class CarFollowingModelType extends ExpressionType<BiFunction<DesiredHeadwayModel, DesiredSpeedModel, CarFollowingModel>>
{

    /** */
    private static final long serialVersionUID = 20260219L;

    /** Function to convert output from expression to the right type. */
    public static final SerializableFunction<Object,
            BiFunction<DesiredHeadwayModel, DesiredSpeedModel, CarFollowingModel>> TO_TYPE =
                    (o) -> "idm".equals(o.toString().toLowerCase()) ? Idm::new : IdmPlus::new;

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public CarFollowingModelType(final BiFunction<DesiredHeadwayModel, DesiredSpeedModel, CarFollowingModel> value)
    {
        super(value, TO_TYPE);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public CarFollowingModelType(final String expression)
    {
        super(expression, TO_TYPE);
    }

}
