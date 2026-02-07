package org.opentrafficsim.editor.decoration.string;

import java.util.function.Function;
import java.util.function.Predicate;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.decoration.AbstractNodeDecorator;

/**
 * General implementation of a string function.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class AbstractStringFunction extends AbstractNodeDecorator
{

    /** Overwrite existing string functions. */
    private boolean overwrite = true;

    /** Cached string function, to prevented repeated creation in getStringFunction(). */
    private Function<XsdTreeNode, String> stringFunction;

    /**
     * Constructor.
     * @param editor editor.
     * @param predicate predicate to accept nodes that should have this decorator.
     */
    public AbstractStringFunction(final OtsEditor editor, final Predicate<XsdTreeNode> predicate)
    {
        super(editor, predicate);
    }

    /**
     * Sets whether this function should overwrite an existing string function in the node. This is {@code true} by default.
     * @param overwrite whether this function should overwrite an existing string function in the node
     */
    protected void setOverwrite(final boolean overwrite)
    {
        this.overwrite = overwrite;
    }

    @Override
    public void notifyCreated(final XsdTreeNode node)
    {
        if (this.stringFunction == null)
        {
            this.stringFunction = getStringFunction();
        }
        node.setStringFunction(this.stringFunction, this.overwrite);
    }

    /**
     * Returns the string function that produces the right string from the contents of a node. It should be cached by the
     * caller.
     * @return string function that produces the right string from the contents of a node.
     */
    public abstract Function<XsdTreeNode, String> getStringFunction();

}
