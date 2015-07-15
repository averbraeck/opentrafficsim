package org.opentrafficsim.demo.ntm.trafficdemand;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version22 Sep 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class TripInfoTimeDynamic extends TripInfo
{

    /** relative amount of trips within a period over the separate segments. */
    private DepartureTimeProfile departureTimeProfile;

    /**
     * @param numberOfTrips amount of....
     * @param departureTimeProfile provides the division of trips by time segments
     */
    public TripInfoTimeDynamic(final double numberOfTrips, final DepartureTimeProfile departureTimeProfile)
    {
        super(numberOfTrips);
        this.departureTimeProfile = departureTimeProfile;
    }

    /**
     * @return departureTimeProfile.
     */
    public final DepartureTimeProfile getDepartureTimeProfile()
    {
        return this.departureTimeProfile;
    }

    /**
     * @param departureTimeProfile set departureTimeProfile.
     */
    public final void setDepartureTimeProfile(final DepartureTimeProfile departureTimeProfile)
    {
        this.departureTimeProfile = departureTimeProfile;
    }

}
