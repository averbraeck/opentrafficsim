package org.opentrafficsim.xml.bindings.types;

import java.lang.reflect.Field;

import org.djutils.reflection.ClassUtil;

/**
 * Expression type with Field value.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class FieldType extends ExpressionType<Field>
{

    /** */
    private static final long serialVersionUID = 20251111L;

    /** Function to convert output from expression to the right type. */
    private static final SerializableFunction<Object, Field> TO_TYPE = SerializableFunction.of(Field.class, FieldType::valueOf);

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public FieldType(final Field value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public FieldType(final String expression)
    {
        super(expression, TO_TYPE);
    }

    /**
     * Parses {@code String} to to the right type.
     * @param str input string
     * @return parsed output
     */
    public static Field valueOf(final String str)
    {
        int dot = str.lastIndexOf(".");
        String className = str.substring(0, dot);
        String fieldName = str.substring(dot + 1);
        try
        {
            return ClassUtil.resolveField(Class.forName(className), fieldName);
        }
        catch (NoSuchFieldException | ClassNotFoundException exception)
        {
            throw new IllegalArgumentException("Unable to parse Field " + str);
        }
    }

}
