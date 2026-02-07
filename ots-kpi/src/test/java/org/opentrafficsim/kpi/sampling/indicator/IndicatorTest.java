package org.opentrafficsim.kpi.sampling.indicator;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.djunits.value.vdouble.scalar.Speed;
import org.junit.jupiter.api.Test;

/**
 * Indicator test.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class IndicatorTest
{

    /** */
    private IndicatorTest()
    {
        // do not instantiate test class
    }

    /**
     * Tests toString methods. Calculations are part of SamplerTest.testQuery().
     */
    @Test
    public void indicatorTest()
    {
        TotalTravelTime ttt = new TotalTravelTime();
        assertNotNull(ttt.toString());
        TotalTravelDistance ttd = new TotalTravelDistance();
        assertNotNull(ttd.toString());
        MeanSpeed ms = new MeanSpeed(ttd, ttt);
        assertNotNull(ms.toString());

        assertNotNull(new TotalNumberOfStops().toString());
        assertNotNull(new TotalDelayReference().toString());
        assertNotNull(new TotalDelay(Speed.ZERO).toString());
        assertNotNull(new MeanTripLength().toString());
        assertNotNull(new MeanTravelTimePerDistance(ms).toString());
        assertNotNull(new MeanIntensity(ttd).toString());
        assertNotNull(new MeanDensity(ttt).toString());
    }

}
