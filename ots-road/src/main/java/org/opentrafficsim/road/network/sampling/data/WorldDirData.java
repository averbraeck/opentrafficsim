package org.opentrafficsim.road.network.sampling.data;

import java.util.Optional;

import org.djunits.unit.DirectionUnit;
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
    public Optional<FloatDirection> getValue(final GtuDataRoad gtu)
    {
        return Optional.ofNullable(convertValue((float) gtu.getGtu().getLocation().dirZ));
    }

    @Override
    protected FloatDirection convertValue(final float value)
    {
        return FloatDirection.ofSI(value);
    }

    @Override
    protected FloatDirectionVector convert(final float[] storage)
    {
        return new FloatDirectionVector(storage);
    }

    @Override
    public FloatDirection parseValue(final String string)
    {
        return FloatDirection.ofSI(Float.valueOf(string));
    }

}
