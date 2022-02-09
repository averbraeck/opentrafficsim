package org.opentrafficsim.core.dsol;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opentrafficsim.core.dsol.AbstractOTSModelTest.OTSModel;

/**
 * Test the OTSReplication class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Feb 13, 2020 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class AbstractOTSSimulationApplicationTest
{

    /**
     * Test the AbstractOTSSimulationApplication class.
     */
    @Test
    public void abstractOTSSimulationApplicationTest()
    {
        OTSSimulatorInterface simulator = new OTSSimulator("Simulator for AbstractOTSSimulationApplicationlTest");
        OTSModel model = new OTSModel(simulator);
        OTSSimulationApplication otsSimulationApplication = new OTSSimulationApplication(model);
        assertEquals("model can be retrieved", model, otsSimulationApplication.getModel());
    }

    /**
     * Instrumented class for testing.
     */
    static class OTSSimulationApplication extends AbstractOTSSimulationApplication
    {

        /** ... */
        private static final long serialVersionUID = 1L;

        /**
         * Construct a OTSSimulationApplication object.
         * @param model OTSModelInterface; the model
         */
        OTSSimulationApplication(final OTSModelInterface model)
        {
            super(model);
        }

    }
}
