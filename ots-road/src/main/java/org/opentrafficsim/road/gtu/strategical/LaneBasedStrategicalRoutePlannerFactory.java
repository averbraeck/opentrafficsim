package org.opentrafficsim.road.gtu.strategical;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.parameters.ParameterFactory;
import org.opentrafficsim.core.parameters.ParameterFactoryDefault;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;

/**
 * Factory for creating {@code LaneBasedStrategicalRoutePlanner} using any {@code LaneBasedTacticalPlannerFactory}.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LaneBasedStrategicalRoutePlannerFactory
        implements LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalRoutePlanner>, Serializable
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Factory for tactical planners. */
    private final LaneBasedTacticalPlannerFactory<? extends LaneBasedTacticalPlanner> tacticalPlannerFactory;

    /** Route supplier. */
    private final RouteGenerator routeGenerator;

    /** Parameter factory. */
    private final ParameterFactory parameterFactory;

    /** Peeked parameters. */
    private Parameters peekedParameters = null;

    /**
     * Constructor with factory for tactical planners.
     * @param tacticalPlannerFactory LaneBasedTacticalPlannerFactory&lt;? extends LaneBasedTacticalPlanner&gt;; factory for
     *            tactical planners
     */
    public LaneBasedStrategicalRoutePlannerFactory(
            final LaneBasedTacticalPlannerFactory<? extends LaneBasedTacticalPlanner> tacticalPlannerFactory)
    {
        this.tacticalPlannerFactory = tacticalPlannerFactory;
        this.routeGenerator = RouteGenerator.NULL;
        this.parameterFactory = new ParameterFactoryDefault();
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
        this.tacticalPlannerFactory = tacticalPlannerFactory;
        this.routeGenerator = RouteGenerator.NULL;
        this.parameterFactory = parametersFactory;
    }

    /**
     * Constructor with factory for tactical planners.
     * @param tacticalPlannerFactory LaneBasedTacticalPlannerFactory&lt;? extends LaneBasedTacticalPlanner&gt;; factory for
     *            tactical planners
     * @param parametersFactory ParameterFactory; factory for parameters
     * @param routeGenerator RouteGenerator; route supplier
     */
    public LaneBasedStrategicalRoutePlannerFactory(
            final LaneBasedTacticalPlannerFactory<? extends LaneBasedTacticalPlanner> tacticalPlannerFactory,
            final ParameterFactory parametersFactory, final RouteGenerator routeGenerator)
    {
        this.tacticalPlannerFactory = tacticalPlannerFactory;
        this.parameterFactory = parametersFactory;
        this.routeGenerator = routeGenerator;
    }

    /** {@inheritDoc} */
    @Override
    public final Speed peekDesiredSpeed(final GtuType gtuType, final Speed speedLimit, final Speed maxGtuSpeed)
            throws GtuException
    {
        return this.tacticalPlannerFactory.peekDesiredSpeed(gtuType, speedLimit, maxGtuSpeed, peekParameters(gtuType));
    }

    /** {@inheritDoc} */
    @Override
    public final Length peekDesiredHeadway(final GtuType gtuType, final Speed speed) throws GtuException
    {
        return this.tacticalPlannerFactory.peekDesiredHeadway(gtuType, speed, peekParameters(gtuType));
    }

    /**
     * Determine or return the next parameter set.
     * @param gtuType GtuType; GTU type to generate parameters for
     * @return Parameters; next parameter set
     * @throws GtuException on parameter exception
     */
    private Parameters peekParameters(final GtuType gtuType) throws GtuException
    {
        if (this.peekedParameters != null)
        {
            return this.peekedParameters;
        }
        try
        {
            this.peekedParameters = this.tacticalPlannerFactory.getParameters();
            this.parameterFactory.setValues(this.peekedParameters, gtuType);
        }
        catch (ParameterException exception)
        {
            throw new GtuException("Parameter was set to illegal value.", exception);
        }
        return this.peekedParameters;
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedStrategicalRoutePlanner create(final LaneBasedGtu gtu, final Route route, final Node origin,
            final Node destination) throws GtuException
    {
        LaneBasedStrategicalRoutePlanner strategicalPlanner = new LaneBasedStrategicalRoutePlanner(
                this.tacticalPlannerFactory.create(gtu), route, gtu, origin, destination, this.routeGenerator);
        gtu.setParameters(nextParameters(gtu.getType()));
        return strategicalPlanner;
    }

    /**
     * Returns the parameters for the next GTU.
     * @param gtuType GtuType; GTU type of GTU to be generated
     * @return Parameters; parameters for the next GTU
     * @throws GtuException on parameter exception
     */
    protected final Parameters nextParameters(final GtuType gtuType) throws GtuException
    {
        Parameters parameters = peekParameters(gtuType);
        this.peekedParameters = null;
        return parameters;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneBasedStrategicalRoutePlannerFactory [tacticalPlannerFactory=" + this.tacticalPlannerFactory + "]";
    }

}
