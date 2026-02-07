package org.opentrafficsim.road;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterType;

import io.github.classgraph.ClassGraph;

/**
 * Loops all OTS classes and prints XML lines for all parameters (@code ParameterType) found.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
        Locale.setDefault(Locale.US);
        Set<ParameterType<?>> done = new LinkedHashSet<>();

        Collection<Class<?>> classList = new ClassGraph().acceptPackages("org.opentrafficsim").scan().getAllClasses().stream()
                .map((ci) -> ci.loadClass()).collect(Collectors.toSet());
        Map<String, ParameterType<?>> parameters = new TreeMap<>(); // sort by field (package.class.field)
        for (Class<?> clazz : classList)
        {
            for (Field field : clazz.getDeclaredFields())
            {
                if (Modifier.isStatic(field.getModifiers()) && field.canAccess(null))
                {
                    Object fieldValue = field.get(null);
                    if (fieldValue instanceof ParameterType && !done.contains(fieldValue))
                    {
                        ParameterType<?> parameter = (ParameterType<?>) fieldValue;
                        String fld = field.getDeclaringClass().getName() + "." + field.getName();
                        if (parameters.containsValue(parameter))
                        {
                            // Find field under which it is stored. Replace if this new field is shorter.
                            // Shorter = heuristic for a better place to defined the parameter with.
                            String f = parameters.entrySet().stream().filter((e) -> e.getValue().equals(parameter)).findFirst()
                                    .get().getKey();
                            if (f.length() > fld.length())
                            {
                                parameters.remove(f);
                                parameters.put(fld, parameter);
                            }
                        }
                        else
                        {
                            parameters.put(fld, parameter);
                        }
                    }
                }
            }
        }

        for (Entry<String, ParameterType<?>> entry : parameters.entrySet())
        {
            ParameterType<?> parameter = entry.getValue();
            String fld = entry.getKey();

            String id = parameter.getId();
            String description = parameter.getDescription();

            String valueTypeName = parameter.getValueClass().getSimpleName();
            if (parameter.hasDefaultValue())
            {
                String value = parameter.getDefaultValue().toString();
                System.out.println(String.format("  <ots:%s Id=\"%s\" Description=\"%s\" Field=\"%s\" Default=\"%s\" />",
                        valueTypeName, id, description, fld, value));
            }
            else
            {
                System.out.println(String.format("  <ots:%s Id=\"%s\" Description=\"%s\" Field=\"%s\" />", valueTypeName, id,
                        description, fld));
            }
        }
    }

}
