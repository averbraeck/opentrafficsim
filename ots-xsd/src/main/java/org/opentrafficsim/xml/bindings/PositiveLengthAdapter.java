package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.logger.CategoryLogger;

/**
 * LengthAdapter converts between the XML String for a Length and the DJUnits Length. The length should be positive. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class PositiveLengthAdapter extends UnitAdapter<Length>
{
    /** {@inheritDoc} */
    @Override
    public Length unmarshal(final String field) throws IllegalArgumentException
    {
        if (field.trim().startsWith("-"))
        {
            CategoryLogger.always().error("PositiveLength cannot be negative '" + field + "'");
            throw new IllegalArgumentException("PositiveLength cannot be negative: " + field);
        }
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

    /** {@inheritDoc} */
    @Override
    public String marshal(final Length length) throws IllegalArgumentException
    {
        if (length.lt(Length.ZERO))
        {
            CategoryLogger.always().error("PositiveLength cannot be negative: " + length);
            throw new IllegalArgumentException("PositiveLength cannot be negative: " + length);
        }
        return super.marshal(length);
    }

}
