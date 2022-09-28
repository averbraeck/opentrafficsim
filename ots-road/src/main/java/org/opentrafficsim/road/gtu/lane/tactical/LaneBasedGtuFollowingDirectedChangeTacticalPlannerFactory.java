package org.opentrafficsim.road.gtu.lane.tactical;

import java.io.Serializable;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModelFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.GtuFollowingModelOld;

/**
 * Factory to create {@code LaneBasedGtuFollowingChange0TacticalPlanner}.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */

public class LaneBasedGtuFollowingDirectedChangeTacticalPlannerFactory
        implements LaneBasedTacticalPlannerFactory<LaneBasedGtuFollowingDirectedChangeTacticalPlanner>, Serializable
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** The car following model. */
    private GtuFollowingModelOld carFollowingModel;

    /** Factory for car-following model. */
    private CarFollowingModelFactory<? extends GtuFollowingModelOld> carFollowingModelFactory;

    /**
     * Constructor with car-following model factory.
     * @param carFollowingModelFactory CarFollowingModelFactory&lt;? extends GtuFollowingModelOld&gt;; car following model
     *            factory
     */
    public LaneBasedGtuFollowingDirectedChangeTacticalPlannerFactory(
            final CarFollowingModelFactory<? extends GtuFollowingModelOld> carFollowingModelFactory)
    {
        this.carFollowingModel = null;
        this.carFollowingModelFactory = carFollowingModelFactory;
    }

    /**
     * Constructor with fixed stateless car-following and lane change model.
     * @param carFollowingModel GtuFollowingModelOld; car following model
     */
    public LaneBasedGtuFollowingDirectedChangeTacticalPlannerFactory(final GtuFollowingModelOld carFollowingModel)
    {
        this.carFollowingModel = carFollowingModel;
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedGtuFollowingDirectedChangeTacticalPlanner create(final LaneBasedGtu gtu) throws GtuException
    {
        if (this.carFollowingModel != null)
        {
            return new LaneBasedGtuFollowingDirectedChangeTacticalPlanner(this.carFollowingModel, gtu);
        }
        return new LaneBasedGtuFollowingDirectedChangeTacticalPlanner(this.carFollowingModelFactory.generateCarFollowingModel(),
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
        return "LaneBasedGtuFollowingChange0TacticalPlannerFactory [car-following=" + this.carFollowingModel + "]";
    }

}
