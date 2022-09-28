package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.Time;
import org.djutils.logger.CategoryLogger;

/**
 * TimeAdapter converts between the XML String for a Time and the DJUnits Time.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class TimeAdapter extends UnitAdapter<Time>
{
    /** {@inheritDoc} */
    @Override
    public Time unmarshal(final String field) throws IllegalArgumentException
    {
        try
        {
            return Time.valueOf(field);
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing Time '" + field + "'");
            throw exception;
        }
    }

}
