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
public class DepartureTimeProfile
{
    /** */
    private ArrayList<FractionOfTripDemandByTimeSegment> departureTimeProfile;

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
    public DepartureTimeProfile(final ArrayList<FractionOfTripDemandByTimeSegment> departureTimeProfile, final String name)
    {
        this.departureTimeProfile = departureTimeProfile;
        this.name = name;
    }

    /**
     * Generates a time profile curve of the trips that are released in the time segments of this simulation.
     * @param durationOfSimulation length in TimeUnits
     * @param departureTimeProfileIn list with information on the number of Trips to be released in a certain time segment
     * @param startSimulationTimeSinceMidnight Start of the simulation by time of (a) day
     * @return List of new segment
     */
    public final ArrayList<FractionOfTripDemandByTimeSegment> checkAndNormalizeCurve(
            final DoubleScalar.Abs<TimeUnit> startSimulationTimeSinceMidnight,
            final DoubleScalar.Rel<TimeUnit> durationOfSimulation,
            final ArrayList<FractionOfTripDemandByTimeSegment> departureTimeProfileIn)
    {
        double totalShare = 0;
        // ShareOfTripDemandByTimeWindow prevSegment = null;
        ArrayList<FractionOfTripDemandByTimeSegment> segmentsOut = new ArrayList<FractionOfTripDemandByTimeSegment>();

        // only select the segments of the DepartureTimeProfile that are within this simulation period
        for (FractionOfTripDemandByTimeSegment segment : departureTimeProfileIn)
        {
            @SuppressWarnings("static-access")
            DoubleScalar.Abs<TimeUnit> endTimeOfSegment =
                    MutableDoubleScalar.Rel.plus(segment.getTimeSinceMidnight(), segment.getDuration()).immutable();
            DoubleScalar.Abs<TimeUnit> endTimeOfSimulation =
                    MutableDoubleScalar.plus(startSimulationTimeSinceMidnight, durationOfSimulation).immutable();

            if (segment.getTimeSinceMidnight().getValueInUnit() < startSimulationTimeSinceMidnight.getValueInUnit()
                    && endTimeOfSegment.getValueInUnit() > startSimulationTimeSinceMidnight.getValueInUnit())
            // first segment from the departureTimeProfile that starts before the start of the simulation, but ends
            // within this simulation
            {
                DoubleScalar.Rel<TimeUnit> durationWithinSimulation =
                        MutableDoubleScalar.minus(segment.getTimeSinceMidnight(), startSimulationTimeSinceMidnight)
                                .immutable();
                double shareFirstSegment =
                        durationWithinSimulation.getValueSI() / segment.getDuration().getValueSI()
                                * segment.getShareOfDemand();
                FractionOfTripDemandByTimeSegment newSegment =
                        new FractionOfTripDemandByTimeSegment(startSimulationTimeSinceMidnight, durationWithinSimulation,
                                shareFirstSegment);
                segmentsOut.add(newSegment);
                totalShare += newSegment.getShareOfDemand();
            }
            // detects that this segment segment from the departureTimeProfile starts beyond the simulation period
            // this is the last segment to be inspected!
            else if (segment.getTimeSinceMidnight().getValueInUnit() < endTimeOfSimulation.getValueInUnit()
                    && endTimeOfSegment.getValueInUnit() >= endTimeOfSimulation.getValueInUnit())
            {
                @SuppressWarnings("static-access")
                DoubleScalar.Rel<TimeUnit> durationWithinSimulation =
                        MutableDoubleScalar.Rel.minus(endTimeOfSimulation, segment.getTimeSinceMidnight()).immutable();
                double share = durationWithinSimulation.getValueSI() / segment.getDuration().getValueSI();
                double newShare = share * segment.getShareOfDemand();
                FractionOfTripDemandByTimeSegment newSegment =
                        new FractionOfTripDemandByTimeSegment(startSimulationTimeSinceMidnight, durationWithinSimulation,
                                newShare);
                segmentsOut.add(newSegment);
                totalShare += newSegment.getShareOfDemand();
                // now leave this loop: we have passed all segments of this simulation period
                break;
            }
            else if (segment.getTimeSinceMidnight().getValueInUnit() >= startSimulationTimeSinceMidnight
                    .getValueInUnit() && endTimeOfSegment.getValueInUnit() <= endTimeOfSimulation.getValueInUnit())
            // all segments from the departureTimeProfile that are within the simulation period
            {
                FractionOfTripDemandByTimeSegment newSegment =
                        new FractionOfTripDemandByTimeSegment(segment.getTimeSinceMidnight(), segment.getDuration(),
                                segment.getShareOfDemand());
                segmentsOut.add(newSegment);
                totalShare += newSegment.getShareOfDemand();
            }

        }
        for (FractionOfTripDemandByTimeSegment segment : segmentsOut)
        {
            // Normalise the share as a fraction of the sum of all shares (a value between 0.0 and 1.0)
            segment.setShareOfDemand(segment.getShareOfDemand() / totalShare);
        }

        return segmentsOut;
    }

    /**
     * @return departureTimeCurve.
     */
    public final ArrayList<FractionOfTripDemandByTimeSegment> getDepartureTimeProfile()
    {
        return this.departureTimeProfile;
    }

    /**
     * @param departureTimeCurve set departureTimeCurve.
     */
    public final void setDepartureTimeCurve(final ArrayList<FractionOfTripDemandByTimeSegment> departureTimeCurve)
    {
        this.departureTimeProfile = departureTimeCurve;
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
