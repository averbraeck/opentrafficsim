package org.opentrafficsim.kpi.sampling.data;

import java.util.Arrays;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.GtuDataInterface;
import org.opentrafficsim.kpi.sampling.SamplingException;

/**
 * Class for unitless values.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 23 mei 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <G> gtu data type
 */
public abstract class ExtendedDataTypeNumber<G extends GtuDataInterface> extends ExtendedDataType<Float, float[], float[], G>
{

    /**
     * Constructor.
     * @param id String; id
     */
    public ExtendedDataTypeNumber(final String id)
    {
        super(id, Float.class);
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
    public String formatValue(final String format, final Float value)
    {
        return String.format(format, value);
    }

    /** {@inheritDoc} */
    @Override
    public Float parseValue(final String string)
    {
        return Float.valueOf(string);
    }

}
