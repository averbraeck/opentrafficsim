package org.opentrafficsim.core.network.factory.xml.units;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.unit.SpeedUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Parser for speed with unit.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public final class SpeedUnits implements UNITS
{
    /** The speed units. */
    public static final Map<String, SpeedUnit> SPEED_UNITS = new LinkedHashMap<>();
    static
    {
        SPEED_UNITS.put("km/h", KM_PER_HOUR);
        SPEED_UNITS.put("mi/h", MILE_PER_HOUR);
        SPEED_UNITS.put("m/s", METER_PER_SECOND);
        SPEED_UNITS.put("ft/s", FOOT_PER_SECOND);
    }

    /** Utility class. */
    private SpeedUnits()
    {
        // do not instantiate
    }

    /**
     * @param s String; the string to parse
     * @return the unit as a String in the Map.
     * @throws NetworkException when parsing fails
     */
    public static String parseSpeedUnit(final String s) throws NetworkException
    {
        String u = null;
        for (String us : SPEED_UNITS.keySet())
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
            throw new NetworkException("Parsing network: cannot instantiate speed unit in: " + s);
        }
        return u;
    }

    /**
     * @param s String; the string to parse
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    public static Speed parseSpeed(final String s) throws NetworkException
    {
        String us = parseSpeedUnit(s);
        SpeedUnit u = SPEED_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            return new Speed(value, u);
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network: cannot instantiate scalar: " + s, nfe);
        }
    }
}
