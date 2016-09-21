package org.opentrafficsim.road.network.sampling;

import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 21, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class Query
{

    private final List<TimePair> timePairs = new ArrayList<>();

    private final List<LaneXPair> laneXPairs = new ArrayList<>();

    public abstract boolean accept(Trajectory trajectory);

    class TimePair
    {
        Time tStart, tEnd;
    }

    class LaneXPair
    {
        Lane lane;

        LongitudinalDirectionality direction;

        Length xStart, xEnd;
    }
}
