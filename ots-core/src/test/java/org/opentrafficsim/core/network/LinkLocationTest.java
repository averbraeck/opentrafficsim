package org.opentrafficsim.core.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.point.Point2d;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine2d;
import org.opentrafficsim.core.mock.MockSimulator;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
        Point2d fromPoint = new Point2d(100, 200);
        Point2d toPoint = new Point2d(1000, 2000);
        Network network = new Network("testNetworkForCapacityOTSLink", MockSimulator.createMock());
        Node fromNode = new Node(network, "startNode", fromPoint);
        Node toNode = new Node(network, "endNode", toPoint);
        LinkType linkType = DefaultsNl.ROAD;
        OtsLine2d designLine = new OtsLine2d(fromPoint, toPoint);
        Link link = new Link(network, "link", fromNode, toNode, linkType, designLine, null);
        Length linkLength = link.getLength();
        // Create an unrelated link
        Point2d a = new Point2d(1, 2);
        Point2d b = new Point2d(11, 12);
        Link otherLink = new Link(network, "otherLink", new Node(network, "a", a), new Node(network, "b", b), linkType,
                new OtsLine2d(a, b), null);
        for (int percentage = 0; percentage <= 100; percentage += 10)
        {
            double fraction = percentage / 100.0;
            LinkLocation ll = new LinkLocation(link, fraction);
            assertTrue(link.equals(ll.getLink()), "link must match");
            assertEquals(fraction, ll.getFractionalLongitudinalPosition(), 0.0001, "fraction must match");
            assertEquals(linkLength.si * fraction, ll.getLongitudinalPosition().si, 0.1, "position must match");
            // Alternate constructor
            Length distance = new Length(linkLength.si * fraction, LengthUnit.SI);
            ll = new LinkLocation(link, distance);
            assertTrue(link.equals(ll.getLink()), "link must match");
            assertEquals(fraction, ll.getFractionalLongitudinalPosition(), 0.0001, "fraction must match");
            assertEquals(linkLength.si * fraction, ll.getLongitudinalPosition().si, 0.1, "position must match");
            // Create another LinkLocation and check the distance between this and that one
            for (int otherPercentage = 0; otherPercentage <= 100; otherPercentage += 25)
            {
                double otherFraction = otherPercentage / 100.0;
                LinkLocation otherLL = new LinkLocation(link, otherFraction);
                assertEquals((otherFraction - fraction) * linkLength.si, ll.distance(otherLL).si, 0.1, "distance must match");
            }
            LinkLocation otherLinkLocation = new LinkLocation(otherLink, 0.5);
            assertNull(ll.distance(otherLinkLocation), "Distance to unrelated link must be null");
            assertTrue(ll.toString().contains(link.getId()));
        }
    }
}
