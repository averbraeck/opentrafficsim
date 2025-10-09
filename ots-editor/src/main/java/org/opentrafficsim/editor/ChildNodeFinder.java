package org.opentrafficsim.editor;

import java.util.NoSuchElementException;

/**
 * Checks whether a child node is present, and if so can return it, in an efficient manner.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ChildNodeFinder
{

    /** Parent. */
    private final XsdTreeNode parent;

    /** Child. */
    private XsdTreeNode child;

    /**
     * Constructor.
     * @param parent parent node
     */
    public ChildNodeFinder(final XsdTreeNode parent)
    {
        this.parent = parent;
    }

    /**
     * Whether the node has given child.
     * @param name child name
     * @return whether the node has given child
     */
    public boolean hasChild(final String name)
    {
        try
        {
            this.child = this.parent.getFirstChild(name);
            return true;
        }
        catch (NoSuchElementException ex)
        {
            return false;
        }
    }

    /**
     * Whether the node has given child which is also active.
     * @param name child name
     * @return whether the node has given child which is also active
     */
    public boolean hasActiveChild(final String name)
    {
        return hasChild(name) && this.child.isActive();
    }

    /**
     * Returns the previously found node.
     * @return previously found node
     */
    public XsdTreeNode get()
    {
        return this.child;
    }

}
