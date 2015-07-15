package org.opentrafficsim.demo.ntm.trafficdemand;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;

import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.demo.ntm.Area;
import org.opentrafficsim.demo.ntm.BoundedNode;
import org.opentrafficsim.demo.ntm.Node;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version12 Sep 2014 <br>
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
     * Compresses the trip demand from detailed areas to larger areas
     * @param tripDemand comprising the original demand
     * @param centroids the detailed areas
     * @param mapSmallAreaToBigArea provides the key from small to big areas (type Node!!)
     * @return
     */
    public static TripDemand<TripInfoTimeDynamic> compressTripDemand(TripDemand<TripInfoTimeDynamic> tripDemand,
            Map<String, Node> centroids, HashMap<Node, Node> mapSmallAreaToBigArea)
    {
        TripDemand<TripInfoTimeDynamic> compressedTripDemand = new TripDemand<TripInfoTimeDynamic>();
        compressedTripDemand.tripInfo = new HashMap<String, Map<String, TripInfoTimeDynamic>>();
        compressedTripDemand.startTime = tripDemand.getStartTime();
        compressedTripDemand.timeSpan = tripDemand.getTimeSpan();
        int notFound = 0;
        // loop through all detailed nodes/areas
        for (Node node : centroids.values())
        {
            if (mapSmallAreaToBigArea.get(node) != null)
            {
                Map<String, TripInfoTimeDynamic> tripDemandRow;
                Map<String, TripInfoTimeDynamic> bigTripDemandRow = null;
                // create or retrieve the (partly filled) compressed data
                if (mapSmallAreaToBigArea.get(node).getId() != null)
                {
                    bigTripDemandRow =
                            compressedTripDemand.getTripDemandOriginToAllDestinations(mapSmallAreaToBigArea.get(node)
                                    .getId());
                }
                else
                // if not found we keep the old centroid
                {
                    bigTripDemandRow = compressedTripDemand.getTripDemandOriginToAllDestinations(node.getId());
                }
                if (bigTripDemandRow == null)
                {
                    bigTripDemandRow = new HashMap<String, TripInfoTimeDynamic>();
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
                        Node destination = centroids.get(idSmall);
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
     * @param origin
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
     * @param thisDemand
     * @param currentTime
     * @param timeStepDurationNTM
     * @param origin
     * @param destination
     * @return mapDestinations a hashmap with destination as key and tripInfo as values
     */
    public static final double getTotalNumberOfTripsFromOrigin(TripDemand<TripInfoTimeDynamic> thisDemand,
            String originID, DoubleScalar.Abs<TimeUnit> currentTime,
            final DoubleScalar.Rel<TimeUnit> timeStepDurationNTM)
    {
        Map<String, Map<String, TripInfoTimeDynamic>> demand = thisDemand.getTripInfo();
        Map<String, TripInfoTimeDynamic> mapDestinations = demand.get(originID);
        double rowTotal = 0.0;
        if (mapDestinations != null)
        {
            for (Entry<String, TripInfoTimeDynamic> tripInfo : mapDestinations.entrySet())
            {
                double trips =
                        getTotalNumberOfTripsFromOriginToDestinationByTimeStep(thisDemand, originID, tripInfo.getKey(),
                                currentTime, timeStepDurationNTM);
                rowTotal += trips;
            }
        }
        return rowTotal;
    }

    public static final double getTotalNumberOfTripsFromOriginToDestinationByTimeStep(
            TripDemand<TripInfoTimeDynamic> thisDemand, String originID, String destination,
            DoubleScalar.Abs<TimeUnit> currentTime, final DoubleScalar.Rel<TimeUnit> timeStepDurationNTM)
    {
        Map<String, Map<String, TripInfoTimeDynamic>> demand = thisDemand.getTripInfo();
        Map<String, TripInfoTimeDynamic> mapDestinations = demand.get(originID);
        double cellTotal = 0.0;
        TripInfoTimeDynamic tripInfo = mapDestinations.get(destination);
        NavigableMap<Abs<TimeUnit>, FractionOfTripDemandByTimeSegment> curve =
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
     * @param origin
     * @param destination
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
     * @param origin
     * @param destination
     * @param tripInfo
     * @param tripInfoAll
     * @return
     */
    public final Map<String, Map<String, TripInformation>> setTripDemandOriginToDestination(final String origin,
            final String destination, TripInformation tripInfo, Map<String, Map<String, TripInformation>> tripInfoAll)
    {
        Map<String, TripInformation> map = tripInfoAll.get(origin);
        if (map == null)
        {
            map = new HashMap<String, TripInformation>();
        }
        map.put(destination, tripInfo);
        return tripInfoAll;
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

}
