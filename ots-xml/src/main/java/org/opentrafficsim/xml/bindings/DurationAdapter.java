package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.xml.bindings.types.DurationType;

/**
 * DurationAdapter converts between the XML String for a Duration and the DJUnits Duration.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DurationAdapter extends ScalarAdapter<Duration, DurationType>
{

    /**
     * Constructor.
     */
    public DurationAdapter()
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
            return new DurationType(Duration.valueOf(field));
        }
        catch (Exception exception)
        {
            Logger.ots().error(exception, "Problem parsing Duration '" + field + "'");
            throw exception;
        }
    }

}
