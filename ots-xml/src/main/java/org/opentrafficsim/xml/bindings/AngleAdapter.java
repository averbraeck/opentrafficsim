package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.Angle;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.xml.bindings.types.AngleType;

/**
 * AngleAdapter converts between the XML String for a Angle and the DJUnits Angle.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AngleAdapter extends ScalarAdapter<Angle, AngleType>
{

    /**
     * Constructor.
     */
    public AngleAdapter()
    {
        //
    }

    @Override
    public AngleType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            return new AngleType(trimBrackets(field));
        }
        try
        {
            return new AngleType(Angle.valueOf(field));
        }
        catch (Exception exception)
        {
            Logger.ots().error(exception, "Problem parsing Angle '" + field + "'");
            throw exception;
        }
    }

}
