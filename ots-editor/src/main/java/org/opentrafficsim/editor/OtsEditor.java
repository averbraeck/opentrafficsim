package org.opentrafficsim.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.CellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
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
import org.djutils.exceptions.Try;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.base.Resource;
import org.opentrafficsim.editor.EvalWrapper.EvalListener;
import org.opentrafficsim.editor.Undo.ActionType;
import org.opentrafficsim.editor.decoration.DefaultDecorator;
import org.opentrafficsim.editor.listeners.AttributesListSelectionListener;
import org.opentrafficsim.editor.listeners.AttributesMouseListener;
import org.opentrafficsim.editor.listeners.ChangesListener;
import org.opentrafficsim.editor.listeners.PopupValueSelectedListener;
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
import org.opentrafficsim.swing.gui.AppearanceControlComboBox;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

import de.javagl.treetable.JTreeTable;

/**
 * Editor window to load, edit and save OTS XML files. The class uses an underlying data structure that is based on the XML
 * Schema for the XML (XSD).<br>
 * <br>
 * This functionality is currently in development.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class OtsEditor extends AppearanceApplication implements EventProducer
{

    /** */
    private static final long serialVersionUID = 20230217L;

    /** Event when a a new file is started. */
    public static final EventType NEW_FILE = new EventType("NEWFILE",
            new MetaData("New file", "New file", new ObjectDescriptor("Root", "New root element", XsdTreeNodeRoot.class)));

    /** Event when the selection in the tree is changed. */
    public static final EventType SELECTION_CHANGED = new EventType("SELECTIONCHANGED", new MetaData("Selection",
            "Selection changed", new ObjectDescriptor("Selected node", "Selected node", XsdTreeNode.class)));

    /** Width of the divider between parts of the screen. */
    private static final int DIVIDER_SIZE = 4;

    /** Time between autosaves. */
    private static final long AUTOSAVE_PERIOD_MS = 60000;

    /** Whether to update the windows as the split is being dragged. */
    private static final boolean UPDATE_SPLIT_WHILE_DRAGGING = true;

    /** Color for inactive nodes (text). */
    public static final Color INACTIVE_COLOR = new Color(160, 160, 160);

    /** Indent for first item shown in dropdown. */
    private int dropdownIndent = 0;

    /** All items eligible to be shown in a dropdown, i.e. they match the currently typed value. */
    private List<String> dropdownOptions = new ArrayList<>();

    /** Map of listeners for {@code EventProducer}. */
    private final EventListenerMap listenerMap = new EventListenerMap();

    /** Main split pane. */
    private final JSplitPane leftRightSplitPane;

    /** Main tabbed pane at the left-hand side. */
    private final JTabbedPane visualizationPane;

    /** Split pane on the right-hand side. */
    private final JSplitPane rightSplitPane;

    /** Scenario selection. */
    private final JComboBox<ScenarioWrapper> scenario;

    /** Eval wrapper, which maintains input parameters and notifies all dependent objects on changes. */
    private EvalWrapper evalWrapper = new EvalWrapper(this);

    /** Tree table at the top in the right-hand side. */
    private JTreeTable treeTable;

    /** Table for attributes at the bottom of the right-hand side. */
    private final JTable attributesTable;

    /** Status label. */
    private final JLabel statusLabel;

    /** Prevents a popup when an expand node is being clicked. */
    private boolean mayPresentChoice = true;

    /** Node for which currently a choice popup is being shown, {@code null} if there is none. */
    private XsdTreeNode choiceNode;

    /** Map of custom icons, to be loaded as the icon for a node is being composed based in its properties. */
    private Map<String, Icon> customIcons = new LinkedHashMap<>();

    /** Icon for in question dialog. */
    private final ImageIcon questionIcon;

    /** Root node of the XSD file. */
    private Document xsdDocument;

    /** Last directory from which a file was loaded or in to which a file was saved. */
    private String lastDirectory;

    /** Last file that was loaded or saved. */
    private String lastFile;

    /** Whether there is unsaved content. */
    private boolean unsavedChanges = false;

    /** Undo unit, storing all actions. */
    private Undo undo;

    /** Auto save task. */
    private TimerTask autosave;

    // navigate

    /** Menu item for jumping back from coupled node. */
    private JMenuItem backItem;

    /** Candidate keyref node that was coupled from to a key node, may be {@code null}. */
    private XsdTreeNode candidateBackNode;

    /** Keyref node that was coupled from to a key node, may be {@code null}. */
    private final LinkedList<XsdTreeNode> backNode = new LinkedList<>();

    /** Candidate attribute of back node referring to coupled node, may be {@code null}. */
    private String candidateBackAttribute;

    /** Attribute of back node referring to coupled node, may be {@code null}. */
    private final LinkedList<String> backAttribute = new LinkedList<>();

    /** Menu item for jumping to coupled node. */
    private JMenuItem coupledItem;

    /** Key node that is coupled to from a keyref node, may be {@code null}. */
    private XsdTreeNode coupledNode;

    // copy/paste

    /** Node in clipboard (sort of...). */
    private XsdTreeNode clipboard;

    /** Whether the node in the clipboard was cut. */
    private boolean cut;

    /** Node actions. */
    private NodeActions nodeActions;

    /** Application store for preferences and recent files. */
    private static final ApplicationStore APPLICATION_STORE = new ApplicationStore("OTS", "editor");

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
        setSize(1280, 720);
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
        makeClickFlippable(this.rightSplitPane);
        JPanel rightContainer = new JPanel();
        rightContainer.setLayout(new BoxLayout(rightContainer, BoxLayout.Y_AXIS));
        rightContainer.setBorder(new LineBorder(null, -1));

        // scenario and controls
        JPanel controlsContainer = new JPanel();
        controlsContainer.add(Box.createHorizontalGlue()); // right-aligns everything
        controlsContainer.setLayout(new BoxLayout(controlsContainer, BoxLayout.X_AXIS));
        controlsContainer.setBorder(new LineBorder(null, -1));
        controlsContainer.setMinimumSize(new Dimension(200, 28));
        controlsContainer.setPreferredSize(new Dimension(200, 28));
        controlsContainer.add(new JLabel("Scenario: "));
        this.scenario = new AppearanceControlComboBox<>();
        this.scenario.addItem(new ScenarioWrapper(null));
        this.scenario.setMinimumSize(new Dimension(50, 22));
        this.scenario.setMaximumSize(new Dimension(250, 22));
        this.scenario.setPreferredSize(new Dimension(200, 22));
        this.scenario.addActionListener((a) ->
        {
            try
            {
                OtsEditor.this.evalWrapper.setDirty();
                OtsEditor.this.evalWrapper
                        .getEval(OtsEditor.this.scenario.getItemAt(OtsEditor.this.scenario.getSelectedIndex()));
            }
            catch (CircularDependencyException ex)
            {
                showCircularInputParameters(ex.getMessage());
            }
            catch (RuntimeException ex)
            {
                showInvalidExpression(ex.getMessage());
            }
        });
        controlsContainer.add(this.scenario);
        controlsContainer.add(Box.createHorizontalStrut(2));
        JButton playRun = new JButton();
        playRun.setToolTipText("Run single run");
        playRun.setIcon(DefaultDecorator.loadIcon("./Play.png", 18, 18, -1, -1));
        Dimension iconDimension = new Dimension(24, 24);
        playRun.setMinimumSize(iconDimension);
        playRun.setMaximumSize(iconDimension);
        playRun.setPreferredSize(iconDimension);
        playRun.addActionListener((a) -> runSingle());
        controlsContainer.add(playRun);
        JButton playScenario = new JButton();
        playScenario.setToolTipText("Run scenario (batch)");
        playScenario.setIcon(DefaultDecorator.loadIcon("./NextTrack.png", 18, 18, -1, -1));
        playScenario.setMinimumSize(iconDimension);
        playScenario.setMaximumSize(iconDimension);
        playScenario.setPreferredSize(iconDimension);
        playScenario.addActionListener((a) -> runBatch(false));
        controlsContainer.add(playScenario);
        JButton playAll = new JButton();
        playAll.setToolTipText("Run all (batch)");
        playAll.setIcon(DefaultDecorator.loadIcon("./Last_recor.png", 18, 18, -1, -1));
        playAll.setMinimumSize(iconDimension);
        playAll.setMaximumSize(iconDimension);
        playAll.setPreferredSize(iconDimension);
        playAll.addActionListener((a) -> runBatch(true));
        controlsContainer.add(playAll);
        controlsContainer.add(Box.createHorizontalStrut(4));

        rightContainer.add(controlsContainer);
        rightContainer.add(this.rightSplitPane);
        this.leftRightSplitPane.setRightComponent(rightContainer);

        this.questionIcon = DefaultDecorator.loadIcon("./Question.png", -1, -1, -1, -1);

        // visualization pane
        UIManager.getInsets("TabbedPane.contentBorderInsets").set(-1, -1, 1, -1);
        this.visualizationPane = new JTabbedPane(JTabbedPane.BOTTOM, JTabbedPane.SCROLL_TAB_LAYOUT);
        this.visualizationPane.setPreferredSize(new Dimension(900, 900));
        this.visualizationPane.setBorder(new LineBorder(Color.BLACK, 0));
        this.leftRightSplitPane.setLeftComponent(this.visualizationPane);

        // There is likely a better way to do this, but setting the icons specific on the tree is impossible for collapsed and
        // expanded. Also in that case after removal of a node, the tree appearance gets reset and java default icons appear.
        // This happens to the leaf/open/closed icons that can be set on the tree. This needs to be done before the JTreeTable
        // is created, otherwise it loads normal default icons.
        UIManager.put("Tree.collapsedIcon",
                new ImageIcon(ImageIO.read(Resource.getResourceAsStream("/Eclipse_collapsed.png"))));
        UIManager.put("Tree.expandedIcon", new ImageIcon(ImageIO.read(Resource.getResourceAsStream("/Eclipse_expanded.png"))));

        // empty tree table
        this.treeTable = new AppearanceControlTreeTable(new XsdTreeTableModel(null));
        XsdTreeTableModel.applyColumnWidth(this.treeTable);
        this.rightSplitPane.setTopComponent(new JScrollPane(this.treeTable));

        // attributes table
        AttributesTableModel tableModel = new AttributesTableModel(null, this.treeTable);
        DefaultTableColumnModel columns = new DefaultTableColumnModel();
        TableColumn column1 = new TableColumn(0);
        column1.setHeaderValue(tableModel.getColumnName(0));
        columns.addColumn(column1);
        TableColumn column2 = new TableColumn(1);
        column2.setHeaderValue(tableModel.getColumnName(1));
        columns.addColumn(column2);
        TableColumn column3 = new TableColumn(2);
        column3.setHeaderValue(tableModel.getColumnName(2));
        columns.addColumn(column3);
        TableColumn column4 = new TableColumn(3);
        column4.setHeaderValue(tableModel.getColumnName(3));
        columns.addColumn(column4);
        this.attributesTable = new JTable(tableModel, columns);
        this.attributesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.attributesTable.putClientProperty("terminateEditOnFocusLost", true);
        this.attributesTable.setDefaultRenderer(String.class,
                new AttributeCellRenderer(DefaultDecorator.loadIcon("./Info.png", 12, 12, 16, 16)));
        AttributesCellEditor editor = new AttributesCellEditor(this.attributesTable, this);
        this.attributesTable.setDefaultEditor(String.class, editor);
        this.attributesTable.addMouseListener(new AttributesMouseListener(this, this.attributesTable));
        this.attributesTable.getSelectionModel()
                .addListSelectionListener(new AttributesListSelectionListener(this, this.attributesTable));
        AttributesTableModel.applyColumnWidth(this.attributesTable);
        this.rightSplitPane.setBottomComponent(new JScrollPane(this.attributesTable));

        addMenuBar();

        this.statusLabel = new StatusLabel();
        this.statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        this.statusLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        add(this.statusLabel, BorderLayout.SOUTH);
        removeStatusLabel();
    }

    /**
     * Makes the divider clickable cause the panel to exchange screen size the other way around.
     * @param pane splitpane to make click-flippable.
     */
    private void makeClickFlippable(final JSplitPane pane)
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
     * Returns the invalid cell color.
     * @return the invalid cell color
     */
    public static Color getInvalidColor()
    {
        return APPLICATION_STORE.getColor("invalid_color");
    }

    /**
     * Returns the expression cell color.
     * @return the expression cell color
     */
    public static Color getExpressionColor()
    {
        return APPLICATION_STORE.getColor("expression_color");
    }

    /**
     * Run a single simulation run.
     */
    private void runSingle()
    {
        if (!((XsdTreeNode) this.treeTable.getTree().getModel().getRoot()).isValid())
        {
            showInvalidToRunMessage();
            return;
        }
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
            showCircularInputParameters(ex.getMessage());
            return;
        }
        File file;
        try
        {
            file = File.createTempFile("ots_", ".xml");
        }
        catch (IOException exception)
        {
            showUnableToRunFromTempFile();
            return;
        }
        save(file, (XsdTreeNodeRoot) this.treeTable.getTree().getModel().getRoot(), false);

        if (index == 0)
        {
            OtsRunner.runSingle(file, null);
        }
        else
        {
            String selectedScenario = this.scenario.getItemAt(index).scenarioNode().getId();
            OtsRunner.runSingle(file, selectedScenario);
        }
        file.delete();
    }

    /**
     * Batch run.
     * @param all all scenarios, or only the selected scenario.
     */
    protected void runBatch(final boolean all)
    {
        if (!((XsdTreeNode) this.treeTable.getTree().getModel().getRoot()).isValid())
        {
            showInvalidToRunMessage();
            return;
        }
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
            getNodeActions().expand(node, path, true);
        }
    }

    /**
     * Shows and selects the given node in the tree.
     * @param node node.
     * @param attribute attribute name, may be {@code null} to just show the node.
     */
    public void show(final XsdTreeNode node, final String attribute)
    {
        if (node.getParent() == null)
        {
            return; // trying to show node that is in collapsed part of the tree
        }
        if (this.treeTable.isEditing())
        {
            CellEditor editor = this.treeTable.getCellEditor();
            if (editor != null)
            {
                editor.cancelCellEditing();
            }
        }
        if (this.attributesTable.isEditing())
        {
            CellEditor editor = this.attributesTable.getCellEditor();
            if (editor != null)
            {
                editor.cancelCellEditing();
            }
        }
        List<XsdTreeNode> nodePath = node.getPath();
        TreePath path = new TreePath(nodePath.toArray());
        TreePath partialPath = new TreePath(nodePath.subList(0, nodePath.size() - 1).toArray());
        this.treeTable.getTree().expandPath(partialPath);
        this.treeTable.getTree().setSelectionPath(path);
        this.treeTable.getTree().scrollPathToVisible(path);
        this.treeTable.updateUI();
        Rectangle bounds = this.treeTable.getTree().getPathBounds(path);
        if (bounds == null)
        {
            return; // trying to show node that is in collapsed part of the tree
        }
        bounds.x += this.treeTable.getX();
        bounds.y += this.treeTable.getY();
        ((JComponent) this.treeTable.getParent()).scrollRectToVisible(bounds);

        this.attributesTable.setModel(new AttributesTableModel(node.isActive() ? node : null, this.treeTable));
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
        saveAs.addActionListener((a) -> saveFileAs((XsdTreeNodeRoot) OtsEditor.this.treeTable.getTree().getModel().getRoot()));
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
        this.backItem = new JMenuItem("Go back");
        this.backItem.setEnabled(false);
        this.backItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        navigateMenu.add(this.backItem);
        this.backItem.addActionListener((a) ->
        {
            show(this.backNode.pollLast(), this.backAttribute.pollLast());
            if (this.backNode.isEmpty())
            {
                this.backItem.setText("Go back");
                this.backItem.setEnabled(false);
            }
            else
            {
                XsdTreeNode back = this.backNode.peekLast();
                this.backItem.setText("Go back to " + back.getNodeName() + (back.isIdentifiable() ? " " + back.getId() : ""));
                this.backItem.setEnabled(true);
            }
        });
        this.coupledItem = new JMenuItem("Go to coupled item");
        this.coupledItem.setEnabled(false);
        this.coupledItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
        navigateMenu.add(this.coupledItem);
        this.coupledItem.addActionListener((a) ->
        {
            if (this.coupledNode != null)
            {
                this.backNode.add(this.candidateBackNode);
                this.backAttribute.add(this.candidateBackAttribute);
                while (this.backNode.size() > APPLICATION_STORE.getInt("max_navigate"))
                {
                    this.backNode.remove();
                    this.backAttribute.remove();
                }
                XsdTreeNode back = OtsEditor.this.backNode.peekLast();
                this.backItem.setText("Go back to " + back.getNodeName() + (back.isIdentifiable() ? " " + back.getId() : ""));
                this.backItem.setEnabled(this.backNode.peekLast() != null);
                show(OtsEditor.this.coupledNode, null);
            }
        });
    }

    /**
     * Updates the recent file menu.
     */
    private void updateRecentFileMenu()
    {
        this.recentFilesMenu.removeAll();
        List<String> files = APPLICATION_STORE.getRecentFiles("recent_files");
        if (!files.isEmpty())
        {
            for (String file : files)
            {
                JMenuItem item = new JMenuItem(file);
                item.addActionListener((i) ->
                {
                    if (confirmDiscardChanges())
                    {
                        File f = new File(file);
                        this.lastDirectory = f.getParent() + File.separator;
                        this.lastFile = f.getName();
                        if (!loadFile(f, "File loaded", true))
                        {
                            boolean remove = JOptionPane.showConfirmDialog(OtsEditor.this,
                                    "File could not be loaded. Do you want to remove it from recent files?",
                                    "Remove from recent files?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                                    this.questionIcon) == JOptionPane.OK_OPTION;
                            if (remove)
                            {
                                APPLICATION_STORE.removeRecentFile("recent_files", file);
                                updateRecentFileMenu();
                            }
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
            boolean clear = JOptionPane.showConfirmDialog(OtsEditor.this, "Are you sure you want to clear the recent files?",
                    "Clear recent files?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                    this.questionIcon) == JOptionPane.OK_OPTION;
            if (clear)
            {
                APPLICATION_STORE.clearProperty("recent_files");
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
        if (toNode == null)
        {
            this.coupledItem.setEnabled(false);
            this.coupledItem.setText("Go to coupled item");
        }
        else
        {
            this.coupledItem.setEnabled(true);
            this.coupledItem.setText("Go to " + (fromAttribute != null ? fromNode.getAttributeValue(fromAttribute)
                    : (fromNode.isIdentifiable() ? fromNode.getId() : fromNode.getValue())));
        }
        this.coupledNode = toNode;
        this.candidateBackNode = fromNode;
        this.candidateBackAttribute = fromAttribute;
    }

    /**
     * Sets whether there are unsaved changes, resulting in a * in the window name, and confirmation pop-ups upon file changes.
     * @param unsavedChanges whether there are unsaved changes.
     */
    public void setUnsavedChanges(final boolean unsavedChanges)
    {
        this.unsavedChanges = unsavedChanges;
        StringBuilder title = new StringBuilder("OTS | The Open Traffic Simulator | Editor");
        if (this.lastFile != null)
        {
            title.append(" (").append(this.lastDirectory).append(this.lastFile).append(")");
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
        Path tmpPath = Paths.get(System.getProperty("java.io.tmpdir") + "ots" + File.separator);
        File tmpDir = tmpPath.toFile();
        if (!tmpDir.exists())
        {
            tmpDir.mkdir();
        }
        Iterator<Path> it;
        try
        {
            it = Files.newDirectoryStream(tmpPath, "autosave*.xml").iterator();
        }
        catch (IOException ioe)
        {
            // skip presenting user with autosave, but also do not delete file
            return;
        }
        if (it.hasNext())
        {
            File file = it.next().toFile();
            int userInput = JOptionPane.showConfirmDialog(this,
                    "Autosave file " + file.getName() + " (" + new Date(file.lastModified())
                            + ") detected. Do you want to load this file? ('No' removes the file)",
                    "Autosave file detected", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                    this.questionIcon);
            if (userInput == JOptionPane.OK_OPTION)
            {
                boolean loaded = loadFile(file, "Autosave file loaded", false);
                if (!loaded)
                {
                    boolean remove = JOptionPane.showConfirmDialog(OtsEditor.this,
                            "Autosave file could not be loaded. Do you want ro remove it?", "Remove autosave?",
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                            this.questionIcon) == JOptionPane.YES_OPTION;
                    if (remove)
                    {
                        file.delete();
                    }
                }
                setUnsavedChanges(true);
                this.treeTable.updateUI();
                file.delete();
            }
            else if (userInput == JOptionPane.NO_OPTION)
            {
                file.delete();
            }
        }
    }

    /**
     * Asks for confirmation to discard unsaved changes, if any, and initializes the tree.
     */
    private void newFile()
    {
        if (confirmDiscardChanges())
        {
            try
            {
                this.undo.setIgnoreChanges(true);
                initializeTree();
                this.attributesTable.setModel(new AttributesTableModel(null, this.treeTable));
                this.undo.clear();
            }
            catch (IOException exception)
            {
                JOptionPane.showMessageDialog(this, "Unable to reload schema.", "Unable to reload schema.",
                        JOptionPane.WARNING_MESSAGE);
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

        // tree table
        XsdTreeTableModel treeModel = new XsdTreeTableModel(this.xsdDocument);
        this.treeTable = new AppearanceControlTreeTable(treeModel);
        this.treeTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.nodeActions = new NodeActions(this, this.treeTable);
        this.treeTable.putClientProperty("terminateEditOnFocusLost", true);
        treeModel.setTreeTable(this.treeTable);
        this.treeTable.setDefaultRenderer(String.class, new StringCellRenderer(this.treeTable));
        ((DefaultCellEditor) this.treeTable.getDefaultEditor(String.class)).setClickCountToStart(1);
        XsdTreeTableModel.applyColumnWidth(this.treeTable);
        // sets custom icon and appends the choice icon for choice nodes
        this.treeTable.getTree().setCellRenderer(new XsdTreeCellRenderer(this));

        // this listener changes Id or node Value for each key being pressed
        // this listener starts a new undo event when the editor gets focus on the JTreeTable
        // this listener may cause new undo actions when cells are navigated using the keyboard
        new XsdTreeEditorListener(this, this.treeTable);

        // throws selection events and updates the attributes table
        // this listener will make sure no choice popup is presented by a left-click on expand/collapse, even for a choice node
        // this listener makes sure that a choice popup can be presented again after a left-click on an expansion/collapse node
        // it also shows the tooltip in tree nodes
        // this listener opens the attributes of a node, and presents the popup for a choice or for addition/deletion of nodes
        new XsdTreeListener(this, this.treeTable, this.attributesTable);

        // listener to keyboard shortcuts and key events that should start (i.e. end previous) undo actions
        new XsdTreeKeyListener(this, this.treeTable);

        int dividerLocation = this.rightSplitPane.getDividerLocation();
        this.rightSplitPane.setTopComponent(new JScrollPane(this.treeTable));
        this.rightSplitPane.setDividerLocation(dividerLocation);

        XsdTreeNodeRoot root = (XsdTreeNodeRoot) treeModel.getRoot();
        EventListener listener = new ChangesListener(this, this.scenario);
        root.addListener(listener, XsdTreeNodeRoot.NODE_CREATED);
        root.addListener(listener, XsdTreeNodeRoot.NODE_REMOVED);
        fireEvent(NEW_FILE, root);

        setUnsavedChanges(false);
        if (this.autosave != null)
        {
            this.autosave.cancel();
        }
        this.autosave = new TimerTask()
        {
            @Override
            public void run()
            {
                if (OtsEditor.this.unsavedChanges)
                {
                    setStatusLabel("Autosaving...");
                    File file = new File(System.getProperty("java.io.tmpdir") + "ots" + File.separator
                            + (OtsEditor.this.lastFile == null ? "autosave.xml" : "autosave_" + OtsEditor.this.lastFile));
                    save(file, root, false);
                    file.deleteOnExit();
                    setStatusLabel("Autosaved");
                }
            }
        };
        new Timer().scheduleAtFixedRate(this.autosave, AUTOSAVE_PERIOD_MS, AUTOSAVE_PERIOD_MS);
        setAppearance(getAppearance()); // because of new AppearanceControlTreeTable
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
        SwingUtilities.invokeLater(() ->
        {
            XsdTreeNode node = (XsdTreeNode) this.treeTable.getValueAt(this.treeTable.getSelectedRow(),
                    this.treeTable.convertColumnIndexToView(0)); // columns may have been moved in view;
            int col = this.treeTable.convertColumnIndexToModel(this.treeTable.getSelectedColumn());
            if (col == 1)
            {
                this.undo.startAction(ActionType.ID_CHANGE, node, null);
            }
            else if (col == 2)
            {
                this.undo.startAction(ActionType.VALUE_CHANGE, node, null);
            }
        });
    }

    /**
     * Returns the XsdTreeNode of the row under the given point (from a mouse or key event).
     * @param point point (from an event)
     * @return the XsdTreeNode of the row under the given point
     */
    public XsdTreeNode getTreeNodeAtPoint(final Point point)
    {
        int row = this.treeTable.rowAtPoint(point);
        int col = this.treeTable.convertColumnIndexToView(0); // columns may have been moved in view
        return (XsdTreeNode) this.treeTable.getValueAt(row, col);
    }

    /**
     * Adds a listener to a popup to remove the popop from the component when the popup becomes invisible. This makes sure that
     * a right-click on another location that should show a different popup, is not overruled by the popup of a previous click.
     * @param popup popup menu.
     * @param component component from which the menu will be removed.
     */
    public void preparePopupRemoval(final JPopupMenu popup, final JComponent component)
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
                component.setComponentPopupMenu(null);
                OtsEditor.this.choiceNode = null;
            }

            @Override
            public void popupMenuCanceled(final PopupMenuEvent e)
            {
            }
        });
    }

    /**
     * Sets a custom icon for nodes that comply to the path. The path may be an absolute path (e.g. "Ots.Network.Connector") or
     * a relative path (e.g. ".Node").
     * @param path path.
     * @param icon image icon.
     */
    public void setCustomIcon(final String path, final ImageIcon icon)
    {
        this.customIcons.put(path, icon);
    }

    /**
     * Obtains a custom icon for the path, or {@code null} if there is no custom icon specified for the path.
     * @param path node path.
     * @return custom icon, or {@code null} if there is no custom icon specified for the path.
     */
    public Icon getCustomIcon(final String path)
    {
        Icon icon = this.customIcons.get(path);
        if (icon != null)
        {
            return icon;
        }
        for (Entry<String, Icon> entry : this.customIcons.entrySet())
        {
            if (path.endsWith(entry.getKey()))
            {
                return entry.getValue();
            }
        }
        return null;
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
    public EventListenerMap getEventListenerMap() throws RemoteException
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
     * @return component of the tab with given name or {@code null} if no such tab
     */
    public Component getTab(final String name)
    {
        for (int index = 0; index < this.visualizationPane.getTabCount(); index++)
        {
            if (this.visualizationPane.getTitleAt(index).equals(name))
            {
                return this.visualizationPane.getComponentAt(index);
            }
        }
        return null;
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
     * Requests the user to confirm the deletion of a node. The default button is "Ok". The window popping up is considered
     * sufficient warning, and in this way a speedy succession of "del" and "enter" may delete a consecutive range of nodes to
     * be deleted.
     * @param node node.
     * @return {@code true} if the user confirms node removal.
     */
    public boolean confirmNodeRemoval(final XsdTreeNode node)
    {
        return JOptionPane.showConfirmDialog(this, "Remove `" + node + "`?", "Remove?", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, this.questionIcon) == JOptionPane.OK_OPTION;
    }

    /**
     * Shows a dialog in a modal pane to confirm discarding unsaved changes.
     * @return whether unsaved changes can be discarded.
     */
    private boolean confirmDiscardChanges()
    {
        if (!this.unsavedChanges)
        {
            return true;
        }
        return JOptionPane.showConfirmDialog(this, "Discard unsaved changes?", "Discard unsaved changes?",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, this.questionIcon) == JOptionPane.OK_OPTION;
    }

    /**
     * Shows a description in a modal pane.
     * @param description description.
     */
    public void showDescription(final String description)
    {
        JOptionPane.showMessageDialog(OtsEditor.this,
                "<html><body><p style='width: 400px;'>" + description + "</p></body></html>");
    }

    /**
     * Show tree invalid.
     */
    public void showInvalidToRunMessage()
    {
        JOptionPane.showMessageDialog(OtsEditor.this, "The setup is not valid. Make sure no red nodes remain.",
                "Setup is not valid", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Show input parameters have a circular dependency.
     * @param message exception message
     */
    public void showCircularInputParameters(final String message)
    {
        JOptionPane.showMessageDialog(OtsEditor.this, "Input parameters have a circular dependency: " + message,
                "Circular input parameter", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Show message about invalid expression.
     * @param message exception message
     */
    public void showInvalidExpression(final String message)
    {
        JOptionPane.showMessageDialog(OtsEditor.this, "An expression is not valid: " + message, "Expression not valid",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Show unable to run.
     */
    public void showUnableToRunFromTempFile()
    {
        JOptionPane.showMessageDialog(OtsEditor.this, "Unable to run, temporary file could not be saved.", "Unable to run",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Places a popup with value options under the cell that is being clicked in a table (tree or attributes). The popup will
     * show items relevant to what is being typed in the cell. The maximum number of items shown is limited to
     * {@code MAX_DROPDOWN_ITEMS}.
     * @param allOptions list of all options, will be filtered when typing.
     * @param table table, will be either the tree table or the attributes table.
     * @param action action to perform based on the option in the popup that was selected.
     */
    public void valueOptionsPopup(final List<String> allOptions, final JTable table, final Consumer<String> action)
    {
        // initially no filtering on current value; this allows a quick reset to possible values
        List<String> options = filterOptions(allOptions, "");
        OtsEditor.this.dropdownOptions = options;
        if (options.isEmpty())
        {
            return;
        }
        JPopupMenu popup = new JPopupMenu();
        int index = 0;
        int maxDropdown = APPLICATION_STORE.getInt("max_dropdown_items");
        for (String option : options)
        {
            JMenuItem item = new JMenuItem(option);
            item.setVisible(index++ < maxDropdown);
            item.addActionListener(new PopupValueSelectedListener(option, table, action, this.treeTable));
            item.setFont(table.getFont());
            popup.add(item);
        }
        this.dropdownIndent = 0;
        popup.addMouseWheelListener(new MouseWheelListener()
        {
            @Override
            public void mouseWheelMoved(final MouseWheelEvent e)
            {
                OtsEditor.this.dropdownIndent += (e.getWheelRotation() * e.getScrollAmount());
                OtsEditor.this.dropdownIndent = OtsEditor.this.dropdownIndent < 0 ? 0 : OtsEditor.this.dropdownIndent;
                int maxIndent = OtsEditor.this.dropdownOptions.size() - maxDropdown;
                if (maxIndent > 0)
                {
                    OtsEditor.this.dropdownIndent =
                            OtsEditor.this.dropdownIndent > maxIndent ? maxIndent : OtsEditor.this.dropdownIndent;
                    showOptionsInScope(popup);
                }
            }
        });
        preparePopupRemoval(popup, table);
        // invoke later because JTreeTable removes the popup with editable cells and it may take previous editable field
        SwingUtilities.invokeLater(() ->
        {
            JTextField field = (JTextField) ((DefaultCellEditor) table.getDefaultEditor(String.class)).getComponent();
            table.setComponentPopupMenu(popup);
            popup.pack();
            popup.setInvoker(table);
            popup.setVisible(true);
            field.requestFocus();
            Rectangle rectangle = field.getBounds();
            placePopup(popup, rectangle, table);
            field.addKeyListener(new KeyAdapter()
            {
                @Override
                public void keyTyped(final KeyEvent e)
                {
                    // invoke later to include this current typed key in the result
                    SwingUtilities.invokeLater(() ->
                    {
                        OtsEditor.this.dropdownIndent = 0;
                        String currentValue = field.getText();
                        OtsEditor.this.dropdownOptions = filterOptions(allOptions, currentValue);
                        boolean anyVisible = showOptionsInScope(popup);
                        // if no items left, show what was typed as a single item
                        // it will be hidden later if we are in the scope of the options, or another current value
                        if (!anyVisible)
                        {
                            JMenuItem item = new JMenuItem(currentValue);
                            item.addActionListener(
                                    new PopupValueSelectedListener(currentValue, table, action, OtsEditor.this.treeTable));
                            item.setFont(table.getFont());
                            popup.add(item);
                        }
                        popup.pack();
                        placePopup(popup, rectangle, table);
                    });
                }
            });
            field.addActionListener((e) ->
            {
                popup.setVisible(false);
                table.setComponentPopupMenu(null);
            });
        });
    }

    /**
     * Filter options for popup, leaving only those that start with the current value.
     * @param options options to filter.
     * @param currentValue current value.
     * @return filtered options.
     */
    private static List<String> filterOptions(final List<String> options, final String currentValue)
    {
        return options.stream().filter((val) -> currentValue == null || currentValue.isEmpty() || val.startsWith(currentValue))
                .distinct().sorted().collect(Collectors.toList());
    }

    /**
     * Updates the options that are shown within a popup menu based on an indent from scrolling.
     * @param popup popup menu.
     * @return whether at least one item is visible.
     */
    private boolean showOptionsInScope(final JPopupMenu popup)
    {
        int optionIndex = 0;
        int maxDropdown = APPLICATION_STORE.getInt("max_dropdown_items");
        for (Component component : popup.getComponents())
        {
            JMenuItem item = (JMenuItem) component;
            boolean visible = optionIndex < maxDropdown && this.dropdownOptions.indexOf(item.getText()) >= this.dropdownIndent;
            item.setVisible(visible);
            if (visible)
            {
                optionIndex++;
            }
        }
        popup.pack();
        return optionIndex > 0;
    }

    /**
     * Places a popup either below or above a given rectangle, based on surrounding space in the window.
     * @param popup popup.
     * @param rectangle rectangle of cell being edited, relative to the parent component.
     * @param parent component containing the cell.
     */
    private void placePopup(final JPopupMenu popup, final Rectangle rectangle, final JComponent parent)
    {
        Point pAttributes = parent.getLocationOnScreen();
        // cannot use screen size in case of multiple monitors, so we keep the popup on the JFrame rather than the window
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
        if (!confirmDiscardChanges())
        {
            return;
        }
        FileDialog fileDialog = new FileDialog(this, "Open XML", FileDialog.LOAD);
        fileDialog.setFilenameFilter((dir, name) -> name.toLowerCase().endsWith(".xml"));
        fileDialog.setVisible(true);
        String fileName = fileDialog.getFile();
        if (fileName == null)
        {
            return;
        }
        if (!fileName.toLowerCase().endsWith(".xml"))
        {
            return;
        }
        this.lastDirectory = fileDialog.getDirectory();
        this.lastFile = fileName;
        File file = new File(this.lastDirectory + this.lastFile);
        boolean loaded = loadFile(file, "File loaded", true);
        if (!loaded)
        {
            JOptionPane.showMessageDialog(this, "Unable to read file.", "Unable to read file.", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Load file.
     * @param file file to load.
     * @param postLoadStatus status message in status bar to show after loading.
     * @param updateRecentFiles whether to include the opened file in recent files.
     * @return whether the file was successfully loaded.
     */
    private boolean loadFile(final File file, final String postLoadStatus, final boolean updateRecentFiles)
    {
        try
        {
            Document document = DocumentReader.open(file.toURI());
            this.undo.setIgnoreChanges(true);
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
            XsdTreeNodeRoot root = (XsdTreeNodeRoot) OtsEditor.this.treeTable.getTree().getModel().getRoot();
            root.setDirectory(this.lastDirectory);
            root.loadXmlNodes(document.getFirstChild());
            this.undo.clear();
            setUnsavedChanges(false);
            setStatusLabel(postLoadStatus);
            this.undo.updateButtons();
            this.backItem.setEnabled(false);
            this.coupledItem.setEnabled(false);
            this.coupledItem.setText("Go to coupled item");
            this.treeTable.updateUI(); // knowing/changing the directory may change validation status through imports
            if (updateRecentFiles)
            {
                APPLICATION_STORE.addRecentFile("recent_files", file.getAbsolutePath());
                updateRecentFileMenu();
            }
            return true;
        }
        catch (SAXException | IOException | ParserConfigurationException exception)
        {
            return false;
        }
    }

    /**
     * Saves the file if a file name is known, otherwise forwards to {@code saveFileAs()}.
     */
    private void saveFile()
    {
        XsdTreeNodeRoot root = (XsdTreeNodeRoot) OtsEditor.this.treeTable.getTree().getModel().getRoot();
        if (this.lastFile == null)
        {
            saveFileAs(root);
            return;
        }
        save(new File(this.lastDirectory + this.lastFile), root, true);
        setUnsavedChanges(false);
        setStatusLabel("Saved");
    }

    /**
     * Shows a dialog to define a file and saves in to it.
     * @param root root node of tree to save, can be a sub-tree of the full tree.
     */
    public void saveFileAs(final XsdTreeNode root)
    {
        FileDialog fileDialog = new FileDialog(this, "Save XML", FileDialog.SAVE);
        fileDialog.setFile("*.xml");
        fileDialog.setVisible(true);
        String fileName = fileDialog.getFile();
        if (fileName == null)
        {
            return;
        }
        if (!fileName.toLowerCase().endsWith(".xml"))
        {
            fileName = fileName + ".xml";
        }
        if (root instanceof XsdTreeNodeRoot)
        {
            this.lastDirectory = fileDialog.getDirectory();
            this.lastFile = fileName;
        }
        save(new File(fileDialog.getDirectory() + fileName), root, true);
        if (root instanceof XsdTreeNodeRoot)
        {
            this.undo.setIgnoreChanges(true);
            ((XsdTreeNodeRoot) root).setDirectory(this.lastDirectory);
            this.treeTable.updateUI();
            this.attributesTable.updateUI();
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
     */
    private void save(final File file, final XsdTreeNode root, final boolean storeAsRecent)
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
                    JOptionPane.showMessageDialog(this,
                            "Unable to save property " + prop + " as its namespace xmlns:" + nameSpace + " is not provided.",
                            "Unable to save property.", JOptionPane.WARNING_MESSAGE);
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
            Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
            // end of fix
            if (storeAsRecent)
            {
                APPLICATION_STORE.addRecentFile("recent_files", file.getAbsolutePath());
                updateRecentFileMenu();
            }
        }
        catch (ParserConfigurationException | TransformerException | IOException exception)
        {
            JOptionPane.showMessageDialog(this, "Unable to save file.", "Unable to save file.", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Exits the system, but not before a confirmation on unsaved changes if there are unsaved changes.
     */
    private void exit()
    {
        if (confirmDiscardChanges())
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
        int maxTooltipLength = APPLICATION_STORE.getInt("max_tooltip_length");
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
     * Returns the node actions.
     * @return node actions.
     */
    public NodeActions getNodeActions()
    {
        return this.nodeActions;
    }

    @Override
    public boolean addListener(final EventListener listener, final EventType eventType)
    {
        return Try.assign(() -> EventProducer.super.addListener(listener, eventType),
                "Local event producer should not give a RemoteException.");
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
            showCircularInputParameters(ex.getMessage());
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
