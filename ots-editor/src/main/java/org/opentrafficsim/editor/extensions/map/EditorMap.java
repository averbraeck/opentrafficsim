package org.opentrafficsim.editor.extensions.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;

import javax.naming.NamingException;
import javax.swing.Box;
import javax.swing.Box.Filler;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import org.djutils.draw.bounds.Bounds2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.reference.ReferenceType;
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.geometry.Flattener;
import org.opentrafficsim.core.geometry.Flattener.NumSegments;
import org.opentrafficsim.draw.network.LinkAnimation;
import org.opentrafficsim.draw.network.LinkAnimation.LinkData;
import org.opentrafficsim.draw.network.NodeAnimation;
import org.opentrafficsim.draw.network.NodeAnimation.NodeData;
import org.opentrafficsim.draw.road.BusStopAnimation;
import org.opentrafficsim.draw.road.BusStopAnimation.BusStopData;
import org.opentrafficsim.draw.road.CrossSectionElementAnimation.ShoulderData;
import org.opentrafficsim.draw.road.GtuGeneratorPositionAnimation;
import org.opentrafficsim.draw.road.GtuGeneratorPositionAnimation.GtuGeneratorPositionData;
import org.opentrafficsim.draw.road.LaneAnimation;
import org.opentrafficsim.draw.road.LaneAnimation.CenterLine;
import org.opentrafficsim.draw.road.LaneAnimation.LaneData;
import org.opentrafficsim.draw.road.LaneDetectorAnimation;
import org.opentrafficsim.draw.road.LaneDetectorAnimation.LoopDetectorData;
import org.opentrafficsim.draw.road.LaneDetectorAnimation.SinkData;
import org.opentrafficsim.draw.road.LaneDetectorAnimation.SinkData.SinkText;
import org.opentrafficsim.draw.road.PriorityAnimation.PriorityData;
import org.opentrafficsim.draw.road.StripeAnimation.StripeData;
import org.opentrafficsim.draw.road.TrafficLightAnimation;
import org.opentrafficsim.draw.road.TrafficLightAnimation.TrafficLightData;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdPaths;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;
import org.opentrafficsim.swing.gui.AppearanceControlComboBox;
import org.opentrafficsim.swing.gui.OtsControlPanel;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.d2.Renderable2d;
import nl.tudelft.simulation.dsol.animation.d2.RenderableScale;
import nl.tudelft.simulation.dsol.swing.animation.d2.AnimationUpdaterThread;
import nl.tudelft.simulation.dsol.swing.animation.d2.VisualizationPanel;
import nl.tudelft.simulation.naming.context.ContextInterface;
import nl.tudelft.simulation.naming.context.Contextualized;
import nl.tudelft.simulation.naming.context.JvmContext;

