package org.opentrafficsim.editor.extensions.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
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
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.LocalEventProducer;
import org.djutils.event.reference.ReferenceType;
import org.opentrafficsim.animation.IconUtil;
import org.opentrafficsim.core.geometry.CurveFlattener;
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

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.d2.Renderable2d;
import nl.tudelft.simulation.dsol.simulators.AnimatorInterface;
import nl.tudelft.simulation.dsol.swing.animation.d2.VisualizationPanel;
import nl.tudelft.simulation.naming.context.ContextInterface;
import nl.tudelft.simulation.naming.context.Contextualized;
import nl.tudelft.simulation.naming.context.JvmContext;

/**
 * Editor map.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class EditorMap extends JPanel implements EventListener
{

    /** */
    private static final long serialVersionUID = 20231010L;

    /** Color for toolbar and toggle bar. */
    private static final Color BAR_COLOR = Color.LIGHT_GRAY;

    /** All types that are valid to show in the map. */
    private static final Set<String> TYPES = Set.of(XsdPaths.CENTROID, XsdPaths.CONNECTOR, XsdPaths.NODE, XsdPaths.LINK,
            XsdPaths.TRAFFIC_LIGHT, XsdPaths.SINK, XsdPaths.GENERATOR, XsdPaths.LIST_GENERATOR);

    /** Context provider. */
    private final Contextualized contextualized;

    /** Editor. */
    private final OtsEditor editor;

    /** Panel to draw in. */
    private final VisualizationPanel visualizationPanel;

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
    // private boolean ignoreKeepScale = false;

    /** Last x-scale. */
    // private Double lastXScale = null;

    /** Last y-scale. */
    // private Double lastYScale = null;

    /** Last screen size. */
    // private Dimension lastScreen = null;

    /** Map of toggle names to toggle animation classes. */
    private Map<String, Class<? extends Locatable>> toggleLocatableMap = new LinkedHashMap<>();

    /** Map of synchronizable stripes. */
    private Map<MapStripeData, SynchronizableMapStripe> synStripes = new LinkedHashMap<>();

    /** Listeners to lane and stripe overrides. */
    private final Map<XsdTreeNode, ChangeListener<Object>> overrideListeners = new LinkedHashMap<>();

    /** Updater of map animation. */
    private final MapUpdater updater = new MapUpdater();

    /**
     * Constructor.
     * @param contextualized context provider.
     * @param editor editor.
     * @throws RemoteException context binding problem.
     * @throws NamingException context binding problem.
     */
    private EditorMap(final Contextualized contextualized, final OtsEditor editor) throws RemoteException, NamingException
    {
        super(new BorderLayout());
        this.contextualized = contextualized;
        this.editor = editor;
        this.visualizationPanel = new VisualizationPanel(new Bounds2d(500, 500), this.updater, contextualized.getContext())
        {
            private static final long serialVersionUID = 20260212L;

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
        };
        this.updater.addListener(this.visualizationPanel, AnimatorInterface.UPDATE_ANIMATION_EVENT);

        /*-
        {
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

            @Override
            public synchronized void home()
            {
                EditorMap.this.ignoreKeepScale = true;
                super.home();
                EditorMap.this.ignoreKeepScale = false;
            }
        };
        */

        this.visualizationPanel.setBackground(Color.GRAY);
        this.visualizationPanel.setShowToolTip(false);
        editor.addListener(this, OtsEditor.NEW_FILE);

        /*-
        this.animationPanel.setRenderableScale(new RenderableScale()
        {
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
        */

        add(this.visualizationPanel, BorderLayout.CENTER);

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

        Dimension buttonSize = new Dimension(24, 24);
        JToggleButton nodeButton = new JToggleButton(IconUtil.of("Node24.png").imageSize(18, 18).get());
        nodeButton.setPreferredSize(buttonSize);
        nodeButton.setMinimumSize(buttonSize);
        nodeButton.setMaximumSize(buttonSize);
        nodeButton.setToolTipText("Add node");
        group.add(nodeButton);
        this.toolPanel.add(nodeButton);

        JToggleButton linkButton = new JToggleButton(IconUtil.of("Link24.png").imageSize(18, 18).get());
        linkButton.setPreferredSize(buttonSize);
        linkButton.setMinimumSize(buttonSize);
        linkButton.setMaximumSize(buttonSize);
        linkButton.setToolTipText("Add link");
        group.add(linkButton);
        this.toolPanel.add(linkButton);

        JToggleButton centroidButton = new JToggleButton(IconUtil.of("Centroid24.png").imageSize(18, 18).get());
        centroidButton.setPreferredSize(buttonSize);
        centroidButton.setMinimumSize(buttonSize);
        centroidButton.setMaximumSize(buttonSize);
        centroidButton.setToolTipText("Add centroid");
        group.add(centroidButton);
        this.toolPanel.add(centroidButton);

        JToggleButton connectorButton = new JToggleButton(IconUtil.of("Connector24.png").imageSize(18, 18).get());
        connectorButton.setPreferredSize(buttonSize);
        connectorButton.setMinimumSize(buttonSize);
        connectorButton.setMaximumSize(buttonSize);
        connectorButton.setToolTipText("Add connector");
        group.add(connectorButton);
        this.toolPanel.add(connectorButton);

        this.toolPanel.add(Box.createHorizontalStrut(5));
        JComboBox<String> shape = new AppearanceControlComboBox<>();
        shape.setModel(new DefaultComboBoxModel<>(new String[] {"Straight", "Bezier", "Clothoid", "Arc", "PolyLine"}));
        shape.setMinimumSize(new Dimension(80, 22));
        shape.setMaximumSize(new Dimension(100, 22));
        shape.setPreferredSize(new Dimension(100, 22));
        shape.setToolTipText("Standard shape for new links");
        // Renderer to combine icon and text
        Map<String, Icon> shapeIcons = Map.of("straight", IconUtil.of("Straight24.png").imageSize(18, 18).get(), "bezier",
                IconUtil.of("Bezier24.png").imageSize(18, 18).get(), "clothoid",
                IconUtil.of("Clothoid24.png").imageSize(18, 18).get(), "arc", IconUtil.of("Arc24.png").imageSize(18, 18).get(),
                "polyline", IconUtil.of("PolyLine24.png").imageSize(18, 18).get());
        shape.setRenderer(new DefaultListCellRenderer()
        {
            /** */
            private static final long serialVersionUID = 20260206L;

            @Override
            public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index,
                    final boolean isSelected, final boolean cellHasFocus)
            {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String shapeId = label.getText().toLowerCase();
                if (shapeIcons.containsKey(shapeId))
                {
                    label.setIcon(shapeIcons.get(shapeId));
                }
                return label;
            }
        });
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
        JButton resetY = new JButton(IconUtil.of("UpDown24.png").imageSize(18, 18).get());
        resetY.setMinimumSize(buttonSize);
        resetY.setMaximumSize(buttonSize);
        resetY.setPreferredSize(buttonSize);
        resetY.setToolTipText("Reset Y-zoom");
        resetY.addActionListener((e) -> this.visualizationPanel.resetZoomY());
        this.toolPanel.add(resetY);

        JButton extent = new JButton(IconUtil.of("ZoomAll24.png").imageSize(18, 18).get());
        extent.setMinimumSize(buttonSize);
        extent.setMaximumSize(buttonSize);
        extent.setPreferredSize(buttonSize);
        extent.setToolTipText("Zoom whole network");
        extent.addActionListener((e) -> safeZoomAll());
        this.toolPanel.add(extent);

        JButton grid = new JButton(IconUtil.of("Grid24.png").imageSize(18, 18).get());
        grid.setMinimumSize(buttonSize);
        grid.setMaximumSize(buttonSize);
        grid.setPreferredSize(buttonSize);
        grid.setToolTipText("Toggle grid on/off");
        grid.addActionListener((e) ->
        {
            this.visualizationPanel.setShowGrid(!this.visualizationPanel.isShowGrid());
            this.updater.update();
        });
        this.toolPanel.add(grid);

        this.toolPanel.add(Box.createHorizontalStrut(5));

        add(this.toolPanel, BorderLayout.NORTH);
    }

    /**
     * Zoom all, or home extent if there are no objects.
     */
    private void safeZoomAll()
    {
        if (!this.visualizationPanel.getElements().isEmpty())
        {
            this.visualizationPanel.zoomAll();
        }
        else
        {
            try
            {
                this.visualizationPanel.home();
            }
            catch (Exception ex)
            {
                SwingUtilities.invokeLater(() -> this.visualizationPanel.home());
            }
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
        button.setSelected(initiallyVisible);
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
     * @param editor editor.
     * @return map.
     * @throws RemoteException context binding problem.
     * @throws NamingException context binding problem.
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
            SwingUtilities.invokeLater(() -> safeZoomAll());
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
                                linkData.evalChanged();
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
            this.updater.update();
        }
        else if (event.getType().equals(XsdTreeNodeRoot.NODE_REMOVED))
        {
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode node = (XsdTreeNode) content[0];
            if (this.datas.containsKey(node)) // node.isType does not work as parent is gone, i.e. type is just "Node"
            {
                remove(node); // updates animation panel
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
            this.updater.update();
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
            this.updater.update();
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
            this.updater.update();
        }
        else if (event.getType().equals(XsdTreeNode.ATTRIBUTE_CHANGED))
        {
            for (MapLinkData linkData : this.links.keySet())
            {
                linkData.notifyNodeIdChanged(linkData.getNode());
            }
            this.updater.update();
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
        if (node.getPathString().equals(XsdPaths.NODE) || node.getPathString().equals(XsdPaths.CENTROID))
        {
            animation = new NodeAnimation((MapNodeData) data, this.contextualized);
        }
        else if (node.getPathString().equals(XsdPaths.LINK) || node.getPathString().equals(XsdPaths.CONNECTOR))
        {
            animation = new LinkAnimation((MapLinkData) data, this.contextualized, 0.5f).setDynamic(true);
        }
        else if (node.getPathString().equals(XsdPaths.TRAFFIC_LIGHT))
        {
            animation = new TrafficLightAnimation((MapTrafficLightData) data, this.contextualized);
        }
        else if (node.getPathString().equals(XsdPaths.SINK))
        {
            Function<LaneDetectorAnimation<SinkData, SinkText>, SinkText> textSupplier = (s) -> new SinkText(s.getSource(),
                    (float) (s.getSource().getLine().getLength() / 2.0 + 0.2), this.contextualized);
            animation = new LaneDetectorAnimation<SinkData, SinkText>((SinkData) data, this.contextualized, Color.ORANGE,
                    textSupplier);
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
            node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        }
        else if (node.getPathString().equals(XsdPaths.LINK) || node.getPathString().equals(XsdPaths.CONNECTOR))
        {
            MapLinkData linkData = new MapLinkData(this, node, this.editor);
            data = linkData;
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
        add(node);
    }

    /**
     * Returns the map data of the given XSD node.
     * @param node node.
     * @return map data of the given XSD node, empty if no such data.
     */
    public Optional<MapData> getData(final XsdTreeNode node)
    {
        return Optional.ofNullable(this.datas.get(node));
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
            linkData.notify(new Event(ChangeListener.CHANGE_EVENT, this.networkFlattenerListener.getNode()));
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
            this.visualizationPanel.objectRemoved(animation);
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
     * Event producer that fires an update animation event.
     */
    public static class MapUpdater extends LocalEventProducer
    {
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
            fireEvent(AnimatorInterface.UPDATE_ANIMATION_EVENT);
        }
    }

}
