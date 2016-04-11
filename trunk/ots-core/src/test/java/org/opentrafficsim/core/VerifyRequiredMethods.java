package org.opentrafficsim.core;

import static org.junit.Assert.fail;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;

import org.junit.Test;

/**
 * Verify that all classes have a toString method.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 11, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class VerifyRequiredMethods
{

    /**
     * Determine if a class is an anonymous inner class.
     * @param c Class; the class to check
     * @return boolean; true if <cite>c</cite> is an anonymous inner class; false otherwise
     */
    public final boolean isAnonymousInnerClass(final Class<?> c)
    {
        String className = c.getName();
        int pos = className.lastIndexOf("$");
        if (pos > 0)
        {
            while (++pos < className.length())
            {
                if (!Character.isDigit(className.charAt(pos)))
                {
                    break;
                }
            }
            if (pos >= className.length())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Check that all classes have a toString method.
     */
    @Test
    public final void toStringTest()
    {
        Collection<Class<?>> classList = ClassList.classList("org.opentrafficsim", true);
        for (Class<?> c : classList)
        {
            if (Exception.class.isAssignableFrom(c))
            {
                continue;
            }
            if (isAnonymousInnerClass(c))
            {
                continue;
            }
            Method toStringMethod = null;
            boolean allStatic = c.getConstructors().length == 0;
            for (Method m : c.getDeclaredMethods())
            {
                if (m.getName().equals("toString") && m.getParameterCount() == 0)
                {
                    toStringMethod = m;
                }
                if (allStatic)
                {
                    boolean isStatic = false;
                    for (String modifierString : Modifier.toString(m.getModifiers()).split(" "))
                    {
                        if ("static".equals(modifierString))
                        {
                            isStatic = true;
                            break;
                        }
                    }
                    if (!isStatic)
                    {
                        allStatic = false;
                    }
                }
            }
            if (null == toStringMethod && !allStatic)
            {
                System.out.println("Class " + c.getName() + " does not have a toString method" + " (modifiers: "
                        + Modifier.toString(c.getModifiers()) + ")");
            }
            else if (null == toStringMethod)
            {
                System.out.println("Class " + c.getName() + " does not need a toString method because all methods are static");
            }
        }
    }

    /**
     * Check that all classes implement the Serializable interface.
     */
    @Test
    public final void serializableTest()
    {
        Collection<Class<?>> classList = ClassList.classList("org.opentrafficsim", true);
        for (Class<?> c : classList)
        {
            if (Exception.class.isAssignableFrom(c))
            {
                continue;
            }
            boolean allStatic = c.getConstructors().length == 0;
            for (Method m : c.getDeclaredMethods())
            {
                if (allStatic)
                {
                    boolean isStatic = false;
                    for (String modifierString : Modifier.toString(m.getModifiers()).split(" "))
                    {
                        if ("static".equals(modifierString))
                        {
                            isStatic = true;
                            break;
                        }
                    }
                    if (!isStatic)
                    {
                        allStatic = false;
                    }
                }
            }
            if (!Serializable.class.isAssignableFrom(c) && !allStatic && !isAnonymousInnerClass(c))
            {
                System.out.println("Class " + c.getName() + " does not implement Serializable");
            }
            // else
            // {
            // System.out.println("Class " + c.getName() + " implements Serializable");
            // }
        }
    }

    /**
     * Check that all classes that implement equals also implement hashCode.
     */
    @Test
    public final void equalsAndHashCodeTest()
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
