package org.opentrafficsim.swing.gui;

import java.awt.Dimension;
import java.rmi.RemoteException;

import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.road.network.RoadNetwork;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.DsolException;

/**
 * Custom simulation uses the custom model class where the network and other simulation aspects are externally specified.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 */
public class CustomSimulation extends OtsSimulationApplication<CustomSimulation.CustomModel>
{
    /** */
    private static final long serialVersionUID = 20240418L;

    /**
     * Create a custom simulation.
     * @param title the title of the Frame
     * @param panel the tabbed panel to display
     * @param model the model
     */
    public CustomSimulation(final String title, final OtsAnimationPanel panel, final CustomModel model)
    {
        super(model, panel);
    }

    /**
     * Start the simulation.
     * @param exitOnClose when running stand-alone: true; when running as part of a demo: false
     * @param simulator simulator.
     * @param model model.
     */
    public static void demo(final boolean exitOnClose, final OtsAnimator simulator, final CustomModel model)
    {
        try
        {
            OtsAnimationPanel animationPanel = new OtsAnimationPanel(model.getNetwork().getExtent(), new Dimension(800, 600),
                    simulator, model, DEFAULT_COLORER, model.getNetwork());
            CustomSimulation app = new CustomSimulation("Custom Simulation", animationPanel, model);
            app.setExitOnClose(exitOnClose);
            animationPanel.enableSimulationControlButtons();
        }
        catch (SimRuntimeException | RemoteException | DsolException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Custom simulation.
     * <p>
     * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     */
    public static class CustomModel extends AbstractOtsModel
    {
        /** */
        private static final long serialVersionUID = 20240418L;

        /** The network. */
        private RoadNetwork network;

        /**
         * Constructor.
         * @param simulator the simulator for this model
         */
        public CustomModel(final OtsSimulatorInterface simulator)
        {
            super(simulator);
        }

        /**
         * Set network.
         * @param network the network
         */
        public void setNetwork(final RoadNetwork network)
        {
            this.network = network;
        }

        /** {@inheritDoc} */
        @Override
        public void constructModel() throws SimRuntimeException
        {
            // custom through external code
        }

        /** {@inheritDoc} */
        @Override
        public RoadNetwork getNetwork()
        {
            return this.network;
        }
    }

}
