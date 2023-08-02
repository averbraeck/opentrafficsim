package org.opentrafficsim.xml.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.djutils.logger.CategoryLogger;

/**
 * CoordinateAdapter converts between the XML String for a coordinate and a Point2d.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class CoordinateAdapter extends XmlAdapter<String, Point2d>
{
    /** {@inheritDoc} */
    @Override
    public Point2d unmarshal(final String field) throws IllegalArgumentException
    {
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
            return new Point2d(x, y);
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing coordinate '" + field + "'");
            throw new IllegalArgumentException("Error parsing coordinate" + field, exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final Point2d point) throws IllegalArgumentException
    {
        return "(" + point.x + ", " + point.y + ")";
    }

}
