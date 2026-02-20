package org.opentrafficsim.xml.bindings.types;

import org.djutils.draw.point.Point2d;
import org.opentrafficsim.xml.bindings.Point2dAdapter;

/**
 * Expression type with Point2d value.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class Point2dType extends ExpressionType<Point2d>
{

    /** */
    private static final long serialVersionUID = 20251111L;

    /** Convert string to point. */
    private static final SerializableFunction<Object, Point2d> TO_POINT = (s) -> Point2dAdapter.of(s.toString());

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public Point2dType(final Point2d value)
    {
        super(value, TO_POINT);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public Point2dType(final String expression)
    {
        super(expression, TO_POINT);
    }

}
