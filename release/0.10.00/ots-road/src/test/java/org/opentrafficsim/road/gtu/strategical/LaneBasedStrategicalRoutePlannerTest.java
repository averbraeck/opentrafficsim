package org.opentrafficsim.road.gtu.strategical;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.OTSLink;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.DefaultTestParameters;
import org.opentrafficsim.road.gtu.lane.perception.CategorialLanePerception;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingLaneChangeTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlus;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;

/**
 * Test the LaneBasedStrategicalRoutePlanner class.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 19, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBasedStrategicalRoutePlannerTest
{

    /**
     * Test the nextLinkDirection method.
     * @throws GTUException if not caught this test has failed
     */
    @Test
    public final void nextLinkDirectionTest() throws GTUException
    {
        GTUType gtuType = new GTUType("car");
        // Build a really simple network
        OTSNode fromNode = new OTSNode("from", new OTSPoint3D(0, 0, 0));
        OTSNode toNode = new OTSNode("to", new OTSPoint3D(100, 0, 0));
        Map<GTUType, LongitudinalDirectionality> directionalityMap = new HashMap<GTUType, LongitudinalDirectionality>();
        directionalityMap.put(gtuType, LongitudinalDirectionality.DIR_PLUS); // Start with the easy cases
        OTSLink link = new OTSLink("link", fromNode, toNode, LinkType.ALL, null, directionalityMap);
        CarFollowingModel cfm = new IDMPlus();
        LaneBasedGTUFollowingLaneChangeTacticalPlanner tacticalPlanner =
                new LaneBasedGTUFollowingLaneChangeTacticalPlanner(null, null);
        BehavioralCharacteristics bc = DefaultTestParameters.create();
        LaneBasedStrategicalRoutePlanner lbsrp = new LaneBasedStrategicalRoutePlanner(bc, tacticalPlanner, null);

    }
}
