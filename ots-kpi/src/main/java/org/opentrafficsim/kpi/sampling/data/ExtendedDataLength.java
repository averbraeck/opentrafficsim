package org.opentrafficsim.kpi.sampling.data;

import org.djunits.unit.LengthUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vfloat.scalar.FloatLength;
import org.djunits.value.vfloat.vector.FloatLengthVector;
import org.djunits.value.vfloat.vector.base.FloatVector;
import org.opentrafficsim.kpi.interfaces.GtuData;

/**
 * Extended data type for length values.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <G> gtu data type
 */
public abstract class ExtendedDataLength<G extends GtuData>
        extends ExtendedDataFloat<LengthUnit, FloatLength, FloatLengthVector, G>
{

    /**
     * Constructor setting the id.
     * @param id String; id
     * @param description String; description
     */
    public ExtendedDataLength(final String id, final String description)
    {
        super(id, description, FloatLength.class);
    }

    /** {@inheritDoc} */
    @Override
    protected final FloatLength convertValue(final float value)
    {
        return FloatLength.instantiateSI(value);
    }

    /** {@inheritDoc} */
    @Override
    protected final FloatLengthVector convert(final float[] storage) throws ValueRuntimeException
    {
        return FloatVector.instantiate(storage, LengthUnit.SI, StorageType.DENSE);
    }

    /** {@inheritDoc} */
    @Override
    public FloatLength interpolate(final FloatLength value0, final FloatLength value1, final double f)
    {
        return FloatLength.interpolate(value0, value1, (float) f);
    }

    /** {@inheritDoc} */
    @Override
    public FloatLength parseValue(final String string)
    {
        return FloatLength.instantiateSI(Float.valueOf(string));
    }

}
