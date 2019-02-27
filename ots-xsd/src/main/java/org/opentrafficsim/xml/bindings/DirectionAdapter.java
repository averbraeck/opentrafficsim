package org.opentrafficsim.xml.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djunits.unit.DirectionUnit;
import org.djunits.value.vdouble.scalar.Direction;

/**
 * DirectionAdapter converts between the XML String for an Direction and the DJUnits Direction. EAST is taken as zero degrees,
 * and the Direction adapts an ENU (East-North-Up) model, where positive x is East, positive y is North, positive z is up, and
 * degrees go anti-clockwise from the positive x-axis (East). <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class DirectionAdapter extends XmlAdapter<String, Direction>
{
    /** {@inheritDoc} */
    @Override
    public Direction unmarshal(final String field) throws IllegalArgumentException
    {
        try
        {
            if (field.endsWith("deg"))
            {
                double d = Double.parseDouble(field.substring(0, field.length() - 3).trim());
                return new Direction(d, DirectionUnit.EAST_DEGREE);
            }
            else if (field.endsWith("rad"))
            {
                double d = Double.parseDouble(field.substring(0, field.length() - 3).trim());
                return new Direction(d, DirectionUnit.EAST_RADIAN);
            }
        }
        catch (Exception exception)
        {
            throw new IllegalArgumentException("Error parsing Direction " + field, exception);
        }
        throw new IllegalArgumentException("Error parsing Direction " + field);
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final Direction direction) throws IllegalArgumentException
    {
        try
        {
            if (direction.getUnit().equals(DirectionUnit.EAST_DEGREE))
            {
                return direction.getInUnit() + " deg";
            }
            else if (direction.getUnit().equals(DirectionUnit.EAST_RADIAN))
            {
                return direction.getInUnit() + " rad";
            }
        }
        catch (Exception exception)
        {
            throw new IllegalArgumentException("Error printing Direction " + direction, exception);
        }
        throw new IllegalArgumentException("Error printing Direction " + direction);
    }

}
