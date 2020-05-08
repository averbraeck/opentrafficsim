package org.opentrafficsim.core.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSSimulator;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.mock.MockSimulator;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jan 2, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LinkLocationTest
{

    /**
     * Test constructor and getters of the LinkLocation class.
     * @throws NetworkException this test has failed if that happens uncaught
     * @throws OTSGeometryException this test has failed if that happens uncaught
     */
    @Test
    public final void testLinkLocation() throws NetworkException, OTSGeometryException
    {
        OTSPoint3D fromPoint = new OTSPoint3D(100, 200, 300);
        OTSPoint3D toPoint = new OTSPoint3D(1000, 2000, 330);
        OTSNetwork network = new OTSNetwork("testNetworkForCapacityOTSLink", true, MockSimulator.createMock());
        Node fromNode = new OTSNode(network, "startNode", fromPoint);
        Node toNode = new OTSNode(network, "endNode", toPoint);
        LinkType linkType = network.getLinkType(LinkType.DEFAULTS.ROAD);
        OTSLine3D designLine = new OTSLine3D(fromPoint, toPoint);
        Link link = new OTSLink(network, "link", fromNode, toNode, linkType, designLine);
        Length linkLength = link.getLength();
        // Create an unrelated link
        OTSPoint3D a = new OTSPoint3D(1, 2, 3);
        OTSPoint3D b = new OTSPoint3D(11, 12, 13);
        Link otherLink = new OTSLink(network, "otherLink", new OTSNode(network, "a", a), new OTSNode(network, "b", b), linkType,
                new OTSLine3D(a, b));
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
