package org.opentrafficsim.base;

import org.djutils.exceptions.Throw;

/**
 * Super class for types with hierarchical structure. Upper level types without parent can be created in sub classes using a
 * protected constructor without parent.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> Self-reference to type.
 */
public abstract class HierarchicalType<T extends HierarchicalType<T>> extends Type<T> implements Identifiable
{

    /** The id of the type to make it identifiable. */
    private final String id;

    /** Parent type. */
    private final T parent;

    /** Cached hash code. */
    private int hashCode;

    /**
     * Constructor for creating the top level types in subclasses.
     * @param id String; The id of the type to make it identifiable.
     * @throws NullPointerException if the id is null
     */
    protected HierarchicalType(final String id) throws NullPointerException
    {
        Throw.whenNull(id, "id cannot be null for hierarchical types");
        this.id = id;
        this.parent = null;
    }

    /**
     * Constructor.
     * @param id String; The id of the type to make it identifiable.
     * @param parent T; parent type
     * @throws NullPointerException if the id is null
     */
    public HierarchicalType(final String id, final T parent) throws NullPointerException
    {
        Throw.whenNull(id, "id cannot be null for hierarchical types");
        this.id = id;
        this.parent = parent;
    }

    /** {@inheritDoc} */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /**
     * @return parent or {@code null} if this is a top level type.
     */
    public final T getParent()
    {
        return this.parent;
    }

    /**
     * Whether this, or any of the parent types, equals the given type.
     * @param type T; type
     * @return whether this, or any of the parent types, equals the given type
     */
    public final boolean isOfType(final T type)
    {
        if (this.equals(type))
        {
            return true;
        }
        if (this.parent != null)
        {
            return this.parent.isOfType(type);
        }
        return false;
    }

    /**
     * Returns the common ancestor of both types.
     * @param type T; other type.
     * @return common ancestor of both types, {@code null} if none
     */
    public final T commonAncestor(final T type)
    {
        T otherType = type;
        while (otherType != null && !isOfType(otherType))
        {
            otherType = otherType.getParent();
        }
        return otherType;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public int hashCode()
    {
        if (this.hashCode == 0)
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
            this.hashCode = prime * result + ((this.parent == null) ? 0 : this.parent.hashCode());
        }
        return this.hashCode;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        HierarchicalType<?> other = (HierarchicalType<?>) obj;
        if (this.id == null)
        {
            if (other.id != null)
            {
                return false;
            }
        }
        else if (!this.id.equals(other.id))
        {
            return false;
        }
        if (this.parent == null)
        {
            if (other.parent != null)
            {
                return false;
            }
        }
        else if (!this.parent.equals(other.parent))
        {
            return false;
        }
        return true;
    }

}
