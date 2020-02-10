package org.opentrafficsim.xml.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.djutils.logger.CategoryLogger;
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

            Length length = Length.valueOf(clean);
            return new LengthBeginEnd(begin, length);
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing LengthBeginEnd '" + field + "'");
            throw new IllegalArgumentException("Error parsing LengthBeginEnd " + field, exception);
        }
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
        return prefix + lbe.getOffset().getInUnit() + " " + lbe.getOffset().getDisplayUnit().getDefaultTextualAbbreviation();
    }

}
