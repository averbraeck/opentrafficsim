package org.opentrafficsim.core.network;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opentrafficsim.core.network.geotools.NodeGeotools;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Test the LinkLocation class.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version20 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LinkLocationTest
{
    /**
     * Test constructor and verify all getters.
     */
    @Test
    public void linkLocationTest()
    {
        // Preparations
        NodeGeotools.STR nodeFrom = new NodeGeotools.STR("From", new Coordinate(0, 0, 0));
        NodeGeotools.STR nodeTo = new NodeGeotools.STR("To", new Coordinate(1000, 0, 0));
        DoubleScalar.Rel<LengthUnit> linkLength = new DoubleScalar.Rel<LengthUnit>(1000, LengthUnit.METER);
        CrossSectionLink<?, ?> link = new CrossSectionLink<String, String>("Link", nodeFrom, nodeTo, linkLength);
        // Now we can make a LinkLocation.
        DoubleScalar.Rel<LengthUnit> referenceLocationDistance =
                new DoubleScalar.Rel<LengthUnit>(123, LengthUnit.METER);
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
            assertEquals("longitudinalPosition should be " + position, position, linkLocation.getLongitudinalPosition()
                    .getSI(), 0.0001);
            // Repeat with the other constructor
            linkLocation = new LinkLocation(link, new DoubleScalar.Rel<LengthUnit>(position, LengthUnit.METER));
            assertEquals("link should be the provided Link", link, linkLocation.getLink());
            assertEquals("fractionalLongitudinalPosition should be " + fraction, fraction,
                    linkLocation.getFractionalLongitudinalPosition(), 0.000001);
            assertEquals("longitudinalPosition should be " + position, position, linkLocation.getLongitudinalPosition()
                    .getSI(), 0.0001);
            double delta = referenceLocationDistance.getSI() - position;
            assertEquals("distance from reference should be " + delta, delta, linkLocation.distance(referenceLocation)
                    .getSI(), 0.0001);
            // TODO distance to location on another link (not yet possible; currently ALWAYS returns null)
        }
    }
}
