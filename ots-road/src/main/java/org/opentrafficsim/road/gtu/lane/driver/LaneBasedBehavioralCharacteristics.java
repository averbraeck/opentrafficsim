package org.opentrafficsim.road.gtu.lane.driver;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.DrivingCharacteristics;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.LaneChangeModel;

/**
 * Driving characteristics of the driver. Sets the parameters for the car following model and the lane change model (e.g., gap
 * acceptance).
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 26, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBasedBehavioralCharacteristics implements DrivingCharacteristics
{
    /** */
    private static final long serialVersionUID = 20151126L;

    /** the GTUFollowing model to use for this driver. */
    private final GTUFollowingModelOld gtuFollowingModel;

    /** laneChangeModel the lane change model to use for this driver. */
    private final LaneChangeModel laneChangeModel;

    /** Standard incentive to stay in the current lane. */
    private static final Acceleration STAYINCURRENTLANEINCENTIVE = new Acceleration(0.1,
        AccelerationUnit.METER_PER_SECOND_2);

    /** Standard incentive to stay in the current lane. */
    private static final Acceleration PREFERREDLANEINCENTIVE = new Acceleration(0.3,
        AccelerationUnit.METER_PER_SECOND_2);

    /** Standard incentive to stay in the current lane. */
    private static final Acceleration NONPREFERREDLANEINCENTIVE = new Acceleration(-0.3,
        AccelerationUnit.METER_PER_SECOND_2);

    /** Standard time horizon for route choices. */
    private static final Time.Rel TIMEHORIZON = new Time.Rel(90, TimeUnit.SECOND);

    /** how far does the driver of this GTU look ahead when perceiving the environment. */
    private Length.Rel forwardHeadwayDistance = new Length.Rel(250.0, LengthUnit.METER);

    /** how far does the driver of this GTU look back when perceiving the environment, stored as a negative number. */
    private Length.Rel backwardHeadwayDistance = new Length.Rel(-100.0, LengthUnit.METER);

    /** update frequency. */
    private Frequency averagePerceptionUpdateFrequency = new Frequency(0.5, FrequencyUnit.PER_SECOND);

    /** {@inheritDoc} */
    @Override
    public Frequency getAveragePerceptionUpdateFrequency()
    {
        return this.averagePerceptionUpdateFrequency;
    }

    /**
     * @param averagePerceptionUpdateFrequency set averagePerceptionUpdateFrequency
     */
    public final void setAveragePerceptionUpdateFrequency(final Frequency averagePerceptionUpdateFrequency)
    {
        this.averagePerceptionUpdateFrequency = averagePerceptionUpdateFrequency;
    }

    /**
     * @param gtuFollowingModel the GTUFollowing model to use for this driver
     * @param laneChangeModel the lane change model to use for this driver
     */
    public LaneBasedBehavioralCharacteristics(GTUFollowingModelOld gtuFollowingModel, LaneChangeModel laneChangeModel)
    {
        super();
        this.gtuFollowingModel = gtuFollowingModel;
        this.laneChangeModel = laneChangeModel;
    }

    /**
     * @return gtuFollowingModel
     */
    public final GTUFollowingModelOld getGTUFollowingModel()
    {
        return this.gtuFollowingModel;
    }

    /**
     * @return laneChangeModel
     */
    public final LaneChangeModel getLaneChangeModel()
    {
        return this.laneChangeModel;
    }

    /**
     * @return forwardHeadwayDistance
     */
    public final Length.Rel getForwardHeadwayDistance()
    {
        return this.forwardHeadwayDistance;
    }

    /**
     * @param forwardHeadwayDistance set forwardHeadwayDistance
     */
    public final void setForwardHeadwayDistance(final Length.Rel forwardHeadwayDistance)
    {
        this.forwardHeadwayDistance = forwardHeadwayDistance;
    }

    /**
     * @return backwardHeadwayDistance, as a negative number
     */
    public final Length.Rel getBackwardHeadwayDistance()
    {
        return this.backwardHeadwayDistance;
    }

    /**
     * @param backwardHeadwayDistance set backwardHeadwayDistance
     */
    public final void setBackwardHeadwayDistance(final Length.Rel backwardHeadwayDistance)
    {
        this.backwardHeadwayDistance = backwardHeadwayDistance;
    }

}
