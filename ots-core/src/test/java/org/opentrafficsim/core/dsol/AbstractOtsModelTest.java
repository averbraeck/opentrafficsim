package org.opentrafficsim.core.dsol;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;

import org.junit.Test;
import org.opentrafficsim.core.network.OtsNetwork;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Test the AbstractOtsModel class.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class AbstractOtsModelTest
{

    /**
     * Test the AbstractOtsModel class.
     */
    @Test
    public void abstractOtsModelTest()
    {
        OtsSimulatorInterface simulator = new OtsSimulator("Simulator for AbstractOtsModelTest");
        String shortName = "shortName";
        String description = "description";
        AbstractOtsModel model = new OtsModel(simulator, shortName, description);
        assertEquals("simulator is returned", simulator, model.getSimulator());
        assertEquals("short name is returned", shortName, model.getShortName());
        assertEquals("description is returned", description, model.getDescription());
        model = new OtsModel(simulator);
        assertEquals("simulator is returned", simulator, model.getSimulator());
        assertEquals("short name is name of the extending class", "OtsModel", model.getShortName());
        assertEquals("description is name of the extending class", "OtsModel", model.getDescription());
        String newShortName = "newShortName";
        model.setShortName(newShortName);
        assertEquals("simulator is returned", simulator, model.getSimulator());
        assertEquals("short name is new short name", newShortName, model.getShortName());
        assertEquals("description is name of the extending class", "OtsModel", model.getDescription());
        String newDescription = "newDescription";
        model.setDescription(newDescription);
        assertEquals("simulator is returned", simulator, model.getSimulator());
        assertEquals("short name is new short name", newShortName, model.getShortName());
        assertEquals("description is new description", newDescription, model.getDescription());

    }

    /**
     * OTS model for testing.
     */
    static class OtsModel extends AbstractOtsModel
    {
        /** ... */
        private static final long serialVersionUID = 1L;

        /**
         * Construct the instrumented OtsModel.
         * @param simulator the simulator
         * @param shortName the name of the model
         * @param description the description of the model
         */
        OtsModel(final OtsSimulatorInterface simulator, final String shortName, final String description)
        {
            super(simulator, shortName, description);
        }

        /**
         * Construct the instrumented OtsModel.
         * @param simulator the simulator
         */
        OtsModel(final OtsSimulatorInterface simulator)
        {
            super(simulator);
        }

        @Override
        public OtsNetwork getNetwork()
        {
            return null;
        }

        @Override
        public void constructModel() throws SimRuntimeException
        {
            // Do nothing
        }

    }

}
