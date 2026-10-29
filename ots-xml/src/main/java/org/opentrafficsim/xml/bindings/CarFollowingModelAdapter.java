package org.opentrafficsim.xml.bindings;

import org.opentrafficsim.road.gtu.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.tactical.following.DesiredHeadwayModel;
import org.opentrafficsim.road.gtu.tactical.following.DesiredSpeedModel;
import org.opentrafficsim.road.gtu.tactical.lmrs.LmrsFactory.CarFollowingModelSupplier;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.Relaxation;
import org.opentrafficsim.xml.bindings.types.CarFollowingModelType;

/**
 * Adapter for car-following models based on {@link DesiredHeadwayModel} and {@link DesiredSpeedModel}.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class CarFollowingModelAdapter extends ExpressionAdapter<
        CarFollowingModelSupplier<DesiredHeadwayModel, DesiredSpeedModel, Relaxation, CarFollowingModel>, CarFollowingModelType>
{

    @Override
    public CarFollowingModelType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            return new CarFollowingModelType(trimBrackets(field));
        }
        return new CarFollowingModelType(CarFollowingModelType.valueOf(field));
    }

}
