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
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.LaneChangeModel;

/**
 * Factory to create {@code LaneBasedCFLCTacticalPlanner}.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */

public class LaneBasedCfLcTacticalPlannerFactory
        implements LaneBasedTacticalPlannerFactory<LaneBasedCfLcTacticalPlanner>, Serializable
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** The car following model. */
    private GtuFollowingModelOld carFollowingModel;

    /** The lane change model. */
    private LaneChangeModel laneChangeModel;

    /**
     * Constructor with fixed stateless car-following and lane change model.
     * @param carFollowingModel car following model
     * @param laneChangeModel lane change model
     */
    public LaneBasedCfLcTacticalPlannerFactory(final GtuFollowingModelOld carFollowingModel,
            final LaneChangeModel laneChangeModel)
    {
        this.carFollowingModel = carFollowingModel;
        this.laneChangeModel = laneChangeModel;
    }

    @Override
    public final LaneBasedCfLcTacticalPlanner create(final LaneBasedGtu gtu) throws GtuException
    {
        return new LaneBasedCfLcTacticalPlanner(this.carFollowingModel, this.laneChangeModel, gtu);
    }

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

    @Override
    public final String toString()
    {
        return "LaneBasedCFLCTacticalPlannerFactory [car-following=" + this.carFollowingModel + ", lane changing="
                + this.laneChangeModel + "]";
    }

}
