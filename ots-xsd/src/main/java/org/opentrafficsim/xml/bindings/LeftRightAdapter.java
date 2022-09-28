package org.opentrafficsim.xml.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.xml.bindings.types.ArcDirection;

/**
 * LeftRightAdapter to convert between XML representations of an arc direction, coded as L | LEFT | R | RIGHT | CLOCKWISE |
 * COUNTERCLOCKWISE, and an enum type.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class LeftRightAdapter extends XmlAdapter<String, ArcDirection>
{
    /** {@inheritDoc} */
    @Override
    public ArcDirection unmarshal(final String field) throws IllegalArgumentException
    {
        try
        {
            String clean = field.replaceAll("\\s", "");
            if (clean.equals("L") || clean.equals("LEFT") || clean.equals("COUNTERCLOCKWISE"))
            {
                return ArcDirection.LEFT;
            }
            if (clean.equals("R") || clean.equals("RIGHT") || clean.equals("CLOCKWISE"))
            {
                return ArcDirection.RIGHT;
            }
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing ArcDirection (LeftRight) '" + field + "'");
            throw new IllegalArgumentException("Error parsing ArcDirection (LeftRight) " + field, exception);
        }
        CategoryLogger.always().error("Problem parsing ArcDirection (LeftRight) '" + field + "'");
        throw new IllegalArgumentException("Error parsing ArcDirection (LeftRight) " + field);
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final ArcDirection arcDirection) throws IllegalArgumentException
    {
        if (arcDirection.equals(ArcDirection.LEFT))
            return "LEFT";
        return "RIGHT";
    }

}
