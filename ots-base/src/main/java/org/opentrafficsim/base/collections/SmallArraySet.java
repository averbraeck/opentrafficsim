package org.opentrafficsim.base.collections;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Implementation of {@link SmallSet} using an internal {@link ArrayList} if required. This is useful for cases where
 * the set rarely contains more than 2 elements, but is then likely to not have many more elements.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 * @param <T> type of elements
 */
public class SmallArraySet<T> extends SmallSet<T>
{

    @Override
    protected Collection<T> getEmptyCollection()
    {
        return new ArrayList<>(3);
    }

    @Override
    public boolean add(final T element)
    {
        // maintain uniqueness
        if (contains(element))
        {
            return false;
        }
        return super.add(element);
    }

}