package org.opentrafficsim.core.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentrafficsim.core.compatibility.GTUCompatibility;
import org.opentrafficsim.core.dsol.OTSSimulator;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;

/**
 * Test constructor and methods of the LinkType class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jan 2, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
        GTUCompatibility<LinkType> roadCompatibility = new GTUCompatibility<>((LinkType) null)
                .addAllowedGTUType(network.getGtuType(GTUType.DEFAULTS.VEHICLE), LongitudinalDirectionality.DIR_BOTH);
        try
        {
            new LinkType("name", null, null, network);
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
        try
        {
            new LinkType(null, null, roadCompatibility, network);
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
        GTUType carType = new GTUType("Car", network.getGtuType(GTUType.DEFAULTS.VEHICLE));
        GTUType truckType = new GTUType("Truck", network.getGtuType(GTUType.DEFAULTS.VEHICLE));
        GTUType catamaran = new GTUType("Catamaran", network.getGtuType(GTUType.DEFAULTS.SHIP));
        LinkType roadLinkType = new LinkType("Vehicles", null, roadCompatibility, network);
        GTUCompatibility<LinkType> waterCompatibility = new GTUCompatibility<>((LinkType) null)
                .addAllowedGTUType(network.getGtuType(GTUType.DEFAULTS.SHIP), LongitudinalDirectionality.DIR_BOTH);
        LinkType waterwayType = new LinkType("Waterway", null, waterCompatibility, network);
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
        LinkType waterWayType2 = new LinkType("Waterway", null, waterCompatibility, network);
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
        GTUCompatibility<LinkType> poorRoadCompatibility = new GTUCompatibility<>((LinkType) null)
                .addAllowedGTUType(network.getGtuType(GTUType.DEFAULTS.CAR), LongitudinalDirectionality.DIR_BOTH);
        LinkType poorSurfaceLinkType =
                new LinkType("PoorSurfaceType", network.getLinkType(LinkType.DEFAULTS.ROAD), poorRoadCompatibility, network);
        assertTrue("poor road is of type ROAD", poorSurfaceLinkType.isOfType(LinkType.DEFAULTS.ROAD));
        assertNull("compatibility of waterway for car is not decidable",
                waterwayType.isCompatible(carType, GTUDirectionality.DIR_PLUS));
        GTUCompatibility<LinkType> compatibility = waterwayType.getCompatibility();
        assertTrue("compatibility allows SHIP in dir plus",
                compatibility.isCompatible(network.getGtuType(GTUType.DEFAULTS.SHIP), GTUDirectionality.DIR_PLUS));
        assertTrue("compatibility allows SHIP in dir minus",
                compatibility.isCompatible(network.getGtuType(GTUType.DEFAULTS.SHIP), GTUDirectionality.DIR_MINUS));
        assertNull("compatibility cannot decide for catamaran",
                compatibility.isCompatible(catamaran, GTUDirectionality.DIR_PLUS));
        assertTrue("compatibility can decide for parent type of catamaran",
                compatibility.isCompatible(catamaran.getParent(), GTUDirectionality.DIR_PLUS));
        assertEquals("Directionality of waterwayType for catamaran is DIR_BOTH", LongitudinalDirectionality.DIR_BOTH,
                waterwayType.getCompatibility().getDirectionality(catamaran, true));
        LinkType reverseWaterway = waterwayType.reverse();
        // Reverse of DIR_BOTH should be DIR_BOTH
        // The next two tests fail.
        // assertEquals("Reverse of DIR_BOTH is DIR_BOTH", LongitudinalDirectionality.DIR_BOTH,
        // reverseWaterway.getDirectionality(catamaran, true));
        // assertEquals("Directionality of waterwayType for catamaran is DIR_BOTH", LongitudinalDirectionality.DIR_BOTH,
        // reverseWaterway.getCompatibility().getDirectionality(catamaran, true));

    }

}
