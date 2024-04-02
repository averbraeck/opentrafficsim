package org.opentrafficsim.editor.decoration.string;

import java.util.function.Function;
import java.util.function.Predicate;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.decoration.AbstractNodeDecorator;

/**
 * General implementation of a string function.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class AbstractStringFunction extends AbstractNodeDecorator
{

    /** */
    private static final long serialVersionUID = 20230910L;

    /** Predicate to accept nodes that should have this string function. */
    private final Predicate<XsdTreeNode> predicate;

    /** Overwrite existing string functions. */
    protected boolean overwrite = true;

    /** Cached string function, to prevented repeated creation in getStringFunction(). */
    private Function<XsdTreeNode, String> stringFunction;

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     * @param predicate Predicate&lt;XsdTreeNode&gt;; predicate to accept nodes that should have this string function.
     */
    public AbstractStringFunction(final OtsEditor editor, final Predicate<XsdTreeNode> predicate)
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
            if (this.stringFunction == null)
            {
                this.stringFunction = getStringFunction();
            }
            node.setStringFunction(this.stringFunction, this.overwrite);
        }
    }

    /**
     * Returns the string function that produces the right string from the contents of a node.
     * @return string function that produces the right string from the contents of a node.
     */
    public abstract Function<XsdTreeNode, String> getStringFunction();

}
