package org.opentrafficsim.core.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.opentrafficsim.core.gtu.GTUType;

/**
 * Test constructor and methods of the LinkType class.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
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
    public final void testLinkType()
    {
        try
        {
            new LinkType("name", null);
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
        try
        {
            new LinkType(null, new ArrayList<GTUType>());
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
        GTUType car = new GTUType("Car", GTUType.VEHICLE);
        GTUType truck = new GTUType("Truck", GTUType.VEHICLE);
        GTUType catamaran = new GTUType("Catamaran", GTUType.BOAT);
        Collection<GTUType> carGroup = new ArrayList<>();
        carGroup.add(car);
        carGroup.add(truck);
        LinkType roadLinkType = new LinkType("Vehicles", carGroup);
        Collection<GTUType> boatGroup = new ArrayList<>();
        boatGroup.add(catamaran);
        LinkType waterwayType = new LinkType("Waterway", boatGroup);
        assertTrue("equals to itself", roadLinkType.equals(roadLinkType));
        assertFalse("not equal to the other", roadLinkType.equals(waterwayType));
        assertTrue("Car is compatible with roadLinkType", roadLinkType.isCompatible(car));
        assertTrue("Truck is compatible with roadLinkType", roadLinkType.isCompatible(truck));
        assertFalse("Catamaran is not compatible with roadLinkType", roadLinkType.isCompatible(catamaran));
        assertFalse("Truck is not compatible with waterwayLinkType", waterwayType.isCompatible(truck));
        assertTrue("Catamaran is not compatible with waterwayLinkType", waterwayType.isCompatible(catamaran));
        Collection<GTUType> allGTUTypeGroup = new ArrayList<>();
        allGTUTypeGroup.add(GTUType.ALL);
        LinkType allLinkType = new LinkType("all", allGTUTypeGroup);
        assertTrue("Car is compatible with allLinkType", allLinkType.isCompatible(car));
        assertTrue("Truck is compatible with allLinkType", allLinkType.isCompatible(truck));
        assertTrue("Catamaran is compatible with allLinkType", allLinkType.isCompatible(catamaran));
        allLinkType = LinkType.ALL;
        assertTrue("Car is compatible with pre-defined ALL LinkType", allLinkType.isCompatible(car));
        assertTrue("Truck is compatible with pre-defined ALL LinkType", allLinkType.isCompatible(truck));
        assertTrue("Catamaran is compatible with pre-defined ALL LinkType", allLinkType.isCompatible(catamaran));
        LinkType lava = LinkType.NONE;
        assertFalse("Car is not compatible with lava", lava.isCompatible(car));
        assertFalse("Truck is not compatible with lava", lava.isCompatible(truck));
        assertFalse("Catamaran is not compatible with lava", lava.isCompatible(catamaran));
        assertEquals("name must match", "Waterway", waterwayType.getId());
        assertTrue("toString returns something with the name in it", waterwayType.toString().contains("Waterway"));
        assertFalse("waterwayType is not equal to null", waterwayType.equals(null));
        assertFalse("waterwayType is not equal to some String", waterwayType.equals("Hello world!"));
        assertFalse("waterwayType is not equal to lava", waterwayType.equals(lava));
        // Try to create another waterwayType
        LinkType waterwayType2 = new LinkType("Waterway", boatGroup);
        assertTrue("waterwayType2 is equal to the first", waterwayType.equals(waterwayType2));
    }

}
