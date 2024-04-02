package org.opentrafficsim.kpi.sampling.indicator;

import java.util.List;

import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.scalar.base.DoubleScalar;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.Query;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Abstract indicator which stores the last calculated value and returns it in {@code getValue()} for an equal query, start time
 * and end time.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> class of the value
 */
public abstract class AbstractIndicator<T extends DoubleScalar<?, ?>>
{

    /** Last query. */
    private Query<?, ?> lastQuery;

    /** Last start time. */
    private Time lastStartTime;

    /** Last end time. */
    private Time lastEndTime;

    /** Last value. */
    private T lastValue;

    /**
     * Get value for given query over time interval, returning earlier calculated value if possible. This method uses
     * {@code Time.ZERO} as start time.
     * @param query Query&lt;G, ?&gt;; query
     * @param endTime Time; start time of interval to calculate indicator over
     * @param trajectoryGroups List&lt;TrajectoryGroup&lt;G&gt;&gt;; group of trajectories to calculate the indicator for
     * @param <G> gtu data type
     * @return value for given query
     */
    public final <G extends GtuData> T getValue(final Query<G, ?> query, final Time endTime,
            final List<TrajectoryGroup<G>> trajectoryGroups)
    {
        return getValue(query, Time.ZERO, endTime, trajectoryGroups);
    }

    /**
     * Get value for given query over time interval, returning earlier calculated value if possible.
     * @param query Query&lt;G, ?&gt;; query
     * @param startTime Time; start time of interval to calculate indicator over
     * @param endTime Time; start time of interval to calculate indicator over
     * @param trajectoryGroups List&lt;TrajectoryGroup&lt;G&gt;&gt;; group of trajectories to calculate the indicator for
     * @param <G> gtu data type
     * @return value for given query
     */
    public final <G extends GtuData> T getValue(final Query<G, ?> query, final Time startTime, final Time endTime,
            final List<TrajectoryGroup<G>> trajectoryGroups)
    {
        Throw.whenNull(query, "Query may not be null.");
        Throw.whenNull(startTime, "Start time may not be null.");
        Throw.whenNull(endTime, "End time may not be null.");
        if (this.lastQuery == null || !this.lastQuery.equals(query) || !this.lastStartTime.equals(startTime)
                || !this.lastEndTime.equals(endTime))
        {
            this.lastQuery = query;
            this.lastStartTime = startTime;
            this.lastEndTime = endTime;
            this.lastValue = calculate(query, startTime, endTime, trajectoryGroups);
        }
        return this.lastValue;
    }

    /**
     * Calculate value for given trajectory group.
     * @param query Query&lt;G, ?&gt;; query
     * @param startTime Time; start time of interval to calculate indicator over
     * @param endTime Time; start time of interval to calculate indicator over
     * @param trajectoryGroups List&lt;TrajectoryGroup&lt;G&gt;&gt;; group of trajectories to calculate the indicator for
     * @param <G> gtu data type
     * @return value for given trajectory group
     */
    protected abstract <G extends GtuData> T calculate(Query<G, ?> query, Time startTime, Time endTime,
            List<TrajectoryGroup<G>> trajectoryGroups);

}
