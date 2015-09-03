package org.opentrafficsim.core.network;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opentrafficsim.core.OTS_SCALAR;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.lane.CrossSectionLink;

/**
 * Test the LinkLocation class.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 20 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LinkLocationTest implements OTS_SCALAR
{
    /**
     * Test constructor and verify all getters.
     */
    @Test
    public void linkLocationTest()
    {
        // Preparations
        OTSNode nodeFrom = new OTSNode("From", new OTSPoint3D(0, 0, 0));
        OTSNode nodeTo = new OTSNode("To", new OTSPoint3D(1000, 0, 0));
        OTSLine3D line = new OTSLine3D(new OTSPoint3D[]{new OTSPoint3D(0, 0, 0), new OTSPoint3D(1000, 0, 0)});
        CrossSectionLink link = new CrossSectionLink("Link", nodeFrom, nodeTo, line);
        Length.Rel linkLength = line.getLength();
        // Now we can make a LinkLocation.
        Length.Rel referenceLocationDistance = new Length.Rel(123, METER);
        LinkLocation referenceLocation = new LinkLocation(link, referenceLocationDistance);
        assertEquals("link should be the provided Link", link, referenceLocation.getLink());
        assertEquals("longitudinalPosition should be " + referenceLocationDistance, referenceLocationDistance.getSI(),
            referenceLocation.getLongitudinalPosition().getSI(), 0.0001);
        for (int position = 0; position < 1000; position += 100)
        {
            final double fraction = position / linkLength.getSI();
            LinkLocation linkLocation = new LinkLocation(link, fraction);
            assertEquals("link should be the provided Link", link, linkLocation.getLink());
            assertEquals("fractionalLongitudinalPosition should be " + fraction, fraction, linkLocation
                .getFractionalLongitudinalPosition(), 0.000001);
            assertEquals("longitudinalPosition should be " + position, position, linkLocation.getLongitudinalPosition()
                .getSI(), 0.0001);
            // Repeat with the other constructor
            linkLocation = new LinkLocation(link, new Length.Rel(position, METER));
            assertEquals("link should be the provided Link", link, linkLocation.getLink());
            assertEquals("fractionalLongitudinalPosition should be " + fraction, fraction, linkLocation
                .getFractionalLongitudinalPosition(), 0.000001);
            assertEquals("longitudinalPosition should be " + position, position, linkLocation.getLongitudinalPosition()
                .getSI(), 0.0001);
            double delta = referenceLocationDistance.getSI() - position;
            assertEquals("distance from reference should be " + delta, delta, linkLocation.distance(referenceLocation)
                .getSI(), 0.0001);
            // TODO distance to location on another link (not yet possible; currently ALWAYS returns null)
        }
    }
}
