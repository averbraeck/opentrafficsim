package org.opentrafficsim.road.gtu.lane.tactical;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;

/**
 * A factory class is used to generate tactical planners as the tactical planner is state-full.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> class of the tactical planner generated
 */
public interface LaneBasedTacticalPlannerFactory<T extends LaneBasedTacticalPlanner> extends ModelComponentFactory
{

    /**
     * Creates a new tactical planner for the given GTU.
     * @param gtu GTU
     * @return tactical planner for the given GTU
     * @throws GtuException if the gtu is not suitable in any way for the creation of the tactical planner
     */
    T create(LaneBasedGtu gtu) throws GtuException;

    /**
     * Peek to see the desired speed of the next GTU to be generated at the given location. The default implementation returns
     * {@code null}, at which point the GTU generator will use some other speed.
     * @param gtuType GTU type
     * @param speedLimit speed limit
     * @param maxGtuSpeed maximum GTU speed
     * @param parameters parameters for the next GTU
     * @return desired speed of the next GTU to be generated at the given location, may be {@code null} at which point the GTU
     *         generator will use some other speed
     * @throws GtuException on any exception
     */
    default Speed peekDesiredSpeed(GtuType gtuType, Speed speedLimit, Speed maxGtuSpeed, Parameters parameters)
            throws GtuException
    {
        return null;
    }

    /**
     * Peek to see the desired headway of the next GTU to be generated at the given speed. The default implementation returns
     * {@code null}, at which point the GTU generator will only generate GTU's at fixed locations.
     * @param gtuType GTU type
     * @param speed speed the GTU might be generated at
     * @param parameters parameters for the next GTU
     * @return desired headway of the next GTU to be generated at the given speed, may be {@code null} at which point the GTU
     *         generator will only generate GTU's at fixed locations
     * @throws GtuException on any exception
     */
    default Length peekDesiredHeadway(GtuType gtuType, Speed speed, Parameters parameters) throws GtuException
    {
        return null;
    }

}
