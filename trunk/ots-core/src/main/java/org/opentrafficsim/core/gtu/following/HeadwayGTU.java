package org.opentrafficsim.core.gtu.following;

import org.djunits.unit.LengthUnit;
import org.opentrafficsim.core.OTS_SCALAR;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;

/**
 * Container for a reference to a LaneBasedGTU and a headway.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version 11 feb. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class HeadwayGTU implements OTS_SCALAR
{
    /** the other GTU. */
    private final LaneBasedGTU otherGTU;

    /** the distance to the GTU in meters. */
    private final double distanceSI;

    /**
     * Construct a new HeadwayGTU.
     * @param otherGTU the other GTU
     * @param distanceSI the distance to the other GTU in meters; if the other GTU is parallel, use distance Double.NaN
     */
    public HeadwayGTU(final LaneBasedGTU otherGTU, final double distanceSI)
    {
        this.otherGTU = otherGTU;
        this.distanceSI = distanceSI;
    }

    /**
     * @return the other GTU.
     */
    public final LaneBasedGTU getOtherGTU()
    {
        return this.otherGTU;
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
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; the distance to the GTU, return value null indicates that the other GTU is
     *         parallel to the reference GTU
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
        return String.format("Headway %s to %s", getDistance(), this.otherGTU);
    }

}
