package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.xml.bindings.types.LengthBeginEndType;
import org.opentrafficsim.xml.bindings.types.LengthBeginEndType.LengthBeginEnd;

/**
 * LengthAdapter converts between the XML String for a Length and the DJUnits Length. The length should be positive.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LengthBeginEndAdapter extends ExpressionAdapter<LengthBeginEnd, LengthBeginEndType>
{

    @Override
    public LengthBeginEndType unmarshal(final String field) throws IllegalArgumentException
    {
        if (isExpression(field))
        {
            return new LengthBeginEndType(trimBrackets(field));
        }

        String clean = field.replaceAll("\\s", "").trim();

        try
        {
            if (clean.equals("BEGIN"))
            {
                return new LengthBeginEndType(new LengthBeginEnd(true, Length.ZERO));
            }

            if (clean.equals("END"))
            {
                return new LengthBeginEndType(new LengthBeginEnd(false, Length.ZERO));
            }

            if (clean.endsWith("%"))
            {
                double d = 0.01 * Double.parseDouble(clean.substring(0, clean.length() - 1).trim());
                Throw.when(d < 0.0 || d > 1.0, IllegalArgumentException.class,
                        "fraction must be between 0.0 and 1.0 (inclusive)");
                return new LengthBeginEndType(new LengthBeginEnd(d));
            }

            if (clean.matches("([0]?\\.?\\d+)|[1](\\.0*)"))
            {
                double d = Double.parseDouble(clean);
                return new LengthBeginEndType(new LengthBeginEnd(d));
            }

            boolean begin = true;
            if (clean.startsWith("END-"))
            {
                begin = false;
                clean = clean.substring(4);
            }

            Length length = Length.valueOf(clean);
            return new LengthBeginEndType(new LengthBeginEnd(begin, length));
        }
        catch (Exception exception)
        {
            throw new IllegalArgumentException("Error parsing LengthBeginEnd " + field, exception);
        }
    }

    @Override
    public String marshal(final LengthBeginEndType value)
    {
        return marshal(value, this::marshalValue);
    }

    /**
     * Marshal value (not an expression).
     * @param lbe value.
     * @return marshaled value.
     * @throws IllegalArgumentException when the fraction is out of bounds
     */
    private String marshalValue(final LengthBeginEnd lbe) throws IllegalArgumentException
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
