package org.opentrafficsim.core.network.factory.xml.units;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.util.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Parser for acceleration with unit.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public final class AccelerationUnits implements UNITS
{
    /** The acceleration units. */
    public static final Map<String, AccelerationUnit> ACCELERATION_UNITS = new LinkedHashMap<>();
    static
    {
        ACCELERATION_UNITS.put("km/h^2", KM_PER_HOUR_2);
        ACCELERATION_UNITS.put("mi/h^2", MILE_PER_HOUR_2);
        ACCELERATION_UNITS.put("m/s^2", METER_PER_SECOND_2);
        ACCELERATION_UNITS.put("ft/s^2", FOOT_PER_SECOND_2);
    }

    /** Utility class. */
    private AccelerationUnits()
    {
        // do not instantiate
    }

    /**
     * @param s String; the string to parse
     * @return the unit as a String in the Map.
     * @throws NetworkException when parsing fails
     */
    public static String parseAccelerationUnit(final String s) throws NetworkException
    {
        String u = null;
        for (String us : ACCELERATION_UNITS.keySet())
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
            throw new NetworkException("Parsing network: cannot instantiate acceleration unit in: " + s);
        }
        return u;
    }

    /**
     * @param s String; the string to parse
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    public static Acceleration parseAcceleration(final String s) throws NetworkException
    {
        String us = parseAccelerationUnit(s);
        AccelerationUnit u = ACCELERATION_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            return new Acceleration(value, u);
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network: cannot instantiate scalar: " + s, nfe);
        }
    }
}
