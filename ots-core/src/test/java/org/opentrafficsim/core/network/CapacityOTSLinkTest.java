package org.opentrafficsim.core.network;

import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.unit.FrequencyUnit;
import org.djunits.value.vdouble.scalar.Frequency;
import org.junit.Test;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.mock.MockSimulator;

/**
 * Test the CapacityOTSLink class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class CapacityOTSLinkTest
{
    /**
     * Test the constructor and all getters.
     * @throws NetworkException this test has failed if this exception occurs uncaught
     * @throws OTSGeometryException this test has failed if this exception occurs uncaught
     */
    @Test
    public final void constructorTest() throws NetworkException, OTSGeometryException
    {
        OTSPoint3D fromPoint = new OTSPoint3D(100, 200, 300);
        OTSPoint3D toPoint = new OTSPoint3D(1000, 2000, 330);
        OTSNetwork network = new OTSNetwork("testNetworkForCapacityOTSLink", true, MockSimulator.createMock());
        Node fromNode = new OTSNode(network, "startNode", fromPoint);
        Node toNode = new OTSNode(network, "endNode", toPoint);
        LinkType linkType = network.getLinkType(LinkType.DEFAULTS.ROAD);
        OTSLine3D designLine = new OTSLine3D(fromPoint, toPoint);
        Frequency initialCapacity = new Frequency(1234, FrequencyUnit.PER_HOUR);
        Frequency finalCapacity = new Frequency(1234, FrequencyUnit.PER_HOUR);
        CapacityOTSLink link = new CapacityOTSLink(network, "link", fromNode, toNode, linkType, designLine, initialCapacity);
        assertTrue("from point matches", fromPoint.equals(link.getDesignLine().get(0)));
        assertTrue("to point matches", toPoint.equals(link.getDesignLine().get(1)));
        assertTrue("from node matches", fromNode.equals(link.getStartNode()));
        assertTrue("to node matches", toNode.equals(link.getEndNode()));
        assertTrue("capacity mathes", initialCapacity.equals(link.getCapacity()));
        link.setCapacity(finalCapacity);
        assertTrue("capacity mathes", finalCapacity.equals(link.getCapacity()));
    }
}
