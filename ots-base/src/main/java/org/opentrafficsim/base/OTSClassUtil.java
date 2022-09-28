package org.opentrafficsim.base;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for class operations.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
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
     * @param object T; the object to provide the class for
     * @param <T> the type
     * @return the class of the object
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getTypedClass(final T object)
    {
        return (Class<T>) object.getClass();
    }

    /**
     * @param object T; the object to provide the class list for
     * @param <T> the type
     * @return the class of the object
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<List<T>> getTypedClassList(final T object)
    {
        List<T> list = new ArrayList<>();
        return (Class<List<T>>) list.getClass();
    }

}
