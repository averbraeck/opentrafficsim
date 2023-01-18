package org.opentrafficsim.core.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.djutils.exceptions.Try;
import org.junit.Test;
import org.opentrafficsim.core.compatibility.GtuCompatibility;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsSimulator;
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
        OtsNetwork network = new OtsNetwork("test", true, new OtsSimulator("Simulator for LinkTypeTest"));
        Try.testFail(() -> new LinkType(null, null, network), NullPointerException.class);
        Try.testFail(() -> new LinkType("name", null, null), NullPointerException.class);

        GtuType carType = new GtuType("Car", DefaultsNl.VEHICLE);
        GtuType truckType = new GtuType("Truck", DefaultsNl.VEHICLE);
        GtuType catamaran = new GtuType("Catamaran", DefaultsNl.SHIP);

        LinkType roadLinkType = new LinkType("Vehicles", null, network);
        roadLinkType.addCompatibleGtuType(DefaultsNl.VEHICLE);

        LinkType waterwayType = new LinkType("Waterway", null, network);
        waterwayType.addCompatibleGtuType(DefaultsNl.SHIP);

        assertTrue("equals to itself", roadLinkType.equals(roadLinkType));
        assertFalse("not equal to the other", roadLinkType.equals(waterwayType));
        assertTrue("Car is compatible with roadLinkType", roadLinkType.isCompatible(carType));
        assertTrue("Truck is compatible with roadLinkType", roadLinkType.isCompatible(truckType));
        assertFalse("Catamaran is not compatible with roadLinkType", roadLinkType.isCompatible(catamaran));
        assertFalse("Truck is not compatible with waterwayLinkType", waterwayType.isCompatible(truckType));
        assertTrue("Catamaran is compatible with waterwayLinkType", waterwayType.isCompatible(catamaran));

        assertEquals("name must match", "Waterway", waterwayType.getId());
        assertTrue("toString returns something with the name in it", waterwayType.toString().contains("Waterway"));
        assertFalse("waterwayType is not equal to null", waterwayType.equals(null));
        assertFalse("waterwayType is not equal to some String", waterwayType.equals("Hello world!"));
        LinkType lava = network.getLinkType(LinkType.DEFAULTS.NONE);
        assertFalse("waterwayType is not equal to lava", waterwayType.equals(lava));

        // Try to create another waterwayType
        LinkType waterWayType2 = new LinkType("Waterway", null, network);
        assertTrue("waterwayType2 is equal to the first", waterwayType.equals(waterWayType2));
        assertFalse("road is not of type NONE", roadLinkType.isOfType(LinkType.DEFAULTS.NONE));
        assertFalse("road is not of type NONE (alterative way to test)", roadLinkType.isNone());
        assertTrue("none returns true for isNone()", network.getLinkType(LinkType.DEFAULTS.NONE).isNone());
        assertFalse("waterWayType2 is not a road", waterWayType2.isRoad());

        LinkType poorSurfaceLinkType = new LinkType("PoorSurfaceType", network.getLinkType(LinkType.DEFAULTS.ROAD), network);
        poorSurfaceLinkType.addCompatibleGtuType(DefaultsNl.CAR);
        assertTrue("poor road is of type ROAD", poorSurfaceLinkType.isOfType(LinkType.DEFAULTS.ROAD));
        assertFalse("compatibility of waterway for car is false", waterwayType.isCompatible(carType));
        assertNull("compatibility of waterway for car is undecidable on level", waterwayType.isCompatibleOnInfraLevel(carType));
        GtuCompatibility<LinkType> compatibility = waterwayType.getGtuCompatibility();
        assertTrue("compatibility allows SHIP", compatibility.isCompatible(DefaultsNl.SHIP));
    }

}
