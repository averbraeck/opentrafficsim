package org.opentrafficsim.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

/**
 * Test the basics of the HierarchicalType class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class HierarchicalTypeTest
{

    /** */
    private HierarchicalTypeTest()
    {
        // do not instantiate the test class
    }

    /**
     * Test the basics of the HierarchicalType class.
     */
    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testBasics()
    {
        SubType st = new SubType("id");
        assertEquals("id", st.getId(), "id check");
        assertNull(st.getParent().orElse(null), "parent type is null");
        assertTrue(st.isOfType(st), "type is type of st");
        assertFalse(st.isOfType(null), "type is not of type null");
        SubType st2 = new SubType("id2", st);
        assertEquals("id2", st2.getId(), "id check");
        assertEquals(st, st2.getParent().get(), "parent type is null");
        assertTrue(st2.isOfType(st), "parent is type of child");
        assertFalse(st.isOfType(st2), "childis not of type of parent");
        assertTrue(st.hashCode() != st2.hashCode(), "hash codes should be different");
        assertFalse(st.equals("String"), "st not equals to some String");
        SubType st3 = new SubType("id3", st);
        assertFalse(st2.equals(st3), "other subtype with different name is not equal");
        st3 = new SubType("id2", st);
        assertTrue(st2.equals(st3), "other subtype with same name and parent is equal");
        st3 = new SubType("id2");
        assertFalse(st2.equals(st3), "other subtype with same name but no parent is not equal");
        assertFalse(st3.equals(st2), "other subtype with same name but no parent is not equal");
        st3 = new SubType("id3", st2);
        SubType different = new SubType("different");
        assertNull(different.commonAncestor(st3).orElse(null), "No common ancestor");
        assertNull(st3.commonAncestor(different).orElse(null), "No common ancestor");
        SubType common = new SubType("common", st2);
        assertEquals(st2, common.commonAncestor(st3).get(), "Common ancestor");
        assertEquals(st2, st3.commonAncestor(common).get(), "Common ancestor");
        assertTrue(common.toString().startsWith("SubType "), "toString method returns something descriptive");
        try
        {
            new SubType(null, common);
            fail("Constructor with null for type name should have thrown a NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
        try
        {
            new SubType(null);
            fail("Constructor with null for type name should have thrown a NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
    }

    /** S. */
    static class S implements HierarchicallyTyped<SubType, S>
    {
        /** type. */
        private final SubType xType;

        /**
         * Constructor.
         * @param xType type
         */
        S(final SubType xType)
        {
            this.xType = xType;
        }

        @Override
        public SubType getType()
        {
            return this.xType;
        }
    }

    /**
     * Extend the HierarchicalType class so we instantiate (and then test) things.
     */
    static class SubType extends HierarchicalType<SubType, S>
    {
        /**
         * Construct a new SubType instance.
         * @param id id of the new SubType
         * @param parent parent of the new SubType instance
         * @throws NullPointerException ...
         */
        SubType(final String id, final SubType parent) throws NullPointerException
        {
            super(id, parent);
        }

        /**
         * Construct a new SubType instance.
         * @param id id of the new SubType
         * @throws NullPointerException ...
         */
        SubType(final String id) throws NullPointerException
        {
            super(id);
        }

        @Override
        public String toString()
        {
            return "SubType [id=" + getId() + ", parent=" + getParent() + "]";
        }
    }

    /** X. */
    static class X implements HierarchicallyTyped<XType, X>
    {
        /** type. */
        private final XType xType;

        /**
         * Constructor.
         * @param xType type
         */
        X(final XType xType)
        {
            this.xType = xType;
        }

        @Override
        public XType getType()
        {
            return this.xType;
        }
    }

    /** XType. */
    static class XType extends HierarchicalType<XType, X>
    {
        /**
         * Instantiate hierarchical type.
         * @param id the id
         * @param parent the parent or null
         * @throws NullPointerException when id is null
         */
        protected XType(final String id, final XType parent) throws NullPointerException
        {
            super(id, parent);
        }
    }

    /** Y. */
    static class Y implements HierarchicallyTyped<YType, Y>
    {
        /** type. */
        private final YType yType;

        /**
         * Constructor.
         * @param yType type
         */
        Y(final YType yType)
        {
            this.yType = yType;
        }

        @Override
        public YType getType()
        {
            return this.yType;
        }
    }

    /** YType. */
    static class YType extends HierarchicalType<YType, Y>
    {
        /**
         * Instantiate hierarchical type.
         * @param id the id
         * @param parent the parent or null
         * @throws NullPointerException when id is null
         */
        protected YType(final String id, final YType parent) throws NullPointerException
        {
            super(id, parent);
        }
    }

    /** ZType. */
    static class ZType extends HierarchicalType<XType, X>
    {
        /**
         * Instantiate hierarchical type.
         * @param id the id
         * @param parent the parent or null
         * @throws NullPointerException when id is null
         */
        protected ZType(final String id, final XType parent) throws NullPointerException
        {
            super(id, parent);
        }
    }

    /** Test different hierarchies XType, YType and ZType. */
    @Test
    public void testHierarchies()
    {
        XType vehicle = new XType("vehicle", null);
        assertNull(vehicle.getParent().orElse(null));
        assertEquals(0, vehicle.getChildren().size());
        XType car = new XType("car", vehicle);
        assertEquals(vehicle, car.getParent().get());
        assertEquals(1, vehicle.getChildren().size());
        XType truck = new XType("truck", vehicle);
        assertEquals(2, vehicle.getChildren().size());
        assertTrue(truck.isType(truck));
        assertFalse(car.isType(truck));
        assertFalse(vehicle.isOfType(car));
        assertTrue(car.isOfType(vehicle));
        YType road = new YType("road", null);
        YType highway = new YType("highway", road);
        assertTrue(highway.isOfType(road));
        assertFalse(road.isOfType(highway));
        // this should not compile: YType yt1 = new YType("yt1", vehicle);
        ZType zt1 = new ZType("zt1", car);
        assertEquals(car, zt1.getParent().get());
        assertTrue(zt1.isOfType(car));
        assertTrue(zt1.isOfType(vehicle));
        // Ztype is of course strange. So this should not compile: ZType zt2 = new ZType("zt2", zt1);

        // ofType tests
        X xCar = new X(car);
        X xVehicle = new X(vehicle);
        assertTrue(xCar.isOfType(vehicle));
        assertFalse(xCar.isOfType(truck));
        assertFalse(xVehicle.isOfType(car));
        // this should not compile: xCar.isOfType(highway);
    }
}
