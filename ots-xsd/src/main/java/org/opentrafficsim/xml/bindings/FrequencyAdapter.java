package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.Frequency;
import org.djutils.logger.CategoryLogger;

/**
 * FrequencyAdapter converts between the XML String for a Frequency and the DJUnits Frequency.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class FrequencyAdapter extends UnitAdapter<Frequency>
{
    /** {@inheritDoc} */
    @Override
    public Frequency unmarshal(final String field) throws IllegalArgumentException
    {
        try
        {
            return Frequency.valueOf(field);
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing Frequency '" + field + "'");
            throw exception;
        }
    }

}
