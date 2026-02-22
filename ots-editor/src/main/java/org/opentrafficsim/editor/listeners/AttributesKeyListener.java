package org.opentrafficsim.editor.listeners;

import javax.swing.JComponent;
import javax.swing.JTable;

import org.opentrafficsim.editor.Actions;
import org.opentrafficsim.editor.OtsEditor;

/**
 * Listener on the attributes table to show a description of the selected attribute when F1 is pressed or the invalid message of
 * an invalid attribute is selected and F2 is pressed.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AttributesKeyListener
{

    /**
     * Constructor.
     * @param editor editor
     * @param attributesTable tree table
     */
    public AttributesKeyListener(final OtsEditor editor, final JTable attributesTable)
    {
        Actions.bind(attributesTable, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                editor.actions().showAttributeDescription());
        Actions.bind(attributesTable, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, editor.actions().showAttributeInvalid());
    }

}
