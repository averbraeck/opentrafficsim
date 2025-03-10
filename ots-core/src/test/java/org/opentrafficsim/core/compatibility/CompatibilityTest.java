package org.opentrafficsim.core.compatibility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.HierarchicallyTyped;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.gtu.GtuType;

/**
 * Test the classes and interfaces in the compatibility package.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class CompatibilityTest
{

    /** */
    private CompatibilityTest()
    {
        // do not instantiate test class
    }

    /**
     * Test Compatibility and GtuCompatibility.
     */
    @SuppressWarnings({"unchecked", "rawtypes", "unlikely-arg-type"})
    @Test
    public void testCompatibility()
    {
        TestRoadType pathType = new TestRoadType("path");
        TestRoadType bikeType = new TestRoadType("bike", pathType);
        TestRoadType streetType = new TestRoadType("street", pathType);
        TestRoadType provincialType = new TestRoadType("provincial", streetType);

        pathType.addCompatibleGtuType(DefaultsNl.ROAD_USER);
        bikeType.addIncompatibleGtuType(DefaultsNl.VEHICLE);
        provincialType.addIncompatibleGtuType(DefaultsNl.BICYCLE);
        provincialType.addIncompatibleGtuType(DefaultsNl.PEDESTRIAN);

        assertNull(pathType.isCompatibleOnInfraLevel(DefaultsNl.BICYCLE));
        assertTrue(pathType.isCompatibleOnInfraLevel(DefaultsNl.ROAD_USER));
        assertFalse(provincialType.isCompatibleOnInfraLevel(DefaultsNl.BICYCLE));
        assertFalse(bikeType.isCompatible(DefaultsNl.VEHICLE));
        assertTrue(bikeType.isCompatible(DefaultsNl.BICYCLE));
        assertFalse(provincialType.isCompatible(DefaultsNl.BICYCLE));
        assertFalse(provincialType.isCompatible(new GtuType("NEW")));

        assertEquals(pathType.getInfrastructure(), pathType);
        assertEquals(pathType.isCompatible(DefaultsNl.ROAD_USER),
                pathType.getGtuCompatibility().isCompatible(DefaultsNl.ROAD_USER));
        assertEquals(pathType.getGtuCompatibility(), new GtuCompatibility(pathType.getGtuCompatibility()));

        provincialType.getGtuCompatibility().hashCode();
        provincialType.getGtuCompatibility().toString();
        assertTrue(provincialType.getGtuCompatibility().equals(provincialType.getGtuCompatibility()));
        assertFalse(provincialType.getGtuCompatibility().equals(null));
        assertFalse(provincialType.getGtuCompatibility().equals("String"));
        assertFalse(provincialType.getGtuCompatibility().equals(pathType.getGtuCompatibility()));
    }

    /** Test HierarchicallyTyped. */
    static class TestRoad implements HierarchicallyTyped<TestRoadType, TestRoad>
    {
        /** Deicing method. */
        private final TestRoadType deicingMethod;

        /**
         * Constructor.
         * @param deicingMethod deicing method
         */
        TestRoad(final TestRoadType deicingMethod)
        {
            this.deicingMethod = deicingMethod;
        }

        @Override
        public TestRoadType getType()
        {
            return this.deicingMethod;
        }
    }

    /** Test GtuCompatibleInfraType. */
    static class TestRoadType extends GtuCompatibleInfraType<TestRoadType, TestRoad>
    {
        /** */
        private static final long serialVersionUID = 20241108L;

        /**
         * Instantiate an infrastructure type.
         * @param id the id
         * @param parent the parent
         */
        TestRoadType(final String id, final TestRoadType parent)
        {
            super(id, parent);
        }

        /**
         * Instantiate an infrastructure type without a parent.
         * @param id the id
         */
        TestRoadType(final String id)
        {
            super(id);
        }
    }
}
