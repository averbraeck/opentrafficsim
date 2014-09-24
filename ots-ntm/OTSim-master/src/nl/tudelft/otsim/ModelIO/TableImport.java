package nl.tudelft.otsim.ModelIO;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class TableImport  extends JFrame {
	private static final long serialVersionUID = 1L;
	ArrayList<Object[]> rowAndColumn;
	String[] header;
    private static boolean DEBUG = false;
    private MyTableModel model;
    private JTable table;
    public final static String EMPTYCOLUMN = "no similar field / derive" ;
    // constructor that will display a JTable based on elements received as arguments

    public TableImport(Object[][] obj, String[] header, Object[] longValues)  {
        super("Static JTable example");
        // JPanel to horl the JTable
        JPanel panel = new JPanel(new BorderLayout());
        // constructor of JTable model
        model = new MyTableModel(obj, header, longValues);
        // the table from that model
	        table = new JTable(model);
	    }
 
	    public MyTableModel getModel() {
			return model;
		}

		public void setModel(MyTableModel model) {
			this.model = model;
		}

		public JTable getTable() {
			return table;
		}

		public void setTable(JTable table) {
			this.table = table;
		}

		public static void initColumnSizes(JTable table, Integer columns) {
	        MyTableModel model = (MyTableModel)table.getModel();
	        TableColumn column = null;
	        Component comp = null;
	        int headerWidth = 0;
	        int cellWidth = 0;
	        Object[] longValues = model.longValues;
	        TableCellRenderer headerRenderer =
	            table.getTableHeader().getDefaultRenderer();
	 
	        for (int i = 0; i < columns; i++) {
	            column = table.getColumnModel().getColumn(i);
	 
	            comp = headerRenderer.getTableCellRendererComponent(
	                                 null, column.getHeaderValue(),
	                                 false, false, 0, 0);
	            headerWidth = comp.getPreferredSize().width;
	 
	            comp = table.getDefaultRenderer(model.getColumnClass(i)).
	                             getTableCellRendererComponent(
	                                 table, longValues[i],
	                                 false, false, 0, i);
	            cellWidth = comp.getPreferredSize().width;
	 
	            if (DEBUG) {
	                System.out.println("Initializing width of column "
                                   + i + ". "
                                   + "headerWidth = " + headerWidth
                                   + "; cellWidth = " + cellWidth);
            }
            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }
 
    public static void setUpDropdownColumn(JTable table,
                                 TableColumn dropDownColumn, String[] names) {
    	if (dropDownColumn != null)   {
	        //Set up the editor for the cells.
	        JComboBox comboBox = new JComboBox();
	        comboBox.addItem(EMPTYCOLUMN);
	        for (int i = 0; i < names.length; i++)   {
	        	comboBox.addItem(names[i]);
	        }
	        dropDownColumn.setCellEditor(new DefaultCellEditor(comboBox));
	 
	        //Set up tool tips for the sport cells.
	        DefaultTableCellRenderer renderer =
	                new DefaultTableCellRenderer();
	        renderer.setToolTipText("Click for combo box");
	        dropDownColumn.setCellRenderer(renderer);
    	}
    }
  
	 class MyTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		ArrayList<Object[]> al;
        // the headers
        String[] header;
        Object[] longValues;
        // constructor
        MyTableModel(Object[][] obj, String[] header, Object[] longValues) {
            // save the header
            this.header = header;  
            // and the rows
            this.longValues = longValues;
            al = new ArrayList<Object[]>();
            // copy the rows into the ArrayList
	            for(int i = 0; i < obj.length; ++i)
	                al.add(obj[i]);
	        }
 

	        @Override
			public int getColumnCount() {
	            return header.length;
	        }
	 
	        @Override
			public int getRowCount() {
	            return al.size();
	        }
	 
	        @Override
			public String getColumnName(int col) {
	            return header[col];
	        }
	 
	        @Override
			public Object getValueAt(int row, int col) {
	            return al.get(row)[col];
	        }
	 
	        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        @Override
		public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }
 
        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        @Override
		public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            if (col < 1) {
                return false;
            } else {
                return true;
            }
        }
 
        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        @Override
		public void setValueAt(Object value, int row, int col) {
            if (DEBUG) {
                System.out.println("Setting value at " + row + "," + col
                                   + " to " + value
                                   + " (an instance of "
                                   + value.getClass() + ")");
            }
 
            al.get(row)[col] = value;
            fireTableCellUpdated(row, col);
 
            if (DEBUG) {
                System.out.println("New value of data:");
                printDebugData();
            }
        }
 
        private void printDebugData() {
            int numRows = getRowCount();
            int numCols = getColumnCount();
 
            for (int i=0; i < numRows; i++) {
                System.out.print("    row " + i + ":");
                for (int j=0; j < numCols; j++) {
                    System.out.print("  " + al.get(i)[j]);
                }
                System.out.println();
            }
            System.out.println("--------------------------");
        }
    }

}
