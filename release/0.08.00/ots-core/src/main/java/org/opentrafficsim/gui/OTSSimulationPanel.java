package org.opentrafficsim.gui;

import java.awt.BorderLayout;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import nl.tudelft.simulation.dsol.gui.swing.Console;
import nl.tudelft.simulation.dsol.gui.swing.StatusBar;
import nl.tudelft.simulation.dsol.gui.swing.TabbedContentPane;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.simulationengine.WrappableAnimation;
import org.opentrafficsim.simulationengine.properties.AbstractProperty;
import org.opentrafficsim.simulationengine.properties.CompoundProperty;

/**
 * GUI with simulator, console, control panel, status bar, etc.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jun 18, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class OTSSimulationPanel extends JPanel
{
    /** */
    private static final long serialVersionUID = 20150617L;

    /** The simulator. */
    private final OTSDEVSSimulatorInterface simulator;

    /** The console to log messages. */
    private final Console console = new Console();

    /** The control panel to control start/stop, speed of the simulation. */
    private final OTSControlPanel otsControlPanel;

    /** The tabbed pane that contains the different (default) screens. */
    private final TabbedContentPane tabbedPane = new TabbedContentPane(SwingConstants.BOTTOM);

    /** The status bar at the bottom to indicate wall clock time and simulation time. */
    private final StatusBar statusBar;

    /**
     * Construct a panel that looks like the DSOLPanel for quick building of OTS applications.
     * @param simulator the simulator or animator of the model.
     * @param wrappableAnimation the builder and rebuilder of the simulation, based on properties.
     * @throws RemoteException when communications to a remote machine fails
     */
    public OTSSimulationPanel(final OTSDEVSSimulatorInterface simulator, final WrappableAnimation wrappableAnimation)
        throws RemoteException
    {
        this.simulator = simulator;

        this.setLayout(new BorderLayout());

        // Let's add our simulationControl
        this.otsControlPanel = new OTSControlPanel(simulator, wrappableAnimation);
        this.add(this.otsControlPanel, BorderLayout.NORTH);

        // Let's add our console to our tabbed pane
        this.tabbedPane.addTab("console", new JScrollPane(this.console));

        // Let's add the properties of the simulation model as a tab
        ArrayList<AbstractProperty<?>> propertyList =
            new CompoundProperty("", "", wrappableAnimation.getUserModifiedProperties(), true, 0).displayOrderedValue();
        StringBuilder html = new StringBuilder();
        html.append("<html><table border=\"1\"><tr><th colspan=\"" + propertyList.size() + "\">Settings</th></tr><tr>");

        for (AbstractProperty<?> ap : propertyList)
        {
            html.append("<td valign=\"top\">" + ap.htmlStateDescription() + "</td>");
        }
        html.append("</table></html>");
        JLabel propertySettings = new JLabel(html.toString());
        this.tabbedPane.addTab("settings", new JScrollPane(propertySettings));

        // Let's display our tabbed contentPane
        this.add(this.tabbedPane, BorderLayout.CENTER);

        // put a status bar at the bottom
        this.statusBar = new StatusBar(this.simulator);
        this.add(this.statusBar, BorderLayout.SOUTH);
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
    public final OTSDEVSSimulatorInterface getSimulator()
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

}
