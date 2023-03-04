package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.Direction;
import org.djutils.logger.CategoryLogger;

/**
 * DirectionAdapter converts between the XML String for an Direction and the DJUnits Direction. EAST is taken as zero degrees,
 * and the Direction adapts an ENU (East-North-Up) model, where positive x is East, positive y is North, positive z is up, and
 * degrees go anti-clockwise from the positive x-axis (East).
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class DirectionAdapter extends UnitAdapter<Direction>
{
    /** {@inheritDoc} */
    @Override
    public Direction unmarshal(final String field) throws IllegalArgumentException
    {
        try
        {
            String direction = field;
            if (direction.trim().endsWith("deg"))
            {
                direction = direction.replace("deg", "deg(E)");
            }
            if (direction.trim().endsWith("rad"))
            {
                direction = direction.replace("deg", "rad(E)");
            }
            direction = direction.replace("East", "E");
            direction = direction.replace("North", "N");
            return Direction.valueOf(direction);
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing Direction '" + field + "'");
            throw exception;
        }
    }

}
