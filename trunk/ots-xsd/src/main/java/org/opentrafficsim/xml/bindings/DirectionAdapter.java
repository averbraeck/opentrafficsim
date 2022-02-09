package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.Direction;
import org.djutils.logger.CategoryLogger;

/**
 * DirectionAdapter converts between the XML String for an Direction and the DJUnits Direction. EAST is taken as zero degrees,
 * and the Direction adapts an ENU (East-North-Up) model, where positive x is East, positive y is North, positive z is up, and
 * degrees go anti-clockwise from the positive x-axis (East). <br>
 * <br>
 * Copyright (c) 2003-2022 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
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
