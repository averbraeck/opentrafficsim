package org.opentrafficsim.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.ExpandVetoException;
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
import org.opentrafficsim.editor.listeners.AttributesListSelectionListener;
import org.opentrafficsim.editor.listeners.AttributesMouseListener;
import org.opentrafficsim.editor.listeners.ChangesListener;
import org.opentrafficsim.editor.listeners.XsdTreeKeyListener;
import org.opentrafficsim.editor.listeners.XsdTreeMouseListener;
import org.opentrafficsim.editor.listeners.XsdTreeSelectionListener;
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
    private static final int DIVIDER_SIZE = 3;

    /** Time between autosaves. */
    private static final long AUTOSAVE_PERIOD_MS = 60000;

    /** Whether to update the windows as the split is being dragged. */
    private static final boolean UPDATE_SPLIT_WHILE_DRAGGING = true;

    /** Color for inactive nodes (text). */
    public static final Color INACTIVE_COLOR = new Color(160, 160, 160);

    /** Color for status label. */
    public static final Color STATUS_COLOR = new Color(128, 128, 128);

    /** Color for invalid nodes and values (background). */
    public static final Color INVALID_COLOR = new Color(255, 240, 240);

    /** Color for expression nodes and values (background). */
    public static final Color EXPRESSION_COLOR = new Color(252, 250, 239);

    /** Maximum length for tooltips. */
    private static final int MAX_TOOLTIP_LENGTH = 96;

    /** Maximum number of items to show in a dropdown menu. */
    private static final int MAX_DROPDOWN_ITEMS = 20;

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
    private XsdTreeNode backNode;

    /** Candidate atrribute of back node referring to coupled node, may be {@code null}. */
    private String candidateBackAttribute;

    /** Atrribute of back node referring to coupled node, may be {@code null}. */
    private String backAttribute;

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
    private ApplicationStore applicationStore = new ApplicationStore("ots", "editor");

    /** Menu with recent files. */
    private JMenu recentFilesMenu;

    /**
     * Constructor.
     * @throws IOException when a resource could not be loaded.
     */
    public OtsEditor() throws IOException
    {
        super();

        setSize(1280, 720);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            /** {@inheritDoc} */
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
        add(this.leftRightSplitPane);
        this.rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, UPDATE_SPLIT_WHILE_DRAGGING);
        this.rightSplitPane.setDividerSize(DIVIDER_SIZE);
        this.rightSplitPane.setResizeWeight(0.5);
        this.rightSplitPane.setAlignmentX(0.5f);

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
        JLabel scenarioLabel = new JLabel("Scenario: ");
        // scenarioLabel.setFont(FONT);
        controlsContainer.add(scenarioLabel);
        this.scenario = new AppearanceControlComboBox<>();
        // this.scenario.setFont(FONT);
        this.scenario.addItem(new ScenarioWrapper(null));
        this.scenario.setMinimumSize(new Dimension(50, 22));
        this.scenario.setMaximumSize(new Dimension(250, 22));
        this.scenario.setPreferredSize(new Dimension(200, 22));
        this.scenario.addActionListener((a) ->
        {
            try
            {
                OtsEditor.this.evalWrapper
                        .getEval(OtsEditor.this.scenario.getItemAt(OtsEditor.this.scenario.getSelectedIndex()));
            }
            catch (CircularDependencyException exception)
            {
                showCircularInputParameters();
            }
        });
        controlsContainer.add(this.scenario);
        controlsContainer.add(Box.createHorizontalStrut(2));
        JButton playRun = new JButton();
        playRun.setToolTipText("Run single run");
        playRun.setIcon(loadIcon("./Play.png", 18, 18, -1, -1));
        playRun.setMinimumSize(new Dimension(24, 24));
        playRun.setMaximumSize(new Dimension(24, 24));
        playRun.setPreferredSize(new Dimension(24, 24));
        playRun.addActionListener((a) -> runSingle());
        controlsContainer.add(playRun);
        JButton playScenario = new JButton();
        playScenario.setToolTipText("Run scenario (batch)");
        playScenario.setIcon(loadIcon("./NextTrack.png", 18, 18, -1, -1));
        playScenario.setMinimumSize(new Dimension(24, 24));
        playScenario.setMaximumSize(new Dimension(24, 24));
        playScenario.setPreferredSize(new Dimension(24, 24));
        playScenario.addActionListener((a) -> runBatch(false));
        controlsContainer.add(playScenario);
        JButton playAll = new JButton();
        playAll.setToolTipText("Run all (batch)");
        playAll.setIcon(loadIcon("./Last_recor.png", 18, 18, -1, -1));
        playAll.setMinimumSize(new Dimension(24, 24));
        playAll.setMaximumSize(new Dimension(24, 24));
        playAll.setPreferredSize(new Dimension(24, 24));
        playAll.addActionListener((a) -> runBatch(true));
        controlsContainer.add(playAll);
        controlsContainer.add(Box.createHorizontalStrut(4));

        rightContainer.add(controlsContainer);
        rightContainer.add(this.rightSplitPane);
        this.leftRightSplitPane.setRightComponent(rightContainer);

        this.questionIcon = loadIcon("./Question.png", -1, -1, -1, -1);

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
                new AttributeCellRenderer(loadIcon("./Info.png", 12, 12, 16, 16)));
        AttributesCellEditor editor = new AttributesCellEditor(this.attributesTable, this);
        this.attributesTable.setDefaultEditor(String.class, editor);
        this.attributesTable.addMouseListener(new AttributesMouseListener(this, this.attributesTable));
        this.attributesTable.getSelectionModel()
                .addListSelectionListener(new AttributesListSelectionListener(this, this.attributesTable));
        AttributesTableModel.applyColumnWidth(this.attributesTable);
        this.rightSplitPane.setBottomComponent(new JScrollPane(this.attributesTable));

        addMenuBar();

        this.statusLabel = new StatusLabel();
        this.statusLabel.setForeground(STATUS_COLOR);
        this.statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        this.statusLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        add(this.statusLabel, BorderLayout.SOUTH);
        removeStatusLabel();
    }

    /**
     * Run a single simulation run.
     */
    private void runSingle()
    {
        if (!((XsdTreeNode) this.treeTable.getTree().getModel().getRoot()).isValid())
        {
            showInvalidMessage();
            return;
        }
        File file;
        try
        {
            file = File.createTempFile("ots_", ".xml");
        }
        catch (IOException exception)
        {
            showUnableToRun();
            return;
        }
        save(file, (XsdTreeNodeRoot) this.treeTable.getTree().getModel().getRoot(), false);
        int index = this.scenario.getSelectedIndex();
        if (index == 0)
        {
            OtsRunner.runSingle(file, null);
        }
        else
        {
            String scenario = this.scenario.getItemAt(index).getScenarioNode().getId();
            OtsRunner.runSingle(file, scenario);
        }
        file.delete();
    }

    /**
     * Batch run.
     * @param all boolean; all scenarios, or only the selected scenario.
     */
    protected void runBatch(final boolean all)
    {
        if (!((XsdTreeNode) this.treeTable.getTree().getModel().getRoot()).isValid())
        {
            showInvalidMessage();
            return;
        }
        // TODO: should probably create a utility to run from XML as demo fromXML, but with batch function too
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
     * @return Undo; undo unit.
     */
    public Undo getUndo()
    {
        return this.undo;
    }

    /**
     * Shows and selects the given node in the tree.
     * @param node XsdTreeNode; node.
     * @param attribute String; attribute name, may be {@code null} to just show the node.
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
        TreePath partialPath = new TreePath(nodePath.subList(0, nodePath.size() - 1).toArray());
        this.treeTable.getTree().expandPath(partialPath);
        TreePath path = new TreePath(node.getPath().toArray());
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

        if (node.isActive())
        {
            this.attributesTable.setModel(new AttributesTableModel(node, this.treeTable));
        }
        else
        {
            this.attributesTable.setModel(new AttributesTableModel(null, this.treeTable));
        }
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
     * @param label String; status label.
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
        JMenuItem exit = new JMenuItem("Exit");
        fileMenu.add(exit);
        exit.addActionListener((a) -> exit());

        JMenu editMenu = new JMenu("Edit");
        menuBar.add(editMenu);

        JMenuItem undoItem = new JMenuItem("Undo");
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
        editMenu.add(undoItem);
        undoItem.addActionListener((a) ->
        {
            if (undoItem.isEnabled())
            {
                OtsEditor.this.undo.undo();
            }
        });

        JMenuItem redoItem = new JMenuItem("Redo");
        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
        editMenu.add(redoItem);
        redoItem.addActionListener((a) ->
        {
            if (redoItem.isEnabled())
            {
                OtsEditor.this.undo.redo();
            }
        });
        this.undo = new Undo(this, undoItem, redoItem);

        JMenu navigateMenu = new JMenu("Navigate");
        menuBar.add(navigateMenu);
        this.backItem = new JMenuItem("Go back");
        this.backItem.setEnabled(false);
        this.backItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        navigateMenu.add(this.backItem);
        this.backItem.addActionListener((a) ->
        {
            if (OtsEditor.this.backNode != null)
            {
                OtsEditor.this.backItem.setEnabled(false);
                show(OtsEditor.this.backNode, OtsEditor.this.backAttribute);
            }
        });
        this.coupledItem = new JMenuItem("Go to coupled item");
        this.coupledItem.setEnabled(false);
        this.coupledItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
        navigateMenu.add(this.coupledItem);
        this.coupledItem.addActionListener((a) ->
        {
            if (OtsEditor.this.coupledNode != null)
            {
                OtsEditor.this.backNode = OtsEditor.this.candidateBackNode;
                OtsEditor.this.backAttribute = OtsEditor.this.candidateBackAttribute;
                OtsEditor.this.backItem.setEnabled(OtsEditor.this.backNode != null);
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
        List<String> files = this.applicationStore.getRecentFiles("recent_files");
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
                                    "File could not be loaded. Do you want ro remove it from recent files?",
                                    "Remove from recent files?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                                    this.questionIcon) == JOptionPane.OK_OPTION;
                            if (remove)
                            {
                                this.applicationStore.removeRecentFile("recent_files", file);
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
                this.applicationStore.clearProperty("recent_files");
                updateRecentFileMenu();
            }
        });
        this.recentFilesMenu.add(item);
    }

    /**
     * Sets coupled node from user action, i.e. the node that contains the key value to which a user selected node with keyref
     * refers to.
     * @param coupledNode XsdTreeNode; key node that is coupled to from a keyref node, may be {@code null}.
     * @param backNode XsdTreeNode; keyref node that is coupled from to a key node, may be {@code null}.
     * @param backAttribute String; attribute in keyref node that refers to coupled node, may be {@code null}.
     */
    public void setCoupledNode(final XsdTreeNode coupledNode, final XsdTreeNode backNode, final String backAttribute)
    {
        this.coupledItem.setEnabled(coupledNode != null);
        this.coupledItem.setText(coupledNode == null ? "Go to coupled item"
                : ("Go to " + (backAttribute != null ? backNode.getAttributeValue(backAttribute)
                        : (backNode.isIdentifiable() ? backNode.getId() : backNode.getValue()))));
        this.coupledNode = coupledNode;
        this.candidateBackNode = backNode;
        this.candidateBackAttribute = backAttribute;
    }

    /**
     * Sets whether there are unsaved changes, resulting in a * in the window name, and confirmation pop-ups upon file changes.
     * @param unsavedChanges boolean; whether there are unsaved changes.
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
     * @param xsdDocument Document; main node from an XSD schema file.
     * @throws IOException when a resource could not be loaded.
     */
    @SuppressWarnings("checkstyle:hiddenfield")
    public void setSchema(final Document xsdDocument) throws IOException
    {
        this.xsdDocument = xsdDocument;
        this.undo.setIgnoreChanges();
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
                            "File could not be loaded. Do you want ro remove it from recent files?",
                            "Remove from recent files?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
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
                XsdTreeNode node = (XsdTreeNode) this.treeTable.getTree().getModel().getRoot();
                this.undo.startAction(ActionType.ADD, node, null);
                initializeTree();
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

        addTreeTableListeners();

        int dividerLocation = this.rightSplitPane.getDividerLocation();
        this.rightSplitPane.setTopComponent(null);
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
            /** {@inheritDoc} */
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
        setAppearance(getAppearance());
    }

    /**
     * Adds all listeners to the tree table.
     * @throws IOException on exception.
     */
    private void addTreeTableListeners() throws IOException
    {
        // this listener changes Id or node value values for each key being pressed
        DefaultCellEditor editor = (DefaultCellEditor) this.treeTable.getDefaultEditor(String.class);
        ((JTextField) editor.getComponent()).addKeyListener(new KeyAdapter()
        {
            /** {@inheritDoc} */
            @Override
            public void keyReleased(final KeyEvent e)
            {
                int editorCol = OtsEditor.this.treeTable.convertColumnIndexToView(OtsEditor.this.treeTable.getSelectedColumn());
                if (editorCol == 1 || editorCol == 2)
                {
                    int row = OtsEditor.this.treeTable.getSelectedRow();
                    int col = OtsEditor.this.treeTable.convertColumnIndexToView(0); // columns may have been moved in view
                    XsdTreeNode treeNode = (XsdTreeNode) OtsEditor.this.treeTable.getValueAt(row, col);
                    if (editorCol == 1)
                    {
                        treeNode.setId(((JTextField) e.getComponent()).getText());
                    }
                    else if (editorCol == 2)
                    {
                        treeNode.setValue(((JTextField) e.getComponent()).getText());
                    }
                }
            }
        });

        // this listener starts a new undo event when the editor gets focus on the JTreeTable
        ((JTextField) editor.getComponent()).addFocusListener(new FocusListener()
        {
            /** {@inheritDoc} */
            @Override
            public void focusGained(final FocusEvent e)
            {
                startUndoActionOnTreeTable();
            }

            /** {@inheritDoc} */
            @Override
            public void focusLost(final FocusEvent e)
            {
                startUndoActionOnTreeTable();
            }
        });

        // this listener may cause new undo actions when cells are navigated using the keyboard
        editor.addCellEditorListener(new CellEditorListener()
        {
            /** {@inheritDoc} */
            @Override
            public void editingStopped(final ChangeEvent e)
            {
                startUndoActionOnTreeTable();
            }

            /** {@inheritDoc} */
            @Override
            public void editingCanceled(final ChangeEvent e)
            {
                startUndoActionOnTreeTable();
            }
        });

        // throws selection events and updates the attributes table
        this.treeTable.getTree()
                .addTreeSelectionListener(new XsdTreeSelectionListener(this, this.treeTable, this.attributesTable));

        // sets custom icon, prepends grouping icon, and appends the choice icon for choice nodes
        this.treeTable.getTree().setCellRenderer(new XsdTreeCellRenderer(this));

        // this listener will make sure no choice popup is presented by a left-click on expand/collapse, even for a choice node
        this.treeTable.getTree().addTreeWillExpandListener(new TreeWillExpandListener()
        {
            /** {@inheritDoc} */
            @Override
            public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException
            {
                OtsEditor.this.mayPresentChoice = false;
            }

            /** {@inheritDoc} */
            @Override
            public void treeWillCollapse(final TreeExpansionEvent event) throws ExpandVetoException
            {
                OtsEditor.this.mayPresentChoice = false;
            }
        });

        // this listener makes sure that a choice popup can be presented again after a left-click on an expansion/collapse node
        // it also shows the tooltip in tree nodes
        this.treeTable.addMouseMotionListener(new MouseMotionAdapter()
        {
            /** {@inheritDoc} */
            @Override
            public void mouseMoved(final MouseEvent e)
            {
                OtsEditor.this.mayPresentChoice = true;

                // ToolTip
                int row = OtsEditor.this.treeTable.rowAtPoint(e.getPoint());
                int col = OtsEditor.this.treeTable.convertColumnIndexToView(0); // columns may have been moved in view
                XsdTreeNode treeNode = (XsdTreeNode) OtsEditor.this.treeTable.getValueAt(row, col);
                try
                {
                    if (!treeNode.isSelfValid())
                    {
                        OtsEditor.this.treeTable.getTree().setToolTipText(treeNode.reportInvalidNode());
                    }
                    else
                    {
                        OtsEditor.this.treeTable.getTree().setToolTipText(null);
                    }
                }
                catch (Exception ex)
                {
                    if (treeNode.isIdentifiable())
                    {
                        System.out.println("Node " + treeNode.getId() + " no valid.");
                    }
                    else
                    {
                        System.out.println("Node " + treeNode.getNodeName() + " no valid.");
                    }
                }
            }
        });

        // this listener opens the attributes of a node, and presents the popup for a choice or for addition/deletion of nodes
        this.treeTable.addMouseListener(new XsdTreeMouseListener(this, this.treeTable, this.attributesTable));

        // this listener removes the selected node, if it is removable
        this.treeTable.addKeyListener(new XsdTreeKeyListener(this, this.treeTable));
    }

    /**
     * Creates a new undo action as the selection is changed in the tree table.
     */
    public void startUndoActionOnTreeTable()
    {
        XsdTreeNode node = (XsdTreeNode) this.treeTable.getValueAt(this.treeTable.getSelectedRow(), 0);
        int col = this.treeTable.convertColumnIndexToView(this.treeTable.getSelectedColumn());
        if (col == 1)
        {
            this.undo.startAction(ActionType.ID_CHANGE, node, null);
        }
        else if (col == 2)
        {
            this.undo.startAction(ActionType.VALUE_CHANGE, node, null);
        }
    }

    /**
     * Adds a listener to a popup to remove the popop from the component when the popup becomes invisible. This makes sure that
     * a right-clicks on another location that should show a different popup, is not overruled by the popup of a previous click.
     * @param popup JPopupMenu; popup menu.
     * @param component JComponent; component from which the menu will be removed.
     */
    public void preparePopupRemoval(final JPopupMenu popup, final JComponent component)
    {
        popup.addPopupMenuListener(new PopupMenuListener()
        {
            /** {@inheritDoc} */
            @Override
            public void popupMenuWillBecomeVisible(final PopupMenuEvent e)
            {
            }

            /** {@inheritDoc} */
            @Override
            public void popupMenuWillBecomeInvisible(final PopupMenuEvent e)
            {
                component.setComponentPopupMenu(null);
                OtsEditor.this.choiceNode = null;
            }

            /** {@inheritDoc} */
            @Override
            public void popupMenuCanceled(final PopupMenuEvent e)
            {
            }
        });
    }

    /**
     * Sets a custom icon for nodes that comply to the path. The path may be an absolute path (e.g. "Ots.Network.Connector") or
     * a relative path (e.g. ".Node"). The image should be a filename relative in resources.
     * @param path String; path.
     * @param icon ImageIcon; image icon.
     */
    public void setCustomIcon(final String path, final ImageIcon icon)
    {
        this.customIcons.put(path, icon);
    }

    /**
     * Loads an icon, possibly rescaled.
     * @param image String; image filename, relative in resources.
     * @param width int; width to resize to, may be -1 to leave as is.
     * @param height int; width to resize to, may be -1 to leave as is.
     * @param bgWidth int; background image width icon will be centered in, may be -1 to leave as is.
     * @param bgHeight int; background image height icon will be centered in, may be -1 to leave as is.
     * @return ImageIcon; image icon.
     * @throws IOException if the file is not in resources.
     */
    public static ImageIcon loadIcon(final String image, final int width, final int height, final int bgWidth,
            final int bgHeight) throws IOException
    {
        Image im = ImageIO.read(Resource.getResourceAsStream(image));
        if (width > 0 || height > 0)
        {
            im = im.getScaledInstance(width > 0 ? width : im.getWidth(null), height > 0 ? height : im.getHeight(null),
                    Image.SCALE_SMOOTH);
        }
        if (bgWidth > 0 && bgHeight > 0)
        {
            BufferedImage bg = new BufferedImage(bgWidth > 0 ? bgWidth : im.getWidth(null),
                    bgHeight > 0 ? bgHeight : im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics g = bg.getGraphics();
            g.drawImage(im, (bg.getWidth() - im.getWidth(null)) / 2, (bg.getHeight() - im.getHeight(null)) / 2, null);
            im = bg;
        }
        return new ImageIcon(im);
    }

    /**
     * Obtains a custom icon for the path, or {@code null} if there is no custom icon specified for the path.
     * @param path String; node path.
     * @return Icon; custom icon, or {@code null} if there is no custom icon specified for the path.
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
     * @param choiceNode XsdTreeNode; node that is the currently selected choice.
     */
    public void setChoiceNode(final XsdTreeNode choiceNode)
    {
        this.choiceNode = choiceNode;
    }

    /**
     * Returns whether a choice may be presented.
     * @return whether a choice may be presented.
     */
    public boolean mayPresentChoice()
    {
        return this.mayPresentChoice;
    }

    /** {@inheritDoc} */
    @Override
    public EventListenerMap getEventListenerMap() throws RemoteException
    {
        return this.listenerMap;
    }

    /**
     * Adds a tab to the main window.
     * @param name String; name of the tab.
     * @param icon Icon; icon for the tab, may be {@code null}.
     * @param component Component; component that will fill the tab.
     * @param tip String; tool-tip for the tab, may be {@code null}.
     */
    public void addTab(final String name, final Icon icon, final Component component, final String tip)
    {
        this.visualizationPane.addTab(name, icon, component, tip);
    }

    /**
     * Returns the component of the tab with given name.
     * @param name String; name of the tab.
     * @return Component; component of the tab with given name.
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
     * @param name String; name of the tab.
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
     * @param node XsdTreeNode; node.
     * @return boolean; {@code true} if the user confirms node removal.
     */
    public boolean confirmNodeRemoval(final XsdTreeNode node)
    {
        return JOptionPane.showConfirmDialog(this, "Remove `" + node + "`?", "Remove?", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, this.questionIcon) == JOptionPane.OK_OPTION;
    }

    /**
     * Shows a dialog in a modal pane to confirm discarding unsaved changes.
     * @return boolean; whether unsaved changes can be discarded.
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
     * @param description String; description.
     */
    public void showDescription(final String description)
    {
        JOptionPane.showMessageDialog(OtsEditor.this,
                "<html><body><p style='width: 400px;'>" + description + "</p></body></html>");
    }

    /**
     * Show tree invalid.
     */
    public void showInvalidMessage()
    {
        JOptionPane.showMessageDialog(OtsEditor.this, "The tree is not valid. Make sure no red nodes remain.");
    }

    /**
     * Show input parameters have a circular dependency.
     */
    public void showCircularInputParameters()
    {
        JOptionPane.showMessageDialog(OtsEditor.this, "Input parameters have a circular dependency.");
    }

    /**
     * Show unable to run.
     */
    public void showUnableToRun()
    {
        JOptionPane.showMessageDialog(OtsEditor.this, "Unable to run, temporary file could not be saved.");
    }

    /**
     * Places a popup with options under the cell that is being clicked in a table. The popup will show items relevant to what
     * is being typed in the cell. The maximum number of items shown is limited to {@code MAX_DROPDOWN_ITEMS}.
     * @param allOptions List&lt;String&gt;; list of all options, will be filtered when typing.
     * @param table JTable; table, will be either the tree table or the attributes table.
     * @param action Consumer&lt;String&gt;; action to perform based on the option in the popup that was selected.
     */
    public void optionsPopup(final List<String> allOptions, final JTable table, final Consumer<String> action)
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
        for (String option : options)
        {
            JMenuItem item = new JMenuItem(option);
            item.setVisible(index++ < MAX_DROPDOWN_ITEMS);
            item.addActionListener(new ActionListener()
            {
                /** {@inheritDoc} */
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    action.accept(option);
                    CellEditor cellEditor = table.getCellEditor();
                    if (cellEditor != null)
                    {
                        cellEditor.cancelCellEditing();
                    }
                    OtsEditor.this.treeTable.updateUI();
                }
            });
            item.setFont(table.getFont());
            popup.add(item);
        }
        this.dropdownIndent = 0;
        popup.addMouseWheelListener(new MouseWheelListener()
        {

            /** {@inheritDoc} */
            @Override
            public void mouseWheelMoved(final MouseWheelEvent e)
            {
                OtsEditor.this.dropdownIndent += (e.getWheelRotation() * e.getScrollAmount());
                OtsEditor.this.dropdownIndent = OtsEditor.this.dropdownIndent < 0 ? 0 : OtsEditor.this.dropdownIndent;
                int maxIndent = OtsEditor.this.dropdownOptions.size() - MAX_DROPDOWN_ITEMS;
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
        SwingUtilities.invokeLater(new Runnable()
        {
            /** {@inheritDoc} */
            @Override
            public void run()
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
                    /** {@inheritDoc} */
                    @Override
                    public void keyTyped(final KeyEvent e)
                    {
                        // invoke later to include this current typed key in the result
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            /** {@inheritDoc} */
                            @Override
                            public void run()
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
                                    item.addActionListener(new ActionListener()
                                    {
                                        /** {@inheritDoc} */
                                        @Override
                                        public void actionPerformed(final ActionEvent e)
                                        {
                                            action.accept(currentValue);
                                            CellEditor cellEditor = table.getCellEditor();
                                            if (cellEditor != null)
                                            {
                                                cellEditor.cancelCellEditing();
                                            }
                                            OtsEditor.this.treeTable.updateUI();
                                        }
                                    });
                                    item.setFont(table.getFont());
                                    popup.add(item);
                                }
                                popup.pack();
                                placePopup(popup, rectangle, table);
                            }
                        });
                    }
                });
                field.addActionListener(new ActionListener()
                {
                    /** {@inheritDoc} */
                    @Override
                    public void actionPerformed(final ActionEvent e)
                    {
                        popup.setVisible(false);
                        table.setComponentPopupMenu(null);
                    }
                });
            }
        });
    }

    /**
     * Updates the options that are shown within an dropdown menu based on an indent from scrolling.
     * @param popup JPopupMenu; dropdown menu.
     * @return boolean; whether at least one item is visible.
     */
    private boolean showOptionsInScope(final JPopupMenu popup)
    {
        int optionIndex = 0;
        for (Component component : popup.getComponents())
        {
            JMenuItem item = (JMenuItem) component;
            boolean visible =
                    optionIndex < MAX_DROPDOWN_ITEMS && this.dropdownOptions.indexOf(item.getText()) >= this.dropdownIndent;
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
     * @param popup JPopupMenu; popup.
     * @param rectangle Rectangle; rectangle of cell being edited, relative to the parent component.
     * @param parent JComponent; component containing the cell.
     */
    private void placePopup(final JPopupMenu popup, final Rectangle rectangle, final JComponent parent)
    {
        Point pAttributes = parent.getLocationOnScreen();
        // cannot use screen size in case of multiple monitors, so we keep the popup on the JFrame rather than the window
        Dimension windowSize = OtsEditor.this.getSize();
        Point pWindow = OtsEditor.this.getLocationOnScreen();
        if (pAttributes.y + (int) rectangle.getMaxY() + popup.getBounds().getHeight() > windowSize.height + pWindow.y - 1)
        {
            popup.setLocation(pAttributes.x + (int) rectangle.getMinX(),
                    pAttributes.y + (int) rectangle.getMinY() - 1 - (int) popup.getBounds().getHeight());
        }
        else
        {
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
        fileDialog.setFilenameFilter(new FilenameFilter()
        {
            /** {@inheritDoc} */
            @Override
            public boolean accept(final File dir, final String name)
            {
                return name.toLowerCase().endsWith(".xml");
            }
        });
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
     * @param file File; file to load.
     * @param status String; status message in status bar to show upon loading.
     * @param updateRecentFiles boolean; whether to include the opened file in recent files.
     * @return boolean; whether the file was successfully loaded.
     */
    private boolean loadFile(final File file, final String status, final boolean updateRecentFiles)
    {
        try
        {
            Document document = DocumentReader.open(file.toURI());
            this.undo.setIgnoreChanges();
            initializeTree();
            XsdTreeNodeRoot root = (XsdTreeNodeRoot) OtsEditor.this.treeTable.getTree().getModel().getRoot();
            root.setDirectory(this.lastDirectory);
            root.loadXmlNodes(document.getFirstChild());
            this.undo.clear();
            setUnsavedChanges(false);
            setStatusLabel(status);
            this.undo.updateButtons();
            this.backItem.setEnabled(false);
            this.coupledItem.setEnabled(false);
            this.coupledItem.setText("Go to coupled item");
            this.treeTable.updateUI(); // knowing/changing the directory may change validation status
            if (updateRecentFiles)
            {
                this.applicationStore.addRecentFile("recent_files", file.getAbsolutePath());
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
     * Saves the file is a file name is known, otherwise forwards to {@code saveFileAs()}.
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
     * @param root XsdTreeNode; root node of tree to save, can be a sub-tree of the full tree.
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
            ((XsdTreeNodeRoot) root).setDirectory(this.lastDirectory);
            this.treeTable.updateUI();
            this.attributesTable.updateUI();
            setUnsavedChanges(false);
        }
        setStatusLabel("Saved");
    }

    /**
     * Performs the actual saving, either from {@code saveFile()} or {@code saveFileAs()}.
     * @param file File; file to save.
     * @param root XsdTreeNode; root node of tree to save, can be a sub-tree of the full tree.
     * @param storeAsRecent boolean; whether to store the file under recent files.
     */
    private void save(final File file, final XsdTreeNode root, final boolean storeAsRecent)
    {
        try
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
            xmlRoot.setAttribute("xmlns:ots", "http://www.opentrafficsim.org/ots");
            xmlRoot.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            if (root instanceof XsdTreeNodeRoot)
            {
                XsdTreeNodeRoot otsRoot = (XsdTreeNodeRoot) root;
                if (otsRoot.getSchemaLocation() != null)
                {
                    xmlRoot.setAttribute("xsi:schemaLocation", otsRoot.getSchemaLocation());
                }
            }
            xmlRoot.setAttribute("xmlns:xi", "http://www.w3.org/2001/XInclude");

            FileOutputStream fileOutputStream = new FileOutputStream(file);
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
                this.applicationStore.addRecentFile("recent_files", file.getAbsolutePath());
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
     * @param message String; tooltip message, may be {@code null}.
     * @return String; possibly shortened tooltip message.
     */
    public static String limitTooltip(final String message)
    {
        if (message == null)
        {
            return null;
        }
        if (message.length() < MAX_TOOLTIP_LENGTH)
        {
            return message;
        }
        return message.substring(0, MAX_TOOLTIP_LENGTH - 3) + "...";
    }

    /**
     * Filter options, leaving only those that start with the current value.
     * @param options List&lt;String&gt;; options to filter.
     * @param currentValue String; current value.
     * @return List&lt;String&gt;; filtered options.
     */
    private static List<String> filterOptions(final List<String> options, final String currentValue)
    {
        return options.stream().filter((val) -> currentValue == null || currentValue.isEmpty() || val.startsWith(currentValue))
                .distinct().sorted().collect(Collectors.toList());
    }

    /**
     * Adds a listener to the cell editor of the attributes table.
     * @param listener CellEditorListener; listener to the cell editor of the attributes table.
     */
    public void addAttributeCellEditorListener(final CellEditorListener listener)
    {
        this.attributesTable.getDefaultEditor(String.class).addCellEditorListener(listener);
    }

    /**
     * Sets a node in the clipboard.
     * @param clipboard XsdTreeNode; node to set in the clipboard.
     * @param cut boolean; whether the node was cut.
     */
    public void setClipboard(final XsdTreeNode clipboard, final boolean cut)
    {
        this.clipboard = clipboard;
        this.cut = cut;
    }

    /**
     * Returns the clipboard node.
     * @return XsdTreeNode; clipboard node.
     */
    public XsdTreeNode getClipboard()
    {
        return this.clipboard;
    }

    /**
     * Remove node that was cut.
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
     * @return NodeActions; node actions.
     */
    public NodeActions getNodeActions()
    {
        return this.nodeActions;
    }

    /** {@inheritDoc} */
    @Override
    public boolean addListener(final EventListener listener, final EventType eventType)
    {
        return Try.assign(() -> EventProducer.super.addListener(listener, eventType),
                "Local event producer should not give a RemoteException.");
    }

    /**
     * Return an evaluator to evaluate expression values. This evaluator uses the input parameters of the currently selected
     * scenario.
     * @return Eval; evaluator to evaluate expression values.
     */
    public Eval getEval()
    {
        try
        {
            return this.evalWrapper.getEval(OtsEditor.this.scenario.getItemAt(OtsEditor.this.scenario.getSelectedIndex()));
        }
        catch (CircularDependencyException ex)
        {
            showCircularInputParameters();
            return this.evalWrapper.getLastValidEval();
        }
    }

    /**
     * Adds listener to changes in the evaluator, i.e. added, removed or changed input parameters.
     * @param listener EvalListener; listener.
     */
    public void addEvalListener(final EvalListener listener)
    {
        this.evalWrapper.addListener(listener);
    }

    /**
     * Removes listener to changes in the evaluator, i.e. added, removed or changed input parameters.
     * @param listener EvalListener; listener.
     */
    public void removeEvalListener(final EvalListener listener)
    {
        this.evalWrapper.removeListener(listener);
    }

    /** {@inheritDoc} */
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
