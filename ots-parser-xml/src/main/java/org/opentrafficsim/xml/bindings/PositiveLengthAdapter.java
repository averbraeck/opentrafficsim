package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.xml.bindings.types.LengthType;

/**
 * LengthAdapter converts between the XML String for a Length and the DJUnits Length (positive).
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class PositiveLengthAdapter extends ScalarAdapter<Length, LengthType>
{

    @Override
    public LengthType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            return new LengthType(trimBrackets(field));
        }
        try
        {
            Length value = Length.valueOf(field);
            Throw.when(value.lt0(), IllegalArgumentException.class, "PositiveLength value %s is not a positive value.", value);
            return new LengthType(value);
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing Length '" + field + "'");
            throw exception;
        }
    }

    @Override
    public String marshal(final LengthType value)
    {
        Throw.when(!value.isExpression() && value.getValue().lt0(), IllegalArgumentException.class,
                "PositiveLength value %s is not a positive value.", value.getValue());
        return super.marshal(value);
    }

}
