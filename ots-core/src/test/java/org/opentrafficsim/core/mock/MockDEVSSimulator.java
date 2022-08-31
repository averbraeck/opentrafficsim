package org.opentrafficsim.core.mock;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.mockito.Mockito;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

/**
 * MockSimulator.java. <br>
 * <br>
 * Copyright (c) 2003-2022 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public final class MockDEVSSimulator
{
    /** */
    private MockDEVSSimulator()
    {
        // Utility class
    }

    /**
     * @return mocked DEVSSimulator
     */
    public static OTSSimulatorInterface createMock()
    {
        OTSSimulatorInterface mockSimulator = Mockito.mock(OTSSimulatorInterface.class);
        Mockito.when(mockSimulator.getSimulatorAbsTime()).thenReturn(Time.ZERO);
        Mockito.when(mockSimulator.getSimulatorTime()).thenReturn(Duration.ZERO);
        return mockSimulator;
    }
}
