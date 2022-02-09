package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djutils.logger.CategoryLogger;

/**
 * AccelerationAdapter converts between the XML String for an Acceleration and the DJUnits Acceleration. <br>
 * <br>
 * Copyright (c) 2003-2022 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
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