/**
 * Editor map.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class EditorMap extends JPanel implements EventListener
{

    /** */
    private static final long serialVersionUID = 20231010L;

    /** Color for toolbar and toggle bar. */
    private static final Color BAR_COLOR = Color.LIGHT_GRAY;

    /** All types that are valid to show in the map. */
    private static final Set<String> TYPES = Set.of(XsdPaths.NODE, XsdPaths.LINK, XsdPaths.TRAFFIC_LIGHT, XsdPaths.SINK,
            XsdPaths.GENERATOR, XsdPaths.LIST_GENERATOR);

    /** Context provider. */
    private final Contextualized contextualized;

    /** Editor. */
    private final OtsEditor editor;

    /** Panel to draw in. */
    private final VisualizationPanel animationPanel;

    /** Panel with tools. */
    private final JPanel toolPanel;

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

    /** Animation objects of all data's drawn. */
    private final LinkedHashMap<XsdTreeNode, Renderable2d<?>> animations = new LinkedHashMap<>();

    /** Whether we can ignore maintaining the scale. */
    private boolean ignoreKeepScale = false;

    /** Last x-scale. */
    private Double lastXScale = null;

    /** Last y-scale. */
    private Double lastYScale = null;

    /** Last screen size. */
    private Dimension lastScreen = null;

    /** Map of toggle names to toggle animation classes. */
    private Map<String, Class<? extends Locatable>> toggleLocatableMap = new LinkedHashMap<>();

    /**
     * Constructor.
     * @param animator thread for frequent painting.
     * @param contextualized context provider.
     * @param editor editor.
     * @throws RemoteException context binding problem.
     * @throws NamingException context binding problem.
     */
    private EditorMap(final AnimationUpdaterThread animator, final Contextualized contextualized, final OtsEditor editor)
            throws RemoteException, NamingException
    {
        super(new BorderLayout());
        this.contextualized = contextualized;
        this.editor = editor;
        this.animationPanel = new VisualizationPanel(new Bounds2d(500, 500), animator, contextualized.getContext())
        {
            /** */
            private static final long serialVersionUID = 20231016L;

            /** {@inheritDoc} */
            @Override
            public void setExtent(final Bounds2d extent)
            {
                if (EditorMap.this.lastScreen != null)
                {
                    // this prevents zoom being undone when resizing the screen afterwards
                    EditorMap.this.lastXScale = this.getRenderableScale().getXScale(extent, EditorMap.this.lastScreen);
                    EditorMap.this.lastYScale = this.getRenderableScale().getYScale(extent, EditorMap.this.lastScreen);
                }
                super.setExtent(extent);
            }

            /** {@inheritDoc} */
            @Override
            public synchronized void zoomAll()
            {
                EditorMap.this.ignoreKeepScale = true;
                Bounds2d extent = EditorMap.this.animationPanel.fullExtent();
                if (Double.isFinite(extent.getMaxX()))
                {
                    super.zoomAll();
                }
                else if (getSize().height != 0)
                {
                    // there are no objects
                    super.home();
                }
                EditorMap.this.ignoreKeepScale = false;
            }

            /** {@inheritDoc} */
            @Override
            public synchronized void home()
            {
                EditorMap.this.ignoreKeepScale = true;
                super.home();
                EditorMap.this.ignoreKeepScale = false;
            }
        };
        this.animationPanel.setBackground(Color.GRAY);
        this.animationPanel.setShowToolTip(false);
        editor.addListener(this, OtsEditor.NEW_FILE);
        this.animationPanel.setRenderableScale(new RenderableScale()
        {
            /** {@inheritDoc} */
            @Override
            public Bounds2d computeVisibleExtent(final Bounds2d extent, final Dimension screen)
            {
                if (EditorMap.this.ignoreKeepScale)
                {
                    return super.computeVisibleExtent(extent, screen);
                }
                // overridden to preserve zoom scale, otherwise dragging the split screen may pump up the zoom factor
                double xScale = getXScale(extent, screen);
                double yScale = getYScale(extent, screen);
                Bounds2d result;
                if (EditorMap.this.lastYScale != null && yScale == EditorMap.this.lastYScale)
                {
                    result = new Bounds2d(extent.midPoint().getX() - 0.5 * screen.getWidth() * yScale,
                            extent.midPoint().getX() + 0.5 * screen.getWidth() * yScale, extent.getMinY(), extent.getMaxY());
                    xScale = yScale;
                }
                else if (EditorMap.this.lastXScale != null && xScale == EditorMap.this.lastXScale)
                {
                    result = new Bounds2d(extent.getMinX(), extent.getMaxX(),
                            extent.midPoint().getY() - 0.5 * screen.getHeight() * xScale * getYScaleRatio(),
                            extent.midPoint().getY() + 0.5 * screen.getHeight() * xScale * getYScaleRatio());
                    yScale = xScale;
                }
                else
                {
                    double scale = EditorMap.this.lastXScale == null ? Math.min(xScale, yScale)
                            : EditorMap.this.lastXScale * EditorMap.this.lastScreen.getWidth() / screen.getWidth();
                    result = new Bounds2d(extent.midPoint().getX() - 0.5 * screen.getWidth() * scale,
                            extent.midPoint().getX() + 0.5 * screen.getWidth() * scale,
                            extent.midPoint().getY() - 0.5 * screen.getHeight() * scale * getYScaleRatio(),
                            extent.midPoint().getY() + 0.5 * screen.getHeight() * scale * getYScaleRatio());
                    yScale = scale;
                    xScale = scale;
                }
                EditorMap.this.lastXScale = xScale;
                EditorMap.this.lastYScale = yScale;
                EditorMap.this.lastScreen = screen;
                return result;
            }
        });
        add(this.animationPanel, BorderLayout.CENTER);

        this.toolPanel = new JPanel();
        setupTools();

        this.togglePanel = new JPanel();
        this.togglePanel.setBackground(BAR_COLOR);
        setAnimationToggles();
        this.togglePanel.setLayout(new BoxLayout(this.togglePanel, BoxLayout.Y_AXIS));
        add(this.togglePanel, BorderLayout.WEST);
    }

    /**
     * Sets up all the tool in the tool panel on top.
     */
    private void setupTools()
    {
        this.toolPanel.setBackground(BAR_COLOR);
        this.toolPanel.setMinimumSize(new Dimension(350, 28));
        this.toolPanel.setPreferredSize(new Dimension(350, 28));
        this.toolPanel.setLayout(new BoxLayout(this.toolPanel, BoxLayout.X_AXIS));

        this.toolPanel.add(Box.createHorizontalStrut(5));
        this.toolPanel.add(new JLabel("Add tools:"));

        this.toolPanel.add(Box.createHorizontalStrut(5));
        // button group that allows no selection when toggling the currently selected
        ButtonGroup group = new ButtonGroup()
        {
            /** */
            private static final long serialVersionUID = 20240227L;

            /** {@inheritDoc} */
            @Override
            public void setSelected(final ButtonModel model, final boolean selected)
            {
                if (selected)
                {
                    super.setSelected(model, selected);
                }
                else
                {
                    clearSelection();
                }
            }
        };

        JToggleButton nodeButton = new JToggleButton(loadIcon("./OTS_node.png"));
        nodeButton.setPreferredSize(new Dimension(24, 24));
        nodeButton.setMinimumSize(new Dimension(24, 24));
        nodeButton.setMaximumSize(new Dimension(24, 24));
        nodeButton.setToolTipText("Add node");
        group.add(nodeButton);
        this.toolPanel.add(nodeButton);

        JToggleButton linkButton = new JToggleButton(loadIcon("./OTS_link.png"));
        linkButton.setPreferredSize(new Dimension(24, 24));
        linkButton.setMinimumSize(new Dimension(24, 24));
        linkButton.setMaximumSize(new Dimension(24, 24));
        linkButton.setToolTipText("Add link");
        group.add(linkButton);
        this.toolPanel.add(linkButton);

        JToggleButton centroidButton = new JToggleButton(loadIcon("./OTS_centroid.png"));
        centroidButton.setPreferredSize(new Dimension(24, 24));
        centroidButton.setMinimumSize(new Dimension(24, 24));
        centroidButton.setMaximumSize(new Dimension(24, 24));
        centroidButton.setToolTipText("Add centroid");
        group.add(centroidButton);
        this.toolPanel.add(centroidButton);

        JToggleButton connectorButton = new JToggleButton(loadIcon("./OTS_connector.png"));
        connectorButton.setPreferredSize(new Dimension(24, 24));
        connectorButton.setMinimumSize(new Dimension(24, 24));
        connectorButton.setMaximumSize(new Dimension(24, 24));
        connectorButton.setToolTipText("Add connector");
        group.add(connectorButton);
        this.toolPanel.add(connectorButton);

        this.toolPanel.add(Box.createHorizontalStrut(5));
        JComboBox<String> shape = new AppearanceControlComboBox<>();
        shape.setModel(new DefaultComboBoxModel<>(new String[] {"Straight", "Bezier", "Clothoid", "Arc", "PolyLine"}));
        shape.setMinimumSize(new Dimension(50, 22));
        shape.setMaximumSize(new Dimension(90, 22));
        shape.setPreferredSize(new Dimension(90, 22));
        shape.setToolTipText("Standard shape for new links");
        this.toolPanel.add(shape);

        this.toolPanel.add(Box.createHorizontalStrut(5));
        JComboBox<String> roadLayout = new AppearanceControlComboBox<>();
        roadLayout.setModel(new DefaultComboBoxModel<>(new String[] {}));
        roadLayout.setMinimumSize(new Dimension(50, 22));
        roadLayout.setMaximumSize(new Dimension(125, 22));
        roadLayout.setPreferredSize(new Dimension(125, 22));
        roadLayout.setToolTipText("Standard defined road layout for new links");
        roadLayout.setEnabled(false);
        this.toolPanel.add(roadLayout);

        this.toolPanel.add(Box.createHorizontalStrut(5));
        JComboBox<String> linkType = new AppearanceControlComboBox<>();
        linkType.setModel(new DefaultComboBoxModel<>(new String[] {}));
        linkType.setMinimumSize(new Dimension(50, 22));
        linkType.setMaximumSize(new Dimension(125, 22));
        linkType.setPreferredSize(new Dimension(125, 22));
        linkType.setToolTipText("Standard link type for new links and connectors");
        linkType.setEnabled(false);
        this.toolPanel.add(linkType);

        this.toolPanel.add(Box.createHorizontalStrut(5));
        Dimension minDim = new Dimension(0, 1);
        Dimension prefDim = new Dimension(0, 1);
        Dimension maxDim = new Dimension(5000, 1);
        this.toolPanel.add(new Filler(minDim, prefDim, maxDim)); // pushes further elements right aligned

        this.toolPanel.add(new JLabel("Show:"));

        this.toolPanel.add(Box.createHorizontalStrut(5));
        JButton extent = new JButton(loadIcon("./Expand.png"));
        extent.setMinimumSize(new Dimension(24, 24));
        extent.setMaximumSize(new Dimension(24, 24));
        extent.setPreferredSize(new Dimension(24, 24));
        extent.setToolTipText("Zoom whole network");
        extent.addActionListener((e) -> this.animationPanel.zoomAll());
        this.toolPanel.add(extent);

        JButton grid = new JButton(loadIcon("./Grid.png"));
        grid.setMinimumSize(new Dimension(24, 24));
        grid.setMaximumSize(new Dimension(24, 24));
        grid.setPreferredSize(new Dimension(24, 24));
        grid.setToolTipText("Toggle grid on/off");
        grid.addActionListener((e) -> this.animationPanel.setShowGrid(!this.animationPanel.isShowGrid()));
        this.toolPanel.add(grid);

        this.toolPanel.add(Box.createHorizontalStrut(5));

        add(this.toolPanel, BorderLayout.NORTH);
    }

    /**
     * Loads an icon from the given file. Returns {@code null} if the file can not be found.
     * @param file file.
     * @return icon.
     */
    private Icon loadIcon(final String file)
    {
        try
        {
            return OtsEditor.loadIcon(file, 16, 16, -1, -1);
        }
        catch (IOException ioe)
        {
            // skip loading icon
            return null;
        }
    }

    /**
     * Sets the animation toggles as useful for in the editor.
     */
    private void setAnimationToggles()
    {
        addToggle("Node", NodeData.class, "/icons/Node24.png", "Show/hide nodes", true, false);
        addToggle("NodeId", NodeAnimation.Text.class, "/icons/Id24.png", "Show/hide node ids", false, true);
        addToggle("Link", LinkData.class, "/icons/Link24.png", "Show/hide links", true, false);
        addToggle("LinkId", LinkAnimation.Text.class, "/icons/Id24.png", "Show/hide link ids", false, true);
        addToggle("Priority", PriorityData.class, "/icons/Priority24.png", "Show/hide link priority", true, false);
        addToggle("Lane", LaneData.class, "/icons/Lane24.png", "Show/hide lanes", true, false);
        addToggle("LaneId", LaneAnimation.Text.class, "/icons/Id24.png", "Show/hide lane ids", false, true);
        addToggle("LaneCenter", CenterLine.class, "/icons/CenterLine24.png", "Show/hide lane center lines", false, false);
        addToggle("Stripe", StripeData.class, "/icons/Stripe24.png", "Show/hide stripes", true, false);
        addToggle("Shoulder", ShoulderData.class, "/icons/Shoulder24.png", "Show/hide shoulders", true, false);
        // TODO: perhaps a specific data type for generators?
        addToggle("Generator", GtuGeneratorPositionData.class, "/icons/Generator24.png", "Show/hide generators", true, false);
        addToggle("Sink", SinkData.class, "/icons/Sink24.png", "Show/hide sinks", true, true);
        addToggle("Detector", LoopDetectorData.class, "/icons/Detector24.png", "Show/hide loop detectors", true, false);
        addToggle("DetectorId", LoopDetectorData.Text.class, "/icons/Id24.png", "Show/hide loop detector ids", false, true);
        addToggle("Light", TrafficLightData.class, "/icons/TrafficLight24.png", "Show/hide traffic lights", true, false);
        addToggle("LightId", TrafficLightAnimation.Text.class, "/icons/Id24.png", "Show/hide traffic light ids", false, true);
        addToggle("Bus", BusStopData.class, "/icons/BusStop24.png", "Show/hide bus stops", true, false);
        addToggle("BusId", BusStopAnimation.Text.class, "/icons/Id24.png", "Show/hide bus stop ids", false, true);
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
    public final void addToggle(final String name, final Class<? extends Locatable> locatableClass, final String iconPath,
            final String toolTipText, final boolean initiallyVisible, final boolean idButton)
    {
        JToggleButton button;
        Icon icon = OtsControlPanel.loadIcon(iconPath);
        Icon unIcon = OtsControlPanel.loadGrayscaleIcon(iconPath);
        button = new JCheckBox();
        button.setSelectedIcon(icon);
        button.setIcon(unIcon);
        button.setPreferredSize(new Dimension(32, 28));
        button.setName(name);
        button.setEnabled(true);
        button.setSelected(initiallyVisible);
        button.setActionCommand(name);
        button.setToolTipText(toolTipText);
        button.addActionListener(new ActionListener()
        {
            /** {@inheritDoc} */
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                String actionCommand = e.getActionCommand();
                if (EditorMap.this.toggleLocatableMap.containsKey(actionCommand))
                {
                    Class<? extends Locatable> locatableClass = EditorMap.this.toggleLocatableMap.get(actionCommand);
                    EditorMap.this.animationPanel.toggleClass(locatableClass);
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

        if (initiallyVisible)
        {
            this.animationPanel.showClass(locatableClass);
        }
        else
        {
            this.animationPanel.hideClass(locatableClass);
        }
        this.toggleLocatableMap.put(name, locatableClass);
    }

    /**
     * Builds a map panel with an animator and context.
     * @param editor editor.
     * @return map.
     * @throws RemoteException context binding problem.
     * @throws NamingException context binding problem.
     */
    public static EditorMap build(final OtsEditor editor) throws RemoteException, NamingException
    {
        AnimationUpdaterThread animator = new AnimationUpdaterThread();
        animator.start();
        ContextInterface context = new JvmContext("ots-context");
        Contextualized contextualized = new Contextualized()
        {
            /** {@inheritDoc} */
            @Override
            public ContextInterface getContext()
            {
                return context;
            }
        };
        return new EditorMap(animator, contextualized, editor);
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        if (event.getType().equals(OtsEditor.NEW_FILE))
        {
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
            XsdTreeNodeRoot root = (XsdTreeNodeRoot) event.getContent();
            root.addListener(this, XsdTreeNodeRoot.NODE_CREATED);
            root.addListener(this, XsdTreeNodeRoot.NODE_REMOVED);
            SwingUtilities.invokeLater(() -> this.animationPanel.zoomAll());
        }
        else if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED))
        {
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode node = (XsdTreeNode) content[0];
            if (isType(node))
            {
                node.addListener(this, XsdTreeNode.ACTIVATION_CHANGED);
                node.addListener(this, XsdTreeNode.OPTION_CHANGED);
                if (node.isActive())
                {
                    add(node);
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
        }
        else if (event.getType().equals(XsdTreeNodeRoot.NODE_REMOVED))
        {
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode node = (XsdTreeNode) content[0];
            if (this.datas.containsKey(node)) // node.isType does not work as parent is gone, i.e. type is just "Node"
            {
                remove(node);
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
        }
        else if (event.getType().equals(XsdTreeNode.ACTIVATION_CHANGED))
        {
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode node = (XsdTreeNode) content[0];
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
        }
        else if (event.getType().equals(XsdTreeNode.OPTION_CHANGED))
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
        }
        else if (event.getType().equals(XsdTreeNode.ATTRIBUTE_CHANGED))
        {
            for (MapLinkData linkData : this.links.keySet())
            {
                linkData.notifyNodeIdChanged(linkData.getNode());
            }
        }
    }

    /**
     * Returns whether the node is any of the visualized types.
     * @param node node.
     * @return whether the node is any of the visualized types.
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
     * @param data data that is valid to draw.
     */
    public void setValid(final MapData data)
    {
        XsdTreeNode node = data.getNode();
        if (this.animations.containsKey(node))
        {
            return;
        }
        Renderable2d<?> animation;
        if (node.getPathString().equals(XsdPaths.NODE))
        {
            animation = Try.assign(() -> new NodeAnimation((MapNodeData) data, this.contextualized), "");
        }
        else if (node.getPathString().equals(XsdPaths.LINK))
        {
            animation = Try.assign(() -> new LinkAnimation((MapLinkData) data, this.contextualized, 0.5f).setDynamic(true), "");
        }
        else if (node.getPathString().equals(XsdPaths.TRAFFIC_LIGHT))
        {
            animation = Try.assign(() -> new TrafficLightAnimation((MapTrafficLightData) data, this.contextualized), "");
        }
        else if (node.getPathString().equals(XsdPaths.SINK))
        {
            Function<LaneDetectorAnimation<SinkData, SinkText>, SinkText> textSupplier =
                    (s) -> Try.assign(() -> new SinkText(s.getSource(),
                            (float) (s.getSource().getLine().getLength() / 2.0 + 0.2), this.contextualized), "");
            animation = Try.assign(() -> new LaneDetectorAnimation<SinkData, SinkText>((SinkData) data, this.contextualized,
                    Color.ORANGE, textSupplier), "");
        }
        else if (node.getPathString().equals(XsdPaths.GENERATOR) || node.getPathString().equals(XsdPaths.LIST_GENERATOR))
        {
            animation = Try
                    .assign(() -> new GtuGeneratorPositionAnimation((GtuGeneratorPositionData) data, this.contextualized), "");
        }
        else
        {
            throw new UnsupportedOperationException("Data cannot be added by the map editor.");
        }
        this.animations.put(node, animation);
    }

    /**
     * Set the data as being invalid to draw.
     * @param data data that is invalid to draw.
     */
    // TODO: for some reason, this does not work... because data remains in JVM?
    public void setInvalid(final MapData data)
    {
        //
    }

    /**
     * Adds a data representation of the node. This will not yet be drawn until the data object itself tells the map it is valid
     * to be drawn.
     * @param node node of element to draw.
     * @throws RemoteException context binding problem.
     */
    private void add(final XsdTreeNode node) throws RemoteException
    {
        MapData data;
        if (this.datas.containsKey(node))
        {
            return; // activated choice
        }
        if (node.getPathString().equals(XsdPaths.NODE))
        {
            data = new MapNodeData(this, node, this.editor);
            node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        }
        else if (node.getPathString().equals(XsdPaths.LINK))
        {
            MapLinkData linkData = new MapLinkData(this, node, this.editor);
            data = linkData;
            for (RoadLayoutListener roadLayoutListener : this.roadLayoutListeners.values())
            {
                roadLayoutListener.addListener(linkData, ChangeListener.CHANGE_EVENT, ReferenceType.WEAK);
            }
            if (this.networkFlattenerListener != null)
            {
                this.networkFlattenerListener.addListener(linkData, ChangeListener.CHANGE_EVENT, ReferenceType.WEAK);
            }
            this.links.put(linkData, null);
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
        this.datas.put(node, data);
    }

    /**
     * Remove the drawing data of pertaining to the node.
     * @param node node.
     */
    private void remove(final XsdTreeNode node)
    {
        if (node.getPathString().equals(XsdPaths.NODE))
        {
            node.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        }
        if (node.getPathString().equals(XsdPaths.LINK))
        {
            Iterator<MapLinkData> it = this.links.keySet().iterator();
            while (it.hasNext())
            {
                MapLinkData link = it.next();
                if (link.getNode().equals(node))
                {
                    for (RoadLayoutListener roadLayoutListener : this.roadLayoutListeners.values())
                    {
                        roadLayoutListener.removeListener(link, ChangeListener.CHANGE_EVENT);
                    }
                    if (this.networkFlattenerListener != null)
                    {
                        this.networkFlattenerListener.removeListener(link, ChangeListener.CHANGE_EVENT);
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
     * @param node node.
     */
    public void reinitialize(final XsdTreeNode node)
    {
        remove(node);
        Try.execute(() -> add(node), RuntimeException.class, "Unable to bind to context.");
    }

    /**
     * Returns the map data of the given XSD node.
     * @param node node.
     * @return map data of the given XSD node, {@code null} if no such data.
     */
    public MapData getData(final XsdTreeNode node)
    {
        return this.datas.get(node);
    }

    /**
     * Add defined road layout.
     * @param node node of the defined road layout.
     */
    private void addRoadLayout(final XsdTreeNode node)
    {
        RoadLayoutListener roadLayoutListener = new RoadLayoutListener(node, () -> this.editor.getEval());
        for (MapLinkData linkData : this.links.keySet())
        {
            roadLayoutListener.addListener(linkData, ChangeListener.CHANGE_EVENT, ReferenceType.WEAK);
        }
        this.roadLayoutListeners.put(node, roadLayoutListener);
    }

    /**
     * Remove defined road layout.
     * @param node node of the defined road layout.
     */
    private void removeRoadLayout(final XsdTreeNode node)
    {
        RoadLayoutListener roadLayoutListener = this.roadLayoutListeners.remove(node);
        roadLayoutListener.destroy();
    }

    /**
     * Sets the network level flattener.
     * @param node node of network flattener.
     */
    private void setNetworkFlattener(final XsdTreeNode node)
    {
        this.networkFlattenerListener = new FlattenerListener(node, () -> this.editor.getEval());
        for (MapLinkData linkData : this.links.keySet())
        {
            this.networkFlattenerListener.addListener(linkData, ChangeListener.CHANGE_EVENT, ReferenceType.WEAK);
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
        }
        this.networkFlattenerListener.destroy();
        for (MapLinkData linkData : this.links.keySet())
        {
            Try.execute(() -> linkData.notify(new Event(ChangeListener.CHANGE_EVENT, this.networkFlattenerListener.getNode())),
                    "Remove event exception.");
        }
        this.networkFlattenerListener = null;
    }

    /**
     * Returns the road layout listener from which a {@code MapLinkData} can obtain offsets.
     * @param node node of a defined layout.
     * @return listener, can be used to obtain offsets.
     */
    RoadLayoutListener getRoadLayoutListener(final XsdTreeNode node)
    {
        return this.roadLayoutListeners.get(node);
    }

    /**
     * Remove animation.
     * @param animation animation to remove.
     */
    void removeAnimation(final Renderable2d<?> animation)
    {
        if (animation != null)
        {
            this.animationPanel.objectRemoved(animation);
            animation.destroy(this.contextualized);
        }
    }

    /**
     * Returns the context.
     * @return context.
     */
    Contextualized getContextualized()
    {
        return this.contextualized;
    }

    /**
     * Returns the network level flattener, or a 64 segment flattener of none specified.
     * @return flattener.
     */
    public Flattener getNetworkFlattener()
    {
        if (this.networkFlattenerListener != null)
        {
            Flattener flattener = this.networkFlattenerListener.getData();
            if (flattener != null)
            {
                return flattener; // otherwise, return default
            }
        }
        return new NumSegments(64);
    }

}
