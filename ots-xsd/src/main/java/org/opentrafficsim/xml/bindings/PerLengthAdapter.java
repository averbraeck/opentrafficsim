package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djutils.logger.CategoryLogger;

/**
 * PerLengthAdapter converts between the XML String for a LinearDensity and the DJUnits LinearDensity.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class PerLengthAdapter extends UnitAdapter<LinearDensity>
{
    /** {@inheritDoc} */
    @Override
    public LinearDensity unmarshal(final String field) throws IllegalArgumentException
    {
        try
        {
            return LinearDensity.valueOf(field);
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing PerLength (LinearDensity) '" + field + "'");
            throw exception;
        }
    }

}
