package org.opentrafficsim.demo.ntm.trafficdemand;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.demo.ntm.NTMNode;

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
 * @param <TripInformation>
 */
public class TripDemand<TripInformation>
{

    /** Information on trips: number, shortest path etc.. */
    private Map<String, Map<String, TripInformation>> tripInfo;

    /** Starting time. */
    private Time startTime;

    /** Time period covered by this demand. */
    private Duration timeSpan;

    /**
     */
    public TripDemand()
    {
        super();
    }

    /**
     * @param tripInfo Map&lt;String,Map&lt;String,TripInformation&gt;&gt;; information for all non-empty OD-pairs
     */
    public TripDemand(final Map<String, Map<String, TripInformation>> tripInfo)
    {
        super();
        this.tripInfo = tripInfo;
    }

    /**
     * Compresses the trip demand from detailed areas to larger areas
     * @param tripDemand TripDemand&lt;TripInfoTimeDynamic&gt;; comprising the original demand
     * @param centroids Map&lt;String,NTMNode&gt;; the detailed areas
     * @param mapSmallAreaToBigArea LinkedHashMap&lt;NTMNode,NTMNode&gt;; provides the key from small to big areas (type Node!!)
     * @return
     */
    public static TripDemand<TripInfoTimeDynamic> compressTripDemand(TripDemand<TripInfoTimeDynamic> tripDemand,
            Map<String, NTMNode> centroids, LinkedHashMap<NTMNode, NTMNode> mapSmallAreaToBigArea)
    {
        TripDemand<TripInfoTimeDynamic> compressedTripDemand = new TripDemand<TripInfoTimeDynamic>();
        compressedTripDemand.tripInfo = new LinkedHashMap<String, Map<String, TripInfoTimeDynamic>>();
        compressedTripDemand.startTime = tripDemand.getStartTime();
        compressedTripDemand.timeSpan = tripDemand.getTimeSpan();
        int notFound = 0;
        // loop through all detailed nodes/areas
        for (NTMNode node : centroids.values())
        {
            if (mapSmallAreaToBigArea.get(node) != null)
            {
                Map<String, TripInfoTimeDynamic> tripDemandRow;
                Map<String, TripInfoTimeDynamic> bigTripDemandRow = null;
                // create or retrieve the (partly filled) compressed data
                if (mapSmallAreaToBigArea.get(node).getId() != null)
                {
                    bigTripDemandRow =
                            compressedTripDemand.getTripDemandOriginToAllDestinations(mapSmallAreaToBigArea.get(node).getId());
                }
                else
                // if not found we keep the old centroid
                {
                    bigTripDemandRow = compressedTripDemand.getTripDemandOriginToAllDestinations(node.getId());
                }
                if (bigTripDemandRow == null)
                {
                    bigTripDemandRow = new LinkedHashMap<String, TripInfoTimeDynamic>();
                }
                // retrieve the detailled trips
                tripDemandRow = tripDemand.getTripDemandOriginToAllDestinations(node.getId());
                // get all destinations
                if (tripDemandRow != null)
                {
                    for (Map.Entry<String, TripInfoTimeDynamic> entry : tripDemandRow.entrySet())
                    {
                        String idSmall = entry.getKey();
                        // mapSmallAreaToBigArea.get(node);
                        NTMNode destination = centroids.get(idSmall);
                        TripInfoTimeDynamic tripInfo = tripDemandRow.get(idSmall);
                        if (destination != null)
                        {

                            if (mapSmallAreaToBigArea.get(destination) == null)
                            {
                                System.out.println("null mapping");
                            }

                            else if (mapSmallAreaToBigArea.get(destination).getId() != null)
                            {
                                // System.out.println("node " + destination.getId() + "bigNode ID "
                                // + mapSmallAreaToBigArea.get(destination).getId());
                                TripInfoTimeDynamic bigTripInfo =
                                        bigTripDemandRow.get(mapSmallAreaToBigArea.get(destination).getId());
                                if (bigTripInfo == null)
                                {
                                    bigTripInfo = new TripInfoTimeDynamic(0, null);
                                }

                                bigTripInfo.addNumberOfTrips(tripInfo.getNumberOfTrips());
                                bigTripInfo.setDepartureTimeProfile(tripInfo.getDepartureTimeProfile());
                                bigTripDemandRow.put(mapSmallAreaToBigArea.get(destination).getId(), bigTripInfo);
                            }
                            else
                            {
                                System.out.println("node bigNode not found?");

                            }
                        }
                        else
                        {
                            notFound++;
                            System.out.println("destination not found? " + notFound);
                        }

                    }
                }
                if (mapSmallAreaToBigArea.get(node).getId() != null)
                {
                    compressedTripDemand.tripInfo.put(mapSmallAreaToBigArea.get(node).getId(), bigTripDemandRow);
                }
                else
                // if not found we keep the old centroid
                {
                    compressedTripDemand.tripInfo.put(node.getId(), bigTripDemandRow);
                }
            }
        }
        // then compress the rows
        return compressedTripDemand;
    }

