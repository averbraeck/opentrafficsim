package org.opentrafficsim.base.parameters.constraint;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Constraint that checks whether the value is any of a given collection of classes, where each class is a sub class of a given
 * type.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 30 jun. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> super type for all possible classes, e.g. TacticalPlanner
 */
public final class ClassConstraint<T> extends CollectionConstraint<Class<? extends T>>
{

    /**
     * @param classes Collection&lt;Class&lt;? extends T&gt;&gt;; acceptable classes
     */
    private ClassConstraint(final Collection<Class<? extends T>> classes)
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
    public static <T> ClassConstraint<T> newInstance(final Class<? extends T>... objs)
    {
        Collection<Class<? extends T>> collection = new LinkedHashSet<>();
        for (Class<? extends T> clazz : objs)
        {
            collection.add(clazz);
        }
        return new ClassConstraint<>(collection);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "ClassConstraint [classes=" + super.objects + "]";
    }

}
