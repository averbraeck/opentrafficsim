package org.opentrafficsim.swing.gui;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.rmi.RemoteException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.opentrafficsim.core.dsol.OTSModelInterface;

import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.dsol.swing.gui.Console;
import nl.tudelft.simulation.dsol.swing.gui.StatusBar;
import nl.tudelft.simulation.dsol.swing.gui.TabbedContentPane;

/**
 * GUI with simulator, console, control panel, status bar, etc.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2018-10-11 22:54:04 +0200 (Thu, 11 Oct 2018) $, @version $Revision: 4696 $, by $Author: averbraeck $,
 * initial version Jun 18, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class OTSSimulationPanel extends JPanel
{
    /** */
    private static final long serialVersionUID = 20150617L;

    /** The simulator. */
    private final DEVSSimulatorInterface.TimeDoubleUnit simulator;

    /** The console to log messages. */
    private final Console console = new Console();

    /** The control panel to control start/stop, speed of the simulation. */
    private final OTSControlPanel otsControlPanel;

    /** Animation, required to add properties tab. */
    private final OTSModelInterface otsModel;

    static
    {
        // use narrow border for TabbedPane, which cannot be changed afterwards
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(1, 1, 1, 1));
    }

    /** The tabbed pane that contains the different (default) screens. */
    private final TabbedContentPane tabbedPane = new AppearanceControlTabbedContentPane(SwingConstants.BOTTOM);

    /** The status bar at the bottom to indicate wall clock time and simulation time. */
    private final StatusBar statusBar = null;

    /**
     * Construct a panel that looks like the DSOLPanel for quick building of OTS applications.
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator or animator of the model.
     * @param otsModel OTSModelInterface; the model with its properties.
     * @throws RemoteException when communications to a remote machine fails
     */
    public OTSSimulationPanel(final DEVSSimulatorInterface.TimeDoubleUnit simulator, final OTSModelInterface otsModel)
            throws RemoteException
    {

        this.simulator = simulator;
        this.otsModel = otsModel;

        this.setLayout(new BorderLayout());

        // Let's add our simulationControl
        this.otsControlPanel = new OTSControlPanel(simulator, otsModel);
        this.add(this.otsControlPanel, BorderLayout.NORTH);

        // Let's display our tabbed contentPane
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
    public final DEVSSimulatorInterface.TimeDoubleUnit getSimulator()
    {
        return this.simulator;
    }

    /**
     * @return statusBar.
     */
    public final StatusBar getStatusBar()
    {
        return this.statusBar;
    }

    /**
     * Return the OTSControlPanel of this OTSSimulationPanel.
     * @return OTSControlPanel; the OTS control panel
     */
    public final OTSControlPanel getOtsControlPanel()
    {
        return this.otsControlPanel;
    }

    /**
     * @return console
     */
    public final Console getConsole()
    {
        return this.console;
    }

    /**
     * @return otsModel
     */
    public final OTSModelInterface getOtsModel()
    {
        return this.otsModel;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "OTSSimulationPanel [simulatorTime=" + this.simulator.getSimulatorTime() + "]";
    }

    /**
     * TabbedContentPane which ignores appearance (it has too much colors looking ugly / becoming unreadable).
     * <p>
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision: 4696 $, $LastChangedDate: 2018-10-11 22:54:04 +0200 (Thu, 11 Oct 2018) $, by $Author: averbraeck $,
     *          initial version 6 feb. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
