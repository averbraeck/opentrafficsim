package org.opentrafficsim.kpi.sampling.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.djunits.value.vdouble.scalar.Speed;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.impl.TestGtuData;

/**
 * Test filters.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class FiltersTest
{

    /** */
    private FiltersTest()
    {
        // do not instantiate test class
    }

    /**
     * Test filters.
     */
    @Test
    public void testFilters()
    {
        TestGtuData gtu = new TestGtuData("id", "origin", "destination", "gtuType", "route", Speed.instantiateSI(10.0));
        testFilter(new FilterDataOrigin(), "origin", gtu);
        testFilter(new FilterDataDestination(), "destination", gtu);
        testFilter(new FilterDataGtuType(), "gtuType", gtu);
        testFilter(new FilterDataRoute(), "route", gtu);
        testFilter(new FilterDataType<>("test", "test", String.class)
        {
            @Override
            public String getValue(final GtuData gtu)
            {
                return "test";
            }
        }, "test", gtu);
    }

    /**
     * Test filter.
     * @param filter filter
     * @param value value it should give
     * @param gtu GTU
     */
    private void testFilter(final FilterDataType<?, GtuData> filter, final String value, final TestGtuData gtu)
    {
        assertNotNull(filter.getId());
        assertNotNull(filter.getDescription());
        assertNotNull(filter.toString());
        assertEquals(filter.getType(), String.class);
        assertEquals(filter.getValue(gtu), value);
    }

    /**
     * Test equals method.
     */
    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testEquals()
    {
        FilterDataGtuType filter = new FilterDataGtuType();
        assertTrue(filter.equals(filter));
        assertTrue(filter.equals(new FilterDataGtuType()));
        assertFalse(filter.equals(null));
        assertFalse(filter.equals("Not a filter."));
        FilterDataType<String, GtuData> type2 = new FilterDataType<>("different", "filter", String.class)
        {
            @Override
            public String getValue(final GtuData gtu)
            {
                return "";
            }
        };
        assertFalse(filter.equals(type2));
        class TestType extends FilterDataType<String, TestGtuData>
        {
            /**
             * Constructor.
             * @param id id
             */
            TestType(final String id)
            {
                super(id, "description", String.class);
            }

            @Override
            public String getValue(final TestGtuData gtu)
            {
                return null;
            }
        }
        assertFalse(new TestType("a").equals(new TestType("b")));
    }

}
