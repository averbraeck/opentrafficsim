package org.opentrafficsim.xml.bindings;

import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.xml.bindings.types.Point2dType;

/**
 * Point2dAdapter converts between the XML String for a coordinate and a Point2d.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class Point2dAdapter extends ExpressionAdapter<Point2d, Point2dType>
{

    @Override
    public Point2dType unmarshal(final String field) throws IllegalArgumentException
    {
        if (isExpression(field))
        {
            return new Point2dType(trimBrackets(field));
        }
        try
        {
            String clean = field.replaceAll("\\s", "");
            Throw.when(!clean.startsWith("("), IllegalArgumentException.class, "Coordinate must start with '(': " + field);
            Throw.when(!clean.endsWith(")"), IllegalArgumentException.class, "Coordinate must end with ')': " + field);
            clean = clean.substring(1, clean.length() - 1);
            String[] digits = clean.split(",");
            Throw.when(digits.length < 2, IllegalArgumentException.class, "Coordinate must have at least x and y: " + field);
            Throw.when(digits.length > 2, IllegalArgumentException.class,
                    "Coordinate must have at most 2 dimensions: " + field);

            double x = Double.parseDouble(digits[0]);
            double y = Double.parseDouble(digits[1]);
            return new Point2dType(new Point2d(x, y));
        }
        catch (Exception exception)
        {
            throw new IllegalArgumentException("Error parsing coordinate" + field, exception);
        }
    }

    @Override
    public String marshal(final Point2dType point) throws IllegalArgumentException
    {
        return marshal(point, (p) -> "(" + p.x + ", " + p.y + ")");
    }

}
