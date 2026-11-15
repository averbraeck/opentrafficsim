package org.opentrafficsim.editor.extensions.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseListener;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import javax.naming.NamingException;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.Box.Filler;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.djutils.draw.bounds.Bounds2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.LocalEventProducer;
import org.djutils.event.reference.ReferenceType;
import org.opentrafficsim.animation.data.util.IconUtil;
import org.opentrafficsim.animation.network.LinkAnimation;
import org.opentrafficsim.animation.network.LinkAnimation.LinkData;
import org.opentrafficsim.animation.network.NodeAnimation;
import org.opentrafficsim.animation.network.NodeAnimation.NodeData;
import org.opentrafficsim.animation.road.BusStopAnimation;
import org.opentrafficsim.animation.road.BusStopAnimation.BusStopData;
import org.opentrafficsim.animation.road.CrossSectionElementAnimation.ShoulderData;
import org.opentrafficsim.animation.road.GtuGeneratorPositionAnimation;
import org.opentrafficsim.animation.road.GtuGeneratorPositionAnimation.GtuGeneratorPositionData;
import org.opentrafficsim.animation.road.LaneAnimation;
import org.opentrafficsim.animation.road.LaneAnimation.CenterLine;
import org.opentrafficsim.animation.road.LaneAnimation.LaneData;
import org.opentrafficsim.animation.road.LaneDetectorAnimation;
import org.opentrafficsim.animation.road.LaneDetectorAnimation.LoopDetectorData;
import org.opentrafficsim.animation.road.LaneDetectorAnimation.SinkData;
import org.opentrafficsim.animation.road.PriorityAnimation.PriorityData;
import org.opentrafficsim.animation.road.StripeAnimation.StripeData;
import org.opentrafficsim.animation.road.TrafficLightAnimation;
import org.opentrafficsim.animation.road.TrafficLightAnimation.TrafficLightData;
import org.opentrafficsim.core.geometry.CurveFlattener;
import org.opentrafficsim.editor.ComboBoxSearchSupport;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.Undo;
import org.opentrafficsim.editor.XsdPaths;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;
import org.opentrafficsim.editor.extensions.map.edit.MapInputListener;
import org.opentrafficsim.road.network.factory.xml.utils.StripeSynchronization;
import org.opentrafficsim.swing.gui.AppearanceControlComboBox;
import org.opentrafficsim.swing.gui.PropertiesStore;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.d2.Renderable2d;
import nl.tudelft.simulation.dsol.simulators.AnimatorInterface;
import nl.tudelft.simulation.dsol.swing.animation.d2.InputListener;
import nl.tudelft.simulation.naming.context.ContextInterface;
import nl.tudelft.simulation.naming.context.Contextualized;
import nl.tudelft.simulation.naming.context.JvmContext;

