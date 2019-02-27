package org.opentrafficsim.xml.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.xml.bindings.types.LengthBeginEnd;

/**
 * LengthAdapter converts between the XML String for a Length and the DJUnits Length. The length should be positive. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class LengthBeginEndAdapter extends XmlAdapter<String, LengthBeginEnd>
{
    /** {@inheritDoc} */
    @Override
    public LengthBeginEnd unmarshal(final String field) throws IllegalArgumentException
    {
        String clean = field.replaceAll("\\s", "");

        try
        {
            if (clean.trim().equals("BEGIN"))
            {
                return new LengthBeginEnd(true, Length.ZERO);
            }

            if (clean.trim().equals("END"))
            {
                return new LengthBeginEnd(false, Length.ZERO);
            }

            if (clean.endsWith("%"))
            {
                double d = 0.01 * Double.parseDouble(clean.substring(0, clean.length() - 1).trim());
                Throw.when(d < 0.0 || d > 1.0, IllegalArgumentException.class,
                        "fraction must be between 0.0 and 1.0 (inclusive)");
                return new LengthBeginEnd(d);
            }

            if (clean.matches("([0]?\\.?\\d+)|[1](\\.0*)"))
            {
                double d = Double.parseDouble(clean);
                return new LengthBeginEnd(d);
            }

            boolean begin = true;
            if (clean.startsWith("END-"))
            {
                begin = false;
                clean = clean.substring(4);
            }

            // mm|cm|dm|m|dam|hm|km|mi|y|ft
            if (clean.endsWith("mm"))
            {
                double d = Double.parseDouble(clean.substring(0, clean.length() - 2).trim());
                return new LengthBeginEnd(begin, new Length(d, LengthUnit.MILLIMETER));
            }
            else if (clean.endsWith("cm"))
            {
                double d = Double.parseDouble(clean.substring(0, clean.length() - 2).trim());
                return new LengthBeginEnd(begin, new Length(d, LengthUnit.CENTIMETER));
            }
            else if (clean.endsWith("dm"))
            {
                double d = Double.parseDouble(clean.substring(0, clean.length() - 2).trim());
                return new LengthBeginEnd(begin, new Length(d, LengthUnit.DECIMETER));
            }
            else if (clean.endsWith("dam"))
            {
                double d = Double.parseDouble(clean.substring(0, clean.length() - 3).trim());
                return new LengthBeginEnd(begin, new Length(d, LengthUnit.DEKAMETER));
            }
            else if (clean.endsWith("hm"))
            {
                double d = Double.parseDouble(clean.substring(0, clean.length() - 2).trim());
                return new LengthBeginEnd(begin, new Length(d, LengthUnit.HECTOMETER));
            }
            else if (clean.endsWith("km"))
            {
                double d = Double.parseDouble(clean.substring(0, clean.length() - 2).trim());
                return new LengthBeginEnd(begin, new Length(d, LengthUnit.KILOMETER));
            }
            else if (clean.endsWith("m"))
            {
                double d = Double.parseDouble(clean.substring(0, clean.length() - 1).trim());
                return new LengthBeginEnd(begin, new Length(d, LengthUnit.METER));
            }
            else if (clean.endsWith("mi"))
            {
                double d = Double.parseDouble(clean.substring(0, clean.length() - 2).trim());
                return new LengthBeginEnd(begin, new Length(d, LengthUnit.MILE));
            }
            else if (clean.endsWith("y"))
            {
                double d = Double.parseDouble(clean.substring(0, clean.length() - 1).trim());
                return new LengthBeginEnd(begin, new Length(d, LengthUnit.YARD));
            }
            else if (clean.endsWith("ft"))
            {
                double d = Double.parseDouble(clean.substring(0, clean.length() - 2).trim());
                return new LengthBeginEnd(begin, new Length(d, LengthUnit.FOOT));
            }
        }
        catch (Exception exception)
        {
            throw new IllegalArgumentException("Error parsing LengthBeginEnd " + field, exception);
        }
        throw new IllegalArgumentException("Error parsing LengthBeginEnd " + field);
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final LengthBeginEnd lbe) throws IllegalArgumentException
    {
        if (!lbe.isAbsolute())
        {
            Throw.when(lbe.getFraction() < 0.0 || lbe.getFraction() > 1.0, IllegalArgumentException.class,
                    "fraction must be between 0.0 and 1.0 (inclusive)");
            return "" + lbe.getFraction();
        }

        if (lbe.getOffset().eq(Length.ZERO))
        {
            return lbe.isBegin() ? "BEGIN" : "END";
        }

        String prefix = lbe.isBegin() ? "" : "END-";

        // mm|cm|dm|m|dam|hm|km|mi|y|ft
        try
        {
            if (lbe.getOffset().getUnit().equals(LengthUnit.MILLIMETER))
            {
                return prefix + lbe.getOffset().getInUnit() + " mm";
            }
            else if (lbe.getOffset().getUnit().equals(LengthUnit.CENTIMETER))
            {
                return prefix + lbe.getOffset().getInUnit() + " cm";
            }
            else if (lbe.getOffset().getUnit().equals(LengthUnit.DECIMETER))
            {
                return prefix + lbe.getOffset().getInUnit() + " dm";
            }
            else if (lbe.getOffset().getUnit().equals(LengthUnit.METER))
            {
                return prefix + lbe.getOffset().getInUnit() + " m";
            }
            else if (lbe.getOffset().getUnit().equals(LengthUnit.DEKAMETER))
            {
                return prefix + lbe.getOffset().getInUnit() + " dam";
            }
            else if (lbe.getOffset().getUnit().equals(LengthUnit.HECTOMETER))
            {
                return prefix + lbe.getOffset().getInUnit() + " hm";
            }
            else if (lbe.getOffset().getUnit().equals(LengthUnit.KILOMETER))
            {
                return prefix + lbe.getOffset().getInUnit() + " km";
            }
            else if (lbe.getOffset().getUnit().equals(LengthUnit.MILE))
            {
                return prefix + lbe.getOffset().getInUnit() + " mi";
            }
            else if (lbe.getOffset().getUnit().equals(LengthUnit.YARD))
            {
                return prefix + lbe.getOffset().getInUnit() + " y";
            }
            else if (lbe.getOffset().getUnit().equals(LengthUnit.FOOT))
            {
                return prefix + lbe.getOffset().getInUnit() + " ft";
            }
        }
        catch (Exception exception)
        {
            throw new IllegalArgumentException("Error printing LengthBeginEnd   " + lbe, exception);
        }
        return prefix + lbe.getOffset().getSI() + " m";
    }

}
