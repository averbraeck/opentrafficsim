package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.logger.CategoryLogger;

/**
 * SignedLengthAdapter converts between the XML String for a Length and the DJUnits Length. The length can be positive or
 * negative. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class LengthAdapter extends UnitAdapter<Length>
{
    /** {@inheritDoc} */
    @Override
    public Length unmarshal(final String field) throws IllegalArgumentException
    {
        try
        {
            return Length.valueOf(field);
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing Length '" + field + "'");
            throw exception;
        }
    }

}
