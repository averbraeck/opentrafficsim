package org.opentrafficsim.xml.bindings;

import org.djutils.draw.point.Point2d;
import org.opentrafficsim.xml.bindings.types.Point2dType;

/**
 * Point2dAdapter converts between the XML String for a coordinate and a Point2d.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Wouter Schakel
 */
public class Point2dAdapter extends ExpressionAdapter<Point2d, Point2dType>
{

    /**
     * Constructor.
     */
    public Point2dAdapter()
    {
        //
    }

    @Override
    public Point2dType unmarshal(final String field) throws IllegalArgumentException
    {
        if (isExpression(field))
        {
            return new Point2dType(trimBrackets(field));
        }
        return new Point2dType(Point2dType.valueOf(field));
    }

    @Override
    public String marshal(final Point2dType point) throws IllegalArgumentException
    {
        return marshalAsExpressionOrValue(point, (p) -> "(" + p.x + ", " + p.y + ")");
    }

}
