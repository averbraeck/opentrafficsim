package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.logger.CategoryLogger;

/**
 * DurationAdapter converts between the XML String for a Duration and the DJUnits Duration.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class DurationAdapter extends UnitAdapter<Duration>
{
    /** {@inheritDoc} */
    @Override
    public Duration unmarshal(final String field) throws IllegalArgumentException
    {
        try
        {
            return Duration.valueOf(field);
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing Duration '" + field + "'");
            throw exception;
        }
    }

}
