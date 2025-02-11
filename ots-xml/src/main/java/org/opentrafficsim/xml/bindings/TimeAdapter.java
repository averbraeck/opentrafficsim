package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.Time;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.xml.bindings.types.TimeType;

/**
 * TimeAdapter converts between the XML String for a Time and the DJUnits Time.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TimeAdapter extends ScalarAdapter<Time, TimeType>
{

    /**
     * Constructor.
     */
    public TimeAdapter()
    {
        //
    }

    @Override
    public TimeType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            return new TimeType(trimBrackets(field));
        }
        try
        {
            return new TimeType(Time.valueOf(field));
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing Time '" + field + "'");
            throw exception;
        }
    }

}
