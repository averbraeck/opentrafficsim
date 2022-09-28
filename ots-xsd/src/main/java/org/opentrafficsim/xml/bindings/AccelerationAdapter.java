package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djutils.logger.CategoryLogger;

/**
 * AccelerationAdapter converts between the XML String for an Acceleration and the DJUnits Acceleration.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class AccelerationAdapter extends UnitAdapter<Acceleration>
{
    /** {@inheritDoc} */
    @Override
    public Acceleration unmarshal(final String field) throws IllegalArgumentException
    {
        try
        {
            return Acceleration.valueOf(field);
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing Acceleration '" + field + "'");
            throw exception;
        }
    }
}
