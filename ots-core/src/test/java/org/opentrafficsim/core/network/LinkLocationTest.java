package org.opentrafficsim.core.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.junit.Test;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine3d;
import org.opentrafficsim.core.geometry.OtsPoint3d;
import org.opentrafficsim.core.mock.MockSimulator;

/**
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class LinkLocationTest
{

    /**
     * Test constructor and getters of the LinkLocation class.
     * @throws NetworkException this test has failed if that happens uncaught
     * @throws OtsGeometryException this test has failed if that happens uncaught
     */
    @Test
    public final void testLinkLocation() throws NetworkException, OtsGeometryException
    {
        OtsPoint3d fromPoint = new OtsPoint3d(100, 200, 300);
        OtsPoint3d toPoint = new OtsPoint3d(1000, 2000, 330);
        OtsNetwork network = new OtsNetwork("testNetworkForCapacityOTSLink", MockSimulator.createMock());
        Node fromNode = new Node(network, "startNode", fromPoint);
        Node toNode = new Node(network, "endNode", toPoint);
        LinkType linkType = DefaultsNl.ROAD;
        OtsLine3d designLine = new OtsLine3d(fromPoint, toPoint);
        Link link = new OtsLink(network, "link", fromNode, toNode, linkType, designLine);
        Length linkLength = link.getLength();
        // Create an unrelated link
        OtsPoint3d a = new OtsPoint3d(1, 2, 3);
        OtsPoint3d b = new OtsPoint3d(11, 12, 13);
        Link otherLink = new OtsLink(network, "otherLink", new Node(network, "a", a), new Node(network, "b", b), linkType,
                new OtsLine3d(a, b));
        for (int percentage = 0; percentage <= 100; percentage += 10)
        {
            double fraction = percentage / 100.0;
            LinkLocation ll = new LinkLocation(link, fraction);
            assertTrue("link must match", link.equals(ll.getLink()));
            assertEquals("fraction must match", fraction, ll.getFractionalLongitudinalPosition(), 0.0001);
            assertEquals("position must match", linkLength.si * fraction, ll.getLongitudinalPosition().si, 0.1);
            // Alternate constructor
            Length distance = new Length(linkLength.si * fraction, LengthUnit.SI);
            ll = new LinkLocation(link, distance);
            assertTrue("link must match", link.equals(ll.getLink()));
            assertEquals("fraction must match", fraction, ll.getFractionalLongitudinalPosition(), 0.0001);
            assertEquals("position must match", linkLength.si * fraction, ll.getLongitudinalPosition().si, 0.1);
            // Create another LinkLocation and check the distance between this and that one
            for (int otherPercentage = 0; otherPercentage <= 100; otherPercentage += 25)
            {
                double otherFraction = otherPercentage / 100.0;
                LinkLocation otherLL = new LinkLocation(link, otherFraction);
                assertEquals("distance must match", (otherFraction - fraction) * linkLength.si, ll.distance(otherLL).si, 0.1);
            }
            LinkLocation otherLinkLocation = new LinkLocation(otherLink, 0.5);
            assertNull("Distance to unrelated link must be null", ll.distance(otherLinkLocation));
            assertTrue(ll.toString().contains(link.getId()));
        }
    }
}
