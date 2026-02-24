package org.opentrafficsim.demo.conflict;

import java.rmi.RemoteException;

import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.demo.conflict.TJunctionDemo.TJunctionModel;
import org.opentrafficsim.road.network.factory.xml.OtsXmlModel;
import org.opentrafficsim.swing.gui.OtsSimulationApplication;
import org.opentrafficsim.swing.gui.OtsSimulationPanel;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.DsolException;

/**
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TJunctionDemo extends OtsSimulationApplication<TJunctionModel>
{
    /** */
    private static final long serialVersionUID = 20161211L;

    /**
     * Create a T-Junction demo.
     * @param title the title of the Frame
     * @param panel the tabbed panel to display
     * @param model the model
     */
    public TJunctionDemo(final String title, final OtsSimulationPanel panel, final TJunctionModel model)
    {
        super(model, panel);
    }

    /**
     * Main program.
     * @param args the command line arguments (not used)
     */
    public static void main(final String[] args)
    {
        demo(true);
    }

    /**
     * Start the demo.
     * @param exitOnClose when running stand-alone: true; when running as part of a demo: false
     */
    public static void demo(final boolean exitOnClose)
    {
        try
        {
            OtsAnimator simulator = new OtsAnimator("TJunctionDemo");
            final TJunctionModel junctionModel = new TJunctionModel(simulator);
            OtsSimulationPanel simulationPanel = new OtsSimulationPanel(junctionModel.getNetwork());
            TJunctionDemo app = new TJunctionDemo("T-Junction demo", simulationPanel, junctionModel);
            app.setExitOnClose(exitOnClose);
            simulationPanel.enableSimulationControlButtons();
        }
        catch (SimRuntimeException | RemoteException | DsolException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * The simulation model.
     */
    public static class TJunctionModel extends OtsXmlModel
    {
        /**
         * Constructor.
         * @param simulator the simulator for this model
         */
        public TJunctionModel(final OtsSimulatorInterface simulator)
        {
            super(simulator, "/resources/conflict/TJunction.xml");
        }
    }
}
