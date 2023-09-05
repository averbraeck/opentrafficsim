package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.Frequency;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.xml.bindings.types.FrequencyType;

/**
 * FrequencyAdapter converts between the XML String for a Frequency and the DJUnits Frequency.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class FrequencyAdapter extends ScalarAdapter<Frequency, FrequencyType>
{
    
    /** {@inheritDoc} */
    @Override
    public FrequencyType unmarshal(final String field) throws IllegalArgumentException
    {
        if (isExpression(field))
        {
            return new FrequencyType(trimBrackets(field));
        }
        try
        {
            return new FrequencyType(Frequency.valueOf(field));
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing Frequency '" + field + "'");
            throw exception;
        }
    }

}
