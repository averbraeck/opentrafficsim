package org.opentrafficsim.core.network.factory.xml.units;

import java.util.HashMap;
import java.util.Map;

import org.djunits.unit.AnglePlaneUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.AnglePlane;
import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public final class AngleUnits implements UNITS
{
    /** the angle units. */
    public static final Map<String, AnglePlaneUnit> ANGLE_UNITS = new HashMap<>();

    static
    {
        ANGLE_UNITS.put("deg", DEGREE);
        ANGLE_UNITS.put("rad", RADIAN);
    }

    /** Utility class. */
    private AngleUnits()
    {
        // do not instantiate
    }

    /**
     * @param s the string to parse
     * @return the unit as a String in the Map.
     * @throws NetworkException when parsing fails
     */
    public static String parseAngleUnit(final String s) throws NetworkException
    {
        String u = null;
        for (String us : ANGLE_UNITS.keySet())
        {
            if (s.toString().contains(us))
            {
                if (u == null || us.length() > u.length())
                {
                    u = us;
                }
            }
        }
        if (u == null)
        {
            throw new NetworkException("Parsing network: cannot instantiate angle unit in: " + s);
        }
        return u;
    }

    /**
     * @param s the string to parse
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    public static AnglePlane.Abs parseAngleAbs(final String s) throws NetworkException
    {
        String us = parseAngleUnit(s);
        AnglePlaneUnit u = ANGLE_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            AnglePlane.Abs angle = new AnglePlane.Abs(value, u);
            return new AnglePlane.Abs(AnglePlaneUnit.normalize(angle).si, AnglePlaneUnit.SI);
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network: cannot instantiate scalar: " + s, nfe);
        }
    }

    /**
     * @param s the string to parse
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    public static AnglePlane.Rel parseAngleRel(final String s) throws NetworkException
    {
        String us = parseAngleUnit(s);
        AnglePlaneUnit u = ANGLE_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            AnglePlane.Rel angle = new AnglePlane.Rel(value, u);
            return new AnglePlane.Rel(AnglePlaneUnit.normalize(angle).si, AnglePlaneUnit.SI);
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network: cannot instantiate scalar: " + s, nfe);
        }
    }

}
