package org.opentrafficsim.base.collections;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Set implementation that is tailored towards almost always containing only 0, 1 or 2 elements, but sometimes more or possibly
 * much more. It does so by directly storing pointers to 0, 1 or 2 elements, or reverting to an internal collection otherwise.
 * This prevents pointer chasing for small sets.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 * @param <T> type of elements
 */
public abstract class SmallSet<T> extends AbstractSet<T>
{

    /** First element. */
    private T first;

    /** Second element. */
    private T second;

    /** Collection in case of 3 or more elements. */
    private Collection<T> many;

    @Override
    public int size()
    {
        if (this.many != null)
        {
            return this.many.size();
        }
        if (this.first == null)
        {
            return 0;
        }
        return this.second == null ? 1 : 2;
    }

    @Override
    public boolean isEmpty()
    {
        return this.first == null && this.many == null;
    }

    @Override
    public boolean contains(final Object o)
    {
        if (o == null)
        {
            return false;
        }
        if (this.many != null)
        {
            return this.many.contains(o);
        }
        return Objects.equals(this.first, o) || Objects.equals(this.second, o);
    }

    @Override
    public boolean add(final T element)
    {
        Objects.requireNonNull(element);
        if (this.many != null)
        {
            return this.many.add(element);
        }
        if (this.first == null)
        {
            this.first = element;
            return true;
        }
        if (element.equals(this.first))
        {
            return false;
        }
        if (this.second == null)
        {
            this.second = element;
            return true;
        }
        if (element.equals(this.second))
        {
            return false;
        }
        this.many = getEmptyCollection();
        this.many.add(this.first);
        this.many.add(this.second);
        this.first = null;
        this.second = null;
        return this.many.add(element);
    }

    @Override
    public boolean remove(final Object o)
    {
        if (o == null)
        {
            return false;
        }
        if (this.many != null)
        {
            boolean removed = this.many.remove(o);
            if (!removed)
            {
                return false;
            }
            int size = this.many.size();
            if (size == 2)
            {
                Iterator<T> it = this.many.iterator();
                this.first = it.next();
                this.second = it.next();
                this.many = null;
            }
            else if (size == 1)
            {
                Iterator<T> it = this.many.iterator();
                this.first = it.next();
                this.second = null;
                this.many = null;
            }
            else if (size == 0)
            {
                this.first = null;
                this.second = null;
                this.many = null;
            }
            return true;
        }
        if (Objects.equals(this.first, o))
        {
            this.first = this.second;
            this.second = null;
            return true;
        }
        if (Objects.equals(this.second, o))
        {
            this.second = null;
            return true;
        }
        return false;
    }

    @Override
    public void clear()
    {
        this.first = null;
        this.second = null;
        this.many = null;
    }

    @Override
    public Iterator<T> iterator()
    {
        if (this.many != null)
        {
            return this.many.iterator();
        }
        if (this.first == null)
        {
            return Collections.emptyIterator();
        }
        // To circumvent concurrent modification issues in the one/two-element case a snapshot with both references is required
        return new Iterator<>()
        {
            private T f = SmallSet.this.first;

            private T s = SmallSet.this.second;

            @Override
            public boolean hasNext()
            {
                return this.f != null || this.s != null;
            }

            @Override
            public T next()
            {
                if (this.f != null)
                {
                    T t = this.f;
                    this.f = null;
                    return t;
                }
                if (this.s != null)
                {
                    T t = this.s;
                    this.s = null;
                    return t;
                }
                throw new NoSuchElementException();
            }
        };
    }

    /**
     * Returns an implementation specific collection instance for the case of 3 or more elements.
     * @return an implementation specific collection instance for the case of 3 or more elements
     */
    protected abstract Collection<T> getEmptyCollection();

}
