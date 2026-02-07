package org.opentrafficsim.core.definitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.djutils.test.UnitTest;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.object.DetectorType;

import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Defaults test.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class DefaultsTest
{

    /** */
    private DefaultsTest()
    {
        // do not instantiate test class
    }

    /**
     * Tests DefaultsNl.
     */
    @Test
    public void testNl()
    {
        StreamInterface stream = new MersenneTwister(1L);
        assertNotNull(Defaults.NL.apply(DefaultsNl.CAR, stream).get());
        assertNotNull(Defaults.NL.apply(DefaultsNl.TRUCK, stream).get());
        assertNotNull(Defaults.NL.apply(DefaultsNl.BUS, stream).get());
        assertNotNull(Defaults.NL.apply(DefaultsNl.VAN, stream).get());
        assertNotNull(Defaults.NL.apply(DefaultsNl.EMERGENCY_VEHICLE, stream).get());
        assertNotNull(Defaults.NL.apply(DefaultsNl.MOTORCYCLE, stream).get());
        assertNotNull(Defaults.NL.apply(DefaultsNl.BICYCLE, stream).get());
        assertTrue(Defaults.NL.apply(new GtuType("NEW"), stream).isEmpty());

        assertEquals(Defaults.NL.getLocale().getCountry(), "NL");
        assertNotNull(Defaults.getByName(GtuType.class, "NL.CAR").get());
        assertNotNull(Defaults.getByName(GtuType.class, "NL.TRUCK").get());
        assertNotNull(Defaults.getByName(GtuType.class, "NL.BUS").get());
        assertNotNull(Defaults.getByName(GtuType.class, "NL.VAN").get());
        assertNotNull(Defaults.getByName(GtuType.class, "NL.EMERGENCY_VEHICLE").get());
        assertNotNull(Defaults.getByName(GtuType.class, "NL.MOTORCYCLE").get());
        assertNotNull(Defaults.getByName(GtuType.class, "NL.BICYCLE").get());
        assertNull(Defaults.getByName(GtuType.class, "NL.NOT_A_TYPE").orElse(null));
    }

    /**
     * Tests Definitions.
     */
    @Test
    public void definitionsTest()
    {
        Definitions defs = new Definitions();

        UnitTest.testFail(() -> defs.add(GtuType.class, null), NullPointerException.class);
        UnitTest.testFail(() -> defs.add(null, DefaultsNl.CAR), NullPointerException.class);
        defs.add(GtuType.class, DefaultsNl.CAR);

        UnitTest.testFail(() -> defs.get(GtuType.class, null), NullPointerException.class);
        UnitTest.testFail(() -> defs.get(null, "NL.CAR"), NullPointerException.class);
        assertEquals(DefaultsNl.CAR, defs.get(GtuType.class, "NL.CAR").get());
        assertNull(defs.get(GtuType.class, "NL.NOT_A_TYPE").orElse(null));
        assertNull(defs.get(LinkType.class, "NL.HIGHWAY").orElse(null));

        assertFalse(defs.getAll(GtuType.class).isEmpty());
        assertTrue(defs.getAll(DetectorType.class).isEmpty());
    }

}
