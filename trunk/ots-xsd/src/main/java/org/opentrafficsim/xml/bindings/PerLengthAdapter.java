package org.opentrafficsim.xml.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djunits.unit.LinearDensityUnit;
import org.djunits.value.vdouble.scalar.LinearDensity;

/**
 * PerLengthAdapter converts between the XML String for a LinearDensity and the DJUnits LinearDensity. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class PerLengthAdapter extends XmlAdapter<String, LinearDensity>
{
    /** {@inheritDoc} */
    @Override
    public LinearDensity unmarshal(final String field) throws IllegalArgumentException
    {
        // /mm|/cm|/dm|/m|/dam|/hm|/km|/mi|/y|/ft
        String trimmed = field.replaceAll(" ", "");
        try
        {
            if (trimmed.endsWith("/mm"))
            {
                double d = Double.parseDouble(trimmed.substring(0, trimmed.length() - 3).trim());
                return new LinearDensity(d, LinearDensityUnit.PER_MILLIMETER);
            }
            else if (trimmed.endsWith("/cm"))
            {
                double d = Double.parseDouble(trimmed.substring(0, trimmed.length() - 3).trim());
                return new LinearDensity(d, LinearDensityUnit.PER_CENTIMETER);
            }
            else if (trimmed.endsWith("/dm"))
            {
                double d = Double.parseDouble(trimmed.substring(0, trimmed.length() - 3).trim());
                return new LinearDensity(d, LinearDensityUnit.PER_DECIMETER);
            }
            else if (trimmed.endsWith("/dam"))
            {
                double d = Double.parseDouble(trimmed.substring(0, trimmed.length() - 4).trim());
                return new LinearDensity(d, LinearDensityUnit.PER_DEKAMETER);
            }
            else if (trimmed.endsWith("/hm"))
            {
                double d = Double.parseDouble(trimmed.substring(0, trimmed.length() - 3).trim());
                return new LinearDensity(d, LinearDensityUnit.PER_HECTOMETER);
            }
            else if (trimmed.endsWith("/km"))
            {
                double d = Double.parseDouble(trimmed.substring(0, trimmed.length() - 3).trim());
                return new LinearDensity(d, LinearDensityUnit.PER_KILOMETER);
            }
            else if (trimmed.endsWith("/m"))
            {
                double d = Double.parseDouble(trimmed.substring(0, trimmed.length() - 2).trim());
                return new LinearDensity(d, LinearDensityUnit.PER_METER);
            }
            else if (trimmed.endsWith("/mi"))
            {
                double d = Double.parseDouble(trimmed.substring(0, trimmed.length() - 3).trim());
                return new LinearDensity(d, LinearDensityUnit.PER_MILE);
            }
            else if (trimmed.endsWith("/y"))
            {
                double d = Double.parseDouble(trimmed.substring(0, trimmed.length() - 2).trim());
                return new LinearDensity(d, LinearDensityUnit.PER_YARD);
            }
            else if (trimmed.endsWith("/ft"))
            {
                double d = Double.parseDouble(trimmed.substring(0, trimmed.length() - 3).trim());
                return new LinearDensity(d, LinearDensityUnit.PER_FOOT);
            }
        }
        catch (Exception exception)
        {
            throw new IllegalArgumentException("Error parsing LinearDensity " + trimmed, exception);
        }
        throw new IllegalArgumentException("Error parsing LinearDensity " + trimmed);
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final LinearDensity LinearDensity) throws IllegalArgumentException
    {
        // /mm|/cm|/dm|/m|/dam|/hm|/km|/mi|/y|/ft
        try
        {
            if (LinearDensity.getUnit().equals(LinearDensityUnit.PER_MILLIMETER))
            {
                return LinearDensity.getInUnit() + " /mm";
            }
            else if (LinearDensity.getUnit().equals(LinearDensityUnit.PER_CENTIMETER))
            {
                return LinearDensity.getInUnit() + " /cm";
            }
            else if (LinearDensity.getUnit().equals(LinearDensityUnit.PER_DECIMETER))
            {
                return LinearDensity.getInUnit() + " /dm";
            }
            else if (LinearDensity.getUnit().equals(LinearDensityUnit.PER_METER))
            {
                return LinearDensity.getInUnit() + " /m";
            }
            else if (LinearDensity.getUnit().equals(LinearDensityUnit.PER_DEKAMETER))
            {
                return LinearDensity.getInUnit() + " /dam";
            }
            else if (LinearDensity.getUnit().equals(LinearDensityUnit.PER_HECTOMETER))
            {
                return LinearDensity.getInUnit() + " /hm";
            }
            else if (LinearDensity.getUnit().equals(LinearDensityUnit.PER_KILOMETER))
            {
                return LinearDensity.getInUnit() + " /km";
            }
            else if (LinearDensity.getUnit().equals(LinearDensityUnit.PER_MILE))
            {
                return LinearDensity.getInUnit() + " /mi";
            }
            else if (LinearDensity.getUnit().equals(LinearDensityUnit.PER_YARD))
            {
                return LinearDensity.getInUnit() + " /y";
            }
            else if (LinearDensity.getUnit().equals(LinearDensityUnit.PER_FOOT))
            {
                return LinearDensity.getInUnit() + " /ft";
            }
        }
        catch (Exception exception)
        {
            throw new IllegalArgumentException("Error printing LinearDensity " + LinearDensity, exception);
        }
        return LinearDensity.getSI() + " m";
    }

}
