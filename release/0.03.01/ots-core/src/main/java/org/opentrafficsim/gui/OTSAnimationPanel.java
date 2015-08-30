package org.opentrafficsim.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;

import javax.swing.JPanel;

import nl.tudelft.simulation.dsol.animation.D2.AnimationPanel;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.Event;

import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.simulationengine.SimpleAnimator;
import org.opentrafficsim.simulationengine.WrappableSimulation;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jun 18, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class OTSAnimationPanel extends OTSSimulationPanel
{
    /** */
    private static final long serialVersionUID = 20150617L;

    /** the animation panel on tab position 0. */
    private final AnimationPanel animationPanel;

    /** Border panel in which the animation is shown. */
    private final JPanel borderPanel;

    /** The switchableGTUColorer used to color the GTUs. */
    private GTUColorer gtuColorer = null;

    /** The ColorControlPanel that allows the user to operate the SwitchableGTUColorer. */
    private ColorControlPanel colorControlPanel = null;

    /**
     * Construct a panel that looks like the DSOLPanel for quick building of OTS applications.
     * @param extent Rectangle2D; bottom left corner, length and width of the area (world) to animate.
     * @param size the size to be used for the animation.
     * @param simulator the simulator or animator of the model.
     * @param wrappableSimulation the builder and rebuilder of the simulation, based on properties.
     * @param gtuColorer the colorer to use for the GTUs.
     * @throws RemoteException when notification to the animation panel fails.
     */
    public OTSAnimationPanel(final Rectangle2D extent, final Dimension size, final SimpleAnimator simulator,
        final WrappableSimulation wrappableSimulation, final GTUColorer gtuColorer) throws RemoteException
    {
        super(simulator, wrappableSimulation);

        // Add the animation panel as a tab.
        this.animationPanel = new AnimationPanel(extent, size, simulator);
        this.borderPanel = new JPanel(new BorderLayout());
        this.borderPanel.add(this.animationPanel, BorderLayout.CENTER);
        getTabbedPane().addTab(0, "animation", this.borderPanel);
        getTabbedPane().setSelectedIndex(0); // Show the animation panel as the default tab

        // Include the GTU colorer control panel NORTH of the animation.
        this.gtuColorer = gtuColorer;
        this.colorControlPanel = new ColorControlPanel(this.gtuColorer);
        this.borderPanel.add(this.colorControlPanel, BorderLayout.NORTH);

        // Tell the animation to build the list of animation objects.
        this.animationPanel.notify(new Event(SimulatorInterface.START_REPLICATION_EVENT, simulator, null));
    }

    /**
     * Easy access to the AnimationPanel.
     * @return AnimationPanel
     */
    public final AnimationPanel getAnimationPanel()
    {
        return this.animationPanel;
    }

    /**
     * Access the GTUColorer of this animation ControlPanel.
     * @return GTUColorer the colorer used. If it is a SwitchableGTUColorer, the wrapper with the list will be returned, not the
     *         actual colorer in use.
     */
    public final GTUColorer getGTUColorer()
    {
        return this.gtuColorer;
    }

    /**
     * Access the ColorControlPanel of this ControlPanel. If the simulator is not a SimpleAnimator, no ColorControlPanel was
     * constructed and this method will return null.
     * @return ColorControlPanel
     */
    public final ColorControlPanel getColorControlPanel()
    {
        return this.colorControlPanel;
    }

}
