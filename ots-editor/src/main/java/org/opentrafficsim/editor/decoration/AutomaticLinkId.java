package org.opentrafficsim.editor.decoration;

import java.rmi.RemoteException;
import java.util.function.Predicate;

import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * Sets the Id of a link if it is not defined yet, and both NodeStart and NodeEnd are defined.
 * @author wjschakel
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
     * @throws RemoteException if an exception occurs while adding as a listener.
     */
    public AutomaticLinkId(final OtsEditor editor) throws RemoteException
    {
        super(editor, (node) -> node.isType("Ots.Network.Link"), "NodeStart", "NodeEnd");
        editor.addAttributeCellEditorListener(this);
    }

    /**
     * Constructor for sub classes.
     * @param editor OtsEditor; editor.
     * @param predicate Predicate&lt;XsdTreeNode&gt;; predicate to accept nodes that should have this attribute decorator.
     * @param attributes String...; attributes to trigger on.
     * @throws RemoteException if an exception occurs while adding as a listener.
     */
    protected AutomaticLinkId(final OtsEditor editor, final Predicate<XsdTreeNode> predicate, final String... attributes)
            throws RemoteException
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
            this.lastId = nodeStart + "-" + nodeEnd;
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
}
