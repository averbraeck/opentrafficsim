package org.opentrafficsim.road.mock;

import org.mockito.Mockito;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTU;

/**
 * MockGTU.java. <br>
 * <br>
 * Copyright (c) 2003-2019 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
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
        super();
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
