package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.xml.bindings.types.SpeedType;

/**
 * SpeedAdapter converts between the XML String for a Speed and the DJUnits Speed.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class SpeedAdapter extends ScalarAdapter<Speed, SpeedType>
{

    /**
     * Constructor.
     */
    public SpeedAdapter()
    {
        //
    }

    @Override
    public SpeedType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            return new SpeedType(trimBrackets(field));
        }
        try
        {
            return new SpeedType(Speed.valueOf(field));
        }
        catch (Exception exception)
        {
            Logger.ots().error(exception, "Problem parsing Speed '" + field + "'");
            throw exception;
        }
    }

}
