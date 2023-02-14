package org.opentrafficsim.core.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine3d;
import org.opentrafficsim.core.geometry.OtsPoint3d;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.mock.MockSimulator;

/**
 * Test the OtsNode class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class OtsNodeTest
{

    /**
     * Test the basics of the OtsNode class.
     * @throws NetworkException if that happens uncaught; this test has failed
     * @throws OtsGeometryException if that happens unchaught; this test has failed
     */
    @Test
    public final void testOtsNode() throws NetworkException, OtsGeometryException
    {
        OtsNetwork network = new OtsNetwork("Node test network", MockSimulator.createMock());
        OtsPoint3d point1 = new OtsPoint3d(20, 40, 60);
        double heading = Math.toRadians(123);
        OtsNode node1 = new OtsNode(network, "node 1", point1, heading);
        assertEquals("network matches", network, node1.getNetwork());
        assertEquals("name matches", "node 1", node1.getId());
        assertEquals("point matches", point1, node1.getPoint());
        assertEquals("getLocation", new DirectedPoint(point1.x, point1.y, point1.z), node1.getLocation());
        assertTrue("name is in toString", node1.toString().contains(node1.getId()));
        assertEquals("heading matches", heading, node1.getHeading(), 0.00001);
        OtsPoint3d point2 = new OtsPoint3d(120, 240, 60);
        OtsNode node2 = new OtsNode(network, "node 2", point2);
        assertEquals("network matches", network, node2.getNetwork());
        assertEquals("name matches", "node 2", node2.getId());
        assertEquals("point matches", point2, node2.getPoint());
        assertTrue("Node 1 matches itself", node1.equals(node1));
        assertFalse("Node 1 does not match null", node1.equals(null));
        assertFalse("Node 1 does not match node 2", node1.equals(node2));
        assertTrue("Node 2 has heading NaN", Double.isNaN(node2.getHeading()));
        // Create another node with name node 1 in another network
        OtsSimulatorInterface simulator = MockSimulator.createMock();
        Network otherNetwork = new OtsNetwork("Node test network 2", simulator);
        OtsNode node3 = new OtsNode(otherNetwork, "node 1", point1);
        assertTrue("Node 1 does match node 3 in other network", node1.equals(node3));

        assertEquals("node 1 has no links", 0, node1.getLinks().size());

        // Create a couple of links
        Link link1 = new OtsLink(network, "link 1", node1, node2, DefaultsNl.ROAD,
                new OtsLine3d(node1.getPoint(), node2.getPoint()));
        assertEquals("node 1 has one link", 1, node1.getLinks().size());
        assertEquals("node 2 has one link", 1, node2.getLinks().size());
        assertEquals("link at node 1 is link1", link1, node1.getLinks().iterator().next());
        assertEquals("link at node 2 is link1", link1, node2.getLinks().iterator().next());
        OtsNode node4 = new OtsNode(network, "node 3", new OtsPoint3d(10, 10, 10));
        Link link2 = new OtsLink(network, "link 2", node1, node4, DefaultsNl.ROAD,
                new OtsLine3d(node1.getPoint(), node4.getPoint()));
        Link link3 = new OtsLink(network, "link 3", node4, node2, DefaultsNl.ROAD,
                new OtsLine3d(node4.getPoint(), node2.getPoint()));
        Link link4 = new OtsLink(network, "link 4", node2, node1, DefaultsNl.ROAD,
                new OtsLine3d(node2.getPoint(), node1.getPoint()));
        assertEquals("node 1 has three links", 3, node1.getLinks().size());
        assertEquals("node 2 has three links", 3, node2.getLinks().size());
        assertEquals("node 4 has two links", 2, node4.getLinks().size());
        assertTrue("node 1 has link 1", node1.getLinks().contains(link1));
        assertTrue("node 1 has link 2", node1.getLinks().contains(link2));
        assertTrue("node 1 has link 4", node1.getLinks().contains(link4));
        assertFalse("node 1 does not have link 3", node1.getLinks().contains(link3));
        Set<Link> nextLinks = node1.nextLinks(DefaultsNl.VEHICLE, link4);
        assertEquals("incoming over link 4, node 1 has two next links", 2, nextLinks.size());
        assertTrue("incoming over link 4, next links of node 1 contains link 1", nextLinks.contains(link1));
        assertTrue("incoming over link 4, next links of node 1 contains link 1", nextLinks.contains(link1));
        try
        {
            node1.nextLinks(DefaultsNl.VEHICLE, link3);
            fail("nextLinks from link that does not connect to the node should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        assertTrue("node 1 has direct connection to node 2", node1.isConnectedTo(DefaultsNl.VEHICLE, node2));
        Node node5 = new OtsNode(network, "node 5", new OtsPoint3d(1000, 0, 0));
        assertFalse("node 1 has no direct connection to node 5", node1.isConnectedTo(DefaultsNl.VEHICLE, node5));
        Link link5 = new OtsLink(network, "link 5", node5, node1, DefaultsNl.FREEWAY,
                new OtsLine3d(node1.getPoint(), node5.getPoint()));
        assertFalse("node 1 still has no direct connection to node 5", node1.isConnectedTo(DefaultsNl.VEHICLE, node5));
        assertTrue("node 5 does have a direct connection to node 1", node5.isConnectedTo(DefaultsNl.VEHICLE, node1));
        assertEquals("Connection from node 5 to node 1 is link5", link5, node5.getLinks().iterator().next());
        node5.removeLink(link5);
        assertFalse("node 5 no longer has direct connection to node 1", node5.isConnectedTo(DefaultsNl.VEHICLE, node1));
    }

    /**
     * Test the addConnection method and related functions.
     * @throws NetworkException if that happens uncaught; this test has failed
     * @throws OtsGeometryException if that happens uncaught; this test has failed
     */
    @Test
    public final void connectionTest() throws NetworkException, OtsGeometryException
    {
        OtsSimulatorInterface simulator = MockSimulator.createMock();
        OtsNetwork network = new OtsNetwork("connection test network", simulator);
        OtsNode node = new OtsNode(network, "main", new OtsPoint3d(10, 100, 10));
        int maxNeighbor = 10;
        for (int i = 0; i < maxNeighbor; i++)
        {
            Node neighborNode = new OtsNode(network, "neighbor node " + i, new OtsPoint3d(20 + 10 * i, 0, 10));
            new OtsLink(network, "link from neighbor node " + i, neighborNode, node, DefaultsNl.ROAD,
                    new OtsLine3d(neighborNode.getPoint(), node.getPoint()));
        }
        // Prove that we can go from any neighborNode to any OTHER neighborNode
        for (int fromIndex = 0; fromIndex < maxNeighbor; fromIndex++)
        {
            Link fromLink = network.getLink("link from neighbor node " + fromIndex);
            Set<Link> nextLinks = node.nextLinks(DefaultsNl.VEHICLE, fromLink);
            assertEquals("should be maxNeighbor - 1 nextLinks", maxNeighbor - 1, nextLinks.size());
            assertFalse("should not contain fromLink", nextLinks.contains(fromLink));
        }
        // Add an explicit connection for the link from neighbor 1 to neighbor 2
        node.addConnection(DefaultsNl.VEHICLE, network.getLink("link from neighbor node 1"),
                network.getLink("link from neighbor node 2"));
        for (int fromIndex = 0; fromIndex < maxNeighbor; fromIndex++)
        {
            Link fromLink = network.getLink("link from neighbor node " + fromIndex);
            Set<Link> nextLinks = node.nextLinks(DefaultsNl.VEHICLE, fromLink);
            if (1 == fromIndex)
            {
                assertEquals("should be 1", 1, nextLinks.size());
                assertEquals("should only contain link form neighbor 2", network.getLink("link from neighbor node 2"),
                        nextLinks.iterator().next());
            }
            else
            {
                assertEquals("should be 0", 0, nextLinks.size());
            }
        }
        Node n1 = network.getNode("neighbor node 1");
        Node n2 = network.getNode("neighbor node 2");
        Link unrelatedLink =
                new OtsLink(network, "unrelated link", n1, n2, DefaultsNl.ROAD, new OtsLine3d(n1.getPoint(), n2.getPoint()));
        try
        {
            node.addConnection(DefaultsNl.VEHICLE, unrelatedLink, network.getLink("link from neighbor node 1"));
            fail("attempt to connect from a link not connected to node should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            node.addConnection(DefaultsNl.VEHICLE, network.getLink("link from neighbor node 1"), unrelatedLink);
            fail("attempt to connect to a link not connected to node should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        GtuType unrelatedGtuType = new GtuType("junk", DefaultsNl.SHIP);
        try
        {
            node.nextLinks(unrelatedGtuType, network.getLink("link from neighbor node 1"));
            fail("nextLinks for unsupported GtuType should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        // Create a link that does not allow traffic TO the node
        Link oneWayFromNode = new OtsLink(network, "one way from node", node, n1, DefaultsNl.FREEWAY,
                new OtsLine3d(node.getPoint(), n1.getPoint()));
        Link oneWayToNode = new OtsLink(network, "one way towards node", n1, node, DefaultsNl.FREEWAY,
                new OtsLine3d(n1.getPoint(), node.getPoint()));
        try
        {
            node.addConnection(DefaultsNl.VEHICLE, oneWayFromNode, network.getLink("link from neighbor node 1"));
            fail("attempt to connect from a link that does not allow traffic TO the node should have thrown a "
                    + "NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            node.addConnection(DefaultsNl.VEHICLE, network.getLink("link from neighbor node 1"), oneWayToNode);
            fail("attempt to connect to a link that does not allow outbound traffic should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        Link noWay = new OtsLink(network, "no way traffic inbound link", n2, node, DefaultsNl.RAILWAY,
                new OtsLine3d(n2.getPoint(), node.getPoint()));
        try
        {
            node.addConnection(DefaultsNl.VEHICLE, network.getLink("link from neighbor node 1"), noWay);
            fail("attempt to connect to a no way link should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            node.addConnection(DefaultsNl.VEHICLE, noWay, network.getLink("link from neighbor node 1"));
            fail("attempt to connect from a no way link should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        noWay = new OtsLink(network, "no way traffic outbound link", node, n2, DefaultsNl.RAILWAY,
                new OtsLine3d(node.getPoint(), n2.getPoint()));
        try
        {
            node.addConnection(DefaultsNl.VEHICLE, network.getLink("link from neighbor node 1"), noWay);
            fail("attempt to connect to a no way link should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            node.addConnection(DefaultsNl.VEHICLE, noWay, network.getLink("link from neighbor node 1"));
            fail("attempt to connect from a no way link should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
    }

    /**
     * Test the addConnection method with a Set and related functions.
     * @throws NetworkException if that happens uncaught; this test has failed
     * @throws OtsGeometryException if that happens uncaught; this test has failed
     */
    @Test
    public final void connectionSetTest() throws NetworkException, OtsGeometryException
    {
        OtsSimulatorInterface simulator = MockSimulator.createMock();
        OtsNetwork network = new OtsNetwork("connectionSets test network", simulator);
        OtsNode node = new OtsNode(network, "main", new OtsPoint3d(10, 100, 10));
        int maxNeighbor = 10;
        for (int i = 0; i < maxNeighbor; i++)
        {
            Node neighborNode = new OtsNode(network, "neighbor node " + i, new OtsPoint3d(20 + 10 * i, 0, 10));
            new OtsLink(network, "link from neighbor node " + i, neighborNode, node, DefaultsNl.ROAD,
                    new OtsLine3d(neighborNode.getPoint(), node.getPoint()));
        }
        // Prove that we can go from any neighborNode to any OTHER neighborNode
        for (int fromIndex = 0; fromIndex < maxNeighbor; fromIndex++)
        {
            Link fromLink = network.getLink("link from neighbor node " + fromIndex);
            Set<Link> nextLinks = node.nextLinks(DefaultsNl.VEHICLE, fromLink);
            assertEquals("should be maxNeighbor - 1 nextLinks", maxNeighbor - 1, nextLinks.size());
            assertFalse("should not contain fromLink", nextLinks.contains(fromLink));
        }
        // Add an explicit connection for the link from neighbor 1 to neighbor 2
        node.addConnections(DefaultsNl.VEHICLE, network.getLink("link from neighbor node 1"),
                wrap(network.getLink("link from neighbor node 2")));
        for (int fromIndex = 0; fromIndex < maxNeighbor; fromIndex++)
        {
            Link fromLink = network.getLink("link from neighbor node " + fromIndex);
            Set<Link> nextLinks = node.nextLinks(DefaultsNl.VEHICLE, fromLink);
            if (1 == fromIndex)
            {
                assertEquals("should be 1", 1, nextLinks.size());
                assertEquals("should only contain link form neighbor 2", network.getLink("link from neighbor node 2"),
                        nextLinks.iterator().next());
            }
            else
            {
                assertEquals("should be 0", 0, nextLinks.size());
            }
        }
        Node n1 = network.getNode("neighbor node 1");
        Node n2 = network.getNode("neighbor node 2");
        Link unrelatedLink =
                new OtsLink(network, "unrelated link", n1, n2, DefaultsNl.ROAD, new OtsLine3d(n1.getPoint(), n2.getPoint()));
        try
        {
            node.addConnections(DefaultsNl.VEHICLE, unrelatedLink, wrap(network.getLink("link from neighbor node 1")));
            fail("attempt to connect from a link not connected to node should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            node.addConnections(DefaultsNl.VEHICLE, network.getLink("link from neighbor node 1"), wrap(unrelatedLink));
            fail("attempt to connect to a link not connected to node should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        GtuType unrelatedGtuType = new GtuType("junk", DefaultsNl.SHIP);
        try
        {
            node.nextLinks(unrelatedGtuType, network.getLink("link from neighbor node 1"));
            fail("nextLinks for unsupported GtuType should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        // Create a link that does not allow traffic TO the node
        Link oneWayFromNodeOnly = new OtsLink(network, "one way away from node", node, n1, DefaultsNl.FREEWAY,
                new OtsLine3d(node.getPoint(), n1.getPoint()));
        // Create a link that does not allow traffic FROM the node
        Link oneWayToNodeOnly = new OtsLink(network, "one way towards node", n1, node, DefaultsNl.FREEWAY,
                new OtsLine3d(n1.getPoint(), node.getPoint()));
        try
        {
            node.addConnections(DefaultsNl.VEHICLE, oneWayFromNodeOnly, wrap(network.getLink("link from neighbor node 1")));
            fail("attempt to connect from a link that does not allow traffic TO the node should have thrown a "
                    + "NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            node.addConnections(DefaultsNl.VEHICLE, network.getLink("link from neighbor node 1"), wrap(oneWayToNodeOnly));
            fail("attempt to connect to a link that does not allow outbound traffic should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        Link noWay = new OtsLink(network, "no way traffic inbound link", n2, node, DefaultsNl.RAILWAY,
                new OtsLine3d(n2.getPoint(), node.getPoint()));
        try
        {
            node.addConnections(DefaultsNl.VEHICLE, network.getLink("link from neighbor node 1"), wrap(noWay));
            fail("attempt to connect to a rail way link should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            node.addConnections(DefaultsNl.VEHICLE, noWay, wrap(network.getLink("link from neighbor node 1")));
            fail("attempt to connect from a tail way link should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        noWay = new OtsLink(network, "no way traffic outbound link", node, n2, DefaultsNl.RAILWAY,
                new OtsLine3d(node.getPoint(), n2.getPoint()));
        try
        {
            node.addConnections(DefaultsNl.VEHICLE, network.getLink("link from neighbor node 1"), wrap(noWay));
            fail("attempt to connect to a rail way link should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            node.addConnections(DefaultsNl.VEHICLE, noWay, wrap(network.getLink("link from neighbor node 1")));
            fail("attempt to connect from a rail way link should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
    }

    /**
     * Put some Links in a Set and return that Set.
     * @param links Link...; the links
     * @return Set&lt;Link&gt;; the set that contains only the given link
     */
    private Set<Link> wrap(final Link... links)
    {
        Set<Link> result = new LinkedHashSet<>();
        for (Link link : links)
        {
            result.add(link);
        }
        return result;
    }

}
