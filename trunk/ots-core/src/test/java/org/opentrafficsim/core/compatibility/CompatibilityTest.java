package org.opentrafficsim.core.compatibility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.OTSNetwork;

/**
 * Test the classes and interfaces in the compatibility package.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CompatibilityTest
{

    /**
     * Test the interface.
     */
    @Test
    public void testInteface()
    {
        OTSNetwork network = new OTSNetwork("CompatibilityTestNetwork", true);
        assertTrue("EVERYTHING returns true for any GTU type",
                Compatible.EVERYTHING.isCompatible(network.getGtuType(GTUType.DEFAULTS.CAR), GTUDirectionality.DIR_PLUS));
        assertTrue("EVERYTHING returns true for any GTU type",
                Compatible.EVERYTHING.isCompatible(network.getGtuType(GTUType.DEFAULTS.CAR), GTUDirectionality.DIR_MINUS));
        assertTrue("EVERYTHING returns true for any GTU type",
                Compatible.EVERYTHING.isCompatible(network.getGtuType(GTUType.DEFAULTS.SHIP), GTUDirectionality.DIR_PLUS));
        assertTrue("EVERYTHING returns true for any GTU type",
                Compatible.EVERYTHING.isCompatible(network.getGtuType(GTUType.DEFAULTS.SHIP), GTUDirectionality.DIR_MINUS));
        assertTrue("PLUS returns true for any GTU type in DIR_PLUS",
                Compatible.PLUS.isCompatible(network.getGtuType(GTUType.DEFAULTS.CAR), GTUDirectionality.DIR_PLUS));
        assertFalse("PLUS returns false for any GTU type in DIR_MINUS",
                Compatible.PLUS.isCompatible(network.getGtuType(GTUType.DEFAULTS.CAR), GTUDirectionality.DIR_MINUS));
        assertTrue("PLUS returns true for any GTU type in DIR_PLUS",
                Compatible.PLUS.isCompatible(network.getGtuType(GTUType.DEFAULTS.SHIP), GTUDirectionality.DIR_PLUS));
        assertFalse("PLUS returns false for any GTU type in DIR_MINUS",
                Compatible.PLUS.isCompatible(network.getGtuType(GTUType.DEFAULTS.SHIP), GTUDirectionality.DIR_MINUS));
        assertFalse("MINUS returns false for any GTU type in DIR_PLUS",
                Compatible.MINUS.isCompatible(network.getGtuType(GTUType.DEFAULTS.CAR), GTUDirectionality.DIR_PLUS));
        assertTrue("MINUS returns true for any GTU type in DIR_MINUS",
                Compatible.MINUS.isCompatible(network.getGtuType(GTUType.DEFAULTS.CAR), GTUDirectionality.DIR_MINUS));
        assertFalse("MINUS returns false for any GTU type in DIR_PLUS",
                Compatible.MINUS.isCompatible(network.getGtuType(GTUType.DEFAULTS.SHIP), GTUDirectionality.DIR_PLUS));
        assertTrue("MINUS returns true for any GTU type in DIR_MINUS",
                Compatible.MINUS.isCompatible(network.getGtuType(GTUType.DEFAULTS.SHIP), GTUDirectionality.DIR_MINUS));
    }

    /**
     * Test the class.
     */
    @SuppressWarnings({ "unlikely-arg-type" })
    @Test
    public void testClass()
    {
        OTSNetwork network = new OTSNetwork("CompatibilityTestNetwork", true);
        LinkType linkType = network.getLinkType(LinkType.DEFAULTS.FREEWAY);
        GTUCompatibility<LinkType> compatibility = new GTUCompatibility<>(linkType);
        assertNull("Freshly initialized compatibility does not know about any GTUType",
                compatibility.isCompatible(network.getGtuType(GTUType.DEFAULTS.CAR), GTUDirectionality.DIR_PLUS));
        assertNull("Freshly initialized compatibility does not know about any GTUType",
                compatibility.isCompatible(network.getGtuType(GTUType.DEFAULTS.CAR), GTUDirectionality.DIR_MINUS));
        compatibility.addAllowedGTUType(network.getGtuType(GTUType.DEFAULTS.CAR), LongitudinalDirectionality.DIR_PLUS);
        assertTrue("now compatible with CAR in DIR_PLUS",
                compatibility.isCompatible(network.getGtuType(GTUType.DEFAULTS.CAR), GTUDirectionality.DIR_PLUS));
        assertFalse("now incompatible with CAR in DIR_MINUS",
                compatibility.isCompatible(network.getGtuType(GTUType.DEFAULTS.CAR), GTUDirectionality.DIR_MINUS));
        compatibility.removeAllowedGTUType(network.getGtuType(GTUType.DEFAULTS.CAR), LongitudinalDirectionality.DIR_PLUS);
        assertNull("After remove, compatibility does not know about any GTUType",
                compatibility.isCompatible(network.getGtuType(GTUType.DEFAULTS.CAR), GTUDirectionality.DIR_PLUS));
        assertNull("After remove, compatibility does not know about any GTUType",
                compatibility.isCompatible(network.getGtuType(GTUType.DEFAULTS.CAR), GTUDirectionality.DIR_MINUS));
        compatibility.addAllowedGTUType(network.getGtuType(GTUType.DEFAULTS.SHIP), LongitudinalDirectionality.DIR_PLUS);
        assertNull("Compatibility does not know about CAR",
                compatibility.isCompatible(network.getGtuType(GTUType.DEFAULTS.CAR), GTUDirectionality.DIR_PLUS));
        assertNull("Compatibility does not know about CAR",
                compatibility.isCompatible(network.getGtuType(GTUType.DEFAULTS.CAR), GTUDirectionality.DIR_MINUS));
        compatibility.addAllowedGTUType(network.getGtuType(GTUType.DEFAULTS.CAR), LongitudinalDirectionality.DIR_MINUS);
        assertFalse("now incompatible with CAR in DIR_PLUS",
                compatibility.isCompatible(network.getGtuType(GTUType.DEFAULTS.CAR), GTUDirectionality.DIR_PLUS));
        assertTrue("now compatible with CAR in DIR_MINUS",
                compatibility.isCompatible(network.getGtuType(GTUType.DEFAULTS.CAR), GTUDirectionality.DIR_MINUS));
        GTUCompatibility<LinkType> copy = new GTUCompatibility<>(compatibility);
        assertTrue("copy equals original", copy.equals(compatibility));
        // Remove CAr from original
        assertEquals("hashCode of copy is equal to hashcode of original", copy.hashCode(), compatibility.hashCode());
        compatibility.removeAllowedGTUType(network.getGtuType(GTUType.DEFAULTS.CAR), LongitudinalDirectionality.DIR_MINUS);
        // It is EXTREMELY unlikely that hash codes match after removal; so we'll test that as well
        assertFalse("copy no longer equal to original", copy.equals(compatibility));
        assertNotEquals("Hash codes should be different", copy.hashCode(), compatibility.hashCode());
        // Check that it is still there in the copy
        assertFalse("now incompatible with CAR in DIR_PLUS",
                copy.isCompatible(network.getGtuType(GTUType.DEFAULTS.CAR), GTUDirectionality.DIR_PLUS));
        assertTrue("now compatible with CAR in DIR_MINUS",
                copy.isCompatible(network.getGtuType(GTUType.DEFAULTS.CAR), GTUDirectionality.DIR_MINUS));
        assertNull("After remove, compatibility does not know about CAR",
                compatibility.isCompatible(network.getGtuType(GTUType.DEFAULTS.CAR), GTUDirectionality.DIR_PLUS));
        assertNull("After remove, compatibility does not know about CAR",
                compatibility.isCompatible(network.getGtuType(GTUType.DEFAULTS.CAR), GTUDirectionality.DIR_MINUS));
        compatibility.addAllowedGTUType(network.getGtuType(GTUType.DEFAULTS.CAR), LongitudinalDirectionality.DIR_BOTH);
        assertTrue("now compatible with CAR in DIR_PLUS",
                compatibility.isCompatible(network.getGtuType(GTUType.DEFAULTS.CAR), GTUDirectionality.DIR_PLUS));
        assertTrue("now compatible with CAR in DIR_MINUS",
                compatibility.isCompatible(network.getGtuType(GTUType.DEFAULTS.CAR), GTUDirectionality.DIR_MINUS));
        compatibility.removeAllowedGTUType(network.getGtuType(GTUType.DEFAULTS.CAR), LongitudinalDirectionality.DIR_BOTH);
        compatibility.addAllowedGTUType(network.getGtuType(GTUType.DEFAULTS.CAR), LongitudinalDirectionality.DIR_NONE);
        assertFalse("now incompatible with CAR in DIR_PLUS",
                compatibility.isCompatible(network.getGtuType(GTUType.DEFAULTS.CAR), GTUDirectionality.DIR_PLUS));
        assertFalse("now incompatible with CAR in DIR_MINUS",
                compatibility.isCompatible(network.getGtuType(GTUType.DEFAULTS.CAR), GTUDirectionality.DIR_MINUS));
        // Additional tests for equals
        assertFalse("not equal to null", compatibility.equals(null));
        assertFalse("not equal to some other object", compatibility.equals("bla"));
        assertTrue("equal to itself", compatibility.equals(compatibility));
        // toString returns something descriptive
        assertTrue("The toString methods returns something descriptive",
                compatibility.toString().startsWith("GTUCompatibility"));
        // Hierarchical on GTUType
        compatibility = new GTUCompatibility<>(linkType);
        compatibility.addAllowedGTUType(network.getGtuType(GTUType.DEFAULTS.BUS), LongitudinalDirectionality.DIR_PLUS);
        assertNull("Not directly compatible with scheduled bus",
                compatibility.isCompatible(network.getGtuType(GTUType.DEFAULTS.SCHEDULED_BUS), GTUDirectionality.DIR_PLUS));
        assertNull("Not directly knowledgeable about scheduled bus",
                compatibility.getDirectionality(network.getGtuType(GTUType.DEFAULTS.SCHEDULED_BUS), false));
        assertEquals("Indirectly compatible with scheduled bus",
                compatibility.getDirectionality(network.getGtuType(GTUType.DEFAULTS.SCHEDULED_BUS), true),
                LongitudinalDirectionality.DIR_PLUS);
        assertEquals("Not directly or indirectly passable for SHIP",
                compatibility.getDirectionality(network.getGtuType(GTUType.DEFAULTS.SHIP), true),
                LongitudinalDirectionality.DIR_NONE);
        // System.out.println(linkType);
        // System.out.println(linkType.getParent());
        assertEquals("Indirectly compatible with PEDESTRIAN",
                compatibility.getDirectionality(network.getGtuType(GTUType.DEFAULTS.PEDESTRIAN), true),
                LongitudinalDirectionality.DIR_BOTH);
        assertNull("Not directly compatible with PEDESTRIAN",
                compatibility.getDirectionality(network.getGtuType(GTUType.DEFAULTS.PEDESTRIAN), false));
        
        // TODO Test isCompatibleWith method (OR remove that method from the class; it is never used).
    }

}
