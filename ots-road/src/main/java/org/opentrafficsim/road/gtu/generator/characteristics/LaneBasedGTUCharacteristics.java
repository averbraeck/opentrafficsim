package org.opentrafficsim.road.gtu.generator.characteristics;

import org.opentrafficsim.core.gtu.GTUCharacteristics;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.VehicleModel;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;

/**
 * Characteristics for a lane base GTU. This class is used to store all characteristics of a (not-yet constructed) LaneBasedGTU.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
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

    /** Origin. */
    private final Node origin;

    /** Destination. */
    private final Node destination;

    /** Vehicle model. */
    private final VehicleModel vehicleModel;

    /**
     * Construct a new set of lane based GTU characteristics.
     * @param gtuCharacteristics GTUCharacteristics; characteristics of the super GTU type to be used for the GTU
     * @param laneBasedStrategicalPlannerFactory LaneBasedStrategicalPlannerFactory&lt;?&gt;; the strategical planner for the
     *            GTU
     * @param route Route; route
     * @param origin Node; origin
     * @param destination Node; destination
     * @param vehicleModel VehicleModel; vehicle model
     */
    public LaneBasedGTUCharacteristics(final GTUCharacteristics gtuCharacteristics,
            final LaneBasedStrategicalPlannerFactory<?> laneBasedStrategicalPlannerFactory, final Route route,
            final Node origin, final Node destination, final VehicleModel vehicleModel)
    {
        super(gtuCharacteristics.getGTUType(), gtuCharacteristics.getLength(), gtuCharacteristics.getWidth(),
                gtuCharacteristics.getMaximumSpeed(), gtuCharacteristics.getMaximumAcceleration(),
                gtuCharacteristics.getMaximumDeceleration(), gtuCharacteristics.getFront());
        this.strategicalPlannerFactory = laneBasedStrategicalPlannerFactory;
        this.route = route;
        this.origin = origin;
        this.destination = destination;
        this.vehicleModel = vehicleModel;
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
     * @return Node; origin
     */
    public Node getOrigin()
    {
        return this.origin;
    }

    /**
     * @return Node; destination
     */
    public Node getDestination()
    {
        return this.destination;
    }

    /**
     * Returns the vehicle model.
     * @return VehicleModel; vehicle model
     */
    public VehicleModel getVehicleModel()
    {
        return this.vehicleModel;
    }

}
