package org.opentrafficsim.gui;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.rmi.RemoteException;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.opentrafficsim.base.modelproperties.CompoundProperty;
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.simulationengine.WrappableAnimation;

import nl.tudelft.simulation.dsol.gui.swing.Console;
import nl.tudelft.simulation.dsol.gui.swing.StatusBar;
import nl.tudelft.simulation.dsol.gui.swing.TabbedContentPane;

/**
 * GUI with simulator, console, control panel, status bar, etc.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

    /** Animation, required to add properties tab. */
    private final WrappableAnimation wrappableAnimation;

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
     * @param simulator the simulator or animator of the model.
     * @param wrappableAnimation the builder and rebuilder of the simulation, based on properties.
     * @throws RemoteException when communications to a remote machine fails
     * @throws PropertyException when one of the user modified properties has the empty string as key
     */
    public OTSSimulationPanel(final OTSDEVSSimulatorInterface simulator, final WrappableAnimation wrappableAnimation)
            throws RemoteException, PropertyException
    {

        this.simulator = simulator;
        this.wrappableAnimation = wrappableAnimation;

        this.setLayout(new BorderLayout());

        // Let's add our simulationControl
        this.otsControlPanel = new OTSControlPanel(simulator, wrappableAnimation);
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
     * @throws PropertyException on exception with properties
     */
    public final void addPropertiesTab() throws PropertyException
    {
        // Let's add the properties of the simulation model as a tab
        List<Property<?>> propertyList =
                new CompoundProperty("", "", "", this.wrappableAnimation.getUserModifiedProperties(), true, 0)
                        .displayOrderedValue();
        StringBuilder html = new StringBuilder();
        html.append("<html><table border=\"1\"><tr><th colspan=\"" + propertyList.size() + "\">Settings</th></tr><tr>");

        for (Property<?> ap : propertyList)
        {
            html.append("<td valign=\"top\">" + ap.htmlStateDescription() + "</td>");
        }
        html.append("</table></html>");
        JLabel propertySettings = new JLabel(html.toString());
        JScrollPane settings = new JScrollPane(propertySettings);
        settings.setBorder(null);
        this.tabbedPane.addTab("settings", settings);
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

    /**
     * Return the OTSControlPanel of this OTSSimulationPanel.
     * @return OTSControlPanel; the OTS control panel
     */
    public final OTSControlPanel getOtsControlPanel()
    {
        return this.otsControlPanel;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "OTSSimulationPanel [simulatorTime=" + this.simulator.getSimulatorTime().getTime() + "]";
    }

    /**
     * TabbedContentPane which ignores appearance (it has too much colors looking ugly / becoming unreadable).
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$,
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
         * @param tabPlacement
         */
        public AppearanceControlTabbedContentPane(int tabPlacement)
        {
            super(tabPlacement);
        }
    }

}
