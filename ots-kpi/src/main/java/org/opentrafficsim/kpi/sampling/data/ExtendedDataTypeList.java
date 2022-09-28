package org.opentrafficsim.kpi.sampling.data;

import java.util.ArrayList;
import java.util.List;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.GtuDataInterface;
import org.opentrafficsim.kpi.sampling.SamplingException;

/**
 * Extended data type for anything that can be captured in a list. Typically, these are non-numeric objects.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 21 mrt. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> type of value
 * @param <G> gtu data type
 */
public abstract class ExtendedDataTypeList<T, G extends GtuDataInterface> extends ExtendedDataType<T, List<T>, List<T>, G>
{

    /**
     * Constructor setting the id.
     * @param id String; id
     * @param type Class&lt;T&gt;; type class
     */
    public ExtendedDataTypeList(final String id, final Class<T> type)
    {
        super(id, type);
    }

    /** {@inheritDoc} */
    @Override
    public T getOutputValue(final List<T> output, final int i) throws SamplingException
    {
        Throw.when(i < 0 || i >= output.size(), SamplingException.class, "Index %d out of range.", i);
        return output.get(i);
    }

    /** {@inheritDoc} */
    @Override
    public T getStorageValue(final List<T> output, final int i) throws SamplingException
    {
        // same format for lists
        return getOutputValue(output, i);
    }

    /** {@inheritDoc} */
    @Override
    public List<T> setValue(final List<T> storage, final int index, final T value)
    {
        if (index == storage.size())
        {
            storage.add(value);
        }
        else
        {
            storage.set(index, value);
        }
        return storage;
    }

    /** {@inheritDoc} */
    @Override
    public List<T> initializeStorage()
    {
        return new ArrayList<>();
    }

    /** {@inheritDoc} */
    @Override
    public List<T> convert(final List<T> storage, final int size)
    {
        return storage;
    }

}
