package org.opentrafficsim.demo.ntm.trafficdemand;

import java.util.HashMap;
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
 * @param <TripInformation>
 */
public class TripDemand<TripInformation>
{

    /** information on trips: number, shortest path etc.. */
    private Map<String, Map<String, TripInformation>> tripInfo;

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
    public TripDemand(final Map<String, Map<String, TripInformation>> tripInfo)
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
    public final void setStartTime(final DoubleScalar.Abs<TimeUnit> startTime)
    {
        this.startTime = startTime;
    }

    /**
     * @return tripDemand
     */
    public final Map<String, Map<String, TripInformation>> getTripInfo()
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
    public final void setTripInfo(final Map<String, Map<String, TripInformation>> tripInfo)
    {
        this.tripInfo = tripInfo;
    }

    /**
     * @param origin
     * @param destination
     * @return mapDestinations a hashmap with destination as key and tripInfo as values
     */
    public final Map<String, TripInformation> getTripDemandOriginToAllDestinations(final String origin,
            final String destination)
    {
        Map<String, Map<String, TripInformation>> demand = this.getTripInfo();
        Map<String, TripInformation> mapDestinations = demand.get(origin);
        return mapDestinations;
    }

    /**
     * @param origin
     * @param destination
     * @return tripInfo by OD pair
     */
    public final TripInformation getTripDemandOriginToDestination(final String origin, final String destination)
    {
        Map<String, Map<String, TripInformation>> tripInfoAll = this.getTripInfo();
        Map<String, TripInformation> map = tripInfoAll.get(origin);
        TripInformation info = map.get(destination);
        return info;
    }

    /**
     * @param origin 
     * @param destination 
     * @param tripInfo 
     * @param tripInfoAll 
     * @return  
     */
    public final Map<String, Map<String, TripInformation>> setTripDemandOriginToDestination(final String origin, final String destination,
            TripInformation tripInfo, Map<String, Map<String, TripInformation>> tripInfoAll)
    {
        Map<String, TripInformation> map = tripInfoAll.get(origin);
        map.put(destination, tripInfo);
        return tripInfoAll;
    }

}
