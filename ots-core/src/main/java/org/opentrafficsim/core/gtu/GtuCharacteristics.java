package org.opentrafficsim.core.gtu;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;

/**
 * Characteristics of a GTU. This class is used to store all characteristics of a (not-yet constructed) GTU.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class GtuCharacteristics implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160000L;

    /** The type of the GTU. */
    private final GtuType gtuType;

    /** Length of the GTU. */
    private final Length length;

    /** Width of the GTU. */
    private final Length width;

    /** Maximum speed of the GTU. */
    private final Speed maximumSpeed;

    /** Maximum acceleration. */
    private final Acceleration maximumAcceleration;

    /** Maximum deceleration. */
    private final Acceleration maximumDeceleration;

    /** Front position relative to the reference position. */
    private final Length front;

    /**
     * Construct a new set of GTUCharacteristics.
     * @param gtuType GtuType; type of the (not yet constructed) GTU
     * @param length Length; the length of the (non yet constructed) GTU
     * @param width Length; the width of the (non yet constructed) GTU
     * @param maximumSpeed Speed; the maximum speed of the (non yet constructed) GTU
     * @param maximumAcceleration Acceleration; maximum acceleration
     * @param maximumDeceleration Acceleration; maximum deceleration
     * @param front Length; front position relative to the reference position
     */
    public GtuCharacteristics(final GtuType gtuType, final Length length, final Length width, final Speed maximumSpeed,
            final Acceleration maximumAcceleration, final Acceleration maximumDeceleration, final Length front)
    {
        this.gtuType = gtuType;
        this.length = length;
        this.width = width;
        this.maximumSpeed = maximumSpeed;
        this.maximumAcceleration = maximumAcceleration;
        this.maximumDeceleration = maximumDeceleration;
        this.front = front;
    }

    /**
     * Retrieve the GTU type.
     * @return GtuType
     */
    public final GtuType getGtuType()
    {
        return this.gtuType;
    }

    /**
     * Retrieve the length.
     * @return Length
     */
    public final Length getLength()
    {
        return this.length;
    }

    /**
     * Retrieve the width.
     * @return Length
     */
    public final Length getWidth()
    {
        return this.width;
    }

    /**
     * Retrieve the maximum speed.
     * @return Speed
     */
    public final Speed getMaximumSpeed()
    {
        return this.maximumSpeed;
    }

    /**
     * Retrieve the maximum acceleration.
     * @return Acceleration
     */
    public final Acceleration getMaximumAcceleration()
    {
        return this.maximumAcceleration;
    }

    /**
     * Retrieve the maximum deceleration.
     * @return Acceleration
     */
    public final Acceleration getMaximumDeceleration()
    {
        return this.maximumDeceleration;
    }

    /**
     * Retrieve the front position relative to the reference position.
     * @return Length
     */
    public final Length getFront()
    {
        return this.front;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "GTUCharacteristics [gtuType=" + this.gtuType + ", length=" + this.length + ", width=" + this.width
                + ", maximumSpeed=" + this.maximumSpeed + ", maximumAcceleration=" + this.maximumAcceleration
                + ", maximumDeceleration=" + this.maximumDeceleration + ", front=" + this.front + "]";
    }

}
