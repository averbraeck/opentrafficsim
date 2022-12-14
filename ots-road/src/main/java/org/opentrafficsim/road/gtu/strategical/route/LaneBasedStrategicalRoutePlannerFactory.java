package org.opentrafficsim.road.gtu.strategical.route;

import java.io.Serializable;

import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.parameters.ParameterFactory;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.AbstractLaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;

/**
 * Factory for creating {@code LaneBasedStrategicalRoutePlanner} using any {@code LaneBasedTacticalPlannerFactory}.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class LaneBasedStrategicalRoutePlannerFactory
        extends AbstractLaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Route supplier. */
    private final RouteGeneratorOd routeGenerator;

    /**
     * Constructor with factory for tactical planners.
     * @param tacticalPlannerFactory LaneBasedTacticalPlannerFactory&lt;? extends LaneBasedTacticalPlanner&gt;; factory for
     *            tactical planners
     */
    public LaneBasedStrategicalRoutePlannerFactory(
            final LaneBasedTacticalPlannerFactory<? extends LaneBasedTacticalPlanner> tacticalPlannerFactory)
    {
        super(tacticalPlannerFactory);
        this.routeGenerator = null;
    }

    /**
     * Constructor with factory for tactical planners.
     * @param tacticalPlannerFactory LaneBasedTacticalPlannerFactory&lt;? extends LaneBasedTacticalPlanner&gt;; factory for
     *            tactical planners
     * @param routeGenerator RouteGeneratorOD; route generator
     */
    public LaneBasedStrategicalRoutePlannerFactory(
            final LaneBasedTacticalPlannerFactory<? extends LaneBasedTacticalPlanner> tacticalPlannerFactory,
            final RouteGeneratorOd routeGenerator)
    {
        super(tacticalPlannerFactory);
        this.routeGenerator = routeGenerator;
    }

    /**
     * Constructor with factory for tactical planners.
     * @param tacticalPlannerFactory LaneBasedTacticalPlannerFactory&lt;? extends LaneBasedTacticalPlanner&gt;; factory for
     *            tactical planners
     * @param parametersFactory ParameterFactory; factory for parameters
     */
    public LaneBasedStrategicalRoutePlannerFactory(
            final LaneBasedTacticalPlannerFactory<? extends LaneBasedTacticalPlanner> tacticalPlannerFactory,
            final ParameterFactory parametersFactory)
    {
        super(tacticalPlannerFactory, parametersFactory);
        this.routeGenerator = null;
    }

    /**
     * Constructor with factory for tactical planners.
     * @param tacticalPlannerFactory LaneBasedTacticalPlannerFactory&lt;? extends LaneBasedTacticalPlanner&gt;; factory for
     *            tactical planners
     * @param parametersFactory ParameterFactory; factory for parameters
     * @param routeGenerator RouteGeneratorOD; route supplier
     */
    public LaneBasedStrategicalRoutePlannerFactory(
            final LaneBasedTacticalPlannerFactory<? extends LaneBasedTacticalPlanner> tacticalPlannerFactory,
            final ParameterFactory parametersFactory, final RouteGeneratorOd routeGenerator)
    {
        super(tacticalPlannerFactory, parametersFactory);
        this.routeGenerator = routeGenerator;
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedStrategicalPlanner create(final LaneBasedGtu gtu, final Route route, final Node origin,
            final Node destination) throws GtuException
    {
        LaneBasedStrategicalRoutePlanner strategicalPlanner;
        if (this.routeGenerator == null)
        {
            strategicalPlanner =
                    new LaneBasedStrategicalRoutePlanner(nextTacticalPlanner(gtu), route, gtu, origin, destination);
        }
        else
        {
            strategicalPlanner = new LaneBasedStrategicalRoutePlanner(nextTacticalPlanner(gtu), route, gtu, origin, destination,
                    this.routeGenerator);
        }
        gtu.setParameters(nextParameters(gtu.getType()));
        return strategicalPlanner;
    }

    /** {@inheritDoc} */
    @Override
    protected Parameters getParameters()
    {
        // no specific parameters required for a LaneBasedStrategicalRoutePlanner
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneBasedStrategicalRoutePlannerFactory [tacticalPlannerFactory=" + getTacticalPlannerFactory() + "]";
    }

}
