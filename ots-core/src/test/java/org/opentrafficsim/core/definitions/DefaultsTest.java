package org.opentrafficsim.core.definitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.djutils.exceptions.Try;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.object.DetectorType;

import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Defaults test.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DefaultsTest
{

    /**
     * Constructor.
     */
    public DefaultsTest()
    {
        //
    }

    /**
     * Tests DefaultsNl.
     */
    @Test
    public void testNl()
    {
        StreamInterface stream = new MersenneTwister(1L);
        assertNotNull(Defaults.NL.apply(DefaultsNl.CAR, stream));
        assertNotNull(Defaults.NL.apply(DefaultsNl.TRUCK, stream));
        assertNotNull(Defaults.NL.apply(DefaultsNl.BUS, stream));
        assertNotNull(Defaults.NL.apply(DefaultsNl.VAN, stream));
        assertNotNull(Defaults.NL.apply(DefaultsNl.EMERGENCY_VEHICLE, stream));
        assertNotNull(Defaults.NL.apply(DefaultsNl.MOTORCYCLE, stream));
        assertNotNull(Defaults.NL.apply(DefaultsNl.BICYCLE, stream));
        Try.testFail(() -> Defaults.NL.apply(new GtuType("NEW"), stream), NullPointerException.class);

        assertEquals(Defaults.NL.getLocale().getCountry(), "NL");
        assertNotNull(Defaults.getByName(GtuType.class, "NL.CAR"));
        assertNotNull(Defaults.getByName(GtuType.class, "NL.TRUCK"));
        assertNotNull(Defaults.getByName(GtuType.class, "NL.BUS"));
        assertNotNull(Defaults.getByName(GtuType.class, "NL.VAN"));
        assertNotNull(Defaults.getByName(GtuType.class, "NL.EMERGENCY_VEHICLE"));
        assertNotNull(Defaults.getByName(GtuType.class, "NL.MOTORCYCLE"));
        assertNotNull(Defaults.getByName(GtuType.class, "NL.BICYCLE"));
        assertNull(Defaults.getByName(GtuType.class, "NL.NOT_A_TYPE"));
    }

    /**
     * Tests Definitions.
     */
    @Test
    public void definitionsTest()
    {
        Definitions defs = new Definitions();

        Try.testFail(() -> defs.add(GtuType.class, null), NullPointerException.class);
        Try.testFail(() -> defs.add(null, DefaultsNl.CAR), NullPointerException.class);
        defs.add(GtuType.class, DefaultsNl.CAR);

        Try.testFail(() -> defs.get(GtuType.class, null), NullPointerException.class);
        Try.testFail(() -> defs.get(null, "NL.CAR"), NullPointerException.class);
        assertEquals(DefaultsNl.CAR, defs.get(GtuType.class, "NL.CAR"));
        assertNull(defs.get(GtuType.class, "NL.NOT_A_TYPE"));
        assertNull(defs.get(LinkType.class, "NL.HIGHWAY"));

        assertFalse(defs.getAll(GtuType.class).isEmpty());
        assertTrue(defs.getAll(DetectorType.class).isEmpty());
    }

}
