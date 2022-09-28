package org.opentrafficsim.road.mock;

import org.mockito.Mockito;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTU;

/**
 * MockGTU.java.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class MockGTU
{
    /** mocked GTU. */
    private GTU mockGTU;

    /** name. */
    private String name;

    /** mocked simulator. */
    private OTSSimulatorInterface simulator = MockDEVSSimulator.createMock();

    /**
     * @param name the name
     */
    public MockGTU(final String name)
    {
        this.name = name;
        this.mockGTU = Mockito.mock(GTU.class);
        Mockito.when(this.mockGTU.getSimulator()).thenReturn(this.simulator);
        Mockito.when(this.mockGTU.getId()).thenReturn(this.name);
    }

    /**
     * @return mocked DEVSSimulator
     */
    public GTU getMock()
    {
        return this.mockGTU;
    }

}
