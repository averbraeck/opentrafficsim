package org.opentrafficsim.core.network;

import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.unit.FrequencyUnit;
import org.djunits.value.vdouble.scalar.Frequency;
import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.mock.MockSimulator;

/**
 * Test the CapacityOTSLink class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jan 2, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
        Map<GTUType, LongitudinalDirectionality> directionalityMap = new LinkedHashMap<>();
        directionalityMap.put(network.getGtuType(GTUType.DEFAULTS.VEHICLE), LongitudinalDirectionality.DIR_PLUS);
        CapacityOTSLink link = new CapacityOTSLink(network, "link", fromNode, toNode, linkType, designLine, initialCapacity);
        assertTrue("from point matches", fromPoint.equals(link.getDesignLine().get(0)));
        assertTrue("to point matches", toPoint.equals(link.getDesignLine().get(1)));
        assertTrue("from node matches", fromNode.equals(link.getStartNode()));
        assertTrue("to node matches", toNode.equals(link.getEndNode()));
        assertTrue("capacity mathes", initialCapacity.equals(link.getCapacity()));
        link.setCapacity(finalCapacity);
        assertTrue("capacity mathes", finalCapacity.equals(link.getCapacity()));

        OTSNetwork newNetwork = new OTSNetwork("clonedNetworkForCapacityOTSLink", true, MockSimulator.createMock());
        // Create nodes with matching IDs in the new network
        new OTSNode(newNetwork, fromNode.getId(), fromPoint);
        new OTSNode(newNetwork, toNode.getId(), toPoint);
        OTSSimulatorInterface newSimulator = MockSimulator.createMock();
        CapacityOTSLink clonedLink = new CapacityOTSLink(newNetwork, link);
        assertTrue("from point matches", fromPoint.equals(clonedLink.getDesignLine().get(0)));
        assertTrue("to point matches", toPoint.equals(clonedLink.getDesignLine().get(1)));
        // XXXX is it really intentional that the equals method of Node does NOT check equality of the network field?
        assertTrue("from node matches", fromNode.equals(clonedLink.getStartNode()));
        assertTrue("to node matches", toNode.equals(clonedLink.getEndNode()));
        assertTrue("capacity mathes", finalCapacity.equals(clonedLink.getCapacity()));
        clonedLink.setCapacity(initialCapacity);
        assertTrue("capacity mathes", initialCapacity.equals(clonedLink.getCapacity()));
        newNetwork.removeLink(clonedLink);
        clonedLink = link.clone(newNetwork);
        assertTrue("from point matches", fromPoint.equals(clonedLink.getDesignLine().get(0)));
        assertTrue("to point matches", toPoint.equals(clonedLink.getDesignLine().get(1)));
        // XXXX is it really intentional that the equals method of Node does NOT check equality of the network field?
        assertTrue("from node matches", fromNode.equals(clonedLink.getStartNode()));
        assertTrue("to node matches", toNode.equals(clonedLink.getEndNode()));
        assertTrue("capacity mathes", finalCapacity.equals(clonedLink.getCapacity()));
        clonedLink.setCapacity(initialCapacity);
        assertTrue("capacity mathes", initialCapacity.equals(clonedLink.getCapacity()));
        assertTrue("toString method returns something with the class name in it", link.toString().contains("CapacityOTSLink"));
    }
}
