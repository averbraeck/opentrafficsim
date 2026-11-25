package org.opentrafficsim.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.CellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.djutils.eval.Eval;
import org.djutils.event.EventListener;
import org.djutils.event.EventListenerMap;
import org.djutils.event.EventProducer;
import org.djutils.event.EventType;
import org.djutils.io.ResourceResolver.ResourceHandle;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.animation.data.util.IconUtil;
import org.opentrafficsim.editor.DocumentReader.DocumentResource;
import org.opentrafficsim.editor.EvalWrapper.EvalListener;
import org.opentrafficsim.editor.Undo.ActionType;
import org.opentrafficsim.editor.listeners.AttributesKeyListener;
import org.opentrafficsim.editor.listeners.AttributesListSelectionListener;
import org.opentrafficsim.editor.listeners.AttributesMouseListener;
import org.opentrafficsim.editor.listeners.ChangesListener;
import org.opentrafficsim.editor.listeners.FieldListener;
import org.opentrafficsim.editor.listeners.PopupValueSelectedListener;
import org.opentrafficsim.editor.listeners.ScenarioActionListener;
import org.opentrafficsim.editor.listeners.XsdTreeEditorListener;
import org.opentrafficsim.editor.listeners.XsdTreeKeyListener;
import org.opentrafficsim.editor.listeners.XsdTreeListener;
import org.opentrafficsim.editor.render.AttributeCellRenderer;
import org.opentrafficsim.editor.render.AttributesCellEditor;
import org.opentrafficsim.editor.render.StringCellRenderer;
import org.opentrafficsim.editor.render.XsdTreeCellRenderer;
import org.opentrafficsim.road.network.factory.xml.CircularDependencyException;
import org.opentrafficsim.swing.gui.Appearance;
import org.opentrafficsim.swing.gui.AppearanceApplication;
import org.opentrafficsim.swing.gui.AppearanceControlButton;
import org.opentrafficsim.swing.gui.AppearanceControlComboBox;
import org.opentrafficsim.swing.gui.AppearanceControlTextField;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

import com.google.common.base.Objects;

import de.javagl.treetable.JTreeTable;

