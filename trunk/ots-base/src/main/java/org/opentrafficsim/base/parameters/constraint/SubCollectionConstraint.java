package org.opentrafficsim.base.parameters.constraint;

import java.util.Collection;
import java.util.HashSet;

import nl.tudelft.simulation.language.Throw;

/**
 * Constraint that checks whether a collection (the parameter value) is a subset of a constraint collection.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 10 sep. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> object type
 */
public class SubCollectionConstraint<T> implements Constraint<Collection<T>>
{

    /** Acceptable objects. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final Collection<T> objects;

    /**
     * @param objects acceptable objects
     */
    public SubCollectionConstraint(final Collection<T> objects)
    {
        Throw.whenNull(objects, "Collection of acceptable objects may not be null.");
        this.objects = objects;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public boolean accept(final Collection<T> value)
    {
        return this.objects.containsAll(value);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
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
        Collection<T> collection = new HashSet<>();
        for (T t : objs)
        {
            collection.add(t);
        }
        return new SubCollectionConstraint<>(collection);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "SubCollectionConstraint [objects=" + this.objects + "]";
    }

}
