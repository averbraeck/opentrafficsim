package org.opentrafficsim.core.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.rmi.RemoteException;

import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.EventType;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.mock.MockGtu;
import org.opentrafficsim.core.mock.MockSimulator;

/**
 * Test the Link class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
     */
    @Test
    public final void testOTSLink() throws NetworkException
    {
        Network network = new Network("OTSLinkTestNetwork", MockSimulator.createMock());
        Node startNode = new Node(network, "start", new Point2d(10, 20));
        Node endNode = new Node(network, "end", new Point2d(1000, 2000));
        LinkType linkType = new LinkType("myLinkType", DefaultsNl.ROAD);
        OtsLine2d designLine = new OtsLine2d(startNode.getPoint(), endNode.getPoint());
        Link link = new Link(network, "link1", startNode, endNode, linkType, designLine, null);
        assertTrue(network.containsLink(link), "network contains the newly constructed link");
        // directionalityMap is currently empty
        assertEquals(0, link.getGTUCount(), "The link contains no GTUs");
        assertEquals(0, link.getGTUs().size(), "The link contains zero GTUs");

        link.addListener(this, Link.GTU_ADD_EVENT);
        link.addListener(this, Link.GTU_REMOVE_EVENT);
        assertEquals(0, this.gtuAddedCount, "add counter is 0");
        assertEquals(0, this.gtuRemovedCount, "remove counter is 0");
        assertEquals(0, this.otherEventCount, "other event counter is 0");
        MockGtu mockGtu1 = new MockGtu("gtu1");
        Gtu gtu1 = mockGtu1.getMock();
        MockGtu mockGtu2 = new MockGtu("gtu2");
        Gtu gtu2 = mockGtu2.getMock();
        link.addGTU(gtu1);
        assertEquals(1, this.gtuAddedCount, "add counter is now 1");
        assertEquals(0, this.gtuRemovedCount, "remove counter is 0");
        assertEquals(0, this.otherEventCount, "other event counter is 0");
        assertEquals(1, link.getGTUCount(), "The link contains one GTU");
        assertEquals(1, link.getGTUs().size(), "The link contains one GTU");
        assertEquals(gtu1, link.getGTUs().iterator().next(), "The link contains our GTU");
        link.addGTU(gtu2);
        assertEquals(2, this.gtuAddedCount, "add counter is now 2");
        assertEquals(0, this.gtuRemovedCount, "remove counter is 0");
        assertEquals(0, this.otherEventCount, "other event counter is 0");
        assertTrue(link.getGTUs().contains(gtu1), "The link contains gtu1");
        assertTrue(link.getGTUs().contains(gtu2), "The link contains gtu2");
        link.addGTU(gtu1); // Add gtu again (should make no difference)
        assertEquals(2, this.gtuAddedCount, "add counter is now 2");
        assertEquals(0, this.gtuRemovedCount, "remove counter is 0");
        assertEquals(0, this.otherEventCount, "other event counter is 0");
        assertTrue(link.getGTUs().contains(gtu1), "The link contains gtu1");
        assertTrue(link.getGTUs().contains(gtu2), "The link contains gtu2");
        link.removeGTU(gtu1);
        assertEquals(2, this.gtuAddedCount, "add counter is now 2");
        assertEquals(1, this.gtuRemovedCount, "remove counter is 1");
        assertEquals(0, this.otherEventCount, "other event counter is 0");
        assertFalse(link.getGTUs().contains(gtu1), "The link no longer contains gtu1");
        assertTrue(link.getGTUs().contains(gtu2), "The link contains gtu2");
        link.removeGTU(gtu1); // removing it again has no effect
        assertEquals(2, this.gtuAddedCount, "add counter is now 2");
        assertEquals(1, this.gtuRemovedCount, "remove counter is 1");
        assertEquals(0, this.otherEventCount, "other event counter is 0");
        assertFalse(link.getGTUs().contains(gtu1), "The link no longer contains gtu1");
        assertTrue(link.getGTUs().contains(gtu2), "The link contains gtu2");
        link.removeGTU(gtu2);
        assertEquals(2, this.gtuAddedCount, "add counter is now 2");
        assertEquals(2, this.gtuRemovedCount, "remove counter is 2");
        assertEquals(0, this.otherEventCount, "other event counter is 0");
        assertFalse(link.getGTUs().contains(gtu1), "The link no longer contains gtu1");
        assertFalse(link.getGTUs().contains(gtu2), "The link no longer contains gtu2");
        assertEquals(network, link.getNetwork(), "Network is correctly returned");
        assertEquals(linkType, link.getType(), "LinkType is correctly returned");
        Point2d location = link.getLocation();
        OrientedPoint2d expectedLocation = designLine.getLocationPointFraction(0.5);
        assertEquals(expectedLocation.distance(location), 0.0, 0.1,
                "location is at halfway point of design line (because design line contains only two points)");
        // RotZ of location is bogus; makes no sense to test that
        Bounds bounds = link.getBounds();
        assertNotNull(bounds, "bounds should not be null");
        assertFalse(link.equals(null), "link is not equal to null");
        assertFalse(link.equals("Hello World!"), "link is not equal to some other object");
        // Make another link to test the rest of equals
        Link otherLink = new Link(network, "link5", startNode, endNode, linkType, designLine, null);
        assertFalse(link.equals(otherLink), "link is not equal to extremely similar link with different id");
        // make a link with the same name in another network
        Network otherNetwork = new Network("other", MockSimulator.createMock());
        linkType = new LinkType("myLinkType", DefaultsNl.ROAD);
        otherLink = new Link(otherNetwork, "link1", new Node(otherNetwork, "start", new Point2d(10, 20)),
                new Node(otherNetwork, "end", new Point2d(1000, 2000)), linkType, designLine, null);
        assertTrue(link.equals(otherLink), "link is equal to extremely similar link with same id but different network");
    }

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
