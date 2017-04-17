package org.opentrafficsim.road.gtu.strategical.route;

import java.io.Serializable;

import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristicsFactory;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristicsFactoryDefault;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.network.route.Route;
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

public class LaneBasedStrategicalRoutePlannerFactory
        implements LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner>, Serializable
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Factory for tactical planners. */
    private final LaneBasedTacticalPlannerFactory<? extends LaneBasedTacticalPlanner> tacticalPlannerFactory;

    /** Characteristics factory. */
    private final BehavioralCharacteristicsFactory behavioralCharacteristicsFactory;

    /**
     * Constructor with factory for tactical planners.
     * @param tacticalPlannerFactory factory for tactical planners
     */
    public LaneBasedStrategicalRoutePlannerFactory(
            final LaneBasedTacticalPlannerFactory<? extends LaneBasedTacticalPlanner> tacticalPlannerFactory)
    {
        this.tacticalPlannerFactory = tacticalPlannerFactory;
        this.behavioralCharacteristicsFactory = new BehavioralCharacteristicsFactoryDefault();
    }

    /**
     * Constructor with factory for tactical planners.
     * @param tacticalPlannerFactory factory for tactical planners
     * @param behavioralCharacteristicsFactory factory for behavioral characteristics
     */
    public LaneBasedStrategicalRoutePlannerFactory(
            final LaneBasedTacticalPlannerFactory<? extends LaneBasedTacticalPlanner> tacticalPlannerFactory,
            final BehavioralCharacteristicsFactory behavioralCharacteristicsFactory)
    {
        this.tacticalPlannerFactory = tacticalPlannerFactory;
        this.behavioralCharacteristicsFactory = behavioralCharacteristicsFactory;
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedStrategicalPlanner create(final LaneBasedGTU gtu, final Route route) throws GTUException
    {
        BehavioralCharacteristics behavioralCharacteristics = this.tacticalPlannerFactory.getDefaultBehavioralCharacteristics();
        try 
        {
            this.behavioralCharacteristicsFactory.setValues(behavioralCharacteristics, gtu.getGTUType());
        }
        catch (ParameterException exception)
        {
            throw new GTUException("Parameter was set to illegal value.", exception);
        }
        LaneBasedStrategicalRoutePlanner strategicalPlanner = new LaneBasedStrategicalRoutePlanner(behavioralCharacteristics,
                this.tacticalPlannerFactory.create(gtu), route, gtu);
        return strategicalPlanner;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneBasedStrategicalRoutePlannerFactory [tacticalPlannerFactory=" + this.tacticalPlannerFactory + "]";
    }

}
