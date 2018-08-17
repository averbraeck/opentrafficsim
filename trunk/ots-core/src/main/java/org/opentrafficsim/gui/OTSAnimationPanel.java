package org.opentrafficsim.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.rmi.RemoteException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.Try;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.simulationengine.SimpleAnimator;
import org.opentrafficsim.simulationengine.WrappableAnimation;

import nl.javel.gisbeans.map.MapInterface;
import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.AnimationPanel;
import nl.tudelft.simulation.dsol.animation.D2.GisRenderable2D;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.Event;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;
import nl.tudelft.simulation.language.reflection.ClassUtil;

/**
 * Animation panel with various controls.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jun 18, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class OTSAnimationPanel extends OTSSimulationPanel implements ActionListener, WindowListener, EventListenerInterface
{
    /** */
    private static final long serialVersionUID = 20150617L;

    /** The animation panel on tab position 0. */
    private final AutoAnimationPanel animationPanel;

    /** Border panel in which the animation is shown. */
    private final JPanel borderPanel;

    /** Toggle panel with which animation features can be shown/hidden. */
    private final JPanel togglePanel;

    /** Map of toggle names to toggle animation classes. */
    private Map<String, Class<? extends Locatable>> toggleLocatableMap = new HashMap<>();

    /** Set of animation classes to toggle buttons. */
    private Map<Class<? extends Locatable>, JToggleButton> toggleButtons = new HashMap<>();

    /** Set of GIS layer names to toggle GIS layers . */
    private Map<String, MapInterface> toggleGISMap = new HashMap<>();

    /** Set of GIS layer names to toggle buttons. */
    private Map<String, JToggleButton> toggleGISButtons = new HashMap<>();

    /** The switchableGTUColorer used to color the GTUs. */
    private GTUColorer gtuColorer = null;

    /** The ColorControlPanel that allows the user to operate the SwitchableGTUColorer. */
    private ColorControlPanel colorControlPanel = null;

    /** The coordinates of the cursor. */
    private final JLabel coordinateField;

    /** The GTU count field. */
    private final JLabel gtuCountField;

    /** The GTU count. */
    private int gtuCount = 0;

    /** The animation buttons. */
    private final ArrayList<JButton> buttons = new ArrayList<>();

    /** The formatter for the world coordinates. */
    private static final NumberFormat FORMATTER = NumberFormat.getInstance();

    /** Has the window close handler been registered? */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected boolean closeHandlerRegistered = false;

    /** Indicate the window has been closed and the timer thread can stop. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected boolean windowExited = false;

    /** Autopan. */
    private boolean autoPan = false;

    /** Autopan toggle. */
    private final JCheckBox autoPanToggle;

    /** Autopan Id text field. */
    private final JTextField autoPanField;

    /** Initialize the formatter. */
    static
    {
        FORMATTER.setMaximumFractionDigits(3);
    }

    /**
     * Construct a panel that looks like the DSOLPanel for quick building of OTS applications.
     * @param extent Rectangle2D; bottom left corner, length and width of the area (world) to animate.
     * @param size the size to be used for the animation.
     * @param simulator the simulator or animator of the model.
     * @param wrappableAnimation the builder and rebuilder of the simulation, based on properties.
     * @param gtuColorer the colorer to use for the GTUs.
     * @param network OTSNetwork; network
     * @throws RemoteException when notification of the animation panel fails
     * @throws PropertyException when one of the user modified properties has the empty string as key
     */
    public OTSAnimationPanel(final Rectangle2D extent, final Dimension size, final SimpleAnimator simulator,
            final WrappableAnimation wrappableAnimation, final GTUColorer gtuColorer, final OTSNetwork network)
            throws RemoteException, PropertyException
    {
        super(simulator, wrappableAnimation);

        // Add the animation panel as a tab.

        this.animationPanel = new AutoAnimationPanel(extent, size, simulator, network);
        this.animationPanel.showGrid(false);
        this.borderPanel = new JPanel(new BorderLayout());
        this.borderPanel.add(this.animationPanel, BorderLayout.CENTER);
        getTabbedPane().addTab(0, "animation", this.borderPanel);
        getTabbedPane().setSelectedIndex(0); // Show the animation panel as the default tab

        // Include the GTU colorer control panel NORTH of the animation.
        this.gtuColorer = gtuColorer;
        this.colorControlPanel = new ColorControlPanel(this.gtuColorer);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        this.borderPanel.add(buttonPanel, BorderLayout.NORTH);
        buttonPanel.add(this.colorControlPanel);

        // Include the TogglePanel WEST of the animation.
        this.togglePanel = new JPanel();
        this.togglePanel.setLayout(new BoxLayout(this.togglePanel, BoxLayout.Y_AXIS));
        this.borderPanel.add(this.togglePanel, BorderLayout.WEST);

        // add the buttons for home, zoom all, grid, and mouse coordinates
        buttonPanel.add(new JLabel("   "));
        buttonPanel.add(makeButton("allButton", "/Expand.png", "ZoomAll", "Zoom whole network", true));
        buttonPanel.add(makeButton("homeButton", "/Home.png", "Home", "Zoom to original extent", true));
        buttonPanel.add(makeButton("gridButton", "/Grid.png", "Grid", "Toggle grid on/off", true));
        buttonPanel.add(new JLabel("   "));

        // add info labels next to buttons
        JPanel infoTextPanel = new JPanel();
        buttonPanel.add(infoTextPanel);
        infoTextPanel.setMinimumSize(new Dimension(250, 20));
        infoTextPanel.setPreferredSize(new Dimension(250, 20));
        infoTextPanel.setLayout(new BoxLayout(infoTextPanel, BoxLayout.Y_AXIS));
        this.coordinateField = new JLabel("Mouse: ");
        this.coordinateField.setMinimumSize(new Dimension(250, 10));
        this.coordinateField.setPreferredSize(new Dimension(250, 10));
        infoTextPanel.add(this.coordinateField);
        // gtu fields
        JPanel gtuPanel = new JPanel();
        gtuPanel.setAlignmentX(0.0f);
        gtuPanel.setLayout(new BoxLayout(gtuPanel, BoxLayout.X_AXIS));
        gtuPanel.setMinimumSize(new Dimension(250, 10));
        gtuPanel.setPreferredSize(new Dimension(250, 10));
        infoTextPanel.add(gtuPanel);
        if (null != network)
        {
            network.addListener(this, Network.GTU_ADD_EVENT);
            network.addListener(this, Network.GTU_REMOVE_EVENT);
        }
        // gtu follow
        /** Text field with appearance control. */
        class AppearanceTextField extends JTextField implements AppearanceControl
        {
            private static final long serialVersionUID = 20180430L;
        }
        this.autoPanField = new AppearanceTextField();
        this.autoPanField.setMaximumSize(new Dimension(100, 20));
        this.autoPanField.setVisible(false);
        this.autoPanField.addActionListener(new ActionListener()
        {
            @SuppressWarnings("synthetic-access")
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                OTSAnimationPanel.this.animationPanel.repaint();
            }
        });
        this.autoPanToggle = new JCheckBox();
        this.autoPanToggle.setToolTipText("Pan to GTU");
        this.autoPanToggle.addActionListener(new ActionListener()
        {
            @SuppressWarnings("synthetic-access")
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                OTSAnimationPanel.this.autoPan = !OTSAnimationPanel.this.autoPanField.isVisible();
                OTSAnimationPanel.this.autoPanField.setVisible(OTSAnimationPanel.this.autoPan);
                if (OTSAnimationPanel.this.autoPan)
                {
                    OTSAnimationPanel.this.autoPanField.requestFocusInWindow();
                    OTSAnimationPanel.this.autoPanField.selectAll();
                }
                gtuPanel.revalidate();
            }
        });
        gtuPanel.add(this.autoPanToggle);
        gtuPanel.add(this.autoPanField);
        // gtu counter
        this.gtuCountField = new JLabel("0 GTU's");
        this.gtuCount = null == network ? 0 : network.getGTUs().size();
        gtuPanel.add(this.gtuCountField);
        setGtuCountText();

        // Tell the animation to build the list of animation objects.
        this.animationPanel.notify(new Event(SimulatorInterface.START_REPLICATION_EVENT, simulator, null));

        // switch off the X and Y coordinates in a tooltip.
        this.animationPanel.setShowToolTip(false);

        // run the update task for the mouse coordinate panel
        new UpdateTimer().start();

        // make sure the thread gets killed when the window closes.
        installWindowCloseHandler();
    }

    /**
     * Create a button.
     * @param name String; name of the button
     * @param iconPath String; path to the resource
     * @param actionCommand String; the action command
     * @param toolTipText String; the hint to show when the mouse hovers over the button
     * @param enabled boolean; true if the new button must initially be enable; false if it must initially be disabled
     * @return JButton
     */
    private JButton makeButton(final String name, final String iconPath, final String actionCommand, final String toolTipText,
            final boolean enabled)
    {
        // JButton result = new JButton(new ImageIcon(this.getClass().getResource(iconPath)));
        JButton result = new JButton(OTSControlPanel.loadIcon(iconPath));
        result.setPreferredSize(new Dimension(34, 32));
        result.setName(name);
        result.setEnabled(enabled);
        result.setActionCommand(actionCommand);
        result.setToolTipText(toolTipText);
        result.addActionListener(this);
        this.buttons.add(result);
        return result;
    }

    /**
     * Add a button for toggling an animatable class on or off. Button icons for which 'idButton' is true will be placed to the
     * right of the previous button, which should be the corresponding button without the id. An example is an icon for
     * showing/hiding the class 'Lane' followed by the button to show/hide the Lane ids.
     * @param name the name of the button
     * @param locatableClass the class for which the button holds (e.g., GTU.class)
     * @param iconPath the path to the 24x24 icon to display
     * @param toolTipText the tool tip text to show when hovering over the button
     * @param initiallyVisible whether the class is initially shown or not
     * @param idButton id button that needs to be placed next to the previous button
     */
    public final void addToggleAnimationButtonIcon(final String name, final Class<? extends Locatable> locatableClass,
            final String iconPath, final String toolTipText, final boolean initiallyVisible, final boolean idButton)
    {
        JToggleButton button;
        Icon icon = OTSControlPanel.loadIcon(iconPath);
        Icon unIcon = OTSControlPanel.loadGrayscaleIcon(iconPath);
        button = new JCheckBox();
        button.setSelectedIcon(icon);
        button.setIcon(unIcon);
        button.setPreferredSize(new Dimension(32, 28));
        button.setName(name);
        button.setEnabled(true);
        button.setSelected(initiallyVisible);
        button.setActionCommand(name);
        button.setToolTipText(toolTipText);
        button.addActionListener(this);

        // place an Id button to the right of the corresponding content button
        if (idButton && this.togglePanel.getComponentCount() > 0)
        {
            JPanel lastToggleBox = (JPanel) this.togglePanel.getComponent(this.togglePanel.getComponentCount() - 1);
            lastToggleBox.add(button);
        }
        else
        {
            JPanel toggleBox = new JPanel();
            toggleBox.setLayout(new BoxLayout(toggleBox, BoxLayout.X_AXIS));
            toggleBox.add(button);
            this.togglePanel.add(toggleBox);
            toggleBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        if (initiallyVisible)
        {
            this.animationPanel.showClass(locatableClass);
        }
        else
        {
            this.animationPanel.hideClass(locatableClass);
        }
        this.toggleLocatableMap.put(name, locatableClass);
        this.toggleButtons.put(locatableClass, button);
    }

    /**
     * Add a button for toggling an animatable class on or off.
     * @param name the name of the button
     * @param locatableClass the class for which the button holds (e.g., GTU.class)
     * @param toolTipText the tool tip text to show when hovering over the button
     * @param initiallyVisible whether the class is initially shown or not
     */
    public final void addToggleAnimationButtonText(final String name, final Class<? extends Locatable> locatableClass,
            final String toolTipText, final boolean initiallyVisible)
    {
        JToggleButton button;
        button = new JCheckBox(name);
        button.setName(name);
        button.setEnabled(true);
        button.setSelected(initiallyVisible);
        button.setActionCommand(name);
        button.setToolTipText(toolTipText);
        button.addActionListener(this);

        JPanel toggleBox = new JPanel();
        toggleBox.setLayout(new BoxLayout(toggleBox, BoxLayout.X_AXIS));
        toggleBox.add(button);
        this.togglePanel.add(toggleBox);
        toggleBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (initiallyVisible)
        {
            this.animationPanel.showClass(locatableClass);
        }
        else
        {
            this.animationPanel.hideClass(locatableClass);
        }
        this.toggleLocatableMap.put(name, locatableClass);
        this.toggleButtons.put(locatableClass, button);
    }

    /**
     * Add a text to explain animatable classes.
     * @param text the text to show
     */
    public final void addToggleText(final String text)
    {
        JPanel textBox = new JPanel();
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.X_AXIS));
        textBox.add(new JLabel(text));
        this.togglePanel.add(textBox);
        textBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    /**
     * Add a button to toggle a GIS Layer on or off.
     * @param layerName the name of the layer
     * @param displayName the name to display next to the tick box
     * @param gisMap the map
     * @param toolTipText the tool tip text
     */
    public final void addToggleGISButtonText(final String layerName, final String displayName, final GisRenderable2D gisMap,
            final String toolTipText)
    {
        JToggleButton button;
        button = new JCheckBox(displayName);
        button.setName(layerName);
        button.setEnabled(true);
        button.setSelected(true);
        button.setActionCommand(layerName);
        button.setToolTipText(toolTipText);
        button.addActionListener(this);

        JPanel toggleBox = new JPanel();
        toggleBox.setLayout(new BoxLayout(toggleBox, BoxLayout.X_AXIS));
        toggleBox.add(button);
        this.togglePanel.add(toggleBox);
        toggleBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        this.toggleGISMap.put(layerName, gisMap.getMap());
        this.toggleGISButtons.put(layerName, button);
    }

    /**
     * Set a GIS layer to be shown in the animation to true.
     * @param layerName the name of the GIS-layer that has to be shown.
     */
    public final void showGISLayer(final String layerName)
    {
        MapInterface gisMap = this.toggleGISMap.get(layerName);
        if (gisMap != null)
        {
            try
            {
                gisMap.showLayer(layerName);
                this.toggleGISButtons.get(layerName).setSelected(true);
                this.animationPanel.repaint();
            }
            catch (RemoteException exception)
            {
                exception.printStackTrace();
            }
        }
    }

    /**
     * Set a GIS layer to be hidden in the animation to true.
     * @param layerName the name of the GIS-layer that has to be hidden.
     */
    public final void hideGISLayer(final String layerName)
    {
        MapInterface gisMap = this.toggleGISMap.get(layerName);
        if (gisMap != null)
        {
            try
            {
                gisMap.hideLayer(layerName);
                this.toggleGISButtons.get(layerName).setSelected(false);
                this.animationPanel.repaint();
            }
            catch (RemoteException exception)
            {
                exception.printStackTrace();
            }
        }
    }

    /**
     * Toggle a GIS layer to be displayed in the animation to its reverse value.
     * @param layerName the name of the GIS-layer that has to be turned off or vice versa.
     */
    public final void toggleGISLayer(final String layerName)
    {
        MapInterface gisMap = this.toggleGISMap.get(layerName);
        if (gisMap != null)
        {
            try
            {
                if (gisMap.getVisibleLayers().contains(gisMap.getLayerMap().get(layerName)))
                {
                    gisMap.hideLayer(layerName);
                    this.toggleGISButtons.get(layerName).setSelected(false);
                }
                else
                {
                    gisMap.showLayer(layerName);
                    this.toggleGISButtons.get(layerName).setSelected(true);
                }
                this.animationPanel.repaint();
            }
            catch (RemoteException exception)
            {
                exception.printStackTrace();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void actionPerformed(final ActionEvent actionEvent)
    {
        String actionCommand = actionEvent.getActionCommand();
        try
        {
            if (actionCommand.equals("Home"))
            {
                this.animationPanel.home();
            }
            if (actionCommand.equals("ZoomAll"))
            {
                this.animationPanel.zoomAll();
            }
            if (actionCommand.equals("Grid"))
            {
                this.animationPanel.showGrid(!this.animationPanel.isShowGrid());
            }

            if (this.toggleLocatableMap.containsKey(actionCommand))
            {
                Class<? extends Locatable> locatableClass = this.toggleLocatableMap.get(actionCommand);
                this.animationPanel.toggleClass(locatableClass);
                this.togglePanel.repaint();
            }

            if (this.toggleGISMap.containsKey(actionCommand))
            {
                this.toggleGISLayer(actionCommand);
                this.togglePanel.repaint();
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
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
     * Update the checkmark related to a programmatically changed animation state.
     * @param locatableClass Class; class to show the checkmark for
     */
    public final void updateAnimationClassCheckBox(final Class<? extends Locatable> locatableClass)
    {
        JToggleButton button = this.toggleButtons.get(locatableClass);
        if (button == null)
        {
            return;
        }
        // TODO complete hack, but everything is final...
        Field field = Try.assign(() -> ClassUtil.resolveField(AnimationPanel.class, "visibilityMap"), "No field visibilityMap");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Class<? extends Locatable>, Boolean> map =
                Try.assign(() -> (Map<Class<? extends Locatable>, Boolean>) field.get(this.getAnimationPanel()),
                        "visibilityMap not a map?");
        Boolean show = map.get(locatableClass);
        if (show == null)
        {
            return;
        }
        button.setSelected(show);
    }

    /**
     * Display the latest world coordinate based on the mouse position on the screen.
     */
    protected final void updateWorldCoordinate()
    {
        String worldPoint = "(x=" + FORMATTER.format(this.animationPanel.getWorldCoordinate().getX()) + " ; y="
                + FORMATTER.format(this.animationPanel.getWorldCoordinate().getY()) + ")";
        this.coordinateField.setText("Mouse: " + worldPoint);
        int requiredWidth = this.coordinateField.getGraphics().getFontMetrics().stringWidth(this.coordinateField.getText());
        if (this.coordinateField.getPreferredSize().width < requiredWidth)
        {
            Dimension requiredSize = new Dimension(requiredWidth, this.coordinateField.getPreferredSize().height);
            this.coordinateField.setPreferredSize(requiredSize);
            this.coordinateField.setMinimumSize(requiredSize);
            Container parent = this.coordinateField.getParent();
            parent.setPreferredSize(requiredSize);
            parent.setMinimumSize(requiredSize);
            // System.out.println("Increased minimum width to " + requiredSize.width);
            parent.revalidate();
        }
        this.coordinateField.repaint();
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

    /**
     * Install a handler for the window closed event that stops the simulator (if it is running).
     */
    public final void installWindowCloseHandler()
    {
        if (this.closeHandlerRegistered)
        {
            return;
        }

        // make sure the root frame gets disposed of when the closing X icon is pressed.
        new DisposeOnCloseThread(this).start();
    }

    /** Install the dispose on close when the OTSControlPanel is registered as part of a frame. */
    protected class DisposeOnCloseThread extends Thread
    {
        /** The current container. */
        private OTSAnimationPanel panel;

        /**
         * @param panel the OTSControlpanel container.
         */
        public DisposeOnCloseThread(final OTSAnimationPanel panel)
        {
            super();
            this.panel = panel;
        }

        /** {@inheritDoc} */
        @Override
        public final void run()
        {
            Container root = this.panel;
            while (!(root instanceof JFrame))
            {
                try
                {
                    Thread.sleep(10);
                }
                catch (@SuppressWarnings("unused") InterruptedException exception)
                {
                    // nothing to do
                }

                // Search towards the root of the Swing components until we find a JFrame
                root = this.panel;
                while (null != root.getParent() && !(root instanceof JFrame))
                {
                    root = root.getParent();
                }
            }
            JFrame frame = (JFrame) root;
            frame.addWindowListener(this.panel);
            this.panel.closeHandlerRegistered = true;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "DisposeOnCloseThread of OTSAnimationPanel [panel=" + this.panel + "]";
        }
    }

    /** {@inheritDoc} */
    @Override
    public void windowOpened(final WindowEvent e)
    {
        // No action
    }

    /** {@inheritDoc} */
    @Override
    public final void windowClosing(final WindowEvent e)
    {
        // No action
    }

    /** {@inheritDoc} */
    @Override
    public final void windowClosed(final WindowEvent e)
    {
        this.windowExited = true;
    }

    /** {@inheritDoc} */
    @Override
    public final void windowIconified(final WindowEvent e)
    {
        // No action
    }

    /** {@inheritDoc} */
    @Override
    public final void windowDeiconified(final WindowEvent e)
    {
        // No action
    }

    /** {@inheritDoc} */
    @Override
    public final void windowActivated(final WindowEvent e)
    {
        // No action
    }

    /** {@inheritDoc} */
    @Override
    public final void windowDeactivated(final WindowEvent e)
    {
        // No action
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        if (event.getType().equals(Network.GTU_ADD_EVENT))
        {
            this.gtuCount++;
            setGtuCountText();
        }
        else if (event.getType().equals(Network.GTU_REMOVE_EVENT))
        {
            this.gtuCount--;
            setGtuCountText();
        }
    }

    /**
     * Updates the text of the GTU counter.
     */
    private void setGtuCountText()
    {
        this.gtuCountField.setText(this.gtuCount + " GTU's");
    }

    /**
     * UpdateTimer class to update the coordinate on the screen.
     */
    protected class UpdateTimer extends Thread
    {
        /** {@inheritDoc} */
        @Override
        public final void run()
        {
            while (!OTSAnimationPanel.this.windowExited)
            {
                if (OTSAnimationPanel.this.isShowing())
                {
                    OTSAnimationPanel.this.updateWorldCoordinate();
                }
                try
                {
                    Thread.sleep(50); // 20 times per second
                }
                catch (@SuppressWarnings("unused") InterruptedException exception)
                {
                    // do nothing
                }
            }
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "UpdateTimer thread for OTSAnimationPanel";
        }

    }

    /**
     * Animation panel that adds autopan functionality.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$,
     *          initial version 30 apr. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class AutoAnimationPanel extends AnimationPanel
    {

        /** */
        private static final long serialVersionUID = 20180430L;

        /** Network. */
        private final OTSNetwork network;

        /** Last GTU that was followed. */
        private GTU lastGtu;

        /**
         * Constructor.
         * @param extent Rectangle2D; home extent
         * @param size Dimension; size
         * @param simulator SimulatorInterface; simulator
         * @param network OTSNetwork; network
         */
        AutoAnimationPanel(final Rectangle2D extent, final Dimension size, final SimulatorInterface<?, ?, ?> simulator,
                final OTSNetwork network)
        {
            super(extent, size, simulator);
            this.network = network;
            MouseListener[] listeners = getMouseListeners();
            for (MouseListener listener : listeners)
            {
                removeMouseListener(listener);
            }
            this.addMouseListener(new MouseAdapter()
            {
                /** {@inheritDoc} */
                @SuppressWarnings("synthetic-access")
                @Override
                public void mouseReleased(final MouseEvent e)
                {
                    if (e.isControlDown())
                    {
                        GTU gtu = getSelectedGTU(e.getPoint());
                        if (gtu != null)
                        {
                            OTSAnimationPanel.this.autoPanField.setText(gtu.getId());
                            OTSAnimationPanel.this.autoPanField.setVisible(true);
                            OTSAnimationPanel.this.autoPanToggle.setSelected(true);
                            OTSAnimationPanel.this.autoPanToggle.getParent().validate();
                            OTSAnimationPanel.this.autoPan = true;
                            e.consume(); // sadly doesn't work to prevent a pop up
                        }
                    }
                    e.consume();
                }
            });
            for (MouseListener listener : listeners)
            {
                addMouseListener(listener);
            }
        }

        /**
         * returns the list of selected objects at a certain mousePoint.
         * @param mousePoint the mousePoint
         * @return the selected objects
         */
        @SuppressWarnings("synthetic-access")
        protected GTU getSelectedGTU(final Point2D mousePoint)
        {
            List<GTU> targets = new ArrayList<>();
            Point2D point = Renderable2DInterface.Util.getWorldCoordinates(mousePoint,
                    OTSAnimationPanel.this.animationPanel.getExtent(), OTSAnimationPanel.this.animationPanel.getSize());
            for (Renderable2DInterface<?> renderable : OTSAnimationPanel.this.animationPanel.getElements())
            {
                if (OTSAnimationPanel.this.animationPanel.isShowElement(renderable) && renderable.contains(point,
                        OTSAnimationPanel.this.animationPanel.getExtent(), OTSAnimationPanel.this.animationPanel.getSize()))
                {
                    if (renderable.getSource() instanceof GTU)
                    {
                        targets.add((GTU) renderable.getSource());
                    }
                }
            }
            if (targets.size() == 1)
            {
                return targets.get(0);
            }
            return null;
        }

        /** {@inheritDoc} */
        @SuppressWarnings("synthetic-access")
        @Override
        public void paint(final Graphics g)
        {
            if (OTSAnimationPanel.this.autoPan)
            {
                String id = OTSAnimationPanel.this.autoPanField.getText();
                if (this.lastGtu == null || !this.lastGtu.getId().equals(id) || this.lastGtu.isDestroyed())
                {
                    this.lastGtu = this.network.getGTU(id);
                }
                DirectedPoint point;
                try
                {
                    point = this.lastGtu != null ? this.lastGtu.getLocation() : null;
                }
                catch (@SuppressWarnings("unused") RemoteException exception)
                {
                    System.err.println("Could not pan to GTU location.");
                    return;
                }
                if (point != null)
                {
                    double w = this.extent.getWidth();
                    double h = this.extent.getHeight();
                    this.extent = new Rectangle2D.Double(point.getX() - w / 2, point.getY() - h / 2, w, h);
                }
            }
            super.paint(g);
        }
    }

}
