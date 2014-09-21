package org.opentrafficsim.demo.ntm.trafficdemand;

import java.util.ArrayList;

import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar;

/**
 * Describes the division the departure of trips within a period over the separate segments
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 10 Sep 2014 <br>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class DepartureTimeCurve
{
    /** */
    ArrayList<ShareTripDemandByTimeSegmentOfDay> departureTimeCurve;

    /**
     * @param durationOfSimulation
     * @param segmentsIn
     * @param startSimulationTimeSinceMidnight
     */
    public DepartureTimeCurve(DoubleScalar.Rel<TimeUnit> durationOfSimulation,
            ArrayList<ShareTripDemandByTimeSegmentOfDay> segmentsIn,
            DoubleScalar.Abs<TimeUnit> startSimulationTimeSinceMidnight)
    {
        this.departureTimeCurve =
                checkAndNormalizeCurve(durationOfSimulation, segmentsIn, startSimulationTimeSinceMidnight);
    }

    /**
     * Check if this curve is consistent
     * @param durationOfSimulation
     * @param segmentsIn
     * @param startSimulationTimeSinceMidnight
     * @return List of new segment
     */
    public ArrayList<ShareTripDemandByTimeSegmentOfDay> checkAndNormalizeCurve(
            DoubleScalar.Rel<TimeUnit> durationOfSimulation, ArrayList<ShareTripDemandByTimeSegmentOfDay> segmentsIn,
            DoubleScalar.Abs<TimeUnit> startSimulationTimeSinceMidnight)
    {
        double totalShare = 0;
        ShareTripDemandByTimeSegmentOfDay prevSegment = null;
        boolean firstTry = true;
        ArrayList<ShareTripDemandByTimeSegmentOfDay> segmentsOut = new ArrayList<ShareTripDemandByTimeSegmentOfDay>();
        for (ShareTripDemandByTimeSegmentOfDay segmentIn : segmentsIn)
        {
            if (segmentIn.timeSinceMidnight.getValueInUnit() < startSimulationTimeSinceMidnight.getValueInUnit())
            {
                prevSegment = segmentIn;
                continue;
            }
            else if (segmentIn.timeSinceMidnight.getValueInUnit() >= startSimulationTimeSinceMidnight.getValueInUnit()
                    + durationOfSimulation.getValueInUnit())
            {
                // @SuppressWarnings("static-access")
                // DoubleScalar.Rel<TimeUnit> timeToEnd =
                // MutableDoubleScalar.Rel.minus(segmentIn.timeSinceMidnight,durationOfSimulation);
                // double share = timeToEnd.getValueSI() / prevSegment.getDuration().getValueSI();
                // double newShare = share * prevSegment.getShareOfDemand();
                // ShareTripDemandByTimeSegmentOfDay newSegment = new
                // ShareTripDemandByTimeSegmentOfDay(startSimulationTimeSinceMidnight, timeToEnd, newShare);
                // segmentsOut.add(newSegment);
                // totalShare += newSegment.shareOfDemand;
                break;
            }
            else
            {
                if (segmentIn.timeSinceMidnight.getValueInUnit() >= startSimulationTimeSinceMidnight.getValueInUnit()
                        && firstTry)
                {
                    // firstTry = false;
                    // DoubleScalar.Rel<TimeUnit> timeSinceStart =
                    // MutableDoubleScalar.minus(segmentIn.timeSinceMidnight, startSimulationTimeSinceMidnight);
                    // double share = timeSinceStart.getValueSI() / prevSegment.getDuration().getValueSI();
                    // double newShare = share * prevSegment.getShareOfDemand();
                    // ShareTripDemandByTimeSegmentOfDay newSegment = new
                    // ShareTripDemandByTimeSegmentOfDay(startSimulationTimeSinceMidnight, timeSinceStart, newShare);
                    // segmentsOut.add(newSegment);
                    // totalShare += newSegment.shareOfDemand;
                }
                else
                {
                    ShareTripDemandByTimeSegmentOfDay newSegment =
                            new ShareTripDemandByTimeSegmentOfDay(segmentIn.timeSinceMidnight, segmentIn.duration,
                                    segmentIn.shareOfDemand);
                    segmentsOut.add(newSegment);
                    totalShare += newSegment.shareOfDemand;
                }

            }
        }
        for (ShareTripDemandByTimeSegmentOfDay segment : segmentsOut)
        {
            segment.shareOfDemand = segment.shareOfDemand / totalShare;
        }
        return segmentsOut;
    }

}
