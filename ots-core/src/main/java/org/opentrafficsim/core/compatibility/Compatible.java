package org.opentrafficsim.core.compatibility;

import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;

/**
 * Interface for infrastructure types to assess traversability by GTU types.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 25, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface Compatible
{

    /**
     * Default {@code Compatible} that allows every GTU type in every direction.
     */
    Compatible EVERYTHING = new Compatible()
    {
        @Override
        public boolean isCompatible(final GTUType gtuType, final GTUDirectionality directionality)
        {
            return true;
        }
    };

    /** Plus direction {@code Compatible} that allows all GTU types in the plus direction. */
    Compatible PLUS = new Compatible()
    {
        @Override
        public boolean isCompatible(final GTUType gtuType, final GTUDirectionality directionality)
        {
            return directionality.isPlus();
        }
    };

    /** Minus direction {@code Compatible} that allows all GTU types in the minus direction. */
    Compatible MINUS = new Compatible()
    {
        @Override
        public boolean isCompatible(final GTUType gtuType, final GTUDirectionality directionality)
        {
            return directionality.isMinus();
        }
    };

    /**
     * Test if a GTUType is handled by the infrastructure in the given direction. For Lane and Link, <cite>handled</cite> means
     * that GTUs of this type can travel over this infrastructure in the direction. For Sensors it means that the sensor will
     * detect GTUs that travel over it in the given direction.
     * @param gtuType GTUType; the type of the GTU
     * @param directionality GTUDirectionality; the direction of the GTU with respect to the design direction of the
     *            infrastructure
     * @return boolean; true if the GTU is handled by the infrastructure in the given direction
     */
    boolean isCompatible(GTUType gtuType, GTUDirectionality directionality);

}
