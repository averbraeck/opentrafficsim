package org.opentrafficsim.swing.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.point.Point;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.TimedEvent;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.animation.data.AnimationGtuData;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.draw.ColorInterpolator;
import org.opentrafficsim.draw.colorer.Colorer;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.d2.Renderable2dInterface;
import nl.tudelft.simulation.dsol.animation.gis.GisMapInterface;
import nl.tudelft.simulation.dsol.animation.gis.GisRenderable2d;
import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.dsol.swing.animation.d2.AnimationPanel;
import nl.tudelft.simulation.language.DsolException;

/**
 * Animation panel with various controls.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class OtsAnimationPanel extends OtsSimulationPanel implements ActionListener, WindowListener, EventListener
{
    /** */
    private static final long serialVersionUID = 20150617L;

    /** Pattern to split string by upper case, with lower case adjacent, without disregarding the match itself. */
    private static final Pattern UPPER_PATTERN = Pattern.compile("(?=\\p{Lu})(?<=\\p{Ll})|(?=\\p{Lu}\\p{Ll})");

    /** The format for the world coordinates. */
    private static final String COORD_FORMAT = "%09.2f";

    /** Pattern to split leading zeros from the rest of a number. */
    private static final Pattern LEADING_ZEROS = Pattern.compile("^([+-]?)(0*)([1-9]\\d*(?:[\\.,]\\d+)?|0(?:[\\.,]\\d+)?)$");

    /** The animation panel on tab position 0. */
    private final AutoAnimationPanel animationPanel;

    /** Border panel in which the animation is shown. */
    private final JPanel borderPanel;

    /** Toggle panel with which animation features can be shown/hidden. */
    private final JPanel togglePanel;

    /** Demo panel. */
    private JPanel demoPanel = null;

    /** Map of toggle names to toggle animation classes. */
    private Map<String, Class<? extends Locatable>> toggleLocatableMap = new LinkedHashMap<>();

    /** Set of animation classes to toggle buttons. */
    private Map<Class<? extends Locatable>, JToggleButton> toggleButtons = new LinkedHashMap<>();

    /** Set of GIS layer names to toggle GIS layers . */
    private Map<String, GisMapInterface> toggleGISMap = new LinkedHashMap<>();

    /** Set of GIS layer names to toggle buttons. */
    private Map<String, JToggleButton> toggleGISButtons = new LinkedHashMap<>();

    /** The ColorControlPanel that allows the user to operate the SwitchableGtuColorer. */
    private ColorControlPanel colorControlPanel = null;

    /** The coordinates of the cursor. */
    private final JLabel coordinateField;

    /** The GTU count field. */
    private final JLabel gtuCountField;

    /** The GTU count. */
    private int gtuCount = 0;

    /** The animation buttons. */
    private final ArrayList<JButton> buttons = new ArrayList<>();

    /** Has the window close handler been registered? */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected boolean closeHandlerRegistered = false;

    /** Indicate the window has been closed and the timer thread can stop. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected boolean windowExited = false;

    /** Id of object to auto pan to. */
    private String autoPanId = null;

    /** Type of object to auto pan to. */
    private OtsSearchPanel.ObjectKind<?> autoPanKind = null;

    /** Track auto pan object continuously? */
    private boolean autoPanTrack = false;

    /** Track auto on the next paintComponent operation; then copy state from autoPanTrack. */
    private boolean autoPanOnNextPaintComponent = false;

    /**
     * Construct a panel that looks like the DSOLPanel for quick building of OTS applications.
     * @param extent bottom left corner, length and width of the area (world) to animate.
     * @param simulator the simulator or animator of the model.
     * @param otsModel the builder and rebuilder of the simulation, based on properties.
     * @param gtuColorers the colorers to use for the GTUs.
     * @param network network
     * @throws RemoteException when notification of the animation panel fails
     * @throws DsolException when simulator does not implement AnimatorInterface
     */
    public OtsAnimationPanel(final Rectangle2D extent, final OtsAnimator simulator, final OtsModelInterface otsModel,
            final List<Colorer<? super Gtu>> gtuColorers, final Network network) throws RemoteException, DsolException
    {
        super(simulator, otsModel);

        // Add the animation panel as a tab.

        this.animationPanel = new AutoAnimationPanel(extent, simulator, network);
        this.animationPanel.showGrid(false);
        this.borderPanel = new JPanel(new BorderLayout());
        this.borderPanel.add(this.animationPanel, BorderLayout.CENTER);
        getTabbedPane().addTab(0, "animation", this.borderPanel);
        getTabbedPane().setSelectedIndex(0); // Show the animation panel as the default tab

        // Include the GTU colorer control panel NORTH of the animation.
        this.colorControlPanel = new ColorControlPanel();
        for (Colorer<? super Gtu> colorer : gtuColorers)
        {
            this.colorControlPanel.addItem(colorer);
        }
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setPreferredSize(new Dimension(200, 35));
        this.borderPanel.add(buttonPanel, BorderLayout.NORTH);
        buttonPanel.add(this.colorControlPanel);

        // Include the TogglePanel WEST of the animation.
        this.togglePanel = new JPanel();
        this.togglePanel.setLayout(new BoxLayout(this.togglePanel, BoxLayout.Y_AXIS));
        this.borderPanel.add(this.togglePanel, BorderLayout.WEST);

        // add the buttons for home, zoom all, grid, and mouse coordinates
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(makeButton("yZoomButton", "UpDown24.png", "Reset Y-zoom", "Reset Y-zoom to match X-zoom", true));
        buttonPanel.add(makeButton("allButton", "ZoomAll24.png", "ZoomAll", "Zoom whole network", true));
        buttonPanel.add(makeButton("homeButton", "Home24.png", "Home", "Zoom to original extent", true));
        buttonPanel.add(makeButton("gridButton", "Grid24.png", "Grid", "Toggle grid on/off", true));
        buttonPanel.add(Box.createHorizontalStrut(10));

        // add info labels next to buttons
        JPanel infoTextPanel = new JPanel();
        buttonPanel.add(infoTextPanel);
        infoTextPanel.setMinimumSize(new Dimension(250, 30));
        infoTextPanel.setPreferredSize(new Dimension(250, 30));
        infoTextPanel.setMaximumSize(new Dimension(250, 30));
        infoTextPanel.setLayout(new BoxLayout(infoTextPanel, BoxLayout.Y_AXIS));
        this.coordinateField = new JLabel();
        this.coordinateField.setMinimumSize(new Dimension(150, 15));
        this.coordinateField.setPreferredSize(new Dimension(150, 15));
        this.coordinateField.setMaximumSize(new Dimension(150, 15));
        this.coordinateField.setFont(new Font("Consolas", Font.PLAIN, 12));
        infoTextPanel.add(this.coordinateField);
        // gtu fields
        JPanel gtuPanel = new JPanel();
        gtuPanel.setAlignmentX(0.0f);
        gtuPanel.setLayout(new BoxLayout(gtuPanel, BoxLayout.X_AXIS));
        gtuPanel.setMinimumSize(new Dimension(250, 15));
        gtuPanel.setPreferredSize(new Dimension(250, 15));
        gtuPanel.setMaximumSize(new Dimension(250, 15));
        infoTextPanel.add(gtuPanel);
        if (null != network)
        {
            network.addListener(this, Network.GTU_ADD_EVENT);
            network.addListener(this, Network.GTU_REMOVE_EVENT);
        }
        // gtu counter
        this.gtuCountField = new JLabel("0 GTU's");
        this.gtuCountField.setMinimumSize(new Dimension(150, 15));
        this.gtuCountField.setPreferredSize(new Dimension(150, 15));
        this.gtuCountField.setMaximumSize(new Dimension(150, 15));
        this.gtuCount = null == network ? 0 : network.getGTUs().size();
        gtuPanel.add(this.gtuCountField);
        setGtuCountText();

        // Tell the animation to build the list of animation objects.
        this.animationPanel
                .notify(new TimedEvent<>(Replication.START_REPLICATION_EVENT, null, getSimulator().getSimulatorTime()));

        // switch off the X and Y coordinates in a tooltip.
        this.animationPanel.setShowToolTip(false);

        // run the update task for the mouse coordinate panel
        new UpdateTimer().start();

        // make sure the thread gets killed when the window closes.
        installWindowCloseHandler();

    }

    /**
     * Change auto pan target.
     * @param newAutoPanId id of object to track (or
     * @param newAutoPanKind kind of object to track
     * @param newAutoPanTrack if true; tracking is continuously; if false; tracking is once
     */
    public void setAutoPan(final String newAutoPanId, final OtsSearchPanel.ObjectKind<?> newAutoPanKind,
            final boolean newAutoPanTrack)
    {
        this.autoPanId = newAutoPanId;
        this.autoPanKind = newAutoPanKind;
        this.autoPanTrack = newAutoPanTrack;
        this.autoPanOnNextPaintComponent = true;
        Logger.ots().trace("AutoPan id=" + newAutoPanId + ", kind=" + newAutoPanKind + ", track=" + newAutoPanTrack);
        if (null != this.autoPanId && null != OtsAnimationPanel.this.animationPanel && this.autoPanId.length() > 0
                && null != this.autoPanKind)
        {
            OtsAnimationPanel.this.animationPanel.repaint();
        }
    }

    /**
     * Create a button.
     * @param name name of the button
     * @param iconPath path to the resource
     * @param actionCommand the action command
     * @param toolTipText the hint to show when the mouse hovers over the button
     * @param enabled true if the new button must initially be enable; false if it must initially be disabled
     * @return JButton
     */
    private JButton makeButton(final String name, final String iconPath, final String actionCommand, final String toolTipText,
            final boolean enabled)
    {
        JButton result = new JButton(IconUtil.of(iconPath).get());
        result.setMinimumSize(new Dimension(34, 32));
        result.setPreferredSize(new Dimension(34, 32));
        result.setMaximumSize(new Dimension(34, 32));
        result.setName(name);
        result.setEnabled(enabled);
        result.setActionCommand(actionCommand);
        result.setToolTipText(toolTipText);
        result.addActionListener(this);
        this.buttons.add(result);
        return result;
    }

    /**
     * Add a button for toggling an animatable class on or off. Button icons for which 'nextToPrevious' is true will be placed
     * to the right of the previous button, which should be the corresponding button for id buttons. An example is an icon for
     * showing/hiding the class 'Lane' followed by the button to show/hide the Lane ids. Other buttons can be placed next to the
     * previous too.
     * @param name the name of the button
     * @param locatableClass the class for which the button holds (e.g., GTU.class)
     * @param iconPath the path to the 24x24 icon to display
     * @param toolTipText the tool tip text to show when hovering over the button
     * @param initiallyVisible whether the class is initially shown or not
     * @param nextToPrevious button that needs to be placed next to the previous button
     */
    public final void addToggleAnimationButtonIcon(final String name, final Class<? extends Locatable> locatableClass,
            final String iconPath, final String toolTipText, final boolean initiallyVisible, final boolean nextToPrevious)
    {
        JToggleButton button;
        Icon icon = IconUtil.of(iconPath).get();
        Icon unIcon = IconUtil.of(iconPath).gray().get();
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

        // place button to the right of the previous content button?
        if (nextToPrevious && this.togglePanel.getComponentCount() > 0)
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
        button.setText(separatedName(name));
        button.setEnabled(true);
        button.setSelected(initiallyVisible);
        button.setActionCommand(name);
        button.setToolTipText(toolTipText);
        button.addActionListener(this);
        button.setPreferredSize(new Dimension(113, 19)); // Can just fit "Generator Q" at largest Appearance Control font size
        button.setMaximumSize(new Dimension(113, 19));

        this.togglePanel.add(button);

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
     * Add buttons for toggling all GIS layers on or off.
     * @param header the name of the group of layers
     * @param gisMap the GIS map for which the toggles have to be added
     * @param toolTipText the tool tip text to show when hovering over the button
     */
    public final void addAllToggleGISButtonText(final String header, final GisRenderable2d gisMap, final String toolTipText)
    {
        addToggleText(" ");
        addToggleText(header);
        for (String layerName : gisMap.getMap().getLayerMap().keySet())
        {
            addToggleGISButtonText(layerName, layerName, gisMap, toolTipText);
        }
    }

    /**
     * Add a button to toggle a GIS Layer on or off.
     * @param layerName the name of the layer
     * @param displayName the name to display next to the tick box
     * @param gisMap the map
     * @param toolTipText the tool tip text
     */
    public final void addToggleGISButtonText(final String layerName, final String displayName, final GisRenderable2d gisMap,
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
        GisMapInterface gisMap = this.toggleGISMap.get(layerName);
        if (gisMap != null)
        {
            gisMap.showLayer(layerName);
            this.toggleGISButtons.get(layerName).setSelected(true);
            this.animationPanel.repaint();
        }
    }

    /**
     * Set a GIS layer to be hidden in the animation to true.
     * @param layerName the name of the GIS-layer that has to be hidden.
     */
    public final void hideGISLayer(final String layerName)
    {
        GisMapInterface gisMap = this.toggleGISMap.get(layerName);
        if (gisMap != null)
        {
            gisMap.hideLayer(layerName);
            this.toggleGISButtons.get(layerName).setSelected(false);
            this.animationPanel.repaint();
        }
    }

    /**
     * Toggle a GIS layer to be displayed in the animation to its reverse value.
     * @param layerName the name of the GIS-layer that has to be turned off or vice versa.
     */
    public final void toggleGISLayer(final String layerName)
    {
        GisMapInterface gisMap = this.toggleGISMap.get(layerName);
        if (gisMap != null)
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
    }

    @Override
    public final void actionPerformed(final ActionEvent actionEvent)
    {
        String actionCommand = actionEvent.getActionCommand();
        Logger.ots().trace("Action command is " + actionCommand);
        try
        {
            if (actionCommand.equals("Reset Y-zoom"))
            {
                this.animationPanel.resetZoomY();
            }
            if (actionCommand.equals("Home"))
            {
                this.animationPanel.resetZoomY();
                this.animationPanel.home();
            }
            if (actionCommand.equals("ZoomAll"))
            {
                this.animationPanel.resetZoomY();
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
     * Creates a demo panel within the animation area.
     * @param position any string from BorderLayout indicating the position of the demo panel, except CENTER.
     * @throws IllegalStateException if the panel was already created
     */
    public void createDemoPanel(final DemoPanelPosition position)
    {
        Throw.when(this.demoPanel != null, IllegalStateException.class,
                "Attempt to create demo panel, but it's already created");
        Throw.whenNull(position, "Position may not be null.");
        Container parent = this.animationPanel.getParent();
        parent.remove(this.animationPanel);

        JPanel splitPanel = new JPanel(new BorderLayout());
        parent.add(splitPanel);
        splitPanel.add(this.animationPanel, BorderLayout.CENTER);

        this.demoPanel = new JPanel();
        this.demoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        splitPanel.add(this.demoPanel, position.getBorderLayoutPosition());
    }

    /**
     * Return a panel for on-screen demo controls. The panel is create on first call.
     * @return panel
     */
    public JPanel getDemoPanel()
    {
        if (this.demoPanel == null)
        {
            createDemoPanel(DemoPanelPosition.RIGHT);
            // this.demoPanel = new JPanel();
            // this.demoPanel.setLayout(new BoxLayout(this.demoPanel, BoxLayout.Y_AXIS));
            // this.demoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            // this.demoPanel.setPreferredSize(new Dimension(300, 300));
            // getAnimationPanel().getParent().add(this.demoPanel, BorderLayout.EAST);
            this.demoPanel.addContainerListener(new ContainerListener()
            {
                @Override
                public void componentAdded(final ContainerEvent e)
                {
                    try
                    {
                        // setAppearance(getAppearance());
                    }
                    catch (NullPointerException exception)
                    {
                        //
                    }
                }

                @Override
                public void componentRemoved(final ContainerEvent e)
                {
                    //
                }
            });
        }
        return this.demoPanel;
    }

    /**
     * Update the checkmark related to a programmatically changed animation state.
     * @param locatableClass class to show the checkmark for
     */
    public final void updateAnimationClassCheckBox(final Class<? extends Locatable> locatableClass)
    {
        JToggleButton button = this.toggleButtons.get(locatableClass);
        if (button == null)
        {
            return;
        }
        button.setSelected(getAnimationPanel().isShowClass(locatableClass));
    }

    /**
     * Display the latest world coordinate based on the mouse position on the screen.
     */
    protected final void updateWorldCoordinate()
    {
        String x = String.format(COORD_FORMAT, this.animationPanel.getWorldCoordinate().getX());
        String y = String.format(COORD_FORMAT, this.animationPanel.getWorldCoordinate().getY());
        String worldPoint = "<html>(x=" + fadeLeadingZeros(x) + "; y=" + fadeLeadingZeros(y) + ")</html>";
        this.coordinateField.setText(worldPoint);
        String worldPointNoHtml = "(x=" + x + "; y=" + y + ")";
        int requiredWidth = this.coordinateField.getGraphics().getFontMetrics().stringWidth(worldPointNoHtml);
        if (this.coordinateField.getPreferredSize().width < requiredWidth)
        {
            Dimension requiredSize = new Dimension(requiredWidth, this.coordinateField.getPreferredSize().height);
            this.coordinateField.setMinimumSize(requiredSize);
            this.coordinateField.setPreferredSize(requiredSize);
            this.coordinateField.setMaximumSize(requiredSize);
            Container parent = this.coordinateField.getParent();
            requiredSize = new Dimension(requiredWidth, parent.getPreferredSize().height);
            parent.setMinimumSize(requiredSize);
            parent.setPreferredSize(requiredSize);
            parent.setMaximumSize(requiredSize);
            Logger.ots().trace("Increased minimum width to " + requiredSize.width);
            parent.revalidate();
        }
        this.coordinateField.repaint();
    }

    /**
     * Gives the leading zeros a faded color using HTML.
     * @param formatted formatted number
     * @return formatted number with leading zeros faded
     */
    protected String fadeLeadingZeros(final String formatted)
    {
        Matcher m = LEADING_ZEROS.matcher(formatted);
        if (!m.matches())
        {
            return formatted;
        }
        String sign = m.group(1) == null ? "" : m.group(1);
        String zeros = m.group(2) == null ? "" : m.group(2);
        String digits = m.group(3);
        Color brighter = ColorInterpolator.interpolateColor(this.coordinateField.getBackground(),
                this.gtuCountField.getForeground(), 0.2);
        String zerosColor = String.format("#%02x%02x%02x", brighter.getRed(), brighter.getGreen(), brighter.getBlue());
        return sign + "<span style='color:" + zerosColor + ";'>" + zeros + "</span>" + digits;
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

    /** Install the dispose on close when the OtsControlPanel is registered as part of a frame. */
    protected class DisposeOnCloseThread extends Thread
    {
        /** The current container. */
        private OtsAnimationPanel panel;

        /**
         * Constructor.
         * @param panel the OTSControlpanel container.
         */
        public DisposeOnCloseThread(final OtsAnimationPanel panel)
        {
            this.panel = panel;
        }

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
                catch (InterruptedException exception)
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

        @Override
        public final String toString()
        {
            return "DisposeOnCloseThread of OtsAnimationPanel [panel=" + this.panel + "]";
        }
    }

    @Override
    public void windowOpened(final WindowEvent e)
    {
        // No action
    }

    @Override
    public final void windowClosing(final WindowEvent e)
    {
        // No action
    }

    @Override
    public final void windowClosed(final WindowEvent e)
    {
        this.windowExited = true;
    }

    @Override
    public final void windowIconified(final WindowEvent e)
    {
        // No action
    }

    @Override
    public final void windowDeiconified(final WindowEvent e)
    {
        // No action
    }

    @Override
    public final void windowActivated(final WindowEvent e)
    {
        // No action
    }

    @Override
    public final void windowDeactivated(final WindowEvent e)
    {
        // No action
    }

    @Override
    public void notify(final Event event)
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
        String text = this.gtuCount + " GTU's";
        this.gtuCountField.setText(text);
    }

    /**
     * UpdateTimer class to update the coordinate on the screen.
     */
    protected class UpdateTimer extends Thread
    {
        /**
         * Constructor.
         */
        public UpdateTimer()
        {
            //
        }

        @Override
        public final void run()
        {
            while (!OtsAnimationPanel.this.windowExited)
            {
                if (OtsAnimationPanel.this.isShowing())
                {
                    OtsAnimationPanel.this.updateWorldCoordinate();
                }
                try
                {
                    Thread.sleep(50); // 20 times per second
                }
                catch (InterruptedException exception)
                {
                    // do nothing
                }
            }
        }

        @Override
        public final String toString()
        {
            return "UpdateTimer thread for OtsAnimationPanel";
        }

    }

    /**
     * Adds a thin space before each capital character in a {@code String}, except the first.
     * @param name name of node.
     * @return input string but with a thin space before each capital character, except the first.
     */
    public static String separatedName(final String name)
    {
        String[] parts = UPPER_PATTERN.split(name);
        if (parts.length == 1)
        {
            return parts[0];
        }
        String separator = "";
        StringBuilder stringBuilder = new StringBuilder();
        for (String part : parts)
        {
            stringBuilder.append(separator).append(part);
            separator = "\u2009"; // thin space
        }
        return stringBuilder.toString();
    }

    /**
     * Animation panel that adds autopan functionality.
     * <p>
     * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    private class AutoAnimationPanel extends AnimationPanel
    {

        /** */
        private static final long serialVersionUID = 20180430L;

        /** Network. */
        private final Network network;

        /** Last GTU that was followed. */
        private Gtu lastGtu;

        /**
         * Constructor.
         * @param extent home extent
         * @param simulator simulator
         * @param network network
         * @throws RemoteException on remote animation error
         * @throws DsolException when simulator does not implement AnimatorInterface
         */
        AutoAnimationPanel(final Rectangle2D extent, final OtsSimulatorInterface simulator, final Network network)
                throws RemoteException, DsolException
        {
            super(new Bounds2d(extent.getMinX(), extent.getMaxX(), extent.getMinY(), extent.getMaxY()), simulator);
            setPreferredSize(new Dimension(800, 600));
            this.network = network;
            MouseListener[] listeners = getMouseListeners();
            for (MouseListener listener : listeners)
            {
                removeMouseListener(listener);
            }
            this.addMouseListener(new MouseAdapter()
            {
                @SuppressWarnings("synthetic-access")
                @Override
                public void mouseClicked(final MouseEvent e)
                {
                    if (e.isControlDown())
                    {
                        Gtu gtu = getSelectedGTU(e.getPoint());
                        if (gtu != null)
                        {
                            getOtsControlPanel().getOtsSearchPanel().selectAndTrackObject("GTU", gtu.getId(), true);
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
        protected Gtu getSelectedGTU(final Point2D mousePoint)
        {
            List<LaneBasedGtu> targets = new ArrayList<>();
            Point2d point = getRenderableScale().getWorldCoordinates(mousePoint, getExtent(), getSize());
            for (Renderable2dInterface<?> renderable : getElements())
            {
                if (isShowElement(renderable) && renderable.contains(point, getExtent()))
                {
                    if (renderable.getSource() instanceof AnimationGtuData animData)
                    {
                        targets.add(animData.getObject());
                    }
                }
            }
            if (targets.size() == 1)
            {
                return targets.get(0);
            }
            return null;
        }

        @SuppressWarnings("synthetic-access")
        @Override
        public void paintComponent(final Graphics g)
        {
            final OtsSearchPanel.ObjectKind<?> panKind = OtsAnimationPanel.this.autoPanKind;
            final String panId = OtsAnimationPanel.this.autoPanId;
            final boolean doPan = OtsAnimationPanel.this.autoPanOnNextPaintComponent;
            OtsAnimationPanel.this.autoPanOnNextPaintComponent = OtsAnimationPanel.this.autoPanTrack;
            if (doPan && panKind != null && panId != null)
            {
                Optional<? extends Locatable> locatable = panKind.searchNetwork(this.network, panId);
                if (locatable.isPresent())
                {
                    Point<?> point = locatable.get().getLocation();
                    if (point != null) // Center extent around point
                    {
                        double w = getExtent().getDeltaX();
                        double h = getExtent().getDeltaY();
                        setExtent(new Bounds2d(point.getX() - w / 2, point.getX() + w / 2, point.getY() - h / 2,
                                point.getY() + h / 2));
                    }
                }
            }
            super.paintComponent(g);
        }

        @Override
        public void setBackground(final Color bg)
        {
            int threshold = 64;
            int alternative = 96;
            if (bg.getRed() <= threshold && bg.getGreen() <= threshold && bg.getBlue() <= threshold)
            {
                setGridColor(new Color(alternative, alternative, alternative));
            }
            else
            {
                setGridColor(Color.BLACK);
            }
            super.setBackground(bg);
        }

        @Override
        public String toString()
        {
            return "AutoAnimationPanel [network=" + this.network + ", lastGtu=" + this.lastGtu + "]";
        }
    }

    /**
     * Enum for demo panel position. Each value contains a field representing the position correlating to the
     * {@code BorderLayout} class.
     */
    public enum DemoPanelPosition
    {
        /** Top. */
        TOP("First"),

        /** Bottom. */
        BOTTOM("Last"),

        /** Left. */
        LEFT("Before"),

        /** Right. */
        RIGHT("After");

        /** Value used in {@code BorderLayout}. */
        private final String direction;

        /**
         * Constructor.
         * @param direction value used in {@code BorderLayout}
         */
        DemoPanelPosition(final String direction)
        {
            this.direction = direction;
        }

        /**
         * Return border layout position.
         * @return value used in {@code BorderLayout}
         */
        public String getBorderLayoutPosition()
        {
            return this.direction;
        }
    }

}
