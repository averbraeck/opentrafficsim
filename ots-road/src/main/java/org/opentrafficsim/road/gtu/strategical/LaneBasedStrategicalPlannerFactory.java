package org.opentrafficsim.road.gtu.strategical;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * A factory class is used to generate strategical planners as the strategical planner is state-full.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 29, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> class of the strategical planner generated
 */
public interface LaneBasedStrategicalPlannerFactory<T extends LaneBasedStrategicalPlanner>
{

    /**
     * Creates a new strategical planner for the given GTU. This method should also set the parameters at the GTU.
     * @param gtu LaneBasedGTU; GTU
     * @param route Route; route, may be null
     * @param origin Node; origin, may be null
     * @param destination Node; destination, may be null
     * @return strategical planner for the given GTU
     * @throws GTUException if the gtu is not suitable in any way for the creation of the strategical planner
     */
    T create(LaneBasedGTU gtu, Route route, Node origin, Node destination) throws GTUException;

    /**
     * Peek to see the desired speed of the next GTU to be generated at the given location. The default implementation returns
     * {@code null}, at which point the GTU generator will use some other speed.
     * @param gtuType GTUType; GTU type
     * @param speedLimit Speed; speed limit
     * @param maxGtuSpeed Speed; maximum GTU speed
     * @return desired speed of the next GTU to be generated at the given location, may be {@code null} at which point the GTU
     *         generator will use some other speed
     * @throws GTUException on parameter exception or network exception
     */
    default Speed peekDesiredSpeed(final GTUType gtuType, final Speed speedLimit, final Speed maxGtuSpeed) throws GTUException
    {
        return null;
    }

    /**
     * Peek to see the desired headway of the next GTU to be generated at the given speed. The default implementation returns
     * {@code null}, at which point the GTU generator will only generate GTU's at fixed locations.
     * @param gtuType GTUType; GTU type
     * @param speed Speed; speed the GTU might be generated at
     * @return Length; desired headway of the next GTU to be generated at the given speed, may be {@code null} at which point
     *         the GTU generator only generate GTU's at fixed locations
     * @throws GTUException on parameter exception or network exception
     */
    default Length peekDesiredHeadway(final GTUType gtuType, final Speed speed) throws GTUException
    {
        return null;
    }

}
