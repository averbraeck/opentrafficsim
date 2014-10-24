package org.opentrafficsim.core.network;

/**
 * "1D" implementation.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Oct 22, 2014 <br>
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

}
