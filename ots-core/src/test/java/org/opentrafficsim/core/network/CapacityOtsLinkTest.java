package org.opentrafficsim.core.network;

import static org.junit.Assert.assertTrue;

import org.djunits.unit.FrequencyUnit;
import org.djunits.value.vdouble.scalar.Frequency;
import org.junit.Test;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine3d;
import org.opentrafficsim.core.geometry.OtsPoint3d;
import org.opentrafficsim.core.mock.MockSimulator;

/**
 * Test the CapacityOTSLink class.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class CapacityOtsLinkTest
{
    /**
     * Test the constructor and all getters.
     * @throws NetworkException this test has failed if this exception occurs uncaught
     * @throws OtsGeometryException this test has failed if this exception occurs uncaught
     */
    @Test
    public final void constructorTest() throws NetworkException, OtsGeometryException
    {
        OtsPoint3d fromPoint = new OtsPoint3d(100, 200, 300);
        OtsPoint3d toPoint = new OtsPoint3d(1000, 2000, 330);
        OtsNetwork network = new OtsNetwork("testNetworkForCapacityOTSLink", MockSimulator.createMock());
        Node fromNode = new Node(network, "startNode", fromPoint);
        Node toNode = new Node(network, "endNode", toPoint);
        LinkType linkType = DefaultsNl.ROAD;
        OtsLine3d designLine = new OtsLine3d(fromPoint, toPoint);
        Frequency initialCapacity = new Frequency(1234, FrequencyUnit.PER_HOUR);
        Frequency finalCapacity = new Frequency(1234, FrequencyUnit.PER_HOUR);
        CapacityOtsLink link = new CapacityOtsLink(network, "link", fromNode, toNode, linkType, designLine, initialCapacity);
        assertTrue("from point matches", fromPoint.equals(link.getDesignLine().get(0)));
        assertTrue("to point matches", toPoint.equals(link.getDesignLine().get(1)));
        assertTrue("from node matches", fromNode.equals(link.getStartNode()));
        assertTrue("to node matches", toNode.equals(link.getEndNode()));
        assertTrue("capacity mathes", initialCapacity.equals(link.getCapacity()));
        link.setCapacity(finalCapacity);
        assertTrue("capacity mathes", finalCapacity.equals(link.getCapacity()));
    }
}
