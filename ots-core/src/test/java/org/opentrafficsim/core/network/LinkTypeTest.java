package org.opentrafficsim.core.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.djutils.exceptions.Try;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.compatibility.GtuCompatibility;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.gtu.GtuType;

/**
 * Test constructor and methods of the LinkType class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
        Network network = new Network("test", new OtsSimulator("Simulator for LinkTypeTest"));
        Try.testFail(() -> new LinkType(null, null), NullPointerException.class);

        GtuType carType = new GtuType("Car", DefaultsNl.VEHICLE);
        GtuType truckType = new GtuType("Truck", DefaultsNl.VEHICLE);
        GtuType catamaran = new GtuType("Catamaran", DefaultsNl.SHIP);

        LinkType roadLinkType = new LinkType("Vehicles", null);
        roadLinkType.addCompatibleGtuType(DefaultsNl.VEHICLE);

        LinkType waterwayType = new LinkType("Waterway", null);
        waterwayType.addCompatibleGtuType(DefaultsNl.SHIP);

        assertTrue(roadLinkType.equals(roadLinkType), "equals to itself");
        assertFalse(roadLinkType.equals(waterwayType), "not equal to the other");
        assertTrue(roadLinkType.isCompatible(carType), "Car is compatible with roadLinkType");
        assertTrue(roadLinkType.isCompatible(truckType), "Truck is compatible with roadLinkType");
        assertFalse(roadLinkType.isCompatible(catamaran), "Catamaran is not compatible with roadLinkType");
        assertFalse(waterwayType.isCompatible(truckType), "Truck is not compatible with waterwayLinkType");
        assertTrue(waterwayType.isCompatible(catamaran), "Catamaran is compatible with waterwayLinkType");

        assertEquals("Waterway", waterwayType.getId(), "name must match");
        assertTrue(waterwayType.toString().contains("Waterway"), "toString returns something with the name in it");
        assertFalse(waterwayType.equals(null), "waterwayType is not equal to null");
        assertFalse(waterwayType.equals("Hello world!"), "waterwayType is not equal to some String");

        // Try to create another waterwayType
        LinkType waterWayType2 = new LinkType("Waterway", null);
        assertTrue(waterwayType.equals(waterWayType2), "waterwayType2 is equal to the first");

        LinkType poorSurfaceLinkType = new LinkType("PoorSurfaceType", DefaultsNl.ROAD);
        poorSurfaceLinkType.addCompatibleGtuType(DefaultsNl.CAR);
        assertTrue(poorSurfaceLinkType.isOfType(DefaultsNl.ROAD), "poor road is of type ROAD");
        assertFalse(waterwayType.isCompatible(carType), "compatibility of waterway for car is false");
        assertNull(waterwayType.isCompatibleOnInfraLevel(carType), "compatibility of waterway for car is undecidable on level");
        GtuCompatibility<LinkType> compatibility = waterwayType.getGtuCompatibility();
        assertTrue(compatibility.isCompatible(DefaultsNl.SHIP), "compatibility allows SHIP");
    }

}
