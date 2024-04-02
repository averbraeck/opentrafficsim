package org.opentrafficsim.base.parameters.constraint;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.djutils.exceptions.Throw;

/**
 * Constraint that checks whether a value is in a given constraint collection.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> value type
 */
public class CollectionConstraint<T> implements Constraint<T>
{

    /** Acceptable objects. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final Collection<T> objects;

    /**
     * @param objects Collection&lt;T&gt;; acceptable objects
     */
    public CollectionConstraint(final Collection<T> objects)
    {
        Throw.whenNull(objects, "Collection of acceptable objects may not be null.");
        this.objects = objects;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public boolean accept(final T value)
    {
        return this.objects.contains(value);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String failMessage()
    {
        return "Value of parameter '%s' is not in the collection of acceptable values.";
    }

    /**
     * Creates a new instance with given objects.
     * @param objs T...; acceptable objects
     * @param <T> type
     * @return new instance with given objects
     */
    @SafeVarargs
    public static <T> CollectionConstraint<T> newInstance(final T... objs)
    {
        Collection<T> collection = new LinkedHashSet<>();
        for (T t : objs)
        {
            collection.add(t);
        }
        return new CollectionConstraint<>(collection);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "CollectionConstraint [objects=" + this.objects + "]";
    }

}
