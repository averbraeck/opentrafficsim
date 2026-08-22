package org.opentrafficsim.xml.bindings.types;

import org.opentrafficsim.xml.bindings.types.ArcDirectionType.ArcDirection;

/**
 * Expression type with ArcDirection value.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class ArcDirectionType extends ExpressionType<ArcDirection>
{

    /** Serialization version UID. */
    private static final long serialVersionUID = 20251111L;

    /** Function to convert output from expression to the right type. */
    private static final SerializableFunction<Object, ArcDirection> TO_TYPE =
            SerializableFunction.ofStaticField(ArcDirection.class);

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public ArcDirectionType(final ArcDirection value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public ArcDirectionType(final String expression)
    {
        super(expression, TO_TYPE);
    }

    /**
     * Parses {@code String} to to the right type.
     * @param str input string
     * @return parsed output
     */
    public static ArcDirection valueOf(final String str)
    {
        String clean = str.replaceAll("\\s", "");
        if (clean.equals("L") || clean.equals("LEFT") || clean.equals("COUNTERCLOCKWISE"))
        {
            return ArcDirection.LEFT;
        }
        if (clean.equals("R") || clean.equals("RIGHT") || clean.equals("CLOCKWISE"))
        {
            return ArcDirection.RIGHT;
        }
        throw new IllegalArgumentException("Unable to parse ArcDirection (LeftRight) " + str);
    }

    /**
     * Direction of the arc; LEFT or RIGHT.
     */
    public enum ArcDirection
    {
        /** Left = counter-clockwise. */
        LEFT,

        /** Right = clockwise. */
        RIGHT;
    }

}
