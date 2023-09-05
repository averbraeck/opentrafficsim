package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.Direction;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.xml.bindings.types.DirectionType;

/**
 * DirectionAdapter converts between the XML String for an Direction and the DJUnits Direction. EAST is taken as zero degrees,
 * and the Direction adapts an ENU (East-North-Up) model, where positive x is East, positive y is North, positive z is up, and
 * degrees go anti-clockwise from the positive x-axis (East).
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class DirectionAdapter extends ScalarAdapter<Direction, DirectionType>
{
    
    /** {@inheritDoc} */
    @Override
    public DirectionType unmarshal(final String field) throws IllegalArgumentException
    {
        if (isExpression(field))
        {
            return new DirectionType(trimBrackets(field));
        }
        try
        {
            String direction = field;
            if (direction.trim().endsWith("deg"))
            {
                direction = direction.replace("deg", "deg(E)");
            }
            if (direction.trim().endsWith("rad"))
            {
                direction = direction.replace("rad", "rad(E)");
            }
            direction = direction.replace("East", "E");
            direction = direction.replace("North", "N");
            return new DirectionType(Direction.valueOf(direction));
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing Direction '" + field + "'");
            throw exception;
        }
    }

}

