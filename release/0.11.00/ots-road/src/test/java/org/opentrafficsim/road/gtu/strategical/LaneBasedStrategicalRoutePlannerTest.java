package org.opentrafficsim.road.gtu.strategical;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSLink;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.DefaultTestParameters;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingLaneChangeTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlus;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;

/**
 * Test the LaneBasedStrategicalRoutePlanner class.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
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
     * @throws NetworkException  if not caught this test has failed
     * @throws OTSGeometryException when construction of design line fails
     */
    @Test
    public final void nextLinkDirectionTest() throws GTUException, NetworkException, OTSGeometryException
    {
        Network network = new OTSNetwork("next link direction test");
        GTUType gtuType = new GTUType("car");
        // Build a really simple network
        OTSNode fromNode = new OTSNode(network, "from", new OTSPoint3D(0, 0, 0));
        OTSNode toNode = new OTSNode(network, "to", new OTSPoint3D(100, 0, 0));
        Map<GTUType, LongitudinalDirectionality> directionalityMap = new HashMap<GTUType, LongitudinalDirectionality>();
        directionalityMap.put(gtuType, LongitudinalDirectionality.DIR_PLUS); // Start with the easy cases
        OTSLine3D designLine = new OTSLine3D(fromNode.getPoint(), toNode.getPoint());
        OTSLink link = new OTSLink(network, "link", fromNode, toNode, LinkType.ALL, designLine, directionalityMap);
        CarFollowingModel cfm = new IDMPlus();
        LaneBasedGTUFollowingLaneChangeTacticalPlanner tacticalPlanner =
                new LaneBasedGTUFollowingLaneChangeTacticalPlanner(null, null);
        BehavioralCharacteristics bc = DefaultTestParameters.create();
        LaneBasedStrategicalRoutePlanner lbsrp = new LaneBasedStrategicalRoutePlanner(bc, tacticalPlanner, null);

    }
}
