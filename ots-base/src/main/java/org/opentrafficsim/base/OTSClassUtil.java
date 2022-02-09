package org.opentrafficsim.base;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for class operations.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
