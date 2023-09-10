package org.opentrafficsim.editor.decoration;

import java.rmi.RemoteException;
import java.util.function.Function;
import java.util.function.Predicate;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;

/**
 * General implementation of a string function.
 * @author wjschakel
 */
public abstract class AbstractStringFunction implements EventListener
{

    /** */
    private static final long serialVersionUID = 202309010L;

    /** Predicate to accept nodes that should have this string function. */
    private final Predicate<XsdTreeNode> predicate;

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     * @param predicate Predicate&lt;XsdTreeNode&gt;; predicate to accept nodes that should have this string function.
     * @throws RemoteException if an exception occurs while listening for new nodes.
     */
    public AbstractStringFunction(final OtsEditor editor, final Predicate<XsdTreeNode> predicate) throws RemoteException
    {
        editor.addListener(this, OtsEditor.NEW_FILE);
        this.predicate = predicate;
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        XsdTreeNodeRoot root = (XsdTreeNodeRoot) event.getContent();
        root.addListener(new EventListener()
        {
            /** */
            private static final long serialVersionUID = 20230301L;

            /** {@inheritDoc} */
            @Override
            public void notify(final Event event) throws RemoteException
            {
                XsdTreeNode node = (XsdTreeNode) event.getContent();
                if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED)
                        && AbstractStringFunction.this.predicate.test(node))
                {
                    node.setStringFunction(getStringFunction());
                }
            }
        }, XsdTreeNodeRoot.NODE_CREATED);
    }

    /**
     * Returns the string function that produces the right string from the contents of a node.
     * @return string function that produces the right string from the contents of a node.
     */
    public abstract Function<XsdTreeNode, String> getStringFunction();
    
}
