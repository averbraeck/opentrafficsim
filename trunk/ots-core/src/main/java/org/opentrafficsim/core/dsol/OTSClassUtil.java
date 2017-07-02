package org.opentrafficsim.core.dsol;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for class operations.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 2 jul. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class OTSClassUtil
{

    /**
     * Empty private constructor.
     */
    private OTSClassUtil()
    {
        //
    }
    
    /**
     * @param object the object to provide the class for
     * @param <T> the type
     * @return the class of the object
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getTypedClass(final T object)
    {
        return (Class<T>) object.getClass();
    }
    
    /**
     * @param object the object to provide the class list for
     * @param <T> the type
     * @return the class of the object
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<List<T>> getTypedClassList(final T object)
    {
        List<T> list = new ArrayList<>();
        return (Class<List<T>>) (list).getClass();
    }
    
    /**
     * Create typed list from classes.
     * @param type type class
     * @param objs classes for in the list
     * @param <T> type class
     * @return list of classes
     */
    @SafeVarargs
    public static <T> List<Class<? extends T>> toTypedList(final Class<T> type, final Class<? extends T>... objs)
    {
        List<Class<? extends T>> list = new ArrayList<>();
        for (Class<? extends T> clazz : objs)
        {
            list.add(clazz);
        }
        return list;
    }
    
    /**
     * Create typed set from classes.
     * @param type type class
     * @param objs classes for in the set
     * @param <T> type class
     * @return set of classes
     */
    @SafeVarargs
    public static <T> Set<Class<? extends T>> toTypedSet(final Class<T> type, final Class<? extends T>... objs)
    {
        Set<Class<? extends T>> set = new HashSet<>();
        for (Class<? extends T> clazz : objs)
        {
            set.add(clazz);
        }
        return set;
    }
    
}
