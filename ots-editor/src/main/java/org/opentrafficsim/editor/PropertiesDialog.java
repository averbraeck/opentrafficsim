package org.opentrafficsim.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Shows a modal dialog to edit properties (of the main XML tag).
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class PropertiesDialog extends JDialog
{

    /** */
    private static final long serialVersionUID = 20250918L;

    /**
     * Constructor.
     * @param owner owner
     * @param properties list of properties (keys even indices, values at uneven)
     * @param questionMark question mark icon
     */
    public PropertiesDialog(final OtsEditor owner, final List<String> properties, final ImageIcon questionMark)
    {
        super(owner, "Properties", true);

        // main panel
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        // columns
        TableColumnModel columns = new DefaultTableColumnModel();
        TableColumn column1 = new TableColumn(0, 200);
        column1.setHeaderValue("Property");
        columns.addColumn(column1);
        TableColumn column2 = new TableColumn(1, 600);
        column2.setHeaderValue("Value");
        columns.addColumn(column2);
        // model
        TableModel model = new AbstractTableModel()
        {
            /** */
            private static final long serialVersionUID = 20240314L;

            @Override
            public int getRowCount()
            {
                return properties.size() / 2;
            }

            @Override
            public int getColumnCount()
            {
                return 2;
            }

            @Override
            public Object getValueAt(final int rowIndex, final int columnIndex)
            {
                return properties.get(rowIndex * 2 + columnIndex);
            }

            @Override
            public boolean isCellEditable(final int rowIndex, final int columnIndex)
            {
                return columnIndex == 1;
            }

            @Override
            public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex)
            {
                properties.set(rowIndex * 2 + columnIndex, aValue.toString());
                owner.setUnsavedChanges(true);
            }
        };
        // table
        JTable table = new JTable(model, columns);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer()
        {
            /** */
            private static final long serialVersionUID = 20240314L;

            @Override
            public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
                    final boolean hasFocus, final int row, final int column)
            {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (table.convertColumnIndexToModel(column) == 0)
                {
                    setOpaque(true);
                }
                else
                {
                    setOpaque(false);
                }
                return component;
            }
        };
        renderer.setBackground(panel.getBackground()); // for editable cells
        table.setDefaultRenderer(Object.class, renderer);
        JScrollPane scroll = new JScrollPane(table); // put table in scroll window, also makes the header visible
        scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        panel.add(scroll, BorderLayout.CENTER);
        // dialog
        setMinimumSize(new Dimension(250, 150));
        setPreferredSize(new Dimension(800, 200));
        // buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton defaults = new JButton("Set defaults...");
        defaults.addActionListener((a) ->
        {
            boolean set = JOptionPane.showConfirmDialog(PropertiesDialog.this, "Are you sure? This will reset all properties.",
                    "Are you sure?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                    questionMark) == JOptionPane.OK_OPTION;
            if (set)
            {
                table.getDefaultEditor(Object.class).stopCellEditing();
                owner.setDefaultProperties();
                table.updateUI();
                owner.setUnsavedChanges(true);
            }
        });
        buttons.add(defaults);
        JButton ok = new JButton("Ok");
        ok.addActionListener((a) ->
        {
            table.getDefaultEditor(Object.class).stopCellEditing();
            dispose();
        });
        buttons.add(ok);
        panel.add(buttons, BorderLayout.PAGE_END);
        // pack and visualize
        getContentPane().add(panel);
        pack();
        setLocationRelativeTo(this);
        setVisible(true);
    }

}
