package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.logger.CategoryLogger;

/**
 * LengthAdapter converts between the XML String for a Length and the DJUnits Length. The length should be positive.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
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
