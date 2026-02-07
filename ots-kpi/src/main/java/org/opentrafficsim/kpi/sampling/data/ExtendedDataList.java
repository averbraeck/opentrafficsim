package org.opentrafficsim.kpi.sampling.data;

import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.kpi.interfaces.GtuData;

/**
 * Extended data type for anything that can be captured in a list. Typically, these are non-numeric objects.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> type of value
 * @param <G> GTU data type
 */
public abstract class ExtendedDataList<T, G extends GtuData> extends ExtendedDataType<T, List<T>, List<T>, G>
{

    /**
     * Constructor.
     * @param id id
     * @param description description
     * @param type type class
     */
    public ExtendedDataList(final String id, final String description, final Class<T> type)
    {
        super(id, description, type);
    }

    @Override
    public T getOutputValue(final List<T> output, final int i)
    {
        return output.get(i);
    }

    @Override
    public T getStorageValue(final List<T> output, final int i)
    {
        // same format for lists
        return getOutputValue(output, i);
    }

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

    @Override
    public List<T> initializeStorage()
    {
        return new ArrayList<>();
    }

    @Override
    public List<T> convert(final List<T> storage, final int size)
    {
        return storage.subList(0, size);
    }

}
