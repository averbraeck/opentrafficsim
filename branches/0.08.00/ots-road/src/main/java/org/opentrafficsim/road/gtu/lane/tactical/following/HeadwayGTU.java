package org.opentrafficsim.road.gtu.lane.tactical.following;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUType;

/**
 * Container for a reference to information about a (lane based) GTU and a headway.
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
    /** The id of the GTU for comparison purposes. */
    private final String gtuId;

    /** The (perceived) speed of the other GTU. Can be null if unknown. */
    private final Speed gtuSpeed;

    /** The (perceived) distance to the GTU. */
    private final Length.Rel distance;

    /** The perceived GTU type. */
    private final GTUType gtuType;

    /**
     * Construct a new HeadwayGTU information object.
     * @param gtuId the id of the GTU for comparison purposes, can be null if no GTU but just headway
     * @param gtuSpeed the (perceived) speed of the GTU in front of us; can be null if unknown
     * @param distance the distance to the other GTU; if the other GTU is parallel, use distance null
     * @param gtuType the perceived GTU type, can be null if GTU unknown
     */
    public HeadwayGTU(final String gtuId, final Speed gtuSpeed, final Length.Rel distance, final GTUType gtuType)
    {
        this.gtuId = gtuId;
        this.gtuSpeed = gtuSpeed;
        this.distance = distance;
        this.gtuType = gtuType;
    }

    /**
     * Construct a new HeadwayGTU information object.
     * @param gtuId the id of the GTU for comparison purposes, can be null if no GTU but just headway
     * @param gtuSpeed the (perceived) speed of the GTU in front of us; can be null if unknown
     * @param distanceSI the distance to the other GTU; if the other GTU is parallel, use distance null
     * @param gtuType the perceived GTU type, can be null if GTU unknown
     */
    public HeadwayGTU(final String gtuId, final Speed gtuSpeed, final double distanceSI, final GTUType gtuType)
    {
        this.gtuId = gtuId;
        this.gtuSpeed = gtuSpeed;
        if (Double.isNaN(distanceSI))
        {
            this.distance = null;
        }
        else
        {
            this.distance = new Length.Rel(distanceSI, LengthUnit.SI);
        }
        this.gtuType = gtuType;
    }

    /**
     * @return the id of the GTU for comparison purposes, can be null if no GTU but just headway
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
     * Retrieve the strongly typed distance to the other GTU.
     * @return Length.Rel; the distance to the GTU, return value null indicates that the other GTU is parallel to the reference
     *         GTU
     */
    public final Length.Rel getDistance()
    {
        return this.distance;
    }

    /**
     * @return the (perceived) gtuType, can be null if no GTU but just headway
     */
    public final GTUType getGtuType()
    {
        return this.gtuType;
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        return String.format("Headway %s to GTU %s of type %s with speed %s", getDistance(), getGtuId(), getGtuType(),
            getGtuSpeed());
    }

}
