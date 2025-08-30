package org.opentrafficsim.road.gtu;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.gtu.Stateless;

/**
 * Tests that all classes that extend {@code Stateless} have no non-final or non-static fields.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class StatelessTest
{

    /**
     * Tests that classes with {@code @Stateless} are stateless.
     */
    @Test
    public void testStateless()
    {
        Collection<Class<?>> classes = ClassList.classList("org.opentrafficsim", true);
        for (Class<?> clazz : classes)
        {
            if (Stateless.class.isAssignableFrom(clazz))
            {
                for (Field field : clazz.getDeclaredFields())
                {
                    assertTrue(Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers()), "Field '"
                            + field.getName() + "' is not final or static in stateless class " + clazz.getSimpleName());
                }
            }
        }
    }

}
