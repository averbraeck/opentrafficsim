package org.opentrafficsim.road.network.factory.xml.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Parser - Utility class for parsing using JAXB generated classes.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public final class ParseUtil
{
    /** */
    private ParseUtil()
    {
        // utility class
    }

    /**
     * Returns all objects of given type from the list of all objects.
     * @param objectList List&lt;?&gt;; list of objects
     * @param clazz Class&lt;T&gt;; class of type of objects to return
     * @param <T> type
     * @return list of all objects of given type from the list of all objects
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getObjectsOfType(final List<?> objectList, final Class<T> clazz)
    {
        List<T> list = new ArrayList<>();
        for (Object object : objectList)
        {
            if (clazz.isAssignableFrom(object.getClass()))
            {
                list.add((T) object);
            }
        }
        return list;
    }

    /**
     * Select object of given type by predicate.
     * @param objectList List&lt;?&gt;; list of objects
     * @param clazz Class&lt;T&gt;; class of type of objects to return
     * @param predicate Predicate&lt;T&gt;; predicate
     * @param <T> type
     * @return (first) object of given type that matches the predicate
     */
    public static <T> T findObject(final List<?> objectList, final Class<T> clazz, final Predicate<T> predicate)
    {
        for (Object object : objectList)
        {
            if (clazz.isAssignableFrom(object.getClass()))
            {
                @SuppressWarnings("unchecked")
                T t = (T) object;
                if (predicate.test(t))
                {
                    return t;
                }
            }
        }
        throw new RuntimeException(String.format("Object of type %s could not be found.", clazz));
    }

    /**
     * Select object of given type by predicate.
     * @param objectList List&lt;T&gt;; list of objects
     * @param predicate Predicate&lt;T&gt;; predicate
     * @param <T> type
     * @return (first) object of given type that matches the predicate
     */
    public static <T> T findObject(final List<T> objectList, final Predicate<T> predicate)
    {
        for (T object : objectList)
        {
            if (predicate.test(object))
            {
                return object;
            }
        }
        throw new RuntimeException("Object with predicate not be found.");
    }
}
