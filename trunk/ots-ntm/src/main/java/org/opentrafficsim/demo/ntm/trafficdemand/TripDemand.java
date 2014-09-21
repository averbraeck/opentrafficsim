package org.opentrafficsim.demo.ntm.trafficdemand;

import java.util.Map;

import org.opentrafficsim.demo.ntm.TripInfo;

/**
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
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
