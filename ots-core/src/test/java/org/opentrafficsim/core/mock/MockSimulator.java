package org.opentrafficsim.core.mock;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.mockito.Mockito;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;

/**
 * MockSimulator.java.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public final class MockSimulator
{
    /** */
    private MockSimulator()
    {
        // Utility class
    }

    /**
     * @return mocked Simulator
     */
    public static OtsSimulatorInterface createMock()
    {
        OtsSimulatorInterface result = Mockito.mock(OtsSimulatorInterface.class);
        Mockito.when(result.getSimulatorAbsTime()).thenReturn(Time.ZERO);
        Mockito.when(result.getSimulatorTime()).thenReturn(Duration.ZERO);
        return result;
    }
}
