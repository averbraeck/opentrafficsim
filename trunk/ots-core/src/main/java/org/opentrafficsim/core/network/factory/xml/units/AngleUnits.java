package org.opentrafficsim.core.network.factory.xml.units;

import java.util.HashMap;
import java.util.Map;

import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.AnglePlaneUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public final class AngleUnits
{
    /** the angle units. */
    public static final Map<String, AnglePlaneUnit> ANGLE_UNITS = new HashMap<>();

    static
    {
        ANGLE_UNITS.put("deg", AnglePlaneUnit.DEGREE);
        ANGLE_UNITS.put("rad", AnglePlaneUnit.RADIAN);
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
    public static DoubleScalar.Abs<AnglePlaneUnit> parseAngleAbs(final String s) throws NetworkException
    {
        String us = parseAngleUnit(s);
        AnglePlaneUnit u = ANGLE_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            DoubleScalar.Abs<AnglePlaneUnit> angle = new DoubleScalar.Abs<AnglePlaneUnit>(value, u);
            return AnglePlaneUnit.normalize(angle);
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network: cannot instantiate scalar: " + s, nfe);
        }
    }

}

