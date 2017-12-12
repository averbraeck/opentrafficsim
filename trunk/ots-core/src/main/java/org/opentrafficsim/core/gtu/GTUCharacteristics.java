package org.opentrafficsim.core.gtu;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;

/**
 * Characteristics of a GTU. This class is used to store all characteristics of a (not-yet constructed) GTU.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 8, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class GTUCharacteristics implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160000L;

    /** The type of the GTU. */
    private final GTUType gtuType;

    /** Length of the GTU. */
    private final Length length;

    /** Width of the GTU. */
    private final Length width;

    /** Maximum speed of the GTU. */
    private final Speed maximumSpeed;

    /**
     * Construct a new set of GTUCharacteristics.
     * @param gtuType GTUType; type of the (not yet constructed) GTU
     * @param length Length; the length of the (non yet constructed) GTU
     * @param width Length; the width of the (non yet constructed) GTU
     * @param maximumSpeed Length; the maximum speed of the (non yet constructed) GTU
     */
    public GTUCharacteristics(final GTUType gtuType, final Length length, final Length width,
            final Speed maximumSpeed)
    {
        this.gtuType = gtuType;
        this.length = length;
        this.width = width;
        this.maximumSpeed = maximumSpeed;
    }

    /**
     * Retrieve the GTU type.
     * @return GTUType.
     */
    public final GTUType getGTUType()
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
     * @return Width.Rel
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

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "GTUCharacteristics [gtuType=" + this.gtuType + ", length=" + this.length + ", width=" + this.width
                + ", maximumSpeed=" + this.maximumSpeed + "]";
    }

}
