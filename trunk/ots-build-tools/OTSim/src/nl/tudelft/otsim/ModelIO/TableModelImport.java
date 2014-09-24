package nl.tudelft.otsim.ModelIO;

/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
/*
 * TableRenderDemo.java requires no other files.
 */
 
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import nl.tudelft.otsim.GUI.Main;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
 
/** 
 * TableRenderDemo is just like TableDemo, except that it
 * explicitly initializes column sizes and it uses a combo box
 * as an editor for the Sport column.
 */
public class TableModelImport extends JPanel implements TableModelListener {
	private static final long serialVersionUID = 1L;
    private TableImport tableDirection;
    private TableImport tableField;
	
    public TableModelImport(String[] namesDropDown, Object[] types, Object[][] dataDir) {
        super(new GridBagLayout());
        
        GridBagConstraints gbConstraints = new GridBagConstraints();
        int gridX = 0; 
        int gridY = 0;
        
        gbConstraints.gridx = gridX;
        gbConstraints.gridy = gridY;
        gbConstraints.anchor = GridBagConstraints.PAGE_START;
        
        gbConstraints.gridwidth = 1;
        gbConstraints.weighty = 0.0;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.PAGE_START; 
        JLabel label = new JLabel("Direction indicator");     
        add(label, gbConstraints);
/*        Object[][] dataDir = {
			{"Direction indicator", "DIRECTION"},
			{"AB", "1"},
			{"BA", "2"},
			{"AB and BA", "3"},
			};*/
    	final String[] headerDir = {"Direction indicator", "Value"};
        Object[] longValues = {"Jane", "Kathy"};        
    	tableDirection = new TableImport(dataDir, headerDir, longValues);
    	tableDirection.getTable().setPreferredScrollableViewportSize(new Dimension(500, 70));
    	tableDirection.getTable().setFillsViewportHeight(true); 
    	tableDirection.getTable().getModel().addTableModelListener(this);
        JScrollPane scrollPane1 = new JScrollPane(tableDirection.getTable()); 
        //Set up column sizes.
        TableImport.initColumnSizes(tableDirection.getTable(), headerDir.length); 
        int dropDownColumnNumber = -1;
        TableColumn dropDownColumn = null;
        if (dropDownColumnNumber>= 0)
        	dropDownColumn = tableDirection.getTable().getColumnModel().getColumn(dropDownColumnNumber);
        //Fiddle with the dropdown column's cell editors/renderers.
        TableImport.setUpDropdownColumn(dropDownColumn, namesDropDown);   
        //Add the scroll pane to this panel.
        add(scrollPane1, gbConstraints);     
        
        gridY++;
        gbConstraints.gridwidth = 2;
        gbConstraints.weighty = 0.0;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        label = new JLabel("Enter corresponding attributes in 'Import' column");
        add(label, gbConstraints);

        gridY++;
        gbConstraints.gridx = gridX;
        gbConstraints.gridy = gridY;
        gbConstraints.gridwidth = 2;
        gbConstraints.ipady = 40;
        gbConstraints.weighty = 1.0;
        gbConstraints.insets = new Insets(10,8,8,20);  //top padding
        //Important: determines the type of data!!!!
        Object[] longValuesFields = types;
        	//{"Jane", "Kathy", Boolean.TRUE};
        final String[] header = {"OpenTraffic", "Import", "Derive"};

    	Object[][] dataFields = {
	    		{"point(s)", "the_geom", false},
	    		{"fromNode", "ANODE", false},
	    		{"toNode", "BNODE", false},
	    		{"capacity", "CAPACITYAB", false},
	    		{"lanes", "LANESAB", false},
	    		{"turnLanes", "LANESMASAB", false},
	    		{"exitLanes", "EXITLANEAB", false},
	    		{"maxSpeed", "SPEEDAB", false},       		
	    		{"length", "LENGTH", false}       		
    		};

        tableField = new TableImport(dataFields, header, longValuesFields);
        tableField.getTable().setPreferredScrollableViewportSize(new Dimension(500, 70));
        tableField.getTable().setFillsViewportHeight(true); 
        tableField.getTable().getModel().addTableModelListener(this);
        //Set up column sizes.
        TableImport.initColumnSizes(tableField.getTable(), header.length); 
        dropDownColumnNumber = 1;
        dropDownColumn = null;
        if (dropDownColumnNumber>= 0)
        	dropDownColumn = tableField.getTable().getColumnModel().getColumn(dropDownColumnNumber);
        //Fiddle with the dropdown column's cell editors/renderers.
        TableImport.setUpDropdownColumn(dropDownColumn, namesDropDown);
        JScrollPane scrollPane2 = new JScrollPane(tableField.getTable()); 
        //Add the scroll pane to this panel.
        add(scrollPane2, gbConstraints);
        
        gridY++;
        gbConstraints.gridx = gridX;
        gbConstraints.gridy = gridY;
        gbConstraints.gridwidth = 2;
        gbConstraints.weighty = 1.0;
        gbConstraints.anchor = GridBagConstraints.PAGE_END; //bottom of space
        JScrollPane scrollPane3 = new JScrollPane();
        add(scrollPane3, gbConstraints);
    }

	@Override
	public void tableChanged(TableModelEvent arg0) {
		Main.mainFrame.getImportModelShapeWizard().getFinishButton().setEnabled(true);
		for (int i = 1; i < this.getTableDirection().getTable().getRowCount(); i++) {
			Object test1 = this.getTableDirection().getTable().getValueAt(i, 1);
			if (test1.toString().isEmpty() )  {
				System.out.println("--" + test1 + "yes");
				Main.mainFrame.getImportModelShapeWizard().getFinishButton().setEnabled(false);
			}
		}
		for (int i = 1; i < this.getTableField().getTable().getRowCount(); i++) {
			Object test1 = this.getTableField().getTable().getValueAt(i, 1);
			Object test2 = this.getTableField().getTable().getValueAt(i, 2);
			if (test1.toString().isEmpty() && test2.toString() == "false")  {
				System.out.println("--" + test1 + "yes" + test2);
				Main.mainFrame.getImportModelShapeWizard().getFinishButton().setEnabled(false);
			}
		}
	}

	TableImport getTableDirection() {
		return tableDirection;
	}

	TableImport getTableField() {
		return tableField;
	}

}