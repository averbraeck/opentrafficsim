package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.xml.bindings.types.LengthBeginEndType;
import org.opentrafficsim.xml.bindings.types.LengthBeginEndType.LengthBeginEnd;

/**
 * LengthAdapter converts between the XML String for a Length and the DJUnits Length. The length should be positive.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Wouter Schakel
 */
public class LengthBeginEndAdapter extends ExpressionAdapter<LengthBeginEnd, LengthBeginEndType>
{

    /**
     * Constructor.
     */
    public LengthBeginEndAdapter()
    {
        //
    }

    @Override
    public LengthBeginEndType unmarshal(final String field) throws IllegalArgumentException
    {
        if (isExpression(field))
        {
            return new LengthBeginEndType(trimBrackets(field));
        }
        return new LengthBeginEndType(LengthBeginEndType.valueOf(field));
    }

    @Override
    public String marshal(final LengthBeginEndType value)
    {
        return marshalAsExpressionOrValue(value, LengthBeginEndAdapter::marshalValue);
    }

    /**
     * Marshal value (not an expression).
     * @param lbe value.
     * @return marshaled value.
     * @throws IllegalArgumentException when the fraction is out of bounds
     */
    private static String marshalValue(final LengthBeginEnd lbe) throws IllegalArgumentException
    {
        if (!lbe.isAbsolute())
        {
            Throw.when(lbe.getFraction() < 0.0 || lbe.getFraction() > 1.0, IllegalArgumentException.class,
                    "fraction must be between 0.0 and 1.0 (inclusive)");
            return "" + lbe.getFraction();
        }

        // Negative values, including -0.0, not allowed.
        Throw.when(Double.compare(lbe.getOffset().si, 0.0) < 0, IllegalArgumentException.class,
                "Negative offset in LengthBeginEnd %s", lbe);

        if (lbe.getOffset().eq(Length.ZERO))
        {
            return lbe.isBegin() ? "BEGIN" : "END";
        }

        String prefix = lbe.isBegin() ? "" : "END-";
        return prefix + lbe.getOffset().getInUnit() + " " + lbe.getOffset().getDisplayUnit().getDefaultTextualAbbreviation();
    }

}
