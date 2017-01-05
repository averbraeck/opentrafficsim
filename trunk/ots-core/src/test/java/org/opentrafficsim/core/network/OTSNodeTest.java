package org.opentrafficsim.core.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.AngleUnit;
import org.djunits.value.vdouble.scalar.Direction;
import org.junit.Test;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUType;

/**
 * Test the OTSNode class.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
        Network network = new OTSNetwork("Node test network");
        Direction direction = new Direction(2.3, AngleUnit.RADIAN);
        Direction slope = new Direction(10, AngleUnit.PERCENT);
        OTSPoint3D point1 = new OTSPoint3D(20, 40, 60);
        OTSNode node1 = new OTSNode(network, "node 1", point1, direction, slope);
        assertEquals("network matches", network, node1.getNetwork());
        assertEquals("name matches", "node 1", node1.getId());
        assertEquals("point matches", point1, node1.getPoint());
        assertEquals("direction matches", direction, node1.getDirection());
        assertEquals("slope matches", slope, node1.getSlope());
        assertEquals("getLocation", new DirectedPoint(point1.x, point1.y, point1.z), node1.getLocation());
        assertTrue("name is in toString", node1.toString().contains(node1.getId()));
        OTSPoint3D point2 = new OTSPoint3D(120, 240, 60);
        OTSNode node2 = new OTSNode(network, "node 2", point2);
        assertEquals("network matches", network, node2.getNetwork());
        assertEquals("name matches", "node 2", node2.getId());
        assertEquals("point matches", point2, node2.getPoint());
        assertEquals("direction matches", Direction.ZERO, node2.getDirection());
        assertEquals("slope matches", Direction.ZERO, node2.getSlope());
        assertTrue("Node 1 matches itself", node1.equals(node1));
        assertFalse("Node 1 does not match null", node1.equals(null));
        assertFalse("Node 1 does not match some String", node1.equals("Hello World!"));
        assertFalse("Node 1 does not match node 2", node1.equals(node2));
        // Create another node with name node 1 in another network
        Network otherNetwork = new OTSNetwork("Node test network 2");
        OTSNode node3 = new OTSNode(otherNetwork, "node 1", point1);
        assertTrue("Node 1 does match node 3 in other network", node1.equals(node3));

        assertEquals("node 1 has no links", 0, node1.getLinks().size());
        // Create a couple of links
        Link link1 =
                new OTSLink(network, "link 1", node1, node2, LinkType.ALL, new OTSLine3D(node1.getPoint(), node2.getPoint()),
                        LongitudinalDirectionality.DIR_BOTH);
        assertEquals("node 1 has one link", 1, node1.getLinks().size());
        assertEquals("node 2 has one link", 1, node2.getLinks().size());
        assertEquals("link at node 1 is link1", link1, node1.getLinks().iterator().next());
        assertEquals("link at node 2 is link1", link1, node2.getLinks().iterator().next());
        OTSNode node4 = new OTSNode(network, "node 3", new OTSPoint3D(10, 10, 10));
        Link link2 =
                new OTSLink(network, "link 2", node1, node4, LinkType.ALL, new OTSLine3D(node1.getPoint(), node4.getPoint()),
                        LongitudinalDirectionality.DIR_BOTH);
        Link link3 =
                new OTSLink(network, "link 3", node4, node2, LinkType.ALL, new OTSLine3D(node4.getPoint(), node2.getPoint()),
                        LongitudinalDirectionality.DIR_BOTH);
        Link link4 =
                new OTSLink(network, "link 4", node2, node1, LinkType.ALL, new OTSLine3D(node2.getPoint(), node1.getPoint()),
                        LongitudinalDirectionality.DIR_BOTH);
        assertEquals("node 1 has three links", 3, node1.getLinks().size());
        assertEquals("node 2 has three links", 3, node2.getLinks().size());
        assertEquals("node 4 has two links", 2, node4.getLinks().size());
        assertTrue("node 1 has link 1", node1.getLinks().contains(link1));
        assertTrue("node 1 has link 2", node1.getLinks().contains(link2));
        assertTrue("node 1 has link 4", node1.getLinks().contains(link4));
        assertFalse("node 1 does not have link 3", node1.getLinks().contains(link3));
        Set<Link> nextLinks = node1.nextLinks(GTUType.ALL, link4);
        assertEquals("incoming over link 4, node 1 has two next links", 2, nextLinks.size());
        assertTrue("incoming over link 4, next links of node 1 contains link 1", nextLinks.contains(link1));
        assertTrue("incoming over link 4, next links of node 1 contains link 1", nextLinks.contains(link1));
        try
        {
            node1.nextLinks(GTUType.ALL, link3);
            fail("nextLinks from link that does not connect to the node should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        assertTrue("node 1 has direct connection to node 2", node1.isDirectionallyConnectedTo(GTUType.ALL, node2));
        Node node5 = new OTSNode(network, "node 5", new OTSPoint3D(1000, 0, 0));
        assertFalse("node 1 has no direct connection to node 5", node1.isDirectionallyConnectedTo(GTUType.ALL, node5));
        Link link5 =
                new OTSLink(network, "link 5", node1, node5, LinkType.ALL, new OTSLine3D(node1.getPoint(), node5.getPoint()),
                        LongitudinalDirectionality.DIR_MINUS);
        assertFalse("node 1 still has no direct connection to node 5", node1.isDirectionallyConnectedTo(GTUType.ALL, node5));
        assertTrue("node 5 does have a direct connection to node 1", node5.isDirectionallyConnectedTo(GTUType.ALL, node1));
        assertEquals("Connection from node 5 to node 1 is link5", link5, node5.getLinks().iterator().next());
        Point3d pt = new Point3d();
        Bounds b = node1.getBounds();
        BoundingSphere bs = (BoundingSphere) b;
        bs.getCenter(pt);
        assertEquals("center of bounding sphere of node 1 is origin", 0,
                new OTSPoint3D(0, 0, 0).distance(new OTSPoint3D(pt)).si, 0.001);
        assertEquals("radius of bounding sphere of node 1 is 10m", 10, bs.getRadius(), 00001);
    }
}
