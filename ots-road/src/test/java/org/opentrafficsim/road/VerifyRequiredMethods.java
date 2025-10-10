package org.opentrafficsim.road;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfoList;

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
        Collection<ClassInfo> classList =
                new ClassGraph().acceptPackages("org.opentrafficsim").ignoreClassVisibility().ignoreFieldVisibility().scan()
                        .getAllClasses().stream().filter((ci) -> !ci.isInterface()).filter((ci) -> !isAnonymousInnerClass(ci))
                        .filter((ci) -> !Exception.class.isAssignableFrom(ci.loadClass())).collect(Collectors.toSet());
        for (ClassInfo ci : classList)
        {
            Method toStringMethod = null;
            Class<?> c = ci.loadClass();
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
                if (isFinal(toStringMethod))
                {
                    System.out.println("Class " + c.getName() + " can not override the toString method because a super "
                            + "class implements a final toString method");
                }
                else if (!hasNonStaticFields(c))

                {
                    System.out.println("Class " + c.getName()
                            + " does not have to override the toString method because it does not have non-static fields");
                }
                else if (ci.isAbstract())
                {
                    System.out.println("Class " + c.getName() + " does not have to override the toString method because "
                            + "it is an abstract class");
                }
                else if (ci.isEnum())
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
        Collection<ClassInfo> classList =
                new ClassGraph().acceptPackages("org.opentrafficsim").ignoreClassVisibility().ignoreFieldVisibility().scan()
                        .getAllClasses().stream().filter((ci) -> !ci.isInterface()).collect(Collectors.toSet());
        for (ClassInfo ci : classList)
        {
            Class<?> c = ci.loadClass();
            if (Serializable.class.isAssignableFrom(c))
            {
                if (ci.isEnum())
                {
                    // System.out.println("Class " + c.getName() + " is an enum and (by inheritance) implements Serializable");
                }
                else if (!hasNonStaticFields(c))
                {
                    System.err.println("Class " + ci.getName()
                            + " does not contain non-static fields and should NOT implement Serializable");
                }
                else if (Thread.class.isAssignableFrom(c))
                {
                    System.err.println("Class " + ci.getName() + " is a thread and should NOT implement Serializable");
                }
                else if (isAnonymousInnerClass(ci))
                {
                    System.err.println(
                            "Class " + ci.getName() + " is an anonymous inner class and should NOT implement Serializable");
                }
                else if (Exception.class.isAssignableFrom(c))
                {
                    System.out.println("Class " + ci.getName() + " is an Exception and (correctly) implements Serializable");
                }
                else
                {
                    // System.out.println("Class " + c.getName() + " should (and does) implement Serializable");
                }
            }
            else
            {
                if (ci.isEnum())
                {
                    System.err.println(
                            "Class " + ci.getName() + " is an enum and should (by inheritence) implement Serializable");
                }
                else if (!hasNonStaticFields(c))
                {
                    // System.out.println("Class " + c.getName()
                    // + " does not contain non-static fields and (correctly) does not implement Serializable");
                }
                else if (Thread.class.isAssignableFrom(c))
                {
                    // System.out.println("Class " + c.getName() +
                    // " is a thread and (correctly) does not implement Serializable");
                }
                else if (isAnonymousInnerClass(ci))
                {
                    // System.out.println("Class " + c.getName()
                    // + " is an anonymous inner class and (correctly) does not implement Serializable");
                }
                else if (Exception.class.isAssignableFrom(c))
                {
                    System.err.println(
                            "Class " + ci.getName() + " is an Exception and should (but does NOT) implement Serializable");
                }
                else
                {
                    System.err.println("Class " + ci.getName() + " should (but does NOT) implement Serializable");
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
        Collection<ClassInfo> classList = new ClassGraph().acceptPackages("org.opentrafficsim").enableMethodInfo().scan()
                .getAllClasses().stream().filter((ci) -> !ci.isInterface())
                .filter((ci) -> !Exception.class.isAssignableFrom(ci.loadClass())).collect(Collectors.toSet());
        for (ClassInfo ci : classList)
        {
            MethodInfoList equalsMethod = ci.getDeclaredMethodInfo("equals");
            MethodInfoList hashCodeMethod = ci.getDeclaredMethodInfo("hashCode");
            if (equalsMethod.size() == 0)
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
            else if (hashCodeMethod.size() == 0)
            {
                fail("Class " + ci.getName() + " implements equals but NOT hashCode");
            }
            else
            {
                // System.out.println("Class " + c.getName() + " implements equals and hashCode (good)");
            }
        }
    }

    /**
     * Returns whether the method is final.
     * @param method method
     * @return whether the method is final
     */
    private static boolean isFinal(final Method method)
    {
        return Arrays.asList(Modifier.toString(method.getModifiers()).split(" ")).contains("final");
    }

    /**
     * Returns whether the class is an anonymous inner class. Note that {@code ClassInfo.isAnonymousInnerClass()} incorrectly
     * identifies locally defined classes with a name as anonymous. This method therefore additionally requires the first
     * character in the class name to no be a digit.
     * @param ci class ifno
     * @return whether the class is an anonymous inner class
     */
    private static boolean isAnonymousInnerClass(final ClassInfo ci)
    {
        return ci.isAnonymousInnerClass() && !Character.isDigit(ci.getSimpleName().charAt(0));
    }

    /**
     * Report if a class has non-static fields.
     * @param c the class
     * @return true if the class has non-static fields
     */
    private static boolean hasNonStaticFields(final Class<?> c)
    {
        // Cannot use ClassInfo as it does not load super classes not within "org.opentrafficsim"
        for (Field f : c.getDeclaredFields())
        {
            if (!Modifier.isStatic(f.getModifiers()))
            {
                return true;
            }
        }
        if (c.equals(Object.class))
        {
            return false;
        }
        return hasNonStaticFields(c.getSuperclass());
    }

}
