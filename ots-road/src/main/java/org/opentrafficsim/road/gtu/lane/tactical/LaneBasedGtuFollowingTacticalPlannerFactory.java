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
import org.opentrafficsim.road.gtu.lane.tactical.following.GtuFollowingModelOld;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */

public class LaneBasedGtuFollowingTacticalPlannerFactory
        implements LaneBasedTacticalPlannerFactory<LaneBasedGtuFollowingTacticalPlanner>, Serializable
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** The car following model. */
    private GtuFollowingModelOld carFollowingModel;

    /**
     * Constructor with fixed stateless car-following and lane change model.
     * @param carFollowingModel GtuFollowingModelOld; car following model
     */
    public LaneBasedGtuFollowingTacticalPlannerFactory(final GtuFollowingModelOld carFollowingModel)
    {
        this.carFollowingModel = carFollowingModel;
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
    public final LaneBasedGtuFollowingTacticalPlanner create(final LaneBasedGtu gtu) throws GtuException
    {
        return new LaneBasedGtuFollowingTacticalPlanner(this.carFollowingModel, gtu);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneBasedGtuFollowingTacticalPlanner [car-following=" + this.carFollowingModel + "]";
    }

}
