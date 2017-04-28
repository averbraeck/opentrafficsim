package org.opentrafficsim.road.network;

import static org.junit.Assert.assertEquals;

import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Length;
import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.LinkLocation;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;

import mockit.MockUp;

/**
 * Test the LinkLocation class.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-16 19:20:07 +0200 (Wed, 16 Sep 2015) $, @version $Revision: 1405 $, by $Author: averbraeck $,
 * initial version 20 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LinkLocationTest implements UNITS
{
    /**
     * Test constructor and verify all getters.
     * @throws OTSGeometryException if that happens this test has failed
     * @throws NetworkException if that happens this test has failed
     */
    @Test
    public void linkLocationTest() throws OTSGeometryException, NetworkException
    {
        // Preparations
        Network network = new OTSNetwork("link location test network");
        OTSNode nodeFrom = new OTSNode(network, "From", new OTSPoint3D(0, 0, 0));
        OTSNode nodeTo = new OTSNode(network, "To", new OTSPoint3D(1000, 0, 0));
        OTSLine3D line = new OTSLine3D(new OTSPoint3D[] { new OTSPoint3D(0, 0, 0), new OTSPoint3D(1000, 0, 0) });
        OTSSimulatorInterface simulator = new MockUp<OTSSimulatorInterface>()
        {
            // no implementation needed.
        }.getMockInstance();
        CrossSectionLink link = new CrossSectionLink(network, "Link", nodeFrom, nodeTo, LinkType.ALL, line, simulator,
                LongitudinalDirectionality.DIR_PLUS, LaneKeepingPolicy.KEEP_RIGHT);
        Length linkLength = line.getLength();
        // Now we can make a LinkLocation.
        Length referenceLocationDistance = new Length(123, METER);
        LinkLocation referenceLocation = new LinkLocation(link, referenceLocationDistance);
        assertEquals("link should be the provided Link", link, referenceLocation.getLink());
        assertEquals("longitudinalPosition should be " + referenceLocationDistance, referenceLocationDistance.getSI(),
                referenceLocation.getLongitudinalPosition().getSI(), 0.0001);
        for (int position = 0; position < 1000; position += 100)
        {
            final double fraction = position / linkLength.getSI();
            LinkLocation linkLocation = new LinkLocation(link, fraction);
            assertEquals("link should be the provided Link", link, linkLocation.getLink());
            assertEquals("fractionalLongitudinalPosition should be " + fraction, fraction,
                    linkLocation.getFractionalLongitudinalPosition(), 0.000001);
            assertEquals("longitudinalPosition should be " + position, position, linkLocation.getLongitudinalPosition().getSI(),
                    0.0001);
            // Repeat with the other constructor
            linkLocation = new LinkLocation(link, new Length(position, METER));
            assertEquals("link should be the provided Link", link, linkLocation.getLink());
            assertEquals("fractionalLongitudinalPosition should be " + fraction, fraction,
                    linkLocation.getFractionalLongitudinalPosition(), 0.000001);
            assertEquals("longitudinalPosition should be " + position, position, linkLocation.getLongitudinalPosition().getSI(),
                    0.0001);
            double delta = referenceLocationDistance.getSI() - position;
            assertEquals("distance from reference should be " + delta, delta, linkLocation.distance(referenceLocation).getSI(),
                    0.0001);
            // TODO distance to location on another link (not yet possible; currently ALWAYS returns null)
        }
    }
}
