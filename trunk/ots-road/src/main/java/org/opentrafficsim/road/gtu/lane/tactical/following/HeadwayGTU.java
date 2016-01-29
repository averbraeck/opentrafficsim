package org.opentrafficsim.road.gtu.lane.tactical.following;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * Container for a reference to a LaneBasedGTU and a headway.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1368 $, $LastChangedDate: 2015-09-02 00:20:20 +0200 (Wed, 02 Sep 2015) $, by $Author: averbraeck $,
 *          initial version 11 feb. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class HeadwayGTU
{
    /** the other GTU. */
    private final LaneBasedGTU gtu;

    /** the distance to the GTU in meters. */
    private final double distanceSI;

    /**
     * Construct a new HeadwayGTU.
     * @param gtu the GTU in front of us
     * @param distanceSI the distance to the other GTU in meters; if the other GTU is parallel, use distance Double.NaN
     */
    public HeadwayGTU(final LaneBasedGTU gtu, final double distanceSI)
    {
        this.gtu = gtu;
        this.distanceSI = distanceSI;
    }

    /**
     * @return the GTU in front of us.
     */
    public final LaneBasedGTU getGTU()
    {
        return this.gtu;
    }

    /**
     * Retrieve the distance to the other GTU in meters.
     * @return the distance to the other GTU in SI unit for length (meter), the value Double.NaN is used to indicate that the
     *         other GTU is parallel with the reference GTU
     */
    public final double getDistanceSI()
    {
        return this.distanceSI;
    }

    /**
     * Retrieve the strongly typed distance to the other GTU.
     * @return Length.Rel; the distance to the GTU, return value null indicates that the other GTU is parallel to the reference
     *         GTU
     */
    public final Length.Rel getDistance()
    {
        if (Double.isNaN(this.distanceSI))
        {
            return null;
        }
        return new Length.Rel(this.distanceSI, LengthUnit.SI);
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        return String.format("Headway %s to %s", getDistance(), this.gtu);
    }

}
