package org.opentrafficsim.core.network;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.djunits.unit.FrequencyUnit;
import org.djunits.value.vdouble.scalar.Frequency;
import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSDEVSSimulator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUType;

import mockit.MockUp;

/**
 * Test the CapacityOTSLink class.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
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
        Network network = new OTSNetwork("testNetworkForCapacityOTSLink");
        Node fromNode = new OTSNode(network, "startNode", fromPoint);
        Node toNode = new OTSNode(network, "endNode", toPoint);
        LinkType linkType = LinkType.ALL;
        OTSLine3D designLine = new OTSLine3D(fromPoint, toPoint);
        OTSSimulatorInterface simulator = new MockUp<OTSSimulatorInterface>()
        {
            // no implementation needed.
        }.getMockInstance();
        Frequency initialCapacity = new Frequency(1234, FrequencyUnit.PER_HOUR);
        Frequency finalCapacity = new Frequency(1234, FrequencyUnit.PER_HOUR);
        Map<GTUType, LongitudinalDirectionality> directionalityMap = new HashMap<>();
        directionalityMap.put(GTUType.ALL, LongitudinalDirectionality.DIR_PLUS);
        CapacityOTSLink link = new CapacityOTSLink(network, "link", fromNode, toNode, linkType, designLine, simulator,
                initialCapacity, directionalityMap);
        assertTrue("from point matches", fromPoint.equals(link.getDesignLine().get(0)));
        assertTrue("to point matches", toPoint.equals(link.getDesignLine().get(1)));
        assertTrue("from node matches", fromNode.equals(link.getStartNode()));
        assertTrue("to node matches", toNode.equals(link.getEndNode()));
        assertTrue("capacity mathes", initialCapacity.equals(link.getCapacity()));
        link.setCapacity(finalCapacity);
        assertTrue("capacity mathes", finalCapacity.equals(link.getCapacity()));

        link = new CapacityOTSLink(network, "link2", fromNode, toNode, linkType, designLine, simulator, initialCapacity,
                LongitudinalDirectionality.DIR_PLUS);
        assertTrue("from point matches", fromPoint.equals(link.getDesignLine().get(0)));
        assertTrue("to point matches", toPoint.equals(link.getDesignLine().get(1)));
        assertTrue("from node matches", fromNode.equals(link.getStartNode()));
        assertTrue("to node matches", toNode.equals(link.getEndNode()));
        assertTrue("capacity mathes", initialCapacity.equals(link.getCapacity()));
        link.setCapacity(finalCapacity);
        assertTrue("capacity mathes", finalCapacity.equals(link.getCapacity()));

        Network newNetwork = new OTSNetwork("clonedNetworkForCapacityOTSLink");
        // Create nodes with matching IDs in the new network
        new OTSNode(newNetwork, fromNode.getId(), fromPoint);
        new OTSNode(newNetwork, toNode.getId(), toPoint);
        OTSSimulatorInterface newSimulator = new OTSDEVSSimulator();
        CapacityOTSLink clonedLink = new CapacityOTSLink(newNetwork, newSimulator, true, link);
        assertTrue("from point matches", fromPoint.equals(clonedLink.getDesignLine().get(0)));
        assertTrue("to point matches", toPoint.equals(clonedLink.getDesignLine().get(1)));
        // XXXX is it really intentional that the equals method of Node does NOT check equality of the network field?
        assertTrue("from node matches", fromNode.equals(clonedLink.getStartNode()));
        assertTrue("to node matches", toNode.equals(clonedLink.getEndNode()));
        assertTrue("capacity mathes", finalCapacity.equals(clonedLink.getCapacity()));
        clonedLink.setCapacity(initialCapacity);
        assertTrue("capacity mathes", initialCapacity.equals(clonedLink.getCapacity()));
        newNetwork.removeLink(clonedLink);
        clonedLink = link.clone(newNetwork, newSimulator, true);
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
