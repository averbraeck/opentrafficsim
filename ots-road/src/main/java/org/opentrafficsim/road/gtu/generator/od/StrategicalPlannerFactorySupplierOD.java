package org.opentrafficsim.road.gtu.generator.od;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType.DEFAULTS;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.parameters.ParameterFactoryByType;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLMRSPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRSFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.gtu.strategical.route.RouteGeneratorOD;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Supplies a strategical planner factory within DefaultGTUCharacteristicsGeneratorOD.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 26 mrt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface StrategicalPlannerFactorySupplierOD
{

    /**
     * Supplies a strategical factory.
     * @param origin Node; origin
     * @param destination Node; destination
     * @param category Category; category (GTU type, route, or more)
     * @param randomStream StreamInterface; stream for random numbers
     * @return LaneBasedGTUCharacteristics
     * @throws GTUException if characteristics could not be generated for the GTUException
     */
    LaneBasedStrategicalPlannerFactory<?> getFactory(Node origin, Node destination, Category category,
            StreamInterface randomStream) throws GTUException;

    /**
     * Returns a standard implementation for LMRS.
     * @return standard implementation for LMRS
     */
    static StrategicalPlannerFactorySupplierOD lmrs()
    {
        return new StrategicalPlannerFactorySupplierOD()
        {
            /** {@inheritDoc} */
            @Override
            public LaneBasedStrategicalPlannerFactory<?> getFactory(final Node origin, final Node destination,
                    final Category category, final StreamInterface randomStream) throws GTUException
            {
                ParameterFactoryByType params = new ParameterFactoryByType();
                params.addParameter(origin.getNetwork().getGtuType(DEFAULTS.TRUCK), ParameterTypes.A,
                        Acceleration.instantiateSI(0.4));
                return new LaneBasedStrategicalRoutePlannerFactory(
                        new LMRSFactory(new IDMPlusFactory(randomStream), new DefaultLMRSPerceptionFactory()), params,
                        RouteGeneratorOD.getDefaultRouteSupplier(randomStream));
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
    static StrategicalPlannerFactorySupplierOD route(final TacticalPlannerFactorySupplierOD tacticalPlannerFactorySupplierOD)
    {
        return new StrategicalPlannerFactorySupplierOD()
        {
            /** {@inheritDoc} */
            @Override
            public LaneBasedStrategicalPlannerFactory<?> getFactory(final Node origin, final Node destination,
                    final Category category, final StreamInterface randomStream) throws GTUException
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
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 8 jan. 2019 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
