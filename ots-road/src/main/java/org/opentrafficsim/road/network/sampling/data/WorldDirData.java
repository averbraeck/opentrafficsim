package org.opentrafficsim.road.network.sampling.data;

import org.djunits.unit.DirectionUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.vfloat.scalar.FloatDirection;
import org.djunits.value.vfloat.vector.FloatDirectionVector;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataFloat;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Extended data type in sampler to record world direction.
 * @author wjschakel
 */
public class WorldDirData extends ExtendedDataFloat<DirectionUnit, FloatDirection, FloatDirectionVector, GtuDataRoad>
{

    /**
     * Constructor.
     */
    public WorldDirData()
    {
        super("WorldDir", "World direction", FloatDirection.class);
    }

    @Override
    public FloatDirection getValue(final GtuDataRoad gtu)
    {
        return convertValue((float) gtu.getGtu().getLocation().dirZ);
    }

    @Override
    protected FloatDirection convertValue(final float value)
    {
        return FloatDirection.instantiateSI(value);
    }

    @Override
    protected FloatDirectionVector convert(final float[] storage) throws ValueRuntimeException
    {
        return new FloatDirectionVector(storage);
    }

    @Override
    public FloatDirection parseValue(final String string)
    {
        return FloatDirection.instantiateSI(Float.valueOf(string));
    }

}
