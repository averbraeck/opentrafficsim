package org.opentrafficsim.core.network.factory.xml.units;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Parser for times and frequencies with unit.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public final class TimeUnits
{
    /** The time units. */
    public static final Map<String, TimeUnit> TIME_UNITS = new LinkedHashMap<>();

    static
    {
        TIME_UNITS.put("ms", TimeUnit.EPOCH_MILLISECOND);
        TIME_UNITS.put("s", TimeUnit.EPOCH_SECOND);
        TIME_UNITS.put("m", TimeUnit.EPOCH_MINUTE);
        TIME_UNITS.put("min", TimeUnit.EPOCH_MINUTE);
        TIME_UNITS.put("h", TimeUnit.EPOCH_HOUR);
        TIME_UNITS.put("hr", TimeUnit.EPOCH_HOUR);
        TIME_UNITS.put("d", TimeUnit.EPOCH_DAY);
        TIME_UNITS.put("day", TimeUnit.EPOCH_DAY);
        TIME_UNITS.put("wk", TimeUnit.EPOCH_WEEK);
        TIME_UNITS.put("week", TimeUnit.EPOCH_WEEK);
    }

    /** Utility class. */
    private TimeUnits()
    {
        // do not instantiate
    }

    /**
     * @param s String; the string to parse
     * @return the unit as a String in the Map.
     * @throws NetworkException when parsing fails
     */
    public static String parseTimeUnit(final String s) throws NetworkException
    {
        String u = null;
        for (String us : TIME_UNITS.keySet())
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
            throw new NetworkException("Parsing network: cannot instantiate time unit in: " + s);
        }
        return u;
    }

    /**
     * @param s String; the string to parse
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    public static Time parseTime(final String s) throws NetworkException
    {
        String us = parseTimeUnit(s);
        TimeUnit u = TIME_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            return new Time(value, u);
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network: cannot instantiate scalar: " + s, nfe);
        }
    }

}
