package org.opentrafficsim.editor.decoration;

import java.util.function.Predicate;

import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdPaths;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * Sets the Id of a link if it is not defined yet, and both NodeStart and NodeEnd are defined.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AutomaticLinkId extends AbstractNodeDecoratorAttribute implements CellEditorListener
{

    /** */
    private static final long serialVersionUID = 20230910L;

    /** Last node. */
    protected XsdTreeNode lastNode = null;

    /** Last id. */
    protected String lastId = null;

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     */
    public AutomaticLinkId(final OtsEditor editor)
    {
        super(editor, (node) -> node.isType(XsdPaths.LINK), "NodeStart", "NodeEnd");
        editor.addAttributeCellEditorListener(this);
    }

    /**
     * Constructor for sub classes.
     * @param editor OtsEditor; editor.
     * @param predicate Predicate&lt;XsdTreeNode&gt;; predicate to accept nodes that should have this attribute decorator.
     * @param attributes String...; attributes to trigger on.
     */
    protected AutomaticLinkId(final OtsEditor editor, final Predicate<XsdTreeNode> predicate, final String... attributes)
    {
        super(editor, predicate, attributes);
        editor.addAttributeCellEditorListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public void notifyAttributeChanged(final XsdTreeNode node, final String attribute)
    {
        String nodeStart = node.getAttributeValue("NodeStart");
        String nodeEnd = node.getAttributeValue("NodeEnd");
        String id = node.getAttributeValue("Id");
        if (nodeStart != null && nodeEnd != null && id == null)
        {
            this.lastNode = node;
            this.lastId = debrace(nodeStart + "-" + nodeEnd);
        }
        else
        {
            this.lastNode = null;
            this.lastId = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void editingStopped(final ChangeEvent e)
    {
        if (this.lastNode != null)
        {
            this.lastNode.setId(this.lastId);
            this.lastNode = null;
            this.lastId = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void editingCanceled(final ChangeEvent e)
    {
        if (this.lastNode != null)
        {
            this.lastNode.setId(this.lastId);
            this.lastNode = null;
            this.lastId = null;
        }
    }

    /**
     * Returns a string with the { and } removed.
     * @param value String; candidate value.
     * @return String; string with the { and } removed
     */
    protected String debrace(final String value)
    {
        return value.replace("{", "").replace("}", "");
    }
}
