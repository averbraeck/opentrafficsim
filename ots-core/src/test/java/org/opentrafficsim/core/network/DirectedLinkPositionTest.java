package org.opentrafficsim.core.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSSimulator;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;

/**
 * Test the DirectedLinkPosition class.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Feb 24, 2020 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class DirectedLinkPositionTest
{
    /**
     * Test the DirectedLinkPosition class.
     * @throws NetworkException if that happens uncaught; this test has failed
     * @throws OTSGeometryException if that happens uncaught; this test has failed
     */
    @Test
    public void directedLinkPositionTest() throws NetworkException, OTSGeometryException
    {
        Network network = new OTSNetwork("test network for DirectedLinkPosition test", true);
        Node nodeA = new OTSNode(network, "A", new OTSPoint3D(10, 10, 10));
        Node nodeB = new OTSNode(network, "B", new OTSPoint3D(110, 10, 10));
        Link link = new OTSLink(network, "A to B", nodeA, nodeB, network.getLinkType(LinkType.DEFAULTS.ROAD),
                new OTSLine3D(nodeA.getPoint(), nodeB.getPoint()), new OTSSimulator("simulator for DirectedLinkPosition test"));
        double linkLength = link.getLength().si;
        // Apparently (reading the source), DirectedLinkPosition is not restricted to the length-range of the link
        for (double fraction : new double[] { -10, 0, 0.1, 0.5, 0.9, 1.0, 11.0 })
        {
            Length length = new Length(fraction * linkLength, LengthUnit.METER);
            DirectedLinkPosition dlp = new DirectedLinkPosition(link, length, GTUDirectionality.DIR_PLUS);
            System.out.println(dlp);
            assertEquals("link can be retrieved", link, dlp.getLink());
            assertEquals("fraction can be retrieved", fraction, dlp.getFractionalLongitudinalPosition(), 0.001);
            assertEquals("length can be retrieved", link.getLength(), dlp.getLength());
            assertEquals("longitudinal position can be retrieved", length.si, dlp.getLongitudinalPosition().si, 0.001);
            assertTrue("toString returns something descriptive", dlp.toString().startsWith("DirectedLinkPosition"));
            dlp = new DirectedLinkPosition(link, length, GTUDirectionality.DIR_MINUS);
            System.out.println(dlp);
            assertEquals("link can be retrieved", link, dlp.getLink());
            assertEquals("fraction can be retrieved", fraction, dlp.getFractionalLongitudinalPosition(), 0.001);
            assertEquals("length can be retrieved", link.getLength(), dlp.getLength());
            assertEquals("longitudinal position can be retrieved", length.si, dlp.getLongitudinalPosition().si, 0.001);
            assertTrue("toString returns something descriptive", dlp.toString().startsWith("DirectedLinkPosition"));
            dlp = new DirectedLinkPosition(link, fraction, GTUDirectionality.DIR_PLUS);
            System.out.println(dlp);
            assertEquals("link can be retrieved", link, dlp.getLink());
            assertEquals("fraction can be retrieved", fraction, dlp.getFractionalLongitudinalPosition(), 0.001);
            assertEquals("length can be retrieved", link.getLength(), dlp.getLength());
            assertEquals("longitudinal position can be retrieved", length.si, dlp.getLongitudinalPosition().si, 0.001);
            assertTrue("toString returns something descriptive", dlp.toString().startsWith("DirectedLinkPosition"));
            dlp = new DirectedLinkPosition(link, fraction, GTUDirectionality.DIR_MINUS);
            System.out.println(dlp);
            assertEquals("link can be retrieved", link, dlp.getLink());
            assertEquals("fraction can be retrieved", fraction, dlp.getFractionalLongitudinalPosition(), 0.001);
            assertEquals("length can be retrieved", link.getLength(), dlp.getLength());
            assertEquals("longitudinal position can be retrieved", length.si, dlp.getLongitudinalPosition().si, 0.001);
            assertTrue("toString returns something descriptive", dlp.toString().startsWith("DirectedLinkPosition"));
        }
    }

}
