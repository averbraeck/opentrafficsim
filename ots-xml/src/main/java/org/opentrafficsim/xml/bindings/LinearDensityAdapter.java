package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.LinearDensity;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.xml.bindings.types.LinearDensityType;

/**
 * LinearDensityAdapter converts between the XML String for a LinearDensity and the DJUnits LinearDensity.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LinearDensityAdapter extends ScalarAdapter<LinearDensity, LinearDensityType>
{

    /**
     * Constructor.
     */
    public LinearDensityAdapter()
    {
        //
    }

    @Override
    public LinearDensityType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            return new LinearDensityType(trimBrackets(field));
        }
        try
        {
            return new LinearDensityType(LinearDensity.valueOf(field));
        }
        catch (Exception exception)
        {
            Logger.ots().error(exception, "Problem parsing LinearDensity '" + field + "'");
            throw exception;
        }
    }

}
