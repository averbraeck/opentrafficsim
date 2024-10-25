package org.opentrafficsim.editor;

import org.opentrafficsim.swing.gui.AppearanceControl;

import de.javagl.treetable.JTreeTable;
import de.javagl.treetable.TreeTableModel;

/**
 * Prevents the TreeTable from showing appearance background color.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AppearanceControlTreeTable extends JTreeTable implements AppearanceControl
{

    /** */
    private static final long serialVersionUID = 20231016L;

    /**
     * Constructor.
     * @param treeTableModel tree table model.
     */
    public AppearanceControlTreeTable(final TreeTableModel treeTableModel)
    {
        super(treeTableModel);
    }

    @Override
    public boolean isFont()
    {
        return true;
    }

}
