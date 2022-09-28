package org.opentrafficsim.kpi.sampling.data;

import java.util.Arrays;

import org.djunits.unit.Unit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.vfloat.scalar.base.AbstractFloatScalar;
import org.djunits.value.vfloat.vector.base.AbstractFloatVector;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.GtuDataInterface;
import org.opentrafficsim.kpi.sampling.SamplingException;

/**
 * Class to facilitate JUNITS types in extended data.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 21 mrt. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <U> unit
 * @param <T> type in vector
 * @param <O> vector type
 * @param <G> gtu data type
 */
public abstract class ExtendedDataTypeFloat<U extends Unit<U>, T extends AbstractFloatScalar<U, T>,
        O extends AbstractFloatVector<U, T, O>, G extends GtuDataInterface> extends ExtendedDataType<T, O, float[], G>
{
    /**
     * Constructor setting the id.
     * @param id String; id
     * @param type Class&lt;T&gt;; type class
     */
    public ExtendedDataTypeFloat(final String id, final Class<T> type)
    {
        super(id, type);
    }

    /** {@inheritDoc} */
    @Override
    public final float[] setValue(final float[] storage, final int i, final T value)
    {
        float[] out;
        if (i == storage.length)
        {
            int cap = (i - 1) + ((i - 1) >> 1);
            out = Arrays.copyOf(storage, cap);
        }
        else
        {
            out = storage;
        }
        out[i] = value.si;
        return out;
    }

    /** {@inheritDoc} */
    @Override
    public final float[] initializeStorage()
    {
        return new float[10];
    }

    /** {@inheritDoc} */
    @Override
    public final String formatValue(final String format, final T value)
    {
        return String.format(format, value.getSI());
    }

    /** {@inheritDoc} */
    @Override
    public final T getOutputValue(final O output, final int i) throws SamplingException
    {
        try
        {
            return output.get(i);
        }
        catch (ValueRuntimeException exception)
        {
            throw new SamplingException("Index out of range.", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public T getStorageValue(final float[] storage, final int i) throws SamplingException
    {
        Throw.when(i < 0 || i >= storage.length, SamplingException.class, "Index %d out of range.", i);
        return convertValue(storage[i]);
    }

    /**
     * Convert float to typed value.
     * @param value float; float value
     * @return typed value
     */
    protected abstract T convertValue(float value);

    /** {@inheritDoc} */
    @Override
    public O convert(final float[] storage, final int size)
    {
        try
        {
            // cut array to size and delegate
            return convert(Arrays.copyOf(storage, size));
        }
        catch (ValueRuntimeException exception)
        {
            throw new RuntimeException("Could not create typed vector from float array.", exception);
        }
    }

    /**
     * Convert float array to typed array.
     * @param storage float[]; float array storage
     * @return typed array
     * @throws ValueRuntimeException when float array cannot be converted
     */
    protected abstract O convert(float[] storage) throws ValueRuntimeException;

}
