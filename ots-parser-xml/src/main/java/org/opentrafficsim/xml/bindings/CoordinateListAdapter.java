package org.opentrafficsim.xml.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.xml.bindings.types.Point2dList;

/**
 * CoordinateListAdapter converts between the XML String for a list of coordinates and a List of Point2d coordinates. Because
 * the ots-xsd project is not dependent on ots-core, Point2d is chosen instead of OtsPoint2d to store the (x, y, z) information.
 * The marshal function returns 2D-coordinates for points where the z-value is zero. Spaces are not allowed in the textual
 * representation of the list.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class CoordinateListAdapter extends XmlAdapter<String, Point2dList>
{
    /** {@inheritDoc} */
    @Override
    public Point2dList unmarshal(final String field) throws IllegalArgumentException
    {
        try
        {
            Throw.when(!field.startsWith("("), IllegalArgumentException.class, "Coordinate list must start with '(': " + field);
            Throw.when(!field.endsWith(")"), IllegalArgumentException.class, "Coordinate list must end with ')': " + field);
            String clean = field.substring(1, field.length() - 2);
            Point2dList coordinates = new Point2dList();
            String[] coordinateFields = clean.split("\\(,\\)");
            for (String coordinateField : coordinateFields)
            {
                String[] digits = coordinateField.split(",");
                Throw.when(digits.length < 2, IllegalArgumentException.class,
                        "Coordinate must have at least x and y: " + coordinateField);
                Throw.when(digits.length > 2, IllegalArgumentException.class,
                        "Coordinate must have at most 3 dimensions: " + coordinateField);
                double x = Double.parseDouble(digits[0]);
                double y = Double.parseDouble(digits[1]);
                coordinates.add(new Point2d(x, y));
            }
            return coordinates;
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing coordinate list: '" + field + "'");
            throw new IllegalArgumentException("Error parsing coordinate list: " + field, exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final Point2dList points) throws IllegalArgumentException
    {
        String list = "";
        for (Point2d point : points)
        {
            if (list.length() > 0)
                list += ",";
            list += "(" + point.x + "," + point.y + ")";
        }
        return list;
    }

}
