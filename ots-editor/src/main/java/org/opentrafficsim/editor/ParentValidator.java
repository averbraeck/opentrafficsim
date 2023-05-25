package org.opentrafficsim.editor;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.djutils.event.Event;
import org.djutils.event.EventListener;

/**
 * Validates that the PARENT attribute of a node does not refer to self, either directly or indirectly.
 * @author wjschakel
 */
public class ParentValidator implements ValueValidator, EventListener
{

    /** */
    private static final long serialVersionUID = 20230319L;

    /** Path string for the nodes to validate. */
    private final String path;

    /** All nodes created under the path. */
    private Set<XsdTreeNode> nodes = new LinkedHashSet<>();

    /**
     * Constructor.
     * @param path String; path of the nodes that have a Parent attribute referring to another node under the same path.
     */
    public ParentValidator(final String path)
    {
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
    public void notify(final Event event) throws RemoteException
    {
        XsdTreeNodeRoot root = (XsdTreeNodeRoot) event.getContent();
        EventListener listener = new EventListener()
        {
            /** */
            private static final long serialVersionUID = 20230319L;

            /** {@inheritDoc} */
            @Override
            public void notify(final Event event) throws RemoteException
            {
                if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED))
                {
                    XsdTreeNode node = (XsdTreeNode) event.getContent();
                    if (node.getPathString().equals(ParentValidator.this.path))
                    {
                        node.addAttributeValidator("Parent", ParentValidator.this);
                        ParentValidator.this.nodes.add(node);
                    }
                }
                if (event.getType().equals(XsdTreeNodeRoot.NODE_REMOVED))
                {
                    XsdTreeNode node = (XsdTreeNode) event.getContent();
                    if (node.getPathString().equals(ParentValidator.this.path))
                    {
                        ParentValidator.this.nodes.remove(node);
                    }
                }
            }
        };
        root.addListener(listener, XsdTreeNodeRoot.NODE_CREATED);
        root.addListener(listener, XsdTreeNodeRoot.NODE_REMOVED);
    }

}
