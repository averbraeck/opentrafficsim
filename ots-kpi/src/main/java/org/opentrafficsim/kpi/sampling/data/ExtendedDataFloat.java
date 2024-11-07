package org.opentrafficsim.kpi.sampling.data;

import java.util.Arrays;

import org.djunits.unit.Unit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.vfloat.scalar.base.FloatScalar;
import org.djunits.value.vfloat.vector.base.FloatVector;
import org.opentrafficsim.kpi.interfaces.GtuData;

/**
 * Class to facilitate JUNITS types in extended data.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <U> unit
 * @param <T> type in vector
 * @param <O> output vector type
 * @param <G> GTU data type
 */
public abstract class ExtendedDataFloat<U extends Unit<U>, T extends FloatScalar<U, T>, O extends FloatVector<U, T, O>,
        G extends GtuData> extends ExtendedDataType<T, O, float[], G>
{
    /**
     * Constructor.
     * @param id id
     * @param description description
     * @param type type class
     */
    public ExtendedDataFloat(final String id, final String description, final Class<T> type)
    {
        super(id, description, type);
    }

    @Override
    public final float[] setValue(final float[] storage, final int i, final T value)
    {
        float[] out;
        if (i == storage.length)
        {
            int cap = Math.max(10, (i - 1) + ((i - 1) >> 1));
            out = Arrays.copyOf(storage, cap);
        }
        else
        {
            out = storage;
        }
        out[i] = value.si;
        return out;
    }

    @Override
    public final float[] initializeStorage()
    {
        return new float[10];
    }

    @Override
    public final T getOutputValue(final O output, final int i)
    {
        try
        {
            return output.get(i);
        }
        catch (ValueRuntimeException exception)
        {
            throw new IndexOutOfBoundsException("Index " + i + " is out of range for array of size " + output.size() + ".");
        }
    }

    @Override
    public T getStorageValue(final float[] storage, final int i)
    {
        return convertValue(storage[i]);
    }

    /**
     * Convert float to typed value.
     * @param value float value
     * @return typed value
     */
    protected abstract T convertValue(float value);

    @Override
    public O convert(final float[] storage, final int size)
    {
        // cut array to size and delegate
        return convert(Arrays.copyOf(storage, size));
    }

    /**
     * {@inheritDoc}
     * @param string stored string representation without unit
     */
    @Override
    public abstract T parseValue(String string);

    /**
     * Convert float array to typed array.
     * @param storage float array storage
     * @return typed array
     */
    protected abstract O convert(float[] storage);

}
