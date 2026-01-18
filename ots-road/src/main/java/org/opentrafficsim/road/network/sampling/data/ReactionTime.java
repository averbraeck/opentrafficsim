package org.opentrafficsim.road.network.sampling.data;

import java.util.Optional;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vfloat.scalar.FloatDuration;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataDuration;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Leader id in trajectory information.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ReactionTime extends ExtendedDataDuration<GtuDataRoad>
{

    /**
     * Constructor.
     */
    public ReactionTime()
    {
        super("Tr", "Reaction time");
    }

    @Override
    public Optional<FloatDuration> getValue(final GtuDataRoad gtu)
    {
        try
        {
            return Optional.ofNullable(
                    new FloatDuration(gtu.getGtu().getParameters().getParameter(ParameterTypes.TR).getSI(), DurationUnit.SI));
        }
        catch (ParameterException exception)
        {
            return Optional.ofNullable(FloatDuration.NaN);
        }
    }

}
