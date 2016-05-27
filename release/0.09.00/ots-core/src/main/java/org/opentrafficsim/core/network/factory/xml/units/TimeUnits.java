package org.opentrafficsim.core.network.factory.xml.units;

import java.util.HashMap;
import java.util.Map;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Parser for times and frequencies with unit.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public final class TimeUnits implements UNITS
{
    /** The time units. */
    public static final Map<String, TimeUnit> TIME_UNITS = new HashMap<>();

    /** The per time units. */
    public static final Map<String, FrequencyUnit> PER_TIME_UNITS = new HashMap<>();

    static
    {
        TIME_UNITS.put("ms", MILLISECOND);
        TIME_UNITS.put("s", SECOND);
        TIME_UNITS.put("m", MINUTE);
        TIME_UNITS.put("min", MINUTE);
        TIME_UNITS.put("h", HOUR);
        TIME_UNITS.put("hr", HOUR);
        TIME_UNITS.put("d", DAY);
        TIME_UNITS.put("day", DAY);
        TIME_UNITS.put("wk", WEEK);
        TIME_UNITS.put("week", WEEK);

        PER_TIME_UNITS.put("/ms", PER_MILLISECOND);
        PER_TIME_UNITS.put("/s", PER_SECOND);
        PER_TIME_UNITS.put("/m", PER_MINUTE);
        PER_TIME_UNITS.put("/min", PER_MINUTE);
        PER_TIME_UNITS.put("/h", PER_HOUR);
        PER_TIME_UNITS.put("/hr", PER_HOUR);
        PER_TIME_UNITS.put("/d", PER_DAY);
        PER_TIME_UNITS.put("/day", PER_DAY);
        PER_TIME_UNITS.put("/wk", PER_WEEK);
        PER_TIME_UNITS.put("/week", PER_WEEK);
    }

    /** Utility class. */
    private TimeUnits()
    {
        // do not instantiate
    }

    /**
     * @param s the string to parse
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
     * @param s the string to parse
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    public static Time parseTimeAbs(final String s) throws NetworkException
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

    /**
     * @param s the string to parse
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    public static Duration parseTimeRel(final String s) throws NetworkException
    {
        String us = parseTimeUnit(s);
        TimeUnit u = TIME_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            return new Duration(value, u);
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network: cannot instantiate scalar: " + s, nfe);
        }
    }

    /**
     * @param s the string to parse
     * @return the unit as a String in the Map.
     * @throws NetworkException when parsing fails
     */
    public static String parsePerTimeUnit(final String s) throws NetworkException
    {
        String u = null;
        for (String us : PER_TIME_UNITS.keySet())
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
            throw new NetworkException("Parsing network: cannot instantiate per-time unit in: " + s);
        }
        return u;
    }

    /**
     * @param s the string to parse
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    public static Frequency parsePerTimeAbs(final String s) throws NetworkException
    {
        String us = parsePerTimeUnit(s);
        FrequencyUnit u = PER_TIME_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            return new Frequency(value, u);
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network: cannot instantiate scalar: " + s, nfe);
        }
    }

}
