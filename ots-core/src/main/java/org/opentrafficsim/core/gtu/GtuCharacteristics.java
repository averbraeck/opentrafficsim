package org.opentrafficsim.core.gtu;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;

/**
 * Characteristics of a GTU. This class is used to store all characteristics of a (not-yet constructed) GTU.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class GtuCharacteristics
{

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
     * Construct a new set of GtuCharacteristics.
     * @param gtuType type of the (not yet constructed) GTU
     * @param length the length of the (non yet constructed) GTU
     * @param width the width of the (non yet constructed) GTU
     * @param maximumSpeed the maximum speed of the (non yet constructed) GTU
     * @param maximumAcceleration maximum acceleration
     * @param maximumDeceleration maximum deceleration
     * @param front front position relative to the reference position
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

    @Override
    public String toString()
    {
        return "GtuCharacteristics [gtuType=" + this.gtuType + ", length=" + this.length + ", width=" + this.width
                + ", maximumSpeed=" + this.maximumSpeed + ", maximumAcceleration=" + this.maximumAcceleration
                + ", maximumDeceleration=" + this.maximumDeceleration + ", front=" + this.front + "]";
    }

}
