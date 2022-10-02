package org.opentrafficsim.core.dsol;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;

import org.junit.Test;
import org.opentrafficsim.core.network.OTSNetwork;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Test the AbstractOTSModel class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class AbstractOTSModelTest
{

    /**
     * Test the AbstractOTSModel class.
     */
    @Test
    public void abstractOTSModelTest()
    {
        OtsSimulatorInterface simulator = new OtsSimulator("Simulator for AbstractOTSModelTest");
        String shortName = "shortName";
        String description = "description";
        AbstractOtsModel model = new OTSModel(simulator, shortName, description);
        assertEquals("simulator is returned", simulator, model.getSimulator());
        assertEquals("short name is returned", shortName, model.getShortName());
        assertEquals("description is returned", description, model.getDescription());
        model = new OTSModel(simulator);
        assertEquals("simulator is returned", simulator, model.getSimulator());
        assertEquals("short name is name of the extending class", "OTSModel", model.getShortName());
        assertEquals("description is name of the extending class", "OTSModel", model.getDescription());
        String newShortName = "newShortName";
        model.setShortName(newShortName);
        assertEquals("simulator is returned", simulator, model.getSimulator());
        assertEquals("short name is new short name", newShortName, model.getShortName());
        assertEquals("description is name of the extending class", "OTSModel", model.getDescription());
        String newDescription = "newDescription";
        model.setDescription(newDescription);
        assertEquals("simulator is returned", simulator, model.getSimulator());
        assertEquals("short name is new short name", newShortName, model.getShortName());
        assertEquals("description is new description", newDescription, model.getDescription());

    }

    /**
     * OTS model for testing.
     */
    static class OTSModel extends AbstractOtsModel
    {
        /** ... */
        private static final long serialVersionUID = 1L;

        /**
         * Construct the instrumented OTSModel.
         * @param simulator the simulator
         * @param shortName the name of the model
         * @param description the description of the model
         */
        OTSModel(final OtsSimulatorInterface simulator, final String shortName, final String description)
        {
            super(simulator, shortName, description);
        }

        /**
         * Construct the instrumented OTSModel.
         * @param simulator the simulator
         */
        OTSModel(final OtsSimulatorInterface simulator)
        {
            super(simulator);
        }

        @Override
        public OTSNetwork getNetwork()
        {
            return null;
        }

        @Override
        public void constructModel() throws SimRuntimeException
        {
            // Do nothing
        }

        @Override
        public Serializable getSourceId()
        {
            return "sourceID";
        }

    }

}
