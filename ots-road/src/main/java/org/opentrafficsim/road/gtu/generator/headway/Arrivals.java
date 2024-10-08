package org.opentrafficsim.road.gtu.generator.headway;

import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Time;

/**
 * Interface for arrivals in an {@code ArrivalsHeadwayGenerator}. Arrivals are defined as a piece-wise linear frequency over
 * time.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface Arrivals
{

    /**
     * Returns the demand at given time, which may be the sum of child objects. The input {@code sliceStart} is used to resolve
     * the value at a time slice boundary in case of a stepwise (discontinuous) demand pattern. If {@code sliceStart = true} and
     * {@code time} is a slice boundary, the demand value for <i>after</i> the slice boundary should be returned. In that case,
     * the caller is processing a time slice after {@code time}, hence it's the slice start. If {@code sliceStart = false} the
     * demand value of before the slice boundary should be returned. For continuous demand patterns, {@code sliceStart} can be
     * ignored.
     * @param time simulation time
     * @param sliceStart whether the time is at the start of an arbitrary time slice
     * @return returns the total demand for branching nodes, or the demand at a leaf node, at the given time
     */
    Frequency getFrequency(Time time, boolean sliceStart);

    /**
     * Returns the start time of the next time slice after the given time or {@code null} if no such slice exists. The next time
     * slice starts as soon as the current slice ends, where each slice has it's own linear (or constant) demand. Thus, any
     * change of slope in the demand pattern initiates a new slice. If {@code time} is equal to a time slice boundary, the next
     * value should be returned.
     * @param time time after which the first slice start time is requested
     * @return start time of the next time slice after the given time or {@code null} if no such slice exists
     */
    Time nextTimeSlice(Time time);

}
