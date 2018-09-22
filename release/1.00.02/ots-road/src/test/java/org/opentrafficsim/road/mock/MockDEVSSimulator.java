package org.opentrafficsim.road.mock;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Time;
import org.mockito.Mockito;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

/**
 * MockSimulator.java. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
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
    public static DEVSSimulatorInterface.TimeDoubleUnit createMock()
    {
        DEVSSimulatorInterface.TimeDoubleUnit mockSimulator = Mockito.mock(DEVSSimulatorInterface.TimeDoubleUnit.class);
        Mockito.when(mockSimulator.getSimulatorTime()).thenReturn(new Time(0.0, TimeUnit.BASE));
        return mockSimulator;
    }
}
