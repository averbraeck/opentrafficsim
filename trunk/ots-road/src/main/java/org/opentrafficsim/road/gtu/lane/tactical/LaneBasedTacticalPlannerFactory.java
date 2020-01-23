package org.opentrafficsim.road.gtu.lane.tactical;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * A factory class is used to generate tactical planners as the tactical planner is state-full.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 29, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> class of the tactical planner generated
 */
public interface LaneBasedTacticalPlannerFactory<T extends LaneBasedTacticalPlanner> extends ModelComponentFactory
{

    /**
     * Creates a new tactical planner for the given GTU.
     * @param gtu LaneBasedGTU; GTU
     * @return tactical planner for the given GTU
     * @throws GTUException if the gtu is not suitable in any way for the creation of the tactical planner
     */
    T create(LaneBasedGTU gtu) throws GTUException;

    /**
     * Peek to see the desired speed of the next GTU to be generated at the given location. The default implementation returns
     * {@code null}, at which point the GTU generator will use some other speed.
     * @param gtuType GTUType; GTU type
     * @param speedLimit Speed; speed limit
     * @param maxGtuSpeed Speed; maximum GTU speed
     * @param parameters Parameters; parameters for the next GTU
     * @return desired speed of the next GTU to be generated at the given location, may be {@code null} at which point the GTU
     *         generator will use some other speed
     * @throws GTUException on any exception
     */
    default Speed peekDesiredSpeed(GTUType gtuType, Speed speedLimit, Speed maxGtuSpeed, Parameters parameters)
            throws GTUException
    {
        return null;
    }

    /**
     * Peek to see the desired headway of the next GTU to be generated at the given speed. The default implementation returns
     * {@code null}, at which point the GTU generator will only generate GTU's at fixed locations.
     * @param gtuType GTUType; GTU type
     * @param speed Speed; speed the GTU might be generated at
     * @param parameters Parameters; parameters for the next GTU
     * @return Length; desired headway of the next GTU to be generated at the given speed, may be {@code null} at which point
     *         the GTU generator will only generate GTU's at fixed locations
     * @throws GTUException on any exception
     */
    default Length peekDesiredHeadway(GTUType gtuType, Speed speed, Parameters parameters) throws GTUException
    {
        return null;
    }

}
