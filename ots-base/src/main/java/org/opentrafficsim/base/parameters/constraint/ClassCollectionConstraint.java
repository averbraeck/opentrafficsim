package org.opentrafficsim.base.parameters.constraint;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Constraint that checks whether a collection of classes is a sub collection of constraint collection.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 30 jun. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> class
 */
public final class ClassCollectionConstraint<T> extends SubCollectionConstraint<Class<? extends T>>
{

    /**
     * @param classes Collection&lt;Class&lt;? extends T&gt;&gt;; acceptable classes
     */
    private ClassCollectionConstraint(final Collection<Class<? extends T>> classes)
    {
        super(classes);
    }

    /**
     * Creates a new instance with given collection.
     * @param objs Class&lt;? extends T&gt;...; acceptable classes
     * @param <T> type class
     * @return new instance with given collection
     */
    @SafeVarargs
    public static <T> ClassCollectionConstraint<T> newInstance(final Class<? extends T>... objs)
    {
        Collection<Class<? extends T>> collection = new LinkedHashSet<>();
        for (Class<? extends T> clazz : objs)
        {
            collection.add(clazz);
        }
        return new ClassCollectionConstraint<>(collection);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "ClassCollectionConstraint [classes=" + this.objects + "]";
    }

}
