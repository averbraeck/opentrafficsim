package org.opentrafficsim.road.network.sampling.indicator;

import org.djunits.unit.Unit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.core.Throw;
import org.opentrafficsim.road.network.sampling.Query;

/**
 * Abstract indicator which stores the last calculated value and returns it in {@code getValue()} for an equal query, start time
 * and end time.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 okt. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <U> class of the unit
 * @param <T> class of the value
 */
// TODO standard deviation, percentiles, min/max
// XXX think about using Tally and Persistent for some of the indicators. Maybe extend Indicator to TallyIndicator?
// XXX Persistent is already a time-weighed indicator that calculates mean, std, min, max, and confidence interval.
public abstract class AbstractIndicator<U extends Unit<U>, T extends DoubleScalar<U>>
{

    /** Last query. */
    private Query lastQuery;

    /** Last start time. */
    private Duration lastStartTime;

    /** Last end time. */
    private Duration lastEndTime;

    /** Last value. */
    private T lastValue;

    /**
     * Get value for given query until given time, returning earlier calculated value if possible.
     * @param query query
     * @param endTime start time of interval to calculate indicator over
     * @return value for given query
     */
    @SuppressWarnings("checkstyle:designforextension")
    public T getValue(final Query query, final Duration endTime)
    {
        return getValue(query, Duration.ZERO, endTime);
    }

    /**
     * Get value for given query over time interval, returning earlier calculated value if possible.
     * @param query query
     * @param startTime start time of interval to calculate indicator over
     * @param endTime start time of interval to calculate indicator over
     * @return value for given query
     */
    public final T getValue(final Query query, final Duration startTime, final Duration endTime)
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
            this.lastValue = calculate(query, startTime, endTime);
        }
        return this.lastValue;
    }

    /**
     * Calculate value for given query until given time.
     * @param query query
     * @param endTime start time of interval to calculate indicator over
     * @return value for given query
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected T calculate(final Query query, final Duration endTime)
    {
        return calculate(query, Duration.ZERO, endTime);
    }

    /**
     * Calculate value for given query over time interval.
     * @param query query
     * @param startTime start time of interval to calculate indicator over
     * @param endTime start time of interval to calculate indicator over
     * @return value for given query
     */
    protected abstract T calculate(Query query, Duration startTime, Duration endTime);

}
