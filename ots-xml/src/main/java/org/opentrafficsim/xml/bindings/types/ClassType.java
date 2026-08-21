package org.opentrafficsim.xml.bindings.types;

/**
 * Expression type with Class value.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
@SuppressWarnings({"rawtypes", "serial"})
public class ClassType extends ExpressionType<Class>
{

    /** Function to convert output from expression to the right type. */
    private static final SerializableFunction<Object, Class> TO_TYPE = SerializableFunction.of(Class.class, ClassType::valueOf);

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public ClassType(final Class<?> value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public ClassType(final String expression)
    {
        super(expression, TO_TYPE);
    }

    /**
     * Parses {@code String} to to the right type.
     * @param str input string
     * @return parsed output
     */
    public static Class valueOf(final String str)
    {
        try
        {
            return Class.forName(str);
        }
        catch (ClassNotFoundException ex)
        {
            throw new IllegalArgumentException("Unable to parse Class " + str);
        }
    }

}
