package org.opentrafficsim.road.gtu.strategical;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.parameters.ParameterFactory;
import org.opentrafficsim.core.parameters.ParameterFactoryDefault;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;

/**
 * Factory for creating {@code LaneBasedStrategicalRoutePlanner} using any {@code LaneBasedTacticalPlannerFactory}. This
 * abstract class deals with forwarding peeking from the GTU generator to the tactical planner factory. Parameters are set using
 * a {@code ParameterFactory}, after the method {@code setParameters()} has been called, which subclasses need to implement.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <T> class of the strategical planner generated
 */
public abstract class AbstractLaneBasedStrategicalPlannerFactory<T extends LaneBasedStrategicalPlanner>
        implements LaneBasedStrategicalPlannerFactory<T>
{

    /** Factory for tactical planners. */
    private final LaneBasedTacticalPlannerFactory<? extends LaneBasedTacticalPlanner> tacticalPlannerFactory;

    /** Parameter factory. */
    private final ParameterFactory parameterFactory;

    /** Peeked parameters. */
    private Parameters peekedParameters = null;

    /**
     * Constructor with factory for tactical planners.
     * @param tacticalPlannerFactory LaneBasedTacticalPlannerFactory&lt;? extends LaneBasedTacticalPlanner&gt;; factory for
     *            tactical planners
     */
    public AbstractLaneBasedStrategicalPlannerFactory(
            final LaneBasedTacticalPlannerFactory<? extends LaneBasedTacticalPlanner> tacticalPlannerFactory)
    {
        this.tacticalPlannerFactory = tacticalPlannerFactory;
        this.parameterFactory = new ParameterFactoryDefault();
    }

    /**
     * Constructor with factory for tactical planners.
     * @param tacticalPlannerFactory LaneBasedTacticalPlannerFactory&lt;? extends LaneBasedTacticalPlanner&gt;; factory for
     *            tactical planners
     * @param parametersFactory ParameterFactory; factory for parameters
     */
    public AbstractLaneBasedStrategicalPlannerFactory(
            final LaneBasedTacticalPlannerFactory<? extends LaneBasedTacticalPlanner> tacticalPlannerFactory,
            final ParameterFactory parametersFactory)
    {
        this.tacticalPlannerFactory = tacticalPlannerFactory;
        this.parameterFactory = parametersFactory;
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
            Parameters parameters = getParameters();
            if (parameters != null)
            {
                parameters.setAllIn(this.peekedParameters);
            }
            this.parameterFactory.setValues(this.peekedParameters, gtuType);
        }
        catch (ParameterException exception)
        {
            throw new GtuException("Parameter was set to illegal value.", exception);
        }
        return this.peekedParameters;
    }

    /**
     * Returns parameters specific to the strategical planner. The input already contains parameters from the tactical planner.
     * After this method, the {@code ParameterFactory} sets or overwrites additional parameters. Hence, this method may set
     * default (distributed) values for parameters specific to the strategical planner.
     * @return parameters Parameters; parameters for the strategical planner, may be {@code null}
     */
    protected abstract Parameters getParameters();

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

    /**
     * Returns the next tactical planner.
     * @param gtu LaneBasedGtu; GTU to be generated
     * @return T; next tactical planner
     * @throws GtuException on exception during tactical planner creation
     */
    protected final LaneBasedTacticalPlanner nextTacticalPlanner(final LaneBasedGTU gtu) throws GtuException
    {
        return this.tacticalPlannerFactory.create(gtu);
    }

    /**
     * Returns the tactical planner factory.
     * @return LaneBasedTacticalPlannerFactory; tactical planner factory
     */
    protected final LaneBasedTacticalPlannerFactory<? extends LaneBasedTacticalPlanner> getTacticalPlannerFactory()
    {
        return this.tacticalPlannerFactory;
    }

}
