package org.opentrafficsim.demo.ntm.trafficdemand;

import java.util.NavigableMap;
import java.util.TreeMap;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.DoubleScalar.Abs;

/**
 * Describes the division the departure of trips within a period over the separate segments
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 10 Sep 2014 <br>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class DepartureTimeProfile
{
    /** */
    private NavigableMap<Abs<TimeUnit>, FractionOfTripDemandByTimeSegment> departureTimeCurve;

    /** descriptive name of the profile. */
    private String name;

    /**
     * Create a new profile.
     */
    public DepartureTimeProfile()
    {
    }

    /**
     * @param departureTimeProfile
     * @param name Name of the profile
     */
    public DepartureTimeProfile(final NavigableMap<Abs<TimeUnit>, FractionOfTripDemandByTimeSegment> departureTimeProfile,
        final String name)
    {
        this.departureTimeCurve = departureTimeProfile;
        this.name = name;
    }

    /**
     * Generates a time profile curve of the trips that are released in the time segments of this simulation.
     * @param durationOfSimulation length in TimeUnits
     * @param navigableMap list with information on the number of Trips to be released in a certain time segment
     * @param startSimulationTimeSinceMidnight Start of the simulation by time of (a) day
     * @return List of new segment
     */
    public final NavigableMap<DoubleScalar.Abs<TimeUnit>, FractionOfTripDemandByTimeSegment> checkAndNormalizeCurve(
        final DoubleScalar.Abs<TimeUnit> startSimulationTimeSinceMidnight,
        final DoubleScalar.Rel<TimeUnit> durationOfSimulation,
        final NavigableMap<Abs<TimeUnit>, FractionOfTripDemandByTimeSegment> navigableMap)
    {
        double totalShare = 0;
        // ShareOfTripDemandByTimeWindow prevSegment = null;
        NavigableMap<DoubleScalar.Abs<TimeUnit>, FractionOfTripDemandByTimeSegment> segmentsOut =
            new TreeMap<DoubleScalar.Abs<TimeUnit>, FractionOfTripDemandByTimeSegment>();
        DoubleScalar.Abs<TimeUnit> endTimeOfSimulation =
            DoubleScalar.plus(startSimulationTimeSinceMidnight, durationOfSimulation);
        // only select the segments of the DepartureTimeProfile that are within this simulation period
        for (FractionOfTripDemandByTimeSegment segment : navigableMap.values())
        {
            @SuppressWarnings("static-access")
            DoubleScalar.Abs<TimeUnit> endTimeOfSegment =
                DoubleScalar.Rel.plus(segment.getTimeSinceMidnight(), segment.getDuration());
            if (segment.getTimeSinceMidnight().getInUnit() < startSimulationTimeSinceMidnight.getInUnit()
                && endTimeOfSegment.getInUnit() > startSimulationTimeSinceMidnight.getInUnit())
            // first segment from the departureTimeProfile that starts before the start of the simulation, but ends
            // within this simulation
            {
                DoubleScalar.Rel<TimeUnit> durationWithinSimulation =
                    DoubleScalar.minus(segment.getTimeSinceMidnight(), startSimulationTimeSinceMidnight);
                double shareFirstSegment =
                    durationWithinSimulation.getSI() / segment.getDuration().getSI() * segment.getShareOfDemand();
                FractionOfTripDemandByTimeSegment newSegment =
                    new FractionOfTripDemandByTimeSegment(startSimulationTimeSinceMidnight, durationWithinSimulation,
                        shareFirstSegment);
                segmentsOut.put(startSimulationTimeSinceMidnight, newSegment);
                totalShare += newSegment.getShareOfDemand();
            }
            // detects that this segment segment from the departureTimeProfile starts beyond the simulation period
            // this is the last segment to be inspected!
            else if (segment.getTimeSinceMidnight().getInUnit() < endTimeOfSimulation.getInUnit()
                && endTimeOfSegment.getInUnit() >= endTimeOfSimulation.getInUnit())
            {
                @SuppressWarnings("static-access")
                DoubleScalar.Rel<TimeUnit> durationWithinSimulation =
                    DoubleScalar.Rel.minus(endTimeOfSimulation, segment.getTimeSinceMidnight());
                double share = durationWithinSimulation.getSI() / segment.getDuration().getSI();
                double newShare = share * segment.getShareOfDemand();
                FractionOfTripDemandByTimeSegment newSegment =
                    new FractionOfTripDemandByTimeSegment(segment.getTimeSinceMidnight(), durationWithinSimulation, newShare);
                segmentsOut.put(segment.getTimeSinceMidnight(), newSegment);
                totalShare += newSegment.getShareOfDemand();
                // now leave this loop: we have passed all segments of this simulation period
                break;
            }
            else if (segment.getTimeSinceMidnight().getInUnit() >= startSimulationTimeSinceMidnight.getInUnit()
                && endTimeOfSegment.getInUnit() <= endTimeOfSimulation.getInUnit())
            // all segments from the departureTimeProfile that are within the simulation period
            {
                FractionOfTripDemandByTimeSegment newSegment =
                    new FractionOfTripDemandByTimeSegment(segment.getTimeSinceMidnight(), segment.getDuration(), segment
                        .getShareOfDemand());
                segmentsOut.put(segment.getTimeSinceMidnight(), newSegment);
                totalShare += newSegment.getShareOfDemand();
            }

        }
        /*
         * for (FractionOfTripDemandByTimeSegment curve : segmentsOut.values()) { System.out.println("Curve " +
         * curve.getShareOfDemand() + "tijd " + curve.getTimeSinceMidnight() ); } System.out.println("Curve end");
         */

        for (FractionOfTripDemandByTimeSegment segment : segmentsOut.values())
        {
            /*
             * for (FractionOfTripDemandByTimeSegment curve : segmentsOut.values()) { System.out.println("Curve " +
             * curve.getShareOfDemand() + "tijd " + curve.getTimeSinceMidnight() ); } System.out.println("Curve end");
             */

            // Normalise the share as a fraction of the sum of all shares (a value between 0.0 and 1.0)
            segment.setShareOfDemand(segment.getShareOfDemand() / totalShare);
        }

        return segmentsOut;
    }

    /**
     * @return departureTimeCurve.
     */
    public final NavigableMap<Abs<TimeUnit>, FractionOfTripDemandByTimeSegment> getDepartureTimeCurve()
    {
        return this.departureTimeCurve;
    }

    /**
     * @param profileList set departureTimeCurve.
     */
    public final void
        setDepartureTimeCurve(final NavigableMap<Abs<TimeUnit>, FractionOfTripDemandByTimeSegment> profileList)
    {
        this.departureTimeCurve = profileList;
    }

    /**
     * @return name.
     */
    public final String getName()
    {
        return this.name;
    }

    /**
     * @param name set name.
     */
    public final void setName(final String name)
    {
        this.name = name;
    }

}
