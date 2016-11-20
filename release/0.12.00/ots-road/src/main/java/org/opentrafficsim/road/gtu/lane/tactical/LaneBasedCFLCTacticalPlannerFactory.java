package org.opentrafficsim.road.gtu.lane.tactical;

import java.io.Serializable;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.AbstractLaneChangeModel;

/**
 * Factory to create {@code LaneBasedCFLCTacticalPlanner}.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 2, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class LaneBasedCFLCTacticalPlannerFactory implements LaneBasedTacticalPlannerFactory<LaneBasedCFLCTacticalPlanner>, Serializable
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** The car following model. */
    private GTUFollowingModelOld carFollowingModel;

    /** The lane change model. */
    private AbstractLaneChangeModel laneChangeModel;

    /**
     * Constructor with fixed stateless car-following and lane change model.
     * @param carFollowingModel car following model
     * @param laneChangeModel lane change model
     */
    public LaneBasedCFLCTacticalPlannerFactory(final GTUFollowingModelOld carFollowingModel,
        final AbstractLaneChangeModel laneChangeModel)
    {
        this.carFollowingModel = carFollowingModel;
        this.laneChangeModel = laneChangeModel;
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedCFLCTacticalPlanner create(final LaneBasedGTU gtu) throws GTUException
    {
        return new LaneBasedCFLCTacticalPlanner(this.carFollowingModel, this.laneChangeModel, gtu);
    }

    /** {@inheritDoc} */
    @Override
    public final BehavioralCharacteristics getDefaultBehavioralCharacteristics()
    {
        BehavioralCharacteristics bc = new BehavioralCharacteristics().setDefaultParameters(ParameterTypes.class);
        try
        {
            bc.setParameter(ParameterTypes.LOOKAHEAD, new Length(250, LengthUnit.SI));
        }
        catch (ParameterException pe)
        {
            throw new RuntimeException("Parameter type 'LOOKAHEAD' could not be set.", pe);
        }
        return bc;
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        return "LaneBasedCFLCTacticalPlannerFactory [car-following=" + this.carFollowingModel + ", lane changing="
            + this.laneChangeModel + "]";
    }

}
