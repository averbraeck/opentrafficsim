package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.xml.bindings.types.LengthType;

/**
 * PositiveLengthAdapter converts between the XML String for a Length and the DJUnits Length. The length should be positive.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class PositiveLengthAdapter extends ScalarAdapter<Length, LengthType>
{
    
    /** {@inheritDoc} */
    @Override
    public LengthType unmarshal(final String field) throws IllegalArgumentException
    {
        if (isExpression(field))
        {
            return new LengthType(trimBrackets(field));
        }
        if (field.trim().startsWith("-"))
        {
            CategoryLogger.always().error("PositiveLength cannot be negative '" + field + "'");
            throw new IllegalArgumentException("PositiveLength cannot be negative: " + field);
        }
        try
        {
            return new LengthType(Length.valueOf(field));
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing Length '" + field + "'");
            throw exception;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final LengthType length) throws IllegalArgumentException
    {
        Throw.whenNull(length, "Marshalling scalar with unit: argument contains null value");
        if (!length.isExpression() && length.getValue().lt(Length.ZERO))
        {
            CategoryLogger.always().error("PositiveLength cannot be negative: " + length);
            throw new IllegalArgumentException("PositiveLength cannot be negative: " + length);
        }
        return super.marshal(length);
    }

}
