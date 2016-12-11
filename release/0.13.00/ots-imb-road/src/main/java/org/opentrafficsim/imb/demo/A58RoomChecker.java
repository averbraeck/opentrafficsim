package org.opentrafficsim.imb.demo;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTUCharacteristics;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 17 nov. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class A58RoomChecker implements RoomChecker
{

    /** {@inheritDoc} */
    @Override
    public Speed canPlace(Speed leaderSpeed, Length headway, LaneBasedGTUCharacteristics laneBasedGTUCharacteristics)
            throws NetworkException
    {
        Speed speed = Speed.min(leaderSpeed, laneBasedGTUCharacteristics.getMaximumSpeed());
        for (DirectedLanePosition dlp : laneBasedGTUCharacteristics.getInitialLongitudinalPositions())
        {
            if (dlp.getLane().getLaneType().isCompatible(laneBasedGTUCharacteristics.getGTUType()))
            {
                speed = Speed.min(speed, dlp.getLane().getSpeedLimit(laneBasedGTUCharacteristics.getGTUType()));
            }
        }
        // TODO non-hard coded, related to GTU to be generated
        if ((speed.le(leaderSpeed) || headway.divideBy(speed.minus(leaderSpeed)).si > 10.0) && headway.divideBy(speed).si > 1.0
                && headway.si > 3)
        {
            return speed;
        }
        return null;
    }

}
