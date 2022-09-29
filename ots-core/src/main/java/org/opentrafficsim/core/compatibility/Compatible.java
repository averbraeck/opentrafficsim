package org.opentrafficsim.core.compatibility;

import org.opentrafficsim.core.gtu.GtuType;

/**
 * Interface for infrastructure types to assess traversability by GTU types.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
@Deprecated
public interface Compatible
{

    /**
     * Default {@code Compatible} that allows every GTU type in every direction.
     */
    @Deprecated
    Compatible EVERYTHING = new Compatible()
    {
        @Override
        public boolean isCompatible(final GtuType gtuType)
        {
            return true;
        }
    };

    /** Plus direction {@code Compatible} that allows all GTU types in the plus direction. */
    @Deprecated
    Compatible PLUS = new Compatible()
    {
        @Override
        public boolean isCompatible(final GtuType gtuType)
        {
            return true;
        }
    };

    /** Minus direction {@code Compatible} that allows all GTU types in the minus direction. */
    @Deprecated
    Compatible MINUS = new Compatible()
    {
        @Override
        public boolean isCompatible(final GtuType gtuType)
        {
            return false;
        }
    };

    /**
     * Test if a GtuType is handled by the infrastructure in the given direction. For Lane and Link, <cite>handled</cite> means
     * that GTUs of this type can travel over this infrastructure in the direction. For Sensors it means that the sensor will
     * detect GTUs that travel over it in the given direction.
     * @param gtuType GtuType; the type of the GTU infrastructure
     * @return boolean; true if the GTU is handled by the infrastructure in the given direction
     */
    @Deprecated
    boolean isCompatible(GtuType gtuType);

}
