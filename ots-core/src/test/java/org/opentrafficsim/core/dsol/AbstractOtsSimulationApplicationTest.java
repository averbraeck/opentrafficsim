package org.opentrafficsim.core.dsol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.dsol.AbstractOtsModelTest.OtsModel;

/**
 * Test the OTSReplication class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class AbstractOtsSimulationApplicationTest
{

    /**
     * Test the AbstractOtsSimulationApplication class.
     */
    @Test
    public void abstractOtsSimulationApplicationTest()
    {
        OtsSimulatorInterface simulator = new OtsSimulator("Simulator for AbstractOtsSimulationApplicationlTest");
        OtsModel model = new OtsModel(simulator);
        OtsSimulationApplication otsSimulationApplication = new OtsSimulationApplication(model);
        assertEquals(model, otsSimulationApplication.getModel(), "model can be retrieved");
    }

    /**
     * Instrumented class for testing.
     */
    static class OtsSimulationApplication extends AbstractOtsSimulationApplication
    {

        /** ... */
        private static final long serialVersionUID = 1L;

        /**
         * Construct a OtsSimulationApplication object.
         * @param model OtsModelInterface; the model
         */
        OtsSimulationApplication(final OtsModelInterface model)
        {
            super(model);
        }

    }
}
