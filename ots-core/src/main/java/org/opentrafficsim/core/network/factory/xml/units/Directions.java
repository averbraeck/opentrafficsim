package org.opentrafficsim.core.network.factory.xml.units;

import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 */
public final class Directions
{
    /** Utility class. */
    private Directions()
    {
        // do not instantiate
    }

    /**
     * @param dirStr String; the direction.
     * @return the directionality.
     * @throws NetworkException in case of unknown model.
     */
    public static LongitudinalDirectionality parseDirection(final String dirStr) throws NetworkException
    {
        if (dirStr.equals("FORWARD"))
        {
            return LongitudinalDirectionality.DIR_PLUS;
        }
        else if (dirStr.equals("BACKWARD"))
        {
            return LongitudinalDirectionality.DIR_MINUS;
        }
        else if (dirStr.equals("BOTH"))
        {
            return LongitudinalDirectionality.DIR_BOTH;
        }
        throw new NetworkException("Unknown directionality: " + dirStr);
    }

}
