package org.opentrafficsim.core.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.media.j3d.Bounds;

import mockit.Mock;
import mockit.MockUp;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.event.EventType;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUType;

/**
 * Test the OTSLink class.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jan 3, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class OTSLinkTest implements EventListenerInterface
{
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
        Network network = new OTSNetwork("OTSLinkTestNetwork");
        Node startNode = new OTSNode(network, "start", new OTSPoint3D(10, 20, 0));
        Node endNode = new OTSNode(network, "end", new OTSPoint3D(1000, 2000, 10));
        LinkType linkType = LinkType.ALL;
        OTSLine3D designLine = new OTSLine3D(startNode.getPoint(), endNode.getPoint());
        OTSSimulatorInterface simulator = new MockUp<OTSSimulatorInterface>()
        {
            // no implementation needed.
        }.getMockInstance();
        Map<GTUType, LongitudinalDirectionality> directionalityMap = new HashMap<>();
        OTSLink link = new OTSLink(network, "link", startNode, endNode, linkType, designLine, simulator, directionalityMap);
        assertTrue("network contains the newly constructed link", network.containsLink(link));
        assertTrue("our directionality map is stored and returned", directionalityMap.equals(link.getDirectionalityMap()));
        // directionalityMap is currently empty
        assertEquals("directionality for GTUType.ALL is DIR_NONE", LongitudinalDirectionality.DIR_NONE,
                link.getDirectionality(GTUType.ALL));
        GTUType carType = new GTUType("car", GTUType.VEHICLE);
        link.addDirectionality(carType, LongitudinalDirectionality.DIR_MINUS);
        assertEquals("directionality for carType is DIR_MINUS", LongitudinalDirectionality.DIR_MINUS,
                link.getDirectionality(carType));
        GTUType bicycle = new GTUType("bicycle", GTUType.BIKE);
        assertEquals("directionality for bicycle is DIR_NONE", LongitudinalDirectionality.DIR_NONE,
                link.getDirectionality(bicycle));
        link.addDirectionality(GTUType.ALL, LongitudinalDirectionality.DIR_PLUS);
        assertEquals("directionality for bicycle is now DIR_PLUS", LongitudinalDirectionality.DIR_PLUS,
                link.getDirectionality(bicycle));
        link.removeDirectionality(carType);
        assertEquals("directionality for car is now DIR_PLUS", LongitudinalDirectionality.DIR_PLUS,
                link.getDirectionality(carType));
        assertEquals("The link contains no GTUs", 0, link.getGTUCount());
        assertEquals("The link contains zero GTUs", 0, link.getGTUs().size());

        link.addListener(this, Link.GTU_ADD_EVENT);
        link.addListener(this, Link.GTU_REMOVE_EVENT);
        assertEquals("add counter is 0", 0, this.gtuAddedCount);
        assertEquals("remove counter is 0", 0, this.gtuRemovedCount);
        assertEquals("other event counter is 0", 0, this.otherEventCount);
        // GTU gtu1 = new MyGTU("gtu1");
        // GTU gtu2 = new MyGTU("gtu2");
        GTU gtu1 = new MockUp<GTU>()
        {
            @Mock
            public String getId()
            {
                return "gtu1";
            }

            @Mock
            public OTSDEVSSimulatorInterface getSimulator()
            {
                return new MockUp<OTSDEVSSimulatorInterface>()
                {
                    // no implementation needed.
                }.getMockInstance();
            }

        }.getMockInstance();
        GTU gtu2 = new MockUp<GTU>()
        {
            @Mock
            public String getId()
            {
                return "gtu2";
            }

            @Mock
            public OTSDEVSSimulatorInterface getSimulator()
            {
                return new MockUp<OTSDEVSSimulatorInterface>()
                {
                    // no implementation needed.
                }.getMockInstance();
            }

        }.getMockInstance();
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
        OTSLink otherLink =
                new OTSLink(network, "link2", startNode, endNode, linkType, designLine, simulator, directionalityMap);
        assertFalse("link is not equal to extremely similar link with different id", link.equals(otherLink));
        // make a link with the same name in another network
        Network otherNetwork = new OTSNetwork("other");
        otherLink =
                new OTSLink(otherNetwork, "link", new OTSNode(otherNetwork, "start", new OTSPoint3D(10, 20, 0)), new OTSNode(
                        otherNetwork, "end", new OTSPoint3D(1000, 2000, 10)), linkType, designLine, simulator,
                        directionalityMap);
        assertTrue("link is equal to extremely similar link with same id but different network", link.equals(otherLink));
        otherNetwork.removeLink(otherLink);
        OTSSimulatorInterface simulator2 = new MockUp<OTSSimulatorInterface>()
        {
            // no implementation needed.
        }.getMockInstance();
        otherLink = link.clone(otherNetwork, simulator2, false);
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
