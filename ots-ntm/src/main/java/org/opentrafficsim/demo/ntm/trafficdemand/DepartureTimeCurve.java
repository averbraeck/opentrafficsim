package org.opentrafficsim.demo.ntm.trafficdemand;

import java.util.ArrayList;

import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar;

/**
 * Describes the division the departure of trips within a period over the separate segments
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
