package org.opentrafficsim.core.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.mock.MockSimulator;

import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Test the OTSNode class.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jan 5, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class OTSNodeTest
{

    /**
     * Test the basics of the OTSNode class.
     * @throws NetworkException if that happens uncaught; this test has failed
     * @throws OTSGeometryException if that happens unchaught; this test has failed
     */
    @Test
    public final void testOTSNode() throws NetworkException, OTSGeometryException
    {
        Network network = new OTSNetwork("Node test network", true);
        OTSPoint3D point1 = new OTSPoint3D(20, 40, 60);
        OTSNode node1 = new OTSNode(network, "node 1", point1);
        assertEquals("network matches", network, node1.getNetwork());
        assertEquals("name matches", "node 1", node1.getId());
        assertEquals("point matches", point1, node1.getPoint());
        assertEquals("getLocation", new DirectedPoint(point1.x, point1.y, point1.z), node1.getLocation());
        assertTrue("name is in toString", node1.toString().contains(node1.getId()));
        OTSPoint3D point2 = new OTSPoint3D(120, 240, 60);
        OTSNode node2 = new OTSNode(network, "node 2", point2);
        assertEquals("network matches", network, node2.getNetwork());
        assertEquals("name matches", "node 2", node2.getId());
        assertEquals("point matches", point2, node2.getPoint());
        assertTrue("Node 1 matches itself", node1.equals(node1));
        assertFalse("Node 1 does not match null", node1.equals(null));
        assertFalse("Node 1 does not match node 2", node1.equals(node2));
        // Create another node with name node 1 in another network
        Network otherNetwork = new OTSNetwork("Node test network 2", true);
        OTSNode node3 = new OTSNode(otherNetwork, "node 1", point1);
        assertTrue("Node 1 does match node 3 in other network", node1.equals(node3));

        assertEquals("node 1 has no links", 0, node1.getLinks().size());

        OTSSimulatorInterface simulator = MockSimulator.createMock();

        // Create a couple of links
        Link link1 = new OTSLink(network, "link 1", node1, node2, network.getLinkType(LinkType.DEFAULTS.ROAD),
                new OTSLine3D(node1.getPoint(), node2.getPoint()), simulator);
        assertEquals("node 1 has one link", 1, node1.getLinks().size());
        assertEquals("node 2 has one link", 1, node2.getLinks().size());
        assertEquals("link at node 1 is link1", link1, node1.getLinks().iterator().next());
        assertEquals("link at node 2 is link1", link1, node2.getLinks().iterator().next());
        OTSNode node4 = new OTSNode(network, "node 3", new OTSPoint3D(10, 10, 10));
        Link link2 = new OTSLink(network, "link 2", node1, node4, network.getLinkType(LinkType.DEFAULTS.ROAD),
                new OTSLine3D(node1.getPoint(), node4.getPoint()), simulator);
        Link link3 = new OTSLink(network, "link 3", node4, node2, network.getLinkType(LinkType.DEFAULTS.ROAD),
                new OTSLine3D(node4.getPoint(), node2.getPoint()), simulator);
        Link link4 = new OTSLink(network, "link 4", node2, node1, network.getLinkType(LinkType.DEFAULTS.ROAD),
                new OTSLine3D(node2.getPoint(), node1.getPoint()), simulator);
        assertEquals("node 1 has three links", 3, node1.getLinks().size());
        assertEquals("node 2 has three links", 3, node2.getLinks().size());
        assertEquals("node 4 has two links", 2, node4.getLinks().size());
        assertTrue("node 1 has link 1", node1.getLinks().contains(link1));
        assertTrue("node 1 has link 2", node1.getLinks().contains(link2));
        assertTrue("node 1 has link 4", node1.getLinks().contains(link4));
        assertFalse("node 1 does not have link 3", node1.getLinks().contains(link3));
        Set<Link> nextLinks = node1.nextLinks(network.getGtuType(GTUType.DEFAULTS.VEHICLE), link4);
        assertEquals("incoming over link 4, node 1 has two next links", 2, nextLinks.size());
        assertTrue("incoming over link 4, next links of node 1 contains link 1", nextLinks.contains(link1));
        assertTrue("incoming over link 4, next links of node 1 contains link 1", nextLinks.contains(link1));
        try
        {
            node1.nextLinks(network.getGtuType(GTUType.DEFAULTS.VEHICLE), link3);
            fail("nextLinks from link that does not connect to the node should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        assertTrue("node 1 has direct connection to node 2",
                node1.isDirectionallyConnectedTo(network.getGtuType(GTUType.DEFAULTS.VEHICLE), node2));
        Node node5 = new OTSNode(network, "node 5", new OTSPoint3D(1000, 0, 0));
        assertFalse("node 1 has no direct connection to node 5",
                node1.isDirectionallyConnectedTo(network.getGtuType(GTUType.DEFAULTS.VEHICLE), node5));
        Link link5 = new OTSLink(network, "link 5", node5, node1, network.getLinkType(LinkType.DEFAULTS.FREEWAY),
                new OTSLine3D(node1.getPoint(), node5.getPoint()), simulator);
        assertFalse("node 1 still has no direct connection to node 5",
                node1.isDirectionallyConnectedTo(network.getGtuType(GTUType.DEFAULTS.VEHICLE), node5));
        assertTrue("node 5 does have a direct connection to node 1",
                node5.isDirectionallyConnectedTo(network.getGtuType(GTUType.DEFAULTS.VEHICLE), node1));
        assertEquals("Connection from node 5 to node 1 is link5", link5, node5.getLinks().iterator().next());
        node5.removeLink(link5);
        assertFalse("node 5 no longer has direct connection to node 1",
                node5.isDirectionallyConnectedTo(network.getGtuType(GTUType.DEFAULTS.VEHICLE), node1));
        Point3d pt = new Point3d();
        Bounds b = node1.getBounds();
        BoundingSphere bs = (BoundingSphere) b;
        bs.getCenter(pt);
        assertEquals("center of bounding sphere of node 1 is origin", 0,
                new OTSPoint3D(0, 0, 0).distance(new OTSPoint3D(pt)).si, 0.001);
        assertEquals("radius of bounding sphere of node 1 is 10m", 10, bs.getRadius(), 00001);
    }

    /**
     * Test the addConnection method and related functions.
     * @throws NetworkException if that happens uncaught; this test has failed
     * @throws OTSGeometryException if that happens uncaught; this test has failed
     */
    @Test
    public final void connectionTest() throws NetworkException, OTSGeometryException
    {
        Network network = new OTSNetwork("connection test network", true);
        OTSSimulatorInterface simulator = MockSimulator.createMock();
        OTSNode node = new OTSNode(network, "main", new OTSPoint3D(10, 100, 10));
        int maxNeighbor = 10;
        for (int i = 0; i < maxNeighbor; i++)
        {
            Node neighborNode = new OTSNode(network, "neighbor node " + i, new OTSPoint3D(20 + 10 * i, 0, 10));
            new OTSLink(network, "link from neighbor node " + i, neighborNode, node,
                    network.getLinkType(LinkType.DEFAULTS.ROAD), new OTSLine3D(neighborNode.getPoint(), node.getPoint()),
                    simulator);
        }
        // Prove that we can go from any neighborNode to any OTHER neighborNode
        for (int fromIndex = 0; fromIndex < maxNeighbor; fromIndex++)
        {
            Link fromLink = network.getLink("link from neighbor node " + fromIndex);
            Set<Link> nextLinks = node.nextLinks(network.getGtuType(GTUType.DEFAULTS.VEHICLE), fromLink);
            assertEquals("should be maxNeighbor - 1 nextLinks", maxNeighbor - 1, nextLinks.size());
            assertFalse("should not contain fromLink", nextLinks.contains(fromLink));
        }
        // Add an explicit connection for the link from neighbor 1 to neighbor 2
        node.addConnection(network.getGtuType(GTUType.DEFAULTS.VEHICLE), network.getLink("link from neighbor node 1"),
                network.getLink("link from neighbor node 2"));
        for (int fromIndex = 0; fromIndex < maxNeighbor; fromIndex++)
        {
            Link fromLink = network.getLink("link from neighbor node " + fromIndex);
            Set<Link> nextLinks = node.nextLinks(network.getGtuType(GTUType.DEFAULTS.VEHICLE), fromLink);
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
        Link unrelatedLink = new OTSLink(network, "unrelated link", n1, n2, network.getLinkType(LinkType.DEFAULTS.ROAD),
                new OTSLine3D(n1.getPoint(), n2.getPoint()), simulator);
        try
        {
            node.addConnection(network.getGtuType(GTUType.DEFAULTS.VEHICLE), unrelatedLink,
                    network.getLink("link from neighbor node 1"));
            fail("attempt to connect from a link not connected to node should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            node.addConnection(network.getGtuType(GTUType.DEFAULTS.VEHICLE), network.getLink("link from neighbor node 1"),
                    unrelatedLink);
            fail("attempt to connect to a link not connected to node should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        GTUType unrelatedGTUType = new GTUType("junk", network.getGtuType(GTUType.DEFAULTS.SHIP));
        try
        {
            node.nextLinks(unrelatedGTUType, network.getLink("link from neighbor node 1"));
            fail("nextLinks for unsupported GTUType should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        // Create a link that does not allow traffic TO the node
        Link oneWayFromNode = new OTSLink(network, "one way from node", node, n1,
                network.getLinkType(LinkType.DEFAULTS.FREEWAY), new OTSLine3D(node.getPoint(), n1.getPoint()), simulator);
        Link oneWayToNode = new OTSLink(network, "one way towards node", n1, node,
                network.getLinkType(LinkType.DEFAULTS.FREEWAY), new OTSLine3D(n1.getPoint(), node.getPoint()), simulator);
        try
        {
            node.addConnection(network.getGtuType(GTUType.DEFAULTS.VEHICLE), oneWayFromNode,
                    network.getLink("link from neighbor node 1"));
            fail("attempt to connect from a link that does not allow traffic TO the node should have thrown a "
                    + "NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            node.addConnection(network.getGtuType(GTUType.DEFAULTS.VEHICLE), network.getLink("link from neighbor node 1"),
                    oneWayToNode);
            fail("attempt to connect to a link that does not allow outbound traffic should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        Link noWay = new OTSLink(network, "no way traffic inbound link", n2, node,
                network.getLinkType(LinkType.DEFAULTS.RAILWAY), new OTSLine3D(n2.getPoint(), node.getPoint()), simulator);
        try
        {
            node.addConnection(network.getGtuType(GTUType.DEFAULTS.VEHICLE), network.getLink("link from neighbor node 1"),
                    noWay);
            fail("attempt to connect to a no way link should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            node.addConnection(network.getGtuType(GTUType.DEFAULTS.VEHICLE), noWay,
                    network.getLink("link from neighbor node 1"));
            fail("attempt to connect from a no way link should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        noWay = new OTSLink(network, "no way traffic outbound link", node, n2, network.getLinkType(LinkType.DEFAULTS.RAILWAY),
                new OTSLine3D(node.getPoint(), n2.getPoint()), simulator);
        try
        {
            node.addConnection(network.getGtuType(GTUType.DEFAULTS.VEHICLE), network.getLink("link from neighbor node 1"),
                    noWay);
            fail("attempt to connect to a no way link should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            node.addConnection(network.getGtuType(GTUType.DEFAULTS.VEHICLE), noWay,
                    network.getLink("link from neighbor node 1"));
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
     * @throws OTSGeometryException if that happens uncaught; this test has failed
     */
    @Test
    public final void connectionSetTest() throws NetworkException, OTSGeometryException
    {
        Network network = new OTSNetwork("connectionSets test network", true);
        OTSSimulatorInterface simulator = MockSimulator.createMock();
        OTSNode node = new OTSNode(network, "main", new OTSPoint3D(10, 100, 10));
        int maxNeighbor = 10;
        for (int i = 0; i < maxNeighbor; i++)
        {
            Node neighborNode = new OTSNode(network, "neighbor node " + i, new OTSPoint3D(20 + 10 * i, 0, 10));
            new OTSLink(network, "link from neighbor node " + i, neighborNode, node,
                    network.getLinkType(LinkType.DEFAULTS.ROAD), new OTSLine3D(neighborNode.getPoint(), node.getPoint()),
                    simulator);
        }
        // Prove that we can go from any neighborNode to any OTHER neighborNode
        for (int fromIndex = 0; fromIndex < maxNeighbor; fromIndex++)
        {
            Link fromLink = network.getLink("link from neighbor node " + fromIndex);
            Set<Link> nextLinks = node.nextLinks(network.getGtuType(GTUType.DEFAULTS.VEHICLE), fromLink);
            assertEquals("should be maxNeighbor - 1 nextLinks", maxNeighbor - 1, nextLinks.size());
            assertFalse("should not contain fromLink", nextLinks.contains(fromLink));
        }
        // Add an explicit connection for the link from neighbor 1 to neighbor 2
        node.addConnections(network.getGtuType(GTUType.DEFAULTS.VEHICLE), network.getLink("link from neighbor node 1"),
                wrap(network.getLink("link from neighbor node 2")));
        for (int fromIndex = 0; fromIndex < maxNeighbor; fromIndex++)
        {
            Link fromLink = network.getLink("link from neighbor node " + fromIndex);
            Set<Link> nextLinks = node.nextLinks(network.getGtuType(GTUType.DEFAULTS.VEHICLE), fromLink);
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
        Link unrelatedLink = new OTSLink(network, "unrelated link", n1, n2, network.getLinkType(LinkType.DEFAULTS.ROAD),
                new OTSLine3D(n1.getPoint(), n2.getPoint()), simulator);
        try
        {
            node.addConnections(network.getGtuType(GTUType.DEFAULTS.VEHICLE), unrelatedLink,
                    wrap(network.getLink("link from neighbor node 1")));
            fail("attempt to connect from a link not connected to node should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            node.addConnections(network.getGtuType(GTUType.DEFAULTS.VEHICLE), network.getLink("link from neighbor node 1"),
                    wrap(unrelatedLink));
            fail("attempt to connect to a link not connected to node should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        GTUType unrelatedGTUType = new GTUType("junk", network.getGtuType(GTUType.DEFAULTS.SHIP));
        try
        {
            node.nextLinks(unrelatedGTUType, network.getLink("link from neighbor node 1"));
            fail("nextLinks for unsupported GTUType should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        // Create a link that does not allow traffic TO the node
        Link oneWayFromNodeOnly = new OTSLink(network, "one way away from node", node, n1,
                network.getLinkType(LinkType.DEFAULTS.FREEWAY), new OTSLine3D(node.getPoint(), n1.getPoint()), simulator);
        // Create a link that does not allow traffic FROM the node
        Link oneWayToNodeOnly = new OTSLink(network, "one way towards node", n1, node,
                network.getLinkType(LinkType.DEFAULTS.FREEWAY), new OTSLine3D(n1.getPoint(), node.getPoint()), simulator);
        try
        {
            node.addConnections(network.getGtuType(GTUType.DEFAULTS.VEHICLE), oneWayFromNodeOnly,
                    wrap(network.getLink("link from neighbor node 1")));
            fail("attempt to connect from a link that does not allow traffic TO the node should have thrown a "
                    + "NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            node.addConnections(network.getGtuType(GTUType.DEFAULTS.VEHICLE), network.getLink("link from neighbor node 1"),
                    wrap(oneWayToNodeOnly));
            fail("attempt to connect to a link that does not allow outbound traffic should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        Link noWay = new OTSLink(network, "no way traffic inbound link", n2, node,
                network.getLinkType(LinkType.DEFAULTS.RAILWAY), new OTSLine3D(n2.getPoint(), node.getPoint()), simulator);
        try
        {
            node.addConnections(network.getGtuType(GTUType.DEFAULTS.VEHICLE), network.getLink("link from neighbor node 1"),
                    wrap(noWay));
            fail("attempt to connect to a rail way link should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            node.addConnections(network.getGtuType(GTUType.DEFAULTS.VEHICLE), noWay,
                    wrap(network.getLink("link from neighbor node 1")));
            fail("attempt to connect from a tail way link should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        noWay = new OTSLink(network, "no way traffic outbound link", node, n2, network.getLinkType(LinkType.DEFAULTS.RAILWAY),
                new OTSLine3D(node.getPoint(), n2.getPoint()), simulator);
        try
        {
            node.addConnections(network.getGtuType(GTUType.DEFAULTS.VEHICLE), network.getLink("link from neighbor node 1"),
                    wrap(noWay));
            fail("attempt to connect to a rail way link should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            node.addConnections(network.getGtuType(GTUType.DEFAULTS.VEHICLE), noWay,
                    wrap(network.getLink("link from neighbor node 1")));
            fail("attempt to connect from a rail way link should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        checkClone((OTSNetwork) network);
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

    /**
     * Check that cloning the network correctly clones the Nodes.
     * @param network OTSNetwork; the network
     * @throws NetworkException if that happens; this test has failed
     */
    private void checkClone(final OTSNetwork network) throws NetworkException
    {
        OTSSimulatorInterface oldSimulator = MockSimulator.createMock();
        OTSSimulatorInterface newSimulator = MockSimulator.createMock();
        OTSNetwork clonedNetwork = OTSNetworkUtils.clone(network, "clonedNetwork", oldSimulator, newSimulator);
        assertEquals("Number of nodes should be same", network.getNodeMap().size(), clonedNetwork.getNodeMap().size());
        assertTrue("Node map should be equal", network.getNodeMap().equals(clonedNetwork.getNodeMap()));
    }

}
