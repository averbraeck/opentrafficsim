package org.opentrafficsim.core.network.factory.xml.units;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.LinearDensityUnit;
import org.djunits.unit.util.UNITS;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Parser for length with unit.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public final class LengthUnits implements UNITS
{
    /** The length units. */
    public static final Map<String, LengthUnit> LENGTH_UNITS = new LinkedHashMap<>();

    /** The per-length units. */
    public static final Map<String, LinearDensityUnit> PER_LENGTH_UNITS = new LinkedHashMap<>();

    static
    {
        LENGTH_UNITS.put("mm", MILLIMETER);
        LENGTH_UNITS.put("cm", CENTIMETER);
        LENGTH_UNITS.put("dm", DECIMETER);
        LENGTH_UNITS.put("m", METER);
        LENGTH_UNITS.put("dam", DECAMETER);
        LENGTH_UNITS.put("hm", HECTOMETER);
        LENGTH_UNITS.put("km", KILOMETER);
        LENGTH_UNITS.put("mi", MILE);
        LENGTH_UNITS.put("y", YARD);
        LENGTH_UNITS.put("ft", FOOT);

        PER_LENGTH_UNITS.put("/mm", PER_MILLIMETER);
        PER_LENGTH_UNITS.put("/cm", PER_CENTIMETER);
        PER_LENGTH_UNITS.put("/dm", PER_DECIMETER);
        PER_LENGTH_UNITS.put("/m", PER_METER);
        PER_LENGTH_UNITS.put("/dam", PER_DECAMETER);
        PER_LENGTH_UNITS.put("/hm", PER_HECTOMETER);
        PER_LENGTH_UNITS.put("/km", PER_KILOMETER);
        PER_LENGTH_UNITS.put("/mi", PER_MILE);
        PER_LENGTH_UNITS.put("/y", PER_YARD);
        PER_LENGTH_UNITS.put("/ft", PER_FOOT);
    }

    /** Utility class. */
    private LengthUnits()
    {
        // do not instantiate
    }

    /**
     * @param s String; the string to parse
     * @return the unit as a String in the Map.
     * @throws NetworkException when parsing fails
     */
    public static String parseLengthUnit(final String s) throws NetworkException
    {
        String u = null;
        for (String us : LENGTH_UNITS.keySet())
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
            throw new NetworkException("Parsing network: cannot instantiate length unit in: " + s);
        }
        return u;
    }

    /**
     * @param s String; the string to parse
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    public static Length parseLength(final String s) throws NetworkException
    {
        String us = parseLengthUnit(s);
        LengthUnit u = LENGTH_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            return new Length(value, u);
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
    public static String parsePerLengthUnit(final String s) throws NetworkException
    {
        String u = null;
        for (String us : PER_LENGTH_UNITS.keySet())
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
            throw new NetworkException("Parsing network: cannot instantiate per-length unit in: " + s);
        }
        return u;
    }

    /**
     * @param s String; the string to parse
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    public static LinearDensity parseLinearDensity(final String s) throws NetworkException
    {
        String us = parsePerLengthUnit(s);
        LinearDensityUnit u = PER_LENGTH_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            return new LinearDensity(value, u);
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network: cannot instantiate scalar: " + s, nfe);
        }
    }

}
