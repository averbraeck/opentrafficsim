package org.opentrafficsim.road.gtu.lane.tactical.following;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;

/**
 * Container for a reference to information about a LaneBasedGTU and a headway.
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
    /** the id of the GTU for comparison purposes. */
    private final String gtuId;
    
    /** the (perceived) speed of the other GTU. Can be null if unknown. */
    private final Speed gtuSpeed;

    /** the distance to the GTU in meters. */
    private final double distanceSI;

    /**
     * Construct a new HeadwayGTU information object.
     * @param gtuId the id of the GTU for comparison purposes
     * @param gtuSpeed the (perceived) speed of the GTU in front of us; can be null if unknown
     * @param distanceSI the distance to the other GTU in meters; if the other GTU is parallel, use distance Double.NaN
     */
    public HeadwayGTU(final String gtuId, final Speed gtuSpeed, final double distanceSI)
    {
        this.gtuId = gtuId;
        this.gtuSpeed = gtuSpeed;
        this.distanceSI = distanceSI;
    }

    /**
     * @return the id of the GTU for comparison purposes
     */
    public final String getGtuId()
    {
        return this.gtuId;
    }

    /**
     * @return the (perceived) speed of the GTU in front of us; can be null if unknown.
     */
    public final Speed getGtuSpeed()
    {
        return this.gtuSpeed;
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
        return String.format("Headway %s to GTU %s with speed %s", getDistance(), getGtuId(), getGtuSpeed());
    }

}
