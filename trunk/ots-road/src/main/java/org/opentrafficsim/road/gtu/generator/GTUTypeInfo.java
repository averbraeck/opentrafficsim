package org.opentrafficsim.road.gtu.generator;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUType;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 nov. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class GTUTypeInfo
{

    /** Length. */
    private final Length length;

    /** Width. */
    private final Length width;

    /** GTU type. */
    private final GTUType gtuType;

    /** Maximum speed. */
    private final Speed maximumSpeed;

    /**
     * @param length length of the GTU
     * @param width width of the GTU
     * @param gtuType GTU type
     * @param maximumSpeed maximum speed of the GTU
     */
    public GTUTypeInfo(final Length length, final Length width, final GTUType gtuType, final Speed maximumSpeed)
    {
        this.length = length;
        this.width = width;
        this.gtuType = gtuType;
        this.maximumSpeed = maximumSpeed;
    }

    /**
     * @return length.
     */
    public final Length getLength()
    {
        return this.length;
    }

    /**
     * @return width.
     */
    public final Length getWidth()
    {
        return this.width;
    }

    /**
     * @return gtuType.
     */
    public final GTUType getGtuType()
    {
        return this.gtuType;
    }

    /**
     * @return maximumSpeed.
     */
    public final Speed getMaximumSpeed()
    {
        return this.maximumSpeed;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "GTUTypeInfo [length=" + this.length + ", width=" + this.width + ", gtuType=" + this.gtuType
                + ", maximumSpeed=" + this.maximumSpeed + "]";
    }

}
