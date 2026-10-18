package org.opentrafficsim.base.collections;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Implementation of {@link SmallSet} using an internal {@link LinkedHashSet} if required. This is useful for cases
 * where the set rarely contains more than 2 elements, but may contain a lot of elements in those rare cases.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 * @param <T> type of elements
 */
public class SmallLinkedHashSet<T> extends SmallSet<T>
{

    @Override
    protected Collection<T> getEmptyCollection()
    {
        return new LinkedHashSet<>(3);
    }

}