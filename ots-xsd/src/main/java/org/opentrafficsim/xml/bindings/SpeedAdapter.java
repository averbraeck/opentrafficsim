package org.opentrafficsim.xml.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;

/**
 * SpeedAdapter converts between the XML String for a Speed and the DJUnits Speed. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class SpeedAdapter extends XmlAdapter<String, Speed>
{
    /** {@inheritDoc} */
    @Override
    public Speed unmarshal(final String field) throws IllegalArgumentException
    {
        // km/h|m/s|mi/h|ft/s
        try
        {
            if (field.endsWith("km/h"))
            {
                double d = Double.parseDouble(field.substring(0, field.length() - 4).trim());
                return new Speed(d, SpeedUnit.KM_PER_HOUR);
            }
            else if (field.endsWith("m/s"))
            {
                double d = Double.parseDouble(field.substring(0, field.length() - 3).trim());
                return new Speed(d, SpeedUnit.METER_PER_SECOND);
            }
            else if (field.endsWith("mi/h"))
            {
                double d = Double.parseDouble(field.substring(0, field.length() - 4).trim());
                return new Speed(d, SpeedUnit.MILE_PER_HOUR);
            }
            else if (field.endsWith("ft/s"))
            {
                double d = Double.parseDouble(field.substring(0, field.length() - 4).trim());
                return new Speed(d, SpeedUnit.FOOT_PER_SECOND);
            }
        }
        catch (Exception exception)
        {
            throw new IllegalArgumentException("Error parsing Speed " + field, exception);
        }
        throw new IllegalArgumentException("Error parsing Speed " + field);
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final Speed Speed) throws IllegalArgumentException
    {
        // km/h|m/s|mi/h|ft/s
        try
        {
            if (Speed.getUnit().equals(SpeedUnit.KM_PER_HOUR))
            {
                return Speed.getInUnit() + " km/h";
            }
            else if (Speed.getUnit().equals(SpeedUnit.METER_PER_SECOND))
            {
                return Speed.getInUnit() + " m/s";
            }
            else if (Speed.getUnit().equals(SpeedUnit.MILE_PER_HOUR))
            {
                return Speed.getInUnit() + " mi/h";
            }
            else if (Speed.getUnit().equals(SpeedUnit.FOOT_PER_SECOND))
            {
                return Speed.getInUnit() + " ft/s";
            }
        }
        catch (Exception exception)
        {
            throw new IllegalArgumentException("Error printing Speed " + Speed, exception);
        }
        return Speed.getSI() + " m";
    }

}
