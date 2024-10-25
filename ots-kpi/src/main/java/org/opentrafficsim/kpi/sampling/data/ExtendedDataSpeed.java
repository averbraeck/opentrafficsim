package org.opentrafficsim.kpi.sampling.data;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.vfloat.scalar.FloatSpeed;
import org.djunits.value.vfloat.vector.FloatSpeedVector;
import org.opentrafficsim.kpi.interfaces.GtuData;

/**
 * Extended data type for speed values.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <G> gtu data type
 */
public abstract class ExtendedDataSpeed<G extends GtuData> extends ExtendedDataFloat<SpeedUnit, FloatSpeed, FloatSpeedVector, G>
{

    /**
     * Constructor setting the id.
     * @param id id
     * @param description description
     */
    public ExtendedDataSpeed(final String id, final String description)
    {
        super(id, description, FloatSpeed.class);
    }

    @Override
    protected final FloatSpeed convertValue(final float value)
    {
        return FloatSpeed.instantiateSI(value);
    }

    @Override
    protected final FloatSpeedVector convert(final float[] storage) throws ValueRuntimeException
    {
        return new FloatSpeedVector(storage, SpeedUnit.SI);
    }

    @Override
    public FloatSpeed interpolate(final FloatSpeed value0, final FloatSpeed value1, final double f)
    {
        return FloatSpeed.interpolate(value0, value1, (float) f);
    }

    @Override
    public FloatSpeed parseValue(final String string)
    {
        return FloatSpeed.instantiateSI(Float.valueOf(string));
    }

}
