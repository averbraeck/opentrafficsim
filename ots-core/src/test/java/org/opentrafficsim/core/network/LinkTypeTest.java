package org.opentrafficsim.core.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentrafficsim.core.compatibility.GtuCompatibility;
import org.opentrafficsim.core.dsol.OTSSimulator;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GtuType;

/**
 * Test constructor and methods of the LinkType class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class LinkTypeTest
{

    /**
     * Test the constructor and methods of the LinkType class.
     */
    @Test
    @SuppressWarnings({"unlikely-arg-type"})
    public final void testLinkType()
    {
        OTSNetwork network = new OTSNetwork("test", true, new OTSSimulator("Simulator for LinkTypeTest"));
        GtuCompatibility<LinkType> roadCompatibility =
                new GtuCompatibility<>((LinkType) null).addCompatibleGtuType(network.getGtuType(GtuType.DEFAULTS.VEHICLE));
        try
        {
            new LinkType("name", null, network);
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
        try
        {
            new LinkType(null, null, network);
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
        GtuType carType = new GtuType("Car", network.getGtuType(GtuType.DEFAULTS.VEHICLE));
        GtuType truckType = new GtuType("Truck", network.getGtuType(GtuType.DEFAULTS.VEHICLE));
        GtuType catamaran = new GtuType("Catamaran", network.getGtuType(GtuType.DEFAULTS.SHIP));
        LinkType roadLinkType = new LinkType("Vehicles", null, network);
        GtuCompatibility<LinkType> waterCompatibility =
                new GtuCompatibility<>((LinkType) null).addCompatibleGtuType(network.getGtuType(GtuType.DEFAULTS.SHIP));
        LinkType waterwayType = new LinkType("Waterway", null, network);
        assertTrue("equals to itself", roadLinkType.equals(roadLinkType));
        assertFalse("not equal to the other", roadLinkType.equals(waterwayType));
        assertEquals("Car is compatible with roadLinkType", LongitudinalDirectionality.DIR_BOTH,
                roadLinkType.getDirectionality(carType, true));
        assertEquals("Truck is compatible with roadLinkType", LongitudinalDirectionality.DIR_BOTH,
                roadLinkType.getDirectionality(truckType, true));
        assertEquals("Catamaran is not compatible with roadLinkType", LongitudinalDirectionality.DIR_NONE,
                roadLinkType.getDirectionality(catamaran, true));
        assertEquals("Truck is not compatible with waterwayLinkType", LongitudinalDirectionality.DIR_NONE,
                waterwayType.getDirectionality(truckType, true));
        assertEquals("Catamaran is compatible with waterwayLinkType", LongitudinalDirectionality.DIR_BOTH,
                waterwayType.getDirectionality(catamaran, true));
        LinkType lava = network.getLinkType(LinkType.DEFAULTS.NONE);
        assertEquals("name must match", "Waterway", waterwayType.getId());
        assertTrue("toString returns something with the name in it", waterwayType.toString().contains("Waterway"));
        assertFalse("waterwayType is not equal to null", waterwayType.equals(null));
        assertFalse("waterwayType is not equal to some String", waterwayType.equals("Hello world!"));
        assertFalse("waterwayType is not equal to lava", waterwayType.equals(lava));
        // Try to create another waterwayType
        LinkType waterWayType2 = new LinkType("Waterway", null, network);
        assertTrue("waterwayType2 is equal to the first", waterwayType.equals(waterWayType2));
        assertFalse("road is not of type NONE", roadLinkType.isOfType(LinkType.DEFAULTS.NONE));
        assertFalse("road is not of type NONE (alterative way to test)", roadLinkType.isNone());
        // TODO how the hell can you obtain a linkType that will return true to isNone() ?
        // assertTrue("???", LinkType.DEFAULTS.NONE.isNone());
        assertFalse("waterWayType2 is not a road", waterWayType2.isRoad());
        // TODO next one fails - what is wrong?
        // assertTrue("waterWayType2 is a waterway", waterWayType2.isWaterWay());
        // TODO next one fails - what is wrong?
        // assertTrue("roadLinkType is a road", roadLinkType.isRoad());
        GtuCompatibility<LinkType> poorRoadCompatibility =
                new GtuCompatibility<>((LinkType) null).addCompatibleGtuType(network.getGtuType(GtuType.DEFAULTS.CAR));
        LinkType poorSurfaceLinkType =
                new LinkType("PoorSurfaceType", network.getLinkType(LinkType.DEFAULTS.ROAD), network);
        assertTrue("poor road is of type ROAD", poorSurfaceLinkType.isOfType(LinkType.DEFAULTS.ROAD));
        assertNull("compatibility of waterway for car is not decidable",
                waterwayType.isCompatible(carType, GTUDirectionality.DIR_PLUS));
        GtuCompatibility<LinkType> compatibility = waterwayType.getGtuCompatibility();
        assertTrue("compatibility allows SHIP", compatibility.isCompatible(network.getGtuType(GtuType.DEFAULTS.SHIP)));
        assertFalse("compatibility has no info for catamaran", compatibility.isCompatible(catamaran));
        assertTrue("compatibility can decide for parent type of catamaran", compatibility.isCompatible(catamaran.getParent()));
        LinkType reverseWaterway = waterwayType.reverse();
        // Reverse of DIR_BOTH should be DIR_BOTH
        // The next two tests fail.
        // assertEquals("Reverse of DIR_BOTH is DIR_BOTH", LongitudinalDirectionality.DIR_BOTH,
        // reverseWaterway.getDirectionality(catamaran, true));
        // assertEquals("Directionality of waterwayType for catamaran is DIR_BOTH", LongitudinalDirectionality.DIR_BOTH,
        // reverseWaterway.getCompatibility().getDirectionality(catamaran, true));

    }

}
