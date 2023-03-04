package org.opentrafficsim.kpi.sampling.data;

import java.util.Arrays;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.SamplingException;

/**
 * Class for unitless values.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <G> gtu data type
 */
public abstract class ExtendedDataNumber<G extends GtuData> extends ExtendedDataType<Float, float[], float[], G>
{

    /**
     * Constructor.
     * @param id String; id
     * @param description String; description
     */
    public ExtendedDataNumber(final String id, final String description)
    {
        super(id, description, Float.class);
    }

    /** {@inheritDoc} */
    @Override
    public float[] setValue(final float[] storage, final int index, final Float value)
    {
        float[] out;
        if (index == storage.length)
        {
            int cap = (index - 1) + ((index - 1) >> 1);
            out = Arrays.copyOf(storage, cap);
        }
        else
        {
            out = storage;
        }
        out[index] = value;
        return out;
    }

    /** {@inheritDoc} */
    @Override
    public Float getOutputValue(final float[] output, final int index) throws SamplingException
    {
        return output[index];
    }

    /** {@inheritDoc} */
    @Override
    public Float getStorageValue(final float[] storage, final int index) throws SamplingException
    {
        Throw.when(index < 0 || index >= storage.length, SamplingException.class, "Index %d out of range.", index);
        return storage[index];
    }

    /** {@inheritDoc} */
    @Override
    public float[] initializeStorage()
    {
        return new float[10];
    }

    /** {@inheritDoc} */
    @Override
    public float[] convert(final float[] storage, final int size)
    {
        return Arrays.copyOf(storage, size);
    }

    /** {@inheritDoc} */
    @Override
    public Float parseValue(final String string)
    {
        return Float.valueOf(string);
    }

}
