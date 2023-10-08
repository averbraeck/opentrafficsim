package org.opentrafficsim.demo;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.DsolException;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.draw.OtsDrawingException;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.swing.gui.OtsAnimationPanel;
import org.opentrafficsim.swing.gui.OtsSimulationApplication;
import org.locationtech.jts.linearref.LengthIndexedLine;

import java.awt.*;
import java.rmi.RemoteException;

public class CustomSimulation extends OtsSimulationApplication<CustomSimulation.CustomModel>{
    /** */
    private static final long serialVersionUID = 20161211L;

    /**
     * Create a custom simulation.
     * @param title String; the title of the Frame
     * @param panel OtsAnimationPanel; the tabbed panel to display
     * @param model CustomModel; the model
     * @throws OtsDrawingException on animation error
     */
    public CustomSimulation(final String title, final OtsAnimationPanel panel, final CustomModel model)
            throws OtsDrawingException
    {
        super(model, panel);
    }

    /**
     * Start the demo.
     * @param exitOnClose boolean; when running stand-alone: true; when running as part of a demo: false
     */
    public static void demo(final boolean exitOnClose, OtsAnimator simulator, CustomModel model)
    {
        try
        {
            OtsAnimationPanel animationPanel = new OtsAnimationPanel(model.getNetwork().getExtent(),
                    new Dimension(800, 600), simulator, model, DEFAULT_COLORER, model.getNetwork());
            CustomSimulation app = new CustomSimulation("Custom Simulation", animationPanel, model);
            app.setExitOnClose(exitOnClose);
            animationPanel.enableSimulationControlButtons();
        }
        catch (SimRuntimeException | RemoteException | OtsDrawingException | DsolException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * The simulation model.
     */
    public static class CustomModel extends AbstractOtsModel
    {
        /** */
        private static final long serialVersionUID = 20161211L;

        /** The network. */
        private RoadNetwork network;

        /**
         * @param simulator OtsSimulatorInterface; the simulator for this model
         */
        public CustomModel(final OtsSimulatorInterface simulator)
        {
            super(simulator);
        }

        /**
         * @param network the network
         */
        public void setNetwork(RoadNetwork network){
            this.network = network;
        }

        /** {@inheritDoc} */
        @Override
        public void constructModel() throws SimRuntimeException {

        }

        /** {@inheritDoc} */
        @Override
        public RoadNetwork getNetwork()
        {
            return this.network;
        }

    }
}

