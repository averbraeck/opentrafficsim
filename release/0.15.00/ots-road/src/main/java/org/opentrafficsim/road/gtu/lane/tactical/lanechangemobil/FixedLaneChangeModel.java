package org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil;

import java.io.Serializable;
import java.util.Collection;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;

/**
 * Dummy lane change model with totally predictable results (used for testing).
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
 *          initial version 11 feb. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FixedLaneChangeModel implements LaneChangeModel, Serializable
{
    /** */
    private static final long serialVersionUID = 20150211L;

    /** Lane change that will always be returned by this FixedLaneChangeModel. */
    private final LateralDirectionality laneChange;

    /**
     * Construct a FixedLaneChangeModel.
     * @param laneChange LateralDirectionality; the lane change that (always) be returned by this FixedLaneChangeModel.
     */
    public FixedLaneChangeModel(final LateralDirectionality laneChange)
    {
        this.laneChange = laneChange;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:parameternumber")
    @Override
    public final LaneMovementStep computeLaneChangeAndAcceleration(final LaneBasedGTU gtu,
            final Collection<Headway> sameLaneTraffic, final Collection<Headway> rightLaneTraffic,
            final Collection<Headway> leftLaneTraffic, final Speed speedLimit, final Acceleration preferredLaneRouteIncentive,
            final Acceleration laneChangeThreshold, final Acceleration nonPreferredLaneRouteIncentive)
            throws GTUException, ParameterException
    {
        Length headway = gtu.getBehavioralCharacteristics().getParameter(ParameterTypes.LOOKAHEAD);
        GTUFollowingModelOld gtuFollowingModel =
                (GTUFollowingModelOld) ((AbstractLaneBasedTacticalPlanner) gtu.getTacticalPlanner()).getCarFollowingModel();
        if (null == this.laneChange)
        {
            return new LaneMovementStep(gtuFollowingModel.computeDualAccelerationStep(gtu, sameLaneTraffic, headway, speedLimit)
                    .getLeaderAccelerationStep(), null);
        }
        else if (LateralDirectionality.LEFT == this.laneChange)
        {
            return new LaneMovementStep(gtuFollowingModel.computeDualAccelerationStep(gtu, leftLaneTraffic, headway, speedLimit)
                    .getLeaderAccelerationStep(), this.laneChange);
        }
        else if (LateralDirectionality.RIGHT == this.laneChange)
        {
            return new LaneMovementStep(gtuFollowingModel
                    .computeDualAccelerationStep(gtu, rightLaneTraffic, headway, speedLimit).getLeaderAccelerationStep(),
                    this.laneChange);
        }
        throw new Error("Program Error - unhandled LateralDirectionality");
    }

    /** {@inheritDoc} */
    @Override
    public final String getName()
    {
        return "Fixed lane change model";
    }

    /** {@inheritDoc} */
    @Override
    public final String getLongName()
    {
        return "Fixed lane change model. This model returns a lane change decision that is independent of the actual "
                + "traffic. It is used mostly for testing.";
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "FixedLaneChangeModel [laneChange=" + this.laneChange + "]";
    }

}
