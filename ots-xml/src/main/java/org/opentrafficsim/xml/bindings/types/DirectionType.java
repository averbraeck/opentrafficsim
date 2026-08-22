package org.opentrafficsim.xml.bindings.types;

import org.djunits.value.vdouble.scalar.Direction;

/**
 * Expression type with Direction value.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class DirectionType extends ExpressionType<Direction>
{

    /** Serialization version UID. */
    private static final long serialVersionUID = 20251111L;

    /** Function to convert output from expression to the right type. */
    private static final SerializableFunction<Object, Direction> TO_TYPE =
            SerializableFunction.ofNumeric(Direction.class, DirectionType::valueOf, Direction::ofSI);

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public DirectionType(final Direction value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public DirectionType(final String expression)
    {
        super(expression, TO_TYPE);
    }

    /**
     * Parses {@code String} to to the right type, allowing {@code "deg"} as the unit assuming the {@code "deg(E)"} unit.
     * @param str input string
     * @return parsed output
     */
    public static Direction valueOf(final String str)
    {
        String direction = str;
        if (direction.trim().endsWith("deg"))
        {
            direction = direction.replace("deg", "deg(E)");
        }
        if (direction.trim().endsWith("rad"))
        {
            direction = direction.replace("rad", "rad(E)");
        }
        direction = direction.replace("East", "E");
        direction = direction.replace("North", "N");
        return Direction.valueOf(direction);
    }

}
