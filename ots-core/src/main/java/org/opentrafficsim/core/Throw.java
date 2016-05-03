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
     * Throw an Throwable (such as an Exception or Error) if a condition is met, e.g. for pre- and postcondition checking. Use
     * as follows: <br>
     * <code>Throw.when(value == null, GTUException.class, "value cannot be null");</code>
     * @param condition the condition to check; an exception will be thrown if this is <b>true</b>
     * @param throwableClass the Throwable type to throw
     * @param message the message to use in the exception
     * @throws Throwable the throwable to throw on true condition
     * @param <T> the Throwable type
     */
    public static <T extends Throwable> void when(final boolean condition, final Class<T> throwableClass, final String message)
            throws T
    {
        if (condition)
        {
            throwMessage(throwableClass, message, new ArrayList<Object>());
        }
    }

    /**
     * Throw an Throwable (such as an Exception or Error) if a condition is met, e.g. for pre- and postcondition checking. Use
     * as follows: <br>
     * <code>Throw.when(value == null, GTUException.class, "value cannot be null for GTU with id = %s", id);</code>
     * @param condition the condition to check; an exception will be thrown if this is <b>true</b>
     * @param throwableClass the Throwable type to throw
     * @param message the message to use in the exception
     * @param arg1 value to use for the formatting identifiers
     * @throws Throwable the throwable to throw on true condition
     * @param <T> the Throwable type
     */
    public static <T extends Throwable> void when(final boolean condition, final Class<T> throwableClass, final String message,
            final Object arg1) throws T
    {
        if (condition)
        {
            List<Object> argList = new ArrayList<>();
            argList.add(arg1);
            throwMessage(throwableClass, message, argList);
        }
    }

    /**
     * Throw an Throwable (such as an Exception or Error) if a condition is met, e.g. for pre- and postcondition checking. Use
     * as follows: <br>
     * <code>Throw.when(value == null, GTUException.class, "value cannot be null for GTU %s with id %s", name, id);</code>
     * @param condition the condition to check; an exception will be thrown if this is <b>true</b>
     * @param throwableClass the Throwable type to throw
     * @param message the message to use in the exception
     * @param arg1 1st value to use for the formatting identifiers
     * @param arg2 2nd value to use for the formatting identifiers
     * @throws Throwable the throwable to throw on true condition
     * @param <T> the Throwable type
     */
    public static <T extends Throwable> void when(final boolean condition, final Class<T> throwableClass, final String message,
            final Object arg1, final Object arg2) throws T
    {
        if (condition)
        {
            List<Object> argList = new ArrayList<>();
            argList.add(arg1);
            argList.add(arg2);
            throwMessage(throwableClass, message, argList);
        }
    }

    /**
     * Throw an Throwable (such as an Exception or Error) if a condition is met, e.g. for pre- and postcondition checking. Use
     * as follows: <br>
     * <code>Throw.when(value == null, GTUException.class, "value cannot be null for GTU %s with id %s", name, id);</code>
     * @param condition the condition to check; an exception will be thrown if this is <b>true</b>
     * @param throwableClass the Throwable type to throw
     * @param message the message to use in the exception
     * @param arg1 1st value to use for the formatting identifiers
     * @param arg2 2nd value to use for the formatting identifiers
     * @param arg3 3rd value to use for the formatting identifiers
     * @throws Throwable the throwable to throw on true condition
     * @param <T> the Throwable type
     */
    public static <T extends Throwable> void when(final boolean condition, final Class<T> throwableClass, final String message,
            final Object arg1, final Object arg2, final Object arg3) throws T
    {
        if (condition)
        {
            List<Object> argList = new ArrayList<>();
            argList.add(arg1);
            argList.add(arg2);
            argList.add(arg3);
            throwMessage(throwableClass, message, argList);
        }
    }

    /**
     * Throw an Throwable (such as an Exception or Error) if a condition is met, e.g. for pre- and postcondition checking. Use
     * as follows: <br>
     * <code>Throw.when(value == null, GTUException.class, "value cannot be null for GTU with id = %s", id);</code>
     * @param condition the condition to check; an exception will be thrown if this is <b>true</b>
     * @param throwableClass the Throwable type to throw
     * @param message the message to use in the exception, with potential formatting identifiers
     * @param arg1 1st value to use for the formatting identifiers
     * @param arg2 2nd value to use for the formatting identifiers
     * @param arg3 3rd value to use for the formatting identifiers
     * @param args potential 4th and further values to use for the formatting identifiers
     * @throws Throwable the throwable to throw on true condition
     * @param <T> the Throwable type
     */
    public static <T extends Throwable> void when(final boolean condition, final Class<T> throwableClass, final String message,
            final Object arg1, final Object arg2, final Object arg3, final Object... args) throws T
    {
        if (condition)
        {
            List<Object> argList = new ArrayList<>();
            argList.add(arg1);
            argList.add(arg2);
            argList.add(arg3);
            argList.addAll(Arrays.asList(args));
            throwMessage(throwableClass, message, argList);
        }
    }

    /**
     * Private method to handle the throwing an Exception, Throwable or Error.
     * @param throwableClass the Throwable type to throw
     * @param message the message to use in the exception, with potential formatting identifiers
     * @param argList List with potential values to use for the formatting identifiers
     * @throws Throwable the throwable to throw
     * @param <T> the Throwable type
     */
    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void throwMessage(final Class<T> throwableClass, final String message,
            final List<Object> argList) throws T
    {
        // create a clear message
        List<StackTraceElement> steList = new ArrayList<StackTraceElement>(Arrays.asList(new Throwable().getStackTrace()));
        steList.remove(0); // remove the throwMessage(...) call
        steList.remove(0); // remove the when(...) call
        StackTraceElement[] ste = steList.toArray(new StackTraceElement[steList.size()]);
        String where = ste[0].getClassName() + "." + ste[0].getMethodName() + " (" + ste[0].getLineNumber() + "): ";
        Object[] args = argList.toArray();
        String formattedMessage;
        try
        {
            formattedMessage = where + String.format(message, args);
        }
        catch (IllegalFormatException exception)
        {
            formattedMessage = where + message + " [FormatException; args=" + argList + "]";
        }

        // throw exceptions that cannot be thrown as a result of instantiating through reflection.
        if (throwableClass.equals(NoSuchMethodException.class))
        {
            T exception = (T) new NoSuchMethodException(formattedMessage);
            exception.setStackTrace(ste);
            throw exception;
        }
        if (throwableClass.equals(InstantiationException.class))
        {
            T exception =  (T) new InstantiationException(formattedMessage);
            exception.setStackTrace(ste);
            throw exception;
        }
        if (throwableClass.equals(IllegalAccessException.class))
        {
            T exception =  (T) new IllegalAccessException(formattedMessage);
            exception.setStackTrace(ste);
            throw exception;
        }
        if (throwableClass.equals(IllegalArgumentException.class))
        {
            T exception =  (T) new IllegalArgumentException(formattedMessage);
            exception.setStackTrace(ste);
            throw exception;
        }
        if (throwableClass.equals(InvocationTargetException.class))
        {
            T exception =  (T) new InvocationTargetException(new Exception(formattedMessage));
            exception.setStackTrace(ste);
            throw exception;
        }
        
        // throw all other excheptions through reflection 
        try
        {
            Constructor<T> constructor =
                    (Constructor<T>) ClassUtil.resolveConstructor(throwableClass, new Class<?>[] { String.class });
            T exception = constructor.newInstance(formattedMessage);
            exception.setStackTrace(ste);
            throw exception;
        }
        catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException exception3)
        {
            RuntimeException rte = new RuntimeException(formattedMessage);
            rte.setStackTrace(ste);
            throw rte;
        }
    }
}
