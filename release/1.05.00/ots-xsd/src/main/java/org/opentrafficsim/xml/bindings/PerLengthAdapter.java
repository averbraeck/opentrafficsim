package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djutils.logger.CategoryLogger;

/**
 * PerLengthAdapter converts between the XML String for a LinearDensity and the DJUnits LinearDensity. <br>
 * <br>
 * Copyright (c) 2003-2022 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
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
