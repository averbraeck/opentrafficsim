package org.opentrafficsim.swing.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.point.Point;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.TimedEvent;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.animation.IconUtil;
import org.opentrafficsim.animation.data.AnimationGtuData;
import org.opentrafficsim.animation.gtu.colorer.GtuColorerManager;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.draw.ColorInterpolator;
import org.opentrafficsim.road.gtu.LaneBasedGtu;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.d2.Renderable2dInterface;
import nl.tudelft.simulation.dsol.animation.gis.GisMapInterface;
import nl.tudelft.simulation.dsol.animation.gis.GisRenderable2d;
import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.dsol.swing.animation.d2.AnimationPanel;
import nl.tudelft.simulation.dsol.swing.gui.ConsoleOutput;
import nl.tudelft.simulation.dsol.swing.gui.TabbedContentPane;
import nl.tudelft.simulation.language.DsolException;

/**
 * Simulation panel with various controls and animation.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class OtsSimulationPanel extends JPanel implements ActionListener, EventListener
{
    /** */
    private static final long serialVersionUID = 20150617L;

    static
    {
        // use narrow border for TabbedPane, which cannot be changed afterwards
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(1, 0, 1, 0));
    }

    /** Simulator. */
    private final OtsSimulatorInterface simulator;

    /** Control panel to control start/stop, speed of the simulation. */
    private final OtsSimulationControlPanel otsSimulationControlPanel;

    /** Tabbed pane that contains the different (default) screens. */
    private final TabbedContentPane tabbedPane;

    /** Pattern to split string by upper case, with lower case adjacent, without disregarding the match itself. */
    private static final Pattern UPPER_PATTERN = Pattern.compile("(?=\\p{Lu})(?<=\\p{Ll})|(?=\\p{Lu}\\p{Ll})");

    /** Format for the world coordinates. */
    private static final String COORD_FORMAT = "%09.2f";

    /** Pattern to split leading zeros from the rest of a number. */
    private static final Pattern LEADING_ZEROS = Pattern.compile("^([+-]?)(0*)((:?0|[1-9]\\d*)(:?[\\.,]\\d+)?)$");

    /** OTS search panel. */
    private final OtsSearchPanel otsSearchPanel;

    /** Animation panel on tab position 0. */
    private final OtsAnimationPanel otsAnimationPanel;

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

    /** GTU color panel. */
    private OtsGtuColorPanel gtuColorPanel = null;

    /** Coordinates of the cursor. */
    private final JLabel coordinateField;

    /** GTU count field. */
    private final JLabel gtuCountField;

    /** GTU count. */
    private int gtuCount = 0;

    /** Id of object to auto pan to. */
    private String autoPanId = null;

    /** Type of object to auto pan to. */
    private OtsSearchPanel.ObjectKind<?> autoPanKind = null;

    /** Track auto pan object continuously? */
    private boolean autoPanTrack = false;

    /** Track auto on the next paint (if this is true but autoPanTrack is false, only a one-shot auto pan). */
    private boolean autoPanOnNextPaintComponent = false;

    /**
     * Construct a panel that looks like the DSOLPanel for quick building of OTS applications.
     * @param network network
     * @throws RemoteException when notification of the animation panel fails
     * @throws DsolException when simulator does not implement AnimatorInterface
     */
    public OtsSimulationPanel(final Network network) throws RemoteException, DsolException
    {
        this(network.getExtent(), network);
    }

    /**
     * Construct a panel that looks like the DSOLPanel for quick building of OTS applications.
     * @param extent bottom left corner, length and width of the area (world) to animate
     * @param network network
     * @throws RemoteException when notification of the animation panel fails
     * @throws DsolException when simulator does not implement AnimatorInterface
     */
    public OtsSimulationPanel(final Rectangle2D extent, final Network network) throws RemoteException, DsolException
    {
        this(extent, network, new OtsSimulationPanelDecorator()
        {
        });
    }

    /**
     * Construct a panel that looks like the DSOLPanel for quick building of OTS applications.
     * @param network network
     * @param decorator decorator for the animation panel
     * @throws RemoteException when notification of the animation panel fails
     * @throws DsolException when simulator does not implement AnimatorInterface
     * @throws NullPointerException when any input is {@code null}
     */
    public OtsSimulationPanel(final Network network, final OtsSimulationPanelDecorator decorator)
            throws RemoteException, DsolException
    {
        this(network.getExtent(), network, decorator);
    }

    /**
     * Construct a panel that looks like the DSOLPanel for quick building of OTS applications.
     * @param extent bottom left corner, length and width of the area (world) to animate
     * @param network network
     * @param decorator decorator for the animation panel
     * @throws RemoteException when notification of the animation panel fails
     * @throws DsolException when simulator does not implement AnimatorInterface
     * @throws NullPointerException when any input is {@code null}
     */
    public OtsSimulationPanel(final Rectangle2D extent, final Network network, final OtsSimulationPanelDecorator decorator)
            throws RemoteException, DsolException
    {
        Throw.whenNull(network, "network");
        Throw.whenNull(decorator, "decorator");

        AppearanceApplication.setDefaultFont();

        this.simulator = network.getSimulator();

        /*-
         * .-- OtsSimulationApplication -------------------------------------------------------------------------.
         * | OTS | The Open Traffic Simulator | {model name}                                               _ # X |
         * |o== OtsSimulationPanel =============================================================================o|
         * ||+-- topPanel -------------------------------------------------------------------------------------+||
         * |||o-- OtsSimulationControlPanel ----------------------------------o-- OtsSearchPanel -------------o|||
         * ||||  >  >  >                                  00:00:00.000  0.00x |  GTU|v| |Id...    | track     ||||
         * |||o---------------------------------------------------------------o-------------------------------o|||
         * ||o-- AppearanceControlTabbedContentPane -----------------------------------------------------------o||
         * |||+-- borderLayoutPanel --------------------------------------------------------------------------+|||
         * ||||+-- animationTopBarPanel ---------------------------------------------------------------------+||||
         * |||||o-- OtsGtuColorPanel -----------------------------------o               +-- infoTextPanel --+|||||
         * ||||||  Blue     |v|                                         |    Home Grid  | 0 GTU's           ||||||
         * |||||o-------------------------------------------------------o               +-------------------+|||||
         * ||||+-- toggle --o-- OtsAnimationPanel -----------------------------------------------------------o||||
         * |||||   -Panel   |                                                                                |||||
         * |||||            |                                                                                |||||
         * |||||            |                        +-- demoPanel -----------------+                        |||||
         * |||||            |                        |                              |                        |||||
         * |||||            |                        | (top, bottom, left or right) |                        |||||
         * |||||            |                        |                              |                        |||||
         * |||||            |                        +------------------------------+                        |||||
         * |||||            |                                                                                |||||
         * |||||            |                                                                                |||||
         * ||||+------------o--------------------------------------------------------------------------------o||||
         * |||+-----------------------------------------------------------------------------------------------+|||
         * ||| animation /                                                                                     |||
         * ||o-------------------------------------------------------------------------------------------------o||
         * |o---------------------------------------------------------------------------------------------------o|
         * '-----------------------------------------------------------------------------------------------------'
         *
         * Legend:   +-- lowerCaseName -------+    o-- UpperCaseName -------o
         *           |     Regular JPanel     |    |     Specific class     |
         *           +------------------------+    o------------------------o
         */

        setLayout(new BorderLayout());

        // topPanel > OtsSimulationControlPanel, OtsSearchPanel
        JPanel topPanel = new JPanel();
        topPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        this.otsSimulationControlPanel = new OtsSimulationControlPanel(this.simulator, this);
        this.otsSearchPanel = new OtsSearchPanel(this);
        topPanel.add(this.otsSimulationControlPanel);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(this.otsSearchPanel);
        add(topPanel, BorderLayout.NORTH);

        // tabbedPane with borderLayoutPanel in animation tab
        this.tabbedPane = new AppearanceControlTabbedContentPane(SwingConstants.BOTTOM);
        JPanel borderLayoutPanel = new JPanel(new BorderLayout());
        this.tabbedPane.addTab(0, "animation", borderLayoutPanel);
        add(this.tabbedPane, BorderLayout.CENTER);

        // borderLayoutPanel > animationTopBarPanel, togglePanel, OtsAnimationPanel
        JPanel animationTopBarPanel = new JPanel();
        animationTopBarPanel.setLayout(new BoxLayout(animationTopBarPanel, BoxLayout.X_AXIS));
        borderLayoutPanel.add(animationTopBarPanel, BorderLayout.NORTH);
        this.togglePanel = new JPanel();
        this.togglePanel.setLayout(new BoxLayout(this.togglePanel, BoxLayout.Y_AXIS));
        borderLayoutPanel.add(this.togglePanel, BorderLayout.WEST);
        this.otsAnimationPanel = new OtsAnimationPanel(extent, this.simulator, network);
        this.otsAnimationPanel.showGrid(false);
        borderLayoutPanel.add(this.otsAnimationPanel, BorderLayout.CENTER);

        // animationTopBarPanel > OtsGtuColorPanel, buttons, infoTextPanel
        this.gtuColorPanel = new OtsGtuColorPanel();
        decorator.getGtuColorers().forEach((colorer) -> this.gtuColorPanel.addGtuColorer(colorer));
        animationTopBarPanel.add(this.gtuColorPanel);
        animationTopBarPanel.add(Box.createHorizontalStrut(10));
        animationTopBarPanel.add(makeButton("yZoomButton", "UpDown24.png", "Reset Y-zoom", "Reset Y-zoom", true));
        animationTopBarPanel.add(makeButton("allButton", "ZoomAll24.png", "ZoomAll", "Zoom whole network", true));
        animationTopBarPanel.add(makeButton("homeButton", "Home24.png", "Home", "Zoom to original extent", true));
        animationTopBarPanel.add(makeButton("gridButton", "Grid24.png", "Grid", "Toggle grid on/off", true));
        animationTopBarPanel.add(Box.createHorizontalStrut(10));
        JPanel infoTextPanel = new JPanel();
        animationTopBarPanel.add(infoTextPanel);
        infoTextPanel.setMinimumSize(new Dimension(250, 30));
        infoTextPanel.setPreferredSize(new Dimension(250, 30));
        infoTextPanel.setMaximumSize(new Dimension(250, 30));
        infoTextPanel.setLayout(new BoxLayout(infoTextPanel, BoxLayout.Y_AXIS));

        // infoTextPanel contents
        this.coordinateField = new JLabel();
        this.coordinateField.setMinimumSize(new Dimension(150, 15));
        this.coordinateField.setPreferredSize(new Dimension(150, 15));
        this.coordinateField.setMaximumSize(new Dimension(150, 15));
        this.coordinateField.setFont(new Font("Consolas", Font.PLAIN, 12));
        infoTextPanel.add(this.coordinateField);
        this.gtuCountField = new JLabel();
        this.gtuCountField.setMinimumSize(new Dimension(150, 15));
        this.gtuCountField.setPreferredSize(new Dimension(150, 15));
        this.gtuCountField.setMaximumSize(new Dimension(150, 15));
        this.gtuCount = null == network ? 0 : network.getGTUs().size();
        infoTextPanel.add(this.gtuCountField);
        setGtuCountText();

        // only show OtsSearchPanel when the animation tab is selected
        this.tabbedPane.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(final ChangeEvent e)
            {
                int index = OtsSimulationPanel.this.tabbedPane.getSelectedIndex();
                Component component = OtsSimulationPanel.this.tabbedPane.getComponentAt(index);
                OtsSimulationPanel.this.otsSearchPanel.setVisible(borderLayoutPanel.equals(component));
            }
        });

        // listen to update GTU count
        if (null != network)
        {
            network.addListener(this, Network.GTU_ADD_EVENT);
            network.addListener(this, Network.GTU_REMOVE_EVENT);
        }

        // fake start event to draw static objects before the simulation is started (this will all be cleared on real start)
        this.otsAnimationPanel
                .notify(new TimedEvent<>(Replication.START_REPLICATION_EVENT, null, getSimulator().getSimulatorTime()));

        // switch off the X and Y coordinates in a tool-tip
        this.otsAnimationPanel.setShowToolTip(false);

        // decorate
        decorator.decorate(this, network);
    }

    /**
     * Adds the console tab.
     */
    public void addConsoleTab()
    {
        ConsoleOutput console = new ConsoleOutput();
        console.setBorder(null);
        this.tabbedPane.addTab("console", console);
    }

    /**
     * Return tabbed pane.
     * @return tabbed pane
     */
    public TabbedContentPane getTabbedPane()
    {
        return this.tabbedPane;
    }

    /**
     * Return simulator.
     * @return simulator
     */
    public OtsSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    /**
     * Enable the simulation or animation buttons in the GUI. This method HAS TO BE CALLED in order for the buttons to be
     * enabled, because the initial state is DISABLED. Typically, this is done after all tabs, statistics, and other user
     * interface and model components have been constructed and initialized.
     */
    public void enableSimulationControlButtons()
    {
        this.otsSimulationControlPanel.setSimulationControlButtons(true);
    }

    /**
     * Disable the simulation or animation buttons in the GUI.
     */
    public void disableSimulationControlButtons()
    {
        this.otsSimulationControlPanel.setSimulationControlButtons(false);
    }

    /**
     * Change auto pan target.
     * @param newAutoPanId id of object to track
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
        if (null != this.autoPanId && null != OtsSimulationPanel.this.otsAnimationPanel && this.autoPanId.length() > 0
                && null != this.autoPanKind)
        {
            OtsSimulationPanel.this.otsAnimationPanel.repaint();
        }
    }

    /**
     * Create a button.
     * @param name name of the button
     * @param iconFile name of the icon file
     * @param actionCommand the action command
     * @param toolTipText the hint to show when the mouse hovers over the button
     * @param enabled true if the new button must initially be enable; false if it must initially be disabled
     * @return button
     */
    private JButton makeButton(final String name, final String iconFile, final String actionCommand, final String toolTipText,
            final boolean enabled)
    {
        JButton result = new JButton(IconUtil.of(iconFile).get());
        result.setMinimumSize(new Dimension(34, 32));
        result.setPreferredSize(new Dimension(34, 32));
        result.setMaximumSize(new Dimension(34, 32));
        result.setName(name);
        result.setEnabled(enabled);
        result.setActionCommand(actionCommand);
        result.setToolTipText(toolTipText);
        result.addActionListener(this);
        return result;
    }

    /**
     * Add a button for toggling an animation class on or off. Button icons for which 'nextToPrevious' is true will be placed to
     * the right of the previous button, which should be the corresponding button for id buttons. An example is an icon for
     * showing/hiding the class 'Lane' followed by the button to show/hide the Lane ids. Other buttons can be placed next to the
     * previous too.
     * @param name the name of the button
     * @param locatableClass the class for which the button holds (e.g., GTU.class)
     * @param iconPath the path to the 24x24 icon to display
     * @param toolTipText the tool tip text to show when hovering over the button
     * @param initiallyVisible whether the class is initially shown or not
     * @param nextToPrevious button that needs to be placed next to the previous button
     */
    public void addToggleAnimationButtonIcon(final String name, final Class<? extends Locatable> locatableClass,
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
            this.otsAnimationPanel.showClass(locatableClass);
        }
        else
        {
            this.otsAnimationPanel.hideClass(locatableClass);
        }
        this.toggleLocatableMap.put(name, locatableClass);
        this.toggleButtons.put(locatableClass, button);
    }

    /**
     * Add a button for toggling an animation class on or off.
     * @param name the name of the button
     * @param locatableClass the class for which the button holds (e.g., {@code GTU.class})
     * @param toolTipText the tool tip text to show when hovering over the button
     * @param initiallyVisible whether the class is initially shown or not
     */
    public void addToggleAnimationButtonText(final String name, final Class<? extends Locatable> locatableClass,
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
            this.otsAnimationPanel.showClass(locatableClass);
        }
        else
        {
            this.otsAnimationPanel.hideClass(locatableClass);
        }
        this.toggleLocatableMap.put(name, locatableClass);
        this.toggleButtons.put(locatableClass, button);
    }

    /**
     * Add a text to explain animation classes.
     * @param text the text to show
     */
    public void addToggleText(final String text)
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
    public void addAllToggleGISButtonText(final String header, final GisRenderable2d gisMap, final String toolTipText)
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
    public void addToggleGISButtonText(final String layerName, final String displayName, final GisRenderable2d gisMap,
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
     * @param layerName the name of the GIS-layer that has to be shown
     */
    public void showGISLayer(final String layerName)
    {
        GisMapInterface gisMap = this.toggleGISMap.get(layerName);
        if (gisMap != null)
        {
            gisMap.showLayer(layerName);
            this.toggleGISButtons.get(layerName).setSelected(true);
            this.otsAnimationPanel.repaint();
        }
    }

    /**
     * Set a GIS layer to be hidden in the animation to true.
     * @param layerName the name of the GIS-layer that has to be hidden
     */
    public void hideGISLayer(final String layerName)
    {
        GisMapInterface gisMap = this.toggleGISMap.get(layerName);
        if (gisMap != null)
        {
            gisMap.hideLayer(layerName);
            this.toggleGISButtons.get(layerName).setSelected(false);
            this.otsAnimationPanel.repaint();
        }
    }

    /**
     * Toggle a GIS layer to be displayed in the animation to its reverse value.
     * @param layerName the name of the GIS-layer that has to be turned off or vice versa
     */
    public void toggleGISLayer(final String layerName)
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
            this.otsAnimationPanel.repaint();
        }
    }

    @Override
    public void actionPerformed(final ActionEvent actionEvent)
    {
        String actionCommand = actionEvent.getActionCommand();
        Logger.ots().trace("Action command is " + actionCommand);
        try
        {
            if (actionCommand.equals("Reset Y-zoom"))
            {
                this.otsAnimationPanel.resetZoomY();
            }
            if (actionCommand.equals("Home"))
            {
                this.otsAnimationPanel.resetZoomY();
                this.otsAnimationPanel.home();
            }
            if (actionCommand.equals("ZoomAll"))
            {
                this.otsAnimationPanel.resetZoomY();
                this.otsAnimationPanel.zoomAll();
            }
            if (actionCommand.equals("Grid"))
            {
                this.otsAnimationPanel.showGrid(!this.otsAnimationPanel.isShowGrid());
            }

            if (this.toggleLocatableMap.containsKey(actionCommand))
            {
                Class<? extends Locatable> locatableClass = this.toggleLocatableMap.get(actionCommand);
                this.otsAnimationPanel.toggleClass(locatableClass);
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
     * @return animation panel
     */
    public AnimationPanel getAnimationPanel()
    {
        return this.otsAnimationPanel;
    }

    /**
     * Creates a demo panel within the animation area.
     * @param position position within the animation panel
     * @throws IllegalStateException if the panel was already created
     */
    public void createDemoPanel(final DemoPanelPosition position)
    {
        Throw.when(this.demoPanel != null, IllegalStateException.class,
                "Attempt to create demo panel, but it's already created");
        Throw.whenNull(position, "Position may not be null.");
        Container parent = this.otsAnimationPanel.getParent();
        parent.remove(this.otsAnimationPanel);

        JPanel splitPanel = new JPanel(new BorderLayout());
        parent.add(splitPanel);
        splitPanel.add(this.otsAnimationPanel, BorderLayout.CENTER);

        this.demoPanel = new JPanel();
        this.demoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        splitPanel.add(this.demoPanel, position.getBorderLayoutPosition());
    }

    /**
     * Return a panel for on-screen demo controls. The panel is created on the right of the screen on first call, unless
     * {@link #createDemoPanel} was already called.
     * @return demo panel
     */
    public JPanel getDemoPanel()
    {
        if (this.demoPanel == null)
        {
            createDemoPanel(DemoPanelPosition.RIGHT);
        }
        return this.demoPanel;
    }

    /**
     * Update the check-mark related to a programmatically changed animation state.
     * @param locatableClass class to show the check-mark for
     */
    public void updateAnimationClassCheckBox(final Class<? extends Locatable> locatableClass)
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
    private void updateWorldCoordinate()
    {
        String x = String.format(COORD_FORMAT, this.otsAnimationPanel.getWorldCoordinate().getX());
        String y = String.format(COORD_FORMAT, this.otsAnimationPanel.getWorldCoordinate().getY());
        String worldPoint = "<html>(x=" + fadeLeadingZeros(x) + "; y=" + fadeLeadingZeros(y) + ")</html>";
        this.coordinateField.setText(worldPoint);
        String worldPointNoHtml = "(x=" + x + "; y=" + y + ")";
        if (this.coordinateField.getGraphics() == null)
        {
            // window is in a deleted state, but the mouse listener is still causing this event
            return;
        }
        // add 10px margin for if the window is dragged to another monitor and Swing finds a few more pixels required there due
        // to the monitor being set to e.g. 125% instead of 100%
        int requiredWidth = this.coordinateField.getGraphics().getFontMetrics().stringWidth(worldPointNoHtml) + 10;
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
    private String fadeLeadingZeros(final String formatted)
    {
        Matcher m = LEADING_ZEROS.matcher(formatted);
        if (!m.matches())
        {
            return formatted;
        }
        String sign = m.group(1) == null ? "" : m.group(1);
        String zeros = m.group(2) == null ? "" : m.group(2);
        String digits = m.group(3);
        Color faded = ColorInterpolator.interpolateColor(this.coordinateField.getBackground(),
                this.gtuCountField.getForeground(), 0.2);
        String zerosColor = String.format("#%02x%02x%02x", faded.getRed(), faded.getGreen(), faded.getBlue());
        return sign + "<span style='color:" + zerosColor + ";'>" + zeros + "</span>" + digits;
    }

    /**
     * GTU colorer manager from the GTU color panel.
     * @return GTU colorer manager from the GTU color panel
     */
    public GtuColorerManager getGtuColorerManager()
    {
        return this.gtuColorPanel.getGtuColorerManager();
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
     * Adds a thin space before each capital character in a {@code String}, except the first.
     * @param name name of node
     * @return input string but with a thin space before each capital character, except the first
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
     * Animation panel that adds auto-pan functionality.
     */
    private class OtsAnimationPanel extends AnimationPanel
    {

        /** */
        private static final long serialVersionUID = 20180430L;

        /** Network. */
        private final Network network;

        /**
         * Constructor.
         * @param extent home extent
         * @param simulator simulator
         * @param network network
         * @throws RemoteException on remote animation error
         * @throws DsolException when simulator does not implement AnimatorInterface
         */
        OtsAnimationPanel(final Rectangle2D extent, final OtsSimulatorInterface simulator, final Network network)
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
                @Override
                public void mouseClicked(final MouseEvent e)
                {
                    if (e.isControlDown())
                    {
                        Gtu gtu = getSelectedGtu(e.getPoint());
                        if (gtu != null)
                        {
                            OtsSimulationPanel.this.otsSearchPanel.selectAndTrackObject("GTU", gtu.getId(), true);
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
         * Set the world coordinates based on a mouse move.
         * @param point the x,y world coordinates
         */
        @Override
        public synchronized void setWorldCoordinate(final Point2d point)
        {
            super.setWorldCoordinate(point);
            updateWorldCoordinate();
        }

        /**
         * returns the list of selected objects at a certain mousePoint.
         * @param mousePoint the mousePoint
         * @return the selected objects
         */
        private Gtu getSelectedGtu(final Point2D mousePoint)
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

        @Override
        public void paintComponent(final Graphics g)
        {
            final OtsSearchPanel.ObjectKind<?> panKind = OtsSimulationPanel.this.autoPanKind;
            final String panId = OtsSimulationPanel.this.autoPanId;
            final boolean doPan = OtsSimulationPanel.this.autoPanOnNextPaintComponent;
            OtsSimulationPanel.this.autoPanOnNextPaintComponent = OtsSimulationPanel.this.autoPanTrack;
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

        // Overridden because there are rounding and vertical mod errors in the super implementation.
        // See https://github.com/averbraeck/dsol/issues/116.
        @Override
        protected synchronized void drawGrid(final Graphics g)
        {
            // we prepare the graphics object for the grid
            g.setFont(g.getFont().deriveFont(11.0f));
            g.setColor(this.getGridColor());
            double scaleX = this.getRenderableScale().getXScale(this.getExtent(), this.getSize());
            double scaleY = this.getRenderableScale().getYScale(this.getExtent(), this.getSize());

            int count = 0;
            double gridSizePixelsX = this.gridSizeX / scaleX;
            while (gridSizePixelsX < 40)
            {
                this.gridSizeX = 10 * this.gridSizeX;
                int maximumNumberOfDigits = (int) Math.max(0, 1 + Math.ceil(Math.log(1 / this.gridSizeX) / Math.log(10)));
                this.formatter.setMaximumFractionDigits(maximumNumberOfDigits);
                gridSizePixelsX = (int) Math.round(this.gridSizeX / scaleX);
                if (count++ > 10)
                {
                    break;
                }
            }

            count = 0;
            while (gridSizePixelsX > 10 * 40)
            {
                int maximumNumberOfDigits = (int) Math.max(0, 2 + Math.ceil(Math.log(1 / this.gridSizeX) / Math.log(10)));
                this.formatter.setMaximumFractionDigits(maximumNumberOfDigits);
                this.gridSizeX = this.gridSizeX / 10;
                gridSizePixelsX = (int) Math.round(this.gridSizeX / scaleX);
                if (count++ > 10)
                {
                    break;
                }
            }

            double gridSizePixelsY = this.gridSizeY / scaleY;
            while (gridSizePixelsY < 40)
            {
                this.gridSizeY = 10 * this.gridSizeY;
                int maximumNumberOfDigits = (int) Math.max(0, 1 + Math.ceil(Math.log(1 / this.gridSizeY) / Math.log(10)));
                this.formatter.setMaximumFractionDigits(maximumNumberOfDigits);
                gridSizePixelsY = (int) Math.round(this.gridSizeY / scaleY);
                if (count++ > 10)
                {
                    break;
                }
            }

            count = 0;
            while (gridSizePixelsY > 10 * 40)
            {
                int maximumNumberOfDigits = (int) Math.max(0, 2 + Math.ceil(Math.log(1 / this.gridSizeY) / Math.log(10)));
                this.formatter.setMaximumFractionDigits(maximumNumberOfDigits);
                this.gridSizeY = this.gridSizeY / 10;
                gridSizePixelsY = (int) Math.round(this.gridSizeY / scaleY);
                if (count++ > 10)
                {
                    break;
                }
            }

            // Let's draw the vertical lines
            double mod = this.getExtent().getMinX() % this.gridSizeX;
            double x = -mod / scaleX;
            while (x < this.getWidth())
            {
                Point2d point = this.getRenderableScale().getWorldCoordinates(new Point2D.Double(x, 0), this.getExtent(),
                        this.getSize());
                if (point != null)
                {
                    String label = this.formatter.format(Math.round(point.getX() / this.gridSizeX) * this.gridSizeX);
                    double labelWidth = this.getFontMetrics(this.getFont()).getStringBounds(label, g).getWidth();
                    if (x > labelWidth + 4)
                    {
                        int xInt = (int) Math.round(x);
                        g.drawLine(xInt, 15, xInt, this.getHeight());
                        g.drawString(label, (int) Math.round(x - 0.5 * labelWidth), 11);
                    }
                }
                x = x + gridSizePixelsX;
            }

            // Let's draw the horizontal lines
            mod = this.getExtent().getMinY() % this.gridSizeY;
            double y = this.getSize().getHeight() + (mod / scaleY);
            while (y > 15)
            {
                Point2d point = this.getRenderableScale().getWorldCoordinates(new Point2D.Double(0, y), this.getExtent(),
                        this.getSize());
                if (point != null)
                {
                    String label = this.formatter.format(Math.round(point.getY() / this.gridSizeY) * this.gridSizeY);
                    RectangularShape labelBounds = this.getFontMetrics(this.getFont()).getStringBounds(label, g);
                    int yInt = (int) Math.round(y);
                    g.drawLine((int) Math.round(labelBounds.getWidth() + 4), yInt, this.getWidth(), yInt);
                    g.drawString(label, 2, (int) Math.round(y + labelBounds.getHeight() * 0.3));
                }
                y = y - gridSizePixelsY;
            }
        }

        @Override
        public String toString()
        {
            return "OtsAnimationPanel [network=" + this.network + "]";
        }
    }

    /**
     * Enum for demo panel position. Each value contains a field representing the position correlating to the
     * {@link BorderLayout} class.
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

        /** Value used in {@link BorderLayout}. */
        private final String direction;

        /**
         * Constructor.
         * @param direction value used in {@link BorderLayout}
         */
        DemoPanelPosition(final String direction)
        {
            this.direction = direction;
        }

        /**
         * Return border layout position.
         * @return value used in {@link BorderLayout}
         */
        public String getBorderLayoutPosition()
        {
            return this.direction;
        }

    }

}
