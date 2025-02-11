package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.exceptions.Throw;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.xml.bindings.types.DurationType;

/**
 * DurationAdapter converts between the XML String for a Duration and the DJUnits Duration (positive).
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class PositiveDurationAdapter extends ScalarAdapter<Duration, DurationType>
{

    /**
     * Constructor.
     */
    public PositiveDurationAdapter()
    {
        //
    }

    @Override
    public DurationType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            return new DurationType(trimBrackets(field));
        }
        try
        {
            Duration value = Duration.valueOf(field);
            Throw.when(value.lt0(), IllegalArgumentException.class, "PositiveDuration value %s is not a positive value.",
                    value);
            return new DurationType(value);
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing Duration '" + field + "'");
            throw exception;
        }
    }

    @Override
    public String marshal(final DurationType value)
    {
        Throw.when(!value.isExpression() && value.getValue().lt0(), IllegalArgumentException.class,
                "PositiveDuration value %s is not a positive value.", value.getValue());
        return super.marshal(value);
    }

}
