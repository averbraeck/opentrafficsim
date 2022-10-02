package org.opentrafficsim.road.gtu.generator.od;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType.DEFAULTS;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.parameters.ParameterFactoryByType;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLmrsPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LmrsFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.gtu.strategical.route.RouteGeneratorOd;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Supplies a strategical planner factory within DefaultGtuCharacteristicsGeneratorOD.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface StrategicalPlannerFactorySupplierOd
{

    /**
     * Supplies a strategical factory.
     * @param origin Node; origin
     * @param destination Node; destination
     * @param category Category; category (GTU type, route, or more)
     * @param randomStream StreamInterface; stream for random numbers
     * @return LaneBasedGtuCharacteristics
     * @throws GtuException if characteristics could not be generated for the GTUException
     */
    LaneBasedStrategicalPlannerFactory<?> getFactory(Node origin, Node destination, Category category,
            StreamInterface randomStream) throws GtuException;

    /**
     * Returns a standard implementation for LMRS.
     * @return standard implementation for LMRS
     */
    static StrategicalPlannerFactorySupplierOd lmrs()
    {
        return new StrategicalPlannerFactorySupplierOd()
        {
            /** {@inheritDoc} */
            @Override
            public LaneBasedStrategicalPlannerFactory<?> getFactory(final Node origin, final Node destination,
                    final Category category, final StreamInterface randomStream) throws GtuException
            {
                ParameterFactoryByType params = new ParameterFactoryByType();
                params.addParameter(origin.getNetwork().getGtuType(DEFAULTS.TRUCK), ParameterTypes.A,
                        Acceleration.instantiateSI(0.4));
                return new LaneBasedStrategicalRoutePlannerFactory(
                        new LmrsFactory(new IdmPlusFactory(randomStream), new DefaultLmrsPerceptionFactory()), params,
                        RouteGeneratorOd.getDefaultRouteSupplier(randomStream));
            }
        };
    }

    /**
     * Returns a {@code StrategicalPlannerFactorySupplierOD} using a {@code LaneBasedStrategicalRoutePlannerFactory} with a
     * tactical planner factory based on the given supplier. Simulations using this strategical level can be more easily
     * specified in this manner.
     * @param tacticalPlannerFactorySupplierOD TacticalPlannerFactorySupplierOD; tactical planner factory based on OD
     *            information
     * @return strategical factory with default strategical layer and specified tactical layer
     */
    static StrategicalPlannerFactorySupplierOd route(final TacticalPlannerFactorySupplierOD tacticalPlannerFactorySupplierOD)
    {
        return new StrategicalPlannerFactorySupplierOd()
        {
            /** {@inheritDoc} */
            @Override
            public LaneBasedStrategicalPlannerFactory<?> getFactory(final Node origin, final Node destination,
                    final Category category, final StreamInterface randomStream) throws GtuException
            {
                return new LaneBasedStrategicalRoutePlannerFactory(
                        tacticalPlannerFactorySupplierOD.getFactory(origin, destination, category, randomStream));
            }
        };
    }

    /**
     * Interface for tactical factory supplier based on OD information. This class is used by strategical factories where only
     * the strategical level is specified but where the lower levels can be specified.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public interface TacticalPlannerFactorySupplierOD
    {
        /**
         * Returns a tactical planner factory based on OD information.
         * @param origin Node; origin
         * @param destination Node; destination
         * @param category Category; OD category
         * @param randomStream StreamInterface; random stream
         * @return tactical planner factory based on OD information
         */
        LaneBasedTacticalPlannerFactory<? extends LaneBasedTacticalPlanner> getFactory(Node origin, Node destination,
                Category category, StreamInterface randomStream);
    }

}
