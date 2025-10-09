package org.opentrafficsim.road;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;

import org.junit.jupiter.api.Test;

/**
 * Verify that all classes have a toString method (unless the class in non-instantiable, or an enum, or abstract. <br>
 * Verify that no class overrides equals without overriding hashCode. <br>
 * Verify that classes that can be instantiated are Serializable for those classes that this makes sense.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class VerifyRequiredMethods
{

    /** */
    private VerifyRequiredMethods()
    {
        // do not instantiate test class
    }

    /**
     * Check that all classes have a toString method.
     */
    @Test
    public void toStringTest()
    {
        Collection<Class<?>> classList = ClassList.classList("org.opentrafficsim", true);
        for (Class<?> c : classList)
        {
            if (Exception.class.isAssignableFrom(c))
            {
                continue;
            }
            if (ClassList.isAnonymousInnerClass(c))
            {
                continue;
            }
            Method toStringMethod = null;
            boolean isAbstract = false;
            for (String modifierString : Modifier.toString(c.getModifiers()).split(" "))
            {
                if (modifierString.equals("abstract"))
                {
                    isAbstract = true;
                    break;
                }
            }
            for (Method m : c.getDeclaredMethods())
            {
                if (m.getName().equals("toString") && m.getParameterCount() == 0)
                {
                    toStringMethod = m;
                }
            }
            if (null == toStringMethod)
            {
                // Get the nearest toString method from the parent tree
                try
                {
                    toStringMethod = c.getMethod("toString");
                }
                catch (NoSuchMethodException | SecurityException exception)
                {
                    exception.printStackTrace();
                    fail("Cannot happen: getMethod(\"toString\") should never fail - there is one in Object");
                }
                boolean isFinal = false;
                for (String modifierString : Modifier.toString(toStringMethod.getModifiers()).split(" "))
                {
                    if ("final".equals(modifierString))
                    {
                        isFinal = true;
                        break;
                    }
                }
                if (isFinal)
                {
                    System.out.println("Class " + c.getName() + " can not override the toString method because a super "
                            + "class implements a final toString method");
                }
                else if (!ClassList.hasNonStaticFields(c))

                {
                    System.out.println("Class " + c.getName()
                            + " does not have to override the toString method because it does not have non-static fields");
                }
                else if (isAbstract)
                {
                    System.out.println("Class " + c.getName() + " does not have to override the toString method because "
                            + "it is an abstract class");
                }
                else if (c.isEnum())
                {
                    System.out.println(
                            "Class " + c.getName() + " does not have to override toString because this class " + "is an enum");
                }
                else
                {
                    // fail("Class " + c.getName() + " does not (but should) override toString");
                    System.err.println("Class " + c.getName() + " does not (but should) override toString");
                }
            }
        }
    }

    /**
     * Check that all classes implement the Serializable interface.
     */
    @Test
    public void serializableTest()
    {
        Collection<Class<?>> classList = ClassList.classList("org.opentrafficsim", true);
        for (Class<?> c : classList)
        {
            if (Serializable.class.isAssignableFrom(c))
            {
                if (c.isEnum())
                {
                    // System.out.println("Class " + c.getName() + " is an enum and (by inheritance) implements Serializable");
                }
                else if (!ClassList.hasNonStaticFields(c))
                {
                    System.err.println("Class " + c.getName()
                            + " does not contain non-static fields and should NOT implement Serializable");
                }
                else if (Thread.class.isAssignableFrom(c))
                {
                    System.err.println("Class " + c.getName() + " is a thread and should NOT implement Serializable");
                }
                else if (ClassList.isAnonymousInnerClass(c))
                {
                    System.err.println(
                            "Class " + c.getName() + " is an anonymous inner class and should NOT implement Serializable");
                }
                else if (Exception.class.isAssignableFrom(c))
                {
                    System.out.println("Class " + c.getName() + " is an Exception and (correctly) implements Serializable");
                }
                else
                {
                    // System.out.println("Class " + c.getName() + " should (and does) implement Serializable");
                }
            }
            else
            {
                if (c.isEnum())
                {
                    System.err
                            .println("Class " + c.getName() + " is an enum and should (by inheritence) implement Serializable");
                }
                else if (!ClassList.hasNonStaticFields(c))
                {
                    // System.out.println("Class " + c.getName()
                    // + " does not contain non-static fields and (correctly does not implement Serializable");
                }
                else if (Thread.class.isAssignableFrom(c))
                {
                    // System.out.println("Class " + c.getName() +
                    // " is a thread and (correctly) does not implement Serializable");
                }
                else if (ClassList.isAnonymousInnerClass(c))
                {
                    // System.out.println("Class " + c.getName()
                    // + " is an anonymous inner class and (correctly) does not implement Serializable");
                }
                else if (Exception.class.isAssignableFrom(c))
                {
                    System.err.println(
                            "Class " + c.getName() + " is an Exception and should (but does NOT) implement Serializable");
                }
                else
                {
                    System.err.println("Class " + c.getName() + " should (but does NOT) implement Serializable");
                }
            }
        }
    }

    /**
     * Check that all classes that implement equals also implement hashCode.
     */
    @Test
    public void equalsAndHashCodeTest()
    {
        Collection<Class<?>> classList = ClassList.classList("org.opentrafficsim", true);
        for (Class<?> c : classList)
        {
            if (Exception.class.isAssignableFrom(c))
            {
                continue;
            }
            Method equalsMethod = null;
            Method hashCodeMethod = null;
            for (Method m : c.getDeclaredMethods())
            {
                if (m.getName().equals("equals"))
                {
                    equalsMethod = m;
                }
                else if (m.getName().equals("hashCode"))
                {
                    hashCodeMethod = m;
                }
            }
            if (null == equalsMethod)
            {
                if (null == hashCodeMethod)
                {
                    // System.out.println("Class " + c.getName() + " implements neither equals nor hashCode");
                }
                else
                {
                    // System.out.println("Class " + c.getName() + " implements hashCode, but not equals");
                }
            }
            else if (null == hashCodeMethod)
            {
                fail("Class " + c.getName() + " implements equals but NOT hashCode");
            }
            else
            {
                // System.out.println("Class " + c.getName() + " implements equals and hashCode (good)");
            }
        }
    }

}