/**
 * Editor window to load, edit and save OTS XML files. The class uses an underlying data structure that is based on the XML
 * Schema for the XML (XSD).<br>
 * <br>
 * This functionality is currently in development.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class OtsEditor extends AppearanceApplication implements EventProducer, OtsEditorProperties
{

    /** Serialization version UID. */
    private static final long serialVersionUID = 20230217L;

    /** Event when a new file is started. */
    public static final EventType NEW_FILE = new EventType("NEWFILE",
            new MetaData("New file", "New file", new ObjectDescriptor("Root", "New root element", XsdTreeNodeRoot.class)));

    /** Event when the selection in the tree is changed. */
    public static final EventType SELECTION_CHANGED = new EventType("SELECTIONCHANGED", new MetaData("Selection",
            "Selection changed", new ObjectDescriptor("Selected node", "Selected node", XsdTreeNode.class)));

    /** Event when a mouse event took place on a node. */
    public static final EventType MOUSE_EVENT = new EventType("MOUSE_EVENT",
            new MetaData("MouseEvent", "Mouse event on node",
                    new ObjectDescriptor("Concerned node", "Concerned node", XsdTreeNode.class),
                    new ObjectDescriptor("Mouse event", "Mouse event", MouseEvent.class)));

    /** Width of the divider between parts of the screen. */
    private static final int DIVIDER_SIZE = 4;

    /** Whether to update the windows as the split is being dragged. */
    private static final boolean UPDATE_SPLIT_WHILE_DRAGGING = true;

    /** Standard icon size. */
    public static final int ICON_SIZE = 18;

    /** Standard square button dimension. */
    private static final Dimension BUTTON_DIMENSION = new Dimension(24, 24);

    /** Indent for first item shown in drop-down. */
    private int dropdownIndent = 0;

    /** All items eligible to be shown in a drop-down, i.e. they match the currently typed value. */
    private List<String> dropdownOptions = new ArrayList<>();

    /** Listener on text field editor that can remove itself. */
    private FieldListener fieldListener;

    /** Map of listeners for {@code EventProducer}. */
    private final EventListenerMap listenerMap = new EventListenerMap();

    /** Main split pane. */
    private final JSplitPane leftRightSplitPane;

    /** Main tabbed pane at the left-hand side. */
    private final JTabbedPane visualizationPane;

    /** Title of the tab selected at the previous tab change. */
    private String focusedTab;

    /** Actions invoked when the corresponding tab loses focus, before other tab change handling. */
    private final Map<String, List<Runnable>> tabReleaseActions = new LinkedHashMap<>();

    /** Actions invoked when the corresponding tab is selected, before other tab change handling. */
    private final Map<String, List<Runnable>> tabSelectActions = new LinkedHashMap<>();

    /** Split pane on the right-hand side. */
    private final JSplitPane rightSplitPane;

    /** Search toggle button. */
    private final JToggleButton searchToggle;

    /** Search field. */
    private final JTextField search;

    /** Scenario selection. */
    private final JComboBox<ScenarioWrapper> scenario;

    /** Eval wrapper, which maintains input parameters and notifies all dependent objects on changes. */
    private final EvalWrapper evalWrapper = new EvalWrapper(this);

    /** Tree table at the top in the right-hand side. */
    private JTreeTable treeTable;

    /** Table for attributes at the bottom of the right-hand side. */
    private final JTable attributesTable;

    /** Model of the table for attributes. */
    private final AttributesTableModel attributesTableModel = new AttributesTableModel();

    /** Status label. */
    private final JLabel statusLabel;

    /** Prevents a pop-up when an expand node is being clicked. */
    private boolean mayPresentChoice = true;

    /** Node for which currently a choice pop-up is being shown, {@code null} if there is none. */
    private XsdTreeNode choiceNode;

    /** Map of custom icons, to be loaded as the icon for a node is being composed based in its properties. */
    private Map<String, Icon> customIcons = new LinkedHashMap<>();

    /** Icon for in question dialog. */
    private final Icon questionIcon = IconUtil.of("Question24.png").get();

    /** Dialogs. */
    private final Dialogs dialogs = new Dialogs(this);

    /** Root node of the XSD file. */
    private Document xsdDocument;

    /** Last file that was loaded or saved, {@code null} if no file is known. */
    private Path lastPath;

    /** Whether there is unsaved content. */
    private boolean unsavedChanges = false;

    /** Undo unit, storing all actions. */
    private Undo undo;

    /** Whether to disregard changes during loading. */
    private boolean ignoreChanges = false;

    /** Whether updates of XsdTreeNode.expanded from tree events are temporarily suppressed. */
    private boolean suppressExpansionStateUpdates = false;

    /** Auto save scheduler. */
    private ScheduledExecutorService autosave;

    /** Number of auto-save request. */
    private AtomicLong autosaveRequest = new AtomicLong(0L);

    /** Navigation (F3/F4). */
    private final Navigation navigation = new Navigation(this, PROPERTIES_STORE.getInteger(MAX_NAVIGATE_STEPS_KEY));

    // copy/paste

    /** Node in clip-board (sort of...). */
    private XsdTreeNode clipboard;

    /** Whether the node in the clip-board was cut. */
    private boolean cut;

    /** Actions that can be performed on the editor. */
    private Actions actions;

    /** Menu with recent files. */
    private JMenu recentFilesMenu;

    /**
     * List of root properties, such as xmlns:ots="http://www.opentrafficsim.org/ots". Keys at uneven indices and values at even
     * indices.
     */
    private final List<String> properties = new ArrayList<>();

    /**
     * Constructor.
     * @throws IOException when a resource could not be loaded.
     */
    public OtsEditor() throws IOException
    {
        super();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // only exit after possible confirmation in case of unsaved changes
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(final WindowEvent e)
            {
                exit();
            }
        });

        // split panes
        this.leftRightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, UPDATE_SPLIT_WHILE_DRAGGING);
        this.leftRightSplitPane.setDividerSize(DIVIDER_SIZE);
        this.leftRightSplitPane.setResizeWeight(0.5);
        makeClickFlippable(this.leftRightSplitPane);
        add(this.leftRightSplitPane);
        this.rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, UPDATE_SPLIT_WHILE_DRAGGING);
        this.rightSplitPane.setDividerSize(DIVIDER_SIZE);
        this.rightSplitPane.setResizeWeight(0.5);
        this.rightSplitPane.setAlignmentX(0.5f);
        this.rightSplitPane.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        this.rightSplitPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        makeClickFlippable(this.rightSplitPane);
        JPanel rightContainer = new JPanel();
        rightContainer.setLayout(new BoxLayout(rightContainer, BoxLayout.Y_AXIS));
        rightContainer.setBorder(new LineBorder(null, -1));

        // visualization pane
        UIManager.getInsets("TabbedPane.contentBorderInsets").set(-1, -1, 1, -1);
        this.visualizationPane = new JTabbedPane(JTabbedPane.BOTTOM, JTabbedPane.SCROLL_TAB_LAYOUT);
        this.visualizationPane.setPreferredSize(new Dimension(900, 900));
        this.visualizationPane.setBorder(new LineBorder(Color.BLACK, 0));
        this.leftRightSplitPane.setLeftComponent(this.visualizationPane);
        // Registered before any tab is added, so tab actions run before other tab change handlers.
        this.visualizationPane.addChangeListener((e) ->
        {
            int index = this.visualizationPane.getSelectedIndex();
            String newTab = index < 0 ? null : this.visualizationPane.getTitleAt(index);
            if (Objects.equal(this.focusedTab, newTab))
            {
                return;
            }
            String oldTab = this.focusedTab;
            this.focusedTab = newTab;
            if (oldTab != null)
            {
                runTabActions(this.tabReleaseActions.get(oldTab));
            }
            runTabActions(this.tabSelectActions.get(newTab));
        });

        // search, scenario and controls
        JPanel searchWrapper = new JPanel(new BorderLayout());
        searchWrapper.setOpaque(false);
        searchWrapper.setBorder(BorderFactory.createEmptyBorder(1, 3, 2, 4));
        searchWrapper.setVisible(false);
        this.search = new AppearanceControlTextField();
        this.search.getDocument().addDocumentListener(Actions.documentListener(() -> refreshTreeFilter()));
        this.search.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        searchWrapper.add(this.search);
        this.searchToggle = new JToggleButton(IconUtil.of("Search24.png").imageSize(ICON_SIZE, ICON_SIZE).get());
        this.searchToggle.setMinimumSize(BUTTON_DIMENSION);
        this.searchToggle.setMaximumSize(BUTTON_DIMENSION);
        this.searchToggle.setPreferredSize(BUTTON_DIMENSION);
        this.searchToggle.setToolTipText("Filter nodes in the tree");
        this.searchToggle.setFocusPainted(false);
        this.searchToggle.addActionListener((a) ->
        {
            TreePath selectedPath = this.treeTable == null ? null : this.treeTable.getTree().getSelectionPath();
            XsdTreeNode selectedNode = selectedPath == null ? null : (XsdTreeNode) selectedPath.getLastPathComponent();
            boolean filterActive = this.searchToggle.isSelected();
            searchWrapper.setVisible(filterActive);
            refreshTreeFilter();
            rightContainer.revalidate();
            SwingUtilities.invokeLater(() -> restoreSelectionAfterFilterToggle(selectedNode, filterActive));
        });
        JPanel controlsContainer = new JPanel();
        controlsContainer.add(Box.createHorizontalStrut(4));
        controlsContainer.add(this.searchToggle);
        controlsContainer.add(Box.createHorizontalStrut(2));
        controlsContainer.add(Box.createHorizontalGlue()); // right-aligns everything
        controlsContainer.setLayout(new BoxLayout(controlsContainer, BoxLayout.X_AXIS));
        controlsContainer.setBorder(new LineBorder(null, -1));
        controlsContainer.setMinimumSize(new Dimension(240, 28));
        controlsContainer.setPreferredSize(new Dimension(300, 28));
        controlsContainer.add(new JLabel("Scenario: "));
        this.scenario = new AppearanceControlComboBox<>();
        this.scenario.addItem(new ScenarioWrapper(null));
        this.scenario.setMinimumSize(new Dimension(50, 22));
        this.scenario.setMaximumSize(new Dimension(250, 22));
        this.scenario.setPreferredSize(new Dimension(200, 22));
        this.scenario
                .addActionListener(new ScenarioActionListener(this, this.visualizationPane, this.scenario, this.evalWrapper));
        populateControls(controlsContainer);
        rightContainer.add(controlsContainer);
        rightContainer.add(searchWrapper);
        rightContainer.add(this.rightSplitPane);
        this.leftRightSplitPane.setRightComponent(rightContainer);

        setTreeUI();

        // empty tree table
        XsdTreeTableModel emptyModel = new XsdTreeTableModel(null, this::ignoreChanges, this::getEval,
                this.searchToggle::isSelected, this.search::getText);
        this.treeTable = new AppearanceControlTreeTable(emptyModel);
        emptyModel.setTreeTable(this.treeTable);
        XsdTreeTableModel.applyColumnWidth(this.treeTable);
        this.rightSplitPane.setTopComponent(new JScrollPane(this.treeTable));

        // attributes table
        DefaultTableColumnModel columns = new DefaultTableColumnModel();
        for (int i = 0; i < this.attributesTableModel.getColumnCount(); i++)
        {
            TableColumn column = new TableColumn(i);
            column.setHeaderValue(this.attributesTableModel.getColumnName(i));
            columns.addColumn(column);
        }
        this.attributesTable = new JTable(this.attributesTableModel, columns)
        {
            /** Serialization version UID. */
            private static final long serialVersionUID = 20260215L;

            @Override
            public void setBackground(final Color bg)
            {
                super.setBackground(bg);
                setGridColor(bg);
            }
        };
        this.attributesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.attributesTable.putClientProperty("terminateEditOnFocusLost", true);
        this.attributesTable.setDefaultRenderer(String.class,
                new AttributeCellRenderer(IconUtil.of("Information24.png").imageSize(16, 16).get()));
        AttributesCellEditor editor = new AttributesCellEditor(this.attributesTable, this);
        this.attributesTable.setDefaultEditor(String.class, editor);
        this.attributesTable.addMouseListener(new AttributesMouseListener(this, this.attributesTable));
        this.attributesTable.getSelectionModel()
                .addListSelectionListener(new AttributesListSelectionListener(this, this.attributesTable));
        AttributesTableModel.applyColumnWidth(this.attributesTable);
        this.rightSplitPane.setBottomComponent(new JScrollPane(this.attributesTable));
        this.actions = new Actions(this, this.attributesTable);
        this.actions.setTreeTable(this.treeTable);
        new AttributesKeyListener(this, this.attributesTable);

        addMenuBar();

        this.statusLabel = new StatusLabel();
        this.statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        this.statusLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        add(this.statusLabel, BorderLayout.SOUTH);
        removeStatusLabel();

        this.evalWrapper.addListener(() ->
        {
            if (!ignoreChanges())
            {
                SwingUtilities.invokeLater(() ->
                {
                    // because any node value or attribute may become invalid from an expression returning something invalid
                    invalidateAndRepaint();
                });
            }
        });

        pack();
    }

    /**
     * Makes the divider clickable causing the panel to exchange screen size the other way around.
     * @param pane split pane to make click-flippable.
     */
    private static void makeClickFlippable(final JSplitPane pane)
    {
        ((BasicSplitPaneUI) pane.getUI()).getDivider().addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(final MouseEvent e)
            {
                int size = pane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT ? pane.getWidth() : pane.getHeight();
                int target = size - pane.getDividerLocation();
                int minimum = pane.getMinimumDividerLocation();
                int maximum = pane.getMaximumDividerLocation();
                pane.setDividerLocation(Math.min(Math.max(target, minimum), maximum));
            }
        });
    }

    /**
     * Populates controls container.
     * @param controlsContainer controls container
     */
    private void populateControls(final JPanel controlsContainer)
    {
        controlsContainer.add(this.scenario);
        controlsContainer.add(Box.createHorizontalStrut(2));
        JButton playRun = new AppearanceControlButton(IconUtil.of("Play24.png").imageSize(ICON_SIZE, ICON_SIZE).get());
        playRun.setToolTipText("Run single run");
        playRun.setMinimumSize(BUTTON_DIMENSION);
        playRun.setMaximumSize(BUTTON_DIMENSION);
        playRun.setPreferredSize(BUTTON_DIMENSION);
        playRun.addActionListener((a) -> runSingle());
        controlsContainer.add(playRun);
        JButton playScenario = new AppearanceControlButton(IconUtil.of("Next24.png").imageSize(ICON_SIZE, ICON_SIZE).get());
        playScenario.setToolTipText("Run scenario (batch)");
        playScenario.setMinimumSize(BUTTON_DIMENSION);
        playScenario.setMaximumSize(BUTTON_DIMENSION);
        playScenario.setPreferredSize(BUTTON_DIMENSION);
        playScenario.addActionListener((a) -> runBatch(false));
        controlsContainer.add(playScenario);
        JButton playAll = new AppearanceControlButton(IconUtil.of("Step24.png").imageSize(ICON_SIZE, ICON_SIZE).get());
        playAll.setToolTipText("Run all (batch)");
        playAll.setMinimumSize(BUTTON_DIMENSION);
        playAll.setMaximumSize(BUTTON_DIMENSION);
        playAll.setPreferredSize(BUTTON_DIMENSION);
        playAll.addActionListener((a) -> runBatch(true));
        controlsContainer.add(playAll);
        controlsContainer.add(Box.createHorizontalStrut(4));
    }

    /**
     * Sets tree UI values.
     */
    private static void setTreeUI()
    {
        // There is likely a better way to do this, but setting the icons specific on the tree is impossible for collapsed and
        // expanded. Also in that case after removal of a node, the tree appearance gets reset and java default icons appear.
        // This happens to the leaf/open/closed icons that can be set on the tree. This needs to be done before the JTreeTable
        // is created, otherwise it loads normal default icons.
        UIManager.put("Tree.collapsedIcon", IconUtil.of("Collapsed24.png").imageSize(ICON_SIZE, ICON_SIZE).get());
        UIManager.put("Tree.expandedIcon", IconUtil.of("Expanded24.png").imageSize(ICON_SIZE, ICON_SIZE).get());
        UIManager.put("Tree.paintLines", false);
        UIManager.put("Tree.rightChildIndent", 7);
    }

    /**
     * Refreshes the tree model after a search filter state change.
     */
    private void refreshTreeFilter()
    {
        if (this.treeTable != null && this.treeTable.getTree().getModel() instanceof XsdTreeTableModel model)
        {
            model.refresh();
            SwingUtilities.invokeLater(() ->
            {
                XsdTreeNode root = (XsdTreeNode) model.getRoot();
                if (root != null)
                {
                    restoreExpansion(root, new TreePath(root));
                }
            });
        }
    }

    /**
     * Returns the invalid cell color.
     * @return the invalid cell color
     */
    public static Color getInvalidColor()
    {
        return PROPERTIES_STORE.getColor(INVALID_COLOR_KEY);
    }

    /**
     * Returns the expression cell color.
     * @return the expression cell color
     */
    public static Color getExpressionColor()
    {
        return PROPERTIES_STORE.getColor(EXPRESSION_COLOR_KEY);
    }

    /**
     * Returns the inactive cell color.
     * @return the inactive cell color
     */
    public static Color getInactiveColor()
    {
        return PROPERTIES_STORE.getColor(INACTIVE_COLOR_KEY);
    }

    /**
     * Run a single simulation run.
     */
    private void runSingle()
    {
        XsdTreeNodeRoot root = ((XsdTreeTableModel) this.treeTable.getTree().getModel()).getDataRoot();
        if (!root.isValid())
        {
            dialogs().showInvalidToRunMessage();
            return;
        }
        autosave(root);
        int index = this.scenario.getSelectedIndex();
        try
        {
            OtsEditor.this.evalWrapper.setDirty();
            if (OtsEditor.this.evalWrapper.getEval(OtsEditor.this.scenario.getItemAt(index)) == null)
            {
                return;
            }
        }
        catch (CircularDependencyException ex)
        {
            dialogs().showCircularInputParameters(ex.getMessage());
            return;
        }
        File file;
        try
        {
            file = File.createTempFile("ots_", ".xml");
        }
        catch (IOException exception)
        {
            dialogs().showUnableToRunFromTempFile();
            return;
        }
        save(file, root, false);

        if (index == 0)
        {
            OtsRunner.runSingle(file, null, this);
        }
        else
        {
            String selectedScenario = this.scenario.getItemAt(index).scenarioNode().getId();
            OtsRunner.runSingle(file, selectedScenario, this);
        }
        file.delete();
    }

    /**
     * Batch run.
     * @param all all scenarios, or only the selected scenario.
     */
    protected void runBatch(final boolean all)
    {
        XsdTreeNodeRoot root = ((XsdTreeTableModel) this.treeTable.getTree().getModel()).getDataRoot();
        if (!root.isValid())
        {
            dialogs().showInvalidToRunMessage();
            return;
        }
        autosave(root);
        // TODO should probably create a utility to run from XML as demo fromXML, but with batch function too
        if (all)
        {
            System.out.println("Running all.");
        }
        else
        {
            int index = this.scenario.getSelectedIndex();
            if (index == 0)
            {
                System.out.println("Running all runs of the default scenario.");
            }
            else
            {
                System.out.println("Running all runs of scenario " + this.scenario.getItemAt(index) + ".");
            }
        }
    }

    /**
     * Returns the undo unit.
     * @return undo unit.
     */
    public Undo getUndo()
    {
        return this.undo;
    }

    /**
     * Collapses the given node, if expanded.
     * @param node node
     */
    public void collapse(final XsdTreeNode node)
    {
        TreePath path = this.treeTable.getTree().getSelectionPath();
        if (this.treeTable.getTree().isExpanded(path))
        {
            actions().getNodeActions().toggleExpansion(node, path, true);
        }
    }

    /**
     * Selects the given node in the tree.
     * @param node node
     * @param attribute attribute name, may be {@code null} to just show the node
     */
    public void select(final XsdTreeNode node, final String attribute)
    {
        show(node, attribute, false);
    }

    /**
     * Clears the selection in the tree, and with that the attributes table.
     */
    public void clearSelection()
    {
        setHighlight(null);
        ((XsdTreeTableModel) this.treeTable.getTree().getModel()).clearSelection();
    }

    /**
     * Shows and selects the given node in the tree.
     * @param node node
     * @param attribute attribute name, may be {@code null} to just show the node
     */
    public void show(final XsdTreeNode node, final String attribute)
    {
        show(node, attribute, true);
    }

    /**
     * Shows and selects the given node in the tree.
     * @param node node
     * @param attribute attribute name, may be {@code null} to just show the node
     * @param scroll whether to scroll to the element
     */
    private void show(final XsdTreeNode node, final String attribute, final boolean scroll)
    {
        Object root = this.treeTable.getTree().getModel().getRoot();
        if (node.getParent() == null && node != root)
        {
            return; // trying to show node that is in collapsed part of the tree
        }
        if (this.treeTable.isEditing())
        {
            CellEditor editor = this.treeTable.getCellEditor();
            if (editor != null)
            {
                editor.stopCellEditing();
            }
        }
        if (this.attributesTable.isEditing())
        {
            CellEditor editor = this.attributesTable.getCellEditor();
            if (editor != null)
            {
                editor.stopCellEditing();
            }
        }
        List<XsdTreeNode> nodePath = node.getPath();
        int rootIndex = nodePath.indexOf(root);
        if (rootIndex < 0)
        {
            return;
        }
        if (rootIndex > 0)
        {
            nodePath = nodePath.subList(rootIndex, nodePath.size());
        }
        TreePath path = new TreePath(nodePath.toArray());
        if (scroll)
        {
            if (nodePath.size() > 1)
            {
                this.treeTable.getTree().expandPath(new TreePath(nodePath.subList(0, nodePath.size() - 1).toArray()));
            }
            this.treeTable.getTree().setSelectionPath(path);
            this.treeTable.getTree().scrollPathToVisible(path);
            Rectangle bounds = this.treeTable.getTree().getPathBounds(path);
            if (bounds == null)
            {
                return; // trying to show node that is in collapsed part of the tree
            }
            bounds.x += this.treeTable.getX();
            bounds.y += this.treeTable.getY();
            ((JComponent) this.treeTable.getParent()).scrollRectToVisible(bounds);
        }
        else if (this.treeTable.getTree().isVisible(path))
        {
            this.treeTable.getTree().setSelectionPath(path);
        }
        this.attributesTableModel.setNode(node.isActive() ? node : null);
        if (attribute != null)
        {
            int index = node.getAttributeIndexByName(attribute);
            this.attributesTable.setRowSelectionInterval(index, index);
        }
        else
        {
            this.attributesTable.getSelectionModel().clearSelection();
        }
    }

    /**
     * Highlight node.
     * @param node highlighted node, may be {@code null}
     */
    public void setHighlight(final XsdTreeNode node)
    {
        XsdTreeTableModel model = (XsdTreeTableModel) this.treeTable.getTree().getModel();
        XsdTreeNode wasRoot = (XsdTreeNode) model.getRoot();
        model.setRoot(node);
        XsdTreeNode isRoot = (XsdTreeNode) model.getRoot();
        if (!Objects.equal(wasRoot, isRoot))
        {
            restoreExpansion(isRoot, new TreePath(isRoot));
            this.attributesTableModel.setNode(null);
        }
    }

    /**
     * Returns the highlighted node, or {@code null} if no highlight.
     * @return the highlighted node, or {@code null} if no highlight
     */
    public XsdTreeNode getHighlight()
    {
        XsdTreeTableModel model = (XsdTreeTableModel) this.treeTable.getTree().getModel();
        XsdTreeNode node = (XsdTreeNode) model.getRoot();
        return node instanceof XsdTreeNodeRoot ? null : node;
    }

    /**
     * Restore node expansion while suppressing updates to persisted expansion-state flags.
     * @param node node
     * @param path path
     */
    private void restoreExpansion(final XsdTreeNode node, final TreePath path)
    {
        boolean topLevel = !this.suppressExpansionStateUpdates;
        if (topLevel)
        {
            this.suppressExpansionStateUpdates = true;
        }
        try
        {
            if (node.expanded)
            {
                this.treeTable.getTree().expandPath(path);
            }
            if (node.getChildCount() > 0)
            {
                for (XsdTreeNode child : node.getChildren())
                {
                    restoreExpansion(child, path.pathByAddingChild(child));
                }
            }
        }
        finally
        {
            if (topLevel)
            {
                this.suppressExpansionStateUpdates = false;
            }
        }
    }

    /**
     * Invalidate the entire tree and repaint both the tree and attributes table. This method may take a bit of time. Be careful
     * and conservative in invoking this method on the Swing EDT. Consider invoking with {@link SwingUtilities#invokeLater}.
     */
    public void invalidateAndRepaint()
    {
        XsdTreeNodeRoot root = ((XsdTreeTableModel) this.treeTable.getTree().getModel()).getDataRoot();
        if (root != null)
        {
            root.invalidateAll();
        }
        this.treeTable.repaint();
        this.attributesTable.repaint();
    }

    /**
     * Sets a status label.
     * @param label status label.
     */
    public void setStatusLabel(final String label)
    {
        this.statusLabel.setText(label);
    }

    /**
     * Removes the status label.
     */
    public void removeStatusLabel()
    {
        this.statusLabel.setText(" ");
    }

    /**
     * Adds the menu bar.
     */
    private void addMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        JMenuItem newFile = new JMenuItem("New");
        newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        fileMenu.add(newFile);
        newFile.addActionListener((a) -> newFile());
        JMenuItem open = new JMenuItem("Open...");
        open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        fileMenu.add(open);
        open.addActionListener((a) -> openFile());
        this.recentFilesMenu = new JMenu("Recent files");
        updateRecentFileMenu();
        fileMenu.add(this.recentFilesMenu);
        JMenuItem save = new JMenuItem("Save");
        save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        fileMenu.add(save);
        save.addActionListener((a) -> saveFile());
        JMenuItem saveAs = new JMenuItem("Save as...");
        fileMenu.add(saveAs);
        saveAs.addActionListener(
                (a) -> saveFileAs(((XsdTreeTableModel) OtsEditor.this.treeTable.getTree().getModel()).getDataRoot()));
        fileMenu.add(new JSeparator());
        JMenuItem propertiesItem = new JMenuItem("Properties...");
        fileMenu.add(propertiesItem);
        propertiesItem.addActionListener((a) -> new PropertiesDialog(this, this.properties, this.questionIcon));
        fileMenu.add(new JSeparator());
        JMenuItem exit = new JMenuItem("Exit");
        fileMenu.add(exit);
        exit.addActionListener((a) -> exit());

        JMenu editMenu = new JMenu("Edit");
        menuBar.add(editMenu);
        JMenuItem undoItem = new JMenuItem("Undo");
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
        editMenu.add(undoItem);
        undoItem.addActionListener((a) -> OtsEditor.this.undo.undo());
        JMenuItem redoItem = new JMenuItem("Redo");
        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
        editMenu.add(redoItem);
        redoItem.addActionListener((a) -> OtsEditor.this.undo.redo());
        this.undo = new Undo(this, undoItem, redoItem);

        JMenu navigateMenu = new JMenu("Navigate");
        menuBar.add(navigateMenu);
        navigateMenu.add(new JMenuItem(this.navigation.getGoBackAction()));
        navigateMenu.add(new JMenuItem(this.navigation.getGotoCoupledNodeAction()));
    }

    /**
     * Updates the recent file menu.
     */
    private void updateRecentFileMenu()
    {
        this.recentFilesMenu.removeAll();
        List<String> files = PROPERTIES_STORE.getList(RECENT_FILES_KEY);
        if (!files.isEmpty())
        {
            for (String file : files)
            {
                JMenuItem item = new JMenuItem(file);
                item.addActionListener((i) ->
                {
                    if (!this.unsavedChanges || dialogs().confirmDiscardChanges())
                    {
                        Path path;
                        try
                        {
                            path = Path.of(file).normalize().toAbsolutePath();
                        }
                        catch (InvalidPathException exception)
                        {
                            if (dialogs().confirmRemoveRecentFile())
                            {
                                PROPERTIES_STORE.removeFromList(RECENT_FILES_KEY, file);
                                updateRecentFileMenu();
                            }
                            return;
                        }
                        if (loadFile(path.toFile(), "File loaded"))
                        {
                            setCurrentFile(path);
                        }
                        else if (dialogs().confirmRemoveRecentFile())
                        {
                            PROPERTIES_STORE.removeFromList(RECENT_FILES_KEY, file);
                            updateRecentFileMenu();
                        }
                    }
                });
                this.recentFilesMenu.add(item);
            }
            this.recentFilesMenu.add(new JSeparator());
        }
        JMenuItem item = new JMenuItem("Clear history");
        item.setEnabled(!files.isEmpty());
        item.addActionListener((i) ->
        {
            if (dialogs().confirmClearRecentFiles())
            {
                PROPERTIES_STORE.clearProperty(RECENT_FILES_KEY);
                updateRecentFileMenu();
            }
        });
        this.recentFilesMenu.add(item);
    }

    /**
     * Sets coupled node from user action, i.e. the node that contains the key value to which a user selected node with keyref
     * refers to.
     * @param toNode key node that is coupled to from a keyref node, may be {@code null}.
     * @param fromNode keyref node that is coupled from to a key node, may be {@code null}.
     * @param fromAttribute attribute in keyref node that refers to coupled node, may be {@code null}.
     */
    public void setCoupledNode(final XsdTreeNode toNode, final XsdTreeNode fromNode, final String fromAttribute)
    {
        this.navigation.setCoupledNode(toNode, fromNode, fromAttribute);
    }

    /**
     * Sets whether there are unsaved changes, resulting in a * in the window name, and confirmation pop-ups upon file changes.
     * @param unsavedChanges whether there are unsaved changes.
     */
    public void setUnsavedChanges(final boolean unsavedChanges)
    {
        if (this.ignoreChanges)
        {
            return;
        }
        this.unsavedChanges = unsavedChanges;
        StringBuilder title = new StringBuilder("OTS | The Open Traffic Simulator | Editor");
        if (this.lastPath != null)
        {
            title.append(" (").append(this.lastPath).append(")");
        }
        if (this.unsavedChanges)
        {
            title.append(" *");
        }
        setTitle(title.toString());
    }

    /**
     * Sets a new schema in the GUI.
     * @param xsdDocument main node from an XSD schema file.
     * @throws IOException when a resource could not be loaded.
     */
    @SuppressWarnings("checkstyle:hiddenfield")
    public void setSchema(final Document xsdDocument) throws IOException
    {
        this.xsdDocument = xsdDocument;
        this.undo.setIgnoreChanges(true);
        initializeTree();
        this.undo.clear();
        setStatusLabel("Schema " + xsdDocument.getBaseURI() + " loaded");
        setVisible(true);
        this.leftRightSplitPane.setDividerLocation(0.65);
        this.rightSplitPane.setDividerLocation(0.75);
        setAppearance(getAppearance());
        SwingUtilities.invokeLater(() -> checkAutosave());
    }

    /**
     * Checks for "autosave*.xml" files in the temporary directory.
     */
    private void checkAutosave()
    {
        Path tmpPath = autosaveDirectory();
        try
        {
            Files.createDirectories(tmpPath);
        }
        catch (IOException ioe)
        {
            // skip presenting user with autosave, but also do not delete file
            return;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(tmpPath, "autosave*.xml"))
        {
            Iterator<Path> it = stream.iterator();
            if (it.hasNext())
            {
                File file = it.next().toFile();
                Path source = autosaveSource();
                URI basePath = source == null ? null : source.toUri();
                Optional<Boolean> userInput = dialogs().confirmLoadAutosaveFile(file);
                if (userInput.isPresent() && userInput.get())
                {
                    boolean loaded;
                    try
                    {
                        loaded = loadFile(DocumentResource.of(file), "Autosave file loaded", basePath);
                    }
                    catch (FileNotFoundException ex)
                    {
                        loaded = false;
                    }
                    if (!loaded)
                    {
                        if (dialogs().confirmRemoveAutosaveFile())
                        {
                            file.delete();
                            PROPERTIES_STORE.clearProperty(AUTOSAVE_SOURCE_KEY);
                        }
                        return;
                    }
                    if (source != null)
                    {
                        setCurrentFile(source);
                    }
                    setUnsavedChanges(true);
                    file.delete();
                    PROPERTIES_STORE.clearProperty(AUTOSAVE_SOURCE_KEY);
                }
                else if (userInput.isPresent() && !userInput.get())
                {
                    file.delete();
                    PROPERTIES_STORE.clearProperty(AUTOSAVE_SOURCE_KEY);
                }
            }
        }
        catch (IOException ioe)
        {
            // skip presenting user with autosave, but also do not delete file
        }
    }

    /**
     * Returns the directory used for autosave files.
     * @return directory used for autosave files
     */
    private static Path autosaveDirectory()
    {
        return Path.of(System.getProperty("java.io.tmpdir"), "ots");
    }

    /**
     * Returns the source file of the current autosave, used as base for relative includes.
     * @return source file of the current autosave, or {@code null} if unknown or invalid
     */
    private static Path autosaveSource()
    {
        String source = PROPERTIES_STORE.getProperty(AUTOSAVE_SOURCE_KEY);
        if (source == null || source.isBlank())
        {
            return null;
        }
        try
        {
            return Path.of(source).normalize().toAbsolutePath();
        }
        catch (InvalidPathException exception)
        {
            return null;
        }
    }

    /**
     * Asks for confirmation to discard unsaved changes, if any, and initializes the tree.
     */
    private void newFile()
    {
        if (!this.unsavedChanges || dialogs().confirmDiscardChanges())
        {
            try
            {
                this.undo.setIgnoreChanges(true);
                this.lastPath = null;
                initializeTree();
                this.undo.clear();
            }
            catch (IOException exception)
            {
                dialogs().notification("Unable to reload schema.");
            }
        }
    }

    /**
     * Initializes the tree based on the XSD schema.
     * @throws IOException when a resource can not be loaded.
     */
    private void initializeTree() throws IOException
    {
        this.scenario.removeAllItems();
        this.scenario.addItem(new ScenarioWrapper(null));
        setDefaultProperties();
        this.attributesTableModel.setNode(null);

        // tree table
        XsdTreeTableModel treeModel = new XsdTreeTableModel(this.xsdDocument, this::ignoreChanges, this::getEval,
                this.searchToggle::isSelected, this.search::getText);
        this.treeTable = new AppearanceControlTreeTable(treeModel);
        treeModel.setTreeTable(this.treeTable);
        this.treeTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.actions = new Actions(this, this.attributesTable);
        this.actions.setTreeTable(this.treeTable);
        this.treeTable.putClientProperty("terminateEditOnFocusLost", true);
        this.treeTable.setDefaultRenderer(String.class, new StringCellRenderer(this.treeTable));
        ((DefaultCellEditor) this.treeTable.getDefaultEditor(String.class)).setClickCountToStart(1);
        XsdTreeTableModel.applyColumnWidth(this.treeTable);
        // sets custom icon and appends the choice icon for choice nodes
        this.treeTable.getTree().setCellRenderer(new XsdTreeCellRenderer(this));
        this.treeTable.addMouseListener(new MouseAdapter()
        {
            // this listener forwards the mouse event so something can respond to ctrl or shift, and not only selection itself
            @Override
            public void mousePressed(final MouseEvent e)
            {
                XsdTreeNode node = getTreeNodeAtPoint(e.getPoint());
                // if attributesTable is editing Swing sees this as a stop-editing event; selection in table will not change
                if (node != null && !OtsEditor.this.attributesTable.isEditing())
                {
                    fireEvent(MOUSE_EVENT, new Object[] {node, e});
                }
            }
        });
        this.treeTable.getTree().addTreeExpansionListener(new TreeExpansionListener()
        {
            @Override
            public void treeExpanded(final TreeExpansionEvent event)
            {
                boolean filterActive = OtsEditor.this.treeTable.getTree().getModel() instanceof XsdTreeTableModel model
                        && model.isFilterActive();
                if (!OtsEditor.this.suppressExpansionStateUpdates && !filterActive)
                {
                    ((XsdTreeNode) event.getPath().getLastPathComponent()).expanded = true;
                }
            }

            @Override
            public void treeCollapsed(final TreeExpansionEvent event)
            {
                boolean filterActive = OtsEditor.this.treeTable.getTree().getModel() instanceof XsdTreeTableModel model
                        && model.isFilterActive();
                if (!OtsEditor.this.suppressExpansionStateUpdates && !filterActive)
                {
                    ((XsdTreeNode) event.getPath().getLastPathComponent()).expanded = false;
                }
            }
        });

        // this listener changes Id or node Value for each key being pressed
        // this listener starts a new undo event when the editor gets focus on the JTreeTable
        // this listener may cause new undo actions when cells are navigated using the keyboard
        new XsdTreeEditorListener(this, this.treeTable);

        // throws selection events and updates the attributes table
        // this listener will make sure no choice pop-up is presented by a left-click on expand/collapse, even for a choice node
        // this listener makes sure that a choice pop-up can be presented again after a left-click on an expansion/collapse node
        // it also shows the tooltip in tree nodes
        // this listener opens the attributes of a node, and presents the pop-up for a choice or for addition/deletion of nodes
        new XsdTreeListener(this, this.treeTable, this.attributesTable);

        // listener to keyboard shortcuts and key events that should start (i.e. end previous) undo actions
        new XsdTreeKeyListener(this, this.treeTable);

        int dividerLocation = this.rightSplitPane.getDividerLocation();
        this.rightSplitPane.setTopComponent(new JScrollPane(this.treeTable));
        this.rightSplitPane.setDividerLocation(dividerLocation);

        final XsdTreeNodeRoot root = treeModel.getDataRoot();
        EventListener listener = new ChangesListener(this, this.scenario);
        root.addListener(listener, XsdTreeNodeRoot.NODE_CREATED);
        root.addListener(listener, XsdTreeNodeRoot.NODE_REMOVED);
        fireEvent(NEW_FILE, root);

        setUnsavedChanges(false);
        if (this.autosave != null)
        {
            this.autosave.shutdown();
        }
        this.autosave = Executors.newSingleThreadScheduledExecutor((runnable) -> new Thread(runnable, "autosave-scheduler"));
        long autisaveMs = PROPERTIES_STORE.getInteger(AUTOSAVE_MS_KEY);
        long request = this.autosaveRequest.incrementAndGet();
        this.autosave.scheduleAtFixedRate(() ->
        {
            if (request != this.autosaveRequest.get())
            {
                return;
            }
            OtsEditor.this.autosave(root);
        }, autisaveMs, autisaveMs, TimeUnit.MILLISECONDS);
        setAppearance(getAppearance()); // because of new AppearanceControlTreeTable
    }

    /**
     * Auto-save current state.
     * @param root root node
     */
    private void autosave(final XsdTreeNodeRoot root)
    {
        if (OtsEditor.this.unsavedChanges)
        {
            setStatusLabel("Autosaving...");
            Path dir = autosaveDirectory();
            try
            {
                Files.createDirectories(dir);
            }
            catch (IOException exception)
            {
                return;
            }
            String name = this.lastPath == null ? "autosave.xml" : "autosave_" + this.lastPath.getFileName();
            File file = dir.resolve(name).toFile();
            if (save(file, root, false))
            {
                PROPERTIES_STORE.setProperty(AUTOSAVE_SOURCE_KEY, this.lastPath == null ? "" : this.lastPath.toString());
                file.deleteOnExit();
                setStatusLabel("Autosaved");
            }
            else
            {
                setStatusLabel("Autosave failed");
            }
        }
    }

    /**
     * Sets the default properties.
     */
    void setDefaultProperties()
    {
        this.properties.clear();
        this.properties.add("xmlns:ots");
        this.properties.add("http://www.opentrafficsim.org/ots");
        this.properties.add("xmlns:xi");
        this.properties.add("http://www.w3.org/2001/XInclude");
        this.properties.add("xmlns:xsi");
        this.properties.add("http://www.w3.org/2001/XMLSchema-instance");
        this.properties.add("xsi:schemaLocation");
        this.properties.add(null);
    }

    /**
     * Creates a new undo action as the selection is changed in the tree table, editing is stopped, or focus is gained/lost.
     */
    public void startUndoActionOnTreeTable()
    {
        // allow selection to update after any events triggering this
        // SwingUtilities.invokeLater(() ->
        // {
        XsdTreeNode node = (XsdTreeNode) this.treeTable.getValueAt(this.treeTable.getSelectedRow(),
                this.treeTable.convertColumnIndexToView(XsdTreeTableModel.TREE_COLUMN)); // columns may have been moved
        int col = this.treeTable.convertColumnIndexToModel(this.treeTable.getSelectedColumn());
        if (col == XsdTreeTableModel.ID_COLUMN)
        {
            this.undo.startAction(ActionType.ID_CHANGE, node, null);
        }
        else if (col == XsdTreeTableModel.VALUE_COLUMN)
        {
            this.undo.startAction(ActionType.VALUE_CHANGE, node, null);
        }
        // });
    }

    /**
     * Returns the XsdTreeNode of the row under the given point (from a mouse or key event).
     * @param point point (from an event)
     * @return the XsdTreeNode of the row under the given point
     */
    public XsdTreeNode getTreeNodeAtPoint(final Point point)
    {
        int row = this.treeTable.rowAtPoint(point);
        int col = this.treeTable.convertColumnIndexToView(XsdTreeTableModel.TREE_COLUMN); // columns may have been moved
        return (XsdTreeNode) this.treeTable.getValueAt(row, col);
    }

    /**
     * Sets a custom icon for nodes that comply to the path. The path may be an absolute path (e.g. "Ots.Network.Connector") or
     * a relative path (e.g. ".Node").
     * @param path path.
     * @param icon image icon.
     */
    public void setCustomIcon(final String path, final Icon icon)
    {
        this.customIcons.put(path, icon);
    }

    /**
     * Obtains a custom icon for the path.
     * @param path node path.
     * @return custom icon, empty if there is no custom icon specified for the path.
     */
    public Optional<Icon> getCustomIcon(final String path)
    {
        Icon icon = this.customIcons.get(path);
        if (icon != null)
        {
            return Optional.of(icon);
        }
        for (Entry<String, Icon> entry : this.customIcons.entrySet())
        {
            if (path.endsWith(entry.getKey()))
            {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the node that is the currently selected choice.
     * @return node that is the currently selected choice.
     */
    public XsdTreeNode getChoiceNode()
    {
        return this.choiceNode;
    }

    /**
     * Sets the node that is the currently selected choice.
     * @param choiceNode node that is the currently selected choice.
     */
    public void setChoiceNode(final XsdTreeNode choiceNode)
    {
        this.choiceNode = choiceNode;
    }

    /**
     * Sets whether the choice menu may appear.
     * @param mayPresentChoice whether the choice menu may appear
     */
    public void setMayPresentChoice(final boolean mayPresentChoice)
    {
        this.mayPresentChoice = mayPresentChoice;
    }

    /**
     * Returns whether a choice may be presented.
     * @return whether a choice may be presented.
     */
    public boolean mayPresentChoice()
    {
        return this.mayPresentChoice;
    }

    @Override
    public EventListenerMap getEventListenerMap()
    {
        return this.listenerMap;
    }

    /**
     * Adds a tab to the main window.
     * @param name name of the tab.
     * @param icon icon for the tab, may be {@code null}.
     * @param component component that will fill the tab.
     * @param tip tool-tip for the tab, may be {@code null}.
     */
    public void addTab(final String name, final Icon icon, final Component component, final String tip)
    {
        this.visualizationPane.addTab(name, icon, component, tip);
    }

    /**
     * Returns the component of the tab with given name.
     * @param name name of the tab.
     * @return component of the tab with given name or empty if no such tab
     */
    public Optional<Component> getTab(final String name)
    {
        for (int index = 0; index < this.visualizationPane.getTabCount(); index++)
        {
            if (this.visualizationPane.getTitleAt(index).equals(name))
            {
                return Optional.of(this.visualizationPane.getComponentAt(index));
            }
        }
        return Optional.empty();
    }

    /**
     * Place focus on the tab with given name.
     * @param name name of the tab.
     */
    public void focusTab(final String name)
    {
        for (int index = 0; index < this.visualizationPane.getTabCount(); index++)
        {
            if (this.visualizationPane.getTitleAt(index).equals(name))
            {
                this.visualizationPane.setSelectedIndex(index);
            }
        }
    }

    /**
     * Returns whether the tab with given name is currently selected.
     * @param name name of the tab.
     * @return whether the tab with given name is currently selected.
     */
    public boolean isFocusedTab(final String name)
    {
        for (int index = 0; index < this.visualizationPane.getTabCount(); index++)
        {
            if (this.visualizationPane.getTitleAt(index).equals(name))
            {
                return this.visualizationPane.getSelectedIndex() == index;
            }
        }
        return false;
    }

    /**
     * Registers an action that is invoked when the given tab loses focus. The action is invoked before any other
     * tab change handling, independent of the order in which tabs were added.
     * @param tabName name of the tab.
     * @param action action to invoke.
     */
    public void addTabReleaseAction(final String tabName, final Runnable action)
    {
        this.tabReleaseActions.computeIfAbsent(tabName, (n) -> new ArrayList<>()).add(action);
    }

    /**
     * Registers an action that is invoked when the given tab is selected. The action is invoked after release actions
     * for the previous tab and before any other tab change handling.
     * @param tabName name of the tab.
     * @param action action to invoke.
     */
    public void addTabSelectAction(final String tabName, final Runnable action)
    {
        this.tabSelectActions.computeIfAbsent(tabName, (n) -> new ArrayList<>()).add(action);
    }

    /**
     * Runs the given tab actions.
     * @param acts actions, may be {@code null}.
     */
    private void runTabActions(final List<Runnable> acts)
    {
        if (acts != null)
        {
            for (Runnable action : new ArrayList<>(acts)) // an action may register another action
            {
                action.run();
            }
        }
    }

    /**
     * Return utility for dialogs.
     * @return utility for dialogs
     */
    public Dialogs dialogs()
    {
        return this.dialogs;
    }

    /**
     * Places a popup with value options under the cell that is being clicked in a table (tree or attributes). The popup will
     * show items relevant to what is being typed in the cell. The maximum number of items shown is limited to the
     * {@code max_dropdown_items} app property.
     * @param allOptions list of all options, will be filtered when typing
     * @param table table, will be either the tree table or the attributes table
     * @param action action to perform based on the option in the popup that was selected
     * @param currentValue current value
     */
    public void valueOptionsPopup(final List<String> allOptions, final JTable table, final Consumer<String> action,
            final String currentValue)
    {
        // remove any previous listening
        if (this.fieldListener != null)
        {
            this.fieldListener.remove();
        }

        // initially no filtering on current value; this allows a quick reset to possible values
        List<String> options = filterOptions(allOptions, null);
        if (options.isEmpty())
        {
            return;
        }
        this.dropdownIndent = 0;
        this.dropdownOptions = options;

        // create pop-up
        JPopupMenu popup = new JPopupMenu();
        int index = 0;
        boolean includeRemove = currentValue != null && !currentValue.isEmpty();
        int maxDropdown = PROPERTIES_STORE.getInteger(MAX_DROPDOWN_ITEMS_KEY) - (includeRemove ? 1 : 0);
        for (String option : this.dropdownOptions)
        {
            JMenuItem item = new JMenuItem(option);
            item.setName(option);
            item.setVisible(index++ < maxDropdown);
            item.addActionListener(new PopupValueSelectedListener(option, table, action));
            item.setFont(table.getFont());
            popup.add(item);
        }
        if (includeRemove)
        {
            JMenuItem item = new JMenuItem(PopupValueSelectedListener.REMOVE_OPTION);
            item.setName(PopupValueSelectedListener.REMOVE_OPTION);
            item.setVisible(true);
            item.addActionListener(new PopupValueSelectedListener(PopupValueSelectedListener.REMOVE_OPTION, table, action));
            item.setFont(table.getFont());
            popup.add(new JPopupMenu.Separator());
            item.setIcon(PopupValueSelectedListener.REMOVE_ICON);
            popup.add(item);
        }
        JTextField field = (JTextField) ((DefaultCellEditor) table.getDefaultEditor(String.class)).getComponent();
        showOptionsInScope(popup, includeRemove, getPattern(currentValue));

        // scrolling
        popup.addMouseWheelListener(new MouseWheelListener()
        {
            @Override
            public void mouseWheelMoved(final MouseWheelEvent e)
            {
                OtsEditor.this.dropdownIndent += e.getUnitsToScroll();
                OtsEditor.this.dropdownIndent = OtsEditor.this.dropdownIndent < 0 ? 0 : OtsEditor.this.dropdownIndent;
                int maxIndent = OtsEditor.this.dropdownOptions.size() - maxDropdown - 1;
                if (maxIndent > 0)
                {
                    OtsEditor.this.dropdownIndent =
                            OtsEditor.this.dropdownIndent > maxIndent ? maxIndent : OtsEditor.this.dropdownIndent;
                    showOptionsInScope(popup, includeRemove, getPattern(field.getText()));
                    popup.pack();
                    popup.setVisible(true);
                    field.requestFocus();
                }
            }
        });

        // typing
        DocumentListener documentListener = Actions.documentListener(() ->
        {
            if (!table.isEditing())
            {
                // the pop-up should not be made visible if the editor is in a non-editing transient mode
                return;
            }
            OtsEditor.this.dropdownIndent = 0;
            Pattern pattern = getPattern(field.getText());
            OtsEditor.this.dropdownOptions = filterOptions(allOptions, pattern);
            boolean anyVisible = showOptionsInScope(popup, includeRemove, pattern);
            if (!anyVisible)
            {
                popup.setVisible(false);
                field.requestFocus();
                return;
            }
            popup.pack();
            popup.setVisible(true);
            Rectangle rectangle = field.getBounds();
            placePopup(popup, rectangle, table);
            field.requestFocus();
        });
        this.fieldListener = new FieldListener(documentListener, popup, field);

        // place the pop-up
        SwingUtilities.invokeLater(() ->
        {
            Rectangle rectangle = field.getBounds();
            if (rectangle.getWidth() == 0 || rectangle.getHeight() == 0)
            {
                /*
                 * First time requesting a pop-up on this table while user was editing in the other table. State of the field is
                 * not correct so we don't know where to place the pop-up. Should rarely happen, let's suppress pop-up.
                 */
                return;
            }
            table.setComponentPopupMenu(popup);
            popup.setMinimumSize(popup.getSize());
            popup.pack();
            popup.setInvoker(table);
            popup.setVisible(true);
            field.requestFocus();
            placePopup(popup, rectangle, table);
        });

        // prepare removal
        preparePopupRemoval(popup, table);
    }

    /**
     * Adds a listener to a pop-up to remove the pop-up from the component when the pop-up becomes invisible. This makes sure
     * that a right-click on another location that should show a different pop-up, is not overruled by the pop-up of a previous
     * click.
     * @param popup pop-up menu
     * @param table component from which the menu will be removed
     */
    public void preparePopupRemoval(final JPopupMenu popup, final JTable table)
    {
        popup.addPopupMenuListener(new PopupMenuListener()
        {
            @Override
            public void popupMenuWillBecomeVisible(final PopupMenuEvent e)
            {
            }

            @Override
            public void popupMenuWillBecomeInvisible(final PopupMenuEvent e)
            {
                OtsEditor.this.choiceNode = null;
                table.setComponentPopupMenu(null);
            }

            @Override
            public void popupMenuCanceled(final PopupMenuEvent e)
            {
            }
        });
    }

    /**
     * Returns a matching pattern based on the current value of the field.
     * @param currentValue current value
     * @return matching pattern based on the current value of the field
     */
    private Pattern getPattern(final String currentValue)
    {
        return currentValue == null || currentValue.isBlank() ? null
                : Pattern.compile(Pattern.quote(currentValue), Pattern.CASE_INSENSITIVE);
    }

    /**
     * Filter options for pop-up, leaving only those that match the pattern.
     * @param options options to filter
     * @param pattern pattern to find
     * @return filtered options.
     */
    private static List<String> filterOptions(final List<String> options, final Pattern pattern)
    {
        return options.stream().filter((val) -> pattern == null || pattern.matcher(val).find()).distinct().sorted()
                .collect(Collectors.toList());
    }

    /**
     * Updates the options that are shown within a pop-up menu based on an indent from scrolling, highlight anything that
     * matches the pattern, and set scroll icon when appropriate.
     * @param popup pop-up menu
     * @param includeRemove whether to include a remove option
     * @param pattern pattern to highlight
     * @return whether at least one item is visible
     */
    private boolean showOptionsInScope(final JPopupMenu popup, final boolean includeRemove, final Pattern pattern)
    {
        int optionIndex = 0;
        int maxDropdown = PROPERTIES_STORE.getInteger(MAX_DROPDOWN_ITEMS_KEY) - (includeRemove ? 1 : 0);
        JMenuItem first = null;
        JMenuItem last = null;
        boolean anyAbove = false;
        boolean anyBelow = false;

        // set components visible depending on their index in the full list and the current indent
        for (Component component : popup.getComponents())
        {
            if (component instanceof JMenuItem item)
            {
                if (includeRemove && PopupValueSelectedListener.REMOVE_OPTION.equals(item.getName()))
                {
                    // ignore the remove item
                    continue;
                }
                if (pattern != null)
                {
                    // match pattern, make it HTML safe, and but it between bold tags
                    Matcher matcher = pattern.matcher(item.getName());
                    StringBuffer stringBuffer = new StringBuffer();
                    while (matcher.find())
                    {
                        matcher.appendReplacement(stringBuffer, "<b>" + matcher.group().replace("&", "&amp;")
                                .replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;") + "</b>");
                    }
                    matcher.appendTail(stringBuffer);
                    if (stringBuffer.length() > 0)
                    {
                        item.setText("<html>" + stringBuffer.toString() + "</html>");
                    }
                    else
                    {
                        item.setText(item.getName());
                    }
                }
                else
                {
                    item.setText(item.getName());
                }
                int index = this.dropdownOptions.indexOf(item.getName());
                boolean isAbove = index < this.dropdownIndent;
                boolean isBelow = optionIndex > maxDropdown;
                boolean visible = (!isAbove && !isBelow);
                anyAbove |= (isAbove && index >= 0);
                anyBelow |= (isBelow && index >= 0);
                item.setVisible(visible);
                if (visible)
                {
                    if (first == null)
                    {
                        first = item;
                    }
                    // last before possible remove option
                    item.setIcon(null);
                    last = item;
                    optionIndex++;
                }
            }
            else
            {
                // JPopupMenu.Separator above remove option
                component.setVisible(includeRemove);
            }
        }

        // set icons on first and last item that we added, if there are items above or below
        if (anyAbove)
        {
            first.setIcon(PopupValueSelectedListener.UP_ICON);
            if (!anyBelow)
            {
                last.setIcon(PopupValueSelectedListener.EDGE_ICON);
            }
        }
        if (anyBelow)
        {
            last.setIcon(PopupValueSelectedListener.DOWN_ICON);
            if (!anyAbove)
            {
                first.setIcon(PopupValueSelectedListener.EDGE_ICON);
            }
        }
        return optionIndex > 0;
    }

    /**
     * Places a pop-up either below or above a given rectangle, based on surrounding space in the window.
     * @param popup pop-up
     * @param rectangle rectangle of cell being edited, relative to the parent component
     * @param parent component containing the cell
     */
    private void placePopup(final JPopupMenu popup, final Rectangle rectangle, final JComponent parent)
    {
        Point pAttributes = parent.getLocationOnScreen();
        // cannot use screen size in case of multiple monitors, so we keep the pop-up on the JFrame rather than the window
        Dimension windowSize = OtsEditor.this.getSize();
        Point pWindow = OtsEditor.this.getLocationOnScreen();
        if (pAttributes.y + (int) rectangle.getMaxY() + popup.getBounds().getHeight() > windowSize.height + pWindow.y - 1)
        {
            // above
            popup.setLocation(pAttributes.x + (int) rectangle.getMinX(),
                    pAttributes.y + (int) rectangle.getMinY() - 1 - (int) popup.getBounds().getHeight());
        }
        else
        {
            // below
            popup.setLocation(pAttributes.x + (int) rectangle.getMinX(), pAttributes.y + (int) rectangle.getMaxY() - 1);
        }
    }

    /**
     * Asks for confirmation to discard unsaved changes, if any, and show a dialog to open a file.
     */
    void openFile()
    {
        if (this.unsavedChanges && !dialogs().confirmDiscardChanges())
        {
            return;
        }
        FileDialog fileDialog = new FileDialog(this, "Open XML", FileDialog.LOAD);
        if (this.lastPath != null && this.lastPath.getParent() != null)
        {
            try
            {
                fileDialog.setDirectory(this.lastPath.getParent().toString());
            }
            catch (RuntimeException exception)
            {
                // fall back to the platform default directory
            }
        }
        fileDialog.setFilenameFilter((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".xml"));
        fileDialog.setVisible(true);
        Path path = resolveDialogPath(fileDialog);
        if (path == null)
        {
            return;
        }
        if (!path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".xml"))
        {
            dialogs().notification("Only XML files can be opened.", "Unable to open file");
            return;
        }
        if (!Files.isRegularFile(path) || !Files.isReadable(path))
        {
            dialogs().notification("The file cannot be read.", "Unable to open file");
            return;
        }
        if (loadFile(path.toFile(), "File loaded"))
        {
            setCurrentFile(path);
        }
        else
        {
            dialogs().notification("Unable to read file.", "Unable to open file");
        }
    }

    /**
     * Resolves the path selected in a file dialog. Deals with a missing directory and invalid path strings.
     * @param fileDialog file dialog from which a file was selected
     * @return selected, normalized and absolute path, or {@code null} if no file was selected or the path is invalid
     */
    private Path resolveDialogPath(final FileDialog fileDialog)
    {
        String fileName = fileDialog.getFile();
        if (fileName == null)
        {
            return null;
        }
        try
        {
            String directory = fileDialog.getDirectory();
            Path path = directory == null || directory.isBlank() ? Path.of(fileName)
                    : Path.of(directory).resolve(fileName);
            return path.normalize().toAbsolutePath();
        }
        catch (InvalidPathException exception)
        {
            dialogs().notification("Invalid file path.", "Invalid path");
            return null;
        }
    }

    /**
     * Sets the file that is currently open, and updates the window title.
     * @param path file that is currently open
     */
    private void setCurrentFile(final Path path)
    {
        this.lastPath = path.normalize().toAbsolutePath();
        setUnsavedChanges(this.unsavedChanges);
    }

    /**
     * Whether to ignore changes during loading.
     * @return whether to ignore changes during loading
     */
    public boolean ignoreChanges()
    {
        return this.ignoreChanges;
    }

    /**
     * Load file. On success it is stored in recent files, and the document URI is used as base for relative includes.
     * @param file file to load
     * @param postLoadStatus status message in status bar to show after loading
     * @return whether the file was successfully loaded
     */
    private boolean loadFile(final File file, final String postLoadStatus)
    {
        try
        {
            boolean loaded = loadFile(DocumentResource.of(file), postLoadStatus, file.toURI());
            if (loaded)
            {
                PROPERTIES_STORE.addToList(RECENT_FILES_KEY, file.toPath().normalize().toAbsolutePath().toString(),
                        PROPERTIES_STORE.getInteger(MAX_RECENT_FILES_KEY));
                updateRecentFileMenu();
            }
            return loaded;
        }
        catch (FileNotFoundException exception)
        {
            return false;
        }
    }

    /**
     * Load file. This file may come from a place in to which the file cannot be saved. Although includes are imported, no
     * directory is remembered from which the file came. The user will have to save any changes elsewhere, at which point
     * imports may no longer work. If includes use {@code builtin:}, such as {@code builtin:gtutypes} the editor will recognize
     * a default resource which can be imported if a file is not saved, or wherever it is saved.
     * <p>
     * This method should be called using {@link SwingUtilities#invokeLater} to prevent concurrent modification of Swing.
     * @param resource resource
     * @throws IOException if the resource cannot be loaded
     */
    public void preLoadFile(final ResourceHandle resource) throws IOException
    {
        loadFile(new DocumentResource(resource.openStream(), resource.asUri().toString()), "", resource.asUri());
        this.lastPath = null;
        this.setUnsavedChanges(true);
    }

    /**
     * Load file.
     * @param documentResource document resource
     * @param postLoadStatus status message in status bar to show after loading
     * @param basePath base URI against which relative includes are resolved, typically the document URI
     * @return whether the file was successfully loaded
     */
    private boolean loadFile(final DocumentResource documentResource, final String postLoadStatus, final URI basePath)
    {
        try
        {
            Document document = DocumentReader.open(documentResource);
            this.undo.setIgnoreChanges(true);
            this.evalWrapper.setIgnoreEval(true);
            this.ignoreChanges = true;
            initializeTree();
            // load main tag xml properties that are outside of XML OTS specification, so they can remain in a saved file
            NamedNodeMap attributes = document.getFirstChild().getAttributes();
            for (int i = 0; i < attributes.getLength(); i++)
            {
                if (this.properties.contains(attributes.item(i).getNodeName()))
                {
                    int index = this.properties.indexOf(attributes.item(i).getNodeName());
                    this.properties.set(index, attributes.item(i).getNodeName());
                    this.properties.set(index + 1, attributes.item(i).getNodeValue());
                }
                else
                {
                    this.properties.add(attributes.item(i).getNodeName());
                    this.properties.add(attributes.item(i).getNodeValue());
                }
            }
            XsdTreeNodeRoot root = ((XsdTreeTableModel) OtsEditor.this.treeTable.getTree().getModel()).getDataRoot();
            root.setBaseUri(basePath);
            root.loadXmlNodes(document.getFirstChild());
            this.evalWrapper.setIgnoreEval(false);
            this.ignoreChanges = false;
            this.undo.clear();
            setUnsavedChanges(false);
            setStatusLabel(postLoadStatus);
            this.undo.updateButtons();
            this.navigation.clear();
            // knowing/changing the directory may change validation status through imports
            SwingUtilities.invokeLater(() -> ((XsdTreeTableModel) this.treeTable.getTree().getModel()).refresh());
            return true;
        }
        catch (SAXException | IOException | ParserConfigurationException exception)
        {
            this.evalWrapper.setIgnoreEval(false);
            this.ignoreChanges = false;
            SwingUtilities.invokeLater(() -> ((XsdTreeTableModel) this.treeTable.getTree().getModel()).refresh());
            return false;
        }
    }

    /**
     * Saves the file if a file name is known, otherwise forwards to {@code saveFileAs()}.
     */
    private void saveFile()
    {
        XsdTreeNodeRoot root = ((XsdTreeTableModel) this.treeTable.getTree().getModel()).getDataRoot();
        if (this.lastPath == null)
        {
            saveFileAs(root);
            return;
        }
        if (save(this.lastPath.toFile(), root, true))
        {
            setUnsavedChanges(false);
            setStatusLabel("Saved");
        }
    }

    /**
     * Shows a dialog to define a file and saves in to it.
     * @param node root node of tree to save, can be a sub-tree of the full tree.
     */
    public void saveFileAs(final XsdTreeNode node)
    {
        FileDialog fileDialog = new FileDialog(this, "Save XML", FileDialog.SAVE);
        if (this.lastPath != null && this.lastPath.getParent() != null)
        {
            try
            {
                fileDialog.setDirectory(this.lastPath.getParent().toString());
            }
            catch (RuntimeException exception)
            {
                // fall back to the platform default directory
            }
        }
        fileDialog.setFile("*.xml");
        fileDialog.setVisible(true);
        Path path = resolveDialogPath(fileDialog);
        if (path == null)
        {
            return;
        }
        if (!path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".xml"))
        {
            path = path.resolveSibling(path.getFileName().toString() + ".xml");
        }
        if (!save(path.toFile(), node, true))
        {
            return;
        }
        if (node instanceof XsdTreeNodeRoot root)
        {
            this.undo.setIgnoreChanges(true);
            setCurrentFile(path);
            root.setBaseUri(path.toUri());
            invalidateAndRepaint();
            setUnsavedChanges(false);
            this.undo.setIgnoreChanges(false);
        }
        setStatusLabel("Saved");
    }

    /**
     * Performs the actual saving, either from {@code saveFile()}, {@code saveFileAs()} or for a temporary file for simulation.
     * @param file file to save.
     * @param root root node of tree to save, can be a sub-tree of the full tree.
     * @param storeAsRecent whether to store the file under recent files.
     * @return whether the file was successfully saved
     */
    private boolean save(final File file, final XsdTreeNode root, final boolean storeAsRecent)
    {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file))
        {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = docBuilder.newDocument();
            /*
             * The following line omits the 'standalone="no"' in the header xml tag. But there will be no new-line after this
             * header tag. It seems a java bug: https://bugs.openjdk.org/browse/JDK-8249867. Result: <?xml version="1.0"
             * encoding="UTF-8"?><ots:Ots xmlns:ots="http://www.opentrafficsim.org/ots" ... etc. Other lines will be on a new
             * line and indented.
             */
            document.setXmlStandalone(true);
            root.saveXmlNodes(document, document);
            Element xmlRoot = (Element) document.getChildNodes().item(0);
            Set<String> nameSpaces = new LinkedHashSet<>();
            nameSpaces.add("xmlns");
            for (int i = 0; i < this.properties.size(); i = i + 2)
            {
                String prop = this.properties.get(i);
                String value = this.properties.get(i + 1);
                if (prop.startsWith("xmlns") && value != null && !value.isBlank())
                {
                    nameSpaces.add(prop.substring(6));
                }
            }
            for (int i = 0; i < this.properties.size(); i = i + 2)
            {
                String prop = this.properties.get(i);
                String value = this.properties.get(i + 1);
                int semi = prop.indexOf(":");
                String nameSpace = semi < 0 ? null : prop.substring(0, semi);
                if (!nameSpaces.contains(nameSpace) && value != null && !value.isBlank())
                {
                    dialogs().notification(
                            "Unable to save property " + prop + " as its namespace xmlns:" + nameSpace + " is not provided.",
                            "Unable to save property.");
                }
                else if (value != null && !value.isBlank())
                {
                    xmlRoot.setAttribute(prop, value);
                }
            }
            StreamResult result = new StreamResult(fileOutputStream);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(new DOMSource(document), result);

            fileOutputStream.close();
            // this fixes a bug with missing new line
            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            content = content.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator());
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            Files.write(file.toPath(), bytes);
            // end of fix
            if (storeAsRecent)
            {
                PROPERTIES_STORE.addToList(RECENT_FILES_KEY, file.toPath().normalize().toAbsolutePath().toString(),
                        PROPERTIES_STORE.getInteger(MAX_RECENT_FILES_KEY));
                updateRecentFileMenu();
            }
            return true;
        }
        catch (ParserConfigurationException | TransformerException | IOException exception)
        {
            dialogs().notification("Unable to save file.");
            return false;
        }
    }

    /**
     * Exits the system, but not before a confirmation on unsaved changes if there are unsaved changes.
     */
    private void exit()
    {
        if (!this.unsavedChanges || dialogs().confirmDiscardChanges())
        {
            System.exit(0);
        }
    }

    /**
     * Limits the length of a tooltip message. This is to prevent absurd tooltip texts based on really long patterns that should
     * be matched. Will return {@code null} if the input is {@code null}.
     * @param message tooltip message, may be {@code null}.
     * @return possibly shortened tooltip message.
     */
    public static String limitTooltip(final String message)
    {
        int maxTooltipLength = PROPERTIES_STORE.getInteger(MAX_TOOLTIP_LENGTH_KEY);
        if (message == null || message.length() < maxTooltipLength)
        {
            return message;
        }
        return message.substring(0, maxTooltipLength - 3) + "...";
    }

    /**
     * Adds an external listener to the cell editor of the attributes table.
     * @param listener listener to the cell editor of the attributes table.
     */
    public void addAttributeCellEditorListener(final CellEditorListener listener)
    {
        this.attributesTable.getDefaultEditor(String.class).addCellEditorListener(listener);
    }

    /**
     * Sets a node in the clipboard.
     * @param clipboard node to set in the clipboard.
     * @param cut whether the node was cut.
     */
    @SuppressWarnings("hiddenfield")
    public void setClipboard(final XsdTreeNode clipboard, final boolean cut)
    {
        this.clipboard = clipboard;
        this.cut = cut;
    }

    /**
     * Returns the clipboard node.
     * @return clipboard node.
     */
    public XsdTreeNode getClipboard()
    {
        return this.clipboard;
    }

    /**
     * Remove node that was cut. This can be called safely while not knowing the clipboard was cut.
     */
    public void removeClipboardWhenCut()
    {
        if (this.clipboard != null && this.cut)
        {
            if (this.clipboard.isRemovable())
            {
                this.clipboard.remove();
            }
            this.clipboard = null;
        }
    }

    /**
     * Returns the actions that can be performed on the editor.
     * @return node actions.
     */
    public Actions actions()
    {
        return this.actions;
    }

    @Override
    public boolean addListener(final EventListener listener, final EventType eventType)
    {
        return EventProducer.super.addListener(listener, eventType);
    }

    /**
     * Return an evaluator to evaluate expression values. This evaluator uses the input parameters of the currently selected
     * scenario.
     * @return evaluator to evaluate expression values.
     */
    public Eval getEval()
    {
        try
        {
            Eval eval = this.evalWrapper.getEval(OtsEditor.this.scenario.getItemAt(OtsEditor.this.scenario.getSelectedIndex()));
            return eval == null ? this.evalWrapper.getLastValidEval() : eval;
        }
        catch (CircularDependencyException ex)
        {
            dialogs().showCircularInputParameters(ex.getMessage());
            return this.evalWrapper.getLastValidEval();
        }
        catch (RuntimeException ex)
        {
            // some parameters are not valid
            return this.evalWrapper.getLastValidEval();
        }
    }

    /**
     * Adds listener to changes in the evaluator, i.e. added, removed or changed input parameters.
     * @param listener listener.
     */
    public void addEvalListener(final EvalListener listener)
    {
        this.evalWrapper.addListener(listener);
    }

    /**
     * Removes listener to changes in the evaluator, i.e. added, removed or changed input parameters.
     * @param listener listener.
     */
    public void removeEvalListener(final EvalListener listener)
    {
        this.evalWrapper.removeListener(listener);
    }

    /**
     * Sets the color of the tree column.
     * @param color color
     */
    public void setTreeColumnColor(final Color color)
    {
        this.treeTable.setBackground(color);
    }

    /**
     * Restores the selection to the given node if it is still visible in the current tree view.
     * @param node node to restore selection for
     * @param filterActive whether the search filter is active
     */
    private void restoreSelectionAfterFilterToggle(final XsdTreeNode node, final boolean filterActive)
    {
        Object rootNode = this.treeTable.getTree().getModel().getRoot();
        if (node == null || this.treeTable == null || !node.getPath().contains(rootNode))
        {
            return;
        }
        if (!filterActive)
        {
            show(node, null);
            return;
        }
        List<XsdTreeNode> nodePath = node.getPath();
        int rootIndex = nodePath.indexOf(rootNode);
        TreePath path = new TreePath(nodePath.subList(rootIndex, nodePath.size()).toArray());
        if (this.treeTable.getTree().isVisible(path))
        {
            this.treeTable.getTree().setSelectionPath(path);
        }
    }

    /**
     * Returns whether expansion-state updates are currently suppressed for programmatic restoration.
     * @return whether expansion-state updates are currently suppressed for programmatic restoration
     */
    public boolean isSuppressingExpansionStateUpdates()
    {
        return this.suppressExpansionStateUpdates;
    }

    @Override
    public void setAppearance(final Appearance appearance)
    {
        super.setAppearance(appearance);
        // these components are hidden from the Swing structure
        if (this.treeTable != null)
        {
            changeFont((Component) this.treeTable.getTree().getCellRenderer(), appearance.getFont());
            changeFontSize((Component) this.treeTable.getTree().getCellRenderer());
        }
    }

}
