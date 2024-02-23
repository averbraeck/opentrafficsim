package org.opentrafficsim.road.gtu.perception;

import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.core.gtu.perception.AbstractPerceptionCategory;
import org.opentrafficsim.road.ClassList;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */

public class VerifyPerceptionCategoryMethods
{

    /**
     * Check that all sub-classes of AbstractPerceptionCategory have for data named {@code TestField}:
     * <ul>
     * <li>{@code testField*} property of any type. Data may be organized e.g. per lane, so type is not forced to be
     * {@code TimeStampedObject}. Data may also be stored as e.g. {@code testFieldLeft} and {@code testFieldRight}, hence the
     * {@code *}. <br>
     * If the field is not found, wrapped {@code AbstractPerceptionCategory} fields are checked. If either has the property the
     * test succeeds. Methods should still be in place as forwards to the wrapped category.</li>
     * <li>{@code updateTestField} method</li>
     * <li>For boolean:
     * <ul>
     * <li>{@code isTestField} method returning {@code boolean}.</li>
     * <li>{@code isTestFieldTimeStamped} method returning {@code TimeStampedObject}.</li>
     * </ul>
     * </li>
     * <li>For non-boolean:
     * <ul>
     * <li>{@code getTestField} method <b>not</b> returning {@code void}.</li>
     * <li>{@code getTimeStampedTestField} method returning {@code TimeStampedObject}.</li>
     * </ul>
     * </li>
     * </ul>
     * These tests are performed whenever a public method with these naming patterns is encountered:
     * <ul>
     * <li>{@code is*} (boolean).</li>
     * <li>{@code is*TimeStamped} (boolean).</li>
     * <li>{@code get*} (non-boolean).</li>
     * <li>{@code getTimeStamped*} (non-boolean).</li>
     * </ul>
     * The {@code *} is subtracted as field name, with first character made upper or lower case as by convention.
     */
    @Test
    public final void perceptionCategoryTest()
    {
        // TODO: to what extent do we want to prescribe this now that we have more flexible perception categories
        Collection<Class<?>> classList = ClassList.classList("org.opentrafficsim", true);
        for (Class<?> c : classList)
        {
            if (AbstractPerceptionCategory.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers()))
            {
                Set<String> fieldsDone = new LinkedHashSet<>();
                List<String> fieldNames = new ArrayList<>();
                List<String> methodNames = new ArrayList<>();
                List<Class<?>> methodReturnTypes = new ArrayList<>();
                for (Field field : c.getDeclaredFields())
                {
                    fieldNames.add(field.getName());
                }
                for (Method method : c.getMethods())
                {
                    methodNames.add(method.getName());
                    methodReturnTypes.add(method.getReturnType());
                }
                for (Method method : c.getDeclaredMethods())
                {
                    if (Modifier.isPrivate(method.getModifiers()))
                    {
                        continue;
                    }
                    String name = method.getName();
                    String field = null;
                    boolean isBoolean = false;
                    if (name.startsWith("is") && name.endsWith("TimeStamped"))
                    {
                        field = name.substring(2, name.length() - 11);
                        isBoolean = true;
                    }
                    else if (name.startsWith("is"))
                    {
                        field = name.substring(2);
                        isBoolean = true;
                    }
                    else if (name.startsWith("getTimeStamped"))
                    {
                        field = name.substring(14);
                    }
                    else if (name.startsWith("get"))
                    {
                        field = name.substring(3);
                    }

                    if (field != null)
                    {
                        String fieldDown = field.substring(0, 1).toLowerCase() + field.substring(1);
                        if (!fieldsDone.contains(fieldDown))
                        {
                            String fieldUp = field.substring(0, 1).toUpperCase() + field.substring(1);
                            if (isBoolean)
                            {
                                testGetter(c, fieldNames, methodNames, methodReturnTypes, fieldDown, "is" + fieldUp,
                                        "is" + fieldUp + "TimeStamped", "update" + fieldUp);
                            }
                            else
                            {
                                testGetter(c, fieldNames, methodNames, methodReturnTypes, fieldDown, "get" + fieldUp,
                                        "getTimeStamped" + fieldUp, "update" + fieldUp);
                            }
                            fieldsDone.add(fieldDown);
                        }
                    }

                }
            }
        }
    }

    /**
     * @param c class that is checked, subclass of AbstractPerceptionCategory
     * @param fieldNames field names of c
     * @param methodNames method names of c
     * @param methodReturnTypes return types of methods of c
     * @param field field that should be present
     * @param getter regular getter/is method that should be present
     * @param timeStampedGetter time stamped getter/is method that should be present
     * @param updater update method that should be present
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private void testGetter(final Class<?> c, final List<String> fieldNames, final List<String> methodNames,
            final List<Class<?>> methodReturnTypes, final String field, final String getter, final String timeStampedGetter,
            final String updater)
    {
        boolean fieldFound = false;
        int i = 0;
        while (!fieldFound && i < fieldNames.size())
        {
            fieldFound = fieldNames.get(i).startsWith(field);
            i++;
        }
        if (!fieldFound)
        {
            // perhaps the perception category wraps another category
            Field[] fields = c.getDeclaredFields();
            i = 0;
            while (!fieldFound && i < fields.length)
            {
                if (AbstractPerceptionCategory.class.isAssignableFrom(fields[i].getType()))
                {
                    // check if this wrapped category has the right field
                    Field[] wrappedFields = fields[i].getType().getDeclaredFields();
                    int j = 0;
                    while (!fieldFound && j < wrappedFields.length)
                    {
                        fieldFound = wrappedFields[j].getName().startsWith(field);
                        j++;
                    }
                }
                i++;
            }
        }
        if (!fieldFound)
        {
            // System.out.println("Class " + c.getSimpleName() + " does not have a field '" + field + "'.");
            // TODO: fail("Class " + c + " does not have a field '" + field + "*', nor wraps a perception category that does.");
        }
        if (methodNames.contains(getter))
        {
            if (getter.startsWith("is") && !methodReturnTypes.get(methodNames.indexOf(getter)).equals(boolean.class))
            {
                fail("Class " + c + "'s method '" + getter + "' does not return a boolean.");
            }
            else if (methodReturnTypes.get(methodNames.indexOf(getter)).equals(void.class))
            {
                fail("Class " + c + "'s method '" + getter + "' does not return anything.");
            }
            // System.out.println("Class " + c.getSimpleName() + "'s method " + getter + " has correct return type.");
        }
        else
        {
            fail("Class " + c + " does not contain a method '" + getter + "'.");
        }
        if (methodNames.contains(timeStampedGetter))
        {
            if (!methodReturnTypes.get(methodNames.indexOf(timeStampedGetter)).equals(TimeStampedObject.class))
            {
                fail("Class " + c + "'s method '" + timeStampedGetter + "' does not return a TimeStampedObject.");
            }
            // System.out
            // .println("Class " + c.getSimpleName() + "'s method " + timeStampedGetter + " has correct return type.");
        }
        else
        {
            // Accept that no time-stamped method is present
            // System.err.println("Class " + c + " does not contain a method '" + timeStampedGetter + "'.");
            // TODO: fail...
        }
        if (!methodNames.contains(updater))
        {
            // System.out.println("Class " + c.getSimpleName() + " does not contain a method '" + updater + "'.");
            // System.err.print("Class " + c + " does not contain a method '" + updater + "'.");
            // TODO: fail...
        }
    }

    /**
     * @param args arguments
     */
    public static void main(final String[] args)
    {
        VerifyPerceptionCategoryMethods t = new VerifyPerceptionCategoryMethods();
        t.perceptionCategoryTest();
    }

}
