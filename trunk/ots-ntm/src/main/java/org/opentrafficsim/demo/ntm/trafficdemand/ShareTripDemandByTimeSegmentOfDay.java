package org.opentrafficsim.demo.ntm.trafficdemand;

import java.util.Calendar;

import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 12 Sep 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class ShareTripDemandByTimeSegmentOfDay
{

    /** startTime of the segment in Calendar Time */
    DoubleScalar.Abs<TimeUnit> timeSinceMidnight;

    /** */
    DoubleScalar.Rel<TimeUnit> duration;

    /** */
    double shareOfDemand;

    /**
     * @param timeSinceMidnight
     * @param duration
     * @param shareOfDemand
     */

    public ShareTripDemandByTimeSegmentOfDay(DoubleScalar.Abs<TimeUnit> timeSinceMidnight,
            DoubleScalar.Rel<TimeUnit> duration, double shareOfDemand)
    {
        this.timeSinceMidnight = timeSinceMidnight;
        this.duration = duration;
        this.shareOfDemand = shareOfDemand;
    }

    /**
     * @return shareOfDemand
     */
    public double getShareOfDemand()
    {
        return this.shareOfDemand;
    }

    /**
     * @param shareOfDemand set shareOfDemand
     */
    public void setShareOfDemand(double shareOfDemand)
    {
        this.shareOfDemand = shareOfDemand;
    }

    /**
     * @return timeSinceMidnight
     */
    public DoubleScalar.Abs<TimeUnit> getTimeSinceMidnight()
    {
        return this.timeSinceMidnight;
    }

    /**
     * @param timeSinceMidnight set timeSinceMidnight
     */
    public void setTimeSinceMidnight(DoubleScalar.Abs<TimeUnit> timeSinceMidnight)
    {
        this.timeSinceMidnight = timeSinceMidnight;
    }

    /**
     * @return duration
     */
    public DoubleScalar.Rel<TimeUnit> getDuration()
    {
        return this.duration;
    }

    /**
     * @param duration set duration
     */
    public void setDuration(DoubleScalar.Rel<TimeUnit> duration)
    {
        this.duration = duration;
    }

}
