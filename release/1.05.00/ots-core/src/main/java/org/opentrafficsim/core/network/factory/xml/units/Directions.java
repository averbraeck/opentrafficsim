package org.opentrafficsim.core.network.factory.xml.units;

import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
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
