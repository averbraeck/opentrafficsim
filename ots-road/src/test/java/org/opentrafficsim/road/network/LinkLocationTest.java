package org.opentrafficsim.road.network;

import static org.junit.Assert.assertEquals;

import org.djunits.unit.util.UNITS;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.point.Point2d;
import org.junit.Test;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine3d;
import org.opentrafficsim.core.network.LinkLocation;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.mock.MockSimulator;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;

/**
 * Test the LinkLocation class.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class LinkLocationTest implements UNITS
{
    /**
     * Test constructor and verify all getters.
     * @throws OtsGeometryException if that happens this test has failed
     * @throws NetworkException if that happens this test has failed
     */
    @Test
    public final void linkLocationTest() throws OtsGeometryException, NetworkException
    {
        // Preparations
        OtsSimulatorInterface simulator = MockSimulator.createMock();
        RoadNetwork network = new RoadNetwork("link location test network", simulator);
        Node nodeFrom = new Node(network, "From", new Point2d(0, 0), Direction.ZERO);
        Node nodeTo = new Node(network, "To", new Point2d(1000, 0), Direction.ZERO);
        OtsLine3d line = new OtsLine3d(new Point2d[] {new Point2d(0, 0), new Point2d(1000, 0)});
        CrossSectionLink link =
                new CrossSectionLink(network, "Link", nodeFrom, nodeTo, DefaultsNl.ROAD, line, LaneKeepingPolicy.KEEPRIGHT);
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
