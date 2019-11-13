package org.opentrafficsim.demo.ntm.trafficdemand;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 12 Sep 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class FractionOfTripDemandByTimeSegment
{

    /** StartTime of the segment in Calendar Time. */
    private Time timeSinceMidnight;

    /** Length of a time segment. */
    private Duration duration;

    /** Relative amount of trips. */
    private double shareOfDemand;

    /**
     * @param timeSinceMidnight Time; by time of day (HH:MM:SS)
     * @param duration Duration; length of this time segment
     * @param shareOfDemand double; amount of trips of this segment relatively to the total simulation period
     */
    public FractionOfTripDemandByTimeSegment(final Time timeSinceMidnight, final Duration duration, final double shareOfDemand)
    {
        this.timeSinceMidnight = timeSinceMidnight;
        this.duration = duration;
        this.shareOfDemand = shareOfDemand;
    }

    /**
     * @return shareOfDemand
     */
    public final double getShareOfDemand()
    {
        return this.shareOfDemand;
    }

    /**
     * @param shareOfDemand double; set shareOfDemand
     */
    public final void setShareOfDemand(final double shareOfDemand)
    {
        this.shareOfDemand = shareOfDemand;
    }

    /**
     * @return timeSinceMidnight
     */
    public final Time getTimeSinceMidnight()
    {
        return this.timeSinceMidnight;
    }

    /**
     * @param timeSinceMidnight Time; set timeSinceMidnight
     */
    public final void setTimeSinceMidnight(final Time timeSinceMidnight)
    {
        this.timeSinceMidnight = timeSinceMidnight;
    }

    /**
     * @return duration
     */
    public final Duration getDuration()
    {
        return this.duration;
    }

    /**
     * @param duration Duration; set duration
     */
    public final void setDuration(final Duration duration)
    {
        this.duration = duration;
    }

}
