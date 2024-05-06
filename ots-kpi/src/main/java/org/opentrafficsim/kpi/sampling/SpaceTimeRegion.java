package org.opentrafficsim.kpi.sampling;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.LaneData;

/**
 * Defines a rectangular region over space and time on a lane.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <L> lane data type
 * @param lane L; lane
 * @param startPosition Length; start position
 * @param endPosition Length; end position
 * @param startTime Time; start time
 * @param endTime Time; end time
 */
public record SpaceTimeRegion<L extends LaneData<L>>(L lane, Length startPosition, Length endPosition, Time startTime,
        Time endTime)
{

    /**
     * Creates a space-time region.
     * @param lane L; lane
     * @param startPosition Length; start position
     * @param endPosition Length; end position
     * @param startTime Time; start time
     * @param endTime Time; end time
     * @throws IllegalArgumentException if start time is larger than end time
     */
    public SpaceTimeRegion
    {
        Throw.whenNull(startPosition, "Start position may not be null.");
        Throw.whenNull(endPosition, "End position may not be null.");
        Throw.whenNull(startTime, "Start time may not be null.");
        Throw.whenNull(endTime, "End time may not be null.");
        Throw.when(endPosition.lt(startPosition), IllegalArgumentException.class,
                "End position should be greater than start position.");
        Throw.when(endTime.lt(startTime), IllegalArgumentException.class, "End time should be greater than start time.");
    }

}
