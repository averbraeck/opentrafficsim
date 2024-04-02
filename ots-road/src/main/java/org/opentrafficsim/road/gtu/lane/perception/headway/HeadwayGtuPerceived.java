package org.opentrafficsim.road.gtu.lane.perception.headway;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class HeadwayGtuPerceived extends HeadwayGtuRealCopy
{

    /** */
    private static final long serialVersionUID = 20180405L;

    /**
     * @param gtu LaneBasedGtu; gtu
     * @param distance Length; distance
     * @param speed Speed; speed
     * @param acceleration Acceleration; acceleration
     * @throws GtuException ...
     */
    public HeadwayGtuPerceived(final LaneBasedGtu gtu, final Length distance, final Speed speed,
            final Acceleration acceleration) throws GtuException
    {
        super(gtu.getId(), gtu.getType(), distance, gtu.getLength(), gtu.getWidth(), speed, acceleration,
                gtu.getTacticalPlanner().getCarFollowingModel(), new ParameterSet(gtu.getParameters()), getSpeedLimitInfo(gtu),
                gtu.getStrategicalPlanner().getRoute(), gtu.getDesiredSpeed(),
                getGtuStatuses(gtu, gtu.getSimulator().getSimulatorAbsTime()));
    }

    /**
     * @param gtu LaneBasedGtu; gtu
     * @param overlapFront Length; the overlap over the front of the GTU
     * @param overlap Length; ???
     * @param overlapRear Length; the overlap over the rear of the GTU
     * @param speed Speed; speed
     * @param acceleration Acceleration; acceleration
     * @throws GtuException ...
     */
    public HeadwayGtuPerceived(final LaneBasedGtu gtu, final Length overlapFront, final Length overlap,
            final Length overlapRear, final Speed speed, final Acceleration acceleration) throws GtuException
    {
        super(gtu.getId(), gtu.getType(), overlapFront, overlap, overlapRear, gtu.getLength(), gtu.getWidth(), speed,
                acceleration, gtu.getTacticalPlanner().getCarFollowingModel(), new ParameterSet(gtu.getParameters()),
                getSpeedLimitInfo(gtu), gtu.getStrategicalPlanner().getRoute(), gtu.getDesiredSpeed(),
                getGtuStatuses(gtu, gtu.getSimulator().getSimulatorAbsTime()));
    }

}
