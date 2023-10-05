package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.xml.bindings.types.TimeType;

/**
 * TimeAdapter converts between the XML String for a Time and the DJUnits Time (positive).
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class PositiveTimeAdapter extends ScalarAdapter<Time, TimeType>
{

    /** {@inheritDoc} */
    @Override
    public TimeType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            return new TimeType(trimBrackets(field));
        }
        try
        {
            Time value = Time.valueOf(field);
            Throw.when(value.lt0(), IllegalArgumentException.class, "PositiveTime value %s is not a positive value.", value);
            return new TimeType(value);
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing Time '" + field + "'");
            throw exception;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final TimeType value)
    {
        Throw.when(!value.isExpression() && value.getValue().lt0(), IllegalArgumentException.class,
                "PositiveTime value %s is not a positive value.", value.getValue());
        return super.marshal(value);
    }

}
