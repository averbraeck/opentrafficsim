package org.opentrafficsim.kpi.sampling.data;

import org.djunits.unit.DurationUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.vfloat.scalar.FloatDuration;
import org.djunits.value.vfloat.vector.FloatDurationVector;
import org.opentrafficsim.kpi.interfaces.GtuData;

/**
 * Extended data type for duration values.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <G> gtu data type
 */
public abstract class ExtendedDataDuration<G extends GtuData>
        extends ExtendedDataFloat<DurationUnit, FloatDuration, FloatDurationVector, G>
{

    /**
     * Constructor setting the id.
     * @param id id
     * @param description description
     */
    public ExtendedDataDuration(final String id, final String description)
    {
        super(id, description, FloatDuration.class);
    }

    @Override
    protected final FloatDuration convertValue(final float value)
    {
        return FloatDuration.instantiateSI(value);
    }

    @Override
    protected final FloatDurationVector convert(final float[] storage) throws ValueRuntimeException
    {
        return new FloatDurationVector(storage, DurationUnit.SI);
    }

    @Override
    public FloatDuration interpolate(final FloatDuration value0, final FloatDuration value1, final double f)
    {
        return FloatDuration.interpolate(value0, value1, (float) f);
    }

    @Override
    public FloatDuration parseValue(final String string)
    {
        return FloatDuration.instantiateSI(Float.valueOf(string));
    }

}
