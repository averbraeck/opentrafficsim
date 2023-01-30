package org.opentrafficsim.core.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.junit.Test;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine3D;
import org.opentrafficsim.core.geometry.OtsPoint3D;
import org.opentrafficsim.core.mock.MockSimulator;

/**
 * Test the LinkPosition class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
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
        OtsNetwork network = new OtsNetwork("test network for LinkPosition test", MockSimulator.createMock());
        Node nodeA = new OtsNode(network, "A", new OtsPoint3D(10, 10, 10));
        Node nodeB = new OtsNode(network, "B", new OtsPoint3D(110, 10, 10));
        Link link = new OtsLink(network, "A to B", nodeA, nodeB, DefaultsNl.ROAD,
                new OtsLine3D(nodeA.getPoint(), nodeB.getPoint()));
        double linkLength = link.getLength().si;
        // Apparently (reading the source), LinkPosition is not restricted to the length-range of the link
        for (double fraction : new double[] {-10, 0, 0.1, 0.5, 0.9, 1.0, 11.0})
        {
            Length length = new Length(fraction * linkLength, LengthUnit.METER);
            LinkPosition lp = new LinkPosition(link, length);
            System.out.println(lp);
            assertEquals("link can be retrieved", link, lp.getLink());
            assertEquals("fraction can be retrieved", fraction, lp.getFractionalLongitudinalPosition(), 0.001);
            assertEquals("length can be retrieved", link.getLength(), lp.getLinkLength());
            assertEquals("longitudinal position can be retrieved", length.si, lp.getLongitudinalPosition().si, 0.001);
            assertTrue("toString returns something descriptive", lp.toString().startsWith("LinkPosition"));
            lp = new LinkPosition(link, length);
            System.out.println(lp);
            assertEquals("link can be retrieved", link, lp.getLink());
            assertEquals("fraction can be retrieved", fraction, lp.getFractionalLongitudinalPosition(), 0.001);
            assertEquals("length can be retrieved", link.getLength(), lp.getLinkLength());
            assertEquals("longitudinal position can be retrieved", length.si, lp.getLongitudinalPosition().si, 0.001);
            assertTrue("toString returns something descriptive", lp.toString().startsWith("LinkPosition"));
            lp = new LinkPosition(link, fraction);
            System.out.println(lp);
            assertEquals("link can be retrieved", link, lp.getLink());
            assertEquals("fraction can be retrieved", fraction, lp.getFractionalLongitudinalPosition(), 0.001);
            assertEquals("length can be retrieved", link.getLength(), lp.getLinkLength());
            assertEquals("longitudinal position can be retrieved", length.si, lp.getLongitudinalPosition().si, 0.001);
            assertTrue("toString returns something descriptive", lp.toString().startsWith("LinkPosition"));
            lp = new LinkPosition(link, fraction);
            System.out.println(lp);
            assertEquals("link can be retrieved", link, lp.getLink());
            assertEquals("fraction can be retrieved", fraction, lp.getFractionalLongitudinalPosition(), 0.001);
            assertEquals("length can be retrieved", link.getLength(), lp.getLinkLength());
            assertEquals("longitudinal position can be retrieved", length.si, lp.getLongitudinalPosition().si, 0.001);
            assertTrue("toString returns something descriptive", lp.toString().startsWith("LinkPosition"));
        }
    }

}
