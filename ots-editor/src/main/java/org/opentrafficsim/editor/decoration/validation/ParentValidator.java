package org.opentrafficsim.editor.decoration.validation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.decoration.AbstractNodeDecoratorRemove;

/**
 * Validates that the Parent attribute of a node does not refer to self, either directly or indirectly.
 * @author wjschakel
 */
public class ParentValidator extends AbstractNodeDecoratorRemove implements ValueValidator
{

    /** */
    private static final long serialVersionUID = 20230319L;

    /** Path string for the nodes to validate. */
    private final String path;

    /** All nodes created under the path. */
    private Set<XsdTreeNode> nodes = new LinkedHashSet<>();

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     * @param path String; path of the nodes that have a Parent attribute referring to another node under the same path.
     * @throws RemoteException if an exception occurs while adding as a listener.
     */
    public ParentValidator(final OtsEditor editor, final String path) throws RemoteException
    {
        super(editor);
        this.path = path;
    }

    /** {@inheritDoc} */
    @Override
    public String validate(final XsdTreeNode node)
    {
        String value = node.getAttributeValue("Parent");
        if (value == null || value.isBlank() || !node.isActive())
        {
            return null;
        }
        List<XsdTreeNode> list = new ArrayList<>();
        list.add(node);
        return validateParent(value, list);
    }

    /**
     * Finds the parent node, checks whether it is referring to an exiting node in the list, and moves to the next parent.
     * @param parentId String; id of next parent node to find.
     * @param nodeList List&lt;XsdTreeNode&gt;; list of nodes so far, every next node is the parent of the previous.
     * @return String; message if the parent refers to self, directly or indirectly, or {@code null} otherwise.
     */
    private String validateParent(final String parentId, final List<XsdTreeNode> nodeList)
    {
        for (XsdTreeNode otherNode : this.nodes)
        {
            if (otherNode.isActive() && parentId.equals(otherNode.getId()))
            {
                int index = nodeList.indexOf(otherNode);
                if (index == 0)
                {
                    StringBuilder str = new StringBuilder("Parent refers to self: ");
                    String separator = "";
                    for (XsdTreeNode node : nodeList)
                    {
                        str.append(separator).append(node.getId());
                        separator = " > ";
                    }
                    str.append(separator).append(otherNode.getId());
                    return str.toString();
                }
                else if (index > 0)
                {
                    return null; // A > B > C > C > C ... report at C, not at A
                }
                String value = otherNode.getAttributeValue("Parent");
                if (value == null || value.isBlank())
                {
                    return null;
                }
                nodeList.add(otherNode);
                return validateParent(value, nodeList);
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void notifyCreated(final XsdTreeNode node)
    {
        if (node.isType(ParentValidator.this.path))
        {
            node.addAttributeValidator("Parent", ParentValidator.this);
            ParentValidator.this.nodes.add(node);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notifyRemoved(final XsdTreeNode node)
    {
        if (node.isType(ParentValidator.this.path))
        {
            ParentValidator.this.nodes.remove(node);
        }
    }

}
