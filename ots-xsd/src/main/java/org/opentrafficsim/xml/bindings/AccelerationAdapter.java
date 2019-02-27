package org.opentrafficsim.xml.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;

/**
 * AccelerationAdapter converts between the XML String for an Acceleration and the DJUnits Acceleration. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class AccelerationAdapter extends XmlAdapter<String, Acceleration>
{
    /** {@inheritDoc} */
    @Override
    public Acceleration unmarshal(final String field) throws IllegalArgumentException
    {
        // km/h\^2|m/s\^2|mi/h\^2|ft/s\^2
        try
        {
            if (field.endsWith("km/h^2"))
            {
                double d = Double.parseDouble(field.substring(0, field.length() - 6).trim());
                return new Acceleration(d, AccelerationUnit.KM_PER_HOUR_2);
            }
            else if (field.endsWith("m/s^2"))
            {
                double d = Double.parseDouble(field.substring(0, field.length() - 5).trim());
                return new Acceleration(d, AccelerationUnit.METER_PER_SECOND_2);
            }
            else if (field.endsWith("mi/h^2"))
            {
                double d = Double.parseDouble(field.substring(0, field.length() - 6).trim());
                return new Acceleration(d, AccelerationUnit.MILE_PER_HOUR_2);
            }
            else if (field.endsWith("ft/s^2"))
            {
                double d = Double.parseDouble(field.substring(0, field.length() - 6).trim());
                return new Acceleration(d, AccelerationUnit.FOOT_PER_SECOND_2);
            }
        }
        catch (Exception exception)
        {
            throw new IllegalArgumentException("Error parsing Acceleration " + field, exception);
        }
        throw new IllegalArgumentException("Error parsing Acceleration " + field);
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final Acceleration Acceleration) throws IllegalArgumentException
    {
        // km/h\^2|m/s\^2|mi/h\^2|ft/s\^2
        try
        {
            if (Acceleration.getUnit().equals(AccelerationUnit.KM_PER_HOUR_2))
            {
                return Acceleration.getInUnit() + " km/h^2";
            }
            else if (Acceleration.getUnit().equals(AccelerationUnit.METER_PER_SECOND_2))
            {
                return Acceleration.getInUnit() + " m/s^2";
            }
            else if (Acceleration.getUnit().equals(AccelerationUnit.MILE_PER_HOUR_2))
            {
                return Acceleration.getInUnit() + " mi/h^2";
            }
            else if (Acceleration.getUnit().equals(AccelerationUnit.FOOT_PER_SECOND_2))
            {
                return Acceleration.getInUnit() + " ft/s^2";
            }
        }
        catch (Exception exception)
        {
            throw new IllegalArgumentException("Error printing Acceleration " + Acceleration, exception);
        }
        return Acceleration.getSI() + " m";
    }

}
