package org.opentrafficsim.core.dsol;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opentrafficsim.core.dsol.AbstractOtsModelTest.OTSModel;

/**
 * Test the OTSReplication class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class AbstractOtsSimulationApplicationTest
{

    /**
     * Test the AbstractOTSSimulationApplication class.
     */
    @Test
    public void abstractOTSSimulationApplicationTest()
    {
        OtsSimulatorInterface simulator = new OtsSimulator("Simulator for AbstractOTSSimulationApplicationlTest");
        OTSModel model = new OTSModel(simulator);
        OTSSimulationApplication otsSimulationApplication = new OTSSimulationApplication(model);
        assertEquals("model can be retrieved", model, otsSimulationApplication.getModel());
    }

    /**
     * Instrumented class for testing.
     */
    static class OTSSimulationApplication extends AbstractOtsSimulationApplication
    {

        /** ... */
        private static final long serialVersionUID = 1L;

        /**
         * Construct a OTSSimulationApplication object.
         * @param model OTSModelInterface; the model
         */
        OTSSimulationApplication(final OtsModelInterface model)
        {
            super(model);
        }

    }
}
