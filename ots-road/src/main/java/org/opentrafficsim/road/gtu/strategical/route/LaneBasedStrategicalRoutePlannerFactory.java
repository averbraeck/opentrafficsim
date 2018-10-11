package org.opentrafficsim.road.gtu.strategical.route;

import java.io.Serializable;

import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterFactory;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.AbstractLaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;

/**
 * Factory for creating {@code LaneBasedStrategicalRoutePlanner} using any {@code LaneBasedTacticalPlannerFactory}.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 2, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LaneBasedStrategicalRoutePlannerFactory
        extends AbstractLaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Route supplier. */
    private final RouteSupplier routeSupplier;

    /**
     * Constructor with factory for tactical planners.
     * @param tacticalPlannerFactory LaneBasedTacticalPlannerFactory&lt;? extends LaneBasedTacticalPlanner&gt;; factory for
     *            tactical planners
     */
    public LaneBasedStrategicalRoutePlannerFactory(
            final LaneBasedTacticalPlannerFactory<? extends LaneBasedTacticalPlanner> tacticalPlannerFactory)
    {
        super(tacticalPlannerFactory);
        this.routeSupplier = null;
    }

    /**
     * Constructor with factory for tactical planners.
     * @param tacticalPlannerFactory LaneBasedTacticalPlannerFactory&lt;? extends LaneBasedTacticalPlanner&gt;; factory for
     *            tactical planners
     * @param routeSupplier RouteSupplier; route supplier
     */
    public LaneBasedStrategicalRoutePlannerFactory(
            final LaneBasedTacticalPlannerFactory<? extends LaneBasedTacticalPlanner> tacticalPlannerFactory,
            final RouteSupplier routeSupplier)
    {
        super(tacticalPlannerFactory);
        this.routeSupplier = routeSupplier;
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
        this.routeSupplier = null;
    }

    /**
     * Constructor with factory for tactical planners.
     * @param tacticalPlannerFactory LaneBasedTacticalPlannerFactory&lt;? extends LaneBasedTacticalPlanner&gt;; factory for
     *            tactical planners
     * @param parametersFactory ParameterFactory; factory for parameters
     * @param routeSupplier RouteSupplier; route supplier
     */
    public LaneBasedStrategicalRoutePlannerFactory(
            final LaneBasedTacticalPlannerFactory<? extends LaneBasedTacticalPlanner> tacticalPlannerFactory,
            final ParameterFactory parametersFactory, final RouteSupplier routeSupplier)
    {
        super(tacticalPlannerFactory, parametersFactory);
        this.routeSupplier = routeSupplier;
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedStrategicalPlanner create(final LaneBasedGTU gtu, final Route route, final Node origin,
            final Node destination) throws GTUException
    {
        LaneBasedStrategicalRoutePlanner strategicalPlanner;
        if (this.routeSupplier == null)
        {
            strategicalPlanner =
                    new LaneBasedStrategicalRoutePlanner(nextTacticalPlanner(gtu), route, gtu, origin, destination);
        }
        else
        {
            strategicalPlanner = new LaneBasedStrategicalRoutePlanner(nextTacticalPlanner(gtu), route, gtu, origin, destination,
                    this.routeSupplier);
        }
        gtu.setParameters(nextParameters(gtu.getGTUType()));
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
