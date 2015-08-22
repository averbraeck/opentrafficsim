package org.opentrafficsim.core.network.factory.xml.units;

import java.util.HashMap;
import java.util.Map;

import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public final class TimeUnits
{
    /** the time units. */
    public static final Map<String, TimeUnit> TIME_UNITS = new HashMap<>();

    /** the time units. */
    public static final Map<String, FrequencyUnit> PER_TIME_UNITS = new HashMap<>();

    static
    {
        TIME_UNITS.put("ms", TimeUnit.MILLISECOND);
        TIME_UNITS.put("s", TimeUnit.SECOND);
        TIME_UNITS.put("m", TimeUnit.MINUTE);
        TIME_UNITS.put("min", TimeUnit.MINUTE);
        TIME_UNITS.put("h", TimeUnit.HOUR);
        TIME_UNITS.put("hr", TimeUnit.HOUR);
        TIME_UNITS.put("d", TimeUnit.DAY);
        TIME_UNITS.put("day", TimeUnit.DAY);
        TIME_UNITS.put("wk", TimeUnit.WEEK);
        TIME_UNITS.put("week", TimeUnit.WEEK);

        PER_TIME_UNITS.put("/ms", FrequencyUnit.PER_MILLISECOND);
        PER_TIME_UNITS.put("/s", FrequencyUnit.PER_SECOND);
        PER_TIME_UNITS.put("/m", FrequencyUnit.PER_MINUTE);
        PER_TIME_UNITS.put("/min", FrequencyUnit.PER_MINUTE);
        PER_TIME_UNITS.put("/h", FrequencyUnit.PER_HOUR);
        PER_TIME_UNITS.put("/hr", FrequencyUnit.PER_HOUR);
        PER_TIME_UNITS.put("/d", FrequencyUnit.PER_DAY);
        PER_TIME_UNITS.put("/day", FrequencyUnit.PER_DAY);
        PER_TIME_UNITS.put("/wk", FrequencyUnit.PER_WEEK);
        PER_TIME_UNITS.put("/week", FrequencyUnit.PER_WEEK);
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
    public static DoubleScalar.Abs<TimeUnit> parseTimeAbs(final String s) throws NetworkException
    {
        String us = parseTimeUnit(s);
        TimeUnit u = TIME_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            return new DoubleScalar.Abs<TimeUnit>(value, u);
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
    public static DoubleScalar.Rel<TimeUnit> parseTimeRel(final String s) throws NetworkException
    {
        String us = parseTimeUnit(s);
        TimeUnit u = TIME_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            return new DoubleScalar.Rel<TimeUnit>(value, u);
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
    public static DoubleScalar.Abs<FrequencyUnit> parsePerTimeAbs(final String s) throws NetworkException
    {
        String us = parsePerTimeUnit(s);
        FrequencyUnit u = PER_TIME_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            return new DoubleScalar.Abs<FrequencyUnit>(value, u);
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network: cannot instantiate scalar: " + s, nfe);
        }
    }

}
