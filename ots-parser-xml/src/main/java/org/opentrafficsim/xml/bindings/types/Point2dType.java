package org.opentrafficsim.xml.bindings.types;

import org.djutils.draw.point.Point2d;

/**
 * Expression type with Point2d value.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class Point2dType extends ExpressionType<Point2d>
{

    /**
     * Constructor with value.
     * @param value Point2d; value, may be {@code null}.
     */
    public Point2dType(final Point2d value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression String; expression.
     */
    public Point2dType(final String expression)
    {
        super(expression);
    }

}
