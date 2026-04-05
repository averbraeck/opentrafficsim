package org.opentrafficsim.road.network.speed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Speed;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.gtu.GtuType;

/**
 * Test for LaneSpeedLimits.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LaneSpeedLimitsTest
{

    /**
     * Test for LaneSpeedLimits.
     */
    @Test
    void testLaneSpeedLimits()
    {
        Map<GtuType, Speed> map = new LinkedHashMap<>();
        map.put(DefaultsNl.TRUCK, new Speed(80.0, SpeedUnit.KM_PER_HOUR));
        GtuType myTruck = new GtuType("myTruck", DefaultsNl.TRUCK);

        Duration t0 = Duration.ZERO;
        Duration t3 = new Duration(3.0, DurationUnit.HOUR);
        Duration t6 = new Duration(6.0, DurationUnit.HOUR);
        Duration t10 = new Duration(10.0, DurationUnit.HOUR);
        Duration t19 = new Duration(19.0, DurationUnit.HOUR);
        Duration t21 = new Duration(21.0, DurationUnit.HOUR);

        SpeedLimit sl80 = SpeedLimit.of(new Speed(80.0, SpeedUnit.KM_PER_HOUR));
        SpeedLimit sl100 = SpeedLimit.of(new Speed(100.0, SpeedUnit.KM_PER_HOUR));
        SpeedLimit sl130 = SpeedLimit.of(new Speed(130.0, SpeedUnit.KM_PER_HOUR));

        LaneSpeedLimits limits = new LaneSpeedLimits(map);
        assertEquals(sl(null, null), limits.getSpeedLimits(DefaultsNl.CAR, t0));
        assertTrue(limits.getSpeedLimit(t0).isEmpty());
        assertTrue(limits.getSpeedLimit(t6).isEmpty());
        limits.addSpeedLimit(sl100.speed());
        assertEquals(sl100, limits.getSpeedLimit(t0).get());
        assertEquals(sl100, limits.getSpeedLimit(t6).get());
        assertEquals(sl(sl100, null), limits.getSpeedLimits(DefaultsNl.CAR, t0));
        assertEquals(sl(sl100, sl80), limits.getSpeedLimits(DefaultsNl.TRUCK, t0));
        assertEquals(sl(sl100, sl80), limits.getSpeedLimits(myTruck, t0));
        assertEquals(sl(sl100, null), limits.getSpeedLimits(DefaultsNl.CAR, t6));
        assertEquals(sl(sl100, sl80), limits.getSpeedLimits(DefaultsNl.TRUCK, t6));
        assertEquals(sl(sl100, sl80), limits.getSpeedLimits(myTruck, t6));

        limits = new LaneSpeedLimits(map);
        limits.addSpeedLimit(t6, sl100.speed());
        limits.addSpeedLimit(t19, sl130.speed());
        assertEquals(sl(sl130, null), limits.getSpeedLimits(DefaultsNl.CAR, t0));
        assertEquals(sl(sl130, sl80), limits.getSpeedLimits(DefaultsNl.TRUCK, t0));
        assertEquals(sl(sl130, null), limits.getSpeedLimits(DefaultsNl.CAR, t3));
        assertEquals(sl(sl130, sl80), limits.getSpeedLimits(DefaultsNl.TRUCK, t3));
        assertEquals(sl(sl100, null), limits.getSpeedLimits(DefaultsNl.CAR, t6));
        assertEquals(sl(sl100, sl80), limits.getSpeedLimits(DefaultsNl.TRUCK, t6));
        assertEquals(sl(sl100, null), limits.getSpeedLimits(DefaultsNl.CAR, t10));
        assertEquals(sl(sl100, sl80), limits.getSpeedLimits(DefaultsNl.TRUCK, t10));
        assertEquals(sl(sl130, null), limits.getSpeedLimits(DefaultsNl.CAR, t19));
        assertEquals(sl(sl130, sl80), limits.getSpeedLimits(DefaultsNl.TRUCK, t19));
        assertEquals(sl(sl130, null), limits.getSpeedLimits(DefaultsNl.CAR, t21));
        assertEquals(sl(sl130, sl80), limits.getSpeedLimits(DefaultsNl.TRUCK, t21));

        // enforced
        SpeedLimit sl80t = new SpeedLimit(new Speed(80.0, SpeedUnit.KM_PER_HOUR), true);
        SpeedLimit sl80f = new SpeedLimit(new Speed(80.0, SpeedUnit.KM_PER_HOUR), false);
        sl100 = new SpeedLimit(new Speed(100.0, SpeedUnit.KM_PER_HOUR), true);
        sl130 = new SpeedLimit(new Speed(130.0, SpeedUnit.KM_PER_HOUR), true);

        limits = new LaneSpeedLimits(map);
        limits.addSpeedLimit(sl100.speed(), true, true);
        assertEquals(sl(sl100, null), limits.getSpeedLimits(DefaultsNl.CAR, t0));
        assertEquals(sl(sl100, sl80t), limits.getSpeedLimits(DefaultsNl.TRUCK, t0));
        assertEquals(sl(sl100, sl80t), limits.getSpeedLimits(myTruck, t0));
        assertEquals(sl(sl100, null), limits.getSpeedLimits(DefaultsNl.CAR, t6));
        assertEquals(sl(sl100, sl80t), limits.getSpeedLimits(DefaultsNl.TRUCK, t6));
        assertEquals(sl(sl100, sl80t), limits.getSpeedLimits(myTruck, t6));

        limits = new LaneSpeedLimits(map);
        limits.addSpeedLimit(t6, sl100.speed(), true, true);
        limits.addSpeedLimit(t19, sl130.speed(), true, false);
        assertEquals(sl(sl130, null), limits.getSpeedLimits(DefaultsNl.CAR, t0));
        assertEquals(sl(sl130, sl80f), limits.getSpeedLimits(DefaultsNl.TRUCK, t0));
        assertEquals(sl(sl130, null), limits.getSpeedLimits(DefaultsNl.CAR, t3));
        assertEquals(sl(sl130, sl80f), limits.getSpeedLimits(DefaultsNl.TRUCK, t3));
        assertEquals(sl(sl100, null), limits.getSpeedLimits(DefaultsNl.CAR, t6));
        assertEquals(sl(sl100, sl80t), limits.getSpeedLimits(DefaultsNl.TRUCK, t6));
        assertEquals(sl(sl100, null), limits.getSpeedLimits(DefaultsNl.CAR, t10));
        assertEquals(sl(sl100, sl80t), limits.getSpeedLimits(DefaultsNl.TRUCK, t10));
        assertEquals(sl(sl130, null), limits.getSpeedLimits(DefaultsNl.CAR, t19));
        assertEquals(sl(sl130, sl80f), limits.getSpeedLimits(DefaultsNl.TRUCK, t19));
        assertEquals(sl(sl130, null), limits.getSpeedLimits(DefaultsNl.CAR, t21));
        assertEquals(sl(sl130, sl80f), limits.getSpeedLimits(DefaultsNl.TRUCK, t21));

        assertThrows(IllegalArgumentException.class,
                () -> new LaneSpeedLimits(map).addSpeedLimit(Duration.ofSI(-1.0), Speed.ONE, true, true));
        assertThrows(IllegalArgumentException.class,
                () -> new LaneSpeedLimits(map).addSpeedLimit(Duration.ofSI(24.0 * 3600.0 + 1.0), Speed.ONE, true, true));
    }

    /**
     * Create SpeedLimits.
     * @param laneSpeedLimit lane speed limit
     * @param gtuTypeSpeedLimit GTU type speed limit
     * @return SpeedLimits
     */
    private static SpeedLimits sl(final SpeedLimit laneSpeedLimit, final SpeedLimit gtuTypeSpeedLimit)
    {
        return new SpeedLimits(laneSpeedLimit, gtuTypeSpeedLimit);
    }

}
