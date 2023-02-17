package org.opentrafficsim.editor;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.xml.parsers.ParserConfigurationException;

import org.opentrafficsim.swing.gui.Resource;
import org.xml.sax.SAXException;

import de.javagl.treetable.JTreeTable;

/**
 * Editor window to load, edit and save OTS XML files. The class uses an underlying data structure that is based on the XML
 * Schema for the XML (XSD).<br>
 * <br>
 * This functionality is currently in development.
 * @author wjschakel
 */
public class OtsEditor extends JFrame
{

    private static final long serialVersionUID = 20230217L;

    private static final int DIVIDER_SIZE = 3;

    private static final boolean UPDATE_SPLIT_WHILE_DRAGGING = true;

    private final JSplitPane mainSplitPane;

    private final JTabbedPane visualizationPane;

    private final JSplitPane rightSplitPain;

    private final JTreeTable treeTable;

    private final JTable attributesTable;

    public OtsEditor() throws SAXException, IOException, ParserConfigurationException
    {
        setSize(1280, 720);
        setTitle("Open Traffic Sim Editor");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // split panes
        this.mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, UPDATE_SPLIT_WHILE_DRAGGING);
        this.mainSplitPane.setDividerSize(DIVIDER_SIZE);
        this.mainSplitPane.setResizeWeight(0.5);
        add(this.mainSplitPane);
        this.rightSplitPain = new JSplitPane(JSplitPane.VERTICAL_SPLIT, UPDATE_SPLIT_WHILE_DRAGGING);
        this.rightSplitPain.setDividerSize(DIVIDER_SIZE);
        this.rightSplitPain.setResizeWeight(0.5);
        this.mainSplitPane.setRightComponent(this.rightSplitPain);

        // visualization pane
        this.visualizationPane = new JTabbedPane(JTabbedPane.BOTTOM, JTabbedPane.SCROLL_TAB_LAYOUT);
        this.visualizationPane.addTab("Map", buildMapPane());
        this.visualizationPane.addTab("Road layout", buildRoadLayoutPane());
        this.visualizationPane.addTab("OD", buildOdPane());
        this.visualizationPane.addTab("Parameters", buildParameterPane());
        this.visualizationPane.setPreferredSize(new Dimension(900, 900));
        this.mainSplitPane.setLeftComponent(this.visualizationPane);

        // There is likely a better way to do this, but setting the icons specific on the tree is impossible for collapsed and
        // expanded. Also in that case after deletion of a node, the tree appearance gets reset and java default icons appear.
        UIManager.put("Tree.collapsedIcon", getIcon("/OtsPlus.png", -1));
        UIManager.put("Tree.expandedIcon", getIcon("/OtsMinus.png", -1));
        UIManager.put("Tree.leafIcon", getIcon("/New_document.png", 12));
        UIManager.put("Tree.openIcon", getIcon("/Folder.png", 12));
        UIManager.put("Tree.closedIcon", getIcon("/Folder.png", 12));

