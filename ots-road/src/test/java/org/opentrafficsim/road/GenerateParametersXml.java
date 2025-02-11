package org.opentrafficsim.road;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterType;

/**
 * Loops all OTS classes and prints XML lines for all parameters (@code ParameterType) found.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class GenerateParametersXml
{

    /**
     * Constructor.
     */
    private GenerateParametersXml()
    {
        //
    }

    /**
     * Prints XML lines for all parameters (@code ParameterType) found.
     * @param args args.
     * @throws IllegalAccessException exception
     * @throws IllegalArgumentException exception
     * @throws ParameterException exception
     */
    public static void main(final String[] args) throws IllegalArgumentException, IllegalAccessException, ParameterException
    {
        Locale.setDefault(new Locale("NL-nl"));
        Set<ParameterType<?>> done = new LinkedHashSet<>();
        for (Class<?> clazz : ClassList.classList("org.opentrafficsim", false))
        {
            for (Field field : clazz.getDeclaredFields())
            {
                if (Modifier.isStatic(field.getModifiers()) && field.canAccess(null))
                {
                    Object fieldValue = field.get(null);
                    if (fieldValue instanceof ParameterType && !done.contains(fieldValue))
                    {
                        ParameterType<?> parameter = (ParameterType<?>) fieldValue;
                        String id = parameter.getId();
                        String description = parameter.getDescription();
                        String fld = field.getDeclaringClass().getName() + "." + field.getName();
                        if (parameter.hasDefaultValue())
                        {
                            String value = parameter.getDefaultValue().toString();
                            System.out.println(
                                    String.format("  <ots:Length Id=\"%s\" Description=\"%s\" Field=\"%s\" Default=\"%s\" />",
                                            id, description, fld, value));
                        }
                        else
                        {
                            System.out.println(String.format("  <ots:Length Id=\"%s\" Description=\"%s\" Field=\"%s\" />", id,
                                    description, fld));
                        }
                        done.add(parameter);
                    }
                }
            }
        }
    }

}
