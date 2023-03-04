package org.opentrafficsim.road.gtu.strategical;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;

/**
 * A factory class is used to generate strategical planners as the strategical planner is state-full.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <T> class of the strategical planner generated
 */
public interface LaneBasedStrategicalPlannerFactory<T extends LaneBasedStrategicalPlanner>
{

    /**
     * Creates a new strategical planner for the given GTU. This method should also set the parameters at the GTU.
     * @param gtu LaneBasedGtu; GTU
     * @param route Route; route, may be null
     * @param origin Node; origin, may be null
     * @param destination Node; destination, may be null
     * @return strategical planner for the given GTU
     * @throws GtuException if the gtu is not suitable in any way for the creation of the strategical planner
     */
    T create(LaneBasedGtu gtu, Route route, Node origin, Node destination) throws GtuException;

    /**
     * Peek to see the desired speed of the next GTU to be generated at the given location. The default implementation returns
     * {@code null}, at which point the GTU generator will use some other speed.
     * @param gtuType GtuType; GTU type
     * @param speedLimit Speed; speed limit
     * @param maxGtuSpeed Speed; maximum GTU speed
     * @return desired speed of the next GTU to be generated at the given location, may be {@code null} at which point the GTU
     *         generator will use some other speed
     * @throws GtuException on parameter exception or network exception
     */
    default Speed peekDesiredSpeed(final GtuType gtuType, final Speed speedLimit, final Speed maxGtuSpeed) throws GtuException
    {
        return null;
    }

    /**
     * Peek to see the desired headway of the next GTU to be generated at the given speed. The default implementation returns
     * {@code null}, at which point the GTU generator will only generate GTU's at fixed locations.
     * @param gtuType GtuType; GTU type
     * @param speed Speed; speed the GTU might be generated at
     * @return Length; desired headway of the next GTU to be generated at the given speed, may be {@code null} at which point
     *         the GTU generator only generate GTU's at fixed locations
     * @throws GtuException on parameter exception or network exception
     */
    default Length peekDesiredHeadway(final GtuType gtuType, final Speed speed) throws GtuException
    {
        return null;
    }

}
