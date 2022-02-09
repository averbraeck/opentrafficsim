package org.opentrafficsim.kpi.sampling.data;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vfloat.scalar.FloatSpeed;
import org.djunits.value.vfloat.vector.FloatSpeedVector;
import org.djunits.value.vfloat.vector.base.FloatVector;
import org.opentrafficsim.kpi.interfaces.GtuDataInterface;

/**
 * Extended data type for speed values.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 21 mrt. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <G> gtu data type
 */
public abstract class ExtendedDataTypeSpeed<G extends GtuDataInterface>
        extends ExtendedDataTypeFloat<SpeedUnit, FloatSpeed, FloatSpeedVector, G>
{

    /**
     * Constructor setting the id.
     * @param id String; id
     */
    public ExtendedDataTypeSpeed(final String id)
    {
        super(id, FloatSpeed.class);
    }

    /** {@inheritDoc} */
    @Override
    protected final FloatSpeed convertValue(final float value)
    {
        return FloatSpeed.instantiateSI(value);
    }

    /** {@inheritDoc} */
    @Override
    protected final FloatSpeedVector convert(final float[] storage) throws ValueRuntimeException
    {
        return FloatVector.instantiate(storage, SpeedUnit.SI, StorageType.DENSE);
    }

    /** {@inheritDoc} */
    @Override
    public FloatSpeed interpolate(final FloatSpeed value0, final FloatSpeed value1, final double f)
    {
        return FloatSpeed.interpolate(value0, value1, (float) f);
    }
    
    /** {@inheritDoc} */
    @Override
    public FloatSpeed parseValue(final String string)
    {
        return FloatSpeed.instantiateSI(Float.valueOf(string));
    }

}
