package org.opentrafficsim.xml.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;

/**
 * SignedLengthAdapter converts between the XML String for a Length and the DJUnits Length. The length can be positive or
 * negative. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class SignedLengthAdapter extends XmlAdapter<String, Length>
{
    /** {@inheritDoc} */
    @Override
    public Length unmarshal(final String field) throws IllegalArgumentException
    {
        // mm|cm|dm|m|dam|hm|km|mi|y|ft
        try
        {
            if (field.endsWith("mm"))
            {
                double d = Double.parseDouble(field.substring(0, field.length() - 2).trim());
                return new Length(d, LengthUnit.MILLIMETER);
            }
            else if (field.endsWith("cm"))
            {
                double d = Double.parseDouble(field.substring(0, field.length() - 2).trim());
                return new Length(d, LengthUnit.CENTIMETER);
            }
            else if (field.endsWith("dm"))
            {
                double d = Double.parseDouble(field.substring(0, field.length() - 2).trim());
                return new Length(d, LengthUnit.DECIMETER);
            }
            else if (field.endsWith("dam"))
            {
                double d = Double.parseDouble(field.substring(0, field.length() - 3).trim());
                return new Length(d, LengthUnit.DEKAMETER);
            }
            else if (field.endsWith("hm"))
            {
                double d = Double.parseDouble(field.substring(0, field.length() - 2).trim());
                return new Length(d, LengthUnit.HECTOMETER);
            }
            else if (field.endsWith("km"))
            {
                double d = Double.parseDouble(field.substring(0, field.length() - 2).trim());
                return new Length(d, LengthUnit.KILOMETER);
            }
            else if (field.endsWith("m"))
            {
                double d = Double.parseDouble(field.substring(0, field.length() - 1).trim());
                return new Length(d, LengthUnit.METER);
            }
            else if (field.endsWith("mi"))
            {
                double d = Double.parseDouble(field.substring(0, field.length() - 2).trim());
                return new Length(d, LengthUnit.MILE);
            }
            else if (field.endsWith("y"))
            {
                double d = Double.parseDouble(field.substring(0, field.length() - 1).trim());
                return new Length(d, LengthUnit.YARD);
            }
            else if (field.endsWith("ft"))
            {
                double d = Double.parseDouble(field.substring(0, field.length() - 2).trim());
                return new Length(d, LengthUnit.FOOT);
            }
        }
        catch (Exception exception)
        {
            throw new IllegalArgumentException("Error parsing Length " + field, exception);
        }
        throw new IllegalArgumentException("Error parsing Length " + field);
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final Length length) throws IllegalArgumentException
    {
        // mm|cm|dm|m|dam|hm|km|mi|y|ft
        try
        {
            if (length.getUnit().equals(LengthUnit.MILLIMETER))
            {
                return length.getInUnit() + " mm";
            }
            else if (length.getUnit().equals(LengthUnit.CENTIMETER))
            {
                return length.getInUnit() + " cm";
            }
            else if (length.getUnit().equals(LengthUnit.DECIMETER))
            {
                return length.getInUnit() + " dm";
            }
            else if (length.getUnit().equals(LengthUnit.METER))
            {
                return length.getInUnit() + " m";
            }
            else if (length.getUnit().equals(LengthUnit.DEKAMETER))
            {
                return length.getInUnit() + " dam";
            }
            else if (length.getUnit().equals(LengthUnit.HECTOMETER))
            {
                return length.getInUnit() + " hm";
            }
            else if (length.getUnit().equals(LengthUnit.KILOMETER))
            {
                return length.getInUnit() + " km";
            }
            else if (length.getUnit().equals(LengthUnit.MILE))
            {
                return length.getInUnit() + " mi";
            }
            else if (length.getUnit().equals(LengthUnit.YARD))
            {
                return length.getInUnit() + " y";
            }
            else if (length.getUnit().equals(LengthUnit.FOOT))
            {
                return length.getInUnit() + " ft";
            }
        }
        catch (Exception exception)
        {
            throw new IllegalArgumentException("Error printing Length " + length, exception);
        }
        return length.getSI() + " m";
    }

}
