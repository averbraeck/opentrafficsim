package org.opentrafficsim.core.network.factory.xml.units;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.unit.AngleUnit;
import org.djunits.unit.DirectionUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.AngleUtil;
import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Direction;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Parser for angle with unit.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public final class AngleUnits implements UNITS
{
    /** The angle units. */
    public static final Map<String, AngleUnit> ANGLE_UNITS = new LinkedHashMap<>();

    /** The direction units. */
    public static final Map<String, DirectionUnit> DIRECTION_UNITS = new LinkedHashMap<>();

    static
    {
        ANGLE_UNITS.put("deg", DEGREE);
        ANGLE_UNITS.put("rad", RADIAN);

        DIRECTION_UNITS.put("deg", DirectionUnit.EAST_DEGREE);
        DIRECTION_UNITS.put("rad", DirectionUnit.EAST_RADIAN);
    }

    /** Utility class cannot be instantiated. */
    private AngleUnits()
    {
        // do not instantiate
    }

    /**
     * @param s String; the string to parse
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
     * @param s String; the string to parse
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    public static Angle parseAngle(final String s) throws NetworkException
    {
        String us = parseAngleUnit(s);
        AngleUnit u = ANGLE_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            Angle angle = new Angle(value, u);
            return new Angle(AngleUtil.normalize(angle).si, AngleUnit.SI);
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
    public static String parseDirectionUnit(final String s) throws NetworkException
    {
        String u = null;
        for (String us : DIRECTION_UNITS.keySet())
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
            throw new NetworkException("Parsing network: cannot instantiate direction unit in: " + s);
        }
        return u;
    }

    /**
     * @param s String; the string to parse
     * @return the next value.
     * @throws NetworkException when parsing fails
     */
    public static Direction parseDirection(final String s) throws NetworkException
    {
        String us = parseDirectionUnit(s);
        DirectionUnit u = DIRECTION_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            Direction direction = new Direction(value, u);
            return new Direction(AngleUtil.normalize(direction).si, DirectionUnit.EAST_RADIAN);
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network: cannot instantiate scalar: " + s, nfe);
        }
    }

}
