package org.opentrafficsim.core.dsol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Serializable;

import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.network.Network;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Test the AbstractOtsModel class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
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
        assertEquals(simulator, model.getSimulator(), "simulator is returned");
        assertEquals(shortName, model.getShortName(), "short name is returned");
        assertEquals(description, model.getDescription(), "description is returned");
        model = new OtsModel(simulator);
        assertEquals(simulator, model.getSimulator(), "simulator is returned");
        assertEquals("OtsModel", model.getShortName(), "short name is name of the extending class");
        assertEquals("OtsModel", model.getDescription(), "description is name of the extending class");
        String newShortName = "newShortName";
        model.setShortName(newShortName);
        assertEquals(simulator, model.getSimulator(), "simulator is returned");
        assertEquals(newShortName, model.getShortName(), "short name is new short name");
        assertEquals("OtsModel", model.getDescription(), "description is name of the extending class");
        String newDescription = "newDescription";
        model.setDescription(newDescription);
        assertEquals(simulator, model.getSimulator(), "simulator is returned");
        assertEquals(newShortName, model.getShortName(), "short name is new short name");
        assertEquals(newDescription, model.getDescription(), "description is new description");

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
        public Network getNetwork()
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
