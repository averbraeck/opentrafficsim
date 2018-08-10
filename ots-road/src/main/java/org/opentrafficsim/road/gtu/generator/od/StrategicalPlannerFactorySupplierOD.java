package org.opentrafficsim.road.gtu.generator.od;

import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLMRSPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRSFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.gtu.strategical.route.RouteSupplier;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Supplies a strategical planner factories within DefaultGTUCharacteristicsGeneratorOD.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 26 mrt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface StrategicalPlannerFactorySupplierOD
{
    /** Default LMRS model. */
    StrategicalPlannerFactorySupplierOD LMRS = new StrategicalPlannerFactorySupplierOD()
    {
        @Override
        public LaneBasedStrategicalPlannerFactory<?> getFactory(final Node origin, final Node destination,
                final Category category, final StreamInterface randomStream) throws GTUException
        {
            return new LaneBasedStrategicalRoutePlannerFactory(
                    new LMRSFactory(new IDMPlusFactory(randomStream), new DefaultLMRSPerceptionFactory()),
                    RouteSupplier.SHORTEST);
        }
    };

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

}
