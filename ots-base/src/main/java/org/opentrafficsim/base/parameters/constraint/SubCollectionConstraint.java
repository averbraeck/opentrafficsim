package org.opentrafficsim.base.parameters.constraint;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.djutils.exceptions.Throw;

/**
 * Constraint that checks whether a collection (the parameter value) is a subset of a constraint collection.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> object type
 */
public class SubCollectionConstraint<T> implements Constraint<Collection<T>>
{

    /** Acceptable objects. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final Collection<T> objects;

    /**
     * Constructor.
     * @param objects acceptable objects
     */
    public SubCollectionConstraint(final Collection<T> objects)
    {
        Throw.whenNull(objects, "Collection of acceptable objects may not be null.");
        this.objects = objects;
    }

    @Override
    public boolean accept(final Collection<T> value)
    {
        return this.objects.containsAll(value);
    }

    @Override
    public String failMessage()
    {
        return "Value of parameter '%s' contains value(s) not in the collection of acceptable values.";
    }

    /**
     * Creates a new instance with given collection.
     * @param objs acceptable objects
     * @param <T> type
     * @return new instance with given collection
     */
    @SafeVarargs
    public static <T> SubCollectionConstraint<T> newInstance(final T... objs)
    {
        Collection<T> collection = new LinkedHashSet<>();
        for (T t : objs)
        {
            collection.add(t);
        }
        return new SubCollectionConstraint<>(collection);
    }

    @Override
    public String toString()
    {
        return "SubCollectionConstraint [objects=" + this.objects + "]";
    }

}
