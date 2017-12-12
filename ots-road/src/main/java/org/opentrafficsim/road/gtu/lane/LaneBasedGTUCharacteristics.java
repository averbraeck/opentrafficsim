package org.opentrafficsim.road.gtu.lane;

import java.util.Set;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUCharacteristics;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;

/**
 * Characteristics for a lane base GTU. This class is used to store all characteristics of a (not-yet constructed) LaneBasedGTU.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 8, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBasedGTUCharacteristics extends GTUCharacteristics
{
    /** */
    private static final long serialVersionUID = 1L;

    /** The strategical planner factory. */
    private final LaneBasedStrategicalPlannerFactory<?> strategicalPlannerFactory;

    /** Route. */
    private final Route route;

    /** The maximum speed of the GTU. */
    private final Speed speed;

    /** The initial lanes, positions and direction of the GTU. */
    private final Set<DirectedLanePosition> initialPosition;

    /**
     * Construct a new set of lane based GTU characteristics.
     * @param gtuCharacteristics GTUCharacteristics; characteristics of the super GTU type to be used for the GTU
     * @param laneBasedStrategicalPlannerFactory LaneBasedStrategicalPlannerFactory; the strategical planner for the GTU
     * @param route route
     * @param speed Speed; the initial speed of the GTU
     * @param initialPosition Set&lt;DirectedLanePosition&gt;; the lane, initial position and direction of the GTU
     */
    public LaneBasedGTUCharacteristics(final GTUCharacteristics gtuCharacteristics,
            final LaneBasedStrategicalPlannerFactory<?> laneBasedStrategicalPlannerFactory, final Route route,
            final Speed speed, final Set<DirectedLanePosition> initialPosition)
    {
        super(gtuCharacteristics.getGTUType(), gtuCharacteristics.getLength(),
                gtuCharacteristics.getWidth(), gtuCharacteristics.getMaximumSpeed());
        this.strategicalPlannerFactory = laneBasedStrategicalPlannerFactory;
        this.route = route;
        this.speed = speed;
        this.initialPosition = initialPosition;
    }

    /**
     * @return LaneBasedStrategicalPlannerFactory; the strategical planner factory for the GTU
     */
    public final LaneBasedStrategicalPlannerFactory<?> getStrategicalPlannerFactory()
    {
        return this.strategicalPlannerFactory;
    }

    /**
     * @return Route; route
     */
    public final Route getRoute()
    {
        return this.route;
    }

    /**
     * @return Speed; the maximum speed of the GTU
     */
    public final Speed getSpeed()
    {
        return this.speed;
    }

    /**
     * @return Set&lt;DirectedLanePosition&gt;; the position and direction on each lane that the GTU will initially be on
     */
    public final Set<DirectedLanePosition> getInitialPosition()
    {
        return this.initialPosition;
    }

}