    /**
     * @param origin String;
     * @param destination
     * @return mapDestinations a hashmap with destination as key and tripInfo as values
     */
    public final Map<String, TripInformation> getTripDemandOriginToAllDestinations(final String origin)
    {
        Map<String, Map<String, TripInformation>> demand = this.getTripInfo();
        Map<String, TripInformation> mapDestinations = demand.get(origin);
        return mapDestinations;
    }

    /**
     * @param thisDemand TripDemand&lt;TripInfoTimeDynamic&gt;;
     * @param currentTime Time;
     * @param timeStepDurationNTM Duration;
     * @param origin
     * @param destination
     * @return mapDestinations a hashmap with destination as key and tripInfo as values
     */
    public static final double getTotalNumberOfTripsFromOrigin(TripDemand<TripInfoTimeDynamic> thisDemand, String originID,
            Time currentTime, final Duration timeStepDurationNTM)
    {
        Map<String, Map<String, TripInfoTimeDynamic>> demand = thisDemand.getTripInfo();
        Map<String, TripInfoTimeDynamic> mapDestinations = demand.get(originID);
        double rowTotal = 0.0;
        if (mapDestinations != null)
        {
            for (Entry<String, TripInfoTimeDynamic> tripInfo : mapDestinations.entrySet())
            {
                double trips = getTotalNumberOfTripsFromOriginToDestinationByTimeStep(thisDemand, originID, tripInfo.getKey(),
                        currentTime, timeStepDurationNTM);
                rowTotal += trips;
            }
        }
        return rowTotal;
    }

    public static final double getTotalNumberOfTripsFromOriginToDestinationByTimeStep(
            TripDemand<TripInfoTimeDynamic> thisDemand, String originID, String destination, Time currentTime,
            final Duration timeStepDurationNTM)
    {
        Map<String, Map<String, TripInfoTimeDynamic>> demand = thisDemand.getTripInfo();
        Map<String, TripInfoTimeDynamic> mapDestinations = demand.get(originID);
        double cellTotal = 0.0;
        TripInfoTimeDynamic tripInfo = mapDestinations.get(destination);
        NavigableMap<Time, FractionOfTripDemandByTimeSegment> curve =
                tripInfo.getDepartureTimeProfile().getDepartureTimeCurve();
        Object ceilingKey = curve.floorKey(currentTime);
        if (ceilingKey == null)
        {
            System.out.println("TripDemand 186: Strange not within TimeSpan " + ceilingKey);
        }
        FractionOfTripDemandByTimeSegment segment = curve.get(ceilingKey);
        double share = 0;
        if (segment.getDuration().getSI() > 0)
        {
            share = segment.getShareOfDemand() * timeStepDurationNTM.getSI() / segment.getDuration().getSI();
        }
        else
        {
            System.out.println("segment should not be zero");
        }
        cellTotal = tripInfo.getNumberOfTrips() * share;
        return cellTotal;
    }

    /**
     * @param origin String;
     * @param destination String;
     * @return tripInfo by OD pair
     */
    public final TripInformation getTripDemandOriginToDestination(final String origin, final String destination)
    {
        TripInformation info = null;
        Map<String, Map<String, TripInformation>> tripInfoAll = this.getTripInfo();
        Map<String, TripInformation> map = tripInfoAll.get(origin);
        if (map != null)
        {
            if (destination != null)
            {
                if (map.get(destination) == null)
                {
                    System.out.println("null!!!");
                }
                else
                {
                    info = map.get(destination);
                }
            }
        }
        return info;
    }

    /**
     * @param origin String;
     * @param destination String;
     * @param tripInfo TripInformation;
     * @param tripInfoAll Map&lt;String,Map&lt;String,TripInformation&gt;&gt;;
     * @return
     */
    public final Map<String, Map<String, TripInformation>> setTripDemandOriginToDestination(final String origin,
            final String destination, TripInformation tripInfo, Map<String, Map<String, TripInformation>> tripInfoAll)
    {
        Map<String, TripInformation> map = tripInfoAll.get(origin);
        if (map == null)
        {
            map = new LinkedHashMap<String, TripInformation>();
        }
        map.put(destination, tripInfo);
        return tripInfoAll;
    }

    /**
     * @return startTime.
     */
    public final Time getStartTime()
    {
        return this.startTime;
    }

    /**
     * @param startTime Time; set startTime.
     */
    public final void setStartTime(final Time startTime)
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
    public final Duration getTimeSpan()
    {
        return this.timeSpan;
    }

    /**
     * @param timeSpan Duration; set timeSpan.
     */
    public final void setTimeSpan(final Duration timeSpan)
    {
        this.timeSpan = timeSpan;
    }

    /**
     * @param tripInfo Map&lt;String,Map&lt;String,TripInformation&gt;&gt;; sets tripInfo
     */
    public final void setTripInfo(final Map<String, Map<String, TripInformation>> tripInfo)
    {
        this.tripInfo = tripInfo;
    }

}
