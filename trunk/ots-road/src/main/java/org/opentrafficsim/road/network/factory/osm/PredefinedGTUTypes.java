package org.opentrafficsim.road.network.factory.osm;

import org.opentrafficsim.core.gtu.GTUType;

/**
 * Some predefined GTU types.<br>
 * Should be moved into the GTU package.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-08-23 00:48:01 +0200 (Sun, 23 Aug 2015) $, @version $Revision: 1291 $, by $Author: averbraeck $,
 * initial version Feb 26, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public final class PredefinedGTUTypes
{
    /** Do not instantiate this class. */
    private PredefinedGTUTypes()
    {
        // Cannot be instantiated.
    }

    /** Bicycle. */
    public static final GTUType BIKE = GTUType.getInstance("Bike");

    /** Car. */
    public static final GTUType CAR = GTUType.getInstance("Car");

    /** Pedestrian. */
    public static final GTUType PEDESTRIAN = GTUType.getInstance("Pedestrian");

    /** Boat. */
    public static final GTUType BOAT = GTUType.getInstance("Boat");

}
