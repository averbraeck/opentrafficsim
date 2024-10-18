package org.opentrafficsim.core.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.point.Point2d;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.geometry.OtsGeometryException;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.geometry.OtsLine2d;
import org.opentrafficsim.core.mock.MockSimulator;

/**
 * Test the LinkPosition class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class LinkPositionTest
{
    /**
     * Test the LinkPosition class.
     * @throws NetworkException if that happens uncaught; this test has failed
     * @throws OtsGeometryException if that happens uncaught; this test has failed
     */
    @Test
    public void linkPositionTest() throws NetworkException, OtsGeometryException
    {
        Network network = new Network("test network for LinkPosition test", MockSimulator.createMock());
        Node nodeA = new Node(network, "A", new Point2d(10, 10));
        Node nodeB = new Node(network, "B", new Point2d(110, 10));
        Link link = new Link(network, "A to B", nodeA, nodeB, DefaultsNl.ROAD,
                new OtsLine2d(nodeA.getPoint(), nodeB.getPoint()), null);
        double linkLength = link.getLength().si;
        // Apparently (reading the source), LinkPosition is not restricted to the length-range of the link
        for (double fraction : new double[] {-10, 0, 0.1, 0.5, 0.9, 1.0, 11.0})
        {
            Length length = new Length(fraction * linkLength, LengthUnit.METER);
            LinkPosition lp = new LinkPosition(link, length);
            System.out.println(lp);
            assertEquals(link, lp.link(), "link can be retrieved");
            assertEquals(fraction, lp.fractionalLongitudinalPosition(), 0.001, "fraction can be retrieved");
            assertEquals(link.getLength(), lp.getLinkLength(), "length can be retrieved");
            assertEquals(length.si, lp.getLongitudinalPosition().si, 0.001, "longitudinal position can be retrieved");
            assertTrue(lp.toString().startsWith("LinkPosition"), "toString returns something descriptive");
            lp = new LinkPosition(link, length);
            System.out.println(lp);
            assertEquals(link, lp.link(), "link can be retrieved");
            assertEquals(fraction, lp.fractionalLongitudinalPosition(), 0.001, "fraction can be retrieved");
            assertEquals(link.getLength(), lp.getLinkLength(), "length can be retrieved");
            assertEquals(length.si, lp.getLongitudinalPosition().si, 0.001, "longitudinal position can be retrieved");
            assertTrue(lp.toString().startsWith("LinkPosition"), "toString returns something descriptive");
            lp = new LinkPosition(link, fraction);
            System.out.println(lp);
            assertEquals(link, lp.link(), "link can be retrieved");
            assertEquals(fraction, lp.fractionalLongitudinalPosition(), 0.001, "fraction can be retrieved");
            assertEquals(link.getLength(), lp.getLinkLength(), "length can be retrieved");
            assertEquals(length.si, lp.getLongitudinalPosition().si, 0.001, "longitudinal position can be retrieved");
            assertTrue(lp.toString().startsWith("LinkPosition"), "toString returns something descriptive");
            lp = new LinkPosition(link, fraction);
            System.out.println(lp);
            assertEquals(link, lp.link(), "link can be retrieved");
            assertEquals(fraction, lp.fractionalLongitudinalPosition(), 0.001, "fraction can be retrieved");
            assertEquals(link.getLength(), lp.getLinkLength(), "length can be retrieved");
            assertEquals(length.si, lp.getLongitudinalPosition().si, 0.001, "longitudinal position can be retrieved");
            assertTrue(lp.toString().startsWith("LinkPosition"), "toString returns something descriptive");
        }
    }

}
