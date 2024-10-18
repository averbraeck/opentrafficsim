package org.opentrafficsim.core.network;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.djunits.unit.FrequencyUnit;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djutils.draw.point.Point2d;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.geometry.OtsLine2d;
import org.opentrafficsim.core.mock.MockSimulator;

/**
 * Test the CapacityLinkTest class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class CapacityLinkTest
{
    /**
     * Test the constructor and all getters.
     * @throws NetworkException this test has failed if this exception occurs uncaught
     */
    @Test
    public final void constructorTest() throws NetworkException
    {
        Point2d fromPoint = new Point2d(100, 200);
        Point2d toPoint = new Point2d(1000, 2000);
        Network network = new Network("testNetworkForCapacityOTSLink", MockSimulator.createMock());
        Node fromNode = new Node(network, "startNode", fromPoint);
        Node toNode = new Node(network, "endNode", toPoint);
        LinkType linkType = DefaultsNl.ROAD;
        OtsLine2d designLine = new OtsLine2d(fromPoint, toPoint);
        Frequency initialCapacity = new Frequency(1234, FrequencyUnit.PER_HOUR);
        Frequency finalCapacity = new Frequency(1234, FrequencyUnit.PER_HOUR);
        CapacityLink link = new CapacityLink(network, "link", fromNode, toNode, linkType, designLine, null, initialCapacity);
        assertTrue(fromPoint.equals(link.getDesignLine().get(0)), "from point matches");
        assertTrue(toPoint.equals(link.getDesignLine().get(1)), "to point matches");
        assertTrue(fromNode.equals(link.getStartNode()), "from node matches");
        assertTrue(toNode.equals(link.getEndNode()), "to node matches");
        assertTrue(initialCapacity.equals(link.getCapacity()), "capacity mathes");
        link.setCapacity(finalCapacity);
        assertTrue(finalCapacity.equals(link.getCapacity()), "capacity mathes");
    }
}
