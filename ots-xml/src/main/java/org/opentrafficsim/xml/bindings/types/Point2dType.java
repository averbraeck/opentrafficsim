package org.opentrafficsim.xml.bindings.types;

import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;

/**
 * Expression type with Point2d value.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class Point2dType extends ExpressionType<Point2d>
{

    /** */
    private static final long serialVersionUID = 20251111L;

    /** Convert string to point. */
    private static final SerializableFunction<Object, Point2d> TO_POINT =
            SerializableFunction.of(Point2d.class, Point2dType::valueOf);

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public Point2dType(final Point2d value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public Point2dType(final String expression)
    {
        super(expression, TO_POINT);
    }

    /**
     * Parses {@code String} to to the right type.
     * @param str input string
     * @return parsed output
     */
    public static Point2d valueOf(final String str)
    {
        String clean = str.replaceAll("\\s", "");
        Throw.when(!clean.startsWith("("), IllegalArgumentException.class, "Coordinate must start with '(': %s", str);
        Throw.when(!clean.endsWith(")"), IllegalArgumentException.class, "Coordinate must end with ')': %s", str);
        clean = clean.substring(1, clean.length() - 1);
        String[] digits = clean.split(",");
        Throw.when(digits.length < 2, IllegalArgumentException.class, "Coordinate must have at least x and y: %s", str);
        Throw.when(digits.length > 2, IllegalArgumentException.class, "Coordinate must have at most 2 dimensions: %s", str);
        double x = Double.parseDouble(digits[0]);
        double y = Double.parseDouble(digits[1]);
        return new Point2d(x, y);
    }

}
