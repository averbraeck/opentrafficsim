package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.xml.bindings.types.AccelerationType;

/**
 * AccelerationAdapter converts between the XML String for an Acceleration and the DJUnits Acceleration.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class AccelerationAdapter extends ScalarAdapter<Acceleration, AccelerationType>
{
    
    /** {@inheritDoc} */
    @Override
    public AccelerationType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            return new AccelerationType(trimBrackets(field));
        }
        try
        {
            return new AccelerationType(Acceleration.valueOf(field));
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing Acceleration '" + field + "'");
            throw exception;
        }
    }
    
}
