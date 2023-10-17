package org.opentrafficsim.swing.gui;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.rmi.RemoteException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;

import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.dsol.swing.gui.ConsoleOutput;
import nl.tudelft.simulation.dsol.swing.gui.TabbedContentPane;

/**
 * GUI with simulator, console, control panel, status bar, etc.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class OtsSimulationPanel extends JPanel
{
    /** */
    private static final long serialVersionUID = 20150617L;

    /** The simulator. */
    private final OtsSimulatorInterface simulator;

    /** The console to log messages. */
    private final ConsoleOutput console = new ConsoleOutput();

    /** The control panel to control start/stop, speed of the simulation. */
    private final OtsControlPanel otsControlPanel;

    /** Animation, required to add properties tab. */
    private final OtsModelInterface otsModel;

    static
    {
        // use narrow border for TabbedPane, which cannot be changed afterwards
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(1, 1, 1, 1));
    }

    /** The tabbed pane that contains the different (default) screens. */
    private final TabbedContentPane tabbedPane;

    /**
     * Construct a panel that looks like the DSOLPanel for quick building of OTS applications.
     * @param simulator OtsSimulatorInterface; the simulator or animator of the model.
     * @param otsModel OtsModelInterface; the model with its properties.
     * @throws RemoteException when communications to a remote machine fails
     */
    public OtsSimulationPanel(final OtsSimulatorInterface simulator, final OtsModelInterface otsModel) throws RemoteException
    {
        AppearanceApplication.setDefaultFont();
        
        this.simulator = simulator;
        this.otsModel = otsModel;

        this.setLayout(new BorderLayout());

        // Let's add our simulationControl
        this.otsControlPanel = new OtsControlPanel(simulator, otsModel, (OtsAnimationPanel) this);
        this.add(this.otsControlPanel, BorderLayout.NORTH);

        // Let's display our tabbed contentPane
        this.tabbedPane = new AppearanceControlTabbedContentPane(SwingConstants.BOTTOM);
        this.add(this.tabbedPane, BorderLayout.CENTER);

        // put a status bar at the bottom
        // this.statusBar = new StatusBar(this.simulator);
        // this.add(this.statusBar, BorderLayout.SOUTH);
    }

    /**
     * Adds the console tab.
     */
    public final void addConsoleTab()
    {
        // Let's add our console to our tabbed pane
        JScrollPane cons = new JScrollPane(this.console);
        cons.setBorder(null);
        this.tabbedPane.addTab("console", cons);
    }

    /**
     * Adds the properties tab.
     * @throws InputParameterException on exception with properties
     */
    public final void addPropertiesTab() throws InputParameterException
    {
        // TODO: make a tab with the InputParameters
    }

    /**
     * @return tabbedPane
     */
    public final TabbedContentPane getTabbedPane()
    {
        return this.tabbedPane;
    }

    /**
     * @return simulator.
     */
    public final OtsSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    /**
     * Return the OtsControlPanel of this OtsSimulationPanel.
     * @return OtsControlPanel; the OTS control panel
     */
    public final OtsControlPanel getOtsControlPanel()
    {
        return this.otsControlPanel;
    }

    /**
     * @return console
     */
    public final ConsoleOutput getConsole()
    {
        return this.console;
    }

    /**
     * @return otsModel
     */
    public final OtsModelInterface getOtsModel()
    {
        return this.otsModel;
    }

    /**
     * Enable the simulation or animation buttons in the GUI. This method HAS TO BE CALLED in order for the buttons to be
     * enabled, because the initial state is DISABLED. Typically, this is done after all tabs, statistics, and other user
     * interface and model components have been constructed and initialized.
     */
    public void enableSimulationControlButtons()
    {
        getOtsControlPanel().setSimulationControlButtons(true);
    }

    /**
     * Disable the simulation or animation buttons in the GUI.
     */
    public void disableSimulationControlButtons()
    {
        getOtsControlPanel().setSimulationControlButtons(false);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "OtsSimulationPanel [simulatorTime=" + this.simulator.getSimulatorTime() + "]";
    }

    /**
     * TabbedContentPane which ignores appearance (it has too much colors looking ugly / becoming unreadable).
     * <p>
     * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    static class AppearanceControlTabbedContentPane extends TabbedContentPane implements AppearanceControl
    {
        /** */
        private static final long serialVersionUID = 20180206L;

        /**
         * @param tabPlacement int; tabPlacement
         */
        AppearanceControlTabbedContentPane(final int tabPlacement)
        {
            super(tabPlacement);
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "AppearanceControlTabbedContentPane []";
        }

    }

}
