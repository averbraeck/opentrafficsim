package org.opentrafficsim.road.gtu.strategical;

import org.junit.Test;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine3D;
import org.opentrafficsim.core.geometry.OtsPoint3D;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OtsLink;
import org.opentrafficsim.core.network.OtsNode;
import org.opentrafficsim.road.DefaultTestParameters;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedCfLcTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlus;
import org.opentrafficsim.road.mock.MockSimulator;
import org.opentrafficsim.road.network.OtsRoadNetwork;

/**
 * Test the LaneBasedStrategicalRoutePlanner class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class LaneBasedStrategicalRoutePlannerTest
{

    /**
     * Test the nextLinkDirection method.
     * @throws GtuException if not caught this test has failed
     * @throws NetworkException if not caught this test has failed
     * @throws OtsGeometryException when construction of design line fails
     */
    @Test
    public final void nextLinkDirectionTest() throws GtuException, NetworkException, OtsGeometryException
    {
        OtsSimulatorInterface simulator = MockSimulator.createMock();
        OtsRoadNetwork network = new OtsRoadNetwork("next link direction test", simulator);
        // Build a really simple network
        OtsNode fromNode = new OtsNode(network, "from", new OtsPoint3D(0, 0, 0));
        OtsNode toNode = new OtsNode(network, "to", new OtsPoint3D(100, 0, 0));
        OtsLine3D designLine = new OtsLine3D(fromNode.getPoint(), toNode.getPoint());
        OtsLink link = new OtsLink(network, "link", fromNode, toNode, DefaultsNl.ROAD, designLine);
        CarFollowingModel cfm = new IdmPlus();
        LaneBasedCfLcTacticalPlanner tacticalPlanner = new LaneBasedCfLcTacticalPlanner(null, null, null);
        Parameters params = DefaultTestParameters.create();
        // TODO Gtu cannot be null anymore...
        // LaneBasedStrategicalRoutePlanner lbsrp = new LaneBasedStrategicalRoutePlanner(params, tacticalPlanner, null);

    }
}
