package org.opentrafficsim.xml.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djunits.unit.AngleUnit;
import org.djunits.value.vdouble.scalar.Angle;

/**
 * AngleAdapter converts between the XML String for an Angle and the DJUnits Angle. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class AngleAdapter extends XmlAdapter<String, Angle>
{
    /** {@inheritDoc} */
    @Override
    public Angle unmarshal(final String field) throws IllegalArgumentException
    {
        try
        {
            if (field.endsWith("deg"))
            {
                double d = Double.parseDouble(field.substring(0, field.length() - 3).trim());
                return new Angle(d, AngleUnit.DEGREE);
            }
            else if (field.endsWith("rad"))
            {
                double d = Double.parseDouble(field.substring(0, field.length() - 3).trim());
                return new Angle(d, AngleUnit.RADIAN);
            }
        }
        catch (Exception exception)
        {
            throw new IllegalArgumentException("Error parsing Angle " + field, exception);
        }
        throw new IllegalArgumentException("Error parsing Angle " + field);
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final Angle angle) throws IllegalArgumentException
    {
        try
        {
            if (angle.getUnit().equals(AngleUnit.DEGREE))
            {
                return angle.getInUnit() + " deg";
            }
            else if (angle.getUnit().equals(AngleUnit.RADIAN))
            {
                return angle.getInUnit() + " rad";
            }
        }
        catch (Exception exception)
        {
            throw new IllegalArgumentException("Error printing Angle " + angle, exception);
        }
        return angle.getSI() + " rad";
    }

}
