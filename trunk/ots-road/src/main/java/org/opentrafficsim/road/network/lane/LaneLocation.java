package org.opentrafficsim.road.network.lane;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;

/**
 * "1D" implementation.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-03 13:38:01 +0200 (Thu, 03 Sep 2015) $, @version $Revision: 1378 $, by $Author: averbraeck $,
 * initial version Oct 22, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneLocation
{
    /** The lane of the location of a point relative to the GTU. */
    private final Lane lane;

    /** The fractional position (between 0.0 and 1.0) of the reference point on the lane. */
    private final double fractionalLongitudinalPosition;

    /**
     * @param lane The lane of the location of a point relative to the GTU.
     * @param fractionalLongitudinalPosition The fractional position (between 0.0 and 1.0) of the reference point on the lane.
     */
    public LaneLocation(final Lane lane, final double fractionalLongitudinalPosition)
    {
        super();
        this.lane = lane;
        this.fractionalLongitudinalPosition = fractionalLongitudinalPosition;
    }

    /**
     * @param lane The lane of the location of a point relative to the GTU.
     * @param position The position as a length of the reference point on the lane.
     */
    public LaneLocation(final Lane lane, final Length.Rel position)
    {
        super();
        this.lane = lane;
        this.fractionalLongitudinalPosition = position.divideBy(this.lane.getLength()).doubleValue();
    }

    /**
     * @return lane.
     */
    public final Lane getLane()
    {
        return this.lane;
    }

    /**
     * @return fractionalLongitudinalPosition.
     */
    public final double getFractionalLongitudinalPosition()
    {
        return this.fractionalLongitudinalPosition;
    }

    /**
     * @return position as a traveled length on this lane.
     */
    public final Length.Abs getLongitudinalPosition()
    {
        return new Length.Abs(this.lane.getLength().getSI() * this.fractionalLongitudinalPosition, LengthUnit.METER);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneLocation [lane=" + this.lane + ", fractionalLongitudinalPosition=" + this.fractionalLongitudinalPosition
                + "]";
    }

}
