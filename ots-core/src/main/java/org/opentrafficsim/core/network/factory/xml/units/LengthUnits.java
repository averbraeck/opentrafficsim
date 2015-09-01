package org.opentrafficsim.core.network.factory.xml.units;

import java.util.HashMap;
import java.util.Map;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.LinearDensityUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.OTS_SCALAR;
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
public final class LengthUnits implements OTS_SCALAR
{
    /** the length units. */
    public static final Map<String, LengthUnit> LENGTH_UNITS = new HashMap<>();

    /** the per-length units. */
    public static final Map<String, LinearDensityUnit> PER_LENGTH_UNITS = new HashMap<>();

    static
    {
        LENGTH_UNITS.put("mm", LengthUnit.MILLIMETER);
        LENGTH_UNITS.put("cm", LengthUnit.CENTIMETER);
        LENGTH_UNITS.put("dm", LengthUnit.DECIMETER);
        LENGTH_UNITS.put("dam", LengthUnit.DEKAMETER);
        LENGTH_UNITS.put("hm", LengthUnit.HECTOMETER);
        LENGTH_UNITS.put("m", LengthUnit.METER);
        LENGTH_UNITS.put("km", LengthUnit.KILOMETER);
        LENGTH_UNITS.put("mi", LengthUnit.MILE);
        LENGTH_UNITS.put("y", LengthUnit.YARD);
        LENGTH_UNITS.put("ft", LengthUnit.FOOT);

        PER_LENGTH_UNITS.put("/mm", LinearDensityUnit.PER_MILLIMETER);
        PER_LENGTH_UNITS.put("/cm", LinearDensityUnit.PER_CENTIMETER);
        PER_LENGTH_UNITS.put("/dm", LinearDensityUnit.PER_DECIMETER);
        PER_LENGTH_UNITS.put("/dam", LinearDensityUnit.PER_DEKAMETER);
        PER_LENGTH_UNITS.put("/hm", LinearDensityUnit.PER_HECTOMETER);
        PER_LENGTH_UNITS.put("/m", LinearDensityUnit.PER_METER);
        PER_LENGTH_UNITS.put("/km", LinearDensityUnit.PER_KILOMETER);
        PER_LENGTH_UNITS.put("/mi", LinearDensityUnit.PER_MILE);
        PER_LENGTH_UNITS.put("/y", LinearDensityUnit.PER_YARD);
        PER_LENGTH_UNITS.put("/ft", LinearDensityUnit.PER_FOOT);
    }

    /** Utility class. */
    private LengthUnits()
    {
        // do not instantiate
    }

    /**
     * @param s the string to parse
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
     * @param s the string to parse
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    public static Length.Abs parseLengthAbs(final String s) throws NetworkException
    {
        String us = parseLengthUnit(s);
        LengthUnit u = LENGTH_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            return new Length.Abs(value, u);
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
    public static Length.Rel parseLengthRel(final String s) throws NetworkException
    {
        String us = parseLengthUnit(s);
        LengthUnit u = LENGTH_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            return new Length.Rel(value, u);
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
     * @param s the string to parse
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    public static DoubleScalar.Abs<LinearDensityUnit> parsePerLengthAbs(final String s) throws NetworkException
    {
        String us = parsePerLengthUnit(s);
        LinearDensityUnit u = PER_LENGTH_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            return new DoubleScalar.Abs<LinearDensityUnit>(value, u);
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network: cannot instantiate scalar: " + s, nfe);
        }
    }

}
