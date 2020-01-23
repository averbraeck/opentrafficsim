package org.opentrafficsim.base.parameters.constraint;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.djutils.exceptions.Throw;

/**
 * Constraint that checks whether a value is in a given constraint collection.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 10 sep. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
