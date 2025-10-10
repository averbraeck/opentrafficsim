package org.opentrafficsim.road.gtu;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.gtu.Stateless;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.FieldInfo;

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
     * Constructor.
     */
    public StatelessTest()
    {
        //
    }

    /**
     * Tests that classes with {@code @Stateless} are stateless.
     */
    @Test
    public void testStateless()
    {
        Collection<ClassInfo> classList =
                new ClassGraph().acceptPackages("org.opentrafficsim").ignoreClassVisibility().ignoreFieldVisibility().scan()
                        .getAllClasses().stream().filter((ci) -> !ci.isInterface()).collect(Collectors.toSet());
        for (ClassInfo ci : classList)
        {
            if (ci.extendsSuperclass(Stateless.class))
            {
                for (FieldInfo field : ci.getDeclaredFieldInfo())
                {
                    assertTrue(field.isFinal() || field.isStatic(),
                            "Field '" + field.getName() + "' is not final or static in stateless class " + ci.getSimpleName());
                }
            }
        }
    }

}
