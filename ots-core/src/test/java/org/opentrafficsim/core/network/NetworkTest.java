package org.opentrafficsim.core.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.EventType;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine2d;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.mock.MockGtu;
import org.opentrafficsim.core.mock.MockSimulator;
import org.opentrafficsim.core.network.route.Route;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class NetworkTest implements EventListener
{
    /** ... */
    private static final long serialVersionUID = 1L;

    /** Count NODE_ADD events. */
    private int nodeAddedCount = 0;

    /** Count NODE_REMOVE events. */
    private int nodeRemovedCount = 0;

    /** Count LINK_ADD events. */
    private int linkAddedCount = 0;

    /** Count LINK_REMOVE events. */
    private int linkRemovedCount = 0;

    /** Count GTU_ADD events. */
    private int gtuAddedCount = 0;

    /** Count GTU_REMOVE events. */
    private int gtuRemovedCount = 0;

    /** Count other events. */
    private int otherEventCount = 0;

    /**
     * Test Network class.
     * @throws NetworkException if that happens; this test has failed
     * @throws OtsGeometryException if that happens; this test has failed
     */
    @Test
    public final void testNetwork() throws NetworkException, OtsGeometryException
    {
        String networkId = "testNetwork";
        OtsSimulatorInterface simulator = MockSimulator.createMock();
        Network network = new Network(networkId, simulator);
        assertTrue(networkId.equals(network.getId()), "Id must match");
        network.addListener(this, Network.LINK_ADD_EVENT);
        network.addListener(this, Network.LINK_REMOVE_EVENT);
        network.addListener(this, Network.NODE_ADD_EVENT);
        network.addListener(this, Network.NODE_REMOVE_EVENT);
        network.addListener(this, Network.GTU_ADD_EVENT);
        network.addListener(this, Network.GTU_REMOVE_EVENT);
        assertEquals(0, this.linkAddedCount, "link add event count is 0");
        assertEquals(0, this.linkRemovedCount, "link removed event count is 0");
        assertEquals(0, this.nodeAddedCount, "node add event count is 0");
        assertEquals(0, this.nodeRemovedCount, "node removed event count is 0");
        assertEquals(0, this.gtuAddedCount, "GTU add event count is 0");
        assertEquals(0, this.gtuRemovedCount, "GTU removed event count is 0");
        assertEquals(0, this.otherEventCount, "other event count is 0");
        assertEquals(0, network.getNodeMap().size(), "Node map is empty");
        Node node1 = new Node(network, "node1", new Point2d(10, 20));
        assertEquals(0, this.linkAddedCount, "link add event count is 0");
        assertEquals(0, this.linkRemovedCount, "link removed event count is 0");
        assertEquals(1, this.nodeAddedCount, "node add event count is 1");
        assertEquals(0, this.nodeRemovedCount, "node removed event count is 0");
        assertEquals(0, this.gtuAddedCount, "GTU add event count is 0");
        assertEquals(0, this.gtuRemovedCount, "GTU removed event count is 0");
        assertEquals(0, this.otherEventCount, "other event count is 0");
        assertEquals(1, network.getNodeMap().size(), "Node map now contains one node");
        assertEquals(node1, network.getNodeMap().values().iterator().next(), "Node is node1");
        assertEquals(1, network.getRawNodeMap().size(), "Raw node map also contains one node");
        assertEquals(node1, network.getRawNodeMap().values().iterator().next(), "Raw node map also contains node1");
        assertEquals(node1, network.getNode(node1.getId()), "Node can be retrieved by id");
        assertTrue(network.containsNode("node1"), "network contains a node with id node1");
        // Create a node that is NOT in this network; to do that we must create another network
        Network otherNetwork = new Network("other network", simulator);
        Node node2 = new Node(otherNetwork, "node2", new Point2d(11, 12));
        assertFalse(network.containsNode(node2), "node2 is NOT in network");
        assertEquals(0, this.linkAddedCount, "link add event count is 0");
        assertEquals(0, this.linkRemovedCount, "link removed event count is 0");
        assertEquals(1, this.nodeAddedCount, "node add event count is 1");
        assertEquals(0, this.nodeRemovedCount, "node removed event count is 0");
        assertEquals(0, this.gtuAddedCount, "GTU add event count is 0");
        assertEquals(0, this.gtuRemovedCount, "GTU removed event count is 0");
        assertEquals(0, this.otherEventCount, "other event count is 0");
        try
        {
            new Node(network, "node1", new Point2d(110, 20));
            fail("duplicate node id should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            network.addNode(node1);
            fail("duplicate node should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        network.removeNode(node1);
        assertEquals(0, this.linkAddedCount, "link add event count is 0");
        assertEquals(0, this.linkRemovedCount, "link removed event count is 0");
        assertEquals(1, this.nodeAddedCount, "node add event count is 1");
        assertEquals(1, this.nodeRemovedCount, "node removed event count is 1");
        assertEquals(0, this.gtuAddedCount, "GTU add event count is 0");
        assertEquals(0, this.gtuRemovedCount, "GTU removed event count is 0");
        assertEquals(0, this.otherEventCount, "other event count is 0");
        assertEquals(0, network.getNodeMap().size(), "Node map is empty");
        assertEquals(0, network.getNodeMap().size(), "network now had 0 nodes");
        try
        {
            network.removeNode(node1);
            fail("Attempt to remove an already removed node should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        network.addNode(node1);
        assertEquals(1, network.getNodeMap().size(), "Node map now contains one node");
        assertEquals(node1, network.getNodeMap().values().iterator().next(), "Node is node1");
        assertEquals(node1, network.getNode(node1.getId()), "Node can be retrieved by id");
        assertEquals(0, network.getLinkMap().size(), "LinkMap is empty");
        assertEquals(0, this.linkAddedCount, "link add event count is 0");
        assertEquals(0, this.linkRemovedCount, "link removed event count is 0");
        assertEquals(2, this.nodeAddedCount, "node add event count is 2");
        assertEquals(1, this.nodeRemovedCount, "node removed event count is 1");
        assertEquals(0, this.gtuAddedCount, "GTU add event count is 0");
        assertEquals(0, this.gtuRemovedCount, "GTU removed event count is 0");
        assertEquals(0, this.otherEventCount, "other event count is 0");
        try
        {
            new Link(network, "link1", node1, node2, DefaultsNl.ROAD, new OtsLine2d(node1.getPoint(), node2.getPoint()), null);
            fail("new OTSLink should have thrown an exception because node2 is not in network");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            new Link(network, "link1", node2, node1, DefaultsNl.ROAD, new OtsLine2d(node2.getPoint(), node1.getPoint()), null);
            fail("new OTSLink should have thrown an exception because node2 is not in network");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        Node node3 = new Node(network, "node3", new Point2d(11, 12));
        assertEquals(0, this.linkAddedCount, "link add event count is 0");
        assertEquals(0, this.linkRemovedCount, "link removed event count is 0");
        assertEquals(3, this.nodeAddedCount, "node add event count is 3");
        assertEquals(1, this.nodeRemovedCount, "node removed event count is 1");
        assertEquals(0, this.gtuAddedCount, "GTU add event count is 0");
        assertEquals(0, this.gtuRemovedCount, "GTU removed event count is 0");
        assertEquals(0, this.otherEventCount, "other event count is 0");
        Link link1 = new Link(network, "link1", node1, node3, DefaultsNl.ROAD,
                new OtsLine2d(node1.getPoint(), node3.getPoint()), null);
        assertEquals(1, network.getLinkMap().size(), "LinkMap now contains 1 link");
        assertTrue(network.containsLink(link1), "LinkMap contains link1");
        assertTrue(network.containsLink("link1"), "LinkMap.contain link with name link1");
        assertEquals(1, this.linkAddedCount, "link add event count is 1");
        assertEquals(0, this.linkRemovedCount, "link removed event count is 0");
        assertEquals(3, this.nodeAddedCount, "node add event count is 3");
        assertEquals(1, this.nodeRemovedCount, "node removed event count is 1");
        assertEquals(0, this.gtuAddedCount, "GTU add event count is 0");
        assertEquals(0, this.gtuRemovedCount, "GTU removed event count is 0");
        assertEquals(0, this.otherEventCount, "other event count is 0");
        try
        {
            network.addLink(link1);
            fail("Adding link1 again should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        assertEquals(link1, network.getLink(node1, node3), "link1 is the link connecting node1 to node3");
        assertEquals(link1, network.getLink("node1", "node3"),
                "link1 is the link connecting node named node1 to node named node3");
        Node node4 = new Node(otherNetwork, "node4", new Point2d(-2, -3));
        Link otherLink = new Link(otherNetwork, "otherLink", node2, node4, DefaultsNl.ROAD,
                new OtsLine2d(node2.getPoint(), node4.getPoint()), null);
        try
        {
            network.removeLink(otherLink);
            fail("Removing a link that is in another network should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            network.addLink(otherLink);
            fail("Adding a link that connects nodes not in the network should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        assertEquals(1, this.linkAddedCount, "link add event count is 1");
        assertEquals(0, this.linkRemovedCount, "link removed event count is 0");
        assertEquals(3, this.nodeAddedCount, "node add event count is 3");
        assertEquals(1, this.nodeRemovedCount, "node removed event count is 1");
        assertEquals(0, this.gtuAddedCount, "GTU add event count is 0");
        assertEquals(0, this.gtuRemovedCount, "GTU removed event count is 0");
        assertEquals(0, this.otherEventCount, "other event count is 0");
        Link secondLink = new Link(network, "reverseLink", node3, node1, DefaultsNl.ROAD,
                new OtsLine2d(node3.getPoint(), node1.getPoint()), null);
        assertEquals(2, this.linkAddedCount, "link add event count is 2");
        assertEquals(0, this.linkRemovedCount, "link removed event count is 0");
        assertEquals(3, this.nodeAddedCount, "node add event count is 3");
        assertEquals(1, this.nodeRemovedCount, "node removed event count is 1");
        assertEquals(0, this.gtuAddedCount, "GTU add event count is 0");
        assertEquals(0, this.gtuRemovedCount, "GTU removed event count is 0");
        assertEquals(0, this.otherEventCount, "other event count is 0");
        assertTrue(network.containsLink(secondLink), "Network contains secondLink");
        assertTrue(network.containsLink("reverseLink"), "Network contains link named reverseLink");
        assertFalse(network.containsLink("junk"), "Network does not contain link named junk");
        try
        {
            network.getLink("junk", "node3");
            fail("looking up a link starting at nonexistent node should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            network.getLink("node1", "junk");
            fail("looking up a link ending at nonexistent node should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        assertEquals(link1, network.getLink("node1", "node3"), "lookup link from node node1 to node node3");
        assertEquals(link1, network.getLink(node1, node3), "lookup link from node1 to node3");
        assertEquals(secondLink, network.getLink("node3", "node1"), "lookup link from node node3 to node node1");
        assertEquals(secondLink, network.getLink(node3, node1), "lookup link from node3 to node1");
        assertNull(network.getLink(node1, node1), "lookup link that does not exist but both nodes do exist");
        assertNull(network.getLink("node1", "node1"), "lookup link that does not exist but both nodes do exist");
        assertEquals(link1, network.getLink("link1"), "lookup link by name");
        assertEquals(secondLink, network.getLink("reverseLink"), "lookup link by name");
        network.removeLink(link1);
        assertFalse(network.containsLink(link1), "Network no longer contains link1");
        assertFalse(network.containsLink("link1"), "Network no longer contains link with name link1");
        assertEquals(2, this.linkAddedCount, "link add event count is 2");
        assertEquals(1, this.linkRemovedCount, "link removed event count is 1");
        assertEquals(3, this.nodeAddedCount, "node add event count is 3");
        assertEquals(1, this.nodeRemovedCount, "node removed event count is 1");
        assertEquals(0, this.gtuAddedCount, "GTU add event count is 0");
        assertEquals(0, this.gtuRemovedCount, "GTU removed event count is 0");
        assertEquals(0, this.otherEventCount, "other event count is 0");
        assertEquals(1, network.getLinkMap().size(), "network now contains one link");
        MockGtu mockGtu1 = new MockGtu("gtu1");
        Gtu gtu1 = mockGtu1.getMock();
        network.addGTU(gtu1);
        assertEquals(2, this.linkAddedCount, "link add event count is 2");
        assertEquals(1, this.linkRemovedCount, "link removed event count is 1");
        assertEquals(3, this.nodeAddedCount, "node add event count is 3");
        assertEquals(1, this.nodeRemovedCount, "node removed event count is 1");
        assertEquals(1, this.gtuAddedCount, "GTU add event count is 1");
        assertEquals(0, this.gtuRemovedCount, "GTU removed event count is 0");
        MockGtu mockGtu2 = new MockGtu("gtu2");
        Gtu gtu2 = mockGtu2.getMock();
        network.addGTU(gtu2);
        assertEquals(2, this.linkAddedCount, "link add event count is 2");
        assertEquals(1, this.linkRemovedCount, "link removed event count is 1");
        assertEquals(3, this.nodeAddedCount, "node add event count is 3");
        assertEquals(1, this.nodeRemovedCount, "node removed event count is 1");
        assertEquals(2, this.gtuAddedCount, "GTU add event count is 2");
        assertEquals(0, this.gtuRemovedCount, "GTU removed event count is 0");
        assertEquals(gtu1, network.getGTU("gtu1"), "gtu1 can be retrieved");
        assertEquals(gtu2, network.getGTU("gtu2"), "gtu2 can be retrieved");
        network.removeGTU(gtu1);
        assertEquals(2, this.linkAddedCount, "link add event count is 2");
        assertEquals(1, this.linkRemovedCount, "link removed event count is 1");
        assertEquals(3, this.nodeAddedCount, "node add event count is 3");
        assertEquals(1, this.nodeRemovedCount, "node removed event count is 1");
        assertEquals(2, this.gtuAddedCount, "GTU add event count is 2");
        assertEquals(1, this.gtuRemovedCount, "GTU removed event count is 1");
        network.removeGTU(gtu2);
        assertEquals(2, this.linkAddedCount, "link add event count is 2");
        assertEquals(1, this.linkRemovedCount, "link removed event count is 1");
        assertEquals(3, this.nodeAddedCount, "node add event count is 3");
        assertEquals(1, this.nodeRemovedCount, "node removed event count is 1");
        assertEquals(2, this.gtuAddedCount, "GTU add event count is 2");
        assertEquals(2, this.gtuRemovedCount, "GTU removed event count is 2");
        assertNull(network.getGTU("gtu1"), "gtu1 can no longer be retrieved");
        assertNull(network.getGTU("gtu2"), "gtu2 can no longer be retrieved");
        assertTrue(network.toString().contains(network.getId()), "toString contains the name of the network");
    }

    /**
     * Test the getExtent method of Network.
     * @throws NetworkException if that happens uncaught, this test has failed
     */
    @Test
    public final void testExtent() throws NetworkException
    {
        Network network = new Network("test", MockSimulator.createMock());
        Rectangle2D extent = network.getExtent();
        assertEquals(-500, extent.getMinX(), 0, "extend left");
        assertEquals(-500, extent.getMinY(), 0, "extend bottom");
        assertEquals(500, extent.getMaxX(), 0, "extend right");
        assertEquals(500, extent.getMaxY(), 0, "extend top");

        // Add one node (node has a bounding circle with radius 1), and there is an EXTENT_MARGIN of 0.05 in this case
        new Node(network, "node1", new Point2d(10, 20));
        extent = network.getExtent();
        assertEquals(10.0 - 1.0 - 0.05, extent.getMinX(), 0.01, "extend left");
        assertEquals(20.0 - 1.0 - 0.05, extent.getMinY(), 0.01, "extend bottom");
        assertEquals(10.0 + 1.0 + 0.05, extent.getMaxX(), 0.01, "extend right");
        assertEquals(20.0 + 1.0 + 0.05, extent.getMaxY(), 0.01, "extend top");

        // Add another node (node has a bounding circle with radius 1, so diameter 2)
        new Node(network, "node2", new Point2d(110, 220));
        extent = network.getExtent();
        double xMargin = 102 * Network.EXTENT_MARGIN / 2;
        double yMargin = 202 * Network.EXTENT_MARGIN / 2;
        assertEquals(10.0 - 1.0 - xMargin, extent.getMinX(), 0.01, "extend left");
        assertEquals(20.0 - 1.0 - yMargin, extent.getMinY(), 0.01, "extend bottom");
        assertEquals(110.0 + 1.0 + xMargin, extent.getMaxX(), 0.01, "extend right");
        assertEquals(220.0 + 1.0 + yMargin, extent.getMaxY(), 0.01, "extend top");
    }

    /** {@inheritDoc} */
    @Override
    public final void notify(final Event event) throws RemoteException
    {
        EventType type = event.getType();
        if (type.equals(Network.NODE_ADD_EVENT))
        {
            this.nodeAddedCount++;
        }
        else if (type.equals(Network.NODE_REMOVE_EVENT))
        {
            this.nodeRemovedCount++;
        }
        else if (type.equals(Network.LINK_ADD_EVENT))
        {
            this.linkAddedCount++;
        }
        else if (type.equals(Network.LINK_REMOVE_EVENT))
        {
            this.linkRemovedCount++;
        }
        else if (type.equals(Network.GTU_ADD_EVENT))
        {
            this.gtuAddedCount++;
        }
        else if (type.equals(Network.GTU_REMOVE_EVENT))
        {
            this.gtuRemovedCount++;
        }
        else
        {
            this.otherEventCount++;
        }
    }

    /**
     * Test the route map stuff.
     * @throws NetworkException if that happens uncaught; this test has failed
     * @throws OtsGeometryException if that happens uncaught; this test has failed
     */
    @Test
    public final void testRouteMap() throws NetworkException, OtsGeometryException
    {
        Network network = new Network("Route map test network", MockSimulator.createMock());
        Node node1 = new Node(network, "node1", new Point2d(10, 20));
        Node node2 = new Node(network, "node2", new Point2d(110, 20));
        List<Node> nodeList = new ArrayList<>();
        nodeList.add(node1);
        nodeList.add(node2);
        GtuType carType = new GtuType("car", DefaultsNl.CAR);
        GtuType bicycleType = new GtuType("bicycle", DefaultsNl.BICYCLE);
        new Link(network, "Link12", node1, node2, DefaultsNl.ROAD, new OtsLine2d(node1.getPoint(), node2.getPoint()), null);
        Route route1 = new Route("route1", carType, nodeList);
        Route route2 = new Route("route2", carType);
        Route route3 = new Route("route3", bicycleType);
        // The next test makes little sense until the getters are changed to search up to the GtuType root.
        assertEquals(0, network.getDefinedRouteMap(DefaultsNl.VEHICLE).size(), "initially the network has 0 routes");
        network.addRoute(carType, route1);
        assertEquals(1, network.getDefinedRouteMap(carType).size(), "list for carType contains one entry");
        assertEquals(route1, network.getRoute(carType, "route1"), "route for carType route1 is route1");
        assertNull(network.getRoute(bicycleType, "route1"), "route for bycicleType route1 is null");
        assertEquals(0, network.getDefinedRouteMap(bicycleType).size(), "list for bicycleType contains 0 routes");
        network.addRoute(carType, route2);
        network.addRoute(bicycleType, route3);
        assertEquals(2, network.getDefinedRouteMap(carType).size(), "list for carType contains two entries");
        assertEquals(1, network.getDefinedRouteMap(bicycleType).size(), "list for bicycleType contains one entry");
        assertEquals(route1, network.getRoute(carType, "route1"), "route for carType route1 is route1");
        assertEquals(route2, network.getRoute(carType, "route2"), "route for carType route2 is route2");
        assertEquals(route3, network.getRoute(bicycleType, "route3"), "route for bicycle route3 is route3");
        assertNull(network.getRoute(bicycleType, "route1"), "route for bicycle route1 is null");
        try
        {
            network.addRoute(carType, route2);
            fail("adding route again should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        Network otherNetwork = new Network("other Route map test network", MockSimulator.createMock());
        Node badNode = new Node(otherNetwork, "nodeInOtherNetwork", new Point2d(100, 200));
        List<Node> badNodeList = new ArrayList<>();
        badNodeList.add(node1);
        badNodeList.add(node2);
        badNodeList.add(badNode);
        try
        {
            Route badRoute = new Route("badRoute", carType, badNodeList);
            fail("creating a route with a node that is not in the network should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            network.removeRoute(bicycleType, route1);
            fail("attempt to remove a route that is not defined for this GtuType should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        assertEquals(1, network.getRoutesBetween(carType, node1, node2).size(),
                "there is one route from node1 to node2 for carType");
        assertEquals(route1, network.getRoutesBetween(carType, node1, node2).iterator().next(),
                "the one route from node1 to node2 is route1");
        assertEquals(0, network.getRoutesBetween(bicycleType, node1, node2).size(),
                "there are no routes from node1 to node2 for bicycleType");
        assertEquals(0, network.getRoutesBetween(carType, node2, node1).size(),
                "there are no routes from node2 to node1 for carTypecleType");
        assertEquals(0, network.getRoutesBetween(carType, node1, node1).size(),
                "there are no routes from node1 to node1 for carTypecleType");
        GtuType junkType = new GtuType("junk", DefaultsNl.VEHICLE);
        assertEquals(0, network.getRoutesBetween(junkType, node1, node2).size(),
                "there are no routes from node1 to node2 for badType");
        network.removeRoute(carType, route1);
        assertEquals(1, network.getDefinedRouteMap(carType).size(), "list for carType now contains one entry");
        assertEquals(1, network.getDefinedRouteMap(bicycleType).size(), "list for bicycleType contains one entry");
        assertNull(network.getRoute(carType, "route1"), "route for carType route1 is null");
        assertEquals(route2, network.getRoute(carType, "route2"), "route for carType route2 is route2");
        assertEquals(route3, network.getRoute(bicycleType, "route3"), "route for bicycle route3 is route3");
        assertTrue(network.containsRoute(carType, route2), "network contains route2 for carType");
        assertFalse(network.containsRoute(carType, route1), "network does not contain route1 for carType");
        assertTrue(network.containsRoute(carType, "route2"), "network contains route with name route2 for carType");
        assertFalse(network.containsRoute(carType, "route1"), "network does not contain route with name route1 for carType");
        assertFalse(network.containsRoute(junkType, "route1"), "network does not contain route with name route1 for junkType");
    }

    /**
     * Test the shortest path functionality.
     * @throws NetworkException if that happens uncaught; this test has failed
     * @throws OtsGeometryException if that happens uncaught; this test has failed
     */
    @Test
    public final void testShortestPathBiDirectional() throws NetworkException, OtsGeometryException
    {
        Network network = new Network("shortest path test network", MockSimulator.createMock());
        List<Node> nodes = createRingNodesAndLinks(network);
        int maxNode = nodes.size();
        for (int skip = 1; skip < maxNode / 2; skip++)
        {
            for (int fromNodeIndex = 0; fromNodeIndex < maxNode; fromNodeIndex++)
            {
                Node fromNode = nodes.get(fromNodeIndex);
                Node toNode = nodes.get((fromNodeIndex + skip) % maxNode);
                Route route = network.getShortestRouteBetween(DefaultsNl.VEHICLE, fromNode, toNode);
                assertEquals(skip + 1, route.size(), "route size is skip + 1");
                for (int i = 0; i < route.size(); i++)
                {
                    assertEquals(nodes.get((fromNodeIndex + i) % maxNode), route.getNode(i),
                            "node in route at position i should match");
                }
                Route routeWithExplicitLengthWeight =
                        network.getShortestRouteBetween(DefaultsNl.VEHICLE, fromNode, toNode, LinkWeight.LENGTH);
                assertEquals(route, routeWithExplicitLengthWeight, "route with explicit link weight should be the same");
                // reverse direction
                route = network.getShortestRouteBetween(DefaultsNl.VEHICLE, toNode, fromNode);
                // System.out.println("Shortest route from " + toNode + " to " + fromNode + " is " + route);
                assertEquals(10 - skip + 1, route.size(), "route size is 10 - skip + 1");
            }
        }
    }

    /**
     * Test the shortest path functionality.
     * @throws NetworkException if that happens uncaught; this test has failed
     * @throws OtsGeometryException if that happens uncaught; this test has failed
     */
    @Test
    public final void testShortestPathClockWise() throws NetworkException, OtsGeometryException
    {
        Network network = new Network("shortest path test network", MockSimulator.createMock());
        List<Node> nodes = createRingNodesAndLinks(network);
        int maxNode = nodes.size();
        for (int skip = 1; skip < maxNode; skip++)
        {
            for (int fromNodeIndex = 0; fromNodeIndex < maxNode; fromNodeIndex++)
            {
                Node fromNode = nodes.get(fromNodeIndex);
                Node toNode = nodes.get((fromNodeIndex + skip) % maxNode);
                Route route = network.getShortestRouteBetween(DefaultsNl.VEHICLE, fromNode, toNode);
                assertEquals(skip + 1, route.size(), "route size is skip + 1");
                for (int i = 0; i < route.size(); i++)
                {
                    assertEquals(nodes.get((fromNodeIndex + i) % maxNode), route.getNode(i),
                            "node in route at position i should match");
                }
                // reverse direction
                route = network.getShortestRouteBetween(DefaultsNl.VEHICLE, toNode, fromNode);
                // System.out.println("Shortest route from " + toNode + " to " + fromNode + " is " + route);
                assertEquals(maxNode - skip + 1, route.size(), "route size is maxNode - skip + 1");
                for (int i = 0; i < route.size(); i++)
                {
                    assertEquals(nodes.get((fromNodeIndex + skip + i) % maxNode), route.getNode(i),
                            "node in route at position i should match");
                }
            }
        }
    }

    /**
     * Test the shortest path method that takes a list of intermediate nodes.
     * @throws OtsGeometryException if that happens uncaught; this test has failed
     * @throws NetworkException if that happens uncaught; this test has failed
     */
    @Test
    public final void testShortestPathWithIntermediateNodes() throws NetworkException, OtsGeometryException
    {
        Network network = new Network("shortest path test network", MockSimulator.createMock());
        List<Node> nodes = createRingNodesAndLinks(network, 5);
        int maxNode = nodes.size();
        for (int fromNodeIndex = 0; fromNodeIndex < maxNode; fromNodeIndex++)
        {
            Node fromNode = network.getNode("node" + fromNodeIndex);
            for (int intermediateNodes = 0; intermediateNodes <= 2; intermediateNodes++)
            {
                // Because the number of nodes is odd, and they are evenly spread out; there is never a tie
                int numPaths = (int) Math.pow(maxNode - 1, intermediateNodes);
                for (int path = 0; path < numPaths; path++)
                {
                    List<Node> viaNodes = new ArrayList<>();
                    int prevNodeIndex = fromNodeIndex;
                    int pathNumber = path;
                    for (int step = 0; step < intermediateNodes; step++)
                    {
                        int nextNodeIndex = pathNumber % (maxNode - 1);
                        if (nextNodeIndex >= prevNodeIndex)
                        {
                            nextNodeIndex = (nextNodeIndex + 1) % maxNode;
                        }
                        viaNodes.add(network.getNode("node" + nextNodeIndex));
                        prevNodeIndex = nextNodeIndex;
                        pathNumber /= (maxNode - 1);
                    }
                    for (int toNodeIndex = 0; toNodeIndex < maxNode; toNodeIndex++)
                    {
                        if (prevNodeIndex == toNodeIndex)
                        {
                            continue;
                        }
                        // System.out.print("Path " + path + " from " + fromNodeIndex + " to " + toNodeIndex + " visits");
                        // for (Node node : viaNodes)
                        // {
                        // System.out.print(" " + node.getId());
                        // }
                        // System.out.println("");
                        Node toNode = network.getNode("node" + toNodeIndex);
                        Route route = network.getShortestRouteBetween(DefaultsNl.VEHICLE, fromNode, toNode, viaNodes);
                        // Now compute the expected path using our knowledge about the structure
                        List<Node> expectedPath = new ArrayList<>();
                        expectedPath.add(fromNode);
                        viaNodes.add(network.getNode("node" + toNodeIndex));
                        int from = fromNodeIndex;
                        for (int positionInPlan = 0; positionInPlan < viaNodes.size(); positionInPlan++)
                        {
                            Node nextNode = viaNodes.get(positionInPlan);
                            int to = Integer.parseInt(nextNode.getId().substring(4));
                            int distance = (to + maxNode - from) % maxNode;
                            if (distance > maxNode / 2)
                            {
                                distance -= maxNode;
                            }
                            boolean clockWise = true;
                            while (from != to)
                            {
                                from = (from + (clockWise ? 1 : maxNode - 1)) % maxNode;
                                expectedPath.add(network.getNode("node" + from));
                            }
                        }
                        assertEquals(expectedPath.size(), route.size(), "expected path should have same length as route");
                        for (int i = 0; i < expectedPath.size(); i++)
                        {
                            assertEquals(expectedPath.get(i), route.getNode(i), "node i should match");
                        }
                        route = network.getShortestRouteBetween(DefaultsNl.VEHICLE, fromNode, toNode, viaNodes);
                        Route routeWithExplicitLengthAsWeight = network.getShortestRouteBetween(DefaultsNl.VEHICLE, fromNode,
                                toNode, viaNodes, LinkWeight.LENGTH);
                        assertEquals(route, routeWithExplicitLengthAsWeight,
                                "route with explicit weight should be same as route");
                    }
                }
            }
        }
    }

    /**
     * Construct a ring of 10 nodes with links in clockwise fashion.
     * @param network the network that will contain the nodes
     * @return the constructed nodes (in clockwise order)
     * @throws NetworkException if that happens uncaught; this test has failed
     * @throws OtsGeometryException if that happens uncaught; this test has failed
     */
    private List<Node> createRingNodesAndLinks(final Network network) throws NetworkException, OtsGeometryException
    {
        return createRingNodesAndLinks(network, 10);
    }

    /**
     * Construct a ring of nodes with links in clockwise fashion.
     * @param network the network that will contain the nodes
     * @param maxNode number of nodes on the ring
     * @return the constructed nodes (in clockwise order)
     * @throws NetworkException if that happens uncaught; this test has failed
     * @throws OtsGeometryException if that happens uncaught; this test has failed
     */
    private List<Node> createRingNodesAndLinks(final Network network, final int maxNode)
            throws NetworkException, OtsGeometryException
    {
        LinkType linkType = new LinkType("linkType", null);
        linkType.addCompatibleGtuType(DefaultsNl.ROAD_USER);
        List<Node> nodes = new ArrayList<>();
        double radius = 500;
        double centerX = 0;
        double centerY = 0;
        for (int i = 0; i < maxNode; i++)
        {
            double angle = i * Math.PI * 2 / maxNode;
            nodes.add(new Node(network, "node" + i,
                    new Point2d(centerX + radius * Math.cos(angle), centerY + radius * Math.sin(angle))));
        }
        // Create bi-directional links between all adjacent nodes
        Node prevNode = nodes.get(maxNode - 1);
        for (Node node : nodes)
        {
            new Link(network, "from " + prevNode.getId() + " to " + node.getId(), prevNode, node, linkType,
                    new OtsLine2d(prevNode.getPoint(), node.getPoint()), null);
            prevNode = node;
        }
        return nodes;
    }

    /**
     * Tests whether the A* algorithm delivers the same shortest path as Dijkstra.
     * @throws OtsGeometryException on error
     * @throws NetworkException on error
     */
    @Test
    public void testAStar() throws NetworkException, OtsGeometryException
    {
        boolean showTime = false; // not part of formal test, set to true for benchmarking
        long totalTimeDijkstra = 0;
        long totalTimeAStar = 0;
        int gridSize = 20;
        double sigma = 1.0;
        double sigmaLim = 0.4;
        int trials = 100;
        for (int i = 0; i < trials; i++)
        {
            Network network = new Network("shortest path test network", MockSimulator.createMock());
            Node[] od = randomTestNetwork(network, gridSize, sigma, sigmaLim);
            // this first call triggers the graph to be constructed, which should not be part of timing an algorithm
            if (showTime)
            {
                network.getShortestRouteBetween(DefaultsNl.VEHICLE, od[0], od[1], LinkWeight.LENGTH_NO_CONNECTORS);
            }
            long t1 = System.currentTimeMillis();
            Route dijkstra = network.getShortestRouteBetween(DefaultsNl.VEHICLE, od[0], od[1], LinkWeight.LENGTH_NO_CONNECTORS);
            long t2 = System.currentTimeMillis();
            Route aStar =
                    network.getShortestRouteBetween(DefaultsNl.VEHICLE, od[0], od[1], LinkWeight.ASTAR_LENGTH_NO_CONNECTORS);
            long t3 = System.currentTimeMillis();
            totalTimeDijkstra += (t2 - t1);
            totalTimeAStar += (t3 - t2);
            assertEquals(routeLength(dijkstra), routeLength(aStar), 0.001, "A* gave different shortest path from Dijkstra.");
        }
        double percentage = 100.0 * totalTimeAStar / totalTimeDijkstra;
        if (showTime)
        {
            System.out.println("Dijkstra took a total of " + totalTimeDijkstra + "ms.");
            System.out.println("A* took a total of " + totalTimeAStar + "ms, which is " + percentage + "%.");
        }
    }

    /**
     * Creates a random grid network, where each node is randomly located with a 'cell' surrounding it. These cells do not
     * overlap, guaranteeing a logical network, but with random lengths. The origin will be roughly in the middle, while the
     * destination will by roughly in the middle of the upper-right quadrant. The grid will have an overall spacing of 10m.
     * @param network network.
     * @param gridSize the network will consist of gridSize x gridSize nodes.
     * @param sigma Gaussian standard deviation for node location, assuming a unit grid with a spacing of 1.
     * @param sigmaLim limits the random location between -sigmaLim and sigmaLim around its regular grid point.
     * @return origin (at index 0) and destination (at index 1) to use.
     * @throws NetworkException
     * @throws OtsGeometryException
     */
    private Node[] randomTestNetwork(final Network network, final int gridSize, final double sigma, final double sigmaLim)
            throws NetworkException, OtsGeometryException
    {
        double originLocation = (gridSize - 1.0) * 0.5;
        double destinationLocation = (gridSize - 1.0) * 0.75;

        int nodeNumber = 1;
        Random r = new Random();
        Node[] returnOriginDestination = new Node[2];
        for (int i = 0; i < gridSize; i++)
        {
            double y = 10.0 * (Math.max(Math.min(sigma * r.nextGaussian(), sigmaLim), -sigmaLim) + i);
            for (int j = 0; j < gridSize; j++)
            {
                double x = 10.0 * (Math.max(Math.min(sigma * r.nextGaussian(), sigmaLim), -sigmaLim) + j);
                Point2d point = new Point2d(x, y);
                Node node = new Node(network, "Node " + nodeNumber, point);

                // origin-destination
                if (returnOriginDestination[0] == null && i > originLocation && j > originLocation)
                {
                    returnOriginDestination[0] = node;
                }
                if (returnOriginDestination[1] == null && i > destinationLocation && j > destinationLocation)
                {
                    returnOriginDestination[1] = node;
                }

                // create links
                if (j > 0)
                {
                    Node up = network.getNode("Node " + (nodeNumber - 1));
                    String id = "Link " + (nodeNumber - 1) + "-" + nodeNumber;
                    OtsLine2d designLine = new OtsLine2d(up.getPoint(), node.getPoint());
                    new Link(network, id, up, node, DefaultsNl.RURAL, designLine, null);
                    id = "Link " + nodeNumber + "-" + (nodeNumber - 1);
                    designLine = new OtsLine2d(node.getPoint(), up.getPoint());
                    new Link(network, id, node, up, DefaultsNl.RURAL, designLine, null);
                }
                if (i > 0)
                {
                    Node left = network.getNode("Node " + (nodeNumber - gridSize));
                    String id = "Link " + (nodeNumber - gridSize) + "-" + nodeNumber;
                    OtsLine2d designLine = new OtsLine2d(left.getPoint(), node.getPoint());
                    new Link(network, id, left, node, DefaultsNl.RURAL, designLine, null);
                    id = "Link " + nodeNumber + "-" + (nodeNumber - gridSize);
                    designLine = new OtsLine2d(node.getPoint(), left.getPoint());
                    new Link(network, id, node, left, DefaultsNl.RURAL, designLine, null);
                }

                nodeNumber++;
            }
        }
        return returnOriginDestination;
    }

    /**
     * Calculates length of the route.
     * @param route route.
     * @return route length.
     * @throws NetworkException
     */
    private double routeLength(final Route route) throws NetworkException
    {
        double length = 0.0;
        for (int i = 0; i < route.size(); i++)
        {
            if (i > 0)
            {
                length += route.getNode(i - 1).getPoint().distance(route.getNode(i).getPoint());
            }
        }
        return length;
    }

}
