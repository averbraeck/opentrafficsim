package org.opentrafficsim.kpi.sampling.data;

import java.util.Arrays;

import org.opentrafficsim.kpi.interfaces.GtuData;

/**
 * Class for unitless values.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <G> GTU data type
 */
public abstract class ExtendedDataNumber<G extends GtuData> extends ExtendedDataType<Float, float[], float[], G>
{

    /**
     * Constructor.
     * @param id id
     * @param description description
     */
    public ExtendedDataNumber(final String id, final String description)
    {
        super(id, description, Float.class);
    }

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

    @Override
    public Float getOutputValue(final float[] output, final int index)
    {
        return output[index];
    }

    @Override
    public Float getStorageValue(final float[] storage, final int index)
    {
        return storage[index];
    }

    @Override
    public float[] initializeStorage()
    {
        return new float[10];
    }

    @Override
    public float[] convert(final float[] storage, final int size)
    {
        return Arrays.copyOf(storage, size);
    }

    @Override
    public Float parseValue(final String string)
    {
        return Float.valueOf(string);
    }

}
