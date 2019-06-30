package org.opentrafficsim.core.network.factory.xml.units;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.unit.PositionUnit;
import org.djunits.value.vdouble.scalar.Position;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Parser for position with unit.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public final class PositionUnits
{
    /** The position units. */
    public static final Map<String, PositionUnit> POSITION_UNITS = new LinkedHashMap<>();

    static
    {
        POSITION_UNITS.put("mm", PositionUnit.MILLIMETER);
        POSITION_UNITS.put("cm", PositionUnit.CENTIMETER);
        POSITION_UNITS.put("dm", PositionUnit.DECIMETER);
        POSITION_UNITS.put("dam", PositionUnit.DEKAMETER);
        POSITION_UNITS.put("hm", PositionUnit.HECTOMETER);
        POSITION_UNITS.put("m", PositionUnit.METER);
        POSITION_UNITS.put("km", PositionUnit.KILOMETER);
        POSITION_UNITS.put("mi", PositionUnit.MILE);
        POSITION_UNITS.put("y", PositionUnit.YARD);
        POSITION_UNITS.put("ft", PositionUnit.FOOT);
    }

    /** Utility class. */
    private PositionUnits()
    {
        // do not instantiate
    }

    /**
     * @param s String; the string to parse
     * @return the unit as a String in the Map.
     * @throws NetworkException when parsing fails
     */
    public static String parsePositionUnit(final String s) throws NetworkException
    {
        String u = null;
        for (String us : POSITION_UNITS.keySet())
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
    public static Position parsePosition(final String s) throws NetworkException
    {
        String us = parsePositionUnit(s);
        PositionUnit u = POSITION_UNITS.get(us);
        String sv = s.substring(0, s.indexOf(us));
        try
        {
            double value = Double.parseDouble(sv);
            return new Position(value, u);
        }
        catch (NumberFormatException nfe)
        {
            throw new NetworkException("Parsing network: cannot instantiate scalar: " + s, nfe);
        }
    }

}
