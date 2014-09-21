package org.opentrafficsim.demo.ntm.trafficdemand;

import java.util.Map;

import org.opentrafficsim.demo.ntm.TripInfo;

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

    /** */
    Map<Long, Map<Long, TripInfo>> tripDemand;

    /**
     * @param tripDemand
     */
    public TripDemand(Map<Long, Map<Long, TripInfo>> tripDemand)
    {
        super();
        this.tripDemand = tripDemand;
    }

    /**
     * @return tripDemand
     */
    public Map<Long, Map<Long, TripInfo>> getTripDemand()
    {
        return this.tripDemand;
    }

    /**
     * @param tripDemand set tripDemand
     */
    public void setTripDemand(Map<Long, Map<Long, TripInfo>> tripDemand)
    {
        this.tripDemand = tripDemand;
    }

    /**
     * @param origin
     * @param destination
     * @return mapDestinations a hashmap with destination as key and tripInfo as values
     */
    public Map<Long, TripInfo> getTripDemand_Origin_AllDestinations(Long origin, Long destination)
    {
        Map<Long, Map<Long, TripInfo>> demand = this.getTripDemand();
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
        Map<Long, Map<Long, TripInfo>> demand = this.getTripDemand();
        Map<Long, TripInfo> map = demand.get(origin);
        TripInfo tripInfo = map.get(destination);
        return tripInfo;
    }

    /*    *//**
     * @param originSize
     * @param destinationSize
     * @param trips
     * @return a hashmap with Trips info by origin and destination
     */
    /*
     * public Map<Long, Map<Long, TripInfo>> createTripDemand (long originSize, long destinationSize, double[][] trips)
     * { Map<Integer, Map<Integer, TripInfo>> originMap = new HashMap<Integer, Map<Integer, TripInfo>>(); for (long i =
     * 0; i < originSize; i++) { Map<Long, TripInfo> destinationMap = new HashMap<Long, TripInfo>(); double sumTrips =
     * 0; for (long j = 0; j < destinationSize; j++) { if (trips[i][j] > 0) { TripInfo trip = new TripInfo(trips[i][j]);
     * sumTrips += trips[i][j]; destinationMap.put(j, trip); } } if (sumTrips > 0) { originMap.put(i, destinationMap); }
     * } return originMap; }
     */

}
