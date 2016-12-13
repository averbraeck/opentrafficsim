package org.opentrafficsim.road.gtu.lane;

import static org.opentrafficsim.core.gtu.GTUType.VEHICLE;

import org.opentrafficsim.core.gtu.GTUType;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 13 dec. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class RoadGTUTypes
{

    /** Super type for cars. */
    public static final GTUType CAR;

    /** Super type for vans. */
    public static final GTUType VAN;

    /** Super type for busses. */
    public static final GTUType BUS;

    /** Super type for trucks. */
    public static final GTUType TRUCK;

    /* static block to guarantee that ALL is always on the first place, and NONE on the second, for code reproducibility. */
    static
    {
        CAR = new GTUType("CAR", VEHICLE);
        VAN = new GTUType("VAN", VEHICLE);
        BUS = new GTUType("BUS", VEHICLE);
        TRUCK = new GTUType("TRUCK", VEHICLE);
    }

    /**
     * Empty constructor.
     */
    private RoadGTUTypes()
    {
        //
    }

}
