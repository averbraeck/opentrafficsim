package org.opentrafficsim.road.gtu.lane.tactical;

import java.io.Serializable;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModelFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;

/**
 * Factory to create {@code LaneBasedGTUFollowingChange0TacticalPlanner}.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 2, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class LaneBasedGTUFollowingDirectedChangeTacticalPlannerFactory
        implements LaneBasedTacticalPlannerFactory<LaneBasedGTUFollowingDirectedChangeTacticalPlanner>, Serializable
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** The car following model. */
    private GTUFollowingModelOld carFollowingModel;

    /** Factory for car-following model. */
    private CarFollowingModelFactory<? extends GTUFollowingModelOld> carFollowingModelFactory;

    /**
     * Constructor with car-following model factory.
     * @param carFollowingModelFactory CarFollowingModelFactory&lt;? extends GTUFollowingModelOld&gt;; car following model
     *            factory
     */
    public LaneBasedGTUFollowingDirectedChangeTacticalPlannerFactory(
            final CarFollowingModelFactory<? extends GTUFollowingModelOld> carFollowingModelFactory)
    {
        this.carFollowingModel = null;
        this.carFollowingModelFactory = carFollowingModelFactory;
    }

    /**
     * Constructor with fixed stateless car-following and lane change model.
     * @param carFollowingModel GTUFollowingModelOld; car following model
     */
    public LaneBasedGTUFollowingDirectedChangeTacticalPlannerFactory(final GTUFollowingModelOld carFollowingModel)
    {
        this.carFollowingModel = carFollowingModel;
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedGTUFollowingDirectedChangeTacticalPlanner create(final LaneBasedGTU gtu) throws GTUException
    {
        if (this.carFollowingModel != null)
        {
            return new LaneBasedGTUFollowingDirectedChangeTacticalPlanner(this.carFollowingModel, gtu);
        }
        return new LaneBasedGTUFollowingDirectedChangeTacticalPlanner(this.carFollowingModelFactory.generateCarFollowingModel(),
                gtu);
    }

    /** {@inheritDoc} */
    @Override
    public final Parameters getParameters()
    {
        Parameters params = new ParameterSet().setDefaultParameters(ParameterTypes.class);
        try
        {
            params.setParameter(ParameterTypes.LOOKAHEAD, new Length(250, LengthUnit.SI));
        }
        catch (ParameterException pe)
        {
            throw new RuntimeException("Parameter type 'LOOKAHEAD' could not be set.", pe);
        }
        return params;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneBasedGTUFollowingChange0TacticalPlannerFactory [car-following=" + this.carFollowingModel + "]";
    }

}
