package org.opentrafficsim.editor.decoration.string;

import java.rmi.RemoteException;
import java.util.function.Function;
import java.util.function.Predicate;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.decoration.AbstractNodeDecorator;

/**
 * General implementation of a string function.
 * @author wjschakel
 */
public abstract class AbstractStringFunction extends AbstractNodeDecorator
{

    /** */
    private static final long serialVersionUID = 20230910L;

    /** Predicate to accept nodes that should have this string function. */
    private final Predicate<XsdTreeNode> predicate;
    
    /** Overwrite existing string functions. */
    protected boolean overwrite = true;

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     * @param predicate Predicate&lt;XsdTreeNode&gt;; predicate to accept nodes that should have this string function.
     * @throws RemoteException if an exception occurs while adding as a listener.
     */
    public AbstractStringFunction(final OtsEditor editor, final Predicate<XsdTreeNode> predicate) throws RemoteException
    {
        super(editor);
        this.predicate = predicate;
    }

    /** {@inheritDoc} */
    @Override
    public void notifyCreated(final XsdTreeNode node)
    {
        if (AbstractStringFunction.this.predicate.test(node))
        {
            node.setStringFunction(getStringFunction(), this.overwrite);
        }
    }

    /**
     * Returns the string function that produces the right string from the contents of a node.
     * @return string function that produces the right string from the contents of a node.
     */
    public abstract Function<XsdTreeNode, String> getStringFunction();

}
