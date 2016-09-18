package org.opentrafficsim.road.gtu.strategical.route;

import java.io.Serializable;

import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.network.route.RouteGenerator;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;

/**
 * Factory for creating {@code LaneBasedStrategicalRoutePlanner} using any {@code LaneBasedTacticalPlannerFactory}.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 2, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class LaneBasedStrategicalRoutePlannerFactory implements
    LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner>, Serializable
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Route generator for the next strategical planner. */
    private final RouteGenerator routeGenerator;

    /** Behavioral characteristics for the next strategical planner. */
    private BehavioralCharacteristics behavioralCharacteristics;

    /** Factory for tactical planners. */
    private final LaneBasedTacticalPlannerFactory<? extends LaneBasedTacticalPlanner> tacticalPlannerFactory;

    /**
     * Constructor with factory for tactical planners.
     * @param tacticalPlannerFactory factory for tactical planners
     */
    public LaneBasedStrategicalRoutePlannerFactory(
        final LaneBasedTacticalPlannerFactory<? extends LaneBasedTacticalPlanner> tacticalPlannerFactory)
    {
        this.tacticalPlannerFactory = tacticalPlannerFactory;
        this.routeGenerator = null;
    }

    /**
     * Constructor with factory for tactical planners and route generator.
     * @param tacticalPlannerFactory factory for tactical planners
     * @param routeGenerator route generator
     */
    public LaneBasedStrategicalRoutePlannerFactory(
        final LaneBasedTacticalPlannerFactory<? extends LaneBasedTacticalPlanner> tacticalPlannerFactory,
        final RouteGenerator routeGenerator)
    {
        this.tacticalPlannerFactory = tacticalPlannerFactory;
        this.routeGenerator = routeGenerator;
    }

    /** {@inheritDoc} */
    @Override
    public final BehavioralCharacteristics getDefaultBehavioralCharacteristics()
    {
        return this.tacticalPlannerFactory.getDefaultBehavioralCharacteristics();
    }

    /** {@inheritDoc} */
    @Override
    public final void setBehavioralCharacteristics(final BehavioralCharacteristics behavioralCharacteristics)
    {
        this.behavioralCharacteristics = behavioralCharacteristics;
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedStrategicalPlanner create(final LaneBasedGTU gtu) throws GTUException
    {
        if (this.behavioralCharacteristics == null)
        {
            this.behavioralCharacteristics = getDefaultBehavioralCharacteristics();
        }
        Route route = null;
        if (this.routeGenerator != null)
        {
            try
            {
                route = this.routeGenerator.draw();
            }
            catch (ProbabilityException exception)
            {
                throw new GTUException(exception);
            }
        }
        LaneBasedStrategicalRoutePlanner strategicalPlanner =
            new LaneBasedStrategicalRoutePlanner(this.behavioralCharacteristics, this.tacticalPlannerFactory.create(gtu),
                route, gtu);
        
        this.behavioralCharacteristics = null;
        return strategicalPlanner;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneBasedStrategicalRoutePlannerFactory [tacticalPlannerFactory=" + this.tacticalPlannerFactory + "]";
    }

}
