package org.opentrafficsim.core.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.LinkedHashSet;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Direction;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.mock.MockSimulator;

/**
 * Test the Node class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class NodeTest
{

    /** */
    private NodeTest()
    {
        // do not instantiate test class
    }

    /**
     * Test the basics of the Node class.
     * @throws NetworkException if that happens uncaught; this test has failed
     */
    @Test
    public void testNode() throws NetworkException
    {
        Network network = new Network("Node test network", MockSimulator.createMock());
        Point2d point1 = new Point2d(20, 40);
        Direction heading = Direction.ofSI(Math.toRadians(123));
        DirectedPoint2d point1Oriented = new DirectedPoint2d(point1, heading.si);
        Node node1 = new Node(network, "node 1", point1, heading);
        assertEquals(network, node1.getNetwork(), "network matches");
        assertEquals("node 1", node1.getId(), "name matches");
        assertEquals(point1Oriented, node1.getPoint(), "point matches");
        assertEquals(new DirectedPoint2d(point1.x, point1.y, Math.toRadians(123)), node1.getLocation(), "getLocation");
        assertTrue(node1.toString().contains(node1.getId()), "name is in toString");
        assertEquals(heading.si, node1.getHeading().si, 0.00001, "heading matches");
        Point2d point2 = new Point2d(120, 240);
        DirectedPoint2d point2Oriented = new DirectedPoint2d(120, 240, 0.0);
        Node node2 = new Node(network, "node 2", point2);
        assertEquals(network, node2.getNetwork(), "network matches");
        assertEquals("node 2", node2.getId(), "name matches");
        assertEquals(point2Oriented, node2.getPoint(), "point matches");
        assertTrue(node1.equals(node1), "Node 1 matches itself");
        assertFalse(node1.equals(null), "Node 1 does not match null");
        assertFalse(node1.equals(node2), "Node 1 does not match node 2");
        assertTrue(node2.getHeading().si == 0.0, "Node 2 has heading 0");
        // Create another node with name node 1 in another network
        OtsSimulatorInterface simulator = MockSimulator.createMock();
        Network otherNetwork = new Network("Node test network 2", simulator);
        Node node3 = new Node(otherNetwork, "node 1", point1);
        assertTrue(node1.equals(node3), "Node 1 does match node 3 in other network");

        assertEquals(0, node1.getLinks().size(), "node 1 has no links");

        // Create a couple of links
        Link link1 = new Link(network, "link 1", node1, node2, DefaultsNl.ROAD,
                new OtsLine2d(node1.getPoint(), node2.getPoint()), null);
        assertEquals(1, node1.getLinks().size(), "node 1 has one link");
        assertEquals(1, node2.getLinks().size(), "node 2 has one link");
        assertEquals(link1, node1.getLinks().iterator().next(), "link at node 1 is link1");
        assertEquals(link1, node2.getLinks().iterator().next(), "link at node 2 is link1");
        Node node4 = new Node(network, "node 3", new Point2d(10, 10));
        Link link2 = new Link(network, "link 2", node1, node4, DefaultsNl.ROAD,
                new OtsLine2d(node1.getPoint(), node4.getPoint()), null);
        Link link3 = new Link(network, "link 3", node4, node2, DefaultsNl.ROAD,
                new OtsLine2d(node4.getPoint(), node2.getPoint()), null);
        Link link4 = new Link(network, "link 4", node2, node1, DefaultsNl.ROAD,
                new OtsLine2d(node2.getPoint(), node1.getPoint()), null);
        assertEquals(3, node1.getLinks().size(), "node 1 has three links");
        assertEquals(3, node2.getLinks().size(), "node 2 has three links");
        assertEquals(2, node4.getLinks().size(), "node 4 has two links");
        assertTrue(node1.getLinks().contains(link1), "node 1 has link 1");
        assertTrue(node1.getLinks().contains(link2), "node 1 has link 2");
        assertTrue(node1.getLinks().contains(link4), "node 1 has link 4");
        assertFalse(node1.getLinks().contains(link3), "node 1 does not have link 3");
        Set<Link> nextLinks = node1.nextLinks(DefaultsNl.VEHICLE, link4);
        assertEquals(2, nextLinks.size(), "incoming over link 4, node 1 has two next links");
        assertTrue(nextLinks.contains(link1), "incoming over link 4, next links of node 1 contains link 1");
        assertTrue(nextLinks.contains(link1), "incoming over link 4, next links of node 1 contains link 1");
        try
        {
            node1.nextLinks(DefaultsNl.VEHICLE, link3);
            fail("nextLinks from link that does not connect to the node should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        assertTrue(node1.isConnectedTo(DefaultsNl.VEHICLE, node2), "node 1 has direct connection to node 2");
        Node node5 = new Node(network, "node 5", new Point2d(1000, 0));
        assertFalse(node1.isConnectedTo(DefaultsNl.VEHICLE, node5), "node 1 has no direct connection to node 5");
        Link link5 = new Link(network, "link 5", node5, node1, DefaultsNl.FREEWAY,
                new OtsLine2d(node1.getPoint(), node5.getPoint()), null);
        assertFalse(node1.isConnectedTo(DefaultsNl.VEHICLE, node5), "node 1 still has no direct connection to node 5");
        assertTrue(node5.isConnectedTo(DefaultsNl.VEHICLE, node1), "node 5 does have a direct connection to node 1");
        assertEquals(link5, node5.getLinks().iterator().next(), "Connection from node 5 to node 1 is link5");
        node5.removeLink(link5);
        assertFalse(node5.isConnectedTo(DefaultsNl.VEHICLE, node1), "node 5 no longer has direct connection to node 1");
    }

    /**
     * Test the addConnection method and related functions.
     * @throws NetworkException if that happens uncaught; this test has failed
     */
    @Test
    public void connectionTest() throws NetworkException
    {
        OtsSimulatorInterface simulator = MockSimulator.createMock();
        Network network = new Network("connection test network", simulator);
        Node node = new Node(network, "main", new Point2d(10, 100));
        int maxNeighbor = 10;
        for (int i = 0; i < maxNeighbor; i++)
        {
            Node neighborNode = new Node(network, "neighbor node " + i, new Point2d(20 + 10 * i, 0));
            new Link(network, "link to neighbor node " + i, node, neighborNode, DefaultsNl.ROAD,
                    new OtsLine2d(node.getPoint(), neighborNode.getPoint()), null);
            new Link(network, "link from neighbor node " + i, neighborNode, node, DefaultsNl.ROAD,
                    new OtsLine2d(neighborNode.getPoint(), node.getPoint()), null);
        }
        // Prove that we can go from any neighborNode to any OTHER neighborNode including ourselves
        for (int fromIndex = 0; fromIndex < maxNeighbor; fromIndex++)
        {
            Link fromLink = network.getLink("link from neighbor node " + fromIndex).get();
            Set<Link> nextLinks = node.nextLinks(DefaultsNl.VEHICLE, fromLink);
            assertEquals(maxNeighbor, nextLinks.size(), "should be maxNeighbor nextLinks");
            assertFalse(nextLinks.contains(fromLink), "should not contain fromLink");
        }
        // Add an explicit connection for the link from neighbor 1 to neighbor 2
        node.addConnection(DefaultsNl.VEHICLE, network.getLink("link from neighbor node 1").get(),
                network.getLink("link to neighbor node 2").get());
        for (int fromIndex = 0; fromIndex < maxNeighbor; fromIndex++)
        {
            Link fromLink = network.getLink("link from neighbor node " + fromIndex).get();
            Set<Link> nextLinks = node.nextLinks(DefaultsNl.VEHICLE, fromLink);
            if (1 == fromIndex)
            {
                assertEquals(1, nextLinks.size(), "should be 1");
                assertEquals(network.getLink("link to neighbor node 2").get(), nextLinks.iterator().next(),
                        "should only contain link to neighbor 2");
            }
            else
            {
                assertEquals(0, nextLinks.size(), "should be 0");
            }
        }
        Node n1 = network.getNode("neighbor node 1").get();
        Node n2 = network.getNode("neighbor node 2").get();
        Link unrelatedLink =
                new Link(network, "unrelated link", n1, n2, DefaultsNl.ROAD, new OtsLine2d(n1.getPoint(), n2.getPoint()), null);
        try
        {
            node.addConnection(DefaultsNl.VEHICLE, unrelatedLink, network.getLink("link from neighbor node 1").get());
            fail("attempt to connect from a link not connected to node should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            node.addConnection(DefaultsNl.VEHICLE, network.getLink("link from neighbor node 1").get(), unrelatedLink);
            fail("attempt to connect to a link not connected to node should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        GtuType unrelatedGtuType = new GtuType("junk");
        try
        {
            node.nextLinks(unrelatedGtuType, network.getLink("link from neighbor node 1").get());
            fail("nextLinks for unsupported GtuType should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        // Create a link that does not allow traffic TO the node
        Link oneWayFromNode = new Link(network, "one way from node", node, n1, DefaultsNl.FREEWAY,
                new OtsLine2d(node.getPoint(), n1.getPoint()), null);
        Link oneWayToNode = new Link(network, "one way towards node", n1, node, DefaultsNl.FREEWAY,
                new OtsLine2d(n1.getPoint(), node.getPoint()), null);
        try
        {
            node.addConnection(DefaultsNl.VEHICLE, oneWayFromNode, network.getLink("link from neighbor node 1").get());
            fail("attempt to connect from a link that does not allow traffic TO the node should have thrown a "
                    + "NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            node.addConnection(DefaultsNl.VEHICLE, network.getLink("link from neighbor node 1").get(), oneWayToNode);
            fail("attempt to connect to a link that does not allow outbound traffic should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        LinkType railway = new LinkType("RAILWAY");
        Link noWay = new Link(network, "no way traffic inbound link", n2, node, railway,
                new OtsLine2d(n2.getPoint(), node.getPoint()), null);
        try
        {
            node.addConnection(DefaultsNl.VEHICLE, network.getLink("link from neighbor node 1").get(), noWay);
            fail("attempt to connect to a no way link should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            node.addConnection(DefaultsNl.VEHICLE, noWay, network.getLink("link from neighbor node 1").get());
            fail("attempt to connect from a no way link should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        noWay = new Link(network, "no way traffic outbound link", node, n2, railway,
                new OtsLine2d(node.getPoint(), n2.getPoint()), null);
        try
        {
            node.addConnection(DefaultsNl.VEHICLE, network.getLink("link from neighbor node 1").get(), noWay);
            fail("attempt to connect to a no way link should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            node.addConnection(DefaultsNl.VEHICLE, noWay, network.getLink("link from neighbor node 1").get());
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
     */
    @Test
    public void connectionSetTest() throws NetworkException
    {
        OtsSimulatorInterface simulator = MockSimulator.createMock();
        Network network = new Network("connectionSets test network", simulator);
        Node node = new Node(network, "main", new Point2d(10, 100));
        int maxNeighbor = 10;
        for (int i = 0; i < maxNeighbor; i++)
        {
            Node neighborNode = new Node(network, "neighbor node " + i, new Point2d(20 + 10 * i, 0));
            new Link(network, "link from neighbor node " + i, neighborNode, node, DefaultsNl.ROAD,
                    new OtsLine2d(neighborNode.getPoint(), node.getPoint()), null);
            new Link(network, "link to neighbor node " + i, node, neighborNode, DefaultsNl.ROAD,
                    new OtsLine2d(node.getPoint(), neighborNode.getPoint()), null);
        }
        // Prove that we can go from any neighborNode to any neighborNode (inlcuding ourselves, b/c of two links
        for (int fromIndex = 0; fromIndex < maxNeighbor; fromIndex++)
        {
            Link fromLink = network.getLink("link from neighbor node " + fromIndex).get();
            Set<Link> nextLinks = node.nextLinks(DefaultsNl.VEHICLE, fromLink);
            assertEquals(maxNeighbor, nextLinks.size(), "should be maxNeighbor nextLinks");
        }
        // Add an explicit connection for the link from neighbor 1 to neighbor 2
        node.addConnections(DefaultsNl.VEHICLE, network.getLink("link from neighbor node 1").get(),
                wrap(network.getLink("link to neighbor node 2").get()));
        for (int fromIndex = 0; fromIndex < maxNeighbor; fromIndex++)
        {
            Link fromLink = network.getLink("link from neighbor node " + fromIndex).get();
            Set<Link> nextLinks = node.nextLinks(DefaultsNl.VEHICLE, fromLink);
            if (1 == fromIndex)
            {
                assertEquals(1, nextLinks.size(), "should be 1");
                assertEquals(network.getLink("link to neighbor node 2").get(), nextLinks.iterator().next(),
                        "should only contain link to neighbor 2");
            }
            else
            {
                assertEquals(0, nextLinks.size(), "should be 0");
            }
        }
        Node n1 = network.getNode("neighbor node 1").get();
        Node n2 = network.getNode("neighbor node 2").get();
        Link unrelatedLink =
                new Link(network, "unrelated link", n1, n2, DefaultsNl.ROAD, new OtsLine2d(n1.getPoint(), n2.getPoint()), null);
        try
        {
            node.addConnections(DefaultsNl.VEHICLE, unrelatedLink, wrap(network.getLink("link from neighbor node 1").get()));
            fail("attempt to connect from a link not connected to node should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            node.addConnections(DefaultsNl.VEHICLE, network.getLink("link from neighbor node 1").get(), wrap(unrelatedLink));
            fail("attempt to connect to a link not connected to node should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        GtuType unrelatedGtuType = new GtuType("junk");
        try
        {
            node.nextLinks(unrelatedGtuType, network.getLink("link from neighbor node 1").get());
            fail("nextLinks for unsupported GtuType should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        // Create a link that does not allow traffic TO the node
        Link oneWayFromNodeOnly = new Link(network, "one way away from node", node, n1, DefaultsNl.FREEWAY,
                new OtsLine2d(node.getPoint(), n1.getPoint()), null);
        // Create a link that does not allow traffic FROM the node
        Link oneWayToNodeOnly = new Link(network, "one way towards node", n1, node, DefaultsNl.FREEWAY,
                new OtsLine2d(n1.getPoint(), node.getPoint()), null);
        try
        {
            node.addConnections(DefaultsNl.VEHICLE, oneWayFromNodeOnly,
                    wrap(network.getLink("link from neighbor node 1").get()));
            fail("attempt to connect from a link that does not allow traffic TO the node should have thrown a "
                    + "NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            node.addConnections(DefaultsNl.VEHICLE, network.getLink("link from neighbor node 1").get(), wrap(oneWayToNodeOnly));
            fail("attempt to connect to a link that does not allow outbound traffic should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        LinkType railway = new LinkType("RAILWAY");
        Link noWay = new Link(network, "no way traffic inbound link", n2, node, railway,
                new OtsLine2d(n2.getPoint(), node.getPoint()), null);
        try
        {
            node.addConnections(DefaultsNl.VEHICLE, network.getLink("link from neighbor node 1").get(), wrap(noWay));
            fail("attempt to connect to a rail way link should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            node.addConnections(DefaultsNl.VEHICLE, noWay, wrap(network.getLink("link from neighbor node 1").get()));
            fail("attempt to connect from a tail way link should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        noWay = new Link(network, "no way traffic outbound link", node, n2, railway,
                new OtsLine2d(node.getPoint(), n2.getPoint()), null);
        try
        {
            node.addConnections(DefaultsNl.VEHICLE, network.getLink("link from neighbor node 1").get(), wrap(noWay));
            fail("attempt to connect to a rail way link should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            node.addConnections(DefaultsNl.VEHICLE, noWay, wrap(network.getLink("link from neighbor node 1").get()));
            fail("attempt to connect from a rail way link should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
    }

    /**
     * Put some Links in a Set and return that Set.
     * @param links the links
     * @return the set that contains only the given link
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
