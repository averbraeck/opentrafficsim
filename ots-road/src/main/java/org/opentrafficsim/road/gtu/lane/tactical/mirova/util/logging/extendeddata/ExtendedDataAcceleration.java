package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;


import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataFloat;
import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vfloat.scalar.FloatAcceleration;
import org.djunits.value.vfloat.vector.FloatAccelerationVector;

/**
 * Extended data type for Acceleration values.
 * @param <G> GTU data type
 */
public abstract class ExtendedDataAcceleration<G extends GtuData> extends ExtendedDataFloat<AccelerationUnit, FloatAcceleration, FloatAccelerationVector, G>
{

    /**
     * Constructor setting the id.
     * @param id id
     * @param description description
     */
    public ExtendedDataAcceleration(final String id, final String description)
    {
        super(id, description, FloatAcceleration.class);
    }

    @Override
    protected final FloatAcceleration convertValue(final float value)
    {
        return FloatAcceleration.instantiateSI(value);
    }

    @Override
    protected final FloatAccelerationVector convert(final float[] storage)
    {
        return new FloatAccelerationVector(storage, AccelerationUnit.SI);
    }

    @Override
    public FloatAcceleration interpolate(final FloatAcceleration value0, final FloatAcceleration value1, final double f)
    {
        return FloatAcceleration.interpolate(value0, value1, (float) f);
    }

    @Override
    public FloatAcceleration parseValue(final String string)
    {
        return FloatAcceleration.instantiateSI(Float.valueOf(string));
    }

}
