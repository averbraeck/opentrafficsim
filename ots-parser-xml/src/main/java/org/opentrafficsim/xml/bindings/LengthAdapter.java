package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.xml.bindings.types.LengthType;

/**
 * LengthAdapter converts between the XML String for a Length and the DJUnits Length.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class LengthAdapter extends ScalarAdapter<Length, LengthType>
{
    
    /** {@inheritDoc} */
    @Override
    public LengthType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            return new LengthType(trimBrackets(field));
        }
        try
        {
            return new LengthType(Length.valueOf(field));
        }
        catch (Exception exception)
        {
            throw exception;
        }
    }

}
