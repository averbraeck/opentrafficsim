package org.opentrafficsim.core.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import javax.media.j3d.Bounds;

import org.djutils.event.EventInterface;
import org.djutils.event.EventListenerInterface;
import org.djutils.event.EventType;
import org.junit.Test;
import org.opentrafficsim.core.compatibility.GTUCompatibility;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.mock.MockGTU;
import org.opentrafficsim.core.mock.MockSimulator;

import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Test the OTSLink class.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jan 3, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class OTSLinkTest implements EventListenerInterface
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
     * @throws OTSGeometryException should not happen uncaught in this test
     */
    @Test
    public final void testOTSLink() throws NetworkException, OTSGeometryException
    {
        Network network = new OTSNetwork("OTSLinkTestNetwork", true);
        Node startNode = new OTSNode(network, "start", new OTSPoint3D(10, 20, 0));
        Node endNode = new OTSNode(network, "end", new OTSPoint3D(1000, 2000, 10));
        GTUCompatibility<LinkType> compatibility = new GTUCompatibility<LinkType>((LinkType) null)
                .addAllowedGTUType(network.getGtuType(GTUType.DEFAULTS.VEHICLE), LongitudinalDirectionality.DIR_NONE);
        LinkType linkType = new LinkType("myLinkType", network.getLinkType(LinkType.DEFAULTS.ROAD), compatibility, network);
        OTSLine3D designLine = new OTSLine3D(startNode.getPoint(), endNode.getPoint());
        OTSSimulatorInterface simulator = MockSimulator.createMock();
        // Map<GTUType, LongitudinalDirectionality> directionalityMap = new LinkedHashMap<>();
        OTSLink link = new OTSLink(network, "link1", startNode, endNode, linkType, designLine, simulator);
        assertTrue("network contains the newly constructed link", network.containsLink(link));
        // directionalityMap is currently empty
        assertEquals("directionality for network.getGtuType(GTUType.DEFAULTS.VEHICLE) is DIR_NONE",
                LongitudinalDirectionality.DIR_NONE, link.getDirectionality(network.getGtuType(GTUType.DEFAULTS.VEHICLE)));
        GTUType carType = new GTUType("car", network.getGtuType(GTUType.DEFAULTS.VEHICLE));
        compatibility.addAllowedGTUType(carType, LongitudinalDirectionality.DIR_MINUS);
        linkType = new LinkType("myLinkType2", network.getLinkType(LinkType.DEFAULTS.ROAD), compatibility, network);
        link = new OTSLink(network, "link2", startNode, endNode, linkType, designLine, simulator);
        assertEquals("directionality for carType is DIR_MINUS", LongitudinalDirectionality.DIR_MINUS,
                link.getDirectionality(carType));
        GTUType bicycle = new GTUType("bicycle", network.getGtuType(GTUType.DEFAULTS.BICYCLE));
        assertEquals("directionality for bicycle is DIR_NONE", LongitudinalDirectionality.DIR_NONE,
                link.getDirectionality(bicycle));
        compatibility.addAllowedGTUType(network.getGtuType(GTUType.DEFAULTS.BICYCLE), LongitudinalDirectionality.DIR_PLUS);
        linkType = new LinkType("myLinkType3", network.getLinkType(LinkType.DEFAULTS.ROAD), compatibility, network);
        link = new OTSLink(network, "link3", startNode, endNode, linkType, designLine, simulator);
        assertEquals("directionality for bicycle is now DIR_PLUS", LongitudinalDirectionality.DIR_PLUS,
                link.getDirectionality(bicycle));
        compatibility.addAllowedGTUType(carType, LongitudinalDirectionality.DIR_NONE);
        linkType = new LinkType("myLinkType4", network.getLinkType(LinkType.DEFAULTS.ROAD), compatibility, network);
        link = new OTSLink(network, "link4", startNode, endNode, linkType, designLine, simulator);
        assertEquals("directionality for bicycle is now DIR_PLUS", LongitudinalDirectionality.DIR_PLUS,
                link.getDirectionality(network.getGtuType(GTUType.DEFAULTS.BICYCLE)));
        assertEquals("directionality for bicycle is now DIR_PLUS", LongitudinalDirectionality.DIR_NONE,
                link.getDirectionality(carType));
        assertEquals("The link contains no GTUs", 0, link.getGTUCount());
        assertEquals("The link contains zero GTUs", 0, link.getGTUs().size());

        link.addListener(this, Link.GTU_ADD_EVENT);
        link.addListener(this, Link.GTU_REMOVE_EVENT);
        assertEquals("add counter is 0", 0, this.gtuAddedCount);
        assertEquals("remove counter is 0", 0, this.gtuRemovedCount);
        assertEquals("other event counter is 0", 0, this.otherEventCount);
        MockGTU mockGtu1 = new MockGTU("gtu1");
        GTU gtu1 = mockGtu1.getMock();
        MockGTU mockGtu2 = new MockGTU("gtu2");
        GTU gtu2 = mockGtu2.getMock();
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
        assertEquals("LinkType is correctly returned", linkType, link.getLinkType());
        DirectedPoint location = link.getLocation();
        DirectedPoint expectedLocation = designLine.getLocationFraction(0.5);
        assertEquals("location is at halfway point of design line (because design line contains only two points)", 0,
                expectedLocation.distance(location), 0.1);
        // RotZ of location is bogus; makes no sense to test that
        Bounds bounds = link.getBounds();
        assertNotNull("bounds should not be null", bounds);
        assertFalse("link is not equal to null", link.equals(null));
        assertFalse("link is not equal to some other object", link.equals("Hello World!"));
        // Make another link to test the rest of equals
        OTSLink otherLink = new OTSLink(network, "link5", startNode, endNode, linkType, designLine, simulator);
        assertFalse("link is not equal to extremely similar link with different id", link.equals(otherLink));
        // make a link with the same name in another network
        Network otherNetwork = new OTSNetwork("other", true);
        linkType = new LinkType("myLinkType4", network.getLinkType(LinkType.DEFAULTS.ROAD), compatibility, network);
        otherLink = new OTSLink(otherNetwork, "link4", new OTSNode(otherNetwork, "start", new OTSPoint3D(10, 20, 0)),
                new OTSNode(otherNetwork, "end", new OTSPoint3D(1000, 2000, 10)), linkType, designLine, simulator);
        assertTrue("link is equal to extremely similar link with same id but different network", link.equals(otherLink));
        otherNetwork.removeLink(otherLink);
        OTSSimulatorInterface simulator2 = MockSimulator.createMock();
        otherLink = link.clone(otherNetwork, simulator2);
        assertTrue("link is equal to clone in different network", link.equals(otherLink));
    }

    /** {@inheritDoc} */
    @Override
    public final void notify(final EventInterface event) throws RemoteException
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
