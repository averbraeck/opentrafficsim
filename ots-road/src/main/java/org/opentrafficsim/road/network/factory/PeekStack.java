package org.opentrafficsim.road.network.factory;

import java.util.ArrayList;

/**
 * Stack object that allows easy verification of the values of the last few entries.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @param <T> type of the objects stored on this PeekStack
 */
public class PeekStack<T> extends ArrayList<T>
{
    /** */
    private static final long serialVersionUID = 20150624L;

    /**
     * Constructor.
     */
    public PeekStack()
    {
        //
    }

    /**
     * Push an element on this stack.
     * @param element the element to push onto this stack
     */
    public final void push(final T element)
    {
        add(element);
    }

    /**
     * Pop an element off this stack.
     * @return the element that was popped of this stack
     */
    public final T pop()
    {
        int index = this.size() - 1;
        T result = get(index);
        remove(index);
        return result;
    }

    /**
     * Return the N-from-last element of this PeekStack.
     * @param offset if 0 the last pushed, but not yet popped element is returned; if offset is 1, the before last pushed, but
     *            not yet popped element is returned; etc.
     * @return the addressed element
     */
    public final T peekNthLast(final int offset)
    {
        return get(this.size() - 1 - offset);
    }

    /**
     * Check if the elements on the top of this PeekStack are equal to the provided entries.
     * @param entries the provided entries
     * @return true if this PeekStack contains, at the top, the provided entries in the specified order; false otherwise
     */
    public final boolean topEntriesEqual(@SuppressWarnings("unchecked") final T... entries)
    {
        int mySize = size();
        if (entries.length > mySize)
        {
            return false;
        }
        for (int index = 0; index < entries.length; index++)
        {
            if (!peekNthLast(index).equals(entries[index]))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public final String toString()
    {
        return "PeekStack [size=" + size() + "]";
    }

}
