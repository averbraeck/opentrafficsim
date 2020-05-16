package org.opentrafficsim.kpi.sampling.data;

import org.djunits.unit.DurationUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vfloat.scalar.FloatDuration;
import org.djunits.value.vfloat.vector.FloatDurationVector;
import org.djunits.value.vfloat.vector.base.FloatVector;
import org.opentrafficsim.kpi.interfaces.GtuDataInterface;

/**
 * Extended data type for duration values.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 21 mrt. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <G> gtu data type
 */
public abstract class ExtendedDataTypeDuration<G extends GtuDataInterface>
        extends ExtendedDataTypeFloat<DurationUnit, FloatDuration, FloatDurationVector, G>
{

    /**
     * Constructor setting the id.
     * @param id String; id
     */
    public ExtendedDataTypeDuration(final String id)
    {
        super(id, FloatDuration.class);
    }

    /** {@inheritDoc} */
    @Override
    protected final FloatDuration convertValue(final float value)
    {
        return FloatDuration.instantiateSI(value);
    }

    /** {@inheritDoc} */
    @Override
    protected final FloatDurationVector convert(final float[] storage) throws ValueRuntimeException
    {
        return FloatVector.instantiate(storage, DurationUnit.SI, StorageType.DENSE);
    }

    /** {@inheritDoc} */
    @Override
    public FloatDuration interpolate(final FloatDuration value0, final FloatDuration value1, final double f)
    {
        return FloatDuration.interpolate(value0, value1, (float) f);
    }

    /** {@inheritDoc} */
    @Override
    public FloatDuration parseValue(final String string)
    {
        return FloatDuration.instantiateSI(Float.valueOf(string));
    }
    
}
