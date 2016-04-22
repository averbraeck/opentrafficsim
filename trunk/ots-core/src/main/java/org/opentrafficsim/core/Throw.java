package org.opentrafficsim.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.List;

import nl.tudelft.simulation.language.reflection.ClassUtil;

/**
 * The Throw class has a number of static methods that make it easy to throw an exception under conditions for any Exception
 * class, including the standard Java exceptions and exceptions from libraries that are used in the project. Instead of:
 * 
 * <pre>
 * if (Double.isNaN(gtu.getPosition.si))
 * {
 *     throw new GTUException(&quot;position is NaN for GTU &quot; + gtu.getId());
 * }
 * </pre>
 * 
 * we can write:
 * 
 * <pre>
 * Throw.when(Double.isNaN(gtu.getPosition.si), GTUException.class, &quot;position is NaN for GTU %s&quot;, gtu.getId());
 * </pre>
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Apr 22, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class Throw
{
    /** private constructor for utility class. */
    private Throw()
    {
        // utility class
    }

    /**
     * Throw an Exception if a condition is met, e.g. for pre- and postcondition checking. Use e.g. as follows:<br>
     * <code>Throw.when(value == null, GTUException.class, "value cannot be null for GTU with id = %s", id);</code>
     * @param condition the condition to check; an exception will be thrown if this is <b>true</b>
     * @param exceptionClass the exception type to throw
     * @param message the message to use in the exception, with potential formatting identifiers
     * @param args potential values to use for the formatting identifiers
     * @throws Exception the exception to throw on true condition
     * @param <E> the Exception type
     */
    public static <E extends Exception> void when(final boolean condition, final Class<E> exceptionClass, final String message,
            final Object... args) throws E
    {
        if (condition)
        {
            List<StackTraceElement> steList = new ArrayList<StackTraceElement>(Arrays.asList(new Exception().getStackTrace()));
            steList.remove(0);
            StackTraceElement[] ste = steList.toArray(new StackTraceElement[steList.size()]);
            String where = ste[0].getClassName() + "." + ste[0].getMethodName() + " (" + ste[0].getLineNumber() + "): ";
            try
            {
                @SuppressWarnings("unchecked")
                Constructor<E> constructor =
                        (Constructor<E>) ClassUtil.resolveConstructor(exceptionClass, new Class<?>[] { String.class });
                try
                {
                    E exception = constructor.newInstance(where + String.format(message, args));
                    exception.setStackTrace(ste);
                    throw exception;
                }
                catch (IllegalFormatException exception)
                {
                    E exception2 =
                            constructor.newInstance(where + message + "[FormatException; args=" + Arrays.asList(args) + "]");
                    exception2.setStackTrace(ste);
                    throw exception2;
                }
            }
            catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException exception3)
            {
                RuntimeException rte = new RuntimeException(where + message + "[Exception; args=" + Arrays.asList(args) + "]");
                rte.setStackTrace(ste);
                throw rte;
            }
        }
    }
}
