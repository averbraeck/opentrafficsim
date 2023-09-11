package org.opentrafficsim.editor.decoration.validation;

import java.rmi.RemoteException;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.decoration.AbstractNodeDecorator;

/**
 * Validates that the two attributes of a node are not the same.
 * @author wjschakel
 */
public class AttributesNotEqualValidator extends AbstractNodeDecorator implements ValueValidator
{

    /** */
    private static final long serialVersionUID = 20230910L;

    /** Path location of nodes to attach to. */
    private final String path;

    /** First attribute to compare. */
    private final String attribute1;

    /** Second attribute to compare. */
    private final String attribute2;

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     * @param path String; path location of nodes to attach to.
     * @param attribute1 String; first attribute to compare.
     * @param attribute2 String; second attribute to compare.
     * @throws RemoteException if an exception occurs while adding as a listener.
     */
    public AttributesNotEqualValidator(final OtsEditor editor, final String path, final String attribute1,
            final String attribute2) throws RemoteException
    {
        super(editor);
        this.path = path;
        this.attribute1 = attribute1;
        this.attribute2 = attribute2;
    }

    /** {@inheritDoc} */
    @Override
    public String validate(final XsdTreeNode node)
    {
        if (!node.isActive())
        {
            return null;
        }
        String startNode = node.getAttributeValue(this.attribute1);
        if (startNode == null || startNode.isEmpty())
        {
            return null;
        }
        String endNode = node.getAttributeValue(this.attribute2);
        if (endNode == null || endNode.isEmpty() || !endNode.equals(startNode))
        {
            return null;
        }
        return this.attribute1 + " and " + this.attribute2 + " may not be equal.";
    }

    /** {@inheritDoc} */
    @Override
    public void notifyCreated(final XsdTreeNode node)
    {
        if (node.isType(this.path))
        {
            node.addAttributeValidator(this.attribute1, AttributesNotEqualValidator.this);
            node.addAttributeValidator(this.attribute2, AttributesNotEqualValidator.this);
        }
    }

}
