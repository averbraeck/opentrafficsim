package org.opentrafficsim.road.network.factory.xml.network;

import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;

/**
 * LaneParser parses XML classes related to objects on lanes. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public final class LaneParser
{
    /** */
    private LaneParser()
    {
        // utility class
    }

    /**
     * @param lkpStr String; the lane keeping policy string.
     * @return the lane keeping policy.
     * @throws NetworkException in case of unknown policy.
     */
    public static LaneKeepingPolicy parseLaneKeepingPolicy(final String lkpStr) throws NetworkException
    {
        if (lkpStr.equals("KEEPRIGHT"))
        {
            return LaneKeepingPolicy.KEEPRIGHT;
        }
        else if (lkpStr.equals("KEEPLEFT"))
        {
            return LaneKeepingPolicy.KEEPLEFT;
        }
        else if (lkpStr.equals("KEEPLANE"))
        {
            return LaneKeepingPolicy.KEEPLANE;
        }
        throw new NetworkException("Unknown lane keeping policy string: " + lkpStr);
    }


}

