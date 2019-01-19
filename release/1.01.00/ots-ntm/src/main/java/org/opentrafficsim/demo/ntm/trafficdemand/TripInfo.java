package org.opentrafficsim.demo.ntm.trafficdemand;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 15 Sep 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class TripInfo
{
    /** Total number of Trips for this simulation from the trip demand file. */
    private double numberOfTrips;

    /**
     * @param numberOfTrips total number of Trips for this simulation
     */

    public TripInfo(final double numberOfTrips)
    {
        super();
        this.numberOfTrips = numberOfTrips;
    }

    /**
     * @param add double; number of Trips to be added
     */
    public final void addNumberOfTrips(double add)
    {
        this.numberOfTrips = this.getNumberOfTrips() + add;
    }

    /**
     * @return numberOfTrips
     */
    public final double getNumberOfTrips()
    {
        return this.numberOfTrips;
    }

    /**
     * @param numberOfTrips double; set numberOfTrips
     */
    public final void setNumberOfTrips(final double numberOfTrips)
    {
        this.numberOfTrips = numberOfTrips;
    }

}
