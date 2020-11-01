package org.opentrafficsim.xml.bindings;

import javax.vecmath.Point3d;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djutils.exceptions.Throw;
import org.djutils.logger.CategoryLogger;

/**
 * CoordinateAdapter converts between the XML String for a coordinate and a Point3d. Because the ots-xsd project is not
 * dependent on ots-core, Point3d is chosen instead of OTSPoint3D to store the (x, y, z) information. The marshal function
 * returns a 2D-coordinate in case the z-value is zero. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class CoordinateAdapter extends XmlAdapter<String, Point3d>
{
    /** {@inheritDoc} */
    @Override
    public Point3d unmarshal(final String field) throws IllegalArgumentException
    {
        try
        {
            String clean = field.replaceAll("\\s", "");
            Throw.when(!clean.startsWith("("), IllegalArgumentException.class, "Coordinate must start with '(': " + field);
            Throw.when(!clean.endsWith(")"), IllegalArgumentException.class, "Coordinate must end with ')': " + field);
            clean = clean.substring(1, clean.length() - 1);
            String[] digits = clean.split(",");
            Throw.when(digits.length < 2, IllegalArgumentException.class, "Coordinate must have at least x and y: " + field);
            Throw.when(digits.length > 3, IllegalArgumentException.class,
                    "Coordinate must have at most 3 dimensions: " + field);

            double x = Double.parseDouble(digits[0]);
            double y = Double.parseDouble(digits[1]);
            double z = digits.length == 2 ? 0.0 : Double.parseDouble(digits[2]);
            return new Point3d(x, y, z);
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing coordinate '" + field + "'");
            throw new IllegalArgumentException("Error parsing coordinate" + field, exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final Point3d point) throws IllegalArgumentException
    {
        if (point.z == 0.0)
            return "(" + point.x + ", " + point.y + ")";
        return "(" + point.x + ", " + point.y + ", " + point.z + ")";
    }

}
