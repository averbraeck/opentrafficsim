package org.opentrafficsim.road.gtu.lane.perception.headway;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 5 apr. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class HeadwayGTUPerceived extends HeadwayGTURealCopy
{

    /** */
    private static final long serialVersionUID = 20180405L;

    /**
     * @param gtu LaneBasedGTU; gtu
     * @param distance Length; distance
     * @param speed Speed; speed
     * @param acceleration Acceleration; acceleration
     * @throws GTUException
     */
    public HeadwayGTUPerceived(final LaneBasedGTU gtu, final Length distance, final Speed speed,
            final Acceleration acceleration) throws GTUException
    {
        super(gtu.getId(), gtu.getGTUType(), distance, gtu.getLength(), speed, acceleration,
                gtu.getTacticalPlanner().getCarFollowingModel(), new ParameterSet(gtu.getParameters()), getSpeedLimitInfo(gtu),
                gtu.getStrategicalPlanner().getRoute(), gtu.getDesiredSpeed(),
                getGTUStatuses(gtu, gtu.getSimulator().getSimulatorTime().getTime()));
    }

    /**
     * @param gtu LaneBasedGTU; gtu
     * @param overlapFront
     * @param overlap
     * @param overlapRear
     * @param speed Speed; speed
     * @param acceleration Acceleration; acceleration
     * @throws GTUException
     */
    public HeadwayGTUPerceived(final LaneBasedGTU gtu, Length overlapFront, Length overlap, Length overlapRear, Speed speed,
            Acceleration acceleration) throws GTUException
    {
        super(gtu.getId(), gtu.getGTUType(), overlapFront, overlap, overlapRear, gtu.getLength(), speed, acceleration,
                gtu.getTacticalPlanner().getCarFollowingModel(), new ParameterSet(gtu.getParameters()), getSpeedLimitInfo(gtu),
                gtu.getStrategicalPlanner().getRoute(), gtu.getDesiredSpeed(),
                getGTUStatuses(gtu, gtu.getSimulator().getSimulatorTime().getTime()));
    }

}
