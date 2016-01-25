package org.opentrafficsim.road.gtu.lane.perception;

import java.util.HashSet;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.perception.Perception;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * The perception module of a GTU based on lanes. It is responsible for perceiving (sensing) the environment of the GTU, which
 * includes the locations of other GTUs. Perception is done at a certain time, and the perceived information might have a
 * limited validity. In that sense, Perception is stateful. Information can be requested as often as needed, but will only be
 * recalculated when asked explicitly. This "Full" version of LanePerception will perceive all information at once when
 * perceive() is called. <br>
 * Perception for lane-based GTUs involves information about GTUs in front of the owner GTU on the same lane (the 'leader' GTU),
 * parallel vehicles (important if we want to change lanes), distance to other vehicles on parallel lanes, as well in front as
 * to the back (important if we want to change lanes), and information about obstacles, traffic lights, speed signs, and ending
 * lanes.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 15, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LanePerceptionFull extends AbstractLanePerception implements Perception
{
    /** */
    private static final long serialVersionUID = 20151128L;

    /**
     * Create a new LanePerceptionFull module. Because the constructor is often called inside the constructor of a GTU, this
     * constructor does not ask for the pointer to the GTU, as it is often impossible to provide at the time of construction.
     * Use the setter of the GTU instead.
     */
    public LanePerceptionFull()
    {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public void perceive() throws GTUException, NetworkException
    {
        updateTimestamp();
        updateSpeedLimit();
        // TODO updateLanePlan(); includes objects that can force us to stop, such as traffic lights
        updateForwardHeadwayGTU();
        updateBackwardHeadwayGTU();
        updateAccessibleAdjacentLanes();
        updateParallelGTUs();
        updateLeftLaneTraffic();
        updateRightLaneTraffic();
    }

}
