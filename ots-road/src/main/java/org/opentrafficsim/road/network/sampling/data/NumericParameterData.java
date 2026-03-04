package org.opentrafficsim.road.network.sampling.data;

import java.util.Optional;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.ParameterTypeNumeric;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataNumber;
import org.opentrafficsim.kpi.sampling.filter.FilterDataType;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Trajectory data for a numerical parameter type. Typically this involves a parameter that represents an internal model state,
 * i.e. one that is variable over time. For static parameters a {@link FilterDataType} is more suitable.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class NumericParameterData extends ExtendedDataNumber<GtuDataRoad>
{

    /** Parameter type. */
    private final ParameterTypeNumeric<? extends Number> parameterType;

    /**
     * Constructor.
     * @param parameterType parameter type
     */
    public NumericParameterData(final ParameterTypeNumeric<? extends Number> parameterType)
    {
        super("parameter_" + Throw.whenNull(parameterType, "parameterType").getId(), "Parameter " + parameterType.getId());
        this.parameterType = parameterType;
    }

    @Override
    public Optional<Float> getValue(final GtuDataRoad gtu)
    {
        Optional<? extends Number> value = gtu.getGtu().getParameters().getOptionalParameter(this.parameterType);
        if (value.isPresent())
        {
            return Optional.of(value.get().floatValue());
        }
        return Optional.empty();
    }

}
