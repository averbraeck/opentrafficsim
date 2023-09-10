package org.opentrafficsim.editor.decoration.validation;

import java.rmi.RemoteException;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.decoration.AbstractNodeDecorator;

/**
 * Validates that the start node and end node of a link are not the same.
 * @author wjschakel
 */
public class StartEndNodeValidator extends AbstractNodeDecorator implements ValueValidator
{

    /** */
    private static final long serialVersionUID = 20230910L;

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     * @throws RemoteException if an exception occurs while adding as a listener.
     */
    public StartEndNodeValidator(final OtsEditor editor) throws RemoteException
    {
        super(editor);
    }
    
    /** {@inheritDoc} */
    @Override
    public String validate(final XsdTreeNode node)
    {
        if (!node.isActive())
        {
            return null;
        }
        String startNode = node.getAttributeValue("NodeStart");
        if (startNode == null || startNode.isBlank())
        {
            return null;
        }
        String endNode = node.getAttributeValue("NodeEnd");
        if (endNode == null || endNode.isBlank() || !endNode.equals(startNode))
        {
            return null;
        }
        return "Start node and end node may not be equal.";
    }

    /** {@inheritDoc} */
    @Override
    public void notifyCreated(final XsdTreeNode node)
    {
        if (node.isType("Ots.Network.Link"))
        {
            node.addAttributeValidator("NodeStart", StartEndNodeValidator.this);
            node.addAttributeValidator("NodeEnd", StartEndNodeValidator.this);
        }
    }

}
