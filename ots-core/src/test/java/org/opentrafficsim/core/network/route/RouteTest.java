package org.opentrafficsim.core.network.route;

import org.junit.Test;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Test the Route class.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version 20 jan. 2015 <br>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class RouteTest
{
    /**
     * Test the Route class.
     * @throws NetworkException on Network inconsistency (should not happen in this test)
     */
    @Test
    public void routeTest() throws NetworkException
    {
        /*- 
        
        TODO THE ROUTE CLASSES HAVE CHANGED SO THE TESTS HAVE TO BE ADAPTED
         
        Route route = new Route("name");
        assertEquals("No arguments constructor creates an empty Route", 0, route.size());
        try
        {
            route.getNode(0);
            fail("Should have thrown an Exception");
        }
        catch (NetworkException ne)
        {
            // ignore expected exception
        }
        try
        {
            route.originNode();
            fail("Should have thrown an Exception");
        }
        catch (NetworkException ne)
        {
            // ignore expected exception
        }
        try
        {
            route.destinationNode();
            fail("Should have thrown an Exception");
        }
        catch (NetworkException ne)
        {
            // ignore expected exception
        }
        assertEquals("lastVisitedNode should return null", null, route.lastVisitedNode());
        assertEquals("visitNextNode should return null", null, route.visitNextNode());
        assertEquals("nextNodeToVisit should return null", null, route.nextNodeToVisit());
        Node n1 = new Node("N1", new OtsPoint3d(12, 34));
        assertEquals("indexOf should return -1 for an empty Route", -1, route.indexOf(n1));
        try
        {
            route.addNode(1, n1);
            fail("addNode with out of range index should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            route.addNode(-1, n1);
            fail("addNode with negative index should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        route.addNode(n1);
        assertEquals("Route should now contain one Node", 1, route.size());
        assertEquals("Node 0 of route should be N1", n1, route.getNode(0));
        assertEquals("OriginNode should be N1", n1, route.originNode());
        assertEquals("indexOf N1 should return 0", 0, route.indexOf(n1));
        Node n2 = new Node("N2", new OtsPoint3d(56, 78));
        route.addNode(n2);
        assertEquals("Route should now contain two Nodes", 2, route.size());
        assertEquals("Node 0 of route should be N1", n1, route.getNode(0));
        assertEquals("Node 1 of route should be N2", n2, route.getNode(1));
        assertEquals("OriginNode should be N1", n1, route.originNode());
        assertEquals("indexOf N1 should return 0", 0, route.indexOf(n1));
        assertEquals("indexOf N2 should return 1", 1, route.indexOf(n2));
        try
        {
            route.getNode(-1);
            fail("Should have thrown an Exception");
        }
        catch (NetworkException ne)
        {
            // ignore expected exception
        }
        try
        {
            route.getNode(2);
            fail("Should have thrown an Exception");
        }
        catch (NetworkException ne)
        {
            // ignore expected exception
        }
        // System.out.println(route);
        assertEquals("nextNodeToVisit should be n1", n1, route.nextNodeToVisit());
        Node nextNode = route.visitNextNode();
        assertEquals("vistNextNode should have returned n1", n1, nextNode);
        assertEquals("lastVisitedNode should return n1", n1, route.lastVisitedNode());
        assertEquals("nextNodeToVisit should be n2", n2, route.nextNodeToVisit());
        assertEquals("lastVisitedNode should return n1", n1, route.lastVisitedNode());
        // Currently insertion before the "current" node is allowed and increments the internal lastNode index.
        Node n0 = new Node("n0", new OtsPoint3d(0, 0));
        route.addNode(0, n0);
        assertEquals("size should now be 3", 3, route.size());
        assertEquals("nextNodeToVisit should still be n2", n2, route.nextNodeToVisit());
        Node n3 = new Node("n3", new OtsPoint3d(0, 0));
        route.addNode(n3);
        assertEquals("size should now be 4", 4, route.size());
        assertEquals("destinationNode should now be n3", n3, route.destinationNode());
        assertEquals("nextNodeToVisit should still be n2", n2, route.nextNodeToVisit());
        nextNode = route.visitNextNode();
        assertEquals("vistNextNode should have returned n2", n2, nextNode);
        assertEquals("lastVisitedNode should return n2", n2, route.lastVisitedNode());
        nextNode = route.visitNextNode();
        assertEquals("vistNextNode should have returned n3", n3, nextNode);
        nextNode = route.visitNextNode();
        assertEquals("vistNextNode should have returned null", null, nextNode);
        
        List<Node> list = new ArrayList<Node>();
        list.add(n0);
        list.add(n1);
        list.add(n2);
        list.add(n3);
        route = new Route(list);
        assertEquals("Route element 0 is n0", n0, route.getNode(0));
        assertEquals("Route element 1 is n1", n1, route.getNode(1));
        assertEquals("Route element 2 is n2", n2, route.getNode(2));
        assertEquals("Route element 3 is n3", n3, route.getNode(3));
        assertEquals("OriginNode should be n0", n0, route.originNode());
        CrossSectionLink l01 =
                new CrossSectionLink("name", n0, n1, new Length(200,
                        METER));
        assertTrue("Route contains Link l01", route.containsLink(l01));
        CrossSectionLink l10 =
                new CrossSectionLink("name", n1, n0, new Length(200,
                        METER));
        assertFalse("Route contains Link l10", route.containsLink(l10));
        Node removedNode = route.removeNode(2);
        assertEquals("removeNode should return the removed node; i.c. n2", n2, removedNode);
        assertEquals("Route element 0 is n0", n0, route.getNode(0));
        assertEquals("Route element 1 is n1", n1, route.getNode(1));
        assertEquals("Route element 2 is n3", n3, route.getNode(2));
        assertEquals("OriginNode should be n0", n0, route.originNode());
        try
        {
            route.removeNode(-1);
            fail("Negative index should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            route.removeNode(4);
            fail("Too high index should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        // System.out.println(route);
        nextNode = route.visitNextNode();
        // System.out.println(route);
        assertEquals("visitNextNode should return n0", n0, nextNode);
        nextNode = route.visitNextNode();
        // System.out.println(route);
        assertEquals("visitNextNode should return n1", n1, nextNode);
        removedNode = route.removeNode(0);
        assertEquals("removed node should be n0", n0, removedNode);
        // System.out.println(route);
        try
        {
            route.removeNode(0);
            fail("Removing the last visited node on the route should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        
         */
    }

    // TODO write JUnit test for the suitability method (has to wait for the XML network lane parser).

}