/**
 * Editor map.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public final class EditorMap extends JPanel implements EventListener
{

    /** Serialization version UID. */
    private static final long serialVersionUID = 20231010L;

    /** All types that are valid to show in the map. */
    private static final Set<String> TYPES = Set.of(XsdPaths.CENTROID, XsdPaths.CONNECTOR, XsdPaths.NODE, XsdPaths.LINK,
            XsdPaths.TRAFFIC_LIGHT, XsdPaths.SINK, XsdPaths.GENERATOR, XsdPaths.LIST_GENERATOR);

    /** Color for links with the selected link type. */
    private static final Color LINK_TYPE_COLOR =
            OtsEditor.PROPERTIES_STORE.getColorOrDefault("map.linkTypeColor", Color.GREEN.darker());

    /** Link shapes. */
    public static final List<String> LINK_SHAPES = List.of("Straight", "Bezier", "Clothoid", "Arc", "PolyLine");

    /** Context provider. */
    private final Contextualized contextualized;

    /** Editor. */
    private final OtsEditor editor;

    /** Panel to draw in. */
    private final MapVisualizationPanel visualizationPanel;

    /** Panel with tools. */
    private final JPanel toolPanel;

    /** Add mode button. */
    private AbstractButton addModeButton;

    /** Centroid mode button. */
    private AbstractButton centroidModeButton;

    /** Auto-fix button. */
    private AbstractButton autoFixButton;

    /** Default shape selector. */
    private JComboBox<String> defaultShape;

    /** Default road layout selector. */
    private JComboBox<String> defaultRoadLayout;

    /** Road layout paint button. */
    private AbstractButton roadLayoutApplyButton;

    /** Default link type selector. */
    private JComboBox<String> defaultLinkType;

    /** Link type paint button. */
    private AbstractButton linkTypeApplyButton;

    /** Link type show button. */
    private AbstractButton linkTypeShowButton;

    /** Current document root. */
    private XsdTreeNodeRoot root;

    /** Defined road layouts represented in the selector. */
    private final Map<XsdTreeNode, String> definedRoadLayouts = new LinkedHashMap<>();

    /** Defined link types represented in the selector. */
    private final Map<XsdTreeNode, String> definedLinkTypes = new LinkedHashMap<>();

    /** Panel with toggles. */
    private final JPanel togglePanel;

    /** All map data's drawn (or hidden as they are invalid). */
    private final LinkedHashMap<XsdTreeNode, MapData> datas = new LinkedHashMap<>();

    /** Weak references to all created link data's. */
    private final WeakHashMap<MapLinkData, Object> links = new WeakHashMap<>();

    /** Listeners to road layouts. */
    private final LinkedHashMap<XsdTreeNode, RoadLayoutListener> roadLayoutListeners = new LinkedHashMap<>();

    /** Listener to flattener at network level. */
    private FlattenerListener networkFlattenerListener;

    /** Input listener. */
    private MapInputListener inputListener;

    /** Animation objects of all data's drawn. */
    private final LinkedHashMap<XsdTreeNode, Renderable2d<?>> animations = new LinkedHashMap<>();

    /** Map of toggle names to toggle animation classes. */
    private Map<String, Class<? extends Locatable>> toggleLocatableMap = new LinkedHashMap<>();

    /** Map of synchronizable stripes. */
    private Map<MapStripeData, SynchronizableMapStripe> synStripes = new LinkedHashMap<>();

    /** Whether synchronization of stripes is scheduled. */
    private boolean stripeSynchronizationScheduled = false;

    /** Listeners to lane and stripe overrides. */
    private final Map<XsdTreeNode, ChangeListener<Object>> overrideListeners = new LinkedHashMap<>();

    /** Updater of map animation. */
    private final MapUpdater updater = new MapUpdater();

    /**
     * Constructor.
     * @param contextualized context provider.
     * @param editor editor
     * @throws RemoteException context binding problem
     * @throws NamingException context binding problem
     */
    private EditorMap(final Contextualized contextualized, final OtsEditor editor) throws RemoteException, NamingException
    {
        super(new BorderLayout());
        this.contextualized = contextualized;
        this.editor = editor;
        this.editor.getUndo().addListener(this, Undo.UNDO_EVENT);

        this.visualizationPanel = new MapVisualizationPanel(new Bounds2d(500, 500), this.updater, contextualized.getContext());
        this.visualizationPanel.setShowGrid(OtsEditor.PROPERTIES_STORE.getOptionalBoolean("map.grid").orElse(true));
        this.updater.addListener(this.visualizationPanel, AnimatorInterface.UPDATE_ANIMATION_EVENT);
        this.visualizationPanel.setBackground(Color.GRAY);
        this.visualizationPanel.setShowToolTip(false);
        editor.addListener(this, OtsEditor.NEW_FILE);
        add(this.visualizationPanel, BorderLayout.CENTER);
        setupMapListeners();

        this.toolPanel = new JPanel();
        setupTools();

        this.togglePanel = new JPanel();
        setAnimationToggles();
        this.togglePanel.setLayout(new BoxLayout(this.togglePanel, BoxLayout.Y_AXIS));
        add(this.togglePanel, BorderLayout.WEST);
    }

    /**
     * Setup listeners for visual editing in the map.
     */
    private void setupMapListeners()
    {
        for (MouseListener ml : this.visualizationPanel.getMouseListeners())
        {
            if (ml instanceof InputListener il)
            {
                this.visualizationPanel.removeMouseListener(il);
                this.visualizationPanel.removeMouseMotionListener(il);
                this.visualizationPanel.removeMouseWheelListener(il);
                this.visualizationPanel.removeKeyListener(il);
            }
        }
        this.inputListener = new MapInputListener(this.editor, this);
        this.visualizationPanel.addMouseListener(this.inputListener);
        this.visualizationPanel.addMouseMotionListener(this.inputListener);
        this.visualizationPanel.addMouseWheelListener(this.inputListener);
        this.visualizationPanel.addKeyListener(this.inputListener);
    }

    /**
     * Sets up all the tool in the tool panel on top.
     */
    private void setupTools()
    {
        this.toolPanel.setMinimumSize(new Dimension(350, 28));
        this.toolPanel.setPreferredSize(new Dimension(350, 28));
        this.toolPanel.setLayout(new BoxLayout(this.toolPanel, BoxLayout.X_AXIS));
        this.toolPanel.add(Box.createHorizontalStrut(5));

        Dimension buttonSize = new Dimension(24, 24);
        int iconSize = 18;
        Dimension applyButtonSize = new Dimension(19, 22);
        int applyIconSize = 16;
        Dimension shapeSizeSmall = new Dimension(80, 22);
        Dimension shapeSizeLarge = new Dimension(100, 22);
        Dimension definitionSizeSmall = new Dimension(50, 22);
        Dimension definitionSizeLarge = new Dimension(150, 22);
        Border paintBorder = new CompoundBorder(new MatteBorder(1, 0, 1, 1, MetalLookAndFeel.getControlDarkShadow()),
                new EmptyBorder(0, 1, 0, 0));

        // add tools
        String modifier =
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() == InputEvent.META_DOWN_MASK ? "\u2318" : "ctrl";
        this.addModeButton = createToolButton(buttonSize, "AddMode24.png", "Add mode (" + modifier + ")", true, iconSize);
        this.centroidModeButton =
                createToolButton(buttonSize, "Centroid24.png", "Add centroid rather than node (shift)", true, iconSize);
        this.autoFixButton =
                createToolButton(buttonSize, "AutoFix24.png", "Automatically align created elements", true, iconSize);
        storedToggleSetting(this.addModeButton, "addMode");
        storedToggleSetting(this.centroidModeButton, "centroidMode");
        storedToggleSetting(this.autoFixButton, "autoFix");

        // shape
        this.toolPanel.add(Box.createHorizontalStrut(5));
        this.defaultShape = new AppearanceControlComboBox<>();
        this.defaultShape.setModel(new DefaultComboBoxModel<>(LINK_SHAPES.toArray(new String[LINK_SHAPES.size()])));
        setSize(this.defaultShape, shapeSizeSmall, shapeSizeLarge);
        this.defaultShape.setToolTipText("Shape for new links");
        Map<String,
                Icon> shapeIcons = Map.of("straight", IconUtil.of("Straight24.png").imageSize(iconSize, iconSize).get(),
                        "bezier", IconUtil.of("Bezier24.png").imageSize(iconSize, iconSize).get(), "clothoid",
                        IconUtil.of("Clothoid24.png").imageSize(iconSize, iconSize).get(), "arc",
                        IconUtil.of("Arc24.png").imageSize(iconSize, iconSize).get(), "polyline",
                        IconUtil.of("PolyLine24.png").imageSize(iconSize, iconSize).get());
        DefaultListCellRenderer shapeRenderer = new DefaultListCellRenderer();
        this.defaultShape.setRenderer((list, value, index, isSelected, cellHasFocus) ->
        {
            JLabel label = (JLabel) shapeRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String shapeId = label.getText().toLowerCase();
            label.setIcon(shapeIcons.get(shapeId));
            return label;
        });
        String key = PropertiesStore.key("map.shape");
        String value = OtsEditor.PROPERTIES_STORE.getOptionalProperty(key).orElse("Straight");
        this.defaultShape.setSelectedItem(value);
        this.defaultShape.addActionListener(
                (a) -> OtsEditor.PROPERTIES_STORE.setProperty(key, (String) this.defaultShape.getSelectedItem()));
        OtsEditor.PROPERTIES_STORE.setProperty(key, value);
        this.toolPanel.add(this.defaultShape);

        // layout
        this.toolPanel.add(Box.createHorizontalStrut(5));
        this.defaultRoadLayout = new AppearanceControlComboBox<>();
        this.defaultRoadLayout
                .setRenderer(definitionRenderer("RoadLayout24.png", iconSize, "<no layout>", this.defaultRoadLayout));
        this.defaultRoadLayout.setModel(new DefaultComboBoxModel<>(new String[] {}));
        setSize(this.defaultRoadLayout, definitionSizeSmall, definitionSizeLarge);
        this.defaultRoadLayout.setToolTipText("Road layout for new links");
        ComboBoxSearchSupport.install(this.defaultRoadLayout, "defaultRoadLayout");
        this.toolPanel.add(this.defaultRoadLayout);
        this.roadLayoutApplyButton =
                createToolButton(applyButtonSize, "Apply24.png", "Apply road layout to selection", false, applyIconSize);
        this.roadLayoutApplyButton.setBorder(paintBorder);
        this.roadLayoutApplyButton.addActionListener((a) ->
        {
            String selected = (String) this.defaultRoadLayout.getSelectedItem();
            if (selected != null)
            {
                this.inputListener.applyRoadLayoutToSelection(selected);
            }
        });

        // link type
        this.toolPanel.add(Box.createHorizontalStrut(5));
        this.defaultLinkType = new AppearanceControlComboBox<>();
        this.defaultLinkType.setRenderer(definitionRenderer("Link24.png", iconSize, "<no link type>", this.defaultLinkType));
        this.defaultLinkType.setModel(new DefaultComboBoxModel<>(new String[] {}));
        setSize(this.defaultLinkType, definitionSizeSmall, definitionSizeLarge);
        this.defaultLinkType.setToolTipText("Link type for new links and connectors");
        ComboBoxSearchSupport.install(this.defaultLinkType, "defaultLinkType");
        this.defaultLinkType.addActionListener((a) -> updateLinkTypeColors());
        this.toolPanel.add(this.defaultLinkType);
        this.linkTypeApplyButton =
                createToolButton(applyButtonSize, "Apply24.png", "Apply link type to selection", false, applyIconSize);
        this.linkTypeApplyButton.setBorder(paintBorder);
        this.linkTypeApplyButton.addActionListener((a) ->
        {
            String selected = (String) this.defaultLinkType.getSelectedItem();
            if (selected != null)
            {
                this.inputListener.applyLinkTypeToSelection(selected);
            }
        });
        this.linkTypeShowButton =
                createToolButton(applyButtonSize, "Eye24.png", "Show links with link type", true, applyIconSize);
        this.linkTypeShowButton.setBorder(paintBorder);
        storedToggleSetting(this.linkTypeShowButton, "showLinkType");
        this.linkTypeShowButton.addActionListener((a) -> updateLinkTypeColors());

        // filler
        this.toolPanel.add(Box.createHorizontalStrut(5));
        Dimension minDim = new Dimension(0, 1);
        Dimension prefDim = new Dimension(0, 1);
        Dimension maxDim = new Dimension(5000, 1);
        this.toolPanel.add(new Filler(minDim, prefDim, maxDim)); // pushes further elements right aligned

        // view buttons
        this.toolPanel.add(Box.createHorizontalStrut(5));
        JButton resetY = new JButton(IconUtil.of("UpDown24.png").imageSize(iconSize, iconSize).get());
        setSize(resetY, buttonSize, buttonSize);
        resetY.setToolTipText("Reset Y-zoom");
        resetY.addActionListener((e) -> this.visualizationPanel.resetZoomY());
        this.toolPanel.add(resetY);

        JButton extent = new JButton(IconUtil.of("ZoomAll24.png").imageSize(iconSize, iconSize).get());
        setSize(extent, buttonSize, buttonSize);
        extent.setToolTipText("Zoom whole network");
        extent.addActionListener((e) -> safeZoomAll());
        this.toolPanel.add(extent);

        JButton grid = new JButton(IconUtil.of("Grid24.png").imageSize(iconSize, iconSize).get());
        setSize(grid, buttonSize, buttonSize);
        grid.setToolTipText("Toggle grid on/off");
        grid.addActionListener((e) ->
        {
            this.visualizationPanel.setShowGrid(!this.visualizationPanel.isShowGrid());
            OtsEditor.PROPERTIES_STORE.setBoolean("map.grid", this.visualizationPanel.isShowGrid());
            this.updater.update();
        });
        this.toolPanel.add(grid);

        this.toolPanel.add(Box.createHorizontalStrut(5));

        add(this.toolPanel, BorderLayout.NORTH);
    }

    /**
     * Sets stored selected state on toggle and makes sure any change in the state is stored.
     * @param button button
     * @param key key (without "map.toggle.", this method will add this part)
     */
    private void storedToggleSetting(final AbstractButton button, final String key)
    {
        String keyAddress = PropertiesStore.key("map.toggle." + key);
        boolean toggleOn = OtsEditor.PROPERTIES_STORE.getOptionalBoolean(keyAddress).orElse(false);
        button.setSelected(toggleOn);
        button.addActionListener((a) -> OtsEditor.PROPERTIES_STORE.setBoolean(keyAddress, button.isSelected()));
        OtsEditor.PROPERTIES_STORE.setBoolean(keyAddress, toggleOn);
    }

    /**
     * Updates the color of all links based on the link type shown in the map, if the link type show button is selected.
     */
    private void updateLinkTypeColors()
    {
        for (MapLinkData linkData : this.links.keySet())
        {
            updateLinkTypeColor(linkData);
        }
        this.updater.update();
    }

    /**
     * Updates the color of a link based on the link type shown in the map, if the link type show button is selected.
     * @param linkData link data
     */
    private void updateLinkTypeColor(final MapLinkData linkData)
    {
        Renderable2d<?> animation = this.animations.get(linkData.getNode());
        if (animation instanceof LinkAnimation linkAnimation)
        {
            String selected = (String) this.defaultLinkType.getSelectedItem();
            if (this.linkTypeShowButton.isSelected() && selected != null
                    && selected.equals(linkData.getNode().getAttributeValueOrDefault("Type")))
            {
                linkAnimation.setColor(LINK_TYPE_COLOR);
            }
            else
            {
                linkAnimation.resetColor();
            }
        }
    }

    /**
     * Sets small as minimum and large as preferred and maximum size.
     * @param component component
     * @param small small size
     * @param large large size
     */
    private void setSize(final JComponent component, final Dimension small, final Dimension large)
    {
        component.setMinimumSize(small);
        component.setPreferredSize(large);
        component.setMaximumSize(large);
    }

    /**
     * Creates a toggle button for the tool bar.
     * @param buttonSize button size
     * @param icon icon name
     * @param toolTip tool tip
     * @param toggle toggle or trigger
     * @param iconSize size of icon
     * @return a toggle button for the tool bar
     */
    private AbstractButton createToolButton(final Dimension buttonSize, final String icon, final String toolTip,
            final boolean toggle, final int iconSize)
    {
        Icon i = IconUtil.of(icon).imageSize(iconSize, iconSize).get();
        AbstractButton button = toggle ? new JToggleButton(i) : new JButton(i);
        button.setFocusPainted(false);
        button.setPreferredSize(buttonSize);
        button.setMinimumSize(buttonSize);
        button.setMaximumSize(buttonSize);
        button.setToolTipText(toolTip);
        this.toolPanel.add(button);
        return button;
    }

    /**
     * Returns renderer for combo box with icon in main bar only.
     * @param icon icon name
     * @param iconSize icon size
     * @param emptyText text in the main bar if there are no options
     * @param comboBox combo box this will be the renderer for
     * @return renderer for combo box with icon in main bar only
     */
    private ListCellRenderer<? super String> definitionRenderer(final String icon, final int iconSize, final String emptyText,
            final JComboBox<String> comboBox)
    {
        Icon linkTypeIcon = IconUtil.of(icon).gray().imageSize(iconSize, iconSize).get();
        DefaultListCellRenderer linkTypeRenderer = new DefaultListCellRenderer();
        return (list, value, index, isSelected, cellHasFocus) ->
        {
            JLabel label = (JLabel) linkTypeRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setIcon(index == -1 ? linkTypeIcon : null);
            if (value == null)
            {
                label.setText(emptyText);
                comboBox.setForeground(Color.LIGHT_GRAY);
            }
            else
            {
                comboBox.setForeground(Color.BLACK);
            }
            return label;
        };
    }

    /**
     * Zoom all, or home extent if there are no objects.
     */
    private void safeZoomAll()
    {
        if (!this.visualizationPanel.getElements().isEmpty())
        {
            try
            {
                this.visualizationPanel.zoomAll();
                return;
            }
            catch (ArithmeticException | IllegalArgumentException exception)
            {
                // elements may not have a valid location (yet), use the home extent instead
            }
        }
        try
        {
            this.visualizationPanel.home();
        }
        catch (Exception ex)
        {
            SwingUtilities.invokeLater(() -> this.visualizationPanel.home());
        }
    }

    /**
     * Sets the animation toggles as useful for in the editor.
     */
    private void setAnimationToggles()
    {
        addToggle("Node", NodeData.class, "Node24.png", "Show/hide nodes", true, false);
        addToggle("NodeId", NodeAnimation.Text.class, "Id24.png", "Show/hide node ids", false, true);
        addToggle("Link", LinkData.class, "Link24.png", "Show/hide links", true, false);
        addToggle("LinkId", LinkAnimation.Text.class, "Id24.png", "Show/hide link ids", false, true);
        addToggle("Priority", PriorityData.class, "Priority24.png", "Show/hide link priority", true, false);
        addToggle("Lane", LaneData.class, "Lane24.png", "Show/hide lanes", true, false);
        addToggle("LaneId", LaneAnimation.Text.class, "Id24.png", "Show/hide lane ids", false, true);
        addToggle("Stripe", StripeData.class, "Stripe24.png", "Show/hide stripes", true, false);
        addToggle("LaneCenter", CenterLine.class, "CenterLine24.png", "Show/hide lane center lines", false, true);
        addToggle("Shoulder", ShoulderData.class, "Shoulder24.png", "Show/hide shoulders", true, false);
        addToggle("Generator", GtuGeneratorPositionData.class, "Generator24.png", "Show/hide generators", true, false);
        addToggle("Sink", SinkData.class, "Sink24.png", "Show/hide sinks", true, true);
        addToggle("Detector", LoopDetectorData.class, "Detector24.png", "Show/hide loop detectors", true, false);
        addToggle("DetectorId", LoopDetectorData.Text.class, "Id24.png", "Show/hide loop detector ids", false, true);
        addToggle("Light", TrafficLightData.class, "TrafficLight24.png", "Show/hide traffic lights", true, false);
        addToggle("LightId", TrafficLightAnimation.Text.class, "Id24.png", "Show/hide traffic light ids", false, true);
        addToggle("Bus", BusStopData.class, "BusStop24.png", "Show/hide bus stops", true, false);
        addToggle("BusId", BusStopAnimation.Text.class, "Id24.png", "Show/hide bus stop ids", false, true);
    }

    /**
     * Add a button for toggling an animation class on or off. Button icons for which 'idButton' is true will be placed to the
     * right of the previous button, which should be the corresponding button without the id. An example is an icon for
     * showing/hiding the class 'Lane' followed by the button to show/hide the Lane ids.
     * @param name the name of the button
     * @param locatableClass the class for which the button holds (e.g., GTU.class)
     * @param iconPath the path to the 24x24 icon to display
     * @param toolTipText the tool tip text to show when hovering over the button
     * @param initiallyVisible whether the class is initially shown or not
     * @param idButton id button that needs to be placed next to the previous button
     */
    public void addToggle(final String name, final Class<? extends Locatable> locatableClass, final String iconPath,
            final String toolTipText, final boolean initiallyVisible, final boolean idButton)
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
        String key = "map.toggle." + PropertiesStore.key(name);
        boolean toggleOn = OtsEditor.PROPERTIES_STORE.getOptionalBoolean(key).orElse(initiallyVisible);
        button.setSelected(toggleOn);
        OtsEditor.PROPERTIES_STORE.setBoolean(key, toggleOn);
        button.setActionCommand(name);
        button.setToolTipText(toolTipText);
        button.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                String actionCommand = e.getActionCommand();
                if (EditorMap.this.toggleLocatableMap.containsKey(actionCommand))
                {
                    Class<? extends Locatable> locatableClass = EditorMap.this.toggleLocatableMap.get(actionCommand);
                    EditorMap.this.visualizationPanel.toggleClass(locatableClass);
                    OtsEditor.PROPERTIES_STORE.setBoolean(key, EditorMap.this.visualizationPanel.isShowClass(locatableClass));
                    EditorMap.this.togglePanel.repaint();
                }
            }
        });

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

        if (toggleOn)
        {
            this.visualizationPanel.showClass(locatableClass);
        }
        else
        {
            this.visualizationPanel.hideClass(locatableClass);
        }
        this.toggleLocatableMap.put(name, locatableClass);
    }

    /**
     * Builds a map panel with an animator and context.
     * @param editor editor
     * @return map
     * @throws RemoteException context binding problem
     * @throws NamingException context binding problem
     */
    public static EditorMap build(final OtsEditor editor) throws RemoteException, NamingException
    {
        ContextInterface context = new JvmContext("ots-context");
        Contextualized contextualized = new Contextualized()
        {
            @Override
            public ContextInterface getContext()
            {
                return context;
            }
        };
        return new EditorMap(contextualized, editor);
    }

    @Override
    public void notify(final Event event)
    {
        if (event.getType().equals(OtsEditor.NEW_FILE))
        {
            newFileEvent(event);
        }
        else if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED))
        {
            nodeCreatedEvent(event);
        }
        else if (event.getType().equals(XsdTreeNodeRoot.NODE_REMOVED))
        {
            nodeRemovedEvent(event);
        }
        else if (event.getType().equals(XsdTreeNode.ACTIVATION_CHANGED))
        {
            activationChangedEvent(event);
        }
        else if (event.getType().equals(XsdTreeNode.OPTION_CHANGED))
        {
            optionChangedEvent(event);
        }
        else if (event.getType().equals(XsdTreeNode.ATTRIBUTE_CHANGED))
        {
            attributeChangedEvent(event);
        }
        else if (event.getType().equals(Undo.UNDO_EVENT))
        {
            @SuppressWarnings("unchecked")
            Collection<XsdTreeNode> nodes = (Collection<XsdTreeNode>) event.getContent();
            // first let any invokeLater as a response to the undo changes occur (e.g. XsdTreeTableModel.restoreSelection())
            SwingUtilities.invokeLater(() -> this.inputListener.setSelection(nodes));
        }
    }

    /**
     * New file event.
     * @param event event
     */
    private void newFileEvent(final Event event)
    {
        this.definedRoadLayouts.keySet().forEach((node) ->
        {
            node.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            node.removeListener(this, XsdTreeNode.ACTIVATION_CHANGED);
        });
        this.definedLinkTypes.keySet().forEach((node) ->
        {
            node.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            node.removeListener(this, XsdTreeNode.ACTIVATION_CHANGED);
        });
        this.definedRoadLayouts.clear();
        this.definedLinkTypes.clear();
        updateDefinedItems(this.defaultRoadLayout, this.definedRoadLayouts);
        updateDefinedItems(this.defaultLinkType, this.definedLinkTypes);
        for (XsdTreeNode node : new LinkedHashSet<>(this.datas.keySet()))
        {
            remove(node);
        }
        this.datas.clear();
        this.links.clear();
        for (Renderable2d<?> animation : this.animations.values())
        {
            animation.destroy(this.contextualized);
            removeAnimation(animation);
        }
        this.animations.clear();
        for (RoadLayoutListener roadLayoutListener : this.roadLayoutListeners.values())
        {
            roadLayoutListener.destroy();
        }
        this.roadLayoutListeners.clear();
        if (this.networkFlattenerListener != null)
        {
            this.networkFlattenerListener.destroy();
            this.networkFlattenerListener = null;
        }
        this.root = (XsdTreeNodeRoot) event.getContent();
        this.root.addListener(this, XsdTreeNodeRoot.NODE_CREATED);
        this.root.addListener(this, XsdTreeNodeRoot.NODE_REMOVED);
        SwingUtilities.invokeLater(() -> safeZoomAll());
    }

    /**
     * Node created event.
     * @param event event
     */
    private void nodeCreatedEvent(final Event event)
    {
        Object[] content = (Object[]) event.getContent();
        XsdTreeNode node = (XsdTreeNode) content[0];
        if (isDefinedItem(node))
        {
            node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
            node.addListener(this, XsdTreeNode.ACTIVATION_CHANGED, ReferenceType.WEAK);
            updateDefinedItem(node);
        }
        if (isType(node))
        {
            node.addListener(this, XsdTreeNode.ACTIVATION_CHANGED);
            node.addListener(this, XsdTreeNode.OPTION_CHANGED);
            if (node.isActive())
            {
                add(node);
            }
            if (node.getPathString().equals(XsdPaths.NODE))
            {
                node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
                // later as this can be part of a delete-undo that first needs to complete to leave valid nodes
                SwingUtilities.invokeLater(() -> this.links.keySet().forEach((linkData) -> linkData.nodeCreatedOrIdChanged()));
            }
        }
        else if (node.getPathString().equals(XsdPaths.POLYLINE_COORDINATE))
        {
            for (MapLinkData linkData : this.links.keySet())
            {
                linkData.addCoordinate(node);
            }
        }
        else if (node.getPathString().equals(XsdPaths.DEFINED_ROADLAYOUT))
        {
            addRoadLayout(node);
        }
        else if (node.getPathString().equals(XsdPaths.NETWORK + ".Flattener"))
        {
            setNetworkFlattener(node);
        }
        else if (node.getPathString().endsWith("LaneOverride") || node.getPathString().endsWith("StripeOverride"))
        {
            ChangeListener<Object> listener = new ChangeListener<>(node, () -> this.editor.getEval())
            {
                @Override
                public void notify(final Event event)
                {
                    if (event.getType().equals(ChangeListener.CHANGE_EVENT))
                    {
                        MapData data = EditorMap.this.datas.get(getNode().getParent().getParent());
                        if (data instanceof MapLinkData linkData)
                        {
                            linkData.update();
                        }
                    }
                    else
                    {
                        super.notify(event);
                    }
                }

                @Override
                Object calculateData()
                {
                    return null; // This change listener represents no data
                }
            };
            this.overrideListeners.put(node, listener);
            listener.addListener(listener, ChangeListener.CHANGE_EVENT); // register to self, but for change events
        }
        // When a node is restored, child nodes may also be restored later.
        SwingUtilities.invokeLater(() ->
        {
            this.updater.update();
        });
    }

    /**
     * Node removed event.
     * @param event event
     */
    private void nodeRemovedEvent(final Event event)
    {
        Object[] content = (Object[]) event.getContent();
        XsdTreeNode node = (XsdTreeNode) content[0];
        if (isDefinedItem(node) || this.definedRoadLayouts.containsKey(node) || this.definedLinkTypes.containsKey(node))
        {
            node.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            node.removeListener(this, XsdTreeNode.ACTIVATION_CHANGED);
            removeDefinedItem(node);
        }
        if (this.datas.containsKey(node)) // node.isType does not work as parent is gone, i.e. type is just "Node"
        {
            remove(node); // updates animation panel
            this.links.keySet().forEach((linkData) -> linkData.notifyNodeRemoved(node));
        }
        else if (node.getPathString().equals(XsdPaths.POLYLINE_COORDINATE))
        {
            for (MapLinkData linkData : this.links.keySet())
            {
                linkData.removeCoordinate(node);
            }
        }
        else if (node.getPathString().equals(XsdPaths.DEFINED_ROADLAYOUT))
        {
            removeRoadLayout(node);
        }
        else if (node.getPathString().equals(XsdPaths.NETWORK + ".Flattener"))
        {
            removeNetworkFlattener();
        }
        else if (node.getPathString().endsWith("LaneOverride") || node.getPathString().endsWith("StripeOverride"))
        {
            ChangeListener<Object> listener = this.overrideListeners.remove(node);
            if (listener != null)
            {
                listener.removeListener(listener, ChangeListener.CHANGE_EVENT);
                listener.destroy();
            }
        }
        else if (node.getPathString().equals(XsdPaths.NODE))
        {
            // usually "if (this.datas.containsKey(node))" will catch this, but not always
            this.links.keySet().forEach((linkData) -> linkData.notifyNodeRemoved(node));
        }
        this.updater.update();
    }

    /**
     * Activation changed event.
     * @param event event
     */
    private void activationChangedEvent(final Event event)
    {
        Object[] content = (Object[]) event.getContent();
        XsdTreeNode node = (XsdTreeNode) content[0];
        if (isDefinedItem(node))
        {
            updateDefinedItem(node);
        }
        if (isType(node))
        {
            if ((boolean) content[1])
            {
                add(node);
            }
            else
            {
                remove(node);
            }
        }
        else if (node.getPathString().equals(XsdPaths.DEFINED_ROADLAYOUT))
        {
            if ((boolean) content[1])
            {
                addRoadLayout(node);
            }
            else
            {
                removeRoadLayout(node);
            }
        }
        else if (node.getPathString().equals(XsdPaths.NETWORK + ".Flattener"))
        {
            if ((boolean) content[1])
            {
                setNetworkFlattener(node);
            }
            else
            {
                removeNetworkFlattener();
            }
        }
        this.updater.update();
    }

    /**
     * Option changed event.
     * @param event event
     */
    private void optionChangedEvent(final Event event)
    {
        Object[] content = (Object[]) event.getContent();
        XsdTreeNode node = (XsdTreeNode) content[0];
        XsdTreeNode selected = (XsdTreeNode) content[1];
        XsdTreeNode previous = (XsdTreeNode) content[2];
        if (node.equals(selected))
        {
            if (isType(previous))
            {
                remove(previous);
            }
            if (isType(selected) && selected.isActive())
            {
                add(selected);
            }
        }
        this.updater.update();
    }

    /**
     * Attribute changed event.
     * @param event event
     */
    private void attributeChangedEvent(final Event event)
    {
        Object[] content = (Object[]) event.getContent();
        if ("Id".equals(content[1]))
        {
            XsdTreeNode node = (XsdTreeNode) content[0];
            if (isDefinedItem(node))
            {
                updateDefinedItem(node);
            }
            if (node.getPathString().equals(XsdPaths.NODE))
            {
                this.links.keySet().forEach((linkData) -> linkData.nodeCreatedOrIdChanged());
            }
        }
        else if ("Type".equals(content[1]))
        {
            XsdTreeNode node = (XsdTreeNode) content[0];
            for (MapLinkData linkData : this.links.keySet())
            {
                if (linkData.getNode().equals(node))
                {
                    updateLinkTypeColor(linkData);
                    break;
                }
            }
        }
        this.updater.update();
    }

    /**
     * Whether a node defines a road layout or link type.
     * @param node node
     * @return whether the node defines a road layout or link type
     */
    private boolean isDefinedItem(final XsdTreeNode node)
    {
        return node.getPathString().equals(XsdPaths.DEFINED_ROADLAYOUT)
                || node.getPathString().equals(XsdPaths.DEFINED_LINKTYPE);
    }

    /**
     * Add or update a defined item in its selector.
     * @param node definition node
     */
    private void updateDefinedItem(final XsdTreeNode node)
    {
        boolean roadLayout = node.getPathString().equals(XsdPaths.DEFINED_ROADLAYOUT);
        Map<XsdTreeNode, String> definitions = roadLayout ? this.definedRoadLayouts : this.definedLinkTypes;
        JComboBox<String> comboBox = roadLayout ? this.defaultRoadLayout : this.defaultLinkType;
        String previousId = definitions.get(node);
        boolean selectedNode = previousId != null && previousId.equals(comboBox.getSelectedItem());
        String id = node.isActive() ? node.getId() : null;
        if (id == null || id.isEmpty())
        {
            definitions.remove(node);
        }
        else
        {
            definitions.put(node, id);
        }
        updateDefinedItems(comboBox, definitions, selectedNode ? id : null);
    }

    /**
     * Remove a definition from its selector.
     * @param node definition node
     */
    private void removeDefinedItem(final XsdTreeNode node)
    {
        if (this.definedRoadLayouts.remove(node) != null)
        {
            updateDefinedItems(this.defaultRoadLayout, this.definedRoadLayouts);
        }
        if (this.definedLinkTypes.remove(node) != null)
        {
            updateDefinedItems(this.defaultLinkType, this.definedLinkTypes);
        }
    }

    /**
     * Rebuild a selector from its active definitions while retaining its current selection when possible.
     * @param comboBox selector
     * @param definitions active definitions
     */
    private void updateDefinedItems(final JComboBox<String> comboBox, final Map<XsdTreeNode, String> definitions)
    {
        updateDefinedItems(comboBox, definitions, null);
    }

    /**
     * Rebuild a selector, optionally preferring a selection identified by its definition node.
     * @param comboBox selector
     * @param definitions active definitions
     * @param preferredSelection item to select if present
     */
    private void updateDefinedItems(final JComboBox<String> comboBox, final Map<XsdTreeNode, String> definitions,
            final String preferredSelection)
    {
        Object selected = comboBox.getSelectedItem();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        definitions.values().forEach(model::addElement);
        if (preferredSelection != null && definitions.containsValue(preferredSelection))
        {
            comboBox.setSelectedItem(preferredSelection);
        }
        else if (selected instanceof String selectedId && definitions.containsValue(selectedId))
        {
            comboBox.setSelectedItem(selectedId);
        }
        comboBox.setFocusable(!definitions.isEmpty());
        comboBox.setModel(model);
    }

    /**
     * Returns whether the node is any of the visualized types.
     * @param node node
     * @return whether the node is any of the visualized types
     */
    private boolean isType(final XsdTreeNode node)
    {
        for (String type : TYPES)
        {
            if (node.isType(type))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Set the data as being valid to draw.
     * @param data data that is valid to draw
     */
    public void setValid(final MapData data)
    {
        XsdTreeNode node = data.getNode();
        if (this.animations.containsKey(node))
        {
            return;
        }
        this.animations.put(node, null); // skips adding animations through setValid downstream of animation creation
        Renderable2d<?> animation;
        if (node.getPathString().equals(XsdPaths.NODE) || node.getPathString().equals(XsdPaths.CENTROID))
        {
            animation = new NodeAnimation((MapNodeData) data, this.contextualized).setDynamic(true);
        }
        else if (node.getPathString().equals(XsdPaths.LINK) || node.getPathString().equals(XsdPaths.CONNECTOR))
        {
            animation = new LinkAnimation((MapLinkData) data, this.contextualized, 0.5f).setDynamic(true);
        }
        else if (node.getPathString().equals(XsdPaths.TRAFFIC_LIGHT))
        {
            animation = new TrafficLightAnimation((MapTrafficLightData) data, this.contextualized).setDynamic(true);
        }
        else if (node.getPathString().equals(XsdPaths.SINK))
        {
            animation = new LaneDetectorAnimation<SinkData>((SinkData) data, this.contextualized, Color.YELLOW, "Sink");
        }
        else if (node.getPathString().equals(XsdPaths.GENERATOR) || node.getPathString().equals(XsdPaths.LIST_GENERATOR))
        {
            animation = new GtuGeneratorPositionAnimation((GtuGeneratorPositionData) data, this.contextualized);
        }
        else
        {
            throw new UnsupportedOperationException("Data cannot be added by the map editor.");
        }
        this.animations.put(node, animation);
        if (data instanceof MapLinkData linkData)
        {
            updateLinkTypeColor(linkData);
        }
    }

    /**
     * Set the data as being invalid to draw.
     * @param data data that is invalid to draw
     */
    public void setInvalid(final MapData data)
    {
        //
    }

    /**
     * Adds a data representation of the node. This will not yet be drawn until the data object itself tells the map it is valid
     * to be drawn.
     * @param node node of element to draw
     */
    private void add(final XsdTreeNode node)
    {
        MapData data;
        if (this.datas.containsKey(node))
        {
            return; // activated choice
        }
        if (node.getPathString().equals(XsdPaths.NODE) || node.getPathString().equals(XsdPaths.CENTROID))
        {
            data = new MapNodeData(this, node, this.editor);
            node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
            // SwingUtilities.invokeLater(() ->
            // {
            for (MapLinkData linkData : this.links.keySet())
            {
                linkData.update();
            }
            // });
        }
        else if (node.getPathString().equals(XsdPaths.LINK) || node.getPathString().equals(XsdPaths.CONNECTOR))
        {
            MapLinkData linkData = new MapLinkData(this, node, this.editor);
            data = linkData;
            node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
            if (this.networkFlattenerListener != null && !linkData.isConnector())
            {
                this.networkFlattenerListener.addListener(linkData, ChangeListener.CHANGE_EVENT, ReferenceType.WEAK);
            }
            this.links.put(linkData, null);
            // SwingUtilities.invokeLater(() ->
            // {
            for (MapData d : this.datas.values())
            {
                if (d instanceof MapLaneBasedObjectData)
                {
                    d.update();
                }
            }
            // });
        }
        else if (node.getPathString().equals(XsdPaths.TRAFFIC_LIGHT))
        {
            MapTrafficLightData trafficLightData = new MapTrafficLightData(this, node, this.editor);
            data = trafficLightData;
        }
        else if (node.getPathString().equals(XsdPaths.SINK))
        {
            MapSinkData sinkData = new MapSinkData(this, node, this.editor);
            data = sinkData;
        }
        else if (node.getPathString().equals(XsdPaths.GENERATOR) || node.getPathString().equals(XsdPaths.LIST_GENERATOR))
        {
            MapGeneratorData generatorData = new MapGeneratorData(this, node, this.editor);
            data = generatorData;
        }
        else
        {
            throw new UnsupportedOperationException("Node cannot be added by the map editor.");
        }
        data.update();
        data.addListener((e) -> this.updater.update(), MapData.MAP_DATA_CHANGED);
        this.datas.put(node, data);
    }

    /**
     * Remove the drawing data pertaining to the node.
     * @param node node
     */
    private void remove(final XsdTreeNode node)
    {
        if (node.getPathString().equals(XsdPaths.NODE))
        {
            node.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        }
        if (node.getPathString().equals(XsdPaths.LINK) || node.getPathString().equals(XsdPaths.CONNECTOR))
        {
            node.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            Iterator<MapLinkData> it = this.links.keySet().iterator();
            while (it.hasNext())
            {
                MapLinkData link = it.next();
                if (link.getNode().equals(node))
                {
                    if (!link.isConnector())
                    {
                        for (RoadLayoutListener roadLayoutListener : this.roadLayoutListeners.values())
                        {
                            roadLayoutListener.removeListener(link, ChangeListener.CHANGE_EVENT);
                        }
                        if (this.networkFlattenerListener != null)
                        {
                            this.networkFlattenerListener.removeListener(link, ChangeListener.CHANGE_EVENT);
                        }
                    }
                    it.remove();
                    break;
                }
            }
        }
        MapData data = this.datas.remove(node);
        if (data != null)
        {
            data.destroy();
        }
        removeAnimation(this.animations.remove(node));
    }

    /**
     * Reinitialize animation on object who's animator stores static information that depends on something that was changed.
     * This will create a new animation object. Only data objects that know their animations have static data, should call this.
     * And only when information changed on which the static data depends.
     * @param node node
     */
    public void reinitialize(final XsdTreeNode node)
    {
        remove(node);
        add(node);
    }

    /**
     * Returns the map data of the given XSD node.
     * @param node node
     * @return map data of the given XSD node, empty if no such data
     */
    public Optional<MapData> getData(final XsdTreeNode node)
    {
        return Optional.ofNullable(this.datas.get(node));
    }

    /**
     * Perform action on each map data object.
     * @param mapDataConsumer map data consumer
     */
    public void forEachMapData(final Consumer<MapData> mapDataConsumer)
    {
        this.datas.values().forEach(mapDataConsumer);
    }

    /**
     * Add defined road layout.
     * @param node node of the defined road layout
     */
    private void addRoadLayout(final XsdTreeNode node)
    {
        RoadLayoutListener roadLayoutListener = new RoadLayoutListener(node, () -> this.editor.getEval());
        for (MapLinkData linkData : this.links.keySet())
        {
            if (!linkData.isConnector())
            {
                roadLayoutListener.addListener(linkData, ChangeListener.CHANGE_EVENT, ReferenceType.WEAK);
            }
        }
        this.roadLayoutListeners.put(node, roadLayoutListener);
    }

    /**
     * Remove defined road layout.
     * @param node node of the defined road layout
     */
    private void removeRoadLayout(final XsdTreeNode node)
    {
        RoadLayoutListener roadLayoutListener = this.roadLayoutListeners.remove(node);
        roadLayoutListener.destroy();
    }

    /**
     * Sets the network level flattener.
     * @param node node of network flattener
     */
    private void setNetworkFlattener(final XsdTreeNode node)
    {
        if (this.networkFlattenerListener != null)
        {
            this.editor.removeEvalListener(this.networkFlattenerListener);
            this.networkFlattenerListener.destroy();
        }
        this.networkFlattenerListener = new FlattenerListener(node, () -> this.editor.getEval());
        for (MapLinkData linkData : this.links.keySet())
        {
            if (!linkData.isConnector())
            {
                this.networkFlattenerListener.addListener(linkData, ChangeListener.CHANGE_EVENT, ReferenceType.WEAK);
            }
            this.editor.addEvalListener(this.networkFlattenerListener);
        }
        node.addListener(this, XsdTreeNode.ACTIVATION_CHANGED, ReferenceType.WEAK);
    }

    /**
     * Removes the network flattener.
     */
    private void removeNetworkFlattener()
    {
        if (this.networkFlattenerListener != null)
        {
            this.editor.removeEvalListener(this.networkFlattenerListener);
            this.networkFlattenerListener.destroy();
        }
        for (MapLinkData linkData : this.links.keySet())
        {
            linkData.notify(new Event(ChangeListener.CHANGE_EVENT, this.networkFlattenerListener.getNode()));
        }
        this.networkFlattenerListener = null;
    }

    /**
     * Returns the road layout listener from which a {@code MapLinkData} can obtain offsets.
     * @param node node of a defined layout
     * @return listener, can be used to obtain offsets
     */
    RoadLayoutListener getRoadLayoutListener(final XsdTreeNode node)
    {
        return this.roadLayoutListeners.get(node);
    }

    /**
     * Remove animation.
     * @param animation animation to remove
     */
    public void removeAnimation(final Renderable2d<?> animation)
    {
        if (animation != null)
        {
            this.visualizationPanel.objectRemoved(animation);
            animation.destroy(this.contextualized);
        }
    }

    /**
     * Returns the context.
     * @return context
     */
    public Contextualized getContextualized()
    {
        return this.contextualized;
    }

    /**
     * Returns whether map add mode is selected.
     * @return whether map add mode is selected
     */
    public boolean isAddMode()
    {
        return this.addModeButton.isSelected();
    }

    /**
     * Returns whether centroid mode is selected.
     * @return whether centroid mode is selected
     */
    public boolean isCentroidMode()
    {
        return this.centroidModeButton.isSelected();
    }

    /**
     * Returns whether map auto fix mode is selected.
     * @return whether map auto fix mode is selected
     */
    public boolean isAutoFixMode()
    {
        return this.autoFixButton.isSelected();
    }

    /**
     * Returns the selected default shape.
     * @return selected default shape, or {@code null}
     */
    public String getDefaultShape()
    {
        return (String) this.defaultShape.getSelectedItem();
    }

    /**
     * Returns the selected default road layout.
     * @return selected default road layout, or {@code null}
     */
    public String getDefaultRoadLayout()
    {
        return (String) this.defaultRoadLayout.getSelectedItem();
    }

    /**
     * Returns the selected default link type.
     * @return selected default link type, or {@code null}
     */
    public String getDefaultLinkType()
    {
        return (String) this.defaultLinkType.getSelectedItem();
    }

    /**
     * Returns the Network node in the current document.
     * @return Network node, or {@code null} if there is no document
     */
    public XsdTreeNode getNetworkNode()
    {
        return this.root == null || !this.root.hasChild("Network") ? null : this.root.getFirstChild("Network");
    }

    /**
     * Adds a temporary renderable to the visualization panel.
     * @param animation renderable
     */
    public void addAnimation(final Renderable2d<?> animation)
    {
        this.visualizationPanel.objectAdded(animation);
        this.updater.update();
    }

    /**
     * Update via updater. The updater will collect multiple updates in to a single update.
     */
    public void update()
    {
        this.updater.update();
    }

    /**
     * Returns the visualization panel.
     * @return visualization panel
     */
    public MapVisualizationPanel getPanel()
    {
        return this.visualizationPanel;
    }

    /**
     * Returns the network level flattener, or a 64 segment flattener of none specified.
     * @return flattener
     */
    public CurveFlattener getNetworkFlattener()
    {
        if (this.networkFlattenerListener != null)
        {
            CurveFlattener flattener = this.networkFlattenerListener.getData();
            if (flattener != null)
            {
                return flattener; // otherwise, return default
            }
        }
        return new CurveFlattener(64);
    }

    /**
     * Returns the map of synchronizable stripes, not a safe copy.
     * @return map of synchronizable stripes
     */
    public Map<MapStripeData, SynchronizableMapStripe> getSynchronizableStripes()
    {
        return this.synStripes;
    }

    /**
     * Schedules synchronization of all stripes. Multiple requests within one event cycle are bundled. Synchronization is
     * performed in a later event so that all links that are being rebuilt have settled, e.g. after a node is dragged, such that
     * synchronization uses final stripe geometry and offsets.
     */
    public void scheduleStripeSynchronization()
    {
        if (!this.stripeSynchronizationScheduled)
        {
            this.stripeSynchronizationScheduled = true;
            SwingUtilities.invokeLater(() ->
            {
                this.stripeSynchronizationScheduled = false;
                synchronizeStripes();
                this.updater.update();
            });
        }
    }

    /**
     * Resets all dash offsets to their defined value, and synchronizes all stripes. Resetting is required as a stripe that
     * becomes an anchor (e.g. when an extra upstream link is created at a merge) may still have an offset from an earlier
     * synchronization, while an anchor should keep its defined offset.
     */
    private void synchronizeStripes()
    {
        for (SynchronizableMapStripe stripe : this.synStripes.values())
        {
            stripe.resetDashOffset();
        }
        StripeSynchronization.synchronize(new LinkedHashMap<>(this.synStripes));
    }

    /**
     * Event producer that fires an update animation event.
     */
    public static class MapUpdater extends LocalEventProducer
    {

        /** Whether an update is currently queued. */
        private boolean updateQueued;

        /**
         * Constructor.
         */
        public MapUpdater()
        {
            //
        }

        /**
         * Fire update animation event.
         */
        public void update()
        {
            if (!this.updateQueued)
            {
                this.updateQueued = true;
                SwingUtilities.invokeLater(() ->
                {
                    this.updateQueued = false;
                    fireEvent(AnimatorInterface.UPDATE_ANIMATION_EVENT);
                });
            }
        }
    }

}
