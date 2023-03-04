package org.opentrafficsim.road.mock;

import org.mockito.Mockito;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.Gtu;

/**
 * MockGTU.java.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class MockGtu
{
    /** mocked GTU. */
    private Gtu mockGTU;

    /** name. */
    private String name;

    /** mocked simulator. */
    private OtsSimulatorInterface simulator = MockDevsSimulator.createMock();

    /**
     * @param name the name
     */
    public MockGtu(final String name)
    {
        this.name = name;
        this.mockGTU = Mockito.mock(Gtu.class);
        Mockito.when(this.mockGTU.getSimulator()).thenReturn(this.simulator);
        Mockito.when(this.mockGTU.getId()).thenReturn(this.name);
    }

    /**
     * @return mocked DEVSSimulator
     */
    public Gtu getMock()
    {
        return this.mockGTU;
    }

}
