package org.opentrafficsim.xml.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.xml.bindings.types.LaneKeepingType;

/**
 * LaneKeepingAdapter to convert between XML representations of LaneKeeping and an enum type. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class LaneKeepingAdapter extends XmlAdapter<String, LaneKeepingType>
{
    /** {@inheritDoc} */
    @Override
    public LaneKeepingType unmarshal(final String field) throws IllegalArgumentException
    {
        try
        {
            String lkpStr = field.replaceAll("\\s", "");
            if (lkpStr.equals("KEEPRIGHT"))
            {
                return LaneKeepingType.KEEPRIGHT;
            }
            else if (lkpStr.equals("KEEPLEFT"))
            {
                return LaneKeepingType.KEEPLEFT;
            }
            else if (lkpStr.equals("KEEPLANE"))
            {
                return LaneKeepingType.KEEPLANE;
            }
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing LaneKeeping '" + field + "'");
            throw new IllegalArgumentException("Error parsing LaneKeeping " + field, exception);
        }
        CategoryLogger.always().error("Problem parsing LaneKeeping '" + field + "'");
        throw new IllegalArgumentException("Error parsing LaneKeeping " + field);
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final LaneKeepingType laneKeeping) throws IllegalArgumentException
    {
        if (laneKeeping.equals(LaneKeepingType.KEEPRIGHT))
            return "KEEPRIGHT";
        if (laneKeeping.equals(LaneKeepingType.KEEPLEFT))
            return "KEEPLEFT";
        if (laneKeeping.equals(LaneKeepingType.KEEPLANE))
            return "KEEPLANE";
        throw new IllegalArgumentException("Error parsing LaneKeeping " + laneKeeping);
    }

}
