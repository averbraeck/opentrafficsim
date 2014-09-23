package org.opentrafficsim.demo.ntm.trafficdemand;

import java.util.Map;

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
public class TripDemand
{

    /** information on trips: number, shortest path etc..*/
    private Map<Long, Map<Long, TripInfo>> tripInfo;
    /** starting time. */
    private DoubleScalar.Abs<TimeUnit> startTime;
    /** time period covered by this demand. */
    private DoubleScalar.Rel<TimeUnit> timeSpan;
    
    /**
     */
    public TripDemand()
    {
        super();
    }
    /**
     * @param tripInfo information for all non-empty OD-pairs
     */
    public TripDemand(final Map<Long, Map<Long, TripInfo>> tripInfo)
    {
        super();
        this.tripInfo = tripInfo;
    }

    /**
     * @return startTime.
     */
    public final DoubleScalar.Abs<TimeUnit> getStartTime()
    {
        return this.startTime;
    }
    /**
     * @param startTime set startTime.
     */
    public final void setStartTime(DoubleScalar.Abs<TimeUnit> startTime)
    {
        this.startTime = startTime;
    }
    /**
     * @return tripDemand
     */
    public final Map<Long, Map<Long, TripInfo>> getTripInfo()
    {
        return this.tripInfo;
    }

    /**
     * @return timeSpan.
     */
    public final DoubleScalar.Rel<TimeUnit> getTimeSpan()
    {
        return this.timeSpan;
    }
    /**
     * @param timeSpan set timeSpan.
     */
    public final void setTimeSpan(final DoubleScalar.Rel<TimeUnit> timeSpan)
    {
        this.timeSpan = timeSpan;
    }
    /**
     * @param tripInfo sets tripInfo
     */
    public final void setTripInfo(final Map<Long, Map<Long, TripInfo>> tripInfo)
    {
        this.tripInfo = tripInfo;
    }

    /**
     * @param origin
     * @param destination
     * @return mapDestinations a hashmap with destination as key and tripInfo as values
     */
    public Map<Long, TripInfo> getTripDemand_Origin_AllDestinations(Long origin, Long destination)
    {
        Map<Long, Map<Long, TripInfo>> demand = this.getTripInfo();
        Map<Long, TripInfo> mapDestinations = demand.get(origin);
        return mapDestinations;
    }

    /**
     * @param origin
     * @param destination
     * @return tripInfo by OD pair
     */
    public TripInfo getTripDemand_Origin_Destination(Long origin, Long destination)
    {
        Map<Long, Map<Long, TripInfo>> tripInfoAll = this.getTripInfo();
        Map<Long, TripInfo> map = tripInfoAll.get(origin);
        TripInfo info = map.get(destination);
        return info;
    }


}
