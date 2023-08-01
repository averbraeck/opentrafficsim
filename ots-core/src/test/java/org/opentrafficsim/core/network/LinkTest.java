package org.opentrafficsim.core.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.EventType;
import org.junit.Test;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine3d;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.mock.MockGtu;
import org.opentrafficsim.core.mock.MockSimulator;

/**
 * Test the Link class.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class LinkTest implements EventListener
{
    /** ... */
    private static final long serialVersionUID = 1L;

    /** Count GTU_ADD events. */
    private int gtuAddedCount = 0;

    /** Count GTU_REMOVE events. */
    private int gtuRemovedCount = 0;

    /** Count other events. */
    private int otherEventCount = 0;

    /**
     * Test the OTSLink class.
     * @throws NetworkException should not happen uncaught in this test
     * @throws OtsGeometryException should not happen uncaught in this test
     */
    @Test
    public final void testOTSLink() throws NetworkException, OtsGeometryException
    {
        Network network = new Network("OTSLinkTestNetwork", MockSimulator.createMock());
        Node startNode = new Node(network, "start", new Point2d(10, 20));
        Node endNode = new Node(network, "end", new Point2d(1000, 2000));
        LinkType linkType = new LinkType("myLinkType", DefaultsNl.ROAD);
        OtsLine3d designLine = new OtsLine3d(startNode.getPoint(), endNode.getPoint());
        Link link = new Link(network, "link1", startNode, endNode, linkType, designLine);
        assertTrue("network contains the newly constructed link", network.containsLink(link));
        // directionalityMap is currently empty
        assertEquals("The link contains no GTUs", 0, link.getGTUCount());
        assertEquals("The link contains zero GTUs", 0, link.getGTUs().size());

        link.addListener(this, Link.GTU_ADD_EVENT);
        link.addListener(this, Link.GTU_REMOVE_EVENT);
        assertEquals("add counter is 0", 0, this.gtuAddedCount);
        assertEquals("remove counter is 0", 0, this.gtuRemovedCount);
        assertEquals("other event counter is 0", 0, this.otherEventCount);
        MockGtu mockGtu1 = new MockGtu("gtu1");
        Gtu gtu1 = mockGtu1.getMock();
        MockGtu mockGtu2 = new MockGtu("gtu2");
        Gtu gtu2 = mockGtu2.getMock();
        link.addGTU(gtu1);
        assertEquals("add counter is now 1", 1, this.gtuAddedCount);
        assertEquals("remove counter is 0", 0, this.gtuRemovedCount);
        assertEquals("other event counter is 0", 0, this.otherEventCount);
        assertEquals("The link contains one GTU", 1, link.getGTUCount());
        assertEquals("The link contains one GTU", 1, link.getGTUs().size());
        assertEquals("The link contains our GTU", gtu1, link.getGTUs().iterator().next());
        link.addGTU(gtu2);
        assertEquals("add counter is now 2", 2, this.gtuAddedCount);
        assertEquals("remove counter is 0", 0, this.gtuRemovedCount);
        assertEquals("other event counter is 0", 0, this.otherEventCount);
        assertTrue("The link contains gtu1", link.getGTUs().contains(gtu1));
        assertTrue("The link contains gtu2", link.getGTUs().contains(gtu2));
        link.addGTU(gtu1); // Add gtu again (should make no difference)
        assertEquals("add counter is now 2", 2, this.gtuAddedCount);
        assertEquals("remove counter is 0", 0, this.gtuRemovedCount);
        assertEquals("other event counter is 0", 0, this.otherEventCount);
        assertTrue("The link contains gtu1", link.getGTUs().contains(gtu1));
        assertTrue("The link contains gtu2", link.getGTUs().contains(gtu2));
        link.removeGTU(gtu1);
        assertEquals("add counter is now 2", 2, this.gtuAddedCount);
        assertEquals("remove counter is 1", 1, this.gtuRemovedCount);
        assertEquals("other event counter is 0", 0, this.otherEventCount);
        assertFalse("The link no longer contains gtu1", link.getGTUs().contains(gtu1));
        assertTrue("The link contains gtu2", link.getGTUs().contains(gtu2));
        link.removeGTU(gtu1); // removing it again has no effect
        assertEquals("add counter is now 2", 2, this.gtuAddedCount);
        assertEquals("remove counter is 1", 1, this.gtuRemovedCount);
        assertEquals("other event counter is 0", 0, this.otherEventCount);
        assertFalse("The link no longer contains gtu1", link.getGTUs().contains(gtu1));
        assertTrue("The link contains gtu2", link.getGTUs().contains(gtu2));
        link.removeGTU(gtu2);
        assertEquals("add counter is now 2", 2, this.gtuAddedCount);
        assertEquals("remove counter is 2", 2, this.gtuRemovedCount);
        assertEquals("other event counter is 0", 0, this.otherEventCount);
        assertFalse("The link no longer contains gtu1", link.getGTUs().contains(gtu1));
        assertFalse("The link no longer contains gtu2", link.getGTUs().contains(gtu2));
        assertEquals("Network is correctly returned", network, link.getNetwork());
        assertEquals("LinkType is correctly returned", linkType, link.getType());
        OrientedPoint2d location = link.getLocation();
        OrientedPoint2d expectedLocation = designLine.getLocationFraction(0.5);
        assertEquals("location is at halfway point of design line (because design line contains only two points)", 0,
                expectedLocation.distance(location), 0.1);
        // RotZ of location is bogus; makes no sense to test that
        Bounds bounds = link.getBounds();
        assertNotNull("bounds should not be null", bounds);
        assertFalse("link is not equal to null", link.equals(null));
        assertFalse("link is not equal to some other object", link.equals("Hello World!"));
        // Make another link to test the rest of equals
        Link otherLink = new Link(network, "link5", startNode, endNode, linkType, designLine);
        assertFalse("link is not equal to extremely similar link with different id", link.equals(otherLink));
        // make a link with the same name in another network
        Network otherNetwork = new Network("other", MockSimulator.createMock());
        linkType = new LinkType("myLinkType", DefaultsNl.ROAD);
        otherLink = new Link(otherNetwork, "link1", new Node(otherNetwork, "start", new Point2d(10, 20)),
                new Node(otherNetwork, "end", new Point2d(1000, 2000)), linkType, designLine);
        assertTrue("link is equal to extremely similar link with same id but different network", link.equals(otherLink));
    }

    /** {@inheritDoc} */
    @Override
    public final void notify(final Event event) throws RemoteException
    {
        EventType eventType = event.getType();
        if (eventType.equals(Link.GTU_ADD_EVENT))
        {
            this.gtuAddedCount++;
        }
        else if (eventType.equals(Link.GTU_REMOVE_EVENT))
        {
            this.gtuRemovedCount++;
        }
        else
        {
            System.err.println("unhandled event is " + event);
            this.otherEventCount++;
        }
    }

}
