package org.opentrafficsim.road.gtu.lane;

import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.perception.Perception;

/**
 * Utility to make debugging un a specific GTU more convenient.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 10 apr. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class Break
{

    /**
     * Constructor.
     */
    private Break()
    {
        //
    }
    
    /**
     * @param perception perception to obtain gtu from
     * @param id GTU id to break on
     * @param time time to break at (or after)
     * @param additionalCondition additional condition
     */
    public static void on(final Perception perception, final String id, final double time,
            final boolean additionalCondition)
    {
        try
        {
            on(perception.getGtu(), id, time, additionalCondition);
        }
        catch (GTUException exception)
        {
            throw new RuntimeException("Trying to break on gtu, but gtu could not be obtained from perception.", exception);
        }
    }

    /**
     * @param gtu GTU
     * @param id GTU id to break on
     * @param time time to break at (or after)
     * @param additionalCondition additional condition
     */
    public static void on(final GTU gtu, final String id, final double time, final boolean additionalCondition)
    {
        if (gtu.getId().equals(id) && gtu.getSimulator().getSimulatorTime().getTime().si >= time && additionalCondition)
        {
            System.err.println("Break condition for debugging is true.");
        }
    }

}
