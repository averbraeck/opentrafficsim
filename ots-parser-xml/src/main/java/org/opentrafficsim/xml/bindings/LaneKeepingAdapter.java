package org.opentrafficsim.xml.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.xml.bindings.types.LaneKeepingType;

/**
 * LaneKeepingAdapter to convert between XML representations of LaneKeeping and an enum type.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class LaneKeepingAdapter extends XmlAdapter<String, LaneKeepingType>
{
    /** {@inheritDoc} */
    @Override
    public LaneKeepingType unmarshal(final String field) throws IllegalArgumentException
    {
        try
        {
            String lkpStr = field.replaceAll("\\s", "").trim();
            return LaneKeepingType.valueOf(lkpStr);
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing LaneKeeping '" + field + "'");
            throw new IllegalArgumentException("Error parsing LaneKeeping " + field, exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final LaneKeepingType laneKeeping)
    {
        return laneKeeping.name();
    }

}
