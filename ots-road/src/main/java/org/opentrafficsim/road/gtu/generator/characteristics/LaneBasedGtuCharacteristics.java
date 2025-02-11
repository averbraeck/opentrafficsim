package org.opentrafficsim.road.gtu.generator.characteristics;

import org.opentrafficsim.core.gtu.GtuCharacteristics;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.VehicleModel;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;

/**
 * Characteristics for a lane base GTU. This class is used to store all characteristics of a (not-yet constructed) LaneBasedGtu.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class LaneBasedGtuCharacteristics extends GtuCharacteristics
{
    /** */
    private static final long serialVersionUID = 1L;

    /** The strategical planner factory. */
    private final LaneBasedStrategicalPlannerFactory<?> strategicalPlannerFactory;

    /** Route. */
    private final Route route;

    /** Origin. */
    private final Node origin;

    /** Destination. */
    private final Node destination;

    /** Vehicle model. */
    private final VehicleModel vehicleModel;

    /**
     * Construct a new set of lane based GTU characteristics.
     * @param gtuCharacteristics characteristics of the super GTU type to be used for the GTU
     * @param laneBasedStrategicalPlannerFactory the strategical planner for the GTU
     * @param route route
     * @param origin origin
     * @param destination destination
     * @param vehicleModel vehicle model
     */
    public LaneBasedGtuCharacteristics(final GtuCharacteristics gtuCharacteristics,
            final LaneBasedStrategicalPlannerFactory<?> laneBasedStrategicalPlannerFactory, final Route route,
            final Node origin, final Node destination, final VehicleModel vehicleModel)
    {
        super(gtuCharacteristics.getGtuType(), gtuCharacteristics.getLength(), gtuCharacteristics.getWidth(),
                gtuCharacteristics.getMaximumSpeed(), gtuCharacteristics.getMaximumAcceleration(),
                gtuCharacteristics.getMaximumDeceleration(), gtuCharacteristics.getFront());
        this.strategicalPlannerFactory = laneBasedStrategicalPlannerFactory;
        this.route = route;
        this.origin = origin;
        this.destination = destination;
        this.vehicleModel = vehicleModel;
    }

    /**
     * Returns the strategical planner factory.
     * @return the strategical planner factory for the GTU
     */
    public final LaneBasedStrategicalPlannerFactory<?> getStrategicalPlannerFactory()
    {
        return this.strategicalPlannerFactory;
    }

    /**
     * Returns the route.
     * @return route
     */
    public final Route getRoute()
    {
        return this.route;
    }

    /**
     * Returns the origin.
     * @return origin
     */
    public Node getOrigin()
    {
        return this.origin;
    }

    /**
     * Returns the destination.
     * @return destination
     */
    public Node getDestination()
    {
        return this.destination;
    }

    /**
     * Returns the vehicle model.
     * @return vehicle model
     */
    public VehicleModel getVehicleModel()
    {
        return this.vehicleModel;
    }

}
