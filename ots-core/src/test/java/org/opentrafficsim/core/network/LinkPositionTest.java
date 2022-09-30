package org.opentrafficsim.core.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.junit.Test;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
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
     * @throws OTSGeometryException if that happens uncaught; this test has failed
     */
    @Test
    public void linkPositionTest() throws NetworkException, OTSGeometryException
    {
        OTSNetwork network = new OTSNetwork("test network for LinkPosition test", true, MockSimulator.createMock());
        Node nodeA = new OTSNode(network, "A", new OTSPoint3D(10, 10, 10));
        Node nodeB = new OTSNode(network, "B", new OTSPoint3D(110, 10, 10));
        Link link = new OTSLink(network, "A to B", nodeA, nodeB, network.getLinkType(LinkType.DEFAULTS.ROAD),
                new OTSLine3D(nodeA.getPoint(), nodeB.getPoint()));
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
