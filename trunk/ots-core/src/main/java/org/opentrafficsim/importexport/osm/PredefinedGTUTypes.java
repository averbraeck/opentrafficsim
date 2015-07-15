package org.opentrafficsim.importexport.osm;

import org.opentrafficsim.core.gtu.GTUType;

/**
 * Some predefined GTU types.<br>
 * Should be moved into the GTU package.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial versionFeb 26, 2015 <br>
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
    public static final GTUType<String> BIKE = GTUType.makeGTUType("Bike");
    
    /** Car. */
    public static final GTUType<String> CAR = GTUType.makeGTUType("Car");
    
    /** Pedestrian. */
    public static final GTUType<String> PEDESTRIAN = GTUType.makeGTUType("Pedestrian");
    
    /** Boat. */
    public static final GTUType<String> BOAT = GTUType.makeGTUType("Boat");
    
}
