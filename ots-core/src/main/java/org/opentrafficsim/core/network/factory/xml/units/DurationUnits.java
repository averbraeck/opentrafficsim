package org.opentrafficsim.core.network.factory.xml.units;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Parser for durations and frequencies with unit.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public final class DurationUnits implements UNITS
{
    /** The time units. */
    public static final Map<String, DurationUnit> DURATION_UNITS = new LinkedHashMap<>();

    /** The per time units. */
    public static final Map<String, FrequencyUnit> FREQUENCY_UNITS = new LinkedHashMap<>();

    static
    {
        DURATION_UNITS.put("ms", MILLISECOND);
        DURATION_UNITS.put("s", SECOND);
        DURATION_UNITS.put("m", MINUTE);
        DURATION_UNITS.put("min", MINUTE);
        DURATION_UNITS.put("h", HOUR);
        DURATION_UNITS.put("hr", HOUR);
        DURATION_UNITS.put("d", DAY);
        DURATION_UNITS.put("day", DAY);
        DURATION_UNITS.put("wk", WEEK);
        DURATION_UNITS.put("week", WEEK);

        FREQUENCY_UNITS.put("/ms", PER_MILLISECOND);
        FREQUENCY_UNITS.put("/s", PER_SECOND);
        FREQUENCY_UNITS.put("/m", PER_MINUTE);
        FREQUENCY_UNITS.put("/min", PER_MINUTE);
        FREQUENCY_UNITS.put("/h", PER_HOUR);
        FREQUENCY_UNITS.put("/hr", PER_HOUR);
        FREQUENCY_UNITS.put("/d", PER_DAY);
        FREQUENCY_UNITS.put("/day", PER_DAY);
        FREQUENCY_UNITS.put("/wk", PER_WEEK);
        FREQUENCY_UNITS.put("/week", PER_WEEK);
    }

    /** Utility class. */
    private DurationUnits()
    {
        // do not instantiate
    }

    /**
     * @param s String; the string to parse
     * @return the unit as a String in the Map.
     * @throws NetworkException when parsing fails
     */
    public static String parseDurationUnit(final String s) throws NetworkException
    {
        String u = null;
        for (String us : DURATION_UNITS.keySet())
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
    public static Duration parseDuration(final String s) throws NetworkException
    {
        String us = parseDurationUnit(s);
        DurationUnit u = DURATION_UNITS.get(us);
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
     * @param s String; the string to parse
     * @return the unit as a String in the Map.
     * @throws NetworkException when parsing fails
     */
    public static String parseFrequencyUnit(final String s) throws NetworkException
    {
        String u = null;
        for (String us : FREQUENCY_UNITS.keySet())
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
            throw new NetworkException("Parsing network: cannot instantiate frequency unit in: " + s);
        }
        return u;
    }

    /**
     * @param s String; the string to parse
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    public static Frequency parseFrequency(final String s) throws NetworkException
    {
        String us = parseFrequencyUnit(s);
        FrequencyUnit u = FREQUENCY_UNITS.get(us);
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