        // tree table
        // TODO: dynamic link to resource in ots-parser-xml
        File file = new File("C:\\Users\\wjschakel\\git\\opentrafficsim\\ots-xsd\\src\\main\\resources\\xsd\\1.03.00\\ots.xsd");
        this.treeTable = new JTreeTable(new XsdTreeTableModel(XsdReader.open(file.toURI())));
        XsdTreeTableModel.applyColumnWidth(this.treeTable);
        this.treeTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(final MouseEvent e)
            {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1)
                {
                    // set node in attributes table
                    int row = OtsEditor.this.treeTable.rowAtPoint(e.getPoint());
                    int col = OtsEditor.this.treeTable.convertColumnIndexToView(0); // columns may have been moved in view
                    XsdTreeNode treeNode = (XsdTreeNode) OtsEditor.this.treeTable.getValueAt(row, col);
                    OtsEditor.this.attributesTable.setModel(new XsdAttributesTableModel(treeNode));
                }
                else if (e.getButton() == MouseEvent.BUTTON3)
                {
                    // delete node (and children)
                    int row = OtsEditor.this.treeTable.rowAtPoint(e.getPoint());
                    int col = OtsEditor.this.treeTable.convertColumnIndexToView(0); // columns may have been moved in view
                    XsdTreeNode treeNode = (XsdTreeNode) OtsEditor.this.treeTable.getValueAt(row, col);
                    if (treeNode.getParent() != null) // root has null parent
                    {
                        treeNode.remove();
                        OtsEditor.this.treeTable.updateUI();
                    }
                }
                else if (e.getClickCount() == 2)
                {
                    // repopulate node from xsd info
                    int row = OtsEditor.this.treeTable.rowAtPoint(e.getPoint());
                    int col = OtsEditor.this.treeTable.convertColumnIndexToView(0); // columns may have been moved in view
                    XsdTreeNode treeNode = (XsdTreeNode) OtsEditor.this.treeTable.getValueAt(row, col);
                    treeNode.reExpand();
                    OtsEditor.this.treeTable.updateUI();
                }
            }
        });
        // Cell renderer might be used to change what the tree nodes look like and can do
        // this.treeTable.getTree().setCellRenderer(new DefaultTreeCellRenderer());
        this.rightSplitPain.setTopComponent(new JScrollPane(this.treeTable));

        // attributes table
        XsdAttributesTableModel tableModel = new XsdAttributesTableModel(null);
        DefaultTableColumnModel columns = new DefaultTableColumnModel();
        TableColumn column1 = new TableColumn(0, 25); // may add TableCellRenderer/TableCellEditor here
        column1.setHeaderValue(tableModel.getColumnName(0));
        columns.addColumn(column1);
        TableColumn column2 = new TableColumn(1, 200);
        column2.setHeaderValue(tableModel.getColumnName(1));
        columns.addColumn(column2);
        TableColumn column3 = new TableColumn(2, 200);
        column3.setHeaderValue(tableModel.getColumnName(2));
        columns.addColumn(column3);
        this.attributesTable = new JTable(tableModel, columns);
        XsdAttributesTableModel.applyColumnWidth(this.attributesTable);
        this.attributesTable.setBorder(BorderFactory.createTitledBorder("Attributes"));
        this.rightSplitPain.setBottomComponent(new JScrollPane(this.attributesTable));

        // appear to the user
        setVisible(true);
        this.mainSplitPane.setDividerLocation(0.5);
        this.rightSplitPain.setDividerLocation(0.6);
    }

    private Icon getIcon(final String path, final int size) throws IOException
    {
        if (size > 0)
        {
            return new ImageIcon(new ImageIcon(ImageIO.read(Resource.getResourceAsStream(path))).getImage()
                    .getScaledInstance(size, size, Image.SCALE_SMOOTH));
        }
        return new ImageIcon(ImageIO.read(Resource.getResourceAsStream(path)));
    }

    private static JComponent buildMapPane()
    {
        JLabel map = new JLabel("map");
        map.setOpaque(true);
        map.setHorizontalAlignment(JLabel.CENTER);
        return map;
    }

    private static JComponent buildRoadLayoutPane()
    {
        JLabel roadLayout = new JLabel("road layout");
        roadLayout.setOpaque(true);
        roadLayout.setHorizontalAlignment(JLabel.CENTER);
        return roadLayout;
    }

    private static JComponent buildOdPane()
    {
        JLabel od = new JLabel("od");
        od.setOpaque(true);
        od.setHorizontalAlignment(JLabel.CENTER);
        return od;
    }

    private static JComponent buildParameterPane()
    {
        JLabel parameters = new JLabel("parameters");
        parameters.setOpaque(true);
        parameters.setHorizontalAlignment(JLabel.CENTER);
        return parameters;
    }

    public static void main(final String[] args) throws SAXException, IOException, ParserConfigurationException,
            ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
    {
        new OtsEditor();
    }

}
